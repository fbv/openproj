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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * An abstract class for an interval generator that wraps a collection.
 */
public class CollectionIntervalGenerator<T extends HasStartAndEnd>
	implements HasStartAndEnd,
		IntervalGenerator
{
	/**
	 *
	 */
	protected CollectionIntervalGenerator( 
		Collection<T> _collection )
	{
		myCollection = _collection;
		initialize();
	}

	@Override
	public boolean canBeShared()
	{
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo( 
		Object _arg0 )
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.projity.algorithm.IntervalGenerator#current()
	 */
	@Override
	public HasStartAndEnd current()
	{
		if (myActive == true)
		{
			return myCurrent;
		}
		else
		{
			return this;
		}
	}

	/* (non-Javadoc)
	 * @see com.projity.algorithm.IntervalGenerator#next()
	 */
	@Override
	public boolean evaluate( 
		Object _obj )
	{
		// move start ahead		
		myStart = getEnd(); 

		if (myActive == true)
		{ 
			// active implies that the value comes from the collection 
			if (myIterator.hasNext() == false)
			{
				myCurrent = null;
				myFinished = true;
				myActive = false;

				return false;
			}

			myCurrent = myIterator.next();
		}

//		myStart = currentEnd(); // move start ahead

		// will set to active if the current item in collecition starts at start
		updateActiveState(); 

		return true;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.time.HasStartAndEnd#getEnd()
	 */
	@Override
	public long getEnd()
	{
		final long curEnd = (myCurrent == null)
			? Long.MAX_VALUE
			: myCurrent.getEnd();

		if (curEnd == 1)
		{
			System.out.println( " 1 cur end" );
		}

		return myActive
			? curEnd
			: (myCurrent == null)
				? curEnd
				: myCurrent.getStart();
	}

	public static CollectionIntervalGenerator getInstance( 
		final Collection _collection )
	{
		return new CollectionIntervalGenerator( _collection );
	}

	public static CollectionIntervalGenerator getInstance( 
		final HasStartAndEnd _interval )
	{
		final LinkedList<HasStartAndEnd> list = new LinkedList<HasStartAndEnd>();
		list.add( _interval );

		return getInstance( list );
	}

	/* (non-Javadoc)
	 * @see com.projity.algorithm.IntervalGenerator#currentStart()
	 */
	@Override
	public long getStart()
	{
		return myStart;

//		return (myCurrent == null) ? lastEnd : ((HasStartAndEnd)myCurrent).getStart();
	}

	/* (non-Javadoc)
	 * @see com.projity.algorithm.IntervalGenerator#hasNext()
	 */
	@Override
	public boolean hasNext()
	{
		return myIterator.hasNext();
	}

	protected final void initialize()
	{
		myIterator = makeIterator();

		if (myIterator.hasNext() == true)
		{
			myCurrent = myIterator.next();
			updateActiveState();
		}
	}

	@Override
	public boolean isCurrentActive()
	{
		return myActive;
	}

	/**
	 * @return Returns the finished.
	 */
	public boolean isFinished()
	{
		return myFinished;
	}

	protected Iterator<T> makeIterator()
	{
		if (myCollection instanceof List)
		{
			return ((List<T>)myCollection).listIterator();
		}
		else
		{
			return myIterator;
		}
	}

	private void updateActiveState()
	{
		if (myCurrent != null)
		{
			myActive = (myCurrent.getStart() == myStart);
		}
	}

	protected Collection<T> myCollection;
	Iterator<T> myIterator;
	protected boolean myFinished = false;
	T myCurrent = null;
	boolean myActive = false;
	long myStart = 0;
}
