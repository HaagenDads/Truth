package Elements;

import java.util.ArrayList;
import java.util.Iterator;

import Elements.Statement;

/* 
 * 		Only used to better manage Demonstration
 */
public class Assumptions implements Iterable<Assump>{

	private ArrayList<Assump> list;
	public Assumptions() {
		list = new ArrayList<Assump>();
	}
	
	public int size() {
		return list.size();
	}
	
	public Assump get (int i) {
		return list.get(i);
	}
	
	public void acceptAssumptionFromHypothesis(Statement st) {
		addtolist(new Assump(st, "hyp"));
	}
	public void acceptAssumptionFromCasesHypothesis(Statement st) {
		addtolist(new Assump(st, "-1"));
	}
	public void acceptAssumptionFromDemonstration(Statement st, BlockStamp blockstamp) {
		addtolist(new Assump(st, blockstamp.getStamp()));
	}
	
	/*
	 *     There's a difference because I need all of these assumptions to have the same ID
	 */
	public void acceptAssumptionFromDemonstrationThroughCases(Assump a) {
		addtolist(a);
	}
	
	private void addtolist(Assump a) {
		for (Assump b: list) {
			if (a.equals(b)) return;
		}
		list.add(a);
	}
	
	public Assumptions copy() {
		Assumptions clone = new Assumptions();
		for (Assump a: list) clone.list.add(a.copy());
		return clone;
	}

	public Iterator<Assump> iterator() {
		return list.iterator();
	}

	/* For a term like n or g(n), find any relevant assumptions
	 * eg n > 0, g(n) < 4, n is even, forall x: g(x) > 0 */
	public ArrayList<Term> getRelevantAssump(Term term) {
		ArrayList<Term> result = new ArrayList<Term>();
		for (Assump a: this) {
			Term out = isRelevant(a.st, term);
			if (out != null) result.add(out);
		}
		return result;
	}
	
	private Term isRelevant(Statement st, Term t) {
		if (t.equals(st.lside)) return st.toTerm();
		if (t.equals(st.rside)) return st.toTerm();
		else if (st.isTrueImplication()) {
			Term impl = st.getTrueImplication();
			Term.Disp disp = impl.getDisposition();
			if (disp == Term.Disp.TOT) return isRelevant(impl, t);
		}
		return null;
	}
	
	private Term isRelevant(Term assump, Term t) {
		if (Link.isLink(assump.get(1).s)) {
			if (assump.get(0).equals(t) || assump.get(2).equals(t)) return assump;
		}
		return null;
	}
	
	
}
