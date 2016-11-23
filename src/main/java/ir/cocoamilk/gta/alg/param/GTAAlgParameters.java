package ir.cocoamilk.gta.alg.param;

import org.cytoscape.model.CyNetwork;

public class GTAAlgParameters {

	private GTAAlgParametersAttrSelection cancerSelParam;
	private GTAAlgParametersAttrSelection normalSelParam;
	private CyNetwork cyNetwork;
	private boolean generateTScores;
	private boolean generateDegrees;
	private int subnetSize;
	private int resultingNetworkCount;
	public static final int SUBNETWORK_MAX_SIZE_MAX = 20;
	public static final int SUBNETWORK_MAX_SIZE_DEFAULT = 10;
	public static final int NUMBER_OF_GENERATED_NETWORKS_MAX = 100;
	public static final int NUMBER_OF_GENERATED_NETWORKS_DEFAULT = 10;
	
	//
	public static final int RESULTING_NETWORK_MAX_TRY_IF_NO_ANSWER = 10;


	public GTAAlgParameters(CyNetwork cyNetwork, GTAAlgParametersAttrSelection normalSelParam,
			GTAAlgParametersAttrSelection cancerSelParam, boolean generateTScores, boolean generateDegrees, int subnetSize, int resultingNetworkCount) {
		this.setCyNetwork(cyNetwork);
		this.normalSelParam = normalSelParam;
		this.cancerSelParam = cancerSelParam;
		this.generateTScores = generateTScores;
		this.generateDegrees = generateDegrees;
		this.subnetSize = subnetSize;
		this.resultingNetworkCount = resultingNetworkCount;
	}

	public GTAAlgParametersAttrSelection getCancerSelParam() {
		return cancerSelParam;
	}

	public void setCancerSelParam(GTAAlgParametersAttrSelection cancerSelParam) {
		this.cancerSelParam = cancerSelParam;
	}

	public GTAAlgParametersAttrSelection getNormalSelParam() {
		return normalSelParam;
	}

	public void setNormalSelParam(GTAAlgParametersAttrSelection normalSelParam) {
		this.normalSelParam = normalSelParam;
	}

	public CyNetwork getCyNetwork() {
		return cyNetwork;
	}

	public void setCyNetwork(CyNetwork cyNetwork) {
		this.cyNetwork = cyNetwork;
	}

	public boolean isGenerateTScores() {
		return generateTScores;
	}

	public void setGenerateTScores(boolean generateTScores) {
		this.generateTScores = generateTScores;
	}

	public boolean isGenerateDegrees() {
		return generateDegrees;
	}

	public void setGenerateDegrees(boolean generateDegrees) {
		this.generateDegrees = generateDegrees;
	}

	public int getSubnetSize() {
		return subnetSize;
	}

	public void setSubnetSize(int subnetSize) {
		this.subnetSize = subnetSize;
	}

	public int getResultingNetworkCount() {
		return resultingNetworkCount;
	}

	public void setResultingNetworkCount(int resultingNetworkCount) {
		this.resultingNetworkCount = resultingNetworkCount;
	}
	
	

}
