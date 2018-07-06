package mecs.iot.proj.om2m;

import mecs.iot.proj.om2m.Client;

import java.util.ArrayList;
import java.util.List;
import java.net.URISyntaxException;

class PathManager {
	
	private Client client;
	private List<String> uri;
	int level;
	
	PathManager(Client client, String uri, int capacity) {
		this.client = client;
		this.uri = new ArrayList<String>(capacity);
		this.uri.add(uri);
		level = 0;
	}
	
	String uri() {
		String out = "";
		for(int i=0; i<uri.size(); i++) {
			if (i!=uri.size()-1)
				out += uri.get(i) + "/";
			else
				out += uri.get(i);
		}
		return out;
	}
	
	void change(String[] uri) throws URISyntaxException {
		level = uri.length;
		if (level>0) {
			for (int i=this.uri.size()-1; i>0; i--) {
				this.uri.remove(i);
			}
			for(int i=0; i<level; i++) {
				this.uri.add(uri[i]);
			}
			client.connect(uri(),false);
		}
	}
	
	void down(String uri, boolean connect) throws URISyntaxException {
		level += 1;
		this.uri.add(uri);
		if (connect)
			client.connect(uri(),false);
	}
	
	void up() throws URISyntaxException {
		level -= 1;
		this.uri.remove(this.uri.get(this.uri.size()-1));
		client.connect(uri(),false);
	}

}
