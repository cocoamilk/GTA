package ir.cocoamilk.gta.alg;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Label;
import java.lang.reflect.Array;
import java.nio.channels.ShutdownChannelGroupException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.commons.math3.stat.inference.TestUtils;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ir.cocoamilk.gta.alg.param.GTAAlgParameters;
import ir.cocoamilk.gta.alg.param.GTAAlgParametersAttrSelection;
import ir.cocoamilk.gta.service.ServicesUtil;

public class GTAAlg extends AbstractTask {

	private static final Logger logger = LoggerFactory.getLogger(GTAAlg.class);

	private GTAAlgParameters params;
	// private int numberOfTargerNetwroks = 10;

	private int size;
	private long[] nodeIndexToId;
	private Map<Long, Integer> nodeIdToIndex;
	private VectorDouble[] normalExpressionSet, cancerExpressionSet;
	private double[] tScores;
	private int[] degrees;

	private boolean cancelled = false;

	// private int maxSubnetworkSize = 10;

	public GTAAlg(GTAAlgParameters params) {
		this.params = params;
	}

	private VectorDouble[] fillExpressionSets(CyNetwork net, GTAAlgParametersAttrSelection selParam) {
		final String primaryKeyColname = net.getDefaultNodeTable().getPrimaryKey().getName();

		List<CyRow> nodeRows = net.getDefaultNodeTable().getAllRows();
		size = nodeRows.size();

		VectorDouble[] eset = new VectorDouble[nodeRows.size()];
		nodeIdToIndex = new HashMap<Long, Integer>();
		nodeIndexToId = new long[nodeRows.size()];

		for (int i = 0; i < size; i++) {
			Long nodeId = nodeRows.get(i).get(primaryKeyColname, Long.class);
			nodeIndexToId[i] = nodeId;
			nodeIdToIndex.put(nodeId, i);

			logger.debug("Fill Expression: i=" + i + ", nodeId=" + nodeId + ", ");
			eset[i] = new VectorDouble(selParam.getSelectedNames().size());
			for (int j = 0; j < selParam.getSelectedNames().size(); j++) {
				Double d = nodeRows.get(i).get(selParam.getSelectedNames().get(j), Double.class);
				if (d != null)
					eset[i].set(j, d);

			}

		}

		return eset;
	}

	private void convertParameters() {
		CyNetwork net = params.getCyNetwork();

		VectorDouble[] normalExpressionSetRaw = fillExpressionSets(net, params.getNormalSelParam());
		VectorDouble[] cancerExpressionSetRaw = fillExpressionSets(net, params.getCancerSelParam());

		normalExpressionSet = new VectorDouble[normalExpressionSetRaw.length];
		cancerExpressionSet = new VectorDouble[cancerExpressionSetRaw.length];

		GeneExpressionNormalizerLLR llrNormalizer = new GeneExpressionNormalizerLLR();
		for (int i = 0; i < normalExpressionSetRaw.length; i++) {
			Pair<VectorDouble, VectorDouble> normalizedExpressions = llrNormalizer.normalize(normalExpressionSetRaw[i],
					cancerExpressionSetRaw[i]);
			normalExpressionSet[i] = normalizedExpressions.getA();
			cancerExpressionSet[i] = normalizedExpressions.getB();
		}

		logger.error("convertParameters: " + normalExpressionSet + ", " + cancerExpressionSet);

	}

	interface ArrayFillerAlgorithm<T> {
		void run(T[] a);
	}

	private <T> T[] loadOrComputeAndSave(Class<T> c, T defaultValue, String columnName, ArrayFillerAlgorithm<T> alg,
			boolean reGenerateIfExists) {
		@SuppressWarnings("unchecked")
		T[] r = (T[]) Array.newInstance(c, size);

		CyNetwork net = params.getCyNetwork();
		final String primaryKeyColname = net.getDefaultNodeTable().getPrimaryKey().getName();

		if (!reGenerateIfExists && net.getDefaultNodeTable().getColumn(columnName) != null) {
			logger.error("Loading " + columnName);

			for (CyRow row : net.getDefaultNodeTable().getAllRows()) {
				Long nodeId = row.get(primaryKeyColname, Long.class);
				T tScore = row.get(columnName, c);
				r[nodeIdToIndex.get(nodeId)] = tScore == null ? defaultValue : tScore;
			}

		} else {
			logger.error("Computing " + columnName);
			alg.run(r);
			//
			// for (int i=0; i<size; i++) {
			// double tValue = TestUtils.t(normalExpressionSet[i].getValues(),
			// cancerExpressionSet[i].getValues());
			// r[i] = Math.abs(tValue);
			// }

			if (net.getDefaultNodeTable().getColumn(columnName) == null)
				net.getDefaultNodeTable().createColumn(columnName, c, false, defaultValue);

			for (CyRow row : net.getDefaultNodeTable().getAllRows()) {
				Long nodeId = row.get(primaryKeyColname, Long.class);
				row.set(columnName, r[nodeIdToIndex.get(nodeId)]);
			}

		}
		return r;
	}

