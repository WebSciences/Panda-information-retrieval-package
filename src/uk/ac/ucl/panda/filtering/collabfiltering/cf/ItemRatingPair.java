package uk.ac.ucl.panda.filtering.collabfiltering.cf;

/**
 * ItemRatingPair is a class representing a item-rating pair.
 * 
 * @author S01
 */
public class ItemRatingPair extends RatingPair implements Comparable<ItemRatingPair>
{
    /**
     * The item.
     */
    private int item;
    
    /**
     * Constructs a item-rating pair with the given item and rating value.
     * 
     * @param item the item
     * @param rating the rating value
     */
    public ItemRatingPair(int item, double rating)
    {
        super(rating);
        this.item = item;
    }

    /**
     * Returns this pair's item component.
     * 
     * @return this pair's item component
     */
    public int getItem()
    {
        return item;
    }

    public int compareTo(ItemRatingPair o)
    {
        int result = Double.compare(getRating(), o.getRating());
        if (result == 0)
        {
            return item - o.item;
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
        if (!(obj instanceof ItemRatingPair)) return false;
        ItemRatingPair pair = (ItemRatingPair)obj;
        return (getRating() == pair.getRating()) && (item == pair.item);
    }
    
    @Override
    public int hashCode()
    {
        int result = 17;
        result = 37*result + (int)(Double.doubleToLongBits(getRating()) ^ 
                (Double.doubleToLongBits(getRating()) >>> 32));
        result = 37*result + (int)item;
        return result;
    }
    
    @Override
    public String toString()
    {
        return "(" + item + "," + getRating() + ")";
    }
}
