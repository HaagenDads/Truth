package Operation;

import java.util.ArrayList;
import Elements.*;

public class BooleanLogic {
	
	public static enum bool {TRUE, FALSE, BOOLEAN};
	public static final String genericType = "\\boolean";
	
	static public String applyUnaryLogic(Operator op, String a) {

		bool x = readString(a);
		if (x != null && op.equals(Op.not)) {
			if (x.equals(bool.TRUE)) return "\\false";
			if (x.equals(bool.FALSE)) return "\\true";
		}
		return null;
	}
	
	static public String applyBinaryLogic(String a, Operator op, String b) {
		//if (a.equals(genericType) && readString(b) != null) return a;
		//if (b.equals(genericType) && readString(a) != null) return b;
		
		bool ab = readString(a);
		bool bb = readString(b);
		if (ab == null || bb == null) return null;
		if (op.equals(Op.and)) {
			if (ab.equals(bool.TRUE) && bb.equals(bool.TRUE)) return "\\true";
			else return "\\false";
		}
		else if (op.equals(Op.or)) {
			if (ab.equals(bool.TRUE) || bb.equals(bool.TRUE)) return "\\true";
			else return "\\false";
		}
		else if (op.equals(Op.implies)) {
			if (ab.equals(bool.FALSE) || bb.equals(bool.TRUE)) return "\\true";
			else return "\\false";
		}
		else if (op.equals(Op.equiv)) {
			if (ab.equals(bb)) return "\\true";
			return "\\false";
		}
		return null;
	}
	
	static public bool readString(String a) {
		if (a.equals("\\true")) return bool.TRUE;
		else if (a.equals("\\false")) return bool.FALSE;
		else if (a.equals(genericType)) return bool.BOOLEAN;
		else return null;
	}
	
	
	static public boolean validatePartition(Variable casevar, ArrayList<Statement> cases, ArrayList<Type> types) {
		if (cases.size() != 2) System.out.println("Cases partition can only have two cases.");
		
		Statement exp1 = new Statement(new Link(Op.eq), casevar.name, "\\true");
		Statement exp2 = new Statement(new Link(Op.eq), casevar.name, "\\false");
		
		if (exp1.equals(cases.get(0)) && exp2.equals(cases.get(1))) return true;
		if (exp1.equals(cases.get(1)) && exp2.equals(cases.get(0))) return true;
		
		System.out.println("Couldnt match cases to expected cases for boolean variables");
		return false;
	}
	
	
	static public class ExceptionBooleanCasting extends Exception {
		String a;
		public ExceptionBooleanCasting(String a) {
			this.a = a;
		}
		public void explain () { System.out.println("Couldnt match '" + a + "' to true/false cast"); }
	}


	static public boolean isValid(String t) {
		if (readString(t) == bool.TRUE || readString(t) == bool.FALSE) return true;
		return false;
	};
}
