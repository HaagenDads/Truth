package Elements;

public class Token {

	public String value;
	public Token (String value) {
		this.value = value;
	}
	public String toString() { return value; }
	public boolean equals(String s) { return value.equals(s); }
	public boolean equals(Token t) { return value.equals(t.value); }
	
	public boolean isOperator() { return false; }
	public boolean isVariable() { return false; }
	public boolean isToken() { return true; }
	
	/*
	public static Token classify(String x) {
		if (!(x.charAt(0) == '\\')) return new TVariable(x);
		for (String x: String[]{"\\and", "\\or", "\\not"}) {
			
		}
	}*/
	
	
}
