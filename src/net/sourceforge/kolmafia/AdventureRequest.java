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
import java.util.StringTokenizer;

/**
 * An extension of a <code>KoLRequest</code> that handles generic adventures,
 * such as those which involve fighting, vacations at the shore, or gambling
 * at the casino.  It will not handle trips to the hermit or to the sewers,
 * as these must be handled differently.
 */

public class AdventureRequest extends KoLRequest
{
	private String adventureName;
	private String formSource;
	private String adventureID;
	protected int adventuresUsed;

	public static final AdventureResult ABRIDGED = new AdventureResult( 534, -1 );
	public static final AdventureResult BRIDGE = new AdventureResult( 535, -1 );
	public static final AdventureResult DODECAGRAM = new AdventureResult( 479, -1 );
	public static final AdventureResult CANDLES = new AdventureResult( 480, -1 );
	public static final AdventureResult BUTTERKNIFE = new AdventureResult( 481, -1 );

	/**
	 * Constructs a new <code>AdventureRequest</code> which executes the
	 * adventure designated by the given ID by posting to the provided form,
	 * notifying the given client of results (or errors).
	 *
	 * @param	client	The client to which results will be reported
	 * @param	adventureName	The name of the adventure location
	 * @param	formSource	The form to which the data will be posted
	 * @param	adventureID	The identifer for the adventure to be executed
	 */

	public AdventureRequest( KoLmafia client, String adventureName, String formSource, String adventureID )
	{
		super( client, formSource );
		this.adventureName = adventureName;
		this.formSource = formSource;
		this.adventureID = adventureID;

		// The adventure ID is all you need to identify the adventure;
		// posting it in the form sent to adventure.php will handle
		// everything for you.

		// Almost all requests use one adventure
		this.adventuresUsed = 1;

		if ( formSource.equals( "adventure.php" ) )
			addFormField( "snarfblat", adventureID );
		else if ( formSource.equals( "shore.php" ) )
		{
			addFormField( "whichtrip", adventureID );
			addFormField( "pwd" );
			this.adventuresUsed = 3;
		}
		else if ( formSource.equals( "casino.php" ) )
		{
			addFormField( "action", "slot" );
			addFormField( "whichslot", adventureID );
			if ( !adventureID.equals( "11" ) )
				this.adventuresUsed = 0;
		}
		else if ( formSource.equals( "dungeon.php" ) )
		{
			addFormField( "action", "Yep" );
			addFormField( "option", "1" );
			addFormField( "pwd" );
		}
		else if ( formSource.equals( "knob.php" ) )
		{
			addFormField( "pwd" );
			addFormField( "king", "Yep." );
		}
		else if ( formSource.equals( "mountains.php" ) )
		{
			addFormField( "pwd" );
			addFormField( "orcs", "1" );
			this.adventuresUsed = 0;
		}
		else if ( formSource.equals( "friars.php" ) )
		{
			addFormField( "pwd" );
			addFormField( "action", "ritual" );
			this.adventuresUsed = 0;
		}
		else if ( formSource.equals( "lair6.php" ) )
			addFormField( "place", adventureID );
		else if ( !formSource.equals( "rats.php" ) )
			addFormField( "action", adventureID );
	}

	/**
	 * Executes the <code>AdventureRequest</code>.  All items and stats gained
	 * or lost will be reported to the client, as well as any errors encountered
	 * through adventuring.  Meat lost due to an adventure (such as those to
	 * the casino, the shore, or the tavern) will also be reported.  Note that
	 * adventure costs are not yet being reported.
	 */

	public void run()
	{
		// Prevent the request from happening if the client attempted
		// to cancel in the delay period.

		if ( !KoLmafia.permitsContinue() )
			return;

		if ( getProperty( "cloverProtectActive" ).equals( "true" ) )
			DEFAULT_SHELL.executeLine( "use * ten-leaf clover" );

		if ( formSource.equals( "mountains.php" ) )
		{
			KoLRequest check = new KoLRequest( client, "mountains.php" );
			check.run();

			if ( check.responseText.indexOf( "value=80" ) != -1 )
			{
				KoLmafia.updateDisplay( PENDING_STATE, "The Orc Chasm has already been bridged." );
				return;
			}
		}

		super.run();

		if ( getProperty( "cloverProtectActive" ).equals( "true" ) )
			DEFAULT_SHELL.executeLine( "use * ten-leaf clover" );
	}

