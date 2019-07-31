package Elements;

import java.util.ArrayList;

import Core.StringOperations;
import Operation.Operator;

public class ArrayString extends ArrayList<String>{
	
	public ArrayString (){
		super();
	}
	public ArrayString (ArrayList<String> input) {
		super();
		addAll(input);
	}
	
	public ArrayString(String[] var) {
		super();
		for (String s: var) add(s);
	}

	public String[] toStringlist() {
		String[] res = new String[size()];
		for (int i=0; i<size(); i++) res[i] = get(i);
		return res;
	}
	
	public ArrayString sepwithComma () {
		ArrayString commasep = new ArrayString();
		for (String s: this) {
			ArrayList<Character> newstr = new ArrayList<Character>();
			for (char c: s.toCharArray()) {
				newstr.add(c);
				if (c == ',') {
					commasep.add(StringOperations.getString(newstr));
					newstr = new ArrayList<Character>();
				}
			}
			if (newstr.size() != 0) commasep.add(StringOperations.getString(newstr));
		}
		return commasep;
	}
	
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
	}
	
	public void removeVoid() {
		removeIf(s -> (s == null || s.equals("")));
	}

	// TODO add space after parenthsis	
	public Sequence splitPrecedence (Operator[] precedence) {
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
	
	public class Sequence {
		
		ArrayList<ArrayString> v = new ArrayList<ArrayString>();
		ArrayList<Link> links = new ArrayList<Link>();
		private boolean issubbody, isassignment;
		
		public Sequence () {
			v = new ArrayList<ArrayString>();
			links = new ArrayList<Link>();
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
		
		public ArrayString mergeBack () {
			ArrayString res = new ArrayString();
			for (int i=0; i<v.size(); i++) {
				res.addAll(v.get(i));
				res.add(links.get(i).link);
			}
			res.remove(null);
			return res;
		}
	}
}
