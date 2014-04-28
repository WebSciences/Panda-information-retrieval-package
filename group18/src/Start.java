
public class Start {
	
	public static String fileId = "07";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		try {
			ReadWarc.read(fileId + ".warc.gz");
		} catch (Exception ex) {
			System.err.println(ex);
		}

	}

}
