package Graphics;


public class Main {

	public static void main(String[] args) {
		TextZone frame = new TextZone();
        frame.setLocation(250, 50);
        frame.setSize(900, 650); 
        frame.setVisible(true);
        frame.loadTheorem("Logic/OrImplication.txt");
        //frame.loadTheorem("DeMorgan.txt");
        //frame.loadTheorem("TrivialAnd.txt");
	}
	
}
