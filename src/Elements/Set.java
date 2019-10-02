package Elements;

import Core.Theorem;

public class Set extends Type {
	
	public static final String genericType = "\\set";

	Collection c;
	
	public Set (Term t) {
		super(t.toString());
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
		StringBuilder output = new StringBuilder();
		for (int i=0; i<c.size-1; i++) output.append(c.get(i).toString()).append(" � ");
		return output.append(c.get(c.size - 1)).toString();
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
			Type arg;
			try {
				arg = coll.get(i).getType(thm);
			} catch (ExceptionTypeUnknown e) {
				e.explain();
				return false;
			}
			String setname = this.c.get(i).s;
			if (!arg.equals(setname)) return false;
		}
		return true;
	}
	
	public static boolean isValid (String str) {
		return str.equals("\\emptyset");
	}
	
	
}
