package uk.ac.ucl.panda.retrieval;

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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.ac.ucl.panda.GetDocTermStats;
import uk.ac.ucl.panda.indexing.ExtraInformation;
import uk.ac.ucl.panda.indexing.io.IndexReader;
import uk.ac.ucl.panda.retrieval.models.NormalDist;
import uk.ac.ucl.panda.retrieval.models.RawMaterial;
import uk.ac.ucl.panda.retrieval.models.SArray;
import uk.ac.ucl.panda.retrieval.models.VectorSpaceModel;
import uk.ac.ucl.panda.retrieval.query.Query;
import uk.ac.ucl.panda.utility.io.CorruptIndexException;
import uk.ac.ucl.panda.utility.structure.Document;
import uk.ac.ucl.panda.utility.structure.FieldSelector;
import uk.ac.ucl.panda.utility.structure.Term;
import uk.ac.ucl.panda.utility.io.FileReader;


/** An abstract base class for search implementations.
 * Implements the main search methods.
 * 
 * <p>Note that you can only access Hits from a Searcher as long as it is
 * not yet closed, otherwise an IOException will be thrown. 
 */
public class Searcher implements Searchable {


    IndexReader reader;
  
  private String index;
  RawMaterial raws;
//  HashMap CTF;
    private Term term;
  public Searcher(String index) throws FileNotFoundException, IOException, ClassNotFoundException {
	  	this(index, 0);
    }
  
  	public Searcher(String index, int modelNumber) throws FileNotFoundException, IOException, ClassNotFoundException {
      this.index=index;
      this.reader=IndexReader.open(index);
   // this.CTF =ExtraInformation.ReadCTF(index);
      this.raws=new RawMaterial(index, modelNumber); 

  }




  /** Expert: Low-level search implementation.  Finds the top <code>n</code>
   * hits for <code>query</code>, applying <code>filter</code> if non-null.
   *
   * <p>Called by {@link Hits}.
   *
   * <p>Applications should usually call {@link Searcher#search(Query)} or
   * {@link Searcher#search(Query,Filter)} instead.
   * @throws BooleanQuery.TooManyClauses
   */
  public TopDocs search(Query query, Filter filter, int nDocs)
    throws IOException {
    if(query == null) return null;

    if (nDocs <= 0)  // null might be returned from hq.top() below.
      throw new IllegalArgumentException("nDocs must be > 0");

    TopDocCollector collector = new TopDocCollector(nDocs);
    search(query, filter, collector);
    return collector.topDocs();

  }


