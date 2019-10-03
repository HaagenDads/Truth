package Core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;

import Core.Demonstration.ExceptionCaseNonvalid;
import Elements.*;
import Elements.ArrayString.Sequence;
import Elements.Term.TermSynthaxException;
import Graphics.Application;


public class Compiler extends Utils {

	private final static int printPriority = 3;
	
	public ArrayList<Theorem> Theorems;
	private Application source;
	
	public Compiler(Application source) {
		Theorems = new ArrayList<>();
		this.source = source;
	}
	
	public void refreshUnit() {
		Theorems = new ArrayList<>();
	}
	
	
	static public String getUnitFromFile(File file) {
		StringBuilder unit = new StringBuilder();
		try {
			Scanner sc = new Scanner(file); 
		    while (sc.hasNextLine()){ unit.append(sc.nextLine()).append("\n"); }
		    sc.close();
		} catch (IOException ignored){}
		
		return unit.toString();
	}
	
	public Theorem readUnitFromEditor() throws CompilerException, TermSynthaxException {
		String unit = source.topTextarea.getText();
		return readUnit(unit);
	}
	
	static public String getDemonstrationFromUnit (String unit) throws CouldntFindDemonstrationException {
		String body = unit.substring(unit.indexOf("}") + 1).trim();
		if (doesDemonstrationExist(body)) {
			return body.substring(body.indexOf("{")+1, body.lastIndexOf("}")).trim();
		} else {
			throw new CouldntFindDemonstrationException();
		}
	}
	
	private Theorem readUnit (String unit) throws CompilerException, TermSynthaxException {

		assertParenthesis(unit);
		assertTheoremExists(unit);
		
		Theorem thm = new Theorem();
		thm.nlog = new Logging(thm);
		
		char[] chars = unit.toCharArray();
		int pos_identification = unit.indexOf("{");
		int pos_header = getHeaderPos(chars, pos_identification+1);
		
		String identification = unit.substring(8, pos_identification).trim();
		String header = unit.substring(pos_identification+1, pos_header).trim();
		String body = unit.substring(pos_header+1).trim();
		
		findFields(identification, thm);
		readHeader(header, thm);
		
		// Finding the demonstration
		if (doesDemonstrationExist(body)) {
			thm.loadPackages();
			body = body.substring(body.indexOf("{")+1, body.lastIndexOf("}")).trim();
			readBody(body, thm);
		}
			
		Theorems.add(thm);
		return thm;
	}


	private int getHeaderPos (char[] chars, int pos) {
		int openedbracket = 1;
		for (; pos<chars.length; pos++) {
			if (chars[pos] == '{') openedbracket++;
			else if (chars[pos] == '}') {
				if (--openedbracket == 0) return pos;
			}
		}
		return pos;
	}
	
	
	private void readHeader(String header, Theorem thm) throws TermSynthaxException {
		
		//System.out.println(":thm name:  " + thm.name);
		Body head = new Body(header);
		for (Sequence seq: head.seqarray) {
			
			String headToken = seq.getHeadtoken();
			if (seq.isAssignment()) {
				thm.variables.addAll(Term.parseLetStatement(seq.getV(0), thm, true));

			} else if (headToken.equals("\\where")) {
				Statement st = parseStatementFromSequence(seq);
				thm.assumptions.acceptAssumptionFromHypothesis(st);
				
			} else if (headToken.equals("\\state")) {
				if (seq.size() != 2) System.out.println("[FATAL] Couldn't find a TOT disp in declaration for theorem " + thm.name);
				thm.statement = parseStatementFromSequence(seq);
			}
		}
		
	}
	
	private Statement parseStatementFromSequence(Sequence seq) throws TermSynthaxException {
		ArrayString lside = seq.getV(0);
		lside.remove(0);
		Term t1 = Term.compileTerms(lside);
		Term t2 = Term.compileTerms(seq.getV(1));
		return new Statement(seq.getL(0), t1, t2);
	}

	private void readBody(String body, Theorem thm) {
		try {
			thm.compileDemonstration(body);
		} 
		catch (GenException e) { e.explain(); } 
		catch (ExceptionCaseNonvalid e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void findFields(String str, Theorem thm) throws CompilerException {
		
		int name_pos = str.toLowerCase().indexOf("name:");
		int package_pos = str.toLowerCase().indexOf("package:");
		int tags_pos = str.toLowerCase().indexOf("tags:");
		int head_end = str.length();

		if (name_pos == -1 ) { throw new CouldntFindTheoremNameException(); }

		if (tags_pos > -1) { thm.tags = str.substring(tags_pos + 5, head_end).trim().split(", "); head_end = tags_pos; }
		if (package_pos > -1)  {thm.packages = str.substring(package_pos + 8, head_end).trim().split(", "); head_end = package_pos; }
		thm.name = str.substring(name_pos + 5, head_end).trim();
	}
	
	public void acceptTheorems(String packageName) throws CompilerException, TermSynthaxException {
		File[] files = new File("theorems/" + packageName + "/Axioms/").listFiles();
		File[] files2 = new File("theorems/" + packageName + "/FirstOrder/").listFiles();
		File[] files3 = new File("theorems/" + packageName + "/Natural/").listFiles();
		File[] files4 = new File("theorems/" + packageName + "/Set/").listFiles();
		for (File[] folder: new File[][]{files, files2, files3, files4}) {
			for (File f: folder) {
				String unit = getUnitFromFile(f);
				
				assertTheoremExists(unit);
				Theorem thm = new Theorem();
				thm.filepath = f.getPath();
				String identification = unit.substring(8, unit.indexOf("{")).trim();
				String header = unit.substring(unit.indexOf("{") + 1, unit.indexOf("}")).trim();
				findFields(identification, thm);
				readHeader(header, thm);
				Theorems.add(thm);
				printout("Accepted theorem " + thm.name);

			}
		}
	}

	
	private void assertTheoremExists(String body) throws CouldntFindTheoremException {
		if (!body.substring(0, 8).equalsIgnoreCase("theorem ")) throw new CouldntFindTheoremException();
	}

	private void assertParenthesis(String body) throws ErrorInParenthesisException {
		char[] chars = body.toCharArray();
		Stack<Character> stk = new Stack<>();
		for (int i=0; i<chars.length; i++) {
			char c = chars[i];
			if (c == '(' || c == '{' ||  c == '[') {
				stk.push(c);
			} else if (c == ')' && stk.pop() != '(') throw new ErrorInParenthesisException(i);
			else if (c == ']' && stk.pop() != '[') throw new ErrorInParenthesisException(i);
			else if (c == '}' && stk.pop() != '{') throw new ErrorInParenthesisException(i);
		}
	}

	static private boolean doesDemonstrationExist(String body) {
		if (body.length() < 14) return false;
		return body.substring(0, 13).trim().equalsIgnoreCase("demonstration");
	}

	abstract static public class CompilerException extends GenException { public String errorType() { return "Compiler";}}
	static class CouldntFindTheoremException extends CompilerException { public String errorMessage() { return "Couldn't find theorem."; }}
	static class CouldntFindTheoremNameException extends CompilerException { public String errorMessage() { return "Couldn't find theorem name."; }}
	static class CouldntFindDemonstrationException extends CompilerException { public String errorMessage() { return "Couldn't find demonstration."; }}
	static class ErrorInParenthesisException extends CompilerException {
		int i;
		ErrorInParenthesisException(int i) {this.i = i; }
		public String errorMessage() { return "Error in parenthesis at position " + i + "."; }
	}
	
}
