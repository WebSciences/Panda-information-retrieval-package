/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ucl.panda.applications.evaluation.trec;

import java.io.File;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import uk.ac.ucl.panda.indexing.io.FilterIndexReader;
import uk.ac.ucl.panda.indexing.io.IndexReader;
import uk.ac.ucl.panda.retrieval.MeanVarianceScoreDoc;
import uk.ac.ucl.panda.retrieval.ScoreDoc;
import uk.ac.ucl.panda.retrieval.Searcher;
import uk.ac.ucl.panda.retrieval.TopDocCollector;
import uk.ac.ucl.panda.retrieval.TopDocs;
import uk.ac.ucl.panda.retrieval.TopDocsMeanVariance;
import uk.ac.ucl.panda.retrieval.TopMeanVarianceDocCollector;
import uk.ac.ucl.panda.retrieval.query.QualityQuery;
import uk.ac.ucl.panda.retrieval.query.Query;
import uk.ac.ucl.panda.utility.io.DocNameExtractor;
import uk.ac.ucl.panda.utility.io.SubmissionReport;
import uk.ac.ucl.panda.utility.parser.QualityQueryParser;
import uk.ac.ucl.panda.utility.structure.Term;
import uk.ac.ucl.panda.utility.structure.TermFreqVector;



/**
 * Main entry point for running a quality benchmark.
 * <p>
 * There are two main configurations for running a quality benchmark: <ul>
 * <li>Against existing judgements.</li>
 * <li>For submission (e.g. for a contest).</li>
 * </ul>
 * The first configuration requires a non null
 * {@link org.apache.lucene.benchmark.quality.Judge Judge}. 
 * The second configuration requires a non null 
 * {@link org.apache.lucene.benchmark.quality.utils.SubmissionReport SubmissionLogger}.
 */
public class QualityBenchmark {

  /** Quality Queries that this quality benchmark would execute. */
  protected QualityQuery qualityQueries[];
  
  /** Parser for turning QualityQueries into Lucene Queries. */
  protected QualityQueryParser qqParser;
  
  /** Index to be searched. */
  protected Searcher searcher;

  /** Index to be indexreader. */
  protected  IndexReader reader;

  /** index field to extract doc name for each search result; used for judging the results. */  
  protected String docNameField;

  protected String docDataField ="body";
  
  /** maximal number of queries that this quality benchmark runs. Default: maxint. Useful for debugging. */
  private int maxQueries = Integer.MAX_VALUE;
  
  /** maximal number of results to collect for each query. Default: 1000. */
  private int maxResults = 1000;

  /**
   * Create a QualityBenchmark.
   * @param qqs quality queries to run.
   * @param qqParser parser for turning QualityQueries into Lucene Queries. 
   * @param searcher index to be searched.
   * @param docNameField name of field containg the document name.
   *        This allows to extract the doc name for search results,
   *        and is important for judging the results.  
   */
  public QualityBenchmark(QualityQuery qqs[], QualityQueryParser qqParser, 
      String index, String docNameField, int modelNumber) throws FileNotFoundException, IOException, ClassNotFoundException {
	  setUpQualityBenchmark(qqs, qqParser, index, docNameField);
	  this.searcher = new Searcher(index, modelNumber);
  }
  
  public QualityBenchmark(QualityQuery qqs[], QualityQueryParser qqParser, 
	      String index, String docNameField) throws FileNotFoundException, IOException, ClassNotFoundException {
	  setUpQualityBenchmark(qqs, qqParser, index, docNameField);
	    this.searcher = new Searcher(index);
	  }
  
  private void setUpQualityBenchmark(QualityQuery qqs[], QualityQueryParser qqParser, 
		  String index, String docNameField) throws FileNotFoundException, IOException, ClassNotFoundException {
	    this.qualityQueries = qqs;
	    this.qqParser = qqParser;
	    this.reader=IndexReader.open(index);
	    this.docNameField = docNameField;
  }