    /** Expert: Low-level search implementation.  Finds the top <code>n</code>
   * hits for <code>query</code>, applying <code>filter</code> if non-null.
   *
   * <p>Called by {@link Hits}.
   *
   * <p>Applications should usually call {@link Searcher#search(Query)} or
   * {@link Searcher#search(Query,Filter)} instead.
     * @throws ClassNotFoundException 
   * @throws BooleanQuery.TooManyClauses
   */
  public TopDocs search(Query query, Filter filter, int nDocs, double a)
    throws IOException, ClassNotFoundException {
	  
	  //Count the term frequency of terms in the query
	  HashMap<String, Integer> qtfCounts = new HashMap<String, Integer>();

    if (nDocs <= 0)  // null might be returned from hq.top() below.
      throw new IllegalArgumentException("nDocs must be > 0");

    TopDocCollector collector = new TopDocCollector(nDocs);
    
    // HitCollector collector = results;
     if (filter != null) {
    }

    HashMap Docset = new HashMap();
    HashMap queryscore = new HashMap();
    for(int j =0 ; j< query.getTerm().toArray().length ; j++){
        term = (Term)query.getTerm().get(j);
        raws.getDocSet(term, Docset);
        String termText = term.text().toLowerCase().trim();
        if(qtfCounts.containsKey(termText))
        	qtfCounts.put(termText, qtfCounts.get(termText) + 1);//increment term count
        else
        	qtfCounts.put(termText, 1); //add new term
    }
    if(this.raws.getModelType().equals(VectorSpaceModel.class))
    {
        GetDocTermStats tms = new GetDocTermStats(this.index);
        raws.processVM(query, Docset.values(), queryscore, tms);
    }
    else
    {
        for(int j =0 ; j< query.getTerm().toArray().length ; j++){
            term = (Term)query.getTerm().get(j);
            String termText = term.text().toLowerCase().trim();
            raws.process(term, j,Docset.values(), queryscore, qtfCounts.get(termText) ,a);

        }
    }
     

   // double checkscore = 0;

       Collection  Docs=Docset.values();
  for   (Iterator   iterator   =  Docs.iterator();   iterator.hasNext();)
  {
      int DocNum = (Integer)iterator.next();
  //    System.out.println(DocNum+"  "+ (Double)queryscore.get(DocNum));
       collector.collect(DocNum, (Double)queryscore.get(DocNum));
  }
    return collector.topDocs();

  }
  static class MyComparator implements Comparator{

public int compare(Object obj1, Object obj2){

int result=0;Map.Entry e1 = (Map.Entry)obj1 ;

Map.Entry e2 = (Map.Entry)obj2 ;//Sort based on values.

double value1 = Double.parseDouble(e1.getValue().toString());
double value2 = Double.parseDouble(e2.getValue().toString());

//if(value1.compareTo(value2)==0){
//
//String word1=(String)e1.getKey();
//String word2=(String)e2.getKey();
//
////Sort String in an alphabetical order
//result=word1.compareToIgnoreCase(word2);
//
//} else{
//Sort values in a descending order
//result=value2.compareTo( value1 );
if (value2 >value1)
    return 1;
else
    return -1;
//}

//return result;
}
  }
   public TopDocsMeanVariance search_var(Query query, Filter filter, int nDocs, double a_riskadjust)
    throws IOException {


    if (nDocs <= 0)  // null might be returned from hq.top() below.
      throw new IllegalArgumentException("nDocs must be > 0");

    TopMeanVarianceDocCollector collector = new TopMeanVarianceDocCollector(nDocs);

    // HitCollector collector = results;
     if (filter != null) {
    }

   //Count the term frequency of terms in the query
	  HashMap<String, Integer> qtfCounts = new HashMap<String, Integer>();
	  
    HashMap Docset = new HashMap();
    HashMap querymean = new HashMap();
    HashMap queryvar = new HashMap();
    HashMap queryscore = new HashMap();
    boolean rankByScore = true; //false;//true: rank by score; false: rank by mean risk-adjusted
    for(int j =0 ; j< query.getTerm().toArray().length ; j++){
        term = (Term)query.getTerm().get(j);
        raws.getDocSet(term, Docset);
        String termText = term.text().toLowerCase().trim();
        if(qtfCounts.containsKey(termText))
        	qtfCounts.put(termText, qtfCounts.get(termText) + 1);//increment term count
        else
        	qtfCounts.put(termText, 1); //add new term
    }
    for(int j =0 ; j< query.getTerm().toArray().length ; j++){
        term = (Term)query.getTerm().get(j);
        String termText = term.text().toLowerCase().trim();
        raws.process_var(term, j,Docset.values(), querymean, queryvar, queryscore, qtfCounts.get(termText), a_riskadjust);
    }
    ////////////////
   // queryvar
        //sarray my_array = new sarray();

         Hashtable docNumHT = new Hashtable();
         Collection  Docs=Docset.values();
          for   (Iterator   iterator   =  Docs.iterator();   iterator.hasNext();)
          {
              int DocNum = (Integer)iterator.next();
              if (rankByScore)
                docNumHT.put( DocNum, (Double)queryscore.get(DocNum));
              else
                docNumHT.put( DocNum, (Double)querymean.get(DocNum));
          }
         //Put keys and values in to an arraylist using entryset
        ArrayList myArrayList=new ArrayList(docNumHT.entrySet());

        //Sort the values based on values first and then keys.
        Collections.sort(myArrayList, new MyComparator());

        //Show sorted results
        Iterator itr=myArrayList.iterator();
//        String key="";
//        int value=0;
        int cnt=0;
        Hashtable topDocNumHT = new Hashtable();
        while(itr.hasNext() && cnt < 2000){

        cnt++;
        Map.Entry e=(Map.Entry)itr.next();
        topDocNumHT.put(e.getKey(), e.getValue());
//        key = (String)e.getKey();
//        value = ((Integer)e.getValue()).intValue();
        }
        


        //System.out.println(key+â€�, â€œ+value);

//        Collections.sort ( my_array );
//
//        for ( int i = 0 ; i < my_array.size() && i < 300; i++ )
//        {
//            sortable s = (sortable) my_array.elementAt ( i );
//            Integer object_key = (Integer) s.getObject();
//            //Integer integer_object = (Integer) s.getObject();
//            topDocNumHT.put (object_key, 0.0d);
//        }
    Docs=Docset.values();
//    FileOutputStream meanVarFile =new FileOutputStream(new File("mean-var-pair-trec8-mu=0"), true);
//        PrintWriter logger = new PrintWriter(meanVarFile,true);

  for   (Iterator   iterator   =  Docs.iterator();   iterator.hasNext();)
  { 
      int DocNum = (Integer)iterator.next();
      if (topDocNumHT.containsKey(DocNum))
      {
          double score =(Double)queryscore.get (DocNum);
          double mean = (Double)querymean.get(DocNum);
          double var = (Double)queryvar.get(DocNum);;
//          if (a_riskadjust != 0)
//              var = (Double)queryvar.get(DocNum);
          //double score =(Double)queryvar.get(DocNum);
//          logger.append (query.toString() + "	" + DocNum+"	"+mean + "	" + var);
//            logger.println();
          if (rankByScore)
          {
              collector.collect(DocNum, score, mean, var);
          }
          else
          {
              double finalScore = 0.0;
              if (a_riskadjust !=0)
              {
                  double sd = Math.sqrt (var);
                  finalScore = mean-a_riskadjust*sd;
              }
              else
                  finalScore = mean;
              double finalScore1 = finalScore;//Math.exp(finalScore)/(1.0+Math.exp(finalScore));
              collector.collect(DocNum, finalScore1, mean, var);
          }
          /*Collection  Docs1=Docset.values();
          //double expected_rank =0.0d;
          for  (Iterator   iterator1   =  Docs1.iterator();   iterator1.hasNext();)
          {
              int DocNum1 = (Integer)iterator1.next();
              if (DocNum != DocNum1 && (topDocNumHT.containsKey(DocNum1)))
              {
                  double mean1 = (Double)querymean.get(DocNum1) - mean;
                  double var1 = var + (Double)queryvar.get(DocNum1);
                  NormalDist ns = new NormalDist(mean1, var1);
                  expected_rank = expected_rank + 1.0d-ns.cdf (0.0d);
              }
          }*/

          //collector.collect(DocNum, 1/expected_rank, mean, var);
      }
  }
        return collector.topDocs();

  }
        //logger.close();



