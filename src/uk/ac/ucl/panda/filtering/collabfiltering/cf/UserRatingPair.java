package uk.ac.ucl.panda.filtering.collabfiltering.cf;

/**
 * UserRatingPair is a class representing a user-rating pair.
 * 
 * @author S01
 */
public class UserRatingPair extends RatingPair implements Comparable<UserRatingPair>
{
    /**
     * The user.
     */
    private int user;
    
    /**
     * Constructs a user-rating pair with the given user and rating value.
     * 
     * @param user the user
     * @param rating the rating value
     */
    public UserRatingPair(int user, double rating)
    {
        super(rating);
        this.user = user;
    }

    /**
     * Returns this pair's user component.
     * 
     * @return this pair's user component
     */
    public int getUser()
    {
        return user;
    }
    
    public int compareTo(UserRatingPair o)
    {
        int result = Double.compare(getRating(), o.getRating());
        if (result == 0)
        {
            return user - o.user;
        }
        else
        {
            return result;
        }
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (obj == this) return true;
        if (!(obj instanceof UserRatingPair)) return false;
        UserRatingPair pair = (UserRatingPair)obj;
        return (getRating() == pair.getRating()) && (user == pair.user);
    }
    
    @Override
    public int hashCode()
    {
        int result = 17;
        result = 37*result + (int)(Double.doubleToLongBits(getRating()) ^ 
                (Double.doubleToLongBits(getRating()) >>> 32));
        result = 37*result + (int)user;
        return result;
    }

    @Override
    public String toString()
    {
        return "(" + user + "," + getRating() + ")";
    }
}
