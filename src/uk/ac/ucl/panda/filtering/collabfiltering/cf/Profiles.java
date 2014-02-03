package uk.ac.ucl.panda.filtering.collabfiltering.cf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Profiles is a utility class for user and item profiles.
 * 
 * @author S01
 */
public class Profiles
{
    /**
     * Returns the average rating in the given profile.
     * 
     * @param profile the desired profile
     * @return the average rating in the given profile
     */
    public static double getAverageRating(Set<? extends RatingPair> profile)
    {
        List<Double> profileRatings = new ArrayList<Double>();
        
        for (RatingPair pair : profile)
        {
            double rating = pair.getRating();
            profileRatings.add(rating);
        }
        
        return computeAverage(profileRatings);
    }
    
    /**
     * Returns the standard deviation of the ratings in the given profile.
     * 
     * @param profile the desired profile
     * @return the standard deviation of the ratings in the given profile
     */
    public static double getStandardDeviationRating(Set<? extends RatingPair> profile)
    {
        List<Double> profileRatings = new ArrayList<Double>();
        
        for (RatingPair pair : profile)
        {
            double rating = pair.getRating();
            profileRatings.add(rating);
        }
        
        return computeStandardDeviation(profileRatings);
    }
    
    /**
     * Returns the arithmetic mean of the given collection of numbers.
     * 
     * @param c the collection of numbers
     * @return the arithmetic mean of the given collection of numbers
     */
    private static double computeAverage(Collection<Double> c)
    {
        double sum = 0;
        for (double d : c)
        {
            sum += d;
        }
        return sum / c.size();
    }
    
    /**
     * Returns the standard deviation of the numbers in the given collection.
     * 
     * @param c the collection of numbers
     * @return the standard deviation of the numbers in the given collection
     */
    private static double computeStandardDeviation(Collection<Double> c)
    {
        if (c.size() == 1) return 0;
        
        double sum = 0;
        double mean = computeAverage(c);
        for (double d : c)
        {
            sum += Math.pow(d - mean, 2);
        }
        
        return Math.sqrt(sum / (c.size() - 1));
    }
}
