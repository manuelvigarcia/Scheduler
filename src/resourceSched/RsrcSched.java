package resourceSched;

import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import givenAPI.Gateway;
import givenAPI.Message;

public class RsrcSched implements Scheduler{
	private static final Logger LOG = Logger.getGlobal();
	public static final Level globalLogLevel = Level.SEVERE;

//	private final int totalResources;
//	private int usedResources = 0;
	private final Gateway gw;
	static RsrcSched currentRS;
	private MessageQ msgQ;
	private Semaphore semaphore;
	
	
	public RsrcSched (int resources, Gateway gatew){
		LOG.setLevel(globalLogLevel);
		if (null != currentRS){ /*Already created*/
			throw (new IllegalStateException("Schedluer exists."));			
		}
		if (1 > resources) {
			throw new IllegalArgumentException("Need at least 1 resource.");
		}
		if (null != gatew){
			gw = gatew;
		} else {
			throw new IllegalArgumentException("Gateway must not be null.");
		}
		semaphore = new Semaphore(resources);
		msgQ = new MessageQ();
//		totalResources = resources;
		currentRS = this;
	}
	
	/**
	 * Send a message through the Gateway.
	 * To improve resource usage, the first thing to do is sending the
	 * message if there are resources. Queue afterwards.
	 * */
	synchronized public boolean sendAndQueueMsg(MessageWrapper msg){
		boolean alreadySent;
//		if (totalResources > usedResources) {    /*Idle!!!*/
		if (semaphore.availablePermits() > 0){ /*Idle*/
			try{
				semaphore.acquire();
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
			gw.send((Message)msg);
//			usedResources ++;
//			LOG.warning("gw.sent(). usedResources = "+ usedResources);
			alreadySent = true;
		}
		else { 
			alreadySent = false;
		}
		if (!alreadySent) LOG.warning("Queuing "+ msg);
		msgQ.addMsg(msg, alreadySent);
		return true;
	}

	/**
	 * Retrieve the existing Scheduler.
	 * */
	public static RsrcSched getRS(){
		if (null != currentRS){
			return currentRS;
		} else {
			throw (new IllegalStateException("No Schedluer exists."));
		}
		
	}

	public static void discard() {
		currentRS = null;
		
	}

	@Override
	public void freeResource() {
		semaphore.release();
	}
	/**
	 * If there are idle resources, send messages.
	 * Decrease the count for this group; if there are no more messages
	 * queued for the group it will not be considered "in progress" any more
	 * note that if there where messages in this group, they've been picked
	 * before decreasing the count.
	 * */
	@Override
	synchronized public void moreMessages(int gID) {
//		usedResources--;  /*completed() signals that one resource has finished*/
//		LOG.warning("Freed one. usedResources = " + usedResources + " totalResources = " + totalResources);
//		while (totalResources > usedResources){ //At least one idle.
		while (semaphore.availablePermits()>0){
			try{
				semaphore.acquire();
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
			Message msg = (Message) msgQ.getNextMsg();
			if (null != msg){ /*There are messages in the queue*/
				gw.send(msg);
//				usedResources ++;
			} else {
				LOG.warning("No more Messages");
				break;
			}
		}
		/*In any case: decrease the count for the groupID
		 *  of the completed() message*/
		msgQ.removeGroupID(gID);
	}
}