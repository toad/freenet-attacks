package freenet.simulator.mast;

import java.util.Arrays;
import java.util.Random;

/** Simple recursive simulator. We assume one request at a time across the whole network!
 * For our purposes this is sufficient - we don't care about concurrency issues. */
public class Node {
	
	/** Cannot be changed. We set up an ideal Kleinberg network and then route on it. */
	public final double location;
	
	/** We only need to keep the last request ID. This is sufficient to check for biting
	 * our own tail. */
	private long lastRequestID;

	/** Peers */
	private Node[] peers;
	private boolean[] peersVisited;
	
	public Node(double d) {
		location = d;
		lastRequestID = -1;
		peers = new Node[0];
		peersVisited = new boolean[0];
	}
	
	static final Random r = new Random();
	
	/** @param i The insert (HTL will be decremented etc).
	 * @return False if the parent should try other nodes, true if the insert completed. */
	public boolean route(Insert ins) {
		if(!ins.decrementHTL(this)) return true;
		if(ins.id == lastRequestID) return false; // Loop.
		lastRequestID = ins.id;
		for(int i=0;i<peers.length;i++) peersVisited[i] = false;
		while(true) {
			Node best = null;
			double bestDistance = 1.0;
			int iBest = -1;
			for(int i=0;i<peers.length;i++) {
				if(peersVisited[i]) continue;
				Node n = peers[i];
				double distance;
				if(ins.inRandomRoute())
					distance = r.nextDouble();
				else
					distance = Location.distance(n.location, ins.target);
				if(distance < bestDistance) {
					best = n;
					bestDistance = distance;
					iBest = i;
				}
			}
			if(best == null) return false;
			if(best.route(ins)) return true;
			peersVisited[iBest] = true;
		}
	}

	public boolean hasPeer(Node p) {
		if(p == this) return true;
		for(Node n : peers) {
			if(p == n) return true;
		}
		return false;
	}

	public void addPeer(Node p) {
		peers = Arrays.copyOf(peers, peers.length+1);
		peers[peers.length-1] = p;
		peersVisited = Arrays.copyOf(peersVisited, peers.length);
	}
	
}
