package Elements;

import java.util.ArrayList;
import java.util.Arrays;

import Core.StringOperations;
import Elements.Term.TermSynthaxException;
import Operation.Operator;

public class ArrayString extends ArrayList<String>{
	
	public ArrayString (){
		super();
	}

	public ArrayString(String[] var) {
		super();
		addAll(Arrays.asList(var));
	}

	/*
	public ArrayString (ArrayList<String> input) {
		super();
		addAll(input);
	}
	public ArrayString(String x) {
		super();
		add(x);
	}*/
	
	public void removeLast () {
		remove(size()-1);
	}

	/*
	public String[] toStringlist() {
		String[] res = new String[size()];
		for (int i=0; i<size(); i++) res[i] = get(i);
		return res;
	}*/

	/** Levels array string with '(', ')', ',' and ':'. */
	public ArrayString addSpacing () {
		ArrayString commasep = new ArrayString();
		boolean foundchar;
		for (String s: this) {
			ArrayList<Character> newstr = new ArrayList<>();
			foundchar = false;
			for (char c: s.toCharArray()) {
				if (c == '(' || c == ')' || c == ':') {
					foundchar = true;
					commasep.add(StringOperations.getString(newstr));
					commasep.add(Character.toString(c));
					newstr = new ArrayList<>();
				}
				else {
					newstr.add(c);
					if (c == ',') {						// takes ['x,y,z'] into ['x,', 'y,', 'z']
						foundchar = true;
						commasep.add(StringOperations.getString(newstr));
						newstr = new ArrayList<>();
					}
				}
			}
			if (!foundchar) commasep.add(s); // fasttrack
			else if (newstr.size() != 0) commasep.add(StringOperations.getString(newstr));
		}
		return commasep;
	}

	/*
	public boolean isSurroundedByParenthesis() {
		if (get(0).charAt(0) != '(') return false;
		int openedParenthesis = 0;
		for (int i=0; i<size(); i++) {
			char[] x = get(i).trim().toCharArray();
			for (int j=0; j<x.length; j++) {
				if (x[j] == '(') openedParenthesis++;
				if (x[j] == ')') {
					openedParenthesis--;
					if (openedParenthesis == 0) return (i==(size()-1)) && (j==x.length-1);
					if (openedParenthesis < 0) System.out.println("[FATAL] Error in parenthesis comprehension.");
				}
			}
		}
		System.out.println("[FATAL] Error in parenthesis comprehension.");
		return false;		
	}
	
	public void unpeelParenthesis() {
		while (isSurroundedByParenthesis()) {
			set(0, get(0).substring(1));
			String lastString = get(size()-1);
			set(size()-1, lastString.substring(0, lastString.length()-1));
		}
	}*/

	public void removeSpacedParenthesis () {
		while (get(0).equals("(") && get(size()-1).equals(")")) {
			int opndp = 1;
			for (int i=1; i<size()-1; i++) {
				String x = get(i);
				if (x.equals("(")) opndp++;
				else if (x.equals(")")) opndp--;
				if (opndp == 0) return;
			}
			removeLast();
			remove(0);
		}
	}
	
	public void removeVoid() {
		removeIf(s -> (s == null || s.equals("")));
	}
	
	public Sequence splitPrecedence () {
		for (Operator[] oplist: Operator.genPrecedence) {
			Sequence seq = splitPrecedence(oplist);
			if (seq.size() > 1) return seq;
		}
		Sequence res = new Sequence();
		res.add(this, null);
		return res;
	}
	
	private Sequence splitPrecedence (Operator[] precedence) {
		Sequence result = new Sequence();
		
		ArrayString buffer = new ArrayString();
		int openedparenthesis = 0;
		
		for (String s: this) {
			openedparenthesis += StringOperations.getParenthesisDifferential(s);
			if (openedparenthesis == 0 && isPartOfList(s, precedence)) {
				result.add(buffer, new Link(s));
				buffer = new ArrayString();
			} else {
				buffer.add(s);
			}
		}
		result.add(buffer, null);
		return result;
	}
	
	public ArrayString parseCollections () {
		ArrayString output = new ArrayString();
		for (String var: this) {
			for (String v: var.split(",")) if (!v.equals("")) output.add(v);
		}
		
		return output;
	}
	
	
	private boolean isPartOfList(String s, Operator[] opSet) {
		for (Operator op: opSet) {
			if (s.equals(op.s)) return true;
		}
		return false;
	}
	
	
	public ArrayList<ArrayString> getDefineSequences() {
		ArrayList<ArrayString> result = new ArrayList<>();
		ArrayString buffer = new ArrayString();
		removeLast(); // it's a '}'
		
		for (String x: this) {
			String endline = StringOperations.getEndLine(x);
			if (endline != null) {
				buffer.add(endline);
				result.add(buffer);
				buffer = new ArrayString();
			} 
			else buffer.add(x);
		}
		return result;
	}
	
	public ArrayString[] splitArrayBy (String by, int from) {
		ArrayString arr1 = new ArrayString();
		ArrayString arr2 = new ArrayString();
		boolean toFirst = true;
		
		for (int i=from; i<size(); i++) {
			if (get(i).equals(by)) toFirst = false;
			else {
				if (toFirst) arr1.add(get(i));
				else arr2.add(get(i));
			}
		}
		return new ArrayString[]{arr1, arr2};
	}
	
	
	public class Sequence {
		
		ArrayList<ArrayString> v;
		ArrayList<Link> links;
		private boolean issubbody, isassignment;
		
		public Sequence () {
			v = new ArrayList<>();
			links = new ArrayList<>();
			issubbody = false;
			isassignment = false;
		}
		public void add(ArrayString a, Link link) {
			v.add(a);
			links.add(link);
		}
		public int size () {
			return v.size();
		}
		public void addSubbody(ArrayString as) {
			issubbody = true;
			v.add(as);
		}
		
		public void addAssignment (ArrayString as) {
			isassignment = true;
			v.add(as);
		}
		
		public boolean isSubbody() { return issubbody; }
		public boolean isAssignment() { return isassignment; }
		
		public ArrayString getV (int i) { return v.get(i); }
		public Link getL (int i) { return links.get(i); }
		
		public ArrayList<Link> getLinks () { 
			if (links.size() == v.size()) links.remove(links.size()-1);
			return links;
		}
		public String getHeadtoken () {
			return v.get(0).get(0);
		}

		/*
		public ArrayString mergeBack () {
			ArrayString res = new ArrayString();
			for (int i=0; i<v.size(); i++) {
				res.addAll(v.get(i));
				res.add(links.get(i).link);
			}
			res.remove(null);
			return res;
		}*/
		
		public Statement toStatement () throws TermSynthaxException {
			if (v.size() != 2) return null;
			return new Statement(links.get(0), Term.compileTerms(v.get(0)), Term.compileTerms(v.get(1)));
		}
	}
}
