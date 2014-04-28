package uk.ac.ucl.panda.filtering.collabfiltering.util;

import java.io.Closeable;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * An iterator class to input specific data types from an input stream without throwing any
 * exceptions.  If there are any errors during use of the iterator then a message is output to
 * the standard output and the program terminates.
 * <p/>
 * <p>This is just a simple wrapper around <code>Scanner</code> that implements a subset of the
 * <code>Scanner</code> public interface.</p>
 * <p/>
 * <p>This class is useful for people new to Java since it allows them to write programs using
 * input without having to fully understand the read / parse / handle exceptions model of the
 * standard Java classes and in particular <code>Scanner</code>.  So once exceptions and object
 * chaining are covered this class ought not to be used, it is definitely just an "early
 * stepping stone" utility class for initial learning.</p>
 *
 * @author Russel Winder
 * @version 2004-12-16
 */
public class Input implements Closeable, Iterator<String>
{
  /**
   * A reference to the associated <code>Scanner</code> that supplies all the actual input
   * functionality.
   * <p/>
   * <p>This is protected and not final rather than private and final (as we might have
   * expected it to be) so that <code>FileInput</code> can access the variable.  This is
   * necessary because <code>FileInput</code> needs to capture all exceptions that can happen
   * during construction, which means that the <code>super</code> constructor call cannot be
   * used.  This appears to be something of a misfeature of Java.</p>
   */
  protected Scanner scanner;

  /**
   * The default constructor of an <code>Input</code> that assumes <code>System.in</code> is to
   * be the <code>InputStream</code> used.
   */
  public Input()
  {
    this(System.in);
  }

  /**
   * Constructor of an <code>Input</code> object given an <code>InputStream</code> object.
   */
  public Input(final InputStream in)
  {
    scanner = new Scanner(in);
  }

  /**
   * A finalizer to ensure all files are closed if an <code>Input</code> object is garbage
   * collected.
   */
  public void finalize()
  {
    close();
  }

  /**
   * Close the file when finished with it.
   */
  public void close()
  {
    scanner.close();
  }

  /**
   * @return <code>true</code> if there is more input, <code>false</code> otherwise.
   */
  public boolean hasNext()
  {
    boolean returnValue = false;
    try
    {
      returnValue = scanner.hasNext();
    }
    catch (IllegalStateException e)
    {
      illegalStateExceptionHandler();
    }
    return returnValue;
  }

  /**
   * @return the next token (sequence of characters terminated by a whitespace) in the input
   * stream.
   */
  public String next()
  {
    String returnValue = null;
    try
    {
      returnValue = scanner.next();
    }
    catch (NoSuchElementException nsee)
    {
      noSuchElementHandler();
    }
    catch (IllegalStateException ise)
    {
      illegalStateExceptionHandler();
    }
    return returnValue;
  }

  /**
   * This operation is required in order to conform to <code>Iterator&lt;String></code> but is
   * not supported.  Normally an <code>UnsupportedOperationException</code> would be thrown to
   * indicate this situation but the whole point is not to throw exceptions so this is simply a
   * "do nothing" method.
   */
  public void remove()
  {
  }

  /**
   * NB This method currently has a mis-feature in that it returns false incorrectly when there
   * is a single end-of-line left in the file.
   *
   * @return <code>true</code> if there is a <code>char</code> to input, <code>false</code>
   * otherwise.
   */
  public boolean hasNextChar()
  {

    //  Why doesn't this work, it used to.

    //boolean returnValue = false ;
    //try { returnValue = scanner.hasNext ( "(?s)." ) ; }
    //catch ( IllegalStateException e ) { illegalStateExceptionHandler ( ) ; }
    //return returnValue ;

    return hasNext();
  }

  /**
   * @return the next <code>char</code> in the input stream.
   */
  public char nextChar()
  {
    char returnValue = '\0';
    try
    {
      returnValue = scanner.findWithinHorizon("(?s).", 1).charAt(0);
    }
    catch (IllegalArgumentException iae)
    {
      //  This cannot happen as it is clear in the statement that the horizon is 1 which is > 0 and
      //  this exception only happens for negative horizons.
      System.exit(1);
    }
    catch (IllegalStateException ise)
    {
      illegalStateExceptionHandler();
    }
    return returnValue;
  }

