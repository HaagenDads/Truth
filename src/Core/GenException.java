package Core;

abstract public class GenException extends Exception {
    public void explain() { System.out.println("[" + errorType() + " exception] " + errorMessage());}
    abstract String errorType();
    public abstract String errorMessage();
}
