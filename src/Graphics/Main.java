package Graphics;

public class Main {
	
	public static void main(String[] args) {
        Application frame = new Application();
        frame.setLocation(250, 50);
        frame.setSize(900, 650); 
        frame.setVisible(true);
        //frame.loadTheorem("Logic/FirstOrder/OrImplication.txt");
        //frame.loadTheorem("AndImpliesOrTautology.txt");
        //frame.loadTheorem("Logic/FirstOrder/PartitionImplication.txt");
        //frame.loadTheorem("QuantifierTesting.txt");
        //frame.loadTheorem("EmbededQuantifier.txt");
        //frame.loadTheorem("Logic/Set/AxiomPowerset.txt");
        //frame.loadTheorem("unsolvedQuant.txt");
        //frame.loadTheorem("Functions/increasingfnc.txt");
        //frame.loadTheorem("Functions/ExponentUnit.txt");
        frame.loadTheorem("Logic/Natural/AxiomAdditionIneq2.txt");

        // TODO save types in term (easy)
        // TODO for every possible association !!!
	}
	
}
