package Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import Core.Demonstration;
import Core.Theorem;

public class Term {

	public String s;
	public ArrayList<Term> v;
	public int size;
	public Term() {
		v = new ArrayList<Term>();
		size = 0;
	}
	public Term(String s) {
		this.s = s;
		size = 0;
	}
	public void addTerm(Term t) {
		v.add(t);
		size ++;
	}
	
	
	public Term get(int i) {
		return v.get(i);
	}
	
	
	public boolean equals(Term t) {
		t.flatten();
		if (isShallow()) {
			if (t.isShallow()) {
				if (s == null && t.s == null) return true;
				return s.equals(t.s);
			}
			else return t.equals(this);
		} else if (flatten()) {
			return t.equals(this);
		} else if (t.isShallow()) {
			return false;
		} else {
			if (size != t.size) return false;
			for (Term tperm: Term.permute(t).vs) {
				boolean result = true;
				for (int i=0; i<size; i++) result = result && (get(i).equals(tperm.get(i)));
				if (result) return true;
			}
			return false;
		}
	}
	
	public boolean isShallow() { return v == null; }
	public boolean flatten() {
		// Are we able to obtain a shallow term?
		if (!isShallow() && size==1) {
			Term inner = v.get(0);
			if (inner.isShallow()) {
				v = null;
				s = inner.s;
				return true;
			} else {
				v = inner.v;
				size = inner.size;
				return flatten();
			}
		} else {
			return false;
		}
	}
	
	/*
	 * 	Should only be used in the 'toString()' operation
	 */
	private boolean isUnaryShallow() {
		if (!isShallow() && size==2) {
			Term firstterm = v.get(0);
			if (firstterm.isShallow() && Demonstration.isUnaryOperator(firstterm.s)) {
				return true;
			}
		}
		return false;
	}
	
	public Term copy() {
		if (isShallow()) return new Term(s);
		Term copy = new Term();
		for (Term x: v) {
			copy.addTerm(x.copy());
		}
		return copy;
	}
	
	public String toString() {
		if (isShallow()) return s;
		String output = "";
		for (Term x: v) {
			if (x.isShallow()) output += x.s + " ";
			else if (x.isUnaryShallow()) {
				output += x.v.get(0).s + " ";
				Term secondterm = x.v.get(1);
				if (!secondterm.isShallow() && !secondterm.isUnaryShallow()) output += "(" + secondterm.toString() + ") ";
				else output += secondterm.toString() + " ";
			}
			else output += "(" + x.toString() + ") ";
		}
		if (output.length() == 0) return "";
		return output.substring(0, output.length()-1) + "";
	}
	
	
	public void embedVariableNames(String head) {
		if (isShallow()) {
			if (!s.startsWith(head) && !s.startsWith("\\") && !Demonstration.isOperator(s)) s = head + s;
		} else {
			for (Term x: v) x.embedVariableNames(head);
		}
	}

	public void removeEmbeding() {
		if (isShallow()) {
			String[] sections = s.split("\\.");
			s = sections[sections.length-1];
		} else {
			for (Term x: v) x.removeEmbeding();
		}
	}
	
	
	public void applyType(Theorem thm) {
		if (isShallow()) {
			Variable v = thm.getVariable(s);
			if (v != null) s = v.type;
		} else {
			for (Term x: v) x.applyType(thm);
		}
	}
	
	public Term sliceLeft(int to) {
		Term output = new Term();
		for (int i=0; i<to; i++) {
			output.addTerm(get(i));
		}
		return output;
	}
	
	public Term sliceRight(int from) {
		Term output = new Term();
		for (int i=from; i<v.size(); i++) {
			output.addTerm(get(i));
		}
		return output;
	}
	
