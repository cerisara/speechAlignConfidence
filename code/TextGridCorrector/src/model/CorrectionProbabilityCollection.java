package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import model.CorrectionProbabilities.Bound;

public class CorrectionProbabilityCollection {
	private ArrayList<CorrectionProbabilities> correctionList;
	private int trameCountOnRight;
	private int trameCountOnLeft;
	
	public CorrectionProbabilityCollection(){
		correctionList = new ArrayList<CorrectionProbabilities>();
	}
	
	public boolean loadFile(String path){
		try{
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path))));
			String line = br.readLine();
			
			String header[] = line.split(";");
			this.trameCountOnLeft = Integer.parseInt(header[5]);
			this.trameCountOnRight = Integer.parseInt(header[6]);
			
			while((line = br.readLine()) != null){
				//We are reading the header
				String[] sa = line.split(";");
				
				CorrectionProbabilities cp = new CorrectionProbabilities(Integer.parseInt(sa[1]), sa[2].equals("begin")?Bound.BEGIN:Bound.END,
						trameCountOnLeft, trameCountOnRight);
				line = br.readLine();
				sa = line.split(";");
				if(sa.length == 2){
					double prob = Double.parseDouble(sa[0]);
					cp.setProbability(0, prob);
					for(int i = 1; i < (trameCountOnLeft + trameCountOnRight + 1); ++i){
						line = br.readLine();
						prob = Double.parseDouble(line.split(";")[0]);
						cp.setProbability(i, prob);
					}
				} else {
					double prob = Double.parseDouble(sa[0]);
					cp.setProbability(0, prob);
					for(int i = 1; i < (trameCountOnLeft + trameCountOnRight + 1); ++i){
						prob = Double.parseDouble(sa[i]);
						cp.setProbability(i, prob);
					}
				}
				correctionList.add(cp);
			}
			
			br.close();
			
			for(CorrectionProbabilities cp : correctionList){
				cp.processCorrection();
			}
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public CorrectionProbabilities getCorrectionProbabilities(int hashcode, Bound bound){
		for(CorrectionProbabilities cp : correctionList){
			if(cp.getHashcode() == hashcode && cp.getBound() == bound){
				return cp;
			}
		}
		return null;
	}
	
	public int getTrameCount(){
		return this.trameCountOnLeft + this.trameCountOnRight + 1;
	}

}
