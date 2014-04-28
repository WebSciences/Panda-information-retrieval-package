import java.io.IOException;
import java.util.Iterator;

//import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

public class Job1Reducer extends MapReduceBase
    implements Reducer<Text, Text, Text, Text> {

  public void reduce(Text key, Iterator values,
      OutputCollector output, Reporter reporter) throws IOException {

    int sum = 0;
    
    String outputValue = "";
    String toNodeID = "";
    while (values.hasNext()) {

    	Text value = (Text) values.next();
    	
    	toNodeID += value + " ";
    	
//    	String[] components = value.toString().split("\t");
//    	if (components != null && components.length == 3) {
//    		// 3 components = correct.
//    		IntWritable valueForSum = (IntWritable) components[1];
//    		
//    	}
    	
    	
      //sum++;
    }

    // Remove the last whitespace character " ".
    toNodeID = toNodeID.substring(0, toNodeID.length() - 1);
    
    // Add 1.0 as the initial page rank.
    //outputValue = "1.0\t" + toPages;
    
    // After finished going through the valeus
    //float inverse = 1 / sum;

    output.collect(key, new Text("1.0\t" + toNodeID));
  }
}