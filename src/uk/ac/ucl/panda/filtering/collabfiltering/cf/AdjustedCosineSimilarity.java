package uk.ac.ucl.panda.filtering.collabfiltering.cf;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import uk.ac.ucl.panda.filtering.collabfiltering.util.ArraySymmetricMatrix;

/**
 * AdjustedCosineSimilarity is a class that implements the adjusted cosine
 * similarity measure for items. This implementation caches results for
 * performance.
 * 
 * @author S01
 */
public class AdjustedCosineSimilarity
{
    private RatingMatrix matrix;
    private Map<Integer, Set<ItemRatingPair>> userProfiles;
    private Map<Integer, Set<UserRatingPair>> itemProfiles;
    private Map<Integer, Integer> itemMap;
    private ArraySymmetricMatrix cache;
    
    public AdjustedCosineSimilarity(RatingMatrix matrix)
    {
        this.matrix = matrix;
        userProfiles = matrix.getUserProfiles();
        itemProfiles = matrix.getItemProfiles();
        itemMap = new HashMap<Integer, Integer>();
        int i = 0;
        for (int item : matrix.getItems())
        {
            itemMap.put(item, i++);
        }
        cache = new ArraySymmetricMatrix(i, Double.POSITIVE_INFINITY);
    }
    
    public double compute(int i, int j)
    {
        // Check cache
        double cacheResult = Double.POSITIVE_INFINITY;
        try
        {
            cacheResult = cache.getElement(itemMap.get(i), itemMap.get(j));
        }
        catch (IndexOutOfBoundsException e)
        {}
        catch (NullPointerException npe)
        {
            return 0;
        }
        
        if (!Double.isInfinite(cacheResult))
        {
            return cacheResult;
        }

        double result = getSimilarity(i, j);
        cache.setElement(itemMap.get(i), itemMap.get(j), result);
        return result;
    }
    
    private double getSimilarity(int i, int j)
    {
        Set<UserRatingPair> itemProfileOfI = itemProfiles.get(i);
        Set<UserRatingPair> itemProfileOfJ = itemProfiles.get(j);
        if ((itemProfileOfI == null) || (itemProfileOfJ == null))
        {
            return 0;
        }
        
        // Find users that rated both item i AND item j
        Set<Integer> users = new HashSet<Integer>();
        for (UserRatingPair pairFromI : itemProfileOfI)
        {
            for (UserRatingPair pairFromJ : itemProfileOfJ)
            {
                if (pairFromI.getUser() == pairFromJ.getUser())
                {
                    users.add(pairFromI.getUser());
                    break;
                }
            }
        }
        
        if (users.size() == 0)
        {
            return -1;
        }
        
        double topSum = 0;
        double bottomLeftSum = 0;
        double bottomRightSum = 0;
        for (int u : users)
        {
            double ratingForI = matrix.getRating(u, i);
            double ratingForJ = matrix.getRating(u, j);
            Set<ItemRatingPair> userProfileOfU = userProfiles.get(u);
            double averageRatingOfU = Profiles.getAverageRating(userProfileOfU);
            double topLeft = (ratingForI - averageRatingOfU);
            double topRight = (ratingForJ - averageRatingOfU);
            topSum += topLeft * topRight;
            bottomLeftSum += Math.pow(ratingForI - averageRatingOfU, 2);
            bottomRightSum += Math.pow(ratingForJ - averageRatingOfU, 2);
        }
        
        if ((topSum == 0) || (bottomLeftSum == 0) || (bottomRightSum == 0))
        {
            return -1;
        }
        
        return topSum / (Math.sqrt(bottomLeftSum) * Math.sqrt(bottomRightSum));
    }
}
