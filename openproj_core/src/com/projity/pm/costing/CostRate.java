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
package com.projity.pm.costing;

import com.projity.datatype.Rate;
import com.projity.datatype.TimeUnit;

import com.projity.field.FieldContext;

import com.projity.interval.InvalidValueObjectForIntervalException;
import com.projity.interval.ValueObjectForInterval;
import com.projity.interval.ValueObjectForIntervalTable;

import com.projity.pm.calendar.WorkCalendar;

import com.projity.util.DateTime;
import com.projity.util.Environment;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Standard and overtime cost rates are expressed as cost/millisecond
 * Fixed cost is a simple scalar value, not a value.
 */
public class CostRate
	extends ValueObjectForInterval
	implements Cost
{
	public CostRate( 
//		ValueObjectForIntervalTable table,
		final long _start,
		final double _value )
	{
		super( /*table,*/ _start );
		setPrimaryValue( _value );
	}

	public CostRate()
	{
		super();
	}

	private CostRate(
		final CostRate _source )
	{
		super( _source );
		
		overtimeRate = _source.overtimeRate.clone();
		standardRate = _source.standardRate.clone();
		costPerUse = _source.costPerUse;
	}
	
	@Override
	public CostRate clone()
	{
		return new CostRate( this );
	}

	@Override
	public void deserializeCompact( 
		ObjectInputStream _s,
		ValueObjectForIntervalTable _parentTable )
		throws IOException, 
			ClassNotFoundException
	{
		super.deserializeCompact( _s, _parentTable );
		costPerUse = _s.readDouble();
		standardRate = Rate.deserialize( _s );
		overtimeRate = Rate.deserialize( _s );
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.costing.Cost#fieldHideOvertimeRate(com.projity.field.FieldContext)
	 */
	@Override
	public boolean fieldHideOvertimeRate( 
		FieldContext _fieldContext )
	{
		return false;
	}

	/**
	 * @return Returns the costPerUse.
	 */
	@Override
	public double getCostPerUse()
	{
		return costPerUse;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.costing.Cost#getEffectiveDate()
	 */
	@Override
	public long getEffectiveDate()
	{
		return getStart();
	}

	/**
	 * @return Returns the overtimeRate.
	 */
	@Override
	public Rate getOvertimeRate()
	{
		return overtimeRate;
	}

	@Override
	protected double getPrimaryValue()
	{
		return standardRate.getValue();
	}

	/**
	 * @return Returns the standardRate.
	 */
	@Override
	public Rate getStandardRate()
	{
		return standardRate;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.costing.Cost#isReadOnlyEffectiveDate()
	 */
	@Override
	public boolean isReadOnlyEffectiveDate( 
		FieldContext _fieldContext )
	{
		return isDefault();
	}

	private void readObject( 
		ObjectInputStream _s )
		throws IOException, 
			ClassNotFoundException
	{
		_s.defaultReadObject();
		standardRate = Rate.deserialize( _s );
		overtimeRate = Rate.deserialize( _s );
	}

	@Override
	public void serializeCompact( 
		ObjectOutputStream _s,
		ValueObjectForIntervalTable _parentTable )
		throws IOException
	{
		super.serializeCompact( _s, _parentTable );
		_s.writeDouble( costPerUse );
		standardRate.serialize( _s );
		overtimeRate.serialize( _s );
	}

	/**
	 * @param costPerUse The costPerUse to set.
	 */
	@Override
	public void setCostPerUse( 
		double _costPerUse )
	{
		costPerUse = _costPerUse;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.costing.Cost#setEffectiveDate(long)
	 */
	@Override
	public void setEffectiveDate( 
		long _effectiveDate )
		throws InvalidValueObjectForIntervalException
	{
		// TODO: fix this - psc1952 2012.01.19
//		table.adjustStart( _effectiveDate, this );
	}

	/**
	 * @param overtimeRate The overtimeRate to set.
	 */
	@Override
	public void setOvertimeRate( 
		Rate _overtimeRate )
	{
		overtimeRate = _overtimeRate;
	}

	@Override
	protected void setPrimaryValue( 
		double _value )
	{
		standardRate = new Rate( _value, Environment.isNoPodServer()
				? TimeUnit.HOURS
				: TimeUnit.DAYS );
	}

	/**
	 * @param standardRate The standardRate to set.
	 */
	@Override
	public void setStandardRate( 
		Rate _standardRate )
	{
		standardRate = _standardRate;
	}

	@Override
	public String toString()
	{
		return "Start: " + DateTime.shortString( getStart() ) + " End: " + DateTime.shortString( getEnd() ) + " Value:" +
		(getPrimaryValue() * WorkCalendar.MILLIS_IN_HOUR);
	}

	private void writeObject( 
		ObjectOutputStream _stream )
		throws IOException
	{
		_stream.defaultWriteObject();
		standardRate.serialize( _stream );
		overtimeRate.serialize( _stream );
	}

	static final long serialVersionUID = 1726666221119L;
	transient Rate overtimeRate = new Rate();
	transient Rate standardRate = new Rate();
	double costPerUse = 0.0;
}
