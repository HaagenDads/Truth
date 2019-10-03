package Core;

import java.util.ArrayList;
import java.util.Iterator;

import Elements.*;
import Elements.ArrayString.Sequence;
import Elements.Term.Disp;
import Elements.Term.ExceptionTheoremNotApplicable;
import Elements.Term.TermSynthaxException;
import Elements.Type.ExceptionTypeUnknown;
import Operation.BooleanLogic;
import Operation.NaturalNumbers;
import Operation.Op;
import Operation.Operator;
import Operation.RealNumbers;

public class Demonstration extends Utils {
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
	
	
	public void solveDemonstration() throws ExceptionCaseNonvalid, TermSynthaxException {
		printout("body seqarray size::" + body.seqarray.size());
		for (Sequence sequence: body.seqarray) {
			if (sequence.isSubbody()) {
				Cases cases = new Cases();
				cases.parseCase(sequence.getV(0));

				nlog.createCase(cases.hypothesis);
				cases.nested.solveDemonstration();
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
				} 
				catch (GenException e) { e.explain(); return;	}
			}
		}
		
		// Check if we have demonstrated the proposition
		if (!isNested) nlog.conclude(isTheoremDemonstrated());
	}
	
	private void compileProposition (Sequence sequence) throws ExceptionComprehension, TermSynthaxException {
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
		if (firstexp == null) { System.out.println("Something real bad happened "); return; }
		if (conclusion == null) { System.out.println("Link serie does not simplify."); return; }
		if (proposition && !conclusion.equals("")) {
			
			Statement takeaway = new Statement(conclusion, firstexp, t2);
			
			// Special case where the conclusion is that T => p. Since (T -> p) => (p <-> T), we would like
			// the link to reflect this reality.
			if (conclusion.equals(Op.then)) {
				if (firstexp.equalsString("\\true")) takeaway = new Statement(new Link(Op.equiv), t2, firstexp);
			}

			// If conclusion was "true <==> a > 0", the summary should be a > 0.
			if ((conclusion.equals(Op.then) || conclusion.equals(Op.equiv))
				&& firstexp.equalsString("\\true") && t2.getDisposition() == Disp.TOT && ((Operator) t2.get(1)).isComparing())
			{
				takeaway = new Statement(new Link((Operator) t2.get(1)), t2.get(0), t2.get(2));
			}
			if (conclusion.equals(Op.equiv)
				 && t2.equalsString("\\true") && firstexp.getDisposition() == Disp.TOT && ((Operator) firstexp.get(1)).isComparing())
			{
				takeaway = new Statement(new Link((Operator) firstexp.get(1)), firstexp.get(0), firstexp.get(2));
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
	private void acceptCasesCommunProvenStatements(Cases cases) {
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
		try {
			Justification solution = findSolutionForStatementValidation(t1, t2, link);
			if (solution != null) {
				nlog.addLine(link, t2, solution);
				printout(":success!:");
				return true;
			}
			nlog.addLine(link, t2, new Justification("error"));
		} catch (Type.ExceptionCollectionOutOfDomain e) {
			e.explain_nostack();
			nlog.addLine(link, t2, new Justification("error: collection out of domain"));
		}
		return false;
	}

	// TODO a=b <==> c=d  .  if no solution is found, look for a=c && b=d, then a=d && b=c  .
	// Types of statements:
	// bool <==> bool
	// bool ==> bool
	// a = b
	// a < b
	private Justification findSolutionForStatementValidation(Term t1, Term t2, Link link) throws ExceptionComprehension {
		ArrayList<Statement> diffLedger = Term.extractDiff(t1, t2, link);
		if (diffLedger.size() == 0) return new Justification("Trivial equality");

		Justification solution = null;
		for (Statement st: diffLedger) {
			printout("\nValidate statement: " + t1 + st.link.toString() + t2 + "  <==>  " + st.toString());
			solution = validateStatementSpecificDifference(st);
			if (solution != null) break;
		}
		return solution;
	}
	
	/** ----------------------------------------------------------------------------------
	 *  Core of the demonstrator. Tries a bunch of different ways to validate a statement. 
	 *  ----------------------------------------------------------------------------------
	 * */
	private Justification validateStatementSpecificDifference(Statement diff) throws ExceptionComprehension {
	
		Justification solution = null;
		int k = 0;
		while (solution == null) {
			// Trivial proposition
			if (k==0) solution = validateTrivialImplication(diff);
			else if (k==1) solution = SolveFunctionEvaluation(diff);		// Function evaluation
			else if (k==2) solution = tryKnownAssumptions(diff); 			// Using assumptions
			else if (k==3) solution = tryUsingMath(diff);					// Using math
			else if (k==4) solution = tryPreviousTheorems(diff);			// Applying previous theorems
			else break;
			k++;
		}
		// https://stackoverflow.com/questions/4280727/java-creating-an-array-of-methods
		return solution;
	}
	
	/* Can be a nested statement to prove; from the form "\true \eq (x=0)" or "\true \eq \exists x \suchthat (x > 0) */
	private Justification validateTrivialImplication(Statement diff) throws ExceptionComprehension {
		
		Link link = diff.link;
		for (Statement uniDiff: new Statement[]{diff, diff.switchSides()}) {
			Term t1 = uniDiff.lside;
			Term t2 = uniDiff.rside;
			if (t1.equalsString("\\true") && (link.equals(Op.equiv) || link.equals(Op.equiv))) {
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
				if (disp == Term.Disp.TOT && Link.isLink(t2.get(1).s)) {
					return findSolutionForStatementValidation(t2.get(0), t2.get(2), new Link(t2.get(1).s));
				}			
			}
		}
		return null;
	}
	
	/** Resolution for statements of the sort:
	 *  T <==> \exists x (x > 0)     where here, quant="\exists", cond="x", prop="(x > 0)"
	 */
	private Justification validateExistentialProposition(Operator quant, Term cond, Term prop) throws Type.ExceptionTypeUnknown {
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
				return assign.lside.equals(cond) && Type.matchtypes(assign.rside, cond, source);
			}
			// TODO i dont want to always have to write x = 4 ==> x > 3 ==> ... ==> f(x) > 0 ==> Exists x | f(x) > 0
		}
		return false;
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
	
	private Justification tryKnownAssumptions (Statement diff) {
		for (Assump a: assumptions) {
			if (a.st.equals(diff)) return new Justification(a);
		}
		return null;
	}
	
	private Justification tryUsingMath (Statement diff) {
		try {
			Term matht = new Term();
			matht.addTerm(diff.lside);
			matht.addTerm(Op.getOperator(diff.link.link));
			matht.addTerm(diff.rside);
			if (solveMath(matht).equalsString("\\true")) return new Justification("SolvingMath");
		} catch (Exception ignored) {}
		return null;
	}
	
	private Justification tryPreviousTheorems (Statement diff) throws ExceptionTypeUnknown {
		for (Theorem th: source.loadedTheorems) {
			if (matchTheorem(th, diff)) return new Justification(th);
		}	
		return null;
	}
	
	
	/** ----------------------------------------------------------------------------------
	 *  Core of matching theorems 
	 *  ----------------------------------------------------------------------------------
	 */
	private boolean matchTheorem (Theorem th, Statement prop) throws Type.ExceptionTypeUnknown {
		if (!Link.isSufficient(th.statement.link, prop.link)) return false;
		if (matchTheoremUnilateral(th, prop)) return true;
		return Link.isCommutative(prop.link) && matchTheoremUnilateral(th, prop.switchSides());
	}
	
	/* We first find permutations from the left side, make sure the types match. */
	private boolean matchTheoremUnilateral(Theorem th, Statement prop) throws Type.ExceptionTypeUnknown {
		//for (Term tt: perms) System.out.println("- " + tt.toString());
		ArrayList<Term> pmleft = prop.lside.getPermutations();
		ArrayList<Term> pmright = prop.rside.getPermutations();
		
		// TODO think about 'is \true gonna yield an empty validperm?'
		
		_debug(th, ":thm - " + debugthm + ": size pmleft = " + pmleft.size());
		ValidPermutations validleftperms = extractValidPerms(pmleft, th, th.statement.lside);
		if (validleftperms.isEmpty()) return false;
		_debug(th, ":leftperms:");
		for (UniquePerm pleft: validleftperms) _debug(th, pleft.sts.toString());
		
		ValidPermutations validrightperms = extractValidPerms(pmright, th, th.statement.rside);
		if (validrightperms.isEmpty()) return false;
		_debug(th, ":rightperms:");
		for (UniquePerm pright: validrightperms) _debug(th, pright.sts.toString());
		
		for (UniquePerm pleft: validleftperms) {
		for (UniquePerm pright: validrightperms) {
			_debug(th, ":assertingsubs:");
			if (assertValidSubstitutions(pleft.sts, pright.sts)) {
				return true;
			}
			_debug(th, ":!!didntmatch:\n" + pleft.sts.toString() + "\n" + pright.sts.toString());
		}
		}
		
		
		return false;
	}
	
	private boolean assertValidSubstitutions(ArrayList<Statement> grpA, ArrayList<Statement> grpB) {
		for (Statement stA: grpA) {
		for (Statement stB: grpB) {
			if (stA.lside.equals(stB.lside) && !stA.rside.equals(stB.rside)) return false;
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
				// System.out.println("thside=" + thside.toString() + " propPermutation=" + propPermutation.toString());
				// substitutions = orderSubstitutions(substitutions);
				
				// checking here for types - trueSubs ignores substitutions of non-variables (e.g. \true, 4)
				// Case for 3 = 3, for example
				ArrayList<Statement> trueSubs = new ArrayList<>();
				for (Statement st: substitutions) {
					Variable v = th.getVariable(st.lside.s);
					if (v == null) {
						if (!st.lside.equals(st.rside)) {
							_debug(th, ":invalid nonequal nonvariable: " + st.toString());
							throw new ExceptionTheoremNotApplicable();
						}
					} else if (!matchTheoremTypes(v, st.rside)) throw new ExceptionTheoremNotApplicable();
					else trueSubs.add(st);
				}
				validperms.add(propPermutation, trueSubs);
			} catch (ExceptionTheoremNotApplicable ignored) {}
		}
		return validperms;
	}
	

	private static class ValidPermutations implements Iterable<UniquePerm>{
		ArrayList<UniquePerm> perms;
		public ValidPermutations () {
			perms = new ArrayList<>();
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
	private static class UniquePerm {
		@SuppressWarnings("unused")
		Term t;
		ArrayList<Statement> sts;
		public UniquePerm (Term t, ArrayList<Statement> sts) {
			this.t = t;
			this.sts = sts;
		}
	}

	/* v is the theorem type, t corresponds to the demonstration type */
	private boolean matchTheoremTypes(Variable v, Term t) throws Type.ExceptionTypeUnknown {
		//System.out.println(" :term: " + t.toString() + " - " + t.getDisposition());
		return Type.matchtypes(v.type, t.getType(source));
	}
	
	/*	Prioritizes largest substitutions before more shallow ones  */
	/*
	private ArrayList<Statement> orderSubstitutions(ArrayList<Statement> former) {
		System.out.println("ordersubs entry: " + former.toString());
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
				while (pos --> 0 && !found) {
					if (!Term.substitute(result.get(pos).rside, st.rside, new Term()).equals(result.get(pos).rside)) {  // TODO capital what?
						result.add(pos+1, st);
						resultsize++;
						found = true;
					}
				}
				if (!found) result.add(resultsize++ - nbshallow, st);
			}	
		}
		System.out.println("ordersubs exit: " + result.toString() + " resultsize=" + resultsize + " nbshallow=" + nbshallow);
		return result;
	}
	*/
	
	
	private Term solveMath(Term t) {
		
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
		String result;
		
		result = BooleanLogic.applyUnaryLogic(op, term.s);
		if (result != null) return new Term(result);
		
		result = RealNumbers.applyUnaryLogic(op, term.s);
		if (result != null) return new Term(result);
		
		return null;
	}

	/** Solve trivial quantifier operator, e.g. for all x: false. */
	private Term solveQuantifierOperator (Term term) {
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
		
		public Cases() {
			//hypothesis = new ArrayList<Statement>();
			//nested = new ArrayList<Demonstration>();
			//casevar = source.getVariable(casename);
			//set = casevar.type;
			//valid = true;
		}
		
		public void parseCase(ArrayString arr) throws ExceptionCaseNonvalid, TermSynthaxException {
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
		/*
		public void compilePartition() {
			
		}/*
		
		/* Answers to whether the partition is complete of partial - used to determine the assumption that follows from the cases */
		/*
		public boolean isPartitionComplete() {
			return false;/*
					
			if (set.equals("\\boolean")) {
				return "" + BooleanLogic.validatePartition(casevar, hypothesis);
			}
			if (set.equals("\\setnatual")) {
				return NaturalNumbers.validatePartition(casevar, hypothesis);
			}
			printout(3, "Couldnt validate partition. " + set);
			return null;
		}*/
		
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
		/*
		public ArrayList<Assump> enumerateCommunStatements() {
			ArrayList<Assump> result = new ArrayList<>();
			for (Assump x: nested.assumptions) {
				result.add(x);
			}
			return result;
		}*/
	}
	
	//static private class ExceptionCantSolveMath extends Exception {}
	//static private class ExceptionCantReduceQuantifier extends ExceptionCantSolveMath {}
	static public class ExceptionCaseNonvalid extends Exception {}


	abstract static public class ExceptionComprehension extends GenException { public String errorType() { return "Comprehension";}}
	static public class ExceptionComprehensionInQuantifier extends ExceptionComprehension {
		Term t;
		 ExceptionComprehensionInQuantifier (Term _t) { t = _t; }
		public String errorMessage() { return "In quantifier " + t.toString() + ", condition wasn't comprenhended."; }
	}
}
