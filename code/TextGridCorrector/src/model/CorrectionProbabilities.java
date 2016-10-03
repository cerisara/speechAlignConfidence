package model;

import java.util.ArrayList;
import java.util.Collections;

public class CorrectionProbabilities {
	public enum Bound{
		END,
		BEGIN
	}
	
	private class PairProbPos implements Comparable<PairProbPos>{
		public int position;
		public double prob;
		
		public PairProbPos(int position, double prob){
			this.position = position;
			this.prob = prob;
		}

		@Override
		public int compareTo(PairProbPos arg0) {
			if(this.prob == arg0.prob)
				return 0;
			if(this.prob > arg0.prob)
				return -1;
			return 1;
		}
	}
	
	private int hashcode;
	private Bound bound;
	private ArrayList<PairProbPos> probList;
	private int trameCountOnRight;
	private int trameCountOnLeft;
	
	public CorrectionProbabilities(int hashcode, Bound bound, int trameCountOnLeft, int trameCountOnRight){
		this.hashcode = hashcode;
		this.bound = bound;
		this.trameCountOnLeft = trameCountOnLeft;
		this.trameCountOnRight = trameCountOnRight;
		
		probList = new ArrayList<PairProbPos>();
		
	}
	
	public void setProbability(int numero, double value){
		probList.add(new PairProbPos(numero-trameCountOnLeft, value));
	}
	
	public String toString(){
		return "" + hashcode + " " + bound;
	}
	
	public int getHashcode(){
		return hashcode;
	}
	
	public Bound getBound(){
		return bound;
	}
	
	public int getFrameCorrection(int index){
		return probList.get(index).position;
	}
	
	public double getProbabilityFromPosition(int position){
		for(PairProbPos p : probList){
			if(p.position == position){
				return p.prob;
			}
		}
		return -1;
	}
	
	public void processCorrection(){
		Collections.sort(probList);
	}
	
	public String getPositionString(){
		String res = "" + probList.get(0).position;
		for(int i = 1; i < probList.size(); ++i){
			res += ";" + probList.get(i).position;
		}
		return res;
	}
	
	public String getProbString(){
		String res = "" + probList.get(0).prob;
		for(int i = 1; i < probList.size(); ++i){
			res += ";" + probList.get(i).prob;
		}
		return res;
	}
}
