import core.ai.AiFlagInfo;
import core.ai.AiPlayerInfo;

/**
 * This class watches flags for changes and handles
 * them, for example, by locking changed flags
 * 
 * @author Oskar Kirmis <kirmis@st.ovgu.de>
 */
final public class FlagObserver
{
    //! State of the flags last turn; needed to detected changes
    Integer[] m_previousFlagStates = null;
    //! ActionLocker which locks actions when we captured a flag
    ActionLocker m_locker = null;
    //! Own player information
    AiPlayerInfo m_ownPlayer = null;
    // Own bot instance
    GeneralPurpose m_controller;
    
    /**
     * Creates a new empty flag observer for a given number of flags
     * 
     * @param numFlags Number of flags to observe
     * @param locker   An instance of action locker which is used to handle flag events
     * @param ownPlayer Instance of player information
     * @param bot Instance of our bot
     */
    public FlagObserver( int numFlags, AiPlayerInfo ownPlayer, GeneralPurpose bot, ActionLocker locker )
    {
        assert numFlags >= 0 : "Number of flags cannot be negative";
        assert locker != null : "ActionLocker reference should not be null";
        
        m_locker = locker;
        m_ownPlayer = ownPlayer;
        m_controller = bot;
        
        // Create array and initialize it
        m_previousFlagStates = new Integer[ numFlags ];
        for( int i = 0; i < numFlags; ++i )
            m_previousFlagStates[ i ] = null;
    }
    
    /**
     * Update the state of flags and handle the changes. Changes are
     * detected by comparing the previous ownership index to the current one
     */
    public void update( AiFlagInfo[] flags, AiPlayerInfo ownPlayer )
    {
        assert flags.length == m_previousFlagStates.length : "Number of flags changed during the game";
        
        for( int i = 0; i < flags.length; ++i )
        {
            Integer oi = flags[ i ].getCurrentOwnerIndex();
            
            // Check for ownership changes
            if( oi == m_previousFlagStates[ i ] )
                continue;
            

            if( oi == null )
            {
                // Flag was freed
                m_controller.onFlagFreed( flags, i );
                continue;
            }
            
            // Do we captured a flag?
            if( oi == ownPlayer.getPlayerIndex() )
            {
                m_controller.onSelfCapturedFlag( flags, i );
                continue;
            }
            
            // An other bot captured a flag?
            m_controller.onOtherCapturedFlag( flags, i );
        }
        
        // Copy states for next turn
        for( int i = 0; i < flags.length; ++i )
            m_previousFlagStates[ i ] = flags[ i ].getCurrentOwnerIndex();
    }
}