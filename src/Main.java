import java.util.LinkedList;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Main
{
	private static MainFrame mainFrame;

	private static Listener listener = new Listener();

	public static LinkedList<IncomingTransfer> incomingTransfers = new LinkedList<IncomingTransfer>();
	public static LinkedList<OutgoingTransfer> outgoingTransfers = new LinkedList<OutgoingTransfer>();

	public static void main( String[] args )
	{
		try
		{
			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

		}
		catch ( InstantiationException e )
		{
		}
		catch ( ClassNotFoundException e )
		{
		}
		catch ( UnsupportedLookAndFeelException e )
		{
		}
		catch ( IllegalAccessException e )
		{
		}

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
