package Operation;

import java.util.ArrayList;
import Elements.*;

public class BooleanLogic {
	
	public static enum bool {TRUE, FALSE, BOOLEAN};
	public static final String genericType = "\\boolean";
	
	static public String applyUnaryLogic(String op, String a) throws ExceptionBooleanCasting  {
		if (a.equals(genericType)) return a;
		
		bool x = readString(a);
		if (op.equals("\\not")) {
			if (x.equals(bool.TRUE)) return "\\false";
			if (x.equals(bool.FALSE)) return "\\true";
		}
		else System.out.println("[FATAL] Couldnt match logic operator '" + op + "' to 'not' casting.");
		return null;
	}
	
	static public String applyBinaryLogic(String a, String op, String b) throws ExceptionBooleanCasting  {
		//if (a.equals(genericType) && readString(b) != null) return a;
		//if (b.equals(genericType) && readString(a) != null) return b;
		
		bool ab = readString(a);
		bool bb = readString(b);
		if (op.equals("\\and")) {
			if (ab.equals(bool.TRUE) && bb.equals(bool.TRUE)) return "\\true";
			else return "\\false";
		}
		else if (op.equals("\\or")) {
			if (ab.equals(bool.TRUE) || bb.equals(bool.TRUE)) return "\\true";
			else return "\\false";
		}
		else if (op.equals("\\implies")) {
			if (ab.equals(bool.FALSE) || bb.equals(bool.TRUE)) return "\\true";
			else return "\\false";
		}
		else { 
			System.out.println("[FATAL] Couldnt match logic operator '" + op + "' to 'and/or/implies' casting.");
			return "\\false";
		}
	}
	
	static public bool readString(String a) throws ExceptionBooleanCasting {
		if (a.equals("\\true")) return bool.TRUE;
		else if (a.equals("\\false")) return bool.FALSE;
		else if (a.equals(genericType)) return bool.BOOLEAN;
		else {
			throw new ExceptionBooleanCasting(a);
		}
	}
	
	
	static public boolean validatePartition(Variable casevar, ArrayList<Statement> cases, ArrayList<Type> types) {
		if (cases.size() != 2) System.out.println("Cases partition can only have two cases.");
		
		Statement exp1 = new Statement(new Link("="), casevar.name, "\\true");
		Statement exp2 = new Statement(new Link("="), casevar.name, "\\false");
		
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
		try {
			if (readString(t) == bool.TRUE || readString(t) == bool.FALSE) return true;
		} catch (ExceptionBooleanCasting e) {}
		return false;
	};
}
