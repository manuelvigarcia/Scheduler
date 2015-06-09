package test;

import static org.junit.Assert.*;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import resourceSched.GroupIdQ;
import resourceSched.MessageWrapper;
import resourceSched.RsrcSched;

public class GroupIdQTest {
	private static final Logger LOG = Logger.getGlobal();
	private static final Level globalLogLevel = RsrcSched.globalLogLevel;

	@BeforeClass
	public static void setTheScheduler(){
		LOG.setLevel(globalLogLevel);
		/*Only need the scheduler to get allowed to create msgs*/
//		try {
//			MessageWrapper.setScheduler(new RsrcSched(1, new GatewayMockUp()));
//		} catch (IllegalStateException ise) {
//			LOG.severe("Clean up because: " + ise.getMessage());
//		}
	}
	
	@Test
	public void testGroupIdQ() {
		int testSize = 1000;
		for (int i = 0; i < testSize; i++) {
			GroupIdQ q = new GroupIdQ (i);
			assertNotNull(q);
		}
	}

	@Test
	public void testGetGroupId() {
		int testSize = 1000;
		for (int i = 0; i < testSize; i++) {
			GroupIdQ q = new GroupIdQ (i);
			assertTrue(i == q.getGroupId());
		}
	}

	@Test
	public void testSetGroupId() {
		int testSize = 1000;
		for (int i = 0; i < testSize; i++) {
			GroupIdQ q = new GroupIdQ (i);
			q.setGroupId(-1);
			/*Check that the groupID did NOT change*/
			assertFalse(-1 == q.getGroupId());
		}
	}
	
	@Test
	public void queueAndPopMsgTest(){
		int testSize = 1000;
		int gidToUse = 1;
//		RsrcSched rs;
		GroupIdQ q = new GroupIdQ (gidToUse);
//		try {
//			rs = new RsrcSched(1, new GatewayMockUp());
//			MessageWrapper.setScheduler(rs);
//		} catch (IllegalStateException ise) {
//			LOG.severe("Clean up because: " + ise.getMessage());
//		}
		try{
			RsrcSched.discard();
			MessageWrapper.setScheduler(new RsrcSched(1, new GatewayMockUp()));
			assertNotNull(RsrcSched.getRS());
			MessageWrapper wrongMsg = new MessageWrapper(gidToUse + 1, 0);
			q.queueMsg(wrongMsg);
		} catch (IllegalArgumentException iae) {
			assertEquals(iae.getMessage(),"Wrong group queue for that group ID.");
		} catch (Exception e) {
			fail ("unexpected exception" + e.toString());
		}
		for (int i = 0; i < testSize; i++) {
			q.queueMsg(new MessageWrapper(gidToUse, i));
		}
		assertTrue(q.nofMsgs() == testSize);
		/*Check that they are served in sequence*/
		for (int i = 0; i < testSize; i++) {
			MessageWrapper msg = q.popMsg();
			assertNotNull(msg);
			assertTrue(i == msg.getBody());
		}
		assertTrue(q.nofMsgs() == 0);
		assertNull(q.popMsg());
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
		RsrcSched.discard();		
	}
}