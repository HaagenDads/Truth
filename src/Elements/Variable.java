package Elements;

public class Variable {

	public String name;
	public Type type;
	public boolean fromheader;
	
	public Variable(String name, String type, boolean fromheader) {
		this.name = name;
		this.type = new Type(type);
		this.fromheader = fromheader;
	}
	
	public String toHeader() {
		return " be in " + type + "\n";
	}

	public Term toTerm() {
		Term result = new Term(name);
		result.type = type;
		return result;
	}

	public boolean equals (Variable other) {
		return (name.equals(other.name) && type.equals(other.type));
	}
	
	public void embedVariableNames(String head) {
		if (!name.startsWith(head))	name = head + name;
	}
	
	public void removeEmbeding() {
		String[] sections = name.split("\\.");
		name = sections[sections.length-1];
	}

	public Variable copy() {
		return new Variable(name, type.type, fromheader);
	}

	
}
