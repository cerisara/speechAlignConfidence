package textgridparser.model.datastructure;

import java.util.ArrayList;
import java.util.Iterator;

import textgridparser.utils.Utils;

public class TextGridRoot {
	private String fileType;
	private String objectClass;
	private double xmax;
	private double xmin;
	private boolean tiers;
	private int itemCount; //Kept for sanity check
	
	private ArrayList<TextGridItem> itemList;
	
	public TextGridRoot(){
		fileType = null;
		objectClass = null;
		xmax=-1;
		xmin=-1;
		tiers = false;
		itemCount=-1;
		itemList = new ArrayList<TextGridItem>();
	}
	
	public boolean setDataFromString(String data){
		//First we need to clean the line by removing all whitespace at the beginning
		String clean = Utils.removeWhitespace(data);
		
		if(clean.startsWith("xmin")){
			return setXmin(clean);
		} else if (clean.startsWith("xmax")) {
			return setXmax(clean);
		} else if (clean.startsWith("File type")) {
			return setFileType(clean);
		} else if(clean.startsWith("Object class")) {
			return setObjectClass(clean);
		} else if(clean.startsWith("tiers?")) {
			return setTiers(clean);
		} else if(clean.startsWith("size")) {
			return setItemCount(clean);
		}
		return false;
	}
	
	private boolean setXmin(String data){
		xmin = Utils.extractDoubleValue(data.substring(data.lastIndexOf(' ')));
		return true;
	}
	
	private boolean setXmax(String data){
		xmax = Utils.extractDoubleValue(data.substring(data.lastIndexOf(' ')));
		return true;
	}
	
	private boolean setFileType(String data){
		fileType = Utils.extractStringValue(data.substring(data.indexOf('\"')));
		return true;
	}
	
	private boolean setObjectClass(String data){
		objectClass = Utils.extractStringValue(data.substring(data.indexOf('\"')));
		return true;
	}
	
	private boolean setTiers(String data){
		if(data.contains("<exists>")){
			tiers = true;
		} else {
			tiers = false;
		}
		return false;
	}
	
	private boolean setItemCount(String data){
		itemCount = Utils.extractIntegerValue(data.substring(data.lastIndexOf(' ')));
		return true;
	}
	
	public double getXmin(){
		return xmin;
	}
	
	public double getXmax(){
		return xmax;
	}
	
	public boolean isTiersExists(){
		return tiers;
	}
	
	public String getFileType(){
		return fileType;
	}
	
	public String getObjectClass(){
		return objectClass;
	}
	
	public int getItemCount(){
		return itemList.size();
	}
	
	public TextGridItem getItem(int i){
		if(itemList == null || i < 0 || i >= itemList.size())
			return null;
		return itemList.get(i);
	}
	
	public boolean addItem(TextGridItem item){
		if(item == null || itemList == null)
			return false;
		return itemList.add(item);
	}
	
	public String toString(){
		String res = "";
		res += "File type : " + fileType + "\n";
		res += "Object class : " + objectClass + "\n";
		res += "xmin : " + xmin + "\n";
		res += "xmax : " + xmax + "\n";
		res += "tiers? " + (tiers?"<exists>":"") +  "\n";
		res += "size : " + itemCount + "\n";
		
		for(TextGridItem it : itemList){
			res += it.toString();
		}
		return res;
	}
	
	public String toTextGridFormat(){
		String res = "";
		res += "File type = \"" + fileType + "\"\n";
		res += "Object class = \"" + objectClass + "\"\n\n";
		res += "xmin = " + xmin + "\n";
		res += "xmax = " + xmax + "\n";
		res += "tiers? " + (tiers?"<exists>":"") +  "\n";
		res += "size = " + itemCount + "\n";
		res += "item []:\n";
		
		for(int i =0; i < itemList.size(); ++i){
			res += "\titem [" + (i+1) + "]:\n";
			res += itemList.get(i).toTextGridFormat();
		}
		return res;
	}
	
	public boolean sanityCheck(){
		boolean res = true;
		
		res = res && xmin <= xmax;
		res = res && (itemCount == itemList.size());
		for(TextGridItem it : itemList){
			res = res && it.sanityCheck();
		}
		
		return res;
	}
	
	public void setBoundaryInformation(){
		for(TextGridItem it : itemList){
			it.setBoundaryInformation();
		}
	}
	
	public String getTokenListAsStringForTier(TextGridItem.TierLevel level){
		String res = "";
		for(TextGridItem it : itemList){
			if(it.getTierLevel() == level){
				res += it.exportTokenListAsString()+ " ";
			}
		}
		return res;
	}
	
	//WARNING: the suppression is definitive
	public void dropTierLevel(TextGridItem.TierLevel level){
		Iterator<TextGridItem> it = itemList.iterator();
		while(it.hasNext()){
			TextGridItem i = it.next();
			if(i.getTierLevel() == level){
				it.remove();
			}
		}
	}
}
