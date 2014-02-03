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

import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.ArrayList;

import uk.ac.ucl.panda.map.MAPScore;

/**
 * Results of quality benchmark run for a single query or for a set of queries.
 */
public class QualityStats {

  /** Number of points for which precision is computed. */
  public static final int MAX_POINTS = 1000;

  private double maxGoodPoints;
  private double recall;
  private double pAt[];
  private double pReleventSum = 0;
  private double numPoints = 0;
  private double numGoodPoints = 0;
  private double mrr = 0;
  private double One_call =0;
  private double NDCG[];
  private double Two_call = 0;
  private double Three_call = 0;
  private double Four_call = 0;
  private double Five_call = 0;
  private double Six_call = 0;
  private double Seven_call = 0;
  private double Eight_call = 0;
  private double Nine_call = 0;
  private double Ten_call = 0;
  private long searchTime;
  private long docNamesExtractTime;
  private double MAP = 0;

  /**
   * A certain rank in which a relevant doc was found.
   */
  public static class RecallPoint {
    private int rank;
    private double recall;
    private RecallPoint(int rank, double recall) {
      this.rank = rank;
      this.recall = recall;
    }

     /** Returns the rank: where on the list of returned docs this relevant doc appeared. */
    public int getRank() {
      return rank;
    }

    /** Returns the recall: how many relevant docs were returned up to this point, inclusive. */
    public double getRecall() {
      return recall;
    }
  }

  private ArrayList recallPoints;

  /**
   * Construct a QualityStats object with anticipated maximal number of relevant hits.
   * @param maxGoodPoints maximal possible relevant hits.
   */
  public QualityStats(double maxGoodPoints, long searchTime) {
    this.maxGoodPoints = maxGoodPoints;
    this.searchTime = searchTime;
    this.recallPoints = new ArrayList();
    pAt = new double[MAX_POINTS+1]; // pAt[0] unused.
    NDCG = new double[MAX_POINTS+1];
  }







  /**
   * Add a (possibly relevant) doc.
   * @param n rank of the added doc (its ordinal position within the query results).
   * @param isRelevant true if the added doc is relevant, false otherwise.
   */
  public void addResult(int n, boolean isRelevant, long docNameExtractTime) {
    if (Math.abs(numPoints+1 - n) > 1E-6) {
      throw new IllegalArgumentException("point "+n+" illegal after "+numPoints+" points!");
    }
    if (isRelevant) {
      numGoodPoints+=1;
      recallPoints.add(new RecallPoint(n,numGoodPoints));
      if (recallPoints.size()==1 && n<=5) { // first point, but only within 5 top scores.
        mrr =  1.0 / n;
      }
    }
    numPoints = n;
    double p = numGoodPoints / numPoints;
    if (isRelevant) {
      pReleventSum += p;
    }
    if (n<pAt.length) {
      pAt[n] = p;
    }
    if(n==10){
        if(pAt[10]>0)
            One_call = 1;
        if(pAt[10]>0.1d)
            Two_call = 1;
        if(pAt[10]>0.2d)
            Three_call = 1;
        if(pAt[10]>0.3d)
            Four_call = 1;
        if(pAt[10]>0.4d)
            Five_call = 1;
        if(pAt[10]>0.5d)
            Six_call = 1;
        if(pAt[10]>0.6d)
            Seven_call = 1;
        if(pAt[10]>0.7d)
            Eight_call = 1;
        if(pAt[10]>0.8d)
            Nine_call = 1;
        if(pAt[10]==1.0d)
            Ten_call = 1;
        NDCG[10]=ndcg(10);
    }
    if(n==1){
        NDCG[1]=ndcg(1);
    }
    if(n==10){
        NDCG[10]=ndcg(10);
    }
    if(n==15){
        NDCG[15]=ndcg(15);
    }
    if(n==20){
        NDCG[20]=ndcg(20);
    }
    if(n==35){
        NDCG[35]=ndcg(35);
    }
    if(n==50){
        NDCG[50]=ndcg(50);
    }
    if(n==70){
        NDCG[70]=ndcg(70);
    }
    if(n==100){
        NDCG[100]=ndcg(100);
    }
    if(n==200){
        NDCG[200]=ndcg(200);
    }
    if(n==400){
        NDCG[400]=ndcg(400);
    }
    if(n==600){
        NDCG[600]=ndcg(600);
    }
    if(n==1000){
        NDCG[1000]=ndcg(1000);
    }
    if(n==500){
        NDCG[500]=ndcg(500);
    }
    if(n==250){
        NDCG[250]=ndcg(250);
    }
    if(n==700){
        NDCG[700]=ndcg(700);
    }
    if(n==5){
        NDCG[5]=ndcg(5);
    }
    recall = maxGoodPoints<=0 ? p : numGoodPoints/maxGoodPoints;
    docNamesExtractTime += docNameExtractTime;
  }

