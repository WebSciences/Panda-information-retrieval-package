package uk.ac.ucl.panda;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Properties;

import uk.ac.ucl.panda.indexing.TrecIndex;
import uk.ac.ucl.panda.retrieval.TrecRetrieval;
import uk.ac.ucl.panda.retrieval.models.ModelParser;
import uk.ac.ucl.panda.utility.io.FileReader;

/**
 * This is the main class for the Panda platform. 'Run' this class with the following arguments 
 * to perform the required action.
 * 
 * -h --help		print this message
 * -V --version		print version information
 * -i --index	   	index a collection
 * -b --batch		retrieve for batch, must be followed by argument of the form a:i:b, where a is the starting value b is the end value and i is the increment
 * -r --reranking   perform a reranking evaluation,
 *                  followed by arguments specifying of the form method:model,
 *                  where method is {mmr|portfolio>} and model is the number of  underlying scoring model.
 * -br --batchreranking  batch reranking, format: method:model:a:i:b
 * -e --evaluate	evaluates the results
 * -v --var   		get var and mean for each query
 * -m --model   	specify model number (must be followed by an integer and used in conjunction with other arguments)
 */
public class Panda {

	// Paths
	private final String INDEX = "../Index/";
	private final String DATA  = "../Data/Documents/";
	private final static String PANDA_ETC  = System.getProperty("panda.etc", "./etc/");
	private final static String PANDA_VAR  = System.getProperty("panda.var", "./var/");
	private final static String PANDA_HOME = System.getProperty("panda.home", "./");
	
	protected String newline = System.getProperty("line.separator");

	protected String fileseparator = System.getProperty("file.separator");

	protected BufferedReader buf = null;
	/** The unknown option */
	protected String unknownOption;

	/** Specifies whether a help message is printed */
	protected boolean printHelp;

	/** Specified whether a version message is printed */
	protected boolean printVersion;

	/** Specifies whether to index a collection */
	protected boolean indexing;

	/**
	 * Specifies whether to perform trec_eval like evaluation.
	 */
	protected boolean evaluation;
	
	/**
	 * Specifies whether to perform a reranking task and evaluation.
	 */
	protected boolean reranking;
	
	/**
	 * Specifies batch reranking
	 */
	protected boolean batch_reranking;

	/**
	 * The reranking method, "mmr" or "portfolio"
	 */
	protected String reranking_method;
	
	/**
	 * Specifies batch.
	 */
	protected boolean batch;

	/**
	 * Specifies var and mean.
	 */
	protected boolean variance;

	protected boolean plot;
	
	/**
	 * Number of the model to use when evaluating (see ModelParser for more details)
	 */
	protected int modelNumber;
	
	double batchA = 0, batchB = 0, batchIncrement = 0;

	protected void version() {
		System.out.println("LuceneTrec version: 1.0 alpha");
		// System.out.println("Built on ");
	}

	/**
	 * Prints a help message that explains the possible options.
	 */
	protected void usage() {
		System.out.println("LuceneTrec version: 1.0 alpha");
		System.out.println("usage: java LuceneTrec [flags in any order]");
		System.out.println("");
		System.out.println("  -h --help		print this message");
		System.out.println("  -V --version	 print version information");
		System.out.println("  -i --index	   index a collection");
		System.out.println("  -b --batch	retrieve for batch, must be followed by argument of the form a:i:b, where a is the starting value,");
		System.out.println("                  b is the end value and i is the increment");
		System.out.println("  -e --evaluate	evaluates the results");
		
		System.out.println("  -r --reranking   perform a reranking evaluation,");
		System.out.println("                   followed by arguments specifying of the form method:model,");
		System.out.println("                   where method is {mmr|portfolio} and model is the number of  underlying scoring model.");
		System.out.println("  -br --batchreranking  batch reranking, format: method:model:a:i:b");

		System.out.println("  -v --var   get var and mean for each query");
		System.out.println("				   var/results with the specified qrels file");
		System.out.println("				   in the file etc/trec.qrels");
		System.out.println("  -m --model   	specify model number (must be followed by an integer and used in conjunction with other arguments)");
		System.out.println("");
		System.out.println("If invoked with \'-i\', then both the direct and");
		System.out
		.println("inverted files are build, unless it is specified which");
		System.out.println("of the structures to build.");
	}

