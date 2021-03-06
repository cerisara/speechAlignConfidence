package mfcc;

import java.math.BigDecimal;
import java.math.RoundingMode;

//The purpose of this class is to store all element that is relative to mfcc context in deepaligncontext (so we also have the phons)
public class MfccContext {
	//public static final String[] phonList = {"", "a", "an", "b", "ch", "d", "e", "eh", "eu", "euf", "f", "g", "ge", "gn", "i", "in", "j", "k", "l", "m", "n",
	//	"oh", "on", "p", "r", "s", "SIL", "swa", "t", "u", "v", "w", "y", "z"};
	
	public static int getPhonCount(){
		return 33;
	}
	//Pretty ugly but necessary function.
	//TODO order by frequency to spare time
	public static int getPhonNumero(String phon){
		if(phon.equals("a"))
			return 0;
		else if(phon.equals("an") || phon.equals("a~"))
			return 1;
		else if(phon.equals("b"))
			return 2;
		else if(phon.equals("ch") || phon.equals("S"))
			return 3;
		else if(phon.equals("d") || phon.equals("d"))
			return 4;
		else if(phon.equals("e") || phon.equals("eh") || phon.equals("E") || phon.equals("E/"))
			return 5;
		else if(phon.equals("eu") || phon.equals("2"))
			return 6;
		else if(phon.equals("euf") || phon.equals("9"))
			return 7;
		else if(phon.equals("f"))
			return 8;
		else if(phon.equals("g"))
			return 9;
		else if(phon.equals("ge") || phon.equals("Z"))
			return 10;
		else if(phon.equals("gn") || phon.equals("J"))
			return 11;
		else if(phon.equals("i") || phon.equals("H"))
			return 12;
		else if(phon.equals("in") || phon.equals("e~") || phon.equals("9~"))
			return 13;
		else if(phon.equals("j") || phon.equals("%j"))
			return 14;
		else if(phon.equals("k"))
			return 15;
		else if(phon.equals("l"))
			return 16;
		else if(phon.equals("m") || phon.equals("mm"))
			return 17;
		else if(phon.equals("n"))
			return 18;
		else if(phon.equals("oh") || phon.equals("o") || phon.equals("O") || phon.equals("O/"))
			return 19;
		else if(phon.equals("on") || phon.equals("o~"))
			return 20;
		else if(phon.equals("p") || phon.equals("pp"))
			return 21;
		else if(phon.equals("r") || phon.equals("R") || phon.equals("rr"))
			return 22;
		else if(phon.equals("s"))
			return 23;
		else if(phon.equals("SIL") || phon.equals("#") || phon.equals("bb") || phon.equals("bip") ||
				phon.equals("&blabla") || phon.equals("&bruit") || phon.equals("hh") || phon.equals("xx"))
			return 24;
		else if(phon.equals("swa") || phon.equals("@"))
			return 25;
		else if(phon.equals("t") || phon.equals("tt"))
			return 26;
		else if(phon.equals("u") || phon.equals("U~/"))
			return 27;
		else if(phon.equals("v"))
			return 28;
		else if(phon.equals("w") || phon.equals("%w"))
			return 29;
		else if(phon.equals("y"))
			return 30;
		else if(phon.equals("z"))
			return 31;
		else if(phon.equals(""))
			return 32;
		else 
			return -1;
	}

	private boolean goodFrame;
	private MfccCollection collection;
	private MfccFrame[] mfccFrameArray;
	private double time;
	private String text;
	private String leftPhon;
	private String rightPhon;
	private int distanceToJtrans;
	
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	

	
	
	public MfccContext(MfccCollection collection){
		this.collection = collection;
		time = -1;
		mfccFrameArray = null;
		text = "";
		leftPhon = null;
		rightPhon=null;
		distanceToJtrans = 0;
	}
	
	public MfccContext(MfccCollection collection, double time, boolean goodFrameFlag){
		this.collection = collection;
		mfccFrameArray = null;
		text = "";
		leftPhon = null;
		rightPhon=null;
		distanceToJtrans = 0;
		
		loadFrameAndContext(time);
		setGoodFrameFlag(goodFrameFlag);
	}
	
	public MfccContext(MfccCollection collection, double time, boolean goodFrameFlag, String text){
		this.collection = collection;
		this.text = text;
		leftPhon = null;
		rightPhon=null;
		distanceToJtrans = 0;
		
		loadFrameAndContext(time);
		setGoodFrameFlag(goodFrameFlag);
	}
	
