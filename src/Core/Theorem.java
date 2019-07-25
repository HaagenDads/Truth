package Core;

import java.io.File;
import java.util.ArrayList;

import Core.Compiler.CompilerException;
import Core.Compiler.CouldntFindDemonstrationException;
import Elements.*;

public class Theorem {

	public String[] packages, tags;
	public String name;
	public Assumptions assumptions;
	public ArrayList<Theorem> loadedTheorems;
	public ArrayList<Variable> variables;
	public Statement statement;
	public Logging nlog;
	private boolean isEmbeded;
	
	public Demonstration demonstration;
	public String filepath;
	
	public Theorem() {
		name = "<no name>";
		packages = new String[]{};
		tags = new String[]{};
		assumptions = new Assumptions();
		loadedTheorems = new ArrayList<Theorem>();
		variables = new ArrayList<Variable>();
		nlog = new Logging();
		isEmbeded = false;
		demonstration = null;
		filepath = "";
	}

	public boolean hasFilePath() {
		return !filepath.equals("");
	}
	public void compileDemonstration () {
		if (!nlog.isEmpty()) return;
		loadPackages();
		String unit = Compiler.getUnitFromFile(new File(filepath));
		try {
			String body = Compiler.getDemonstrationFromUnit(unit);
			compileDemonstration(body);
		} catch (CouldntFindDemonstrationException e) {}
	}
	public void compileDemonstration (String body) {
		this.demonstration = new Demonstration(body, this);
		this.demonstration.solveDemonstration();
	}
	
	public void loadPackages() {
		Compiler nc = new Compiler(null);
		for (String x: packages) {
			try {
				nc.acceptTheorems(x);
			} catch (CompilerException e) {e.printStackTrace();}
			for (Theorem th: nc.Theorems) {
				loadedTheorems.add(th);
				th.embedVariableNames();
			}
		}
		
	}
	
	public void embedVariableNames() {
		if (isEmbeded) return;
		isEmbeded = true;
		
		ArrayList<String> vars = new ArrayList<String>();
		for (Variable v: variables) {
			vars.add(v.name);
			v.embedVariableNames("thm.");
		}
		statement.embedVariableNames("thm.", vars);
		for (Assump a: assumptions) a.st.embedVariableNames("thm.", vars);
	}
	
	public void removeEmbeding() {
		if (!isEmbeded) return;
		isEmbeded = false;
		statement.removeEmbeding();
		for (Assump a: assumptions) a.st.removeEmbeding();
		for (Variable v: variables) v.removeEmbeding();
	}

	public Variable getVariable(String name) {
		for (Variable v: variables) {
			if (v.name.equals(name)) return v;
		}
		return null;
	}
	
	// This function could/should be expanded
	public boolean equals(Theorem other) {
		if (!this.statement.equals(other.statement) || assumptions.size() != other.assumptions.size()) return false;
		for (int i=0; i<assumptions.size(); i++) {
			if (!assumptions.get(i).equals(other.assumptions.get(i))) return false;
		}
		return true;
	}
	
	public Theorem deepcopy() {
		Theorem output = new Theorem();
		output.packages = packages;
		output.tags = tags;
		output.name = name;
		output.assumptions = this.assumptions.copy();
		for (Variable var: variables) output.variables.add(var.copy());
		output.loadedTheorems = loadedTheorems;
		output.statement = statement.copy();
		
		return output;
	}
	
}
