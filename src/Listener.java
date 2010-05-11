import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import javax.swing.JOptionPane;

/**
 * Each peer runs a Listener, which listens for incoming transfer requests.
 * @author Phillip Cohen
 */
public class Listener
{

	/**
	 * The default port we listen for connections on.
	 */
	public final static int DEFAULT_PORT = 50900;

	private int port;

	/**
	 * The socket with which we listen for connections.
	 */
	private ServerSocket listeningSocket;

	/**
	 * The thread that listens for new connections.
	 */
	private final AcceptThread acceptThread = new AcceptThread();

	private boolean started = false;

	public Listener()
	{
	}

	public void connect()
	{
		// Start listening for connections.
		try
		{
			port = DEFAULT_PORT;
			listeningSocket = new ServerSocket( port );
		}
		catch ( final IOException e )
		{
			JOptionPane.showMessageDialog( null, "Could not listen on port: " + port + ".\nAnother server may already be running.", "Server error", JOptionPane.ERROR_MESSAGE );
			System.exit( 0 );
			return;
		}

		// We're connected!
		started = true;
		System.out.println( "Server started at " + getServerIP() + "!" );
		acceptThread.start();
		Main.getMainFrame().updateLabels();
	}

	@SuppressWarnings( "deprecation" )
	public void disconnect()
	{
		acceptThread.stop();
	}

	/**
	 * Returns this server's IP address and port.
	 * @return "xxx.xxx.xxx.xxx:port"
	 */
	public String getServerIP()
	{
		try
		{
			final InetAddress localHost = InetAddress.getLocalHost();
			final InetAddress[] all_IPs = InetAddress.getAllByName( localHost.getHostName() );
			return ( all_IPs[0].toString().split( "/" ) )[1] + ":" + port;
		}
		catch ( final UnknownHostException e )
		{
			return "Unknown IP:" + port;
		}
	}

	public boolean isStarted()
	{
		return started;
	}

	/**
	 * Listens for new clients, infinitely.
	 */
	protected class AcceptThread extends Thread
	{
		@Override
		public void run()
		{
			while ( started )
				try
				{
					// Wait for a new client.
					IncomingTransfer transfer = new IncomingTransfer( listeningSocket.accept() );
					Main.incomingTransfers.add( transfer ); // Stop here until the client connects.
				}
				catch ( final IOException e )
				{
				}
		}
	}
}
