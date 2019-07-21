package Elements;

import Operation.BooleanLogic;
import Core.Theorem;
import Operation.NaturalNumbers;

public class Type {

	public String type;
	public Type (String t) {
		assertType(t);
		type = t;
	}
	
	public String toString() {
		//return "T[" + type + "]";
		return type;
	}
	
	private void assertType (String t) {
		if (t.equals("\\boolean") || t.equals("\\setnatural")) return;
		System.out.println("Type '" + t + "' doesn't exist");
	}
	
	public boolean equals (Type other) {
		return this.type.equals(other.type);
	}
	public boolean equals (String str) {
		return this.type.equals(str);
	}
	
	static public boolean matchtypes (Type t1, Type t2) {
		if (t1.type == null || t2.type == null) return false;
		if (t1.equals(t2)) return true;
		return false;
	}
	
	static public boolean matchtypes (Term t1, Term t2, Theorem thm) {
		return matchtypes(getType(t1, thm), getType(t2, thm));
	}
	
	static public Type getType(Term t, Theorem thm) {
		Term.Disp termdisp = t.getDisposition();
		if (termdisp == Term.Disp.F) {
			Variable v = thm.getVariable(t.s);
			if (v != null) return v.type;
			if (BooleanLogic.isValid(t.s)) return new Type(BooleanLogic.genericType);
			if (NaturalNumbers.isValid(t.s)) return new Type(NaturalNumbers.genericType);
			return null;
		}
		if (termdisp == Term.Disp.QTT) return new Type(BooleanLogic.genericType);
		if (termdisp == Term.Disp.OT) {
			Type x = getType(t.get(1), thm);
			return solveUnary(t.get(0).s, x);
		}
		if (termdisp == Term.Disp.TOT) {
			Type x = getType(t.get(0), thm);
			Type y = getType(t.get(2), thm);
			return solveBinary(x, t.get(1).s, y);
		}
		else return null;
	}
	
	static private Type solveUnary(String op, Type a) {
		if (a.equals(BooleanLogic.genericType)) {
			if (op.equals("\\not")) return a;
		}
		return null;
	}
	
	static private Type solveBinary(Type a, String op, Type b) {
		if (a.equals(b)) {
			if (a.equals(BooleanLogic.genericType)) {
				if (op.equals("\\and") || op.equals("\\or") || op.equals("\\implies")) return a;
			}
			if (a.equals(NaturalNumbers.genericType)) {
				if (op.equals("+") || op.equals("-") || op.equals("*")) return a;
				if (op.equals("=") || op.equals("<") || op.equals(">") || op.equals(">=") || op.equals("<=") || op.equals("!=")) {
					return new Type(BooleanLogic.genericType);
				}
			}
		}
		return null;
	}
}
