package uk.ac.ucl.panda.map;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import uk.ac.ucl.panda.map.ResultsList.Result;
import uk.ac.ucl.panda.utility.io.FileReader;

/**
 * Use this class to implement the Mean Average Precision (MAP) metric by
 * completing the getMAPScore() method below.
 * 
 * @author Marc Sloan
 * 
 */
public class MAPScore {

	/**
	 * ----------------------------------------------- 
	 * Implement this method to
	 * calculate the MAP score. The getResultsFromFile method below locates the
	 * results file and stores them in a ResultsList object. Likewise the
	 * getQRelsFromFile method locates the qrels file and stores the qrels in a
	 * QRelList object
	 * 
	 * @return the MAP score
	 * 
	 * @see uk.ac.ucl.panda.map.QRelList
	 * @see uk.ac.ucl.panda.map.ResultsList
	 */
	public static double getMAPScore() {
		ResultsList results = getResultsFromFile();
		QRelList qrels = getQRelsFromFile();

		/**
		 * Your solution here
		 */

		return 0.0; //return MAP score here
	}

	/**
	 * Can be used to run your getMAPScore() method and prints out the MAP score for testing purposes
	 */
	public static void main(String args[]) {
		System.out.println("MAP = " + getMAPScore());
	}

	/**
	 * etc and var folder locations
	 */
	private final static String PANDA_ETC = System.getProperty("panda.etc",
			"../Panda/etc/");
	private final static String PANDA_VAR = System.getProperty("panda.var",
			"../Panda/var/");
	protected static String fileseparator = System
			.getProperty("file.separator");

	/**
	 * This method locates the results file (by looking in the Panda/var/
	 * folder), opens it, then iterates through every line, saving each
	 * individual ranked document for each topic in a ResultsList object
	 * 
	 * @return ResultsList representing the results file
	 * 
	 * @see uk.ac.ucl.panda.map.ResultsList
	 */
	public static ResultsList getResultsFromFile() {
		// Opens the results file in the Panda/var/ folder
		FileInputStream resultsFile = null;
		ResultsList resultsArray = new ResultsList();
		try {
			resultsFile = new FileInputStream(PANDA_VAR + fileseparator
					+ "results");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if (resultsFile == null)
			return resultsArray;

		// Read in the file
		DataInputStream resultsStream = new DataInputStream(resultsFile);
		BufferedReader results = new BufferedReader(new InputStreamReader(
				resultsStream));

		StringTokenizer rToken;
		String rLine;
		String topic;
		String docID;
		String rank;
		String score;

		try {
			// iterate through every line in the file
			while ((rLine = results.readLine()) != null) {
				rToken = new StringTokenizer(rLine);
				// extract the meaningful information
				topic = rToken.nextToken();
				rToken.nextToken();
				docID = rToken.nextToken();
				rank = rToken.nextToken();
				score = rToken.nextToken();

				// add this result to our ResultsList
				resultsArray.addResult(Integer.parseInt(topic), docID,
						Integer.parseInt(rank), Double.parseDouble(score));

			}
			if (resultsFile != null)
				resultsFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return resultsArray;
	}

	/**
	 * This method locates the qrels file (by looking in the Panda/etc/ folder
	 * for the QRel.config file and then locating the qrels file location),
	 * opens it, then iterates through every line, saving each individual
	 * document judgement for each topic in a QRelList object
	 * 
	 * @return QRelList representing the qrels file
	 * 
	 * @see QRelList
	 */
	public static QRelList getQRelsFromFile() {
		QRelList qrelList = new QRelList();
		BufferedReader buf = null;
		try {
			// Opens the qrels.config file in the Panda/etc/ folder
			buf = FileReader.openFileReader(PANDA_ETC + fileseparator
					+ "Qrels.config");

			String qrelString;

			// get the qrels file location
			qrelString = buf.readLine();
			FileInputStream qrelsFile = new FileInputStream(qrelString);

			buf.close();

			// read in the qrels file
			DataInputStream qrelsStream = new DataInputStream(qrelsFile);
			BufferedReader qrels = new BufferedReader(new InputStreamReader(
					qrelsStream));

			String qLine;
			StringTokenizer qToken;
			String topic;
			String docID;
			String relevance;

			// iterate through the qrels file line by line
			while ((qLine = qrels.readLine()) != null) {
				qToken = new StringTokenizer(qLine);

				// extract the meaningful information
				topic = qToken.nextToken();
				qToken.nextToken();
				docID = qToken.nextToken();
				relevance = qToken.nextToken();

				// store this qrel in the QRelList
				qrelList.addQRel(Integer.parseInt(topic), docID,
						(relevance.equals("0")) ? false : true);
			}

			if (qrelsStream != null)
				qrelsStream.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return qrelList;

	}
}