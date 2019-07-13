package Elements;

import Operation.BooleanLogic;
import Operation.Operator;
import Core.Theorem;
import Operation.NaturalNumbers;

public class Type {

	String type;
	public Type (String t) {
		assertType(t);
		type = t;
	}
	
	public String toString() {
		return "T[" + type + "]";
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
	
	static public Type getType(Term t, Theorem thm) {
		t.flatten();
		if (t.isShallow()) {
			Variable v = thm.getVariable(t.s);
			if (v != null) return v.type;
			
			if (BooleanLogic.isValid(t.s)) return new Type(BooleanLogic.genericType);
			if (NaturalNumbers.isValid(t.s)) return new Type(NaturalNumbers.genericType);
			return null;

		} else {
			return reduceType(t, thm);
		}
		
		
	}/*
		Term tcopy = t.copy();
		tcopy.applyType(source);
		
		try {
			return reduceType(tcopy).s;
		} catch (Exception e) {}
		return null;
	}*/
	
	static private Type reduceType(Term t, Theorem thm) {
		
		Type X = null;
		String Op = null;
		Type Y = null;
		for (Term token: t.v) {
			
			if (Operator.isUnary(token.s)) {
				if (X == null && Op == null) Op = token.s;
				else if (X == null) System.out.println("Multiple unary operators havent been though of yet.");
			} else if (Operator.isBinary(token.s)) {
				if (X == null) System.out.println( "Couldnt solve for ': " + token + "'");
				else if (Op == null) Op = token.s;
				else System.out.println( "Couldnt solve for '" + X + " " + Op + " : " + token + "'");
			} else {
				Type type = getType(token, thm);
				
				if (X == null && Op == null) X = type;
				else if (X == null) X = solveUnary(Op, type);
				else if (Op == null) System.out.println("Couldnt solve for '" + X + " : " + token + "'");
				else if (Y == null) X = solveBinary(X, Op, type);
				else System.out.println( "Couldnt solve for '" + X + " " + Op + " " + Y + " : " + token + "'");
			}
		}
		return X;
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
				if (op.equals("\\and") || op.equals("\\or")) return a;
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
