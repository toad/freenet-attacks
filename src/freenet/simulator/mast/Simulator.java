package freenet.simulator.mast;

import java.util.Random;

public class Simulator {
	
	protected final Node[] nodes;
	
	public Simulator(int size, int degree, Random r) {
		nodes = new Node[size];
	}
	
	public void initNetwork(int size, int degree, Random r) {
		makeKleinbergNetwork(nodes, true, degree, true, r);
	}
	
	public static void main(String[] args) {
		Random r = new Random(1234);
		Simulator s = new Simulator(1000, 20, r);
		s.initNetwork(1000, 20, r);
		double target = r.nextDouble();
		for(int i=1;i<10;i++) {
			LoggingInsert probe = new LoggingInsert(target, (short) 10);
			Node n = s.nodes[r.nextInt(s.nodes.length)];
			System.out.println("Probe from "+n.location+" to "+target);
			if(!n.route(probe)) System.err.println("Failed to route!");
			probe.dump();
		}
	}
	
	/*
	 * From freenet.node.simulator.RealNodeTest.
	 Borrowed from mrogers simulation code (February 6, 2008)
	 --
	 FIXME: May not generate good networks. Presumably this is because the arrays are always scanned
	        [0..n], some nodes tend to have *much* higher connections than the degree (the first few),
	        starving the latter ones.
	 */
	void makeKleinbergNetwork (Node[] nodes, boolean idealLocations, int degree, boolean forceNeighbourConnections, Random random)
	{
		if(idealLocations) {
			// First set the locations up so we don't spend a long time swapping just to stabilise each network.
			double div = 1.0 / nodes.length;
			double loc = 0.0;
			for (int i=0; i<nodes.length; i++) {
				nodes[i] = createNode(i, loc);
				loc += div;
			}
		}
		if(forceNeighbourConnections) {
			for(int i=0;i<nodes.length;i++) {
				int next = (i+1) % nodes.length;
				connect(nodes[i], nodes[next]);
			}
		}
		for (int i=0; i<nodes.length; i++) {
			Node a = nodes[i];
			// Normalise the probabilities
			double norm = 0.0;
			for (int j=0; j<nodes.length; j++) {
				Node b = nodes[j];
				if (a.location == b.location) continue;
				norm += 1.0 / Location.distance (a.location, b.location);
			}
			// Create degree/2 outgoing connections
			for (int k=0; k<nodes.length; k++) {
				Node b = nodes[k];
				if (a.location == b.location) continue;
				double p = 1.0 / Location.distance (a.location, b.location) / norm;
				for (int n = 0; n < degree / 2; n++) {
					if (random.nextFloat() < p) {
						connect(a, b);
						break;
					}
				}
			}
		}
	}

	protected Node createNode(int i, double loc) {
		return new Node(loc);
	}

	private static void connect(Node n1, Node n2) {
		if(n1.hasPeer(n2)) return;
		n1.addPeer(n2);
		n2.addPeer(n1);
	}
	


}
