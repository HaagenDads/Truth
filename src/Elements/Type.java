package Elements;

import Core.Demonstration;
import Operation.BooleanLogic;
import Core.Theorem;
import Operation.NaturalNumbers;
import Operation.Op;
import Operation.Operator;
import Operation.RealNumbers;

public class Type {

	public static final Type Real = new Type(RealNumbers.genericType);
	public static final Type Nat = new Type(NaturalNumbers.genericType);
	public static final Type Bool = new Type(BooleanLogic.genericType);
	public static final Type Sets = new Type(Set.genericType);
	

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
	
	static public boolean matchtypes (Type deftype, Type demotype) {
		if (deftype.type == null || demotype.type == null) return false;
		if (deftype.equals(demotype)) return true;
		if (deftype.type.equals(RealNumbers.genericType)) return matchtypes(Nat, demotype);
		return false;
	}
	
	static public boolean matchtypes (Term t1, Term t2, Theorem thm) throws ExceptionTypeUnknown {
		return matchtypes(t1.getType(thm), t2.getType(thm));
	}

	static public boolean matchtypes (Term t, Type type, Theorem thm) throws ExceptionTypeUnknown {
		return matchtypes(t.getType(thm), type);
	}
	
	static public Type computeType(Term t, Theorem thm) throws ExceptionTypeUnknown {
		//System.out.println(":getting type of: " + t.toString());
		Term.Disp termdisp = t.getDisposition();
		if (termdisp == Term.Disp.F) {
			if (isin(t.s, new String[]{BooleanLogic.genericType, NaturalNumbers.genericType, 
					                   Set.genericType, RealNumbers.genericType})) return Sets;
			Variable v = thm.getVariable(t.s);
			if (v != null) return v.type;
			if (BooleanLogic.isValid(t.s)) return Bool;
			if (NaturalNumbers.isValid(t.s)) return Nat;
			if (RealNumbers.isValid(t.s)) return Real;
			if (Set.isValid(t.s)) return Sets;
			throw new ExceptionVariableCouldntBeFound(t);
		}
		if (termdisp == Term.Disp.QTT) return Bool;
		if (termdisp == Term.Disp.DEF) return Bool;
		if (termdisp == Term.Disp.OT) {
			Type x = t.get(1).getType(thm);
			return solveUnary(Op.getOperator(t.get(0).s), x);
		}
		if (termdisp == Term.Disp.TOT) {
			Type x = t.get(0).getType(thm);
			Type y = t.get(2).getType(thm);
			return solveBinary(x, Op.getOperator(t.get(1).s), y);
		}
		if (termdisp == Term.Disp.FC) {
			Function fnc = (Function) (thm.getVariable(t.get(0).s));
			Collection coll = (Collection) (t.get(1));
			if (fnc.domain.isElement(coll, thm)) return fnc.image;
			else throw new ExceptionCollectionOutOfDomain(fnc, coll);
		}
		if (termdisp == Term.Disp.C) {
			return new Type("Collection");
		}
		else throw new ExceptionTypeUnknown(t);
	}
	
	
	static private Type solveUnary(Operator op, Type a) throws ExceptionCouldntResolveUnaryType {
		if (a.equals(Bool)) {
			if (op.equals(Op.not)) return a;
		}
		else if (a.equals(Real)) {
			if (op.equals(Op.minus)) return a;
		}
		if (a.equals(Nat)) return solveUnary(op, Real);
		throw new ExceptionCouldntResolveUnaryType(op, a);
	}
	
	static private Type solveBinary(Type a, Operator op, Type b) throws ExceptionCouldntResolveBinaryType {
		if (a.equals(b)) {
			if (a.equals(Bool)) {
				if (isin(op, new Operator[]{Op.and, Op.or, Op.implies, Op.then, Op.equiv})) return a;
			}
			if (a.equals(Real) || a.equals(Nat)) {
				if (isin(op, new Operator[]{Op.plus, Op.mult, Op.exp})) return a;
				if (isin(op, new Operator[]{Op.eq, Op.lt, Op.gt, Op.le, Op.ge, Op.ineq})) {
					return Bool;
				}
			}
			if (b.equals(Set.genericType)) {
				if (a.equals(b)) {
					if (isin(op, new Operator[]{Op.intersection, Op.union})) return a;
					if (isin(op, new Operator[]{Op.subset, Op.psubset, Op.eq})) return Bool;
				} else if (isin(op, new Operator[]{Op.in, Op.notin})) return Bool;
			}
		} else {
			if (a.equals(Nat)) return solveBinary(Real, op, b);
			if (b.equals(Nat)) return solveBinary(a, op, Real);
		}

		throw new ExceptionCouldntResolveBinaryType(op, a, b);
	}
	
	private static boolean isin(Object t, Object[] list) {
		for (Object s: list) {
			if (t.equals(s)) return true;
		}
		return false;
	}


	static public class ExceptionTypeUnknown extends Demonstration.ExceptionComprehension {
		Term t;
		ExceptionTypeUnknown(Term t) { this.t = t; }
		public String errorType() { return "Type";}
		public String errorMessage() { return "Type couldn't be understood from term: " + t.toString(); }
	}

	static public class ExceptionCollectionOutOfDomain extends ExceptionTypeUnknown {
		Function f;
		ExceptionCollectionOutOfDomain(Function f, Collection c) { super(c); this.f = f; }
		public String errorMessage() { return "Collection " + t.toString() + " isn't element of domain of " + f.name;}
	}

	static public class ExceptionVariableCouldntBeFound extends ExceptionTypeUnknown {
		ExceptionVariableCouldntBeFound(Term t) { super(t); }
		public String errorMessage() { return "Type couldn't be infered from flat unknown term " + t.toString(); }
	}

	static public class ExceptionCouldntResolveUnaryType extends ExceptionTypeUnknown {
		Type type; Operator op;
		ExceptionCouldntResolveUnaryType(Operator op, Type type) { super(op); this.type = type; }
		public String errorMessage() { return "Couldn't resolve unary type " + type + " with operator " + t.toString();}
	}

	static public class ExceptionCouldntResolveBinaryType extends ExceptionTypeUnknown {
		Type t1, t2; Operator op;
		ExceptionCouldntResolveBinaryType(Operator op, Type type1, Type type2) { super(op); this.t1 = type1; this.t2 = type2; }
		public String errorMessage() { return "Couldn't resolve binary types " + t1 + " and " + t2 + " with operator " + t.toString();}
	}
}


