package freenet.simulator.mast;

import java.util.ArrayList;
import java.util.List;

public class LoggingInsert extends Insert {

	public LoggingInsert(double t, short htl) {
		super(t, htl);
		nodesVisited = new ArrayList<Node>(htl);
	}
	
	private final List<Node> nodesVisited;

	@Override
	public boolean decrementHTL(Node n) {
		nodesVisited.add(n);
		return super.decrementHTL(n);
	}
	
	public void dump() {
		System.out.println("Visited nodes: ");
		for(Node n : nodesVisited) {
			System.out.println("\t" + n.location);
		}
	}

	public Node getPrevNode() {
		if(nodesVisited.isEmpty()) return null;
		return nodesVisited.get(nodesVisited.size()-1);
	}

}
