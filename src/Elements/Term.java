package Elements;

import java.util.ArrayList;
import java.util.Collections;

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
			if (!s.startsWith(head) && !s.startsWith("\\")) s = head + s;
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
	
	
	static public Term extractTerms(ArrayList<String> seq) {
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
				else result.addTerm(new Term(x));
			}
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
	
	static public ArrayList<Statement> extractDiffArray(Term tthm, Term tprop) throws ExceptionTheoremNotApplicable {
		ArrayList<Statement> result = new ArrayList<Statement>();
		
		tthm.flatten();
		tprop.flatten();
		
		if (tthm.isShallow()) {
			result.add(new Statement("=", tthm, tprop));
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
	
	static public DifferencesLedger extractDiff(Term t1, Term t2) {
		DifferencesLedger dlg = new DifferencesLedger();
		extractDiffInner(t1, t2, dlg);
		Collections.reverse(dlg.diffs);
		return dlg;
	}
	
	static private Statement extractDiffInner(Term t1, Term t2, DifferencesLedger differencesLedger) {
		t1.flatten();
		t2.flatten();
		
		assertSize(t1); assertSize(t2);
		Statement voidstatement = new Statement("=", new Term(), new Term());
		Statement wholestatement = new Statement("=", t1, t2);
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
			if (eq0) return extractDiffInner(p11, p21, differencesLedger); // eq0 && eq1 (equals) above
			if (eq1) return extractDiffInner(p10, p20, differencesLedger);
			return wholestatement;
		} else {
			Term p12 = t1.get(2); Term p22 = t2.get(2);
			boolean eq2 = p12.equals(p22);
			
			if (!eq1) return wholestatement;
			if (eq0) return extractDiffInner(p12, p22, differencesLedger);
			if (eq2) return extractDiffInner(p10, p20, differencesLedger);
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
	
	/*
	static public Statement extractDiff2(Term t1, Term t2) {
		
		t1.flatten();
		t2.flatten();
		
		if (t1.equals(t2)) return new Statement("=", new Term(), new Term());
		if (t1.isShallow() || t2.isShallow()) return new Statement("=", t1, t2);
		
		int leftpos = 0;	
		int rightpos1 = t1.size;
		int rightpos2 = t2.size;
		int leftmax = Math.min(rightpos1, rightpos2);
		while (leftpos < leftmax && t1.v.get(leftpos).equals(t2.v.get(leftpos))) leftpos += 1;
		
		//System.out.println("\n --Terms--\n" + t1 + " === " + t2 + "\n (leftpos, rightpos1, rightpos2) = (" + leftpos + ", " + rightpos1 + ", " + rightpos2+ ")");
		
		while (t1.v.get(rightpos1-1).equals(t2.v.get(rightpos2-1))) {
			rightpos1 -= 1;
			rightpos2 -= 1;
			
			if (leftpos >= rightpos1 && leftpos >= rightpos2) {
				return extractDiff(t1.v.get(leftpos), t2.v.get(leftpos));
			}
			if (leftpos >= rightpos1 || leftpos >= rightpos2) {
				return extractByInt(t1, t2, leftpos, rightpos1+1, rightpos2+1);
			}
		}
		if (leftpos == rightpos1-1 && leftpos == rightpos2-1) {
			return extractDiff(t1.v.get(leftpos), t2.v.get(leftpos));
		}
		return extractByInt(t1, t2, leftpos, rightpos1, rightpos2);
		
	}*/

	
	static public Statement extractByInt(Term t1, Term t2, int leftpos, int rightpos1, int rightpos2) {
		Term nt1 = new Term();
		Term nt2 = new Term();
		for (int i=leftpos; i<rightpos1; i++) nt1.addTerm(t1.v.get(i));
		for (int i=leftpos; i<rightpos2; i++) nt2.addTerm(t2.v.get(i));
		return new Statement("=", nt1, nt2);
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
