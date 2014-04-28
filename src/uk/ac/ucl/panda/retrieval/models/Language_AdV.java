/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.ucl.panda.retrieval.models;

import java.util.HashMap;
import java.util.Vector;

/**
 *
 * @author xxms
 */
public class Language_AdV implements Model {

    //double u =0.00001d;
    double l = 0.1d;
     double a = 10.0d;
     boolean GaussianTransformation = true;//false;//

    public double getscore(double tf, double df, double idf, double DL, double avgDL, int DocNum, double CL, int CTF, int qTF) {
       //  double log2toe = 1.0d / Math.log(2.0d);
      //   System.out.println("tf "+tf+" DL "+DL+" CTF " + CTF + " CL " + CL);
        double u = DL*l/(1-l);
         double tempUP = tf + u*CTF/CL;
         double tempD = DL+u;
         double mean = tempUP/tempD;
         double var = tempUP*(tempD-tempUP)/(tempD*tempD*(tempD+1.0d));
//         double k1=mean;
//         double k2=var;
//         double ci= tempUP;
//         double c = tempD;
//         double k3 = (3.0d*ci*ci*ci*c+6.0d*ci*ci*ci-6.0d*ci*ci*c+ci*ci*ci*c*c+2.0d*ci*c*c)/(c*c*c*(c+1.0d)*(c+2.0d));
//         double k4 = -6.0d*ci*ci*ci*ci/(c*c*c*c) + 12.0d*ci*ci*ci*(ci+1.0d)/(c*c*c*(c+1.0d))-3.0d*ci*ci*(ci+1.0d)*(ci+1.0d)/(c*c*(c+1.0d)*(c+1.0d))
//                 -4.0d*ci*ci*(ci+1.0d)*(ci+2.0d)/(c*c*(c+1.0d)*(c+2.0d))+ci*(ci+1.0d)*(ci+2.0d)*(ci+3.0d)/(c*(c+1.0d)*(c+2.0d)*(c+3.0d));
         if (GaussianTransformation)
         {
             double adjustedMean = Math.log (mean/(1-mean))+ (2.0d*mean-1.0d)*var/(2.0d*mean*mean*(1.0d-mean)*(1.0d-mean));
             //double newVar = var/(mean*mean*(1.0d-mean)*(1.0d-mean));
             //double score =  adjustedMean - a*newVar/2.0d;//k1 -a*k2/2.0d;//+k3*a*a/6.0d-k4*a*a*a/24.0d;//
             return adjustedMean;
         }
         else
         {
             return Math.log(mean);
         }
//         if (score<0)
//             score = Double.MIN_VALUE;
//         score = Math.log (score);
                 //Math.log(tempUP/tempD - a*(tempUP*(tempD-tempUP)/(2*tempD*tempD*(tempD+1))));
     //    System.out.println(score);
         //return score;
    }

    @Override
	public
    double defaultScore(double tf, double df, double idf, double DL, double avgDL, int DocNum, double CL, int CTF, int qTF) {
        double u = DL*l/(1-l);
          double tempUP = u*CTF/CL;
         double tempD = DL+u;
         double mean = tempUP/tempD;
         double var = tempUP*(tempD-tempUP)/(tempD*tempD*(tempD+1.0d));
//         double k1=mean;
//         double k2=var;
//         double ci= tempUP;
//         double c = tempD;
//         double k3 = (3.0d*ci*ci*ci*c+6.0d*ci*ci*ci-6.0d*ci*ci*c+ci*ci*ci*c*c+2.0d*ci*c*c)/(c*c*c*(c+1.0d)*(c+2.0d));
//         double k4 = -6.0d*ci*ci*ci*ci/(c*c*c*c) + 12.0d*ci*ci*ci*(ci+1.0d)/(c*c*c*(c+1.0d))-3.0d*ci*ci*(ci+1.0d)*(ci+1.0d)/(c*c*(c+1.0d)*(c+1.0d))
//                 -4.0d*ci*ci*(ci+1.0d)*(ci+2.0d)/(c*c*(c+1.0d)*(c+2.0d))+ci*(ci+1.0d)*(ci+2.0d)*(ci+3.0d)/(c*(c+1.0d)*(c+2.0d)*(c+3.0d));
         if (GaussianTransformation)
         {
             double adjustedMean = Math.log (mean/(1-mean))+ (2.0d*mean-1.0d)*var/(2.0d*mean*mean*(1.0d-mean)*(1.0d-mean));
             //double newVar = var/(mean*mean*(1.0d-mean)*(1.0d-mean));
             //double newSD = Math.sqrt (newVar);
             //double score = adjustedMean - a*newSD;///2.0d;//k1 - a*k2/2.0d;//+k3*a*a/6.0d-k4*a*a*a/24.0d;//
             return adjustedMean;
         }
         else
         {
             //double newSD = Math.sqrt (var);
             //double score = Math.log(mean - a*newSD);
             return Math.log(mean);
         }
//         double adjustedMean = Math.log (mean/(1-mean))+ (2.0d*mean-1.0d)*var/(2.0d*mean*mean*(1.0d-mean)*(1.0d-mean));
//         double newVar = var/(mean*mean*(1.0d-mean)*(1.0d-mean));
//         double score = adjustedMean - a*newVar/2.0d;//k1 - a*k2/2.0d;//+k3*a*a/6.0d-k4*a*a*a/24.0d;//
//         if (score<0)
//             score = Double.MIN_VALUE;
//         score = Math.log (score);
         //double adjustedMean = Math.log (mean/(1.0d-mean))+ (2.0d*mean-1.0d)*var/(2.0d*mean*mean*(1.0d-mean)*(1.0d-mean));
         //double newVar = var/(mean*mean*(1.0d-mean)*(1.0d-mean));
         //double score = adjustedMean - a*newVar/2.0d;

         //double score = Math.log(tempUP/tempD - a*(tempUP*(tempD-tempUP)/(2*tempD*tempD*(tempD+1))));
     //    System.out.println(score);
//         return score;
    }

