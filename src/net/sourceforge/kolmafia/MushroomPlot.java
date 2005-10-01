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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class MushroomPlot extends StaticEntity
{
	// The player's mushroom plot
	//
	//  1  2  3  4
	//  5  6  7  8
	//  9 10 11 12
	// 13 14 15 16

	private static int [][] actualPlot = new int[4][4];

	private static boolean initialized = false;
	private static boolean ownsPlot = false;

	// Empty spot
	public static final int EMPTY = 0;

	// Sprout
	public static final int SPROUT = 1;

	// First generation mushrooms
	public static final int SPOOKY = 724;
	public static final int KNOB = 303;
	public static final int KNOLL = 723;

	// Second generation mushrooms
	public static final int WARM = 749;
	public static final int COOL = 751;
	public static final int POINTY = 753;

	// Third generation mushrooms
	public static final int FLAMING = 755;
	public static final int FROZEN = 756;
	public static final int STINKY = 757;

	// Assocations between the mushroom IDs
	// and the mushroom image.

	public static final Object [][] MUSHROOMS =
	{
		// Sprout and emptiness
		{ new Integer( EMPTY ), "dirt1.gif", "__", new Integer( 0 ) },
		{ new Integer( SPROUT ), "mushsprout.gif", "..", new Integer( 0 ) },

		// First generation mushrooms
		{ new Integer( KNOB ), "mushroom.gif", "Kb", new Integer( 1 ) },
		{ new Integer( KNOLL ), "bmushroom.gif", "Kn", new Integer( 2 ) },
		{ new Integer( SPOOKY ), "spooshroom.gif", "Sp", new Integer( 3 ) },

		// Second generation mushrooms
		{ new Integer( WARM ), "flatshroom.gif", "Wa", new Integer( 4 ) },
		{ new Integer( COOL ), "plaidroom.gif", "Co", new Integer( 5 ) },
		{ new Integer( POINTY ), "tallshroom.gif", "Po", new Integer( 6 ) },

		// Third generation mushrooms
		{ new Integer( FLAMING ), "fireshroom.gif", "Fl", new Integer( 0 ) },
		{ new Integer( FROZEN ), "iceshroom.gif", "Fr", new Integer( 0 ) },
		{ new Integer( STINKY ), "stinkshroo.gif", "St", new Integer( 0 ) }
	};

	public static final int [][] BREEDING =
	{
		// EMPTY,   KNOB,    KNOLL,   SPOOKY,  WARM,    COOL,    POINTY

		{  EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY   },  // EMPTY
		{  EMPTY,   KNOB,    COOL,    WARM,    EMPTY,   EMPTY,   EMPTY   },  // KNOB
		{  EMPTY,   COOL,    KNOLL,   POINTY,  EMPTY,   EMPTY,   EMPTY   },  // KNOLL
		{  EMPTY,   WARM,    POINTY,  SPOOKY,  EMPTY,   EMPTY,   EMPTY   },  // SPOOKY
		{  EMPTY,   EMPTY,   EMPTY,   EMPTY,   WARM,    STINKY,  FLAMING },  // WARM
		{  EMPTY,   EMPTY,   EMPTY,   EMPTY,   STINKY,  COOL,    FROZEN  },  // COOL
		{  EMPTY,   EMPTY,   EMPTY,   EMPTY,   FLAMING, FROZEN,  POINTY  }   // POINTY
	};

	// Spore data - includes price of the spore
	// and the item ID associated with the spore.

	private static final int [][] SPORE_DATA = { { SPOOKY, 30 }, { KNOB, 40 }, { KNOLL, 50 } };

	/**
	 * Static method which resets the state of the
	 * mushroom plot.  This should be used whenever
	 * the login process is restarted.
	 */

	public static void reset()
	{	initialized = false;
	}

	/**
	 * Utility method which returns a two-dimensional
	 * array showing the arrangement of the plot.
	 */

	public static String getMushroomPlot( boolean isHypertext )
	{
		// If for some reason, the plot was invalid, then
		// the flag would have been set on the client.  In
		// this case, return a message.

		if ( !initialize() )
			return "Your plot is unavailable.";

		return getMushroomPlot( isHypertext, actualPlot );
	}

	/**
	 * Utility method which returns a two-dimensional
	 * array showing the arrangement of the forecasted
	 * plot (ie: what the plot will look like tomorrow).
	 */

	public static String getForecastedPlot( boolean isHypertext )
	{	return getForecastedPlot( isHypertext, actualPlot );
	}

	public static String getForecastedPlot( boolean isHypertext, int [][] plot )
	{
		// If for some reason, the plot was invalid, then
		// the flag would have been set on the client.  In
		// this case, return a message.

		if ( !initialize() )
			return "Your plot is unavailable.";

		// Construct the forecasted plot now.
		int [][] forecastPlot = new int[4][4];

		for ( int row = 0; row < 4; ++row )
			for ( int col = 0; col < 4; ++col )
				forecastPlot[ row ][ col ] = getForecastSquare( row, col, plot );

		// Whenever the forecasted plot doesn't
		// match the original plot, the surrounding
		// mushrooms are assumed to disappear.

		for ( int row = 0; row < 4; ++row )
			for ( int col = 0; col < 4; ++col )
				if ( forecastPlot[ row ][ col ] != EMPTY && plot[ row ][ col ] != forecastPlot[ row ][ col ] )
				{
					if ( row != 0 )  forecastPlot[ row - 1 ][ col ] = EMPTY;
					if ( row != 3 )  forecastPlot[ row + 1 ][ col ] = EMPTY;
					if ( col != 0 )  forecastPlot[ row ][ col - 1 ] = EMPTY;
					if ( col != 3 )  forecastPlot[ row ][ col + 1 ] = EMPTY;
				}

		return getMushroomPlot( isHypertext, forecastPlot );
	}

	private static int getForecastSquare( int row, int col, int [][] plot )
	{
		int [] touched = new int[4];

		// First, determine what kinds of mushrooms
		// touch the square.

		touched[0] = row == 0 ? EMPTY : plot[ row - 1 ][ col ];
		touched[1] = row == 3 ? EMPTY : plot[ row + 1 ][ col ];
		touched[2] = col == 0 ? EMPTY : plot[ row ][ col - 1 ];
		touched[3] = col == 3 ? EMPTY : plot[ row ][ col + 1 ];

		// Determine how many mushrooms total touch
		// the square.

		int [] touchIndex = new int[4];
		int touchCount = 0;

		for ( int i = 0; i < 4; ++i )
		{
			if ( touched[i] != EMPTY && touched[i] != SPROUT )
			{
				for ( int j = 0; j < MUSHROOMS.length; ++j )
					if ( touched[i] == ((Integer)MUSHROOMS[j][0]).intValue() )
						touchIndex[ touchCount ] = ((Integer)MUSHROOMS[j][3]).intValue();

				++touchCount;
			}
		}

		// If exactly two mushrooms are touching the
		// square, then return the result of the breed.
		// Otherwise, it'll be the same as whatever is
		// there right now.

		return touchCount == 2 ? BREEDING[ touchIndex[0] ][ touchIndex[1] ] : plot[ row ][ col ];
	}

	private static String getMushroomPlot( boolean isHypertext, int [][] plot )
	{
		// Otherwise, you need to construct the string form
		// of the mushroom plot.  Shorthand and hpertext are
		// the only two versions at the moment.

		StringBuffer buffer = new StringBuffer();

		buffer.append( isHypertext ? "<center><table cellspacing=4 cellpadding=4>" : LINE_BREAK );

		for ( int row = 0; row < 4; ++row )
		{
			// In a hypertext document, you initialize the
			// row in the table before you start appending
			// the squares.

			if ( isHypertext )
				buffer.append( "<tr>" );

			for ( int col = 0; col < 4; ++col )
			{
				// Hypertext documents need to have their cells opened before
				// the cell can be printed.

				buffer.append( isHypertext ? "<td>" : "  " );
				int square = plot[ row ][ col ];

				String description = MushroomPlot.mushroomDescription( square );

				// If a description is available, and you're creating a
				// hypertext document, print the hyperlink showing the
				// description.

				if ( isHypertext && description != null )
					buffer.append( description );

				// Mushroom images are used in hypertext documents, while
				// shorthand notation is used in non-hypertext documents.

				buffer.append( isHypertext ? mushroomImage( square ) : mushroomShorthand( square ) );

				// If a description is available, and you're creating a
				// hypertext document, close the hyperlink showing the
				// description.

				if ( isHypertext && description != null )
					buffer.append( "</a>" );

				// Hypertext documents need to have their cells closed before
				// another cell can be printed.

				if ( isHypertext )
					buffer.append( "</td>" );
			}

			// In a hypertext document, you need to close the row before
			// continuing.  Note that both documents can have a full
			// line break at the end.

			if ( isHypertext )
				buffer.append( "</tr>" );

			buffer.append( LINE_BREAK );
		}

		if ( isHypertext )
			buffer.append( "</table></center>" );

		// Now that the appropriate string has been constructed,
		// return it to the calling method.

		return buffer.toString();
	}

	/**
	 * Utility method which retrieves the image associated
	 * with the given mushroom type.
	 */

	private static String mushroomImage( int mushroomType )
	{
		for ( int i = 0; i < MUSHROOMS.length; ++i )
			if ( mushroomType == ((Integer) MUSHROOMS[i][0]).intValue() )
				return "<img src=\"http://images.kingdomofloathing.com/itemimages/" + MUSHROOMS[i][1] + "\" width=30 height=30 border=0>";

		return "<img src=\"http://images.kingdomofloathing.com/itemimages/dirt1.gif\" width=30 height=30 border=0>";
	}

	/**
	 * Utility method which retrieves the shorthand notation
	 * for the given mushroom type.
	 */

	private static String mushroomShorthand( int mushroomType )
	{
		for ( int i = 0; i < MUSHROOMS.length; ++i )
			if ( mushroomType == ((Integer) MUSHROOMS[i][0]).intValue() )
				return (String) MUSHROOMS[i][2];

		return "??";
	}

	/**
	 * Utility method which retrieves the hyperlink description
	 * for the given mushroom type.
	 */

	private static String mushroomDescription( int mushroomType )
	{
		return mushroomType == EMPTY || mushroomType == SPROUT ? null :
			"<a href=\"desc_item.php?whichitem=" + TradeableItemDatabase.getDescriptionID( mushroomType ) + "\">";
	}

	/**
	 * One of the major functions of the mushroom plot handler,
	 * this method plants the given spore into the given position
	 * (or square) of the mushroom plot.
	 */

	public static boolean plantMushroom( int square, int spore )
	{
		// Validate square parameter.  It's possible that
		// the user input the wrong spore number.

		if ( square < 1 || square > 16 )
		{
			client.updateDisplay( ERROR_STATE, "Squares are numbered from 1 to 16." );
			client.cancelRequest();
			return false;
		}

		// Determine the spore that the user wishes to
		// plant and the price for that spore.  Place
		// those into holder variables.

		int sporeIndex = -1, sporePrice = -1;
		for ( int i = 0; i < SPORE_DATA.length; ++i )
			if ( SPORE_DATA[i][0] == spore )
			{
				sporeIndex = i + 1;
				sporePrice = SPORE_DATA[i][1];
			}

		// If nothing was reset, then return from this
		// method after notifying the user that the spore
		// they provided is not plantable.

		if ( sporeIndex == -1 )
		{
			client.updateDisplay( ERROR_STATE, "You can't plant that." );
			client.cancelRequest();
			return false;
		}

		// Make sure we have enough meat to pay for the spore.
		// Rather than using requirements validation, check the
		// character data.

		if ( KoLCharacter.getAvailableMeat() < sporePrice )
			return false;

		// Make sure we know current state of mushroom plot
		// before we plant the mushroom.  Bail if it fails.

		if ( !initialize() )
			return false;

		// If the square isn't empty, pick what's there

		int row = (square - 1) / 4;
		int col = (square - 1) % 4;

		if ( actualPlot[ row ][ col ] != EMPTY && !pickMushroom( square, true ) )
			return false;

		// Plant the requested spore.

		MushroomPlotRequest request = new MushroomPlotRequest( square, sporeIndex );
		client.updateDisplay( DISABLED_STATE, "Planting " + TradeableItemDatabase.getItemName( spore ) + " spore..." );
		request.run();

		// If it failed, bail.

		if ( !client.permitsContinue() )
			return false;

		// Pay for the spore.  At this point, it's guaranteed
		// that the client allows you to continue.

		client.processResult( new AdventureResult( AdventureResult.MEAT, 0 - sporePrice ) );
		client.updateDisplay( ENABLED_STATE, "Spore successfully planted." );
		return true;
	}

	/**
	 * One of the major functions of the mushroom plot handler,
	 * this method picks the mushroom located in the given square.
	 */

	public static boolean pickMushroom( int square, boolean pickSpores )
	{
		// Validate square parameter.  It's possible that
		// the user input the wrong spore number.

		if ( square < 1 || square > 16 )
		{
			client.updateDisplay( ERROR_STATE, "Squares are numbered from 1 to 16." );
			client.cancelRequest();
			return false;
		}

		// Make sure we know current state of mushroom plot
		// before we plant the mushroom.  Bail if it fails.

		if ( !initialize() )
			return false;

		// If the square is not empty, run a request to pick
		// the mushroom in the square.

		int row = (square - 1) / 4;
		int col = (square - 1) % 4;

		if ( actualPlot[ row ][ col ] != EMPTY )
		{
			if ( actualPlot[ row ][ col ] != SPROUT || pickSpores )
			{
				MushroomPlotRequest request = new MushroomPlotRequest( square );
				client.updateDisplay( DISABLED_STATE, "Picking square " + square + "..." );
				request.run();
				client.updateDisplay( ENABLED_STATE, "Square picked." );
			}
		}

		return client.permitsContinue();
	}

	/**
	 * Utility method used to initialize the state of
	 * the plot into the one-dimensional array.
	 */

	private static boolean initialize()
	{
		if ( client == null )
			return true;

		// Clear error state so that the flags are
		// properly detected.

		client.resetContinueState();

		// If you're not in a Muscle sign, no go.

		if ( !KoLCharacter.inMuscleSign() )
		{
			client.updateDisplay( ERROR_STATE, "You can't find the mushroom fields." );
			client.cancelRequest();
			return false;
		}

		// Do this only once.

		if ( initialized )
			return true;

		// Ask for the state of your plot.

		initialized = true;
		MushroomPlotRequest request = new MushroomPlotRequest();
		request.run();

		return client.permitsContinue();
	}

	private static class MushroomPlotRequest extends KoLRequest
	{
		public MushroomPlotRequest()
		{	super( MushroomPlot.client, "knoll_mushrooms.php", true );
		}

		public MushroomPlotRequest( int square )
		{
			this();
			addFormField( "action", "click" );
			addFormField( "pos", String.valueOf( square - 1 ) );
		}

		public MushroomPlotRequest( int square, int spore )
		{
			this();
			addFormField( "action", "plant" );
			addFormField( "pos", String.valueOf( square - 1 ) );
			addFormField( "whichspore", String.valueOf( spore ) );
		}

		public void run()
		{
			super.run();
			parsePlot( responseText );

			// If you don't own a mushroom plot, there is nothing
			// left to do.  Update the display indicating this and
			// cancel the request.

			if ( ownsPlot == false )
			{
				client.updateDisplay( ERROR_STATE, "You haven't bought a mushroom plot yet." );
				client.cancelRequest();
				return;
			}

			client.processResults( responseText );
		}
	}

	public static void parsePlot( String text )
	{
		initialized = true;

		// Pretend all of the sections on the plot are empty
		// before you begin parsing the plot.

		for ( int row = 0; row < 4; ++row )
			for ( int col = 0; col < 4; ++col )
				actualPlot[ row ][ col ] = EMPTY;

		Matcher plotMatcher = Pattern.compile( "<b>Your Mushroom Plot:</b><p><table>(<tr>.*?</tr><tr>.*></tr><tr>.*?</tr><tr>.*</tr>)</table>" ).matcher( text );
		ownsPlot = plotMatcher.find();

		// If there is no plot data, then we can assume that
		// the person does not own a plot.  Return from the
		// method if this is the case.  Otherwise, try to find
		// all of the squares.

		if ( !ownsPlot )
			return;

		Matcher squareMatcher = Pattern.compile( "<td>(.*?)</td>" ).matcher( plotMatcher.group(1) );

		for ( int row = 0; row < 4; ++row )
			for ( int col = 0; col < 4 && squareMatcher.find(); ++col )
				actualPlot[ row ][ col ] = parseSquare( squareMatcher.group(1) );
	}

	private static int parseSquare( String text )
	{
		// We figure out what's there based on the image.  This
		// is done by checking the text in the square against
		// the table of square values.

		Matcher gifMatcher = Pattern.compile( ".*/((.*)\\.gif)" ).matcher( text );
		if ( gifMatcher.find() )
		{
			String gif = gifMatcher.group(1);
			for ( int i = 0; i < MUSHROOMS.length; ++i )
				if ( gif.equals( MUSHROOMS[i][1] ) )
					return ((Integer) MUSHROOMS[i][0]).intValue();
		}

		return EMPTY;
	}

	public static void main( String [] args )
	{
		initialized = true;
		ownsPlot = true;

		actualPlot[0][0] = KNOB;
		actualPlot[0][2] = KNOLL;
		actualPlot[1][3] = SPOOKY;
		actualPlot[2][0] = SPOOKY;
		actualPlot[3][1] = KNOLL;
		actualPlot[3][3] = KNOB;

		System.out.println( getMushroomPlot( false ) );
		System.out.println( getForecastedPlot( false ) );
	}
}