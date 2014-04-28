import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Scanner;

//import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

//import org.apache.commons.io.*;

public class Job1Mapper extends MapReduceBase
implements Mapper<LongWritable, Text, Text, Text> {

//private final FloatWritable one = new FloatWritable(1.0);
private Text node = new Text();
private Text values = new Text();

private final int NUM_NODES = 5713449;

@Override
public void map(LongWritable key, Text value,
	OutputCollector<Text, Text> output, Reporter reporter)
	throws IOException {
	String[] components = value.toString().split("\t");
	
	if (components != null && components.length == 2) {
		String fromNode = components[0];
		String toNode = components[1];
		
		node.set(new Text(fromNode));
		
		float initialPageRank = 1f / NUM_NODES;
		
		// Value format:
		// toNode, fromNodeNumOutLinks, initialPageRank
		//values.set(toNode + "\t" + one + "\t" + initialPageRank);
		values.set(toNode);

    	// Set the key and value.
		output.collect(node, values);
    	}
	}
	

	
}
