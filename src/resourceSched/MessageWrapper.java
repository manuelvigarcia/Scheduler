package resourceSched;

import java.util.logging.Level;
import java.util.logging.Logger;

import deprecated.MessageImpl;
import givenAPI.Message;

public class MessageWrapper implements Message, Runnable{
	private static final Logger LOG = Logger.getGlobal();
	private static final Level globalLogLevel = RsrcSched.globalLogLevel;
	private static int activeMsgs = 0;
	private static Scheduler theScheduler = null;

	private final int groupID;
	private final int body;
	
	public MessageWrapper (int gid, int payload){
		if (null == theScheduler){
			throw(new IllegalStateException("Can't start w/o scheduler."));
		}
		LOG.setLevel(globalLogLevel);
		groupID = gid;
		body = payload;
		increaseActiveMsgs();
	}
	@Override
	public String toString(){
		return groupID + "." + body;
	}
	public int getGId(){
		return groupID;
	}
	public int getBody(){
		return body;
	}
	public static int getActiveMsgs(){
		return activeMsgs;
	}
	public static void setScheduler(Scheduler aScheduler){
		theScheduler = aScheduler;
	}
	public static void resetScheduler(){
		theScheduler = null;
	}
	private synchronized void increaseActiveMsgs(){
		activeMsgs ++;
	}
	
	private synchronized void decreaseActiveMsgs(){
		activeMsgs --;
		if (activeMsgs < 0) {
			throw new IllegalStateException ("one too many completed()");
		}
	}
	public Message getMessage() {
		return new MessageImpl(this);
	}

	
	/**
	 * this message has been processed, a resource has been freed
	 * This method is called on the Gateway thread: vacate is ASAP
	 * */
	@Override
	public void completed() {
		LOG.warning("Inside completed");
		theScheduler.freeResource();
		(new Thread(this)).start();
		//gateway tread vacated.
	}

	@Override
	public void run() {
		LOG.warning("Deferred completed()");
		decreaseActiveMsgs();
		theScheduler.moreMessages(groupID);		
	}

}
