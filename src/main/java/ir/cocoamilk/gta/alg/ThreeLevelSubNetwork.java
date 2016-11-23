package ir.cocoamilk.gta.alg;

import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyNode;

public class ThreeLevelSubNetwork {

	private CyNode parent, levelTwo;
	private List<CyNode> levelThree;

	public List<CyNode> getLevelThree() {
		return levelThree;
	}
	public void setLevelThree(List<CyNode> levelThree) {
		this.levelThree = levelThree;
	}
	public CyNode getParent() {
		return parent;
	}
	public void setParent(CyNode parent) {
		this.parent = parent;
	}
	public CyNode getLevelTwo() {
		return levelTwo;
	}
	public void setLevelTwo(CyNode levelTwo) {
		this.levelTwo = levelTwo;
	}
	public ThreeLevelSubNetwork(CyNode parent, CyNode levelTwo, List<CyNode> levelThree) {
		this.parent = parent;
		this.levelTwo = levelTwo;
		this.levelThree = levelThree;
	}
	
	public double localInformation(double[] tScore, Map<Long, Integer> nodeIdToIndex, double alpha, int level) {
		double localInfo = 0;
		if (level == 0)
			localInfo = tScore[nodeIdToIndex.get(levelTwo.getSUID())];
		if (level == 1) {
			for (int i=0; i<levelThree.size(); i++)
				localInfo += tScore[nodeIdToIndex.get(levelThree.get(i).getSUID())];
		}
		return localInfo * alpha;
	}
	

}
