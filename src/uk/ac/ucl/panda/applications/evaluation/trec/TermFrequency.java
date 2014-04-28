package uk.ac.ucl.panda.applications.evaluation.trec;

public class TermFrequency implements Comparable <TermFrequency> {

	private double freq;
	private String term;

	public TermFrequency(String term, double freq ) { 
		this.freq= freq;
		this.term = term;
		}
	
	public String getTerm() {
		return term;
	}
	
	public double getFreq() {
		return freq;
	}
	// descending sort
	public int compareTo(TermFrequency anotherInstance) {

		 if (this.freq- anotherInstance.freq <=0) {
			 return 1;
		 }
		 else {
			 return -1;
		 }
	}

}
