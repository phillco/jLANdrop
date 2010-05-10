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
		fileSize = (int) file.length();

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
	public void run()
	{
		try
		{
			setName( "Sending " + file.getName() );
			form.setVisible( true );
			startTransfer();

			if ( inputStream.readBoolean() )
			{
				transferFile();
				verifyFile();
			}
			else
				setStage( Stage.REJECTED );

		}
		catch ( IOException e )
		{
			transferFailed( "IOException during transfer" );
		}
	}

	private void startTransfer() throws IOException
	{
		setStage( Stage.WAITING );
		outputStream.writeUTF( file.getName() );
		outputStream.writeInt( fileSize );
		outputStream.flush();
	}

	private void transferFile() throws IOException
	{
		setStage( Stage.TRANSFERRING );
		startTime = System.currentTimeMillis();

		byte[] chunk;
		FileInputStream fileIn = new FileInputStream( file );
		for ( int i = 0; i < file.length(); i += Protocol.CHUNK_SIZE )
		{
			chunk = new byte[Protocol.CHUNK_SIZE];
			int numBytes = Math.min( Protocol.CHUNK_SIZE, fileSize - i );

			fileIn.read( chunk, 0, numBytes );
			digest.update( chunk, 0, numBytes );
			outputStream.write( chunk, 0, numBytes );
			bytesTransferred += numBytes;
			outputStream.flush();
			form.updateComponents();
		}
		fileIn.close();

		outputStream.writeUTF( Util.digestToHexString( digest ) );
		outputStream.flush();
	}

	private void verifyFile() throws IOException
	{
		setStage( Stage.VERIFYING );

		if ( inputStream.readBoolean() )
			setStage( Stage.FINISHED );
		else
			transferFailed( "File verification failed." );
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
}
