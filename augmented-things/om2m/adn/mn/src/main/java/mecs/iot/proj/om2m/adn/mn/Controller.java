package mecs.iot.proj.om2m.adn.mn;

import mecs.iot.proj.om2m.exceptions.InvalidRuleException;
import mecs.iot.proj.om2m.exceptions.NoRuleException;
import mecs.iot.proj.om2m.structures.Rule;
import mecs.iot.proj.om2m.structures.Tag;
import mecs.iot.proj.om2m.structures.Token;

public class Controller {
	
	private double[] coefficients;												// a[0], a[1]... a[k-1]
	private FSR<Double> values;													// x[0],x[-1]... x[k-1]
	private Trigger trigger;
	
	private boolean noRule;
	
	public Controller(String rule) throws InvalidRuleException {
		Rule rule_ = null;
		try {
			rule_ = Tag.parseRule(rule);
			noRule = false;
		} catch (NoRuleException e) {
			noRule = true;
		} catch (InvalidRuleException e) {
			throw e;
		}
		if (!noRule) {
			coefficients = rule_.coefficients;
			values = new FSR<Double>(coefficients.length,0.0);
			trigger = new Trigger(rule_.token,rule_.threshold);
		}
	}
	
	public boolean check() {
		if (!noRule)
			return trigger.state;
		else
			return true;
	}
	
	public void insert(double value) {
		if (!noRule) {
			values.add(value);
			double acc = 0.0;
			for (int i=0; i<coefficients.length; i++) {
				acc += coefficients[i]*values.get(i);
			}
			trigger.insert(acc);
		}
	}

}

class Trigger {
	
	private Token token;
	private double threshold;
	private double hystheresis;
	boolean state;
	private boolean started;
	
	Trigger(Token token, double threshold) {
		this.token = token;
		this.threshold = threshold;
		this.hystheresis = 0.2; // TODO: load from .ini
		state = false;
		started = false;
	}
	
	void insert(double value) {
		if (!started) {
			switch(token) {
				case LESS:
					if (value<threshold-(hystheresis/2.0)) {
						state = true;
						started = true;
					} else if (value>threshold+(hystheresis/2.0)) {
						state = false;
						started = true;
					} else {
						return;
					}
					break;
				case GREATER:
					if (value>threshold+(hystheresis/2.0)) {
						state = true;
						started = true;
					} else if (value<threshold-(hystheresis/2.0)) {
						state = false;
						started = true;
					} else {
						return;
					}
					break;
				case EQUAL:
					if (value>threshold-(hystheresis/2.0) && value<threshold+(hystheresis/2.0))
						state = true;
					else
						state = false;
					started = true;
					break;
			}
		}
		switch(token) {
			case LESS:
				if (value<threshold-(hystheresis/2.0))
					state = true;
				else if (value>threshold+(hystheresis/2.0))
					state = false;
				break;
			case GREATER:
				if (value>threshold+(hystheresis/2.0))
					state = true;
				else if (value<threshold-(hystheresis/2.0))
					state = false;
				break;
			case EQUAL:
				if (value>threshold-(hystheresis/2.0) && value<threshold+(hystheresis/2.0))
					state = true;
				else
					state = false;
				break;
		}
	}
	
}
