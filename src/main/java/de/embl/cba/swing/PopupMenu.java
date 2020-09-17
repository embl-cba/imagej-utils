/*-
 * #%L
 * TODO
 * %%
 * Copyright (C) 2018 - 2020 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package de.embl.cba.swing;

import de.embl.cba.bdv.utils.popup.BdvPopupMenus;
import org.scijava.ui.behaviour.ClickBehaviour;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class PopupMenu
{
	private JPopupMenu popup;
	private int x;
	private int y;
	private Map< String, JMenuItem > actionNameToMenuItem;
	private Map< String, JMenu > menuNameToMenu;
	private Map< String, JMenu > actionNameToMenu;

	public PopupMenu()
	{
		createPopupMenu();
	}

	private void createPopupMenu()
	{
		actionNameToMenuItem = new HashMap<>(  );
		menuNameToMenu = new HashMap<>(  );
		actionNameToMenu = new HashMap<>(  );
		popup = new JPopupMenu();
	}

	private void addPopupLine() {
		popup.addSeparator();
	}

	public void addPopupAction( String actionName, ClickBehaviour clickBehaviour )
	{
		if ( actionNameToMenuItem.keySet().contains( actionName ) )
			throw new UnsupportedOperationException( actionName + " is already registered in this popup menu." );

		JMenuItem menuItem = new JMenuItem( actionName );
		menuItem.addActionListener( e -> new Thread( () -> clickBehaviour.click( x, y ) ).start() );
		popup.add( menuItem );
		actionNameToMenuItem.put( actionName, menuItem );
	}

	public void addPopupAction( String menuName, String actionName, ClickBehaviour clickBehaviour )
	{
		final String menuActionName = BdvPopupMenus.getMenuActionName( menuName, actionName );

		if ( actionNameToMenuItem.keySet().contains( menuActionName ) )
			throw new UnsupportedOperationException( menuActionName + " is already registered in this popup menu." );

		JMenu menu = getMenu( menuName );
		JMenuItem menuItem = new JMenuItem( actionName );

		menu.add( menuItem );
		menuItem.addActionListener( e -> new Thread( () -> clickBehaviour.click( x, y ) ).start() );
		popup.add( menu );
		actionNameToMenuItem.put( menuActionName, menuItem );
		actionNameToMenu.put( menuActionName, menu );
	}

	private JMenu getMenu( String menuName )
	{
		if ( ! menuNameToMenu.containsKey( menuName ) )
		{
			menuNameToMenu.put( menuName, new JMenu( menuName ) );
		}

		return menuNameToMenu.get( menuName );
	}

	public void removePopupAction( String actionName  )
	{
		if ( ! actionNameToMenuItem.keySet().contains( actionName ) ) return;
		final JMenuItem jMenuItem = actionNameToMenuItem.get( actionName );
		if ( actionNameToMenu.containsKey( actionName ) )
		{
			final JMenu jMenu = actionNameToMenu.get( actionName );
			jMenu.remove( jMenuItem );
			actionNameToMenuItem.remove( actionName );
			final int itemCount = jMenu.getItemCount();
			if ( itemCount == 0 )
			{
				popup.remove( jMenu );
			}
		}
		else
		{
			popup.remove( jMenuItem );
			actionNameToMenuItem.remove( actionName );
		}
	}

	public void addPopupAction( String actionName, Runnable runnable ) {

		JMenuItem menuItem = new JMenuItem( actionName );
		menuItem.addActionListener( e -> new Thread( () -> runnable.run() ).start() );
		popup.add( menuItem );
	}

	public void show( JComponent display, int x, int y )
	{
		this.x = x;
		this.y = y;
		popup.show( display, x, y );
	}
}
