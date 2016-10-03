package model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import textgridaligner.model.TextGridAligner;
import textgridaligner.model.TextGridAligner.Pair;
import textgridparser.model.Parser;
import textgridparser.model.datastructure.TextGridInterval;
import textgridparser.model.datastructure.TextGridItem;
import textgridparser.model.datastructure.TextGridRoot;
import textgridparser.model.datastructure.TextGridItem.TierLevel;

public class GoldAnalyzer {
	private static TextGridInterval getLeftPhon(TextGridRoot root, double time){
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
	
	private static TextGridInterval getRightPhon(TextGridRoot root, double time){
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
	
	private static TextGridInterval getLeftWord(TextGridRoot root, double time){
		for(int i =0; i < root.getItemCount(); ++i){
			TextGridItem item = root.getItem(i);
			if(item.getTierLevel() != TierLevel.WORD){
				continue;
			}
			TextGridInterval interval = item.getIntervalWithEndTime(time, 0.01);
			if(interval != null){//We get a match
				return interval;
			}
		}
		
		return null;
	}
	
	private static TextGridInterval getRightWord(TextGridRoot root, double time){
		for(int i =0; i < root.getItemCount(); ++i){
			TextGridItem item = root.getItem(i);
			if(item.getTierLevel() != TierLevel.WORD){
				continue;
			}
			TextGridInterval interval = item.getIntervalWithBeginTime(time, 0.01);
			if(interval != null){//We get a match
				return interval;
			}
		}
		
		return null;
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TextGridAligner aligner = new TextGridAligner();
	    ArrayList<Pair<TextGridInterval, TextGridInterval>> aligned = aligner.getAlignedIntervalFromFiles(args[0], args[1]);
	    File f = new File(args[0]);
	    String name = f.getName().toLowerCase();
	    name = name.substring(0, name.lastIndexOf('.'));
	    Parser parser = new Parser();
	    try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(args[3]))));
			TextGridRoot root = parser.parseFile(args[0]);
			TextGridInterval temp = null;
			for(Pair<TextGridInterval, TextGridInterval> p : aligned){
					temp = getLeftWord(root, p.left.getXmin());
					TextGridInterval left = getLeftPhon(root, p.left.getXmin());
					TextGridInterval right = getRightPhon(root, p.left.getXmin());
					if(left != null && right != null){
						if(temp != null){
							bw.write(name + ";" + p.right.getXmin() + ";" + p.left.getXmin() + ";" + temp.hashCode() + ";" + left.getText() + ";" + right.getText() +";end\n");
						}
						bw.write(name + ";" + p.right.getXmin() + ";" + p.left.getXmin() + ";" + p.left.hashCode() + ";" + left.getText() + ";" + right.getText()  + ";begin\n");
					}
					
					left = getLeftPhon(root, p.left.getXmax());
					right = getRightPhon(root, p.left.getXmax());
					if(left != null && right != null){
						bw.write(name + ";" + p.right.getXmax() + ";" + p.left.getXmax() + ";" + p.left.hashCode() + ";" + left.getText() + ";" + right.getText()  + ";end\n");
						temp = getRightWord(root, p.left.getXmax());
						if(temp != null){
							bw.write(name + ";" + p.right.getXmax() + ";" + p.left.getXmax() + ";" + temp.hashCode() + ";" + left.getText() + ";" + right.getText()  + ";begin\n");
						}
					}
			}
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