	private double[] computeAbsTScores() {
		// double[] r = new double[size];
		// CyNetwork net = params.getCyNetwork();
		// final String primaryKeyColname =
		// net.getDefaultNodeTable().getPrimaryKey().getName(),
		final String tScoreColumnName = "t-score";
		Double[] r = loadOrComputeAndSave(Double.class, 0.0, tScoreColumnName, new ArrayFillerAlgorithm<Double>() {
			@Override
			public void run(Double[] r) {
				for (int i = 0; i < size; i++) {
					double tValue = TestUtils.t(normalExpressionSet[i].getValues(), cancerExpressionSet[i].getValues());
					r[i] = Math.abs(tValue);
				}
			}
		}, params.isGenerateTScores());
		double[] rr = new double[r.length];
		for (int i = 0; i < r.length; i++)
			rr[i] = r[i];
		return rr;

		// if (net.getDefaultNodeTable().getColumn(tScoreColumnName) != null) {
		// logger.error("Loading t-scores");
		//
		// for (CyRow row : net.getDefaultNodeTable().getAllRows()) {
		// Long nodeId = row.get(primaryKeyColname, Long.class);
		// Double tScore = row.get(tScoreColumnName, Double.class);
		// r[nodeIdToIndex.get(nodeId)] = tScore == null ? 0 : tScore;
		// }
		//
		// } else {
		// logger.error("Computing t-scores");
		// for (int i=0; i<size; i++) {
		// double tValue = TestUtils.t(normalExpressionSet[i].getValues(),
		// cancerExpressionSet[i].getValues());
		// r[i] = Math.abs(tValue);
		// }
		//
		// net.getDefaultNodeTable().createColumn(tScoreColumnName,
		// Double.class, false, 0.0);
		//
		// for (CyRow row : net.getDefaultNodeTable().getAllRows()) {
		// Long nodeId = row.get(primaryKeyColname, Long.class);
		// row.set(tScoreColumnName, r[nodeIdToIndex.get(nodeId)]);
		// }
		//
		// }

	}

	private int[] computeDegrees() {

		final String degreeColumn = "gta-degree";
		Integer[] r = loadOrComputeAndSave(Integer.class, 0, degreeColumn, new ArrayFillerAlgorithm<Integer>() {
			@Override
			public void run(Integer[] r) {
				CyNetwork net = params.getCyNetwork();

				for (CyNode node : net.getNodeList()) {
					List<CyEdge> e = net.getAdjacentEdgeList(node, CyEdge.Type.ANY);
					r[nodeIdToIndex.get(node.getSUID())] = e.size();
				}
			}
		}, params.isGenerateDegrees());
		int[] rr = new int[r.length];
		for (int i = 0; i < r.length; i++)
			rr[i] = r[i];
		return rr;

		// CyNetwork net = params.getCyNetwork();
		// int[] r = new int[size];
		// for (CyNode node : net.getNodeList()) {
		// List<CyEdge> e = net.getAdjacentEdgeList(node, CyEdge.Type.ANY);
		// r[nodeIdToIndex.get(node.getSUID())] = e.size();
		// }
		// return r;
	}

	/**
	 * Larger T-Score first.
	 * 
	 * @return
	 */
	private Integer[] sortByTScoreAndFilterNans() {
		List<Integer> indicesList = new ArrayList<Integer>();
		for (int i = 0; i < size; i++)
			if (!Double.isNaN(tScores[i]))
				indicesList.add(i);
		Integer[] indices = indicesList.toArray(new Integer[0]);
		return sortByTScore(indices);
	}

