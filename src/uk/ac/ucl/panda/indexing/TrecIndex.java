package uk.ac.ucl.panda.indexing;

import uk.ac.ucl.panda.utility.structure.Document;
import uk.ac.ucl.panda.indexing.io.TrecDoc;
import uk.ac.ucl.panda.indexing.io.IndexWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;


import uk.ac.ucl.panda.utility.analyzer.PorterStemAnalyzer;
import uk.ac.ucl.panda.utility.io.Config;
import uk.ac.ucl.panda.utility.parser.HTMLParser;

public class TrecIndex {

	/**
	 * @param args
	 */
	private static final String newline = System.getProperty("line.separator");
	private String field = "body";
        
        
	public void pocess(String index, String data,Properties appProp) throws Exception {
		
		File indexDir = new File(index);
		File dataDir = new File(data);
		if(!indexDir.exists() || !indexDir.isDirectory()){
			throw new IOException(indexDir + " does not exist or is not a directory");
			
		}
		
                
		Document doc =new Document();

		
		TrecDoc docMaker = new TrecDoc(data);
	     

	     appProp.setProperty("doc.maker.forever", "false");
    
	     Config config = new Config(appProp);
	     docMaker.setConfig(config); 
	     HTMLParser htmlParser = (HTMLParser) Class.forName(config.get("html.parser","uk.ac.ucl.panda.applications.demo.DemoHTMLParser")).newInstance();
	     docMaker.setHTMLParser(htmlParser);
	     
	     IndexWriter writer = new IndexWriter(indexDir, 
					new PorterStemAnalyzer(), true);
			writer.setUseCompoundFile(false);
			
		 long start = new Date().getTime();
	     while ((doc = docMaker.makeDocument()) != null) { 
	    	 writer.addDocument(doc);// add Document to index		
	     }

		
		
		int numIndexed = writer.docCount();
		writer.optimize();
		writer.close();
                long end = new Date().getTime();
		System.out.println("Indexing " + numIndexed + " files took " + (end - start)+ " milliseconds");
                ExtraInformation EI= new ExtraInformation(index, field);
                EI.addExtraInformation();
                
                
	}
	


	

}
