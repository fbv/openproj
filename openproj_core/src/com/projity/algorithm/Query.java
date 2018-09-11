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
import com.projity.pm.time.Interval;
import com.projity.pm.time.MutableInterval;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.Factory;

import java.util.Iterator;
import java.util.LinkedList;


/**
 * This class applies an action visitor closure over an interval and select clauses
 */
public class Query
//	implements Factory<Object>
//		,HasStartAndEnd
{
	/**
	 * The constructor is empty.  The query is built by chaining together parts of statement
	 */
	private Query()
	{
	}

	/** Constructor for Query
	 * 
	 * @param _selectFrom 
	 */
	private Query(
		final SelectFrom... _selectFrom )
	{
		for (SelectFrom from : _selectFrom)
		{
			mySelectFromClauses.add( from );
		}
	}
	
	
	/**
	 * 
	 * @param _actionVisitor
	 * @return 
	 */
	public Query action( 
		final Closure<HasStartAndEnd> _actionVisitor )
	{
		myActionVisitor = _actionVisitor;
		return this;
	}

	public Object currentGroupByObject()
	{
		return myGroupByGenerator.current();
	}

	/**
	 * This is the main calculation function.  It will go thru all elements of the group by generator (if any) and
	 * call back the action visitor.
	 * Eventually, it will be capable of returning a generator which itself can be used in a subsequent query
	 */
	public IntervalGenerator[] execute()
	{
		if (myGroupByGenerator == null)
		{
			myGroupByGenerator = RangeIntervalGenerator.continuous();
		}

		do
		{
			// set range of this element
			myInterval.setStart( myGroupByGenerator.getStart() );
			myInterval.setEnd( myGroupByGenerator.getEnd() );

//			System.out.println( "query dates " + new java.util.Date( myStart ) + " - " + new java.util.Date( myInterval.getEnd() ) );		
			final Iterator<SelectFrom> itor = mySelectFromClauses.iterator();

			while (itor.hasNext() == true)
			{ 
				// go thru select from clauses until they are used up
				final SelectFrom clause = itor.next();
				clause.initializeCalculations();

				// is the clause is used up?
				if (clause.calculate( myInterval.getStart(), myInterval.getEnd() ) == true) 
				{
					// yes, remove it so it won't be treated again
					itor.remove();
				}
			}

			if ((myInterval.getStart() != 0L) 
			 && (myActionVisitor != null))
			{
				myActionVisitor.execute( myInterval );
			}

			// in case where there is no specified group by, should stop when no more things to treat
			if ((myHasGroupBy == false) 
			 && (mySelectFromClauses.isEmpty() == true))
			{
				break;
			}
		}
		while (myGroupByGenerator.evaluate( this ) == true);

		return null; //TODO return array of interval generators
	}

	/**
	 * @return Returns the actionVisitor.
	 */
	public Closure<HasStartAndEnd> getActionVisitor()
	{
		return myActionVisitor;
	}

	/**
	 * @return Returns the end.
	 */
//	@Override
//	public long getEnd()
//	{
//		return myEnd;
//	}

	/**
	 * @return Returns the groupByGenerator.
	 */
	public IntervalGenerator getGroupByGenerator()
	{
		return myGroupByGenerator;
	}

	public static Query getInstance()
	{
		return new Query();
	}

	/**
	 * @return Returns the start.
	 */
//	@Override
//	public long getStart()
//	{
//		return myStart;
//	}

	public Query groupBy( 
		final IntervalGenerator _groupByGenerator )
	{
		if (_groupByGenerator == null)
		{
			return this;
		}

		myHasGroupBy = true;
		myGroupByGenerator = _groupByGenerator;

		return this;
	}

	public Query selectFrom( 
		final SelectFrom... _selectFrom )
	{
//		mySelectFromClauses.add( _selectFrom );
//		return this;
		for (SelectFrom from : _selectFrom)
		{
			mySelectFromClauses.add( from );
		}
		
		return this;
	}

	public Query selectFrom( 
		final LinkedList<SelectFrom> _selectFromClauses )
	{
		mySelectFromClauses = _selectFromClauses;

		return this;
	}

	boolean myHasGroupBy = false;
//	private long myEnd;
//	private long myStart;
	private MutableInterval myInterval = new MutableInterval();
	private Closure<HasStartAndEnd> myActionVisitor = null;
	private IntervalGenerator myGroupByGenerator = null;
	private LinkedList<SelectFrom> mySelectFromClauses = new LinkedList<SelectFrom>();
}
