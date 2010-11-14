import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Queue;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class MainFrame extends JFrame implements ActionListener
{
	private JButton sendButton;

	private JLabel receiveStatus, localIP;

	private JList receiverList;

	public MainFrame()
	{
		setLayout( new BoxLayout( getContentPane(), BoxLayout.Y_AXIS ) );

		// ======================
		// Add the title JPanel.
		// ======================

		JPanel titlePanel = new JPanel();
		{
			JLabel titleLabel = new JLabel( "P2P File Transfer!" );
			titleLabel.setForeground( Color.LIGHT_GRAY );
			titleLabel.setFont( new Font( "Sans serif", Font.BOLD, 28 ) );
			titlePanel.add( Box.createRigidArea( new Dimension( 1, 30 ) ) );
			titlePanel.add( titleLabel );
			titlePanel.setBackground( Color.gray );
		}
		add( titlePanel );

		// ======================
		// Add the receive label.
		// ======================
		sendButton = new JButton( "Send a file..." );
		sendButton.setEnabled( false );
		sendButton.addActionListener( this );

		JPanel receivePanel = new JPanel();
		{
			// receivePanel.setLayout( new BoxLayout( receivePanel, BoxLayout.Y_AXIS ) );
			receivePanel.add( Box.createHorizontalGlue() );
			receivePanel.add( Box.createHorizontalStrut( 10 ) );
			receiveStatus = new JLabel( "Server starting..." );
			receiveStatus.setFont( receiveStatus.getFont().deriveFont( Font.BOLD ) );
			receivePanel.add( receiveStatus );

			localIP = new JLabel( "" );
			receivePanel.add( localIP );

			// receivePanel.add( new JButton( "Copy" ) );
			receivePanel.add( Box.createHorizontalStrut( 5 ) );
			receivePanel.setPreferredSize( new Dimension( 450, 40 ) );
			receivePanel.add( sendButton );
			receivePanel.add( Box.createHorizontalStrut( 10 ) );
			receivePanel.add( Box.createHorizontalGlue() );
		}
		add( Box.createVerticalStrut( 10 ) );
		add( receivePanel );

		JPanel receipientsPanel = new JPanel();
		{
			receiverList = new JList();
			receiverList.addListSelectionListener( new ListSelectionListener()
			{
				@Override
				public void valueChanged( ListSelectionEvent arg0 )
				{
					sendButton.setEnabled( receiverList.getSelectedValue() != null );
				}
			} );
			JScrollPane scrollContainer = new JScrollPane( receiverList );
			scrollContainer.setPreferredSize( new Dimension( 400, 75 ) );
			receipientsPanel.add( scrollContainer );
		}
		add( receipientsPanel );
		add( Box.createVerticalStrut( 10 ) );

		// ================
		// Add the footer.
		// ================

		JPanel footerPanel = new JPanel();
		{
			JLabel footerLabel = new JLabel( "Version 1.2 / created by Phillip Cohen" );
			footerLabel.setForeground( Color.gray );
			footerPanel.add( footerLabel );
		}
		add( footerPanel );

		// Other attributes...
		updateLabels();
		setTitle( "P2P File Transfer!" );
		setDefaultCloseOperation( DISPOSE_ON_CLOSE );
		setMinimumSize( new Dimension( 350, 175 ) );
		pack();
		setResizable( false );
		setVisible( true );

		addWindowListener( new java.awt.event.WindowAdapter()
		{
			@Override
			public void windowClosing( WindowEvent winEvt )
			{
				// Perhaps ask user if they want to save any unsaved files first.
				System.exit( 0 );
			}
		} );

		MulticastManager.addPeerListener( new PeerEventListener()
		{
			@Override
			public void peerListUpdated( Queue<Peer> p )
			{
				Object selected = receiverList.getSelectedValue();
				receiverList.setListData( p.toArray() );
				receiverList.setSelectedValue( selected, true );
			}
		} );
		MulticastManager.startBroadcastLoop();
		MulticastManager.startListenLoop();
	}

	public void updateLabels()
	{
		if ( Main.getListener() != null )
		{
			localIP.setVisible( Main.getListener().isEnabled() );
			if ( !Main.getListener().isEnabled() )
				receiveStatus.setText( "Server starting..." );
			else
			{
				receiveStatus.setText( "Ready to receive files!" );
				localIP.setText( "Choose a user to send to: " );
			}
		}
		invalidate();
	}

	@Override
	public void actionPerformed( ActionEvent arg0 )
	{
		// "Send" button was pressed. Show a file selector dialog.
		final JFileChooser fileChooser = new JFileChooser();

		if ( fileChooser.showOpenDialog( this ) != JFileChooser.APPROVE_OPTION )
			return;

		// Read in the selected file.
		File file = fileChooser.getSelectedFile();
		if ( fileChooser.getSelectedFile().length() > Long.MAX_VALUE )
		{
			JOptionPane.showMessageDialog( this, "The file you selected is too big; the max file size that can be transferred is " + Util.formatFileSize( Long.MAX_VALUE ) + "." );
			return;
		}

		if ( ( receiverList.getSelectedValue() == null ) || !( receiverList.getSelectedValue() instanceof Peer ) )
			JOptionPane.showMessageDialog( this, "You didn't select a sender!" );
		else
			new OutgoingTransfer( file, (Peer) receiverList.getSelectedValue() );
	}
}
