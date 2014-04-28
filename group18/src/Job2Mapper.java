

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

public class Job2Mapper extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text>{
	@Override
    public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
       
    	//Read the output from Job1, find the first and second tab for 
    	int firsttab = value.find("\t");
        int secondtab = value.find("\t", firsttab+1);

        String nodeID = Text.decode(value.getBytes(), 0, firsttab);
        String pagerank = Text.decode(value.getBytes(), 0, secondtab+1);
        

        output.collect(new Text(nodeID), new Text("!"));

        //Stop when met dead end link.
        if(secondtab == -1){
        	return;
        }
        
        String edges = Text.decode(value.getBytes(), secondtab+1, value.getLength()-(secondtab+1));
        String[] othernodes = edges.split(" ");
        
        
        int sumedges = othernodes.length;
        
        
        for (String othernode : othernodes){
            Text pageRankSumEdges = new Text(pagerank + sumedges);
            output.collect(new Text(othernode), pageRankSumEdges);
        }
        
        // Output fromNode, current pagerank and othernodes that the node links to.
        output.collect(new Text(nodeID), new Text("|"+edges));
    }
}
