package mecs.iot.proj.om2m.asn;

public class Physics {
	
	public static double randomFluctuation (double width) {
		return 1+width*2*(Math.random()-0.5);
	}

}