  /**
   * Return the precision at rank n:
   * |{relevant hits within first <code>n</code> hits}| / <code>n</code>.
   * @param n requested precision point, must be at least 1 and at most {@link #MAX_POINTS}.
   */
  public double getPrecisionAt(int n) {
    if (n<1 || n>MAX_POINTS) {
      throw new IllegalArgumentException("n="+n+" - but it must be in [1,"+MAX_POINTS+"] range!");
    }
    if (n>numPoints) {
      return (numPoints * pAt[(int)numPoints])/n;
    }
    return pAt[n];
  }

 
  /**
   * Return the recall: |{relevant hits}| / |{hits}|.
   */
  public double getRecall() {
    return recall;
  }

   /**
   * Return the 1-call.
   */
  public double getOneCall() {
    return One_call;
  }

   /**
   * Return the 2-call.
   */
  public double getTwoCall() {
    return Two_call;
  }

   /**
   * Return the 3-call.
   */
  public double getThreeCall() {
    return Three_call;
  }

   /**
   * Return the 4-call.
   */
  public double getFourCall() {
    return Four_call;
  }

   /**
   * Return the 5-call.
   */
  public double getFiveCall() {
    return Five_call;
  }

   /**
   * Return the 6-call.
   */
  public double getSixCall() {
    return Six_call;
  }

   /**
   * Return the 7-call.
   */
  public double getSevenCall() {
    return Seven_call;
  }

   /**
   * Return the 8-call.
   */
  public double getEightCall() {
    return Eight_call;
  }

   /**
   * Return the 9-call.
   */
  public double getNineCall() {
    return Nine_call;
  }

   /**
   * Return the 10-call.
   */
  public double getTenCall() {
    return Ten_call;
  }

   /*
   *NDCG
   * Score=\sum (power(2,r(j))-1)/\log(1+j)
   *
   *
   */

  private double ndcg(int n){
      if(n<=0)return 0;
      double norm = 0;
      double dcg = 0;
      for(int i =1; i<=n ;i++){
           norm  += (Math.pow(2, 1)-1.0d)/Math.log(1+i);
           double r = 0;
           if(i == 1)
               r = pAt[1];
           else{
            if(pAt[i]>=pAt[i-1] && pAt[i]!=0.0d)
                r=1.0d;
            else
                r=0.0d;
           }
           dcg  += (Math.pow(2, r)-1.0d)/Math.log(1+i);
      }

      return norm==0? 0 :dcg/norm;


  }



   /**
   * Return the NDGC.
   */
  public double getNDCG(int n) {
    return NDCG[n];
  }


