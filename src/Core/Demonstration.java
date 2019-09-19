package Core;

import java.util.ArrayList;
import java.util.Iterator;

import Elements.*;
import Elements.ArrayString.Sequence;
import Elements.Term.Disp;
import Elements.Term.ExceptionTheoremNotApplicable;
import Elements.Term.ExceptionTrivialEquality;
import Elements.Term.Permutations;
import Operation.BooleanLogic;
import Operation.BooleanLogic.ExceptionBooleanCasting;
import Operation.NaturalNumbers;
import Operation.Op;
import Operation.Operator;
import Operation.RealNumbers;

public class Demonstration {

	private final static int printPriority = 1; // 3 = [FATAL] only; 1 = broad;
	private static final String debugthm = "AxiomAdditionIneq";
	
	private boolean isNested;
	
	private Body body;
	private Statement proposition;
	private Assumptions assumptions;
	private Logging nlog;
	private Theorem source;

	public Demonstration (String fulltext, Theorem thm) {
		proposition = thm.statement;
		assumptions = thm.assumptions;
		init(new Body(fulltext), thm);
	}
	public Demonstration (Body body, Statement proposition, Assumptions assumptions, Theorem thm) {
		this.proposition = proposition;
		this.assumptions = assumptions;
		init(body, thm);
	}
	
	private void init (Body body, Theorem thm) {
		isNested = false;
		this.body = body;
		this.source = thm;
		nlog = thm.nlog;
	}
	
	
	public boolean solveDemonstration() throws ExceptionCaseNonvalid {
		
		for (Sequence sequence: body.seqarray) {
			if (sequence.isSubbody()) {
				Cases cases = new Cases();
				cases.parseCase(sequence.getV(0));

				nlog.createCase(cases.hypothesis);
				if (!cases.nested.solveDemonstration()) return false;
				acceptCasesCommunProvenStatements(cases);
				nlog.closeCase();
			} else if (sequence.isAssignment()) {
				// TODO add checking for overloading and validating
				ArrayList<Variable> sts = Term.parseLetStatement(sequence.getV(0), source, false);
				source.variables.addAll(sts);
				nlog.addLine(sts);
			} else {
				try {
					compileProposition(sequence);
				} catch (ExceptionComprehension e) {
					e.explain();
					return false;
				}
			}
		}
		
		// Check if we have demonstrated the proposition
		if (!isNested) {
			boolean demonstrated = isTheoremDemonstrated();
			nlog.conclude(demonstrated);
			return demonstrated;
		}
		return false;		
	}
	
	private void compileProposition (Sequence sequence) throws ExceptionComprehension {
		boolean proposition = true;
		Term firstexp = null;
		Term t1 = null;
		Term t2 = null;
		
		for (int i=0; i<sequence.size(); i++) {
			t2 = Term.compileTerms(sequence.getV(i));
			
			if (t1 == null) {
				nlog.addLine(null, t2, null);
				firstexp = t2;
			} else {
				if (!validateStatement(t1, t2, sequence.getL(i-1))) {
					proposition = false;
					printout(3, "Could not validate statement " + t1 + sequence.getL(i-1).toString() + t2);
				}
			}
			t1 = t2;
		}
		
		Link conclusion = Link.reduceSerie(sequence.getLinks());
		if (proposition && !conclusion.equals("")) {
			
			Statement takeaway = new Statement(conclusion, firstexp, t2);
			
			// Special case where the conclusion is that T => p. Since (T -> p) => (p <-> T), we would like
			// the link to reflect this reality.
			if (conclusion.equals(Op.then) && firstexp.equalsString("\\true")) {
				takeaway = new Statement(new Link(Op.equiv), t2, firstexp);
			} 

			assumptions.acceptAssumptionFromDemonstration(takeaway, nlog.blockstamp);
		}
	}
	
	
	private boolean isTheoremDemonstrated() {
		printout(":end of demonstation. Known assumptions:");
		for (Assump a: assumptions) printout("-   " + a.st.toString());

		for (Assump a: assumptions) {	
			if (a.st.equals(proposition)) return true;
		}
		return false;
	}
	
