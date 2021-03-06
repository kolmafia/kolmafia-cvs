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
import java.util.Map;
import java.util.TreeMap;

import java.io.BufferedReader;
import net.java.dev.spellcast.utilities.LockableListModel;

/**
 * A static class which retrieves all the adventures currently
 * available to <code>KoLmafia</code>.
 */

public class AdventureDatabase extends KoLDatabase
{
	private static LockableListModel adventures = new LockableListModel();

	private static final AdventureResult CASINO = new AdventureResult( 40, 1 );
	private static final AdventureResult DINGHY = new AdventureResult( 141, 1 );
	private static final AdventureResult SOCK = new AdventureResult( 609, 1 );
	private static final AdventureResult ROWBOAT = new AdventureResult( 653, 1 );
	private static final AdventureResult BEAN = new AdventureResult( 186, 1 );
	private static final AdventureResult ASTRAL = new AdventureResult( "Half-Astral", 0 );

	public static final Map ZONE_NAMES = new TreeMap();
	public static final Map ZONE_DESCRIPTIONS = new TreeMap();

	private static StringArray [] adventureTable = new StringArray[4];
	private static final Map areaCombatData = new TreeMap();
	private static final Map adventureLookup = new TreeMap();

	static
	{
		for ( int i = 0; i < 4; ++i )
			adventureTable[i] = new StringArray();

		AdventureDatabase.refreshZoneTable();
		AdventureDatabase.refreshAdventureTable();
		AdventureDatabase.refreshCombatsTable();
	}

	public static final void refreshZoneTable()
	{
		if ( !ZONE_NAMES.isEmpty() )
			return;

		BufferedReader reader = getReader( "zonelist.dat" );
		String [] data;

		while ( (data = readData( reader )) != null )
		{
			if ( data.length == 3 )
			{
				ZONE_NAMES.put( data[0], data[1] );
				ZONE_DESCRIPTIONS.put( data[0], data[2] );
			}
		}

		try
		{
			reader.close();
		}
		catch ( Exception e )
		{
			// This should not happen.  Therefore, print
			// a stack trace for debug purposes.

			StaticEntity.printStackTrace( e );
		}
	}

