package uk.ac.ucl.panda.indexing.io;

import uk.ac.ucl.panda.indexing.*;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import uk.ac.ucl.panda.utility.io.Config;
import uk.ac.ucl.panda.utility.io.NoMoreDataException;
import uk.ac.ucl.panda.utility.parser.HTMLParser;
import uk.ac.ucl.panda.utility.io.FileReader;
import uk.ac.ucl.panda.utility.structure.DocData;

/**
 * A DocMaker using the (compressed) Trec collection for its input.
 * <p>
 * Config properties:<ul>
 * <li>work.dir=&lt;path to the root of docs and indexes dirs| Default: work&gt;</li>
 * <li>docs.dir=&lt;path to the docs dir| Default: trec&gt;</li>
 * </ul>
 */
public class TrecDoc extends BasicDocMaker {

  private static final String newline = System.getProperty("line.separator");
  
  private ThreadLocal dateFormat = new ThreadLocal();
  private File dataDir = null;
  private ArrayList inputFiles = new ArrayList();
  private int nextFile = 0;
  private int iteration=0;
  private BufferedReader reader;
  
  private static final String DATE_FORMATS [] = {
    "EEE, dd MMM yyyy kk:mm:ss z", //Tue, 09 Dec 2003 22:39:08 GMT
    "EEE MMM dd kk:mm:ss yyyy z",  //Tue Dec 09 16:45:08 2003 EST
    "EEE, dd-MMM-':'y kk:mm:ss z", //Tue, 09 Dec 2003 22:39:08 GMT
    "EEE, dd-MMM-yyy kk:mm:ss z", //Tue, 09 Dec 2003 22:39:08 GMT
  };

    public TrecDoc(String data) {
        dataDir = new File(data);
    }
  
  /* (non-Javadoc)
   * @see SimpleDocMaker#setConfig(java.util.Properties)
   */
  public void setConfig(Config config) {
    super.setConfig(config);
  
    collectFiles(dataDir,inputFiles);
    if (inputFiles.size()==0) {
      throw new RuntimeException("No txt files in dataDir: "+dataDir.getAbsolutePath());
    }
 }

  private void openNextFile() throws NoMoreDataException, Exception {
    closeInputs();
    int retries = 0;
    while (true) {
      File f = null;
      synchronized (this) {
        if (nextFile >= inputFiles.size()) { 
          // exhausted files, start a new round, unless forever set to false.
          if (!forever) {
        	  //////////////
        	  return;
        	  //throw new NoMoreDataException();
          }
          nextFile = 0;
          iteration++;
        }
        f = (File) inputFiles.get(nextFile++);
      }
      System.out.println("opening: "+f+" length: "+f.length());
      try {
   
        reader = FileReader.openFileReader(f);
        return;
      } catch (Exception e) {
        retries++;
        if (retries<20) {
          System.out.println("Skipping 'bad' file "+f.getAbsolutePath()+"  #retries="+retries);
          continue;
        } else {
        	//////////////////
        	return;
        	//throw new NoMoreDataException();
        }
      }
    }
  }

  private void closeInputs() {
   
    if (reader!=null) { 
      try {
        reader.close();
      } catch (IOException e) {
        System.out.println("closeInputs(): Ingnoring error: "+e);
        e.printStackTrace();
      }
      reader = null;
    }
  }
  
  // read until finding a line that starts with the specified prefix
  private StringBuffer read (String prefix, StringBuffer sb, boolean collectMatchLine, boolean collectAll) throws Exception {
    sb = (sb==null ? new StringBuffer() : sb);
    String sep = "";
    while (true) {
      String line = reader.readLine();
      if (line==null) {
        openNextFile();
        if(reader==null)
        	return null;
        continue;
      }
      line = line.toUpperCase();
      if (line.trim().startsWith(prefix)) {
        if (collectMatchLine) {
          sb.append(sep+line);
          sep = newline;
        }
        break;
      }
      if (collectAll) {
        sb.append(sep+line);
        sep = newline;
      }
    }
    //System.out.println("read: "+sb);
    return sb;
  }
  
  protected synchronized DocData getNextDocData() throws NoMoreDataException, Exception {
    if (reader==null) {
      openNextFile();
    }
    ///////////////////
    if(reader ==null)return null;
    /////////////////////
    // 1. skip until doc start
    StringBuffer sb = read("<DOC>",null,false,false); 
    if(sb==null)return null;
    // 2. name
    sb = read("<DOCNO>",null,true,false);
    String name = sb.substring("<DOCNO>".length());
    name = name.substring(0,name.indexOf("</DOCNO>")).trim();
/*
    // 3. read doc date
    sb = read("<date",null,true,false); 
    // 4. date
    
    String dateStr = sb.substring(sb.indexOf(">")+1);
    if(dateStr.indexOf("<") != -1)
    	dateStr = dateStr.substring(0,dateStr.indexOf("<")).trim();
    
   // System.out.println("hhh:"+dateStr);	
    // 6. collect until end of doc
    sb = read("</doc>",null,false,true);
    // this is the next document, so parse it 
    Date date = parseDate(dateStr);
 */
    // 6. collect until end of doc
    sb = read("</DOC>",null,false,true);
    // this is the next document, so parse it 
    Date date = new Date();
    HTMLParser p = getHtmlParser();
    DocData docData = p.parse(name, date, sb, getDateFormat(0));
    addBytes(sb.length()); // count char length of parsed html text (larger than the plain doc body text). 
    
    return docData;
  }

  private DateFormat getDateFormat(int n) {
    DateFormat df[] = (DateFormat[]) dateFormat.get();
    if (df == null) {
      df = new SimpleDateFormat[DATE_FORMATS.length];
      for (int i = 0; i < df.length; i++) {
        df[i] = new SimpleDateFormat(DATE_FORMATS[i],Locale.US);
        df[i].setLenient(true);
      }
      dateFormat.set(df);
    }
    return df[n];
  }

  private Date parseDate(String dateStr) {
    Date date = null;
    for (int i=0; i<DATE_FORMATS.length; i++) {
      try {
        date = getDateFormat(i).parse(dateStr.trim());
        return date;
      } catch (ParseException e) {
      }
    }
    // do not fail test just because a date could not be parsed
    System.out.println("ignoring date parse exception (assigning 'now') for: "+dateStr);
    date = new Date(); // now 
    return date;
  }


  /*
   *  (non-Javadoc)
   * @see DocMaker#resetIinputs()
   */
  public synchronized void resetInputs() {
    super.resetInputs();
    closeInputs();
    nextFile = 0;
    iteration = 0;
  }

  /*
   *  (non-Javadoc)
   * @see DocMaker#numUniqueTexts()
   */
  public int numUniqueTexts() {
    return inputFiles.size();
  }



  
  
}
