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
package com.projity.pm.criticalpath;

import com.projity.field.FieldContext;
import com.projity.field.FieldParseException;

import com.projity.pm.calendar.WorkCalendar;


public interface ScheduleWindow
{
	/** Offset the given date by the duration of the remaining duration.
	 *
	 * @param startDate
	 * @param dependencyDate
	 * @param ahead
	 * @param remainingOnly
	 * @param useSooner
	 * @return
	 */
	long calcOffsetFrom( long _startDate, long _dependencyDate, boolean _ahead, boolean _remainingOnly, boolean _useSooner );

//	long calcPredecessorLateFinish();

//	long calcPredecessorLateFinish( Dependency _dependency, long _duration );

//	long calcStart( Dependency _dependency, long _duration );
	
//	void calcStartAndFinish();

//	long calcSuccessorEarlyStart();

//	long calcSuccessorEarlyStart( Dependency _dependency, long _duration );    

	/**
	 * 
	 */
	void clearDuration();

	/**
	 * 
	 * @return 
	 */
	long getActualStart();

	/**
	 *
	 * @return
	 */
	long getConstraintDate();

	/**
	 * @return Returns the constraintType.
	 */
	int getConstraintType();

	/**
	 * 
	 * @return 
	 */
	long getDeadline();

	/**
	 * @return Returns the earlyFinish.
	 */
	long getEarlyFinish();

	/**
	 * @return Returns the earlyStart.
	 */
	long getEarlyStart();

	/**
	 * 
	 * @return 
	 */
	WorkCalendar getEffectiveWorkCalendar();

	/**
	 * 
	 * @return 
	 */
	long getElapsedDuration();

	/**
	 * 
	 * @return 
	 */
	long getEnd();

	/**
	 * The amount of excess time an activity has between its Early Finish and Late Finish dates.
	 */
	long getFinishSlack();

	/**
	 * @return Calculates the free float.
	 */
	long getFreeSlack();

	/**
	 * @return Returns the lateFinish.
	 */
	long getLateFinish();

	/**
	 * @return Returns the lateStart.
	 */
	long getLateStart();

	/**
	 * 
	 * @return 
	 */
	long getRawDuration();

//	Collection getScheduleChildren();

	/**
	 * 
	 * @return 
	 */	
	long getSplitDuration();

	/**
	 * The amount of excess time an activity has between its Early Start and Late Start dates.
	 */
	long getStartSlack();

	/**
	 * @return Calculates the total float.
	 */
	long getTotalSlack();

	/**
	 * @return Returns the windowEarlyFinish.
	 */
	long getWindowEarlyFinish();

	/**
	 * @return Returns the windowEarlyStart.
	 */
	long getWindowEarlyStart();

	/**
	 * @return Returns the windowLateFinish.
	 */
	long getWindowLateFinish();

	/**
	 * @return Returns the windowLateStart.
	 */
	long getWindowLateStart();

	/**
	 * 
	 * @param _fieldContext
	 * @return 
	 */
	boolean isReadOnlyConstraintDate( FieldContext _fieldContext );

	/**
	 * 
	 * @param _constraintDate 
	 */
	void setConstraintDate( long _constraintDate );

	/**
	 * @param constraintType The constraintType to set.
	 * @throws FieldParseException
	 */
	void setConstraintType( int _constraintType ) throws FieldParseException;

	/**
	 * 
	 * @param _deadline 
	 */
	void setDeadline( 	long _deadline );

	/**
	 * @param mustFinish The date the task must finish on.
	 */
	void setMustFinishOn( long _mustFinish );

	/**
	 * @param mustStart The date the task must start on.
	 */
	void setMustStartOn( long _mustStart );

	/**
	 * 
	 * @param _constraintType
	 * @param _date 
	 */
	void setScheduleConstraint( 	int _constraintType, long _date );

	/**
	 * @param windowEarlyFinish The windowEarlyFinish to set.
	 */
	void setWindowEarlyFinish( long _windowEarlyFinish );

	/**
	 * @param windowEarlyStart The windowEarlyStart to set.
	 */
	void setWindowEarlyStart( long _windowEarlyStart );

	/**
	 * @param windowLateFinish The windowLateFinish to set.
	 */
	void setWindowLateFinish( long _windowLateFinish );

	/**
	 * @param windowLateStart The windowLateStart to set.
	 */
	void setWindowLateStart( long _windowLateStart );

	public static int INVALID = -1;
}
