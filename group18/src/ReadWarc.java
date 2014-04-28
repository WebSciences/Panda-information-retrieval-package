import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.zip.GZIPInputStream;
import edu.cmu.lemurproject.WarcRecord;
import edu.cmu.lemurproject.WarcHTMLResponseRecord;
import java.util.Vector;

public class ReadWarc {
	  public static void read(String inputWarcFile) throws IOException {

		    // open our gzip input stream
		    GZIPInputStream gzInputStream=new GZIPInputStream(new FileInputStream(inputWarcFile));
		    
		    // cast to a data input stream
		    DataInputStream inStream=new DataInputStream(gzInputStream);
		    
		    PrintWriter writer = new PrintWriter("links-" + Start.fileId + ".txt", "UTF-8");
		    
		    long numTargets = 0;
		    long numNodes = 0;
		    
		    
		    // iterate through our stream
		    WarcRecord thisWarcRecord;
		    while ((thisWarcRecord=WarcRecord.readNextWarcRecord(inStream))!=null) {
		      // see if it's a response record
		      if (thisWarcRecord.getHeaderRecordType().equals("response")) {
		        // it is - create a WarcHTML record
		        WarcHTMLResponseRecord htmlRecord=new WarcHTMLResponseRecord(thisWarcRecord);
		        // get our TREC ID and target URI
		        String thisTRECID=htmlRecord.getTargetTrecID();
		        String thisTargetURI=htmlRecord.getTargetURI();
		        thisTargetURI = WarcHTMLResponseRecord.removePath(thisTargetURI);
		        
		        Vector<String> outlinks = htmlRecord.getURLOutlinks();
		        
		        numTargets++;
		        
		        // print our data
		        for (String outlink : outlinks) {
		        	// Remove links to self (same domain).
		        	if (!thisTargetURI.equals(outlink)) {
		        		writer.println(thisTargetURI + "\t" + outlink);
		        		numNodes++;
		        	}       	
		        }
		        //System.out.println(thisTargetURI + " : " + outlinks);
		      }
		    }
		    
		    System.out.println("Finished writing to file.");
		    System.out.println("Number of targets:");
		    System.out.println(numTargets);
		    System.out.println("Number of nodes:");
		    System.out.println(numNodes);
		    writer.close();
		    inStream.close();
		  }
}
