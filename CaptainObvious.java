import core.Vector;
import core.ai.AiFlagInfo;
import core.ai.AiMapInfo;
import core.ai.AiPlayerInfo;
import core.ai.AiZombieInfo;
import core.constants.ZombieConstants;
import core.player.PlayerController;

/*
 * The following comments are instructions for java-bind tool if
 * we only want to deliver a single source code file
 */
//#include MathUtils.java
//#include VectorUtils.java
//#include ActionLocker.java
//#include ZombiesInRangeCache.java
//#include FlagStatusHandlerInterface.java
//#include ObviousFlagHandler.java
//#include FlagObserver.java


/**
 * Actual player class
 * 
 * This class is the main class of the bot which represents the
 * bot itself. It uses the classes above to avoid unintended
 * behavior or speed computations up.
 * 
 * @author Oskar Kirmis
 */
public class CaptainObvious extends PlayerController
{

	//! Vectors pointing from players current position to the flags
	Vector[] m_vectorsToFlags;
	
	//! Vectors pointing from players current position to the zombies
	Vector[] m_vectorsToZombies;
	
	//! Currently selected target flag
	Vector m_target;
	
	//! Movement to escape from current position (because of zombies)
	Vector m_escapeMovement;
	
	//! Factor to specify how "much" the bot tries to escpae
	float m_escapeFactor  = 3.0f;
	
	//! Factor describing the bot's attraction to the current target
	float m_greediness    = 1.8f;
	
	//! Speed the bot currently has (length of vector returned by think method)
	float m_currentSpeed  = 1.0f;
	
	//! Number of currently captured flags
	int   m_capturedFlags = 0;
	
	//! Action locker the bot uses to avoid infinitely repeating an action
	ActionLocker        m_locker = null;
	
	//! Object that handles changes in flags
	FlagObserver        m_observer = null;
	
	//! Instance of zombie position cache to increase the bots performance
	ZombiesInRangeCache m_cache  = null;
	
	//! Own player reference
	AiPlayerInfo m_player = null;
	
	//! Flag status event handler
	FlagStatusHandler m_handler = null;

	/**
	 * Name of the bot
	 * @return Displayed name of the bot
	 */
	public String getName( )
	{
		return "Kernel Panic";
	}

	/**
	 * Author of this bot
	 * @return The authors name
	 */
    @Override
    public String getAuthor( )
    {
        return "203586";
    }

    
	/**
	 * This method was made public to allow the usage from
	 * FlagObserver handler methods.
	 * @see FlagObserver
	 * @param target Target point to throw the bottle to
	 */
	@Override
	public void throwBottle( Vector target )
	{
		super.throwBottle( target );
	}
	
	
	/**
	 * This method is called when a new game was started. All members that
	 * require an initialization with game information are instantiated here.
	 */
	@Override
	public void onGameStarted( String gameTypeName, AiMapInfo map, AiPlayerInfo ownPlayer )
	{
		m_locker = new ActionLocker();
		m_locker.initialize( map.getNumFlags() );
		
		m_observer = new FlagObserver( map.getNumFlags(), 
		                               ownPlayer, this, m_locker );
		
		m_cache = new ZombiesInRangeCache();
		m_player = ownPlayer;
	}

	/**
	 * Core method called by the game to get the next action our bot will
	 * perform.
	 * 
	 * @param map Global game situation
	 * @param ownPlayer Information on our position etc.
	 * 
	 * @return Vector indicating the direction to move, length ~ speed
	 */
	@Override
	public Vector think( AiMapInfo map, AiPlayerInfo ownPlayer )
	{
	    // Update internal representation of the world
		m_cache.reset( map );
		m_locker.tick();
		m_observer.update( map.getFlags(), ownPlayer );

		updateFlagInformation( map.getFlags() );
		updateZombieInformation( map.getZombies() );
		
		
		// calculate reaction based on the environment
		calculateMovementSpeed();
		calculateMovements( map );
		
		return calculateMovement();
	}
	

	/**
	 * Calculate vectors from the bot's position to the zombies
	 * @param zombies Zombies to calculate the vectors for
	 */
	private void updateZombieInformation( AiZombieInfo[] zombies )
	{
        Vector ownPosition = m_player.getPosition();
		m_vectorsToZombies = new Vector[ zombies.length ];
		for( int i = 0; i < zombies.length; i++ )
			m_vectorsToZombies[ i ] = zombies[ i ].getPosition().sub( ownPosition );
	}
	