  /**
   * Run the quality benchmark.
   * @param judge the judge that can tell if a certain result doc is relevant for a certain quality query. 
   *        If null, no judgements would be made. Usually null for a submission run. 
   * @param submitRep submission report is created if non null.
   * @param qualityLog If not null, quality run data would be printed for each query.
   * @return QualityStats of each quality query that was executed.
   * @throws Exception if quality benchmark failed to run.
   */
  
  //ucl batch
   public  QualityStats [] execute(Judge judge, SubmissionReport submitRep, 
                                  PrintWriter qualityLog, PrintWriter scorelogger) throws Exception {
          return execute(judge, submitRep,qualityLog, scorelogger, 0,0);

   }

   public  QualityStats [] execute(Judge judge, SubmissionReport submitRep,
                                  PrintWriter qualityLog, PrintWriter scorelogger, double a1, double a2) throws Exception {
    int nQueries = Math.min(maxQueries, qualityQueries.length);
    QualityStats stats[] = new QualityStats[nQueries]; 
    ///////////
  //  System.out.println("Number of queries "+nQueries);
    /////////////
    
    for (int i=0; i<nQueries; i++) {


      QualityQuery qq = qualityQueries[i];
      // generate query
           Query q = qqParser.parse(qq);
      // search with this query 
    long t1 = System.currentTimeMillis();
    System.out.println( "Processing Query " + (i+1));
      TopDocs td = searcher.search(q,null,maxResults, a1);
      /////////////////////////////
      //ucl
       TopDocs f_td = td;
      if(a2!=0){
        f_td = Correlation_Adjust(td, a2);
      }
    
        

 long searchTime = System.currentTimeMillis()-t1;

      outresult(qq, f_td, scorelogger);
/////////////////////////////

      //most likely we either submit or judge, but check both
      if (judge!=null) {
        stats[i] = analyzeQueryResults(qq, q, f_td, judge, qualityLog, searchTime);
      }
      if (submitRep!=null) {
        submitRep.report(qq,f_td,docNameField,searcher);
      }
    }
    if (submitRep!=null) {
      submitRep.flush();
    }

    return stats;
  

   
  }


    public  QualityStats [] execute_var(Judge judge, SubmissionReport submitRep,
                                  PrintWriter qualityLog, PrintWriter scorelogger, double a1, double a2) throws Exception {
    int nQueries = Math.min(maxQueries, qualityQueries.length);
    QualityStats stats[] = new QualityStats[nQueries];
    ///////////
  //  System.out.println("Number of queries "+nQueries);
    /////////////

    for (int i=0; i<nQueries; i++) {
      QualityQuery qq = qualityQueries[i];
      // generate query
           Query q = qqParser.parse(qq);
      // search with this query
 long t1 = System.currentTimeMillis();
      TopDocsMeanVariance td = searcher.search_var(q,null,maxResults, a1);


      /////////////////////////////
      //ucl

       TopDocsMeanVariance f_td = td;
      if(a2!=0){
        f_td = Correlation_Adjust_RR(td, a2);
      }



 long searchTime = System.currentTimeMillis()-t1;

      outresult(qq, f_td, scorelogger);
      if (i==0) printCorrelation(f_td);
/////////////////////////////

      //most likely we either submit or judge, but check both
      if (judge!=null) {
        stats[i] = analyzeQueryResults(qq, q, f_td, judge, qualityLog, searchTime);
      }
      if (submitRep!=null) {
        submitRep.report(qq,f_td,docNameField,searcher);
      }
    }
    if (submitRep!=null) {
      submitRep.flush();
    }

    return stats;



  }


