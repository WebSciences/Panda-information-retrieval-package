package uk.ac.ucl.panda.filtering.collabfiltering.cf;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * UserBasedRecommender is a class that implements a recommender system that
 * uses a user-based collaborative filtering algorithm.
 * 
 * @author S01
 */
public class UserBasedRecommender implements Recommender
{
    /**
     * The rating matrix that rating predictions are based on.
     */
    private RatingMatrix matrix;
    
    /**
     * The map of the user profiles from the rating matrix.
     */
    private Map<Integer, Set<ItemRatingPair>> userProfiles;
    
    /**
     * Constructs a user-based recommender system.
     * 
     * @param matrix the rating matrix that predictions will be based on
     */
    public UserBasedRecommender(RatingMatrix matrix)
    {
        this.matrix = matrix;
        userProfiles = matrix.getUserProfiles();
    }
    
    public double getPrediction(int u, int i)
    {
        SortedSet<UserRatingPair> userSimilarities = getUserSimilarities(u);
        
        double prediction = getPrediction(u, i, userSimilarities);
        return prediction;
    }
    
    private SortedSet<ItemRatingPair> getRecommendations(int u, boolean excludeRatedItems)
    {
        SortedSet<UserRatingPair> userSimilarities = getUserSimilarities(u);
        
        // Find items rated by user u if necessary
        Set<Integer> itemsRatedByU = new HashSet<Integer>();
        if (excludeRatedItems)
        {
            for (ItemRatingPair pair : userProfiles.get(u))
            {
                itemsRatedByU.add(pair.getItem());
            }
        }
        
        SortedSet<ItemRatingPair> recommendations = new TreeSet<ItemRatingPair>();
        for (int i : matrix.getItems())
        {
            if (itemsRatedByU.contains(i))
            {
                continue;
            }
            
            double prediction = getPrediction(u, i, userSimilarities);
            ItemRatingPair pair = new ItemRatingPair(i, prediction);
            recommendations.add(pair);
        }
        return recommendations;
    }
    
    public SortedSet<ItemRatingPair> getRecommendations(int u)
    {
        return getRecommendations(u, true);
    }
    
    private SortedSet<UserRatingPair> getUserSimilarities(int u)
    {
        // Compute similarity between user u and every other user
        SortedSet<UserRatingPair> userSimilarities = new TreeSet<UserRatingPair>();
        for (int v : matrix.getUsers())
        {
            if (v == u)
            {
                continue;
            }
            
            double similarity = getPearsonsCorrelationCoefficient(u, v);
            UserRatingPair pair = new UserRatingPair(v, similarity);
            userSimilarities.add(pair);
        }
        return userSimilarities;
    }
    
    private double getPrediction(int u, int i, SortedSet<UserRatingPair> userSimilarities)
    {
        double prediction = Profiles.getAverageRating(userProfiles.get(u));
        double topSum = 0;
        double bottomSum = 0;
        
        int neighbourhoodSize = 20;
        int currentNeighbourhoodSize = 0;
        
        LinkedList<UserRatingPair> linkedList = new LinkedList<UserRatingPair>(userSimilarities);
        for (Iterator<UserRatingPair> iter = linkedList.descendingIterator(); iter.hasNext(); )
        {
            UserRatingPair pair = iter.next();
            double similarity = pair.getRating();
            if (similarity < 0.1)
            {
                break;
            }
            int v = pair.getUser();
            if (!matrix.hasRating(v, i))
            {
                continue;
            }
            double ratingFromV = matrix.getRating(v, i);
            
            currentNeighbourhoodSize++;
            Set<ItemRatingPair> userProfileOfV = userProfiles.get(v);
            double averageRatingOfV = Profiles.getAverageRating(userProfileOfV);
            topSum += similarity * (ratingFromV - averageRatingOfV);
            bottomSum += Math.abs(similarity);
            
            if (currentNeighbourhoodSize == neighbourhoodSize)
            {
                break;
            }
        }
        
        if (bottomSum == 0)
        {
            return prediction;
        }
        else
        {
            return prediction + (topSum / bottomSum);
        }
    }
    
    private double getPearsonsCorrelationCoefficient(int u, int v)
    {
        Set<ItemRatingPair> userProfileOfU = userProfiles.get(u);
        Set<ItemRatingPair> userProfileOfV = userProfiles.get(v);
        double averageRatingOfU = Profiles.getAverageRating(userProfileOfU);
        double averageRatingOfV = Profiles.getAverageRating(userProfileOfV);
        
        // Find items rated by both user u AND user v
        Set<Integer> coratedItems = new HashSet<Integer>();
        for (ItemRatingPair pairFromU : userProfileOfU)
        {
            for (ItemRatingPair pairFromV : userProfileOfV)
            {
                if (pairFromU.getItem() == pairFromV.getItem())
                {
                    coratedItems.add(pairFromU.getItem());
                    break;
                }
            }
        }
        
        if (coratedItems.size() == 0)
        {
            return -1;
        }
        
        double topSum = 0;
        double bottomLeftSum = 0;
        double bottomRightSum = 0;
        for (int coratedItem : coratedItems)
        {
            double ratingFromU = matrix.getRating(u, coratedItem);
            double ratingFromV = matrix.getRating(v, coratedItem);
            double topLeft = (ratingFromU - averageRatingOfU);
            double topRight = (ratingFromV - averageRatingOfV);
            topSum += topLeft * topRight;
            bottomLeftSum += Math.pow(ratingFromU - averageRatingOfU, 2);
            bottomRightSum += Math.pow(ratingFromV - averageRatingOfV, 2);
        }
        
        if ((topSum == 0) || (bottomLeftSum == 0) || (bottomRightSum == 0))
        {
            return -1;
        }
        
        return topSum / (Math.sqrt(bottomLeftSum) * Math.sqrt(bottomRightSum));
    }
}
