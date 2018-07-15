package mecs.iot.proj.om2m.asn;

import java.util.Random;

public class Physics {
	
	static Random rnd;
	
	public static double randomUniformFluctuation (double deviation) {
		return 1+deviation*(Math.random()-0.5);
	}
	
	public static double randomGaussianFluctuation (double deviation) {
		if (rnd==null)
			rnd = new Random();
		return 1+deviation*rnd.nextGaussian();
	}

}
