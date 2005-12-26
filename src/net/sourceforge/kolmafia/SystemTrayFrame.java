package net.sourceforge.kolmafia;

import javax.swing.JPopupMenu;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;

import com.gc.systray.SystemTrayIconManager;
import net.java.dev.spellcast.utilities.DataUtilities;


public class SystemTrayFrame extends KoLFrame implements Runnable
{
	private SystemTrayIconManager manager;

	public SystemTrayFrame( KoLmafia client )
	{
		super( client, "SystemTrayFrame" );
		(new Thread( this )).start();
	}

	public void run()
	{
		try
		{
			// First load the DesktopIndicator library to allow
			// for system tray usage.

			File library = new File( "data/DesktopIndicator.dll" );

			if ( !library.exists() )
			{
				InputStream input = DataUtilities.getFileInputStream( "", "", "DesktopIndicator.dll" );
				OutputStream output = new FileOutputStream( library );

				byte [] buffer = new byte[ 1024 ];
				int bufferLength;
				while ( (bufferLength = input.read( buffer )) != -1 )
					output.write( buffer, 0, bufferLength );

				output.close();
			}

			// Next, load the icon which will be used by KoLmafia
			// in the system tray.  For now, this will be the old
			// icon used by KoLmelion.

			File trayicon = new File( "images/KoLmelionIcon.ico" );

			if ( !trayicon.exists() )
			{
				java.io.InputStream input = DataUtilities.getFileInputStream( "", "", "KoLmelionIcon.ico" );
				java.io.OutputStream output = new java.io.FileOutputStream( trayicon );

				byte [] buffer = new byte[ 1024 ];
				int bufferLength;
				while ( (bufferLength = input.read( buffer )) != -1 )
					output.write( buffer, 0, bufferLength );

				output.close();
			}

			// Now, make calls to SystemTrayIconManager in order
			// to make use of the system tray.

			System.load( library.getAbsolutePath() );
			this.manager = new SystemTrayIconManager( SystemTrayIconManager.loadImage( trayicon.getAbsolutePath() ), VERSION_NAME );

			JPopupMenu popup = new JPopupMenu();
			constructMenus( popup );
			popup.add( new InvocationMenuItem( "End Session", this, "dispose" ) );

			manager.setLeftClickView( popup );
			manager.setRightClickView( popup );
			manager.setVisible( true );
		}
		catch ( Exception e )
		{
		}
	}

	public void dispose()
	{
		super.dispose();

		KoLFrame [] frames = new KoLFrame[ existingFrames.size() ];
		existingFrames.toArray( frames );
		
		for ( int i = 0; i < frames.length; ++i )
			frames[i].dispose();
	
		manager.setVisible( false );
	}
}