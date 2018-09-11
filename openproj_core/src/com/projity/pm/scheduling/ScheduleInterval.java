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
package com.projity.pm.scheduling;

import com.projity.pm.time.HasStartAndEnd;

import com.projity.util.DateTime;
import java.util.Date;


/**
 * class to hold bar information
 */
public class ScheduleInterval
	implements HasStartAndEnd,
		Cloneable
{
	/** Constructor for ScheduleInterval
	 * 
	 * @param _start used to initialize the start attribute
	 * @param _end used to initialize the end attribute
	 */
	public ScheduleInterval( 
		final long _start,
		final long _end )
	{
		myStart = _start;
		myEnd = _end;
		myMaximumStart = DateTime.getMaxDate().getTime();
		myMaximumEnd = myMaximumStart;

		//System.out.println( "ScheduleInterval(): start = " + new Date( myStart ) + "; end = " + new Date( myEnd ) );
	}

	public ScheduleInterval(
		final ScheduleInterval _source )
	{
		this( _source.myStart, _source.myEnd );
	}
	
	/**
	 * 
	 * @return 
	 */
	public boolean canChangeDuration()
	{
		return myMinimumEnd != myMaximumEnd;
	}

	/**
	 * 
	 * @return 
	 */
	public boolean canChangeStart()
	{
		return myMinimumStart != myMaximumStart;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public ScheduleInterval clone()
	{
		return new ScheduleInterval( this );
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.time.HasStartAndEnd#getEnd()
	 */
	@Override
	public long getEnd()
	{
		return myEnd;
	}

	/** Return the value of the maximum end attribute.
	 * 
	 * @return Returns the maximum end.
	 */
	public long getMaximumEnd()
	{
		return myMaximumEnd;
	}

	/** Return the value of the maximum start attribute.
	 * 
	 * @return Returns the maximum start.
	 */
	public long getMaximumStart()
	{
		return myMaximumStart;
	}

	/** Return the value of the minimum end attribute.
	 * 
	 * @return Returns the minimum end.
	 */
	public long getMinimumEnd()
	{
		return myMinimumEnd;
	}

	/** Return the value of the minimum start attribute.
	 * 
	 * @return Returns the minimum start.
	 */
	public long getMinimumStart()
	{
		return myMinimumStart;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.time.HasStartAndEnd#getStart()
	 */
	@Override
	public long getStart()
	{
		return myStart;
	}

	public ScheduleInterval intersectWith( 
		final HasStartAndEnd _bounds )
	{
		if (_bounds == null)
		{
			return this;
		}

		final ScheduleInterval result = (ScheduleInterval)clone();
		result.myStart = Math.max( myStart, _bounds.getStart() );
		result.myEnd = Math.min( myEnd, _bounds.getEnd() );

		return result;
	}

	public boolean isValid()
	{
		return myStart <= myEnd;
	}

	/** Assign the maximum end attribute
	 * 
	 * @param _maximumEnd used as the source of the assignment
	 */
	public void setMaximumEnd( 
		final long _maximumEnd )
	{
		myMaximumEnd = _maximumEnd;
	}

	/** Assign the maximum start attribute
	 * 
	 * @param _maximumStart used as the source of the assignment
	 */
	public void setMaximumStart( 
		final long _maximumStart )
	{
		myMaximumStart = _maximumStart;
	}

	/** Assign the minimum end attribute
	 * 
	 * @param _minimumEnd used as the source of the assignment
	 */
	public void setMinimumEnd( 
		final long _minimumEnd )
	{
		myMinimumEnd = _minimumEnd;
	}

	/** Assign the minimum start attribute
	 * 
	 * @param _minimumStart used as the source of the assignment
	 */
	public void setMinimumStart( 
		final long _minimumStart )
	{
		myMinimumStart = _minimumStart;
	}

	@Override
	public String toString()
	{
		return "Start:" + new java.util.Date( myStart ) + " End:" + new java.util.Date( myEnd );
	}

	boolean canChangeOwner()
	{ 
		// for now don't allow dragging to a different row
		return false;
	}

	long myEnd;
	long myMaximumEnd;
	long myMaximumStart;
	long myMinimumEnd = 0;
	long myMinimumStart = 0;
	long myStart;
}
