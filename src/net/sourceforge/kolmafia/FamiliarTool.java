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

// utilities
import java.util.List;

/**
 * Xylpher's familiar training tool
 */

public class FamiliarTool
{
	// Array of current five opponents
	private static Opponent [] opponents = new Opponent[5];
	
	// Index of best opponent to fight
	private static int bestOpponent;

	// Index of best arena match against that opponent
	private static int bestMatch;

	// Best weight for own familiar during that match
	private static int bestWeight;

	// Difference from "perfect" weight for that match
	private static int difference;

	/**
	 * Initializes Familiar Tool with all Arena Data
	 * @param	opponents	size 5 array with IDs of all opponents. The index of each opponent will be re-used as a return value
	 * @param	opponentWeights	size 5 array with the Weights of all opponents. Indexing corresponds with opponents parameter
	 */
	public FamiliarTool( List opponents )
	{
		int opponentCount = opponents.size();
		for ( int i = 0; i < opponentCount; ++i )
		{
			CakeArenaManager.ArenaOpponent opponent = (CakeArenaManager.ArenaOpponent)opponents.get( i );
			int id = FamiliarsDatabase.getFamiliarID( opponent.getRace() );
			int weight = opponent.getWeight();
			
			this.opponents[i] = new Opponent( id, weight );
		}
	}

	/**
	 * Runs all the calculation to determine the best matchup for a familiar
	 * @param	ownFamiliar		ID of the familiar to calculate a matchup for
	 * @param	possibleOwnWeights	Array with all possibilities for familiar weight
	 * @return	The ID number of the best opponent. Further information can be collected through other functions
	 */
	public static int bestOpponent( int ownFamiliar, int [] possibleOwnWeights)
	{
		int [] ownSkills = FamiliarsDatabase.getFamiliarSkills( ownFamiliar );
		int possibleWeights = possibleOwnWeights.length;

		bestMatch = bestOpponent = bestWeight = -1;
		difference = 500; // initialize to worst possible value

		for ( int match = 0; match < 4; match++ )
		{
			int ownSkill = ownSkills[match];

			// Skip hopeless contests
			if ( ownSkill == 0 )
				continue;

			for ( int opponent = 0; opponent < 5; ++opponent )
			{
				Opponent opp = opponents[opponent];
				int opponentWeight = opp.getWeight();

				for ( int weightIndex = 0; weightIndex < possibleWeights; ++weightIndex )
				{
					int ownWeight = possibleOwnWeights[weightIndex];
					int ownPower = ownWeight + ownSkill * 3;


					int opponentSkill = opp.getSkill( match );
					int opponentPower;
					if ( opponentSkill == 0 )
						opponentPower = 5;
					else
						opponentPower = opponentWeight + opponentSkill * 3;

					// optimal weight for equal skill is +3
					if ( betterWeightDifference(ownPower - (opponentPower + 3), difference) )
					{
						difference = ownPower - (opponentPower + 3);
						bestOpponent = opponent;
						bestMatch = match;
						bestWeight = ownWeight;
					}
				}
			}
		}

		return bestOpponent;
	}

	/**
	 * Retrieves match data. Will only supply relevant data for last call to bestOpponent
	 * @return	The ID number of the best match. 0 = 'Ultimate Cage Match', 1 = 'Scavenger Hunt', 2 = 'Obstacle Course', 3 = 'Hide and Seek'
	 */
	public static int bestMatch()
	{	return bestMatch;
	}

	/**
	 * Retrieves weight for matchup. This weight will be a value from the possibleOwnWeights parameter in bestOpponent()
	 * @return	Weight value for chosen matchup
	 */
	public static int bestWeight()
	{
		return bestWeight;
	}
		
	/**
	 * Retrieves difference from perfect weight for matchup. Will only supply relevant data for last call to bestOpponent()
	 * @return	Difference from the perfect weight. 0 = perfect, +X = X pounds too heavy, -X is X pounds too light.
	 */
	public static int difference()
	{	return difference;
	}
	
	private static boolean betterWeightDifference(int newVal, int oldVal)
	{
		//I am assuming priority to flow as follows: 0/+1/-1/+2/-2/+3/+4/+5/etc
		if ( oldVal == 0 )
			return false;
		else if ( oldVal == 1 )
			return newVal == 0;
		else if ( oldVal == -1 )
			return newVal == 0 || newVal == 1;
		else if ( oldVal == 2 )
			return newVal == 0 || newVal == 1 || newVal == -1;
		else if ( oldVal == -2 )
			return newVal == 0 || newVal == 1 || newVal == -1 || newVal == 2;
		else if ( newVal == 0 || newVal == 1 || newVal == -1 || newVal == 2 )
			return true;
		else
			return newVal < oldVal && newVal >= -2;
	}

	private static class Opponent
	{
		// Familiar type
		private int type;

		// Weight
		private int weight;

		// Arena parameters
		private int [] arena = new int[4];

		public Opponent( int type, int weight )
		{
			this.type = type;
			this.weight = weight;
			this.arena = FamiliarsDatabase.getFamiliarSkills( type );
		}

		public int getWeight()
		{	return weight;
		}

		public int getSkill( int match )
		{	return arena[match];
		}
	}
}