package Operation;

public class Operator {

	static public boolean isQuantifier(String s) {
		if (s==null) return false;
		String[] opSet = new String[]{"\\exists", "\\forall"};
		for (String op: opSet) {
			if (s.equals(op)) return true;
		}
		return false;
	}
	static public boolean isUnary(String s) {
		if (s==null) return false;
		String[] opSet = new String[]{"\\not"};
		for (String op: opSet) {
			if (s.equals(op)) return true;
		}
		return false;
	}
	static public boolean isBinary(String s) {
		if (s==null) return false;
		String[] opSet = new String[]{"\\or", "\\and", "\\implies", "=", ">", "<", "<=", ">=", "!="};
		for (String op: opSet) {
			if (s.equals(op)) return true;
		}
		return false;
	}
	
	static public boolean isOperator(String s) {
		return isUnary(s) || isBinary(s) || isQuantifier(s);
	}
	

}
