import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;

import javax.swing.JOptionPane;

public class Util
{
	public final static long SECOND = 1000;
	public final static long SECONDS_IN_MINUTE = 60;

	public final static long MINUTE = SECOND * SECONDS_IN_MINUTE;
	public final static long MINUTES_IN_HOUR = 60;

	public final static long HOUR = MINUTE * MINUTES_IN_HOUR;
	public final static long HOURS_IN_DAY = 24;

	public final static long DAY = HOUR * HOURS_IN_DAY;

	/**
	 * Converts time (in milliseconds) to human-readable format
	 * "<w> days, <x> hours, <y> minutes and (z) seconds"
	 * 
	 * from http://www.rgagnon.com/javadetails/java-0585.html
	 */
	public static String millisToLongDHMS( long duration )
	{
		StringBuffer res = new StringBuffer();
		long temp = 0;
		if ( duration >= SECOND )
		{
			temp = duration / DAY;
			if ( temp > 0 )
			{
				duration -= temp * DAY;
				res.append( temp ).append( " day" ).append( temp > 1 ? "s" : "" ).append( duration >= MINUTE ? ", " : "" );
			}

			temp = duration / HOUR;
			if ( temp > 0 )
			{
				duration -= temp * HOUR;
				res.append( temp ).append( " hour" ).append( temp > 1 ? "s" : "" ).append( duration >= MINUTE ? ", " : "" );
			}

			temp = duration / MINUTE;
			if ( temp > 0 )
			{
				duration -= temp * MINUTE;
				res.append( temp ).append( " minute" ).append( temp > 1 ? "s" : "" );
			}

			if ( !res.toString().equals( "" ) && duration >= SECOND )
			{
				res.append( " and " );
			}

			temp = duration / SECOND;
			if ( temp > 0 )
			{
				res.append( temp ).append( " second" ).append( temp > 1 ? "s" : "" );
			}
			return res.toString();
		}
		else
		{
			return "0 seconds";
		}
	}

	public static String md5ToString( byte[] md5 )
	{
		String res = "";

		String tmp = "";
		for ( byte element : md5 )
		{
			tmp = ( Integer.toHexString( 0xFF & element ) );
			if ( tmp.length() == 1 )
				res += "0" + tmp;
			else
				res += tmp;
		}
		return res;
	}

	public static String md5( byte[] data )
	{
		try
		{
			MessageDigest algorithm = MessageDigest.getInstance( "MD5" );
			algorithm.reset();
			algorithm.update( data );
			byte[] md5 = algorithm.digest();

			return md5ToString( md5 );
		}
		catch ( NoSuchAlgorithmException ex )
		{
			JOptionPane.showMessageDialog( null, "Couldn't initialize MD5", "Error", JOptionPane.ERROR_MESSAGE );
			return "";
		}
	}

	public static String formatFileSize( double d )
	{
		String[] types = { "bytes", "KiB", "MiB", "GiB", "TiB", "PiB", "XiB", "ZiB", "YiB", "WTFB" };
		int index = 0;
		while ( d > Math.pow( 1024, index + 1 ) )
			index++;
		return new DecimalFormat( "0.0" ).format( d / Math.pow( 1024, index ) ) + " " + types[index];
	}

}
