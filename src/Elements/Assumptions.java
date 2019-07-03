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
		addtolist(new Assump(st, "0"));
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
	public void acceptAssumptionFromDemonstrationThroughCases(ArrayList<Assump> assumpList, BlockStamp blockstamp) {
		for (Assump a: assumpList) {
			addtolist(new Assump(a.st, blockstamp.getStamp()));
		}
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
		return new Iterator<Assump> () {
            private final Iterator<Assump> iter = list.iterator();

            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public Assump next() {
                return iter.next();
            }
        };
	}
	
	
}
