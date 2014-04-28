


import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

public class Job2Reducer extends MapReduceBase implements Reducer<Text, Text, Text, Text> {

    private static final float dampingfactor = 0.85F;
    
    public void reduce(Text page, Iterator<Text> values, OutputCollector<Text, Text> out, Reporter reporter) throws IOException {
        boolean isExistingNode = false;
        String[] split;
        float sumDonatedToOtherPageRanks = 0;
        String links = "";
        String pagerank;
        
        // For each otherPage: 
        // - check control characters
        // - calculate pageRank share <rank> / count(<links>)
        // - add the share to sumShareOtherPageRanks
        
        //For all the other nodes, Check control characters see if there is any more, update the value of the pageRank that donated to the node it links to and add it up to the SumPageRanks.
        while(values.hasNext()){
            pagerank = values.next().toString();
            
            if(pagerank.equals("!")) {
                isExistingNode = true;
                continue;
            }
            
            if(pagerank.startsWith("|")){
                links = "\t"+pagerank.substring(1);
                continue;
            }

            split = pagerank.split("\\t");
           
            
            float pageRank = Float.valueOf(split[1]);
            int countOutLinks = Integer.valueOf(split[2]);
            
            sumDonatedToOtherPageRanks += (pageRank/countOutLinks);
        }

        if(!isExistingNode) return;
        float newRank = dampingfactor * sumDonatedToOtherPageRanks + (1-dampingfactor);
        System.out.println(newRank);

        out.collect(page, new Text(newRank + links));
    }
}
