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

import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JScrollPane;

import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.CardLayout;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import javax.swing.ToolTipManager;
import javax.swing.table.DefaultTableModel;

import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import com.sun.java.forums.TableSorter;

public class FlowerHunterFrame extends KoLFrame implements ListSelectionListener
{
	private boolean isSimple;
	private CardLayout resultCards;
	private JPanel resultCardPanel;
	private AttackPanel attackPanel;

	private Vector rankLabels = new Vector();
	private JTable [] resultsTable = new JTable[2];
	private TableSorter [] sortedModel = new TableSorter[2];
	private DefaultTableModel [] resultsModel = new DefaultTableModel[2];

	private ProfileRequest [] results;
	private JCheckBox [] detailOptions;

	public FlowerHunterFrame()
	{
		super( "Hardcore Flower Hunter" );

		tabs = new JTabbedPane();
		tabs.add( "Search", new SearchPanel() );

		attackPanel = new AttackPanel();
		tabs.add( "Attack", attackPanel );

		tabs.add( "Profiler", new ClanPanel() );

		updateRank();
		framePanel.setLayout( new BorderLayout() );
		framePanel.add( tabs, BorderLayout.NORTH );

		results = new ProfileRequest[0];

		constructTableModel( 0, new String [] { "Name", "Clan", "Class", "Level", "Rank" } );
		constructTableModel( 1, new String [] { "Name", "Class", "Path", "Level", "Rank", "Drink", "Fashion", "Turns", "Login" } );

		JScrollPane [] resultsScroller = new JScrollPane[2];
		resultsScroller[0] = new JScrollPane( resultsTable[0], JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
		resultsScroller[1] = new JScrollPane( resultsTable[1], JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );

		resultCards = new CardLayout();
		resultCardPanel = new JPanel( resultCards );
		resultCardPanel.add( resultsScroller[0], "0" );
		resultCardPanel.add( resultsScroller[1], "1" );

		framePanel.add( resultCardPanel, BorderLayout.CENTER );

		this.isSimple = true;
		resultCards.show( resultCardPanel, "0" );

		ToolTipManager.sharedInstance().unregisterComponent( resultsTable[0] );
		ToolTipManager.sharedInstance().unregisterComponent( resultsTable[1] );
	}

	private void constructTableModel( int index, String [] headers )
	{
		resultsModel[ index ] = new SearchResultsTableModel( headers );

		if ( resultsTable[ index ] == null )
			resultsTable[ index ] = new JTable( resultsModel[ index ] );
		else
			resultsTable[ index ].setModel( resultsModel[ index ] );

		sortedModel[ index ] = new TableSorter( resultsModel[ index ], resultsTable[ index ].getTableHeader() );
		resultsTable[ index ].setModel( sortedModel[ index ] );
		resultsTable[ index ].getSelectionModel().addListSelectionListener( this );
		resultsTable[ index ].setPreferredScrollableViewportSize(
			new Dimension( (int) resultsTable[ index ].getPreferredScrollableViewportSize().getWidth(), 200 ) );
	}

	public void valueChanged( ListSelectionEvent e )
	{
		JTable table = resultsTable[ isSimple ? 0 : 1 ];
		int selectedIndex = table.getSelectionModel().isSelectionEmpty() ? 0 : 1;

		tabs.setSelectedIndex( selectedIndex );

		if ( selectedIndex == 1 )
		{
			int opponentCount = table.getSelectedRowCount();
			if ( opponentCount == 1 )
				attackPanel.setStatusMessage( "1 opponent selected." );
			else
				attackPanel.setStatusMessage( opponentCount + " opponents selected." );
		}
	}

	private JPanel getRankLabel()
	{
		JPanel rankPanel = new JPanel( new BorderLayout() );
		JLabel rankLabel = new JLabel( " ", JLabel.CENTER );

		rankLabels.add( rankLabel );
		rankPanel.add( rankLabel, BorderLayout.SOUTH );
		return rankPanel;
	}

	private void updateRank()
	{
		int equipmentPower = 0;

		Matcher powerMatcher = Pattern.compile( "\\+(\\d+)" ).matcher( KoLCharacter.getEquipment( KoLCharacter.HAT ) );
		if ( powerMatcher.find() )
			equipmentPower += StaticEntity.parseInt( powerMatcher.group(1) );

		powerMatcher = Pattern.compile( "\\+(\\d+)" ).matcher( KoLCharacter.getEquipment( KoLCharacter.PANTS ) );
		if ( powerMatcher.find() )
			equipmentPower += StaticEntity.parseInt( powerMatcher.group(1) );

		JLabel [] rankLabels = new JLabel[ this.rankLabels.size() ];
		this.rankLabels.toArray( rankLabels );

		for ( int i = 0; i < rankLabels.length; ++i )
			rankLabels[i].setText( "<html><center>Rank " + KoLCharacter.getPvpRank() + "<br>Fashion " + equipmentPower + "</center></html>" );
	}

	private class SearchPanel extends KoLPanel implements Runnable
	{
		private JTextField levelEntry;
		private JTextField rankEntry;
		private JTextField limitEntry;

		public SearchPanel()
		{
			super( "simple", "detail" );

			levelEntry = new JTextField();
			rankEntry = new JTextField();
			limitEntry = new JTextField();

			VerifiableElement [] elements = new VerifiableElement[3];
			elements[0] = new VerifiableElement( "Level: ", levelEntry );
			elements[1] = new VerifiableElement( "Rank: ", rankEntry );
			elements[2] = new VerifiableElement( "Limit: ", limitEntry );

			setContent( elements, null, getRankLabel(), true, true );
		}

		public void actionConfirmed()
		{
			isSimple = true;
			(new RequestThread( this )).start();
		}

		public void actionCancelled()
		{
			isSimple = false;
			(new RequestThread( this )).start();
		}

		public void run()
		{
			int index = isSimple ? 0 : 1;
			int resultLimit = getValue( limitEntry, 100 );

			resultCards.show( resultCardPanel, String.valueOf( index ) );
			KoLmafia.updateDisplay( "Conducting search..." );

			while ( !resultsModel[ index ].getDataVector().isEmpty() )
			{
				resultsModel[ index ].removeRow( 0 );
				resultsModel[ index ].fireTableRowsDeleted( 0, 0 );
			}

			FlowerHunterRequest search = new FlowerHunterRequest( StaticEntity.getClient(), levelEntry.getText(), rankEntry.getText() );
			search.run();

			results = new ProfileRequest[ search.getSearchResults().size() ];
			search.getSearchResults().toArray( results );

			for ( int i = 0; i < resultLimit && i < results.length && KoLmafia.permitsContinue(); ++i )
			{
				resultsModel[ index ].addRow( getRow( results[i], isSimple ) );
				resultsModel[ index ].fireTableRowsInserted( i - 1, i - 1 );
			}

			if ( KoLmafia.permitsContinue() )
				KoLmafia.updateDisplay( "Search completed." );
			else
				KoLmafia.updateDisplay( ERROR_STATE, "Search halted." );
		}

		public Object [] getRow( ProfileRequest result, boolean isSimple )
		{
			if ( isSimple )
				return new Object [] { result.getPlayerName(), result.getClanName(), result.getClassType(),
					result.getPlayerLevel(), result.getPvpRank() };

			KoLmafia.updateDisplay( "Retrieving profile for " + result.getPlayerName() + "..." );

			return new Object [] { result.getPlayerName(), result.getClassType(), result.getRestriction(), result.getPlayerLevel(),
				result.getPvpRank(), result.getDrink(), result.getEquipmentPower(), result.getCurrentRun(), result.getLastLogin() };
		}
	}

	private class ClanPanel extends KoLPanel implements Runnable
	{
		private JTextField clanID;

		public ClanPanel()
		{
			super( "profile", true );

			clanID = new JTextField();

			VerifiableElement [] elements = new VerifiableElement[1];
			elements[0] = new VerifiableElement( "Clan ID: ", clanID );

			setContent( elements, null, getRankLabel(), true, true );
		}

		public void actionConfirmed()
		{	(new RequestThread( this )).start();
		}

		public void actionCancelled()
		{
		}

		public void run()
		{
			isSimple = false;

			resultCards.show( resultCardPanel, "1" );
			KoLmafia.updateDisplay( "Conducting search..." );

			while ( !resultsModel[1].getDataVector().isEmpty() )
			{
				resultsModel[1].removeRow( 0 );
				resultsModel[1].fireTableRowsDeleted( 0, 0 );
			}

			FlowerHunterRequest search = new FlowerHunterRequest( StaticEntity.getClient(), clanID.getText() );
			search.run();

			results = new ProfileRequest[ search.getSearchResults().size() ];
			search.getSearchResults().toArray( results );

			for ( int i = 0; i < results.length && KoLmafia.permitsContinue(); ++i )
			{
				resultsModel[1].addRow( getRow( results[i] ) );
				resultsModel[1].fireTableRowsInserted( i - 1, i - 1 );
			}

			if ( KoLmafia.permitsContinue() )
				KoLmafia.updateDisplay( "Search completed." );
			else
				KoLmafia.updateDisplay( ERROR_STATE, "Search halted." );
		}

		public Object [] getRow( ProfileRequest result )
		{
			KoLmafia.updateDisplay( "Retrieving profile for " + result.getPlayerName() + "..." );

			return new Object [] { result.getPlayerName(), result.getClassType(), result.getRestriction(), result.getPlayerLevel(),
				result.getPvpRank(), result.getDrink(), result.getEquipmentPower(), result.getCurrentRun(), result.getLastLogin() };
		}
	}

	private class AttackPanel extends KoLPanel implements Runnable
	{
		private JTextField message;
		private JComboBox stanceSelect;
		private JComboBox victorySelect;

		public AttackPanel()
		{
			super( "attack", "profile" );

			message = new JTextField();

			stanceSelect = new JComboBox();
			stanceSelect.addItem( "Bully your opponent" );
			stanceSelect.addItem( "Burninate your opponent" );
			stanceSelect.addItem( "Backstab your opponent" );

			victorySelect = new JComboBox();
			victorySelect.addItem( "Steal a pretty flower" );
			victorySelect.addItem( "Fight for leaderboard rank" );

			if ( KoLCharacter.canInteract() )
				victorySelect.addItem( "Nab yourself some dignity" );

			VerifiableElement [] elements = new VerifiableElement[3];
			elements[0] = new VerifiableElement( "Tactic: ", stanceSelect );
			elements[1] = new VerifiableElement( "Mission: ", victorySelect );
			elements[2] = new VerifiableElement( "Message: ", message );

			setContent( elements, null, getRankLabel(), true, true );
			if ( KoLCharacter.getBaseMuscle() >= KoLCharacter.getBaseMysticality() && KoLCharacter.getBaseMuscle() >= KoLCharacter.getBaseMoxie() )
				stanceSelect.setSelectedIndex( 0 );
			if ( KoLCharacter.getBaseMysticality() >= KoLCharacter.getBaseMuscle() && KoLCharacter.getBaseMysticality() >= KoLCharacter.getBaseMoxie() )
				stanceSelect.setSelectedIndex( 1 );
			else
				stanceSelect.setSelectedIndex( 2 );
		}

		public void actionConfirmed()
		{	(new RequestThread( this )).start();
		}

		public void actionCancelled()
		{
			ProfileRequest [] selection = getSelection();
			Object [] parameters = new Object[1];

			for ( int i = 0; i < selection.length; ++i )
			{
				parameters[0] = selection[i];
				(new CreateFrameRunnable( ProfileFrame.class, parameters )).run();
			}
		}

		public void run()
		{
			ProfileRequest [] selection = getSelection();

			String mission = null;
			switch ( victorySelect.getSelectedIndex() )
			{
				case 0:
					mission = "flowers";
					break;

				case 1:
					mission = "rank";
					break;

				case 2:
					mission = "dignity";
					break;
			}

			FlowerHunterRequest request = new FlowerHunterRequest( StaticEntity.getClient(), "",
					stanceSelect.getSelectedIndex() + 1, mission, message.getText() );

			for ( int i = 0; i < selection.length && !KoLmafia.refusesContinue(); ++i )
			{
				KoLmafia.updateDisplay( "Attacking " + selection[i].getPlayerName() + "..." );
				request.setTarget( selection[i].getPlayerID() );  request.run();

				updateRank();
			}

			KoLmafia.updateDisplay( "Attacks completed." );
		}

		private ProfileRequest [] getSelection()
		{
			int index = isSimple ? 0 : 1;
			Vector selectionVector = new Vector();

			for ( int i = 0; i < results.length; ++i )
				if ( resultsTable[ index ].getSelectionModel().isSelectedIndex( i ) )
					selectionVector.add( results[ sortedModel[ index ].modelIndex( i ) ] );

			ProfileRequest [] selection = new ProfileRequest[ selectionVector.size() ];
			selectionVector.toArray( selection );
			return selection;
		}
	}

	private class SearchResultsTableModel extends DefaultTableModel
	{
		public SearchResultsTableModel( Object [] headers )
		{	super( headers, 0 );
		}

		public Class getColumnClass( int c )
		{	return getRowCount() == 0 || getValueAt( 0, c ) == null ? Object.class : getValueAt( 0, c ).getClass();
		}

		public boolean isCellEditable( int row, int col )
		{	return col == getColumnCount() - 1;
		}
	}
}
