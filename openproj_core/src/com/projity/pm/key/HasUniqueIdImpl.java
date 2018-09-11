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
package com.projity.pm.key;

import com.projity.server.data.CommonDataObject;
import com.projity.server.data.DataObject;

import com.projity.session.Session;
import com.projity.session.SessionFactory;

import java.io.Serializable;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class HasUniqueIdImpl
	implements Serializable
{
	/**
	 *
	 */
	public HasUniqueIdImpl( 
		final DataObject _dataObject,
		final long _uniqueId )
	{
		setLocal( CommonDataObject.isLocal( _uniqueId ) );

		//System.out.println((hasUniqueId==null?"null":hasUniqueId.getClass()+"")+" UniqueId "+uniqueId+", local? "+local);
		uniqueIds.put( Long.valueOf( _uniqueId ), _dataObject );
	}

	public HasUniqueIdImpl( 
		final boolean _local,
		final DataObject _hasUniqueId )
	{
		setLocal( _local );

		//System.out.println((hasUniqueId==null?"null":hasUniqueId.getClass()+"")+" UniqueId ?, local? "+local);
		uniqueId = session.getId();
		uniqueIds.put( Long.valueOf( uniqueId ), _hasUniqueId );
	}

	@Override
	public boolean equals( 
		final Object _other )
	{
		if (_other instanceof HasUniqueIdImpl == false)
		{
			return false;
		}

		return uniqueId == ((HasUniqueIdImpl)_other).uniqueId;
	}

	@Override
	protected void finalize()
		throws Throwable
	{
		uniqueIds.remove( Long.valueOf( uniqueId ) );
		super.finalize();
	}

	/**
	 * @return Returns the uniqueId.
	 */
	public long getUniqueId()
	{
		return uniqueId;
	}

	public boolean isLocal()
	{
		return local;
	}

//	public boolean isNew() 
//	{
//		return newId;
//	}

	public boolean renumber( 
		final boolean _localOnly,
		final long _newUniqueId )
	{
		if (uniqueId == -1)
		{
			return false;
		}

		if ((_localOnly == true) 
		 && (CommonDataObject.isLocal( uniqueId ) == false))
		{
			return false;
		}

		if ((_localOnly == true) 
		 && (local == true))
		{
			setLocal( false );
		}

		long oldUniqueId = uniqueId;
		final DataObject dataObject = uniqueIds.remove( Long.valueOf( oldUniqueId ) );
		uniqueId = _newUniqueId;
		uniqueIds.put( Long.valueOf( uniqueId ), dataObject );

		//System.out.println("Renumber "+(hasUniqueId==null?"":(hasUniqueId.getClass()+"/"+hasUniqueId.getName()))+": "+oldUniqueId+"-->"+uniqueId);
		return true;
	}

	public boolean renumber( 
		final boolean _localOnly )
	{
		return renumber( _localOnly, session.getId() );
	}

	/** Assign the <i>local</i> attribute.
	 * 
	 * @param _value 
	 */
	public void setLocal( 
		final boolean _value )
	{
		local = _value;
		session = SessionFactory.getInstance().getSession( _value );
	}

//	public void setNew( 
//		final boolean newId ) 
//	{
//		 this.newId = newId;
//	}

	/** Assign the <i>unique ID</i> attribute.
	 * 
	 * @param _value used as the source of the assignment.
	 */
	public void setUniqueId( 
		final long _value )
	{
		uniqueId = _value;
	}

//	public static void update(Long uniqueId,Long newUniqueId)
//	{
//		DataObject hasUniqueId=(DataObject)uniqueIds.get(uniqueId);
//		if (hasUniqueId==null)
//		{
//			System.out.println( "ERROR null DataObject!" );
//			return;
//		}
//		hasUniqueId.setNew( false );
//	}

//	public static void update(Map updateMap)
//	{
//	    for (Iterator i=updateMap.entrySet().iterator();i.hasNext();)
//		{
//	        Map.Entry entry=(Map.Entry)i.next();
//	        update((Long)entry.getKey(),(Long)entry.getValue());
//	    }
//	}

	private static final long serialVersionUID = 939382200022L;
	private static Map<Long,DataObject> uniqueIds = new HashMap<Long,DataObject>();
	protected transient Session session;
	protected transient boolean local;
	protected long uniqueId = -1L;
}
