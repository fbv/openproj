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

import com.projity.datatype.Duration;
import com.projity.field.FieldContext;

import com.projity.pm.criticalpath.CriticalPath;
import com.projity.pm.time.Interval;

import com.projity.server.access.ErrorLogger;

import com.projity.util.DateTime;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.text.SimpleDateFormat;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.TreeSet;


/**
 * This class holds specific calendar information either for a base calendar or a concrete one, as well as date math functions
 */
public final class CalendarDefinition
	implements WorkCalendar,
		Cloneable
{
	/**
	 *
	 */
	public CalendarDefinition()
	{
		super();

		dayExceptions = new TreeSet<WorkDay>();
		week = new WorkWeek();
	}

	public CalendarDefinition( 
		final CalendarDefinition _base,
		final CalendarDefinition _differences )
	{
		super();
		
		if (_base == null)
		{
			week = new WorkWeek();
		}
		else
		{
			// copy the week days
			week = _base.week.clone(); 
		}

		// Now replace any special weekdays
		week.addDaysFrom( _differences.week ); 

		// copy from differences
		dayExceptions = (TreeSet<WorkDay>)_differences.dayExceptions.clone(); 

		if (_base != null)
		{
			// add in base days. If day is already present it will not be added
			dayExceptions.addAll( _base.dayExceptions ); 
		}

		addSentinelsAndMakeArray();

		if (testValid() == false)
		{
			System.out.println( "calendar is invalid " + getName() );
		}
	}

	/**
	 * 
	 * @param _source 
	 */
	private CalendarDefinition(
		final CalendarDefinition _source )
	{
		week = _source.week.clone();
		dayExceptions = new TreeSet<WorkDay>();

		Iterator<WorkDay> itor = dayExceptions.iterator();

		while (itor.hasNext() == true)
		{
			dayExceptions.add( itor.next().clone() );
		}
	}
	
	/**
	 * Algorithm to add a duration to a date.  This code MUST be very fast as it is the most executed code in the program.
	 * The time required by the algorithm is determined by the number of exceptions encountered and not the duration itself.
	 * To handle reverse scheduling, the date can be negative.  In this case, the date is converted to a positive value, but the duration
	 * is negated.
	 */
	@Override
	public long add( 
		long _date,
		long _duration,
		boolean _useSooner )
	{
		if (_date == 0) // don't bother treating null dates since they will never be valid for calculations
		{
			return 0;
		}

		long result = _date;
		boolean forward = true;
		boolean negative = _date < 0;
		boolean elapsed = Duration.isElapsed( _duration );
		_duration = Duration.millis( _duration );

		if (negative == true)
		{
			_date = -_date;
			_duration = -_duration;
			_useSooner = !_useSooner;

			if (_duration == 0)
			{
				forward = false;
			}
		}

		if (elapsed == true)
		{ 
			// elapsed times do not use calendars, though the result must fall within working time
			result = adjustInsideCalendar( _date + _duration, _useSooner );
		}
		else
		{
			if (_duration < 0)
			{
				forward = false;
				_duration = -_duration;
			}

			//TODO move current day into iterator for speed
			CalendarIterator iterator = CalendarIteratorFactory.getInstance(); // use object pool for speed
			long currentDay = iterator.dayOf( _date );
			iterator.initialize( this, forward, currentDay );

			WorkingHours current = iterator.getNext( currentDay );
			_duration -= current.calcWorkTime( iterator.timeOf( _date ), forward ); // handle the first day

			long numWeeks;

			/*
			 * First, do a "rough tuning" to get within a week of destination day.  This part of the algorithm will
			 * see how many weeks there are in the duration, subtract off the normal working time for a week for each week.
			 * and position the day correctly.  It then adjusts the duration based on any exception days during those weeks.
			 * It is possible, if there are many exceptions, that after adjusting for exception days, there are still weeks of
			 * work left.  That is why this is called in a loop.
			 */
			int weekTries = 0; // in rare cases, the exception value can increase, so abort if so
			long weekDuration = week.getDuration();

			while ((numWeeks = (_duration / weekDuration)) != 0)
			{
				if (weekTries++ == 4) // most likely it's increasing. give up and do remaining day by day
				{
					break;
				}

				currentDay = iterator.nextDay( currentDay ); // move to next day, first is done
				currentDay = iterator.moveNumberOfDays( (int)(WorkWeek.DAYS_IN_WEEK * (forward
					? numWeeks
					: (-numWeeks))), currentDay );
				_duration -= (numWeeks * weekDuration); // subtract off fixed duration
				_duration -= iterator.exceptionDurationDifference( currentDay ); // subtract off difference.

				if (_duration <= 0)
				{ 
					// if exceptions cause too much duration, then go back in other direction
					iterator.reverseDirection();
					_duration = -_duration;
					forward = !forward; // todo is this necessary?
				}
				else{ //TODO verify that this should be in else.
					currentDay = iterator.prevDay( currentDay ); // move back a day for fine tuning which adds it back
				}
			}

			/*
			 * This part of the algorithm is the fine tuning.  It does through the remaining deays and treats them one by one.
			 * Because of the week treatment above, this is guaranteed to go through 6 days at the most.
			 */
			while (_duration >= 0)
			{ 
				// add in days until we go exactly on the spot or past it
				if ((_duration == 0) && (forward == _useSooner))
				{ 
					//TODO verify that this should be in else.
					break;
				}

				currentDay = iterator.nextDay( currentDay );
				current = iterator.getNext( currentDay );
				_duration -= current.getDuration(); // use exception day
			}

			// Handle the last day
			long time = -1;

			while (true)
			{
				if (forward)
				{
					time = current.calcTimeAtRemainingWork( -_duration );
				}
				else
				{
					time = current.calcTimeAtWork( -_duration );
				}

				if (time != -1)
				{
					break;
				}

				currentDay = iterator.nextDay( currentDay );
				current = iterator.getNext( currentDay );
			}

			result = currentDay + time;

			CalendarIteratorFactory.recycle( iterator ); //No longer using iterator, return it to pool
		}

		// if input was negative time, return a negative value
		if (negative == true)
		{
			result = -result;
		}

		return result;
	}

	/**
	 * This method adjusts the given time to a working time in the calendar.
	 * The algorithm just subtracts a tick and adds it back for sooner or vice versa for later
	 * 
	 * @param _date
	 * @param _useSooner
	 * @return
	 */
	@Override
	public long adjustInsideCalendar( 
		long _date,
		boolean _useSooner )
	{
		long result;

		if (_date < 0)
		{
			_date = -_date;
			_useSooner = !_useSooner;
		}

		if (_useSooner == true)
		{
			long backOne = add( _date, -MILLIS_IN_MINUTE, _useSooner );
			result = add( backOne, MILLIS_IN_MINUTE, _useSooner );
		}
		else
		{
			long aheadOne = add( _date, MILLIS_IN_MINUTE, _useSooner );
			result = add( aheadOne, -MILLIS_IN_MINUTE, _useSooner );
		}

		return result;
	}

	public static void cleanUp()
	{
		CalendarIterator.cleanUp();
		CalendarIteratorFactory.cleanUp();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public CalendarDefinition clone()
	{
		return new CalendarDefinition( this );
	}

	/** Get difference of two dates: laterDate - earlierDate according to calendar
	 * 
	 * @param _laterDate
	 * @param _earlierDate
	 * @param _elapsed
	 * @return 
	 */
	@Override
	public long compare( 
		long _laterDate,
		long _earlierDate,
		final boolean _elapsed )
	{
		boolean negative = _laterDate < 0;

		if (negative == true)
		{
			_laterDate = -_laterDate;
			_earlierDate = -_earlierDate;
		}

		if (_elapsed == true)
		{ 
			// if the desired duration is elapsed time, then just to a simple subtraction
			return _laterDate - _earlierDate;
		}

		// if later is before earlier swap the dates.  The value of swap is tested later and sign is reversed if it is used
		long swap = 0;

		if (_laterDate < _earlierDate)
		{
			swap = _earlierDate;
			_earlierDate = _laterDate;
			_laterDate = swap;
		}

		if (_earlierDate == 0) // degenerate case.  A 0 date means undefined, so don't process it
		{
			return _laterDate;
		}

		CalendarIterator iterator = CalendarIteratorFactory.getInstance(); // use object pool for speed
		long earlierDay = iterator.dayOf( _earlierDate );
		long laterDay = iterator.dayOf( _laterDate );
		long currentDay;
		iterator.initialize( this, true, earlierDay );

		WorkingHours current = iterator.getNext( earlierDay );
		long duration = 0;

		// Algo starts here
		// treat start day
		duration += current.calcWorkTimeAfter( iterator.timeOf( _earlierDate ) );
		currentDay = iterator.nextDay( earlierDay ); // move to next day, first is done

		/*
		 * First add in weeks, adjusting for exception days
		 */
		long numWeeks = (iterator.dayOf( _laterDate ) - currentDay) / WorkWeek.MS_IN_WEEK;

		if (numWeeks != 0)
		{
			currentDay = iterator.moveNumberOfDays( (int)(WorkWeek.DAYS_IN_WEEK * numWeeks), currentDay );
			duration += (numWeeks * week.getDuration()); // add on normal working duration
			duration += iterator.exceptionDurationDifference( currentDay ); // add difference.
		}

		// treat remaining middle days  (no more than 6) and the end day
		for (; currentDay <= laterDay; currentDay = iterator.nextDay( currentDay ))
		{
			current = iterator.getNext( currentDay );
			duration += current.getDuration();
		}

		// subtract out part of the end day that is later then laterDate
		duration -= current.calcWorkTimeAfter( iterator.timeOf( _laterDate ) );

		CalendarIteratorFactory.recycle( iterator );

		if (negative)
		{
			duration = -duration;
		}

		return (swap == 0)
			? duration
			: (-duration); // swap == 0 implies that no swap was done since early date had to be minimum
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.calendar.WorkCalendar#dependsOn(com.projity.pm.calendar.WorkCalendar)
	 */
	@Override
	public boolean dependsOn( 
		final WorkCalendar cal )
	{
		return false;
	}

	@Override
	public void deserialiseCompact( 
		final ObjectInputStream _stream )
		throws IOException, 
			ClassNotFoundException
	{
		version = _stream.readByte();
		id = _stream.readLong();
		week = WorkWeek.deserialiseCompact( _stream );

		int dayExceptionsCount = _stream.readInt();

		//if (dayExceptionsCount == -1) 
		//	dayExceptions = null;
		//else
		//{
		//	dayExceptions = new TreeSet();
		
		for (int i = 0; i < dayExceptionsCount; i++)
		{
			dayExceptions.add( WorkDay.deserialiseCompact( _stream ) );
		}

		//}

		exceptions = new WorkDay[ dayExceptions.size() ];
		dayExceptions.toArray( exceptions );
	}

	public String dump()
	{
		String result = "Calendar " + getName() + "\n";
		result += "weekdays\n";

		for (int i = 0; i < 7; i++)
		{
			result += ("day[" + i + "]" + getWeekDay( i ) + "\n");
		}

		result += ("There are " + exceptions.length + " exceptions\n");

		for (int j = 0; j < exceptions.length; j++)
		{
			result += ("exception" + exceptions[ j ].toString());
		}

		return result;
	}

	public WorkDay findExceptionDay( 
		final long _date )
	{
		final Iterator<WorkDay> itor = dayExceptions.iterator();
		while (itor.hasNext()== true)
		{
			final WorkDay day = itor.next();

			if (day.getStart() == _date)
			{
				return day;
			}
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.calendar.WorkCalendar#getBaseCalendar()
	 */
	@Override
	public WorkCalendar getBaseCalendar()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see com.projity.configuration.NamedItem#getCategory()
	 */
	@Override
	public String getCategory()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.calendar.WorkCalendar#getConcreteInstance()
	 */
	@Override
	public CalendarDefinition getConcreteInstance()
	{
		return this; // doesn't make sense to call this
	}

	/** Return the day exceptions
	 * 
	 * @return 
	 */
	public TreeSet<WorkDay> getDayExceptions()
	{
		return dayExceptions;
	}
	
	public static int getDayOfWeek( 
		final long _date )
	{
		Calendar scratchDate = DateTime.calendarInstance();
		scratchDate.setTimeInMillis( _date );

		return scratchDate.get( Calendar.DAY_OF_WEEK ) - 1;
	}

	public WorkDay[] getExceptions()
	{
		return exceptions;
	}

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
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName(
		final FieldContext _fieldContext )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getUniqueId()
	{
		return id;
	}

	public WorkDay getWeekDay( 
		final int d )
	{
		return week.getWeekDay( d );
	}

	public long getWeekDuration()
	{
		return week.getDuration();
	}

	public final WorkDay getWorkDay( 
		final long _date )
	{
		final int i = Arrays.binarySearch( getConcreteInstance().exceptions, new WorkDay( _date, _date ) );

		if (i >= 0)
		{
			return exceptions[ i ];
		}

		return week.getWeekDay( getDayOfWeek( _date ) );
	}
	
	/** Return the work week
	 * 
	 * @return 
	 */
	public WorkWeek getWorkWeek()
	{
		return week;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.calendar.WorkCalendar#invalidate()
	 */
	@Override
	public void invalidate()
	{
	}

	@Override
	public boolean isDirty()
	{
		return dirty;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.calendar.WorkCalendar#isInvalid()
	 */
	@Override
	public boolean isInvalid()
	{
		return false;
	}

	public boolean isNew()
	{
		return myNewId;
	}

	@Override
	public void serialiseCompact( 
		final ObjectOutputStream _stream )
		throws IOException
	{
		_stream.writeByte( version );
		_stream.writeLong( id );
		week.serialiseCompact( _stream );

		if (dayExceptions == null)
		{
			_stream.writeInt( -1 );
		}
		else
		{
			_stream.writeInt( dayExceptions.size() );

			for (Iterator<WorkDay> itor = dayExceptions.iterator(); itor.hasNext() == true;)
			{
				WorkDay d = itor.next();
				d.serialiseCompact( _stream );
			}
		}
	}

	@Override
	public void setDirty( 
		final boolean _dirty )
	{
		//System.out.println("CalendarDefinition _setDirty("+dirty+"): "+getName());
		dirty = _dirty;
	}

	public void setId( 
		final long _id )
	{
		id = _id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setName( 
		final String _name )
	{
		// TODO Auto-generated method stub
	}

	public void setNew( 
		final boolean _newId )
	{
		myNewId = _newId;
	}

	@Override
	public void setUniqueId( 
		final long _id )
	{
		id = _id;
	}

	/** Assign the work week attribute
	 * 
	 * @param _value used as the source of the assignment
	 */
	public void setWorkWeek(
		final WorkWeek _value )
	{
		week = _value;
	}

	public boolean testValid()
	{
		if (week == null)
		{
			return false;
		}

		for (int i = 0; i < 7; i++)
		{
			if (week.getWeekDay( i ) == null)
			{
				return false;
			}
		}

		return true;
	}

	void addOrReplaceException( 
		final WorkDay _exceptionDay )
	{
		dayExceptions.remove( _exceptionDay ); // remove any existing
		dayExceptions.add( _exceptionDay );
		exceptions = new WorkDay[ dayExceptions.size() ];
		dayExceptions.toArray( exceptions );
	}

	void addSentinelsAndMakeArray()
	{
		// Add endpoint sentinels.  This facilitates algorithms which will no longer need to check for boundary conditions
		dayExceptions.add( WorkDay.MINIMUM );
		dayExceptions.add( WorkDay.MAXIMUM );
		exceptions = new WorkDay[ dayExceptions.size() ];
		dayExceptions.toArray( exceptions );
	}

	CalendarDefinition intersectWith( 
		final CalendarDefinition _other )
		throws InvalidCalendarIntersectionException
	{
		CalendarDefinition result = new CalendarDefinition();
		result.setWorkWeek( week.intersectWith( _other.week ) );

		WorkDay exceptionDay;

		// merge exceptions
		for (int i = 0; i < exceptions.length; i++)
		{
			exceptionDay = exceptions[ i ];
			result.dayExceptions.add( exceptionDay.intersectWith( _other.getWorkDay( exceptionDay.getStart() ) ) );
		}

		for (int i = 0; i < _other.exceptions.length; i++)
		{
			exceptionDay = _other.exceptions[ i ];
			result.dayExceptions.add( exceptionDay.intersectWith( getWorkDay( exceptionDay.getStart() ) ) );
		}

		result.addSentinelsAndMakeArray();

		return result;
	}

	/**
	 * This class is an iterator which is used to return week days or exception days
	 *
	 */
	private static class CalendarIterator
	{
		private CalendarIterator()
		{
			myScratchDate = DateTime.calendarInstance(); // will get reused since this class is recycled
		}

		public static void cleanUp()
		{
			myDateFormat = DateTime.dateFormatInstance();
		}

		public long dayOf( 
			final long _date )
		{
			myScratchDate.setTimeInMillis( _date );
			myScratchDate.set( Calendar.HOUR_OF_DAY, 0 );
			myScratchDate.set( Calendar.MINUTE, 0 );
			myScratchDate.set( Calendar.SECOND, 0 ); // Fixed rounding bug as we now go to seconds 8/2/07
//			myScratchDate.set(Calendar.MILLISECOND,0);

			return myScratchDate.getTimeInMillis();
		}

		private int dayOfWeek( 
			final long _day )
		{
			myScratchDate.setTimeInMillis( _day );

			return myScratchDate.get( Calendar.DAY_OF_WEEK ) - 1;
		}

		public String dump()
		{
			String result = "CalendarIterator ";
			result += "weekdays\n";

			for (int i = 0; i < 7; i++)
			{
				result += ("day[" + i + "]" + week.getWeekDay( i ) + "\n");
			}

			result += ("There are " + exceptions.length + " exceptions\n");

			for (int j = 0; j < exceptions.length; j++)
			{
				result += ("exception" + exceptions[ j ].toString());
			}

			return result;
		}

		private long exceptionDurationDifference( 
			final long _endDay )
		{
			long difference = 0;

			if (exceptions.length == 2) // skip sentinels
			{
				return 0;
			}

			while ((myForward && (myExceptionDay < _endDay)) || (!myForward && (myExceptionDay > _endDay)))
			{
				difference -= week.getWeekDay( dayOfWeek( myExceptionDay ) ).getDuration();
				difference += exceptions[ myIndex ].getDuration();
				myIndex += myStep;

				if ((myIndex < 0) || (myIndex >= exceptions.length))
				{
					//					System.out.println("error");
					break; // added april 30 2008 hk
				}

				myExceptionDay = exceptions[ myIndex ].getStart();
			}

			return difference;
		}

		private WorkingHours getNext( 
			final long _day )
		{
			WorkDay workDay;

			if (_day == myExceptionDay)
			{
				workDay = exceptions[ myIndex ]; // move index, save off new value for exception day
				myIndex += myStep;

				if ((myIndex < 0) 
				 || (myIndex == exceptions.length))
				{ 
					//TODO
					System.out.println( "invalid calendar iterator - index is negative or past bounds. avoiding" );
					ErrorLogger.logOnce( "CalendarIterator", "invalid calendar iterator i=" + myIndex + "\n" + CriticalPath.getTrace(),
						null );
				}
				else
				{
					myExceptionDay = exceptions[ myIndex ].getStart(); // move index, save off new value for exception day
				}
			}
			else
			{
				workDay = week.getWeekDay( dayOfWeek( _day ) );
			}

			return workDay.getWorkingHours();
		}

		private void initialize( 
			final CalendarDefinition _cal,
			final boolean _forward,
			final long _day )
		{
			exceptions = _cal.exceptions;
			week = _cal.week;
			this.myForward = _forward;
			myScratchDate.setTimeInMillis( _day );

			try
			{
				DateUtils.truncate( myScratchDate, Calendar.DATE );
			}
			catch (Exception e)
			{
				ErrorLogger.logOnce( "hugedate", "date value is garbage " + myScratchDate + "\n" + CriticalPath.getTrace(), e );
			}

			myStep = (_forward)
				? 1
				: (-1);
//			i = Arrays.binarySearch( exceptions, myScratchDate );
			myIndex = Arrays.binarySearch( exceptions, new WorkDay( myScratchDate.getTimeInMillis(), 
				myScratchDate.getTimeInMillis() ) );

			if (myIndex < 0)
			{ 
				// First day not found
				myIndex = -myIndex - 1; // set index for the future

				if (myForward == false)
				{
					myIndex -= 1;
				}
			}

			myExceptionDay = exceptions[ myIndex ].getStart();
		}

		private long moveNumberOfDays( 
			final int _numberOfDays,
			final long _fromDay )
		{
			myScratchDate.setTimeInMillis( _fromDay );
			myScratchDate.add( Calendar.DATE, _numberOfDays );

			return myScratchDate.getTimeInMillis();
		}

		private long nextDay( 
			final long _day )
		{
			myScratchDate.setTimeInMillis( _day );
			myScratchDate.add( Calendar.DATE, myForward
				? 1
				: (-1) );

			return myScratchDate.getTimeInMillis();
		}

		private long prevDay( 
			final long _day )
		{
			myScratchDate.setTimeInMillis( _day );
			myScratchDate.add( Calendar.DATE, myForward
				? (-1)
				: 1 );

			return myScratchDate.getTimeInMillis();
		}

		/**
		 *
		 */
		private void reverseDirection()
		{
			if (myForward)
			{
				myIndex -= 1;
			}
			else
			{
				myIndex += 1;
			}

			myStep = -myStep;
			myExceptionDay = exceptions[ myIndex ].getStart();
			myForward = !myForward;
		}

		public long timeOf( 
			final long _date )
		{
			return _date - dayOf( _date );
		}

		private static SimpleDateFormat myDateFormat = DateTime.dateFormatInstance();
		Calendar myScratchDate; // will get reused since this class is recycled
		WorkWeek week;
		WorkDay[] exceptions;
		boolean myForward;
		int myIndex;
		int myStep;
		long myExceptionDay;
	}

	/**
	 * This class manages a pool of calendar iterators.
	 *
	 */
	private static class CalendarIteratorFactory
		extends BasePoolableObjectFactory
	{
		public static void cleanUp()
		{
			pool = new GenericObjectPool(new CalendarIteratorFactory());
		}

		public static CalendarIterator getInstance()
		{
			try
			{
				return (CalendarIterator)pool.borrowObject();
			}
			catch (Exception e)
			{
				e.printStackTrace();

				return null;
			}
		}

		@Override
		public Object makeObject()
			throws Exception
		{
			return new CalendarIterator();
		}

		public static void recycle( 
			final CalendarIterator _object )
		{
			try
			{
				pool.returnObject( _object );
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		private static GenericObjectPool pool = new GenericObjectPool( new CalendarIteratorFactory() );
	}

	static final long serialVersionUID = 73883742020831L;
	transient boolean myNewId = true;
	protected long id = -1L;
	private TreeSet<WorkDay> dayExceptions = null;
	private WorkWeek week = new WorkWeek();
	private WorkDay[] exceptions = null;
	private transient boolean dirty;
	private byte version = 1;
}
