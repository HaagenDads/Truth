package Elements;

import Core.Theorem;

public class Set extends Type {
	
	public static final String genericType = "\\set";

	Collection c;
	
	public Set (Term t) {
		super(null);
		if (t.isCollection()) c = (Collection) t;
		else {
			c = new Collection();
			c.addTerm(t);
		}
	}
	public Set (Function f, String purpose) {
		super(null);
		Term abstractset = new Term(f.name + "." + purpose);
		c = new Collection();
		c.addTerm(abstractset);
	}
	
	public String toString() {
		if (c.size == 1) return c.get(0).toString();
		String output = "";
		for (int i=0; i<c.size-1; i++) output += c.get(i).toString() + " × ";
		return output += c.get(c.size-1);
	}
	
	public boolean equals (Set other) {
		if (other.c.size != this.c.size) return false;
		for (int i=0; i<c.size; i++) {
			if (!other.c.get(i).equals(this.c.get(i))) return false;
		}
		return true;
	}
	public boolean equals (Type other) {
		return other.equals(this);
	}
	
	public boolean isElement(Collection coll, Theorem thm) {
		if (coll.size != c.size) return false;
		for (int i=0; i<c.size; i++) {
			Type arg = Type.getType(coll.get(i), thm);
			String setname = this.c.get(i).s;
			if (!arg.equals(setname)) return false;
		}
		return true;
	}
	
	public static boolean isValid (String str) {
		if (str.equals("\\emptyset")) return true;
		return false;
	}
	
	
}
