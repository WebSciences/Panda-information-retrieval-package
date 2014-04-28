/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.ucl.panda.retrieval.models;

import java.util.HashMap;
import java.util.Vector;

/**
 * Jelinek-Mercer
 */
public class Language_lambda implements Model {
    private double lambda = 0.1d;
    private double var = 0;


  
    public double getscore(double tf, double df, double idf, double DL, double avgDL, int DocNum, double CL, int CTF, int qTF) {
                
        double P = (1-lambda)*tf/DL+lambda*CTF/CL;
          double score = Math.log(P);
          return score;
    }

  
    public double defaultScore(double tf, double df, double idf, double DL, double avgDL, int DocNum, double CL, int CTF, int qTF) {
       double P = lambda*CTF/CL;
       double score = Math.log(P);
       return score;
    }

   
    public double defaultScore(double tf, double df, double idf, double DL, double avgDL, int DocNum, double CL, int CTF, int qTF, double a) {
      return defaultScore( tf, df, idf, DL, avgDL, DocNum,CL,CTF, qTF) - a * var;


    }

   
    public double getscore(double tf, double df, double idf, double DL, double avgDL, int DocNum, double CL, int CTF, int qTF, double a) {
        return  getscore(tf, df, idf, DL, avgDL, DocNum, CL, CTF, qTF) - a * var;


    }

    @Override
	public
    double getmean(double tf, double df, double idf, double DL, double avgDL, int DocNum, double CL, int CTF, int qTF, double a) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
	public
    double getvar(double tf, double df, double idf, double DL, double avgDL, int DocNum, double CL, int CTF, int qTF, double a) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
	public
    double defaultmean(double tf, double df, double idf, double DL, double avgDL, int DocNum, double CL, int CTF, int qTF, double a) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
	public
    double defaultvar(double tf, double df, double idf, double DL, double avgDL, int DocNum, double CL, int CTF, int qTF, double a) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


	@Override
	public double getVSMscore(Vector<String> query,
			HashMap<String, Integer> TermVector) {
		// TODO Auto-generated method stub
		return 0;
	}

 
}
