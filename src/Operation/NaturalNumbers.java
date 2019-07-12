package Operation;


public class NaturalNumbers {
	
	public static final String genericType = "\\setnatural";
	public static final String[] oplist = new String[]{"+", "-"};
	public static final String[] opcomparaison = new String[]{"=", "<", ">", "<=", ">=", "!="};
	
	
	static public String applyBinaryLogic(String a, String op, String b) throws ExceptionNaturalNumbersCasting  {
		NatItem stra = readString(a);
		NatItem strb = readString(b);
		
		if ((a.equals(genericType) && stra != null)
		 || (b.equals(genericType) && strb != null)) {
			for (String o: oplist) if (o.equals(op)) return genericType;
			for (String o: opcomparaison) if (o.equals(op)) return BooleanLogic.genericType;
			return null;
		}
		
		if (stra != null && strb != null) {
			if (op.equals("+")) return "" + (stra.v + strb.v);
			if (op.equals("-")) return "" + (stra.v - strb.v);
			else { 
				System.out.println("[FATAL] Couldnt match natural numbers operator '" + op + "' to '+/-' casting.");
				return "null";
			}
		} else throw new ExceptionNaturalNumbersCasting(a + " " + op + " " + b);
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
}