    @Override
	public
    double defaultScore(double tf, double df, double idf, double DL, double avgDL, int DocNum, double CL, int CTF, int qTF, double aa) {
        a=aa;
        double u = DL*l/(1-l);
         double tempUP = u*CTF/CL;
         double tempD = DL+u;
         double mean = tempUP/tempD;
         double var = tempUP*(tempD-tempUP)/(tempD*tempD*(tempD+1.0d));
//         double k1=mean;
//         double k2=var;
//         double ci= tempUP;
//         double c = tempD;
//         double k3 = (3.0d*ci*ci*ci*c+6.0d*ci*ci*ci-6.0d*ci*ci*c+ci*ci*ci*c*c+2.0d*ci*c*c)/(c*c*c*(c+1.0d)*(c+2.0d));
//         double k4 = -6.0d*ci*ci*ci*ci/(c*c*c*c) + 12.0d*ci*ci*ci*(ci+1.0d)/(c*c*c*(c+1.0d))-3.0d*ci*ci*(ci+1.0d)*(ci+1.0d)/(c*c*(c+1.0d)*(c+1.0d))
//                 -4.0d*ci*ci*(ci+1.0d)*(ci+2.0d)/(c*c*(c+1.0d)*(c+2.0d))+ci*(ci+1.0d)*(ci+2.0d)*(ci+3.0d)/(c*(c+1.0d)*(c+2.0d)*(c+3.0d));
         if (GaussianTransformation)
         {
             double adjustedMean = Math.log (mean/(1-mean))+ (2.0d*mean-1.0d)*var/(2.0d*mean*mean*(1.0d-mean)*(1.0d-mean));
             double newVar = var/(mean*mean*(1.0d-mean)*(1.0d-mean));
             double newSD = Math.sqrt (newVar);
             double score = adjustedMean - a*newSD;///2.0d;//k1 - a*k2/2.0d;//+k3*a*a/6.0d-k4*a*a*a/24.0d;//
             return score;
         }
         else
         {
             double newSD = Math.sqrt (var);
             double score = mean - a*newSD;
             if (score >0)
                return Math.log(score);
             else
                 return Math.log (Double.MIN_VALUE);
         }
//         if (score<0)
//             score = Double.MIN_VALUE;
//         score = Math.log (score);
         //double adjustedMean = Math.log (mean/(1.0d-mean))+ (2.0d*mean-1.0d)*var/(2.0d*mean*mean*(1.0d-mean)*(1.0d-mean));
         //double newVar = var/(mean*mean*(1.0d-mean)*(1.0d-mean));
         //double score = adjustedMean - a*newVar/2.0d;

         //
     //    System.out.println(score);
         //return score;
    }