	public MfccContext(MfccCollection collection, double time, boolean goodFrameFlag, String text, String leftPhon, String rightPhon){
		this.collection = collection;
		this.text = text;
		distanceToJtrans = 0;
		
		setLeftPhon(leftPhon);
		setRightPhon(rightPhon);
		
		loadFrameAndContext(time);
		setGoodFrameFlag(goodFrameFlag);
	}
	
	public MfccContext(MfccCollection collection, double time, boolean goodFrameFlag, String text, String leftPhon, String rightPhon, int distanceToJtrans){
		this.collection = collection;
		this.text = text;
		this.distanceToJtrans = distanceToJtrans;
		
		setLeftPhon(leftPhon);
		setRightPhon(rightPhon);
		
		loadFrameAndContext(time);
		setGoodFrameFlag(goodFrameFlag);
	}
	
	private boolean sanityCheck(){
		if(mfccFrameArray == null)
			return false;
		for(MfccFrame f : mfccFrameArray){
			if(f==null){
				return false;
			}
		}
		return true;
	}
	
	public boolean loadFrameAndContext(double time){
		this.time = round(time, 2);
		mfccFrameArray = new MfccFrame[getContextSize()];
		int referenceFrameNumero = MfccCollection.timeToMfccFrameNumero(time);
		if(referenceFrameNumero < (collection.getFrameCountOnLeft() + 1) 
				|| referenceFrameNumero > collection.getMfccFrameCount()-(collection.getFrameCountonRight() + 1)){
			System.out.println("Not enough frame to be able to generate a context");
			return false;
		}
		for(int i=-collection.getFrameCountOnLeft(); i <= collection.getFrameCountonRight() ; ++i){
			mfccFrameArray[i+collection.getFrameCountOnLeft()] = collection.getMfccFrameFromNumero(referenceFrameNumero +i);
		}
		if(!sanityCheck()){
			mfccFrameArray = null;
			return false;
		}
		return true; //We double check because we never know
	}
	
	public boolean isValid(){
		boolean res = sanityCheck();
		if(leftPhon != null){
			res = res && (getPhonNumero(leftPhon) != -1);
		}
		if(rightPhon != null){
			res = res && (getPhonNumero(rightPhon) != -1);
		}
		return res;
	}
	
	public void setGoodFrameFlag(boolean value){
		goodFrame = value;
	}
	
	public double getFrameTime(){
		return time;
	}
	
	public void setLeftPhon(String leftPhon){
		if(leftPhon != null)
			this.leftPhon = leftPhon.trim();
		else
			this.leftPhon = leftPhon;
	}
	
	public String getLeftPhon(){
		return leftPhon;
	}
	
	public void setRightPhon(String rightPhon){
		if(rightPhon != null)
			this.rightPhon = rightPhon.trim();
		else
			this.rightPhon = rightPhon;
	}
	
	public String getRightPhon(){
		return rightPhon;
	}
	
	public void setDistanceToJtrans(int distanceToJtrans){
		this.distanceToJtrans = distanceToJtrans;
	}
	
	public int getDistanceToJtrans(){
		return this.distanceToJtrans;
	}
	
	public String getText(){
		return text;
	}
	
	private static String arrayToString(int[] array){
		String res = ""+ array[0];
		for(int i=1; i < array.length ; ++i){
			res +=  ";" + array[i];
		}
		return res;
	}
	
	public String toString(){
		String res = "";
		if(mfccFrameArray[0]!=null){
		
			res += mfccFrameArray[0].toString();
			for(int i=1; i < mfccFrameArray.length ; ++i){
				res += "\n" + mfccFrameArray[i].toString();
			}
		}
		
		// Phon info exists we add them
		if(rightPhon != null && leftPhon != null){
			int[] left = new int[getPhonCount()];
			int[] right = new int[getPhonCount()];
			
			for(int i=0; i < getPhonCount(); ++i){
				left[i]=0;
			}
			left[getPhonNumero(leftPhon)] = 1;
			
			for(int i=0; i < getPhonCount(); ++i){
				right[i]=0;
			}
			right[getPhonNumero(rightPhon)] = 1;
			
			res += "\n" + arrayToString(left);
			res += "\n" + arrayToString(right);
		}
		res += "\n" + distanceToJtrans;
		return res;
	}
	
	public String getFileString(boolean putHeader){
		String res = "";
		if(mfccFrameArray[0] != null){
			if(putHeader){
				res += collection.getFilename() + ";" + time + ";" + MfccCollection.timeToMfccFrameNumero(time) 
						+ ";" + goodFrame + ";" + text + ";" + leftPhon + ";" + rightPhon + "\n";
			}
			res += toString();
		}				
		return res;
	}
	
	public int getContextSize(){
		return collection.getFrameCountOnLeft() + collection.getFrameCountonRight() + 1;
	}
}
