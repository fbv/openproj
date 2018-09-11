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
import org.apache.commons.collections.Closure;

import java.util.Comparator;
import java.util.Date;


/**
 * Merge is a functor which groups time intervals together and calls a visitor object on the resulting merged intervals
 * You can specify a comparator as well to determine whether two intervals can be merged
 */
public class Merge
	implements Closure<HasStartAndEnd>
{
	private Merge( 
		final Closure<HasStartAndEnd> _callBack )
	{
		myCallBack = _callBack;
		initializeDates();
	}

	private Merge( 
		final Closure<HasStartAndEnd> _callBack,
		final IntervalGenerator _generator,
		final Comparator _comparator )
	{
		this( _callBack );
		myComparator = _comparator;
		myGenerator = _generator;
	}

	/*
	 * Execution of the functor.
	 * This takes care of merging and calling the visitor.
	 */
	@Override
	public void execute( 
		final HasStartAndEnd _interval )
	{
//		Query query = (Query)_object;
//		final IntervalGenerator generator = _query.getGroupByGenerator();

		if (myGenerator.isCurrentActive() == true)
		{
			if ((myStarted == true)
			 && (myComparator != null) 
			 && (myComparator.compare( myCurrentObject, myGenerator.current() ) != 0)) // if myComparator doesnt match
			{
				treatCurrentInterval();
			}

			// starting
			myCurrentObject = myGenerator.current();
			myStarted = true;

			myCurrentStart = Math.min( myCurrentStart, _interval.getStart() );
			myCurrentEnd = Math.max( myCurrentEnd, _interval.getEnd() );

			//System.out.println("in Merge" + new Date(myCurrentStart) + " " + new Date(myCurrentEnd));			
			if (myGenerator.hasNext() == false) // if no more intervals, then terminate this one
			{
				treatCurrentInterval();
			}
		}
		else
		{
			if (myGenerator.current() != null)
			{
				// ending
				myStarted = false;
				treatCurrentInterval();
			}
		}
	}

	//	boolean ignoreZeroValueIntervals = false;
//	public static Merge getInstance( 
//		final Closure<HasStartAndEnd> _callBack )
//	{
//		return new Merge( _callBack );
//	}

	public static Merge getInstance( 
		final Closure<HasStartAndEnd> _callBack,
		final IntervalGenerator _generator,
		final Comparator _comparator )
	{
		if (_callBack == null) throw new NullPointerException( "_callBack is null" );
		if (_generator == null) throw new NullPointerException( "_generator is null" );
		
		return new Merge( _callBack, _generator, _comparator );
	}

	private void initializeDates()
	{
		myCurrentStart = Long.MAX_VALUE;
		myCurrentEnd = Long.MIN_VALUE;
	}

	public void setCallBack( 
		final Closure<HasStartAndEnd> _callBack )
	{
		myCallBack = _callBack;
	}

	private void treatCurrentInterval()
	{
		double value = 0.0D;

		if (myCurrentObject != null)
		{
			value = ((DoubleValue)myCurrentObject).getValue();
		}
		else
		{
			// System.out.println("Merge.treatCurrentInterval currentObject is null - using 0.0 for value");
		}

//		if (!ignoreZeroValueIntervals || value != 0.0D)
		final HasStartAndEnd startAndEnd = IntervalValue.getInstance( myCurrentStart, myCurrentEnd, value );
		myCallBack.execute( startAndEnd ); // finish previous
//		myCallBack.execute( IntervalValue.getInstance( myCurrentStart, myCurrentEnd, value ) ); // finish previous
		myStarted = false;
		initializeDates();
	}

	public class MergedInterval
	{
		public MergedInterval( 
			long _start,
			long _end,
			Object _template )
		{
			myStart = _start;
			myEnd = _end;
			myTemplate = _template;
		}

		/**
		 * @return Returns the end.
		 */
		public long getEnd()
		{
			return myEnd;
		}

		/**
		 * @return Returns the start.
		 */
		public long getStart()
		{
			return myStart;
		}

		/**
		 * @return Returns the template.
		 */
		public Object getTemplate()
		{
			return myTemplate;
		}

		@Override
		public String toString()
		{
			return new Date( myStart ) + "-" + new Date( myEnd ) + myTemplate;
		}

		private Object myTemplate;
		private long myEnd;
		private long myStart;
	}

	private Closure<HasStartAndEnd> myCallBack;
	private Comparator myComparator = null;
	private IntervalGenerator myGenerator = null;
	private Object myCurrentObject = null;
	private boolean myStarted = false;
	private long myCurrentEnd;
	private long myCurrentStart;
}