	/*
	 *   Takes commun proven statements from cases and accepts them in the higher demonstration level
	 */
	private ArrayList<Assump> acceptCasesCommunProvenStatements(Cases cases) {
		// TODO generalise for QTT exists statements
		// TODO make this work
		//if (cases.validatePartition().equals("true")) {
		Assumptions nestedasmp = cases.nested.assumptions;
		for (int i=assumptions.size()+1; i<nestedasmp.size(); i++) {
			Assump a = nestedasmp.get(i);
			Term qtt = new Term();
			qtt.addTerm(Term.makeNewTerm("\\forall")); qtt.addTerm(cases.hypothesis.toTerm()); qtt.addTerm(a.st.toTerm());
			a.st = new Statement(new Link(Op.equiv), new Term("\\true"), qtt);
			System.out.println(": new statement!!!: " + a.st.toString());
			assumptions.acceptAssumptionFromDemonstrationThroughCases(a);
		}
		/*
		if (cases.isPartitionComplete()) {
			ArrayList<Assump> bulkResults = new ArrayList<Assump>();
			for (Assump x: cases.enumerateCommunStatements()) {
				bulkResults.add(x);
			}
			assumptions.acceptAssumptionFromDemonstrationThroughCases(bulkResults, nlog.blockstamp);
			return bulkResults;
		}*/
		return null;
	}
	
	/*
	 * 	Validating the smallest difference is often not enough. 
	 * 	If we know that f(x) = f(y) from a prior assumption and try to use it to go from f(x) to f(y) elsewhere,
	 *  the program would extract to difference to be "prove that x = y". Of course, if x=y, then f(x)=f(y).
	 *  But it is not sufficient. We also need to check if it's known that f(x)=f(y).
	 *  
	 *  f(g(x))=f(g(y)) could be true if we know that x=y, that g(x)=g(y) or that f(g(x)) = f(g(y)).
	 */
	private boolean validateStatement(Term t1, Term t2, Link link) throws ExceptionComprehension {
		
		Justification solution;
		try {
			ArrayList<Statement> diffLedger = Term.extractDiff(t1, t2, link);
			
			for (Statement st: diffLedger) {
				printout("\nValidate statement: " + t1 + st.link.toString() + t2 + "  <==>  " + st.toString());
				solution = validateStatementSpecificDifference(st);
				if (solution != null) {
					printout(":success!:");
					nlog.addLine(link, t2, solution);
					return true;
				}
			}
			printout(3, "Couldnt use assumptions nor math");
			nlog.addLine(link, t2, new Justification("error"));
			return false;
			
		} catch (ExceptionTrivialEquality e) {
			nlog.addLine(link, t2, new Justification("Trivial equality"));
			return true;
		}
	}
	
	/* Resolution for statements of the sort:
	 *    T <==> \exists x (x > 0)     where here, quant="\exists", cond="x", prop="(x > 0)"
	 */
	private Justification validateExistentialProposition(Operator quant, Term cond, Term prop) throws ExceptionComprehensionInQuantifier, Type.ExceptionTypeUnknown {
		boolean isexist = quant.equals(Op.exists); // isforall implied by the negation
	
		// TODO check if cond matches the set, eg x < 0 should be invalid if x in naturals

		// Self evident x s.t. x
		if (Type.matchtypes(cond, Type.Bool, source) && cond.equals(prop)) return new Justification("Self evident");

		// Have we seen assumptions of the sort: x=4 -> x>0,  or x<y -> x<3   or  P <-> Q
		for (Assump a: assumptions) {
			boolean linkeq = a.st.link.equals(Op.equiv);
			if (linkeq || a.st.link.equals(Op.then)) {
				if (a.st.rside.equals(prop)) {
					printout(":trying to match cond exist statement:");
					boolean matching = matchConditionalExistentialStatement(a.st.lside, cond, isexist);
					if (matching) return new Justification(a);
				}
			} 
			// iif gets to try with the switched positions of the assumption
			if (linkeq) {
				if (a.st.lside.equals(prop)) {
					boolean matching = matchConditionalExistentialStatement(a.st.rside, cond, isexist);
					if (matching) return new Justification(a);
				}
			}
		}
		return null;
	}

