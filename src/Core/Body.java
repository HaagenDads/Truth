package Core;

import java.util.ArrayList;
import java.util.Iterator;

import Elements.ArrayString.Sequence;
import Elements.ArrayString;

public class Body {
	
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
		try {
			ArrayList<ArrayString> lines = splitSequences(texttokens);
			for (ArrayString as: lines) {
				as = as.sepwithComma();
				body.add(splitPrecedence(as));
			}
		} catch (ExceptionSequenceParsing e) { System.out.println(e.printError()); }
	}
	
	private Sequence splitPrecedence (ArrayString as) {
		if (as.get(0).equals("\\case")) {
			Sequence res = as.new Sequence();
			res.addSubbody(as);
			return res;
		} else if (as.get(0).equals("\\let")) {
			Sequence res = as.new Sequence();
			res.addAssignment(as);
			return res;
		}
		return as.splitPrecedence();
	}
	
	private ArrayList<ArrayString> splitSequences (ArrayString texttokens) throws ExceptionSequenceParsing {
		ArrayList<ArrayString> result = new ArrayList<ArrayString>();
		ArrayString buffer = new ArrayString();
		
		String x;
		Iterator<String> it = texttokens.iterator();
		while (it.hasNext()) {
			x = it.next();
			if (! (x==null || x=="")) {
				x = x.trim();
				
				/*
				if (x.equals("\\case")) {  // sub body must begin with the startcase token
					int caselevel = 1;
					buffer = new ArrayString(x);
					do {
						x = it.next();
						buffer.add(x);
						if (x.equals("\\startcase")) caselevel++;
						else if (x.equals("\\endcase") && --caselevel == 0) {
							result.add(buffer);
							buffer = new ArrayString();
							break;
						}
					} while (it.hasNext());
					if (caselevel > 0) throw new ExceptionCouldntCloseCases();
				}*/
				
				if (x.equals("{")) {
					int bracketlevel = 1;
					buffer.add(x); // we keep adding it to the \let statement
					do {
						x = it.next();
						buffer.add(x);
						if (x.equals("{")) bracketlevel++;
						else if (x.equals("}") && --bracketlevel == 0) {
							result.add(buffer);
							buffer = new ArrayString();
							break;
						}
					} while (it.hasNext());
					if (bracketlevel > 0) throw new ExceptionCouldntCloseDefinitionBrackets();
				}
				else {
					String endline = StringOperations.getEndLine(x);
					if (endline != null) {
						buffer.add(endline);
						result.add(buffer);
						buffer = new ArrayString();
					} 
					else buffer.add(x);
				}
			}
		}
		return result;
	}
	
	
	static abstract public class ExceptionSequenceParsing extends Exception {
		abstract String printError ();
	};
	/*
	static public class ExceptionCouldntCloseCases extends ExceptionSequenceParsing {
		String printError() { return "Couldn't close cases while parsing. Missing an '\\endcase' token."; }
	};*/
	static public class ExceptionCouldntCloseDefinitionBrackets extends ExceptionSequenceParsing {
		String printError() { return "Couldn't close definition brackets while parsing. Missing a '}' token."; }
	};
	
}