	private Integer[] sortByTScore(Integer[] subIndices) {
		Integer[] indices = subIndices.clone();
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		Arrays.sort(indices, new Comparator<Integer>() {
//			private final double EPSILON = 1e-7;
			@Override
			public int compare(Integer a, Integer b) {
//				if (Math.abs(tScores[a] - tScores[b]) < EPSILON)
//					return 0;
				return tScores[a] < tScores[b] ? 1 : tScores[a] == tScores[b] ? 0 : -1;
			}
		});
		return indices;
	}

	// private Integer[] sortByDegree(Integer[] subIndices) {
	// Integer[] indices = subIndices.clone();
	// Arrays.sort(indices, new Comparator<Integer>() {
	// @Override
	// public int compare(Integer a, Integer b) {
	// return degrees[a] < degrees[b] ? 1 : degrees[a] == degrees[b] ? 0 : -1;
	// }
	// });
	// return indices;
	// }

	private List<ThreeLevelSubNetwork> getSubNetworksAroundAVertex(CyNetwork net, CyNode parent) {
		List<ThreeLevelSubNetwork> subNetworks = new ArrayList<ThreeLevelSubNetwork>();
		if (nodeIdToIndex.get(parent.getSUID()) == null)
			return subNetworks;

		List<CyNode> levelTwos = net.getNeighborList(parent, CyEdge.Type.ANY);
		Set<CyNode> levelOneSet = new HashSet<CyNode>(levelTwos);
		for (CyNode agent : levelTwos) {
			if (nodeIdToIndex.get(agent.getSUID()) == null)
				continue;
			List<CyNode> subNet = new ArrayList<CyNode>();

			List<CyNode> neighbors = net.getNeighborList(agent, CyEdge.Type.ANY);
			for (CyNode nei : neighbors) {
				if (!nei.equals(parent) && !levelOneSet.contains(nei) && nodeIdToIndex.get(nei.getSUID()) != null)
					subNet.add(nei);
			}
			
			//remote non-valid tScores
			List<CyNode> filteredSubNet = new ArrayList<CyNode>();
			for (CyNode n : subNet)
				if (!Double.isNaN(tScores[nodeIdToIndex.get(n.getSUID())]))
					filteredSubNet.add(n);
			
			subNet = filteredSubNet;

			subNet = filterNetworkByClusteringCoefficient(subNet);

			subNetworks.add(new ThreeLevelSubNetwork(parent, agent, subNet));
		}
		return subNetworks;
	}

	private Integer[] getSubNetworkIndices(List<CyNode> subNet) {
		Integer[] subNetIndices = new Integer[subNet.size()];
		int ii = 0;
		for (CyNode node : subNet) {
			subNetIndices[ii] = nodeIdToIndex.get(node.getSUID());
			ii++;
		}
		return subNetIndices;
	}

	public double clusteringCoefficient(List<CyNode> subNetwork) {
		logger.error(" CC ...");
		CyNetwork net = params.getCyNetwork();
		double total = 0.0;
		for (CyNode node : subNetwork) {
			List<CyNode> neis = net.getNeighborList(node, CyEdge.Type.ANY), subNei = new ArrayList<CyNode>();

			for (CyNode nei : neis) {
				if (subNetwork.contains(nei))
					subNei.add(nei);
			}

			int possible = subNei.size() * (subNei.size() - 1);
			int actual = 0;
			for (CyNode u : subNei) {
				for (CyNode v : subNei) {
					if (net.getConnectingEdgeList(u, v, CyEdge.Type.ANY).size() > 0)
						actual++;
				}
			}
			if (possible > 0) {
				total += 1.0 * actual / possible;
			}
		}
		logger.error(" CC done");
		return total / subNetwork.size();
	}

