package Core;

public class Utils {

    private final static int printPriority = 1; // 3 = [FATAL] only; 1 = broad;
    protected static final String debugthm = "AxiomSoustraction ";

    static protected void printout(String text) {
        printout(1, text);
    }
    static protected void printout(int priority, String text) {
        if (printPriority > priority) return;
        if (priority == 3) System.out.println("[FATAL]" + text);
        else System.out.println(text);
    }

    protected void _debug(Theorem thm, String str) {
        if (printPriority==1 && thm.name.equals(Utils.debugthm)) System.out.println(str);
    }
}