	public static final String [][][] CHOICE_ADVS =
	{
		// Lucky sewer options
		{ { "luckySewerAdventure" }, { "Sewer Gnomes" },
		  { "seal-clubbing club", "seal tooth", "helmet turtle", "scroll of turtle summoning", "pasta spoon", "ravioli hat",
		    "saucepan", "disco mask", "disco ball", "stolen accordion", "mariachi pants" } },

		// Choice 1 is unknown

		// Denim Axes Examined
		{ { "choiceAdventure2" }, { "Palindome" },
		  { "Trade for a denim axe", "Keep your rubber axe" } },

		// The Oracle Will See You Now
		// (This adventure cannot be customized)
		// { { "choiceAdventure3" }, { "Teleportitis" },
		//  { "Leave the oracle", "Pay for a minor consultation", "Pay for a major consultation" } },

		// Finger-Lickin'... Death.
		{ { "choiceAdventure4" }, { "South of the Border" },
		  { "Bet on Tapajunta Del Maiz", "Bet on Cuerno De...  the other one", "Walk away in disgust" } },

		// Heart of Very, Very Dark Darkness
		{ { "choiceAdventure5" }, { "Gravy Barrow 1" },
		  { "Enter the cave", "Don't enter the cave" } },

		// Choice 6 is unknown

		// How Depressing
		{ { "choiceAdventure7" }, { "Gravy Barrow 2" },
		  { "Put your hand in the depression", "Leave the cave" } },

		// On the Verge of a Dirge
		{ { "choiceAdventure8" }, { "Gravy Barrow 3" },
		  { "Enter the chamber", "Enter the chamber", "Enter the chamber" } },

		// Choices 9 - 13 are unknown

		// A Bard Day's Night
		{ { "choiceAdventure14" }, { "Knob Goblin Harem" },
		  { "Knob goblin harem veil", "Knob goblin harem pants", "100 meat", "Complete the outfit" },
		  { "306", "305", null } },

		// Yeti Nother Hippy
		{ { "choiceAdventure15" }, { "eXtreme Slope 1" },
		  { "eXtreme mittens", "eXtreme scarf", "200 meat", "Complete the outfit" },
		  { "399", "355", null } },

		// Saint Beernard
		{ { "choiceAdventure16" }, { "eXtreme Slope 2" },
		  { "snowboarder pants", "eXtreme scarf", "200 meat", "Complete the outfit" },
		  { "356", "355", null } },

		// Generic Teen Comedy
		{ { "choiceAdventure17" }, { "eXtreme Slope 3" },
		  { "eXtreme mittens", "snowboarder pants", "200 meat", "Complete the outfit" },
		  { "399", "356", null } },

		// A Flat Miner
		{ { "choiceAdventure18" }, { "Itznotyerzitz Mine 1" },
		  { "miner's pants", "7-Foot Dwarven mattock", "100 meat", "Complete the outfit" },
		  { "361", "362", null } },

		// 100% Legal
		{ { "choiceAdventure19" }, { "Itznotyerzitz Mine 2" },
		  { "miner's helmet", "miner's pants", "100 meat", "Complete the outfit" },
		  { "360", "361", null } },

		// See You Next Fall
		{ { "choiceAdventure20" }, { "Itznotyerzitz Mine 3" },
		  { "miner's helmet", "7-Foot Dwarven mattock", "100 meat", "Complete the outfit" },
		  { "360", "362", null } },

		// Under the Knife
		{ { "choiceAdventure21" }, { "Sleazy Back Alley" },
		  { "Switch Genders", "Umm. No thanks" } },

		// The Arrrbitrator
		{ { "choiceAdventure22" }, { "Pirate's Cove 1" },
		  { "eyepatch", "swashbuckling pants", "100 meat", "Complete the outfit" },
		  { "224", "402", null } },

		// Barrie Me at Sea
		{ { "choiceAdventure23" }, { "Pirate's Cove 2" },
		  { "stuffed shoulder parrot", "swashbuckling pants", "100 meat", "Complete the outfit" },
		  { "403", "402", null } },

		// Amatearrr Night
		{ { "choiceAdventure24" }, { "Pirate's Cove 3" },
		  { "stuffed shoulder parrot", "100 meat", "eyepatch", "Complete the outfit" },
		  { "403", null, "224" } },

		// Ouch! You bump into a door!
		{ { "choiceAdventure25" }, { "Dungeon of Doom" },
		  { "Buy a magic lamp", "Buy some sort of cloak", "Leave without buying anything" } },

		// Choices 26 - 39 are unknown

		// The Effervescent Fray
		{ { "choiceAdventure40" }, { "Cola Wars 1" },
		  { "Cloaca-Cola fatigues", "Dyspepsi-Cola shield", "15 Mysticality" },
		  { "1328", "1329", null } },

		// Smells Like Team Spirit
		{ { "choiceAdventure41" }, { "Cola Wars 2" },
		  { "Dyspepsi-Cola fatigues", "Cloaca-Cola helmet", "15 Muscle" },
		  { "1330", "1331", null } },

		// What is it Good For?
		{ { "choiceAdventure42" }, { "Cola Wars 3" },
		  { "Dyspepsi-Cola helmet", "Cloaca-Cola shield", "15 Moxie" },
		  { "1326", "1327", null } },

		// Choices 43 - 44 are unknown

		// Maps and Legends
		{ { "choiceAdventure45" }, { "Spooky Forest 1" },
		  { "Spooky Temple Map", "Ignore the monolith", "Nothing" } },

		// An Interesting Choice
		{ { "choiceAdventure46" }, { "Spooky Forest 2" },
		  { "Moxie", "Muscle", "Fight" } },

		// Have a Heart
		{ { "choiceAdventure47" }, { "Spooky Forest 3" },
		  { "Trade for used blood", "Keep your hearts" } },

		// Choices 48 - 70 are violet fog adventures
		// Choice 71 is a special adventure

		// Don't Fence Me In
		{ { "choiceAdventure73" }, { "Whitey's Grove 1" },
		  { "Muscle", "white picket fence", "piece of wedding cake" },
		  { null, "270", "262" } },

		// The Only Thing About Him is the Way That He Walks
		{ { "choiceAdventure74" }, { "Whitey's Grove 2" },
		  { "Moxie", "boxed wine", "mullet wig" },
		  { null, "1005", "267" } },

		// Rapido!
		{ { "choiceAdventure75" }, { "Whitey's Grove 3" },
		  { "Mysticality", "white lightning", "white collar" },
		  { null, "266", "1655" } }
	};

