package mecs.iot.proj.om2m.structures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import mecs.iot.proj.om2m.exceptions.InvalidRuleException;
import mecs.iot.proj.om2m.exceptions.NoRuleException;

public class Tag implements JSONSerializable, Cloneable {
	
	public Node node;
	public String id;
	public String serial;
	public String type;
	public String address;
	public String[] attributes;
	
	public boolean active;
	public HashMap<String,String> ruleMap;																		// label -> rule
	
	private String cseBaseName;
	
	// Used inside endpoint nodes
	
	public Tag (String id, String serial, String type, String[] attributes) {
		this.node = Node.SENSOR;
		this.id = id;
		this.serial = serial;
		this.type = type;
		this.attributes = attributes;
	}
	
	public Tag (String id, String serial, String[] attributes) {
		this.node = Node.ACTUATOR;
		this.id = id;
		this.serial = serial;
		this.type = "act";
		this.attributes = attributes;
	}
	
	// Used inside ADNs
	
	public Tag (Node node, String id, String serial, String str, String[] attributes, String cseBaseName) {
		this.node = node;
		this.serial = serial;
		switch (node) {
			case SENSOR:
				this.id = id;
				this.type = str;
				this.address = null;
				this.attributes = attributes;
				this.ruleMap = new HashMap<String,String>();
				for (int i=0; i<attributes.length; i++) {
					String[] splits;
					splits = attributes[i].split(": ");
					if (splits.length>1)
						this.ruleMap.put(splits[1],splits[0]);
					else
						this.ruleMap.put(splits[0],"");
					// TODO: syntax check on labels and rules
				}
				break;
			case ACTUATOR:
				this.id = id;
				this.type = "act";
				this.address = str;
				this.attributes = attributes;
				// TODO: syntax check on labels
				break;
			case USER:
				this.address = str;
				break;
		}
		this.active = true;
		this.cseBaseName = cseBaseName;
	}
	
	public Tag (String id, String address, String cseBaseName) {
		this.node = Node.USER;
		this.id = id;
		this.address = address;
		this.active = true;
		this.cseBaseName = cseBaseName;
	}
	
	private Tag(Node node, String id, String type, String address, String[] attributes, boolean active, String cseBaseName) {
		this.node = node;
		this.id = id;
		this.type = type;
		this.address = address;
		this.attributes = attributes;
		this.active = active;
		this.cseBaseName = cseBaseName;
	}

	// TODO: generalize for rules missing middle terms (see docs)
	public static NumericRule parseNumericRule(String rule) throws NoRuleException, InvalidRuleException {
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
					ArrayList<Sign> sgns = new ArrayList<Sign>();
					char[] chars = body.toCharArray();
					if (chars[0]=='+') {
						sgns.add(Sign.PLUS);
						body = body.substring(1);
					} else if (chars[0]=='-') {
						sgns.add(Sign.MINUS);
						body = body.substring(1);
					} else {
						sgns.add(Sign.PLUS);
					}
					chars = body.toCharArray();
					for (int i=0; i<chars.length; i++) {
						if (chars[i]=='+')
							sgns.add(Sign.PLUS);
						else if (chars[i]=='-')
							sgns.add(Sign.MINUS);
					}
					Sign[] signs = sgns.toArray(new Sign[] {});
					String[] splits = body.split("[+-]");
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
										if (signs[i]==Sign.MINUS)
											coefficient = -coefficient;
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
										if (signs[index]==Sign.PLUS)
											coefficients[index] = 1.0;
										else if (signs[index]==Sign.MINUS)
											coefficients[index] = -1.0;
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
					return new NumericRule(coefficients,to,thr);
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
	
	@Override
	
	public JSONObject toJSON() {
		JSONObject obj = new JSONObject();
		obj.put("node",node);
		if (id!=null)
			obj.put("id",id);
		if (type!=null)
			obj.put("type",type);
		if (address!=null)
			obj.put("address",address);
		if (attributes!=null) {
			for (int i=0; i<attributes.length; i++) {
				obj.append("attributes",attributes[i]);
			}
		}
		obj.put("active",active);
		obj.put("mn",cseBaseName);
		return obj;
	}
	
	@Override
	
	public Object clone() {
		String[] attributes_ = new String[attributes.length];
		for (int i=0; i<attributes.length; i++)
			attributes_[i] = attributes[i];
		return new Tag(node,id,type,address,attributes_,active,cseBaseName);
	}
	
	private enum Sign {
		PLUS, MINUS
	}
	
	public static void main(String[] args) {
		String rule = "(-0.3*[1]+0.2*[3]-[2]-0.05*[0])>4.5";
		try {
			NumericRule r = parseNumericRule(rule);
			System.out.println(r);
		} catch (NoRuleException | InvalidRuleException e) {
			System.out.println("Error");
		}
	}
	
}