	protected void processResults()
	{
		// If this is a lucky adventure, then remove a clover
		// from the player's inventory -- this will occur when
		// you see either "Your ten-leaf clover" or "your
		// ten-leaf clover" (shorten to "our ten-leaf clover"
		// for substring matching)

		if ( responseText.indexOf( "our ten-leaf clover" ) != -1 )
			client.processResult( SewerRequest.CLOVER );

		// Sometimes, there's no response from the server.
		// In this case, simply rerun the request.

		if ( responseText.trim().length() == 0 )
		{
			KoLmafia.updateDisplay( "Empty response from server.  Retrying..." );
			this.run();
			return;
		}

		// If you haven't unlocked the orc chasm yet,
		// try doing so now.

		if ( adventureID.equals( "80" ) && responseText.indexOf( "You shouldn't be here." ) != -1 )
		{
			(new AdventureRequest( client, "Bridge the Orc Chasm", "mountains.php", "80" )).run();
			if ( KoLmafia.permitsContinue() )
				this.run();

			return;
		}

		// We're missing an item, haven't been given a quest yet, or otherwise
		// trying to go somewhere not allowed.

		if ( responseText.indexOf( "You shouldn't be here." ) != -1 || responseText.indexOf( "not yet be accessible" ) != -1 )
		{
			KoLmafia.updateDisplay( ERROR_STATE, "You can't get to that area yet." );
			return;
		}

		if ( responseText.indexOf( "in the regular dimension now" ) != -1 )
		{
			// "You're in the regular dimension now, and don't
			// remember how to get back there."
			KoLmafia.updateDisplay( PENDING_STATE, "You are no longer Half-Astral." );
			return;
		}

		if ( responseText.indexOf( "into the spectral mists" ) != -1 )
		{
			KoLmafia.updateDisplay( ERROR_STATE, "No one may know of its coming or going." );
			return;
		}

		if ( responseText.indexOf( "temporal rift in the plains has closed" ) != -1 )
		{
			KoLmafia.updateDisplay( PENDING_STATE, "The temporal rift has closed." );
			return;
		}

		// Cold protection is required for the area.  This only happens at
		// the peak.  Abort and notify.

		if ( responseText.indexOf( "need some sort of protection" ) != -1 )
		{
			KoLmafia.updateDisplay( ERROR_STATE, "You need cold protection." );
			return;
		}

		// Stench protection is required for the area.	This only
		// happens at the Guano Junction.  Abort and notify.

		if ( responseText.indexOf( "need stench protection to adventure here" ) != -1 )
		{
			KoLmafia.updateDisplay( ERROR_STATE, "You need stench protection." );
			return;
		}

		// This is a server error. Hope for the
		// best and repeat the request.

		if ( responseText.indexOf( "No adventure data exists for this location" ) != -1 )
		{
			KoLmafia.updateDisplay( "Server error.  Repeating request..." );
			this.run();
			return;
		}

		// Nothing more to do in this area

		if ( responseText.indexOf( "This part of the cyrpt is already undefiled" ) != -1 ||
		     responseText.indexOf( "You can't repeat an adventure here." ) != -1 )
		{
			KoLmafia.updateDisplay( PENDING_STATE, "Nothing more to do here." );
			return;
		}

		if ( responseText.indexOf( "You must have at least" ) != -1 )
		{
			KoLmafia.updateDisplay( ERROR_STATE, "Your stats are too low for this location." );
			return;
		}

		// Cobb's Knob King's Chamber: if you've already
		// defeated the goblin king, go into pending state.

		if ( formSource.equals( "knob.php" ) && responseText.indexOf( "You've already slain the Goblin King" ) != -1 )
		{
			KoLmafia.updateDisplay( PENDING_STATE, "You already defeated the Goblin King." );
			return;
		}

		// The Haert of the Cyrpt: if you've already defeated
		// the bonerdagon, go into pending state.

		if ( formSource.equals( "cyrpt.php" ) && responseText.indexOf( "Bonerdagon has been defeated" ) != -1 )
		{
			KoLmafia.updateDisplay( PENDING_STATE, "You already defeated the Bonerdagon." );
			return;
		}

		if ( responseText.indexOf( "already undefiled" ) != -1 )
		{
			KoLmafia.updateDisplay( PENDING_STATE, "Cyrpt area cleared." );
			return;
		}

		// The Orc Chasm (pre-bridge)

		if ( formSource.equals( "mountains.php" ) )
		{
			// If there's no link to the valley beyond, put down a
			// bridge

			if ( responseText.indexOf( "value=80" ) == -1 )
			{
				// If you have an unabridged dictionary in your
				// inventory, visit the untinkerer
				// automatically and repeat the request.

				if ( KoLCharacter.hasItem( ABRIDGED, false ) )
				{
					AdventureDatabase.retrieveItem( ABRIDGED.getNegation() );
					(new UntinkerRequest( client, ABRIDGED.getItemID() )).run();

					this.run();
					return;
				}

				// Otherwise, the player is unable to cross the
				// orc chasm at this time.

				KoLmafia.updateDisplay( ERROR_STATE, "You can't cross the Orc Chasm." );
				return;
			}

			if ( responseText.indexOf( "the path to the Valley is clear" ) != -1 )
			{
				KoLmafia.updateDisplay( PENDING_STATE, "You have bridged the Orc Chasm." );
				client.processResult( BRIDGE );
			}

			return;
		}

		// The Deep Fat Friars' Ceremony Location

		if ( formSource.equals( "friars.php" ) )
		{
			// "The infernal creatures who have tainted the Friar's
			// copse stream back into the gate, hooting and
			// shrieking."

			if ( responseText.indexOf( "hooting and shrieking" ) != -1 )
			{
				client.processResult( DODECAGRAM );
				client.processResult( CANDLES );
				client.processResult( BUTTERKNIFE );

				Matcher learnedMatcher = Pattern.compile( "You emerge with a (.*?) of Steel" ).matcher( responseText );
				if ( learnedMatcher.find() )
					KoLCharacter.addAvailableSkill( new UseSkillRequest( client, learnedMatcher.group(1) + " of Steel", "", 1 ) );

				KoLmafia.updateDisplay( PENDING_STATE, "Taint cleansed." );
				return;
			}

			// Even after you've performed the ritual:
			// "You don't appear to have all of the elements
			// necessary to perform the ritual."

			KoLmafia.updateDisplay( ERROR_STATE, "You can't perform the ritual." );
			return;
		}

		// If you're at the casino, each of the different slot
		// machines deducts meat from your tally

		if ( formSource.equals( "casino.php" ) )
		{
			if ( adventureID.equals( "1" ) )
				client.processResult( new AdventureResult( AdventureResult.MEAT, -5 ) );
			else if ( adventureID.equals( "2" ) )
				client.processResult( new AdventureResult( AdventureResult.MEAT, -10 ) );
			else if ( adventureID.equals( "11" ) )
				client.processResult( new AdventureResult( AdventureResult.MEAT, -10 ) );
		}

		if ( adventureID.equals( "70" ) )
			client.processResult( new AdventureResult( AdventureResult.MEAT, -10 ) );
		else if ( adventureID.equals( "71" ) )
			client.processResult( new AdventureResult( AdventureResult.MEAT, -30 ) );

		// Shore Trips cost 500 meat each; handle
		// the processing here.

		if ( formSource.equals( "shore.php" ) )
			client.processResult( new AdventureResult( AdventureResult.MEAT, -500 ) );

		// Trick-or-treating requires a costume;
		// notify the user of this error.

		if ( formSource.equals( "trickortreat.php" ) && responseText.indexOf( "without a costume" ) != -1 )
		{
			KoLmafia.updateDisplay( ERROR_STATE, "You must wear a costume." );
			return;
		}

		super.processResults();
	}

