/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.ucl.panda.retrieval.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;


/**
 * Selects which IR model to use when evaluating 
 * @author Marc Sloan
 */
public class ModelParser {

    int model;
    Model algorithm;
    
    /**
     *  Lists all of the available IR models 
     *  
     *  Add your own IR model here, simply add another line of code like this:
     * 		models.add('your_IR_model'.class); 
     */
    private static ArrayList<Class<? extends Model>> models = new ArrayList<Class<? extends Model>>();
    static{
    	models.add(TFIDF.class); // model number = 0
    	models.add(VectorSpaceModel.class); // model number = 1
    	models.add(BM25.class); // model number = 2
    	models.add(Language_u.class); // model number = 3
    	models.add(Language_AdV.class); // model number = 4
    	models.add(Language_lambda.class); // model number = 5
    	models.add(BM25PNW.class); // model number = 6
    	// Add new IR models here, remember its position in 'models' is also the model number
    	//	 models.add('your_IR_model'.class);
    	
    }
   
    
    ModelParser(int m) {
        this.model=m;
        try {
			algorithm = (models.get(m)).newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static int getNumberOfModels(){
    	return models.size();
    }

    public double getscore(double tf, double df, double idf, double DL, double avgDL, int DocNum, double CL, int CTF, int qTF){
        return  algorithm.getscore(tf,df,idf,DL,avgDL, DocNum, CL, CTF, qTF);
    }

   public double defaultScore(double tf, double df, double idf, double DL, double avgDL, int DocNum, double CL, int CTF, int qTF) {
        return algorithm.defaultScore( tf, df, idf, DL, avgDL, DocNum, CL,CTF, qTF);
    }

    public double defaultScore(double tf, double df, double idf, double DL, double avgDL, int DocNum, double CL, int CTF, int qTF, double a) {
        return algorithm.defaultScore( tf, df, idf, DL, avgDL, DocNum, CL,CTF, qTF, a);
    }

    double getscore(double tf, double df, double idf, double DL, double avgDL, int DocNum, double CL, int CTF, int qTF, double a) {    
        return  algorithm.getscore(tf,df,idf,DL,avgDL, DocNum, CL, CTF, qTF,a);
    }

    double getvar(double tf, double df, double idf, double DL, double avgDL, int DocNum, double CL, int CTF, int qTF, double a) {
        return  algorithm.getvar(tf,df,idf,DL,avgDL, DocNum, CL, CTF, qTF,a);
    }

    double getmean(double tf, double df, double idf, double DL, double avgDL, int DocNum, double CL, int CTF, int qTF, double a) {
        return  algorithm.getmean(tf,df,idf,DL,avgDL, DocNum, CL, CTF, qTF,a);
    }

       double defaultvar(double tf, double df, double idf, double DL, double avgDL, int DocNum, double CL, int CTF, int qTF, double a) {
        return  algorithm.defaultvar(tf,df,idf,DL,avgDL, DocNum, CL, CTF, qTF,a);
    }

    double defaultmean(double tf, double df, double idf, double DL, double avgDL, int DocNum, double CL, int CTF, int qTF, double a) {
        return  algorithm.defaultmean(tf,df,idf,DL,avgDL, DocNum, CL, CTF, qTF,a);
    }
    
    public double getVSMscore(Vector<String> query, HashMap<String, Integer> TermVector) {
		return algorithm.getVSMscore(query, TermVector);
	}

}
