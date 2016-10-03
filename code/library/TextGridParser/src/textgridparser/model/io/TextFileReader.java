package textgridparser.model.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class TextFileReader {
	private BufferedReader br;
	private String path;
	private String currentLine;
	
	public TextFileReader(String path) throws Exception 
	{
		this.path = path;
		try{
			br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path))));
		} catch(Exception e) {
			System.out.println("Something went wrong when trying to open the file " + path);
			throw e;
		}
	}
	
	public String getNextLine()
	{
		if( br != null)
		{
			currentLine = null;
			try{
				currentLine = br.readLine();
			} catch (Exception e) {
				System.out.println("Something went wrong when reading the file " + path);
				currentLine = null;
				return null;
			}
			return currentLine;
		}
		return null;
	}
	
	public String getCurrentLine(){
		return currentLine;
	}
}
