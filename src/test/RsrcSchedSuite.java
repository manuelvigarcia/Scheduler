package test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	SchedulerTest.class,
	MessageQTest.class,
	GroupIdQTest.class,
	SchedulerParameterTest.class
})

public class RsrcSchedSuite {

}
