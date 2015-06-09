import deprecated.MessageSender;

/**
 * This is a simple "runner" file to run the RsrcSched basic services.
 */

/**
 * @author ManuelVicente
 *
 */
public class ResourceScheduler {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MessageSender ms;
		ms = new MessageSender();
		ms.runTests();
	}

}
