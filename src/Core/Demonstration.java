package Core;

import java.util.ArrayList;
import java.util.LinkedList;

import Elements.*;
import Elements.Term.ExceptionTheoremNotApplicable;
import Operation.BooleanLogic;
import Operation.BooleanLogic.ExceptionBooleanCasting;

public class Demonstration {

	private final static int printPriority = 3; // 3 = [FATAL] only; 1 = broad;
	boolean isNested;
	
	private Body body;
	Statement proposition;
	Assumptions assumptions;
	Logging nlog;
	Theorem source;
	

	public Demonstration (String body, Theorem thm) {
		proposition = thm.statement;
		assumptions = thm.assumptions;
		
		ArrayList<String> arrbody = new ArrayList<String>();
		for (String x: body.split(" ")) { if (!x.equals("")) arrbody.add(x.trim()); }
		
		init(new Body(arrbody), thm);
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
	
	
	public boolean solveDemonstration() {
		
		for (ArrayList<String> subBody: body.parseBody()) {
			
			if (subBody.get(0).equals("\\startcase")) {
				
				nlog.createCase();
				Cases cases = new Cases(subBody.get(1));
				cases.parseCase(subBody);
				
				for (int i=0; i<cases.nested.size(); i++) {
					Demonstration d = cases.nested.get(i);
					Statement casehypothesis = cases.hypothesis.get(i);
					nlog.addCase(casehypothesis);
					d.solveDemonstration();
				}
					
				ArrayList<Assump> caseConclusions = acceptCasesCommunProvenStatements(cases);
				nlog.closeCase(caseConclusions);
				
			
			} else if (subBody.get(0).equalsIgnoreCase("\\let")) {
				if (subBody.get(2).equals("\\in")) {
					source.variables.add(new Variable(subBody.get(1), subBody.get(3)));
				} else {
					printout(3, "Could not comprehend variable initialization from: " + subBody); 
				}
				
			} else {
				compileProposition(subBody);
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
	
	private void compileProposition (ArrayList<String> subbody) {
			
		boolean proposition = true;
		Term first_exp = null;
		ArrayList<String> exp_right = new ArrayList<String>();
		Term t1 = null;
		Term t2;
		Link currentlink = null;
		
		LinkedList<Link> linkserie = new LinkedList<Link>();
		for (String token: subbody) {
			if (Link.isLink(token) && assertGroundParenthesis(exp_right)) {
				linkserie.add(new Link(token));
				t2 = Term.extractTerms(exp_right);
				if (t1 != null) {
					boolean innerState = validateStatement(t1, t2, currentlink);
					proposition = proposition && innerState;
					if (!innerState) printout(3, "Could not validate statement " + t1 + currentlink.toString() + t2);
				} else {
					// First expression - No need to validate anything
					nlog.addLine(null, first_exp = t2, null);
				}
				t1 = t2;
				currentlink = linkserie.getLast();
				exp_right = new ArrayList<String>();
			} else {
				exp_right.add(token);
			}
		}

		linkserie.add(currentlink);
		t2 = Term.extractTerms(exp_right);
		if (!validateStatement(t1, t2, currentlink)) {
			printout(3, "Could not validate statement " + t1 + currentlink.toString() + t2);
			proposition = false;
		}
		
		Link conclusion = Link.reduceSerie(linkserie);
		proposition = proposition && !conclusion.equals("");
		if (proposition) assumptions.acceptAssumptionFromDemonstration(new Statement(conclusion, first_exp, t2), nlog.blockstamp);
	}
	
	
	private boolean assertGroundParenthesis(ArrayList<String> exp) {
		int openedParenthesis = 0;
		for (String s: exp) {
		    for (int i = 0; i < s.length(); i++) {
		        if (s.charAt(i) == '(') {
		        	openedParenthesis++;
		        } else if (s.charAt(i) == ')') {
		            if (openedParenthesis-- == 0)  printout(3, "Trying to close closed parenthesis in " + exp);
		        }
		    }
		}
		return openedParenthesis == 0;
	}
	
	
	private boolean isTheoremDemonstrated() {
		for (Assump a: assumptions) {
			if (a.st.equals(proposition)) return true;
		}
		return false;
	}
	
	/*
	 *   Takes commun proven statements from cases and accepts them in the higher demonstration level
	 */
	private ArrayList<Assump> acceptCasesCommunProvenStatements(Cases cases) {
		
		if (cases.validatePartition()) {
			ArrayList<Assump> bulkResults = new ArrayList<Assump>();
			for (Assump x: cases.enumerateCommunStatements()) {
				bulkResults.add(x);
			}
			assumptions.acceptAssumptionFromDemonstrationThroughCases(bulkResults, nlog.blockstamp);
			return bulkResults;
		}
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
	private boolean validateStatement(Term t1, Term t2, Link link) {
		ArrayList<Statement> diffLedger = Term.extractDiff(t1, t2, link);
		for (Statement st: diffLedger) {
			Justification solution = validateStatementSpecificDifference(t1, t2, st);
			if (solution != null) {
				nlog.addLine(link, t2, solution);
				return true;
			}
		}
		printout(3, "Couldnt use assumptions nor math");
		nlog.addLine(link, t2, new Justification("error"));
		return false;
	}
	
	private Justification validateStatementSpecificDifference(Term t1, Term t2, Statement diff) {
		
		printout("\nValidate statement: " + t1 + diff.link.toString() + t2 + "  <==>  " + diff.toString());
		
		// Using assumptions
		for (Assump a: assumptions) {
			printout(a.st.toString());
			if (a.st.equals(diff)) {
				return new Justification(a);
			}
		}
		
		// Using math (assuming X BinOperation Y / UnaryOperation X )
		try {
			if (solveMath(diff.lside).equals(solveMath(diff.rside))) {
				return new Justification("BooleanLogic");
			}
		} catch (ExceptionBooleanCasting e) {}
		
		
		// Applying previous theorems
		for (Theorem th: source.loadedTheorems) {
			if (matchTheorem(th, diff)) {
				return new Justification(th);
			}
		}	
		
		return null;
	}
	
	
	private boolean matchTheorem (Theorem th, Statement prop) {
		if (!Link.isSufficient(th.statement.link, prop.link)) return false;
		return matchTheoremUnilateral(th, prop) || matchTheoremUnilateral(th, prop.switchSides());
	}
	
	private boolean matchTheoremUnilateral(Theorem th, Statement prop) {
		
		ArrayList<Term> perms = Term.permute(prop.lside).vs;
		for (Term propPermutation: perms) { /////////////////////////////////////////// VALIDATE PERMS (implies)///////
			try {
				// Extracts every substitutions that would have to be made for the theorem to match the proposition
				// A substitution would be " replace 'a' by 'thm.x' from the theorem "
				ArrayList<Statement> substitutions = Term.extractDiffArray(th.statement.lside, propPermutation);
				substitutions = orderSubstitutions(substitutions);
				assertValideSubstitutions(th, substitutions);
				
				// Applies every required changes so that both left sides would be identical
				Term alteredRightSide = prop.rside;
				for (Statement st: substitutions) {
					alteredRightSide = substitute(alteredRightSide, st.rside, st.lside);
				}
				
				// Check if the changes are enough for the right sides to be identical
				if (th.name.equals("Implication")) {
					printout("\n ** :");
					for (Statement st: substitutions) {
						printout("st: " + st.toString());
					}
					printout(alteredRightSide + ", " + th.statement.rside);
				}
				Term thmRightSide = reduceTheoremVariables(th.statement.rside, substitutions);
				if (alteredRightSide.equals(thmRightSide)) {
					printout("** Match theorem : " + substitutions + "   -from " + th.name);
					
					for (Statement st: substitutions) {
						Variable v = th.getVariable(st.lside.s);
						if (v != null && matchTheoremTypes(v, st.rside)) {
							printout("** Types have been validates! ");
							return true;
						}
					}
				}
				
			} catch (Term.ExceptionTheoremNotApplicable e) {}
		}
		return false;
	}
	
	
	private Term reduceTheoremVariables(Term t, ArrayList<Statement> subs) {
		t.flatten();
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
	
	
	private void assertValideSubstitutions(Theorem th, ArrayList<Statement> substitutions) throws ExceptionTheoremNotApplicable {
		for (int i=0; i<substitutions.size(); i++) {
			
			Statement outerst = substitutions.get(i);
			if (th.getVariable(outerst.lside.s) == null) {
				if (!outerst.lside.equals(outerst.rside)) throw new ExceptionTheoremNotApplicable();
			} else {
				for (int j=i+1; j<substitutions.size(); j++) {
					
					Statement innerst = substitutions.get(j);
					if (outerst.lside.equals(innerst.lside) && !outerst.rside.equals(innerst.rside)) {
						throw new ExceptionTheoremNotApplicable();
					}
				}
			}
		}
	}
	
	
	private boolean matchTheoremTypes(Variable v, Term t) {
		return (v.type.equals(getType(t)));
	}
	
	private String getType(Term t) {
		Term tcopy = t.copy();
		tcopy.applyType(source);
		try {
			return solveMath(tcopy).s;
		} catch (Exception e) {}
		return null;
	}
	
	
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
	
	
	private Term substitute(Term from, Term key, Term into) {
		Term result = new Term();
		if (from.isShallow()) {
			if (key.isShallow() && from.equals(key)) return into;
			else return from;
		}
		if (from.equals(key)) return into;
		for (Term x: from.v) {
			result.addTerm(substitute(x, key, into));
		}
		
		return result;
	}
	
	
	private Term solveMath(Term t) throws ExceptionBooleanCasting {
		
		t.flatten();
		if (t.isShallow()) return t;
		String X = null;
		String Op = null;
		String Y = null;
		for (Term token: t.v) {
			if (!token.isShallow()) token = solveMath(token);
			if (isUnaryOperator(token.s)) {
				if (X == null && Op == null) Op = token.s;
				else if (X == null) printout(3, "Multiple unary operators havent been though of yet.");
			} else if (isBinaryOperator(token.s)) {
				if (X == null) printout(3, "Couldnt solve for ': " + token + "'");
				else if (Op == null) Op = token.s;
				else printout(3, "Couldnt solve for '" + X + " " + Op + " : " + token + "'");
			} else {
				if (X == null && Op == null) X = token.s;
				else if (X == null) X = solveUnaryOperator(Op, token.s);
				else if (Op == null) printout(3, "Couldnt solve for '" + X + " : " + token + "'");
				else if (Y == null) X = solveBinaryOperator(X, Op, token.s);
				else printout(3, "Couldnt solve for '" + X + " " + Op + " " + Y + " : " + token + "'");
			}
		}
		return new Term(X);
		
	}
	
		
	static public boolean isBinaryOperator(String s) {
		if (s==null) return false;
		String[] opSet = new String[]{"\\or", "\\and", "\\implies"};
		for (String op: opSet) {
			if (s.equals(op)) return true;
		}
		return false;
	}
	
	
	private String solveBinaryOperator(String x, String op, String token) throws ExceptionBooleanCasting {
		if (!isBinaryOperator(op)) printout(3, "Tried to solve binary operator for '" + op + "'");
		return BooleanLogic.applyBinaryLogic(x, op, token);
	}
	
	
	static public boolean isUnaryOperator(String s) {
		if (s==null) return false;
		String[] opSet = new String[]{"\\not"};
		for (String op: opSet) {
			if (s.equals(op)) return true;
		}
		return false;
	}
	
	
	private String solveUnaryOperator(String op, String x) throws ExceptionBooleanCasting {
		if (!isUnaryOperator(op)) printout(3, "Tried to solve unary operator for '" + op + "'");
		return BooleanLogic.applyUnaryLogic(op, x);
	}
	
	static public boolean isOperator(String s) {
		return isUnaryOperator(s) || isBinaryOperator(s);
	}
	
	
	private int parseCases(int initpos, Variable casevar, Cases cases) {

		if (body.get(initpos).equals("\\case") && body.get(initpos+1).equals(casevar.name)) {
			ArrayList<String> rightHand = new ArrayList<String>();

			// Extracting hypothesis
			int pos = initpos + 2;
			while (!body.get(pos+1).equals("{")) {
				pos += 1;
				rightHand.add(body.get(pos));
			}
			
			// Assigning a partition element to the variable; we then need to use "=".
			Statement hypothesis = new Statement(new Link("="), new Term(casevar.name), Term.extractTerms(rightHand));
			
			// Extracting the conditional body
			ArrayList<String> nestedBody = new ArrayList<String>();
			int openedBrackets = 1;
			pos += 2; // pos+1 is the bracket, pos+2 is the next token
			
			while (openedBrackets > 0) {
				if (pos == body.size()) printout(3, "There has to be an opened bracket somewhere! (" + openedBrackets + ")");
				
				String next = body.get(pos);
				if (next.equals("{")) {
					openedBrackets += 1;
				} else if (next.equals("}")) {
					openedBrackets -= 1;
				} else {
					nestedBody.add(next);
				}
				pos += 1;
				
			}
			Assumptions nestedAssumptions = assumptions.copy();
			nestedAssumptions.acceptAssumptionFromCasesHypothesis(hypothesis);
			Demonstration nested = new Demonstration(new Body(nestedBody), proposition, nestedAssumptions, source);
			nested.isNested = true;
			
			cases.addCase(hypothesis, nested);
			return pos;
		}
		printout(3, "Couldn't match '\\case " + casevar.name + "'");
		return body.size();
		
	}
			
	
	private void printout(String text) {
		printout(1, text);
	}
	
	private void printout(int priority, String text) {
		if (printPriority > priority) return;
		if (priority == 3) System.out.println("[FATAL]" + text);
		else System.out.println(text);
	}
	
	
	public class Cases {
		
		Variable casevar;
		String set;
		ArrayList<Statement> hypothesis;
		ArrayList<Demonstration> nested;
		
		public Cases(String casename) {
			hypothesis = new ArrayList<Statement>();
			nested = new ArrayList<Demonstration>();
			for (Variable v: source.variables) {
				if (v.name.equals(casename)) {
					casevar = v; break;
				}
			}
			set = casevar.type;
		}
		
		public void addCase(Statement hypothesis, Demonstration nested) {
			this.hypothesis.add(hypothesis);
			this.nested.add(nested);
		}
		
		public boolean validatePartition() {
			if (set.equals("\\boolean")) {
				return BooleanLogic.validatePartition(casevar, hypothesis);
			}
			printout(3, "Couldnt validate partition. " + set);
			return false;
		}
		
		
		/*
		 *   Find commun statements proven in every case in order to make a general statement, given a partition of cases
		 *   Returns every commun assumptions
		 */
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
		}
		
		public void parseCase(ArrayList<String> body) {
			int pos = 2;
			while (!(body.get(pos).equals("\\endcase"))) {
				pos = parseCases(pos, casevar, this);
			}
		}
		
	}
}
