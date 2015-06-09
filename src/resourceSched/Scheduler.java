package resourceSched;

public interface Scheduler {
	/**
	 * When a message gets completed() it will call the scheduler's
	 * moreMessages to signal that a resource has finished processing.
	 * To improve resource usage, moreMessages() will trigger sending as many
	 * as possible (counting on resources idle); so one call may trigger
	 * sending more than one message (if other message finished processing
	 * meanwhile) and other call may not trigger sending messages (if there
	 * are no more idle resources).
	 * 
	 * @param groupID decrease the count on this group. If no messages are left
	 * queued for this group, it will not be considered "in progress" any more.
	 * */
	public void moreMessages(int groupID);


	/**
	 * signal that one resource has been freed.
	 * */
	public void freeResource();

}