	/** Trying to assert whether x=4 -> P   can imply that  \exists x s.t. P */
	private boolean matchConditionalExistentialStatement (Term assumpLside, Term cond, boolean isexist) throws Type.ExceptionTypeUnknown {
		if (assumpLside.getDisposition() == Term.Disp.TOT && Link.isConditional(assumpLside.get(1).s)) {
			
			Statement assign = assumpLside.toStatement();
			
			// Exact match between conditions
			if (cond.getDisposition() == Term.Disp.TOT && assign.equals(cond.toStatement())) return true;
			
			// Partial match between conditions
			if (assign.link.equals(Op.eq) && isexist) {
				if (assign.lside.equals(cond) && Type.matchtypes(assign.rside, cond, source)) {
					return true;
				}
			}
			// TODO i dont want to always have to write x = 4 ==> x > 3 ==> ... ==> f(x) > 0 ==> Exists x | f(x) > 0
		}
		return false;
	}
	
	/* Can be a nested statement to prove; from the form "\true \eq (x=0)" or "\true \eq \exists x \suchthat (x > 0) */
	private Justification validateTrivialImplication(Term t1, Term t2, Link link) throws ExceptionComprehension {
		t1.flatten();
		if (link.equals(Op.equiv) && t1.equalsString("\\true")) {
			Term.Disp disp = t2.getDisposition();
			//if (disp == Term.Disp.F) return null; // Back to checking if: \true <=> x
			//if (disp == Term.Disp.OT) return null; // Maybe perform math or something like that

			if (disp == Term.Disp.QTT) {
				Term cond = t2.get(1);
				Term prop = t2.get(2);
				// Makes the distinction between x > 6 and x + 6 as a condition
				if (cond.getDisposition() == Term.Disp.TOT && !Link.isConditional(cond.get(1).s)) throw new ExceptionComprehensionInQuantifier(cond);
				if (!Type.matchtypes(prop, Type.Bool, source)) throw new ExceptionComprehensionInQuantifier(prop);
				return validateExistentialProposition((Operator) (t2.get(0)), t2.get(1), t2.get(2));
			}
			if (disp == Term.Disp.TOT) {
				Term left = t2.get(0);
				String lk = t2.get(1).s;
				Term right = t2.get(2);
				if (Link.isLink(lk)) {
					return validateStatementSpecificDifference(new Statement(new Link(lk), left, right));
				}
			}			
		}
		return null;
	}
	

	private Justification validateStatementSpecificDifference(Statement diff) throws ExceptionComprehension {
	
		// Trivial proposition
		Justification solution;
		solution = validateTrivialImplication(diff.lside, diff.rside, diff.link);
		if (solution != null) return solution;
		solution = validateTrivialImplication(diff.rside, diff.lside, diff.link);
		if (solution != null) return solution;
		
		// Function evaluation
		solution = SolveFunctionEvaluation(diff);
		if (solution != null) return solution;
		
		// Using assumptions
		for (Assump a: assumptions) {
			if (a.st.equals(diff)) {
				return new Justification(a);
			}
		}
		
		// Using math
		try {
			Term matht = new Term();
			matht.addTerm(diff.lside);
			matht.addTerm(Op.getOperator(diff.link.link));
			matht.addTerm(diff.rside);
			
			if (solveMath(matht).equalsString("\\true")) {
				return new Justification("SolvingMath");
			}
		} catch (Exception e) {}
		
		
		// Applying previous theorems
		for (Theorem th: source.loadedTheorems) {
			if (matchTheorem(th, diff)) {
				return new Justification(th);
			}
		}	
		
		return null;
	}
	
