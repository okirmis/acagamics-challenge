import core.Vector;

/**
 * This helper class provides static methods to handle
 * vectors, such as getting the maximum or minimum from a
 * list of vectors.
 * 
 * @note As all methods of this class are static, there shouldn't
 * 		 be any instance of it
 * 
 * @author Oskar Kirmis <kirmis@st.ovgu.de>
 */
public class VectorUtils
{
	/**
	 * Get the shortest vector from an array of vectors.
	 * 
	 * @param vectors Array of vectors to get the shortest vector from
	 * @return The shortest vector of the array
	 */
	public static Vector getMinimum( Vector[] vectors )
	{
		assert vectors.length > 0 : "Cannot get the shortest vector from an empty array";
		
		Vector min = vectors[ 0 ];
		
		// For every vector, check if it is smaller than the current minimum
		for( int i = 1; i < vectors.length; ++i )
			if( vectors[ i ].length() < min.length() )
				min = vectors[ i ];

		return min;
	}

	/**
	 * Get the longest vector from an array of vectors.
	 * 
	 * @param vectors Array of vectors to get the longest vector from
	 * @return The longest vector of the array
	 */
	public static Vector getMaximum( Vector[] vectors )
	{
		assert vectors.length > 0 : "Cannot get the longest vector from an empty array";
		
		Vector max = vectors[ 0 ];
		
		// Check if the any vector is longer than the current maximum
		for( int i = 0; i < vectors.length; ++i )
			if( vectors[ i ].length() > max.length() )
				max = vectors[ i ];

		return max;
	}
	
	/**
	 * Create a vector with a given direction and a given length
	 * 
	 * @param v The vector which has the same direction as the result vector should have
	 * @param length Length of the result vector
	 * @return A vector with given direction and length
	 */
	public static Vector getVectorWithLength( Vector v, float length )
	{
		if( v.length() == 0 )
			return Vector.ZERO();
		
		return v.mult( length / v.length() );
	}
}
