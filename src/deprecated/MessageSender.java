package deprecated;

import givenAPI.Gateway;
import resourceSched.MessageWrapper;
import resourceSched.RsrcSched;
import test.GatewayMockUp;

public class MessageSender {
	private int [][] testSuite = { /* array of [resources, groupID, groupID,...] for each testcase*/
			{1, 2, 1, 2, 3}
			,{2, 2, 1, 2, 3}
			,{3, 2, 1, 2, 3}
	};
	private int [][] testResults = { /*Array of [groupID, groupID,...]*/
			{2, 2, 1, 3}
			,{2, 1, 2, 3}
			,{2, 1, 2, 3}
	};
	public void runTests(){
//		Gateway consumer = new GatewayMockUp();
		for (int [] test : testSuite){
//			RsrcSched rs = new RsrcSched(test[0], consumer); /*Configure number of resources*/
			for (int id = 1; id < test.length; id++) { /*group ID start in the second element*/
//				rs.sendAndQueueMsg(new MessageWrapper(test[id], 0));
			}
		}
		if (testResults != null) System.out.println();
	}
}
