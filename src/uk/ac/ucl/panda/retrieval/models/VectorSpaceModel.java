package uk.ac.ucl.panda.retrieval.models;
import java.util.HashMap;
import java.util.Vector;

/**
*
*	@author Marc Sloan, Abhishek Aggarwal
* 	@author xxms
*/

public class VectorSpaceModel implements Model {
	/**
	 * This method should only be used in the Vector Space Model class. This
	 * method will be called instead of the getScore method below
	 * @param query:  A Vector of Strings representing the query
	 * @param termVector: HashMap linking each term in the document with its term frequency*/
	@Override
	public double getVSMscore(Vector<String> query,
	/* Implementing nnc.nnc VSM*/
			HashMap<String, Integer> TermVector) {
		// Write Vector Space Model code here
		
		//Constructing Query and Document Vectors 
		Vector<Double> qVector = new Vector<Double>();	//Query Vector
		Vector<Double> dVector = new Vector<Double>();	//Document Vector 
		
		for( int q=0; q< query.size(); q++){
			String qTerm = query.get(q);
			qVector.add(1.0);          // query term frequency for query vector   
			if (TermVector.get(qTerm) == null){
				//normFactor += 0.0;				
				dVector.add(0.0);
			}else{
				int termFreq = TermVector.get(qTerm);
				//normFactor += termFreq*termFreq;
				dVector.add((double)termFreq);   //term frequency for document vector
			}
		}
		
		//Cosine Normalization
		//for query vector
		qVector = normalize(qVector, Math.sqrt((double)qVector.size()));
		//for document vector
		double normFactor = 0;			// Cosine Normalization for Document Vector
		Integer[] freqs = new Integer[TermVector.values().toArray().length];
		freqs = TermVector.values().toArray(freqs);
		for (int i=0; i <freqs.length; i++){
			normFactor += freqs[i]*freqs[i];
		}
		normFactor = Math.sqrt(normFactor);
		dVector = normalize(dVector, normFactor);
		
		//returning the score
		return getSimilarity(qVector, dVector); // return vector space model score here
	}
	
	public Vector<Double> normalize(Vector<Double> vect, double normFactor){
		//Divides each element of vector vect with normFactor
		for (int i=0; i< vect.size(); i++){
			vect.set(i, vect.get(i)/normFactor);
		}
		return vect;
	}
	
	public double getSimilarity(Vector<Double> v1, Vector<Double> v2){
		//takes the inner product of vectors v1 and v2
		if (v1.size() != v2.size()){
			System.out.println("Vectors must be of same length!!");
			return -1.0;
		}
		double score = 0.0;
		for(int i=0; i< v1.size(); i++){
			score += v1.get(i)*v2.get(i);
		}
		return score;
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
