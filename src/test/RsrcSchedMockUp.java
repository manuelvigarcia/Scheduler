package test;

import static org.junit.Assert.*;
import resourceSched.Scheduler;

public class RsrcSchedMockUp implements Runnable, Scheduler {

	@Override
	public void moreMessages(int groupID) {
		fail("not implemented");

	}

	@Override
	public void run() {
		fail("not implemented");
	}

	@Override
	public void freeResource() {
		fail("not implemented");
		
	}

}
