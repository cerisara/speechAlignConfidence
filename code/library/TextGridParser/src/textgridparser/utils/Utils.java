package textgridparser.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Utils {
	public static String  removeWhitespace(String data){
		if(data == null)
			return null;
		
		String res = data;
		while(res.length() != 0 && (res.charAt(0) == ' ' || res.charAt(0) == '\t')){
			res = res.substring(1);
		}
		while(res.length() != 0 && (res.charAt(res.length()-1) == ' ' || res.charAt(res.length()-1) == '\t')){
			res = res.substring(0, res.length()-1);
		}
		return res;
	}
	
	public static double extractDoubleValue(String data){
		String clean = removeWhitespace(data);
		return Double.valueOf(clean);
	}
	
	public static int extractIntegerValue(String data){
		String clean = removeWhitespace(data);
		return Integer.valueOf(clean);
	}
	
	public static String extractStringValue(String data){
		String clean = removeWhitespace(data);
		return clean.substring(1, clean.length()-1);
	}
	
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
}
