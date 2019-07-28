package Elements;

import java.util.LinkedList;

import Operation.Op;
import Operation.Operator;

public class Link {

	public String link;
	private Operator op;
	
	public Link () {
		this.link = "";
	}
	public Link (String link) {
		this.link = link;
		this.op = Op.getOperator(link);
		
	}
	public Link (Operator op) {
		this.link = op.s;
		this.op = op;
	}
	
	static private boolean isin(Object s, Object[] opSet) {
		if (s==null) return false;
		for (Object op: opSet) {
			if (s.equals(op)) return true;
		}
		return false;
	}
	
	static public boolean isLink (String string) {
		Operator[] opSet = new Operator[]{Op.eq, Op.ineq, Op.lt, Op.gt, Op.le, Op.ge, Op.equiv, Op.then, Op.assign, Op.iff};
		return isin(Op.getOperator(string), opSet);
	}
	
	static public boolean isConditional (String string) {
		Operator[] opSet = new Operator[]{Op.eq, Op.ineq, Op.lt, Op.gt, Op.le, Op.ge};
		return isin(Op.getOperator(string), opSet);
	}	
	
	static public boolean isSufficient (Link theoremlink, Link propositionlink) {
		if (theoremlink.equals(propositionlink)) return true;
		if (theoremlink.equals(Op.eq) && propositionlink.equals(Op.equiv)) return true;
		return false;
	}
	
	
	static public Link reduceSerie (LinkedList<Link> linkserie) {
		Link reduction = null;
		for (Link b: linkserie) {
			reduction = reduceLinks(reduction, b);
			if (reduction == null) return new Link();
		}
		return reduction;
	}
	
	static private Link reduceLinks (Link a, Link b) {
		if (a == null) return b;
		if (a.equals(b)) return a;
		if (a.equals(Op.lt)) {
			if (b.equals(Op.eq) || b.equals(Op.le)) return a;
		} else if (a.equals(Op.le)) {
			if (b.equals(Op.eq) || b.equals(Op.le)) return a;
		} else if (a.equals(Op.gt)) {
			if (b.equals(Op.eq) || b.equals(Op.ge)) return a;
		} else if (a.equals(Op.ge)) {
			if (b.equals(Op.eq) || b.equals(Op.ge)) return a;
		} else if (a.equals(Op.equiv)) {
			if (b.equals(Op.then)) return b;
		} else if (a.equals(Op.then)) {
			if (b.equals(Op.equiv)) return a;
		}
		System.out.println("NO LINK CONTINUITY BETWEEN '" + a + "' AND '" + b + "'");		
		return new Link();
	}
	
	public String toString() {
		return " " + link + " ";
	}
	
	public Link copy() {
		return new Link(link);
	}
	
	public boolean equals (Link other) {
		return this.link.equals(other.link);
	}
	public boolean equals (Operator op) {
		return this.link.equals(op.s);
	}
	
	public boolean isError() {
		return this.link.equals("error");
	}
	
	static public boolean isCommutative(Link o) {
		if (isLink(o.link)) {
			return o.op.isCommutative();
		}
		return false;
	}

}