    //querymean

    ///////////////


   // double checkscore = 0;

/*       Docs=Docset.values();
  for   (Iterator   iterator   =  Docs.iterator();   iterator.hasNext();)
  {
      int DocNum = (Integer)iterator.next();
      //System.out.println(DocNum+"  "+ (Double)querymean.get(DocNum));
      if (a !=0)
          collector.collect(DocNum, (Double)querymean.get(DocNum)-a/2.0d*(Double)queryvar.get(DocNum));
      else
          collector.collect(DocNum, (Double)querymean.get(DocNum));

  }
 */


   public TopDocs search_plot(Query query, Filter filter, int nDocs, double a, HashMap [] tempScoreMaps)
    throws IOException {


//	    FileOutputStream evals =new FileOutputStream(new File("/workspace/jianhzhu/Panda_0_1/var/term-scores"));
//	    PrintWriter logger = new PrintWriter(evals,true);

    if (nDocs <= 0)  // null might be returned from hq.top() below.
      throw new IllegalArgumentException("nDocs must be > 0");

    TopDocCollector collector = new TopDocCollector(nDocs);

    // HitCollector collector = results;
     if (filter != null) {
    }
	  //Count the term frequency of terms in the query
	  HashMap<String, Integer> qtfCounts = new HashMap<String, Integer>();
    HashMap Docset = new HashMap();
    HashMap queryscore = new HashMap();
    int qLength = query.getTerm().toArray().length;
    for(int j =0 ; j< query.getTerm().toArray().length ; j++){
        term = (Term)query.getTerm().get(j);
        raws.getDocSet(term, Docset);
        String termText = term.text().toLowerCase().trim();
        if(qtfCounts.containsKey(termText))
        	qtfCounts.put(termText, qtfCounts.get(termText) + 1);//increment term count
        else
        	qtfCounts.put(termText, 1); //add new term
    }
    for(int j =0 ; j< query.getTerm().toArray().length ; j++){
        term = (Term)query.getTerm().get(j);
        String termText = term.text().toLowerCase().trim();
        HashMap tempScore = raws.process(term, j,Docset.values(), queryscore,qtfCounts.get(termText), a);
        tempScoreMaps [j] = tempScore;
//        Iterator myVeryOwnIterator = tempScore.keySet().iterator();
//        while(myVeryOwnIterator.hasNext()) {
//            //System.out.println(myVeryOwnIterator.next());
//            Object key = myVeryOwnIterator.next();
//            logger.println(query.toString()+'\t'+key.toString()+'\t'+j+'\t'+tempScore.get(key).toString());
//        }
    }


   // double checkscore = 0;

       Collection  Docs=Docset.values();
       System.out.println(qLength);
  for   (Iterator   iterator   =  Docs.iterator();   iterator.hasNext();)
  {
      int DocNum = (Integer)iterator.next();
       collector.collect(DocNum, (Double)queryscore.get(DocNum)/qLength);
  }
    return collector.topDocs();

  }

public void search(Query query, Filter filter, TopDocCollector results) throws IOException {
   // HitCollector collector = results;
     if (filter != null) {
    }
   //Count the term frequency of terms in the query
	  HashMap<String, Integer> qtfCounts = new HashMap<String, Integer>();
    HashMap Docset = new HashMap();
    HashMap queryscore = new HashMap();
    for(int j =0 ; j< query.getTerm().toArray().length ; j++){
        term = (Term)query.getTerm().get(j);
        raws.getDocSet(term, Docset);
        String termText = term.text().toLowerCase().trim();
        if(qtfCounts.containsKey(termText))
        	qtfCounts.put(termText, qtfCounts.get(termText) + 1);//increment term count
        else
        	qtfCounts.put(termText, 1); //add new term
    }
    for(int j =0 ; j< query.getTerm().toArray().length ; j++){
        term = (Term)query.getTerm().get(j);
        String termText = term.text().toLowerCase().trim();
        raws.process(term, j,Docset.values(), queryscore,qtfCounts.get(termText));

    }
     

   // double checkscore = 0;

       Collection  Docs=Docset.values();
  for   (Iterator   iterator   =  Docs.iterator();   iterator.hasNext();)
  {
      int DocNum = (Integer)iterator.next();
  //    System.out.println(DocNum+"  "+ (Double)queryscore.get(DocNum));
       results.collect(DocNum, (Double)queryscore.get(DocNum));
  }

   


}



