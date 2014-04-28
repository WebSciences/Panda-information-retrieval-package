package uk.ac.ucl.panda.filtering.collabfiltering.util;

/**
 * ArraySymmetricMatrix is a class representing a symmetric matrix, which is a
 * square matrix that is equal to its transpose. For space efficiency, this
 * implementation only stores the elements of the matrix's lower triangle.
 * 
 * @author S01
 */
public class ArraySymmetricMatrix
{
    /**
     * The 2D array of elements.
     */
    private double[][] elements;
    
    /**
     * Constructs a zero symmetric matrix with the specified dimension.
     * 
     * @param dimension the dimension of the matrix
     * @throws IllegalArgumentException if the dimension is less than one
     */
    public ArraySymmetricMatrix(int dimension)
    {
        if (dimension < 1)
        {
            throw new IllegalArgumentException("Illegal Dimension: " +
                    dimension);
        }
        
        // Set up the lower triangular matrix
        elements = new double[dimension][];
        for (int i = 0; i < dimension; i++)
        {
            elements[i] = new double[i + 1];
        }
    }
    
    /**
     * Constructs a symmetric matrix with the specified dimension and all
     * elements set to the specified value.
     * 
     * @param dimension the dimension of the matrix
     * @param d the default value
     * @throws IllegalArgumentException if the dimension is less than one
     */
    public ArraySymmetricMatrix(int dimension, double d)
    {
        this(dimension);
        for (int i = 0; i < elements.length; i++)
        {
            for (int j = 0; j < elements[i].length; j++)
            {
                elements[i][j] = d;
            }
        }
    }
    
    /**
     * Returns the dimension of this matrix.
     * 
     * @return the dimension of this matrix
     */
    public int getDimension()
    {
        return elements.length;
    }
    
    /**
     * Returns the element in the i-th row and j-th column in this matrix.
     * 
     * @param i the row index of the desired element
     * @param j the column index of the desired element
     * @return the element in the i-th row and j-th column in this matrix
     */
    public double getElement(int i, int j)
    {
        checkBounds(i, j);
        int row = Math.max(i, j);
        int col = Math.min(i, j);
        return elements[row][col];
    }
    
    /**
     * Replaces the element in the i-th row and j-th column with the specified
     * value.
     * 
     * @param i the row index of the element to replace
     * @param j the column index of the element to replace
     * @param value the value to place in the i-th row and j-th column
     */
    public void setElement(int i, int j, double value)
    {
        checkBounds(i, j);
        int row = Math.max(i, j);
        int col = Math.min(i, j);
        elements[row][col] = value;
    }
    
    /**
     * Checks if the given row and column indices are in range. If not, an
     * IndexOutOfBoundsException is thrown.
     */
    private void checkBounds(int i, int j)
    {
        if ((i < 0) || (j < 0) || (i >= getDimension()) || (j >= getDimension()))
        {
            throw new IndexOutOfBoundsException("Index: (" + i + "," + j +
                    "), Dimension: " + getDimension());
        }
    }
    
    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < getDimension(); i++)
        {
            sb.append('[');
            for (int j = 0; j < getDimension(); j++)
            {
                int row = Math.max(i, j);
                int col = Math.min(i, j);
                sb.append(elements[row][col] + ",");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append("]\n");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
}
