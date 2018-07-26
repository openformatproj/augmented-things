package mecs.iot.proj.om2m.adn.mn;

import mecs.iot.proj.om2m.exceptions.InvalidRuleException;
import mecs.iot.proj.om2m.exceptions.NoRuleException;
import mecs.iot.proj.om2m.structures.Constants;
import mecs.iot.proj.om2m.structures.NumericRule;
import mecs.iot.proj.om2m.structures.Tag;
import mecs.iot.proj.om2m.structures.Token;

class Controller implements Checker {
	
	private double[] coefficients;												// a[0], a[1]... a[k-1]
	private FSR<Double> values;													// x[0],x[-1]... x[k-1]
	private Trigger trigger;
	
	private boolean noRule;
	
	Controller(String rule) throws InvalidRuleException {
		NumericRule rule_ = null;
		try {
			rule_ = Tag.parseNumericRule(rule);
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
	
	@Override
	
	public void insert(Object value) {
		insert((double)value);
	}
	
	private void insert(double value) {
		if (!noRule) {
			values.add(value);
			double acc = 0.0;
			for (int i=0; i<coefficients.length; i++) {
				acc += coefficients[i]*values.get(i);
			}
			trigger.insert(acc);
		}
	}
	
	@Override
	
	public boolean check() {
		if (!noRule)
			return trigger.state;
		else
			return true;
	}
	
	private class Trigger {
		
		private Token token;
		private double threshold;
		private double hystheresis;
		private boolean started;
		
		boolean state;
		
		Trigger(Token token, double threshold) {
			this.token = token;
			this.threshold = threshold;
			this.hystheresis = Constants.hysteresis;
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

}