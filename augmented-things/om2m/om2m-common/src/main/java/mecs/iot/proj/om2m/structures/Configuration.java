package mecs.iot.proj.om2m.structures;

import mecs.iot.proj.om2m.exceptions.AttributeMissException;
import mecs.iot.proj.om2m.exceptions.AttributeSyntaxException;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Properties;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

public class Configuration {

	private Properties prop;

	public Configuration (String filename) throws NullPointerException, FileNotFoundException, IOException {
		prop = new Properties();
		File file = null;
		try {
			file = new File(filename);
		} catch (NullPointerException e) {
			throw e;
		}
		try {
			prop.load(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			throw e;
		}
	}
	
	public Configuration (String filename, Pack pack) throws NullPointerException, FileNotFoundException, IOException {
		prop = new Properties();
		switch (pack) {
			case MAVEN:
				File file = null;
				try {
					file = new File(filename);
				} catch (NullPointerException e) {
					throw e;
				}
				try {
					prop.load(new FileInputStream(file));
				} catch (FileNotFoundException e) {
					throw e;
				}
				break;
			case JAR:
				InputStream stream = getClass().getResourceAsStream(filename);
				try {
					prop.load(stream);
				} catch (FileNotFoundException e) {
					throw e;
				}
				break;
		}
	}
	
	public Configuration (String filename, String address) throws MalformedURLException, IOException {
		prop = new Properties();
		URL url = null;
		try {
			url = new URL(address + "/" + filename);
		} catch (MalformedURLException e) {
			throw e;
		}
		prop.load(url.openStream());
	}

	public String loadAttribute (String attribute) throws AttributeMissException {
		String attr = prop.getProperty(attribute);
		if (attr==null)
			throw new AttributeMissException();
		return attr;
	}

	public String[] loadAttributeList (String attribute) throws AttributeMissException {
		String attr = prop.getProperty(attribute);
		if (attr==null)
			throw new AttributeMissException();
		String[] attrList = attr.split(";");
		return attrList;
	}

	public String[][] loadAttributeList (String attribute, int n) throws AttributeSyntaxException, AttributeMissException {
		if (n<=0)
			throw new AttributeSyntaxException();
		String attr = prop.getProperty(attribute);
		if (attr==null)
			throw new AttributeMissException();
		String[] attrList = attr.split(";");
		String[][] attrArray = new String[attrList.length][n];
		for(int i = 0; i<attrList.length; i++) {
			String[] attrArray_ = attrList[i].split(",");
			if (attrArray_.length!=n)
				throw new AttributeMissException();
			else
				attrArray[i] = attrArray_;
		}
		return attrArray;
	}

}
