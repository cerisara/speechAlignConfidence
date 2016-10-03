package textgridparser.model.datastructure;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import textgridparser.model.datastructure.TextGridInterval.BoundRelation;
import textgridparser.utils.Utils;

public class TextGridItem {
	public enum TierLevel{
		WORD,
		PHONS,
		NONE
	}
	
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	
	private String className;
	private String name;
	private double xmin;
	private double xmax;
	private int intervalsCount; //We keep the count for sanity check
	
	private String speaker;
	private TierLevel level;
	
	private ArrayList<TextGridInterval> intervalsList;
	
	public TextGridItem(){
		className = null;
		name = null;
		xmin = -1;
		xmax = -1;
		intervalsCount = -1;
		
		intervalsList = new ArrayList<TextGridInterval>();
		
		speaker = null;
		level = TierLevel.NONE;
	}
	
	public boolean setDataFromString(String data){
		String clean = Utils.removeWhitespace(data);
		
		if(clean.startsWith("class")){
			return setClassName(clean);
		} else if(clean.startsWith("name")) {
			return setName(clean);
		} else if(clean.startsWith("xmin")) {
			return setXmin(clean);
		} else if(clean.startsWith("xmax")) {
			return setXmax(clean);
		} else if(clean.startsWith("intervals: size")) {
			return setIntervalsCount(clean);
		}
		return false;
	}
	
	private boolean setClassName(String data){
		className = Utils.extractStringValue(data.substring(data.indexOf('\"')));
		return true;
	}
	
	private boolean setName(String data){
		name = Utils.extractStringValue(data.substring(data.indexOf('\"')));

		extractSpeaker();
		extractTierLevel();
		return true;
	}
	
	private boolean setXmin(String data){
		xmin = Utils.extractDoubleValue(data.substring(data.lastIndexOf(' ')));
		return true;
	}
	
	private boolean setXmax(String data){
		xmax = Utils.extractDoubleValue(data.substring(data.lastIndexOf(' ')));
		return true;
	}
	
	private boolean setIntervalsCount(String data){
		intervalsCount = Utils.extractIntegerValue(data.substring(data.lastIndexOf(' ')));
		return true;
	}
	
	private void extractSpeaker(){
		//There is differencies between name convention between astali and jtrans, so let's check each case
		extractTierLevel(); //We need to have this done
		if(level == TierLevel.WORD){
			if(name.contains(" words")){//Jtrans
				speaker = name.substring(0, name.indexOf(" words"));
			} else if(name.contains("WordTier")){//Astali
				speaker = name.substring(0, name.indexOf("WordTier"));
				if(speaker.equals("")){//If there is no speaker we put it at unknown like in jtrans
					speaker = "Unknown";
				}
			} 
		}
		if(level == TierLevel.PHONS){
			if(name.contains(" phons")){//Jtrans
				speaker = name.substring(0, name.indexOf(" phons"));
			} else if(name.contains("PhonTier")){//Astali
				speaker = name.substring(0, name.indexOf("PhonTier"));
				if(speaker.equals("")){//If there is no speaker we put it at unknown like in jtrans
					speaker = "Unknown";
				}
			} 
		}
	}
	
	private void extractTierLevel(){
		if(name.contains("words") || name.contains("Word")){
			level = TierLevel.WORD;
		} else if(name.contains("phons") || name.contains("Phon")) {
			level = TierLevel.PHONS;
		} else {
			level = TierLevel.NONE;
		}
	}
	
	public String getClassName(){
		return className;
	}
	
	public String getName(){
		return name;
	}
	
	public double getXmin(){
		return xmin;
	}
	
	public double getXmax(){
		return  xmax;
	}
	
	public int getIntervalsCount(){
		return intervalsList.size();
	}
	
	public String getSpeaker(){
		return speaker;
	}
	
	public TierLevel getTierLevel(){
		return level;
	}
	
	public TextGridInterval getInterval(int i){
		if(intervalsList == null || i < 0 || i >= intervalsList.size())
			return null;
		return intervalsList.get(i);
	}
	
	public boolean addInterval(TextGridInterval interval){
		if(interval == null || intervalsList == null)
			return false;
		return intervalsList.add(interval);
	}
	
	public String toString(){
		String res = "";
		res += "\tclass : " + className + "\n";
		res += "\tname : " + name + "\n";
		res += "\txmin : " + xmin + "\n";
		res += "\txmax : " + xmax + "\n";
		res += "\tintervals : size " + intervalsCount + "\n";
		
		for(TextGridInterval in : intervalsList){
			res += "\tintervals : \n";
			res += in.toString();
		}
		return res;
	}
	
