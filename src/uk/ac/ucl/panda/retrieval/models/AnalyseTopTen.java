package uk.ac.ucl.panda.retrieval.models;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ucl.panda.GetDocTermStats;


public class AnalyseTopTen {
	public static void main(String[] args) throws ClassNotFoundException {
		String PANDA_VAR  = System.getProperty("panda.var", "./var/");
		BufferedReader br = null;
		int numLines = 10;
		try {
 
			String sCurrentLine;
			Pattern p = Pattern.compile("\\s+(\\w{4,}-\\d{4,})\\s+");
			System.out.println("   DocID     foreign  minor   germani");
			br = new BufferedReader(new FileReader(PANDA_VAR + "//results"));
			int line=0;
			while ((sCurrentLine = br.readLine()) != null) {
				if (line>=numLines){
					break;
				}
				//System.out.println(sCurrentLine);
				Matcher m = p.matcher(sCurrentLine) ; 
				while (m.find()) {
				    System.out.printf(m.group().replaceAll("\\s", "")+"\t");
				    GetDocTermStats gtdcs = new GetDocTermStats();
				    HashMap<String, Integer> tmsts = gtdcs.GetDocLevelStats(m.group());
				    Iterator<String> iterator = tmsts.keySet().iterator(); 
				    System.out.printf(tmsts.get("foreign")+"\t");
				    System.out.printf(tmsts.get("minor")+"\t");
				    System.out.printf(tmsts.get("germani")+"\n");
				    
				}
				line++;
			}
 
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
 
	}

}
