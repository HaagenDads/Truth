package Elements;


public class Statement {
	
	public String link;
	public Term lside;
	public Term rside;
	
	
	public Statement(String link, Term leftHand, Term rightHand) {
		this.link = link;
		lside = leftHand;
		rside = rightHand;
	}
	public Statement(String link, String leftHand, String rightHand) {
		this.link = link;
		lside = new Term(leftHand);
		rside = new Term(rightHand);
	}
	
	
	public String toString() {
		return lside + " " + link + " " + rside;
	}
	
	public Statement copy() {
		return new Statement(link, lside.copy(), rside.copy());
	}
	
	public boolean equals(Statement other) {
		if (!this.link.equals(other.link)) return false;

		if (lside.equals(other.lside) && rside.equals(other.rside)) return true;
		if (lside.equals(other.rside) && rside.equals(other.lside)) return true;
		return false;
		
	}
	
	public Statement switchSides() {
		Term temp = lside;
		lside = rside;
		rside = temp;
		return this;
	}
	
	public void embedVariableNames(String head) {
		lside.embedVariableNames(head);
		rside.embedVariableNames(head);
	}
	public void removeEmbeding() {
		lside.removeEmbeding();
		rside.removeEmbeding();
	}
}
