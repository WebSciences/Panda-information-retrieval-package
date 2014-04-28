package uk.ac.ucl.panda.filtering.collabfiltering.cf;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * RatingMatrix is a class that represents a rating matrix for collaborative
 * filtering.
 * 
 * @author S01
 */
public class RatingMatrix
{
    /**
     * The map of ratings. User-Item pairs are mapped to ratings.
     */
    private Map<Index, Double> ratings;
    
    /**
     * The map of each user's rated items. Users are mapped to their rated
     * items.
     */
    private Map<Integer, Set<Integer>> ratedItemsByUser;
    
    /**
     * The map of each item's rating users. Items are mapped to the users that
     * have given a rating to them.
     */
    private Map<Integer, Set<Integer>> ratingUsersByItem;
    
    /**
     * The minimum allowable rating.
     */
    private double minRating;
    
    /**
     * The maximum allowable rating.
     */
    private double maxRating;
    
    /**
     * The next available user ID.
     */
    private int nextUser;
    
    /**
     * Constructs an empty rating matrix.
     * 
     * @param minRating the minimum allowable rating.
     * @param maxRating the maximum allowable rating.
     */
    public RatingMatrix(double minRating, double maxRating)
    {
        ratings = new HashMap<Index, Double>();
        ratedItemsByUser = new HashMap<Integer, Set<Integer>>();
        ratingUsersByItem = new HashMap<Integer, Set<Integer>>();
        this.minRating = minRating;
        this.maxRating = maxRating;
        nextUser = 1;
    }
    
    /**
     * Constructs an empty rating matrix with a default minimum and maximum of
     * 1 and 5.
     */
    public RatingMatrix()
    {
        this(1, 5);
    }
    
    /**
     * Returns the number of ratings in this rating matrix.
     * 
     * @return the number of ratings in this rating matrix
     */
    public int getNumberOfRatings()
    {
        return ratings.size();
    }
    
    /**
     * Returns true if and only if the given user has rated the given item.
     * 
     * @param user the desired user to check
     * @param item the desired item to check
     * @return true if and only if the given user has rated the given item
     */
    public boolean hasRating(int user, int item)
    {
        return ratings.containsKey(new Index(user, item));
    }
    
    /**
     * Returns the rating given by the specified user to the specified item if
     * it exists. If not, a NullPointerException is thrown.
     * 
     * @param user the desired user
     * @param item the desired item
     * @return the rating given by the specified user to the specified item
     * @throws java.lang.NullPointerException if no such rating exists
     */
    public double getRating(int user, int item) throws NullPointerException
    {
        Double rating = ratings.get(new Index(user, item));
        if (rating == null)
        {
            throw new NullPointerException("No rating for (" + user + "," + 
                    item + ") exists.");
        }
        return rating;
    }
    
    /**
     * Replaces the specified user's rating for the specified item with the
     * given rating.
     * 
     * @param user the desired user
     * @param item the desired item
     * @param rating the new rating
     */
    public void setRating(int user, int item, double rating)
    {
        ratings.put(new Index(user, item), rating);
        
        if (user >= nextUser)
        {
            nextUser = user + 1;
        }
        
        Set<Integer> ratedItems = ratedItemsByUser.get(user);
        if (ratedItems == null)
        {
            ratedItemsByUser.put(user, ratedItems = new HashSet<Integer>());
        }
        ratedItems.add(item);
        Set<Integer> ratingUsers = ratingUsersByItem.get(item);
        if (ratingUsers == null)
        {
            ratingUsersByItem.put(item, ratingUsers = new HashSet<Integer>());
        }
        ratingUsers.add(user);
    }
    
    /**
     * Removes the specified user's rating for the specified item from this
     * rating matrix.
     * 
     * @param user the desired user
     * @param item the desired item
     */
    public void removeRating(int user, int item)
    {
        Double remove = ratings.remove(new Index(user, item));
        
        if (remove != null)
        {
            Set<Integer> ratedItems = ratedItemsByUser.get(user);
            ratedItems.remove(item);
            if (ratedItems.isEmpty())
            {
                ratedItemsByUser.remove(user);
            }

            Set<Integer> ratingUsers = ratingUsersByItem.get(item);
            ratingUsers.remove(user);
            if (ratingUsers.isEmpty())
            {
                ratingUsersByItem.remove(item);
            }
        }
    }
    
    /**
     * Returns the number of users in this rating matrix.
     * 
     * @return the number of users in this rating matrix
     */
    public int getNumberOfUsers()
    {
        return ratedItemsByUser.size();
    }
    
    /**
     * Returns a set of the users in this rating matrix.
     * 
     * @return a set of the users in this rating matrix
     */
    public Set<Integer> getUsers()
    {
        return new HashSet<Integer>(ratedItemsByUser.keySet());
    }
    
    /**
     * Returns the number of items in this rating matrix.
     * 
     * @return the number of items in this rating matrix
     */
    public int getNumberOfItems()
    {
        return ratingUsersByItem.size();
    }
    
    /**
     * Returns a set of the items in this rating matrix.
     * 
     * @return a set of the items in this rating matrix
     */
    public Set<Integer> getItems()
    {
        return new HashSet<Integer>(ratingUsersByItem.keySet());
    }
    
    /**
     * Returns the specified user's profile.
     * 
     * @param user the desired user
     * @return the specified user's profile
     */
    public Set<ItemRatingPair> getUserProfile(int user)
    {
        Set<ItemRatingPair> userProfile = new HashSet<ItemRatingPair>();

        Set<Integer> ratedItems = ratedItemsByUser.get(user);
        if (ratedItems != null)
        {
            for (int ratedItem : ratedItems)
            {
                double rating = ratings.get(new Index(user, ratedItem));
                userProfile.add(new ItemRatingPair(ratedItem, rating));
            }
        }
        
        return userProfile;
    }
    
