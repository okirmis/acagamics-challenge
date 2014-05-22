import core.ai.AiFlagInfo;


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