	// Some choice adventures have a choice that behaves as an "ignore"
	// setting: if you select it, no adventure is consumed.

	public static final String [][] IGNORABLE_CHOICES =
	{
		// Denim Axes Examined
		{ "choiceAdventure2", "2" },

		// The Oracle Will See You Now
		{ "choiceAdventure3", "3" },

		// Finger-Lickin'... Death.
		{ "choiceAdventure4", "3" },

		// Heart of Very, Very Dark Darkness
		{ "choiceAdventure5", "2" },

		// How Depressing
		{ "choiceAdventure7", "2" },

		// Wheel in the Clouds in the Sky, Keep on Turning
		{ "choiceAdventure9", "3" },
		{ "choiceAdventure10","3" },
		{ "choiceAdventure11", "3" },
		{ "choiceAdventure12", "3" },

		// Under the Knife
		{ "choiceAdventure21", "2" },

		// Ouch! You bump into a door!
		{ "choiceAdventure25", "3" },

		// Maps and Legends
		{ "choiceAdventure45", "2" },

		// Have a Heart
		{ "choiceAdventure47", "2" },
	};

	// Some choice adventures have options that cost meat

	public static final String [][] CHOICE_MEAT_COST =
	{
		// Finger-Lickin'... Death.
		{ "choiceAdventure4", "1", "500" },
		{ "choiceAdventure4", "2", "500" },

		// Under the Knife
		{ "choiceAdventure21", "1", "500" },

		// Ouch! You bump into a door!
		{ "choiceAdventure25", "1", "50" },
		{ "choiceAdventure25", "2", "5000" }
	};

	// Some adventures don't actually cost a turn
	public static final String [] FREE_ADVENTURES =
	{
		"Rock-a-bye larva",
		"Cobb's Knob lab key"
	};

	public static final void refreshAdventureTable()
	{
		BufferedReader reader = getReader( "adventures.dat" );

		for ( int i = 0; i < 4; ++i )
			adventureTable[i].clear();

		String [] data;

		while ( (data = readData( reader )) != null )
		{
			if ( data.length == 4 )
			{
				if ( data[1].indexOf( "send" ) != -1 )
					continue;

				String zone = (String) ZONE_NAMES.get( data[0] );

				// Be defensive: user can supply a broken data file
				if ( zone == null )
				{
					System.out.println( "Bad adventure zone: " + data[0] );
					continue;
				}

				adventureTable[0].add( zone );
				for ( int i = 1; i < 4; ++i )
					adventureTable[i].add( data[i] );
			}
		}

		try
		{
			reader.close();
		}
		catch ( Exception e )
		{
			// This should not happen.  Therefore, print
			// a stack trace for debug purposes.

			StaticEntity.printStackTrace( e );
		}
	}

	/**
	 * Returns the complete list of adventures available to the character
	 * based on the information provided by the given client.  Each element
	 * in this list is a <code>KoLAdventure</code> object.
	 */

	public static final LockableListModel getAsLockableListModel()
	{
		refreshAdventureList();
		return adventures;
	}

