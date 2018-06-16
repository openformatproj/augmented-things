package mecs.iot.proj.om2m.adn.in;

import mecs.iot.proj.om2m.structures.MN;
import mecs.iot.proj.om2m.structures.Configuration;

import java.util.HashMap;

public class Db {
	
	final public static HashMap<Integer,MN> mnMap = new HashMap<Integer,MN>();

	static {
		Configuration conf = null;
		String[][] mn = null;
		try {
			conf = new Configuration ("../../../../../configuration/db.ini");
			System.out.println("Found local database");
		} catch (Exception e0) {
			try {
				conf = new Configuration ("configuration/db.ini","http://thingstalk.altervista.org/augmented-things");
				System.out.println("Found remote database");
			} catch (Exception e1) {
				//e1.printStackTrace();
				try {
					conf = new Configuration ("configuration/db.ini");
					System.out.println("Found local database (Maven)");
				} catch (Exception e2) {
					System.out.println("No databases found, using default values");
				}
			}
		}
		try {
			mn = conf.loadAttributeList("mnList",2);
		} catch (Exception e) {
			mn = new String[1][2];
			mn[0][0] = "augmented-things-MN";
			mn[0][1] = "127.0.0.1";
		}
		for (int i=0; i<mn.length; i++) {
			mnMap.put(i, new MN(mn[i][0],mn[i][1]));
			System.out.println(mn[i][0]+","+mn[i][1]);
		}
	}
	
	public static void main(String[] args) {
		MN mn = mnMap.get(0);
		System.out.println("Id: " + mn.id + ", address: " + mn.address);
	}

}
