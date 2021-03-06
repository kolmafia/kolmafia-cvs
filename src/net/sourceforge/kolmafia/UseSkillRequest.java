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

public class UseSkillRequest extends KoLRequest implements Comparable
{
	private static final int OTTER_TONGUE = 1007;
	private static final int WALRUS_TONGUE = 1010;
	protected static String lastUpdate = "";

	private int skillID;
	private String skillName;
	private String target;
	private int buffCount;
	private String countFieldID;

	private static final AdventureResult ACCORDION = new AdventureResult( 11, 1 );
	public static final AdventureResult ROCKNROLL_LEGEND = new AdventureResult( 50, 1 );
	public static final AdventureResult WIZARD_HAT = new AdventureResult( 1653, 1 );

	private static final AdventureResult ROLL = new AdventureResult( 47, 1 );
	private static final AdventureResult BIG_ROCK = new AdventureResult( 30, 1 );
	private static final AdventureResult HEART = new AdventureResult( 48, 1 );

	// Clover weapons
	private static final AdventureResult BJORNS_HAMMER = new AdventureResult( 32, 1 );
	private static final AdventureResult TURTLESLINGER = new AdventureResult( 60, 1 );
	private static final AdventureResult PASTA_OF_PERIL = new AdventureResult( 68, 1 );
	private static final AdventureResult FIVE_ALARM_SAUCEPAN = new AdventureResult( 57, 1 );
	private static final AdventureResult DISCO_BANJO = new AdventureResult( 54, 1 );

	private static final AdventureResult [] CLOVER_WEAPONS = { BJORNS_HAMMER, TURTLESLINGER, FIVE_ALARM_SAUCEPAN, PASTA_OF_PERIL, DISCO_BANJO };

	/**
	 * Constructs a new <code>UseSkillRequest</code>.
	 * @param	client	The client to be notified of completion
	 * @param	skillName	The name of the skill to be used
	 * @param	target	The name of the target of the skill
	 * @param	buffCount	The number of times the target is affected by this skill
	 */

	public UseSkillRequest( KoLmafia client, String skillName, String target, int buffCount )
	{
		super( client, "skills.php" );
		addFormField( "action", "Skillz." );
		addFormField( "pwd" );

		this.skillID = ClassSkillsDatabase.getSkillID( skillName );
		this.skillName = ClassSkillsDatabase.getSkillName( skillID );
		addFormField( "whichskill", String.valueOf( skillID ) );

		if ( ClassSkillsDatabase.isBuff( skillID ) )
		{
			this.countFieldID = "bufftimes";

			if ( target == null || target.trim().length() == 0 || target.equals( String.valueOf( KoLCharacter.getUserID() ) ) || target.equals( KoLCharacter.getUsername() ) )
			{
				this.target = "yourself";
				if ( KoLCharacter.getUserID() != 0 )
					addFormField( "targetplayer", String.valueOf( KoLCharacter.getUserID() ) );
				else
					addFormField( "specificplayer", KoLCharacter.getUsername() );
			}
			else
			{
				this.target = target;
				addFormField( "specificplayer", target );
			}
		}
		else
		{
			this.countFieldID = "quantity";
			this.target = null;
		}

		for ( int i = 0; i < KoLmafia.BREAKFAST_SKILLS.length; ++i )
			if ( this.skillName.equals( KoLmafia.BREAKFAST_SKILLS[i][0] ) )
				buffCount = Math.min( StaticEntity.parseInt( KoLmafia.BREAKFAST_SKILLS[i][1] ), buffCount );

		if ( buffCount < 1 )
			buffCount = 1;
		else if ( buffCount == Integer.MAX_VALUE )
			buffCount = (int) (KoLCharacter.getMaximumMP() / ClassSkillsDatabase.getMPConsumptionByID( skillID ));

		this.buffCount = buffCount;
	}

	public int compareTo( Object o )
	{
		if ( o == null || !(o instanceof UseSkillRequest) )
			return -1;

		int mpDifference = ClassSkillsDatabase.getMPConsumptionByID( skillID ) -
			ClassSkillsDatabase.getMPConsumptionByID( ((UseSkillRequest)o).skillID );

		return mpDifference != 0 ? mpDifference : skillName.compareToIgnoreCase( ((UseSkillRequest)o).skillName );
	}

	public int getSkillID()
	{	return skillID;
	}

	public String getSkillName()
	{	return skillName;
	}

	public String toString()
	{	return skillName + " (" + ClassSkillsDatabase.getMPConsumptionByID( skillID ) + " mp)";
	}

