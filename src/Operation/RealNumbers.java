package Operation;


public class RealNumbers {
	
	public static final String genericType = "\\setreal";
	
	static public String applyBinaryLogic (String a, Operator op, String b) {
		RealItem stra = readString(a);
		RealItem strb = readString(b);
		
		/*
		if ((a.equals(genericType) && stra != null)
		 || (b.equals(genericType) && strb != null)) {
			for (String o: oplist) if (o.equals(op)) return genericType;
			for (String o: opcomparaison) if (o.equals(op)) return BooleanLogic.genericType;
			return null;
		}*/
		
		if (stra != null && strb != null) {
			if (op.equals(Op.plus)) return "" + (stra.d + strb.d);
			if (op.equals(Op.minus)) return "" + (stra.d - strb.d);
			if (op.equals(Op.mult)) return "" + (stra.d * strb.d);
			// TODO only natural numbers
			if (op.equals(Op.exp)) {
				int res = 1;
				for (int i=0; i<strb.d; i++) res *= stra.d;
				return "" + res;
			}
			if (op.equals(Op.div)) {
				if (strb.d != 0) return "" + (stra.d / strb.d);
				return null;
			}
			
			if (op.equals(Op.eq)) return toBooleanString(stra.d == strb.d);
			if (op.equals(Op.ineq)) return toBooleanString(stra.d != strb.d);
			if (op.equals(Op.lt)) return toBooleanString(stra.d < strb.d);
			if (op.equals(Op.le)) return toBooleanString(stra.d <= strb.d);
			if (op.equals(Op.gt)) return toBooleanString(stra.d > strb.d);
			if (op.equals(Op.ge)) return toBooleanString(stra.d >= strb.d);
			
			return null;
		} else return null;
	}
	
	static private String toBooleanString (boolean b) {
		if (b) return "\\true";
		return "\\false";
	}
	
	static private RealItem readString(String a) {
		//if (a.equals(genericType)) return new RealItem(true);

		boolean negative = a.charAt(0) == '-';
		if (negative) a = a.substring(1);
		
		String[] realstr = a.split("\\.");
		
		int whole = getNumeric(realstr[0]);
		if (whole == -1) return null;
		if (realstr.length == 1 && realstr[0] != "") {
			return new RealItem(negative, whole);
		}
		else if (realstr.length == 2) {
			int decim = getNumeric(realstr[1]);
			if (decim == -1) return null;
			return new RealItem(negative, whole, decim);
		} else return null;
	}
	
	static public boolean isValid (String t) {
		if (readString(t) != null) return true;
		return false;
	}
	
	static private class RealItem {
		double d;
		public RealItem (boolean negative, int whole) {
			if (negative) d = -whole;
			else d = whole;
		}
		public RealItem (boolean negative, int whole, int decim) {
			if (negative) d = -whole;
			else d = whole;
			double partial = (double) decim;
			while (partial >= 1) partial = partial / 10.0;
			d += partial;
		}
	}
	
	
	static protected int getNumeric(String s) {
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
}