	public static void refreshAdventureList()
	{
		String [] zones = getProperty( "zoneExcludeList" ).split( "," );
		if ( zones.length == 1 && zones[0].length() == 0 )
			zones[0] = "-";

		boolean shouldAdd = true;
		String zoneName;

		adventures.clear();
		adventureLookup.clear();

		for ( int i = 0; i < adventureTable[1].size(); ++i )
		{
			shouldAdd = true;
			zoneName = (String) adventureTable[0].get(i);

			for ( int j = 0; j < zones.length && shouldAdd; ++j )
				if ( zoneName.equals( zones[j] ) )
					shouldAdd = false;

			KoLAdventure adventure = getAdventure(i);
			if ( shouldAdd )
				adventures.add( adventure );

			adventureLookup.put( adventure.getRequest().getURLString(), adventure );
		}

		if ( getProperty( "sortAdventures" ).equals( "true" ) )
			adventures.sort();
	}

	public static KoLAdventure getAdventureByURL( String adventureURL )
	{	return (KoLAdventure) adventureLookup.get( adventureURL );
	}

	/**
	 * Returns the first adventure in the database which contains the given
	 * substring in part of its name.
	 */

	public static KoLAdventure getAdventure( String adventureName )
	{
		String lowerCaseName = adventureName.toLowerCase();
		while ( lowerCaseName.indexOf( ":" ) != -1 )
			lowerCaseName = lowerCaseName.substring( lowerCaseName.indexOf( ":" ) + 1 );
		lowerCaseName = lowerCaseName.trim();

		int matchStartIndex;
		String currentTest;

		int bestMatchIndex = -1;
		int bestMatchLength = Integer.MAX_VALUE;
		int bestMatchStartIndex = Integer.MAX_VALUE;

		for ( int i = 0; i < adventureTable[3].size(); ++i )
		{
			currentTest = adventureTable[3].get(i).toLowerCase();
			matchStartIndex = currentTest.indexOf( lowerCaseName );

			if ( matchStartIndex != -1 )
			{
				if ( bestMatchIndex == -1 || matchStartIndex < bestMatchStartIndex ||
					(matchStartIndex == bestMatchStartIndex && currentTest.length() < bestMatchLength) )
				{
					bestMatchIndex = i;
					bestMatchStartIndex = matchStartIndex;
					bestMatchLength = currentTest.length();
				}
			}
		}

		return bestMatchIndex == -1 ? null : getAdventure( bestMatchIndex );
	}

	private static KoLAdventure getAdventure( int tableIndex )
	{
		return new KoLAdventure( client,
			adventureTable[0].get( tableIndex ), adventureTable[1].get( tableIndex ),
			adventureTable[2].get( tableIndex ), adventureTable[3].get( tableIndex ) );
	}

	/**
	 * Checks the map location of the given zone.  This is to ensure that
	 * KoLmafia arms any needed flags (such as for the beanstalk).
	 */

