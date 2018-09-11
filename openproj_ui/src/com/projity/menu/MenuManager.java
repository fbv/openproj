/*
The contents of this file are subject to the Common Public Attribution License
Version 1.0 (the "License"); you may not use this file except in compliance with
the License. You may obtain a copy of the License at
http://www.projity.com/license . The License is based on the Mozilla Public
License Version 1.1 but Sections 14 and 15 have been added to cover use of
software over a computer network and provide for limited attribution for the
Original Developer. In addition, Exhibit A has been modified to be consistent
with Exhibit B.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for the
specific language governing rights and limitations under the License. The
Original Code is OpenProj. The Original Developer is the Initial Developer and
is Projity, Inc. All portions of the code written by Projity are Copyright (c)
2006, 2007. All Rights Reserved. Contributors Projity, Inc.

Alternatively, the contents of this file may be used under the terms of the
Projity End-User License Agreeement (the Projity License), in which case the
provisions of the Projity License are applicable instead of those above. If you
wish to allow use of your version of this file only under the terms of the
Projity License and not to allow others to use your version of this file under
the CPAL, indicate your decision by deleting the provisions above and replace
them with the notice and other provisions required by the Projity  License. If
you do not delete the provisions above, a recipient may use your version of this
file under either the CPAL or the Projity License.

[NOTE: The text of this license may differ slightly from the text of the notices
in Exhibits A and B of the license at http://www.projity.com/license. You should
use the latest text at http://www.projity.com/license for your modifications.
You may not remove this license text from the source files.]

Attribution Information: Attribution Copyright Notice: Copyright ? 2006, 2007
Projity, Inc. Attribution Phrase (not exceeding 10 words): Powered by OpenProj,
an open source solution from Projity. Attribution URL: http://www.projity.com
Graphic Image as provided in the Covered Code as file:  openproj_logo.png with
alternatives listed on http://www.projity.com/logo

Display of Attribution Information is required in Larger Works which are defined
in the CPAL as a work which combines Covered Code or portions thereof with code
not governed by the terms of the CPAL. However, in addition to the other notice
obligations, all copies of the Covered Code in Executable and Source Code form
distributed must, as a form of attribution of the original author, include on
each user interface screen the "OpenProj" logo visible to all users.  The
OpenProj logo should be located horizontally aligned with the menu bar and left
justified on the top left of the screen adjacent to the File menu.  The logo
must be at least 100 x 25 pixels.  When users click on the "OpenProj" logo it
must direct them back to http://www.projity.com.
*/
package com.projity.menu;

import com.projity.pm.graphic.TabbedNavigation;

import com.projity.strings.DirectoryClassLoader;
import com.projity.strings.Messages;

import com.projity.util.ClassLoaderUtils;

import org.apache.batik.util.gui.resource.ActionMap;
import org.apache.batik.util.gui.resource.ButtonFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;


/**
 *
 */
public class MenuManager
{
	private MenuManager( 
		final ActionMap _rootActionMap )
	{
		myRootActionMap = _rootActionMap;

		if (myBundle == null)
		{
			try
			{
				final DirectoryClassLoader dir = new DirectoryClassLoader();

				if (dir.isValid() == true)
				{
					myBundle = ResourceBundle.getBundle( MENU_BUNDLE_CONF_DIR, Locale.getDefault(), dir );
				}
			}
			catch (Exception e)
			{
				// ignore
			}

			if (myBundle == null)
			{
				myBundle = ResourceBundle.getBundle( MENU_BUNDLE, Locale.getDefault(), ClassLoaderUtils.getLocalClassLoader() );
			}
		}

		myMenuFactory = new ExtMenuFactory( myBundle, myRootActionMap );
		myToolBarFactory = new ExtToolBarFactory( myBundle, myRootActionMap );
	}

	public void add( 
		final TabbedNavigation _t )
	{
		myTabbedNavigations.add( _t );
	}

	public Action getActionFromId( 
		final String _id )
	{
		return myMenuFactory.getActionFromId( _id );
	}

	public String getActionStringFromId( 
		final String _id )
	{
		final String result = myMenuFactory.getActionStringFromId( _id );

		if (result == null)
		{
			System.out.println( "Invalid item: " + _id +
				" it must be a menu item in the menu properties, even if it's only shown in a toolbar" );
		}

		return result;
	}

	public String getFullTipText( 
		final String _name )
	{
		String s = getStringOrNull( _name + ButtonFactory.TOOLTIP_SUFFIX );

		if (s != null)
		{
			final String help = getStringOrNull( _name + ButtonFactory.HELP_SUFFIX );
			final String demo = getStringOrNull( _name + ButtonFactory.DEMO_SUFFIX );
			final String doc = getStringOrNull( _name + ButtonFactory.DOC_SUFFIX );

			if (doc != null)
			{
				s = HyperLinkToolTip.helpTipText( s, help, demo, doc );
			}
		}

		return s;
	}

	public static MenuManager getInstance( 
		final ActionMap _rootActionMap )
	{
//		if (instance == null)
//			instance = new MenuManager(myRootActionMap);
//		return instance;
		return new MenuManager( _rootActionMap );
	}

	public JMenuBar getMenu( 
		final String _name )
	{
		return myMenuFactory.createJMenuBar( _name );
	}

	public JMenuItem getMenuItemFromId( 
		final String _id )
	{
		return myMenuFactory.getMenuItemFromId( _id );
	}

	public static String getMenuString( 
		final String _key )
	{
		return myBundle.getString( _key );
	}

	public JPopupMenu getPopupMenu( 
		final String _name )
	{
		return myMenuFactory.createJPopupMenuBar( _name );
	}

