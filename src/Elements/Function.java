package Elements;

import Core.Theorem;

public class Function extends Variable {

	public static final String genericType = "\\function";
	public Set domain;
	public Set image;
	
	public Collection args;
	public Term definition;
	public boolean defaultDomain, defaultImage;
	
	public Function(String name) {
		super(name, "\\function");
		domain = new Set(this, "domain");
		image = new Set(this, "image");
		defaultDomain = true;
		defaultImage = true;
	}
	
	public String toHeader() {
		String res = " be a function";
		if (!defaultDomain && !defaultImage) {
			res += " of " + domain.toString() + " -> " + image.toString();
		}
		return res + "\n";
	}
	
	public void setDomain (Term term, Theorem thm) throws ExceptionSetInvalid {
		Type tp = Type.getType(term, thm);
		if (!tp.equals(Set.genericType)) throw new ExceptionSetInvalid(term, true);
		domain = new Set(term);
		defaultDomain = false;
	}
	
	public void setImage (Term term, Theorem thm) throws ExceptionSetInvalid {
		Type tp = Type.getType(term, thm);
		if (!tp.equals(Set.genericType)) throw new ExceptionSetInvalid(term, false);
		image = new Set(term);
		defaultImage = false;
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
			return "[ FNC ] Could not comprehend initialisation of " + set + " from :" + invalidset.toString();
		}
	}
}
