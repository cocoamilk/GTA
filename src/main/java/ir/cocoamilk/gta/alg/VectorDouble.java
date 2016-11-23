package ir.cocoamilk.gta.alg;

public class VectorDouble {
	
	private double[] values;
	
	public VectorDouble(double[] values) {
		this.values = values;
	}

	public VectorDouble(int size) {
		this.values = new double[size];
	}

	public double[] getValues() {
		return values;
	}

	public void setValues(double[] values) {
		this.values = values;
	}
	
	public double sum() {
		double _sum = 0;
		for (double v: values) {
			_sum += v;
		}
		return _sum;
	}
	
	public double mean() {
		return sum() / values.length;
	}
	
	public double var() {
		double _var = 0, _m = mean();
		for (double v: values) {
			_var += (v - _m) * (v - _m);
		}
		return _var;
	}
	
	public double std() {
		return Math.sqrt(var());
	}
	

	public void divEq(double b) {
		for (int i=0; i<values.length; i++) {
			values[i] /= b;
		}
	}
	
	public void divEq(VectorDouble b) {
		if (values.length != b.values.length)
			throw new RuntimeException("Invalid Double Vector Sizes");
		for (int i=0; i<values.length; i++) {
			values[i] /= b.values[i];
		}
	}

	public VectorDouble div(VectorDouble b) {
		VectorDouble r = new VectorDouble(values.clone());
		r.divEq(b);
		return r;
	}
	
	public VectorDouble mul(VectorDouble b) {
		if (values.length != b.values.length)
			throw new RuntimeException("Invalid Double Vector Sizes");
		double[] r = new double[values.length];
		for (int i=0; i<r.length; i++) {
			r[i] = values[i] * b.values[i];
		}
		return new VectorDouble(r);
	}
	
	public VectorDouble mul(double m) {
		double[] r = new double[values.length];
		for (int i=0; i<r.length; i++) {
			r[i] = values[i] * m;
		}
		return new VectorDouble(r);
	}
	
	public void addEq(double b) {
		for (int i=0; i<values.length; i++) {
			values[i] += b;
		}
	}

	public void addEq(VectorDouble b) {
		if (values.length != b.values.length)
			throw new RuntimeException("Invalid Double Vector Sizes");
		for (int i=0; i<values.length; i++) {
			values[i] += b.values[i];
		}
	}

	public VectorDouble add(VectorDouble b) {
		VectorDouble r = new VectorDouble(this.values.clone());
		r.addEq(b);
		return r;
	}

	public VectorDouble sub(VectorDouble b) {
		if (values.length != b.values.length)
			throw new RuntimeException("Invalid Double Vector Sizes");
		double[] r = new double[values.length];
		for (int i=0; i<r.length; i++) {
			r[i] = values[i] - b.values[i];
		}
		return new VectorDouble(r);
	}

	public int size() {
		return values.length;
	}

	public VectorDouble sqr() {
		double[] r = new double[values.length];
		for (int i=0; i<r.length; i++) {
			r[i] = values[i] * values[i];
		}
		return new VectorDouble(r);
	}

	public void log2Eq() {
		double log10to2 = Math.log10(2);
		for (int i=0; i<values.length; i++) {
			values[i] = Math.log10(values[i]) / log10to2;
		}
	}

	public void set(int i, double v) {
		values[i] = v;
	}
	
	public double get(int i) {
		return values[i];
	}

	public void pow2Eq() {
		for (int i=0; i<values.length; i++) {
			values[i] = Math.pow(2, values[i]);
		}
	}

}
