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

import org.apache.batik.util.gui.resource.ActionMap;

import org.apache.commons.collections.bidimap.DualHashBidiMap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;


/**
 * The menu builder code requires an action map.  Here, I make a dummy ActionMap that defers to a dummy MenuAction.
 * This menu action is not used, though we can add things like logging for it later.
 */
public class MenuActionsMap
{
	public MenuActionsMap( 
		final ActionMap _actionMap,
		final MenuManager _menuManager )
	{
		myActionMap = _actionMap;
		myMenuManager = _menuManager;
	}

	public void addHandler( 
		final String _menuId,
		final AbstractAction _action )
	{
		myHandlers.put( myMenuManager.getActionStringFromId( _menuId ), _action );
		myMenuIdActionMap.put( _menuId, _action );
	}

	public Action getActionFromMenuId( 
		final String _id )
	{
		return (Action)myMenuIdActionMap.get( _id );
	}

	public Action getConcreteAction( 
		final String _key )
	{
		return (Action)myHandlers.get( _key );
	}

	public String getStringFromAction( 
		final Action _action )
	{
		return myHandlers.getKey( _action );
	}

	public void setEnabledDocumentMenuActions( 
		final boolean _enable )
	{
		final Iterator<String> itor = myHandlers.keySet().iterator();

		while (itor.hasNext() == true)
		{
			final String actionText = itor.next();
			final GlobalMenuAction action = (GlobalMenuAction)myHandlers.get( actionText );

			if (action.needsDocument() == true)
			{
				myMenuManager.setActionEnabled( actionText, _enable && action.allowed( _enable ) );

				//To disable save action for local project
			}
		}
	}

	public abstract static class DocumentMenuAction
		extends GlobalMenuAction
	{
		@Override
		protected boolean needsDocument()
		{
			return true;
		}
	}

	public abstract static class GlobalMenuAction
		extends AbstractAction
	{
		public boolean allowed( 
			final boolean _enable )
		{
			return true;
		}

		protected boolean needsDocument()
		{
			return false;
		}
	}

	protected final DualHashBidiMap<String,Action> myHandlers = new DualHashBidiMap<String,Action>();

	private ActionMap myActionMap;
	private final Map<String,Action> myMenuIdActionMap = new HashMap<String,Action>();
	private MenuManager myMenuManager;
}
