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
package com.projity.algorithm.buffer;

import com.projity.pm.calendar.WorkCalendar;

import org.apache.commons.lang.time.DateUtils;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;


/**
 * Calculated values that are grouped by time buckets, such as a histogram
 */
public class GroupedCalculatedValues
	implements CalculatedValues,
		Serializable
{
	//	boolean dayByDay;
	public GroupedCalculatedValues( 
		final double _yScale )
	{
		super();
		myYScale = _yScale;
	}

	public GroupedCalculatedValues()
	{
		this( 1.0D );
	}

	public GroupedCalculatedValues dayByDayConvert()
	{
		final GroupedCalculatedValues calculatedValues = new GroupedCalculatedValues();

		//calculatedValues.setDayByDay( true );
		for (Iterator<Point> i = myPoints.iterator(); i.hasNext();)
		{
			final Point point = i.next();
			calculatedValues.myPoints.add( new Point( point.getDate(), point.getValue() * DateUtils.MILLIS_PER_HOUR ) );
		}

		return calculatedValues;
	}

	@Override
	public void dump()
	{
		for (int i = 0; i < myPoints.size(); i++)
		{
			System.out.println( i + " " + new java.util.Date( getDate( i ).longValue() ) + " " + getValue( i ) );
		}
	}

	@Override
	public Long getDate( 
		final int _index )
	{
		final Point point = myPoints.get( _index );

		if (point == null)
		{
			return null;
		}

		return Long.valueOf( point.getDate() );
	}

	public final double getUnscaledValue( 
		final int _index )
	{
		if (myPoints.isEmpty() == true)
		{
			System.out.println( "empty values in GroupedCalculatedValues" );

			return 0;
		}
		else if (_index >= myPoints.size())
		{
			System.out.println( "index out of bounds in GroupedCalculatedValues " + _index );

			return 0;
		}

		final Point point = myPoints.get( _index );

		if (point == null)
		{
			return 0;
		}

		return point.getValue();
	}

	@Override
	public Double getValue( 
		final int _index )
	{
		if (myPoints.isEmpty() == true)
		{
			System.out.println( "empty values in GroupedCalculatedValues" );

			return ZERO;
		}
		else if (_index >= myPoints.size())
		{
			System.out.println( "index out of bounds in GroupedCalculatedValues " + _index );

			return ZERO;
		}

		final Point point = myPoints.get( _index );

		if (point == null)
		{
			return null;
		}

		return Double.valueOf( point.getValue() / myYScale );
	}

	public final ArrayList<Point> getValues()
	{
		return myPoints;
	}

	public ListIterator<Point> iterator( 
		final int _index )
	{
		return myPoints.listIterator( _index );
	}

	/**
	 * Transforms values into cumulative values or back to non cumulative
	 *
	 */
	@Override
	public void makeCumulative( 
		final boolean _cumulative )
	{
		double sum = 0;

		for (int i = 0; i < myPoints.size(); i++)
		{
			final Point point = myPoints.get( i );

			if (_cumulative == true)
			{
				sum += point.getValue();
				point.setValue( sum );
			}
			else
			{
				point.minusValue( sum );
				sum += point.getValue();
			}
		}
	}

	@Override
	public void makeRectilinearSeries( 
		final SeriesCallback _callback )
	{
		double previous = 0.0D;

		for (int i = 0; i < myPoints.size(); i++)
		{
			final Point point = myPoints.get( i );
			_callback.add( 2 * i, point.getDate(), previous );
			previous = point.getValue();
			_callback.add( (2 * i) + 1, point.getDate(), previous );
		}
	}

	@Override
	public void makeSeries( 
		final boolean _cumulative,
		final SeriesCallback _callback )
	{
		Long[] d = new Long[ myPoints.size() ];
		Double[] v = new Double[ myPoints.size() ];

		//long lastDate = -10L;
		double sum = 0;

		//int deltai = 0;
		for (int i = 0; i < myPoints.size(); i++)
		{
			final Point point = myPoints.get( i );

//			lc hack to enable day by day values
//			if (dayByDay&&point.date-lastDate>DateUtils.MILLIS_PER_DAY+12*DateUtils.MILLIS_PER_HOUR){
//				if (lastDate>0L&&point.date-lastDate>2*DateUtils.MILLIS_PER_DAY+12*DateUtils.MILLIS_PER_HOUR)
//					callback.add(i+deltai++,lastDate+DateUtils.MILLIS_PER_DAY,0.0);
//				callback.add(i+deltai++,point.date-DateUtils.MILLIS_PER_DAY,0.0);
//			}
			
			_callback.add( i /*+deltai*/, point.getDate(), point.getValue() + ( _cumulative
				? sum
				: 0) );
			sum += point.getValue();

//			lastDate = point.date;
		}

//		if (dayByDay && point != null) 
//		{
//			callback.add( myPoints.size() + deltai, point.date + DateUtils.MILLIS_PER_DAY, 0.0 );
//		}
	}

	public void mergeIn( 
		final GroupedCalculatedValues _add )
	{
		final Iterator<Point> baseIterator = myPoints.iterator();
		final Iterator<Point> addIterator = _add.myPoints.iterator();
		Point basePoint = baseIterator.hasNext()
			? (Point)baseIterator.next()
			: null;
		final long start = basePoint.getDate();
		Point previousAddPoint = null;
		Point addPoint = addIterator.hasNext()
			? (Point)addIterator.next()
			: null;

		while ((basePoint != null) && (addPoint != null))
		{
			//TODO handle overlaps
			if (basePoint.compareTo( addPoint ) >= 0)
			{
				if (addPoint.getDate() >= start)
				{
					basePoint.plusValue( addPoint.getValue() );

					if ((basePoint.getDate() == start) && (previousAddPoint != null))
					{ 
						// if first time
						double proratedAmount = ((double)addPoint.getDate() - start) / (addPoint.getDate() 
							- previousAddPoint.getDate());

						if (proratedAmount > 0)
						{
							basePoint.plusValue( (previousAddPoint.getValue() * proratedAmount) );
						}
					}
				}

				previousAddPoint = addPoint;
				addPoint = addIterator.hasNext()
					? (Point)addIterator.next()
					: null;

				continue;
			}

			if (baseIterator.hasNext())
			{
				basePoint = (Point)baseIterator.next();
			}
			else
			{
				if (previousAddPoint != null)
				{ 
					// handle end boundary
					double proratedAmount = ((double)(basePoint.getDate() - previousAddPoint.getDate())) / (addPoint.getDate() -
						previousAddPoint.getDate() );

					if (proratedAmount > 0)
					{
						basePoint.plusValue( (addPoint.getValue() * proratedAmount) );
					}
				}

				basePoint = null;
			}
		}
	}

	@Override
	public void set( 
		final int _index,
		final long _date,
		final long _endDate,
		final double _value,
		final WorkCalendar _assignmentCalendar )
	{
		if (_date == 0)
		{
			return;
		}

		Point point;

		if (_index > (myPoints.size() - 1))
		{
//			System.out.println("add index " + _index + new java.util.Date( _date ) + " - " + new java.util.Date( _endDate ) 
//				+ " value " + _value );
			myPoints.add( _index, new Point( _date, _value ) );
		}
		else
		{
			point = (Point)myPoints.get( _index );

			if (point == null)
			{
				myPoints.set( _index, new Point( _date, _value ) );

//				System.out.println("add indexb " + _index + new java.util.Date( _date ) + " - " + new java.util.Date( _endDate ) 
//					+ " value " + _value );
			}
			else
			{
				point.addValue( _value );

//				System.out.println("add value " + _index + new java.util.Date( _date ) + " - " + new java.util.Date( _endDate ) 
//					+ " value " + _value );
			}
		}
	}

	public void setValue( 
		final int _index,
		final double _value )
	{
		final Point point = myPoints.get( _index );

		if (point == null)
		{
			return;
		}

		point.setValue( _value );
	}

	@Override
	public int size()
	{
		return myPoints.size();
	}

	public static GroupedCalculatedValues union( 
		final GroupedCalculatedValues _values1,
		final GroupedCalculatedValues _values2 )
	{
		GroupedCalculatedValues c1;
		GroupedCalculatedValues c2;

		if (_values1.size() >= _values2.size())
		{
			c1 = _values1;
			c2 = _values2;
		}
		else
		{
			c1 = _values2;
			c2 = _values1;
		}

		GroupedCalculatedValues c = new GroupedCalculatedValues();
		ListIterator i1 = c1.myPoints.listIterator();
		ListIterator i2 = c2.myPoints.listIterator();
		Point p1;
		Point p2 = null;

		while (i1.hasNext())
		{
			p1 = (Point)i1.next();

			while (i2.hasNext())
			{
				p2 = (Point)i2.next();

				if (p2.getDate() < p1.getDate())
				{
					c.myPoints.add( p2 );
				}
				else if (p2.getDate() > p1.getDate())
				{
					i2.previous();

					break;
				}
				else
				{
					break;
				}
			}

			if ((p2 != null) && (p1.getDate() == p2.getDate()))
			{
				c.myPoints.add( new Point(p1.getDate(), p1.getValue() + p2.getValue()) );
			}
			else
			{
				c.myPoints.add( p1 );
			}
		}

		while (i2.hasNext())
		{
			c.myPoints.add( (Point)i2.next() );
		}

		return c;
	}

	static final long serialVersionUID = 8900927827L;
	private static final Double ZERO = new Double(0.0D);

	//(x,y pairs) //TODO a set would be better because this is often sparse
	private ArrayList<Point> myPoints = new ArrayList<Point>(); 
	private double myYScale;
}
