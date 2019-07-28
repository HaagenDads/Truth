package Graphics;

import Elements.*;
import Operation.Op;

@SuppressWarnings("unused")
public class Main {
	
	
	public static void main(String[] args) {
		TextZone frame = new TextZone();
        frame.setLocation(250, 50);
        frame.setSize(900, 650); 
        frame.setVisible(true);
        //frame.loadTheorem("Logic/FirstOrder/OrImplication.txt");
        //frame.loadTheorem("AndImpliesOrTautology.txt");
        //frame.loadTheorem("TrivialAnd.txt");
        //frame.loadTheorem("Logic/FirstOrder/PartitionImplication.txt");
        //frame.loadTheorem("QuantifierTesting.txt");
        //frame.loadTheorem("EmbededQuantifier.txt");
        //frame.loadTheorem("Logic/Set/AxiomPowerset.txt");
        frame.loadTheorem("unsolvedQuant.txt");
	}
	
	/*
	public static void main(String[] args) {
		
		Term a = new Term("aa");
		Term b = Op.getOperator("+");
		Term c = new Term();
		c.addTerm(new Term("3"));
		c.addTerm(Op.getOperator("+"));
		c.addTerm(new Term("a"));
		Term d = new Term();
		d.addTerm(new Term("8"));
		d.addTerm(Op.getOperator("*"));
		d.addTerm(new Term("b"));
		Term all = new Term();
		Collection col = new Collection();
		
		col.addTerm(c);
		col.addTerm(d);
		all.addTerm(a);
		all.addTerm(b);
		all.addTerm(col);

		
		for (Term t: Term.permute(col).vs) {
			System.out.println(t);
		}
		System.out.println(all.toString());
		
	}*/
	
}
