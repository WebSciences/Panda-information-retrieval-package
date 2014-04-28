/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.ucl.panda.retrieval.models;

/**
 *
 * @author jianzhu
 */
public interface Distribution {

   /**
    * Returns the distribution function <SPAN CLASS="MATH"><I>F</I>(<I>x</I>)</SPAN>.
    *
    * @param x value at which the distribution function is evaluated
    *
    *    @return distribution function evaluated at <TT>x</TT>
    *
    */
   public double cdf (double x);


   /**
    * Returns
    * <SPAN CLASS="MATH">bar(F)(<I>x</I>) = 1 - <I>F</I>(<I>x</I>)</SPAN>.
    *
    * @param x value at which the complementary distribution function is evaluated
    *
    *  @return complementary distribution function evaluated at <TT>x</TT>
    *
    */
   public double barF (double x);


   /**
    * Returns the inverse distribution function
    *    <SPAN CLASS="MATH"><I>F</I><SUP>-1</SUP>(<I>u</I>)</SPAN>, defined in.
    *
    * @param u value in the interval <SPAN CLASS="MATH">(0, 1)</SPAN> for which the inverse
    *      distribution function is evaluated
    *
    *    @return the inverse distribution function evaluated at <TT>u</TT>
    *
    */
   public double inverseF (double u);


   /**
    * Returns the mean of the distribution function.
    *
    */
   public double getMean();


   /**
    * Returns the variance of the distribution function.
    *
    */
   public double getVariance();


   /**
    * Returns the standard deviation of the distribution function.
    *
    */
   public double getStandardDeviation();


   /**
    * Returns the parameters of the distribution function in the same
    *  order as in the constructors.
    *
    */
   public double[] getParams();

}
