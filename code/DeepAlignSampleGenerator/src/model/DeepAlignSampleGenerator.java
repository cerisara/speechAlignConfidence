package model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import textgridaligner.model.TextGridAligner;
import textgridaligner.model.TextGridAligner.Pair;
import textgridparser.model.Parser;
import textgridparser.model.datastructure.TextGridInterval;
import textgridparser.model.datastructure.TextGridInterval.BoundRelation;
import textgridparser.model.datastructure.TextGridItem.TierLevel;
import textgridparser.model.datastructure.TextGridItem;
import textgridparser.model.datastructure.TextGridRoot;
import mfcc.MfccCollection;
import mfcc.MfccContext;
import mfcc.LSTMMfccContext;

//hacked version of DeepAlignSampleGenerator in purpose of testing
public class DeepAlignSampleGenerator {
	
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
	
	public static TextGridInterval getRightInterval(TextGridRoot root, TextGridInterval ref){
		for(int i=0; i < root.getItemCount(); ++i){
			TextGridItem item = root.getItem(i);
			if(item.isContainingInterval(ref)){
				return item.getFollowingRelevantInterval(ref);
			}
		}
		return null; //In case we don't find the interval in any item
	}
	
	public static TextGridInterval getLeftInterval(TextGridRoot root, TextGridInterval ref){
		for(int i=0; i < root.getItemCount(); ++i){
			TextGridItem item = root.getItem(i);
			if(item.isContainingInterval(ref)){
				return item.getLastRelevantInterval(ref);
			}
		}
		return null; //In case we don't find the interval in any item
	}
	
	public static boolean checkIntervalOnLeft(TextGridRoot refRoot, TextGridInterval refInter, TextGridRoot cmpRoot, TextGridInterval cmpInter){
		TextGridInterval refLeft = getLeftInterval(refRoot, refInter);
		TextGridInterval cmpLeft = getLeftInterval(cmpRoot, cmpInter);
		if(refLeft == null || cmpLeft == null){//Technically, the first and the last bound are always exclude because of this, but it 
			return false;                      //is not an issue because there are also exclude by the phon stage
		}
		return refLeft.hashCode() == cmpLeft.hashCode();
	}
	
	public static boolean checkIntervalOnRight(TextGridRoot refRoot, TextGridInterval refInter, TextGridRoot cmpRoot, TextGridInterval cmpInter){
		TextGridInterval refRight = getRightInterval(refRoot, refInter);
		TextGridInterval cmpRight = getRightInterval(cmpRoot, cmpInter);
		if(refRight == null || cmpRight == null){//Technically, the first and the last bound are always exclude because of this, but it 
			return false;                      //is not an issue because there are also exclude by the phon stage
		}
		return refRight.hashCode() == cmpRight.hashCode();
	}
	
