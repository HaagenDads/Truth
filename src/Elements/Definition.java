package Elements;

import java.util.ArrayList;
import java.util.Arrays;


public class Definition extends Term {

	public Definition () {
		super();
		isdefinition = true;
	}
	
	public void parseTerms(Term result, String str) {
		result.flatten();
		addTerm(result);
		
		ArrayList<Character> parsed = new ArrayList<Character>();
		ArrayList<Character> variable = new ArrayList<Character>();
		Collection col = new Collection();
		int openedbrackets = 0;
		for (char c: str.toCharArray()) {
			if (openedbrackets == 0) parsed.add(c);
			if (c == '[' && ++openedbrackets == 1) {
				variable = new ArrayList<Character>();
			}
			else if (c == ']' && (--openedbrackets == 0)) {
				String[] var = getStringRepresentation(variable).split(" ");
				col.addTerm(Term.compileTerms(new ArrayList<String>(Arrays.asList(var))));
				parsed.add(c);
			} 
			else if (openedbrackets != 0) variable.add(c);
		}
		
		addTerm(new Term(getStringRepresentation(parsed)));
		addTerm(col);
	}
	
	public boolean equals (Term other) {
		if (!other.isDefinition()) return false;
		
		for (int i=0; i<3; i++) {
			if (!get(i).equals(other.get(i))) return false;
		}
		return true;
	}
	
	public String toString() {
		String output = "";
		output += get(0).toString() + " ";
		
		Collection col = (Collection) get(2);
		int varindex = 0;
		ArrayList<Character> res = new ArrayList<Character>();
		for (char c: get(1).s.trim().toCharArray()) {
			if (c == '[') {
				Term t = col.get(varindex++);
				for (char cc: adjustParenthesis(t).toCharArray()) res.add(cc);
			} else if (c != ']') {
				res.add(c);
			}
		}
		
		output += Term.getStringRepresentation(res);
		return output;
	}
	
	static public Permutations permute (Definition def) {
		
		Permutations perm = new Permutations();
		ArrayList<Definition> defs = new ArrayList<Definition>();

		for (Term perma: permute(def.get(0)).vs) {
		for (Term permc: Collection.permute(def.get(2)).vs) {
			Definition newdef = new Definition();
			newdef.addTerm(perma);
			newdef.addTerm(def.get(1));
			newdef.addTerm(permc);
			defs.add(newdef);
		}
		}
		
		for (Term t: defs) perm.add(t);
		return perm;
	}

}