    /**
     * Calculate vectors from the bot's position to the flags ( = targets )
     * @param flags Flags to calculate the vectors for
     */
	private void updateFlagInformation( AiFlagInfo[] flags )
	{
	    Vector ownPosition = m_player.getPosition();
		m_vectorsToFlags = new Vector[ flags.length ];
		for( int i = 0; i < flags.length; i++ )
			m_vectorsToFlags[ i ] = flags[ i ].getPosition().sub( ownPosition );
	}
	
	/**
	 * Choose a flag where not so many zombies are, but which
	 * is also near us.
	 * 
	 * @param map Global game situation information
	 * @return Position of the flag ( = our target ) where to go
	 */
	private Vector chooseTarget( AiMapInfo map )
	{
		Vector ownPos = m_player.getPosition();
		AiFlagInfo[] flags = map.getFlags();
		
		// Next safe flag characteristics
		float min = Float.MAX_VALUE;
		Vector k  = VectorUtils.getMaximum( m_vectorsToFlags );
		
		for( int i = 0; i < flags.length; ++i )
		{
			if( flags[ i ].getPosition().euclideanDistance( ownPos ) < min )
			{
				// Only look at flags which are currently not locked ( == visited short time ago)
				if( !m_locker.isLocked( ActionLocker.ACTION_OWNED_FLAGS + i ) &&
						!flags[ i ].isOwner( m_player ) )
				{
					// Flag is safe? Epic -> use it (because it is [currently] the nearest one)
					if( m_cache.getZombiesInRange(
							flags[ i ].getPosition(),
							(int)( ZombieConstants.FLAG_CONQUER_VOLUME_RADIUS / m_greediness  ) ).length <= 1 )
					{
						k   = flags[ i ].getPosition();
						min = k.euclideanDistance( ownPos );
					}
				}

			}

		}
		
		return k.sub( ownPos ).getNoramlized();
	}
	
	/**
	 * Adjust speed so that the zombie which is nearest to us can't hear us.
	 */
	private void calculateMovementSpeed()
	{
		m_currentSpeed = VectorUtils.getMinimum( m_vectorsToZombies ).length()
				/ ( ZombieConstants.MAX_PLAYER_VOLUME_RADIUS + 1 );
	}

	/**
	 * Update movements towards the target and away from the zombies.
	 * @see chooseTarget()
	 * @see createEscapeVector()
	 * 
	 * @param map Current game situation
	 */
	private void calculateMovements( AiMapInfo map )
	{
		m_target = chooseTarget( map ).getNoramlized();
		if( new Float( m_target.x + m_target.y ).isNaN() )
			m_target = Vector.ZERO();
		
        m_target.mult( m_greediness );
        
		m_escapeMovement = createEscapeVector( map ).getNoramlized();
		if( new Float( m_escapeMovement.x + m_escapeMovement.y ).isNaN() )
		    m_escapeMovement = Vector.ZERO();
	}
	
	/**
	 * Get a vector which describes the escape direction from
	 * the zombies.
	 * 
	 * @param map Current game situation information
	 * @return Vector for escape movement
	 */
	private Vector createEscapeVector( AiMapInfo map )
	{
        m_escapeFactor = ( map.getAllOwnedFlags( m_player ).length * Math.max( 1, m_cache.getZombiesInRange( m_player.getPosition(),
                (int)ZombieConstants.MAX_PLAYER_VOLUME_RADIUS ).length ) )
                / map.getNumFlags();
	    
		Vector m = Vector.ZERO();
		
		AiZombieInfo[] zombies =  m_cache.getZombiesInRange( m_player.getPosition(),
                (int)ZombieConstants.MAX_PLAYER_VOLUME_RADIUS );
		
		for( int i = 0; i < zombies.length; ++i )
			m.addReference( zombies[ i ].getPosition().sub( m_player.getPosition() ) );
			
		return m.getNoramlized();
	}
	
	/**
	 * This method might later be used to create a vector to force escape
	 * from the zombies.
	 * 
	 * @return Escape vector
	 */
	@SuppressWarnings("unused")
    private Vector emergencyEscape( )
	{
	    Vector m = Vector.ZERO();
	    
	    AiZombieInfo[] zombies = m_cache.getZombiesInRange( m_player.getPosition(),
	                                                        (int)Math.max( ZombieConstants.MAX_PLAYER_VOLUME_RADIUS / 4.0f,
	                                                                  m_player.getCurrentNoiseRadius() * 2.0f ) );
	    
	    for( int i = 0; i < zombies.length; ++i )
	        m.addReference( zombies[ i ].getPosition().sub( m_player.getPosition() ) );
	    
	    return m.mult( -1 );
	}


