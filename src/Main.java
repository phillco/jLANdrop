import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.LinkedList;

import javax.swing.JOptionPane;
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

	public static String formatFileSize( long numBytes )
	{
		String[] types = { "bytes", "KiB", "MiB", "GiB", "TiB", "PiB", "XiB", "ZiB", "YiB", "WTFB" };
		int index = 0;
		while ( numBytes > Math.pow( 1024, index + 1 ) )
			index++;
		return new DecimalFormat( "0.0" ).format( numBytes / Math.pow( 1024, index ) ) + " " + types[index];
	}

	public static String md5( byte[] data )
	{
		String res = "";
		try
		{
			MessageDigest algorithm = MessageDigest.getInstance( "MD5" );
			algorithm.reset();
			algorithm.update( data );
			byte[] md5 = algorithm.digest();
			String tmp = "";
			for ( int i = 0; i < md5.length; i++ )
			{
				tmp = ( Integer.toHexString( 0xFF & md5[i] ) );
				if ( tmp.length() == 1 )
					res += "0" + tmp;
				else
					res += tmp;
			}
		}
		catch ( NoSuchAlgorithmException ex )
		{
			JOptionPane.showMessageDialog( null, "Couldn't initialize MD5", "Error", JOptionPane.ERROR_MESSAGE );
		}
		return res;
	}
}
