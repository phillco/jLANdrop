import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Parent class of Incoming and OutgoingTransfer; used to share code among the two.
 */
public abstract class Transfer extends Thread
{
	public enum Stage
	{
		WAITING, REJECTED, TRANSFERRING, VERIFYING, FINISHED, FAILED
	}

	/**
	 * Which stage this transfer is in.
	 */
	private Stage stage = Stage.WAITING;

	/**
	 * The form associated with this transfer.
	 */
	protected TransferForm form;

	/**
	 * Local socket that's connected to our partner.
	 */
	protected Socket socket;

	/**
	 * Stream of data coming from our partner.
	 */
	protected DataInputStream dataIn;

	/**
	 * Stream of data going to our partner.
	 */
	protected DataOutputStream dataOut;

	/**
	 * The digest we're using to hash the file and provide verification.
	 */
	protected MessageDigest verificationDigest = null;

	/**
	 * Size of the file we're transferring.
	 */
	protected int fileSize;

	/**
	 * How many bytes we've sent or received (for the progress bar).
	 */
	protected int bytesTransferred = 0;

	/**
	 * When we started the transfer - see System.currentTimeMillis();
	 */
	protected long startTime;

	/**
	 * Error, if any, that occurred (displayed on the form if stage is FAILED).
	 */
	protected String error = "";

	/**
	 * Creates this transfer.
	 * @param name The thread's name.
	 */
	public Transfer( String name )
	{
		super( name );

		// Set up the MD5 hasher.
		try
		{
			verificationDigest = MessageDigest.getInstance( "MD5" );
			verificationDigest.reset();
		}
		catch ( NoSuchAlgorithmException e )
		{
			e.printStackTrace();
		}

		// Make the form.
		form = new TransferForm( this );
	}

	/**
	 * Returns the details string for the form (e.g. "25MB of 200MB done").
	 */
	public String getDetails()
	{
		if ( stage == Stage.TRANSFERRING )
			return Util.formatFileSize( bytesTransferred ) + " of " + Util.formatFileSize( fileSize ) + " at " + Util.formatFileSize( getTransferSpeed() * 1000 ) + "/s";
		else if ( stage == Stage.FINISHED )
			return "Verified with MD5";
		else if ( ( stage == Stage.FAILED ) && ( error.length() > 0 ) )
			return error;
		else
			return " ";
	}

	/**
	 * Returns the excepted time left of the transfer ("about 45 minutes and 20 seconds left").
	 */
	public String getTimeLeft()
	{
		if ( stage == Stage.TRANSFERRING )
		{
			if ( getTransferSpeed() == 0 )
				return "";
			else
			{
				int milliSecondsLeft = (int) ( ( fileSize - bytesTransferred ) / getTransferSpeed() );
				return "about " + Util.millisToLongDHMS( milliSecondsLeft ) + " left";
			}
		}
		else
			return " ";
	}

	/**
	 * Returns the current transfer speed (averaged since the transfer started), in bytes/millisecond.
	 */
	public double getTransferSpeed()
	{
		if ( System.currentTimeMillis() == startTime )
			return 0;
		else
			return bytesTransferred / ( System.currentTimeMillis() - startTime );
	}

	/**
	 * Returns how much of the transfer is complete, in % (0 to 100).
	 */
	public int getProgress()
	{
		if ( fileSize == 0 )
			return 0;
		else
			return (int) Math.round( 100 * (double) bytesTransferred / fileSize );
	}

	/**
	 * Returns the current stage.
	 */
	public Stage getStage()
	{
		return stage;
	}

	/**
	 * Sets the current stage and updates the form.
	 */
	public void setStage( Stage stage )
	{
		this.stage = stage;

		System.out.println( "\"" + getName() + "\" is now in the " + stage + " stage." );

		if ( form != null )
			form.updateComponents();
	}

	/**
	 * Call if any sort of error occurred. Marks the state and shuts down.
	 */
	protected void transferFailed( String error )
	{
		this.error = error;
		setStage( Stage.FAILED );

		// Try to close the socket, too.
		try
		{
			if ( socket != null )
				socket.close();
		}
		catch ( IOException e )
		{
		}
	}

}
