package Elements;

import java.util.*;

import Core.Demonstration;
import Core.GenException;
import Core.Theorem;
import Elements.Function.ExceptionAssignDefinition;
import Elements.Function.ExceptionSetInvalid;
import Operation.Op;
import Operation.Operator;

public class Term {

	public enum Disp {SET, TOT, QTT, OT, F, C, FC, DEF, ERR};
	private Disp disp;
	protected boolean iscollection = false;
	protected boolean isoperator = false;
	protected boolean isdefinition = false;
	
	public Term[] v;
	public int size;
	public String s;
	protected Permutations permutations;

	public Term() { __init__();	}
	public Term(String s) {
		this.s = s;
		__init__();
	}

	private void __init__() {
		size = 0;
		disp = null;
		permutations = null;
	}
	
	public static Term makeNewTerm (String s) {
		Operator op = Op.getOperator(s);
		if (op == null) return new Term(s);
		else return op;
	}
	
	public boolean isCollection () { return iscollection; }
	public boolean isOperator () { return isoperator; }
	public boolean isDefinition() { return isdefinition; }
	
	public void addTerm(Term t) {
		Term[] oldv = v;
		v = new Term[++size];
		if (oldv != null) System.arraycopy(oldv, 0, v, 0, oldv.length);
		v[size-1] = t;
		disp = null;
	}

	/* hacky way of fitting collections and function calls to the term compilation */
	private void removeLastterm() {
		Term [] oldv = v;
		v = new Term[--size];
		if (oldv != null) System.arraycopy(oldv, 0, v, 0, size);
		disp = null;
	}
	
	public Term get(int i) {
		try {
			return v[i];
		} catch (Exception e) {
			if (v == null) System.out.println("[[[ FATAL ]]] Tried to get index " + i + " from empty term");
			System.out.println("[[[ FATAL ]]] Tried to get index " + i + " from length " + size + " in term: " + v.toString());
			throw new IndexOutOfBoundsException();
		}
	}

	public Statement toStatement() {
		if (getDisposition() == Disp.TOT)
			return new Statement(new Link(get(1).s), get(0), get(2));
		else
			return null;
	}

	// I don't want 'Term' to be iterable...
	/*
	public class TermArrayIterator implements Iterable<Term> {
		Term[] v;
		public TermArrayIterator (Term[] v) { this.v = v; }
		public Iterator<Term> iterator() {
			return v;
		}
	}
	public TermArrayIterator iterator() {
		return new TermArrayIterator(v);
	}*/
	
