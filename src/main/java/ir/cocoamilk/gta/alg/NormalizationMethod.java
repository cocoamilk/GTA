package ir.cocoamilk.gta.alg;

public enum NormalizationMethod {
	NONE(0, "none (prescaled)", new GeneExpressionNormalizerDoNothing()), LINEAR_LOWER(1, "linear/lower",
//			new GeneExpressionNormalizerLinearLower()), LINEAR_UPPER(2, "linear/upper",
//					new GeneExpressionNormalizerLinearUpper()), RANK_LOWER(3, "rank/lower",
//							new GeneExpressionNormalizerRankLower()), RANK_UPPER(4, "rank/upper",
//									new GeneExpressionNormalizerRankUpper()), LLR(5, "LLR",
											new GeneExpressionNormalizerLLR());

	private String displayString;
//	private int id;
	private GeneExpressionNormalizer normalizer;

	NormalizationMethod(int id, final String displayString, GeneExpressionNormalizer normalizer) {
//		this.id = id;
		this.displayString = displayString;
//		this.normalizer = normalizer;
		//TODO correct it
		this.normalizer = new GeneExpressionNormalizerLLR();
	}

	public String getDisplayString() {
		return displayString;
	}

	static public NormalizationMethod getEnumValue(final String displayString) {
		for (final NormalizationMethod method : NormalizationMethod.values()) {
			if (method.getDisplayString().equals(displayString))
				return method;
		}

		throw new IllegalStateException("unknown string representation: \"" + displayString + "\"!");
	}

	public Pair<VectorDouble, VectorDouble> apply(VectorDouble normalExpression, VectorDouble cancerExpression) {
		Pair<VectorDouble, VectorDouble> normalizedExpressions = normalizer.normalize(normalExpression, cancerExpression);
		return normalizedExpressions;
	}
	
}
