package ir.cocoamilk.gta.alg.param;

public enum NetFilteringMethod {
	FILTER_BY_TSCORE("T-Score"),
	FILTER_BY_CLUSTERING_COEFFICIENT("Clustering Coefficient / T-Score");
	
	private String text;
	
	private NetFilteringMethod(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	public String toString() {
		return text;
	}
	
}