	public static void addExclusionZone(HashSet<Double> tSet, double refTime){
		tSet.add(refTime);// We only put this because we want a all possible
		//tSet.add(round(refTime-0.01, 2));//We should not need to secure the side as phon info is there
		//tSet.add(round(refTime-0.02, 2));
		//tSet.add(round(refTime+0.01, 2));
		//tSet.add(round(refTime+0.02, 2));
	}
	//TODO check if there is some rounding issue
	public static ArrayList<LSTMMfccContext> generateContextList(String refPath, String cmpPath, String mfccPath, 
			int frameCountOnLeft, int frameCountOnRight){
		ArrayList<LSTMMfccContext> res = new ArrayList<LSTMMfccContext>();
		HashSet<Double> timestampSet = new HashSet<Double>();
		ArrayList<LSTMMfccContext> trueContextList = new ArrayList<LSTMMfccContext>();
		ArrayList<LSTMMfccContext> falseContextList = new ArrayList<LSTMMfccContext>();
		Random rand = new Random();
		
		MfccCollection collection = new MfccCollection();
		collection.loadCollectionFromFile(mfccPath);
		collection.setFrameCountOnLeft(frameCountOnLeft);
		collection.setFrameCountOnRight(frameCountOnRight);
		
		TextGridAligner aligner = new TextGridAligner();
		ArrayList<Pair<TextGridInterval, TextGridInterval>> alignedInterval = 
				aligner.getAlignedIntervalFromFiles(refPath, cmpPath);
		if(alignedInterval == null){
			return res; 
		}
		
		//We know get the root of the ref (jtrans) to be able to get the phon informations
		Parser parser = new Parser();
		TextGridRoot rootRef=null;
		TextGridRoot rootCmp = null;
		try{
			rootRef = parser.parseFile(refPath);
			rootCmp = parser.parseFile(cmpPath);
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("Unable to get the phon list. Abort");
			return res;
		}
		
		int n=0;
		LSTMMfccContext temp = null;
		System.out.println("Alignment finished");
		for(Pair<TextGridInterval, TextGridInterval> p : alignedInterval){
			if(p.left.getBoundRelation() != BoundRelation.NONE ||  p.right.getBoundRelation() != BoundRelation.NONE){
				continue;
			} //We have non aligned matching intervals
			
			if((Math.abs(p.left.getXmin() - p.right.getXmin()) <= 0.02) //If we have less than 2 frame of distance
					&& !timestampSet.contains(p.left.getXmin())){ //And if the time is not already record
				TextGridInterval left = getLeftPhon(rootRef, p.left.getXmin());
				TextGridInterval right = getRightPhon(rootRef, p.left.getXmin());
				if(left != null && right != null){
					//temp = new LSTMMfccContext(collection, round((p.left.getXmin() + p.right.getXmin())/2, 2), p.left.getText(), left.getText(), right.getText(), 5, 0);
					temp = new LSTMMfccContext(collection, p.left.getXmin(), p.left.getText(), left.getText(), right.getText(), 5, 0);
					if(temp.isValid()){
						temp.setBegin(true);
						temp.setReferenceHashcode(p.left.hashCode());
						trueContextList.add(temp);
						addExclusionZone(timestampSet, temp.getFrameTime());
						++n;
					}
				}
			}
			if((Math.abs(p.left.getXmax() - p.right.getXmax()) <= 0.02) //If we have less than 2 frame of distance
					&& !timestampSet.contains(p.left.getXmax())){ //And if the time is not already record
				TextGridInterval left = getLeftPhon(rootRef, p.left.getXmax());
				TextGridInterval right = getRightPhon(rootRef, p.left.getXmax());
				if(left != null && right != null){
					//temp = new LSTMMfccContext(collection, round((p.left.getXmax() + p.right.getXmax())/2, 2), p.left.getText(), left.getText(), right.getText(), 5, 0);
					temp = new LSTMMfccContext(collection, p.left.getXmax(), p.left.getText(), left.getText(), right.getText(), 5, 0);
					if(temp.isValid()){
						temp.setBegin(false);
						temp.setReferenceHashcode(p.left.hashCode());
						trueContextList.add(temp);
						addExclusionZone(timestampSet, temp.getFrameTime());
						++n;
					}
				}
			}
		}
		
		System.out.println("generate wrong example");
		for (LSTMMfccContext c : trueContextList){
			int shift = 0;
			int goodFrameShift =0;
			do{
				double time = c.getFrameTime();
				switch(shift){
				case 0:
					time -= 0.05;
					goodFrameShift = -5;
					break;
				case 1:
					time -= 0.04;
					goodFrameShift = -4;
					break;
				case 2:
					time -= 0.03;
					goodFrameShift = -3;
					break;
				case 3:
					time -= 0.02;
					goodFrameShift = -2;
					break;
				case 4:
					time -= 0.01;
					goodFrameShift = -1;
					break;
				case 5:
					time += 0.01;
					goodFrameShift = 1;
					break;
				case 6:
					time += 0.02;
					goodFrameShift = 2;
					break;
				case 7:
					time += 0.03;
					goodFrameShift = 3;
					break;
				case 8:
					time += 0.04;
					goodFrameShift = 4;
					break;
				case 9:
					time += 0.05;
					goodFrameShift = 5;
					break;
				case 10:
					time += 0.10;
					goodFrameShift = -10;
					break;
				case 11:
					time += 0.10;
					goodFrameShift = -10;
					break;
				}
				time = round(time, 2);
				if(!timestampSet.contains(time)){//Now we check if the time exist or not
					temp = new LSTMMfccContext(collection, time, c.getText(), c.getLeftPhon(), c.getRightPhon(), 5 - goodFrameShift, 0);
					if(temp.isValid()){
						temp.setBegin(c.getBegin());
						temp.setReferenceHashcode(c.getReferenceHashcode());
						falseContextList.add(temp);
						timestampSet.add(time);
						++n;
					}
				}
				
				++shift;
				shift %= 12;
			}while(shift != 0);
		}
		System.out.println("Generate " + n + " context");
		
		res.addAll(trueContextList);
		res.addAll(falseContextList);
		return res;
	}

