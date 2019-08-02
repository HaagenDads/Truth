package Elements;

import java.util.ArrayList;

import Core.Theorem;

public class Function extends Variable {

	public static final String genericType = "\\function";
	public Set domain;
	public Set image;
	
	public Collection args;
	public Term definition;
	public boolean defaultDomain, defaultImage;
	
	private ArrayList<Definition> defs;
	private boolean isdefinitionComplete;
	private boolean isdefinitionExclusive;
	private boolean haselsecondition;
	
	public Function(String name, boolean fromheader) {
		super(name, "\\function", fromheader);
		domain = new Set(this, "domain");
		image = new Set(this, "image");
		defaultDomain = true;
		defaultImage = true;
		defs = new ArrayList<Definition>();
		isdefinitionComplete = false;
		isdefinitionExclusive = false;
		haselsecondition = false;
	}
	
	public String toHeader() {
		String res = " be a function";
		if (!defaultDomain && !defaultImage) {
			res += " of " + domain.toString() + " -> " + image.toString();
		}
		if (defs.size() == 0) return res + '\n';
		for (Definition d: defs) {
			res += "\n\t  ";
			if (d.cond == null) res += d.def.toString();
			else {
				String end = "\t\t\t if  (" + d.cond.toString() + ")";
				if (d.cond.equalsString("else")) end = "\t otherwise";
				res += d.def.toString() + end;
			}
		}
		
		
		return res + '\n';
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
	

	public void setDefinition(Theorem thm, ArrayString generalDef) throws ExceptionAssignDefinition {
		if (defs.size() > 0) throw new ExceptionDefinitionMultipleUnconditional(Term.compileTerms(generalDef));
		setDefinition(thm, null, generalDef);
	}
	public void setDefinition(Theorem thm, ArrayString condstring, ArrayString defstring) throws ExceptionAssignDefinition {
		Term def = Term.compileTerms(defstring);
		Term fnc = def.get(0);
		
		if (fnc.getDisposition() != Term.Disp.FC) throw new ExceptionDefinitionLeftsideUnclear(def);
		if (!fnc.get(0).equalsString(name)) throw new ExceptionDefinitionLeftsideUnclear(def);
		Collection args = (Collection) (fnc.get(1));
		if (!domain.isElement(args, thm)) throw new ExceptionDomainDoesntMatch(def);
		
		// TODO for (Term t: def.get(2).enumerateVariables()) check();	
		if (condstring == null) {
			defs.add(new Definition(args, def));
			isdefinitionComplete = true;
			isdefinitionExclusive = true;
		}
		else {
			Term cond = Term.compileTerms(condstring);
			if (cond.equalsString("else")) {
				if (haselsecondition) throw new ExceptionMultipleElseConditions(def);
				defs.add(new Definition(args, cond, def));
				isdefinitionComplete = true;
				haselsecondition = true;
				if (defs.size() == 2) isdefinitionExclusive = true;
			}
			else {
				// TODO for (Term t: def.get(2).enumerateVariables()) check();	
				defs.add(new Definition(args, cond, def));
			}
		}
	}
	
	/* Complete partition: every point of the domain has a definition */
	public boolean isDefinitionComplete() {
		return isdefinitionComplete;
	}
	
	/* No overlapping in definitions */
	public boolean isDefinitionExclusive() {
		return isdefinitionExclusive;
	}


	private class Definition {
		Term cond;
		Term def;
		Collection var;

		public Definition (Collection var, Term def) {
			cond = null;
			this.def = def;
			this.var = var;
		}
		public Definition (Collection var, Term cond, Term def) {
			this.cond = cond;
			this.def = def;
			this.var = var;
		}
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
	static abstract public class ExceptionAssignDefinition extends Exception {
		Term t;
		public ExceptionAssignDefinition (Term t) {this.t = t;}
		String printError() { return "[DefError] " + getError() + " in: " + t.toString(); }
		abstract String getError();
	}
	static public class ExceptionDefinitionLeftsideUnclear extends ExceptionAssignDefinition {
		public ExceptionDefinitionLeftsideUnclear(Term t) {super(t);}
		String getError () { return "Left side of definition should be a function evaluated at a known variable";}
	}
	static public class ExceptionDomainDoesntMatch extends ExceptionAssignDefinition {
		public ExceptionDomainDoesntMatch(Term t) {super(t);}
		String getError () { return  "Elements of definition didn't fit the domain of the function";}
	}
	static public class ExceptionDefinitionMultipleUnconditional extends ExceptionAssignDefinition {
		public ExceptionDefinitionMultipleUnconditional(Term t) {super(t);}
		String getError () { return  "Can't have more than one unconditional definition";}
	}
	static public class ExceptionMultipleElseConditions extends ExceptionAssignDefinition {
		public ExceptionMultipleElseConditions(Term t) {super(t);}
		String getError () { return "Can't have more than one 'else' unconditional definition";}
	}
}
