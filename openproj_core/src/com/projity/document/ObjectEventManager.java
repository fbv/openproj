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
package com.projity.document;

import com.projity.field.Field;
import com.projity.pm.assignment.Assignment;
import com.projity.undo.NodeUndoInfo;
import java.util.Iterator;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.openproj.domain.task.Task;

/**
 *
 */
public class ObjectEventManager
{
	// This methods allows classes to register for ObjectEvents
	public void addListener( 
		final ObjectEvent.Listener _listener )
	{
		listenerList.add( ObjectEvent.Listener.class, _listener );
	}

	private void fire( 
		final Object _source,
		final Object _object,
		final int _eventType,
		final NodeUndoInfo _info )
	{
		final ObjectEvent event = getInstance( _source, _object, _eventType, _info );
		fire( event );
	}

	public void fire( 
		final ObjectEvent _event )
	{
		Object[] listeners = listenerList.getListenerList();

		// Each listener occupies two elements - the first is the listener class
		// and the second is the listener instance
		for (int i = 0; i < listeners.length; i += 2)
		{
			if (listeners[ i ] == ObjectEvent.Listener.class)
			{
//            	if (evt.isUpdate()) 
//					System.out.println("ObjectEvent update: object="+evt.getObject()+", field="+evt.getField()+", source="+evt.getSource()+", info="+evt.getInfo()+", listener="+listeners[i+1]);
//            	else if (evt.isCreate()) 
//					System.out.println("ObjectEvent create: object="+evt.getObject()+", field="+evt.getField()+", source="+evt.getSource()+", info="+evt.getInfo()+", listener="+listeners[i+1]);
//            	else if (evt.isDelete()) 
//					System.out.println("ObjectEvent delete: object="+evt.getObject()+", field="+evt.getField()+", source="+evt.getSource()+", info="+evt.getInfo()+", listener="+listeners[i+1]);
//            	else 
//					System.out.println("ObjectEvent: object="+evt.getObject()+", field="+evt.getField()+", source="+evt.getSource()+", info="+evt.getInfo()+", listener="+listeners[i+1]);

				((ObjectEvent.Listener)listeners[ i + 1 ]).objectChanged( _event );
			}
		}

		recycle( _event );
	}

	public void fireCreateEvent( 
		final Object _source,
		final Object _object )
	{
		fire( _source, _object, ObjectEvent.CREATE, null );
	}

	public void fireCreateEvent( 
		final Object _source,
		final Object _object,
		final NodeUndoInfo _info )
	{
		fire( _source, _object, ObjectEvent.CREATE, _info );
	}

	public void fireDeleteEvent( 
		final Object _source,
		final Object _object )
	{
		fire( _source, _object, ObjectEvent.DELETE, null );
	}

	public void fireDeleteEvent( 
		final Object _source,
		final Object _object,
		final NodeUndoInfo _info )
	{
		fire( _source, _object, ObjectEvent.DELETE, _info );
	}

	public void fireUpdateEvent( 
		final Object _source,
		final Object _object )
	{
		fire( _source, _object, ObjectEvent.UPDATE, null );
	}

	public void fireUpdateEvent( 
		final Object _source,
		final Object _object,
		final NodeUndoInfo _info )
	{
		fire( _source, _object, ObjectEvent.UPDATE, _info );
	}

	public void fireUpdateEvent( 
		final Object _source,
		final Object _object,
		final Field _field )
	{
		ObjectEvent evt = getInstance( _source, _object, ObjectEvent.UPDATE, null );
		evt.setField( _field );
		fire( evt );

		if (_object instanceof Task && _field.isApplicable( Assignment.class ))
		{ // fix for bug 258

			Iterator i = ((Task)_object).getAssignments().iterator();

			while (i.hasNext())
			{
				fireUpdateEvent( _source, i.next(), _field );
			}
		}
	}

	public static ObjectEvent getInstance( 
		final Object _source )
	{
		try
		{
			final ObjectEvent objectEvent = pool.borrowObject();
			objectEvent.set( _source );

			return objectEvent;
		}
		catch (final Exception _e)
		{
			_e.printStackTrace();
		}

		return null;
	}

	public static ObjectEvent getInstance( 
		final Object _source,
		final Object _object,
		final int _eventType,
		final NodeUndoInfo _info )
	{
		final ObjectEvent objectEvent = getInstance( _source );
		objectEvent.set( _source, _object, _eventType, _info );

		return objectEvent;
	}
	
	public static void recycle(
		final ObjectEvent _event )
	{
		try
		{
			pool.returnObject( _event );
		}
		catch (final Exception _e)
		{
			// TODO Auto-generated catch block
			_e.printStackTrace();
		}
	}

	public void removeAll()
	{
		listenerList = new javax.swing.event.EventListenerList();
	}

	// This methods allows classes to unregister for ObjectEvents
	public void removeListener( 
		final ObjectEvent.Listener _listener )
	{
		listenerList.remove( ObjectEvent.Listener.class, _listener );
	}

	private static class ObjectEventFactory
		extends BasePoolableObjectFactory<ObjectEvent>
	{
		@Override
		public void activateObject( 
			final ObjectEvent _event )
			throws Exception
		{
			_event.reset();
		}

		@Override
		public ObjectEvent makeObject()
			throws Exception
		{
			return new ObjectEvent();
		}
	}

	//	 Create the listener list
	protected javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();
	
	/**
	 * 
	 */
	private static GenericObjectPool<ObjectEvent> pool = new GenericObjectPool<ObjectEvent>( new ObjectEventFactory() );

}
