package test;

import givenAPI.Message;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import resourceSched.MessageWrapper;
import resourceSched.RsrcSched;


public class SchedulerParameterTest{
	private static final Logger LOG = Logger.getGlobal();
	private static final Level globalLogLevel = RsrcSched.globalLogLevel;
	private int resources;
	private int [] dataSet;
	private int [] expected;
	private int dataIdx = 0;
	private int expectedIdx;
	private int nofMsgs = 0;
	private int fail = -1;
	private Semaphore token;

	/* *
	 * Each case is in the form:{resources, data2send, expectedMsgOrder}, for example:
	 * {resources}, {groupID, groupID,... }, {1stScheduled, 2ndScheduled, 3rdScheduled...},
	 */
	public static int [][][] testCaseData = {
    		  {{ 1},{2,1,2,3}, {1,3,2,4}} /*Test case from spec, single resource*/
    		  ,{{3},{2,1,2,3}, {1,2,3,4}} /*Test case from spec, three resources*/
    		  ,{{3},{1,2,3,4,1,1,1,1,2,2,2,3,3,3}, {1,2,3,5,6,7,8,9,10,11,12,13,14,4}} /*suspected case from paper exercise*/
    		  ,{{1},{1,1,1,1,1,1,1,1,1,1,1,1},   {1,2,3,4,5,6,7,8,9,10,11,12}} /*Single groupID, single resource*/
    		  ,{{3},{1,1,1,1,1,1,1,1,1,1,1,1},   {1,2,3,4,5,6,7,8,9,10,11,12}} /*Single GroupID, Multiple resources */
    		  ,{{3},{1,2,3,4,5,1,1,1,1,1,1}, {1,2,3,6,7,8,9,10,11,4,5}} /*All messages from first GroupID are processed first*/
    		  ,{{3},{1,2,3,4,5,6,7,8,1}, {1,2,3,9,4,5,6,7,8}} /*All messages from first GroupID are processed first*/ 
    		  ,{{3},{1,9,9,8,1,1,1,1,1}, {1,2,3,5,6,7,8,9,4}} /*GroupID does not have anything to do with schedule*/
    		  ,{{5},{9,8,7,6,5,4,3,2,1}, {1,2,3,4,5,6,7,8,9}} /*GroupID does not have anything to do with schedule*/
    		  ,{{20},{Integer.MAX_VALUE,Integer.MAX_VALUE-1,Integer.MAX_VALUE-2,Integer.MAX_VALUE-3,Integer.MAX_VALUE,
    		  /*case continues*/Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE}, {1,2,3,4,5,6,7,8,9}}
   	};
	/**
	 * get the data for the next testcase, i.e. the next row from testCaseData
	 * dataIdx is progressing through rows
	 * First column is resources
	 * The array in second column is the set of groupIDs to be sent
	 * The array in the third (and last) column is the order in which that sequence
	 * of groupID must be sent for the testcase to suceed.
	 * */
	private boolean getNextCase(){
		if (testCaseData.length > dataIdx) {/*There still cases to run*/
			resources = testCaseData[dataIdx][0][0]; //integer inside the first array in this row
			dataSet = testCaseData[dataIdx][1];
			expected = testCaseData[dataIdx][2];
			dataIdx ++;
			return true;
		} else {
			return false;
		}
	}
	
	
	@Before
	public void initialize() {
		/*Initialization*/
		LOG.setLevel(globalLogLevel);
	}


	@Test
	public void testEachCase() {
		RsrcSched rs;
		GatewayMockUpParameterized gw;
		gw = new GatewayMockUpParameterized();
		gw.setCounter(this);
		while (getNextCase()){
			resetNofMsgs();
			rs = new RsrcSched(resources, gw);
			assertNotNull(rs);
			token = new Semaphore(dataSet.length);
			MessageWrapper.setScheduler(rs);
			expectedIdx = 0; /*results didn't start arriving yet*/
			LOG.warning("Start Sending");
			for (int i = 0; i < dataSet.length; i++){
				rs.sendAndQueueMsg(new MessageWrapper(dataSet[i], getMsgIdx()));
				try{
					token.acquire(); /*one message sent*/
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}

			}
			LOG.warning("Sent, now wait");
			/*Wait while the gateway processes all data*/
			try {
				LOG.warning("Going to wait " + token.availablePermits());
				token.acquire(dataSet.length);/*wait for all msgs to be processed.*/
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
			LOG.warning("Woke up.");
			if (-1 != fail) {
				fail("Arrived out of order: message " + fail);
			}
			RsrcSched.discard();
			LOG.severe("Next set: " + dataIdx);
			LOG.setLevel(globalLogLevel);
		}
		LOG.severe("No more sets.");
	}

	@Ignore
	@Test
	public void testAlwaysPass(){
		assertTrue(true);
	}
	private int getMsgIdx(){
		nofMsgs ++;
		return (nofMsgs);
	}

	public int getNofMsgs(){
		return (nofMsgs);
	}
	public void resetNofMsgs(){
		nofMsgs = 0;
	}
	
	public void failCurrentTstAt(int idx){
		fail = idx;
	}

	public void thisArrived(Message msg) {
		int msgIdx = ((MessageWrapper)msg).getBody();
		LOG.severe("Received completed() on message " + (msgIdx));
		if (expected[expectedIdx]!= msgIdx){
			failCurrentTstAt(msgIdx);
			LOG.severe("Out of sync: " + msgIdx + "º (expected " + expected[expectedIdx]+ ", arrived " + ((MessageWrapper)msg).toString() + ")");
		}
		expectedIdx++;
		token.release(); /*One message processed*/
		LOG.fine("Should be waking " + token.availablePermits());
		if (dataSet.length <= msgIdx) {//we're done
			LOG.warning("Time to wake it up");
		}
	}
	
}