	public static AdventureResult optimizeEquipment( int skillID )
	{
		AdventureResult songWeapon = null;

		if ( skillID > 6000 && skillID < 7000 )
		{
			songWeapon = prepareAccordion();

			if ( songWeapon == null )
			{
				KoLmafia.updateDisplay( ERROR_STATE, "You need an accordion to play Accordion Thief songs." );
				return null;
			}

			// If there's a stolen accordion equipped, unequip it so the
			// Rock and Roll Legend in inventory is used to play the song

			if ( songWeapon != ACCORDION && KoLCharacter.hasEquipped( ACCORDION ) )
				(new EquipmentRequest( StaticEntity.getClient(), EquipmentRequest.UNEQUIP, KoLCharacter.WEAPON )).run();
		}

		if ( ClassSkillsDatabase.isBuff( skillID ) && KoLCharacter.getInventory().contains( WIZARD_HAT ) )
			(new EquipmentRequest( StaticEntity.getClient(), WIZARD_HAT.getName(), KoLCharacter.HAT )).run();

		return songWeapon;
	}

	public void run()
	{
		if ( !KoLCharacter.hasSkill( skillName ) )
			return;

		String initialWeapon = KoLCharacter.getEquipment( KoLCharacter.WEAPON );
		String initialOffhand = KoLCharacter.getEquipment( KoLCharacter.OFFHAND );
		String initialHat = KoLCharacter.getEquipment( KoLCharacter.HAT );

		// Cast the skill as many times as needed

		AdventureResult songWeapon = optimizeEquipment( skillID );
		useSkillLoop();

		if ( !KoLmafia.isRunningBetweenBattleChecks() )
			restoreEquipment( songWeapon, initialWeapon, initialOffhand, initialHat );
	}

	public static void restoreEquipment( AdventureResult songWeapon, String initialWeapon, String initialOffhand, String initialHat )
	{
		// If we untinkered a Clover Weapon and built a Rock and Roll
		// Legend, undo it all.

		if ( songWeapon != null && songWeapon != ACCORDION && songWeapon != ROCKNROLL_LEGEND )
		{
			// Untinker the Rock and Roll Legend we constructed and,
			// rebuild the weapon we started with, but only if that
			// weapon was equipped!

			if ( initialWeapon != null && initialWeapon.equalsIgnoreCase( songWeapon.getName() ) )
			{
				untinkerCloverWeapon( ROCKNROLL_LEGEND );
				ItemCreationRequest.getInstance( StaticEntity.getClient(), songWeapon ).run();
			}
		}

		// If we unequipped a weapon, equip it again
		if ( initialWeapon != null && !initialWeapon.equals( KoLCharacter.getEquipment( KoLCharacter.WEAPON ) ) )
			(new EquipmentRequest( StaticEntity.getClient(), initialWeapon, KoLCharacter.WEAPON )).run();

		// If we unequipped an off-hand weapon, equip it again
		if ( initialOffhand != null && !initialOffhand.equals( KoLCharacter.getEquipment( KoLCharacter.OFFHAND ) ) )
			(new EquipmentRequest( StaticEntity.getClient(), initialOffhand, KoLCharacter.OFFHAND )).run();

		// If we unequipped a hat, equip it again
		if ( initialHat != null && !initialHat.equals( KoLCharacter.getEquipment( KoLCharacter.HAT ) ) )
			(new EquipmentRequest( StaticEntity.getClient(), initialHat, KoLCharacter.HAT )).run();
	}