	static public Term glueTerms(Term[] ts) {
		Term output = new Term();
		for (Term x: ts) {
			output.addTerm(x);
		}
		return output;
	}
	
	
	static private Term reduce (Term termarray) {
		if (termarray.isShallow()) System.out.println("Term sent to be reduced but was shallow... : " + termarray.toString());
		Term result = new Term();
		LinkedList<Term> output = new LinkedList<Term>();
		
		for (int i=termarray.size-1; i>=0; i--) {
			Term ith = termarray.get(i);
			ith.flatten();
			if (ith.isShallow() && ith.s.equals("\\forall")) {
				if (output.get(1).isShallow() && output.get(1).s.equals("\\follows")) {
					Term forallterm = new Term();
					forallterm.addTerm(ith);
					forallterm.addTerm(output.pop());
					output.pop();
					forallterm.addTerm(output.pop());
					output.addFirst(forallterm);
				}
			} else if (ith.isShallow() && ith.s.equals("\\exists")) {
				if (output.get(1).isShallow() && output.get(1).s.equals("\\suchthat")) {
					Term existsterm = new Term();
					existsterm.addTerm(ith);
					existsterm.addTerm(output.pop());
					output.pop();
					existsterm.addTerm(output.pop());
					output.addFirst(existsterm);
				}
					
			} else if (ith.isShallow() && ith.s.equals("\\not")) {
				Term notterm = new Term();
				notterm.addTerm(ith);
				notterm.addTerm(output.pop());
				output.addFirst(notterm);
			} else {
				output.addFirst(ith);
			}
		}
		
		for (Term t: output) result.addTerm(t);
		result.flatten();
		return result;
	}
	
	/*
	 * Returns a term structure (list of other terms, never immediatly shallow)
	 */
	static public Term extractTerms(ArrayList<String> seq) {
		/*System.out.println("___");
		for (String s: seq) System.out.println(s);*/
		
		Term result = new Term();
		ArrayList<String> innerBuffer = new ArrayList<String>();
		int openedParenthesis = 0;
		
		// In case of redundant parenthesis
		while (seq.get(0).equals("")) seq.remove(0);
		while (isSurroundedByParenthesis(seq)) {
			seq.set(0, seq.get(0).substring(1));
			String lastString = seq.get(seq.size()-1);
			seq.set(seq.size()-1, lastString.substring(0, lastString.length()-1));
		}
		
		if (seq.size() == 1) return new Term(seq.get(0));
		
		boolean foundlink = false;
		for (String x: seq) {
			x = x.trim();
			if (openedParenthesis > 0) {
				openedParenthesis += getParenthesisDifferential(x);
				innerBuffer.add(x);
				
				if (openedParenthesis < 0) System.out.println("[FATAL] Problem with parenthesis in: " + x);
				if (openedParenthesis == 0) {
					result.addTerm(extractTerms(innerBuffer));
					innerBuffer = new ArrayList<String>();
				}
				
			} else {
				openedParenthesis += getParenthesisDifferential(x);
				if (openedParenthesis > 0) innerBuffer.add(x);
				else {
					if (Link.isLink(x)) {
						foundlink = true;
					}
					result.addTerm(new Term(x));
				}
			}
		}
		
		if (!foundlink) {
			//System.out.println("Reducing: " + result.toString() + "  (" + result.size + ")");
			result = Term.reduce(result);
			//System.out.println("Reduced: " + result.toString() + "  (" + result.size + ")");
		}
		if (foundlink && result.size > 3) {
			
			Term parsed = new Term();
			for (int i=0; i<result.size; i++) {
				Term ith = result.v.get(i);
				if (ith.isShallow() && Link.isLink(ith.s)) {

					// Left
					Term tleft = new Term();
					for (int j=0; j<i; j++) {
						tleft.addTerm(result.get(j));
					}
					
					// Right
					Term tright = new Term();
					for (int j=i+1; j<result.size; j++) {
						tright.addTerm(result.get(j));
					}
					
					//System.out.println("LeftReducing: " + tleft.toString() + "  (" + tleft.size + ")");
					parsed.addTerm(Term.reduce(tleft));
					//System.out.println("LeftReduced: " + tleft.toString() + "  (" + tleft.size + ")");
					parsed.addTerm(result.get(i));
					//System.out.println("RightReducing: " + tright.toString() + "  (" + tright.size + ")");
					parsed.addTerm(Term.reduce(tright));
					//System.out.println("RightReduced: " + tright.toString() + "  (" + tright.size + ")");
					
				}
			}
			return parsed;
		}
		/*
		// When an "=" is placed between parenthesis, we want to assume each sides are terms
		// We can only do that if we take them and put them inside ourselves here
		if (result.size > 3) {
			System.out.println("too big. results look like: " + result.toString());
			for (Term t: result.v) {
				System.out.println("  " + t.toString());
			}
			Term parsed = new Term();
			boolean foundop = false;
			for (int i=1; i<result.size; i++) {
				Term ith = result.v.get(i);
				if (ith.isShallow() && ith.s.equals("=")) {
					foundop = true;
					
					// Regroup the left
					if (i > 1) {
						innerBuffer = new ArrayList<String>();
						innerBuffer.add("(" + result.get(0).toString());
						for (int j=1; j<i-1; j++) {
							String nextterm = result.get(j).toString();
							//if (!result.get(j).isShallow()) nextterm = "(" + nextterm + ")";
							innerBuffer.add(nextterm);
						}
						innerBuffer.add(result.get(i-1).toString() + ")");
						parsed.addTerm(extractTerms(innerBuffer));
					} else {
						parsed.addTerm(result.get(0));
					}
					
					
					// Add middle operator (eg. "=")
					parsed.addTerm(ith);
					
					
					// Regroup the right
					innerBuffer = new ArrayList<String>();
					if (i < result.size - 2) {
						parsed.addTerm(result.get(i+1));						
					} else {
						innerBuffer = new ArrayList<String>();
						innerBuffer.add("(" + result.get(i+1).toString());
						for (int j=i+2; j<result.size-1; j++) {
							String nextterm = result.get(j).toString();
							innerBuffer.add(nextterm);
						}
						innerBuffer.add(result.get(result.size-1).toString() + ")");
						parsed.addTerm(extractTerms(innerBuffer));
						System.out.println("the right parsed: " + extractTerms(innerBuffer).toString());
					}
				}
			}
			if (foundop) return parsed;
		}
		*/
		
		/*
		// Comprehend Exist and Forall operators, from the written structure
		//    \exists (x, y) \suchthat ((x > 0) \and (3 = 3))
		// into a known size 3 term of the form
		//    [\exists, [(x, y)]_collection, [[x > 0], \and, [3 = 3]]]
		if (result.size == 4) {
			Term headToken = result.get(0);
			Term middleToken = result.get(2);
			if (headToken.isShallow() && middleToken.isShallow()) {
				if ((headToken.s.equals("\\exists") && middleToken.s.equals("\\suchthat"))
				 || (headToken.s.equals("\\forall") && middleToken.s.equals("\\follows"))) {
					Term parsed = new Term();
					parsed.addTerm(headToken);
					// TODO
					// Implement collections for terms (eg (x, y, z))
					parsed.addTerm(result.get(1));
					parsed.addTerm(result.get(3));
					return parsed;
				}
			}
		}*/
		return result;
	}
	
	
	static private boolean isSurroundedByParenthesis(ArrayList<String> seq) {
		if (seq.get(0).charAt(0) != '(') return false;
		int openedParenthesis = 0;
		for (int i=0; i<seq.size(); i++) {
			char[] x = seq.get(i).trim().toCharArray();
			for (int j=0; j<x.length; j++) {
				if (x[j] == '(') openedParenthesis++;
				if (x[j] == ')') {
					openedParenthesis--;
					if (openedParenthesis == 0) return (i==(seq.size()-1)) && (j==x.length-1);
					if (openedParenthesis < 0) System.out.println("[FATAL] Error in parenthesis comprehension.");
				}
			}
		}
		System.out.println("[FATAL] Error in parenthesis comprehension.");
		return false;		
	}
	