    public  QualityStats [] execute_plot(Judge judge, SubmissionReport submitRep,
                                  PrintWriter qualityLog, PrintWriter scorelogger, double a1, double a2, PrintWriter relScoreLogger) throws Exception {
    int nQueries = Math.min(maxQueries, qualityQueries.length);
    QualityStats stats[] = new QualityStats[nQueries];
    ///////////
  //  System.out.println("Number of queries "+nQueries);
    /////////////

    for (int i=0; i<nQueries; i++) {
      QualityQuery qq = qualityQueries[i];
      // generate query
           Query q = qqParser.parse(qq);

      // search with this query
 long t1 = System.currentTimeMillis();
      TopDocs td = searcher.search_plot(q,null,maxResults, a1);


      /////////////////////////////
      //ucl

       TopDocs f_td = td;
      if(a2!=0){
        f_td = Correlation_Adjust(td, a2);
      }



 long searchTime = System.currentTimeMillis()-t1;

      outresult(qq, f_td, scorelogger);
/////////////////////////////

      //most likely we either submit or judge, but check both
      if (judge!=null) {
        stats[i] = analyzeQueryResults_plot(qq, q, f_td, judge, qualityLog, searchTime, relScoreLogger);
      }
      if (submitRep!=null) {
        submitRep.report(qq,f_td,docNameField,searcher);
      }
    }
    if (submitRep!=null) {
      submitRep.flush();
    }

    return stats;



  }


   private void printCorrelation(TopDocsMeanVariance td) throws IOException{

    FileOutputStream meanVarFile =new FileOutputStream(new File("mean-var-pair-trec8-mu=0"));
    PrintWriter logger = new PrintWriter(meanVarFile,true);
     String sep = " \t ";
    DocNameExtractor xt = new DocNameExtractor(docNameField);
        TopDocsMeanVariance f_td = td;
        MeanVarianceScoreDoc temp[] = f_td.MeanVariancescoreDocs;
        for(int d=0; d < temp.length ; d++){
            for(int j = d+1; j <temp.length ; j++){
               double rou = correlation(temp[d].doc, temp[j].doc);
               String docName1 = xt.docName(searcher,temp[d].doc);
               String docName2 = xt.docName(searcher,temp[j].doc);
                 logger.println(docName1    + sep +  docName2        + sep +   rou + sep) ;

                }
            }
        }


   private TopDocsMeanVariance Correlation_Adjust_NDCG(TopDocsMeanVariance td, double c) throws IOException {
        double log2toe = 1.0d / Math.log(2.0d);
        TopDocsMeanVariance f_td = td;
        MeanVarianceScoreDoc temp[] = f_td.MeanVariancescoreDocs;
    //    System.out.println("number: "+temp.length);
        TopMeanVarianceDocCollector collector = null;
        HashMap scoreHM = new HashMap();
        for (int k=0; k<temp.length; k++)
        {
            scoreHM.put (temp[k].doc, temp[k].score);
        }
        for(int d=0; d < 19 ; d++){
            collector = new TopMeanVarianceDocCollector(temp.length);
            for(int j = 0; j <=d ; j++){
                collector.collect(temp[j].doc, temp[j].score+100.0d, temp[j].mean, temp[j].variance);
            }
            for(int i = d+1; i < temp.length ; i++){
                double currentScore = Double.parseDouble(scoreHM.get(temp[i].doc).toString());
                double adjustAmount = Math.pow(2.0, currentScore) + temp[i].variance * Math.pow(2.0, currentScore)*Math.log(2)*Math.log(2);
                        //b_correlation /2.0/Math.log(d+2)/ log2toe *correlation(temp[d].doc,temp[i].doc)*0.01;//Math.sqrt(temp[d].variance *temp[i].variance);
//                System.out.println( Double.toString(correlation(temp[d].doc,temp[i].doc)) +"\t" + Double.toString(adjustAmount));
               temp[i].score = adjustAmount;//*temp[d].score*temp[i].score;
//c *correlation(temp[d].doc,temp[i].doc);
          //      System.out.println(temp[d].doc+"  "+temp[i].doc);
          //       System.out.println("Score: "+temp[i].score + " Correlation: "+correlation(temp[d].doc,temp[i].doc));
                collector.collect(temp[i].doc, temp[i].score, temp[i].mean, temp[i].variance);
//               temp[i].score-= c /Math.log(d+2)/ log2toe *correlation(temp[d].doc,temp[i].doc);
////c *correlation(temp[d].doc,temp[i].doc);
//          //      System.out.println(temp[d].doc+"  "+temp[i].doc);
//          //       System.out.println("Score: "+temp[i].score + " Correlation: "+correlation(temp[d].doc,temp[i].doc));
//                collector.collect(temp[i].doc, temp[i].score, temp[i].mean, temp[i].variance);
            }
               f_td  = collector.topDocs();
               temp = f_td.MeanVariancescoreDocs;
        }
        return f_td;
    }

