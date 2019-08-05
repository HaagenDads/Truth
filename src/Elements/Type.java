package Elements;

import Operation.BooleanLogic;
import Core.Theorem;
import Operation.NaturalNumbers;
import Operation.Op;
import Operation.Operator;

public class Type {

	public String type;
	public Type (String t) {
		//assertType(t);
		type = t;
	}
	
	public String toString() {
		//return "T[" + type + "]";
		return type;
	}
	
	/*
	private void assertType (String t) {
		if (isin(t, new String[]{BooleanLogic.genericType, NaturalNumbers.genericType, Set.genericType, 
				                 Function.genericType})) return;
		System.out.println("Type '" + t + "' doesn't exist");
	}*/
	
	public boolean equals (Set other) {
		if (other.c.size == 1) {
			Term s = other.c.get(0);
			//System.out.println(":eql: " + s.toString() + ":" + this.type + ":" + s.equals(this));
			return s.equalsString(type);
		}
		return false;
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
			if (isin(t.s, new String[]{BooleanLogic.genericType, NaturalNumbers.genericType, Set.genericType})) return new Type(Set.genericType);
			Variable v = thm.getVariable(t.s);
			if (v != null) return v.type;
			if (BooleanLogic.isValid(t.s)) return new Type(BooleanLogic.genericType);
			if (NaturalNumbers.isValid(t.s)) return new Type(NaturalNumbers.genericType);
			if (Set.isValid(t.s)) return new Type(Set.genericType);
			return null;
		}
		if (termdisp == Term.Disp.QTT) return new Type(BooleanLogic.genericType);
		if (termdisp == Term.Disp.DEF) return new Type(BooleanLogic.genericType);
		if (termdisp == Term.Disp.OT) {
			Type x = getType(t.get(1), thm);
			return solveUnary(Op.getOperator(t.get(0).s), x);
		}
		if (termdisp == Term.Disp.TOT) {
			Type x = getType(t.get(0), thm);
			Type y = getType(t.get(2), thm);
			return solveBinary(x, Op.getOperator(t.get(1).s), y);
		}
		if (termdisp == Term.Disp.FC) {
			Function fnc = (Function) (thm.getVariable(t.get(0).s));
			Collection coll = (Collection) (t.get(1));
			if (fnc.domain.isElement(coll, thm)) return fnc.image;
			System.out.println(":coll " + coll.toString() + " isnt element of " + fnc.domain.toString());
			return null;
		}
		if (termdisp == Term.Disp.C) {
			return new Type("Collection");
		}
		else return null;
	}
	
	
	static private Type solveUnary(Operator op, Type a) {
		if (a.equals(BooleanLogic.genericType)) {
			if (op.equals(Op.not)) return a;
		}
		return null;
	}
	
	static private Type solveBinary(Type a, Operator op, Type b) {
		if (a.equals(b)) {
			if (a.equals(BooleanLogic.genericType)) {
				if (isin(op, new Operator[]{Op.and, Op.or, Op.implies})) return a;
			}
			if (a.equals(NaturalNumbers.genericType)) {
				if (isin(op, new Operator[]{Op.plus, Op.minus, Op.mult, Op.exp})) return a;
				if (isin(op, new Operator[]{Op.eq, Op.lt, Op.gt, Op.le, Op.ge, Op.ineq})) {
					return new Type(BooleanLogic.genericType);
				}
			}
			if (b.equals(Set.genericType)) {
				if (a.equals(b)) {
					if (isin(op, new Operator[]{Op.intersection, Op.union})) return a;
					if (isin(op, new Operator[]{Op.subset, Op.psubset, Op.eq})) return new Type(BooleanLogic.genericType);
				} else if (isin(op, new Operator[]{Op.in, Op.notin})) return new Type(BooleanLogic.genericType);
			}
		}
		return null;
	}
	
	public static boolean isin(Object t, Object[] list) {
		for (Object s: list) {
			if (t.equals(s)) return true;
		}
		return false;
	}

}
