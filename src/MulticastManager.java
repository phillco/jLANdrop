import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MulticastManager
{
	private static Queue<Peer> peers = new ConcurrentLinkedQueue<Peer>();
	private static List<PeerEventListener> listeners = new LinkedList<PeerEventListener>();

	final static String multicastAddress = "224.255.255.255";
	final static int multicastPort = 10666;
	final static int BUFFER_LENGTH = 4096;

	public static void addPeerListener( PeerEventListener listener )
	{
		listeners.add( listener );
	}

	private static void broadcast() throws IOException
	{
		DatagramSocket socket = new DatagramSocket();

		byte[] b = ( System.getProperty( "user.name" ) + " on " + java.net.InetAddress.getLocalHost().getHostName() ).getBytes( "UTF-8" );

		try
		{
			DatagramPacket dgram = new DatagramPacket( b, b.length, InetAddress.getByName( multicastAddress ), multicastPort );
			socket.send( dgram );
		}
		catch ( UnknownHostException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void startBroadcastLoop()
	{
		Runnable start = new Runnable()
		{
			@Override
			public void run()
			{
				while ( true )
				{
					try
					{
						broadcast();

						// Clean the linked list while we're here.
						checkForTimeouts();
						Util.safeSleep( 1000 );
					}
					catch ( IOException e )
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};

		new Thread( start ).start();
	}

	private static void checkForTimeouts()
	{
		boolean listChanged = false;
		Iterator<Peer> it = peers.iterator();
		while ( it.hasNext() )
		{
			if ( ( System.currentTimeMillis() - it.next().lastSeen.getTime() ) > 1000 * 5 )
			{
				System.out.println( "Removing timed out peer" );
				listChanged = true;
				it.remove();
			}
		}

		if ( listChanged )
			fireUpdateEvents();
	}

	private static void fireUpdateEvents()
	{
		for ( PeerEventListener l : listeners )
			l.peerListUpdated( peers );
	}

	private static void addPeer( Peer newPeer )
	{
		// Don't include the local computer.
		try
		{
			if ( newPeer.getAddress().equals( InetAddress.getLocalHost() ) )
				return;
		}
		catch ( UnknownHostException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for ( Peer p : peers )
		{
			if ( p.equals( newPeer ) )
			{
				p.see();
				return;
			}
		}
		peers.add( newPeer );
		fireUpdateEvents();
	}

	public static void startListenLoop()
	{
		Runnable start = new Runnable()
		{
			@Override
			public void run()
			{
				while ( true )
				{
					try
					{
						byte[] b = new byte[BUFFER_LENGTH];
						MulticastSocket socket = new MulticastSocket( multicastPort );
						socket.joinGroup( InetAddress.getByName( multicastAddress ) );

						while ( true )
						{
							DatagramPacket dgram = new DatagramPacket( b, b.length );
							socket.receive( dgram ); // blocks until a datagram is received
							Peer peer = new Peer( dgram.getAddress(), new String( b, 0, dgram.getLength(), "UTF-8" ) );
							addPeer( peer );
						}
					}
					catch ( IOException e )
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};

		new Thread( start ).start();
	}

	public static Queue<Peer> getPeers()
	{
		return peers;
	}
}
