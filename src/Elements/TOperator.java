package Elements;

public class TOperator extends Token{
	
	boolean isUnary;
	String[] signature;
	public TOperator(String value) {
		super(value);
		determineUnary();
	}
	
	public boolean isOperator() { return true; }

	private void determineUnary() {
		if (value.equals("\\not")) {
			isUnary = true;
			signature = new String[]{"\\boolean", "\\boolean"};
		} else if (value.equals("\\and")) {
			isUnary = false;
			signature = new String[]{"\\boolean", "\\boolean", "\\boolean"};
		} else if (value.equals("\\or")) {
			isUnary = false;
			signature = new String[]{"\\boolean", "\\boolean", "\\boolean"};
		}
	}
	
}