    @Override
	public
    double getscore(double tf, double df, double idf, double DL, double avgDL, int DocNum, double CL, int CTF, int qTF, double aa) {
              //  double log2toe = 1.0d / Math.log(2.0d);
      //   System.out.println("tf "+tf+" DL "+DL+" CTF " + CTF + " CL " + CL);
        a=aa;
       double u = DL*l/(1-l);
         double tempUP = tf + u*CTF/CL;
         double tempD = DL+u;
         double mean = tempUP/tempD;
         double var = tempUP*(tempD-tempUP)/(tempD*tempD*(tempD+1.0d));
//         double k1=mean;
//         double k2=var;
//         double ci= tempUP;
//         double c = tempD;
//         double k3 = (3.0d*ci*ci*ci*c+6.0d*ci*ci*ci-6.0d*ci*ci*c+ci*ci*ci*c*c+2.0d*ci*c*c)/(c*c*c*(c+1.0d)*(c+2.0d));
//         double k4 = -6.0d*ci*ci*ci*ci/(c*c*c*c) + 12.0d*ci*ci*ci*(ci+1.0d)/(c*c*c*(c+1.0d))-3.0d*ci*ci*(ci+1.0d)*(ci+1.0d)/(c*c*(c+1.0d)*(c+1.0d))
//                 -4.0d*ci*ci*(ci+1.0d)*(ci+2.0d)/(c*c*(c+1.0d)*(c+2.0d))+ci*(ci+1.0d)*(ci+2.0d)*(ci+3.0d)/(c*(c+1.0d)*(c+2.0d)*(c+3.0d));
         if (GaussianTransformation)
         {
             double adjustedMean = Math.log (mean/(1-mean))+ (2.0d*mean-1.0d)*var/(2.0d*mean*mean*(1.0d-mean)*(1.0d-mean));
             double newVar = var/(mean*mean*(1.0d-mean)*(1.0d-mean));
             double newSD = Math.sqrt (newVar);
             double score = adjustedMean - a*newSD;///2.0d;//k1 - a*k2/2.0d;//+k3*a*a/6.0d-k4*a*a*a/24.0d;//
             return score;
         }
         else
         {
             double newSD = Math.sqrt (var);
             double score = mean - a*newSD;
             if (score >0)
                return Math.log(score);
             else
                 return Math.log (Double.MIN_VALUE);
         }
//         if (score<0)
//             score = Double.MIN_VALUE;
//         score = Math.log (score);
         //double adjustedMean = Math.log (mean/(1-mean))+ (2.0d*mean-1.0d)*var/(2.0d*mean*mean*(1.0d-mean)*(1.0d-mean));
         //double newVar = var/(mean*mean*(1.0d-mean)*(1.0d-mean));
         //double score = adjustedMean - a*newVar/2.0d;
                 //Math.log(tempUP/tempD - a*(tempUP*(tempD-tempUP)/(2*tempD*tempD*(tempD+1))));
    }

    @Override
	public
    double getmean(double tf, double df, double idf, double DL, double avgDL, int DocNum, double CL, int CTF, int qTF, double aa) {
       double u = DL*l/(1-l);
         double tempUP = tf + u*CTF/CL;
         double tempD = DL+u;
         double mean = tempUP/tempD;
         double var = tempUP*(tempD-tempUP)/(tempD*tempD*(tempD+1.0d));
         double adjustedMean = Math.log (mean/(1-mean))+ (2.0d*mean-1.0d)*var/(2.0d*mean*mean*(1.0d-mean)*(1.0d-mean));
         if (GaussianTransformation)
             return adjustedMean;
         else
            return Math.log(mean);//adjustedMean;
    }

    @Override
	public
    double getvar(double tf, double df, double idf, double DL, double avgDL, int DocNum, double CL, int CTF, int qTF, double a) {
        double u = DL*l/(1-l);
         double tempUP = tf + u*CTF/CL;
         double tempD = DL+u;
         double mean = tempUP/tempD;
         double var = tempUP*(tempD-tempUP)/(tempD*tempD*(tempD+1.0d));
         double newVar = var/(mean*mean*(1.0d-mean)*(1.0d-mean));
         if (GaussianTransformation)
             return newVar;
         else
            return var;//newVar;
    }

    @Override
	public
    double defaultmean(double tf, double df, double idf, double DL, double avgDL, int DocNum, double CL, int CTF, int qTF, double a) {
        double u = DL*l/(1-l);
         double tempUP = u*CTF/CL;
         double tempD = DL+u;
         double mean = tempUP/tempD;
         double var = tempUP*(tempD-tempUP)/(tempD*tempD*(tempD+1.0d));
         double adjustedMean = Math.log (mean/(1-mean))+ (2.0d*mean-1.0d)*var/(2.0d*mean*mean*(1.0d-mean)*(1.0d-mean));
         if (GaussianTransformation)
            return adjustedMean;//adjustedMean;
         else
             return Math.log(mean);
    }

    @Override
	public
    double defaultvar(double tf, double df, double idf, double DL, double avgDL, int DocNum, double CL, int CTF, int qTF, double a) {
        double u = DL*l/(1-l);
        double tempUP = u*CTF/CL;
         double tempD = DL+u;
         double mean = tempUP/tempD;
         double var = tempUP*(tempD-tempUP)/(tempD*tempD*(tempD+1.0d));
         double newVar = var/(mean*mean*(1.0d-mean)*(1.0d-mean));
         if (GaussianTransformation)
             return newVar;
         else
            return var;//newVar;
    }

	@Override
	public double getVSMscore(Vector<String> query,
			HashMap<String, Integer> TermVector) {
		// TODO Auto-generated method stub
		return 0;
	}


}