	static private int getParenthesisDifferential(String x) {
		int result = 0;
		for (char c: x.toCharArray()) {
			if (c == '(') result++;
			if (c == ')') result--;
		}
		return result;
	}
	
	static public ArrayList<Statement> extractDiffArray(Term tthm, Term tprop) throws ExceptionTheoremNotApplicable {
		ArrayList<Statement> result = new ArrayList<Statement>();
		
		tthm.flatten();
		tprop.flatten();
		
		if (tthm.isShallow()) {
			result.add(new Statement(new Link("="), tthm, tprop));
		}
		else {
			if (tthm.size != tprop.size) throw new ExceptionTheoremNotApplicable();
			for (int i=0; i<tthm.size; i++) {
				Term tA = tthm.get(i);
				Term tB = tprop.get(i);
				result.addAll(extractDiffArray(tA, tB));
				
			}
		}
		return result;
	}
	
	
	static public class DifferencesLedger {
		
		public ArrayList<Statement> diffs;
		
		public DifferencesLedger() {
			diffs = new ArrayList<Statement>();
		}
		public void addDifference(Statement st) {
			for (Statement diff: diffs) {
				if (diff.equals(st)) return;
			}
			diffs.add(st);
		}
	}
	
	/*
	 * If our link is "=", then we need to prove that a = b or that f(a) = f(b)
	 * If our link is propositional, we need to prove that a = b, f(a) = f(b) (( when different names mean the same thing ))
	 *     or that a <=> b (( since  (a <==> b) <==> (a v c <==> a v b)  ))
	 *     idem for f(a) <=> f(b)
	 */
	static public ArrayList<Statement> extractDiff(Term t1, Term t2, Link link) {
		DifferencesLedger dlg = new DifferencesLedger();
		
		if (link.equals("=")) {
			extractDiffInner(t1, t2, link, dlg);
		} else if (link.equals("\\eq")) {
			extractDiffInner(t1, t2, new Link("="), dlg);
			extractDiffInner(t1, t2, link, dlg);
		} else if (link.equals("<=")) {
			extractDiffInner(t1, t2, new Link("="), dlg);
			extractDiffInner(t1, t2, new Link("<"), dlg);
			extractDiffInner(t1, t2, link, dlg);
		} else if (link.equals(">=")) {
			extractDiffInner(t1, t2, new Link("="), dlg);
			extractDiffInner(t1, t2, new Link(">"), dlg);
			extractDiffInner(t1, t2, link, dlg);
		} else if (link.equals("!=")) {
			extractDiffInner(t1, t2, link, dlg);
			extractDiffInner(t1, t2, new Link(">"), dlg);
			extractDiffInner(t1, t2, new Link("<"), dlg);
		}
		
		Collections.reverse(dlg.diffs);
		return dlg.diffs;
	}
	
