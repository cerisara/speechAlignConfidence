package model;

import textgridparser.model.datastructure.TextGridInterval;
import mfcc.*;

public class MfccContextForCorrection {
	private MfccContext[] contextArray;
	private MfccCollection collection;
	
	private boolean loaded;
	private int trameCountOnLeft;
	private int trameCountOnRight;
	
	
	public MfccContextForCorrection(MfccCollection collection, int trameCountOnLeft, int trameCountOnRight){
		this.collection = collection;
		
		this.trameCountOnLeft = trameCountOnLeft;
		this.trameCountOnRight = trameCountOnRight;
		
		contextArray = new MfccContext[getTotalTrameCount()];
		
		loaded = false;
	}
	
	public boolean loadMfccContextForCorrection(double time, TextGridInterval leftPhonInterval, TextGridInterval rightPhonInterval){
		int centralNumero = MfccCollection.timeToMfccFrameNumero(time);
		
		for(int i =-trameCountOnLeft; i <= trameCountOnRight ; ++i){
			MfccContext c = new MfccContext(collection);
			
			if(!c.loadFrameAndContext(MfccCollection.mfccFrameNumeroToTime(centralNumero + i))){
				System.out.println("Unable to load a proper context for " + time);
				return false;
			}
			c.setLeftPhon(leftPhonInterval.getText());
			c.setRightPhon(rightPhonInterval.getText());
			c.setDistanceToJtrans(i);
			contextArray[i+trameCountOnLeft] = c;
		}
		loaded = true;
		return true;
	}
	
	public String getFileString(){
		if(loaded == false){
			System.out.println("Trying to dump an unload context for correction");
			return null;
		}
		String res = "";
		
		res += contextArray[0].getFileString(false);
		for(int i=1; i < 5+1+5 ; ++i){
			res += "\n" + contextArray[i].getFileString(false); //We already generate an header, don't need a new one
		}
		
		return res;
		
	}
	
	public int getTotalTrameCount(){
		return this.trameCountOnLeft + this.trameCountOnRight + 1;
	}
	
}
