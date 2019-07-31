package Core;

import java.util.ArrayList;

import Elements.ArrayString.Sequence;
import Operation.Op;
import Operation.Operator;
import Elements.ArrayString;

public class Body {

	static private final Operator[] precedence1 = new Operator[]{Op.equiv, Op.then};
	static private final Operator[] precedence2 = new Operator[]{Op.eq, Op.gt, Op.ge, Op.lt, Op.le, Op.ineq,
																 Op.in, Op.psubset, Op.subset};
	
	ArrayList<Sequence> body;
	
	public Body (String fulltext) {
		ArrayString texttokens = StringOperations.split(fulltext, new char[]{' ', '\n', '\t'});
		init(texttokens);
	}
	
	public Body (ArrayString subbody) {
		init(subbody);
	}
	
	private void init (ArrayString texttokens) {
		body = new ArrayList<Sequence>();
		ArrayList<ArrayString> lines = splitSequences(texttokens);
		for (ArrayString as: lines) body.add(splitPrecedence(as));
	}
	
	private Sequence splitPrecedence (ArrayString as) {
		if (as.get(0).equals("\\startcase")) {
			Sequence res = as.new Sequence();
			res.addSubbody(as);
			return res;
		} else if (as.get(0).equals("\\let")) {
			Sequence res = as.new Sequence();
			res.addAssignment(as);
			return res;
		}
		for (Operator[] oplist: new Operator[][]{precedence1, precedence2}) {
			Sequence seq = as.splitPrecedence(oplist);
			if (seq.size() > 1) return seq;
		}
		Sequence res = as.new Sequence();
		res.add(as, null);
		return res;
	}
	
	private ArrayList<ArrayString> splitSequences (ArrayString texttokens) {
		ArrayList<ArrayString> result = new ArrayList<ArrayString>();
		ArrayString buffer = new ArrayString();
		
		int caseLevels = 0;
		for (String x: texttokens) {
			if (! (x==null || x=="")) {
				x = x.trim();
				if (x.equals("\\startcase")) {
					caseLevels += 1;
					buffer = new ArrayString(); // sub body must begin with the startcase token
					buffer.add(x);
				}
				else if (x.equals("\\endcase")) {
					caseLevels -= 1;
					buffer.add(x);
					if (caseLevels == 0) {
						result.add(buffer);
						buffer = new ArrayString();
					}
				}
				else if (caseLevels == 0 && isEndLine(x)) {
					buffer.add(getEndLine(x));
					result.add(buffer);
					buffer = new ArrayString();
				} 
				else {
					buffer.add(x);
				}	
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
