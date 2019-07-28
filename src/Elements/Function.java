package Elements;

import Core.Theorem;

public class Function extends Variable {

	public static final String genericType = "\\function";
	public Set domain;
	public Set image;
	
	public Collection args;
	public Term definition;
	
	public Function(String name) {
		super(name, "\\function");
	}
	
	public String toHeader() {
		String res = " be a function";
		if (domain != null && image != null) {
			res += " of " + domain.toString() + " -> " + image.toString();
		}
		return res + "\n";
	}
	
	public void setDomain (Term term, Theorem thm) throws ExceptionSetInvalid {
		Type tp = Type.getType(term, thm);
		if (!tp.equals(Set.genericType)) throw new ExceptionSetInvalid(term, true);
		domain = new Set(term);
	}
	
	public void setImage (Term term, Theorem thm) throws ExceptionSetInvalid {
		Type tp = Type.getType(term, thm);
		if (!tp.equals(Set.genericType)) throw new ExceptionSetInvalid(term, false);
		image = new Set(term);
	}

	public class ExceptionSetInvalid extends Exception {
		public Term invalidset;
		public boolean isdomain;
		public ExceptionSetInvalid (Term set, boolean isdomain) {
			invalidset = set;
			this.isdomain = isdomain;
		}
		public String getError () {
			String set;
			if (isdomain) set = "domain";
			else set = "image";
			return "[ FATAL ] Could not comprehend initialisation of " + set + " from :" + invalidset.toString();
		}
	}
}
