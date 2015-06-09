package test;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

import resourceSched.MessageQ;
import resourceSched.MessageWrapper;
import resourceSched.RsrcSched;

public class MessageQTest {
	private static final Logger LOG = Logger.getGlobal();
	private static final Level globalLogLevel = RsrcSched.globalLogLevel;

	@BeforeClass
	public static void setTheScheduler(){
		/*Only need the scheduler to get allowed to create msgs*/
		try {
			MessageWrapper.setScheduler(new RsrcSched(1, new GatewayMockUp()));
		} catch (IllegalStateException ise) {
			LOG.severe("Clean up because: " + ise.getMessage());
		}
	}

	@AfterClass
	public static void resetTheScheduler(){
		MessageWrapper.resetScheduler();
		try{
			MessageWrapper msg = new MessageWrapper(1, 0);
			if (null != msg){
				msg = null;
			}
		} catch (IllegalStateException ise) {
			assertEquals(ise.getMessage(), "Can't start w/o scheduler.");
		} catch (Exception e) {
			fail ("unexpected exception" + e.toString());
		}
		
	}

	@Test
	public void creatorTest(){
		MessageQ mq = new MessageQ();
		assertNotNull(mq);
	}
	
	@Test
	public void getNextMsgTest() {
		MessageQ mq = new MessageQ();
		RsrcSchedMockUp rs = new RsrcSchedMockUp ();
		MessageWrapper.setScheduler(rs);
		MessageWrapper msg = mq.getNextMsg();
		assertNull(msg);
		msg = new MessageWrapper(1,1);
		mq.addMsg(msg, false);
		assertEquals(msg, mq.getNextMsg());
	}
	
	@Test
	public void addMsgTest(){
		RsrcSchedMockUp rs = new RsrcSchedMockUp ();
		MessageWrapper.setScheduler(rs);
		MessageQ mq = new MessageQ();
		MessageWrapper msg = new MessageWrapper(Integer.MAX_VALUE,Integer.MAX_VALUE);
		mq.addMsg(msg, false);
		assertEquals(msg, mq.getNextMsg());
		msg = new MessageWrapper(Integer.MIN_VALUE,Integer.MIN_VALUE);
		mq.addMsg(msg, false);
		assertEquals(msg, mq.getNextMsg());
		msg = new MessageWrapper(-1,-1);
		mq.addMsg(msg, false);
		assertEquals(msg, mq.getNextMsg());
	}
	
	@Test
	public void testLongQ(){
		MessageQ mq = new MessageQ();
		RsrcSchedMockUp rs = new RsrcSchedMockUp ();
		MessageWrapper.setScheduler(rs);
		int testingSize = 1000;
//		LOG.setLevel(Level.FINE);
		assertTrue(testingSize < Integer.MAX_VALUE / 2);
		for (int i = 0; i < testingSize; i++){
			/*Fill the Q with one of each groupID*/
			mq.addMsg(new MessageWrapper(i, 1), false);			
		}
		assertTrue(MessageWrapper.getActiveMsgs() == testingSize);
		LOG.fine("done with the first " + testingSize);
		for (int i = 0; i < testingSize; i++){
			/*Fill the Q with another one of each groupID*/
			mq.addMsg(new MessageWrapper(i, 2), false);			
		}
		assertTrue(MessageWrapper.getActiveMsgs() == testingSize * 2);
		LOG.fine("done with the last " + testingSize);
		for (int i = 0; i < testingSize; i++){
			MessageWrapper msg = mq.getNextMsg();
			assertNotNull (msg);
			LOG.fine("retrieved: " + msg.getGId() + "." + msg.getBody());
			int gid = msg.getGId();
			/*check that groupIDs are processed in arrival sequence*/
			assertTrue(gid == i);
			/*check that you get two of each groupID together*/
			msg = mq.getNextMsg();
			LOG.fine("retrieved: " + msg.getGId() + "." + msg.getBody());
			assertTrue(gid == msg.getGId());
		}
		LOG.fine("Got all OK");
		/*Check that the "different" groupID will be processed after all the "equal"*/
		for (int i = 0; i < testingSize; i++){
			/*Fill the Q with groupID=1 messages*/
			mq.addMsg(new MessageWrapper(1, i), false);			
		}
		LOG.fine("add a different one");
		mq.addMsg(new MessageWrapper(2, testingSize), false);			
		for (int i = 0; i < testingSize; i++){
			/*Fill the Q with another one of each groupID*/
			mq.addMsg(new MessageWrapper(1, testingSize + i + 1), false);			
		}
		/*Check that all groupID=1 come first*/
		for (int i = 0; i < testingSize*2; i++){
			assertTrue(mq.getNextMsg().getGId() == 1);
		}
		/*Check that there is one left with groupID=2*/
		assertTrue(mq.getNextMsg().getGId() == 2);
		LOG.fine("Everything OK");
		/*Check that we didn't leave anything there*/
		assertTrue(mq.getNextMsg() == null);
		LOG.fine("and no messages left");
		LOG.setLevel(globalLogLevel);
	}
}