   /**
   * Log information on this QualityStats object.
   * @param logger Logger.
   * @param prefix prefix before each log line.
   */
  public void log(String title, int paddLines, PrintWriter logger, String prefix) {
    for (int i=0; i<paddLines; i++) {
      logger.println();
    }
    if (title!=null && title.trim().length()>0) {
      logger.println(title);
    }
    prefix = prefix==null ? "" : prefix;
    NumberFormat nf = NumberFormat.getInstance();
    nf.setMaximumFractionDigits(3);
    nf.setMinimumFractionDigits(3);
    nf.setGroupingUsed(true);
    int M = 19;
    logger.println(prefix+format("Search Seconds: ",M)+
        fracFormat(nf.format((double)searchTime/1000)));
    logger.println(prefix+format("DocName Seconds: ",M)+
        fracFormat(nf.format((double)docNamesExtractTime/1000)));
    logger.println(prefix+format("Num Points: ",M)+
        fracFormat(nf.format(numPoints)));
    logger.println(prefix+format("Num Good Points: ",M)+
        fracFormat(nf.format(numGoodPoints)));
    logger.println(prefix+format("Max Good Points: ",M)+
        fracFormat(nf.format(maxGoodPoints)));
    logger.println(prefix+format("MRR: ",M)+
        fracFormat(nf.format(getMRR())));
    logger.println(prefix+format("MAP: ",M)+
            fracFormat(nf.format(getMAP())));//Also include MAP score
    logger.println(prefix+format("Recall: ",M)+
        fracFormat(nf.format(getRecall())));
    logger.println(prefix+format("1-call: ",M)+
        fracFormat(nf.format(getOneCall())));
    logger.println(prefix+format("2-call: ",M)+
        fracFormat(nf.format(getTwoCall())));
    logger.println(prefix+format("3-call: ",M)+
        fracFormat(nf.format(getThreeCall())));
    logger.println(prefix+format("4-call: ",M)+
        fracFormat(nf.format(getFourCall())));
    logger.println(prefix+format("5-call: ",M)+
        fracFormat(nf.format(getFiveCall())));
    logger.println(prefix+format("6-call: ",M)+
        fracFormat(nf.format(getSixCall())));
    logger.println(prefix+format("7-call: ",M)+
        fracFormat(nf.format(getSevenCall())));
    logger.println(prefix+format("8-call: ",M)+
        fracFormat(nf.format(getEightCall())));
    logger.println(prefix+format("9-call: ",M)+
        fracFormat(nf.format(getNineCall())));
     logger.println(prefix+format("10-call: ",M)+
        fracFormat(nf.format(getTenCall())));
     logger.println(prefix+format("NDCG@1: ",M)+
        fracFormat(nf.format(getNDCG(1))));
     logger.println(prefix+format("NDCG@5: ",M)+
        fracFormat(nf.format(getNDCG(5))));
     logger.println(prefix+format("NDCG@10: ",M)+
        fracFormat(nf.format(getNDCG(10))));
     logger.println(prefix+format("NDCG@15: ",M)+
        fracFormat(nf.format(getNDCG(15))));
     logger.println(prefix+format("NDCG@20: ",M)+
        fracFormat(nf.format(getNDCG(20))));
     logger.println(prefix+format("NDCG@35: ",M)+
        fracFormat(nf.format(getNDCG(35))));
     logger.println(prefix+format("NDCG@50: ",M)+
        fracFormat(nf.format(getNDCG(50))));
     logger.println(prefix+format("NDCG@70: ",M)+
        fracFormat(nf.format(getNDCG(70))));
     logger.println(prefix+format("NDCG@100: ",M)+
        fracFormat(nf.format(getNDCG(100))));
     logger.println(prefix+format("NDCG@200: ",M)+
        fracFormat(nf.format(getNDCG(200))));
     logger.println(prefix+format("NDCG@250: ",M)+
        fracFormat(nf.format(getNDCG(250))));
     logger.println(prefix+format("NDCG@400: ",M)+
        fracFormat(nf.format(getNDCG(400))));
     logger.println(prefix+format("NDCG@500: ",M)+
        fracFormat(nf.format(getNDCG(500))));
     logger.println(prefix+format("NDCG@600: ",M)+
        fracFormat(nf.format(getNDCG(600))));
     logger.println(prefix+format("NDCG@700: ",M)+
        fracFormat(nf.format(getNDCG(700))));
     logger.println(prefix+format("NDCG@1000: ",M)+
        fracFormat(nf.format(getNDCG(1000))));
    for (int i=1; i<(int)numPoints && i<pAt.length && i<21; i++) {
      logger.println(prefix+format("Precision At "+i+": ",M)+
          fracFormat(nf.format(getPrecisionAt(i))));
    }
    for (int i=21; i<(int)numPoints && i<pAt.length && i<200;i+=20) {
        logger.println(prefix+format("Precision At "+i+": ",M)+
          fracFormat(nf.format(getPrecisionAt(i))));
    }
    for (int i=200; i<(int)numPoints && i<pAt.length;i+=50) {
        logger.println(prefix+format("Precision At "+i+": ",M)+
          fracFormat(nf.format(getPrecisionAt(i))));
    }
    for (int i=0; i<paddLines; i++) {
      logger.println();
    }
  }