	private Justification SolveFunctionEvaluation (Statement diff) {
		for (Statement st: new Statement[]{diff, diff.switchSides()}) {
			if (st.lside.getDisposition() == Term.Disp.FC) {
				Function fnc = (Function) source.getVariable(st.lside.get(0).s);
				Collection col = (Collection) st.lside.get(1);
				if (fnc.domain.isElement(col, source)) {
					Term eval = fnc.getEvaluation(col, assumptions);
					if (eval != null) {
						printout(":fnceval: " + eval.toString() + ",  :rside:  " + st.rside.toString());
						if (eval.equals(st.rside)) {
							return new Justification("Evaluating function");
						}
					} else {
						printout(":null fnceval");
					}
				}
			}
		}
		return null;
	}
	
	private boolean matchTheorem (Theorem th, Statement prop) throws Type.ExceptionTypeUnknown {
		if (!Link.isSufficient(th.statement.link, prop.link)) return false;
		if (matchTheoremUnilateral(th, prop)) return true;
		if (Link.isCommutative(prop.link) && matchTheoremUnilateral(th, prop.switchSides())) return true;
		return false;
	}
	
	@SuppressWarnings("unused")
	private void _debug(Theorem thm, String thmname, String str) {
		if (printPriority==1 && thm.name.equals(thmname)) {
			System.out.println(str);
		}
	}
	
	/*
	 * We first find permutations from the left side, make sure the types match.
	 */
	private boolean matchTheoremUnilateral(Theorem th, Statement prop) throws Type.ExceptionTypeUnknown {
		//for (Term tt: perms) System.out.println("- " + tt.toString());
		Permutations pmleft = Term.permute(prop.lside);
		Permutations pmright = Term.permute(prop.rside);
		
		// TODO think about 'is \true gonna yield an empty validperm?'
		
		_debug(th, debugthm, ":thm - " + debugthm + ": size pmleft = " + pmleft.vs.size());
		ValidPermutations validleftperms = extractValidPerms(pmleft.vs, th, th.statement.lside);
		if (validleftperms.isEmpty()) return false;
		_debug(th, debugthm, ":leftperms:");
		for (UniquePerm pleft: validleftperms) _debug(th, debugthm, pleft.sts.toString());
		
		ValidPermutations validrightperms = extractValidPerms(pmright.vs, th, th.statement.rside);
		if (validrightperms.isEmpty()) return false;
		_debug(th, debugthm, ":rightperms:");
		for (UniquePerm pright: validrightperms) _debug(th, debugthm, pright.sts.toString());
		
		for (UniquePerm pleft: validleftperms) {
		for (UniquePerm pright: validrightperms) {
			_debug(th, debugthm, ":assertingsubs:");
			if (assertValidSubstitutions(pleft.sts, pright.sts)) {
				return true;
			}
			_debug(th, debugthm, ":!!didntmatch:\n" + pleft.sts.toString() + "\n" + pright.sts.toString());
		}
		}
		
		
		return false;
	}
	
	private boolean assertValidSubstitutions(ArrayList<Statement> grpA, ArrayList<Statement> grpB) {
		for (Statement stA: grpA) {
		for (Statement stB: grpB) {
			if (stA.lside.equals(stB.lside) && !stA.rside.equals(stB.rside)) {
				return false;
			}
		}
		}
		return true;
	}	
	
