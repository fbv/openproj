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

import com.projity.configuration.Settings;

import com.projity.datatype.Rate;

import com.projity.field.FieldContext;

import com.projity.interval.InvalidValueObjectForIntervalException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.Arrays;


/**
 *
 */
public class CostRateTables
	implements Cost,
		Serializable,
		Cloneable
{
	/** Constructor for CostRateTables
	 *
	 *
	 */
	public CostRateTables()
	{
		super();
		
		// initialize array
		myCostRateTableArray = new CostRateTable[ Settings.NUM_COST_RATES ]; 

		//add default element
		myCostRateTableArray[ DEFAULT ] = new CostRateTable( getName( DEFAULT ) ); 

//		java.util.GregorianCalendar start1 = new java.util.GregorianCalendar(2003,java.util.GregorianCalendar.JANUARY,4,0,0);
//		java.util.GregorianCalendar start2 = new java.util.GregorianCalendar(2005,java.util.GregorianCalendar.JANUARY,7,0,0);
//		try {
//			CostRate test;
//			test = myCostRateTableArray[DEFAULT].newRate(start1.getTimeInMillis());
//			test.setStandardRate(100.0/(1000*60*60*8));
//			test.setOvertimeRate(110.0/(1000*60*60*8));
//			test.setCostPerUse(450);
//			test = myCostRateTableArray[DEFAULT].newRate(start2.getTimeInMillis());
//			test.setStandardRate(13);
//			test.setOvertimeRate(1300);
//		} catch (InvalidCostRateException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	public CostRateTables(
		final CostRateTables _source )
	{
		super();
		
		Arrays.copyOf( myNames, _source.myNames.length );

		// deep copy
		if (_source.myCostRateTableArray != null)
		{
			myCostRateTableArray = new CostRateTable[ _source.myCostRateTableArray.length ];

			for (int i = 0; i < _source.myCostRateTableArray.length; i++)
			{
				myCostRateTableArray[ i ] = (_source.myCostRateTableArray[ i ] == null)
					? null
					: (CostRateTable)_source.myCostRateTableArray[ i ].clone();

//				if (myCostRateTableArray[ i ] != null)
//				{
//					myCostRateTableArray[ i ].initAfterCloning();
//				}
			}
		}
		
	}

	@Override
	public Object clone()
	{
		return new CostRateTables( this );
		
//		try
//		{
//			final CostRateTables c = (CostRateTables)super.clone();
//
//			if (myNames != null)
//			{
//				c.myNames = new String[ myNames.length ];
//			}
//			else
//			{
//				for (int i = 0; i < myNames.length; i++)
//				{
//					c.myNames[ i ] = (myNames[ i ] == null)
//						? null
//						: myNames[ i ];
//				}
//			}
//
//			if (myCostRateTableArray != null)
//			{
//				c.myCostRateTableArray = new CostRateTable[ myCostRateTableArray.length ];
//
//				for (int i = 0; i < myCostRateTableArray.length; i++)
//				{
//					c.myCostRateTableArray[ i ] = (myCostRateTableArray[ i ] == null)
//						? null
//						: (CostRateTable)myCostRateTableArray[ i ].clone();
//
//					if (c.myCostRateTableArray[ i ] != null)
//					{
//						c.myCostRateTableArray[ i ].initAfterCloning();
//					}
//				}
//			}
//
//			return c;
//		}
//		catch (CloneNotSupportedException e)
//		{
//			throw new InternalError();
//		}
	}

	public static CostRateTables deserialize( 
		final ObjectInputStream _stream )
		throws IOException, 
			ClassNotFoundException
	{
		CostRateTables tables = new CostRateTables();
		tables.myNames = (String[])_stream.readObject();

		final ArrayList[] costRates = (ArrayList[])_stream.readObject();
		tables.myCostRateTableArray = new CostRateTable[ costRates.length ];

		for (int i = 0; i < costRates.length; i++)
		{
			tables.myCostRateTableArray[ i ] = (costRates[ i ] == null)
				? null
				: new CostRateTable( tables.myNames[ i ], costRates[ i ] );
		}

		return tables;
	}

	public static CostRateTables deserializeCompact( 
		final ObjectInputStream _stream )
		throws IOException, ClassNotFoundException
	{
		final CostRateTables table = new CostRateTables();
		table.myNames = (String[])_stream.readObject();

		final int count = _stream.readInt();
		table.myCostRateTableArray = null;

		if (count >= 0)
		{
			table.myCostRateTableArray = new CostRateTable[ count ];

			for (int index = 0; index < count; index++)
			{
				boolean hasTable = _stream.readBoolean();

				if (hasTable == true)
				{
					CostRateTable ct = new CostRateTable();
					ct.deserializeCompact( _stream );
					table.myCostRateTableArray[ index ] = ct;
				}
			}
		}

		return table;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.costing.Cost#fieldHideOvertimeRate(com.projity.field.FieldContext)
	 */
	@Override
	public boolean fieldHideOvertimeRate( 
		final FieldContext _fieldContext )
	{
		return getCurrent().fieldHideOvertimeRate( _fieldContext );
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.costing.Rate#getCostPerUse()
	 */
	@Override
	public double getCostPerUse()
	{
		return getCurrent().getCostPerUse();
	}

	public CostRateTable getCostRateTable( 
		final int _index )
	{
		if (myCostRateTableArray[ _index ] == null)
		{
			myCostRateTableArray[ _index ] = new CostRateTable( getName( _index ) );
		}

		return myCostRateTableArray[ _index ];
	}

	private CostRate getCurrent()
	{
		return (CostRate)myCostRateTableArray[ DEFAULT ].findCurrent();
	}

	@Override
	public long getEffectiveDate()
	{
		return getCurrent().getEffectiveDate();
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.costing.Rate#getOvertimeRate()
	 */
	@Override
	public Rate getOvertimeRate()
	{
		return getCurrent().getOvertimeRate();
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.costing.Rate#getStandardRate()
	 */
	@Override
	public Rate getStandardRate()
	{
		return getCurrent().getStandardRate();
	}

	@Override
	public boolean isReadOnlyEffectiveDate( 
		final FieldContext _fieldContext )
	{
		return getCurrent().isReadOnlyEffectiveDate( _fieldContext );
	}

	public void serialize( 
		final ObjectOutputStream _stream )
		throws IOException
	{
		_stream.writeObject( myNames );

		final ArrayList[] costRates = new ArrayList[ myCostRateTableArray.length ];

		for (int i = 0; i < costRates.length; i++)
		{
			costRates[ i ] = (myCostRateTableArray[ i ] == null)
				? null
				: myCostRateTableArray[ i ].getValueObjects();
		}

		_stream.writeObject( costRates );
	}

	public void serializeCompact( 
		final ObjectOutputStream _stream )
		throws IOException
	{
		_stream.writeObject( myNames );

		if (myCostRateTableArray == null)
		{
			_stream.writeInt( -1 );
		}
		else
		{
			_stream.writeInt( myCostRateTableArray.length );

			for (int i = 0; i < myCostRateTableArray.length; i++)
			{
				_stream.writeBoolean( myCostRateTableArray[ i ] != null );

				if (myCostRateTableArray[ i ] != null)
				{
					myCostRateTableArray[ i ].serializeCompact( _stream );
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.costing.Cost#setCostPerUse(double)
	 */
	@Override
	public void setCostPerUse( 
		final double _costPerUse )
	{
		getCurrent().setCostPerUse( _costPerUse );
	}

	public void setCostRateTable( 
		final int _index,
		CostRateTable _table )
	{
		myCostRateTableArray[ _index ] = _table;
	}

	@Override
	public void setEffectiveDate( 
		final long _effectiveDate )
		throws InvalidValueObjectForIntervalException
	{
//		getCurrent().setEffectiveDate( _effectiveDate );
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.costing.Cost#setOvertimeRate(double)
	 */
	@Override
	public void setOvertimeRate( 
		final Rate _overtimeRate )
	{
		getCurrent().setOvertimeRate( _overtimeRate );
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.costing.Cost#setStandardRate(double)
	 */
	@Override
	public void setStandardRate( 
		final Rate standardRate )
	{
		getCurrent().setStandardRate( standardRate );
	}

	String getName( 
		final int _index )
	{
		if (myNames == null)
		{
			myNames = Settings.COST_RATE_NAMES.split( ";" );
		}

		return myNames[ _index ];
	}

	public static final int DEFAULT = 0;
	protected CostRateTable[] myCostRateTableArray;
	protected String[] myNames = null;
}
