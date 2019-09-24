package Elements;

import java.util.ArrayList;

import Operation.Op;



public class Collection extends Term {
	
	public ArrayList<Term> items;
	boolean iscartesian;
	boolean isset;
	public Collection () {
		super();
		items = new ArrayList<Term>();
		size = 0;
		iscollection = true;
		iscartesian = false;
		isset = false;
	}
	
	public Term get (int i) { return items.get(i);  }
	public boolean isEmpty() { return size == 0; }
	
	public boolean equalsString(String s) { return false; }
	public boolean equals (Term other) {
		if (!other.isCollection()) return false;
		Collection c = (Collection) other;
		
		int len = this.size;
		if (c.size != len) return false;
		
		for (int i=0; i<len; i++) {
			if (!other.get(i).equals(this.get(i))) return false;
		}
		return true;
	}
	
	public boolean isOperator() { return false; }
	public boolean isShallow() { return false; }
	public void flatten() {}
	
	public void addTerm (Term t) { size++; items.add(t); }
	
	public Collection copy() {
		Collection output = new Collection();
		for (Term t: items) output.addTerm(t.copy());
		return output;
	}
	public String toString() {
		if (isEmpty()) return "()";
		String link = ", ";
		if (iscartesian) link = " × ";
		String output = "(";
		String closing = ")";
		if (isset) {
			output = "{";
			closing = "}";
		}
		for (Term t: items) {
			//Disp tdisp = t.getDisposition();
			//if (!t.isShallow() && !(tdisp == Disp.C) && !(tdisp == Disp.FC)) output += "(" + t.toString() + ")" + link;
			//else
			output += t.toString() + link;
		}
		return output.substring(0, output.length()-link.length()) + closing;
	}
	
	
	public boolean isCartesian() { return iscartesian; }
	
	public void embedVariableNames(String head, ArrayList<String> vars) {
		for (Term x: items) x.embedVariableNames(head, vars);
	}

	public void removeEmbeding() {
		for (Term x: items) x.removeEmbeding();
	}
	

	static protected ArrayList<Statement> extractDiffArray(Term tthm, Term tprop, ArrayList<Statement> result) throws ExceptionTheoremNotApplicable {
		Collection c1 = (Collection) tthm;
		
		if (tprop.getDisposition() != Disp.C) throw new ExceptionTheoremNotApplicable();
		Collection c2 = (Collection) tprop;
		
		if (c1.size != c2.size) throw new ExceptionTheoremNotApplicable();
		for (int i=0; i<c1.size; i++) {
			result.addAll(Term.extractDiffArray(c1.get(i), c2.get(i), result));
		}
		return result;
	}
	
	static boolean extractDiffInner(Term t1, Term t2, Link clink, DifferencesLedger dL) {

		if (!(clink.equals(Op.eq) || clink.equals(Op.equiv))) return false;
		Collection c1 = (Collection) t1;
		Collection c2 = (Collection) t2;
		if (c1.size != c2.size) return false;
		
		int singleDifference = -1;
		for (int i=0; i<c1.size; i++) {
			if (!c1.get(i).equals(c2.get(i))) {
				if (singleDifference == -1) singleDifference = i;
				else return false; // Won't deal with several differences
			}
		}
		
		if (singleDifference == -1) return true;
		else {
			return Term.extractDiffInner(c1.get(singleDifference), c2.get(singleDifference), clink, dL);
		}
		
	}

	public Permutations getPermutations() {
		if (permutations == null) permutations = permute(this);
		return permutations;
	}


	/** Permutes the collection by iteratively adding permutations collections of growing dimensions. Cool stuff. */
	static protected Permutations permute (Collection c) {
		Permutations perm = new Permutations();
		
		ArrayList<Collection> collectionPerms = new ArrayList<Collection>();
		collectionPerms.add(new Collection());
		for (Term t: c.items) {
			ArrayList<Collection> newcolls = new ArrayList<Collection>();
			for (Term p: Term.permute(t).vs) {
				for (Collection col: collectionPerms) {
					Collection newcol = col.copy();
					newcol.addTerm(p);
					newcolls.add(newcol);
				}
			}
			collectionPerms = newcolls;
		}
		
		for (Term t: collectionPerms) perm.add(t);
		return perm;
	}
	
	
	/* ====================================
	 * Collection functions
	 */
	
	/*
	public boolean isElementOfSet (Set S) {
		return false;
	}*/
}
