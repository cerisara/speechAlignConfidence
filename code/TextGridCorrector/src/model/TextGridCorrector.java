package model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import mfcc.MfccCollection;
import model.CorrectionProbabilities.Bound;
import textgridaligner.model.TextGridAligner;
import textgridaligner.model.TextGridAligner.Pair;
import textgridparser.model.Parser;
import textgridparser.model.datastructure.TextGridInterval;
import textgridparser.model.datastructure.TextGridItem;
import textgridparser.model.datastructure.TextGridItem.TierLevel;
import textgridparser.model.datastructure.TextGridRoot;

public class TextGridCorrector {
	
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

	//Say yes if the correction is to close from an existing border
	public static boolean isForbiddenBegin(TextGridItem item, double time, int intervalNumero){
		int frameNumero = MfccCollection.timeToMfccFrameNumero(time);
		
		for(int i=0; i < item.getIntervalsCount(); ++i){
			TextGridInterval inter = item.getInterval(i);
			if(intervalNumero != i){
				int distance = Math.abs(MfccCollection.timeToMfccFrameNumero(inter.getXmin()) - frameNumero);
				if(distance < 3)
					return true;
			}
			if(intervalNumero-1 != i){
				int distance = Math.abs(MfccCollection.timeToMfccFrameNumero(inter.getXmax()) - frameNumero);
				if(distance < 3)
					return true;
			}
		}
		
		return false;
	}
	
	public static boolean isForbiddenEnd(TextGridItem item, double time, int intervalNumero){
		int frameNumero = MfccCollection.timeToMfccFrameNumero(time);
		
		for(int i=0; i < item.getIntervalsCount(); ++i){
			TextGridInterval inter = item.getInterval(i);
			if(intervalNumero+1 != i){
				int distance = Math.abs(MfccCollection.timeToMfccFrameNumero(inter.getXmin()) - frameNumero);
				if(distance < 3)
					return true;
			}
			if(intervalNumero != i){
				int distance = Math.abs(MfccCollection.timeToMfccFrameNumero(inter.getXmax()) - frameNumero);
				if(distance < 3)
					return true;
			}
		}
		
		return false;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length == 3){
			Parser p = new Parser();
			CorrectionProbabilityCollection col = new CorrectionProbabilityCollection();
			try{
				TextGridRoot root = p.parseFile(args[0]);
				root.dropTierLevel(TierLevel.NONE);//We drop unwanted tier to avoid to mess up stuff
				root.dropTierLevel(TierLevel.PHONS);
				if(!col.loadFile(args[1])){
					System.out.println("Unable to load the prediction file. Abort");
					System.exit(-1);
				}
				
				for(int i=0; i < root.getItemCount(); ++i){
					TextGridItem item = root.getItem(i);
					for(int j = 0; j < item.getIntervalsCount(); ++j){
						TextGridInterval inter = item.getInterval(j);
						CorrectionProbabilities correction = col.getCorrectionProbabilities(inter.hashCode(), Bound.BEGIN);
						if(correction == null){
							System.out.println("Unable to find a correction for this beginning : " + inter.getText());
						} else {
							int index = 0;
							boolean found = false;
							double newTime = -1;
							do{
								int delta = correction.getFrameCorrection(index);
								if(correction.getProbabilityFromPosition(delta) < 0){
									System.out.println("unable to find a positive new border for begin of : " + inter.getText());
									break;
								}
								newTime = MfccCollection.mfccFrameNumeroToTime(MfccCollection.timeToMfccFrameNumero(inter.getXmin()) - delta);
								found = !isForbiddenBegin(item, newTime, j);
								++index;
							}while(index < col.getTrameCount() && !found); // We suppose that trames are centered on the jtrans trame
							if(found)
								inter.correctXmin(newTime);
							else
								System.out.println("unable to find a new border for begin of : " + inter.getText());
						}
						
						correction = col.getCorrectionProbabilities(inter.hashCode(), Bound.END);
						if(correction == null){
							System.out.println("Unable to find a correction for this ending : " + inter.getText());
						} else {
							int index = 0;
							boolean found = false;
							double newTime = -1;
							do{
								int delta = correction.getFrameCorrection(index);
								if(correction.getProbabilityFromPosition(delta) < 0){
									System.out.println("unable to find a positive new border for end of : " + inter.getText());
									break;
								}
								newTime = MfccCollection.mfccFrameNumeroToTime(MfccCollection.timeToMfccFrameNumero(inter.getXmax()) - delta);
								found = !isForbiddenEnd(item, newTime, j);
								++index;
							}while(index < col.getTrameCount() && !found); // We suppose that trames are centered on the jtrans trame
							if(found)
								inter.correctXmax(newTime);
							else
								System.out.println("unable to find a new border for end of : " + inter.getText());
						}
					}
				}
				
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(args[2]))));
				bw.write(root.toTextGridFormat());
				bw.close();
				
			} catch(Exception e) {
				e.printStackTrace();
			}
		} else if(args.length == 4){// we need to generate a special stat file
			TextGridAligner aligner = new TextGridAligner();
		    ArrayList<Pair<TextGridInterval, TextGridInterval>> aligned = aligner.getAlignedIntervalFromFiles(args[0], args[1]);
		    CorrectionProbabilityCollection col = new CorrectionProbabilityCollection();
		    Parser parser = new Parser();
		    try {
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(args[3]))));
				if(!col.loadFile(args[2])){
					System.out.println("Unable to load the prediction file. Abort");
					System.exit(-1);
				}
				TextGridRoot root = parser.parseFile(args[0]);
				for(Pair<TextGridInterval, TextGridInterval> p : aligned){
					CorrectionProbabilities prob = col.getCorrectionProbabilities(p.left.hashCode(), Bound.BEGIN);
					if(prob != null){
						bw.write("" + p.right.getXmin() + ";" + p.left.getXmin() + ";" + p.left.hashCode() + ";" + 
								getLeftPhon(root, p.left.getXmin()).getText() + ";" + getRightPhon(root, p.left.getXmin()).getText() + "\n");
						bw.write(prob.getPositionString() + "\n");
						bw.write(prob.getProbString() + "\n\n");
					}
					
					/*prob = col.getCorrectionProbabilities(p.left.hashCode(), Bound.END);	
					if(prob != null){
						bw.write("" + p.right.getXmax() + ";" + p.left.getXmax() + ";" + p.left.hashCode() + "\n");
						bw.write(prob.getPositionString() + "\n");
						bw.write(prob.getProbString() + "\n\n");
					}*/
					
				}
				bw.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