	public static void validateAdventure( KoLAdventure adventure )
	{
		KoLRequest request = null;

		String adventureID = adventure.getAdventureID();
		String formSource = adventure.getFormSource();

		// The beach is unlocked provided the player has the meat car
		// accomplishment and a meatcar in inventory.

		if ( formSource.equals( "shore.php" ) || adventureID.equals( "45" ) )
		{
			if ( !KoLmafia.permitsContinue() )
				return;

			// Obviate following request by checking accomplishment:
			// questlog.php?which=3
			// "You have built your own Bitchin' Meat Car."

			// Sometimes, the player has just built the meatcar and
			// visited the council -- check the main map to see if
			// the beach is unlocked.

			KoLmafia.updateDisplay( "Validating map location..." );
			request = new KoLRequest( client, "main.php" );
			request.run();

			if ( request.responseText.indexOf( "beach.php" ) == -1 )
			{
				KoLmafia.updateDisplay( ERROR_STATE, "Beach is not yet unlocked." );
				return;
			}

			return;
		}

		else if ( adventureID.equals( "60" ) || adventureID.equals( "61" ) || adventureID.equals( "62" ) || adventureID.equals( "63" ) || adventureID.equals( "64" ) )
		{
			// Obviate following request by checking accomplishment:
			// questlog.php?which=2
			// "You have learned how to hunt Yetis from the L337
			// Tr4pz0r."

			KoLmafia.updateDisplay( "Validating map location..." );
			// See if we can get to the location already
			request = new KoLRequest( client, "mclargehuge.php" );
			request.run();
			if ( request.responseText.indexOf( adventureID ) != -1 )
				return;

			// No. See if the trapper will give it to us
			request = new KoLRequest( client, "trapper.php" );
			request.run();

			// See if we can now get to the location
			request = new KoLRequest( client, "mclargehuge.php" );
		}

		else if ( adventureID.equals( "96" ) || adventureID.equals( "97" ) || adventureID.equals( "98" ) )
		{
			// You must be Half-Astral to go on a trip
			int astral = ASTRAL.getCount( ( KoLCharacter.getEffects() ) );
			if ( astral == 0 )
			{
				KoLmafia.updateDisplay( ERROR_STATE, "Eat an astral mushroom to take a trip." );
				return;
			}

			// If we haven't selected a trip yet, do so now
			if ( astral == 5)
			{
				String nextAdventure = getProperty( "nextAdventure" );
				if ( nextAdventure.indexOf( "An Incredibly Strange Place") == -1 )
				{
					String choice = "1";
					if ( adventureID.equals( "96" ) )
					{
						choice = "1";
						adventure = getAdventure( "Bad Trip" );
					}
					else if ( adventureID.equals( "98" ) )
					{
						choice = "2";
						adventure = getAdventure( "Mediocre Trip" );
					}
					else
					{
						choice = "3";
						adventure = getAdventure( "Great Trip" );
					}

					// Choose the trip
					String name = adventure.getAdventureName();
					StaticEntity.setProperty( "chosenTrip", name );

					StaticEntity.setProperty( "choiceAdventure71", choice );

					// Avoid infinite recursion
					StaticEntity.setProperty( "nextAdventure", name );

					// Arm the adventure by running it once to get
					// the "Journey to the Center of your Mind" choice.
					client.makeRequest( adventure, 1 );

					return;
				}
			}

			String chosenTrip = getProperty( "chosenTrip" );

			// If we've already selected a trip, we can't switch
			if ( !chosenTrip.equals( adventure.getAdventureName() ) )
			{
				KoLmafia.updateDisplay( ERROR_STATE, "You're already taking a different trip." );
				return;
			}

			return;
		}

		// The casino is unlocked provided the player
		// has a casino pass in their inventory.

		else if ( formSource.equals( "casino.php" ) || adventureID.equals( "70" ) || adventureID.equals( "71" ) )
		{
			retrieveItem( CASINO );
			return;
		}

		// The island is unlocked provided the player
		// has a dingy dinghy in their inventory.

		else if ( adventureID.equals( "26" ) || adventureID.equals( "65" ) || adventureID.equals( "27" ) || adventureID.equals( "29" ) || adventureID.equals( "66" ) || adventureID.equals( "67") )
		{
			retrieveItem( DINGHY );
			return;
		}

		// The Castle in the Clouds in the Sky is unlocked provided the
		// character has either a S.O.C.K. or an intragalactic rowboat

		else if ( adventureID.equals( "82" ) )
		{
			if ( KoLCharacter.hasItem( ROWBOAT, false ) )
				retrieveItem( ROWBOAT );
			else
				retrieveItem( SOCK );
			return;
		}

		// The Hole in the Sky is unlocked provided the player has an
		// intragalactic rowboat in their inventory.

		else if ( adventureID.equals( "83" ) )
		{
			retrieveItem( ROWBOAT );
			return;
		}

		// The beanstalk is unlocked when the player
		// has planted a beanstalk -- but, the zone
		// needs to be armed first.

		else if ( adventureID.equals( "81" ) && !KoLCharacter.beanstalkArmed() )
		{
			// If the character has a S.O.C.K. or an intragalactic
			// rowboat, they can get to the airship

			if ( KoLCharacter.hasItem( SOCK, false ) || KoLCharacter.hasItem( ROWBOAT, false ) )
				return;

			// Obviate following request by checking accomplishment:
			// questlog.php?which=3
			// "You have planted a Beanstalk in the Nearby Plains."

			request = new KoLRequest( client, "plains.php" );
			request.run();

			if ( request.responseText.indexOf( "beanstalk.php" ) == -1 )
			{
				// If not, check to see if the player has an enchanted
				// bean which can be used.  If they don't, then try to
				// find one through adventuring.

				if ( !KoLCharacter.hasItem( BEAN, false ) )
				{
					ArrayList temporary = new ArrayList();
					temporary.addAll( client.getConditions() );

					client.getConditions().clear();
					client.getConditions().add( BEAN );

					KoLAdventure beanbat = getAdventure( "beanbat" );
					client.makeRequest( beanbat, KoLCharacter.getAdventuresLeft() );

					if ( !client.getConditions().isEmpty() )
					{
						KoLmafia.updateDisplay( ERROR_STATE, "Unable to complete enchanted bean quest." );
						client.getConditions().clear();
						client.getConditions().addAll( temporary );
						return;
					}

					client.getConditions().clear();
					client.getConditions().addAll( temporary );
				}

				// Now that you've retrieved the bean, ensure that
				// it is in your inventory, and then use it.  Take
				// advantage of item consumption automatically doing
				// what's needed in grabbing the item.

				(new KoLRequest( client, "council.php" )).run();
				(new ConsumeItemRequest( client, BEAN )).run();
			}

			request = new KoLRequest( client, "beanstalk.php" );

			if ( KoLCharacter.getLevel() >= 10 )
				KoLCharacter.armBeanstalk();
		}

		// If you do not need to arm anything, then
		// return from this method.

		if ( request == null )
			return;

		KoLmafia.updateDisplay( "Validating map location..." );
		request.run();

		// Now that the zone is armed, check to see
		// if the adventure is even available.	If
		// it's not, cancel the request before it's
		// even made to minimize server hits.

		if ( request.responseText.indexOf( adventure.getAdventureID() ) == -1 )
		{
			KoLmafia.updateDisplay( ERROR_STATE, "This adventure is not yet unlocked." );
			return;
		}
	}