	private List<CyNode> filterNetworkByClusteringCoefficient(List<CyNode> subNet) {
		CyNetwork net = params.getCyNetwork();
		if (subNet.size() < params.getSubnetSize())
			return subNet;

		// TODO: removed for speed.
		// {
		// Integer[] originialSubnetworkIndices = getSubNetworkIndices(subNet);
		// List<CyNode> relaxedSubNetwork = new ArrayList<CyNode>(subNet);
		//
		//
		// Integer[] degreeSortedIndices =
		// sortByDegree(originialSubnetworkIndices);
		// double prevClusteringCoefficient =
		// clusteringCoefficient(relaxedSubNetwork);
		// for (int ii=degreeSortedIndices.length-1; ii>0; ii--) {
		// int i = degreeSortedIndices[ii];
		// CyNode iNode = net.getNode(nodeIndexToId[i]);
		//
		// relaxedSubNetwork.remove(iNode);
		// //We can compute it faster for all the networks together
		// double newCC = clusteringCoefficient(relaxedSubNetwork);
		// if (newCC > prevClusteringCoefficient) {
		// prevClusteringCoefficient = newCC;
		// } else {
		// relaxedSubNetwork.add(iNode);
		// break;
		// }
		// }
		// subNet = relaxedSubNetwork;
		// }

		// TODO: it is changed from matlab to get connected component with
		// maximum minimum tScore
		if (subNet.size() > params.getSubnetSize()) {
			Integer[] subnetworkIndices = getSubNetworkIndices(subNet);
			Integer[] sortedIndices = sortByTScore(subnetworkIndices);

			// for (int i=0; i<maxSubnetworkSize; i++) {
			// subNet.add(net.getNode(nodeIndexToId[sortedIndices[i]]));
			// }

			Set<CyNode> subnetworkNodes = new HashSet<CyNode>();

			for (Integer subnetworkIndex : subnetworkIndices) {
				subnetworkNodes.add(net.getNode(nodeIndexToId[subnetworkIndex]));
			}

			for (int i = 0; i < sortedIndices.length; i++) {
				Set<CyNode> dfsSubnet = new HashSet<CyNode>();
				findConnectedComponent(subnetworkNodes, net.getNode(nodeIndexToId[sortedIndices[0]]), dfsSubnet,
						params.getSubnetSize(), tScores[sortedIndices[i]]);
				logger.info("Filter subnet " + subnetworkNodes.size() + " by " + tScores[sortedIndices[i]]
						+ " result-size: " + dfsSubnet.size());
				subNet = new ArrayList<CyNode>(dfsSubnet);
				if (dfsSubnet.size() >= params.getSubnetSize()) {
					break;
				}
			}

		}

		return subNet;
	}

	private void findConnectedComponent(Set<CyNode> subnetworkToBeSearched, CyNode v, Set<CyNode> result, int maxSize,
			double minTScore) {
		if (result.size() >= maxSize)
			return;
		if (!result.contains(v)) {
			result.add(v);
		}
		for (CyNode u : params.getCyNetwork().getNeighborList(v, CyEdge.Type.ANY)) {
			if (!result.contains(u) && tScores[nodeIdToIndex.get(u.getSUID())] >= minTScore)
				findConnectedComponent(subnetworkToBeSearched, u, result, maxSize, minTScore);
		}
	}

	private static CyLayoutAlgorithm layoutAlgorithm = ServicesUtil.cyLayoutsServiceRef.getLayout("force-directed");