	public static String registerEncounter( KoLRequest request )
	{
		String urlString = request.getURLString();
		if ( !(request instanceof AdventureRequest) && !containsEncounter( urlString, request.responseText ) )
			return "";

		// The first round is unique in that there is no
		// data fields.  Therefore, it will equal fight.php
		// exactly every single time.

		if ( urlString.equals( "fight.php" ) )
		{
			Matcher encounterMatcher = Pattern.compile( "<span id='monname'>(.*?)</span>" ).matcher( request.responseText );
			if ( encounterMatcher.find() )
			{
				String encounter = encounterMatcher.group(1);
				KoLmafiaCLI.printLine( "Encounter: " + encounter );
				StaticEntity.getClient().registerEncounter( encounter );
				return encounter;
			}
		}
		else
		{
			Matcher encounterMatcher = Pattern.compile( "<center><b>(.*?)</b>" ).matcher( request.responseText );
			if ( encounterMatcher.find() )
			{
				String encounter = encounterMatcher.group(1);
				KoLmafiaCLI.printLine( "Encounter: " + encounter );
				StaticEntity.getClient().registerEncounter( encounter );
				return encounter;
			}
		}

		return "";
	}

	private static boolean containsEncounter( String formSource, String responseText )
	{
		// The first round is unique in that there is no
		// data fields.  Therefore, it will equal fight.php
		// exactly every single time.

		if ( formSource.startsWith( "fight.php" ) )
			return formSource.equals( "fight.php" );

		// All other adventures can be identified via their
		// form data and the place they point to.

		else if ( formSource.startsWith( "adventure.php" ) )
			return true;
		else if ( formSource.startsWith( "cave.php" ) && formSource.indexOf( "end" ) != -1 )
			return true;
		else if ( formSource.equals( "shore.php" ) && formSource.indexOf( "whichtrip" ) != -1 )
			return true;
		else if ( formSource.equals( "dungeon.php" ) && formSource.indexOf( "action" ) != -1 )
			return true;
		else if ( formSource.equals( "knob.php" ) && formSource.indexOf( "king" ) != -1 )
			return true;
		else if ( formSource.equals( "cyrpt.php" ) && formSource.indexOf( "action" ) != -1 )
			return true;
		else if ( formSource.equals( "rats.php" ) )
			return true;

		// It is not a known adventure.  Therefore,
		// do not log the encounter yet.

		return false;
	}

	public int getAdventuresUsed()
	{	return KoLmafia.permitsContinue() && ( responseText == null || responseText.indexOf( "oyster egg" ) == -1 ) ? adventuresUsed : 0;
	}

	public String toString()
	{	return adventureName;
	}
}
