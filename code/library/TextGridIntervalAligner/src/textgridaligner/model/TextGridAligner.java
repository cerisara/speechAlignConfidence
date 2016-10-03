package textgridaligner.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;

import textgridparser.model.Parser;
import textgridparser.model.datastructure.TextGridInterval;
import textgridparser.model.datastructure.TextGridItem;
import textgridparser.model.datastructure.TextGridItem.TierLevel;
import textgridparser.model.datastructure.TextGridRoot;

public class TextGridAligner {
		private Parser parser;
		private TextGridRoot root1, root2;

		public class Pair<A, B>{
			public A left;
			public B right;
			
			public Pair(A l, B r){
				left = l;
				right = r;
			}
		}
		
		public TextGridAligner(){
			parser = new Parser();
			
			root1 = null;
			root2 = null;
		}
		
		private boolean loadFiles(String path1, String path2){
			try{
				root1 = parser.parseFile(path1);
				root2 = parser.parseFile(path2);
			} catch(Exception e){
				System.out.println("Unable to parse file");
				e.printStackTrace();
				
				return false;
			}
			return true;
		}
		
		public ArrayList<Pair<TextGridInterval, TextGridInterval>> getAlignedIntervalFromFiles(String ref, String cmp){
			if(!loadFiles(ref, cmp)){
				return null;
			}
			
			//We need to make the couple of comparable items
			ArrayList<Pair<TextGridItem, TextGridItem>> coupleList = new ArrayList<Pair<TextGridItem, TextGridItem>>();
			if(!getItemCouple(coupleList)){
				System.out.println("Some item cannot be matched, discard the analysis of these files");
				return null;
			}
			
			//Then, for each couple of item, we found the aligned interval in them
			ArrayList<Pair<TextGridInterval, TextGridInterval>> alignedInterval = new ArrayList<Pair<TextGridInterval, TextGridInterval>>();
			for(int i=0; i < coupleList.size(); ++i){
				alignedInterval.addAll(getAlignedIntervals(coupleList.get(i).left,coupleList.get(i).right));
			}
			
			return alignedInterval;
		}
		
		private boolean getItemCouple(ArrayList<Pair<TextGridItem, TextGridItem>> coupleList){
			//ArrayList<Pair<TextGridItem, TextGridItem>> coupleList = new ArrayList<Pair<TextGridItem, TextGridItem>>();
			boolean allMatch = true;
			for(int i=0; i < root1.getItemCount(); ++i){
				TextGridItem ref = root1.getItem(i);
				if(ref.getTierLevel() != TierLevel.WORD){
					continue;
				}
				
				boolean found = false;
				for(int j = 0; j < root2.getItemCount(); ++j){
					TextGridItem cmp = root2.getItem(j);
					if(ref.getTierLevel() == cmp.getTierLevel() && ref.getSpeaker().equals(cmp.getSpeaker())){
						coupleList.add(new Pair<TextGridItem, TextGridItem>(ref, cmp));
						
						found = true;
						break;
					}
				}
				if(!found){
					//System.out.println("An item couldn't be paired");
					allMatch = false;
				}
			}
			
			return allMatch;
		}
		
		private ArrayList<Pair<TextGridInterval, TextGridInterval>> getAlignedIntervals(TextGridItem itemRef, TextGridItem itemCmp){
			ArrayList<Pair<TextGridInterval, TextGridInterval>> alignedInterval = new ArrayList<Pair<TextGridInterval, TextGridInterval>>();

			SuiteDeMots suiteLeft = new SuiteDeMots(TextGridItemtoArray(itemRef));
			SuiteDeMots suiteRight = new SuiteDeMots(TextGridItemtoArray(itemCmp));
			suiteLeft.align(suiteRight);
			 
			//Now we need to store the found alignment
			for(int j = 0; j < itemRef.getIntervalsCount(); ++j){
				TextGridInterval ref = itemRef.getInterval(j);
				int[] alignedList = suiteLeft.getLinkedWords(j);
				
				if(alignedList.length == 1){
					TextGridInterval found = itemCmp.getInterval(alignedList[0]);
					if(found.getText().equals(ref.getText())){
						alignedInterval.add(new Pair<TextGridInterval, TextGridInterval>(ref, found));
					} else {
						//System.out.println("Cannot align " + ref.getText());
					}
				} else {
					//System.out.println("Cannot align " + ref.getText());
				}
			}
			//System.out.println("Aligned " + alignedInterval.size() + " on " + itemRef.getIntervalsCount() + " elements");
			return alignedInterval;
		}
		
		private String[] TextGridItemtoArray(TextGridItem it){
			String[] res = new String[it.getIntervalsCount()];
			for(int i=0; i < it.getIntervalsCount(); ++i){
				res[i] = it.getInterval(i).getText();
			}
			return res;
		}
		
		public static float getError(ArrayList<Pair<TextGridInterval, TextGridInterval>> pairList){
			float res = 0;
			int i = 0;
			for(Pair<TextGridInterval, TextGridInterval> p : pairList){
				double d = Math.abs(p.left.getXmin() - p.right.getXmin());
				if(d < 1){
					++i;
					res += d;
				}
				d = Math.abs(p.left.getXmax() - p.right.getXmax());
				if( d < 1){
					++i;
					res += d;
				}
			}
			return res/i;
		}
		
		
		public static void main (String[] args){
			TextGridAligner a = new TextGridAligner();
			if(args.length == 2){
				ArrayList<Pair<TextGridInterval, TextGridInterval>> pairList = 
						a.getAlignedIntervalFromFiles(args[0], args[1]);
				System.out.println(getError(pairList));
			} else if(args.length == 3){
				ArrayList<Pair<TextGridInterval, TextGridInterval>> pairList = 
						a.getAlignedIntervalFromFiles(args[0], args[1]);
				try {
					String filename = (new File(args[0])).getName();
					BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(args[2]))));
					for(Pair<TextGridInterval, TextGridInterval> p : pairList){
						bw.write(filename + ";" + p.left.hashCode() + ";begin;" + p.right.getXmin()+ ";" + p.left.getText() + "\n");
						bw.write(filename + ";" + p.left.hashCode() + ";end;" + p.right.getXmax() + ";" + p.left.getText() + "\n");
					}
					bw.close();
				} catch(Exception e){
					
				}
				
			} else {
				ArrayList<Pair<TextGridInterval, TextGridInterval>> pairList = 
						a.getAlignedIntervalFromFiles("/home/gserrier/aligned_gold_jtrans/acc_del_07.textgrid", "/home/gserrier/aligned_gold_astali/acc_del_07.textgrid");
				System.out.println(getError(pairList));
			}
		}
}
