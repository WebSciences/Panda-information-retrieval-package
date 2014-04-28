/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.ucl.panda.utility.structure;


import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;


/**
 *
 * @author xxms
 */
public class Normalise implements Serializable {

      /** Cache of decoded bytes. */
  private static final float[] NORM_TABLE = new float[256];

  static {
    for (int i = 0; i < 256; i++)
      NORM_TABLE[i] = SmallFloat.byte315ToFloat((byte)i);
  }

    public float lengthNorm(String fieldName, int numTerms) {
    //return (float)(1.0 / Math.sqrt(numTerms));
    return numTerms;
  }
  
  
    /** Encodes a normalization factor for storage in an index.
   *
   * <p>The encoding uses a three-bit mantissa, a five-bit exponent, and
   * the zero-exponent point at 15, thus
   * representing values from around 7x10^9 to 2x10^-9 with about one
   * significant decimal digit of accuracy.  Zero is also represented.
   * Negative numbers are rounded up to zero.  Values too large to represent
   * are rounded down to the largest representable value.  Positive values too
   * small to represent are rounded up to the smallest positive representable
   * value.
   *
   * @see org.apache.lucene.document.Field#setBoost(float)
   * @see org.apache.lucene.util.SmallFloat
   */
  public static byte encodeNorm(float f) {
    return SmallFloat.floatToByte315(f);
  }

  
  /** Decodes a normalization factor stored in an index.
   * @see #encodeNorm(float)
   */
  public static float decodeNorm(byte b) {
    return NORM_TABLE[b & 0xFF];  // & 0xFF maps negative bytes to positive above 127
  }

  /** Returns a table for decoding normalization bytes.
   * @see #encodeNorm(float)
   */
  public static float[] getNormDecoder() {
    return NORM_TABLE;
  }
    
    
}