  /**
   * Log information on this QualityStats object.
   * @param logger Logger.
   * @param prefix prefix before each log line.
   */
  public void batch_log(String title, int paddLines, PrintWriter logger, String prefix) {
    logger.println();
    if (title!=null && title.trim().length()>0) {
      logger.println("a= "+title);
    }
    prefix = prefix==null ? "" : prefix;
    NumberFormat nf = NumberFormat.getInstance();
    nf.setMaximumFractionDigits(3);
    nf.setMinimumFractionDigits(3);
    nf.setGroupingUsed(true);
    logger.print(fracFormat(nf.format(getMRR()))+'\t');  //MRR
    logger.print(fracFormat(nf.format(getRecall()))+'\t');//Recall
    //logger.print(fracFormat(nf.format(getMAP()))+'\t');//MAP
   // 1-call
    logger.print(fracFormat(nf.format(getOneCall()))+'\t');
    // 2-call
    logger.print(fracFormat(nf.format(getTwoCall()))+'\t');
    // 3-call
    logger.print(fracFormat(nf.format(getThreeCall()))+'\t');
    // 4-call
    logger.print(fracFormat(nf.format(getFourCall()))+'\t');
    // 5-call
    logger.print(fracFormat(nf.format(getFiveCall()))+'\t');
    // 6-call
    logger.print(fracFormat(nf.format(getSixCall()))+'\t');
    // 7-call
    logger.print(fracFormat(nf.format(getSevenCall()))+'\t');
    // 8-call
    logger.print(fracFormat(nf.format(getEightCall()))+'\t');
    // 9-call
    logger.print(fracFormat(nf.format(getNineCall()))+'\t');
    //10-call
     logger.print(fracFormat(nf.format(getTenCall()))+'\t');
     //NDCG@1
     logger.print(fracFormat(nf.format(getNDCG(1)))+'\t');
     //NDCG@5
     logger.print(fracFormat(nf.format(getNDCG(5)))+'\t');
     //NDCG@10
     logger.print(fracFormat(nf.format(getNDCG(10)))+'\t');
     //NDCG@15
     logger.print(fracFormat(nf.format(getNDCG(15)))+'\t');
     //NDCG@20
     logger.print(fracFormat(nf.format(getNDCG(20)))+'\t');
     //NDCG@35
     logger.print(fracFormat(nf.format(getNDCG(35)))+'\t');
     //NDCG@50
     logger.print(fracFormat(nf.format(getNDCG(50)))+'\t');
     //NDCG@70
     logger.print(fracFormat(nf.format(getNDCG(70)))+'\t');
     //NDCG@100
     logger.print(fracFormat(nf.format(getNDCG(100)))+'\t');
     //NDCG@200
     logger.print(fracFormat(nf.format(getNDCG(200)))+'\t');
     //NDCG@250
     logger.print(fracFormat(nf.format(getNDCG(250)))+'\t');
     //NDCG@400
     logger.print(fracFormat(nf.format(getNDCG(400)))+'\t');
     //NDCG@500
     logger.print(fracFormat(nf.format(getNDCG(500)))+'\t');
     //NDCG@600
     logger.print(fracFormat(nf.format(getNDCG(600)))+'\t');
     //NDCG@700
     logger.print(fracFormat(nf.format(getNDCG(700)))+'\t');
     //NDCG@1000
     logger.print(fracFormat(nf.format(getNDCG(1000)))+'\t');
     //Precision At i
    for (int i=1; i<(int)numPoints && i<pAt.length && i<100; i++) {
      logger.print(fracFormat(nf.format(getPrecisionAt(i)))+'\t');
    }
    for (int i=0; i<paddLines; i++) {
      logger.println();
    }
  }

  private static String padd = "                                    ";
  private String format(String s, int minLen) {
    s = (s==null ? "" : s);
    int n = Math.max(minLen,s.length());
    return (s+padd).substring(0,n);
  }
  private String fracFormat(String frac) {
    int k = frac.indexOf('.');
    if(k == -1){
        k = frac.length();
    }
    String s1 = padd+frac.substring(0,k);
    int n = Math.max(k,6);
    s1 = s1.substring(s1.length()-n);
    return s1 + frac.substring(k);
  }

