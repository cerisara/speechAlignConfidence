package mfcc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;

public class MfccCollection {
	private ArrayList<MfccFrame> mfccFrameList;
	private String filename;
	private int frameCountOnLeft;
	private int frameCountOnRight;
	
	public MfccCollection(){
		mfccFrameList = new ArrayList<MfccFrame>();
		filename = null;
		
		frameCountOnLeft = 5;
		frameCountOnRight = 5;
	}
	
	public boolean loadCollectionFromFile(String path){
		try{
			File f = new File(path);
			filename = f.getName().substring(0, f.getName().lastIndexOf('.'));
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
			
			String line=null;
			int numero=1; //The first frame is the frame 1
			while((line = br.readLine()) != null){
				mfccFrameList.add(new MfccFrame(line, numero, filename));
				++numero;
			}
			
			br.close();
			return true;
		} catch(Exception e) {
			System.out.println("Something went wrong when loading the file " + path);
			return false;
		}
	}
	
	//Function made to test if the file is correctly loaded
	public boolean dumpToFile(String path){
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(path))));
			
			for (int i=0; i < mfccFrameList.size(); ++i){
				bw.write(mfccFrameList.get(i).toString());
				bw.newLine();
			}
			bw.close();
		
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public MfccFrame getMfccFrameFromTime(double time){
		int frameNumero = timeToMfccFrameNumero(time);
		if(frameNumero < 0 || frameNumero >= mfccFrameList.size())
			return null;
		else
			return mfccFrameList.get(frameNumero);
	}
	
	public MfccFrame getMfccFrameFromNumero(int numero){
		int index = numero -1; //Numero starts at 1 and index at 0
		if(index < 0 || index >= mfccFrameList.size())
			return null;
		else
			return mfccFrameList.get(index);
	}
	
	//We make one function to centralized the time conversion
	public static int timeToMfccFrameNumero(double time){
		return (int) Math.round(time * 100)+1; //A frame is 10ms long
	}
	
	public static double mfccFrameNumeroToTime(int numero){
		return ((double)numero-1) / 100.;
	}
	
	public int getMfccFrameCount(){
		return mfccFrameList.size();
	}
	
	public String getFilename(){
		return filename;
	}
	
	public void setFrameCountOnLeft(int frameCount){
		frameCountOnLeft = frameCount;
	}
	
	public int getFrameCountOnLeft(){
		return frameCountOnLeft;
	}
	
	public void setFrameCountOnRight(int frameCount){
		frameCountOnRight = frameCount;
	}
	
	public int getFrameCountonRight(){
		return frameCountOnRight;
	}
	
	public static void main(String args[]){
		MfccCollection c = new MfccCollection();
		c.loadCollectionFromFile("/home/gserrier/mfcc/Acc_del_07.mfcc");
		HashSet<Integer> known = new HashSet<Integer>();
		
		LSTMMfccContext context = new LSTMMfccContext(c, 0.24, "e", "e", "e", 5, 0);
		String dump = context.dumpRestrictedMfccFrame(known);
		String b = context.dumpRestrictedMfccFrame(known);
		if(b != null)
			dump += "\n" + b;
		System.out.println(dump);
	}
}
