package Elements;

import java.util.ArrayList;

public class BlockStamp {
	
	public ArrayList<Integer> idxs;
	public BlockStamp () {
		idxs = new ArrayList<Integer>();
		idxs.add(0);
	}
	
	public void increment() {
		int lastid = idxs.size() -1;
		idxs.set(lastid, idxs.get(lastid) + 1);
	}
	
	public void enterlevel() {
		idxs.add(0);
	}
	
	public void leavelevel() {
		ArrayList<Integer> res = new ArrayList<Integer>();
		for (int i=0; i<idxs.size()-1; i++) {
			res.add(idxs.get(i));
		}
		this.idxs = res;
	}
	
	public String getStamp() {
		String output = "" + idxs.get(0);
		for (int i=1; i<idxs.size(); i++) {
			output += "." + idxs.get(i);
		}
		return output;
	}
	
	
	
}
