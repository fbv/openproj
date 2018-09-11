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
package com.projity.pm.graphic.network.layout;

import com.projity.document.ObjectEvent;

import com.projity.graphic.configuration.BarStyles;

import com.projity.pm.graphic.model.cache.NodeModelCache;
import com.projity.pm.graphic.model.event.CompositeCacheEvent;
import com.projity.pm.graphic.network.NetworkParams;
import com.projity.pm.scheduling.ScheduleEvent;

import java.awt.Rectangle;

import java.util.EventListener;

import javax.swing.event.EventListenerList;

/**
 *
 */
public abstract class AbstractNetworkLayout
	implements NetworkLayout
{
	public AbstractNetworkLayout( 
		final NetworkParams _network )
	{
		myNetwork = _network;
		addNetworkLayoutListener( _network );
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.graphic.network.layout#addNetworkLayoutListener(NetworkLayoutListener)
	 */
	@Override
	public final void addNetworkLayoutListener( 
		final NetworkLayoutListener _listener )
	{
		if (myListeners == null)
		{
			myListeners = new EventListenerList();
		}

		myListeners.add( NetworkLayoutListener.class, _listener );
	}

	protected void fireLayoutChanged()
	{
		// Do we have any listeners?
		if (myListeners == null)
		{
			// No, nothing more to do
		}
		
		final NetworkLayoutListener[] listeners = myListeners.getListeners( NetworkLayoutListener.class );
		NetworkLayoutEvent event = null;

		for (int index = listeners.length - 1; index >= 0; index--)
		{
			if (event == null)
			{
				event = new NetworkLayoutEvent( this );
			}

			listeners[ index ].layoutChanged( event );
		}
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.graphic.network.layout#getBounds()
	 */
	@Override
	public Rectangle getBounds()
	{
		return myBounds;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.graphic.network.layout#getListeners(Class)
	 */
	@Override
	public EventListener[] getListeners( 
		final Class<?> _listenerType )
	{
		return myListeners.getListeners( (Class)_listenerType );
	}

//	@Override
//	public NetworkLayoutListener[] getNetworkLayoutListeners()
//	{
//		return (NetworkLayoutListener[])myListeners.getListeners( NetworkLayoutListener.class );
//	}

	@Override
	public void graphicNodesCompositeEvent( 
		final CompositeCacheEvent _compositeEvent )
	{
	}

	protected boolean isEmpty()
	{
		return myBounds.isEmpty();
	}

	@Override
	public void objectChanged( 
		final ObjectEvent _objectEvent )
	{
	}

	@Override
	public void removeNetworkLayoutListener( 
		final NetworkLayoutListener _listener )
	{
		// Do we have any listeners?
		if (myListeners == null)
		{
			// No, nothing more to do
		}
		
		myListeners.remove( NetworkLayoutListener.class, _listener );
	}

	@Override
	public void scheduleChanged( 
		final ScheduleEvent _event )
	{
	}

	@Override
	public void setBarStyles( 
		final BarStyles _barStyles )
	{
		myBarStyles = _barStyles;
	}

	@Override
	public void setCache( 
		final NodeModelCache _cache )
	{
		myCache = _cache;
		myBounds = new Rectangle();
	}

	protected void setEmpty()
	{
		myBounds.setSize( 0, 0 );
	}

	protected BarStyles myBarStyles;
	protected EventListenerList myListeners;
	protected NetworkParams myNetwork;
	protected NodeModelCache myCache;
	protected Rectangle myBounds;
}
