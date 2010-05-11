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
	/**
	 * The file we're transferring.
	 */
	private File file;

	/**
	 * Connects to the given peer to transfer <code>file</code>.
	 */
	public OutgoingTransfer( File file, String serverAddress, int serverPort )
	{
		super( "Sending " + file.getName() );
		this.file = file;
		fileSize = file.length();

		try
		{
			// Hook up the socket.
			socket = new Socket( serverAddress, serverPort );
			dataIn = new DataInputStream( socket.getInputStream() );
			dataOut = new DataOutputStream( socket.getOutputStream() );
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
			// Send the receiver info about the file.
			startTransfer();

			// See if they accept the transfer.
			if ( dataIn.readBoolean() )
			{
				// They did!
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

	/**
	 * Starts the transfer by sending the receiver the file's information.
	 */
	private void startTransfer() throws IOException
	{
		setStage( Stage.WAITING );

		// Update the form.
		form.setVisible( true );

		// Send the user the file's attributes.
		dataOut.writeUTF( file.getName() );
		dataOut.writeLong( fileSize );
		dataOut.flush();
	}

	/**
	 * Transfers the file, chunk-by-chunk.
	 */
	private void transferFile() throws IOException
	{
		setStage( Stage.TRANSFERRING );

		// Start the clock (for transfer speed calculation).
		startTime = System.currentTimeMillis();

		// Open the file for reading...
		FileInputStream fileIn = new FileInputStream( file );

		// Iterate through the file in chunk-sized increments.
		for ( long i = 0; i < file.length(); i += Transfer.CHUNK_SIZE )
		{
			// Calculate the number of bytes we're about to send. (CHUNK_SIZE or less, if we're at the end of the file)
			int numBytes = (int) Math.min( Transfer.CHUNK_SIZE, fileSize - i );

			// Create the chunk.
			byte[] chunk = new byte[numBytes];

			// Read in the chunk, add it to our MD5 digest, and send it to the receiver.
			fileIn.read( chunk, 0, numBytes );
			verificationDigest.update( chunk, 0, numBytes );
			dataOut.write( chunk, 0, numBytes );
			dataOut.flush();

			// Update the form.
			bytesTransferred += numBytes;
			form.updateComponents();
		}

		// ...and we're done.
		fileIn.close();
	}

	/**
	 * Handles the verification of the file with the receiver.
	 */
	private void verifyFile() throws IOException
	{
		setStage( Stage.VERIFYING );

		// Send the receiver the correct MD5 of our file (the digest is updated in transferFile).
		dataOut.writeUTF( Util.digestToHexString( verificationDigest ) );
		dataOut.flush();

		// Wait for the receiver to verify their file. Read in whether it succeeded.
		if ( dataIn.readBoolean() )
			setStage( Stage.FINISHED );
		else
			transferFailed( "Receiver's file verification failed." );
	}

	/**
	 * Returns the status of this transfer.
	 */
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
				return "File sent successfully!";
			default:
				return "Unknown";
		}
	}

	@Override
	public String getDetails()
	{
		if ( getStage() == Stage.WAITING )
			return "(They must accept the transfer.)";
		else
			return super.getDetails();
	}
}
