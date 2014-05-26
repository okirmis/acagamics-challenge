/**
 * Lock actions for a given number of ticks
 * 
 * The actions are references by ACTION_*-constants. If a special flag 
 * should be referenced, simply use: ACTION_OWNED_FLAGS + idOfTheFlag
 * 
 * @author Oskar Kirmis <kirmis@st.ovgu.de>
 */
final public class ActionLocker
{

    // Constants used for actions IDs
    public static final int ACTION_THROW_BOTTLE = 0;
    public static final int ACTION_HIT_POT = 1;
    public static final int ACTION_ESCAPE = 2;
    public static final int ACTION_OWNED_FLAGS = 3;
    
    // Registered actions which can be locked
    private int[] m_registeredActions = null;
    
    /**
     * Initialize the action locker (every counter is set to 0)
     * 
     * @param[in] numFlags Number of flags to capture in the game
     */
    public void initialize( int numFlags )
    {
        this.m_registeredActions = new int[ 
                                    ActionLocker.ACTION_OWNED_FLAGS + numFlags 
                                ];
        // Reset all counters
        for( int i = 0; i < this.m_registeredActions.length; ++i )
            this.m_registeredActions[ i ] = 0;
    }
    
    /**
     * Decrease all counters that are greater zero. This method should always
     * be called when the think-method is called.
     */
    public void tick()
    {
        for( int i = 0; i < this.m_registeredActions.length; ++i )
        {
            // Count down state for locked actions
            if( this.m_registeredActions[ i ] > 0 )
                --this.m_registeredActions[ i ];
        }
    }
    
    /**
     * Lock the action with given "actionID" for "ticks" number of ticks.
     * 
     * @param actionID Id of the action to be locked
     * @param ticks    Number of ticks the action has to be locked
     */
    public void lock( int actionID, int ticks )
    {
        // Some preconditions
        assert this.m_registeredActions.length > actionID : "Invalid action given: " + actionID;
        assert this.m_registeredActions[ actionID ] == 0  : "Couter for action " + actionID + " is not 0!";
        assert ticks >= 0                                 : "Invalid number of ticks: " + ticks;
        
        // Set "timer"
        this.m_registeredActions[ actionID ] = ticks;
    }
    
    /**
     * Check if the given action id currently is locked
     * 
     * @param actionID Id of the action to check
     * @return true, if the action is locked, false, if not
     */
    public boolean isLocked( int actionID )
    {
        assert this.m_registeredActions.length > actionID : "Invalid action given: " + actionID;
        
        // If required ticks is greater zero, the action is still locked
        return this.m_registeredActions[ actionID ] > 0;
    }
}