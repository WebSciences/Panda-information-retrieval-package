package uk.ac.ucl.panda.filtering.collabfiltering.cf;

/**
 * RatingPair is a base class for UserRatingPair and ItemRatingPair.
 * 
 * @author S01
 */
public abstract class RatingPair
{
    /**
     * The rating value.
     */
    private double rating;
    
    /**
     * Constructs a RatingPair with the given rating value.
     * 
     * @param rating the rating
     */
    public RatingPair(double rating)
    {
        this.rating = rating;
    }

    /**
     * Returns this pair's rating value.
     * 
     * @return this pair's rating value
     */
    public double getRating()
    {
        return rating;
    }
}
