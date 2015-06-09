package test;

/*This test class is in place of the consumer in production env. */

import java.util.Timer;

import givenAPI.Gateway;
import givenAPI.Message;

/**
 * Simulate lengthy process of a message.
 * Take (at least) half second from receiving a message to calling
 *  its completed().
 * */
public class GatewayMockUp implements Gateway {
	Timer delayer = new Timer();

	@Override
	public void send(Message msg) {
		delayer.schedule(
				new java.util.TimerTask() {
					@Override
			        public void run() {
						msg.completed();
					}
			    }, 
			    500 
			);
	}

}
