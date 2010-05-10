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
	
	public Transfer()
	{
		form = new TransferForm( this );
	}

	public String getDetails()
	{
		if ( stage == Stage.TRANSFERRING )
			return Main.formatFileSize( bytesTransferred ) + " of " + Main.formatFileSize( fileSize );
		else if ( stage == Stage.FINISHED )
			return "Verified with MD5";
		else
			return " ";
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
