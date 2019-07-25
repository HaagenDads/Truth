package Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;

import Operation.Operator;

public class Term {

	public static enum Disp {TOT, QTT, OT, F, ERR};
	private Disp disp;
	
	private ArrayList<Term> v;
	public int size;
	public String s;

	public Term() {
		v = new ArrayList<Term>();
		size = 0;
		disp = null;
	}
	public Term(String s) {
		this.s = s;
		size = 0;
		disp = null;
	}
	public void addTerm(Term t) {
		v.add(t);
		size ++;
		disp = null;
	}
	
	public Term get(int i) {
		try {
			return v.get(i);
		} catch (Exception e) {
			System.out.println("[[[ FATAL ]]] Tried to get index " + i + " from length " + size + " in term: " + v.toString());
			return null;
		}
	}
	
	// I don't want 'Term' to be iterable...
	public class TermArrayIterator implements Iterable<Term> {
		ArrayList<Term> v;
		public TermArrayIterator (ArrayList<Term> v) { this.v = v; }
		public Iterator<Term> iterator() {
			return v.iterator();
		}
	}
	public TermArrayIterator iterator() {
		return new TermArrayIterator(v);
	}
	
	public boolean equalsString(String s) {
		if (!isShallow()) return false;
		return this.s.equals(s);
	}
	
	public boolean isOperator() {
		if (isShallow() && Operator.isOperator(s)) return true;
		return false;
	}	
	
