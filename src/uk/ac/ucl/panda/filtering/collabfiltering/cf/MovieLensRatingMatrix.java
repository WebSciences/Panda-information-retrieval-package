package uk.ac.ucl.panda.filtering.collabfiltering.cf;

import uk.ac.ucl.panda.filtering.collabfiltering.util.FileInput;


/**
 * MovieLensRatingMatrix is a class that represents the MovieLens data set that
 * has 100,000 ratings.
 * 
 * @author S01
 */
public class MovieLensRatingMatrix extends RatingMatrix
{
    /**
     * Constructs a rating matrix with the ratings from a MovieLens data set.
     */
    public MovieLensRatingMatrix(String filename)
    {
        super(1, 5);
        readDataset(filename, "\t");
    }
    
    /**
     * Constructs a rating matrix with the ratings from the MovieLens data set
     * that has 100,000 ratings.
     */
    public MovieLensRatingMatrix()
    {
        this("u.data");
    }
    
    /**
     * Reads ratings in the specified data set and adds the ratings to this
     * rating matrix.
     * 
     * @param filename the filename of the data set
     * @param delimiter the delimiter
     */
    private void readDataset(String filename, String delimiter)
    {
        FileInput in = new FileInput(filename);
        while (in.hasNextLine())
        {
            String[] split = in.nextLine().split(delimiter);
            int user = Integer.valueOf(split[0]);
            int item = Integer.valueOf(split[1]);
            double rating = Double.valueOf(split[2]);

            super.setRating(user, item, rating);
        }
        in.close();
    }
}
