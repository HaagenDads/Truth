package Core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import Elements.*;
import Elements.Function.ExceptionSetInvalid;
import Graphics.TextZone;


public class Compiler {

	private final static int printPriority = 3;
	
	public ArrayList<Theorem> Theorems;
	TextZone source;
	
	public Compiler(TextZone source) {
		Theorems = new ArrayList<Theorem>();
		this.source = source;
	}
	
	public void refreshUnit() {
		Theorems = new ArrayList<Theorem>();
	};
	
	
	static public String getUnitFromFile(File file) {
		String unit = "";
		try {
			Scanner sc = new Scanner(file); 
		    while (sc.hasNextLine()){ unit += sc.nextLine() + "\n"; }
		    sc.close();
		} catch (IOException exp){}
		
		return unit;
	}
	
	public Theorem readUnitFromEditor() throws CompilerException {
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
	
	private Theorem readUnit (String unit) throws CompilerException {
		
		assertTheoremExists(unit);
		
		Theorem thm = new Theorem();
		Logging nlog = new Logging();
		thm.nlog = nlog;
		
		String identification = unit.substring(8, unit.indexOf("{")).trim();
		String header = unit.substring(unit.indexOf("{") + 1, unit.indexOf("}")).trim();
		String body = unit.substring(unit.indexOf("}") + 1).trim();
		
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
	
	
	private void readHeader(String header, Theorem thm) {
		
		String[] items = header.split(";");
		for (String statement: items) {
			
			statement = statement.trim();
			String[] elements = statement.split(" ");
			String headToken = elements[0];
			
			if (headToken.equals("\\let")) {
				
				ArrayList<String> varname = new ArrayList<String>();
				int i = 1;
				while (!(elements[i].equals("\\in") || elements[i].equals("\\be"))) {
					varname.add(elements[i++]);
				}
				varname = parseVarnames(varname);
				String connection = elements[i++];

				if (connection.equals("\\in")) {
					String set = elements[i];
					for (String var: varname) thm.variables.add(new Variable(var, set));
				} else if (connection.equals("\\be") && elements[i].equals("\\set")) {
					for (String var: varname) thm.variables.add(new Variable(var, "\\set"));
				} else if (connection.equals("\\be") && elements[i].equals("\\function")) {
					i++;
					if (elements.length > i) {
						ArrayList<String> domain = new ArrayList<String>();
						ArrayList<String> image = new ArrayList<String>();
						boolean todomain = true;
						
						for (; i<elements.length; i++) {
							if (elements[i].equals("->")) todomain = false;
							else {
								if (todomain) domain.add(elements[i]);
								else image.add(elements[i]);
							}
						}
						
						try {
							for (String var: varname) {
								Function func = new Function(var);
								func.setDomain(Term.compileTerms(domain), thm);
								func.setImage(Term.compileTerms(image), thm);
								thm.variables.add(func);
							}
						} catch (ExceptionSetInvalid e) {
							printout(3, e.getError());
						}
					} else {
						for (String var: varname) {
							Function func = new Function(var);
							thm.variables.add(func);
						}
					}
					
				} else { 
					printout(3, "Could not comprehend variable initialization from: " + statement);
				}
				
			} else if (headToken.equals("\\where")) {
				Statement st = parseStatementFromLine(elements);
				thm.assumptions.acceptAssumptionFromHypothesis(st);
				
			} else if (headToken.equals("\\state")) {
				thm.statement = parseStatementFromLine(elements);
			}
		}
		
	}
	
	private ArrayList<String> parseVarnames (ArrayList<String> varname) {
		ArrayList<String> output = new ArrayList<String>();
		for (String var: varname) {
			for (String v: var.split(",")) if (!v.equals("")) output.add(v);
		}
		
		return output;
	}
	
	/* Specifically disregard the first element of the String[], as it corresponds to the head token. */
	private Statement parseStatementFromLine (String[] tokens) {
		ArrayList<String> als = new ArrayList<String>();
		for (String s: tokens) als.add(s);
		als.remove(0);
		Term terms = Term.compileTerms(als);
		//System.out.println(terms.toString());
		return new Statement(new Link(terms.get(1).s), terms.get(0), terms.get(2));
	}
	
	
	private void readBody(String body, Theorem thm) {
		thm.compileDemonstration(body);
	}
	
	
	private void findFields(String str, Theorem thm) throws CompilerException {
		
		int name_pos = str.toLowerCase().indexOf("name:");
		int package_pos = str.toLowerCase().indexOf("package:");
		int tags_pos = str.toLowerCase().indexOf("tags:");
		int head_end = str.length();
		
		if (name_pos == -1 ) { throw new CouldntFindTheoremNameException(); }
		if (package_pos == -1 && tags_pos == -1) {
			thm.name = str.substring(name_pos + 5, head_end).trim();
		} else if (package_pos == -1) {
			thm.name = str.substring(name_pos + 5, tags_pos).trim();
			thm.tags = str.substring(tags_pos + 5, head_end).trim().split(", ");
		} else if (tags_pos == -1) {
			thm.name = str.substring(name_pos + 5, package_pos).trim();
			thm.packages = str.substring(package_pos + 8, head_end).trim().split(", ");
		} else {
			thm.name = str.substring(name_pos + 5, package_pos).trim();
			thm.packages = str.substring(package_pos + 8, tags_pos).trim().split(", ");
			thm.tags = str.substring(tags_pos + 5, head_end).trim().split(", ");
		}
		
	}
	
	
	public void acceptTheorems(String packageName) throws CompilerException {
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
	
	
	private void printout(String text) {
		printout(1, text);
	}
	
	private void printout(int priority, String text) {
		if (printPriority > priority) return;
		if (priority == 3) System.out.println("[FATAL]" + text);
		else System.out.println(text);
	}
	
	private void assertTheoremExists(String body) throws CouldntFindTheoremException {
		if (!body.substring(0, 8).equalsIgnoreCase("theorem ")) throw new CouldntFindTheoremException();
	}
	static private boolean doesDemonstrationExist(String body) {
		if (body.length() < 14) return false;
		return body.substring(0, 13).trim().equalsIgnoreCase("demonstration");
	}
		
	static public class CompilerException extends Exception {};
	static class CouldntFindTheoremException extends CompilerException {};
	static class CouldntFindTheoremNameException extends CompilerException {}
	static class CouldntFindDemonstrationException extends CompilerException {}
	
}
