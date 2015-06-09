package resourceSched;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MessageQ {
	private static final Logger LOG = Logger.getGlobal();
	private static final Level globalLogLevel = RsrcSched.globalLogLevel;
	private final List<GroupIdQ> msgGroups;
	
	public MessageQ(){
		LOG.setLevel(globalLogLevel);
		msgGroups = new ArrayList<GroupIdQ>();
	}
	/**
	 * Find out which is the next message to send through the Gateway,
	 * remove it from the queue, and return it.
	 * @return null if there are no more messages, else
	 * @return next message to send in arrival order, from the "in progress"
	 *          groupID first
	 * */
	public MessageWrapper getNextMsg() {
		/*find first GroupIdQ with a message*/
//		LOG.setLevel(Level.FINE);
		MessageWrapper msg = null;
		for (Iterator<GroupIdQ> groups = msgGroups.listIterator(); groups.hasNext();){
			GroupIdQ gidQ = groups.next();
			LOG.fine("Checking GroupID " + gidQ.getGroupId() + " with " + gidQ.nofMsgs() + " messages.");
			if (0 < gidQ.nofMsgs()){ /*found one*/
				msg = gidQ.popMsg();
				break;
			}
		}		
//		LOG.setLevel(globalLogLevel);
		return msg;
	}
	/**
	 * Add a message to the queue.
	 * Set "in progress" the group of the message.
	 * Since "first send, then queue," only actually queue if not yet sent
	 */
	public void addMsg(MessageWrapper msg, boolean sent) {
		int group = ((MessageWrapper)msg).getGId();
		GroupIdQ groupQ= addGroupID(group);
		if (!sent){
			groupQ.queueMsg(msg);
		}
	}
	
	

	/**
	 * Add an empty GroupIdQ (with no messages).
	 * This covers the case of message sent right away: the GroupIdQ
	 * must still exist to remember that this groupID is "in progress"
	 * at least until after the completed() is called.
	 * */
	private GroupIdQ addGroupID(int groupID) {
		/*Check if the groupID queue already exists.*/
		GroupIdQ gidQ = null;
		for (Iterator<GroupIdQ> groups = msgGroups.listIterator(); groups.hasNext();){
			gidQ = groups.next();
			if (gidQ.getGroupId() == groupID) { /*It exists*/
				break;
			} 
			if (groups.hasNext() == false){
				gidQ = null;
			}
		}
		if (null == gidQ) {
			gidQ = new GroupIdQ(groupID);
			msgGroups.add(gidQ);
		}
		return gidQ;
	}
	/**
	 * Find groupID message queue and remove it if it is empty.
	 * This is called after a message has been completed to avoid keeping the
	 * queue for this groupID if there are no messages queued. This allows 
	 * choosing as "next message" one that has arrived after the last one
	 * in the same groupID has been sent, but before it has been completed().
	 * @param groupID suspected groupID
	 * */
	public void removeGroupID(int groupID){
		for (Iterator<GroupIdQ> iter = msgGroups.listIterator(); iter.hasNext();) {
			GroupIdQ q = iter.next();
			if (q.getGroupId() == groupID){/*This is the one*/
				if (q.nofMsgs() == 0){
					iter.remove();
				}
				break;
			}
		}
	}
}