	static private Statement extractDiffInner(Term t1, Term t2, Link clink, DifferencesLedger differencesLedger) {
		t1.flatten();
		t2.flatten();
		
		assertSize(t1); assertSize(t2);
		Statement voidstatement = new Statement(clink, new Term(), new Term());
		Statement wholestatement = new Statement(clink, t1, t2);
		differencesLedger.addDifference(wholestatement);
		
		
		if (t1.equals(t2)) return voidstatement;
		if (t1.isShallow() || t2.isShallow()) return wholestatement;
		assertUnaryOrBinaryPlacement(t1);
		assertUnaryOrBinaryPlacement(t2);
		
		Term p10 = t1.get(0); Term p11 = t1.get(1);
		Term p20 = t2.get(0); Term p21 = t2.get(1);
		
		boolean eq0 = p10.equals(p20);
		boolean eq1 = p11.equals(p21);
		
		if (t1.size != t2.size) return wholestatement;
		else if (t1.size == 2) {
			if (eq0) return extractDiffInner(p11, p21, clink, differencesLedger); // eq0 && eq1 (equals) above
			if (eq1) return extractDiffInner(p10, p20, clink, differencesLedger);
			return wholestatement;
		} else {
			Term p12 = t1.get(2); Term p22 = t2.get(2);
			boolean eq2 = p12.equals(p22);
			
			if (!eq1) return wholestatement;
			if (eq0) return extractDiffInner(p12, p22, clink, differencesLedger);
			if (eq2) return extractDiffInner(p10, p20, clink, differencesLedger);
			return wholestatement;
		}
	}
	
	static public void assertSize(Term t) {
		if (t.size > 3) System.out.println("[[[FATAL]]] Size of term " + t + " is >3 (" + t.size + ").");
	}
	
	static public void assertUnaryOrBinaryPlacement(Term t) {
		if (!((t.size == 2
			&& Demonstration.isUnaryOperator(t.get(0).s)
			&& !Demonstration.isOperator(t.get(1).s))
		|| (t.size == 3
			&& Demonstration.isBinaryOperator(t.get(1).s)
			&& !Demonstration.isOperator(t.get(0).s)
			&& !Demonstration.isOperator(t.get(2).s)
		))) { System.out.println("[[[FATAL]]] Term configuration not understood:  " + t); }
	}

	
	static public Permutations permute(Term t) {
		t.flatten();
		Permutations perm = new Permutations();
		
		if (!t.isShallow()) {		
			for (int i=0; i<t.v.size(); i++) {
				Term x = t.v.get(i);
				if (x.isShallow() && Demonstration.isBinaryOperator(x.s)) {
					Term ta = t.sliceLeft(i);
					Term tb = t.sliceRight(i+1);
					for (Term permta: permute(ta).vs) {
					for (Term permtb: permute(tb).vs) {
						perm.add(glueTerms(new Term[]{permta, x, permtb}));
						perm.add(glueTerms(new Term[]{permtb, x, permta}));
					}
					}
				}
			}
		}
		
		if (perm.vs.isEmpty()) perm.add(t);
		return perm;
	}
	
	
	static public class ExceptionTheoremNotApplicable extends Exception {};
	
	static public class Permutations {
		
		public ArrayList<Term> vs;
		public Permutations() {
			vs = new ArrayList<Term>();
		}
		public void add(Term t) {
			vs.add(t);
		}
	}

	
}
