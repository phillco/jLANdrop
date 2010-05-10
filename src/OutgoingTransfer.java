import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JOptionPane;

public class OutgoingTransfer extends Transfer
{
	Socket localSocket;
	DataInputStream inputStream;
	DataOutputStream outputStream;
	File file;

	public OutgoingTransfer( File file, String serverAddress, int serverPort )
	{
		this.file = file;
		this.fileSize = (int) file.length();

		// Connect to the server!
		try
		{
			// Hook up the socket.
			localSocket = new Socket( serverAddress, serverPort );
			inputStream = new DataInputStream( localSocket.getInputStream() );
			outputStream = new DataOutputStream( localSocket.getOutputStream() );
		}
		catch ( final UnknownHostException e )
		{
			JOptionPane.showMessageDialog( null, "Couldn't look up " + serverAddress + ".", "Connection error", JOptionPane.ERROR_MESSAGE );
			System.exit( 0 );
			return;
		}
		catch ( final IOException e )
		{
			JOptionPane.showMessageDialog( null, "Couldn't connect to " + serverAddress + ".", "Connection error", JOptionPane.ERROR_MESSAGE );
			System.exit( 0 );
			return;
		}

		start();
	}

	@Override
	public String toString()
	{
		switch ( getStage() )
		{
			case WAITING:
				return "Waiting for receiver...";
			case TRANSFERRING:
				return "Sending... (" + getProgress() + "%)";
			case VERIFYING:
				return "Receiver is verifying...";
			case FAILED:
				return "Transfer failed!";
			case FINISHED:
				return "Transfer completed successfully.";
		}

		return "Unknown";
	}

	@Override
	public void run()
	{
		// Initial handshaking.
		try
		{
			setName( "Sending " + file.getName() );
			form.setVisible( true );
			setStage( Stage.WAITING );
			int len = (int) file.length();
			outputStream.writeUTF( file.getName() );
			outputStream.writeInt( len );
			outputStream.flush();

			if ( inputStream.readBoolean() )
			{
				setStage( Stage.TRANSFERRING );
				byte[] data = new byte[len];
				FileInputStream input = new FileInputStream( file );

				for ( int i = 0; i < file.length(); i += Protocol.CHUNK_SIZE )
				{
					input.read( data, i, Math.min( Protocol.CHUNK_SIZE, len - i ) );
					outputStream.write( data, i, Math.min( Protocol.CHUNK_SIZE, len - i ) );
					bytesTransferred = i;
					outputStream.flush();
				/*	try
					{
						Thread.sleep( 0 );
					}
					catch ( InterruptedException e )
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}*/
					form.updateComponents();
				}

				outputStream.writeUTF( Main.md5( data ) );
				outputStream.flush();

				setStage( Stage.VERIFYING );

				if ( inputStream.readBoolean() )
					setStage( Stage.FINISHED );
				else
					setStage( Stage.FAILED );
			}
			else
				setStage( Stage.REJECTED );

		}
		catch ( IOException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
