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

import com.projity.grouping.core.model.NodeModel;
import com.projity.grouping.core.model.NodeModelDataFactory;

import com.projity.pm.calendar.WorkCalendar;

import com.projity.strings.Messages;

import com.projity.util.DateTime;
import com.projity.util.Environment;
import com.projity.util.MathUtils;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

/**
 *
 */
public abstract class ValueObjectForIntervalTable
	implements Cloneable,
		Comparator<ValueObjectForInterval>,
		NodeModelDataFactory,
		Serializable
{
	/** Null constructor for ValueObjectForIntervalTable
	 * Serialization
	 */
	public ValueObjectForIntervalTable()
	{
		super();
		
		myValueObjects = new ArrayList<ValueObjectForInterval>();
	}

	public ValueObjectForIntervalTable( 
		final String _name,
		final ArrayList<ValueObjectForInterval> _valueObjects )
	{ 
		myName = _name;
		myValueObjects = _valueObjects;
	}

	public ValueObjectForIntervalTable( 
		final String _name )
	{
		this();
		
		myName = _name;

		// put in default one
		myValueObjects.add( createValueObject( ValueObjectForInterval.NA_TIME, 0.0D ) ); 
	}

	/** Copy constructor for ValueObjectForIntervalTable
	 * 
	 * @param _source used as the source of information to copy
	 */
	protected ValueObjectForIntervalTable(
		final ValueObjectForIntervalTable _source )
	{
		super();
		
		myName = _source.myName;
		myValueObjects = new ArrayList<ValueObjectForInterval>();
		
		for (Iterator<ValueObjectForInterval> itor = _source.myValueObjects.iterator(); itor.hasNext() == true;)
		{
			myValueObjects.add( itor.next().clone() );
		}
	}
	
	/** Add a new interval to the table with the specified start date and value. The end date will default to the maximum
	 * date possible.
	 * 
	 * @param _start used at the start date
	 * @param _value used as the value
	 * @return 
	 */
	public ValueObjectForInterval add( 
		final long _start,
		final double _value )
	{
		return addInterval( _start, DateTime.getMaxDate().getTime(), _value );
	}

	/**
	 * 
	 * @param _start
	 * @param _finish
	 * @param _value
	 * @return 
	 */
	public ValueObjectForInterval addInterval( 
		long _start,
		long _end,
		final double _value )
	{
		System.out.println( "add interval " + DateTime.medString( _start ) + " " + DateTime.medString( _end ) + " " + _value +
			"  as millis " + (_value * WorkCalendar.MILLIS_IN_HOUR) );

		if (_start > DateTime.getMaxDate().getTime())
		{
			return null;
		}

		if (_end < 0)
		{
			return null;
		}

		_start = inRange( _start );
		_end = inRange( _end );

		if (_start == _end)
		{
			return null;
		}

		try
		{
			final ValueObjectForInterval newOne = newValueObject( _start, _value );
			newOne.setEnd( _end );
			
			return newOne;
		}
		catch (InvalidValueObjectForIntervalException e)
		{
			return null;
		}
	}

	@Override
	public void addUnvalidatedObject( 
		final Object _object,
		final NodeModel _nodeModel,
		final Object _parent )
	{
	}

	/**
	 * Adjust the start date of a value object. Assure that it is in valid range, and adjust previous element's end as well as
	 * this one's start
	 *
	 * @param _newStart
	 * @param _valueObject
	 *                                                                                                                                                                                                  InvalidValueObjectForIntervalException
	 */
	public void adjustStart( 
		final long _newStart,
		final ValueObjectForInterval _valueObject )
		throws InvalidValueObjectForIntervalException
	{
		int index = myValueObjects.indexOf( _valueObject );

		if (index == 0)
		{
			return;
		}

		final ValueObjectForInterval previous = (ValueObjectForInterval)myValueObjects.get( index - 1 );

		if (_newStart <= previous.getStart())
		{
			throw new InvalidValueObjectForIntervalException( Messages.getString( 
					"ValueObjectForIntervalTable.ThisDateMustBeAfter" ) );
		}

		// see if this would disappear
		if (_newStart >= _valueObject.getEnd()) 
		{
			throw new InvalidValueObjectForIntervalException( Messages.getString( 
					"ValueObjectForIntervalTable.ThisDateMustBeBefore" ) );
		}

		previous.setEnd( _newStart );
		_valueObject.setStart( _newStart );
		removeZeros();
	}

	@Override
	public abstract ValueObjectForIntervalTable clone();

	@Override
	public int compare( 
		final ValueObjectForInterval _1,
		final ValueObjectForInterval _2 )
	{
		return MathUtils.signum( _1.getStart() - _2.getStart() );
	}

	/**
	 * Create a new entry one year later
	 */
	@Override
	public Object createUnvalidatedObject( 
		final NodeModel _nodeModel,
		final Object _parent )
	{
		// get last one
		final ValueObjectForInterval last = myValueObjects.get( myValueObjects.size() - 1 ); 

		long baseDate = DateTime.midnightToday();

		// latest of today or last entry
		baseDate = Math.max( baseDate, last.getStart() ); 

		final GregorianCalendar cal = DateTime.calendarInstance();
		cal.setTimeInMillis( baseDate );
		
		// one year later than last one's start or today
		cal.roll( GregorianCalendar.YEAR, true ); 

		final long date = cal.getTimeInMillis();

		try
		{
			return newValueObject( date, 0.0D );
		}
		catch (InvalidValueObjectForIntervalException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace(); // should not ever happen

			return null;
		}
	}

	protected abstract ValueObjectForInterval createValueObject();

	protected abstract ValueObjectForInterval createValueObject( long _date, double _value );

	protected static ValueObjectForIntervalTable deserialize( 
		final ObjectInputStream _stream,
		final ValueObjectForIntervalTable _valueTable )
		throws IOException, 
			ClassNotFoundException
	{
		_valueTable.myName = (String)_stream.readObject();
		_valueTable.myValueObjects = (ArrayList)_stream.readObject();

		return _valueTable;
	}

	public void deserializeCompact( 
		final ObjectInputStream _stream )
		throws IOException, 
			ClassNotFoundException
	{
		myName = (String)_stream.readObject();

		int count = _stream.readInt();

		for (int i = 0; i < count; i++)
		{
			ValueObjectForInterval v = createValueObject();
			v.deserializeCompact( _stream, this );
			myValueObjects.add( v );
		}
	}

	/**
	 * Finds the Rate/Availability which is on or before a date
	 *
	 * @param _date
	 *
	 * @return
	 */
	public ValueObjectForInterval findActive( 
		final long _date )
	{
		return myValueObjects.get( findActiveIndex( _date ) );
	}

	private int findActiveIndex( 
		final long _date )
	{
		final ValueObjectForInterval find = createValueObject( _date, 0.0D );

		// find it
		int index = Collections.binarySearch( myValueObjects, find, this ); 

		// binary search is weird.  The element before is -index - 2
		if (index < 0) 
		{
			// gets index of element before
			index = -index - 2; 
		}

		return index;
	}

	public ValueObjectForInterval findCurrent()
	{
		return findActive( System.currentTimeMillis() );
	}

	public long getEnd()
	{
		long end = 0;
		Iterator<ValueObjectForInterval> itor = myValueObjects.iterator();

		while (itor.hasNext() == true)
		{
			end = Math.max( end, itor.next().getEnd() );
		}

		return end;
	}

	@Override
	public NodeModelDataFactory getFactoryToUseForChildOfParent( 
		final Object _impl )
	{
		return this;
	}

	public List<ValueObjectForInterval> getList()
	{
		return Collections.unmodifiableList( myValueObjects );
	}

	public final String getName()
	{
		return myName;
	}

	public long getStart()
	{
		long start = DateTime.getMaxDate().getTime();
		final Iterator<ValueObjectForInterval> itor = myValueObjects.iterator();

		while (itor.hasNext() == true)
		{
			start = Math.min( start, itor.next().getStart() );
		}

		return start;
	}

	public ArrayList<ValueObjectForInterval> getValueObjects()
	{ 
		//serialization
		return myValueObjects;
	}

	private static long inRange( 
		final long _val )
	{
		if (_val < 0)
		{
			return 0;
		}

		if (_val > DateTime.getMaxDate().getTime())
		{
			return DateTime.getMaxDate().getTime();
		}

		return _val;
	}

//	public void initAfterCloning()
//	{
//		for (Iterator<ValueObjectForInterval> itor = myValueObjects.iterator(); itor.hasNext() == true;)
//		{
//			itor.next().setTable( this );
//		}
//	}

	/**
	 * A factory method returning a new value at a given date
	 *
	 * @param _start
	 *
	 * @return
	 *
	 * @throws InvalidValueObjectForIntervalException
	 */
	protected ValueObjectForInterval newValueObject( 
		final long _start,
		final double _value )
		throws InvalidValueObjectForIntervalException
	{
		final ValueObjectForInterval newOne = createValueObject( _start, _value );

		// Is this the first one?
		if (myValueObjects.isEmpty() == true)
		{
			// Yes, just add to end
			newOne.setEnd( DateTime.getMaxDate().getTime() );
			myValueObjects.add( newOne );
			return newOne;
		}
		
		// determine where to insert the new one
		int index = Collections.binarySearch( myValueObjects, newOne, this );

		// Not found?
		if (index < 0)
		{
			index = -index - 1;

			if ((index == 0)
			 && (myValueObjects.isEmpty() == false))
			{
				final ValueObjectForInterval next = (ValueObjectForInterval)myValueObjects.get( 0 );
				newOne.setEnd( next.getStart() );
			}
			else
			{
				// get previous element
				final ValueObjectForInterval previous = (ValueObjectForInterval)myValueObjects.get( index - 1 );

				//set new one's end to prevous end
				newOne.setEnd( previous.getEnd() );

				// set previous end to this start
				previous.setEnd( _start );
			}
			
			// add new one into it's proper place
			myValueObjects.add( index, newOne );

		}
		else
		{
			// The start date was found in the list. This is not allowed, so send back an error
			if ((Environment.isNoPodServer() == false) 
			 || (index != (myValueObjects.size() - 1)))
			{
				throw new InvalidValueObjectForIntervalException(Messages.getString( 
						"ValueObjectForIntervalTable.ThatEffectiveDateIsAlreadyInTheTable" ));
			}

			// ???
			final ValueObjectForInterval found = (ValueObjectForInterval)myValueObjects.get( index );
			found.setStart( newOne.getEnd() );
			myValueObjects.add( index, newOne );
		}

		removeZeros();

		return newOne;
	}

	@Override
	public void remove( 
		final Object _toRemove,
		final NodeModel _nodeModel,
		final boolean _deep,
		final boolean _undo,
		final boolean _removeDependencies )
	{
		try
		{
			remove( (ValueObjectForInterval)_toRemove );
		}
		catch (InvalidValueObjectForIntervalException e)
		{
//			Alert.error(e.getMessage());
//			throw new NodeException( e );
		}
	}

	/**
	 * Remove an entry from the table
	 *
	 * @param interval object
	 *
	 * @throws InvalidValueObjectForIntervalException if it's the first element
	 */
	public void remove( 
		final ValueObjectForInterval _removeMe )
		throws InvalidValueObjectForIntervalException
	{
		if (_removeMe.isFirst() == true) 
		{
			// don't allow removal of first value
			throw new InvalidValueObjectForIntervalException( Messages.getString( 
					"ValueObjectForIntervalTable.YouCannotRemoveTheFirst" ) );
		}

		final int index = myValueObjects.indexOf( _removeMe );

		// set previous end to this end
		final ValueObjectForInterval previous = (ValueObjectForInterval)myValueObjects.get( index - 1 ); 
		
		previous.setEnd( _removeMe.getEnd() );
		myValueObjects.remove( _removeMe );
		removeZeros();
	}

	private void removeZeros()
	{
		final Iterator<ValueObjectForInterval> itor = myValueObjects.iterator();

		while (itor.hasNext() == true)
		{
			final ValueObjectForInterval value = itor.next();

			if (value.getStart() >= value.getEnd())
			{
				itor.remove();
			}
		}
	}

	@Override
	public void rollbackUnvalidated( 
		final NodeModel _nodeModel,
		final Object _object )
	{
//		try 
//		{
			remove( _object, _nodeModel, false, true, true );
//		} catch (NodeException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	//	public void fireCreated(Object newlyCreated){}
	public void serialize( 
		final ObjectOutputStream _stream )
		throws IOException
	{
		_stream.writeObject( myName );
		_stream.writeObject( myValueObjects );
	}

	public void serializeCompact( 
		final ObjectOutputStream _stream )
		throws IOException
	{
		_stream.writeObject( myName );

		if (myValueObjects == null)
		{
			_stream.writeInt( -1 );

			return;
		}

		_stream.writeInt( myValueObjects.size() );

		for (Iterator<ValueObjectForInterval> itor = myValueObjects.iterator(); itor.hasNext() == true;)
		{
			itor.next().serializeCompact( _stream, this );
		}
	}

	public final void setName(
		final String _value )
	{
		myName = _value;
	}
	
	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString( this );
	}

	@Override
	public void validateObject( 
		final Object _newlyCreated,
		final NodeModel _nodeModel,
		final Object _eventSource,
		final Object _hierarchyInfo,
		final boolean _isNew )
	{
	}

	static final long serialVersionUID = 7728399282882L;
	
	private static final long NO_VALUE = -1L;

	private ArrayList<ValueObjectForInterval> myValueObjects;
	private String myName = "";
}
