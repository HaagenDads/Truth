package Core;

import java.util.ArrayList;

public class Body {

	ArrayList<String> body;
	
	public Body (ArrayList<String> body) {
		this.body = body;
	}
	
	public String get (int i) {
		return this.body.get(i);
	}
	
	public int size () {
		return this.body.size();
	}
	
	public ArrayList<ArrayList<String>> parseBody() {
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		ArrayList<String> proposition = new ArrayList<String>();
		
		int caseLevels = 0;
		for (String x: body) {
			if (x.equals("\\startcase")) {
				caseLevels += 1;
				proposition.add(x);
			}
			else if (x.equals("\\endcase")) {
				caseLevels -= 1;
				proposition.add(x);
				if (caseLevels == 0) {
					result.add(proposition);
					proposition = new ArrayList<String>();
				}
			}
			else if (caseLevels == 0 && isEndLine(x)) {
				proposition.add(getEndLine(x));
				result.add(proposition);
				proposition = new ArrayList<String>();
			} 
			else {
				proposition.add(x);
			}		
		}
		return result;
	}
	
	private String getEndLine(String token) {
		int backslash = 0;
		String result = "";
		for (char x: token.toCharArray()) {
			if (x == '\\') backslash += 1;
			else if (x == ';' && backslash % 2 == 0) return result;
			else backslash = 0;
			result += x;
		}
		return result;
	}
	
	private boolean isEndLine(String token) {
		int backslash = 0;
		for (char x: token.toCharArray()) {
			if (x == '\\') backslash += 1;
			else if (x == ';' && backslash % 2 == 0) return true;
			else backslash = 0;
		}
		return false;
	}
}
