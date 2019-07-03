package Core;

import javax.swing.JTextPane;

abstract public class StringOperations extends JTextPane {

	protected String join(String[] expression, String sep) {
		
		if (expression.length == 0) return "";
		String output = expression[0].toString();
		for (int i=1; i<expression.length; i++) output += sep + expression[i];
		
		return output;
	}
	
	protected int len(String s) {
		return replaceSymbols(s).length();
	}
	
	protected String replaceSymbols(String s) {
		String result = s;
		result = result.replace((CharSequence) "\\not ", (CharSequence) "¬");
		result = result.replace((CharSequence) "\\false", (CharSequence) "F");
		result = result.replace((CharSequence) "\\true", (CharSequence) "T");
		result = result.replace((CharSequence) "\\and", (CharSequence) "and");
		result = result.replace((CharSequence) "\\or", (CharSequence) "or");
		result = result.replace((CharSequence) "\\implies", (CharSequence) "→");
		return result;
		
	}
	
	protected String getTab(int ntab) {
		String result = "";
		while (ntab-- > 0) result += "    ";
		return result;
	}
	
	protected String repeat(String a, int n) {
		String result = "";
		while (n-- > 0) result += a;
		return result;
	}
}
