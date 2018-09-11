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

import com.projity.undo.NodeUndoInfo;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

import java.util.EventListener;
import java.util.EventObject;


/**
 *
 */
public class ObjectEvent
	extends EventObject
{
	public ObjectEvent()
	{
		super( NULL_SOURCE ); // it needs a source.  Will be modified later
		reset();
	}

	/**
	 * @return Returns the field.
	 */
	public Field getField()
	{
		return field;
	}

	public NodeUndoInfo getInfo()
	{
		return info;
	}

	public Object getObject()
	{
		return object;
	}

	public int getType()
	{
		return eventType;
	}

	public boolean isCreate()
	{
		return (eventType == CREATE);
	}

	public boolean isDelete()
	{
		return (eventType == DELETE);
	}

	public boolean isUpdate()
	{
		return (eventType == UPDATE);
	}

	void reset()
	{
		source = null;
		eventType = 0;
		object = null;
	}
	
	void set(
		final Object _source )
	{
		source = _source;
	}
	
	void set(
		final Object _source,
		final Object _object,
		final int _eventType,
		final NodeUndoInfo _info )
	{
		source = _source;
		object = _object;
		eventType = _eventType;
		info = _info;
	}

	/**
	 * @param field The field to set.
	 */
	public void setField( 
		final Field _field )
	{
		field = _field;
	}

	/**
	 * @param object The object to set.
	 */
	public void setObject( 
		final Object _object )
	{
		object = _object;
	}

	public interface Listener
		extends EventListener
	{
		public void objectChanged( 
			final ObjectEvent _objectEvent );
	}

	private static Object NULL_SOURCE = new Object();
	public static final int CREATE = 1;
	public static final int UPDATE = 0;
	public static final int DELETE = -1;
	private Field field = null;
	private NodeUndoInfo info;
	private Object object;
	private int eventType;
}
