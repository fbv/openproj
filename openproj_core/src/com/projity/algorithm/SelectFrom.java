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
import com.projity.pm.time.MutableInterval;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.functors.ChainedClosure;
import org.apache.commons.collections.functors.FalsePredicate;
import org.apache.commons.collections.functors.TruePredicate;

import java.util.Collection;
import java.util.LinkedList;


/**
 * The part of a query that will apply visitors over generators
 */
public class SelectFrom
{
	/**
	 *
	 */
	private SelectFrom()
	{
		super();

		// TODO Auto-generated constructor stub
	}

	public SelectFrom all()
	{
		myMustProcessAll = true; // must be set after from generators are set!

		return this;
	}

	/**
	 * Calculate values in a range of times by calling each visitor on subranges until the range is complete.
	 * @param _groupByStart start of calculation range.  currently unused!
	 * @param _groupByEnd end of calculation range
	 * @return true if all of the from generators are still active, false if one of them has been used up.
	 */
	public boolean calculate( 
		final long _groupByStart,
		final long _groupByEnd )
	{
		if (myFinished == true)
		{ 
			// if the last item of a generator was processed in previous call
			resetCalculations(); // since it is no longer active, should always return 0s from now on

			return false;
		}

		while (true)
		{
			if (myGenerator == null) // will be null on first call, and after the previously active generator has been evaluated
			{
				myGenerator = myFromGenerators.earliestEndingGenerator();
			}

			if (myGenerator == null)
			{ 
				//could be case if there are no from generatros at all TODO is this test needed?
				myFinished = true;

				break;
			}

			// if current generator was interrupted by ending a range, we need to start at point left off
			myInterval.setStart( Math.max( myInterval.getStart(), myGenerator.getStart() ) ); 
			myInterval.setEnd( Math.min( _groupByEnd, myGenerator.getEnd() ) );

			if (myInterval.getEnd() >= myInterval.getStart())
			{ 
				// in cases where a clause starts in the middle, such as remaining work, end may be less than start at first
				//System.out.println("SelectFrom start" + new java.util.Date(myStart) + " end " + new java.util.Date(myEnd) 
				//	+ " " + myGenerator);			
				// evaluate fields
				boolean whereConditionMet = myWherePredicate.evaluate( myInterval );

				if (myFieldVisitors != null)
				{
					for (int i = 0; i < myFieldVisitorArray.length; i++)
					{
						// if we are in the calculation range, or if the functor is cumulative
						if ((whereConditionMet == true)
						 || (myFieldVisitorArray[ i ].isCumulative() == true))
						{
							myFieldVisitorArray[ i ].execute( myInterval );
						}
					}
				}
			}

			myInterval.setStart( myInterval.getEnd() ); // for next iteration, shift start to current end

			if (myInterval.getEnd() == _groupByEnd) // at end of groupBy. 
			{
				break;
			}

			if (myGenerator.evaluate( this ) == false)
			{
				if (myMustProcessAll == true)
				{ 
					// if all froms must be treated
					myFromGenerators.remove( myGenerator );

					// any left?
					myFinished = myFromGenerators.isEmpty(); 
				}
				else
				{
					myFinished = true; // The next time calculate is called, it should return false
				}

				if (myFinished == true)
				{
					break;
				}
			}

			// The current generator has been finished.  Will need to find earliest next time
			myGenerator = null; 
		}

		return true;
	}

	public SelectFrom from( 
		final LinkedList<IntervalGenerator> _fromGeneratorList )
	{
		if (myFromGenerators == null)
		{
			myFromGenerators = IntervalGeneratorSet.getInstance( _fromGeneratorList );
		}
		else
		{
			myFromGenerators.getGenerators().addAll( _fromGeneratorList );
		}

		return this;
	}

	public SelectFrom from( 
		final IntervalGenerator _fromGenerator )
	{
		if (myFromGenerators == null)
		{
			myFromGenerators = IntervalGeneratorSet.getInstance( _fromGenerator );
		}
		else
		{
			myFromGenerators.getGenerators().add( _fromGenerator );
		}

		return this;
	}

	public Collection<IntervalGenerator> getFromIntervalGenerators()
	{
		return myFromGenerators.getGenerators();
	}

	/**
	 * Factory method
	 * @return
	 */
	public static SelectFrom getInstance()
	{
		return new SelectFrom();
	}

