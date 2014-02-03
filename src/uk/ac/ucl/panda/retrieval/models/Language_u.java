/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.ucl.panda.retrieval.models;

import java.util.HashMap;
import java.util.Vector;

/**
 * Dirichlet model
 */
public class Language_u implements Model {

    double u = 1000d;
    double var = 0;

   
   public double getscore(double tf, double df, double idf, double DL, double avgDL, int DocNum, double CL, int CTF, int qTF) {
       //  double log2toe = 1.0d / Math.log(2.0d);
      //   System.out.println("tf "+tf+" DL "+DL+" CTF " + CTF + " CL " + CL);
         double temp = (tf + u*CTF/CL)/(DL+u);
         double score = Math.log(temp);
     //    System.out.println(score);
         return score;
    }

  
   public double defaultScore(double tf, double df, double idf, double DL, double avgDL, int DocNum, double CL, int CTF, int qTF) {
        return Math.log(u*CTF/CL/(DL+u));
    }

   
   public double defaultScore(double tf, double df, double idf, double DL, double avgDL, int DocNum, double CL, int CTF, int qTF, double a) {
        return defaultScore( tf, df, idf, DL, avgDL, DocNum, CL, CTF, qTF) - a * var;

    }

 
   public double getscore(double tf, double df, double idf, double DL, double avgDL, int DocNum, double CL, int CTF, int qTF, double a) {
        return getscore(tf, df, idf, DL, avgDL, DocNum, CL, CTF, qTF) - a * var;

    }

    @Override
    public double getmean(double tf, double df, double idf, double DL, double avgDL, int DocNum, double CL, int CTF, int qTF, double a) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double getvar(double tf, double df, double idf, double DL, double avgDL, int DocNum, double CL, int CTF, int qTF, double a) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double defaultmean(double tf, double df, double idf, double DL, double avgDL, int DocNum, double CL, int CTF, int qTF, double a) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double defaultvar(double tf, double df, double idf, double DL, double avgDL, int DocNum, double CL, int CTF, int qTF, double a) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


	@Override
	public double getVSMscore(Vector<String> query,
			HashMap<String, Integer> TermVector) {
		// TODO Auto-generated method stub
		return 0;
	}

}