	public String getString( 
		final String _key )
	{
		return myMenuFactory.getString( _key );
	}

	public String getStringFromAction( 
		final Action _action )
	{
		return myMenuFactory.getStringFromAction( _action );
	}

	public String getStringOrNull( 
		final String _key )
	{
		try
		{
			return getString( _key );
		}
		catch (MissingResourceException e)
		{
			return null;
		}
	}

	public String getTextForId( 
		final String _id )
	{
		return myMenuFactory.getTextForId( _id );
	}

	public JToolBar getToolBar( 
		final String _name )
	{
		return myToolBarFactory.createJToolBar( _name );
	}

	public final ExtToolBarFactory getToolBarFactory()
	{
		return myToolBarFactory;
	}

	public ArrayList getToolButtonsFromId( 
		final String _id )
	{
		return myToolBarFactory.getButtonsFromId( _id );
	}

	public boolean isActionEnabled( 
		final String _id )
	{
		final Collection buttons = myToolBarFactory.getButtonsFromId( _id );

		if (buttons != null)
		{
			final Iterator i = buttons.iterator();

			while (i.hasNext() == true)
			{
				final AbstractButton button = (AbstractButton)i.next();

				return button.isEnabled();
			}
		}
		else
		{
			final JMenuItem menuItem = myMenuFactory.getMenuItemFromId( _id );

			if (menuItem != null)
			{
				return menuItem.isEnabled();
			}
		}

		return false;
	}

	public void setActionEnabled( 
		final String _id,
		final boolean _enable )
	{
		final Collection buttons = myToolBarFactory.getButtonsFromId( _id );

		if (buttons != null)
		{
			final Iterator i = buttons.iterator();

			while (i.hasNext() == true)
			{
				final AbstractButton button = (AbstractButton)i.next();

				if (button != null)
				{
					button.setEnabled( _enable );
				}
			}
		}

		final JMenuItem menuItem = myMenuFactory.getMenuItemFromId( _id );

		if (menuItem != null)
		{
			menuItem.setEnabled( _enable );
		}
	}

	public void setActionSelected( 
		final String _id,
		final boolean _enable )
	{
		final Collection buttons = myToolBarFactory.getButtonsFromId( _id );

		if (buttons != null)
		{
			final Iterator i = buttons.iterator();

			while (i.hasNext() == true)
			{
				final AbstractButton button = (AbstractButton)i.next();

				if (button != null)
				{
					button.setSelected( _enable );

					if (button instanceof JToggleButton == true)
					{
						//	button.setBackground(enable ? Color.GRAY : ExtButtonFactory.BACKGROUND_COLOR);
					}
				}
			}
		}

		final JMenuItem menuItem = myMenuFactory.getMenuItemFromId( _id );

		if (menuItem != null)
		{
			menuItem.setSelected( _enable );
		}

		final Iterator<TabbedNavigation> itor = myTabbedNavigations.iterator();

		while (itor.hasNext() == true)
		{
			itor.next().setActivatedView( _id, _enable );
		}
	}

	public void setActionVisible( 
		final String _id,
		final boolean _enable )
	{
		final Collection buttons = myToolBarFactory.getButtonsFromId( _id );

		if (buttons != null)
		{
			final Iterator i = buttons.iterator();

			while (i.hasNext() == true)
			{
				final AbstractButton button = (AbstractButton)i.next();

				if (button != null)
				{
					button.setVisible( _enable );
				}
			}
		}

		final JMenuItem menuItem = myMenuFactory.getMenuItemFromId( _id );

		if (menuItem != null)
		{
			menuItem.setVisible( _enable );
		}
	}

	public void setText( 
		final String _id,
		final String _text )
	{
		final Collection buttons = myToolBarFactory.getButtonsFromId( _id );

		if (buttons != null)
		{
			final Iterator i = buttons.iterator();

			while (i.hasNext() == true)
			{
				final AbstractButton button = (AbstractButton)i.next();

				if (button != null)
				{
					button.setToolTipText( _text );
				}
			}
		}

		final JMenuItem menuItem = myMenuFactory.getMenuItemFromId( _id );

		if (menuItem != null)
		{
			menuItem.setText( _text );
		}
	}

	private static final String MENU_BUNDLE = Messages.getMetaString( "MenuBundle" ); //com.projity.menu.menu";
	private static final String MENU_BUNDLE_CONF_DIR = Messages.getMetaString( "MenuBundleConfDir" ); //"menu";
	public static final String STANDARD_MENU = "StandardMenuBar";
	public static final String MAC_STANDARD_MENU = "MacStandardMenuBar";
	public static final String SERVER_STANDARD_MENU = "ServerStandardMenuBar";
	public static final String SF_MENU = "SFMenuBar";
	public static final String STANDARD_TOOL_BAR = "StandardToolBar";
	public static final String MAC_STANDARD_TOOL_BAR = "MacStandardToolBar";
	public static final String FILE_TOOL_BAR = "FileToolBar";
	public static final String BIG_TOOL_BAR = "BigToolBar";
	public static final String VIEW_TOOL_BAR = "ViewToolBar";
	public static final String VIEW_TOOL_BAR_WITH_NO_SUB_VIEW_OPTION = "ViewToolBarNoSubView";
	public static final String PRINT_PREVIEW_TOOL_BAR = "PrintPreviewToolBar";

	//private static MenuManager instance = null;
	static ResourceBundle myBundle;
	ActionMap myRootActionMap;

	/*static*/ ExtMenuFactory myMenuFactory;
	ExtToolBarFactory myToolBarFactory;
	LinkedList<TabbedNavigation> myTabbedNavigations = new LinkedList<TabbedNavigation>();
}
