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

import java.awt.BorderLayout;
import net.java.dev.spellcast.utilities.ActionPanel;

/**
 * A special frame which serves to display a single <code>KoLPanel</code>
 * with a 10 pixel border around the edges.  This is designed to eliminate
 * the need for nested KoLFrame classes in the event that only one panel
 * is to be displayed.
 */

public class KoLPanelFrame extends KoLFrame
{
	/**
	 * Constructs an empty <code>KoLPanelFrame</code> which
	 * uses the given StaticEntity.getClient() and has the given title.
	 */

	public KoLPanelFrame( String title )
	{	super( title );
	}

	/**
	 * Constructs a <code>KoLPanelFrame</code> which contains
	 * the given StaticEntity.getClient() and has the given title.  The content
	 * panel for this frame will be initialized to the panel
	 * that is provided.
	 */

	public KoLPanelFrame( String title, ActionPanel panel )
	{
		super( title );
		setContentPanel( panel );
	}

	/**
	 * Sets the content panel for this <code>KoLPanelFrame</code>
	 * to the given panel.  This can only be called once, and is
	 * used to initialize the <code>KoLPanelFrame</code> in the
	 * event that the panel is not known at construction time
	 * (for example, for descendant classes).
	 */

	protected void setContentPanel( ActionPanel panel )
	{
		if ( getClass() == KoLPanelFrame.class )
		{
			this.frameName = panel.getClass().getName();
			this.frameName = frameName.substring( frameName.lastIndexOf( "." ) + 1 );
		}

		framePanel.add( panel, BorderLayout.CENTER );
	}
}