	private void useSkillLoop()
	{
		// Before executing the skill, ensure that all necessary mana is
		// recovered in advance.

		int castsRemaining = buffCount;
		int mpPerCast = ClassSkillsDatabase.getMPConsumptionByID( skillID );

		int currentMP = KoLCharacter.getCurrentMP();
		int maximumMP = KoLCharacter.getMaximumMP();

		if ( KoLmafia.refusesContinue() || maximumMP < mpPerCast )
			return;

		int currentCast = 0;
		int maximumCast = maximumMP / mpPerCast;

		while ( !KoLmafia.refusesContinue() && castsRemaining > 0 )
		{
			// Find out how many times we can cast with current MP

			currentCast = Math.min( castsRemaining, KoLCharacter.getCurrentMP() / mpPerCast );

			// If none, attempt to recover MP in order to cast;
			// take auto-recovery into account.

			if ( currentCast == 0 )
			{
				currentCast = Math.min( castsRemaining, maximumCast );

				currentMP = KoLCharacter.getCurrentMP();
				client.recoverMP( mpPerCast * currentCast );

				// If no change occurred, that means the person was
				// unable to recover MP; abort the process.

				if ( currentMP == KoLCharacter.getCurrentMP() )
					return;

				currentCast = Math.min( castsRemaining, KoLCharacter.getCurrentMP() / mpPerCast );
			}

			if ( KoLmafia.refusesContinue() )
				return;

			if ( currentCast > 0 )
			{
				// Attempt to cast the buff.  In the event that it
				// fails, make sure to report it and return whether
				// or not at least one cast was completed.

				addFormField( countFieldID, String.valueOf( currentCast ), false );

				if ( target == null || target.trim().length() == 0 )
					KoLmafia.updateDisplay( "Casting " + skillName + " " + currentCast + " times..." );
				else
					KoLmafia.updateDisplay( "Casting " + skillName + " on " + target + " " + currentCast + " times..." );

				super.run();

				// Otherwise, you have completed the correct number
				// of casts.  Deduct it from the number of casts
				// remaining and continue.

				castsRemaining -= currentCast;
			}
		}
	}

	public static AdventureResult prepareAccordion()
	{
		// Can the rock and roll legend be acquired in some way
		// right now?  If so, retrieve it.

		if ( KoLCharacter.hasItem( ROCKNROLL_LEGEND, true ) )
		{
			if ( !KoLCharacter.hasEquipped( ROCKNROLL_LEGEND ) )
				AdventureDatabase.retrieveItem( ROCKNROLL_LEGEND );

			return ROCKNROLL_LEGEND;
		}

		if ( KoLCharacter.canInteract() )
		{
			DEFAULT_SHELL.executeLine( "buy " + ROCKNROLL_LEGEND.getName() );
			return ROCKNROLL_LEGEND;
		}

		// He must have at least a stolen accordion

		if ( !KoLCharacter.hasItem( ACCORDION, false ) )
			return null;

		// Does he have a hot buttered roll?  If not,
		// untinkering weapons won't help.

		if ( !KoLCharacter.hasItem( ROLL, false ) )
			return ACCORDION;

		// Can we get a big rock from a clover weapon?

		AdventureResult cloverWeapon = null;
		for ( int i = 0; i < CLOVER_WEAPONS.length; ++i )
			if ( KoLCharacter.hasItem( CLOVER_WEAPONS[i], false ) )
				cloverWeapon = CLOVER_WEAPONS[i];

		// If not, just use existing stolen accordion

		if ( cloverWeapon == null )
			return ACCORDION;

		// If he's already helped the Untinker, cool - but we don't
		// want to run adventures to fulfill that quest.

		if ( !canUntinker() )
			return ACCORDION;

		// Unequip the clover weapon, if necessary, and turn the
		// clover weapon into a big rock.  Then, build a rock and
		// roll legend.

		AdventureDatabase.retrieveItem( cloverWeapon );
		untinkerCloverWeapon( cloverWeapon );

		AdventureDatabase.retrieveItem( ROCKNROLL_LEGEND );
		return cloverWeapon;
	}

	private static boolean canUntinker()
	{
		// If you're too low level, don't even try

		if ( KoLCharacter.getLevel() < 4 )
			return false;

		// If you're in a muscle sign, KoLmafia will finish the
		// quest without problems.

		if ( KoLCharacter.inMuscleSign() )
			return true;

		// Otherwise, visit the untinker and see what he says.
		// If he mentions Degrassi Knoll, you haven't given him
		// his screwdriver yet.

		KoLRequest questCompleter = new UntinkerRequest( StaticEntity.getClient() );
		questCompleter.run();
		return questCompleter.responseText.indexOf( "Degrassi Knoll" ) == -1;
	}