 // inherit javadoc
  public Document doc(int i) throws CorruptIndexException, IOException {
    return reader.document(i);
  }




  // inherit javadoc
  public Document doc(int i, FieldSelector fieldSelector) throws CorruptIndexException, IOException {
	    return reader.document(i, fieldSelector);
  }

    public TopFieldDocs search(Query query, Filter filter, int n, Sort sort) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public TopDocs search_plot(Query query, Filter filter, int nDocs, double a)
			throws IOException {

		if (nDocs <= 0) // null might be returned from hq.top() below.
			throw new IllegalArgumentException("nDocs must be > 0");

		TopDocCollector collector = new TopDocCollector(nDocs);

		// HitCollector collector = results;
		if (filter != null) {
		}
		//Count the term frequency of terms in the query
		  HashMap<String, Integer> qtfCounts = new HashMap<String, Integer>();
		HashMap Docset = new HashMap();
		HashMap queryscore = new HashMap();
		int qLength = query.getTerm().toArray().length;
		for (int j = 0; j < query.getTerm().toArray().length; j++) {
			term = (Term) query.getTerm().get(j);
			raws.getDocSet(term, Docset);
	        String termText = term.text().toLowerCase().trim();
	        if(qtfCounts.containsKey(termText))
	        	qtfCounts.put(termText, qtfCounts.get(termText) + 1);//increment term count
	        else
	        	qtfCounts.put(termText, 1); //add new term
		}
		for (int j = 0; j < query.getTerm().toArray().length; j++) {
			term = (Term) query.getTerm().get(j);
			String termText = term.text().toLowerCase().trim();
			raws.process(term, j, Docset.values(), queryscore,qtfCounts.get(termText), a);

		}

		// double checkscore = 0;

		Collection Docs = Docset.values();
		System.out.println(qLength);
		for (Iterator iterator = Docs.iterator(); iterator.hasNext();) {
			int DocNum = (Integer) iterator.next();
			collector
					.collect(DocNum, (Double) queryscore.get(DocNum) / qLength);
		}
		return collector.topDocs();

	}




 
}
