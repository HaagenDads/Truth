package Operation;

public class Operator {

	static private boolean isPartOfList(String s, String[] opSet) {
		if (s==null) return false;
		for (String op: opSet) {
			if (s.equals(op)) return true;
		}
		return false;
	}
	static public boolean isQuantifier(String s) {
		String[] opSet = new String[]{"\\exists", "\\forall"};
		return isPartOfList(s, opSet);
		
	}
	static public boolean isUnary(String s) {
		String[] opSet = new String[]{"\\not"};
		return isPartOfList(s, opSet);
	}
	static public boolean isBinary(String s) {
		String[] opSet = new String[]{"\\or", "\\and", "\\implies", "=", ">", "<", "<=", ">=", "!="};
		return isPartOfList(s, opSet);
	}
	
	static public boolean isOperator(String s) {
		return isUnary(s) || isBinary(s) || isQuantifier(s);
	}
	
	static public boolean isCommutative(String s) {
		String[] opSet = new String[]{"\\or", "\\and", "=", "!=", "+", "-", "*", "\\eq"};
		return isPartOfList(s, opSet);
	}

}