	public static void untinkerCloverWeapon( AdventureResult item )
	{
		switch ( item.getItemID() )
		{
			case 32:	// Bjorn's Hammer
				( new UntinkerRequest( StaticEntity.getClient(), 32 ) ).run();
				( new UntinkerRequest( StaticEntity.getClient(), 31 ) ).run();
				break;
			case 50:	// Rock and Roll Legend
				( new UntinkerRequest( StaticEntity.getClient(), 50 ) ).run();
				( new UntinkerRequest( StaticEntity.getClient(), 48 ) ).run();
				break;
			case 54:	// Disco Banjo
				( new UntinkerRequest( StaticEntity.getClient(), 54 ) ).run();
				( new UntinkerRequest( StaticEntity.getClient(), 53 ) ).run();
				break;
			case 57:	// 5-Alarm Saucepan
				( new UntinkerRequest( StaticEntity.getClient(), 57 ) ).run();
				( new UntinkerRequest( StaticEntity.getClient(), 56 ) ).run();
				break;
			case 60:	// Turtleslinger
				( new UntinkerRequest( StaticEntity.getClient(), 60 ) ).run();
				( new UntinkerRequest( StaticEntity.getClient(), 58 ) ).run();
				break;
			case 68:	// Pasta of Peril
				( new UntinkerRequest( StaticEntity.getClient(), 68 ) ).run();
				( new UntinkerRequest( StaticEntity.getClient(), 67 ) ).run();
				break;
		}
	}

	protected void processResults()
	{
		boolean encounteredError = false;

		// If a reply was obtained, check to see if it was a success message
		// Otherwise, try to figure out why it was unsuccessful.

		if ( responseText == null || responseText.trim().length() == 0 )
		{
			encounteredError = true;
			lastUpdate = "Encountered lag problems.";
		}
		else if ( responseText.indexOf( "You don't have that skill" ) != -1 )
		{
			encounteredError = true;
			lastUpdate = "That skill is unavailable.";
		}
		else if ( responseText.indexOf( "You don't have enough" ) != -1 )
		{
			encounteredError = true;
			lastUpdate = "Not enough mana to continue.";
		}
		else if ( responseText.indexOf( "You can only conjure" ) != -1 ||
			  responseText.indexOf( "You can only scrounge up" ) != -1 ||
			  responseText.indexOf( "You can only summon" ) != -1 )
		{
			// If it's a buff count greater than one,
			// try to scale down the request.

			if ( buffCount > 1 )
			{
				--this.buffCount;
				this.run();
				return;
			}

			encounteredError = true;
			lastUpdate = "Summon limit exceeded.";
		}
		else if ( responseText.indexOf( "too many songs" ) != -1 )
		{
			encounteredError = true;
			lastUpdate = target + " has 3 AT buffs already.";
		}
		else if ( responseText.indexOf( "casts left of the Smile of Mr. A" ) != -1 )
		{
			encounteredError = true;
			lastUpdate = "You cannot cast that many smiles.";
		}
		else if ( responseText.indexOf( "Invalid target player" ) != -1 )
		{
			encounteredError = true;
			lastUpdate = target + " is not a valid target.";
		}
		else if ( responseText.indexOf( "busy fighting" ) != -1 )
		{
			encounteredError = true;
			lastUpdate = target + " is busy fighting.";
		}
		else if ( responseText.indexOf( "receive buffs" ) != -1 )
		{
			encounteredError = true;
			lastUpdate = target + " cannot receive buffs.";
		}
		else if ( responseText.indexOf( "accordion equipped" ) != -1 )
		{
			// "You need to have an accordion equipped or in your
			// inventory if you want to play that song."

			encounteredError = true;
			lastUpdate = "You need an accordion to play Accordion Thief songs.";
		}
		else
		{
			lastUpdate = "";
		}

		// Now that all the checks are complete, proceed
		// to determine how to update the user display.

		if ( encounteredError )
		{
			KoLmafia.updateDisplay( PENDING_STATE, lastUpdate );

			if ( BuffBotHome.isBuffBotActive() )
				BuffBotHome.timeStampedLogEntry( BuffBotHome.ERRORCOLOR, lastUpdate );
		}
		else
		{
			if ( target == null )
				KoLmafia.updateDisplay( skillName + " was successfully cast." );
			else
				KoLmafia.updateDisplay( skillName + " was successfully cast on " + target + "." );

			// Tongue of the Walrus (1010) automatically
			// removes any beaten up.

			client.processResult( new AdventureResult( AdventureResult.MP, 0 - (ClassSkillsDatabase.getMPConsumptionByID( skillID ) * buffCount) ) );
			client.applyRecentEffects();

			if ( skillID == OTTER_TONGUE || skillID == WALRUS_TONGUE )
			{
				KoLCharacter.getEffects().remove( KoLAdventure.BEATEN_UP );
				needsRefresh = true;
			}

			super.processResults();
		}
	}

	public boolean equals( Object o )
	{	return o != null && o instanceof UseSkillRequest && getSkillName().equals( ((UseSkillRequest)o).getSkillName() );
	}

	public String getCommandForm( int iterations )
	{	return "cast " + buffCount + " " + skillName;
	}
}
