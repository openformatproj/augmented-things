package mecs.iot.proj.om2m.adn.mn;

class FSR<T> {
	
	private T[] register;
	private int n;
	
	@SuppressWarnings("unchecked")
	FSR(int n, T ic) {
		this.n = n;
		register = (T[]) new Object[n];
		for (int i=0; i<n; i++)
			register[i] = ic;
	}
	
	void add(T value) {
		for (int i=n-1; i>0; i--)
			register[i] = register[i-1];
		register[0] = value;
	}
	
	T get(int i) {
		return register[i];
	}
	
}
