import core.Vector;
import core.ai.AiFlagInfo;
import core.ai.AiMapInfo;
import core.ai.AiPlayerInfo;
import core.ai.AiZombieInfo;
import core.constants.ZombieConstants;
import core.player.PlayerController;


public class DummyFlagLock extends PlayerController
{
    final float CTRL_NOISE_SCALE = 1.1f;
    final float CTRL_ESCAPE_PRIOR = 2.0f;
    final float CTRL_FLAG_PRIOR = 1.0f;
    final float CTRL_ESCAPE_POWER = 2.0f;
    final boolean CTRL_FLAG_LOCK_ENABLED = true;
    
    int[] m_lockedFlags = null;
    boolean[] m_lastOwnFlags = null;
    
    public String getName()
    {
        return "Dummy";
    }
    
    public String getAuthor()
    {
        return "iftrue";
    }

    public void onGameStarted( String gameTypeName, AiMapInfo map, AiPlayerInfo ownPlayer )
    {
        if( CTRL_FLAG_LOCK_ENABLED )
        {
            // Initialize flag lock states
            m_lockedFlags = new int[ map.getNumFlags() ];
            m_lastOwnFlags = new boolean[ map.getNumFlags() ];

            for( int i = 0; i < m_lockedFlags.length; m_lockedFlags[ i ] = 0, m_lastOwnFlags[ i++ ] = false );
        }
    }

    @Override
    public Vector think( AiMapInfo map, AiPlayerInfo ownPlayer )
    {
        Vector pos = ownPlayer.getPosition();
        
        // Nearest flag information
        AiFlagInfo[] flags  = map.getFlags();
        Vector flagMovement = new Vector( Float.MAX_VALUE, Float.MAX_VALUE );
        
        for( int i = 0; i < flags.length; ++i )
        {
            if( CTRL_FLAG_LOCK_ENABLED )
                // Decrease lock state counters for flags (to avoid two bots "fighting" for same flag)
                --m_lockedFlags[ i ];
            
            if( flags[ i ].isOwner( ownPlayer ) )
            {
                if( CTRL_FLAG_LOCK_ENABLED )
                {
                    if( !m_lastOwnFlags[ i ] )
                        // Lock flag for 20 steps
                        m_lockedFlags[ i ] = 20;
                }
                continue;
            }
            
            if( CTRL_FLAG_LOCK_ENABLED )
                // Save if it is our flag this turn
                m_lastOwnFlags[ i ] = flags[ i ].isOwner( ownPlayer );
            
            
            Vector m = flags[ i ].getPosition().sub( pos );
            if( ( !CTRL_FLAG_LOCK_ENABLED || m_lockedFlags[ i ] <= 0 ) && m.length() < flagMovement.length() )
                flagMovement = m;
        }
        
        // This will fail if we own all flags
        flagMovement.normalize();
        
        
        // Escape movement
        float radius = ownPlayer.getCurrentNoiseRadius() * CTRL_NOISE_SCALE;
        Vector escape = new Vector( 0,0 );
        AiZombieInfo[] zombies = map.getZombiesInRadius( pos, radius );
        
        for( int i = 0; i < zombies.length; ++i )
        {
            Vector thisZombieVect = zombies[ i ].getPosition().sub( pos );
            escape.addReference( thisZombieVect.mult(
                                                 -(float)Math.pow( radius / thisZombieVect.length(), CTRL_ESCAPE_POWER )
                                                 ) );
        }
        
        if( escape.length() != 0 )
            escape.normalize();
        
        Vector movement = escape.mult( CTRL_ESCAPE_PRIOR ).add( 
                                    flagMovement.mult( CTRL_FLAG_PRIOR )
                                );
        if( movement.length() == 0 )
            return new Vector( 0, 0 );
        movement.normalize();
        
        // Don't fall out of the world
        float distanceToMid = pos.add( movement ).length();
        while( distanceToMid > ZombieConstants.MAP_RADIUS )
        {
            movement = movement.add( pos.mult( -1.0f ) );
            distanceToMid = pos.add( movement ).length();
        }
        
        return movement.getNoramlized();
    }
}