	// TODO assert that a thm cant have an undeclared variable ever
	private ValidPermutations extractValidPerms (ArrayList<Term> perms, Theorem th, Term thside) throws Type.ExceptionTypeUnknown {
		ValidPermutations validperms = new ValidPermutations();
		for (Term propPermutation: perms) { 
			
			
			try {
				// extractDiffArray already checks for surjectivity
				ArrayList<Statement> substitutions = Term.extractDiffArray(thside, propPermutation);
				substitutions = orderSubstitutions(substitutions);
				
				// checking here for types - trueSubs ignores substitutions of non-variables (e.g. \true, 4)
				// Case for 3 = 3, for example
				ArrayList<Statement> trueSubs = new ArrayList<Statement>();
				for (Statement st: substitutions) {
					Variable v = th.getVariable(st.lside.s);
					if (v == null) {
						if (!st.lside.equals(st.rside)) {
							_debug(th, debugthm, ":invalid nonequal nonvariable: " + st.toString());
							throw new ExceptionTheoremNotApplicable();
						}
					} else if (!matchTheoremTypes(v, st.rside)) throw new ExceptionTheoremNotApplicable();
					else trueSubs.add(st);
				}
				validperms.add(propPermutation, trueSubs);
			} catch (ExceptionTheoremNotApplicable e) {}
		}
		return validperms;
	}
	

	private class ValidPermutations implements Iterable<UniquePerm>{
		ArrayList<UniquePerm> perms;
		public ValidPermutations () {
			perms = new ArrayList<UniquePerm>();
		}
		public void add(Term propperm, ArrayList<Statement> subs) {
			perms.add(new UniquePerm(propperm, subs));
		}
		public boolean isEmpty() {
			return perms.size() == 0;
		}
		public Iterator<UniquePerm> iterator() {
			return perms.iterator();
		}
	}
	
	// TODO remove t if not actually needed... after running lots of tests.
	private class UniquePerm {
		@SuppressWarnings("unused")
		Term t;
		ArrayList<Statement> sts;
		public UniquePerm (Term t, ArrayList<Statement> sts) {
			this.t = t;
			this.sts = sts;
		}
	}
	
	
	/* FORGOT WHAT THAT DID... 
	private Term reduceTheoremVariables(Term t, ArrayList<Statement> subs) {
		if (t.isShallow()) return t;
		for (int i=subs.size()-1; i>0; i--) {
			for (int j=i-1; j>=0; j--) {
				Term ta = subs.get(j).rside;
				Term tb = subs.get(i).rside;
				if (ta.equals(tb)) t = substitute(t, subs.get(i).lside, subs.get(j).lside);
			}
		}
		return t;
	}
	*/
	
	/* v is the theorem type, t corresponds to the demonstration type */
	private boolean matchTheoremTypes(Variable v, Term t) throws Type.ExceptionTypeUnknown {
		//System.out.println(" :term: " + t.toString() + " - " + t.getDisposition());
		return Type.matchtypes(v.type, Type.getType(t, source));
	}
	