	public boolean equals(Term t) {
		if (isShallow()) {
			if (t.isShallow()) {
				if (s == null && t.s == null) return true;
				return s.equals(t.s);
			} else return false;
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
	
	public boolean isShallow() {
		flatten();
		return v == null;
	}
	public void flatten() {
		// Are we able to obtain a shallow term?
		if (size==1) {
			Term inner = get(0);
			disp = null;
			if (inner.size == 0) {
				v = null;
				s = inner.s;
				size = 0;
			} else {
				v = inner.v;
				size = inner.size;
				flatten();
			}
		}
	}	
	
	/* TOT := [Term, Operator, Term]   QTT := [Quantifier, Term, Term]  OT := [Operator, Term]  F := [Term] (flat)   ERR := error  */
	public Disp getDisposition() {
		if (disp == null) disp = computeDisposition();
		return disp;
	}
	
	private Disp computeDisposition() {
		if (isShallow()) return Disp.F;
		
			Term t1 = get(0);
			Term t2 = get(1);
			if (size == 2) {
				if (t1.isOperator() && Operator.isUnary(t1.s) && !t2.isOperator()) return Disp.OT;
			} else if (size == 3) {
				Term t3 = get(2);
				if (t2.isOperator() && Operator.isBinary(t2.s) && !t1.isOperator() && !t3.isOperator()) return Disp.TOT;
				if (t1.isOperator() && Operator.isQuantifier(t1.s) && !t2.isOperator() && !t3.isOperator()) return Disp.QTT;
			}
		return Disp.ERR;
	}
	
	
	public Term copy() {
		if (isShallow()) return new Term(s);
		Term copy = new Term();
		for (Term x: iterator()) {
			copy.addTerm(x.copy());
		}
		return copy;
	}
	
	public String toString() {
		Disp disp = getDisposition();
		if (disp == Disp.F) return s;	
		if (disp == Disp.QTT) return get(0).s + get(1).toString() + ": " + get(2).toString();
		
		String output = "";
		if (disp == Disp.ERR) output += "[disp error (size="+size+")] ";
		
		for (Term x: iterator()) {
			Disp innerdisp = x.getDisposition();
			if (innerdisp == Disp.F) output += x.s + " ";
			else if (innerdisp == Disp.OT) {
				output += x.get(0).s + " ";
				Term secondterm = x.get(1);
				Disp seconddisp = secondterm.getDisposition();
				if (seconddisp != Disp.F && seconddisp != Disp.OT) output += "(" + secondterm.toString() + ") ";
				else output += secondterm.toString() + " ";
			}
			else output += "(" + x.toString() + ") ";
		}
		return removeLastSpace(output);
	}
	
	private String removeLastSpace(String s) {
		int len = s.length();
		if (len == 0) return "";
		return "" + s.substring(0, len-1);
	}
	
	
	public void embedVariableNames(String head, ArrayList<String> vars) {
		if (isShallow()) {
			//if (!s.startsWith(head) && !s.startsWith("\\") && !Operator.isOperator(s)) s = head + s;
			if (!s.startsWith(head) && vars.contains(s)) s = head + s;
		} else {
			for (Term x: v) x.embedVariableNames(head, vars);
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
	
	/*
	public Type getType(Theorem thm) {
		if (isShallow()) {
			Variable v = thm.getVariable(s);
			if (v != null) return v.type;
			return null;
		} else {
			for (Term x: v) x.applyType(thm);
		}
	}*/
	
	public Term sliceLeft(int to) {
		Term output = new Term();
		for (int i=0; i<to; i++) {
			output.addTerm(get(i));
		}
		return output;
	}
	
	public Term sliceRight(int from) {
		Term output = new Term();
		for (int i=from; i<size; i++) {
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
		if (termarray.isShallow()) {
			return termarray;
		}
		Term result = new Term();
		LinkedList<Term> output = new LinkedList<Term>();
		
		Stack<Term> input = new Stack<Term>();
		input.addAll(termarray.v);

		while (!input.isEmpty()) {
			Term ith = input.pop();
			if (ith.isShallow()) {
				if (ith.s.equals("\\forall")) {
					// TODO collection, FIND \\follows instead of assuming it is at pos 1
					if (output.get(1).isShallow() && output.get(1).s.equals("\\follows")) {
						Term forallterm = new Term();
						forallterm.addTerm(ith);
						forallterm.addTerm(output.pop());
						output.pop();
						forallterm.addTerm(output.pop());
						output.addFirst(forallterm);
					}
				} else if (ith.s.equals("\\exists")) {
					if (output.get(1).isShallow() && output.get(1).s.equals("\\suchthat")) {
						Term existsterm = new Term();
						existsterm.addTerm(ith);
						existsterm.addTerm(output.pop());
						output.pop();
						existsterm.addTerm(output.pop());
						output.addFirst(existsterm);
					}
						
				} else if (ith.s.equals("\\not")) {
					Term notterm = new Term();
					notterm.addTerm(ith);
					notterm.addTerm(output.pop());
					output.addFirst(notterm);
				} else output.addFirst(ith);
			} else output.addFirst(ith);
		}
		
		for (Term t: output) result.addTerm(t);
		return result;
	}
	
	/*
	 * Returns a term structure (list of other terms, never immediatly shallow)
	 */
	static public Term extractTerms(ArrayList<String> seq) {
		//System.out.println("___");
		//for (String s: seq) System.out.println(s);
		
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
				Term ith = result.get(i);
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
	
	
	/*
	 * Demonstration territory
	 */
	static public ArrayList<Statement> extractDiffArray(Term tthm, Term tprop) throws ExceptionTheoremNotApplicable {
		ArrayList<Statement> result = new ArrayList<Statement>();
		
		Disp disp = tthm.getDisposition();
		if (disp == Disp.F) {
			// Following case implies no difference; no need to check.
			if (tprop.isShallow() && tprop.equals(tthm)) return result;
				
			
			// Can't overload a thm variable (thm.a can't be both equal to (x>0) and (x=0))
			for (Statement st: result) {
				if (st.lside.equals(tthm) && !st.rside.equals(tprop)) throw new ExceptionTheoremNotApplicable();
			}
			
			result.add(new Statement(new Link(":="), tthm, tprop));
		}
		else {
			if (disp != tprop.getDisposition()) throw new ExceptionTheoremNotApplicable();
			if (disp == Disp.OT) {
				assertSameOperator(tthm, tprop, 0);
				result.addAll(extractDiffArray(tthm.get(1), tprop.get(1)));
			} else if (disp == Disp.TOT) {
				assertSameOperator(tthm, tprop, 1);
				result.addAll(extractDiffArray(tthm.get(0), tprop.get(0)));
				result.addAll(extractDiffArray(tthm.get(2), tprop.get(2)));
			} else if (disp == Disp.QTT) {
				assertSameOperator(tthm, tprop, 0);
				result.addAll(extractDiffArray(tthm.get(1), tprop.get(1)));
				result.addAll(extractDiffArray(tthm.get(2), tprop.get(2)));
			} else throw new ExceptionTheoremNotApplicable();
		}
		return result;
	}	
	
	static private void assertSameOperator (Term a, Term b, int pos) throws ExceptionTheoremNotApplicable {
		if (!a.get(pos).equalsString(b.get(pos).s)) throw new ExceptionTheoremNotApplicable();
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
		} else if (link.equals("\\then")) {
			extractDiffInner(t1, t2, new Link("="), dlg);
			extractDiffInner(t1, t2, new Link("\\eq"), dlg);
			extractDiffInner(t1, t2, link, dlg);
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
			&& Operator.isUnary(t.get(0).s)
			&& !Operator.isOperator(t.get(1).s))
		|| (t.size == 3
			&& Operator.isBinary(t.get(1).s)
			&& !Operator.isOperator(t.get(0).s)
			&& !Operator.isOperator(t.get(2).s))
		|| (t.size == 3
			&& Operator.isQuantifier(t.get(0).s)
			&& !Operator.isOperator(t.get(1).s)
			&& !Operator.isOperator(t.get(2).s)
		))) { System.out.println("[[[FATAL]]] Term configuration not understood:  " + t); }
	}

	
	static public Permutations permute(Term t) {
		t.flatten();
		Permutations perm = new Permutations();
		
		if (!t.isShallow()) {		
			for (int i=0; i<t.size; i++) {
				Term x = t.get(i);
				if (x.isShallow() && Operator.isCommutative(x.s)) {
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
