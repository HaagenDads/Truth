package Elements;

import java.util.ArrayList;

public abstract class Set {
	
	public static final String genericType = "\\set";
	ArrayList<Set> subsets;
	ArrayList<Set> cartesian;
	int multiplicity;
	
	public Set () {
		subsets = new ArrayList<Set>();
		cartesian = new ArrayList<Set>();
		multiplicity = 1;
		
	}
	
	public boolean isShallow () {
		return cartesian.size() == 0;
	}
	public boolean isSingleton () {
		return subsets.size() == 0;
	}
	
	public boolean equals (Set other) {
		
	}
	
	abstract public Set join (Set a, Set b);
	abstract public boolean isComplete ();
	
	public class Singleton {
		Link link;
		public Singleton (Variable v, Link link, Term )
	}
	
	
}
