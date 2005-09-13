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

public class Nemesis implements KoLConstants
{
	private static KoLmafia client;

	// Items for the cave

	private static final AdventureResult FLY_SWATTER = new AdventureResult( 123, 1 );
	private static final AdventureResult COG = new AdventureResult( 120, 1 );
	private static final AdventureResult SPROCKET = new AdventureResult( 119, 1 );
	private static final AdventureResult GRAVY = new AdventureResult( 80, 1 );
	private static final AdventureResult TONGS = new AdventureResult( 36, 1 );
	private static final AdventureResult KETCHUP = new AdventureResult( 106, 1 );
	private static final AdventureResult CATSUP = new AdventureResult( 107, 1 );

	public static void setClient( KoLmafia client )
	{
		Nemesis.client = client;
	}

	private static boolean checkPrerequisites()
	{
		KoLRequest request;

		// If the client has not yet been set, then there is no cave

		if ( client == null )
			return false;

		client.updateDisplay( DISABLED_STATE, "Checking prerequisites..." );

		// If the player has never ascended, then they're going
		// to have to do it all by hand.

		if ( client.getCharacterData().getAscensions() < 0 )
		{
			client.updateDisplay( ERROR_STATE, "Sorry, you've never ascended." );
			client.cancelRequest();
			return false;
		}

		// Make sure the player has been given the quest

		request = new KoLRequest( client, "mountains.php", true );
		request.run();

		if ( request.responseText.indexOf( "cave.php" ) == -1 )
		{
			client.updateDisplay( ERROR_STATE, "You haven't been given the quest to defeat your Nemesis!" );
			client.cancelRequest();
			return false;
		}

		return true;
	}

	public static void faceNemesis()
	{
		KoLCharacter data = client.getCharacterData();

		// Make sure the player is qualified to use this script

		if ( !checkPrerequisites() )
			return;

		// See how far the player has gotten in this quest

		KoLRequest request = new KoLRequest( client, "cave.php", true );
		request.run();

		int region = 0;

		if ( request.responseText.indexOf( "value='flies'" ) != -1 )
			region = 4;
		else if ( request.responseText.indexOf( "value='door1'" ) != -1 )
			region = 5;
		else if ( request.responseText.indexOf( "value='troll1'" ) != -1 )
			region = 6;
		else if ( request.responseText.indexOf( "value='door2'" ) != -1 )
			region = 7;
		else if ( request.responseText.indexOf( "value='troll2'" ) != -1 )
			region = 8;
		else if ( request.responseText.indexOf( "value='end'" ) != -1 )
			region = 9;
		else if ( request.responseText.indexOf( "cave9done" ) != -1 )
		{
			client.updateDisplay( ERROR_STATE, "You've already defeated your nemesis." );
			client.cancelRequest();
			return;
		}

		List requirements = new ArrayList();

		// Need a flyswatter to get past the Fly Bend

		if ( region <= 4 )
			requirements.add( FLY_SWATTER );

		// Need a cog and a sprocket to get past the Stone Door

		if ( region <= 5 )
		{
			requirements.add( COG );
			requirements.add( SPROCKET );
		}

		// Need fairy gravy to get past the first lavatory troll

		if ( region <= 6 )
			requirements.add( GRAVY );

		// Need tongs to get past the salad covered door

		if ( region <= 7 )
			requirements.add( TONGS );

		// Need some kind of ketchup to get past the second lavatory troll

		AdventureResult ketchup = CATSUP.getCount( client.getInventory() ) > 0 ? CATSUP : KETCHUP;

		if ( region <= 8 )
			requirements.add( ketchup );

		if ( !client.checkRequirements( requirements ) )
			return;

		// Save currently equipped weapon so we can re-equip it for the final battle.

		String weapon = data.getEquipment( KoLCharacter.WEAPON );
		boolean needsWeapon = false;

		// Pass the obstacles one at a time.

		for ( int i = region; i <= 9; i++ )
		{
			String action = "none";

			switch (i)
			{
			case 4: // The Fly Bend
				// Equip fly swatter
				(new EquipmentRequest( client, FLY_SWATTER.getName() )).run();
				needsWeapon = true;
				action = "flies";
				client.updateDisplay( DISABLED_STATE, "Swatting flies..." );
				break;

			case 5: // A Stone Door
				action = "door1";
				client.updateDisplay( DISABLED_STATE, "Passing Stone Door..." );
				break;

			case 6: // Lavatory Troll 1
				action = "troll1";
				client.updateDisplay( DISABLED_STATE, "Feeding first troll..." );
				break;

			case 7:	// Salad-Covered Door
				// Equip tongs
				(new EquipmentRequest( client, TONGS.getName() )).run();
				needsWeapon = true;
				action = "door2";
				client.updateDisplay( DISABLED_STATE, "Plucking Salad..." );
				break;

			case 8: // Lavatory Troll 2
				action = "troll2";
				client.updateDisplay( DISABLED_STATE, "Feeding second troll..." );
				break;

			case 9: // Chamber of Epic Conflict
				// Equip your original weapon
				if ( needsWeapon )
					(new EquipmentRequest( client, weapon )).run();
				action = "end";
				client.updateDisplay( DISABLED_STATE, "Fighting your nemesis..." );
				break;
			}

			// Visit the cave
			request = new AdventureRequest( client, "cave.php", action );
			request.run();

			// Consume items
			switch (i)
			{
			case 5: // A Stone Door
				// Use up cog & sprocket
				client.processResult( COG.getNegation() );
				client.processResult( SPROCKET.getNegation() );
				break;

			case 6: // Lavatory Troll 1
				// Use up fairy gravy
				client.processResult( GRAVY.getNegation() );
				break;

			case 8: // Lavatory Troll 2
				// Use up ketchup
				client.processResult( ketchup.getNegation() );
				break;

			case 9: // Chamber of Epic Conflict
				client.processResult( new AdventureResult( AdventureResult.ADV, -1 ) );
				break;
			}

			// Gain items and stats
			client.processResults( request.responseText );
		}

		if ( !client.permitsContinue() )
		{
			client.updateDisplay( ERROR_STATE, "KoLmafia was unable to defeat your nemesis." );
			return;
		}

		client.updateDisplay( ENABLED_STATE, "You defeated your nemesis. Congratulations!" );
	}
}