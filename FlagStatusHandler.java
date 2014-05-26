import core.ai.AiFlagInfo;

/**
 * This interface should have specified the methods to
 * handle flag changes. But because of class loader restrictions,
 * the bot cannot have any interfaces :(
 * @unused
 * 
 * @author Oskar Kirmis <kirmis@st.ovgu.de>
 */
public interface FlagStatusHandler
{
    /**
     * This handler is called whenever a flag loses its "owned" state
     * 
     * @param flagIndex Index of the flag which was captured
     * @param flags List of current flag states
     */
    public void onFlagFreed( AiFlagInfo[] flags, int flagIndex );

    /**
     * This handler is called whenever an other bot captures a flag
     * 
     * @param flagIndex Index of the flag which was captured
     * @param flags List of current flag states
     */
    public void onOtherCapturedFlag( AiFlagInfo[] flags, int flagIndex );

    /**
     * This handler is called when our bot captured a flag
     * 
     * @param flagIndex Index of the flag that was captured
     * @param flags List of current flag states
     */
    public void onSelfCapturedFlag( AiFlagInfo[] flags, int flagIndex );

}