	public String toTextGridFormat(){
		String res = "";
		res += "\t\tclass = \"" + className + "\"\n";
		res += "\t\tname = \"" + name + "\"\n";
		res += "\t\txmin = " + xmin + "\n";
		res += "\t\txmax = " + xmax + "\n";
		res += "\t\tintervals: size = " + intervalsCount + "\n";
		
		for(int i=1; i <= intervalsList.size(); ++i){
			res += "\t\tintervals [" + i + "]:\n";
			res += intervalsList.get(i-1).toTextGridFormat();
		}
		return res;
	}
	
	public boolean sanityCheck(){
		boolean res = true;
		
		res = res && (xmin <= xmax);
		res = res && (intervalsCount == intervalsList.size());
		for(TextGridInterval in : intervalsList){
			res = res && in.sanityCheck();
		}
		if(!res){
			System.out.println("Error in item " + xmin+ "  "+ xmax + " " + intervalsCount + " " + intervalsList.size());
		}
		return res;
	}
	
	private void setBoundaryRelationTo(int i, boolean begin){
		if(i <0 || i >= intervalsList.size()){
			return;
		}
		TextGridInterval it = intervalsList.get(i);
		if(begin){
			it.setBeginFlag();
		} else {
			it.setEndFlag();
		}
	}
	
	public void setBoundaryInformation(){
		//We need to parse the list, and for each boundary we need to update 
		for(int i=0; i < intervalsList.size(); ++i){
			TextGridInterval it = intervalsList.get(i);
			if(it.getBoundRelation() == BoundRelation.BEGIN_BOUND){
				setBoundaryRelationTo(i-1, true);
				setBoundaryRelationTo(i+1, true);
			} else if(it.getBoundRelation() == BoundRelation.END_BOUND){
				setBoundaryRelationTo(i-1, false);
				setBoundaryRelationTo(i+1, false);
			}
		}
	}
	
	
	public String exportTokenListAsString(){
		String res = "";
		for(int i=0; i < intervalsList.size(); ++i){
			String text = intervalsList.get(i).getText();
			if(!text.contains("#") && !text.contains("&np"))
				res += text + " ";
		}
		return res;
	}
	
	public TextGridInterval getIntervalWithBeginTime(double time){
		for(TextGridInterval it : intervalsList){
			if(it.getXmin() == time){
				return it;
			}
		}
		return null;
	}
	
	public TextGridInterval getIntervalWithBeginTime(double time, double tolerance){
		for(TextGridInterval it : intervalsList){
			if(Math.abs(round(it.getXmin() - time, 2)) <= tolerance){
				return it;
			}
		}
		return null;
	}
	
	public TextGridInterval getIntervalWithEndTime(double time){
		return getIntervalWithEndTime(time, 0);
	}
	
	public TextGridInterval getIntervalWithEndTime(double time, double tolerance){
		for(TextGridInterval it : intervalsList){
			if(Math.abs(round(it.getXmax() - time, 2)) <= tolerance){
				return it;
			}
		}
		return null;
	}
	
	public boolean isContainingInterval(TextGridInterval interval){
		for(TextGridInterval temp : intervalsList){
			if(temp.hashCode() == intervalsList.hashCode()){
				return true;
			}
		}
		return false;
	}
	
	public int getIntervalIndex(TextGridInterval interval){
		for(int i=0; i < getIntervalsCount(); ++i){
			TextGridInterval inter = intervalsList.get(i);
			if(inter.hashCode() == interval.hashCode()){
				return i;
			}
		}
		return -1;
	}
	
	//This a wrapper to safely deal with index inside the class
	private TextGridInterval getTextGridIntervalFromIndex(int index){
		if( index < 0 || index >= getIntervalsCount())
			return null;
		return intervalsList.get(index);
	}
	
	//This function give the 
	public TextGridInterval getLastRelevantInterval(TextGridInterval interval){
		int index = getIntervalIndex(interval);
		if(index == -1){
			System.out.println("Cannot found the interval.");
			return null;
		}
		TextGridInterval inter;
		--index;
		while(true){
			inter = getTextGridIntervalFromIndex(index);
			if(inter == null){// We cannot find a relevant interval before
				break;
			}
			if(Utils.round(inter.getDuration(), 2) != 0){ // We check if the interval have at least one frame of duration
				break;
			}
			--index;
		}
		return inter;
	}
	
	public TextGridInterval getFollowingRelevantInterval(TextGridInterval interval){
		int index = getIntervalIndex(interval);
		if(index == -1){
			System.out.println("Cannot found the interval.");
			return null;
		}
		TextGridInterval inter;
		++index;
		while(true){
			inter = getTextGridIntervalFromIndex(index);
			if(inter == null){// We cannot find a relevant interval before
				break;
			}
			if(Utils.round(inter.getDuration(), 2) != 0){ // We check if the interval have at least one frame of duration
				break;
			}
			++index;
		}
		return inter;
	}
}
