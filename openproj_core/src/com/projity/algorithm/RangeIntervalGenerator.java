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
package com.projity.algorithm;

import com.projity.pm.time.HasStartAndEnd;
import com.projity.util.DateTime;

import java.util.GregorianCalendar;


/**
 * A generator corresponding to a start/end with an optional stepping value.  The stepping value is specified as
 * a unit type of the Calendar class (DAY_OF_YEAR for example) as a gregorian calendar is used.
 */
public class RangeIntervalGenerator
	implements IntervalGenerator
{
	private RangeIntervalGenerator()
	{
		this( 0, Long.MAX_VALUE );
	}

	private RangeIntervalGenerator( 
		long _start,
		long _end )
	{
		start = _start;
		end = _end;
		currentEnd = _end;
		step = _end; // make sure that will go past end
		nextEnd = _end + step;
	}

	private RangeIntervalGenerator( 
		long _start,
		long _end,
		int _calendarStepUnit )
	{
		start = _start;
		end = _end;
		calendarStepUnit = _calendarStepUnit;
		stepCal = new GregorianCalendar();
		stepCal.setTimeInMillis( _start );
		stepCal.add( _calendarStepUnit, calendarStepAmount );
		currentEnd = stepCal.getTimeInMillis();

		if (currentEnd > _end) // in case just one time period
		{
			currentEnd = _end;
		}

		stepCal.add( _calendarStepUnit, calendarStepAmount );
		nextEnd = stepCal.getTimeInMillis();
	}

	/**
	 * Used if filtering values between dates, not in a groupBy
	 * @param _start
	 * @param _end
	 * @return
	 */
	public static RangeIntervalGenerator betweenInstance( 
		long _start,
		long _end )
	{
		RangeIntervalGenerator result = getInstance( _start, _end );
		result.currentEnd = _start;

		return result;
	}

	/* (non-Javadoc)
	 * @see com.projity.algorithm.IntervalGenerator#canBeShared()
	 */
	@Override
	public boolean canBeShared()
	{
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo( 
		Object arg0 )
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * Continuous time is considered from 1/1/1970 to 1/1/3000.  
	 * Note I do not use Long.MAX_VALUE because adding to it would cause wrapping to negative values
	 */
	public static RangeIntervalGenerator continuous()
	{
		return getInstance( 0, DateTime.getMaxCalendar().getTimeInMillis() );
	}

	/* (non-Javadoc)
	 * @see com.projity.algorithm.IntervalGenerator#current()
	 */
	@Override
	public HasStartAndEnd current()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see com.projity.algorithm.IntervalGenerator#currentEnd()
	 */
	public long currentEnd()
	{
		return currentEnd;
	}

	public long currentStart()
	{
		return start;
	}

	public static RangeIntervalGenerator empty()
	{
		return getInstance( 0, 0 );
	}

	/*  (non-Javadoc)
	 * @see com.projity.algorithm.IntervalGenerator#next()
	 */
	@Override
	public boolean evaluate( 
		Object obj )
	{
		start = currentEnd; // move on to next interval.  If only one, then will stop here
		currentEnd = nextEnd;

		if (currentEnd > end)
		{
			currentEnd = end;
		}

		if (stepCal != null)
		{
			stepCal.add( calendarStepUnit, calendarStepAmount );
			nextEnd = stepCal.getTimeInMillis();
		}
		else
		{
			nextEnd += step;
		}

		if (start >= end)
		{
			return false;
		}

		return true;
	}

	/**
	 * @return Returns the end.
	 */
	@Override
	public long getEnd()
	{
		return end;
	}

	public static RangeIntervalGenerator getInstance()
	{
		return new RangeIntervalGenerator();
	}

	public static RangeIntervalGenerator getInstance( 
		long _start,
		long _end )
	{
		return new RangeIntervalGenerator( _start, _end );
	}

	public static RangeIntervalGenerator getInstance( 
		long _start,
		long _end,
		int _calendarStepUnit )
	{
		return new RangeIntervalGenerator( _start, _end, _calendarStepUnit );
	}

	/**
	 * @return Returns the start.
	 */
	@Override
	public long getStart()
	{
		return start;
	}

	/* (non-Javadoc)
	 * @see com.projity.algorithm.IntervalGenerator#hasNext()
	 */
	@Override
	public boolean hasNext()
	{
		return (nextEnd < end);
	}

	/* (non-Javadoc)
	 * @see com.projity.algorithm.IntervalGenerator#isActive()
	 */
	@Override
	public boolean isCurrentActive()
	{
		return true;
	}

	private GregorianCalendar stepCal = null;
	private int calendarStepAmount = 1;
	private int calendarStepUnit;
	private long currentEnd;
	private long end;
	private long nextEnd;
	private long start;
	private long step;
}
