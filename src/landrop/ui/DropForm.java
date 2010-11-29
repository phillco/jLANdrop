package landrop.ui;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import landrop.Main;
import landrop.Util;
import landrop.ui.FileDrop.Listener;

/**
 * The main application form that lets the user drop files onto it. 
 */
public class DropForm extends JFrame
{
	private JLabel localIP;

	private JPanel fileDropPanel;

	public DropForm()
	{
		setLayout( new BoxLayout( getContentPane(), BoxLayout.Y_AXIS ) );

		// ======================
		// Add the title JPanel.
		// ======================

		JPanel titlePanel = new JPanel();
		{
			JLabel titleLabel = new JLabel( "LANdrop!" );
			titleLabel.setForeground( Color.LIGHT_GRAY );
			titleLabel.setFont( new Font( "Sans serif", Font.BOLD, 32 ) );
			titlePanel.add( titleLabel );
			titlePanel.setBackground( Color.gray );
		}
		add( titlePanel );

		// ==================
		// Add the file drop.
		// ==================

		fileDropPanel = new JPanel();
		{
			JLabel dropLabel = new JLabel( "Drag files here to send..." );
			dropLabel.setFont( dropLabel.getFont().deriveFont( Font.BOLD ) );
			dropLabel.setForeground( new Color( 100, 100, 100 ) );
			fileDropPanel.add( Box.createVerticalStrut( 70 ) );
			fileDropPanel.add( dropLabel );

			fileDropPanel.setBackground( Color.lightGray );
			fileDropPanel.setPreferredSize( new Dimension( 400, 80 ) );
			new FileDrop( fileDropPanel, new FileDrop.Listener()
			{
				@Override
				public void filesDropped( java.io.File[] files )
				{
					onFilesDropped( files );
				}

			} );
		}
		add( fileDropPanel );
		add( Box.createVerticalStrut( 10 ) );

		// ================
		// Add the footer.
		// ================

		JPanel footerPanel = new JPanel();
		{
			JLabel footerLabel = new JLabel( "v1.3 by Phillip Cohen" );

			localIP = new JLabel( "" );
			localIP.setForeground( Color.darkGray );
			footerPanel.add( localIP );
			footerPanel.add( Box.createHorizontalStrut( 2 ) );

			footerLabel.setForeground( Color.gray );
			footerPanel.add( footerLabel );
		}
		add( footerPanel );

		// Other attributes...
		updateLabels();
		setTitle( "Welcome to LANdrop!" );
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
				System.exit( 0 );
			}
		} );
	}

	/**
	 * Called when something's dropped onto the file drop panel.
	 */
	private void onFilesDropped( File[] files )
	{
		File file = files[0]; // Todo: support multiple files

		if ( file.isDirectory() )
			return; // Todo: support directories
		else if ( file.length() > Long.MAX_VALUE )
		{
			JOptionPane.showMessageDialog( this, "The file you selected is too big; the max file size that can be transferred is " + Util.formatFileSize( Long.MAX_VALUE ) + "." );
			return;
		}
		else
			new RecipientChooserForm( file );
	}

	public void updateLabels()
	{
		if ( Main.getListener() != null )
		{
			localIP.setVisible( Main.getListener().isEnabled() );
			if ( Main.getListener().isEnabled() )
				localIP.setText( "Your IP is " + Main.getListener().getServerIP() );
		}
		invalidate();
	}
}