	/**
	 * set config files
	 */

	private static void setproperties(Properties appProp) {
		appProp.setProperty("panda.home", PANDA_HOME);
		appProp.setProperty("panda.var", PANDA_VAR);
		appProp.setProperty("panda.etc", PANDA_ETC);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException, Exception {
		Properties appProp = new Properties();
		setproperties(appProp);
		try {
			Panda panda = new Panda();
			int status = panda.processOptions(args);
			panda.applyOptions(status, appProp);
		} catch (java.lang.OutOfMemoryError oome) {
			System.err.println(oome);
			oome.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Processes the command line arguments and sets the corresponding
	 * properties accordingly.
	 * 
	 * @param args
	 *            the command line arguments.
	 * @return int zero if the command line arguments are processed
	 *         successfully, otherwise it returns an error code.
	 */
	protected int processOptions(String[] args) {
		if (args.length == 0)
			return ERROR_NO_ARGUMENTS;
		boolean hasModelFlag = false;
		boolean isBatch = false;
		boolean isReranking = false;
		boolean isBatchReranking = false;
		int pos = 0;
		while (pos < args.length) {
			if(hasModelFlag){
				hasModelFlag = false;
				try {
			    	modelNumber = Integer.parseInt(args[pos]); //see if one of the arguments is a model number
			    	if(modelNumber < 0 || modelNumber >= ModelParser.getNumberOfModels())
			    		return ERROR_WRONG_MODEL_NUMBER;
			    } catch(NumberFormatException e) { 
			    	modelNumber = -1;
			    	return ERROR_WRONG_MODEL_NUMBER;
			    }
			}else if(isBatch){
				isBatch = false;
				String[] vars = args[pos].split(":");
				if(vars.length != 3)
					return ERROR_WRONG_BATCH_ARGUMENT;
				try{
					batchA = Double.parseDouble(vars[0]);
					batchIncrement = Double.parseDouble(vars[1]);
					batchB = Double.parseDouble(vars[2]);
				}catch(NumberFormatException e){
					batchA = 0;
					batchB = 0;
					batchIncrement = 0;
					return ERROR_WRONG_BATCH_ARGUMENT;
				}
			} else if (isBatchReranking) {
				isBatchReranking = false;
				String[] vars = args[pos].split(":");
				if(vars.length != 5)
					return ERROR_WRONG_BATCH_ARGUMENT;
				try{
					reranking_method = vars[0].toLowerCase();
					if(!reranking_method.equals("mmr") && ! reranking_method.equals("portfolio")) {
						return ERROR_WRONG_RERANKING_ARGUMENT;
					}
					modelNumber = Integer.parseInt(vars[1]);
			    	if(modelNumber < 0 || modelNumber >= ModelParser.getNumberOfModels())
			    		return ERROR_WRONG_MODEL_NUMBER;
					batchA = Double.parseDouble(vars[2]);
					batchIncrement = Double.parseDouble(vars[3]);
					batchB = Double.parseDouble(vars[4]);
				}catch(NumberFormatException e){
					reranking_method = null;
					modelNumber = -1;
					batchA = 0;
					batchB = 0;
					batchIncrement = 0;
					return ERROR_WRONG_BATCH_ARGUMENT;
				}
			} else if (isReranking){
				isReranking = false;
				String[] vars = args[pos].split(":");
				if(vars.length != 2)
					return ERROR_WRONG_RERANKING_ARGUMENT;
				try{
					reranking_method = vars[0].toLowerCase();
					if(!reranking_method.equals("mmr") && ! reranking_method.equals("portfolio")) {
						return ERROR_WRONG_RERANKING_ARGUMENT;
					}
					modelNumber = Integer.parseInt(vars[1]);
			    	if(modelNumber < 0 || modelNumber >= ModelParser.getNumberOfModels())
			    		return ERROR_WRONG_MODEL_NUMBER;
				}catch(NumberFormatException e){
					reranking_method = null;
					modelNumber = -1;
					return ERROR_WRONG_RERANKING_ARGUMENT;
				}
			}else if (args[pos].equals("-h") || args[pos].equals("--help"))
				printHelp = true;

			else if (args[pos].equals("-i") || args[pos].equals("--index"))
				indexing = true;

			else if (args[pos].equals("-v") || args[pos].equals("--var"))
				variance = true;

			else if (args[pos].equals("-e") || args[pos].equals("--evaluate")) {
				evaluation = true;
			} else if (args[pos].equals("-r") || args[pos].equals("--reranking")) {
				reranking = true;
				isReranking = true;
			} else if (args[pos].equals("-br") || args[pos].equals("--batchreranking")) {
				batch_reranking = true;
				isBatchReranking = true;
			} else if (args[pos].equals("-b") || args[pos].equals("--batch")) {
				batch = true;
				isBatch = true;
			} else if (args[pos].equals("-p") || args[pos].equals("--plot")) {
				plot = true;
			} else if (args[pos].equals("-m") || args[pos].equals("--model")) {
				hasModelFlag = true;
			}else {
			    unknownOption = args[pos];
				return ERROR_UNKNOWN_OPTION;

			}
			pos++;
		}

		return ARGUMENTS_OK;
	}

	/**
	 * Calls the required classes from Terrier.
	 */
	public void run(Properties appProp) throws IOException, Exception {
		if (printVersion) {
			version();
			return;
		}
		if (printHelp) {
			usage();
			return;
		}
		long startTime = System.currentTimeMillis();
		if (batch) {
			buf = FileReader.openFileReader(appProp.getProperty("panda.etc")
					+ fileseparator + "IndexDir.config");
			String index = buf.readLine();

			buf = FileReader.openFileReader(appProp.getProperty("panda.etc")
					+ fileseparator + "Topics.config");
			String topics = buf.readLine();

			buf = FileReader.openFileReader(appProp.getProperty("panda.etc")
					+ fileseparator + "Qrels.config");
			String qrels = buf.readLine();
			// System.out.println(index);
			// System.out.println(topics);
			// System.out.println(qrels);
			TrecRetrieval trecsearch = new TrecRetrieval();
			trecsearch.batch(index, topics, qrels, appProp
					.getProperty("panda.var"), modelNumber, batchA, batchB, batchIncrement);
		}
		else if (evaluation) {
			buf = FileReader.openFileReader(appProp.getProperty("panda.etc")
					+ fileseparator + "IndexDir.config");
			String index = buf.readLine();

			buf = FileReader.openFileReader(appProp.getProperty("panda.etc")
					+ fileseparator + "Topics.config");
			String topics = buf.readLine();

			buf = FileReader.openFileReader(appProp.getProperty("panda.etc")
					+ fileseparator + "Qrels.config");
			String qrels = buf.readLine();

			// System.out.println(index);
			// System.out.println(topics);
			// System.out.println(qrels);

			TrecRetrieval trecsearch = new TrecRetrieval();
			trecsearch.process(index, topics, qrels, appProp
					.getProperty("panda.var"), modelNumber);
		} else if (reranking) {
			buf = FileReader.openFileReader(appProp.getProperty("panda.etc")
					+ fileseparator + "IndexDir.config");
			String index = buf.readLine();

			buf = FileReader.openFileReader(appProp.getProperty("panda.etc")
					+ fileseparator + "Topics.config");
			String topics = buf.readLine();

			buf = FileReader.openFileReader(appProp.getProperty("panda.etc")
					+ fileseparator + "Qrels.config");
			String qrels = buf.readLine();

			// reranking
			TrecRetrieval trecsearch = new TrecRetrieval();
			trecsearch.process_reranking(index, topics, qrels, appProp
					.getProperty("panda.var"), reranking_method, modelNumber);
		} else if (batch_reranking) {
			buf = FileReader.openFileReader(appProp.getProperty("panda.etc")
					+ fileseparator + "IndexDir.config");
			String index = buf.readLine();

			buf = FileReader.openFileReader(appProp.getProperty("panda.etc")
					+ fileseparator + "Topics.config");
			String topics = buf.readLine();

			buf = FileReader.openFileReader(appProp.getProperty("panda.etc")
					+ fileseparator + "Qrels.config");
			String qrels = buf.readLine();

			// batch reranking
			TrecRetrieval trecsearch = new TrecRetrieval();
			trecsearch.batch_reranking(index, topics, qrels, appProp
					.getProperty("panda.var"), reranking_method, modelNumber,
					batchA, batchB, batchIncrement);
		} else if (variance) {
			buf = FileReader.openFileReader(appProp.getProperty("panda.etc")
					+ fileseparator + "IndexDir.config");
			String index = buf.readLine();

			buf = FileReader.openFileReader(appProp.getProperty("panda.etc")
					+ fileseparator + "Topics.config");
			String topics = buf.readLine();

			buf = FileReader.openFileReader(appProp.getProperty("panda.etc")
					+ fileseparator + "Qrels.config");
			String qrels = buf.readLine();

			// System.out.println(index);
			// System.out.println(topics);
			// System.out.println(qrels);

			TrecRetrieval trecsearch = new TrecRetrieval();
			trecsearch.process_var(index, topics, qrels, appProp
					.getProperty("panda.var"));

		} else if (plot) {
			buf = FileReader.openFileReader(appProp.getProperty("panda.etc")
					+ fileseparator + "IndexDir.config");
			String index = buf.readLine();

			buf = FileReader.openFileReader(appProp.getProperty("panda.etc")
					+ fileseparator + "Topics.config");
			String topics = buf.readLine();

			buf = FileReader.openFileReader(appProp.getProperty("panda.etc")
					+ fileseparator + "Qrels.config");
			String qrels = buf.readLine();

			// System.out.println(index);
			// System.out.println(topics);
			// System.out.println(qrels);

			TrecRetrieval trecsearch = new TrecRetrieval();
			trecsearch.process_plot(index, topics, qrels, appProp
					.getProperty("panda.var"));

		} else if (indexing) {

			buf = FileReader.openFileReader(appProp.getProperty("panda.etc")
					+ fileseparator + "IndexDir.config");
                        String index = buf.readLine();

			buf = FileReader.openFileReader(appProp.getProperty("panda.etc")
					+ fileseparator + "DataDir.config");
                        String data = buf.readLine();
			TrecIndex trecindex = new TrecIndex();
			// System.out.println(appProp);
			// System.s(0);
			//trecindex.pocess(INDEX, DATA, appProp);
                        trecindex.pocess(index, data, appProp);
		}

		long endTime = System.currentTimeMillis();
		System.err.println("Time elapsed: " + (endTime - startTime) / 1000.0d
				+ " seconds.");
	}

	public void applyOptions(int status, Properties appProp)
	throws IOException, Exception {
		switch (status) {
		case ERROR_NO_ARGUMENTS:
			usage();
			break;
		case ERROR_NO_C_VALUE:
			System.err
			.println("A value for the term frequency normalisation parameter");
			System.err
			.println("is required. Please specify it with the option '-c value'");
			break;
		case ERROR_CONFLICTING_ARGUMENTS:
			System.err
			.println("There is a conclict between the specified options. For example,");
			System.err
			.println("option '-c' is used only in conjuction with option '-r'.");
			System.err
			.println("In addition, options '-v' or '-d' are used only in conjuction");
			System.err.println("with option '-i'");
			break;
		case ERROR_DIRECT_FILE_EXISTS:
			System.err
			.println("Trying to build a new direct file, while there exists a previous");

			break;
		case ERROR_DIRECT_FILE_NOT_EXISTS:
			System.err
			.println("Trying to build an inverted file, while there is no direct file.");
			break;
		case ERROR_INVERTED_FILE_EXISTS:
			System.err
			.println("Trying to build a new inverted file, while there exists a previous. Please delete the .if file in the index directory.");
			System.err.println("inverted file.");
			break;
		case ERROR_PRINT_DOCINDEX_FILE_NOT_EXISTS:
			System.err
			.println("The specified document index file does not exist.");
			break;
		case ERROR_PRINT_INVERTED_FILE_NOT_EXISTS:
			System.err.println("The specified inverted index does not exist.");
			break;
		case ERROR_PRINT_DIRECT_FILE_NOT_EXISTS:
			System.err.println("The specified direct index does not exist.");
			break;
		case ERROR_UNKNOWN_OPTION:
			System.err.println("The option '" + unknownOption
					+ "' is not recognised");
			break;
		case ERROR_DIRECT_NOT_INDEXING:
			System.err
			.println("The option '-d' or '--direct' can be used only while indexing with option '-i'.");
			break;
		case ERROR_INVERTED_NOT_INDEXING:
			System.err
			.println("The option '-i' or '--inverted' can be used only while indexing with option '-i'.");
			break;
		case ERROR_EXPAND_NOT_RETRIEVE:
			System.err
			.println("The option '-q' or '--queryexpand' can be used only while retrieving with option '-r'.");
			break;
		case ERROR_LANGUAGEMODEL_NOT_RETRIEVE:
			System.err
			.println("The option '-l' or '--langmodel' can be used only while retrieving with option '-r'.");
			break;
		case ERROR_GIVEN_C_NOT_RETRIEVING:
			System.err
			.println("A value for the parameter c can be specified only while retrieving with option '-r'.");
			break;
		case ERROR_WRONG_MODEL_NUMBER:
			System.err
			.println("The model number that you entered is incorrect, there are currently " + ModelParser.getNumberOfModels() + " models.");
			break;
		case ERROR_WRONG_BATCH_ARGUMENT:
			System.err
			.println("You entered an incorrect format for the batch argument, -b must be followed by argument of the form a:i:b, where a is the starting value b is the end value and i is the increment");
			break;
		case ERROR_WRONG_RERANKING_ARGUMENT:
			System.err
			.println("You entered an incorrect format for the reranking argument, -r must be followed by argument of the form method:model, where method is <mmr|portfolio> and model is the number of  underlying scoring model.");
			break;
		case ARGUMENTS_OK:
		default:
			run(appProp);
		}
	}

	protected static final int ARGUMENTS_OK = 0;
	protected static final int ERROR_NO_ARGUMENTS = 1;
	protected static final int ERROR_NO_C_VALUE = 2;
	protected static final int ERROR_CONFLICTING_ARGUMENTS = 3;
	protected static final int ERROR_DIRECT_FILE_EXISTS = 4;
	protected static final int ERROR_INVERTED_FILE_EXISTS = 5;
	protected static final int ERROR_DIRECT_FILE_NOT_EXISTS = 6;
	protected static final int ERROR_PRINT_DOCINDEX_FILE_NOT_EXISTS = 7;
	protected static final int ERROR_PRINT_LEXICON_FILE_NOT_EXISTS = 8;
	protected static final int ERROR_PRINT_INVERTED_FILE_NOT_EXISTS = 9;
	protected static final int ERROR_PRINT_STATS_FILE_NOT_EXISTS = 10;
	protected static final int ERROR_PRINT_DIRECT_FILE_NOT_EXISTS = 11;
	protected static final int ERROR_UNKNOWN_OPTION = 12;
	protected static final int ERROR_DIRECT_NOT_INDEXING = 13;
	protected static final int ERROR_INVERTED_NOT_INDEXING = 14;
	protected static final int ERROR_EXPAND_NOT_RETRIEVE = 15;
	protected static final int ERROR_GIVEN_C_NOT_RETRIEVING = 16;
	protected static final int ERROR_LANGUAGEMODEL_NOT_RETRIEVE = 17;
	protected static final int ERROR_WRONG_MODEL_NUMBER = 18;
	protected static final int ERROR_WRONG_BATCH_ARGUMENT = 19;
	protected static final int ERROR_WRONG_RERANKING_ARGUMENT = 20;

}