  /**
   * @return <code>true</code> if there is an <code>int</code> to input, <code>false</code>
   * otherwise.
   */
  public boolean hasNextInt()
  {
    boolean returnValue = false;
    try
    {
      returnValue = scanner.hasNextInt();
    }
    catch (IllegalStateException e)
    {
      illegalStateExceptionHandler();
    }
    return returnValue;
  }

  /**
   * @return the next <code>int</code> in the input stream assumed to be in the default radix
   * which is 10.
   */
  public int nextInt()
  {
    int returnValue = 0;
    try
    {
      returnValue = scanner.nextInt();
    }
    catch (InputMismatchException ime)
    {
      inputMismatchExceptionHandler("int");
    }
    catch (NoSuchElementException nsee)
    {
      noSuchElementHandler();
    }
    catch (IllegalStateException ise)
    {
      illegalStateExceptionHandler();
    }
    return returnValue;
  }

  /**
   * @param radix the radix of the input.
   * @return the next <code>int</code> in the input stream using the radix
   * <code>radix</code>.
   */
  public int nextInt(final int radix)
  {
    int returnValue = 0;
    try
    {
      returnValue = scanner.nextInt(radix);
    }
    catch (InputMismatchException ime)
    {
      inputMismatchExceptionHandler("int");
    }
    catch (NoSuchElementException nsee)
    {
      noSuchElementHandler();
    }
    catch (IllegalStateException ise)
    {
      illegalStateExceptionHandler();
    }
    return returnValue;
  }

  /**
   * @return <code>true</code> if there is a <code>long</code> to input, <code>false</code>
   * otherwise.
   */
  public boolean hasNextLong()
  {
    boolean returnValue = false;
    try
    {
      returnValue = scanner.hasNextLong();
    }
    catch (IllegalStateException e)
    {
      illegalStateExceptionHandler();
    }
    return returnValue;
  }

  /**
   * @return the next <code>long</code> in the input stream assumed to be in the default radix
   * which is 10.
   */
  public long nextLong()
  {
    long returnValue = 0;
    try
    {
      returnValue = scanner.nextLong();
    }
    catch (InputMismatchException ime)
    {
      inputMismatchExceptionHandler("long");
    }
    catch (NoSuchElementException nsee)
    {
      noSuchElementHandler();
    }
    catch (IllegalStateException ise)
    {
      illegalStateExceptionHandler();
    }
    return returnValue;
  }

  /**
   * @param radix the radix of the input sequence.
   * @return the next <code>long</code> in the input stream using the radix
   * <code>radix</code>.
   */
  public long nextLong(final int radix)
  {
    long returnValue = 0;
    try
    {
      returnValue = scanner.nextLong(radix);
    }
    catch (InputMismatchException ime)
    {
      inputMismatchExceptionHandler("long");
    }
    catch (NoSuchElementException nsee)
    {
      noSuchElementHandler();
    }
    catch (IllegalStateException ise)
    {
      illegalStateExceptionHandler();
    }
    return returnValue;
  }

  /**
   * @return <code>true</code> if there is a <code>BigInteger</code> to input, <code>false</code>
   * otherwise.
   */
  public boolean hasNextBigInteger()
  {
    boolean returnValue = false;
    try
    {
      returnValue = scanner.hasNextBigInteger();
    }
    catch (IllegalStateException e)
    {
      illegalStateExceptionHandler();
    }
    return returnValue;
  }

  /**
   * @return the next <code>BigInteger</code> in the input stream assumed to be in the default
   * radix which is 10.
   */
  public BigInteger nextBigInteger()
  {
    BigInteger returnValue = new BigInteger("0");
    try
    {
      returnValue = scanner.nextBigInteger();
    }
    catch (InputMismatchException ime)
    {
      inputMismatchExceptionHandler("BigInteger");
    }
    catch (NoSuchElementException nsee)
    {
      noSuchElementHandler();
    }
    catch (IllegalStateException ise)
    {
      illegalStateExceptionHandler();
    }
    return returnValue;
  }

  /**
   * @param radix the radix of the input sequence.
   * @return the next <code>BigInteger</code> in the input stream using the radix
   * <code>radix</code.
   */
  public BigInteger nextBigInteger(final int radix)
  {
    BigInteger returnValue = new BigInteger("0");
    try
    {
      returnValue = scanner.nextBigInteger(radix);
    }
    catch (InputMismatchException ime)
    {
      inputMismatchExceptionHandler("BigInteger");
    }
    catch (NoSuchElementException nsee)
    {
      noSuchElementHandler();
    }
    catch (IllegalStateException ise)
    {
      illegalStateExceptionHandler();
    }
    return returnValue;
  }

