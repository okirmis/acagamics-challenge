import java.util.ArrayList;
import java.util.HashMap;

import core.Vector;
import core.ai.AiMapInfo;
import core.ai.AiZombieInfo;

/**
 * Class to speedup getZombiesInRange calls
 * 
 * Cache the zombies in a given range to avoid using the given
 * class from acagamics which is horribly slow due to the creation
 * of new objects for every zombie every time the function getZombiesInRange
 * is called. Also cache the state for given ranges and positions so that
 * iterating over all zombies is not necessary every time.
 * 
 * @note This class is not required but it heavily improves the performance of
 *       General Purpose.
 * 
 * @author Oskar Kirmis <kirmis@st.ovgu.de>
 */
final public class ZombiesInRangeCache
{
    //! Internal zombie information map to be used for the current turn
    private AiMapInfo m_map = null;
    //! Cache for zombie queries at a given position and range
    private HashMap<String, AiZombieInfo[]> m_cache;
    //! Copy of zombie info to avoid performance problems
    private AiZombieInfo[] m_info = null;
    
    /**
     * Construct a new empty zombies cache.
     */
    public ZombiesInRangeCache()
    {
        m_cache = new HashMap<String, AiZombieInfo[]>();
    }
    
    /**
     * Reset the internal state of the cache (clear cached entries).
     * This has to be done in every turn.
     * 
     * @param map Zombie information map used for the current turn
     */
    public void reset( AiMapInfo map )
    {
        assert map != null : "Zombie information map cannot be null";
        
        // Reset status information
        m_map = map;
        
        // Clear last cache
        m_cache.clear();
        
        m_info = m_map.getZombies();
    }
    
    /**
     * This is the main method of this class where the zombies for
     * a given position and range can be queried. If during the
     * current turn zombies for the given position and range have already been
     * queried, the cached result will be returned. Otherwise the zombies in the
     * range are computed, cached and returned.
     * 
     * @param pos Position from where to check the zombies in the range
     * @param range The radius of the circle around the position where to check for zombies
     * 
     * @return An array containing all zombies in the given range around the given position.
     */
    public AiZombieInfo[] getZombiesInRange( Vector pos, int range )
    {
        String key = ZombiesInRangeCache.createKey( pos, range );
        
        // Was this already cache for the current turn?
        if( m_cache.containsKey( key ) )
        {
            // Yes -> so return the cached values
            return m_cache.get( key );
        }
        else
        {
            // Nope -> we have to query the information
            ArrayList<AiZombieInfo> info = new ArrayList<AiZombieInfo>();
            for( int i = 0; i < m_info.length; ++i )
            {
                if( m_info[ i ].getPosition().add( 
                        m_info[ i ].getMovement() ).sub( pos ).length() <= range )
                    info.add( m_info[ i ] );
            }
            
            // List to array
            AiZombieInfo[] zbInfo = new AiZombieInfo[ info.size() ];
            for( int i = 0; i < info.size(); ++i )
                zbInfo[ i ] = info.get( i );

            // Cache list until next update
            m_cache.put( key, zbInfo );
            
            return zbInfo;
        }
    }
    
    /**
     * Create a string that is unique for a given position and a given range.
     * This will be used as the key for the cache map.
     * 
     * @param pos Position to get the key for
     * @param range Range to get the key for
     * 
     * @return A unique key for the position and the range
     */
    private static String createKey( Vector pos, int range )
    {
        // For example, position (10,8) with range 20 creates the
        // key "10/8/20"
        return pos.x + "/" + pos.y + "/" + range;
    }
}