   //Correlation adjust the TOP 10 rank
    private TopDocsMeanVariance Correlation_Adjust_AP(TopDocsMeanVariance td, double c) throws IOException {
        double log2toe = 1.0d / Math.log(2.0d);
        TopDocsMeanVariance f_td = td;
        MeanVarianceScoreDoc temp[] = f_td.MeanVariancescoreDocs;
    //    System.out.println("number: "+temp.length);
        TopMeanVarianceDocCollector collector = null;
        HashMap scoreHM = new HashMap();
        for (int k=0; k<temp.length; k++)
        {
            scoreHM.put (temp[k].doc, temp[k].score);
        }
        for(int d=0; d < 19 ; d++){
            collector = new TopMeanVarianceDocCollector(temp.length);
            for(int j = 0; j <=d ; j++){
                collector.collect(temp[j].doc, temp[j].score+100.0d, temp[j].mean, temp[j].variance);
            }
            for(int i = d+1; i < temp.length ; i++){
                double currentScore = Double.parseDouble(scoreHM.get(temp[i].doc).toString());
                double adjustAmount = currentScore/(d+2);
                for (int s=0; s<=d; s++)
                {
                    double docScore = Double.parseDouble(scoreHM.get(temp[s].doc).toString());
                    adjustAmount += 1.0/(d+2)*(correlation(temp[s].doc,temp[i].doc)*0.01);//Math.sqrt(temp[s].variance *temp[i].variance));//+ currentScore*docScore
                }
                        //b_correlation /2.0/Math.log(d+2)/ log2toe *correlation(temp[d].doc,temp[i].doc)*0.01;//Math.sqrt(temp[d].variance *temp[i].variance);
//                System.out.println( Double.toString(correlation(temp[d].doc,temp[i].doc)) +"\t" + Double.toString(adjustAmount));
               temp[i].score = adjustAmount;//*temp[d].score*temp[i].score;
//c *correlation(temp[d].doc,temp[i].doc);
          //      System.out.println(temp[d].doc+"  "+temp[i].doc);
          //       System.out.println("Score: "+temp[i].score + " Correlation: "+correlation(temp[d].doc,temp[i].doc));
                collector.collect(temp[i].doc, temp[i].score, temp[i].mean, temp[i].variance);
//               temp[i].score-= c /Math.log(d+2)/ log2toe *correlation(temp[d].doc,temp[i].doc);
////c *correlation(temp[d].doc,temp[i].doc);
//          //      System.out.println(temp[d].doc+"  "+temp[i].doc);
//          //       System.out.println("Score: "+temp[i].score + " Correlation: "+correlation(temp[d].doc,temp[i].doc));
//                collector.collect(temp[i].doc, temp[i].score, temp[i].mean, temp[i].variance);
            }
               f_td  = collector.topDocs();
               temp = f_td.MeanVariancescoreDocs;
        }
        return f_td;
    }
//Correlation adjust the TOP 10 rank
    private TopDocsMeanVariance Correlation_Adjust_RR(TopDocsMeanVariance td, double c) throws IOException {
        double log2toe = 1.0d / Math.log(2.0d);
        TopDocsMeanVariance f_td = td;
        MeanVarianceScoreDoc temp[] = f_td.MeanVariancescoreDocs;
    //    System.out.println("number: "+temp.length);
        TopMeanVarianceDocCollector collector = null;
        HashMap scoreHM = new HashMap();
        for (int k=0; k<temp.length; k++)
        {
            scoreHM.put (temp[k].doc, temp[k].score);
        }
        for(int d=0; d < 19 ; d++){
            collector = new TopMeanVarianceDocCollector(temp.length);
            for(int j = 0; j <=d ; j++){
                collector.collect(temp[j].doc, temp[j].score+100.0d, temp[j].mean, temp[j].variance);
            }
            for(int i = d+1; i < temp.length ; i++){
                double expectedVi = 1.0;
                double cov = 1.0;
                for (int s=0; s<=d; s++)
                {
                    expectedVi = expectedVi * (1- Double.parseDouble(scoreHM.get(temp[s].doc).toString()));
                    cov = cov* (correlation(temp[s].doc,temp[i].doc))*Math.sqrt(temp[s].variance *temp[i].variance);
                }
                expectedVi = expectedVi*Double.parseDouble(scoreHM.get(temp[i].doc).toString())/(d+2);
                cov = -cov/(d+2);
               temp[i].score = expectedVi+cov;//adjustAmount;//*temp[d].score*temp[i].score;
//c *correlation(temp[d].doc,temp[i].doc);
          //      System.out.println(temp[d].doc+"  "+temp[i].doc);
          //       System.out.println("Score: "+temp[i].score + " Correlation: "+correlation(temp[d].doc,temp[i].doc));
                collector.collect(temp[i].doc, temp[i].score, temp[i].mean, temp[i].variance);
//               temp[i].score-= c /Math.log(d+2)/ log2toe *correlation(temp[d].doc,temp[i].doc);
////c *correlation(temp[d].doc,temp[i].doc);
//          //      System.out.println(temp[d].doc+"  "+temp[i].doc);
//          //       System.out.println("Score: "+temp[i].score + " Correlation: "+correlation(temp[d].doc,temp[i].doc));
//                collector.collect(temp[i].doc, temp[i].score, temp[i].mean, temp[i].variance);
            }
               f_td  = collector.topDocs();
               temp = f_td.MeanVariancescoreDocs;
        }
        return f_td;
    }

