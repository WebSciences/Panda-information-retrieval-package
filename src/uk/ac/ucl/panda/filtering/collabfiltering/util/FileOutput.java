package uk.ac.ucl.panda.filtering.collabfiltering.util;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.Flushable;
import java.io.IOException;

/**
 * A class to enable the writing of values to a text file without having to deal with
 * exceptions.  If any errors occur, an error message is displayed and the program is
 * terminated.
 * <p/>
 * <p>This class is intended for learners of Java to be able to use file output before having
 * learnt about exceptions.  Once exceptions have been learnt about this class loses its
 * usefulness and the standard library classes should be used directly.</p>
 *
 * @author Graham Roberts
 * @author Russel Winder
 * @version 2005-08-10
 */
public class FileOutput implements Closeable, Flushable
{
  /**
   * The name of the file being written to.
   */
  protected final String filename;
  /**
   * The writer to the output stream connected to the file being written to.  Use
   * <code>BufferedWriter</code> not <code>Writer</code> as we want access to the
   * <code>newLine</code> method.
   */
  protected final BufferedWriter writer;

  /**
   * Constructor of a <code>FileOutput</code> object given a file name string.
   * Setting append to true will open the file in append mode.
   */
  public FileOutput(final String filename, boolean append)
  {
    this.filename = filename;
    //  Because the FileWriter constructor can throw an IOException we have to protect its call with
    //  a try/catch block.  The BufferedWriter constructor does not throw an exception so no
    //  try/catch block is needed which is fortunate since if it had to be in a try/catch block we
    //  could not make the writer field final because of the way the Java compilers do their flow
    //  analysis.
    FileWriter fw = null;
    try
    {
      fw = new FileWriter(filename,append);
    }
    catch (IOException e)
    {
      error("Cannot open file: " + filename);
    }
    writer = new BufferedWriter(fw);
  }

  /**
   * Constructor of a <code>FileOutput</code> object given a <code>File</code> object.
   * Setting append to true will open the file in append mode.
   */
  public FileOutput(final File file, boolean append)
  {
    this(file.getName(),append);
  }

  /**
   * Constructor of a <code>FileOutput</code> object given a file name string.
   * If the file already exists its existing contents will be overwritten.
   */
  public FileOutput(final String filename)
  {
    this(filename,false);
  }

  /**
   * Constructor of a <code>FileOutput</code> object given a <code>File</code> object.
   * If the file already exists its existing contents will be overwritten.
   */
  public FileOutput(final File file)
  {
    this(file.getName(),false);
  }

  /**
   * Finalizer to close the file in case the using code fails to and a <code>FileOutput</code>
   * object gets garbage collected.
   */
  public void finalize()
  {
    close();
  }

  /**
   * Flush the associated writer.
   */
  public final synchronized void flush()
  {
    try
    {
      writer.flush();
    }
    catch (IOException e)
    {
      error("Cannot flush file: " + filename);
    }
  }

  /**
   * Close the file as it is finished with.
   */
  public final synchronized void close()
  {
    flush();
    try
    {
      writer.close();
    }
    catch (IOException e)
    {
      error("Cannot close file: " + filename);
    }
  }

  /**
   * Write an <code>int</code> value to a file.
   */
  public final synchronized void writeInt(final int i)
  {
    try
    {
      writer.write(Integer.toString(i));
    }
    catch (IOException e)
    {
      error("writeInteger failed to file: " + filename);
    }
  }

  /**
   * Write a <code>long</code> value to a file.
   */
  public final synchronized void writeLong(final long l)
  {
    try
    {
      writer.write(Long.toString(l));
    }
    catch (IOException e)
    {
      error("writeLong failed to file: " + filename);
    }
  }

  /**
   * Write a <code>float</code> value to a file.
   */
  public final synchronized void writeFloat(final float f)
  {
    try
    {
      writer.write(Float.toString(f));
    }
    catch (IOException e)
    {
      error("writeFloat failed to file: " + filename);
    }
  }

  /**
   * Write a <code>double</code> value to a file.
   */
  public final synchronized void writeDouble(final double d)
  {
    try
    {
      writer.write(Double.toString(d));
    }
    catch (IOException e)
    {
      error("writeDouble failed to file: " + filename);
    }
  }

  /**
   * Write a <code>char</code> value to a file.
   */
  public final synchronized void writeChar(final char c)
  {
    try
    {
      writer.write(c);
    }
    catch (IOException e)
    {
      error("writeChar failed to file: " + filename);
    }
  }

  /**
   * Write a <code>String</code> value to a file.
   */
  public final synchronized void writeString(final String s)
  {
    try
    {
      writer.write(s);
    }
    catch (IOException e)
    {
      error("writeString failed to file: " + filename);
    }
  }

  /**
   * Write an end-of-line marker to a file.  This writes whatever characters represent the
   * end-of-line marker on the operating system the program is run on.
   */
  public final synchronized void writeEndOfLine()
  {
    try
    {
      writer.newLine();
    }
    catch (IOException e)
    {
      error("writeEndOfLine failed to file: " + filename);
    }
  }

  /**
   * Deal with an error.  All errors a terminal with this class.
   */
  private void error(final String message)
  {
    System.err.println(message);
    System.err.println("Unable to continue executing program.");
    System.exit(1);
  }
}