	/**
	 * Utility method which retrieves an item by calling a CLI
	 * command, which is constructed based on the parameters.
	 */

	private static final int retrieveItem( String command, LockableListModel source, AdventureResult item, int missingCount )
	{
		int retrieveCount = source == null ? missingCount : Math.min( missingCount, item.getCount( source ) );

		if ( retrieveCount > 0 )
		{
			DEFAULT_SHELL.executeLine( command + " " + retrieveCount + " " + item.getName() );
			return item.getCount() - item.getCount( KoLCharacter.getInventory() );
		}

		return missingCount;
	}

	/**
	 * Utility method which creates an item by invoking the
	 * appropriate CLI command.
	 */

	private static final void retrieveItem( ItemCreationRequest item, int missingCount )
	{
		int createCount = Math.min( missingCount, item.getCount( ConcoctionsDatabase.getConcoctions() ) );

		if ( createCount > 0 )
		{
			DEFAULT_SHELL.executeLine( "make " + createCount + " " + item.getName() );
			return;
		}
	}

	public static final void retrieveItem( AdventureResult item )
	{
		try
		{
			int missingCount = item.getCount() - item.getCount( KoLCharacter.getInventory() );

			// If you already have enough of the given item, then
			// return from this method.

			if ( missingCount <= 0 )
				return;

			// Next, if you have a piece of equipment, then
			// assume this might be a check on equipment.
			// in this case, return from this method.

			if ( KoLCharacter.hasEquipped( item ) )
			{
				if ( missingCount == 1 )
					return;

				while ( KoLCharacter.hasEquipped( item ) )
					DEFAULT_SHELL.executeLine( "unequip " + item.getName() );

				missingCount = item.getCount() - item.getCount( KoLCharacter.getInventory() );
			}

			if ( missingCount <= 0 )
				return;

			// First, attempt to pull the item from the closet.
			// If this is successful, return from the method.

			missingCount = retrieveItem( "closet take", KoLCharacter.getCloset(), item, missingCount );

			if ( missingCount <= 0 )
				return;

			// Next, attempt to create the item from existing
			// ingredients (if possible).

			ItemCreationRequest creator = ItemCreationRequest.getInstance( client, item.getItemID(), missingCount );
			if ( creator != null )
			{
				if ( ConcoctionsDatabase.getMixingMethod( item.getItemID() ) == ItemCreationRequest.NOCREATE ||
					ConcoctionsDatabase.hasAnyIngredient( item.getItemID() ) )
				{
					retrieveItem( creator, missingCount );
					missingCount = item.getCount() - item.getCount( KoLCharacter.getInventory() );

					if ( missingCount <= 0 )
						return;
				}
			}

			// Next, hermit item retrieval is possible when
			// you have worthless items.  Use this method next.

			if ( client.hermitItems.contains( item.getName() ) )
			{
				int worthlessItemCount = HermitRequest.getWorthlessItemCount();
				if ( worthlessItemCount > 0 )
					(new HermitRequest( client, item.getItemID(), Math.min( worthlessItemCount, missingCount ) )).run();

				missingCount = item.getCount() - item.getCount( KoLCharacter.getInventory() );

				if ( missingCount <= 0 )
					return;
			}

			// Next, attempt to pull the items out of storage,
			// if you are out of ronin.

			if ( KoLCharacter.canInteract() )
				missingCount = retrieveItem( "hagnk", KoLCharacter.getStorage(), item, missingCount );

			if ( missingCount <= 0 )
				return;

			// Try to purchase the item from the mall, if the
			// user wishes to autosatisfy through purchases,
			// and the item is not creatable through combines.

			int price = TradeableItemDatabase.getPriceByID( item.getItemID() );

			boolean shouldPurchase = price != 0 && price != -1;
			boolean canUseNPCStore = NPCStoreDatabase.contains( item.getName() );

			boolean shouldAutoSatisfyEarly = canUseNPCStore || !ConcoctionsDatabase.hasAnyIngredient( item.getItemID() );
			boolean shouldUseMall = getProperty( "autoSatisfyChecks" ).equals( "true" );

			switch ( ConcoctionsDatabase.getMixingMethod( item.getItemID() ) )
			{
				case ItemCreationRequest.COOK:
				case ItemCreationRequest.COOK_REAGENT:
				case ItemCreationRequest.SUPER_REAGENT:
				case ItemCreationRequest.COOK_PASTA:
				case ItemCreationRequest.MIX:
				case ItemCreationRequest.MIX_SPECIAL:
				case ItemCreationRequest.MIX_SUPER:
				case ItemCreationRequest.PIXEL:

					shouldAutoSatisfyEarly = true;
			}

			if ( shouldPurchase && shouldAutoSatisfyEarly )
			{
				if ( canUseNPCStore || (KoLCharacter.canInteract() && shouldUseMall) )
					missingCount = retrieveItem( "buy", null, item, missingCount );
			}

			if ( missingCount <= 0 )
				return;

			// Finally, if it's creatable, rather than seeing
			// what main ingredient is missing, show what
			// sub-ingredients are missing; but only do this
			// if it's not a clover or a wad of dough, which
			// causes infinite recursion.

			if ( creator != null )
			{
				switch ( item.getItemID() )
				{
					case 24:
					case 196:
					case 159:
					case 301:

						break;

					default:

						creator.setQuantityNeeded( missingCount );
						creator.run();
						return;
				}
			}

			// Try to purchase the item from the mall, if the
			// user wishes to autosatisfy through purchases,
			// but only for combinable items (non-combinables
			// would have been handled earlier).

			if ( shouldPurchase && !shouldAutoSatisfyEarly )
			{
				if ( KoLCharacter.canInteract() && shouldUseMall )
					missingCount = retrieveItem( "buy", null, item, missingCount );
			}

			if ( missingCount <= 0 )
				return;

			// If the item does not exist in sufficient quantities,
			// then notify the client that there aren't enough items
			// available to continue and cancel the request.

			KoLmafia.updateDisplay( ERROR_STATE, "You need " + missingCount + " more " + item.getName() + " to continue." );
		}
		catch ( Exception e )
		{
			// This should not happen.  Therefore, print
			// a stack trace for debug purposes.

			StaticEntity.printStackTrace( e );
		}
	}