   //Correlation adjust the TOP 10 rank
    private TopDocsMeanVariance Correlation_Adjust(TopDocsMeanVariance td, double c) throws IOException {
        double log2toe = 1.0d / Math.log(2.0d);
        TopDocsMeanVariance f_td = td;
        MeanVarianceScoreDoc temp[] = f_td.MeanVariancescoreDocs;
    //    System.out.println("number: "+temp.length);
        TopMeanVarianceDocCollector collector = null;
        for(int d=0; d < 19 ; d++){
            collector = new TopMeanVarianceDocCollector(temp.length);
            for(int j = 0; j <=d ; j++){
                collector.collect(temp[j].doc, temp[j].score+100.0d, temp[j].mean, temp[j].variance);
            }
            for(int i = d+1; i < temp.length ; i++){
               temp[i].score-= c /Math.log(d+2)/ log2toe *correlation(temp[d].doc,temp[i].doc)*0.01;
//c *correlation(temp[d].doc,temp[i].doc);
          //      System.out.println(temp[d].doc+"  "+temp[i].doc);
          //       System.out.println("Score: "+temp[i].score + " Correlation: "+correlation(temp[d].doc,temp[i].doc));
                collector.collect(temp[i].doc, temp[i].score, temp[i].mean, temp[i].variance);
            }
               f_td  = collector.topDocs();
               temp = f_td.MeanVariancescoreDocs;
        }
        return f_td;
    }

   //Correlation adjust the TOP 10 rank
    private TopDocs Correlation_Adjust(TopDocs td, double c) throws IOException {
        double log2toe = 1.0d / Math.log(2.0d);
        TopDocs f_td = td;
        ScoreDoc temp[] = f_td.scoreDocs;
    //    System.out.println("number: "+temp.length);
        TopDocCollector collector = null;
        for(int d=0; d < 19 ; d++){
            collector = new TopDocCollector(temp.length);
            for(int j = 0; j <=d ; j++){
                collector.collect(temp[j].doc, temp[j].score+100.0d);
            }
            for(int i = d+1; i < temp.length ; i++){
               temp[i].score-= c /Math.log(d+2)/ log2toe *correlation(temp[d].doc,temp[i].doc);
//c *correlation(temp[d].doc,temp[i].doc);
          //      System.out.println(temp[d].doc+"  "+temp[i].doc);
          //       System.out.println("Score: "+temp[i].score + " Correlation: "+correlation(temp[d].doc,temp[i].doc));
                collector.collect(temp[i].doc, temp[i].score);
            }
               f_td  = collector.topDocs();
               temp = f_td.scoreDocs;
        }
        return f_td;
    }