	public boolean equalsString(String s) {
		if (!isShallow()) return false;
		return this.s.equals(s);
	}

	
	public boolean equals(Term t) {
		Disp disp = this.getDisposition();
		if (t.getDisposition() != disp) return false;
		if (disp == Disp.F) {
			if (s == null && t.s == null) return true;
			return s.equals(t.s);
		} else {
			if (size != t.size) return false;
			for (Term tperm: t.getPermutations()) {
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
		if (isShallow() || isOperator()) return Disp.F;
		if (isCollection()) return Disp.C;
		if (isDefinition()) return Disp.DEF;
		
		Term t1 = get(0);
		Term t2 = get(1);
		if (size == 2) {
			if (t1.isOperator()) {
				if (((Operator) t1).isUnary() && !t2.isOperator()) return Disp.OT;
			} else if (t1.isShallow() && t2.isCollection()) return Disp.FC;
		} else if (size == 3) {
			Term t3 = get(2);
			boolean binary = t2.isOperator() && (((Operator) t2).isBinary() || Op.minus.equals(t2));
			if (binary && !t1.isOperator() && !t3.isOperator()) {
				return Disp.TOT;
			}
			if (t1.isOperator() && !t2.isOperator() && !t3.isOperator()) {
				Operator op1 = (Operator) t1;
				if (op1.isQuantifier()) return Disp.QTT;
				if (op1.isSet()) return Disp.SET;
			}
		}
		return Disp.ERR;
	}
	

	public Term copy() {
		if (isShallow()) return makeNewTerm(s);
		Term copy = new Term();
		for (Term x: v) {
			copy.addTerm(x.copy());
		}
		return copy;
	}
	
	public String toString() {
		Disp disp = getDisposition();
		if (disp == Disp.F) return s;	
		if (disp == Disp.C) return toString();
		if (disp == Disp.QTT) return get(0).s + get(1).toString() + ": " + adjustParenthesis(get(2));
		if (disp == Disp.FC) return get(0).s + get(1).toString();
		
		String output = "";
		if (disp == Disp.ERR) output += "[disp error (size="+size+")] ";
		
		if (disp == Disp.OT && v[0] == Op.minus) return "-" + v[1].toString();
		if (disp == Disp.TOT && Op.plus.equals(v[1]) && v[2].getDisposition() == Disp.OT && Op.minus.equals(v[2].v[0])) {
			Term tempterm = new Term();
			tempterm.addTerm(v[0]);
			tempterm.addTerm(Op.minus);
			tempterm.addTerm(v[2].v[1]);
			return tempterm.toString();
		}
		for (Term x: v) {
			Disp innerdisp = x.getDisposition();
			if (innerdisp == Disp.F) output += x.s + " ";
			else if (innerdisp == Disp.C) output += x.toString() + " ";
			else if (innerdisp == Disp.OT) {
				output += x.get(0).s + " ";
				Term secondterm = x.get(1);
				/*
				if (seconddisp != Disp.F && seconddisp != Disp.OT) output += "(" + secondterm.toString() + ") ";
				else output += secondterm.toString() + " ";*/
				output += adjustParenthesis(secondterm) + " ";
			}
			else if (innerdisp == Disp.FC) {
				output += x.get(0).s + x.get(1).toString() + " ";
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
	
	protected String adjustParenthesis (Term t) {
		Disp d = t.getDisposition();
		if (d == Disp.C || d == Disp.F || d == Disp.FC || d == Disp.OT) return t.toString();
		return "(" + t.toString() + ")";
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
	
	static private Term glueTerms(Term[] ts) {
		Term output = new Term();
		for (Term x: ts) {
			output.addTerm(x);
		}
		return output;
	}
	
	static private Stack<Term> toStack (Term[] array) {
		Stack<Term> result = new Stack<Term>();
		result.addAll(Arrays.asList(array));
		return result;
	}
	
	static private Term parseTerms (Term termarray) throws TermSynthaxException {
		if (termarray.size < 4 || termarray.getDisposition() != Disp.ERR) {
			termarray = reduceBinaryMinus(termarray);
			return termarray;
		}
		Term term = reduce(termarray);
		if (term.size < 4 || term.getDisposition() != Disp.ERR) {
			termarray = reduceBinaryMinus(termarray);
			return term;
		}
		
		Term parsed = new Term();
		for (int i=0; i<term.size; i++) {
			Term ith = term.get(i);
			if (ith.isOperator() && Link.isLink(ith.s)) {

				// Left
				Term tleft = new Term();
				for (int j=0; j<i; j++) {
					tleft.addTerm(term.get(j));
				}
				
				// Right
				Term tright = new Term();
				for (int j=i+1; j<term.size; j++) {
					tright.addTerm(term.get(j));
				}
				parsed.addTerm(parseTerms(tleft));
				parsed.addTerm(term.get(i));
				parsed.addTerm(parseTerms(tright));
			}
		}
		
		return parsed;
	}

	/** Takes ['x', 'and', 'not', 'y'] into ['x', 'and', ['not', 'y']] 
	 * @throws TermSynthaxException */
	static private Term reduce (Term termarray) throws TermSynthaxException {
		if (termarray.isShallow() || termarray.isCollection()) {
			return termarray;
		}
		
		Stack<Term> input = toStack(termarray.v);
		LinkedList<Term> output = new LinkedList<Term>();

		//System.out.println(":input stack:");
		//for (Term t: termarray.v) System.out.println(t.toString());
		while (!input.isEmpty()) {
			Term ith = input.pop(); // Reversed order
			if (ith.isOperator()) {
				if (ith.equals(Op.forall)) {
					// TODO collection as syntatxic sugar, from forall x: forall y: into forall (x, y)
					output = extractQuantifiers(output, Op.forall);
					
				} else if (ith.equals(Op.exists)) {
					output = extractQuantifiers(output, Op.exists);
					
				} else if (ith.equals(Op.not)) {
					Term notterm = new Term();
					notterm.addTerm(ith);
					notterm.addTerm(output.pop());
					output.addFirst(notterm);
					
				} else output.addFirst(ith);
			} else output.addFirst(ith);
		}
		
		Term result = new Term();
		for (Term t: output) result.addTerm(t);
		return result;
	}
	
	static private LinkedList<Term> extractQuantifiers (LinkedList<Term> output, Operator quantop) throws TermSynthaxException {

		int followsPos=1;

		if (output.size() < 3) throw new QuantifierSynthaxException_Size(output);
		while (!output.get(followsPos).equalsString(":")) {
			if (++followsPos >= output.size()) throw new QuantifierSynthaxException_Colon(output);
		}
		Term quantterm = new Term();
		Term condition = new Term();
		Term proposition = new Term();
		
		for (int i=0; i<followsPos; i++) condition.addTerm(output.pop());
		output.pop(); // removal of the ":" term
		while (!output.isEmpty()) proposition.addTerm(output.pop());
					
		quantterm.addTerm(quantop);
		quantterm.addTerm(parseTerms(condition));
		quantterm.addTerm(parseTerms(proposition));
		output.addFirst(quantterm);
		return output;
	}
	
	/*
	 * Returns a term structure (list of other terms, never immediatly shallow)
	 */
	static public Term compileTerms (ArrayString seq) throws TermSynthaxException {
		seq.removeVoid();
		return compileTermsClean(seq);
	}

	/*
	static private String unpeelParenthesis(String s) {
		int lastindex = s.length()-1;
		while (s.charAt(0) == '(' && s.charAt(lastindex) == ')') {
			int openedparenthesis = 0;
			for (char c: s.substring(0, lastindex-1).toCharArray()) {
				if (c == '(') openedparenthesis++;
				if (c == ')') openedparenthesis--;
				if (openedparenthesis == 0) return s;
			}
			s = s.substring(1, --lastindex);
		}
		return s;
	}*/
	
	static private Term compileTermsClean(ArrayString seq) throws TermSynthaxException {
		Term result = new Term();
		ArrayString innerBuffer = new ArrayString();
		int openedParenthesis = 0;
		
		// In case of redundant parenthesis
		seq.removeSpacedParenthesis();

		/*
		if (seq.size() == 1) {
			String element = seq.get(0);
			if (isSingletonCollection(element)) return compileSingletonCollection(element);
			else return makeNewTerm(element);
		}*/
		boolean foundlink = false;

		String prev = null;
		for (String x: seq) {
			x = x.trim();

			if (openedParenthesis > 0) {
				if (x.equals("(")) openedParenthesis++;
				else if (x.equals(")")) openedParenthesis--;
				innerBuffer.add(x);

				if (openedParenthesis == 0) {
					Term innerterm;
					if (isCollectionHeader(prev)) {
						innerterm = compileCollection(innerBuffer, prev); // TODO tuples not admited
						result.removeLastterm();
					}
					else {
						innerterm = compileTermsClean(innerBuffer);
					}
					result.addTerm(innerterm);
					innerBuffer = new ArrayString();
				}
				
			} else {
				if (x.equals("(")) openedParenthesis++;
				else if (x.equals(")")) openedParenthesis--;

				//if (isSingletonCollection(x)) result.addTerm(compileSingletonCollection(x));
				if (openedParenthesis > 0) {
					innerBuffer.add(x);
				}
				else {
					if (isUnaryHeader(x)) {
						String[] unary = getUnaryHeader(x);
						Term inner = new Term(unary[1]);
						char[] chararray = unary[0].toCharArray();
						for (int i=chararray.length-1; i>=0; i-- ) {
							char c = chararray[i];
							Term newinner = new Term();
							newinner.addTerm(makeNewTerm(Character.toString(c)));
							newinner.addTerm(inner);
							newinner.disp = Disp.OT;
							inner = newinner;
						}
						prev = unary[1];
						result.addTerm(inner);
					}
					// Straight to a definition
					else if (x.equals("is")) {
						Definition def = new Definition();
						String str = "";
						boolean foundis = false;
						for (String d: seq) {
							if (!foundis && d.equals("is")) foundis = true;
							if (foundis) str += d + " ";
						}
						def.parseTerms(result, str);
						return def;
					}
					else {
						if (Link.isLink(x)) foundlink = true;
						prev = x;
						result.addTerm(makeNewTerm(x));
					}

				}
			}
		}
		
		if (!foundlink) result = reduce(result);
		if (foundlink && result.size > 3) {
			//System.out.println(":result term (size=" + result.size + "):  " + result.toString());
			result = parseTerms(result);
			//System.out.println(":into (size=" + result.size + "):   " + result.toString());
		}
		result = reduceBinaryMinus(result);
		if (result.getDisposition() == Disp.ERR) throw new TermDispositionUnknownException(result);
		return result;
	}
	
	static private Term reduceBinaryMinus (Term t) {
		if (t.getDisposition() == Disp.TOT && ((Operator) t.get(1)).equals(Op.minus)) {
			
			Term plusres = new Term();
			plusres.addTerm(t.get(0));
			plusres.addTerm(Op.plus);
			
			Term minusterm = new Term();
			minusterm.addTerm(Op.minus);
			minusterm.addTerm(t.get(2));
			plusres.addTerm(minusterm);
			
			t = plusres;
		}
		return t;
	}

	static private boolean isUnaryHeader (String s) {
		if ((s.charAt(0) == '-' && s.charAt(s.length()-1) != '-')) {
			System.out.println(s + " is a unary header.");
			return true;
		}
		return false;
	}

	static private String[] getUnaryHeader (String s) {
		StringBuilder unaryhead = new StringBuilder();
		while (s.charAt(0) == '-') {
			unaryhead.append("-");
			s = s.substring(1);
		}
		return new String[]{unaryhead.toString(), s};
	}

	static private boolean isCollectionHeader (String s) {
		if (s == null || s.equals("")) return false;
		char first = s.charAt(0);
		return Character.isLetter(first) || s.startsWith("\\cartprod") || s.startsWith("\\set") || s.startsWith("\\tuple");
		//return s.startsWith("\\set(") || s.startsWith("\\cartprod(");
	}

	
	// expect a space after a comma
	static private Term compileCollection (ArrayString as, String prev) throws TermSynthaxException {
		ArrayList<ArrayString> parsed = new ArrayList<ArrayString>();
		ArrayString inner = new ArrayString();
		as.removeSpacedParenthesis();

		int openedParenthesis = 0;
		for (String s: as) {
			if (s.equals("(")) openedParenthesis++;
			else if (s.equals(")")) openedParenthesis--;

			if (openedParenthesis == 0 && s.endsWith(",")) {
				inner.add(s.substring(0, s.length()-1));
				parsed.add(inner);
				inner = new ArrayString();
			} else {
				inner.add(s);
			}
		}
		parsed.add(inner);
		return compileCollectionParsed(parsed, prev);
	}
	
	
	static private Term compileCollectionParsed (ArrayList<ArrayString> aas, String coll_header) throws TermSynthaxException {

		Collection coll = new Collection();
		for (ArrayString as: aas) coll.addTerm(compileTermsClean(as));
		if (coll_header.equals("\\cartprod")) coll.iscartesian = true;
		else if (coll_header.equals("\\set")) coll.isset = true;
		else if (coll_header.equals("\\tuple"));
		else {
			Term fc = new Term();
			fc.addTerm(makeNewTerm(coll_header));
			fc.addTerm(coll);
			fc.disp = Disp.FC;
			return fc;
		}
		return coll;
	}
	
	/* Flags wrong construction of the form a()()(x, y, z) but not a(()x, y, z) */
	/*
	static private void assertProperParenthesis (String str, boolean isSingleton) {
		int result = 1;
		boolean reached0 = false;
		for (char c: str.toCharArray()) {
			if (reached0) System.out.println("[[[ FATAL ]]] Parenthesis error in " + str);
			if (c == '(') result++;
			if (c == ')') result--;
			if (result == 0) {
				if (isSingleton) reached0 = true;
				else System.out.println("[[[ FATAL ]]] Parenthesis error in " + str);
			}
		}
	}*/
		
	static public ArrayList<Variable> parseLetStatement (ArrayString strings, Theorem thm, boolean fromheader) {
		ArrayList<Variable> results = new ArrayList<Variable>();
		
		ArrayString varname = new ArrayString();
		int i = 1;
		while (!(strings.get(i).equals("\\in") || strings.get(i).equals("\\be"))) {
			varname.add(strings.get(i++));
		}
		varname = varname.parseCollections();
		String connection = strings.get(i++);
	
		if (connection.equals("\\in")) {
			String set = strings.get(i);
			for (String var: varname) results.add(new Variable(var, set, fromheader));
		} else if (connection.equals("\\be") && strings.get(i).equals("\\set")) {
			for (String var: varname) results.add(new Variable(var, "\\set", fromheader));
		} else if (connection.equals("\\be") && strings.get(i).equals("\\function")) {
			i++;
			if (strings.size() > i) {

				ArrayList<Function> funcs = new ArrayList<Function>();
				ArrayString[] domain = strings.splitArrayBy("->", i);
				ArrayString[] args = domain[1].splitArrayBy("{", 0);
				ArrayString image = args[0];
				ArrayString defineSts = args[1];
				try {
					for (String var: varname) {
						Function func = new Function(var, fromheader);
						func.setDomain(compileTerms(domain[0]), thm);
						func.setImage(compileTerms(image), thm);
						funcs.add(func);
					}
				}
				catch (ExceptionSetInvalid e) { Demonstration.printout(3, e.getError()); }
				catch (GenException e) { e.explain(); }
				if (defineSts.size() == 0) {
					results.addAll(funcs);
					return results;
				}
				
				// deal with \define statements
				if (varname.size() > 1) System.out.println("[Error] Can't assign definitions to multiple functions at the same time");
				Function func = funcs.get(0);
				
				try {
					for (ArrayString define: defineSts.getDefineSequences()) {
						if (!define.get(0).equals("\\define")) System.out.println("[Error] Couldnt find 'define' token in: " + define.toString());
						ArrayString[] sts = define.splitArrayBy(":", 1);
						if (sts[1].size() == 0) func.setDefinition(thm, sts[0]);
						else func.setDefinition(thm, sts[0], sts[1]);					
					}
				
					results.add(func);
					return results;
				} 
				catch (ExceptionAssignDefinition e) {	System.out.println(e.printError());	}
				catch (TermSynthaxException e) { e.explain(); }
				
				
			} else {
				for (String var: varname) {
					Function func = new Function(var, fromheader);
					results.add(func);
				}
			}
			
		} else { 
			Demonstration.printout(3, "Could not comprehend variable initialization from: " + strings.toString());
		}
		return results;
	}
	
	
	
	/*
	 * Demonstration territory
	 */
	static public ArrayList<Statement> extractDiffArray(Term tthm, Term tprop) throws ExceptionTheoremNotApplicable {
		ArrayList<Statement> result = new ArrayList<Statement>();
		return extractDiffArray(tthm, tprop, result);
	}
	
	static protected ArrayList<Statement> extractDiffArray(Term tthm, Term tprop, ArrayList<Statement> result) throws ExceptionTheoremNotApplicable {	
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
			if (disp == Disp.C) {
				result.addAll(Collection.extractDiffArray(tthm, tprop, result));
			} else if (disp == Disp.OT) {
				assertSameOperator(tthm, tprop, 0);
				result.addAll(extractDiffArray(tthm.get(1), tprop.get(1), result));
			} else if (disp == Disp.TOT) {
				assertSameOperator(tthm, tprop, 1);
				result.addAll(extractDiffArray(tthm.get(0), tprop.get(0)));
				result.addAll(extractDiffArray(tthm.get(2), tprop.get(2), result));
			} else if (disp == Disp.QTT) {
				assertSameOperator(tthm, tprop, 0);
				result.addAll(extractDiffArray(tthm.get(1), tprop.get(1), result));
				result.addAll(extractDiffArray(tthm.get(2), tprop.get(2), result));
			} else if (disp == Disp.FC) {
				result.addAll(extractDiffArray(tthm.get(0), tprop.get(0), result));
				result.addAll(Collection.extractDiffArray(tthm.get(1), tprop.get(1), result));
			} else if (disp == Disp.DEF) {
				assertSameOperator(tthm, tprop, 1);
				result.addAll(extractDiffArray(tthm.get(0), tprop.get(0), result));
				result.addAll(Collection.extractDiffArray(tthm.get(2), tprop.get(2), result));
			}
			else throw new ExceptionTheoremNotApplicable();
		}
		return result;
	}	
	
	static private void assertSameOperator (Term a, Term b, int pos) throws ExceptionTheoremNotApplicable {
		if (!a.get(pos).equalsString(b.get(pos).s)) throw new ExceptionTheoremNotApplicable();
	}
	
	static class DifferencesLedger {
		
		private Term t1, t2;
		private boolean trivial;
		public ArrayList<Statement> diffs;
		
		public DifferencesLedger(Term t1, Term t2) {
			diffs = new ArrayList<Statement>();
			this.t1 = t1;
			this.t2 = t2;
			trivial = false;
		}
		public void addDifference(Statement st) {
			for (Statement diff: diffs) {
				if (diff.equals(st)) return;
			}
			diffs.add(st);
		}
		public void extractDiff(Link link) {
			if (Term.extractDiffInner(t1, t2, link, this)) trivial = true;
		}

		public boolean isTrivial() {
			return trivial;
		}
		public void addNestedComparaison(ArrayList<Statement> sts) {
			if (sts != null) {
				for (Statement st: sts) {
					addDifference(st);
				}
			}
		}
	}
	
	/*
	 * If our link is "=", then we need to prove that a = b or that f(a) = f(b)
	 * If our link is propositional, we need to prove that a = b, f(a) = f(b) (( when different names mean the same thing ))
	 *     or that a <=> b (( since  (a <==> b) <==> (a v c <==> a v b)  ))
	 *     idem for f(a) <=> f(b)
	 */
	static public ArrayList<Statement> extractDiff(Term t1, Term t2, Link link) {
		DifferencesLedger dlg = new DifferencesLedger(t1, t2);
		
		// TODO equiv only makes sense for booleans
		if (link.equals(Op.eq)) {
			dlg.extractDiff(link);
		} else if (link.equals(Op.equiv)) {
			dlg.extractDiff(link);
		} else if (link.equals(Op.le)) {
			dlg.extractDiff(new Link(Op.eq));
			dlg.extractDiff(new Link(Op.lt));
			dlg.extractDiff(link);
		} else if (link.equals(Op.ge)) {
			dlg.extractDiff(new Link(Op.eq));
			dlg.extractDiff(new Link(Op.gt));
			dlg.extractDiff(link);
		} else if (link.equals(Op.ineq)) {
			dlg.extractDiff(new Link(Op.gt));
			dlg.extractDiff(new Link(Op.lt));
			dlg.extractDiff(link);
		} else if (link.equals(Op.then)) {
			dlg.extractDiff(new Link(Op.equiv));
			dlg.extractDiff(link);
		} else {
			dlg.extractDiff(link);
		}
		
		dlg.addNestedComparaison(extract4TOTdiff(t1, t2, link));
		if (dlg.isTrivial()) return new ArrayList<Statement>();
		Collections.reverse(dlg.diffs);
		return dlg.diffs;
	}
	
	// TODO is this the best way of doing this? with the error and such?
	/* For the frequent case of a = b \eq c = b, or a < b \eq b < c */
	static private ArrayList<Statement> extract4TOTdiff (Term t1, Term t2, Link link) {
		if (link.equals(Op.equiv) || link.equals(Op.then)) {
			if (t1.getDisposition() == Disp.TOT && t2.getDisposition() == Disp.TOT) {
				Term a, b, c, d; a = t1.get(0); b = t1.get(2); c = t2.get(0); d = t2.get(2);
				Operator op1, op2; op1 = (Operator) t1.get(1); op2 = (Operator) t2.get(1);
				int eqlt = 0;
				if (a.equals(c)) eqlt = 11;
				else if (a.equals(d)) eqlt = 12;
				else if (b.equals(c)) eqlt = 21;
				else if (b.equals(d)) eqlt = 22;
				if (eqlt != 0) {
					boolean reverseIneq = (eqlt == 12 || eqlt == 21);
					Operator newop2 = op2;
					if (reverseIneq && op2 == Op.lt) newop2 = Op.gt;
					if (reverseIneq && op2 == Op.le) newop2 = Op.ge;
					if (reverseIneq && op2 == Op.gt) newop2 = Op.lt;
					if (reverseIneq && op2 == Op.ge) newop2 = Op.le;
					
					if (op1 == newop2) {
						Link eqlink = new Link(Op.eq);
						if (eqlt == 11)	return extractDiff(b, d, eqlink);
						if (eqlt == 12)	return extractDiff(b, c, eqlink);
						if (eqlt == 21)	return extractDiff(a, d, eqlink);
						return extractDiff(a, c, eqlink);
					}
				}
			}
		}
		return null;
	}

	/** Digs from complex (a+((b*c) - (d/e))) = (a+((b*c) - (x/e))) into a "d = x" expression */
	static boolean extractDiffInner(Term t1, Term t2, Link clink, DifferencesLedger dL) {

		Statement wholestatement = new Statement(clink, t1, t2);
		dL.addDifference(wholestatement);
		
		Disp d1 = t1.getDisposition();
		Disp d2 = t2.getDisposition();

		if (d1 != d2) return false;
		else if (t1.equals(t2)) return true;
		else {
			if (d1 == Disp.F) return false;
			if (d1 == Disp.C) return Collection.extractDiffInner(t1, t2, clink, dL);
			Term p10 = t1.get(0); Term p11 = t1.get(1);
			Term p20 = t2.get(0); Term p21 = t2.get(1);
			
			boolean eq0 = p10.equals(p20);
			boolean eq1 = p11.equals(p21);
			if (d1 == Disp.OT) {
				if (eq0) return extractDiffInnerWithReversing(p11, p21, clink, dL, p10.s);
				if (eq1) return false; // No sympathy for operator synonyms
				return false;
			} else if (d1 == Disp.FC) {
				if (eq0 && (clink.equals(Op.eq) || clink.equals(Op.equiv))) return extractDiffInner(p11, p21, clink, dL);
				if (eq1 && (clink.equals(Op.eq))) return extractDiffInner(p10, p20, clink, dL);
				return false;
			} else {
				Term p12 = t1.get(2); Term p22 = t2.get(2);
				boolean eq2 = p12.equals(p22);
				
				if (d1 == Disp.TOT) {
					if (!eq1) return false;
					Link adjlink = adjustLink(clink, (Operator) p11);
					if (eq0) return extractDiffInner(p12, p22, adjlink, dL);
					if (eq2) return extractDiffInner(p10, p20, adjlink, dL);
					// TODO for every possible association !!!

				} else if (d1 == Disp.QTT) {
					if (!eq0) return false;
					//if (eq1) return extractDiffInner(p12, p22, clink, dL); this is bullshit
					if (eq2) return extractDiffInner(p11, p21, clink, dL);
				} else if (d1 == Disp.DEF) {
					if (!eq1) return false;
					if (eq0 && (clink.equals(Op.eq) || clink.equals(Op.equiv) || clink.equals(Op.then))) return extractDiffInner(p12, p22, clink, dL);
					if (eq2 && (clink.equals(Op.eq))) return extractDiffInner(p10, p20, clink, dL);
				}
				return false;
			}
		}
	}

	/** a and b iif a and c  =>  b iif c
	 *  a < 0   iif b < 0    =>  a = b
	 *  a + b   <   a + c    =>  b = c
	 */
	static private Link adjustLink(Link lbroad, Operator op) {
		if (op.associatesBooleans()) return lbroad;
		return new Link(Op.eq);
	}

	/** Takes -2 > -3  into  3 > 2   for unary minus and div signs only */
	static private boolean extractDiffInnerWithReversing(Term a, Term b, Link clink, DifferencesLedger dL, String opstr) {
		Operator op = Op.getOperator(opstr);
		if (op.isReversing()) return extractDiffInner(b, a, clink, dL);
		return extractDiffInner(a, b, clink, dL);
	}

	/** Made so that permutations aren't calculated every time. */
	public ArrayList<Term> getPermutations() {
		if (permutations == null) permutations = permute(this);
		return permutations.vs;
	}

	static protected Permutations permute(Term t) {
		Permutations perm = new Permutations();
		Disp disp = t.getDisposition();
		
		if (disp == Disp.F) {
			perm.add(t);
		} 
		else if (disp == Disp.TOT) {
			Operator op = (Operator) (t.get(1));
			boolean commutative = op.isCommutative();
			for (Term permta: t.get(0).getPermutations()) {
			for (Term permtb: t.get(2).getPermutations()) {
				perm.add(glueTerms(new Term[]{permta, op, permtb}));
				if (commutative) perm.add(glueTerms(new Term[]{permtb, op, permta}));
			}
			}
		}
		else if (disp == Disp.OT) {
			for (Term permt: t.get(1).getPermutations()) {
				perm.add(glueTerms(new Term[]{t.get(0), permt}));
			}
		}
		else if (disp == Disp.QTT) {
			for (Term permc: permute(t.get(1)).vs) {
			for (Term permp: permute(t.get(2)).vs) {
				perm.add(glueTerms(new Term[]{t.get(0), permc, permp}));
			}
			}
		}
		else if (disp == Disp.SET) {
			// TODO are sets still a thing? i'd think so?
		}
		else if (disp == Disp.C) {
			return Collection.permute((Collection) t);
		}
		else if (disp == Disp.FC) {
			for (Term permc: t.get(1).getPermutations()) {
				perm.add(glueTerms(new Term[]{t.get(0), permc}));
			}
		}
		else if (disp == Disp.DEF) {
			return Definition.permute((Definition) t);
		}
		return perm;
	}
	
	
	static public class ExceptionTheoremNotApplicable extends Exception {};
	//static public class ExceptionTrivialEquality extends Exception {};

	//static public class ExceptionQuantifierOperatorSynthax extends TermSynthaxException {};
	
	abstract static public class TermSynthaxException extends GenException {
		public String errorType() { return "Term";}
	}
	static public class QuantifierSynthaxException_Colon extends TermSynthaxException {
		LinkedList<Term> err;
		public QuantifierSynthaxException_Colon (LinkedList<Term> err) { this.err = err; }
		public String errorMessage() { return "Quantifier couldn't find mandatory ':' token in term " + err.toString(); }
	}
	static public class QuantifierSynthaxException_Size extends TermSynthaxException {
		LinkedList<Term> err;
		public QuantifierSynthaxException_Size (LinkedList<Term> err) { this.err = err; }
		public String errorMessage() { return "Quantifier has too few tokens in term " + err.toString() + " (size=" + err.size() + ")"; }
	}
	static public class TermDispositionUnknownException extends TermSynthaxException {
		Term t;
		public TermDispositionUnknownException (Term t) { this.t = t; }
		public String errorMessage() { return "Following term has an unknown disposition: " + t.toString(); }
	}
	
	
	
	static public class Permutations {
		public ArrayList<Term> vs;
		public Permutations() {
			vs = new ArrayList<Term>();
		}
		public void add(Term t) {
			vs.add(t);
		}
	}
	
	static public Term replace(Term term, Term from, Term into) {
		
		if (term.equals(from)) return into;
		Disp d = term.getDisposition();
		if (d == Disp.F) {
			if (term.equals(from)) return into;
			return term;
		} else if (d == Disp.C) {
			Collection col = new Collection();
			Collection tc = (Collection) term;
			for (Term t: tc.items) col.addTerm(replace(t, from, into));
			return col;
		} else {
			Term res = new Term();
			for (Term t: term.v) res.addTerm(replace(t, from, into));
			return res;
		}
		
	}
	
}
