/**
 * Copyright (c) 2003, Spellcast development team
 * http://spellcast.dev.java.net/
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
 *  [3] Neither the name "Spellcast development team" nor the names of
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

// layout
import java.awt.Color;
import java.awt.Dimension;
import java.awt.CardLayout;
import java.awt.BorderLayout;
import javax.swing.DefaultListCellRenderer;

// events
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

// containers
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Component;
import javax.swing.JList;
import javax.swing.JTabbedPane;

// utilities
import java.util.Map;
import java.util.Iterator;
import java.util.Comparator;
import net.java.dev.spellcast.utilities.LockableListModel;
import net.java.dev.spellcast.utilities.JComponentUtilities;

/**
 * A Frame to examine item descriptions
 */

public class ExamineItemsFrame extends KoLFrame
{
	private JTabbedPane tabs;
	private ExamineItemsPanel items;
	private ItemLookupPanel familiars, skills, effects;

	private static LockableListModel allItems;
	static
	{
		allItems = new LockableListModel();

		Iterator items = TradeableItemDatabase.iterator();
		while ( items.hasNext() )
			allItems.add( items.next() );
	}

	private static LockableListModel allEffects;
	static
	{
		allEffects = new LockableListModel();

		Iterator effects = StatusEffectDatabase.iterator();
		while ( effects.hasNext() )
			allEffects.add( effects.next() );
	}

	private static LockableListModel allSkills;
	static
	{
		allSkills = new LockableListModel();

		Iterator skills = ClassSkillsDatabase.iterator();
		while ( skills.hasNext() )
			allSkills.add( skills.next() );
	}

	private static LockableListModel allFamiliars;
	static
	{
		allFamiliars = new LockableListModel();

		Iterator familiars = FamiliarsDatabase.iterator();
		while ( familiars.hasNext() )
			allFamiliars.add( familiars.next() );
	}

	public ExamineItemsFrame( KoLmafia client )
	{
		super( client, "Kingdom of Loathing Encyclopedia" );

		tabs = new JTabbedPane();

		items = new ExamineItemsPanel( allItems );
		JPanel itemsContainer = new JPanel();
		itemsContainer.setLayout( new BorderLayout() );
		itemsContainer.add( items, BorderLayout.CENTER );
		tabs.addTab( "Items", itemsContainer );

		familiars = new ItemLookupPanel( allFamiliars, "Familiars", "familiar", "which" );
		JPanel familiarsContainer = new JPanel();
		familiarsContainer.setLayout( new BorderLayout() );
		familiarsContainer.add( familiars, BorderLayout.CENTER );
		tabs.addTab( "Familiars", familiarsContainer );

		skills = new ItemLookupPanel( allSkills, "Skills", "skill", "whichskill" );
		JPanel skillsContainer = new JPanel();
		skillsContainer.setLayout( new BorderLayout() );
		skillsContainer.add( skills, BorderLayout.CENTER );
		tabs.addTab( "Skills", skillsContainer );

		effects = new ItemLookupPanel( allEffects, "Effects", "effect", "whicheffect" );
		JPanel effectsContainer = new JPanel();
		effectsContainer.setLayout( new BorderLayout() );
		effectsContainer.add( effects, BorderLayout.CENTER );
		tabs.addTab( "Effects", effectsContainer );

		getContentPane().setLayout( new CardLayout( 10, 10 ) );
		getContentPane().add( tabs, "" );
	}

	public boolean isEnabled()
	{	return true;
	}

	private class ItemLookupPanel extends ItemManagePanel
	{
		private LockableListModel list;
		private String type;
		private String which;

		public ItemLookupPanel( LockableListModel list, String title, String type, String which )
		{
			super( "All KoL " + title, "Sort by name", "Sort by " + type + " #", list );

			this.list = list;
			this.type = type;
			this.which = which;

			elementList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
			elementList.addMouseListener( new ShowEntryAdapter() );
			actionConfirmed();
		}

		protected void actionConfirmed()
		{
			// Sort elements by name
			elementList.clearSelection();
			java.util.Collections.sort( list, new EntryNameComparator() );
			elementList.setCellRenderer( new EntryCellRenderer() );
		}

		public void actionCancelled()
		{
			// Sort elements by ID number
			elementList.clearSelection();
			java.util.Collections.sort( list, new EntryIDComparator() );
			elementList.setCellRenderer( new EntryCellRenderer() );
		}

		public String IDNumberMapper( int id )
		{	return String.valueOf( id );
		}

		private class ShowEntryAdapter extends MouseAdapter
		{
			public void mouseClicked( MouseEvent e )
			{
				if ( e.getClickCount() == 2 )
				{
					int index = elementList.locationToIndex( e.getPoint() );
					Object entry = elementList.getModel().getElementAt( index );

					if ( !(entry instanceof Map.Entry ) )
						return;

					String id = IDNumberMapper( ((Integer)((Map.Entry)entry).getValue()).intValue() );
					elementList.ensureIndexIsVisible( index );
					openRequestFrame( "desc_" + type + ".php?" + which + "=" + id );
				}
			}
		}
	}

	private class ExamineItemsPanel extends ItemLookupPanel
	{
		public ExamineItemsPanel( LockableListModel list )
		{	super( list, "Items", "item", "whichitem" );
		}

		public String IDNumberMapper( int id )
		{	return TradeableItemDatabase.getDescriptionID( id );
		}
	}

	private class EntryCellRenderer extends DefaultListCellRenderer
	{
		public EntryCellRenderer()
		{	setOpaque( true );
		}

		public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected, boolean cellHasFocus )
		{
			Component defaultComponent = super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );

			if ( value == null || !(value instanceof Map.Entry ) )
				return defaultComponent;

			Map.Entry entry = (Map.Entry) value;

			StringBuffer stringForm = new StringBuffer();
			stringForm.append( (String)entry.getKey() );
			stringForm.append( " (" );
			stringForm.append( (Integer)entry.getValue() );
			stringForm.append( ")" );

			((JLabel) defaultComponent).setText( stringForm.toString() );
			return defaultComponent;
		}
	}

	private class EntryIDComparator implements Comparator
	{
		public int compare( Object o1, Object o2 )
		{
			if ( !(o1 instanceof Map.Entry ) ||
			     !(o2 instanceof Map.Entry ) )
				throw new ClassCastException();

			int i1 = ((Integer)((Map.Entry)o1).getValue()).intValue();
			int i2 = ((Integer)((Map.Entry)o2).getValue()).intValue();
			return i1 - i2;
		}
	}

	private class EntryNameComparator implements Comparator
	{
		public int compare( Object o1, Object o2 )
		{
			if ( !(o1 instanceof Map.Entry ) ||
			     !(o2 instanceof Map.Entry ) )
				throw new ClassCastException();

			String s1 = (String)((Map.Entry)o1).getKey();
			String s2 = (String)((Map.Entry)o2).getKey();
			return s1.compareTo( s2 );
		}
	}

	public static void main( String [] args )
	{
		Object [] parameters = new Object[1];
		parameters[0] = null;

		(new CreateFrameRunnable( ExamineItemsFrame.class, parameters )).run();
	}
}