package Core;

import java.util.ArrayList;

import javax.swing.JTextPane;

import Elements.ArrayString;

abstract public class StringOperations extends JTextPane {

	protected String join(String[] expression, String sep) {
		
		if (expression.length == 0) return "";
		String output = expression[0].toString();
		for (int i=1; i<expression.length; i++) output += sep + expression[i];
		
		return output;
	}
	
	protected int len(String s) {
		ArrayList<Styledsequence> list = parseVisualText(s);
		int length = 0;
		for (Styledsequence ss: list) {
			length += ss.sequence.length();
		}

		return length;
	}
	
	
	protected ArrayList<Styledsequence> parseVisualText(String line) {
		
		ArrayList<Styledsequence> result = new ArrayList<Styledsequence>();
		
		String rs = replaceSymbols(line);
		int nextstyle = rs.indexOf("\\$");
		while (nextstyle != -1) {
			
			if (nextstyle > 0) result.add(new Styledsequence(rs.substring(0, nextstyle)));
			rs = rs.substring(nextstyle+2);
			
			int endstylename = rs.indexOf('{');
			String stylename = rs.substring(0, endstylename);
			
			rs = rs.substring(endstylename+1);
			String styledsequence = getStyleSequence(rs);
			
			rs = rs.substring(styledsequence.length()+1);
			nextstyle = rs.indexOf("\\$");
			
			result.add(new Styledsequence(styledsequence, stylename));
		}
		result.add(new Styledsequence(rs));
		return result;
	}
	
	private String getStyleSequence (String s) {
		int nbopenedbracket = 1;
		String sequence = "";
		for (char c: s.toCharArray()) {
			if (c == '{') nbopenedbracket += 1;
			if (c == '}') nbopenedbracket -= 1;
			if (nbopenedbracket == 0) return sequence;
			sequence += Character.toString(c);
		}
		return null;
	}
	
	protected class Styledsequence {
		public String style;
		public String sequence;
		
		Styledsequence (String sequence, String style) {
			this.style = style;
			this.sequence = sequence;
		}
		Styledsequence (String sequence) {
			this.style = "";
			this.sequence = sequence;
		}
	}
	
	protected String replaceSymbols(String s) {
		String result = s;
		String[] from = new String[]{"\\not ", "\\false", "\\true", "\\and", "\\or", "\\implies", "\\eq", 
				"\\forall", "\\exists", "\\setnatural", "\\in", "\\notin", "\\then", "\\subset", "\\psubset", "\\notsubset", 
				"\\intersection", "\\union", "\\emptyset", "\\pset", "->", "\\setreal"};
		String[] to = new String[]{"¬", "\\$bold{F}", "\\$bold{T}", "∧", "∨", "→", "≡", "∀", "∃", "ℕ", "∈", "∉", "⇒", "⊆", "⊂", "⊆ ?", "⋂", "⋃", "∅",
				"\\$ital{P", "→", "ℝ"};
		for (int i=0; i<from.length; i++) {
			result = result.replace((CharSequence) from[i], (CharSequence) to[i]);
		}
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
	
	
	/**
	 *  General String operations
	 */

	/** Build string from array of characters. */
	public static String getString (ArrayList<Character> list) {    
	    StringBuilder builder = new StringBuilder(list.size());
	    for(Character ch: list) { builder.append(ch); }
	    return builder.toString();
	}
	
	public static int getParenthesisDifferential(String x) {
		int result = 0;
		for (char c: x.toCharArray()) {
			if (c == '(' || c == '[') result++;
			if (c == ')' || c == ']') result--;
		}
		return result;
	}

	public static ArrayString split(String text, char[] cs) {
		ArrayString result = new ArrayString();
		String buffer = "";
		for (char c: text.toCharArray()) {
			if (isPartOfList(c, cs)) {
				if (!buffer.equals(""))	result.add(buffer);
				buffer = "";
			} else {
				buffer += c;
			}
		}
		if (!buffer.equals(""))	result.add(buffer);
		return result;
	}
	
	static private boolean isPartOfList(char t, char[] list) {
		for (char s: list) {
			if (t == s) return true;
		}
		return false;
	}
	
	static public String getEndLine(String token) {
		int backslash = 0;
		String result = "";
		for (char x: token.toCharArray()) {
			if (x == '\\') backslash += 1;
			else if (x == ';' && backslash % 2 == 0) return result;
			else backslash = 0;
			result += x;
		}
		return null;
	}
	
}
