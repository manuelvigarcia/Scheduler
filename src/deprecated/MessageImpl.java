package deprecated;

import java.util.logging.Level;
import java.util.logging.Logger;

import resourceSched.MessageWrapper;
import resourceSched.RsrcSched;
import givenAPI.Message;


public class MessageImpl implements Message{
	private static final Logger LOG = Logger.getGlobal();
	private static final Level globalLogLevel = RsrcSched.globalLogLevel;

	private final int groupID;
	private final Object payload;
	private final MessageWrapper wrapper;
	
	
	public MessageImpl (MessageWrapper msg){
		LOG.setLevel(globalLogLevel);
		groupID = msg.getGId();
		payload = msg.getBody();
		wrapper = msg;
	}

	@Override
	public String toString(){
		return groupID + "." + payload.toString();
	}

	/**
	 * this message has been processed, a resource has been freed
	 * */
	@Override
	public void completed() {
		wrapper.completed();
	}
}
