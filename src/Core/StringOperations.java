package Core;

import java.util.ArrayList;

import javax.swing.JTextPane;

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
		result = result.replace((CharSequence) "\\not ", (CharSequence) "¬");
		result = result.replace((CharSequence) "\\false", (CharSequence) "\\$bold{F}");
		result = result.replace((CharSequence) "\\true", (CharSequence) "\\$bold{T}");
		result = result.replace((CharSequence) "\\and", (CharSequence) "and");
		result = result.replace((CharSequence) "\\or", (CharSequence) "or");
		result = result.replace((CharSequence) "\\implies", (CharSequence) "→");
		result = result.replace((CharSequence) "\\eq", (CharSequence) "≡");
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
