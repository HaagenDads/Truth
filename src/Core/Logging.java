package Core;

import java.util.ArrayList;

import Elements.Assump;
import Elements.BlockStamp;
import Elements.Justification;
import Elements.Link;
import Elements.Statement;
import Elements.Term;
import Elements.Variable;

public class Logging {

	Logging activeLog, parent;
	public ArrayList<Logging> v;
	public enum type {cases, specificCase, statement, ground};
	public type t;
	public Statement caseStatement;
	public ArrayList<Arg> args;
	public boolean solved;
	
	public BlockStamp blockstamp;
	public String blocID;
	
	
	public Logging() {
		v = new ArrayList<Logging>();
		activeLog = this;
		this.t = type.ground;
		blockstamp = new BlockStamp();
	}
	public Logging(type t, Logging parent, BlockStamp blockstamp) {
		v = new ArrayList<Logging>();
		activeLog = this;
		this.t = t;
		this.parent = parent;
		if (t.equals(type.statement) || t.equals(type.cases)) args = new ArrayList<Arg>();
		this.blockstamp = blockstamp;
	}
	
	
	public boolean isEmpty() {
		return v.size() == 0 && t == type.ground;
	}
	
	public void createCase() {
		blockstamp.increment();
		Logging nv = new Logging(type.cases, activeLog, blockstamp);
		nv.blocID = blockstamp.getStamp();
		blockstamp.enterlevel();
		activeLog.v.add(nv);
		activeLog = nv;
	}
	
	public void closeCase() {
		while (!activeLog.t.equals(type.cases)) {
			activeLog = activeLog.parent;
		}
		
		blockstamp.leavelevel();
		activeLog = activeLog.parent;
		/*
		for (Assump a: caseConclusions) {
			activeLog.addLine(null, a.st.lside, null);
			activeLog.addLine(new Link("\\eq"), a.st.rside, new Justification(a));
		}*/
	}
	
	public void addCase(Statement s) {
		while (!activeLog.t.equals(type.cases)) {
			activeLog = activeLog.parent;
		}
		Logging nv = new Logging(type.specificCase, activeLog, blockstamp);
		nv.caseStatement = s;
		activeLog.v.add(nv);
		activeLog = nv;
	}
	
	public void addLine(Link link, Term term, Justification expl) {
		
		if (activeLog.t.equals(type.cases)) {
			System.out.println("[LOGS] Found line in cases : " + term + "  " + expl);
			return;
		}
		
		if (link == null) {
			if (activeLog.t.equals(type.statement)) { 
				activeLog = activeLog.parent;
			}
			
			blockstamp.increment();
			Logging st = new Logging(type.statement, activeLog, blockstamp);
			st.blocID = blockstamp.getStamp();
			activeLog.v.add(st);
			activeLog = st;
		}
		
		activeLog.args.add(new Arg(term, link, expl));
	}
	
	public void addLine(ArrayList<Variable> sts) {
		blockstamp.increment();
		Logging st = new Logging(type.statement, activeLog, blockstamp);
		st.blocID = blockstamp.getStamp();
		activeLog.v.add(st);
		activeLog = st;
	
		activeLog.args.add(new Arg(sts));
		
	}
	
	public void conclude(boolean state) {
		solved = state;
	}
	
	public void rawPrint() {
		System.out.println(t);
		if (t.equals(type.specificCase)) {System.out.println(caseStatement.toString()); }
		if (t.equals(type.statement)) {
			for (Arg x: args) {
				if (x.expl!=null) System.out.println(x.toString());
			}
		}
		for (Logging x: v) { x.rawPrint(); }
	}
	
	public class Arg {
		
		public Term term;
		public Link link;
		public Justification expl;
		public ArrayList<Variable> vars;
		
		public Arg(Term term, Link link, Justification expl) {
			this.term = term;
			this.link = link;
			this.expl = expl;
		}
		
		public Arg(ArrayList<Variable> sts) {
			this.vars = sts;
		}

		public String toString() {
			return link.toString() + term.toString() + " (" + expl.toString() + ")";
		}
		
	}
}
