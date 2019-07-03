package Elements;

import java.util.ArrayList;
import java.util.Iterator;

public class Expression {

	public ArrayList<String> expression;
	
	public Expression () {
		expression = new ArrayList<String>();
	}
	public Expression (ArrayList<String> exp) {
		expression = exp;
	}
	public Expression (String exp) {
		ArrayList<String> t = new ArrayList<String>();
		t.add(exp);
		expression = t;
	}
	
	public String toString() {
		
		if (expression.size() == 0) return "";
		String output = expression.get(0).toString();
		for (int i=1; i<expression.size(); i++) output += " " + expression.get(i);
		
		return output;
	}

	public Expression copy() {
		Expression result = new Expression();
		for (String s: expression) result.expression.add(s);
		return result;
	}
	
	public boolean equals(Expression other) {
		Iterator<String> it1 = this.expression.iterator();
		Iterator<String> it2 = other.expression.iterator();
		while (it1.hasNext()) {
			if (!it2.hasNext()) return false;
			if (!it1.next().equals(it2.next())) return false;
		}
		if (it2.hasNext()) return false;
		return true;		
	}
}