	private void computeNashEqulibriumSubnetworks(TaskMonitor taskMonitor, double progressStart, double progressEnd) {
		Object context = layoutAlgorithm.getDefaultLayoutContext();

		CyNetwork net = params.getCyNetwork();
		Integer[] sortedIndices = sortByTScoreAndFilterNans();
		int size = sortedIndices.length;
		int degSum = 0;
		for (int i = 0; i < size; i++)
			degSum += degrees[i];
		double degMean = degSum * 1.0 / size;
		int createdNets = 0;
		for (int ii = 0, tries = 0; ii < size && createdNets < params.getResultingNetworkCount()
				&& tries < params.getResultingNetworkCount() * GTAAlgParameters.RESULTING_NETWORK_MAX_TRY_IF_NO_ANSWER
				&& !cancelled; ii++) {
			taskMonitor.setStatusMessage("Computing nash equilibrium subnet " + (createdNets + 1) + "/" + params.getResultingNetworkCount() + " [" + tries +" tries]");
			taskMonitor.setProgress(createdNets * 1.0 / params.getResultingNetworkCount() * (progressEnd - progressStart) + progressStart);
			int i = sortedIndices[ii];
			if (degrees[i] >= degMean) {
				tries++;
				logger.info("Subnetwork for node " + i + " " + net.getNode(nodeIndexToId[i]));

				CyNode parent = net.getNode(nodeIndexToId[i]);
				List<ThreeLevelSubNetwork> subNetworks = getSubNetworksAroundAVertex(net, parent);

				List<CyNode> nashMergedSubnet = computeMergedNashSubNetworks(subNetworks);
				// //TODO: just for test:
				// List<CyNode> nashMergedSubnet = new ArrayList<CyNode>();
				// for (ThreeLevelSubNetwork subNet : subNetworks) {
				// nashMergedSubnet.addAll(subNet.getLevelThree());
				// nashMergedSubnet.add(subNet.getLevelTwo());
				// nashMergedSubnet.add(subNet.getParent());
				// }

				if (nashMergedSubnet.size() == 0)
					continue;

				CyRootNetwork rootNetwork = ServicesUtil.cyRootNetworkFactory.getRootNetwork(net);

				CySubNetwork subnet = rootNetwork.addSubNetwork();
				subnet.getRow(subnet).set(CyNetwork.NAME,
						"GTA_Module_" + net.getRow(net.getNode(nodeIndexToId[i])).get(CyNetwork.NAME, String.class));
				for (CyNode n : nashMergedSubnet) {
					subnet.addNode(n);
				}
				for (CyNode n : nashMergedSubnet)
					for (CyEdge e : net.getAdjacentEdgeList(n, CyEdge.Type.ANY))
						if (subnet.containsNode(e.getTarget()) && subnet.containsNode(e.getSource())
								&& !subnet.containsEdge(e))
							subnet.addEdge(e);

				ServicesUtil.cyNetworkManagerServiceRef.addNetwork(subnet);

//				ServicesUtil.cyEventHelperServiceRef.flushPayloadEvents();

				CyNetworkView view = ServicesUtil.cyNetworkViewFactoryServiceRef.createNetworkView(subnet);
				ServicesUtil.cyNetworkViewManagerServiceRef.addNetworkView(view);
				
				
//				ServicesUtil.cyEventHelperServiceRef.flushPayloadEvents();
				ServicesUtil.visualMappingManagerRef.getVisualStyle(view).apply(view);
				view.getNodeView(net.getNode(nodeIndexToId[i])).setVisualProperty(BasicVisualLexicon.NODE_FILL_COLOR, Color.RED);
				ServicesUtil.cyEventHelperServiceRef.flushPayloadEvents();
				
				// //TODO: just for test:
				// for (ThreeLevelSubNetwork subNet : subNetworks) {
				// view.getNodeView(subNet.getParent()).setVisualProperty(BasicVisualLexicon.NODE_FILL_COLOR,
				// Color.BLUE);
				// view.getNodeView(subNet.getLevelTwo()).setVisualProperty(BasicVisualLexicon.NODE_FILL_COLOR,
				// Color.GREEN);
				// }
				


				insertTasksAfterCurrentTask(
						layoutAlgorithm.createTaskIterator(view, context, CyLayoutAlgorithm.ALL_NODE_VIEWS, ""));
				// ServicesUtil.visualMappingManagerRef.setVisualStyle(moduleVS,
				// view);
				// view.updateView();

				createdNets++;
			}
		}
		if (createdNets == 0) {
			EventQueue.invokeLater(new Runnable() {
		        @Override
		        public void run() {
					JOptionPane.showMessageDialog(null, "No network could be created.", "No output", JOptionPane.WARNING_MESSAGE);
		        }
		    });
		}
	}

	private List<CyNode> computeMergedNashSubNetworks(List<ThreeLevelSubNetwork> subNetworks) {
		Set<CyNode> r = new HashSet<CyNode>();
		for (ThreeLevelSubNetwork subNet : subNetworks) {
			int n = 1 << (subNet.getLevelThree().size() + 1);
			double[][] payoffs = new double[n][subNet.getLevelThree().size() + 1 + 1];
			// List<CyNode> currentSubsubnet = new ArrayList<CyNode>();
			// computePayoffsRecursively(payoffs, subNet, currentSubsubnet, 0,
			// 0, 0);
			computePayoffs(payoffs, subNet);
			int state = computeMergedNashEquilibriums(payoffs, subNet);
			if (stateLength(state) >= 2) {
				r.add(subNet.getParent());
				if (inState(state, 0))
					r.add(subNet.getLevelTwo());
				for (int i = 1; i < subNet.getLevelThree().size() + 1; i++)
					if (inState(state, i))
						r.add(subNet.getLevelThree().get(i - 1));
			}
		}
		return new ArrayList<CyNode>(r);
	}

	private int stateLength(int state) {
		int l = 0;
		for (; state > 0; state /= 2)
			if (state % 2 == 1)
				l++;
		return l;
	}

