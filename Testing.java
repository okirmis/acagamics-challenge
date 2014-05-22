import core.Vector;

/**
 * Basic class to test various classes of this package
 * 
 * @author Oskar Kirmis <kirmis@st.ovgu.de>
 */
public class Testing {

	/**
	 * Run the program. JavaVM has to be started using "-ea" argument to enable assertions.
	 */
	public static void main(String[] args) {
		testMathUtils();
		testActionLocker();
	}

	/**
	 * Test MathUtils class. This class does basic operations on vectors.
	 */
	public static void testMathUtils()
	{	
		
		// Test MathUtils.angleFromVector()
		assert cmp( MathUtils.angleFromVector( new Vector(1, 1) ), Math.PI / 4.0 ) : "MathUtils failed.";
		assert cmp( MathUtils.angleFromVector( new Vector(1,-1) ), -Math.PI / 4.0 ) : "MathUtils failed.";
		assert cmp( MathUtils.angleFromVector( new Vector(0, 1) ), Math.PI / 2 ) : "MathUtils failed.";
		assert cmp( MathUtils.angleFromVector( new Vector(0,-1) ), -Math.PI / 2 ) : "MathUtils failed.";

		// Test MathUtils.angleOnCircle()
		assert cmp( MathUtils.angleOnCircle(5, 2.5), Math.PI / 4 ) : "MathUtils failed.";
		assert cmp( MathUtils.angleOnCircle(0, 2.5), Math.PI / 2 ) : "MathUtils failed.";
		
		// Test MathUtils.isVectorParallel()
		assert  MathUtils.isVectorParallel( new Vector( 1,1 ), new Vector( 1, 1 ) ) : "MathUtils failed.";
		assert  MathUtils.isVectorParallel( new Vector( 1,1 ), new Vector( 1, 0.8f ) ) : "MathUtils failed.";
		assert !MathUtils.isVectorParallel( new Vector( 1,1 ), new Vector( 1, 0 ) ) : "MathUtils failed.";
		
		System.out.println( "MathUtils test passed." );
	}

	/**
	 * Test ActionLocker class. This class is used to time events in the game.
	 */
	public static void testActionLocker()
	{	
		
		ActionLocker locker = new ActionLocker();
		locker.initialize( 10 );

		// Test actionlocker isLocked/initialize()
		assert !locker.isLocked( ActionLocker.ACTION_THROW_BOTTLE ) : "ActionLocker failed";
		assert !locker.isLocked( ActionLocker.ACTION_HIT_POT ) : "ActionLocker failed";
		assert !locker.isLocked( ActionLocker.ACTION_THROW_BOTTLE ) : "ActionLocker failed";
		assert !locker.isLocked( ActionLocker.ACTION_OWNED_FLAGS + 9 ) : "ActionLocker failed";
		
		// Test lock
		locker.lock( ActionLocker.ACTION_THROW_BOTTLE , 1 );
		assert locker.isLocked( ActionLocker.ACTION_THROW_BOTTLE ) : "ActionLocker failed";
		
		// Test tick
		locker.tick();
		assert !locker.isLocked( ActionLocker.ACTION_THROW_BOTTLE ) : "ActionLocker failed";
		
		System.out.println( "ActionLocker test passed." );
	}
	
	
	
	private static boolean cmp( double a, double b )
	{
		return Math.abs( a - b ) < Math.pow( 10 , -6 );
	}
}
