/**
 * Copyright (c) 2005, KoLmafia development team
 * http://kolmafia.sourceforge.net/
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  [1] Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *  [2] Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in
 *      the documentation and/or other materials provided with the
 *      distribution.
 *  [3] Neither the name "KoLmafia development team" nor the names of
 *      its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written
 *      permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package net.sourceforge.kolmafia;

import javax.swing.JFrame;
import java.awt.Dimension;
import javax.swing.SwingUtilities;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

/**
 * A special class which ensures that frames can be created inside
 * of the Swing thread.  This avoids deadlock problems that often
 * cause KoLmafia not to load properly.
 */

public class CreateFrameRunnable implements Runnable, KoLConstants
{
	private static final Class [] MULTI_INSTANCE =
	{
		ChatFrame.class,
		RequestFrame.class,
		ProfileFrame.class,
		SendMessageFrame.class
	};

	private Class creationType;
	private JFrame creation;

	private Constructor creator;
	private Object [] parameters;
	private boolean ranRequests;

	public CreateFrameRunnable( Class creationType )
	{	this( creationType, new Object[0] );
	}

	public CreateFrameRunnable( Class creationType, Object [] parameters )
	{
		this.creationType = creationType;
		this.parameters = parameters;
		this.ranRequests = false;

		Class [] parameterTypes= new Class[ parameters.length ];
		for ( int i = 0; i < parameters.length; ++i )
			parameterTypes[i] = parameters[i] == null ? null : parameters[i].getClass();

		this.creator = null;
		boolean isValidConstructor;

		Class [] constructorParameterTypes;
		Constructor [] constructors = creationType.getConstructors();

		for ( int i = 0; i < constructors.length; ++i )
		{
			constructorParameterTypes = constructors[i].getParameterTypes();
			if ( constructorParameterTypes.length != parameters.length )
				continue;

			isValidConstructor = true;
			for ( int j = 0; j < constructorParameterTypes.length && isValidConstructor; ++j )
				if ( parameterTypes[j] != null && !constructorParameterTypes[j].isAssignableFrom( parameterTypes[j] ) )
					isValidConstructor = false;

			if ( isValidConstructor )
				this.creator = constructors[i];
		}
	}

	public JFrame getCreation()
	{	return creation;
	}

	public void run()
	{
		if ( creationType == StoreManageFrame.class && !KoLCharacter.hasStore() )
		{
			KoLmafia.updateDisplay( ERROR_STATE, "Sorry, you don't have a store." );
			KoLmafia.enableDisplay();
			return;
		}

		// If there is no creation creation, then return
		// from the method because there's nothing to do.

		if ( this.creator == null )
		{
			KoLmafia.updateDisplay( ERROR_STATE, "Frame could not be loaded." );
			return;
		}

		// Run any needed requests before falling into
		// the event dispatch thread.

		if ( !ranRequests )
		{
			ranRequests = runRequests();
			if ( !ranRequests )
				return;
		}

		// If you are in the Swing thread, then wait
		// until you are no longer in the Swing thread
		// so you are able to see debug messages.

		if ( !SwingUtilities.isEventDispatchThread() )
		{
			try
			{
				SwingUtilities.invokeAndWait( this );
				return;
			}
			catch ( Exception e )
			{
				// This should not happen.  Therefore, print
				// a stack trace for debug purposes.

				StaticEntity.printStackTrace( e, "Swing thread interrupted" );
				return;
			}
		}

		// Now that you're guaranteed to be in the event
		// dispatch thread, run the construction.

		runConstruction();
	}

