package test;

import static org.junit.Assert.*;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.BeforeClass;
import org.junit.Test;

import givenAPI.Gateway;
import resourceSched.MessageWrapper;
import resourceSched.RsrcSched;


public class SchedulerTest {
	private static final Logger LOG = Logger.getGlobal();
	private static final Level globalLogLevel = RsrcSched.globalLogLevel;

	@BeforeClass
	public static void setUp(){
		LOG.setLevel(globalLogLevel);
		RsrcSched.discard();
	}
	
	
	@Test
	public void alwaysPass(){
		LOG.info("\n\ntest that always passes.");
		
		LOG.log(Level.OFF, "logging is including OFF.");
		LOG.log(Level.SEVERE, "logging is including SEVERE.");
		LOG.log(Level.WARNING, "logging is including WARNING.");
		LOG.log(Level.INFO, "logging is including INFO.");
		LOG.log(Level.CONFIG, "logging is including CONFIG.");
		LOG.log(Level.FINE, "logging is including FINE.");
		LOG.log(Level.FINER, "logging is including FINER.");
		LOG.log(Level.FINEST, "logging is including FINEST.");
		LOG.log(Level.ALL, "logging is including ALL.");

		assertTrue(true);
	}
	
	@Test
	public void testSchedCreation(){
		LOG.warning("\n\ntestSchedCreation");
		//LOG.setLevel(Level.INFO);
		LOG.info("Start Test Scheduler. Creation Failures.");

		LOG.info("alocate variables");
		Gateway gw = new GatewayMockUp();

		LOG.info("attempt creation with 0 resources");
		try {
			RsrcSched rs = new RsrcSched(0, gw);
			if (null != rs) RsrcSched.discard();
			fail ("succeeded creating a 0-resourced scheduler.");
		} catch (IllegalArgumentException iae){
			LOG.info("failed with IllegalArgumentException: " + iae.getMessage());
		} catch (Exception e){
			fail ("Threw exception different from IllegalArgument: " + e.getMessage());
		}

		LOG.info("attempt creating with null gateway");
		try {
			RsrcSched rs = new RsrcSched(1,null);
			if (null != rs) RsrcSched.discard();
			fail ("succeeded creating with a null gateway.");
		} catch (IllegalArgumentException iae){			
			LOG.info("failed with IllegalArgumentException: " + iae.getMessage());
		} catch (Exception e){
			fail ("Threw exception different from IllegalArgument.");
		}
		
		LOG.info("Create correct scheduler");
		try {
			RsrcSched rs = new RsrcSched(1,gw);
			assertNotNull(rs);
			RsrcSched.discard(); /*discard*/
		} catch (Exception e){
			fail ("Unexpected exception.");
		}

		try {
			RsrcSched rs = new RsrcSched(1,gw);
			LOG.info("Attempt creation of a second scheduler");
			RsrcSched rs2 = new RsrcSched(1,gw);
			if (null != rs) RsrcSched.discard();
			if (null != rs2) RsrcSched.discard();
		} catch (IllegalStateException ise){
			LOG.info("failed with IllegalStateException: " + ise.getMessage());
		} catch (Exception e){
			fail ("Threw exception different from IllegalStateException");
		}
		RsrcSched.discard();
		LOG.setLevel(globalLogLevel);
	}
	

	@Test
	public void testOneResourcePerMsg() {
		LOG.info("\n\nTest enough resources for incoming messages.");
		
//		LOG.setLevel(Level.INFO);
		for (int i = 1; i < 10; i ++){
			LOG.info("Series for " + i + " available resource");
			Gateway gw = new GatewayMockUp();
			assertNotNull(gw);
			RsrcSched rs = new RsrcSched(i, gw);
			assertNotNull(rs);
			MessageWrapper.setScheduler(rs);
			for (int body = 1; body <= i; body ++){
            	LOG.info(i + "." + body + " sending.");				
				rs.sendAndQueueMsg(new MessageWrapper(i, body));
            	LOG.info(i + "." + body + " sent.");				
			}
			RsrcSched.discard(); /*discard*/
		}
		LOG.info("Done. Wait for complete");
		while (MessageWrapper.getActiveMsgs() > 0){
			try {   /*wait for all the messages to be processed*/
				Thread.sleep(500);
			} catch (InterruptedException ie){
				ie.printStackTrace();
			}
		}
		RsrcSched.discard();
		LOG.setLevel(globalLogLevel);
	}
	
    public static void getCompletedIndication(int group, int msgCount) {
//		LOG.setLevel(Level.INFO);
        Thread t = new Thread(new Runnable() {
            public void run() {
            	LOG.info(group + "." + msgCount + " completed.");
            }
        });
        t.start();
		LOG.setLevel(globalLogLevel);
    } 

}
