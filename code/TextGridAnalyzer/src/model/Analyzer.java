package model;

import java.io.File;
import java.util.ArrayList;

import textgridaligner.model.TextGridAligner;
import textgridaligner.model.TextGridAligner.Pair;

import textgridparser.model.datastructure.TextGridInterval;

public class Analyzer {
	private StatisticsGenerator global_generator;
	private TextGridAligner aligner;
	
	public Analyzer(){
		global_generator = new StatisticsGenerator();
		aligner = new TextGridAligner();
		
	}
	
	public boolean compareAlignement(String ref, String cmp){
		StatisticsGenerator local_generator = new StatisticsGenerator();
		
		ArrayList<Pair<TextGridInterval, TextGridInterval>> alignedInterval = aligner.getAlignedIntervalFromFiles(ref, cmp);
		if(alignedInterval == null){
			System.out.println("Unable to get the alignment");
			return false;
		}
		
		//Now we need to compute stats on every aligned intervals.
		for(Pair<TextGridInterval, TextGridInterval> p : alignedInterval){
			global_generator.addToStatistics(p);
			local_generator.addToStatistics(p);
		}
		
		local_generator.dumpDataToCSV(ref.replace(".textgrid", ".csv"));
		//System.out.println("Generate " + generator.amountOfData() +  " data.");
		return true;
	}
	
	public boolean saveStatistics(String path){
		return global_generator.dumpDataToCSV(path);
	}
	
	public boolean comparedAlignmentInDirectory(String refDirectoryPath, String cmpDirectoryPath){
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
						compareAlignement(fileEntryRef.getAbsolutePath(), fileEntryCmp.getAbsolutePath());
					}
				}
			}
		}
		
		
		return true;
	}
	
	public static void main (String[] args){
		Analyzer a = new Analyzer();
		if(args.length == 0){
			a.comparedAlignmentInDirectory("/home/gserrier/aligned_gold_corrected_jtrans", "/home/gserrier/aligned_gold_astali");
			//a.compareAlignement("/home/gserrier/test_jtrans_LIF.textgrid", "/home/gserrier/test_astali.textgrid");
			a.saveStatistics("/home/gserrier/global_corrected_jtrans.csv");
			//a.saveStatistics("/home/gserrier/test.csv");
		} else {
			a.comparedAlignmentInDirectory(args[0], args[1]);
			a.saveStatistics(args[2]);
		}
	}
}
