package model;

import mfcc.LSTMMfccContext;
import mfcc.MfccCollection;
import textgridparser.model.datastructure.TextGridInterval;

public class LSTMMfccContextForCorrection {
	private LSTMMfccContext context;
	private MfccCollection collection;
	
	private boolean loaded;
	
	
	public LSTMMfccContextForCorrection(MfccCollection collection){
		this.collection = collection;
		context = null;
		loaded = false;
	}
	
	public boolean loadMfccContextForCorrection(double time, TextGridInterval leftPhonInterval, TextGridInterval rightPhonInterval){
		LSTMMfccContext c = new LSTMMfccContext(collection);
		
		if(!c.loadFrameAndContext(time)){
			System.out.println("Unable to load a proper context for " + time);
			return false;
		}
		c.setLeftPhon(leftPhonInterval.getText());
		c.setRightPhon(rightPhonInterval.getText());
		c.setDistanceToJtrans(0);
		context = c;
			
		loaded = true;
		return true;
	}
	
	public String getFileString(){
		if(loaded == false){
			System.out.println("Trying to dump an unload context for correction");
			return null;
		}
		String res = context.getFileString(false);
		return res;
	}	
}
