package freenet.simulator.mast;

import java.util.Random;

/** Static topology, Mallory controls one node, how much information does he get? */
public class MultiplePassiveListenersCountsSimulator extends Simulator {

	private final boolean[] mallory;
	private final int alice;
	public static int sentRequests;
	
	public MultiplePassiveListenersCountsSimulator(int size, int degree, Random r, int mallories) {
		super(size, degree, r);
		mallory = new boolean[nodes.length];
		alice = r.nextInt(nodes.length);
		for(int i=0;i<mallories;i++) {
			int m;
			do {
				m = r.nextInt(nodes.length);
			} while(m == alice || mallory[m]);
			mallory[m] = true;
		}
	}
	
	@Override
	protected Node createNode(int i, double loc) {
		if(mallory[i])
			return new SimpleMalloryNode(loc);
		else
			return new Node(loc);
	}

	public static void main(String[] args) {
		Random r = new Random(123456);
		MultiplePassiveListenersCountsSimulator s = new MultiplePassiveListenersCountsSimulator(1000, 20, r, 10);
		s.initNetwork(1000, 20, r);
		Node alice = s.getAlice();
		for(;true;sentRequests++) {
			double target = r.nextDouble();
			alice.route(new Insert(target, (short) 10)); 
		}
	}

	private Node getAlice() {
		return nodes[alice];
	}
	
	private long totalCount;
	
	private void doMallory(Insert ins, SimpleMalloryNode n) {
		n.count++;
		totalCount++;
		if(totalCount % 1024 == 0) {
			System.out.println();
			for(int i=0;i<nodes.length;i++) {
				if(i == alice) {
					System.out.println("Alice at "+i+" ("+nodes[i].location+") sent "+sentRequests);
				} else if(nodes[i] instanceof SimpleMalloryNode) {
					SimpleMalloryNode m = (SimpleMalloryNode) nodes[i];
					System.out.println("Mallory at "+i+" ("+nodes[i].location+") received "+m.count);
				}
			}
		}
	}
	
	public class SimpleMalloryNode extends Node {
		
		long count;
		
		public SimpleMalloryNode(double d) {
			super(d);
		}

		public boolean route(Insert ins) {
			doMallory(ins, this);
			return super.route(ins);
		}
	}


	
}