	/*	Prioritizes largest substitutions before more shallow ones  */
	private ArrayList<Statement> orderSubstitutions(ArrayList<Statement> former) {
		ArrayList<Statement> result = new ArrayList<Statement>();
		int nbshallow = 0;
		int resultsize = 0;
		for (Statement st: former) {
			if (st.rside.isShallow()) {
				result.add(resultsize++, st);
				nbshallow++;
			}
			else {
				boolean found = false;
				int pos = resultsize - nbshallow;
				while (0 < pos-- && !found) {
					if (!substitute(result.get(pos).rside, st.rside, new Term()).equals(result.get(pos).rside)) {
						result.add(pos+1, st);
						resultsize++;
						found = true;
					}
				}
				if (!found) result.add(resultsize++ - nbshallow, st);
			}	
		}
		return result;
	}
	
	
	static private Term substitute(Term from, Term key, Term into) {
		Term result = new Term();
		if (from.isShallow()) {
			if (key.isShallow() && from.equals(key)) return into;
			else return from;
		}
		if (from.equals(key)) return into;
		if (from.getDisposition() != Disp.C) {
			for (Term x: from.v) result.addTerm(substitute(x, key, into));
			return result;
		} else {
			Collection coll = (Collection) from;
			Collection res = new Collection();
			for (Term x: coll.items) res.addTerm(substitute(x, key, into));
			return res;
		}
		
		
	}
	
	
	private Term solveMath(Term t) throws ExceptionBooleanCasting, ExceptionCantReduceQuantifier {
		
		if (t == null) return null;
		Term.Disp disp = t.getDisposition();
		if (disp == Term.Disp.F) return t;
		if (disp == Term.Disp.OT) {
			Term t1 = solveMath(t.get(1));
			return solveUnaryOperator((Operator) (t.get(0)), t1);
		}
		if (disp == Term.Disp.TOT) {
			Term t1 = solveMath(t.get(0));
			Term t2 = solveMath(t.get(2));
			return solveBinaryOperator((Operator) (t.get(1)), t1, t2);
		}
		if (disp == Term.Disp.QTT) {
			return solveQuantifierOperator(t.get(2));
		}

		return null;
		
	}
	
	
	private Term solveBinaryOperator(Operator op, Term t1, Term t2) {
		if (!t1.isShallow() || !t2.isShallow()) return null;
		String result;
		
		result = NaturalNumbers.applyBinaryLogic(t1.s, op, t2.s);
		if (result != null) return new Term(result);
		
		result = RealNumbers.applyBinaryLogic(t1.s, op, t2.s);
		if (result != null) return new Term(result);
		
		result = BooleanLogic.applyBinaryLogic(t1.s, op, t2.s);
		if (result != null) return new Term(result);
		return null;
	}
	
	
	private Term solveUnaryOperator(Operator op, Term term) {
		if (!term.isShallow()) return null;
		
		String result = BooleanLogic.applyUnaryLogic(op, term.s);
		if (result == null) return null;
		return new Term(result);
	}
	
	private Term solveQuantifierOperator (Term term) {
		term.flatten();
		if (term.equalsString("\\true")) return term;
		if (term.equalsString("\\false")) return term;
		return null;
	}
	
	
	/* =========================================================
	 *  CASES
	 */
	
	/*
	private int parseCases(ArrayString subbody, int initpos, Variable casevar, Cases cases) throws ExceptionCaseNonvalid {

		if (subbody.get(initpos).equals("\\case") && subbody.get(initpos+1).equals(casevar.name)) {
			ArrayString assignment = new ArrayString();

			// Extracting hypothesis
			int pos = initpos;
			while (!subbody.get(++pos).equals("{")) {
				assignment.add(subbody.get(pos));
			}
			
			Term assign = Term.compileTerms(assignment);
			if (assign.getDisposition() != Term.Disp.TOT) throw new ExceptionCaseNonvalid();
			
			// TODO kill casevar - let complex cases live
			Statement hypothesis = new Statement(new Link(assign.get(1).s), assign.get(0), assign.get(2));
			
			// Extracting the conditional body
			ArrayString nestedBody = new ArrayString();
			int openedBrackets = 1;
			pos += 2; // pos+1 is the bracket, pos+2 is the next token
			
			while (openedBrackets > 0) {
				if (pos == subbody.size()) printout(3, "There has to be an opened bracket somewhere! (" + openedBrackets + ")");
				
				String next = subbody.get(pos++);
				if (next.equals("{")) openedBrackets += 1;
				else if (next.equals("}")) openedBrackets -= 1;
				else nestedBody.add(next);
			}
			
			Assumptions nestedAssumptions = assumptions.copy();
			nestedAssumptions.acceptAssumptionFromCasesHypothesis(hypothesis);
			Demonstration nested = new Demonstration(new Body(nestedBody), proposition, nestedAssumptions, source);
			nested.isNested = true;
			
			cases.addCase(hypothesis, nested);
			return pos;
		}
		printout(3, "Couldn't match '\\case " + casevar.name + "'");
		return subbody.size();
		
	} */
	
	public class Cases {
		
		Statement hypothesis;
		Demonstration nested;
		boolean valid;
		
		public Cases() {
			//hypothesis = new ArrayList<Statement>();
			//nested = new ArrayList<Demonstration>();
			//casevar = source.getVariable(casename);
			//set = casevar.type;
			//valid = true;
		}
		
