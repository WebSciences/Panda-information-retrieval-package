package uk.ac.ucl.panda.retrieval.models;

import java.util.HashMap;
import java.util.Vector;

/**
 * The interface that all models must inherit and implement.
 */
public interface Model {

	public double defaultScore(double tf, double df, double idf, double DL,
			double avgDL, int DocNum, double CL, int CTF, int qTF);

	public double getscore(double tf, double df, double idf, double DL, double avgDL,
			int DocNum, double CL, int CTF, int qTF);
	
	public double getVSMscore(Vector<String> query, HashMap<String, Integer> TermVector);

	public double defaultScore(double tf, double df, double idf, double DL,
			double avgDL, int DocNum, double CL, int CTF, int qTF, double a);

	public double getscore(double tf, double df, double idf, double DL, double avgDL,
			int DocNum, double CL, int CTF, int qTF, double a);

	public double getmean(double tf, double df, double idf, double DL, double avgDL,
			int DocNum, double CL, int CTF, int qTF, double a);

	public double getvar(double tf, double df, double idf, double DL, double avgDL,
			int DocNum, double CL, int CTF, int qTF, double a);

	public double defaultmean(double tf, double df, double idf, double DL,
			double avgDL, int DocNum, double CL, int CTF, int qTF, double a);

	public double defaultvar(double tf, double df, double idf, double DL,
			double avgDL, int DocNum, double CL, int CTF, int qTF, double a);
}
