package uk.ac.ucl.panda.retrieval.models;

import java.util.HashMap;
import java.util.Vector;

/**
 * 
 * Implements the Vector Space Model. See course notes for a description of the
 * Vector Space Model
 * 
 * @author Marc Sloan
 */
public class VectorSpaceModel implements Model {

	/**
	 * This method should only be used in the Vector Space Model class. This
	 * method will be called instead of the getScore method below
	 * 
	 * @param query
	 *            A Vector of Strings representing the query
	 * @param termVector
	 *            HashMap linking each term in the document with its term
	 *            frequency
	 * 
	 *            Example: query = [foreign, minor, germani] 
	 *            TermVector = {spy=1, accept=1, servic=1, visit=2, languag=1, ...
	 * 
	 * This method is called for every query and every document in the collection.
	 */
	@Override
	public double getVSMscore(Vector<String> query,
			HashMap<String, Integer> TermVector) {
		// Write Vector Space Model code here

		return 0; // return vector space model score here

	}

	/**
	 *  The following functions are not needed for Text Retrieval assignment
	 */

	public double getscore(double tf, double df, double idf, double DL,
			double avgDL, int DocNum, double CL, int CTF, int qTF) {
		return 0.0d;
	}

	@Override
	public double defaultScore(double tf, double df, double idf, double DL,
			double avgDL, int DocNum, double CL, int CTF, int qTF) {
		return 0.0d;
	}

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
