package uk.ac.ucl.panda.filtering.collabfiltering.cf;

public class Test
{
    public static void main(String[] args)
    {
        RatingMatrix matrix = new MovieLensRatingMatrix();
        Recommender recommender = new UserBasedRecommender(matrix);
        int user = 2;
        int item = 3;
        double prediction = recommender.getPrediction(user, item);
      //  System.out.println("rating prediction on item " + item + " for user " +user " is: " + prediction );
    }
}