  /**
   * @return <code>true</code> if there is a <code>float</code> to input, <code>false</code>
   * otherwise.
   */
  public boolean hasNextFloat()
  {
    boolean returnValue = false;
    try
    {
      returnValue = scanner.hasNextFloat();
    }
    catch (IllegalStateException e)
    {
      illegalStateExceptionHandler();
    }
    return returnValue;
  }

  /**
   * @return the next <code>float</code> in the input stream.
   */
  public float nextFloat()
  {
    float returnValue = 0;
    try
    {
      returnValue = scanner.nextFloat();
    }
    catch (InputMismatchException ime)
    {
      inputMismatchExceptionHandler("float");
    }
    catch (NoSuchElementException nsee)
    {
      noSuchElementHandler();
    }
    catch (IllegalStateException ise)
    {
      illegalStateExceptionHandler();
    }
    return returnValue;
  }

  /**
   * @return <code>true</code> if there is a <code>double</code> to input, <code>false</code>
   * otherwise.
   */
  public boolean hasNextDouble()
  {
    boolean returnValue = false;
    try
    {
      returnValue = scanner.hasNextDouble();
    }
    catch (IllegalStateException e)
    {
      illegalStateExceptionHandler();
    }
    return returnValue;
  }

  /**
   * @return the next <code>double</code> in the input stream.
   */
  public double nextDouble()
  {
    double returnValue = 0;
    try
    {
      returnValue = scanner.nextDouble();
    }
    catch (InputMismatchException ime)
    {
      inputMismatchExceptionHandler("double");
    }
    catch (NoSuchElementException nsee)
    {
      noSuchElementHandler();
    }
    catch (IllegalStateException ise)
    {
      illegalStateExceptionHandler();
    }
    return returnValue;
  }

  /**
   * @return <code>true</code> if there is a <code>BigDecimal</code> to input,
   * <code>false</code> otherwise.
   */
  public boolean hasNextBigDecimal()
  {
    boolean returnValue = false;
    try
    {
      returnValue = scanner.hasNextBigDecimal();
    }
    catch (IllegalStateException e)
    {
      illegalStateExceptionHandler();
    }
    return returnValue;
  }

  /**
   * @return the next <code>BigDecimal</code> in the input stream.
   */
  public BigDecimal nextBigDecimal()
  {
    BigDecimal returnValue = new BigDecimal("0");
    try
    {
      returnValue = scanner.nextBigDecimal();
    }
    catch (InputMismatchException ime)
    {
      inputMismatchExceptionHandler("BigDecimal");
    }
    catch (NoSuchElementException nsee)
    {
      noSuchElementHandler();
    }
    catch (IllegalStateException ise)
    {
      illegalStateExceptionHandler();
    }
    return returnValue;
  }

  /**
   * @return <code>true</code> if there is more input including an end of line marker,
   * <code>false</code> otherwise.
   */
  public boolean hasNextLine()
  {
    boolean returnValue = false;
    try
    {
      returnValue = scanner.hasNextLine();
    }
    catch (IllegalStateException e)
    {
      illegalStateExceptionHandler();
    }
    return returnValue;
  }

  /**
   * @return all the characters in the input stream up to and including the next end of line
   * marker in the input stream.
   */
  public String nextLine()
  {
    String returnValue = null;
    try
    {
      returnValue = scanner.nextLine();
    }
    catch (NoSuchElementException nsee)
    {
      noSuchElementHandler();
    }
    catch (IllegalStateException ise)
    {
      illegalStateExceptionHandler();
    }
    return returnValue;
  }

  /**
   * The method to handle an <code>IllegalStateException</code>.
   */
  private void illegalStateExceptionHandler()
  {
    System.err.println("Input has been closed.");
    System.exit(1);
  }

  /**
   * The method to handle an <code>InputMismatchException</code>.
   */
  private void inputMismatchExceptionHandler(final String type)
  {
    System.err.println("Input did not represent " +
      (type.equals("int") ? "an" : "a") + " " + type + " value.");
    System.exit(1);
  }

  /**
   * The method to handle an <code>NoSuchElementException</code>.
   */
  private void noSuchElementHandler()
  {
    System.err.println("No input to be read.");
    System.exit(1);
  }
}