	private boolean runRequests()
	{
		if ( !StaticEntity.getClient().shouldMakeConflictingRequest() )
		{
			try
			{
				Method m = creationType.getMethod( "executesConflictingRequest", NOPARAMS );
				Boolean result = (Boolean) m.invoke( creationType, null );

				if ( result.equals( Boolean.TRUE ) )
				{
					KoLmafia.updateDisplay( "You can't do that while adventuring." );
					return false;
				}
			}
			catch ( Exception e )
			{
				// In this case, you know for sure that the
				// method does not exist.  So, do nothing.
			}
		}

		// Check to see if this is a frame that should
		// only be loaded once, based on the static list.

		KoLFrame currentFrame;
		Class currentType;
		String currentTypeName;

		String creationTypeName = (creationType == KoLPanelFrame.class ? parameters[1].getClass() : creationType).getName();
		creationTypeName = creationTypeName.substring( creationTypeName.lastIndexOf( "." ) + 1 );

		for ( int i = 0; i < existingFrames.size() && this.creation == null; ++i )
		{
			currentFrame = (KoLFrame) existingFrames.get(i);
			currentTypeName = currentFrame.getFrameName();

			if ( currentTypeName.equals( creationTypeName ) )
			{
				currentType = currentFrame.getClass();

				boolean allowMultiple = false;
				for ( int j = 0; j < MULTI_INSTANCE.length; ++j )
					if ( currentType == MULTI_INSTANCE[j] )
						allowMultiple = true;

				if ( !allowMultiple )
					this.creation = currentFrame;
			}
		}

		// Now, test to see if any requests need to be run before
		// you fall into the event dispatch thread.

		if ( this.creation == null )
		{
			if ( creationType == BuffRequestFrame.class )
				BuffBotDatabase.configureBuffBots();
			if ( creationType == CakeArenaFrame.class || creationType == FamiliarTrainingFrame.class )
				CakeArenaManager.getOpponentList();
		}

		// If it gets this far, then all requests were successfully
		// run, so return true.

		return true;
	}

	private void runConstruction()
	{
		// Now, if you aren't supposed to create a new instance,
		// do not do so -- however, if it's okay to do so, then
		// go ahead and create it.

		try
		{
			if ( this.creation == null )
				this.creation = (JFrame) creator.newInstance( parameters );

			String tabSetting = "," + StaticEntity.getProperty( "initialDesktop" ) + ",";
			String searchString = this.creation instanceof ChatFrame ? "KoLMessenger" :
				this.creation instanceof KoLFrame ? ((KoLFrame)this.creation).getFrameName() : "...";

			boolean appearsInTab = this.creation instanceof KoLFrame && tabSetting.indexOf( "," + searchString + "," ) != -1;

			appearsInTab &= !(this.creation instanceof RequestFrame) ||
				(this.creation.getClass() == RequestFrame.class && ((RequestFrame)this.creation).hasSideBar());

			// If the gui is limited to one frame, then make this frame
			// a tab and remove any extra tabs created this way perviouly.

			if ( !(this.creation instanceof LoginFrame) && StaticEntity.getProperty( "guiUsesOneWindow" ).equals( "true" ) )
			{
				if ( !appearsInTab )
					KoLDesktop.removeExtraTabs();

				appearsInTab = true;
			}

			// If the person is requesting a this.creation that is meant
			// to appear in the KoLDesktop interface, then make
			// sure you initialize it.

			if ( appearsInTab && !KoLDesktop.isInitializing() )
			{
				KoLDesktop.getInstance().initializeTabs();
				KoLDesktop.getInstance().pack();
				KoLDesktop.getInstance().setVisible( true );
			}

			// Load the KoL frame to the appropriate location
			// on the screen now that the frame has been packed
			// to the appropriate size.

			if ( !appearsInTab && this.creation instanceof KoLFrame )
			{
				((KoLFrame)this.creation).constructToolbar();
				if ( ((KoLFrame)this.creation).useSidePane() )
					((KoLFrame)this.creation).addCompactPane();

				this.creation.setJMenuBar( new KoLMenuBar() );
			}
			else if ( !(this.creation instanceof KoLFrame) )
				this.creation.setJMenuBar( new KoLMenuBar() );

			this.creation.pack();
			if ( this.creation instanceof SkillBuffFrame && parameters.length == 1 )
				((SkillBuffFrame)this.creation).setRecipient( (String) parameters[0] );

			if ( !(this.creation instanceof KoLFrame) )
				this.creation.setLocationRelativeTo( null );

			// With the location set set on screen, make sure
			// to disable it (if necessary), ensure the frame's
			// visibility on screen and request focus.

			this.creation.setEnabled( true );

			if ( appearsInTab )
				KoLDesktop.addTab( (KoLFrame) this.creation );
			else
				this.creation.setVisible( true );

			this.creation.requestFocus();
		}
		catch ( Exception e )
		{
			// This should not happen.  Therefore, print
			// a stack trace for debug purposes.

			StaticEntity.printStackTrace( e, "Frame could not be loaded" );
			return;
		}
	}
}
