import core.Vector;

/**
 * This class only provides a collection of utilities required
 * for easy vector handling in bots. An instantiation is not
 * required because all methods are static.
 * 
 * @author Oskar Kirmis <kirmis@st.ovgu.de>
 */
final public class MathUtils {

	public static final double DEFAULT_PARALLEL_THRESHOLD = Math.PI / 4;
	
	/**
	 * Get the angle from a given vector (polar form).
	 * 
	 * @param v The vector to  get the angle from
	 * @return Angle of the vector
	 */
	public static double angleFromVector( Vector v )
	{
		assert v != null : "Null pointer given";
		
		// Special situation handling: +/- 90
		if( v.x == 0 )
			return v.y > 0 ? Math.PI / 2.0 : -Math.PI / 2.0;
			
		// Normal handling
		return Math.atan( v.y / v.x );
	}
	
	/**
	 * Get the angle the projection of an object with the given object radius
	 * produces on a given distance (circle radius).
	 * 
	 * @param circleRadius Distance of the object
	 * @param objectRadius The radius of the object to project
	 * @return Angle a projection of this object on a circle takes
	 */
	public static double angleOnCircle( double circleRadius, double objectRadius )
	{
		// Special situation handling: +/- 90
		if( circleRadius == 0 )
			return Math.PI / 2.0;
			
		// Normal handling
		return Math.atan( 2.0 * objectRadius / circleRadius );
	}
	
	/**
	 * Find out if two vectors are parallel. This is decided by comparing
	 * the angles of the vectors (smaller PI/4)
	 * 
	 * @param v0 First vector for comparison
	 * @param v1 Second vector to compare to the first one
	 * @return true, if the difference of the angles is smaller MathUtils.DEFAULT_PARALLEL_THRESHOLD
	 */
	public static boolean isVectorParallel( Vector v0, Vector v1 )
	{
		assert v0 != null && v1 != null : "Null pointer given";
		
		// Get difference of the vectors angles
		double d = Math.abs( 
						MathUtils.angleFromVector( v0 ) - MathUtils.angleFromVector( v1 )
					 );
		
		return d < MathUtils.DEFAULT_PARALLEL_THRESHOLD;
	}
}
