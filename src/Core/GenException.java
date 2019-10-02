package Core;

abstract public class GenException extends Exception {
    public void explain() { System.out.println("[" + errorType() + " exception] " + errorMessage()); printStackTrace();}
    public void explain_nostack() { System.out.println("[" + errorType() + " exception] " + errorMessage()); }
    public abstract String errorType();
    public abstract String errorMessage();
}
