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
package com.projity.interval;

import com.projity.algorithm.DoubleValue;

import com.projity.pm.time.MutableHasStartAndEnd;

import com.projity.util.DateTime;
import com.projity.util.MathUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.util.Comparator;


/**
 *
 */
public abstract class ValueObjectForInterval
	implements Cloneable,
		Comparable<ValueObjectForInterval>,
		/*Comparator<ValueObjectForInterval>,*/
		DoubleValue,
		MutableHasStartAndEnd,
		Serializable
{
	public ValueObjectForInterval()
	{
	}

	protected ValueObjectForInterval( 
		final long _start )
	{
		start = _start;
	}

	/** Copy constructor for ValueObjectForInterval
	 * 
	 * @param _source used as the {@code ValueObjectForInterval} to copy
	 */
	protected ValueObjectForInterval(
		final ValueObjectForInterval _source )
	{
		super();
		
		end = _source.end;
		start = _source.start;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public abstract ValueObjectForInterval clone();
	
//	@Override
//	public int compare( 
//		final ValueObjectForInterval _1,
//		final ValueObjectForInterval _2 )
//	{
//		return MathUtils.signum( _1.start - _2.start );
//	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo( 
		final ValueObjectForInterval _other )
	{
		return MathUtils.signum( start - _other.start );
	}

//	protected abstract ValueObjectForIntervalTable createValueObjectTable();

	public void deserializeCompact( 
		ObjectInputStream _stream,
		ValueObjectForIntervalTable _parentTable )
		throws IOException, 
			ClassNotFoundException
	{
		setStart( _stream.readLong() );
		setEnd( _stream.readLong() );

//		boolean hasTable = _stream.readBoolean();

//		if (hasTable)
//		{
//			ValueObjectForIntervalTable t=createValueObjectTable();
//			t.deserializeCompact(s);
//			setTable(t);
//		}
		
//		table = _parentTable;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals( 
		final Object _other )
	{
		return start == ((ValueObjectForInterval)_other).start;
	}

	/**
	 * @return Returns the end.
	 */
	@Override
	public final long getEnd()
	{
		return end;
	}

	protected abstract double getPrimaryValue();

	/**
	 * @return Returns the start.
	 */
	@Override
	public final long getStart()
	{
		return start;
	}

//	public ValueObjectForIntervalTable getTable()
//	{
//		return table;
//	}

	/* (non-Javadoc)
	 * @see com.projity.algorithm.DoubleValue#getValue()
	 */
	@Override
	public double getValue()
	{
		return getPrimaryValue();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public final int hashCode()
	{
		int hash = 5;
		hash = 19 * hash + (int) (start ^ (start >>> 32));
		return hash;
	}

	protected boolean isDefault()
	{
		return (start == NA_TIME);
	}

	public boolean isFirst()
	{
		return start == NA_TIME;
	}

	public void serializeCompact( 
		ObjectOutputStream _stream,
		ValueObjectForIntervalTable _parentTable )
		throws IOException
	{
		_stream.writeLong( start );
		_stream.writeLong( end );

//	    s.writeBoolean(table!=null);
//	    if (table!=null){
//	    	table.serializeCompact(s);
//	    }
		
		_stream.writeBoolean( false );
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.time.MutableHasStartAndEnd#setEnd(long)
	 */
	@Override
	public final void setEnd( 
		final long _end )
	{
		end = _end;
	}

	protected abstract void setPrimaryValue( double _value );

	/* (non-Javadoc)
	 * @see com.projity.pm.time.MutableHasStartAndEnd#setStart(long)
	 */
	@Override
	public final void setStart( 
		final long _start )
	{
		if (_start < NA_TIME) // check weird case if assigning to Jan 1 1970 at midnight
		{
			start = NA_TIME;
		}
		else
		{
			start = _start;
		}	
	}

//	public void setTable( 
//		ValueObjectForIntervalTable _table )
//	{
//		table = _table;
//	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "Start: " + DateTime.shortString( getStart() ) + " End: " + DateTime.shortString( getEnd() ) + " Value:" +
		getPrimaryValue();
	}

	static final long serialVersionUID = 286111222666L;
	protected static long NA_TIME = DateTime.NA_TIME.getTime();
//	protected ValueObjectForIntervalTable table;
	protected long end = DateTime.getMaxDate().getTime();
	long start = NA_TIME;
}