	public static void main(String args[]){
		String refDirectoryPath = null;
		String cmpDirectoryPath = null;
		String mfccDirectoryPath = null;
		String resultPathMeta = null;
		String resultPathMfccDict = null;
		
		int frameCountOnRight = 5;
		int frameCountOnLeft = 5;
		if(args.length == 0){
			refDirectoryPath = "/home/gserrier/aligned_jtrans";
			cmpDirectoryPath = "/home/gserrier/aligned_astali";
			mfccDirectoryPath = "/home/gserrier/mfcc";
			resultPathMeta = "/home/gserrier/sample_allWrong_meta.fc";
			resultPathMfccDict = "/home/gserrier/sample_allWrong_dict.fc";
		} else if (args.length == 6){
			refDirectoryPath = args[0];
			cmpDirectoryPath = args[1];
			mfccDirectoryPath = args[2];
			resultPathMeta = args[3];
			resultPathMfccDict = args[4];
			frameCountOnLeft = Integer.parseInt(args[5]);
			frameCountOnRight = Integer.parseInt(args[6]);
		} else {
			System.out.println("Wrong amount of arguments. Abort execution");
			
		}
		HashSet<Integer> knownHashCode = new HashSet<Integer>();
		
		try {
			BufferedWriter bufferMeta = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(resultPathMeta))));
			BufferedWriter bufferDict = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(resultPathMfccDict))));
			String header = "" + frameCountOnLeft + ";" + frameCountOnRight + ";39;2;" + MfccContext.getPhonCount() + "\n";
			bufferMeta.write(header);
			//We need to match the file between themselves
			File refDirecotryFile = new File(refDirectoryPath);
			File cmpDirectoryFile = new File(cmpDirectoryPath);
			for(File fileEntryRef : refDirecotryFile.listFiles()){
				if(fileEntryRef.getAbsolutePath().endsWith(".textgrid")){
					String refName = fileEntryRef.getName().toLowerCase();
					
					//Now we need to find the corresponding file if exists
					for(File fileEntryCmp : cmpDirectoryFile.listFiles()){
						String cmpName = fileEntryCmp.getName().toLowerCase();
						if(refName.equals(cmpName)){
							
							String mfccPath = mfccDirectoryPath + "/" + fileEntryRef.getName().replace(".textgrid", ".mfcc");
							System.out.println(fileEntryRef.getAbsolutePath() +"   " + fileEntryCmp.getAbsolutePath() +"   " + mfccPath);
							ArrayList<LSTMMfccContext> contextList = generateContextList(fileEntryRef.getAbsolutePath(),
									fileEntryCmp.getAbsolutePath(), mfccPath, frameCountOnLeft, frameCountOnRight);
							for (LSTMMfccContext c : contextList){
								String dump = c.dumpRestrictedMfccFrame(knownHashCode);
								if(dump != null){
									bufferDict.write(dump);
									bufferDict.newLine();
								}
								
								bufferMeta.write(c.getMetaInfo());
								bufferMeta.newLine();
							}
						}
					}
				}
			}
			
			bufferMeta.close();
			bufferDict.close();
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
