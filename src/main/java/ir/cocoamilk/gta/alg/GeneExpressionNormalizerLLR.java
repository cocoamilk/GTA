package ir.cocoamilk.gta.alg;

import org.apache.commons.math3.distribution.NormalDistribution;

public class GeneExpressionNormalizerLLR implements GeneExpressionNormalizer {

	@Override
	public Pair<VectorDouble, VectorDouble> normalize(VectorDouble normalExpression,
			VectorDouble cancerExpression) {
		
		VectorDouble normalExpr = normalExpression,
				cancerExpr = cancerExpression;
		
		normalExpr.pow2Eq();
		cancerExpr.pow2Eq();
		
		double normalMean = normalExpr.mean(),
				normalStd = normalExpr.std(),
				cancerMean = cancerExpr.mean(),
				cancerStd = cancerExpr.std();
		
		if (normalStd <= 0) {
			throw new RuntimeException("Variation in expression of normal genes is zero, please select more normal gene expressions.");
		}
		if (cancerStd <= 0)
			throw new RuntimeException("Variation in expression of cancerous genes is zero, please select more cancerous gene expressions.");
		
		VectorDouble yNormal = getY(normalExpr, normalMean, normalStd, cancerMean, cancerStd),
				yCancer = getY(cancerExpr, normalMean, normalStd, cancerMean, cancerStd);
		
		double mean = (yNormal.sum() + yCancer.sum()) / (yNormal.size() + yCancer.size());
		yNormal.addEq(mean);
		yCancer.addEq(mean);
		
		VectorDouble yNormal2 = yNormal.sqr(),
				yCancer2 = yCancer.sqr();
		double std = Math.sqrt((yNormal2.sum() + yCancer2.sum()) / (yNormal2.size() + yCancer2.size()));
		
		yNormal.divEq(std);
		yCancer.divEq(std);
		
		return new Pair<VectorDouble, VectorDouble>(yNormal, yCancer);
	}
	
	private VectorDouble getY(VectorDouble ge, double mu1, double sigma1, double mu2, double sigma2) {
		VectorDouble y1 = putOnDensity(ge, mu1, sigma1),
				y2 = putOnDensity(ge, mu2, sigma2);
		y1.divEq(y2);
		y1.log2Eq();
		return y1;
	}
	
	private VectorDouble putOnDensity(VectorDouble ge, double mu, double sigma) {
		NormalDistribution normalNormalDist = new NormalDistribution(mu, sigma);
		
		double[] values = ge.getValues();
		double[] y1 = new double[values.length];
		
		for (int i=0; i<y1.length; i++) {
			y1[i] = normalNormalDist.density(values[i]);
		}
		return new VectorDouble(y1);
		
	}

}
