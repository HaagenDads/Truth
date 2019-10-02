package Elements;

import Core.Theorem;

// Expresses the validity of a step
public class Justification {

	public Theorem thm;
	private Assump assump;
	public boolean isTheorem;
	
	public Justification(Theorem a) {
		this.thm = a;
		this.assump = null;
		isTheorem = true;
	}
	
	public Justification(Assump assump) {
		this.thm = null;
		this.assump = assump;
		isTheorem = false;
	}
	
	public Justification(String str) {
		this.thm = null;
		if (str.startsWith("error")) assump = new Assump(new Statement(new Link(str), "", ""), "0");
		// TODO: When does this str=BooleanLogic happen...
		else {
			assump = new Assump(new Statement(new Link(), str, ""), "0");
		}
		isTheorem = false;
	}
	
	public boolean isError() {
		return (!isTheorem && assump.st.link.isError());
	}
	
	public String getName() {

		if (isTheorem) return thm.name;
		else {
			String stamp = assump.IndexStamp;
			switch (stamp) {
				case "hyp":
					return "Hypothesis";
				case "0":
					return assump.st.toString();
				case "-1":
					return "Case: " + assump.st.toString();
				default:
					return "(" + stamp + ")";
			}
		}
	}
	
	public String toString() {
		if (isError()) return assump.st.link.link;
		return getName();
	}
	
}
