package test;

/*This test class is in place of the consumer in production env. */

import java.util.Timer;
import java.util.logging.Logger;

import givenAPI.Gateway;
import givenAPI.Message;

/**
 * Simulate lengthy process of a message.
 * Take (at least) half second from receiving a message to calling
 *  its completed().
 * */
public class GatewayMockUpParameterized implements Gateway {
	private Timer delayer = new Timer();
	private SchedulerParameterTest whom;
	private static final Logger LOG = Logger.getGlobal();

	public void setCounter (SchedulerParameterTest master){
		LOG.warning("Counter set.");
		whom = master;
		
	}

	@Override
	public void send(Message msg) {
		LOG.warning("entering send");
		whom.thisArrived(msg); /*notify immediately about messages sent*/
		delayer.schedule(
				new java.util.TimerTask() {
					@Override
			        public void run() {
						LOG.warning("Calling completed()");
						msg.completed();
					}
			    }, 
			    500 
			);
	}
}
