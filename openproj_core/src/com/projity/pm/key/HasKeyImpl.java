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

import com.projity.field.FieldContext;
import com.projity.server.data.DataObject;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;

/**
 *
 */
public class HasKeyImpl
	extends HasUniqueIdImpl
	implements HasKey
{
	/**
	 *
	 */
	public HasKeyImpl( 
		final DataObject _dataObject,
		final long _uniqueId )
	{
		super( _dataObject, _uniqueId );
	}

	public HasKeyImpl( 
		final boolean _local,
		final DataObject _dataObject )
	{
		super( _local, _dataObject );
	}

	//call init to complete initialization
	public static HasKeyImpl deserialize( 
		final ObjectInputStream _stream,
		final DataObject _dataObject )
		throws IOException, 
			ClassNotFoundException
	{
		HasKeyImpl hasKey = new HasKeyImpl( _dataObject, _stream.readLong() );

		//hasKey.setUniqueId(s.readLong());
		hasKey.setId( _stream.readLong() );

//	    hasKey.setNew( false );
		return hasKey;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals( 
		final Object _other )
	{
		if (_other instanceof HasKey == false)
		{
			return false;
		}

		return uniqueId == ((HasKey)_other).getUniqueId();
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public Date getCreated()
	{
		return created;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public long getId()
	{
		return id;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public String getName()
	{
		return name;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public String getName( 
		final FieldContext _fieldContext )
	{
		return name;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDirty()
	{
		return dirty;
	}

//	public boolean isNew() {
//		return CommonDataObject.isLocal(getUniqueId());
//	}

	public void serialize( 
		final ObjectOutputStream _s )
		throws IOException
	{
		_s.writeLong( getUniqueId() );
		_s.writeLong( getId() );
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void setCreated( 
		final Date _created )
	{
		this.created = _created;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void setDirty( 
		final boolean _dirty )
	{
		//System.out.println("HasKeyImpl _setDirty("+dirty+"): "+getName());
		this.dirty = _dirty;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void setId( 
		final long _id )
	{
		this.id = _id;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void setName( 
		String _name )
	{
		if ((_name != null) && (_name.length() > MAX_NAME_LENGTH))
		{
			System.out.println( "Truncating long name from " + _name.length() + " chars to " + MAX_NAME_LENGTH + " chars" );
			_name = _name.substring( 0, MAX_NAME_LENGTH );
		}

		name = _name;
	}

	private static final long serialVersionUID = 739020202L;
	private static final int MAX_NAME_LENGTH = 255;
	Date created = new Date();
	String name = "";
	long id = 0L;

	//because it implements DataObject, should implement a different interface
	private transient boolean dirty;
}