  /**
   * Create a QualityStats object that is the average of the input QualityStats objects.
   * @param stats array of input stats to be averaged.
   * @return an average over the input stats.
   */
  public static QualityStats average(QualityStats[] stats) {
    QualityStats avg = new QualityStats(0,0);
    if (stats.length==0) {
      // weired, no stats to average!
      return avg;
    }
    int m = 0; // queries with positive judgements
    // aggregate
    for (int i=0; i<stats.length; i++) {
      avg.searchTime += stats[i].searchTime;
      avg.docNamesExtractTime += stats[i].docNamesExtractTime;
      if (stats[i].maxGoodPoints>0) {
        m++;
        avg.numGoodPoints += stats[i].numGoodPoints;
        avg.numPoints += stats[i].numPoints;
        avg.recall += stats[i].recall;
        avg.One_call+=stats[i].getOneCall();
        avg.Two_call+=stats[i].getTwoCall();
        avg.Three_call+=stats[i].getThreeCall();
        avg.Four_call+=stats[i].getFourCall();
        avg.Five_call+=stats[i].getFiveCall();
        avg.Six_call+=stats[i].getSixCall();
        avg.Seven_call+=stats[i].getSevenCall();
        avg.Eight_call+=stats[i].getEightCall();
        avg.Nine_call+=stats[i].getNineCall();
        avg.Ten_call+=stats[i].getTenCall();

        avg.mrr += stats[i].getMRR();
        avg.maxGoodPoints += stats[i].maxGoodPoints;
        for (int j=1; j<avg.pAt.length; j++) {
          avg.pAt[j] += stats[i].getPrecisionAt(j);
          avg.NDCG[j]+=stats[i].getNDCG(j);
        }
      }
    }
    assert m>0 : "Fishy: no \"good\" queries!";
    // take average: times go by all queries, other meassures go by "good" queries noly.
    avg.searchTime /= stats.length;
    avg.docNamesExtractTime /= stats.length;
    avg.numGoodPoints /= m;
    avg.numPoints /= m;
    avg.recall /= m;
    avg.One_call /=m;
    avg.Two_call /=m;
    avg.Three_call /=m;
    avg.Four_call /=m;
    avg.Five_call /=m;
    avg.Six_call /=m;
    avg.Seven_call /=m;
    avg.Eight_call /=m;
    avg.Nine_call /=m;
    avg.Ten_call /=m;
    avg.mrr /= m;
    avg.maxGoodPoints /= m;
    for (int j=1; j<avg.pAt.length; j++) {
       avg.NDCG[j] /=m;
       avg.pAt[j] /= m;
    }
    avg.MAP = MAPScore.getMAPScore();
    return avg;
  }

  /**
   * Returns the time it took to extract doc names for judging the measured query, in milliseconds.
   */
  public long getDocNamesExtractTime() {
    return docNamesExtractTime;
  }

  /**
   * Returns the maximal number of good points.
   * This is the number of relevant docs known by the judge for the measured query.
   */
  public double getMaxGoodPoints() {
    return maxGoodPoints;
  }

  /**
   * Returns the number of good points (only relevant points).
   */
  public double getNumGoodPoints() {
    return numGoodPoints;
  }

  /**
   * Returns the number of points (both relevant and irrelevant points).
   */
  public double getNumPoints() {
    return numPoints;
  }

  /**
   * Returns the recallPoints.
   */
  public RecallPoint [] getRecallPoints() {
    return (RecallPoint[]) recallPoints.toArray(new RecallPoint[0]);
  }

  /**
   * Returns the Mean reciprocal rank over the queries or RR for a single query.
   * <p>
   * Reciprocal rank is defined as <code>1/r</code> where <code>r</code> is the
   * rank of the first correct result, or <code>0</code> if there are no correct
   * results within the top 5 results.
   * <p>
   * This follows the definition in
   * <a href="http://www.cnlp.org/publications/02cnlptrec10.pdf">
   * Question Answering - CNLP at the TREC-10 Question Answering Track</a>.
   */
  public double getMRR() {
    return mrr;
  }
  
  public double getMAP() {
	    return MAP;
	  }


  /**
   * Returns the search time in milliseconds for the measured query.
   */
  public long getSearchTime() {
    return searchTime;
  }

}
