package landrop.transfer;

import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import javax.swing.*;
import landrop.Util;

public class IncomingTransfer extends Transfer
{
	private String fileName;

    private static String lastPathUsed = new File("").getAbsolutePath();

	private FileOutputStream fileOut;

	/**
	 * Receives the transfer from the given socket.
	 */
	public IncomingTransfer( Socket socket ) throws IOException
	{
		super( "Receiving from " + socket.getInetAddress() );

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
			// Receive the file's details.
			startTransfer();

			// Ask the user if, and where to, store the file.
			if ( getConfirmation() )
			{
				dataOut.writeBoolean( true );
				dataOut.flush();

				transferFile();
				verifyFile();
			}
			else
			{
				dataOut.writeBoolean( false );
				setStage( Stage.REJECTED );
			}

			socket.close();
		}
		catch ( IOException e )
		{
			e.printStackTrace();

			transferFailed( "IOException during transfer" );
		}
	}

	/**
	 * Reads in the file's attributes from the sender, so we can start the transfer.
	 */
	private void startTransfer() throws IOException
	{
		fileName = dataIn.readUTF();
		fileSize = dataIn.readLong();

		setName( "Receiving " + fileName );
		System.out.println( socket.getInetAddress() + " would like to send us " + fileName + " (" + Util.formatFileSize( fileSize ) + ")" );
	}

	/**
	 * Asks the user if they want to accept the transfer, and if so, where.
	 * Returns whether the transfer should proceed.
	 */
	private boolean getConfirmation() throws FileNotFoundException
	{
		// Asks the user if they want to accept the transfer.
        JLabel header = new JLabel("Would you like to receive \"" + fileName + "\" (" + Util.formatFileSize( fileSize ) + ") from " + socket.getInetAddress().toString().substring( 1 ) + "?");
        header.setFont(header.getFont().deriveFont(Font.BOLD));
        JCheckBox checkbox = new JCheckBox("Select a storage location other than \"" + new File(lastPathUsed).getName() + "\".");
        Object[] params = {header, checkbox};
        if ( JOptionPane.showConfirmDialog(null, params, "Incoming transfer", JOptionPane.YES_NO_OPTION)  != JOptionPane.YES_OPTION )
            return false;

        // Decide where to put the file.
        String storagePath = Util.combinePaths(lastPathUsed, fileName);
        if( checkbox.isSelected() )
            storagePath = askWhereToSaveFile( fileName );

        if ( storagePath != null )
        {
            fileOut = new FileOutputStream( storagePath );
            return true;
        }
        else
            return false;
	}

    private String askWhereToSaveFile(String defaultName)
    {
        // Make a JFileChooser to ask for the save location.
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile( new File( defaultName ) );

        // Show the dialog.
        if ( fileChooser.showSaveDialog( null ) == JFileChooser.APPROVE_OPTION )
        {
            lastPathUsed = Util.getParentDirectory(fileChooser.getSelectedFile().getAbsolutePath());
            System.out.println(lastPathUsed);
            return fileChooser.getSelectedFile().getAbsolutePath();
        }
        else
            return null;
    }

	/**
	 * Receives the file's data, chunk-by-chunk.
	 */
	private void transferFile() throws IOException
	{
		setStage( Stage.TRANSFERRING );

		// Start the clock (for transfer speed calculation).
		startTime = System.currentTimeMillis();
		form.setVisible( true );

		// Iterate through the file in chunk-sized increments.
		while ( bytesTransferred < fileSize )
		{
			// Create the chunk.
			byte[] chunk = new byte[CHUNK_SIZE * 5];

			// Read in the chunk, add it to our MD5 digest, and send it to the file.
			int bytesRead = dataIn.read( chunk );
			if ( bytesRead > 0 )
			{
				verificationDigest.update( chunk, 0, bytesRead );
				fileOut.write( chunk, 0, bytesRead );

				// Update the form.
				bytesTransferred += bytesRead;
				form.updateComponents();
			}
			else
				break;
		}

		// ...and we're done.
		stopTime = System.currentTimeMillis();
		fileOut.close();
		dataOut.writeBoolean( true );
	}

	/**
	 * Verifies the data we've just received.
	 */
	private void verifyFile() throws IOException
	{
		setStage( Stage.VERIFYING );

		// Read in the sender's MD5 checksum; calculate ours.
		String theirMD5 = dataIn.readUTF();
		String ourMD5 = Util.digestToHexString( verificationDigest );
		System.out.println( "Comparing file hashes...\nSender's: " + theirMD5 + "\nOurs: " + ourMD5 );

		// Did they match?
		if ( ourMD5.equals( theirMD5 ) )
		{
			// Success! Transfer finished.
			dataOut.writeBoolean( true );
			setStage( Stage.FINISHED );
		}
		else
		{
			// Failure!
			dataOut.writeBoolean( false );
			transferFailed( "File verification failed." );
		}
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
				return "Waiting for approval...";
			case TRANSFERRING:
				return "Receiving... (" + getProgress() + "%)";
			case VERIFYING:
				return "Verifying data...";
			case FAILED:
				return "Transfer failed!";
			case FINISHED:
				return "File received successfully!";
		}

		return "Unknown";
	}
}
