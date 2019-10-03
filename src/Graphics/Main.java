package Graphics;

import java.util.ArrayList;

public class Main {

	public static void main(String[] args) {
        Application frame = new Application();
        frame.setLocation(250, 50);
        frame.setSize(900, 650); 
        frame.setVisible(true);
        //frame.loadTheorem("Logic/FirstOrder/OrImplication.txt");
        //frame.loadTheorem("AndImpliesOrTautology.txt");
        //frame.loadTheorem("Logic/FirstOrder/PartitionImplication.txt");
        frame.loadTheorem("QuantifierTesting.txt");
        //frame.loadTheorem("EmbededQuantifier.txt");
        //frame.loadTheorem("Logic/Set/AxiomPowerset.txt");
        //frame.loadTheorem("unsolvedQuant.txt");
        //frame.loadTheorem("Functions/increasingfnc.txt");
        //frame.loadTheorem("Functions/ExponentUnit.txt");
        //frame.loadTheorem("Logic/Natural/AxiomAdditionIneq2.txt");

        // TODO am i sure that (((a+b) + c) + d) + e    has every associative power? I am not.
	}

	/*
	public static void main(String[] args) {
	    A a = new A(11);
	    A b = new A(22);
        ArrayList<A> alist = new ArrayList<A>();
        alist.add(a); alist.add(b);
        alist.get(0).setv(3);
        alist.get(1).setv(5);
        System.out.println(a.v + " " + b.v + " : " + alist.get(0).v + " " +alist.get(1).v);

    }


	static class A {
	    int v;
	    int id;
	    public A(int id) {
	        this.id = id;
	        this.v = 0;
        }
	    public void setv(int v) {
	        System.out.println("Setting v to " + v + " in " + id);
	        this.v = v;
        }
    }*/
}
