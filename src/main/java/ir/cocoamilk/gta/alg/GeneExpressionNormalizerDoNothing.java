package ir.cocoamilk.gta.alg;

public class GeneExpressionNormalizerDoNothing implements GeneExpressionNormalizer {

	@Override
	public Pair<VectorDouble, VectorDouble> normalize(VectorDouble normalExpression,
			VectorDouble cancerExpression) {
		
		return new Pair<VectorDouble, VectorDouble>(normalExpression, cancerExpression);
	}

}