	public static String ignoreChoiceOption( String choice )
	{
		for ( int i = 0; i < IGNORABLE_CHOICES.length; ++i )
			if ( choice.equals( IGNORABLE_CHOICES[i][0] ) )
				return IGNORABLE_CHOICES[i][1];
		return null;
	}

	public static boolean consumesAdventure( String choice, String decision )
	{
		// See if it's a free movement in the violet fog
		if ( VioletFog.freeAdventure( choice, decision ) )
			return false;

		for ( int i = 0; i < IGNORABLE_CHOICES.length; ++i )
			if ( choice.equals( IGNORABLE_CHOICES[i][0] ) )
				return !decision.equals( IGNORABLE_CHOICES[i][1] );
		return true;
	}

	public static int consumesMeat( String choice, String decision )
	{
		for ( int i = 0; i < CHOICE_MEAT_COST.length; ++i )
			if ( choice.equals( CHOICE_MEAT_COST[i][0] ) &&
			     decision.equals( CHOICE_MEAT_COST[i][1] ) )
				return StaticEntity.parseInt( CHOICE_MEAT_COST[i][2] );
		return 0;
	}

	public static boolean freeAdventure( String text )
	{
		for ( int i = 0; i < FREE_ADVENTURES.length; ++i )
			if ( text.indexOf( FREE_ADVENTURES[i] ) != -1 )
				return true;
		return false;
	}