		public void parseCase(ArrayString arr) throws ExceptionCaseNonvalid {			
			ArrayString[] split = arr.splitArrayBy("{", 1);
			ArrayString hypo = split[0];
			ArrayString subbody = split[1];
			subbody.removeLast();
			
			hypothesis = hypo.splitPrecedence().toStatement();
			if (hypothesis == null) throw new ExceptionCaseNonvalid();
			
			Assumptions nestedAssumptions = assumptions.copy();
			nestedAssumptions.acceptAssumptionFromCasesHypothesis(hypothesis);
			
			nested = new Demonstration(new Body(subbody), proposition, nestedAssumptions, source);
			nested.isNested = true;
		}
		
		/*
		public void addCase(Statement hypothesis, Demonstration nested) {
			String err = assertCase(hypothesis);
			if (err != null) {
				printout(3, err); valid = false;
			}
	
			this.hypothesis.add(hypothesis);
			this.nested.add(nested);
		}*/
		/*
		private String assertCase(Statement hypothesis) {
			String err = "Error in case hypothesis; ";
			
			if (!Link.isConditional(hypothesis.link.link)) return err + "link not a conditional statement.";
			else if (!hypothesis.lside.equalsString(casevar.name)) return err + "case variable invalid.";
			else if (!Type.matchtypes(new Term(casevar.name), hypothesis.rside, source)) return "types non-matching";
			return null;
		}*/
		
		// TODO 
		public void compilePartition() {
			
		}
		
		/* Answers to whether the partition is complete of partial - used to determine the assumption that follows from the cases */
		public boolean isPartitionComplete() {
			return false;/*
					
			if (set.equals("\\boolean")) {
				return "" + BooleanLogic.validatePartition(casevar, hypothesis);
			}
			if (set.equals("\\setnatual")) {
				return NaturalNumbers.validatePartition(casevar, hypothesis);
			}
			printout(3, "Couldnt validate partition. " + set);
			return null;*/
		}
		
		/*
		 *   Find commun statements proven in every case in order to make a general statement, given a partition of cases
		 *   Returns every commun assumptions
		 */
		/*
		public ArrayList<Assump> enumerateCommunStatements() {
			ArrayList<Assump> result = new ArrayList<Assump>();
			for (Assump x: nested.get(0).assumptions) {
				boolean all = true;
				Assump commun = new Assump(x.st, "");
				for (Demonstration demo: nested) {
					
					// found: Found That Assumption In This Particular Nested Demonstration
					boolean found = false;
					for (Assump y: demo.assumptions) {
						if (y.equals(x)) {
							found = true; 
							commun.mergeStamps(y);
							break;
						}
					}
					if (!found) {
						all = false; break;
					}
				}
				if (all) {
					result.add(commun);
				}
			}
			return result;
		}*/
		public ArrayList<Assump> enumerateCommunStatements() {
			ArrayList<Assump> result = new ArrayList<Assump>();
			for (Assump x: nested.assumptions) {
				result.add(x);
			}
			return result;
		}
	}

	
	static public void printout(String text) {
		printout(1, text);
	}
	
	static public void printout(int priority, String text) {
		if (printPriority > priority) return;
		if (priority == 3) System.out.println("[FATAL]" + text);
		else System.out.println(text);
	}
	
	private class ExceptionCantSolveMath extends Exception {}
	private class ExceptionCantReduceQuantifier extends ExceptionCantSolveMath {}
	
	public class ExceptionCaseNonvalid extends Exception {}

	abstract static public class ExceptionComprehension extends Exception {
		abstract public void explain();
	}
	static public class ExceptionComprehensionInQuantifier extends ExceptionComprehension {
		Term t;
		public ExceptionComprehensionInQuantifier (Term _t) { t = _t; }
		public void explain() {
			System.out.println("[COMPREHENSION ERROR] In quantifier " + t.toString() + ", condition wasn't comprenhended.");
		}
	}
}
