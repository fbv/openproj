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

import com.projity.algorithm.CollectionIntervalGenerator;
import com.projity.algorithm.Query;
import com.projity.algorithm.SelectFrom;
import com.projity.algorithm.WeightedSum;

import com.projity.datatype.Money;
import com.projity.grouping.core.model.DefaultNodeModel;

import com.projity.grouping.core.model.NodeModel;

import com.projity.interval.ValueObjectForInterval;
import com.projity.interval.ValueObjectForIntervalTable;

import com.projity.pm.calendar.WorkCalendar;

import com.projity.undo.DataFactoryUndoController;

import com.projity.util.DateTime;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collection;



public class CurrencyRateTable
	extends ValueObjectForIntervalTable
{
	public CurrencyRateTable( 
		final String _baseCurrency,
		final String _displayCurrency )
	{
		super( _displayCurrency );

		// put in initial non  zero value;
		add( 0, 1.0 ); 
		
		myBaseCurrency = _baseCurrency;
		Money.setCurrency( _displayCurrency );

//		initUndo();
	}

	private CurrencyRateTable()
	{
		super();

		// put in initial non  zero value;
		add( 0, 1.0 ); 

//		initUndo();
	}

	/** Copy constructor for CurrencyRateTable
	 * 
	 * @param _source used as the source of information to copy
	 */
	protected CurrencyRateTable(
		final CurrencyRateTable _source )
	{
		super( _source );
	}

	public double calculateWeightedRateOverPeriod( 
		final long _start,
		final long _end,
		final WorkCalendar _cal )
	{
		final long duration = _cal.compare( _end, _start, false );

		if (duration == 0)
		{
			return 0;
		}

		final SelectFrom clause = SelectFrom.getInstance();
		// go through project which will delegate to global value. Should realy be project related, not global
		final CollectionIntervalGenerator currencyRate = CollectionIntervalGenerator.getInstance( getList() ); 
		final WeightedSum weightedSum = WeightedSum.getInstance( _cal, currencyRate );

		clause.select( weightedSum ).from( currencyRate ).whereInRange( _start, _end );

		Query.getInstance().selectFrom( clause ).execute();

		return weightedSum.getValue() / duration;
	}

	@Override
	public CurrencyRateTable clone()
	{
		return new CurrencyRateTable( this );
	}
	
	public static Number convertFromDisplay( 
		final Number _value )
	{
		if (_value == null)
		{
			return null;
		}

		if (myInstance == null)
		{
			return 1.0;
		}

		final double newValue = _value.doubleValue() / myInstance.getMultiplier();

		//		System.out.println("convert from display " + value + " " + newValue);		
		if (_value instanceof Money == true)
		{
			//System.out.println("returning Money value of: "+ Money.getInstance(newValue) +" from CurrencyRateTable.convertFromDisplay");
			return Money.getInstance( newValue );
		}
		else
		{
			//System.out.println("returning Double value of: "+ Double.valueOf(newValue) +" from CurrencyRateTable.convertFromDisplay");
			return Double.valueOf( newValue );
		}
	}

	public static Number convertToDisplay( 
		final Number _value )
	{
		if (_value == null)
		{
			return null;
		}

		if (myInstance == null)
		{
			getInstance();
		}

		final double newValue = _value.doubleValue() * myInstance.getMultiplier();

		//System.out.println("convert to display " + value + " " + newValue);		
		if (_value instanceof Money == true)
		{
			//System.out.println("returning Money value of: "+ Money.getInstance(newValue) +" from CurrencyRateTable.convertToDisplay");
			return Money.getInstance( newValue );
		}
		else
		{
			//System.out.println("returning Double value of: "+ Double.valueOf(newValue) +" from CurrencyRateTable.convertToDisplay");
			return Double.valueOf( newValue );
		}
	}

	@Override
	public NodeModel createNodeModel()
	{
		return new DefaultNodeModel( this );
	}

	@Override
	public NodeModel createNodeModel( 
		final Collection _collection )
	{
		NodeModel model = new DefaultNodeModel( this );
		model.addImplCollection( null, _collection, NodeModel.SILENT );

		return model;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.costing.ValueObjectForIntervalTable#createValueObject(long)
	 */
	@Override
	protected ValueObjectForInterval createValueObject( 
		final long _date,
		final double _value )
	{
		return new CurrencyRate( _date, _value );
	}

	@Override
	protected ValueObjectForInterval createValueObject()
	{
		return new CurrencyRate();
	}

	public static CurrencyRateTable deserialize( 
		final ObjectInputStream _stream )
		throws IOException, 
			ClassNotFoundException
	{
		return (CurrencyRateTable)deserialize( _stream, new CurrencyRateTable() );
	}

	public String getBaseCurrency()
	{
		return myBaseCurrency;
	}

	/* (non-Javadoc)
	 * @see com.projity.configuration.NamedItem#getCategory()
	 */
	public String getCategory()
	{
		return "CurrencyRate";
	}

	public String getDisplayCurrency()
	{
		return getName();
	}

	public long getEffectiveDate()
	{
		return myEffectiveDate;
	}

	public static CurrencyRateTable getInstance()
	{
		if (null == myInstance)
		{
			myInstance = new CurrencyRateTable();
		}

		return myInstance;
	}

	private double getMultiplier()
	{
		if (isActive() == false)
		{
			System.out.println( "not active - returning 1.0" );

			return 1.0D;
		}

		return ((CurrencyRate)findActive( myEffectiveDate )).getRate();
	}

	@Override
	public DataFactoryUndoController getUndoController()
	{
		return myUndoController;
	}

	@Override
	public void initOutline( 
		final NodeModel _nodeModel )
	{
	}

	protected void initUndo()
	{
		myUndoController = new DataFactoryUndoController(this);
	}

	public static boolean isActive()
	{
		if (myInstance == null)
		{
			return false;
		}

		return myInstance.myActive;
	}

//	@Override
//	public final boolean isGroupDirty()
//	{
//		return myIsGroupDirty;
//	}

	public static void setActive( 
		final boolean _active )
	{
		if (myInstance == null)
		{
			return;
		}

		myInstance.myActive = _active;
	}

	public void setBaseCurrency( 
		final String _baseCurrency )
	{
		myBaseCurrency = _baseCurrency;
	}

	public void setDisplayCurrency( 
		final String _displayCurrency )
	{
		Money.setCurrency( _displayCurrency );
		setName( _displayCurrency );
	}

	public void setEffectiveDate( 
		final long _effectiveDate )
	{
		System.out.println( "xxxx setting effective date to " + new java.util.Date( _effectiveDate ) );
		myEffectiveDate = _effectiveDate;
	}

	@Override
	public final void setGroupDirty( 
		final boolean _isGroupDirty )
	{
		myIsGroupDirty = _isGroupDirty;
	}

	public static void setInstance( 
		final CurrencyRateTable _table )
	{
		myInstance = _table;
	}

	private static final long serialVersionUID = 9110087379489649891L;
	private static CurrencyRateTable myInstance = null;

	//Undo
	protected transient DataFactoryUndoController myUndoController;
	String myBaseCurrency = "USD";
	private boolean myActive = true;
	protected boolean myIsGroupDirty = false;
	private long myEffectiveDate = DateTime.midnightToday(); // default use today;

	@Override
	public void setUndoController( DataFactoryUndoController _undoController )
	{
		throw new UnsupportedOperationException( "Not supported yet." );
	}
}
