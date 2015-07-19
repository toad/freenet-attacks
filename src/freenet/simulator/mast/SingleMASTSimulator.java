package freenet.simulator.mast;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SingleMASTSimulator extends Simulator {

	private final int mallory;
	private final int alice;
	public static int sentRequests;
	
	public SingleMASTSimulator(int size, int degree, Random r) {
		super(size, degree, r);
		alice = r.nextInt(nodes.length);
		int m;
		do {
			m = r.nextInt(nodes.length);
		} while(m == alice);
		mallory = m;
		//alice = degree/2;
		//mallory = alice - 1;
	}
	
	@Override
	protected Node createNode(int i, double loc) {
		if(i == mallory)
			return new SimpleMalloryNode(loc);
		else
			return new Node(loc);
	}

	public static void main(String[] args) {
		Random r = new Random(12345);
		while(true) {
			SingleMASTSimulator s = new SingleMASTSimulator(1000, 20, r);
			s.initNetwork(1000, 20, r);
			Node alice = s.getAlice();
			for(sentRequests=0;sentRequests<200000;sentRequests++) {
				double target = r.nextDouble();
				alice.route(new Insert(target, (short) 10));
			}
			s.dumpStats();
		}
	}

	private Node getAlice() {
		return nodes[alice];
	}
	
	private double longestSoFarPositive = 0.0;
	private double longestSoFarNegative = 0.0;
	private int countPositive;
	private int countNegative;
	
	private void doMallory(Insert ins, SimpleMalloryNode n) {
		double signedDistance = Location.change(n.location, ins.target);
		if(signedDistance > 0) {
			if(longestSoFarPositive < signedDistance) {
				longestSoFarPositive = signedDistance;
				//System.out.println("New +ve record: "+longestSoFarPositive+" (Alice -> "+ins.target+" through "+n.location+" ) on "+sentRequests);
				//((LoggingInsert) ins).dump();
			}
			countPositive++;
		} else if(signedDistance < 0) {
			if(longestSoFarNegative < -signedDistance) {
				longestSoFarNegative = -signedDistance;
				//System.out.println("New -ve record: "+longestSoFarNegative+" (Alice -> "+ins.target+" through "+n.location+" ) on "+sentRequests);
			}
			countNegative++;
		}
		if((countPositive + countNegative) % 10000 == 0) {
			//dumpStats();
		}
	}
	
	static int counterMalloryPositive = 0;
	static int counterMalloryNegative = 0;
	static int counterAlicePositive = 0;
	static int counterAliceNegative = 0;
	
	static enum Evaluate {
		Counts,
		Extremes
	}
	
	Evaluate mode = Evaluate.Extremes;
	
	private void dumpStats() {
		double aliceLoc = getAlice().location;
		double malloryLoc = nodes[mallory].location;
		//System.out.println("Actual target: "+aliceLoc+" Mallory is at "+malloryLoc);
		//System.out.println("Ratio "+((double)countPositive/(double)(countPositive+countNegative))+ ": Positive "+countPositive+" negative "+countNegative+" records "+(-longestSoFarNegative)+" "+longestSoFarPositive);
		//System.out.println("Total proportion "+((double)(countPositive + countNegative))/sentRequests +"="+(countPositive + countNegative)+"/"+sentRequests);
		boolean malloryGreaterThanAlice = Location.change(aliceLoc, malloryLoc) > 0;
		boolean morePositiveThanNegative;
		switch(mode) {
		case Counts:
			if(countNegative == countPositive) return;
			morePositiveThanNegative = countPositive > countNegative;
			break;
		case Extremes:
			if(longestSoFarNegative == 0 && longestSoFarPositive == 0) return;
			morePositiveThanNegative = longestSoFarPositive > longestSoFarNegative;
			System.out.println("Range: "+(-longestSoFarNegative)+" to "+longestSoFarPositive);
			break;
		default:
			throw new IllegalStateException();
		}
		//System.out.println("Mallory > Alice: "+malloryGreaterThanAlice+" More positive than negative extent: "+morePositiveThanNegative);
		if(malloryGreaterThanAlice) {
			if(morePositiveThanNegative) {
				counterMalloryPositive++;
			} else {
				counterMalloryNegative++;
			}
		} else {
			if(morePositiveThanNegative) {
				counterAlicePositive++;
			} else {
				counterAliceNegative++;
			}
		}
		int total = counterMalloryPositive + counterMalloryNegative +
				counterAlicePositive + counterAliceNegative;
		System.out.println("Totals: "+counterMalloryPositive+"("+(100*counterMalloryPositive / total)+
				"%) "+counterMalloryNegative+" (" +(100*counterMalloryNegative / total)+"%) "+
				counterAlicePositive+" (" +(100*counterAlicePositive / total)+"%) "+
				counterAliceNegative+" (" +(100*counterAliceNegative / total)+"%)");
	}
	
	public class SimpleMalloryNode extends Node {
		
		private Map<Node, Integer> forwardedBy = new HashMap<Node, Integer>();
		private Map<Node, Integer> forwardedByTotalHTL = new HashMap<Node, Integer>();
		private int dumped = 0;
		
		public SimpleMalloryNode(double d) {
			super(d);
		}

		public boolean route(Insert ins) {
			if(!ins.inRandomRoute()) {
//				Node prevNode = ((LoggingInsert)ins).getPrevNode();
//				if(prevNode != null) {
//					Integer count = forwardedBy.get(prevNode);
//					if(count == null) count = 0;
//					count++;
//					Integer totalHTL = forwardedByTotalHTL.get(prevNode);
//					if(totalHTL == null) totalHTL = 0;
//					totalHTL += ins.getHTL();
//					forwardedBy.put(prevNode, count);
//					forwardedByTotalHTL.put(prevNode, totalHTL);
//					if(dumped++ % (10*1024) == 0) {
//						for(Map.Entry<Node, Integer> entry : forwardedBy.entrySet()) {
//							Node peer = entry.getKey();
//							int c = entry.getValue();
//							System.out.println("Peer "+peer.location+" : "+c+" distance "+Location.change(location, peer.location)+" mean HTL "+(forwardedByTotalHTL.get(peer) / c));
//						}
//					}
//				}
				if(ins.getHTL() >= 7)
					doMallory(ins, this);
			}
			return super.route(ins);
		}
	}


	
}
