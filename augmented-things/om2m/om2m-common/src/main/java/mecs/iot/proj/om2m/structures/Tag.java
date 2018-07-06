package mecs.iot.proj.om2m.structures;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import mecs.iot.proj.om2m.structures.exceptions.InvalidRuleException;
import mecs.iot.proj.om2m.structures.exceptions.NoRuleException;

public class Tag implements JSONSerializable {
	
	public Node node;
	public String id;
	public String serial;
	public String address;
	public String type;
	public String[] attributes;
	
	private String cseBaseName;
	
	public HashMap<String,String> ruleMap;																		// label -> rule
	
	public Tag (String id, String serial, String type, String[] attributes) {
		this.node = Node.SENSOR;
		this.id = id;
		this.serial = serial;
		this.address = null;
		this.type = type;
		this.attributes = attributes;
		ruleMap = null;
	}
	
	public Tag (String id, String serial, String[] attributes) {
		this.node = Node.ACTUATOR;
		this.id = id;
		this.serial = serial;
		this.address = null;
		this.type = "act";
		this.attributes = attributes;
		ruleMap = null;
	}
	
	public Tag (Node node, String id, String description, String[] attributes, String cseBaseName) {
		this.node = node;
		this.id = id;
		this.serial = null;
		switch (node) {
			case SENSOR:
				this.address = null;
				this.type = description;
				this.attributes = attributes;
				ruleMap = new HashMap<String,String>();
				for (int i=0; i<attributes.length; i++) {
					String[] splits;
					splits = attributes[i].split(": ");
					if (splits.length>1)
						ruleMap.put(splits[1],splits[0]);
					else
						ruleMap.put(splits[0],"");
					// TODO: syntax check on label
				}
				break;
			case ACTUATOR:
				this.address = description;
				this.type = "act";
				this.attributes = attributes;
				ruleMap = new HashMap<String,String>();
				for (int i=0; i<attributes.length; i++) {
					ruleMap.put(attributes[i],"");
				}
				break;
			case USER:
				break;
		}
		this.cseBaseName = cseBaseName;
	}
	
	public static Rule parseRule(String rule) throws NoRuleException, InvalidRuleException {
		if (rule.equals("")) {
			throw new NoRuleException();
		} else {
			Pattern pattern = Pattern.compile("^\\(?(.+?)\\)?([><=])(\\d+(?:\\.\\d+)?)$");
			Matcher matcher = pattern.matcher(rule);
			if (matcher.find()) {
				String body = matcher.group(1);
				String token = matcher.group(2);
				String threshold = matcher.group(3);
				if (body!=null && token!=null && threshold!=null) {
					String[] splits = body.split("[+-]"); // TODO: check if plus or minus
					Pattern placeholder = Pattern.compile("^\\[(\\d+)\\]$");
					double[] coefficients = new double[splits.length];
					boolean[] assigned = new boolean[splits.length];
					int index;
					double coefficient;
					for (int i=0; i<splits.length; i++) {
						String[] splits_ = splits[i].split("\\*");
						if (splits_.length==2) {
							matcher = placeholder.matcher(splits_[1]);
							if (matcher.find()) {
								String content = matcher.group(1);
								if (content!=null) {
									try {
										index = Integer.parseInt(content);
									} catch (NumberFormatException e) {
										throw new InvalidRuleException();
									}
									if (index<splits.length && !assigned[index]) {
										try {
											coefficient = Double.parseDouble(splits_[0]);
										} catch (NumberFormatException e) {
											throw new InvalidRuleException();
										}
										coefficients[index] = coefficient;
										assigned[index] = true;
									} else {
										throw new InvalidRuleException();
									}
								} else {
									throw new InvalidRuleException();
								}
							} else {
								throw new InvalidRuleException();
							}
						} else if (splits_.length==1) {
							matcher = placeholder.matcher(splits_[0]);
							if (matcher.find()) {
								String content = matcher.group(1);
								if (content!=null) {
									try {
										index = Integer.parseInt(content);
									} catch (NumberFormatException e) {
										throw new InvalidRuleException();
									}
									if (index<splits.length && !assigned[index]) {
										coefficients[index] = 1.0;
										assigned[index] = true;
									} else {
										throw new InvalidRuleException();
									}
								} else {
									throw new InvalidRuleException();
								}
							} else {
								throw new InvalidRuleException();
							}
						} else {
							throw new InvalidRuleException();
						}
					}
					Token to;
					if (token.equals("<"))
						to = Token.LESS;
					else if (token.equals(">"))
						to = Token.GREATER;
					else if (token.equals("="))
						to = Token.EQUAL;
					else
						throw new InvalidRuleException();
					double thr;
					try {
						thr = Double.parseDouble(threshold);
					} catch (NumberFormatException e) {
						throw new InvalidRuleException();
					}
					return new Rule(coefficients,to,thr);
				} else {
					throw new InvalidRuleException();
				}
			} else {
				throw new InvalidRuleException();
			}
		}
	}
	
	@Override
	
	public String toString() {
		String str = "id=" + id + ", type=" + type;
		for (int i=0; i<attributes.length; i++) {
			str += ", attributes[" + Integer.toString(i) + "]=" + attributes[i];
		}
		return str;
	}
	
	public JSONObject toJSON() {
		JSONObject obj = new JSONObject();
		obj.put("id",id);
		obj.put("type",type);
		for (int i=0; i<attributes.length; i++) {
			obj.append("attributes",attributes[i]);
		}
		if (cseBaseName!=null)
			obj.put("mn",cseBaseName);
		return obj;
	}
	
	public static void main(String[] args) {
		String rule = "(0.3*[1]-0.05*[0])>4.5";
		try {
			Rule r = parseRule(rule);
			System.out.println(r);
		} catch (NoRuleException | InvalidRuleException e) {
			System.out.println("Error");
		}
	}
	
}
