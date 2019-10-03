package Core;

import java.util.ArrayList;

import Elements.BlockStamp;
import Elements.Justification;
import Elements.Link;
import Elements.Statement;
import Elements.Term;
import Elements.Variable;

public class Logging {

	private Logging activeLog, parent;
	public ArrayList<Logging> v;
	public enum type {cases, statement, ground};
	public type t;
	public Statement caseStatement;
	public ArrayList<Arg> args;
	public boolean solved;
	
	public BlockStamp blockstamp;
	public String blocID;
	public Theorem thm;
	
	
	public Logging(Theorem thm) {
		v = new ArrayList<Logging>();
		activeLog = this;
		this.t = type.ground;
		blockstamp = new BlockStamp();
		this.thm = thm;
	}
	private Logging(type t, Logging parent, BlockStamp blockstamp) {
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
	
	public void createCase(Statement s) {
		getActivelogToGround();
		blockstamp.increment();
		Logging nv = new Logging(type.cases, activeLog, blockstamp);
		nv.blocID = blockstamp.getStamp();
		nv.caseStatement = s;
		
		blockstamp.enterlevel();
		activeLog.v.add(nv);
		activeLog = nv;
	}
	
	public void closeCase() {
		while (!activeLog.t.equals(type.cases)) activeLog = activeLog.parent;
		
		blockstamp.leavelevel();
		activeLog = activeLog.parent;
		/*
		for (Assump a: caseConclusions) {
			activeLog.addLine(null, a.st.lside, null);
			activeLog.addLine(new Link("\\eq"), a.st.rside, new Justification(a));
		}*/
	}
	
	public void addLine(Link link, Term term, Justification expl) {
		
		if (link == null) {
			getActivelogToGround();
			blockstamp.increment();
			
			Logging st = new Logging(type.statement, activeLog, blockstamp);
			st.blocID = blockstamp.getStamp();
			activeLog.v.add(st);
			activeLog = st;
		}
		
		activeLog.args.add(new Arg(term, link, expl));
	}
	
	/* 'let' statements */
	public void addLine(ArrayList<Variable> sts) {
		getActivelogToGround();
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
	
	private void rawPrint() {
		System.out.println(t);
		if (t.equals(type.cases)) {System.out.println(caseStatement.toString()); }
		if (t.equals(type.statement)) {
			for (Arg x: args) {
				if (x.expl!=null) System.out.println(x.toString());
			}
		}
		for (Logging x: v) { x.rawPrint(); }
	}
	
	private void getActivelogToGround () {
		while (activeLog.t.equals(type.statement)) activeLog = activeLog.parent;
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
