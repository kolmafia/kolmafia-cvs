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

import java.util.List;
import java.util.ArrayList;
import java.util.ListIterator;

import net.java.dev.spellcast.utilities.LockableListModel;

/**
 * An extension of a <code>KoLRequest</code> which specifically handles
 * buffing the clan.  Note that this does not calculate how many members
 * will be buffed - this is determined by the server.
 */

public class ClanBuffRequest extends KoLRequest
{
	private int buffID;

	/**
	 * Constructs a new <code>ClanBuffRequest</code> with the
	 * specified buff identifier.  This constructor is only
	 * available internally.  Note that no descendents are
	 * possible because of the nature of the constructor.
	 *
	 * @param	client	The client to be notified in the event of error
	 * @param	buffID	The unique numeric identifier of the buff
	 */

	private ClanBuffRequest( KoLmafia client, int buffID )
	{
		super( client, "clan_stash.php" );

		this.buffID = buffID;
		addFormField( "action", "buyround" );
		addFormField( "size", String.valueOf( buffID % 10 ) );
		addFormField( "whichgift", String.valueOf( (int) ( buffID / 10 ) ) );
	}

	/**
	 * Returns a list of all the possible requests available through
	 * the current implementation of <code>ClanBuffRequest</code>.
	 *
	 * @param	client	The client to be associated with the requests
	 * @return	A complete <code>ListModel</code>
	 */

	public static LockableListModel getRequestList( KoLmafia client )
	{
		LockableListModel requestList = new LockableListModel();
		for ( int i = 1; i <= 6; ++i )
			for ( int j = 1; j <= 3; ++j )
				requestList.add( new ClanBuffRequest( client, 10*i + j ) );

		return requestList;
	}

	/**
	 * Returns the string form of this request, which is the formal name
	 * of the buff that this buff request represents.
	 *
	 * @return	The formal name of the clan buff requested
	 */

	public String toString()
	{
		StringBuffer stringForm = new StringBuffer();

		switch ( buffID % 10 )
		{
			case 1:
				stringForm.append( "Cheap " );
				break;
			case 2:
				stringForm.append( "Normal " );
				break;
			default:
				stringForm.append( "Expensive " );
				break;
		}

		switch ( ((int) ( buffID / 10 )) % 3 )
		{
			case 1:
				stringForm.append( "Muscle " );
				break;
			case 2:
				stringForm.append( "Mysticality " );
				break;
			default:
				stringForm.append( "Moxie " );
				break;
		}

		stringForm.append( buffID < 40 ? "Training" : "Boost" );
		return stringForm.toString();
	}
}
