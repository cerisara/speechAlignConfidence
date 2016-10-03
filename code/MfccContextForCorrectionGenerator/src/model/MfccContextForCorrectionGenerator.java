package model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;

import textgridparser.model.Parser;
import textgridparser.model.datastructure.*;
import textgridparser.model.datastructure.TextGridItem.TierLevel;

import mfcc.*;

//Warning, for now the code can only be use with the jtrans gold generated for one speaker only
public class MfccContextForCorrectionGenerator {
	
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_DOWN);
	    return bd.doubleValue();
	}
	
	public static TextGridInterval getLeftPhon(TextGridRoot root, double time){
		for(int i =0; i < root.getItemCount(); ++i){
			TextGridItem item = root.getItem(i);
			if(item.getTierLevel() != TierLevel.PHONS){
				continue;
			}
			TextGridInterval interval = item.getIntervalWithEndTime(time, 0.01);
			if(interval != null){//We get a match
				return interval;
			}
		}
		
		return null;
	}
	
	public static TextGridInterval getRightPhon(TextGridRoot root, double time){
		for(int i =0; i < root.getItemCount(); ++i){
			TextGridItem item = root.getItem(i);
			if(item.getTierLevel() != TierLevel.PHONS){
				continue;
			}
			TextGridInterval interval = item.getIntervalWithBeginTime(time, 0.01);
			if(interval != null){//We get a match
				return interval;
			}
		}
		
		return null;
	}
	public static void main(String[] args) {
		Parser p = new Parser();
		File f = new File(args[0]);
		
		int trameCountOnLeft = Integer.parseInt(args[5]);
		int trameCountOnRight = Integer.parseInt(args[6]);
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(args[2]))));
			
			TextGridRoot root = p.parseFile(f.getAbsolutePath());
			MfccCollection collection = new MfccCollection();
			collection.loadCollectionFromFile(args[1]);
			collection.setFrameCountOnLeft(Integer.parseInt(args[3]));
			collection.setFrameCountOnRight(Integer.parseInt(args[4]));
			
			String header = "" + collection.getFrameCountOnLeft() + ";" + collection.getFrameCountonRight() + 
					";39;2;" + MfccContext.getPhonCount() + ";" + trameCountOnLeft + ";" + trameCountOnRight +"\n";
			bw.write(header);
			
			for(int i =0; i < root.getItemCount(); ++i){
				TextGridItem item = root.getItem(i);
				if(item.getTierLevel() == TierLevel.WORD){ // We only have one speaker for the gold
					for(int j=0; j < item.getIntervalsCount(); ++j){
						TextGridInterval it = item.getInterval(j);
						LSTMMfccContextForCorrection cm4 = new LSTMMfccContextForCorrection(collection);
						LSTMMfccContextForCorrection c = new LSTMMfccContextForCorrection(collection);
						LSTMMfccContextForCorrection cp4 = new LSTMMfccContextForCorrection(collection);
						
						TextGridInterval left = getLeftPhon(root, it.getXmin());
						TextGridInterval right = getRightPhon(root, it.getXmin());
						if(left != null && right != null){
							if(c.loadMfccContextForCorrection(it.getXmin(), left, right) 
									&& cm4.loadMfccContextForCorrection(round(it.getXmin() - 0.04, 2), left, right)
									&& cp4.loadMfccContextForCorrection(round(it.getXmin() + 0.04, 2), left, right)){
								bw.write(f.getName() + ";" + it.hashCode()+ ";begin;" + it.getXmin() + "\n");
								bw.write(cm4.getFileString() + "\n");
								bw.write(c.getFileString() + "\n");
								bw.write(cp4.getFileString()+ "\n");
							}
						} else {
							System.out.println("Unable to find phons for time " + it.getXmin() + " (" + left + ", " +right + ")");
						}
						
						left = getLeftPhon(root, it.getXmax());
						right = getRightPhon(root, it.getXmax());
						cm4 = new LSTMMfccContextForCorrection(collection); //Maybe not mandatory but should avoid bug in textgrid correction phase
						c = new LSTMMfccContextForCorrection(collection);
						cp4 = new LSTMMfccContextForCorrection(collection);
						if(left != null && right != null){
							if(c.loadMfccContextForCorrection(it.getXmax(), left, right) 
									&& cm4.loadMfccContextForCorrection(round(it.getXmax() - 0.04, 2), left, right)
									&& cp4.loadMfccContextForCorrection(round(it.getXmax() + 0.04, 2), left, right)){
								bw.write(f.getName() + ";" + it.hashCode()+ ";end;" + it.getXmax() + "\n");
								bw.write(cm4.getFileString() + "\n");
								bw.write(c.getFileString()+"\n");
								bw.write(cp4.getFileString()+ "\n");
							}
						}else {
							System.out.println("Unable to find phons for time " + it.getXmax() + " (" + left + ", " +right + ")");
						}
					}
				}
			}
			
			bw.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
