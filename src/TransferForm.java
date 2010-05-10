import java.awt.Dimension;
import java.awt.Font;
import java.awt.SystemColor;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class TransferForm extends JFrame
{
	private Transfer transfer;

	private JLabel statusLabel, detailLabel1, detailLabel2;

	private JProgressBar progressBar;

	private long lastUpdateTime = 0;

	public TransferForm( Transfer transfer )
	{
		this.transfer = transfer;
		setLayout( new BoxLayout( getContentPane(), BoxLayout.Y_AXIS ) );

		statusLabel = new JLabel();
		statusLabel.setFont( statusLabel.getFont().deriveFont( Font.BOLD, 14 ) );
		statusLabel.setAlignmentX( CENTER_ALIGNMENT );
		detailLabel1 = new JLabel();
		detailLabel2 = new JLabel();

		detailLabel1.setPreferredSize( new Dimension( 350, detailLabel1.getHeight() ) );
		detailLabel1.setAlignmentX( CENTER_ALIGNMENT );
		detailLabel2.setPreferredSize( new Dimension( 350, detailLabel2.getHeight() ) );
		detailLabel2.setAlignmentX( CENTER_ALIGNMENT );
		detailLabel2.setForeground( SystemColor.controlDkShadow );

		add( Box.createRigidArea( new Dimension( 5, 8 ) ) );
		add( statusLabel );
		add( Box.createRigidArea( new Dimension( 5, 5 ) ) );
		add( detailLabel1 );
		add( Box.createRigidArea( new Dimension( 5, 3 ) ) );
		add( detailLabel2 );
		add( Box.createRigidArea( new Dimension( 5, 8 ) ) );

		JPanel progressPanel = new JPanel();
		{
			progressBar = new JProgressBar();
			progressPanel.add( progressBar );
			progressBar.setPreferredSize( new Dimension( 300, 25 ) );
			progressPanel.setPreferredSize( new Dimension( 250, 65 ) );
		}
		add( progressPanel );
		add( Box.createRigidArea( new Dimension( 5, 15 ) ) );

		updateComponents();
		setSize( 350, 145 );
		setResizable( false );
	}

	public void updateComponents()
	{
		setTitle( transfer.getName() );
		progressBar.setValue( transfer.getProgress() );
		if ( ( transfer.getStage() != Transfer.Stage.TRANSFERRING ) || ( System.currentTimeMillis() - lastUpdateTime > 100 ) )
		{
			statusLabel.setText( transfer.toString() );
			detailLabel1.setText( transfer.getDetailLine1() );
			detailLabel2.setText( transfer.getDetailLine2() );
			lastUpdateTime = System.currentTimeMillis();
		}
	}

}
