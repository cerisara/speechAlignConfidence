package textgridparser.model;

import textgridparser.model.datastructure.TextGridInterval;
import textgridparser.model.datastructure.TextGridItem;
import textgridparser.model.datastructure.TextGridItem.TierLevel;
import textgridparser.model.datastructure.TextGridRoot;
import textgridparser.model.io.TextFileReader;
import textgridparser.utils.Utils;

public class Parser {
	
	private TextFileReader reader;

	public Parser(){
		reader = null;
	}
	
	public TextGridRoot parseFile(String path) throws Exception
	{
		reader = new TextFileReader(path);
		TextGridRoot res = parseRoot();
		res.setBoundaryInformation();
		return res;
	}
	
	private TextGridRoot parseRoot(){
		TextGridRoot root = new TextGridRoot();
		String line = null;
		boolean hasAlreadySeenItem = false;
		
		while((line = reader.getNextLine()) != null){
			line = Utils.removeWhitespace(line);
			if(line.startsWith("item [")){
				if(hasAlreadySeenItem){//We need to generate the item list
					String currentLine = null;
					do {
						root.addItem(parseItem());
						currentLine = Utils.removeWhitespace(reader.getCurrentLine());
					} while(currentLine != null && currentLine.startsWith("item ["));
					hasAlreadySeenItem = false; //We finish the list so we reset the item list marker
				} else {
					hasAlreadySeenItem = true; //We need to ignore the first one
				}
			} else { //We are parsing root level informations
				root.setDataFromString(line);
			}
		}
		
		return root;
	}
	
	private TextGridItem parseItem(){
		TextGridItem item = new TextGridItem();
		String line = null;
		
		while((line = reader.getNextLine()) != null){
			line = Utils.removeWhitespace(line);
			
			if(line.startsWith("intervals [")){//We start parsing the interval list
				String currentLine = null;
				do {
					item.addInterval(parseInterval());
					currentLine = Utils.removeWhitespace(reader.getCurrentLine());
				} while(currentLine != null && currentLine.startsWith("intervals ["));
				line = currentLine;
			} else { //We are parsing item element
				item.setDataFromString(line);
			}
			//If we encounter the beginning of a new item or the end of the file we break the loop and return
			if(line == null || line.startsWith("item [")){
				break;
			}
		}
		return item;
	}
	
	private TextGridInterval parseInterval(){
		TextGridInterval interval = new TextGridInterval();
		String line = null;
		
		while((line = reader.getNextLine()) != null){
			line = Utils.removeWhitespace(line);
			
			if(line.startsWith("intervals [") || line.startsWith("item [")){//We start a new interval or a new item, so we need to break the loop
				break;
			} else {
				interval.setDataFromString(line);
			}
		}
		return interval;
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		Parser p = new Parser();
		if(args.length == 0){
			TextGridRoot root = p.parseFile("/home/gserrier/aligned_gold_jtrans/acc_del_07.textgrid");
			System.out.println(root.sanityCheck());
			root.dropTierLevel(TierLevel.PHONS);
			root.dropTierLevel(TierLevel.NONE);
			System.out.println(root.toTextGridFormat());
			
		}
		else {
			TextGridRoot root = p.parseFile(args[0]);
			root.dropTierLevel(TierLevel.WORD);
			root.dropTierLevel(TierLevel.NONE);
			System.out.println(root.toTextGridFormat());
		}
	}

}
