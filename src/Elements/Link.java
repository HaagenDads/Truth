package Elements;

import java.util.ArrayList;

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
		return theoremlink.equals(Op.eq) && propositionlink.equals(Op.equiv);
	}
	
	
	static public Link reduceSerie (ArrayList<Link> arrayList) {
		Operator reduction = null;
		for (Link b: arrayList) {
			Operator op = Op.getOperator(b.link);
			reduction = reduceLinks(reduction, op);
			if (reduction == null) return null;
		}
		if (reduction == null) return null;
		return new Link(reduction);
	}
	
	private static Operator reduceLinks(Operator a, Operator b) {
		if (a == null) return b;
		if (a == b) return a;
		if (a == Op.lt) {
			if (b == Op.eq || b == Op.le) return a;
		} else if (a == Op.le) {
			if (b == Op.eq) return a;
		} else if (a == Op.gt) {
			if (b == Op.eq || b == Op.ge) return a;
		} else if (a == Op.ge) {
			if (b == Op.eq) return a;
		} else if (a == Op.equiv) {
			if (b == Op.then) return b;
		} else if (a == Op.then) {
			if (b == Op.equiv) return a;
		}
		System.out.println("NO LINK CONTINUITY BETWEEN '" + a + "' AND '" + b + "'");		
		return null;
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
	public boolean equals (String str) {
		return this.link.equals(str);
	}
	
	public boolean isError() {
		return this.link.startsWith("error");
	}
	
	static public boolean isCommutative(Link o) {
		if (isLink(o.link))	return o.op.isCommutative();
		return false;
	}
	
	public boolean hasSymmInverse() {
		if (isLink(link)) return op.hasSymmInverse();
		return false;
	}
	public Link getSymmInverse() {
		if (isLink(link)) return new Link(op.reverse());
		return null;
	}

}
