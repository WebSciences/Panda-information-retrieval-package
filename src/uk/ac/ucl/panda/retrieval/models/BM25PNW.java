package uk.ac.ucl.panda.retrieval.models;

import java.util.HashMap;
import java.util.Vector;

/**
 *
 *	@author Marc Sloan, Abhishek Aggarwal
 * 	@author xxms
 */
public class BM25PNW implements Model {
    private float s = 0.20f;  //Pivoted Normalization Weighting Parameter

	/**
	 *  BM25 - See Formulas 3.15 and 3.3 in 'The Probabilistic Relevance Framework: BM25 and Beyond' 
	 *  by Stephen Robertson and Hugo Zaragov
	 *  http://www.soi.city.ac.uk/~ser/papers/foundations_bm25_review.pdf 
	 *  for more information on how this formula is derived
	 *  
	 *  @param tf - Term Frequency, number of times term appears in the document
	 *  @param df - Document Frequency, number of documents the term appears in
	 *  @param idf - Inverse Document Frequency
	 *  @param DL - number of terms in document
	 *  @param avgDL - average number of terms in all documents
	 *  @param DocNum - number of documents in the collection
	 *  @param CL - Collection Length, number of terms in document collection
	 *  @param CTF - Collection Term Frequency, number of times the term appears in the collection
	 *  @param qTF - Query Term Frequency, number of times the term appears in the query
	 */
   public double getscore(double tf, double df, double idf, double DL, double avgDL, int DocNum, double CL, int CTF, int qTF) {
	    //System.out.println(s);
	    double log2toe = 1.0d / Math.log(2.0d);
        double numer = 1 + Math.log(1 + Math.log(tf) * log2toe) * log2toe;
        double denom = ((1-s) + s * DL / avgDL);
        
        double firstTerm = numer/denom;
        double secondTerm = qTF;
        double thirdTerm = Math.log((DocNum + 1)/df) * log2toe;
        double score = firstTerm * secondTerm * thirdTerm;
        return score;
    }
   
	/**
	 *  The following functions are not needed for Text Retrieval assignment
	 */
   
	@Override
	public double getVSMscore(Vector<String> query, HashMap<String, Integer> TermVector) {
		return 0;
	}

    @Override
	public
    double defaultScore(double tf, double df, double idf, double DL, double avgDL, int DocNum, double CL, int CTF, int qTF) {
        return 0.0d;
    }


    public double defaultScore(double tf, double df, double idf, double DL, double avgDL, int DocNum, double CL, int CTF, int qTF, double a) {
    	//b = (float)a;
    	return defaultScore(tf,df, idf, DL, avgDL, DocNum, CL, CTF, qTF);
    }

    
    public double getscore(double tf, double df, double idf, double DL, double avgDL, int DocNum, double CL, int CTF, int qTF, double a) {
    	s = (float) a;
    	return getscore(tf, df, idf, DL, avgDL, DocNum, CL, CTF, qTF);
    }

    @Override
	public
    double getmean(double tf, double df, double idf, double DL, double avgDL, int DocNum, double CL, int CTF, int qTF, double a) {
        //double log2toe = 1.0d / Math.log(2.0d);
        //double temp =Math.log((DocNum - df +0.5f)/(df +0.5f)) * log2toe;
        //double score = temp*tf*(k1 + 1.0f) / (tf + k1 * ( 1.0f - b + b * DL / avgDL));  //BM25 function
        return 0.0;
    }

    @Override
	public
    double getvar(double tf, double df, double idf, double DL, double avgDL, int DocNum, double CL, int CTF, int qTF, double a) {
        return 0.0;
    }

    @Override
	public
    double defaultmean(double tf, double df, double idf, double DL, double avgDL, int DocNum, double CL, int CTF, int qTF, double a) {
    	s = (float) a;
    	return defaultScore(tf,df, idf, DL, avgDL, DocNum, CL, CTF, qTF);
    }

    @Override
	public
    double defaultvar(double tf, double df, double idf, double DL, double avgDL, int DocNum, double CL, int CTF, int qTF, double a) {
        return 0.0;
    }





}
