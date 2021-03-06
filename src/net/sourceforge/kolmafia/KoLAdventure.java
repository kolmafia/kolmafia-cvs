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

import java.io.File;
import java.io.FileInputStream;

/**
 * An auxiliary class which stores runnable adventures so that they
 * can be created directly from a database.  Encapsulates the nature
 * of the adventure so that they can be easily listed inside of a
 * <code>ListModel</code>, with the potential to be extended to fit
 * other requests to the Kingdom of Loathing which need to be stored
 * within a database.
 */

public class KoLAdventure implements Runnable, KoLConstants, Comparable
{
	private static final AdventureResult WAND = new AdventureResult( 626, 1 );
	public static final AdventureResult BEATEN_UP = new AdventureResult( "Beaten Up", 1, true );

	private KoLmafia client;
	private String zone, adventureID, formSource, adventureName;
	private KoLRequest request;
	private AreaCombatData areaSummary;

	/**
	 * Constructs a new <code>KoLAdventure</code> with the given
	 * specifications.
	 *
	 * @param	client	The client to which the results of the adventure are reported
	 * @param	formSource	The form associated with the given adventure
	 * @param	adventureID	The identifier for this adventure, relative to its form
	 * @param	adventureName	The string form, or name of this adventure
	 */

	public KoLAdventure( KoLmafia client, String zone, String formSource, String adventureID, String adventureName )
	{
		this.client = client;
		this.zone = zone;
		this.formSource = formSource;
		this.adventureID = adventureID;
		this.adventureName = adventureName;

		if ( formSource.equals( "sewer.php" ) )
			this.request = new SewerRequest( client, false );
		else if ( formSource.equals( "luckysewer.php" ) )
			this.request = new SewerRequest( client, true );
		else if ( formSource.equals( "campground.php" ) )
			this.request = new CampgroundRequest( client, adventureID );
		else if ( formSource.equals( "clan_gym.php" ) )
			this.request = new ClanGymRequest( client, StaticEntity.parseInt( adventureID ) );
		else
			this.request = new AdventureRequest( client, adventureName, formSource, adventureID );

		this.areaSummary = AdventureDatabase.getAreaCombatData( adventureName );
	}

	/**
	 * Returns the form source for this adventure.
	 */

	public String getFormSource()
	{	return formSource;
	}

	/**
	 * Returns the name of this adventure.
	 */

	public String getAdventureName()
	{	return adventureName;
	}

	/**
	 * Returns the adventure ID for this adventure.
	 * @return	The adventure ID for this adventure
	 */

	public String getAdventureID()
	{	return adventureID;
	}

	/**
	 * Returns the request associated with this adventure.
	 * @return	The request for this adventure
	 */

	public KoLRequest getRequest()
	{	return request;
	}

	/**
	 * Retrieves the string form of the adventure contained within this
	 * encapsulation, which is generally the name of the adventure.
	 *
	 * @return	The string form of the adventure
	 */

	public String toString()
	{
		boolean includeZoneName = StaticEntity.getProperty( "showAdventureZone" ).equals( "true" );
		return includeZoneName ? zone + ": " + adventureName : adventureName;
	}

	/**
	 * Executes the appropriate <code>KoLRequest</code> for the adventure
	 * encapsulated by this <code>KoLAdventure</code>.
	 */

	public void run()
	{
		if ( getFormSource().equals( "shore.php" ) && KoLCharacter.getAvailableMeat() < 500 )
		{
			KoLmafia.updateDisplay( ERROR_STATE, "Insufficient funds for shore vacation." );
			return;
		}

		KoLCharacter.setNextAdventure( this );

		String action = CombatSettings.getShortCombatOptionName( StaticEntity.getProperty( "battleAction" ) );
		int haltTolerance = (int)( StaticEntity.parseDouble( StaticEntity.getProperty( "battleStop" ) ) * (double) KoLCharacter.getMaximumHP() );

		if ( ( action.equals( "item536" ) && FightRequest.DICTIONARY1.getCount( KoLCharacter.getInventory() ) < 1 ) ||
			 ( action.equals( "item1316" ) && FightRequest.DICTIONARY2.getCount( KoLCharacter.getInventory() ) < 1 ) )
		{
			KoLmafia.updateDisplay( ERROR_STATE, "Sorry, you don't have a dictionary." );
			KoLCharacter.setNextAdventure( null );
			return;
		}

		// If auto-recovery failed, return from the run attempt.
		// This prevents other messages from overriding the actual
		// error message.

		client.runBetweenBattleChecks();
		if ( !KoLmafia.permitsContinue() )
			return;

		if ( haltTolerance >= 0 && KoLCharacter.getCurrentHP() <= haltTolerance && !(request instanceof CampgroundRequest) )
		{
			KoLmafia.updateDisplay( ABORT_STATE, "Insufficient health to continue (auto-abort triggered)." );
			KoLCharacter.setNextAdventure( null );
			return;
		}

		// Make sure there are enough adventures to run the request
		// so that you don't spam the server unnecessarily.

		if ( KoLCharacter.getAdventuresLeft() == 0 || KoLCharacter.getAdventuresLeft() < request.getAdventuresUsed() )
		{
			KoLmafia.updateDisplay( ERROR_STATE, "Insufficient adventures to continue." );
			KoLCharacter.setNextAdventure( null );
			return;
		}

		// Check for dictionaries as a battle strategy, if the
		// person is not adventuring at the chasm.

		if ( !adventureID.equals( "80" ) && request instanceof AdventureRequest &&
			request.getAdventuresUsed() == 1 && (action.equals( "item536" ) || action.equals( "item1316" )) &&
			KoLCharacter.getFamiliar().getID() != 16 && KoLCharacter.getFamiliar().getID() != 17 && KoLCharacter.getFamiliar().getID() != 48 )
		{
			KoLmafia.updateDisplay( ERROR_STATE, "A dictionary would be useless there." );
			KoLCharacter.setNextAdventure( null );
			return;
		}

		// If the person doesn't stand a chance of surviving,
		// automatically quit and tell them so.

		if ( action.equals( "attack" ) && areaSummary != null && !areaSummary.willHitSomething() )
		{
			KoLmafia.updateDisplay( ABORT_STATE, "You don't stand a chance." );
			return;
		}

		// If the test is successful, then it is safe to run the
		// request (without spamming the server).

		recordToSession();
		request.run();
		client.runBetweenBattleChecks();
	}

	public void recordToSession()
	{
		KoLmafia.getSessionStream().println();
		KoLmafia.getSessionStream().println( "[" + (KoLCharacter.getTotalTurnsUsed() + 1) + "] " + getAdventureName() );
		client.registerAdventure( this );
	}

	public int compareTo( Object o )
	{
		return ( o == null || !( o instanceof KoLAdventure ) ) ? 1 :
			compareTo( (KoLAdventure) o );
	}

	public int compareTo( KoLAdventure ka )
	{	return toString().compareToIgnoreCase( ka.toString() );
	}
}
