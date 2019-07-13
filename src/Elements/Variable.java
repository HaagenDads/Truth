package Elements;

public class Variable {

	public String name;
	public Type type;
	
	public Variable(String name, String type) {
		this.name = name;
		this.type = new Type(type);
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
		return new Variable(name, type.type);
	}

	
}
