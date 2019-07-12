package Elements;

import java.util.LinkedList;

public class Link {

	String link;
	
	public Link () {
		this.link = "";
	}
	public Link (String link) {
		this.link = link;
	}
	
	static public boolean isLink (String string) {
		if (string.equals("=")) return true;
		if (string.equals("!=")) return true;
		if (string.equals("\\eq")) return true;
		if (string.equals("\\then")) return true;
		return false;
	}
	
	static public boolean isSufficient (Link theoremlink, Link propositionlink) {
		if (theoremlink.equals(propositionlink)) return true;
		if (theoremlink.equals("=") && propositionlink.equals("\\eq")) return true;
		return false;
	}
	
	
	static public Link reduceSerie (LinkedList<Link> linkserie) {
		Link reduction = new Link();
		for (Link b: linkserie) {
			reduction = reduceLinks(reduction, b);
			if (reduction.equals("")) return new Link();
		}
		return reduction;
	}
	
	static private Link reduceLinks (Link a, Link b) {
		if (a.equals("")) return b;
		if (a.equals(b)) return a;
		if (a.equals("<")) {
			if (b.equals("=") || b.equals("<=")) return a;
		} else if (a.equals("<=")) {
			if (b.equals("=") || b.equals("<=")) return a;
		} else if (a.equals(">")) {
			if (b.equals("=") || b.equals(">=")) return a;
		} else if (a.equals(">=")) {
			if (b.equals("=") || b.equals(">=")) return a;
		} else if (a.equals("\\eq")) {
			if (b.equals("\\then")) return b;
		} else if (a.equals("\\then")) {
			if (b.equals("\\eq")) return a;
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
	public boolean equals (String str) {
		return this.link.equals(str);
	}
	
	public boolean isError() {
		return this.link.equals("error");
	}
	

}
