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
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class FamiliarRequest extends KoLRequest
{
	private FamiliarData changeTo;

	public FamiliarRequest( KoLmafia client )
	{
		super( client, "familiar.php" );
		this.changeTo = null;
	}

	public FamiliarRequest( KoLmafia client, FamiliarData changeTo )
	{
		super( client, "familiar.php" );

		if ( changeTo == FamiliarData.NO_FAMILIAR )
		{
			addFormField( "action", "putback" );
		}
		else
		{
			addFormField( "action", "newfam" );
			addFormField( "newfam", String.valueOf( changeTo.getID() ) );
		}

		this.changeTo = changeTo;
	}

	public String getFamiliarChange()
	{	return changeTo == null ? null : changeTo.toString();
	}

	public void run()
	{
		if ( changeTo == null)
			KoLmafia.updateDisplay( "Retrieving familiar data..." );
		else
		{
			FamiliarData familiar = KoLCharacter.getFamiliar();
			if ( familiar != FamiliarData.NO_FAMILIAR )
				KoLmafia.updateDisplay( "Putting " + familiar.getName() + " the " + familiar.getRace() + " back into terrarium..." );

			if (changeTo != FamiliarData.NO_FAMILIAR )
				KoLmafia.updateDisplay( "Taking " + changeTo.getName() + " the " + changeTo.getRace() + " out of terrarium..." );
		}

		super.run();
	}

	protected void processResults()
	{
		super.processResults();
		FamiliarData.registerFamiliarData( client, responseText );
		KoLCharacter.updateEquipmentList( KoLCharacter.FAMILIAR );

		if ( changeTo == null )
			KoLmafia.updateDisplay( "Familiar data retrieved." );
	}

	public String getCommandForm( int iterations )
	{
		String familiarName = getFamiliarChange();
		return familiarName == null ? "" : "familiar " + familiarName;
	}
}
