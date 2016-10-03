package textgridparser.model.datastructure;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import textgridparser.utils.Utils;

public class TextGridInterval {
	public enum BoundRelation{
		NONE,
		FIRST_SEGMENT,
		LAST_SEGMENT,
		BOTH_SEGMENT,
		BEGIN_BOUND,
		END_BOUND
	}
	
	private double xmin;
	private double xmax;
	
	//We need to differentiate them for the hashcode
	private double correctedXmin;
	private double correctedXmax;
	
	private String text;
	private BoundRelation relation;
	
	private static Pattern startPattern = Pattern.compile("^\\[idtext[0-9]+:start\\]");
	private static Pattern endPattern = Pattern.compile("^(\\[idtext[0-9]+:end\\])");
	
	public TextGridInterval(){
		xmin = -1;
		xmax = -1;
		correctedXmin = -1;
		correctedXmax = -1;
		text = null;
		relation = BoundRelation.NONE;
	}
	
	public boolean setDataFromString(String data){
		//First we need to clean the line by removing all whitespace at the beginning
		String clean = Utils.removeWhitespace(data);
		
		if(clean.startsWith("xmin")){
			return setXmin(clean);
		} else if (clean.startsWith("xmax")) {
			return setXmax(clean);
		} else if (clean.startsWith("text")) {
			return setText(clean);
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
	
	private boolean setText(String data){
		text = Utils.extractStringValue(data.substring(data.indexOf('\"')));
			
		Matcher m = startPattern.matcher(text);
		if(m.find()) {
			relation = BoundRelation.BEGIN_BOUND;
		} else {
			m = endPattern.matcher(text);
			
			if(m.find()){
				relation = BoundRelation.END_BOUND;
			} else {
				relation = BoundRelation.NONE;
			}
		}
		
		return true;
	}
	
	public double getXmin(){
		if(correctedXmin != -1)
			return correctedXmin;
		return xmin;
	}
	
	public double getXmax(){
		if(correctedXmax != -1)
			return correctedXmax;
		return xmax;
	}
	
	public void correctXmin(double time){
		correctedXmin = time;
	}
	
	public void correctXmax(double time){
		correctedXmax = time;
	}
	
	public double getDuration(){
		return xmax - xmin;
	}
	
	public String getText(){
		return text;
	}
	
	public BoundRelation getBoundRelation(){
		return relation;
	}
	
	public void setBoundRelation(BoundRelation br){
		relation = br;
	}
	
	public void setBeginFlag(){
		if(relation == BoundRelation.BEGIN_BOUND || relation == BoundRelation.END_BOUND)
			return;
		
		if(relation ==  BoundRelation.LAST_SEGMENT){
			relation = BoundRelation.BOTH_SEGMENT;
		} else {
			relation = BoundRelation.FIRST_SEGMENT;
		}
	}
	
	public void setEndFlag(){
		if(relation == BoundRelation.BEGIN_BOUND || relation == BoundRelation.END_BOUND)
			return;
		
		if(relation ==  BoundRelation.FIRST_SEGMENT){
			relation = BoundRelation.BOTH_SEGMENT;
		} else {
			relation = BoundRelation.LAST_SEGMENT;
		}
	}
	
	public String toString(){
		String res = "";
		res += "\t\txmin : " + this.getXmin() + "\n";
		res += "\t\txmax : " + this.getXmax() + "\n";
		res += "\t\ttext : " + text + "\n";
		res += "\t\trelation : " + relation +"\n";
		return res;
	}
	
	public boolean sanityCheck(){
		if(!(this.getXmin() <= this.getXmax())){
			System.out.println("Error in interval bound");
			return false;
		}
		return true;
	}
	
	public String toTextGridFormat(){
		String res = "";
		res += "\t\t\txmin = " + this.getXmin() + "\n";
		res += "\t\t\txmax = " + this.getXmax() + "\n";
		res += "\t\t\ttext = \"" + text + "\"\n";
		return res;
	}
	
	//We compute the hash we the with the text, the begin and the end timeCode
	//Should be unique over a file (hopefully)
	public int hashCode(){
		String id = text + xmin + xmax + relation;
		return id.hashCode();
	}
}
