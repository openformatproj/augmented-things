package mecs.iot.proj.om2m.structures;

import mecs.iot.proj.om2m.exceptions.AttributeMissException;
import mecs.iot.proj.om2m.exceptions.AttributeSyntaxException;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

public class Configuration {

	private Properties prop;
	private Document doc;
	private Type type;
	
	public Configuration (String filename, Pack pack, Type type) throws NullPointerException, MalformedURLException, FileNotFoundException, IOException {
		this.type = type;
		switch (pack) {
			case MAVEN:
				File file = new File(filename);
				load(new FileInputStream(file),type);
				break;
			case JAR:
				InputStream stream = getClass().getResourceAsStream(filename);
				load(stream,type);
				break;
			case REMOTE:
				URL url = new URL(filename);
//				prop = new Properties();
//				prop.load(url.openStream());
				load(url.openStream(),type);
		}
	}
	
	private void load (InputStream stream, Type type) throws FileNotFoundException, IOException {
		switch (type) {
			case INI:
				prop = new Properties();
				prop.load(stream);
				break;
			case XML:
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = null;
				try {
					dBuilder = dbFactory.newDocumentBuilder();
					doc = dBuilder.parse(stream);
					doc.getDocumentElement().normalize();
				} catch (ParserConfigurationException e) {
					throw new IOException();
				} catch (SAXException e) {
					throw new IOException();
				}
				break;
		}
	}
	
//	public Configuration (String filename, String address) throws MalformedURLException, IOException {
//		prop = new Properties();
//		URL url = null;
//		try {
//			url = new URL(address + "/" + filename);
//		} catch (MalformedURLException e) {
//			throw e;
//		}
//		prop.load(url.openStream());
//	}

	public String getAttribute (String attribute) throws AttributeMissException, AttributeSyntaxException {
		switch (type) {
			case INI:
				String attr = prop.getProperty(attribute);
				if (attr==null)
					throw new AttributeMissException();
				return attr;
			case XML:
				throw new AttributeSyntaxException();
		}
		return null;
	}
	
	public String getAttribute (String attribute, Node node) throws AttributeMissException, AttributeSyntaxException {
		switch (type) {
			case INI:
				throw new AttributeSyntaxException();
			case XML:
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element e = (Element)node;
					return e.getAttribute(attribute);
				} else {
					throw new AttributeSyntaxException();
				}
		}
		return null;
	}
	
	public String getTagTextContent (String name, Node node) throws AttributeMissException, AttributeSyntaxException {
		switch (type) {
			case INI:
				throw new AttributeSyntaxException();
			case XML:
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element e = (Element)node;
					return e.getElementsByTagName(name).item(0).getTextContent();
				} else {
					throw new AttributeSyntaxException();
				}
		}
		return null;
	}
	
	public NodeList getElements (String attribute) throws AttributeMissException, AttributeSyntaxException {
		switch (type) {
		case INI:
			throw new AttributeSyntaxException();
		case XML:
			return doc.getElementsByTagName(attribute);
		}
	return null;
	}
	
	public NodeList getElements (String attribute, Node node) throws AttributeMissException, AttributeSyntaxException {
		switch (type) {
		case INI:
			throw new AttributeSyntaxException();
		case XML:
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element e = (Element)node;
				return e.getElementsByTagName(attribute);
			} else {
				throw new AttributeSyntaxException();
			}
		}
	return null;
	}

	public String[] getAttributeList (String attribute) throws AttributeMissException, AttributeSyntaxException {
		switch (type) {
			case INI:
				String attr = prop.getProperty(attribute);
				if (attr==null)
					throw new AttributeMissException();
				String[] attrList = attr.split(";");
				return attrList;
			case XML:
				throw new AttributeSyntaxException();
		}
		return null;
	}

	public String[][] getAttributeList (String attribute, int n) throws AttributeSyntaxException, AttributeMissException {
		switch (type) {
			case INI:
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
			case XML:
				throw new AttributeSyntaxException();
		}
		return null;
	}

}