    /**
     * Get sum of target and escape movement at calculated speed
     * @see calculateMovementSpeed
     * @see calculateMovements
     * 
     * @return The vector which will be the result of think()
     */
	public Vector calculateMovement( )
	{
	    Vector m = m_target.add( m_escapeMovement.mult( -1.0f ) ).getNoramlized();
	    
	    if( new Float( m.x + m.y ).isNaN() )
			m = new Vector( 0, 0 );
		
		return m.multReference( m_currentSpeed );
	}
	
	
	
    /**
     * This handler is called whenever a flag loses its "owned" state
     * 
     * @param flagIndex Index of the flag which was captured
     * @param flags List of current flag states
     */
    public void onFlagFreed( AiFlagInfo[] flags, int flagIndex )
    {
        // Avoid fighting for flags
        if( !m_locker.isLocked( ActionLocker.ACTION_OWNED_FLAGS + flagIndex ) )
            m_locker.lock( ActionLocker.ACTION_OWNED_FLAGS + flagIndex, 5 );
    }

    /**
     * This handler is called whenever an other bot captures a flag
     * 
     * @param flagIndex Index of the flag which was captured
     * @param flags List of current flag states
     */
    public void onOtherCapturedFlag( AiFlagInfo[] flags, int flagIndex )
    {
        // We cannot throw a bottle - so rest is not needed
        if( m_locker.isLocked( ActionLocker.ACTION_THROW_BOTTLE ) )
            return;
        
        Integer index = flags[ flagIndex ].getCurrentOwnerIndex();
        
        int[] playerFlags = new int[ ZombieConstants.MAX_NUM_PLAYERS ];
        
        // Find out how many flags which player has
        for( int i = 0; i < flags.length; ++i )
        {
            if( flags[ i ].isOwned() )
                playerFlags[ flags[ i ].getCurrentOwnerIndex() ]++;
        }
        
        // Find out player if is dangerous == max flags
        int bestPlayerIdx   = -1;
        int bestPlayerFlags = 0;
        for( int i = 0; i < ZombieConstants.MAX_NUM_PLAYERS; ++i )
        {
            // Player has more than 17% of flags and most flags over all?
            if( playerFlags[ i ] > bestPlayerFlags && 
                    playerFlags[ i ] > flags.length / 4 )
            {
                bestPlayerIdx = i;
                bestPlayerFlags = playerFlags[ i ];
            }
        }
        
        // We are the best? Ignore the event
        if( bestPlayerIdx == m_player.getPlayerIndex() )
            return;
        
        int nearestFlag = 0;
        float minDist    = Float.MAX_VALUE;
        
        for( int i = 0; i < flags.length; ++i )
        {
            if( i == flagIndex )
                continue;
            
            float distance = flags[ i ].getPosition().sub( flags[ flagIndex ].getPosition() ).length();
            if( distance < minDist && flags[ i ].getCurrentOwnerIndex() != index )
            {

                // Can we reach the flag?
                if( m_player.getPosition().sub( flags[ nearestFlag ].getPosition() ).length() / ZombieConstants.BOTTLE_SPEED
                        < minDist / ZombieConstants.MAX_PLAYER_SPEED + 25 )
                {
                    minDist = distance;
                    nearestFlag = i;
                }
            }
        }
        
        // Bottle to players "next flag"
        if( m_player.getPosition().sub( flags[ nearestFlag ].getPosition() ).length() / ZombieConstants.BOTTLE_SPEED
                < minDist / ZombieConstants.MAX_PLAYER_SPEED + 25 )
        {
            throwBottle( flags[ nearestFlag ].getPosition() );
            m_locker.lock( ActionLocker.ACTION_THROW_BOTTLE, 10 );
        }
    }

    /**
     * This handler is called when our bot captured a flag
     * 
     * @param flagIndex Index of the flag that was captured
     * @param flags List of current flag states
     */
    public void onSelfCapturedFlag( AiFlagInfo[] flags, int flagIndex )
    {
        // Lock every captured flag for 25 turns to avoid conflicts with other zombies
        if( !m_locker.isLocked( ActionLocker.ACTION_OWNED_FLAGS + flagIndex ) )
            m_locker.lock( ActionLocker.ACTION_OWNED_FLAGS + flagIndex, 25 );
    }

}
