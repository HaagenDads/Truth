package Elements;

import java.util.ArrayList;

import Operation.Op;

public class Statement {
	
	public Link link;
	public Term lside;
	public Term rside;
	
	
	public Statement(Link link, Term leftHand, Term rightHand) {
		this.link = link;
		lside = leftHand;
		rside = rightHand;
	}
	public Statement(Link link, String leftHand, String rightHand) {
		this.link = link;
		lside = new Term(leftHand);
		rside = new Term(rightHand);
	}
	
	
	public String toString() {
		return lside + link.toString() + rside;
	}
	
	public Statement copy() {
		return new Statement(link.copy(), lside.copy(), rside.copy());
	}
	
	public boolean equals(Statement other) {
		if (this.link.equals(other.link)) {
			if (lside.equals(other.lside) && rside.equals(other.rside)) return true;
			if (Link.isCommutative(link)) {
				if (lside.equals(other.rside) && rside.equals(other.lside)) return true;
			}
		}
		if (link.hasSymmInverse() && (link.getSymmInverse()).equals(other.link)) {		// a > 0  ==  0 < a
			return lside.equals(other.rside) && rside.equals(other.lside);
		}
		return false;
		
	}
	
	public Statement switchSides() {
		return new Statement(link, rside, lside);
	}
	
	public void embedVariableNames(String head, ArrayList<String> vars) {
		lside.embedVariableNames(head, vars);
		rside.embedVariableNames(head, vars);
	}
	public void removeEmbeding() {
		lside.removeEmbeding();
		rside.removeEmbeding();
	}
	public boolean isTrueImplication() {
		if (link.equals(Op.equiv) || link.equals(Op.then)) {
			if (lside.equalsString("\\true")) return true;
			if (rside.equalsString("\\true")) return true;
		}
		return false;
	}
	public Term getTrueImplication() {
		if (lside.equalsString("\\true")) return rside;
		if (rside.equalsString("\\true")) return lside;
		return null;
	}
	
	public Term toTerm() {
		Term res = new Term();
		res.addTerm(lside);
		res.addTerm(Op.getOperator(link.link));
		res.addTerm(rside);
		return res;
	}
}