    //Pearson's product-moment coefficient
    private double correlation(int a, int b) throws IOException{
        double score= 0;
         TermFreqVector doc_a=reader.getTermFreqVector(a, docDataField);
         ///////////////
        // if( doc_a==null) System.out.println("doc_a is null");
         ///////////////
         TermFreqVector doc_b=reader.getTermFreqVector(b, docDataField);

         //stroe all the term freq in hashmap
         HashMap term_map =new HashMap();
         HashMap map_a =new HashMap();
         String terms[] = doc_a.getTerms();
         int freq[] = doc_a.getTermFrequencies();
         double ave_a = 0;
         for(int i = 0; i < terms.length ; i++){
            map_a.put(terms[i], freq[i]);
            term_map.put(terms[i], terms[i]);
            ave_a +=freq[i];
         }
         ave_a = ave_a / terms.length;

          double ave_b = 0;
         HashMap map_b =new HashMap();
         terms = doc_b.getTerms();
         freq = doc_b.getTermFrequencies();
         for(int i = 0; i < terms.length ; i++){
            map_b.put(terms[i], freq[i]);
            term_map.put(terms[i], terms[i]);
             ave_b +=freq[i];
         }
         ave_b = ave_b / terms.length;
         //compute correlation
         double sum_up = 0;
         double sum_a = 0;
         double sum_b = 0;

        Collection  Cterm=term_map.values();

  for   (Iterator   iterator   =  Cterm.iterator();   iterator.hasNext();)
  {
      int V_a = 0;
      int V_b = 0;
      String term = (String)iterator.next();
      if(map_a.containsKey(term))V_a = (Integer) map_a.get(term);
      if(map_b.containsKey(term))V_b = (Integer) map_b.get(term);
      sum_up += (1.0d* V_a - ave_a)*(1.0d * V_b - ave_b);
      sum_a += (1.0d* V_a - ave_a) * (1.0d* V_a - ave_a);
      sum_b += (1.0d* V_b - ave_b) * (1.0d* V_b - ave_b);

  }
        score = sum_up / (Math.sqrt(sum_a * sum_b));


        return score;
    }

  //UCL
  private void outresult(QualityQuery qq, TopDocs td, PrintWriter logger) throws IOException{
    ScoreDoc sd[] = td.scoreDocs;
    DocNameExtractor xt = new DocNameExtractor(docNameField);
  //  System.out.println( sd.length );
    for (int i=0; i<sd.length; i++) {
      String docName = xt.docName(searcher,sd[i].doc);
     if (logger!=null) {
      logger.println(qq.getQueryID()+'\t'+"Q0"+'\t'+docName+'\t'+i+'\t'+sd[i].score+'\t'+"test");
    }
    }
  }
    

    private void outresult(QualityQuery qq, TopDocsMeanVariance td, PrintWriter logger) throws IOException{
    MeanVarianceScoreDoc sd[] = td.MeanVariancescoreDocs;
    DocNameExtractor xt = new DocNameExtractor(docNameField);
  //  System.out.println( sd.length );
    for (int i=0; i<sd.length; i++) {
      String docName = xt.docName(searcher,sd[i].doc);
     if (logger!=null) {
      logger.println(qq.getQueryID()+'\t'+"Q0"+'\t'+docName+'\t'+i+'\t'+sd[i].score+'\t'+sd[i].mean+'\t'+sd[i].variance+'\t'+"test");
    }
    
    }
    
    
  }
  
 


