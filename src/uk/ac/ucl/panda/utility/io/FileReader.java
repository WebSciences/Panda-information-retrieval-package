/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 
 * Read file from compressed file ".ZIP",".GZ",".Z"
 */

package uk.ac.ucl.panda.utility.io;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author xxms
 */
public class FileReader {

    
	/** Opens a reader to the file called file. Provided for easy overriding for encoding support etc in 
	  * child classes. Called from openNextFile().
	  * @param file File to open.
	  * @return BufferedReader of the file
	  */
	public static BufferedReader openFileReader(File file) throws IOException
	{
		return openFileReader(file.getPath(),null);
	}

	/** Opens a reader to the file called filename. Provided for easy overriding for encoding support etc in
	  * child classes. Called from openNextFile().
	  * @param file File to open.
	  * @param charset Character set encoding of file. null for system default.
	  * @return BufferedReader of the file
	  */
	public static BufferedReader openFileReader(File file, String charset) throws IOException
	{
		return openFileReader(file.getPath(), charset);
	}

	/** Opens a reader to the file called filename. Provided for easy overriding for encoding support etc in
	  * child classes. Called from openNextFile().
	  * @param filename File to open.
	  * @return BufferedReader of the file
	  */
	public static BufferedReader openFileReader(String filename) throws IOException
	{
		return openFileReader(filename,null);
	}
	

	/** Opens a reader to the file called filename. Provided for easy overriding for encoding support etc in 
	  * child classes. Called from openNextFile().
	  * @param filename File to open.
	  * @param charset Character set encoding of file. null for system default.
	  * @return BufferedReader of the file
	  */
	public static BufferedReader openFileReader(String filename, String charset) throws IOException
	{
		BufferedReader rtr = null;
		if (filename.toLowerCase().endsWith("zip")) {
                    ZipInputStream in = new ZipInputStream(new FileInputStream(filename));
                     // Get the first entry
		        ZipEntry entry = in.getNextEntry();
                    rtr = new BufferedReader(
				 charset == null				
				? new InputStreamReader(in)
				: new InputStreamReader(in, charset)
			);
		} 
                else if (filename.toLowerCase().endsWith("gz")) {
			rtr = new BufferedReader(
				 charset == null				
				? new InputStreamReader(new GZIPInputStream(new FileInputStream(filename)))
				: new InputStreamReader(new GZIPInputStream(new FileInputStream(filename)), charset)
			);
		} 
		else if (filename.toLowerCase().endsWith("z")){
			rtr = new BufferedReader(
					 charset == null				
					? new InputStreamReader(new ZcompressInputStream(new FileInputStream(filename)))
					: new InputStreamReader(new ZcompressInputStream(new FileInputStream(filename)), charset)
				);
			
		}
		else {
			rtr = new BufferedReader(
				charset == null 
					? new InputStreamReader(new FileInputStream(filename))// new FileReader(filename)
					: new InputStreamReader(new FileInputStream(filename), charset)
				);
		}
		return rtr;
	}

	/** Opens an InputStream to a file called file. 
	  * @param file File to open.
	  * @return InputStream of the file
	  */
	public static InputStream openFileStream(File file) throws IOException
	{
		return openFileStream(file.getPath());
	}

	

	/** Opens an InputStream to a file called filename.
	  * @param filename File to open.
	  * @return InputStream of the file
	  */
	public static InputStream openFileStream(String filename) throws IOException
	{
		BufferedInputStream rtr = null;
		if (filename.toLowerCase().endsWith("zip")) {
                     ZipInputStream in = new ZipInputStream(new FileInputStream(filename));
                     // Get the first entry
		        ZipEntry entry = in.getNextEntry();
			rtr = new BufferedInputStream(in);
		} 
                else if (filename.toLowerCase().endsWith("gz")) {
			rtr = new BufferedInputStream(
				new GZIPInputStream(new FileInputStream(filename)));
		} 
		else if(filename.toLowerCase().endsWith("z")){
			rtr = new BufferedInputStream(
					new ZcompressInputStream(new FileInputStream(filename)));
			
		}
		else {
			rtr = new BufferedInputStream(new FileInputStream(filename));
		}
		return (InputStream) rtr;
	}

    
    
    
    
}
