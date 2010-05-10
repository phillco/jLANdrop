import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class Transfer extends Thread
{
	public enum Stage
	{
		WAITING, REJECTED, TRANSFERRING, VERIFYING, FINISHED, FAILED
	}

	private Stage stage = Stage.WAITING;

	protected TransferForm form;

	protected int fileSize;

	protected int bytesTransferred = 0;

	protected long startTime;

	protected MessageDigest digest = null;

	public Transfer()
	{
		try
		{
			digest = MessageDigest.getInstance( "MD5" );
			digest.reset();
		}
		catch ( NoSuchAlgorithmException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		form = new TransferForm( this );
	}

	public String getDetails()
	{
		if ( stage == Stage.TRANSFERRING )
			return Main.formatFileSize( bytesTransferred ) + " of " + Main.formatFileSize( fileSize ) + " at " + Main.formatFileSize( getTransferSpeed() ) + "/s";
		else if ( stage == Stage.FINISHED )
			return "Verified with MD5";
		else
			return " ";
	}

	public double getTransferSpeed()
	{
		if ( System.currentTimeMillis() == startTime )
			return 15;
		else
			return bytesTransferred * 100.0 / ( System.currentTimeMillis() - startTime );
	}

	public int getProgress()
	{
		if ( fileSize == 0 )
			return 0;
		else
			return (int) Math.round( 100 * (double) bytesTransferred / fileSize );
	}

	public void setStage( Stage stage )
	{
		this.stage = stage;

		if ( form != null )
			form.updateComponents();
	}

	public Stage getStage()
	{
		return stage;
	}

}
