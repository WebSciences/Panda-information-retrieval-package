package uk.ac.ucl.panda.filtering.collabfiltering.cf;

import java.util.SortedSet;

/**
 * Recommender is an interface that all recommender systems implement.
 * 
 * @author S01
 */
public interface Recommender
{
    /**
     * Returns the rating prediction of the specified item for the specified
     * user.
     * 
     * @param user the user to get a recommendation for
     * @param item the item to get a recommendation for
     * @return the rating prediction of the item for the user
     */
    public double getPrediction(int user, int item);
    
    /**
     * Returns a sorted set of recommendations for the specified user. Items
     * that have already been rated by the user are not included.
     * 
     * @param user the user to get recommendations for
     * @return a sorted set of recommendations for the specified user
     */
    public SortedSet<ItemRatingPair> getRecommendations(int user);
}
