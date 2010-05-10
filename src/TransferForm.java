import java.awt.Dimension;
import java.awt.Font;

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

	public TransferForm( Transfer transfer )
	{
		this.transfer = transfer;
		setLayout( new BoxLayout( getContentPane(), BoxLayout.Y_AXIS ) );

		JPanel labelPanel = new JPanel();
		{
			JPanel inner = new JPanel();

			inner.setLayout( new BoxLayout( inner, BoxLayout.Y_AXIS ) );
			statusLabel = new JLabel();
			statusLabel.setFont( statusLabel.getFont().deriveFont( Font.BOLD ) );
			detailLabel1 = new JLabel();
			detailLabel2 = new JLabel();

			inner.add( statusLabel );
			inner.add( Box.createRigidArea( new Dimension( 5, 5 ) ) );
			inner.add( detailLabel1 );
			inner.add( detailLabel2 );
			labelPanel.add( inner );

			// labelPanel.add( );
			labelPanel.setPreferredSize( new Dimension( 400, 100 ) );
		}
		add( Box.createRigidArea( new Dimension( 5, 5 ) ) );
		add( labelPanel );

		JPanel progressPanel = new JPanel();
		{
			progressBar = new JProgressBar();
			progressPanel.add( progressBar );
			progressBar.setPreferredSize( new Dimension( 200, 20 ) );
			progressPanel.setPreferredSize( new Dimension( 250, 65 ) );
		}
		add( progressPanel );

		updateComponents();
		setSize( 300, 130 );
		setResizable( false );
	}

	public void updateComponents()
	{
		setTitle( transfer.getName() );
		statusLabel.setText( transfer.toString() );
		detailLabel1.setText( transfer.getDetailLine1() );
		detailLabel2.setText( transfer.getDetailLine2() );
		progressBar.setValue( transfer.getProgress() );
	}

}
