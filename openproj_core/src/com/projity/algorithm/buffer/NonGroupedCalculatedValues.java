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

import com.projity.options.CalendarOption;

import com.projity.pm.calendar.WorkCalendar;

import java.util.TreeMap;


/**
 * Stores an array of values as a bunch of ordered values at dates.
 */
public class NonGroupedCalculatedValues
	implements CalculatedValues
{
	public NonGroupedCalculatedValues( 
		final double _yScale,
		final boolean _cumulative,
		final long _origin )
	{
		super();
		
		myYScale = _yScale;
		myCumulative = _cumulative;
		myOrigin = _origin;
	}

	public NonGroupedCalculatedValues( 
		final boolean _cumulative,
		final long _origin )
	{
		this( 1.0D, _cumulative, _origin );
	}

	@Override
	public void dump()
	{
		for (int i = 0; i < myValuesArray.length; i++)
		{
			System.out.println( i + " " + new java.util.Date(myDates[ i ].longValue()) + " " + myValuesArray[ i ] );
		}
	}

	@Override
	public Long getDate( 
		final int _index )
	{
		return myDates[ _index ];
	}

	@Override
	public Double getValue( 
		final int _index )
	{
		return myValuesArray[ _index ];
	}

	public void makeContiguousNonZero( 
		final IntervalCallback _callback,
		final WorkCalendar _workCalendar )
	{
		Long[] d = new Long[ myValuesMap.size() ];
		Double[] v = new Double[ myValuesMap.size() ];
		myValuesMap.keySet().toArray( d );
		myValuesMap.values().toArray( v );

		double sum = 0;

		for (int i = 0; i < (d.length - 1); i++)
		{
			sum += v[ i ].doubleValue();
			_callback.add( d.length - 2 - i, d[ i ].longValue(), d[ i + 1 ].longValue(), sum );
		}
	}

	@Override
	public void makeCumulative( 
		final boolean _cumulative )
	{
		double sum = 0;

		for (int i = 0; i < myValuesArray.length; i++)
		{
			if (_cumulative == true)
			{
				sum += myValuesArray[ i ].doubleValue();
				myValuesArray[ i ] = Double.valueOf( sum );
			}
			else
			{
				myValuesArray[ i ] = Double.valueOf( myValuesArray[ i ].doubleValue() - sum );
				sum += myValuesArray[ i ].doubleValue();
			}
		}
	}

	@Override
	public void makeRectilinearSeries( 
		final SeriesCallback _callback )
	{
		makeSeries( false, _callback );
	}

//	public void finish() {
//		Long[] d = new Long[myValuesMap.size()];
//		Double[] v = new Double[myValuesMap.size()];
//		myValuesMap.keySet().toArray(d);
//		myValuesMap.values().toArray(v);
//		myDates = new Long[d.length*2];
//		myValuesArray = new Double[d.length*2];
//		Double previous = new Double(0);
//		double sum = 0;
//		for (int i = 0; i < d.length; i++) {
//			myDates[2*i] = d[i];
//			myDates[2*i+1] = d[i];
//			myValuesArray[2*i] = previous;
//			sum += v[i].doubleValue();
//			
//			myValuesArray[2*i+1] = new Double(sum);
//			previous = myValuesArray[2*i+1];
//		}
//		
//		//makeCumulative(true); // converts + and - into correct values
//	}
	
	@Override
	public void makeSeries( 
		final boolean _cumulative,
		final SeriesCallback _callback )
	{
		Long[] d = new Long[ myValuesMap.size() ];
		Double[] v = new Double[ myValuesMap.size() ];
		myValuesMap.keySet().toArray( d );
		myValuesMap.values().toArray( v );

		double sum = 0;
		double cum = 0;
		double z;

		if (_cumulative == true)
		{
			for (int i = 0; i < d.length; i++)
			{
				sum += v[ i ].doubleValue();
				_callback.add( i, d[ i ].doubleValue(), sum );
			}
		}
		else
		{
			for (int i = 0; i < d.length; i++)
			{
				_callback.add( 2 * i, d[ i ].doubleValue(), sum );
				sum += v[ i ].doubleValue();
				_callback.add( (2 * i) + 1, d[ i ].doubleValue(), sum );
			}
		}
	}

	/**
	 * Here is how ranges are added
	 * @param _startDate - date value increases
	 * @param _endDate - date value decreases
	 * @param _value - amount of increase/decrease
	 */
	@Override
	public void set( 
		final int _index,
		final long _startDate,
		final long _endDate,
		final double _value,
		final WorkCalendar _assignmentCalendar )
	{
		if (_startDate == 0)
		{
			return;
		}

		if (myCumulative == false)
		{
			double v = _value;

			if (_assignmentCalendar != null)
			{ 
				// can be null in case of value at date where start and end are the same
				// need to divide by duration to get value
				final long duration = _assignmentCalendar.compare( _endDate, _startDate, false ); 

				if (duration != 0) // avoid divide by zero
				{
					v /= (((double)duration) / CalendarOption.getInstance().getMillisPerDay());
				}

//				else if (myOrigin == 0) // for bars
//					return;
			}

			setValue( _startDate, v );
			setValue( _endDate, -v );
		}
		else
		{
			//System.out.println("start " + new Date(startDate) + " end " + new Date(endDate) + " value" + value );//+ " v/s " + v/s + " cal " + DurationFormat.format(duration));		
			setValue( _startDate, 0 );
			setValue( _endDate, _value );
		}
	}

	/**
	 * Add or modify existing point
	 * @param _date
	 * @param _value
	 */
	private void setValue( 
		final long _date,
		final double _value )
	{
		final Long longDate = new Long( _date );
		Double v = (Double)myValuesMap.get( longDate );

		if (v != null) // if already present, add to it
		{
			v = new Double( v.doubleValue() + _value );
		}
		else
		{
			v = new Double( _value );
		}

		myValuesMap.put( longDate, v );
	}

	@Override
	public int size()
	{
		return myValuesArray.length;
	}

	private static long MILLIS_PER_DAY = CalendarOption.getInstance().getMillisPerDay();

	//(x,y pairs) //TODO a set would be better because this is often sparse
	private TreeMap<Long,Double> myValuesMap = new TreeMap<Long,Double>(); 
	private Long[] myDates;
	private Double[] myValuesArray;
	private boolean myCumulative;
	private double myYScale;
	private long myOrigin;
}
