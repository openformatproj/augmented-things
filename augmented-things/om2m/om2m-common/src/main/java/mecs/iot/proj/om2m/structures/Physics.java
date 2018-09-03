package mecs.iot.proj.om2m.structures;

import java.util.Random;

public class Physics {
	
	static Random rnd;
	
	public static double randomUniformFluctuation (double relativeDeviation) {
		return 1+relativeDeviation*(Math.random()-0.5);
	}
	
	public static double randomGaussianFluctuation (double relativeDeviation) {
		if (rnd==null)
			rnd = new Random();
		return 1+relativeDeviation*rnd.nextGaussian();
	}
	
	public static double threshold (double value, Double min, Double max) {
		if (min!=null && value<=min.doubleValue())
			return min.doubleValue();
		else if (max!=null && value>=max.doubleValue())
			return max.doubleValue();
		else
			return value;
	}

}