	public static final void refreshCombatsTable()
	{
		areaCombatData.clear();

		BufferedReader reader = getReader( "combats.dat" );
		String [] data;

		String [] adventures = adventureTable[3].toArray();

		while ( (data = readData( reader )) != null )
		{
			if ( data.length > 2 )
			{
				if ( !validateAdventureArea( data[0], adventures ) )
				{
					System.out.println( "Invalid adventure area: \"" + data[0] + "\"" );
					continue;
				}

				int combats = StaticEntity.parseInt( data[1] );
				AreaCombatData combat = new AreaCombatData( combats );

				for ( int i = 2; i < data.length; ++i )
					combat.addMonster( data[i] );

				areaCombatData.put( data[0], combat );
			}
		}

		try
		{
			reader.close();
		}
		catch ( Exception e )
		{
			// This should not happen.  Therefore, print
			// a stack trace for debug purposes.

			StaticEntity.printStackTrace( e );
		}
	}

	private static boolean validateAdventureArea( String area, String [] areas )
	{
		for ( int i = 0; i < areas.length; ++i )
			if ( area.equals( areas[i] ) )
			     return true;
		return false;
	}

	public static AreaCombatData getAreaCombatData( String area )
	{
		// Strip out zone name if present
		int index = area.indexOf( ":" );
		if ( index != -1 )
			area = area.substring( index + 2 );

		// Get the combat data
		return (AreaCombatData) areaCombatData.get( area );
	}
}
