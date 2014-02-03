package uk.ac.ucl.panda.retrieval.models;

import java.util.HashMap;
import java.util.Vector;

/**
 * Implements the TFIDF model. Use this class as a template for implementing
 * other Information Retrieval models
 */
public class TFIDF implements Model {

	/**
	 *  This method is called for each term in the query and for each document in the collection 
	 *  where tf > 0
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
	public double getscore(double tf, double df, double idf, double DL,
			double avgDL, int DocNum, double CL, int CTF, int qTF) {
		tf = tf / DL;
		double score = tf * idf; // basic tf*idf function
		return score;
	}

	/**
	 * if tf =0, defaultScore function is used to compute the document score.
	 * i.e default score for the document if the term is not present in the
	 * document.
	 */
	@Override
	public double defaultScore(double tf, double df, double idf, double DL,
			double avgDL, int DocNum, double CL, int CTF, int qTF) {
		return 0.0d;
	}

	/**
	 * This method should only be used in the Vector Space Model class. This
	 * method will be called instead of the getScore method above
	 * 
	 * @param query
	 *            A Vector of Strings representing the query
	 * @param termVector
	 *            HashMap linking each term in the document with its term
	 *            frequency
	 * 
	 *            Example: query = [foreign, minor, germani] 
	 *            TermVector = {spy=1, accept=1, servic=1, visit=2, languag=1, ...
	 */
	@Override
	public double getVSMscore(Vector<String> query, HashMap<String, Integer> termVector) {
		return 0.0;
	}

	/**
	 *  The following functions are not needed for Text Retrieval assignment
	 */

	@Override
	public double defaultScore(double tf, double df, double idf, double DL,
			double avgDL, int DocNum, double CL, int CTF, int qTF, double a) {
		return 0.0d;

	}

	@Override
	public double getscore(double tf, double df, double idf, double DL,
			double avgDL, int DocNum, double CL, int CTF, int qTF, double a) {
		return 0.0d;

	}

	@Override
	public double getmean(double tf, double df, double idf, double DL,
			double avgDL, int DocNum, double CL, int CTF, int qTF, double a) {

		return 0.0d;
	}

	@Override
	public double getvar(double tf, double df, double idf, double DL,
			double avgDL, int DocNum, double CL, int CTF, int qTF, double a) {
		return 0.0;
	}

	@Override
	public double defaultmean(double tf, double df, double idf, double DL,
			double avgDL, int DocNum, double CL, int CTF, int qTF, double a) {
		return 0.0d;
	}

	@Override
	public double defaultvar(double tf, double df, double idf, double DL,
			double avgDL, int DocNum, double CL, int CTF, int qTF, double a) {
		return 0.0;
	}

}
