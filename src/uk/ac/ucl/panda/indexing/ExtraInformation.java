/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.ucl.panda.indexing;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.HashMap;
import uk.ac.ucl.panda.indexing.io.IndexReader;
import uk.ac.ucl.panda.utility.io.CorruptIndexException;
import uk.ac.ucl.panda.utility.io.FileReader;
import uk.ac.ucl.panda.utility.structure.Document;
import uk.ac.ucl.panda.utility.structure.Normalise;
import uk.ac.ucl.panda.utility.structure.Term;
import uk.ac.ucl.panda.utility.structure.TermDocs;
import uk.ac.ucl.panda.utility.structure.TermEnum;

/**
 *
 * @author xxms
 */
public class ExtraInformation {

    public static double ReadCL(String index) throws IOException {
        double avgDL=0;
        double CLength = 0;
    BufferedReader extra = FileReader.openFileReader(new File(index+"ExtraInformation"));
    avgDL=Double.parseDouble(extra.readLine());
    CLength=Double.parseDouble(extra.readLine());
    return CLength;
    }

     private double tf;
  private double df;
  private double DL;
  private int DocNum;

 private int doc;

  private final int[] docs = new int[32];         // buffered doc numbers
  private final int[] freqs = new int[32];        // buffered term freqs
  private int pointer;
  private int pointerMax;

  private IndexReader reader;
  private TermDocs termDocs;
  private String field;
  private double avgDL;
  private double CL;
  private String indexDir;
  protected static String newline = System.getProperty("line.separator");

  protected static String fileseparator = System.getProperty("file.separator");
    private TermEnum termEum;
    private Term term;
  
  
  public ExtraInformation(String index, String field) throws IOException{
    this.reader=IndexReader.open(index);
    this.indexDir = index;
    this.field = field;
 
  }
  
  public int docFreq(Term term) throws IOException {
    return reader.docFreq(term);
  }

/*
  public static HashMap ReadCTF(String index) throws IOException, ClassNotFoundException {
      
         FileInputStream stream = new FileInputStream(new File(index+"ExtendedCTF"));
            ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = stream.read(b)) != -1)
                out.write(b, 0, n);
            stream.close();
            b = out.toByteArray();
        //    System.out.println(b.length);
            ByteArrayInputStream bi = new ByteArrayInputStream(b);
            ObjectInputStream oi = new ObjectInputStream(bi);

            out.close();


        return (HashMap) oi.readObject();
  }
*/


  /*
  public boolean AddCTF() throws FileNotFoundException{

     termEum = reader.terms();
    while(termEum.next()){



     term = termEum.term();


     termDocs = reader.termDocs(term);



        if (termDocs == null){
            return false;
        }

        next();

      tf=0.0d;
    while (doc < Integer.MAX_VALUE) {                           // for docs in window
      int f = freqs[pointer];


       tf+=f*1.0d;





      if (++pointer >= pointerMax) {
        pointerMax = termDocs.read(docs, freqs);  // refill buffers
        if (pointerMax != 0) {
          pointer = 0;
        } else {
          termDocs.close();                       // close stream
          doc = Integer.MAX_VALUE;                // set to sentinel value
          break;
        }
      }
      doc = docs[pointer];
    }

     // System.out.println(term.text()+"  "+ tf);
  //    CTF.put(term.text(), tf);




 }
       termEum.close();

     return true;


  }

*/

  public boolean AddTFcollection() throws IOException {
     HashMap CTF = new HashMap();

     termEum = reader.terms();
    while(termEum.next()){

     
     
     term = termEum.term();


     termDocs = reader.termDocs(term);



        if (termDocs == null){
            return false;
        }
      
        next();

      tf=0.0d;
    while (doc < Integer.MAX_VALUE) {                           // for docs in window
      int f = freqs[pointer];
    
      
       tf+=f*1.0d;





      if (++pointer >= pointerMax) {
        pointerMax = termDocs.read(docs, freqs);  // refill buffers
        if (pointerMax != 0) {
          pointer = 0;
        } else {
          termDocs.close();                       // close stream
          doc = Integer.MAX_VALUE;                // set to sentinel value
          break;
        }
      }
      doc = docs[pointer];
    }

     // System.out.println(term.text()+"  "+ tf);
      CTF.put(term.text(), tf);


 

 }
       termEum.close();
             FileOutputStream result =new FileOutputStream(new File(indexDir+"ExtendedCTF"));

       
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ObjectOutputStream oo = new ObjectOutputStream(bo);
        oo.writeObject(CTF);
        result.write(bo.toByteArray());
             result.close();
     return true;
  }


      /** Advances to the next document matching the query.
   * <br>The iterator over the matching documents is buffered using
   * {@link TermDocs#read(int[],int[])}.
   * @return true iff there is another document matching the query.
   */
  public boolean next() throws IOException {
    pointer++;
    if (pointer >= pointerMax) {
      pointerMax = termDocs.read(docs, freqs);    // refill buffer
      if (pointerMax != 0) {
        pointer = 0;
      } else {
        termDocs.close();                         // close stream
        doc = Integer.MAX_VALUE;                  // set to sentinel value
        return false;
      }
    }
    doc = docs[pointer];
    return true;
  }




  public Document doc(int i) throws CorruptIndexException, IOException {
    return reader.document(i);
  }
  
  public double totalWords() throws CorruptIndexException, IOException{
    byte[] fieldNorms = reader.norms(field);
    double collectionLength = 0;
      for (int i=0; i<fieldNorms.length; i++) {
       collectionLength += 1.0d * Normalise.decodeNorm(fieldNorms[i]);
       //System.out.println(field);
	  }
          CL = collectionLength;
	  avgDL =collectionLength / fieldNorms.length * 1.0d;
    
      
      
      
    return collectionLength;
  }  
   
  public double getAvgDL(){
    return avgDL;
  }

  public void addExtraInformation() throws CorruptIndexException, IOException {
     
     
        double collectionLength = totalWords();
        
        FileOutputStream result =new FileOutputStream(new File(indexDir+"ExtraInformation"));
        PrintWriter logger = new PrintWriter(result,true);  
        logger.println(avgDL);
        logger.println(collectionLength);
        logger.close();

    //     AddCTF();
  
    }
    
  public static double ReadAvgDL(String index) throws FileNotFoundException, IOException{
    double avgDL=0;
    BufferedReader extra = FileReader.openFileReader(new File(index+"ExtraInformation"));
    avgDL=Double.parseDouble(extra.readLine());
    extra.close();
    return avgDL;
  }
  
  
}
