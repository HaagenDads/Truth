package Operation;

import java.util.ArrayList;

import Elements.Statement;
import Elements.Type;
import Elements.Variable;

public class NaturalNumbers {
	
	public static final String genericType = "\\setnatural";
	public static final Operator[] oplist = new Operator[]{Op.plus, Op.minus, Op.mult, Op.exp};
	public static final Operator[] opcomparaison = new Operator[]{Op.eq, Op.lt, Op.gt, Op.le, Op.ge, Op.ineq};
	
	
	static public String applyBinaryLogic (String a, Operator op, String b) {
		NatItem stra = readString(a);
		NatItem strb = readString(b);
		
		/*
		if ((a.equals(genericType) && stra != null)
		 || (b.equals(genericType) && strb != null)) {
			for (String o: oplist) if (o.equals(op)) return genericType;
			for (String o: opcomparaison) if (o.equals(op)) return BooleanLogic.genericType;
			return null;
		}*/
		
		if (stra != null && strb != null) {
			if (op.equals(Op.plus)) return "" + (stra.v + strb.v);
			if (op.equals(Op.minus)) return "" + (stra.v - strb.v);
			if (op.equals(Op.mult)) return "" + (stra.v * strb.v);
			if (op.equals(Op.exp)) {
				int res = 1;
				for (int i=0; i<strb.v; i++) res *= stra.v;
				return "" + res;
			}
			
			if (op.equals(Op.eq)) return toBooleanString(stra.v == strb.v);
			if (op.equals(Op.ineq)) return toBooleanString(stra.v != strb.v);
			if (op.equals(Op.lt)) return toBooleanString(stra.v < strb.v);
			if (op.equals(Op.le)) return toBooleanString(stra.v <= strb.v);
			if (op.equals(Op.gt)) return toBooleanString(stra.v > strb.v);
			if (op.equals(Op.ge)) return toBooleanString(stra.v >= strb.v);
			
			return "null";
		} else return null;
	}
	
	static private String toBooleanString (boolean b) {
		if (b) return "\\true";
		return "\\false";
	}
	
	/* Only three possible conclusion:
	 * 		1- null (no conclusion)
	 * 		2- exists (there is at least one example where it is true)
	 * 		3- forall (the full partition was validated) 
	 */
	static public String validatePartition (Variable casevar, ArrayList<Statement> cases, ArrayList<Type> types) {

		// Ordering and asserting that the casevar is in the first position with a valid operator
		ArrayList<Statement> lesserthan = new ArrayList<Statement>(); 
		ArrayList<Statement> equalsto = new ArrayList<Statement>();
		ArrayList<Statement> largerthan = new ArrayList<Statement>();
		for (Statement st: cases) {
			if (!st.lside.equals(casevar.name)) return null;
			// TODO case for x  = y + 3
			// 				 x != y + 3
			if (!st.rside.isShallow()) return null;
			if (st.link.equals("<")) lesserthan.add(st);
			else if (st.link.equals("=")) equalsto.add(st);
			else if (st.link.equals(">")) lesserthan.add(st);
			else if (st.link.equals("<=")) {
				lesserthan.add(st);
				equalsto.add(st);
			}
			else if (st.link.equals(">=")) {
				largerthan.add(st);
				equalsto.add(st);
			}
			else return null;
		}
		
		NatPartition numerical = new NatPartition();
		for (Statement st: lesserthan) {
			// TODO only shallow assignations for now
			int num = getNumeric(st.rside.s);
			if (num >= 0) numerical.addupper(num);
		}
		for (Statement st: equalsto) {
			int num = getNumeric(st.rside.s);
			if (num >= 0) numerical.addsingle(num);
		}
		for (Statement st: largerthan) {
			int num = getNumeric(st.rside.s);
			if (num >= 0) numerical.addlower(num);
		}
		
		if (numerical.isVacuous()) {
			return "exists";
		} else {
			return "forall";
		}
		
	}
	
	static private class NatPartition {
		int upperbound;
		int lowerbound;
		boolean upperincluded;
		boolean vacuous;
		
		public NatPartition () {
			upperbound = 0;
			lowerbound = -1;
			upperincluded = false;
			vacuous = false;
		}
		public void addupper(int bound) {
			if (bound > upperbound) {
				upperincluded = false;
				upperbound = bound;
			}
		}
		public void addsingle(int single) {
			if (upperbound == single) {
				upperincluded = true;
			} else if (upperbound < single) {
				vacuous = true;
			}
		}
		public void addlower(int bound) {
			if (bound < lowerbound) {
				lowerbound = bound;
			}
		}
		public boolean isVacuous() {
			if (vacuous) return true;
			if (upperbound > lowerbound && lowerbound > -1) return true;
			if (upperbound == lowerbound && upperincluded) return true;
			return false;
		}
	}
	
	static public NatItem readString(String a) {
		if (a.equals(genericType)) return new NatItem(true);
		int value = getNumeric(a);
		if (value >= 0) return new NatItem(value);
		else return null;
	}
	
	static private int getNumeric(String s) {
		s = s.trim();
		int result = 0;
		for (char c: s.toCharArray()) {
			if (c >= 48 && c <= 57) {
				result *= 10;
				result += c - 48;
			} else {
				return -1;
			}
		}
		return result;
	}
	
	
	static private class NatItem {
		int v;
		public NatItem (int v) {
			this.v = v;
		}
		public NatItem (boolean b) {
			v = -1;
		}
	}
	
	static public class ExceptionNaturalNumbersCasting extends Exception{
		String a;
		public ExceptionNaturalNumbersCasting(String a) {
			this.a = a;
		}
		public void explain () { System.out.println("Couldnt match '" + a + "' to true/false cast"); }
	};
	
	static public boolean isValid (String t) {
		if (getNumeric(t) >= 0) return true;
		return false;
	}
	
}
