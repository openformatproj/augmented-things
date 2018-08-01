package mecs.iot.proj.om2m.adn.mn;

import mecs.iot.proj.om2m.structures.ASN;

class NotificationRegister extends Thread {
	
	private PeriodicManager manager;
	ASN asn;
	long period;
	private long lastReset;
	private long threshold;
	private int i;
	
	private boolean executing;
	
	NotificationRegister(PeriodicManager manager, ASN asn) {
		this.manager = manager;
		this.asn = asn;
		switch(asn.node) {
			case SENSOR:
				period = asn.period;
				threshold = 10*period;
				break;
			case ACTUATOR:
			case USER:
				threshold = 60000;
				break;
		}
		i = 0;
		executing = true;
		reset();
		start();
	}
	
	void update() {
		i++;
		switch(asn.node) {
			case SENSOR:
				if (i>1) {
					asn.period = (long)Math.rint(((System.currentTimeMillis()-lastReset)+(i-1)*asn.period)/(double)i);
					period = asn.period;
					threshold = 10*period;
				}
				reset();
				break;
			case ACTUATOR:
			case USER:
				reset();
				break;
		}
	}
	
	void terminate() {
		executing = false;
	}
	
	@Override
	public void run() {
		long timeElapsedFromlastReset;
		while(executing) {
			try {
				sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			timeElapsedFromlastReset = System.currentTimeMillis()-lastReset;
			if (executing && timeElapsedFromlastReset>threshold) {
				synchronized(manager) {
					manager.push(this);
					manager.notify();
				}
			}
		}
	}
	
	private void reset() {
		lastReset = System.currentTimeMillis();
	}
	
}
