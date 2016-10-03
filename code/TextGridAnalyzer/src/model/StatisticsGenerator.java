package model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import textgridparser.model.datastructure.TextGridInterval;
import textgridparser.model.datastructure.TextGridInterval.BoundRelation;

import textgridaligner.model.TextGridAligner.Pair;

//TODO change paradigm of this.....
public class StatisticsGenerator {
	ArrayList<BoundRelation> relationList;
	ArrayList<String> textList;
	ArrayList<Double> xMinDiff;
	ArrayList<Double> xMaxDiff;
	ArrayList<Double> durationDiff;
	ArrayList<Integer> hashcodeList;
	
	public StatisticsGenerator(){
		xMinDiff = new ArrayList<Double>();
		xMaxDiff = new ArrayList<Double>();
		durationDiff = new ArrayList<Double>();
		relationList = new ArrayList<BoundRelation>();
		textList = new ArrayList<String>();
		hashcodeList = new ArrayList<Integer>();
	}
	
	//WARNING: the first element of the pair should be the jtrans interval
	public boolean addToStatistics(Pair<TextGridInterval, TextGridInterval> p){
		xMinDiff.add(p.left.getXmin() - p.right.getXmin());
		xMaxDiff.add(p.left.getXmax() - p.right.getXmax());
		durationDiff.add(p.left.getDuration() - p.right.getDuration());
		
		relationList.add(p.left.getBoundRelation());
		textList.add(p.left.getText());
		hashcodeList.add(p.left.hashCode());
		return true;
	}
	
	public int amountOfData(){
		return xMinDiff.size();
	}
	
	public boolean dumpDataToCSV(String path){
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(path))));
			
			for(int i = 0; i < xMinDiff.size(); ++i){
				bw.write(xMinDiff.get(i) + ";" + xMaxDiff.get(i) + ";" + durationDiff.get(i) + ";" + relationList.get(i));
				bw.write(";" + textList.get(i) + ";" + hashcodeList.get(i));
				bw.newLine();
			}
			bw.close();
			
			System.out.println("Save " + xMinDiff.size()+ " data to " + path);
		
		} catch (Exception e) {
			System.out.println("Unable to store data in the file " + path);
			e.printStackTrace();
			
			return false;
		}
		
		return true;
	}
}
