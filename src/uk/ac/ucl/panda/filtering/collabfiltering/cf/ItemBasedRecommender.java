package uk.ac.ucl.panda.filtering.collabfiltering.cf;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * ItemBasedRecommender is a class that implements a recommender system that
 * uses a item-based collaborative filtering algorithm.
 * 
 * @author S01
 */
public class ItemBasedRecommender implements Recommender
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
     * The item similarity measure.
     */
    private AdjustedCosineSimilarity sim;
    
    /**
     * Constructs a item-based recommender system.
     * 
     * @param matrix the rating matrix that predictions will be based on
     */
    public ItemBasedRecommender(RatingMatrix matrix)
    {
        this.matrix = matrix;
        userProfiles = matrix.getUserProfiles();
        sim = new AdjustedCosineSimilarity(matrix);
    }
    
    public double getPrediction(int u, int i)
    {
        SortedSet<ItemRatingPair> itemSimilarities = getItemSimilarities(i);
        
        double prediction = getPrediction(u, itemSimilarities);
        return prediction;
    }
    
    private SortedSet<ItemRatingPair> getRecommendations(int u, boolean excludeRatedItems)
    {
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
            
            SortedSet<ItemRatingPair> itemSimilarities = getItemSimilarities(i);
            
            double prediction = getPrediction(u, itemSimilarities);
            ItemRatingPair pair = new ItemRatingPair(i, prediction);
            recommendations.add(pair);
        }
        return recommendations;
    }
    
    public SortedSet<ItemRatingPair> getRecommendations(int u)
    {
        return getRecommendations(u, true);
    }
    
    private SortedSet<ItemRatingPair> getItemSimilarities(int i)
    {
        // Compute similarity between item i and every other item
        SortedSet<ItemRatingPair> itemSimilarities = new TreeSet<ItemRatingPair>();
        for (int j : matrix.getItems())
        {
            if (j == i)
            {
                continue;
            }
            
            double similarity = sim.compute(i, j);
            ItemRatingPair pair = new ItemRatingPair(j, similarity);
            itemSimilarities.add(pair);
        }
        return itemSimilarities;
    }
    
    private double getPrediction(int u, SortedSet<ItemRatingPair> itemSimilarities)
    {
        double topSum = 0;
        double bottomSum = 0;
        
        int neighbourhoodSize = 20;
        int currentNeighbourhoodSize = 0;
        
        LinkedList<ItemRatingPair> linkedList = new LinkedList<ItemRatingPair>(itemSimilarities);
        for (Iterator<ItemRatingPair> iter = linkedList.descendingIterator(); iter.hasNext(); )
        {
            ItemRatingPair pair = iter.next();
            double similarity = pair.getRating();
            if (similarity < 0)
            {
                break;
            }
            int j = pair.getItem();
            if (!matrix.hasRating(u, j))
            {
                continue;
            }
            double ratingFromU = matrix.getRating(u, j);
            
            currentNeighbourhoodSize++;
            topSum += ratingFromU * similarity;
            bottomSum += similarity;
            
            if (currentNeighbourhoodSize == neighbourhoodSize)
            {
                break;
            }
        }
        
        if (bottomSum == 0)
        {
            Set<ItemRatingPair> userProfileOfU = userProfiles.get(u);
            double averageRatingOfU = Profiles.getAverageRating(userProfileOfU);
            return averageRatingOfU;
        }
        else
        {
            return (topSum / bottomSum);
        }
    }
}
