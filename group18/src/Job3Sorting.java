

import java.io.IOException;
import java.nio.charset.CharacterCodingException;

import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

public class Job3Sorting extends MapReduceBase implements Mapper<LongWritable, Text, FloatWritable, Text> {
    
    public void map(LongWritable key, Text value, OutputCollector<FloatWritable, Text> output, Reporter reporter3
    		) throws IOException {
        
    	
    	
    	
    	String[] pageAndRank = getandnodeandpagerank(key, value);
        
        float parseFloat = Float.parseFloat(pageAndRank[1]);
        
        Text node = new Text(pageAndRank[0]);
        FloatWritable rank = new FloatWritable(parseFloat);
        
        output.collect(rank, node);
    }
    
    private String[] getandnodeandpagerank(LongWritable key, Text value) throws CharacterCodingException {
        
    	String[] nodeAndRank = new String[2];
        int tabNodeIndex = value.find("\t");
        int tabRankIndex = value.find("\t", tabNodeIndex + 1);
        
       //no more links
        int end;
      
        if (tabRankIndex == -1) {
            end = value.getLength() - (tabNodeIndex + 1);
        } 
        else {
            end = tabRankIndex - (tabNodeIndex + 1);
        }
        
        nodeAndRank[0] = Text.decode(value.getBytes(), 0, tabNodeIndex);
        nodeAndRank[1] = Text.decode(value.getBytes(), tabNodeIndex + 1, end);
        
        return nodeAndRank;
    }
    
}
