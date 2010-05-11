
public class Main
{
	private static MainFrame mainFrame;

	private static Listener listener = new Listener();

	public static void main( String[] args )
	{
		// Use the native look and feel.
		Util.useNativeLookAndFeel();

		// Create the main form; start listening for connections.
		mainFrame = new MainFrame();
		listener.connect();
	}

	public static MainFrame getMainFrame()
	{
		return mainFrame;
	}

	public static Listener getListener()
	{
		return listener;
	}
}
