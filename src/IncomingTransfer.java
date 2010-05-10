import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class IncomingTransfer extends Transfer
{
	private Socket socket;

	private String fileName;

	private DataInputStream dataIn;

	private DataOutputStream dataOut;

	private FileOutputStream fileOut;

	public IncomingTransfer( Socket socket ) throws IOException
	{
		this.socket = socket;

		dataIn = new DataInputStream( socket.getInputStream() );
		dataOut = new DataOutputStream( socket.getOutputStream() );

		start();
	}

	@Override
	public void run()
	{
		try
		{
			fileName = dataIn.readUTF();
			fileSize = dataIn.readInt();

			setName( "Receiving " + fileName );
			System.out.println( "New incoming transfer from " + socket.getInetAddress() + "." );

			if ( getConfirmation() )
			{
				dataOut.writeBoolean( true );
				dataOut.flush();
			}
			else
			{
				dataOut.writeBoolean( false );
				setStage( Stage.REJECTED );
				socket.close();
				return;
			}

			transferFile();
			verifyFile();

		}
		catch ( IOException e )
		{
			transferFailed( "IOException during transfer" );
		}
	}

	@Override
	public String toString()
	{
		switch ( getStage() )
		{
			case WAITING:
				return "Waiting for approval...";
			case TRANSFERRING:
				return "Receiving... (" + getProgress() + "%)";
			case VERIFYING:
				return "Verifying data...";
			case FAILED:
				return "Transfer failed!";
			case FINISHED:
				return "Transfer completed successfully.";
		}

		return "Unknown";
	}

	private boolean getConfirmation() throws FileNotFoundException
	{
		if ( JOptionPane.showConfirmDialog( null, "Would you like to receive \"" + fileName + "\" from " + socket.getInetAddress().toString().substring( 1 ) + "?\nSize: " + Util.formatFileSize( fileSize ) + ".", "Incoming transfer", JOptionPane.YES_NO_OPTION ) != JOptionPane.YES_OPTION )
			return false;

		// Get the save location.
		final JFileChooser fc = new JFileChooser();
		String newDefaultFilename = fileName.substring( 0, fileName.length() - 4 ) + "-new" + fileName.substring( fileName.length() - 4 );
		fc.setSelectedFile( new File( newDefaultFilename ) );

		if ( fc.showSaveDialog( null ) != JFileChooser.APPROVE_OPTION )
			return false;

		fileOut = new FileOutputStream( fc.getSelectedFile() );
		return true;
	}

	private void transferFile() throws IOException
	{
		setStage( Stage.TRANSFERRING );
		startTime = System.currentTimeMillis();
		form.setVisible( true );

		byte[] chunk;

		System.out.println( "Receiving..." );
		for ( int i = 0; i < fileSize; i += Protocol.CHUNK_SIZE )
		{
			chunk = new byte[Protocol.CHUNK_SIZE];
			int numBytes = Math.min( Protocol.CHUNK_SIZE, fileSize - i );
			dataIn.read( chunk, 0, numBytes );
			if ( numBytes == -1 )
				throw new IOException( "-1 bytes read" );
			fileOut.write( chunk, 0, numBytes );
			digest.update( chunk, 0, numBytes );
			bytesTransferred += numBytes;
			if ( form != null )
				form.updateComponents();
		}
		fileOut.close();
	}

	private void verifyFile() throws IOException
	{
		setStage( Stage.VERIFYING );
		String theirMD5 = dataIn.readUTF();
		String ourMD5 = Util.digestToHexString( digest );
		System.out.println( "Comparing file hashes...\nTheirs: " + theirMD5 + "\nOurs: " + ourMD5 );
		if ( ourMD5.equals( theirMD5 ) )
		{
			// JOptionPane.showMessageDialog( null, "Done! " + fileName + " has been received and verified.\n\nChecksum: " + ourMD5 );
			setStage( Stage.FINISHED );
			dataOut.writeBoolean( true );
		}
		else
		{
			dataOut.writeBoolean( false );
			JOptionPane.showMessageDialog( null, "An error occured during the file transfer (checksum fail).\n\nReceived: " + Util.formatFileSize( fileSize ) + "\nLocal checksum: " + ourMD5 + "\nCorrect checksum: " + theirMD5, "Transfer error", JOptionPane.ERROR_MESSAGE );
			setStage( Stage.FAILED );

		}

	}

}
