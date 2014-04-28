import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;

public class Runner {

  public static void main(String[] args) {
    JobClient client = new JobClient();
    JobConf conf = new JobConf(Runner.class);
    conf.setJobName("runner");

    // specify output types
    conf.setOutputKeyClass(Text.class);
    //conf.setOutputValueClass(IntWritable.class);
    conf.setOutputValueClass(Text.class);
    

    // specify input and output dirs
    
//    FileInputPath.addInputPath(conf, new Path("input"));
//    FileOutputPath.addOutputPath(conf, new Path("output"));

    FileInputFormat.setInputPaths(conf, new Path("input"));
    FileOutputFormat.setOutputPath(conf, new Path("output"));
    
    // specify a mapper
    conf.setMapperClass(Job1Mapper.class);

    // specify a reducer
    conf.setReducerClass(Job1Reducer.class);
    conf.setCombinerClass(Job1Reducer.class);

    client.setConf(conf);
    try {
      JobClient.runJob(conf);
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    // Now do the other mapping and reducing
//    try {
//    	PageRanker.main(null);
//    } catch (Exception e) {
//    	e.printStackTrace();
//    }

    
  }
}