	/**
	 * Initializes all calculation totals for active field visitors.  This will set all non-cumulative ones to 0s
	 * Cumulative ones are not initialized
	 *
	 */
	public void initializeCalculations()
	{
		if (myFieldVisitorArray == null)
		{
			return;
		}

		for (int i = 0; i < myFieldVisitorArray.length; i++)
		{
			myFieldVisitorArray[ i ].initialize();
		}
	}

	public static LinkedList<SelectFrom> listInstance( 
		final SelectFrom _a )
	{
		final LinkedList<SelectFrom> list = new LinkedList<SelectFrom>();
		list.add( _a );

		return list;
	}

	public static LinkedList<SelectFrom> listInstance( 
		final SelectFrom _a,
		final SelectFrom _b )
	{
		final LinkedList<SelectFrom> list = new LinkedList<SelectFrom>();
		list.add( _a );
		list.add( _b );

		return list;
	}

	public static LinkedList<SelectFrom> listInstance( 
		final SelectFrom _a,
		final SelectFrom _b,
		final SelectFrom _c )
	{
		final LinkedList<SelectFrom> list = new LinkedList<SelectFrom>();
		list.add( _a );
		list.add( _b );
		list.add( _c );

		return list;
	}

	public static LinkedList<SelectFrom> listInstance( 
		final SelectFrom _a,
		final SelectFrom _b,
		final SelectFrom _c,
		final SelectFrom _d )
	{
		final LinkedList<SelectFrom> list = new LinkedList<SelectFrom>();
		list.add( _a );
		list.add( _b );
		list.add( _c );
		list.add( _d );

		return list;
	}

	/**
	 * Put fields back to their 0 state. This is used when the clause is used up.  Cumulative fields as well.
	 *
	 */
	public void resetCalculations()
	{
		if (myFieldVisitorArray == null)
		{
			return;
		}

		for (int i = 0; i < myFieldVisitorArray.length; i++)
		{
			myFieldVisitorArray[ i ].reset();
		}
	}

	public SelectFrom select( 
		final CalculationVisitor[] _fieldVisitorArray )
	{
		myFieldVisitorArray = _fieldVisitorArray;
		myFieldVisitors = new ChainedClosure( _fieldVisitorArray );

		return this;
	}

	public SelectFrom select( 
		final CalculationVisitor _fieldVisitor )
	{
		// Does field visitor array already exist?
		if (myFieldVisitorArray != null)
		{ 
			// Yes, add to it
			CalculationVisitor[] newArray = new CalculationVisitor[ myFieldVisitorArray.length + 1 ];
			System.arraycopy( myFieldVisitorArray, 0, newArray, 0, myFieldVisitorArray.length );
			newArray[ myFieldVisitorArray.length ] = _fieldVisitor; // add new one to end

			return select( newArray );
		}

		//make one element array
		myFieldVisitorArray = new CalculationVisitor[]
		{
			_fieldVisitor
		}; 
		
		// no need to make chained closure since only one element
		myFieldVisitors = _fieldVisitor; 

		return this;
	}

	public static LinkedList selectFromListInstance()
	{
		return new LinkedList();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "Select From where = " + myWherePredicate;
	}

	public SelectFrom where( 
		final Predicate _wherePredicate )
	{
		myWherePredicate = _wherePredicate;

		return this;
	}

	public SelectFrom whereInRange( 
		long _start,
		long _end )
	{
		if (_start <= _end)
		{ 
			// if non backwards range 
			// If there is already a range, intersect with it
			if ((myWherePredicate != null) && myWherePredicate instanceof DateInRangePredicate)
			{
				DateInRangePredicate range = (DateInRangePredicate)myWherePredicate;
				range.limitTo( _start, _end );
				_start = range.getStart();
				_end = range.getEnd();
			}
			else
			{
				myWherePredicate = DateInRangePredicate.getInstance( _start, _end );
			}

			// add a generator assuring the endpoints are treated corrctly
			from( RangeIntervalGenerator.betweenInstance( _start, _end ) ); 
		}
		else
		{	
			// take care in cases where range is invalid
			myWherePredicate = FalsePredicate.falsePredicate();
		}

		return this;
	}

	public static final SelectFrom[] NOTHING = new SelectFrom[] 
	{  
	};

	protected IntervalGeneratorSet myFromGenerators = null;
	Closure myFieldVisitors = null;
	Predicate<HasStartAndEnd> myWherePredicate = TruePredicate.truePredicate();
	CalculationVisitor[] myFieldVisitorArray = null;
	boolean myFinished = false;
	boolean myMustProcessAll = false;
	private IntervalGenerator myGenerator = null;

	private MutableInterval myInterval = new MutableInterval( 0, 0 );
//	long myEnd;
//	long myStart;
}
