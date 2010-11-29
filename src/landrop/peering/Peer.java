package landrop.peering;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import javax.swing.JOptionPane;

public class Peer
{
	InetAddress address;
	int port = 50900; // TODO: allow peers to specify their port number
	String name = "Peer";
	Date lastSeen;

	public Peer( InetAddress address, String name )
	{
		super();
		this.address = address;
		this.name = name;
		lastSeen = new Date();
	}

	public Peer( String input ) throws UnknownHostException
	{
		try
		{
			if ( input.split( ":" ).length == 2 )
			{
				port = Integer.parseInt( input.split( ":" )[1] );
				this.address = InetAddress.getByName( input.split( ":" )[0] );
			}
			else
				this.address = InetAddress.getByName( input );
		}
		catch ( NumberFormatException ex )
		{
			JOptionPane.showMessageDialog( null, "Error parsing that port.", "Input error", JOptionPane.ERROR_MESSAGE );
			throw ex;
		}

	}

	@Override
	public String toString()
	{
		return name + " at " + address + ":" + port;
	}

	public void see()
	{
		lastSeen = new Date();
	}

	// :(

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ( ( address == null ) ? 0 : address.hashCode() );
		result = prime * result + port;
		return result;
	}

	@Override
	public boolean equals( Object obj )
	{
		if ( this == obj )
			return true;
		if ( obj == null )
			return false;
		if ( getClass() != obj.getClass() )
			return false;
		Peer other = (Peer) obj;
		if ( address == null )
		{
			if ( other.address != null )
				return false;
		}
		else if ( !address.equals( other.address ) )
			return false;
		if ( port != other.port )
			return false;
		return true;
	}

	public InetAddress getAddress()
	{
		return address;
	}

	public int getPort()
	{
		return port;
	}

}