	private int computeMergedNashEquilibriums(double[][] payoffs, ThreeLevelSubNetwork subNet) {
		int n = subNet.getLevelThree().size() + 1;
		int r = 0;
		for (int state = 0; state < payoffs.length; state++) {
			for (int j = 0; j < n; j++) {
				boolean equilibrium = true;

				// It is changed to be similar to Matlab code
				int state2 = exchangeState(state, j);
				if (payoffs[state2][j] > payoffs[state][j]) {
					equilibrium = false;
					break;
				}

				// for (int jj=0; jj<n; jj++) {
				// int state2 = exchangeState(state, jj);
				// if (jj != j && payoffs[state2][j] > payoffs[state][j]) {
				// equilibrium = false;
				// break;
				// }
				// }
				if (equilibrium == false)
					break;
				if (equilibrium)
					r |= (1 << j);
			}
		}
		return r;
	}

	private int exchangeState(int state, int l) {
		if (inState(state, l))
			return state - (1 << l);
		return state + (1 << l);
	}

	private boolean inState(int state, int l) {
		return (state / (1 << l)) % 2 == 1;
	}

	// private void computePayoffsRecursively(double[][] payoffs,
	// ThreeLevelSubNetwork subNet, List<CyNode> currentSubsubnet, int level,
	// int state) {
	private void computePayoffs(double[][] payoffs, ThreeLevelSubNetwork subNet) {
		// if (level >= subNet.getLevelThree().size() + 1) {
		int n = subNet.getLevelThree().size() + 1;
		for (int state = 0; state < (1 << n); state++) {
			double alpha1 = 1.2, alpha3 = 1;//, alpha2 = 1, C = 2;

			payoffs[state][0] = tScores[nodeIdToIndex.get(subNet.getLevelTwo().getSUID())]
					* (inState(state, 0) ? 1 : alpha1);
			int vC = 0;
			for (int i = 0; i < n; i++) {
				vC += inState(state, i) ? 1 : 0;
			}

			double lls = vC * 1.0 / n;

			for (int i = 1; i < subNet.getLevelThree().size() + 1; i++) {
				payoffs[state][i] = tScores[nodeIdToIndex.get(subNet.getLevelThree().get(i - 1).getSUID())]
						* (inState(state, i) ? 1 : alpha1);
				if (inState(state, i)) {
					payoffs[state][i] += subNet.localInformation(tScores, nodeIdToIndex, alpha1, 2) * alpha3;
					payoffs[state][i] += lls;
				}
			}
			// return;
		}
		// computePayoffsRecursively(payoffs, subNet, currentSubsubnet, level+1,
		// state);
		//
		// if (level >= 1)
		// currentSubsubnet.add(subNet.getLevelThree().get(level-1));
		// else
		// currentSubsubnet.add(subNet.getLevelTwo());
		// computePayoffsRecursively(payoffs, subNet, currentSubsubnet, level+1,
		// state + (1 << level));
		// currentSubsubnet.remove(currentSubsubnet.size()-1);
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setProgress(0.0);
		taskMonitor.setStatusMessage("Searching modules ....");

		System.gc();

		taskMonitor.setProgress(0.1);
		taskMonitor.setStatusMessage("Converting parameters ....");
		convertParameters();

		taskMonitor.setProgress(0.3);
		taskMonitor.setStatusMessage("Computing t-scores ....");
		tScores = computeAbsTScores();

		taskMonitor.setProgress(0.35);
		taskMonitor.setStatusMessage("Computing degrees ....");
		degrees = computeDegrees();

		taskMonitor.setProgress(0.4);
		taskMonitor.setStatusMessage("Computing nash subnetworks ....");
		computeNashEqulibriumSubnetworks(taskMonitor, 0.4, 1);

		// addTestNetworks();

		taskMonitor.setProgress(1.0);

	}

	// private void addTestNetworks() {
	// CyNetwork myNet =
	// ServicesUtil.cyNetworkFactoryServiceRef.createNetwork();
	// myNet.getRow(myNet).set(CyNetwork.NAME, "ALO");
	//
	// CyNode n1 = myNet.addNode();
	// CyNode n2 = myNet.addNode();
	//
	// myNet.getRow(n1).set(CyNetwork.NAME, "Node 1");
	// myNet.getRow(n2).set(CyNetwork.NAME, "Node 2");
	//
	// myNet.addEdge(n1, n2, true);
	// ServicesUtil.cyNetworkManagerServiceRef.addNetwork(myNet);
	// }

	@Override
	public void cancel() {
		super.cancel();
		cancelled = true;
	}

}
