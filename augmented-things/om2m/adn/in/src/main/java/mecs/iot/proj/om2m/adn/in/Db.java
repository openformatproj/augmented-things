package mecs.iot.proj.om2m.adn.in;

import mecs.iot.proj.om2m.structures.MN;
import mecs.iot.proj.om2m.structures.Pack;
import mecs.iot.proj.om2m.structures.Configuration;

import java.util.HashMap;

public class Db {
	
	final public static HashMap<Integer,MN> mnMap = new HashMap<Integer,MN>();

	static {
		Configuration db = null;
		String[][] mn = null;
		try {
			db = new Configuration ("/configuration/db.ini",Pack.JAR);
			System.out.println("Found local database");
		} catch (Exception e0) {
			try {
				db = new Configuration ("src/main/resources/configuration/db.ini",Pack.MAVEN);
				System.out.println("Found local database");
			} catch (Exception e1) {
				try {
					db = new Configuration ("configuration/db.ini","http://thingstalk.altervista.org/augmented-things");
					System.out.println("Found remote database");
				} catch (Exception e2) {
					System.out.println("No databases found, using default values");
				}
			}
		}
		try {
			mn = db.loadAttributeList("mecs.iot.proj.om2m.mnList",2);
		} catch (Exception e) {
			mn = new String[1][2];
			mn[0][0] = "augmented-things-MN";
			mn[0][1] = "127.0.0.1";
		}
		for (int i=0; i<mn.length; i++) {
			mnMap.put(i, new MN(mn[i][0],mn[i][1]));
			System.out.println("\t"+mn[i][0]+","+mn[i][1]);
		}
	}
	
	public static void main(String[] args) {
		MN mn = mnMap.get(0);
		System.out.println("Id: " + mn.id + ", address: " + mn.address);
	}

}