  /* Analyze/judge results for a single quality query; optionally log them. */  
  private QualityStats analyzeQueryResults(QualityQuery qq, Query q, TopDocs td, Judge judge, PrintWriter logger, long searchTime) throws IOException {
    QualityStats stts = new QualityStats(judge.maxRecall(qq),searchTime);
    ScoreDoc sd[] = td.scoreDocs;
    long t1 = System.currentTimeMillis(); // extraction of first doc name we meassure also construction of doc name extractor, just in case.
    DocNameExtractor xt = new DocNameExtractor(docNameField);
  //  System.out.println( sd.length );
    for (int i=0; i<sd.length; i++) {
      String docName = xt.docName(searcher,sd[i].doc);
    
   //   System.out.println( sd[i].doc);
      long docNameExtractTime = System.currentTimeMillis() - t1;
      t1 = System.currentTimeMillis();
      boolean isRelevant = judge.isRelevant(docName,qq);
      stts.addResult(i+1,isRelevant, docNameExtractTime);
    }
    if (logger!=null) {
      logger.println(qq.getQueryID()+"  -  "+q);
      stts.log(qq.getQueryID()+" Stats:",1,logger,"  ");
    }
    return stts;
  }
//

  /* Analyze/judge results for a single quality query; optionally log them. */
  private QualityStats analyzeQueryResults(QualityQuery qq, Query q, TopDocsMeanVariance td, Judge judge, PrintWriter logger, long searchTime) throws IOException {
    QualityStats stts = new QualityStats(judge.maxRecall(qq),searchTime);
    MeanVarianceScoreDoc sd[] = td.MeanVariancescoreDocs;
    long t1 = System.currentTimeMillis(); // extraction of first doc name we meassure also construction of doc name extractor, just in case.
    DocNameExtractor xt = new DocNameExtractor(docNameField);
  //  System.out.println( sd.length );
    for (int i=0; i<sd.length; i++) {
      String docName = xt.docName(searcher,sd[i].doc);

   //   System.out.println( sd[i].doc);
      long docNameExtractTime = System.currentTimeMillis() - t1;
      t1 = System.currentTimeMillis();
      boolean isRelevant = judge.isRelevant(docName,qq);
      stts.addResult(i+1,isRelevant, docNameExtractTime);
    }
    if (logger!=null) {
      logger.println(qq.getQueryID()+"  -  "+q);
      stts.log(qq.getQueryID()+" Stats:",1,logger,"  ");
    }
    return stts;
  }
//
   private QualityStats analyzeQueryResults_plot(QualityQuery qq, Query q, TopDocs td, Judge judge, PrintWriter logger, long searchTime, PrintWriter relScoreLogger)
           throws IOException {
    QualityStats stts = new QualityStats(judge.maxRecall(qq),searchTime);
    ScoreDoc sd[] = td.scoreDocs;
    long t1 = System.currentTimeMillis(); // extraction of first doc name we meassure also construction of doc name extractor, just in case.
    DocNameExtractor xt = new DocNameExtractor(docNameField);
  //  System.out.println( sd.length );
    for (int i=0; i<sd.length; i++) {
      String docName = xt.docName(searcher,sd[i].doc);

   //   System.out.println( sd[i].doc);
      long docNameExtractTime = System.currentTimeMillis() - t1;
      t1 = System.currentTimeMillis();
      boolean isRelevant = judge.isRelevant(docName,qq);
      if (isRelevant) relScoreLogger.println(docName + "	" + qq.getQueryID() + "	"+ i  +"	" + sd[i].score);
      stts.addResult(i+1,isRelevant, docNameExtractTime);
    }
    if (logger!=null) {
      logger.println(qq.getQueryID()+"  -  "+q);
      stts.log(qq.getQueryID()+" Stats:",1,logger,"  ");
    }
    return stts;
  }
  /**
   * @return the maximum number of quality queries to run. Useful at debugging.
   */
  public int getMaxQueries() {
    return maxQueries;
  }

  /**
   * Set the maximum number of quality queries to run. Useful at debugging.
   */
  public void setMaxQueries(int maxQueries) {
    this.maxQueries = maxQueries;
  }

  /**
   * @return the maximum number of results to collect for each quality query.
   */
  public int getMaxResults() {
    return maxResults;
  }

  /**
   * set the maximum number of results to collect for each quality query.
   */
  public void setMaxResults(int maxResults) {
    this.maxResults = maxResults;
  }

}
