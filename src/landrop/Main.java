package landrop;

import landrop.peering.MulticastManager;
import landrop.transfer.Listener;
import landrop.ui.DropForm;

public class Main
{
    public static final String VERSION = "v1.5";

	private static DropForm mainFrame;

	private static Listener listener = new Listener();

	public static void main( String[] args )
	{
		// Use the native look and feel.
		Util.useNativeLookAndFeel();

		// Multicast peer announcing.
		MulticastManager.startBroadcastLoop();
		MulticastManager.startListenLoop();

		// Create the main form; start listening for connections.
		mainFrame = new DropForm();
		listener.connect();
	}

	public static DropForm getMainFrame()
	{
		return mainFrame;
	}

	public static Listener getListener()
	{
		return listener;
	}
}
