
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;


import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;


public class PageRanker {
    
    private static NumberFormat nf = new DecimalFormat("00");
    
    public static void main(String[] args) throws Exception {
        PageRanker pageRanking = new PageRanker();

        System.out.println("pageRanking");
        
        //int runs = 0;
        for (int runs = 0; runs < 10; runs++) {
            pageRanking.runRankCalculation("pagerank/ranking/iter"+nf.format(runs), "pagerank/ranking/iter"+nf.format(runs + 1));
        }
        
        pageRanking.runRankOrdering("pagerank/ranking/iter10", "pagerank/result/final");
        
    }
    
    private void runRankCalculation(String inputPath, String outputPath) throws IOException {
        JobConf conf = new JobConf(PageRanker.class);
        
        conf.setOutputKeyClass(Text.class);
        conf.setOutputValueClass(Text.class);
        
        conf.setInputFormat(TextInputFormat.class);
        conf.setOutputFormat(TextOutputFormat.class);
        
        FileInputFormat.setInputPaths(conf, new Path(inputPath));
        FileOutputFormat.setOutputPath(conf, new Path(outputPath));
        
        conf.setMapperClass(Job2Mapper.class);
        conf.setReducerClass(Job2Reducer.class);
        
        JobClient.runJob(conf);
    }
    
    private void runRankOrdering(String inputPath, String outputPath) throws IOException {
        JobConf conf = new JobConf(PageRanker.class);
        
        conf.setOutputKeyClass(FloatWritable.class);
        conf.setOutputValueClass(Text.class);
        conf.setInputFormat(TextInputFormat.class);
        conf.setOutputFormat(TextOutputFormat.class);
        
        FileInputFormat.setInputPaths(conf, new Path(inputPath));
        FileOutputFormat.setOutputPath(conf, new Path(outputPath));
        
        conf.setMapperClass(Job3Sorting.class);
        
        JobClient.runJob(conf);
    }
    
}
