package mecs.iot.proj.om2m.structures;

public class Rule {
	
	public double[] coefficients;
	public Token token;
	public double threshold;
	
	Rule(double[] coefficients, Token token, double threshold) {
		this.coefficients = coefficients;
		this.token = token;
		this.threshold = threshold;
	}
	
	@Override
	
	public String toString() {
		String str = "Coefficients: ";
		for (int i=0; i<coefficients.length; i++) {
			if (i<coefficients.length-1)
				str += "w[" + i + "]=" + Double.toString(coefficients[i]) + ", ";
			else
				str += "w[" + i + "]=" + Double.toString(coefficients[i]) + ". ";
		}
		str += "Token: " + token + ". ";
		str += "Threshold: " + Double.toString(threshold) + ". ";
		return str;
	}

}
