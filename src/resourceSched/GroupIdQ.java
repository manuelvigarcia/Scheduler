package resourceSched;

import java.util.concurrent.ConcurrentLinkedQueue;

public class GroupIdQ{
	private ConcurrentLinkedQueue<MessageWrapper> msgQ;
	/*The queue for a groupID has a constant groupId until death*/
	private final int groupId; 
	
	public GroupIdQ (int gId){
		groupId = gId;
		msgQ = new ConcurrentLinkedQueue<MessageWrapper>();
	}
	
	public int getGroupId(){
		return groupId;
	}
	public final void setGroupId(int gId){
		/*You wish*/
	}
	public void queueMsg(MessageWrapper msg){
		if (msg.getGId() == groupId){
			msgQ.add(msg);
		} else {
			throw (new IllegalArgumentException("Wrong group queue for that group ID."));
		}
	}
	public MessageWrapper popMsg(){
		return msgQ.poll();
	}
	public int nofMsgs(){
		return msgQ.size();
	}
}
