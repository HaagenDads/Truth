package Operation;

import java.util.HashMap;

public final class Op {
	
	public static final Operator not = new Operator("\\not");
	public static final Operator and = new Operator("\\and");
	public static final Operator or = new Operator("\\or");
	public static final Operator implies = new Operator("\\implies");
	public static final Operator iff = new Operator("\\iff");
	public static final Operator equiv = new Operator("\\eq");
	public static final Operator then = new Operator("\\then");
	public static final Operator assign = new Operator(":=");
	
	public static final Operator plus = new Operator("+");
	public static final Operator minus = new Operator("-");
	public static final Operator mult = new Operator("*");
	public static final Operator div = new Operator("/");
	public static final Operator exp = new Operator("^");

	public static final Operator eq = new Operator("=");
	public static final Operator lt = new Operator("<");
	public static final Operator gt = new Operator(">");
	public static final Operator le = new Operator("<=");
	public static final Operator ge = new Operator(">=");
	public static final Operator ineq = new Operator("!=");
	
	public static final Operator intersection = new Operator("\\intersection");
	public static final Operator union = new Operator("\\union");
	public static final Operator subset = new Operator("\\subset");
	public static final Operator psubset = new Operator("\\psubset");
	public static final Operator in = new Operator("\\in");
	public static final Operator notin = new Operator("\\notin");
	
	public static final Operator exists = new Operator("\\exists");
	public static final Operator forall = new Operator("\\forall");
	
	private static final HashMap<String, Operator> hashmap = new HashMap<String, Operator>() {{
		Operator[] oplist = new Operator[]{not, and, or, implies, iff, equiv, then, assign,
										   plus, minus, mult, div, exp,
										   eq, lt, gt, le, ge, ineq,
										   intersection, union, subset, psubset, in, notin,
										   exists, forall};
		String[] strlist = new String[]{"\\not", "\\and", "\\or", "\\implies", "\\iff", "\\eq", "\\then", ":=",
										"+", "-", "*", "/", "^",
										"=", "<", ">", "<=", ">=", "!=",
										"\\intersection", "\\union", "\\subset", "\\psubset", "\\in", "\\notin",
										"\\exists", "\\forall"};
		
		for (int i=0; i<oplist.length; i++) put(strlist[i], oplist[i]); 
	}};
	
	
	public static Operator getOperator(String s) {
		return hashmap.get(s);
	}

	

}