    /**
     * Inserts the given user profile into this rating matrix.
     * 
     * @param userProfile the user profile to insert
     * @return the new user ID associated to the given user profile
     */
    public int addUserProfile(Set<ItemRatingPair> userProfile)
    {
        for (ItemRatingPair pair : userProfile)
        {
            int item = pair.getItem();
            double rating = pair.getRating();
            ratings.put(new Index(nextUser, item), rating);
            
            Set<Integer> ratedItems = ratedItemsByUser.get(nextUser);
            if (ratedItems == null)
            {
                ratedItemsByUser.put(nextUser, ratedItems = new HashSet<Integer>());
            }
            ratedItems.add(item);
            Set<Integer> ratingUsers = ratingUsersByItem.get(item);
            if (ratingUsers == null)
            {
                ratingUsersByItem.put(item, ratingUsers = new HashSet<Integer>());
            }
            ratingUsers.add(nextUser);
        }
        
        return nextUser++;
    }
    
    /**
     * Removes the specified user's profile from this rating matrix.
     * 
     * @param user the desired user
     * @return the removed user profile
     */
    public Set<ItemRatingPair> removeUserProfile(int user)
    {
        Set<ItemRatingPair> userProfile = new HashSet<ItemRatingPair>();

        Set<Integer> ratedItems = ratedItemsByUser.get(user);
        if (ratedItems != null)
        {
            for (int ratedItem : ratedItems)
            {
                double rating = ratings.remove(new Index(user, ratedItem));
                userProfile.add(new ItemRatingPair(ratedItem, rating));
                
                Set<Integer> ratingUsers = ratingUsersByItem.get(ratedItem);
                ratingUsers.remove(user);
                if (ratingUsers.isEmpty())
                {
                    ratingUsersByItem.remove(ratedItem);
                }
            }
        }
        
        ratedItemsByUser.remove(user);
        
        return userProfile;
    }
    
    /**
     * Returns a map of all the user profiles in this rating matrix.
     * 
     * @return a map of all the user profiles in this rating matrix
     */
    public Map<Integer, Set<ItemRatingPair>> getUserProfiles()
    {
        Map<Integer, Set<ItemRatingPair>> userProfiles = 
                new HashMap<Integer, Set<ItemRatingPair>>();
        
        for (int user : ratedItemsByUser.keySet())
        {
            userProfiles.put(user, getUserProfile(user));
        }
        
        return userProfiles;
    }
    
    /**
     * Returns the specified item's profile.
     * 
     * @param item the desired item
     * @return the specified item's profile
     */
    public Set<UserRatingPair> getItemProfile(int item)
    {
        Set<UserRatingPair> itemProfile = new HashSet<UserRatingPair>();

        Set<Integer> ratingUsers = ratingUsersByItem.get(item);
        if (ratingUsers != null)
        {
            for (int ratingUser : ratingUsers)
            {
                double rating = ratings.get(new Index(ratingUser, item));
                itemProfile.add(new UserRatingPair(ratingUser, rating));
            }
        }
        
        return itemProfile;
    }
    
    /**
     * Returns a map of all the item profiles in this rating matrix.
     * 
     * @return a map of all the item profiles in this rating matrix
     */
    public Map<Integer, Set<UserRatingPair>> getItemProfiles()
    {
        Map<Integer, Set<UserRatingPair>> itemProfiles = 
                new HashMap<Integer, Set<UserRatingPair>>();
        
        for (int item : ratingUsersByItem.keySet())
        {
            itemProfiles.put(item, getItemProfile(item));
        }
        
        return itemProfiles;
    }
    
    /**
     * Returns the minimum allowable rating.
     * 
     * @return the minimum allowable rating
     */
    public double getMinRating()
    {
        return minRating;
    }
    
    /**
     * Returns the maximum allowable rating.
     * 
     * @return the maximum allowable rating
     */
    public double getMaxRating()
    {
        return maxRating;
    }
    
    /**
     * Returns the average rating in this rating matrix.
     * 
     * @return the average rating in this rating matrix
     */
    public double getAverageRating()
    {
        return computeAverage(ratings.values());
    }
    
    /**
     * Returns the standard deviation of the ratings in this rating matrix.
     * 
     * @return the standard deviation of the ratings in this rating matrix
     */
    public double getStandardDeviationRating()
    {
        return computeStandardDeviation(ratings.values());
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
    
    @Override
    public String toString()
    {
        return "Rating matrix with " + getNumberOfUsers() + " users and " + 
                getNumberOfRatings() + " ratings on " + getNumberOfItems() + 
                " items";
    }
    
    private static class Index
    {
        private int user = 0;
        private int item = 0;
        private int hashvalue = 0;

        public Index(final int user, final int item)
        {
            this.user = user;
            this.item = item;
            hashvalue = ((user + "") + (item + "")).hashCode();
        }

        // Override equals and hashcode to ensure Index objects
        // behave correctly when used as keys in a hash table.
        @Override
        public boolean equals(final Object obj)
        {
            if (obj instanceof Index)
            {
                Index index = (Index) obj;
                return ((user == index.user) && (item == index.item));
            }
            else
            {
                return false;
            }
        }

        @Override
        public int hashCode()
        {
            return hashvalue;
        }
    }
}
