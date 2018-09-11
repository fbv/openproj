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
package com.projity.pm.calendar;

import org.openproj.domain.calendar.HasCalendar;
import com.projity.configuration.CircularDependencyException;
import com.projity.field.FieldContext;

import com.projity.grouping.core.Node;
import com.projity.grouping.core.hierarchy.NodeHierarchy;

import com.projity.options.CalendarOption;

import com.projity.pm.key.HasCommonKeyImpl;

import com.projity.strings.Messages;

import com.projity.util.DateTime;
import com.projity.util.Environment;

import org.apache.commons.collections.Closure;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;


/**
 * Calendar functions
 */
public final class WorkingCalendar
	implements WorkCalendar,
		Serializable,
		Comparable<WorkingCalendar>
{
	private WorkingCalendar()
	{ 
		// for static
		super();
	}

	// JGao 9/18/2009 Added this method to support the working hours for Mariner. In Mariner, the working hours is either
	// nonstop (24 hrs) nor 8:00 -12:00, 13:00 - 17:00 but rather, continous one block, i.e 8:00 - 16:00. As a result,
	// I am clone the working hours of first working day.
	private WorkingHours getDefaultWorkingHours()
	{
		boolean isPodServer = !Environment.isNoPodServer();
		WorkingHours hours = null;

		if (isPodServer == true)
		{
			hours = (WorkingHours)(CalendarOption.getInstance().isAddedCalendarTimeIsNonStop()
				? WorkingHours.getNonStop().clone()
				: WorkingHours.getDefault().clone());
		}
		else
		{
			// Clone working hours from first working day of the base calendar
			CalendarDefinition baseDefintion = baseCalendar.getConcreteInstance();

			for (int i = 0; i < 7; i++)
			{
				WorkDay w = baseDefintion.getWeekDay( i );

				if ((w == null) || (w.getWorkingHours() == null) || !w.getWorkingHours().hasHours())
				{
					continue;
				}

				hours = (WorkingHours)w.getWorkingHours().clone();

				break;
			}
		}

		return hours;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.time.WorkCalendar#add(long, long, boolean)
	 */
	@Override
	public final long add( 
		final long _date,
		final long _duration,
		final boolean _useSooner )
	{
//		if (date == 0)
//			DebugUtils.dumpStack("0 date");
		return getConcreteInstance().add( _date, _duration, _useSooner );
	}

	public final void addCalendarTime( 
		long _start,
		long _end )
	{
		_start = DateTime.dayFloor( _start );
		_end = DateTime.dayFloor( _end );

		// JGao 9/18/2009 Moved getting working hours from the for loop as it won't change during the loop
		WorkingHours hours = getDefaultWorkingHours();

		for (long day = _start; day < _end; day = DateTime.nextDay( day ))
		{
			WorkDay workDay = new WorkDay(day, day);
			workDay.setWorkingHours( hours );
			addOrReplaceException( workDay );
		}

		invalidate(); // the calendar needs to be reevaluated
	}

	// JGao 9/18/2009 Consolidate this function to use another addCalendarTime function after the above change
	public final void addCalendarTime( 
		final long _start )
	{
//		start = DateTime.dayFloor(start);
		addCalendarTime( _start, DateTime.nextDay( _start ) );

//		WorkDay workDay = new WorkDay(start, start);
//		WorkingHours hours = GetDefaultWorkingHours();

//		// Clone working hours from first working day of the base calendar
//		CalendarDefinition baseDefintion = baseCalendar.getConcreteInstance();
//		for (int i = 0; i < 7; i++) {
//			WorkDay w = baseDefintion.getWeekDay(i);
//			if (w == null || w.getWorkingHours() == null || !w.getWorkingHours().hasHours())
//				continue;
//
//			hours = (WorkingHours) w.getWorkingHours().clone();
//			break;
//		}

//		workDay.setWorkingHours(hours);
//		addOrReplaceException(workDay);
//		invalidate(); // the calendar needs to be reevaluated
	}

	public final void addObjectUsing( 
		final HasCalendar _cal )
	{
		getObjectsUsing().add( _cal );
	}

	public final void addOrReplaceException( 
		final WorkDay _exceptionDay )
	{
		_exceptionDay.initialize(); // make sure cached duration is set
		differences.addOrReplaceException( _exceptionDay );
	}

	@Override
	public final long adjustInsideCalendar( 
		final long _date,
		final boolean _useSooner )
	{
		return getConcreteInstance().adjustInsideCalendar( _date, _useSooner );
	}

//	public boolean isNew() {
//		return hasKey.isNew();
//	}
	
//	public void setNew(boolean isNew) {
//		hasKey.setNew(isNew);
//	}
	
	public final void assignFrom( 
		final WorkingCalendar _source )
	{
		baseCalendar = _source.baseCalendar;

		if (!_source.getName().equals( getName() ))
		{
			setName( _source.getName() );

			//			CalendarService.getInstance().add(this);
		}

		differences = _source.differences;
		concrete = null;
	}

	public final void changeBaseCalendar( 
		final WorkCalendar _baseCalendar )
		throws CircularDependencyException
	{
		setBaseCalendar( _baseCalendar );
	}

	public static void cleanUp()
	{
		_24HoursInstance = null;
		defaultInstance = null;
		nightShiftInstance = null;
		myStandardInstance = null;
	}

	@Override
	public final WorkingCalendar clone()
		throws CloneNotSupportedException
	{
		WorkingCalendar cal = (WorkingCalendar)super.clone();

		//	cal.hasKey = new HasCommonKeyImpl(this);
		cal.setName( getName() );

		return cal;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.time.WorkCalendar#compare(long, long, boolean)
	 */
	@Override
	public final long compare( 
		final long _laterDate,
		final long _earlierDate,
		final boolean _elapsed )
	{
		return getConcreteInstance().compare( _laterDate, _earlierDate, _elapsed );
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public final int compareTo( 
		final WorkingCalendar _other )
	{
		if (_other == null)
		{
			return 1;
		}

//		if (!(arg0 instanceof WorkingCalendar))
//		{
//			return -1;
//		}

		return getName().compareTo( _other.getName() );
	}

	/**
	 * Test for circular dependency
	 */
	@Override
	public final boolean dependsOn( 
		final WorkCalendar _cal )
	{
		if (this == _cal)
		{
			return true;
		}

		WorkCalendar base = getBaseCalendar();

		if (base == null)
		{
			return false;
		}

		return base.dependsOn( _cal );
	}

	@Override
	public void deserialiseCompact( 
		final ObjectInputStream _stream )
		throws IOException, 
			ClassNotFoundException
	{
		version = _stream.readByte();

		byte baseCalendarType = _stream.readByte();

		if (baseCalendarType > 0)
		{
			baseCalendar = (baseCalendarType == 1)
				? WorkingCalendar.getInstance()
				: new CalendarDefinition();
			baseCalendar.deserialiseCompact( _stream );
		}
		else
		{
			baseCalendar = null;
		}

		differences = new CalendarDefinition();
		differences.deserialiseCompact( _stream );
		fixedId = _stream.readInt();
		serializedName = (String)_stream.readObject();

		hasKey = HasCommonKeyImpl.deserialize( _stream, this );

		if (serializedName == null)
		{
			serializedName = "";
		}

		setName( serializedName );

		if (baseCalendar == null)
		{
			baseCalendar = CalendarService.getInstance().getStandardInstance();
		}

		CalendarService.getInstance().add( this );
	}

	public final String dump()
	{
		StringBuilder result = new StringBuilder( "Calendar " + getName() + "\n" );
		result.append( "weekdays\n" );

		for (int i = 0; i < 7; i++)
		{
			result.append( "day[" ).append( i ).append( "]" ).append( getWeekDay( i ) ).append( "\n");
		}

		result.append( "There are " ).append( differences.getDayExceptions() ).append( " exceptions\n");

		Iterator<WorkDay> itor = differences.getDayExceptions().iterator();

		while (itor.hasNext() == true)
		{
			result.append( "exception" ).append( itor.next().toString());
		}

		return result.toString();
	}

	@Override
	public final boolean equals( 
		final Object _arg0 )
	{
		return (this == _arg0);
	}

	public static ArrayList<WorkingCalendar> extractCalendars( 
		final Collection<? extends HasCalendar> _collection )
	{
		ArrayList<WorkingCalendar> list = new ArrayList<WorkingCalendar>();
		Iterator<? extends HasCalendar> itor = _collection.iterator();

		while (itor.hasNext() == true)
		{
			final WorkingCalendar cal = (WorkingCalendar)itor.next().getWorkCalendar();

			if (cal != null)
			{
				list.add( cal );
			}
		}

		Collections.sort( list );

		return list;
	}

	public static ArrayList extractCalendars( 
		final NodeHierarchy _hierarchy )
	{
		final ArrayList list = new ArrayList();
		_hierarchy.visitAll( 
			new Closure()
		{
			@Override
			public void execute( 
				final Object _arg0 )
			{
				if (_arg0 != null)
				{
					Object impl = ((Node)_arg0).getValue();

					if (impl instanceof HasCalendar)
					{
						list.add( impl );
					}
				}
			}
		} );

		return WorkingCalendar.extractCalendars( list );
	}

	public final WorkDay findExceptionDay( 
		final long _date )
	{
		return differences.findExceptionDay( _date );
	}

	static WorkingCalendar get24HoursInstance()
	{
		if (_24HoursInstance != null)
		{
			return _24HoursInstance;
		}

		_24HoursInstance = getStandardBasedInstance();

		WorkDay working = null;
		working = new WorkDay();

		try
		{
			working.getWorkingHours().setInterval( 0, hourTime( 0 ), hourTime( 0 ) );
		}
		catch (WorkRangeException e)
		{
			e.printStackTrace();
		}

		_24HoursInstance.setWeekends( working );
		_24HoursInstance.setWeekDays( working );

		_24HoursInstance.setName( Messages.getString( "Calendar.24Hours" ) );
		_24HoursInstance.setFixedId( 2 );
		CalendarService.getInstance().add( _24HoursInstance ); // put standard calendar in list

		return _24HoursInstance;
	}

	/**
	 * @return Returns the baseCalendar.
	 */
	@Override
	public final WorkCalendar getBaseCalendar()
	{
		return baseCalendar;
	}

	@Override
	public final String getCategory()
	{
		return CALENDAR_CATEGORY;
	}

	@Override
	public final CalendarDefinition getConcreteInstance()
	{
		if (concrete == null)
		{
			final WorkCalendar base = baseCalendar;
			concrete = new CalendarDefinition( (base == null)
				? null
				: base.getConcreteInstance(), differences );
		}

		return concrete;
	}

	private static WorkDay getDay( 
		final Collection<? extends WorkDay> _collection,
		final long _day )
	{
		final Iterator<? extends WorkDay> itor = _collection.iterator();
		while (itor.hasNext() == true)
		{
			final WorkDay current = itor.next();

			if (current.getStart() == _day)
			{
				return current;
			}
		}

		return null;
	}

	public static WorkingCalendar getDefaultInstance()
	{
		if (defaultInstance != null)
		{
			return defaultInstance;
		}

		defaultInstance = getStandardBasedInstance();
		defaultInstance.setName( Messages.getString( "Calendar.Standard" ) );
		defaultInstance.setFixedId( 1 );
		CalendarService.getInstance().add( defaultInstance );

		get24HoursInstance();
		getNightShiftInstance();

		return defaultInstance;
	}

	public final WorkDay getDerivedWeekDay( 
		final int _dayNum )
	{
		WorkDay day = differences.getWorkWeek().getWeekDay( _dayNum );

		if ((day == null) 
		 && (baseCalendar != null))
		{
			day = ((WorkingCalendar)baseCalendar).getDerivedWeekDay( _dayNum );
		}

		return day;
	}

	public final WorkDay[] getExceptionDays()
	{ 
		// get day exceptions in derived cal
		return differences.getExceptions();
	}

	public final int getFixedId()
	{
		return fixedId;
	}

	public final long getId()
	{
		return hasKey.getId();
	}

	public static WorkingCalendar getInstance()
	{
		return new WorkingCalendar();
	}

	public static WorkingCalendar getInstanceBasedOn( 
		final WorkCalendar _base )
	{
		WorkingCalendar cal = getInstance();

		try
		{
			cal.setBaseCalendar( _base );
		}
		catch (CircularDependencyException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return cal;
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public final String getName()
	{
		return hasKey.getName();
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public final String getName(
		final FieldContext _fieldContext )
	{
		return hasKey.getName();
	}

	@Override
	public final void serialiseCompact( 
		final ObjectOutputStream _stream )
		throws IOException
	{
		serializedName = getName();

		// don't serialize default base. treat it as null
		if (baseCalendar == CalendarService.getInstance().getStandardInstance()) 
		{
			baseCalendar = null;
		}

		_stream.writeByte( version );
		_stream.writeByte( (baseCalendar == null)
			? 0
			: ((baseCalendar instanceof WorkingCalendar)
				? 1
				: 2) ); //bad, to compress

		if (baseCalendar != null)
		{
			baseCalendar.serialiseCompact( _stream );
		}

		differences.serialiseCompact( _stream );
		_stream.writeInt( fixedId );
		_stream.writeObject( serializedName );

		hasKey.serialize( _stream );

		if (baseCalendar == null)
		{
			baseCalendar = CalendarService.getInstance().getStandardInstance(); // put it back so program will work
		}
	}

	final DayDescriptor getMonthDayDescriptor( 
		final long _date )
	{
		final DayDescriptor descriptor = new DayDescriptor();
		final int dayNum = CalendarDefinition.getDayOfWeek( _date );

		// is this day modified in derived calendar?
		descriptor.workDay = getDay( differences.getDayExceptions(), _date ); 
		descriptor.modified = descriptor.workDay != null;

		if (descriptor.workDay == null)
		{
			// try difference week day
			descriptor.workDay = differences.getWorkWeek().getWeekDay( dayNum ); 
		}

		// if not overrideen in derived calendar, see if base calendar has a special day
		if (descriptor.workDay == null) 
		{
			descriptor.workDay = getConcreteInstance().getWorkDay( _date );
		}

		if (descriptor.workDay == null) // return week day
		{
			descriptor.workDay = getConcreteInstance().getWorkWeek().getWeekDay( CalendarDefinition.getDayOfWeek( _date ) );
		}

		return descriptor;
	}

	static WorkingCalendar getNightShiftInstance()
	{
		if (nightShiftInstance != null)
		{
			return nightShiftInstance;
		}

		nightShiftInstance = WorkingCalendar.getStandardBasedInstance();

		final WorkDay nonWorking = new WorkDay();
		nonWorking.getWorkingHours().setNonWorking();

		final WorkDay working = new WorkDay();

		// will revert to overall default for sunday which is not working
		nightShiftInstance.setWeekDay( Calendar.SUNDAY - 1, null ); 

		final WorkDay monday = new WorkDay();

		try
		{
			monday.getWorkingHours().setInterval( 0, hourTime( 23 ), hourTime( 0 ) );
		}
		catch (WorkRangeException e)
		{
			e.printStackTrace();
		}

		nightShiftInstance.setWeekDay( Calendar.MONDAY - 1, monday );

		try
		{
			working.getWorkingHours().setInterval( 0, hourTime( 0 ), hourTime( 3 ) );
			working.getWorkingHours().setInterval( 1, hourTime( 4 ), hourTime( 8 ) );
			working.getWorkingHours().setInterval( 2, hourTime( 23 ), hourTime( 0 ) );
		}
		catch (WorkRangeException e)
		{
			e.printStackTrace();
		}

		nightShiftInstance.setWeekDay( Calendar.TUESDAY - 1, working );
		nightShiftInstance.setWeekDay( Calendar.WEDNESDAY - 1, working );
		nightShiftInstance.setWeekDay( Calendar.THURSDAY - 1, working );
		nightShiftInstance.setWeekDay( Calendar.FRIDAY - 1, working );

		WorkDay saturday = new WorkDay();

		try
		{
			saturday.getWorkingHours().setInterval( 0, hourTime( 0 ), hourTime( 3 ) );
			saturday.getWorkingHours().setInterval( 1, hourTime( 4 ), hourTime( 8 ) );
		}
		catch (WorkRangeException e)
		{
			e.printStackTrace();
		}

		nightShiftInstance.setWeekDay( Calendar.SATURDAY - 1, saturday );

		nightShiftInstance.setName( Messages.getString( "Calendar.NightShift" ) );
		nightShiftInstance.setFixedId( 3 );

		CalendarService.getInstance().add( nightShiftInstance ); // put night shift calendar in list

		return nightShiftInstance;
	}

	public final HashSet getObjectsUsing()
	{
		if (objectsUsing == null)
		{
			objectsUsing = new HashSet();
		}

		return objectsUsing;
	}

	public static WorkingCalendar getStandardBasedInstance()
	{
		WorkingCalendar cal = getInstance();

		try
		{
			cal.setBaseCalendar( WorkingCalendar.getStandardInstance() );
		}
		catch (CircularDependencyException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return cal; //TODO should share this instance
	}

	public static WorkingCalendar getStandardInstance()
	{
		if (myStandardInstance != null)
		{
			return myStandardInstance;
		}

		myStandardInstance = WorkingCalendar.getInstance();

		final WorkDay nonWorking = new WorkDay();
		nonWorking.getWorkingHours().setNonWorking();
		
		final WorkDay working = new WorkDay();

		try
		{
			working.getWorkingHours().setInterval( 0, hourTime( 8 ), hourTime( 12 ) );
			working.getWorkingHours().setInterval( 1, hourTime( 13 ), hourTime( 17 ) );
		}
		catch (WorkRangeException e)
		{
			e.printStackTrace();
		}

		myStandardInstance.setWeekends( nonWorking );
		myStandardInstance.setWeekDays( working ); // 8 hours

		myStandardInstance.setName( "default base" );

		return myStandardInstance;
	}

	@Override
	public final long getUniqueId()
	{
		return hasKey.getUniqueId();
	}

	/**
	 * @param _dayNum
	 * @return
	 */
	public final WorkDay getWeekDay( 
		final int _dayNum )
	{
		return differences.getWorkWeek().getWeekDay( _dayNum );
	}

	public final long getWeekDuration()
	{
		return getConcreteInstance().getWeekDuration();
	}

	final DayDescriptor getWeekDayDescriptor( 
		int _dayNum )
	{
		_dayNum -= 1; // SUNDAY is 1, so need to subtract 1

		DayDescriptor descriptor = new DayDescriptor();
		descriptor.workDay = differences.getWorkWeek().getWeekDay( _dayNum );

		descriptor.modified = descriptor.workDay != null;
		descriptor.workDay = getConcreteInstance().getWorkWeek().getWeekDay( _dayNum );

//		if (isBaseCalendar()) {
//			// for base calendars, the notion of modified is based on the default work week
//			WorkDay baseDay = WorkingCalendar.getDefaultInstance(null).getWeekDay(dayNum);
//			descriptor.modified = !(baseDay.hasSameWorkHours(descriptor.workDay));
//		} else {
//			if (descriptor.workDay == null)
//				descriptor.workDay = getConcreteInstance().week.getWeekDay(dayNum);
//		}
		
		return descriptor;
	}

	final void makeDefaultDay( 
		final long _date )
	{
		WorkDay day = getDay( differences.getDayExceptions(), _date );

		if (day != null)
		{
			differences.getDayExceptions().remove( day );
		}

		concrete = null;
	}

	final void makeDefaultWeekDay( 
		int _dayNum )
	{
		_dayNum -= 1; // SUNDAY is 1, so need to subtract 1
		differences.getWorkWeek().setWeekDay( _dayNum, null );
		concrete = null;
	}

	final void setDayWorkingHours( 
		final long _date,
		final WorkingHours _workingHours )
		throws WorkRangeException
	{
		_workingHours.validate();

		WorkDay day = new WorkDay( _date, _date );
		day.setWorkingHours( _workingHours );
		addOrReplaceException( day );
		concrete = null;
	}

	final void setWeekDayNonWorking( 
		int _dayNum )
	{
		_dayNum -= 1; // SUNDAY is 1, so need to subtract 1

		final WorkDay day = new WorkDay();
		differences.getWorkWeek().setWeekDay( _dayNum, day );
		concrete = null;
	}

	final void setWeekDayWorkingHours( 
		int _dayNum,
		final WorkingHours _workingHours )
		throws WorkRangeException
	{
		_dayNum -= 1; // SUNDAY is 1, so need to subtract 1
		_workingHours.validate();

		WorkDay day = new WorkDay();
		day.setWorkingHours( _workingHours );
		differences.getWorkWeek().setWeekDay( _dayNum, day );
		concrete = null;
	}

	private static long hourTime( 
		final int _hour )
	{
		return WorkingHours.hourTime( _hour );
	}

	public final WorkingCalendar intersectWith( 
		final WorkingCalendar _other )
		throws InvalidCalendarIntersectionException
	{
		CalendarDefinition newDef = new CalendarDefinition();

		// do week
		final WorkWeek weekResult = new WorkWeek();

		for (int i = 0; i < WorkWeek.DAYS_IN_WEEK; i++)
		{
			weekResult.workDay[ i ] = getDerivedWeekDay( i ).intersectWith( _other.getDerivedWeekDay( i ) );
		}

		weekResult.updateWorkingDuration();

		if (weekResult.getDuration() == 0) // a calendar cannot have no working time for its work week
		{
			throw new InvalidCalendarIntersectionException();
		}

		// do exceptions
		final CalendarDefinition thisDef = getConcreteInstance();
		final CalendarDefinition otherDef = _other.getConcreteInstance();
		WorkDay exceptionDay;

		// merge exceptions
		for (int i = 0; i < thisDef.getExceptions().length; i++)
		{
			exceptionDay = thisDef.getExceptions()[ i ];
			newDef.getDayExceptions().add( exceptionDay.intersectWith( otherDef.getWorkDay( exceptionDay.getStart() ) ) );
		}

		for (int i = 0; i < otherDef.getExceptions().length; i++)
		{
			exceptionDay = otherDef.getExceptions()[ i ];
			newDef.getDayExceptions().add( exceptionDay.intersectWith( thisDef.getWorkDay( exceptionDay.getStart() ) ) );
		}

		newDef.addSentinelsAndMakeArray();
		newDef.setWorkWeek( weekResult );

		final WorkingCalendar intersection = new WorkingCalendar();
		intersection.concrete = newDef;
		intersection.setName( "AssignCal: " + getName() + "/" + _other.getName() );

		return intersection;
	}

	@Override
	public final void invalidate()
	{
		concrete = null;
		CalendarService.getInstance().invalidate( this );
	}

	public final boolean isBaseCalendar()
	{
		return baseCalendar == getStandardInstance();
	}

	@Override
	public final boolean isDirty()
	{
		return dirty;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.calendar.WorkCalendar#isInvalid()
	 */
	@Override
	public final boolean isInvalid()
	{
		return concrete == null;
	}

	public final WorkingCalendar makeScratchCopy()
	{
		final WorkingCalendar newOne = new WorkingCalendar();
		newOne.baseCalendar = baseCalendar;
		newOne.setName( getName() );
		newOne.differences = differences.clone();

		return newOne;
	}

	public void notifyChanged()
	{
		concrete = null;
	}

	public static void setDefaultInstance( 
		final WorkingCalendar _wc )
	{
		defaultInstance = _wc;
	}

	final void setDayNonWorking( 
		final long _date )
	{
		WorkDay day = new WorkDay( _date, _date );
		addOrReplaceException( day );
		concrete = null;
	}

	private final void readObject( 
		final ObjectInputStream _stream )
		throws IOException, 
			ClassNotFoundException
	{
		//final ObjectInputStream.GetField fields = _stream.readFields();
		
		_stream.defaultReadObject();
		
		hasKey = HasCommonKeyImpl.deserialize( _stream, this );

		if (serializedName == null)
		{
			serializedName = "";
		}

		setName( serializedName );

		if (baseCalendar == null)
		{
			baseCalendar = CalendarService.getInstance().getStandardInstance();
		}

		CalendarService.getInstance().add( this );
	}

	public final void removeEmptyDays()
	{
		int nullCount = 0;

		for (int i = 0; i < 7; i++)
		{
			WorkDay w = getWeekDay( i );

			if (w == null)
			{
				continue;
			}

			if ((w.getWorkingHours() == null) || !w.getWorkingHours().hasHours())
			{
				setWeekDay( i, null );
			}

			if (getWeekDay( i ) == null)
			{
				nullCount++;
			}
		}

//		if ( nullCount == 7) { // if all nulls, copy default cal
//			for (int i = 0; i < 7; i++) {
//				setWeekDay(i,(WorkDay) getDefaultInstance().getWeekDay(i).clone());
//			}
//		}
	}

	public final void removeException( 
		final WorkDay _exceptionDay )
	{
		differences.getDayExceptions().remove( _exceptionDay ); // remove any existing
	}

	public final void removeObjectUsing( 
		final HasCalendar _cal )
	{
		getObjectsUsing().remove( _cal );
	}

	/**
	 * @param baseCalendar The baseCalendar to set.
	 */
	public final void setBaseCalendar( 
		final WorkCalendar _baseCalendar )
		throws CircularDependencyException
	{
		if ((_baseCalendar != null) && _baseCalendar.dependsOn( this )) // avoid circular
		{
			throw new CircularDependencyException(Messages.getString( "Calendar.ExceptionCircular" ));
		}

		baseCalendar = _baseCalendar;
	}

	@Override
	public final void setDirty( 
		final boolean _dirty )
	{
		//System.out.println("WorkingCalendar _setDirty("+dirty+"): "+getName());
		dirty = _dirty;
	}

	public final void setFixedId( 
		final int _fixedId )
	{
		fixedId = _fixedId;
	}

	public final void setId( 
		final long _id )
	{
		hasKey.setId( _id );
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public final void setName( 
		final String _name )
	{
		hasKey.setName( _name );
	}

	public static void setStandardInstance( 
		final WorkingCalendar _standard )
	{
		myStandardInstance = _standard;
		defaultInstance = getStandardBasedInstance();
		defaultInstance.setName( _standard.getName() );
		defaultInstance.setFixedId( 1 );
		CalendarService.getInstance().add( defaultInstance );
	}

	@Override
	public final void setUniqueId( 
		final long _id )
	{
		hasKey.setUniqueId( _id );
	}

	/**
	 * @param _dayNum
	 * @param _day
	 */
	public final void setWeekDay( 
		final int _dayNum,
		final WorkDay _day )
	{
		differences.getWorkWeek().setWeekDay( _dayNum, _day );
	}

	/**
	 * @param day
	 */
	public final void setWeekDays( 
		final WorkDay _day )
	{
		differences.getWorkWeek().setWeekDays( _day );
	}

	/**
	 * @param day
	 */
	public final void setWeekends( 
		final WorkDay _day )
	{
		differences.getWorkWeek().setWeekends( _day );
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public final String toString()
	{
		return getName();
	}

	private void writeObject( 
		final ObjectOutputStream _stream )
		throws IOException
	{
		serializedName = getName();

		if (baseCalendar == CalendarService.getInstance().getStandardInstance()) // don't serialize default base. treat it as null
		{
			baseCalendar = null;
		}

		_stream.defaultWriteObject();
		hasKey.serialize( _stream );

		if (baseCalendar == null)
		{
			baseCalendar = CalendarService.getInstance().getStandardInstance(); // put it back so program will work
		}
	}

	static final long serialVersionUID = 27738049223431L;
	public static final WorkingCalendar INVALID_INTERSECTION_CALENDAR = new WorkingCalendar();
	private static WorkingCalendar myStandardInstance = null;
	private static WorkingCalendar defaultInstance = null;
	private static WorkingCalendar _24HoursInstance = null;
	private static WorkingCalendar nightShiftInstance = null;
	String serializedName = ""; // non transient version of name for serialization

	/**
	 * This function will return a concrete calendar instance.  That is, one for which the days are already merged
	 * @return concrete instance
	 */
	private transient CalendarDefinition concrete = null;
	private CalendarDefinition differences = new CalendarDefinition();
	private transient HasCommonKeyImpl hasKey = new HasCommonKeyImpl(true, this); //true if calendars aren't internal

	// the objects that use this calendar
	private transient HashSet objectsUsing = null;
	private WorkCalendar baseCalendar = null;
	private transient boolean dirty;
	private byte version = 1;
	private int fixedId = 0;
}
