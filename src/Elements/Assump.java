package Elements;

import Elements.Statement;

public class Assump {
	public Statement st;
	public String IndexStamp;
	public Assump(Statement st, String id) {
		this.st = st;
		IndexStamp = id;
	}
	
	public Assump copy() {
		Assump clone = new Assump(st.copy(), IndexStamp);
		return clone;
	}
	
	public boolean equals (Assump other) {
		return this.st.equals(other.st);
	}
	
	public void mergeStamps (Assump other) {
		if (this.IndexStamp.equals("")) this.IndexStamp = other.IndexStamp;
		else this.IndexStamp += " & " + other.IndexStamp;
	}
}
