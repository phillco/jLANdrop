package landrop.ui;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Queue;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


import landrop.peering.MulticastManager;
import landrop.peering.Peer;
import landrop.peering.PeerEventListener;
import landrop.transfer.OutgoingTransfer;

/**
 * Shown when the user has dragged a file to send. Lets them pick who to send it to.
 */
public class RecipientChooserForm extends JFrame
{
	private File file;

	private JButton sendButton, addAddressButton;

	private JLabel statusLabel, detailLabel1;

	private JList receiverList;

	public RecipientChooserForm( final File file )
	{
		this.file = file;
		setLayout( new BoxLayout( getContentPane(), BoxLayout.Y_AXIS ) );

		// Add header.
		addLabels();
		add( Box.createVerticalStrut( 7 ) );

		// Add list of peers.
		add( getRecipientsPanel() );
		add( Box.createVerticalStrut( 3 ) );

		// Add buttons.
		add( getButtonsPanel() );
		add( Box.createVerticalStrut( 15 ) );

		listenForPeers();
		setMinimumSize( new Dimension( 400, 60 ) );
		setResizable( false );
		setVisible( true );
		pack();
	}

	/**
	 * Adds the labels seen at the top of the form.
	 */
	private void addLabels()
	{
		// Add the big "sending xxx..." header label.
		statusLabel = new JLabel( "Sending " + file.getName() );
		statusLabel.setFont( statusLabel.getFont().deriveFont( Font.BOLD, 14 ) );
		statusLabel.setAlignmentX( CENTER_ALIGNMENT );
		add( Box.createVerticalStrut( 10 ) );
		add( statusLabel );
		setTitle( statusLabel.getText() );

		// Add the sub-header.
		detailLabel1 = new JLabel( "Choose a user to send to:" );
		detailLabel1.setAlignmentX( CENTER_ALIGNMENT );
		add( Box.createVerticalStrut( 6 ) );
		add( detailLabel1 );
	}

	/**
	 * Creates a panel with a list of possible peers to send the file to.
	 */
	private JPanel getRecipientsPanel()
	{
		JPanel receipientsPanel = new JPanel();
		{
			receiverList = new JList();
			receiverList.addListSelectionListener( new ListSelectionListener()
			{
				@Override
				public void valueChanged( ListSelectionEvent arg0 )
				{
					// Enable the button now that a user is selected.
					sendButton.setEnabled( receiverList.getSelectedValue() != null );
				}
			} );

			// Embed in a scrollable pane.
			JScrollPane scrollContainer = new JScrollPane( receiverList );
			scrollContainer.setPreferredSize( new Dimension( 350, 60 ) );
			receipientsPanel.add( scrollContainer );
		}

		return receipientsPanel;
	}

	/**
	 * Creates a panel with control buttons.
	 */
	private JPanel getButtonsPanel()
	{
		JPanel buttonsPanel = new JPanel();
		{
			// Add the "add IP" button.
			addAddressButton = new JButton( "Add Computer..." );
			addAddressButton.setPreferredSize( new Dimension( 120, 25 ) );
			addAddressButton.setEnabled( false );
			buttonsPanel.add( addAddressButton );

			buttonsPanel.add( Box.createHorizontalStrut( 30 ) );

			// Add the "send" button.
			sendButton = new JButton( "Send!" );
			sendButton.setPreferredSize( new Dimension( 90, 25 ) );
			sendButton.setEnabled( false );
			sendButton.setFont( sendButton.getFont().deriveFont( Font.BOLD ) );
			sendButton.addActionListener( new ActionListener()
			{
				@Override
				public void actionPerformed( ActionEvent arg0 )
				{
					if ( ( receiverList.getSelectedValue() != null ) && ( receiverList.getSelectedValue() instanceof Peer ) )
						startTransfer( (Peer) receiverList.getSelectedValue() );
				}
			} );
			buttonsPanel.add( sendButton );
		}
		return buttonsPanel;
	}

	/**
	 * Registers this with the multicast-peer listener.
	 */
	private void listenForPeers()
	{
		receiverList.setListData( MulticastManager.getPeers().toArray() );
		if ( receiverList.getVisibleRowCount() > 0 )
			receiverList.setSelectedIndex( 0 );

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

	}

	private void startTransfer( Peer p )
	{
		new OutgoingTransfer( file, p );
		dispose();
	}
}
