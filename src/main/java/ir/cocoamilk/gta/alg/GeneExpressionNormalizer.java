package ir.cocoamilk.gta.alg;

public interface GeneExpressionNormalizer {

	Pair<VectorDouble, VectorDouble> normalize(VectorDouble normalExpression,
			VectorDouble cancerExpression);

}
