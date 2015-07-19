package freenet.simulator.mast;

public class BaseRequest {
	
	/** Destination */
	final double target;

	/** Number of hops remaining */
	private short htl;
	public short origHTL;

	/** Request ID */
	public final long id;
	private static long counter = 0;
	
	/** Received by a node. Decrement the HTL (even in the case of a loop).
	 * @return True unless the request should terminate */
	public boolean decrementHTL(Node n) {
		if(htl == 0) return false;
		htl--;
		return true;
	}
	
	public boolean inRandomRoute() {
//		int threshold = origHTL - 3;
//		if(threshold < 3) return false;
//		return (htl >= threshold);
		return false;
	}

	public BaseRequest(double t, short htl) {
		target = t;
		id = generateID();
		this.htl = htl;
		origHTL = htl;
	}
	
	private static synchronized long generateID() {
		return counter++;
	}
	
	public Integer getHTL() {
		return (int) htl;
	}

}
