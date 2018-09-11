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

import com.projity.field.FieldContext;

import com.projity.functor.IntervalConsumer;

import com.projity.pm.calendar.WorkCalendar;
import com.projity.pm.time.MutableHasStartAndEnd;

/**
 *
 */
public interface Schedule
	extends Cloneable,
		MutableHasStartAndEnd,
		TimeSheetSchedule
{
	/**
	 * 
	 * @return 
	 */
	Object backupDetail();

	/**
	 * 
	 */
	void clearDuration();

	/**
	 * 
	 * @param _consumer 
	 */
	void consumeIntervals( IntervalConsumer _consumer );

	/**
	 * 
	 * @param _fieldContext
	 * @return 
	 */
	boolean fieldHideTimesheetClosed( FieldContext _fieldContext );

	/**
	 * 
	 * @return 
	 */
	long getActualDuration();

	/**
	 * 
	 * @return 
	 */
	long getActualFinish();

	/**
	 * 
	 * @return 
	 */
	long getActualStart();

	/**
	 * 
	 * uses % complete as opposed to stop which is soonest of all assignments
	 * 
	 * @return 
	 */
	long getCompletedThrough(); 

	/**
	 * 
	 * the date on which constraints push this task.   Can be different from start if splitting a started task/assignment
	 * 
	 * @return 
	 */
	long getDependencyStart(); 

	/**
	 * 
	 * @return 
	 */
	long getDuration();

	/**
	 * 
	 * @return 
	 */
//	long getDurationActive();

	/**
	 * 
	 * @return 
	 */
//	long getDurationSpan();

	/**
	 * 
	 * if many assignments, date where the the first stop occurs in any of them
	 * 
	 * @return 
	 */
	long getEarliestStop();

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
	long getResume();

	/**
	 * Stop is the earliest completion date of the assignments
	 * @return
	 */
	long getStop();

	/**
	 * 
	 * @return 
	 */
	boolean isJustModified();

	/**
	 * 
	 * @param _fieldContext
	 * @return 
	 */
	boolean isReadOnlyActualDuration( FieldContext _fieldContext );

	/**
	 * 
	 * @param _fieldContext
	 * @return 
	 */
	boolean isReadOnlyActualFinish( FieldContext _fieldContext );

	/**
	 * 
	 * @param _fieldContext
	 * @return 
	 */
	boolean isReadOnlyActualStart( FieldContext _fieldContext );

	/**
	 * 
	 * @param _context
	 * @return 
	 */
	boolean isReadOnlyCompletedThrough( FieldContext _context );

	/**
	 * 
	 * @param _fieldContext
	 * @return 
	 */
	boolean isReadOnlyTimesheetClosed( FieldContext _fieldContext );

	/**
	 * 
	 * @return 
	 */
	boolean isTimesheetClosed();

	/**
	 * 
	 * @return 
	 */
	boolean isTimesheetComplete();

	/**
	 * 
	 * @param _eventSource
	 * @param _start
	 * @param _end
	 * @param _oldInterval
	 * @param _isChild 
	 */
	void moveInterval( Object _eventSource, long _start, long _end, ScheduleInterval _oldInterval, boolean _isChild );

	/**
	 * Move remaining work to date - used when doing a project update for this task
	 * @param _date 
	 */
	void moveRemainingToDate( long _date );

	/**
	 * 
	 * @param _eventSource
	 * @param _detail
	 * @param _isChild 
	 */
	void restoreDetail( Object _eventSource, Object _detail, boolean _isChild );

	/**
	 * 
	 * @param _actualDuration 
	 */
	void setActualDuration( long _actualDuration );

	/**
	 * 
	 * @param _actualFinish 
	 */
	void setActualFinish( long _actualFinish );

	/**
	 * 
	 * @param _actualStart 
	 */
	void setActualStart( long _actualStart );

	/**
	 * 
	 * @param _completedThrough 
	 */
	void setCompletedThrough( long _completedThrough );

	/**
	 * 
	 * @param _dependencyStart 
	 */
	void setDependencyStart( long _dependencyStart );

	/**
	 * 
	 * @param _duration 
	 */
	void setDuration( long _duration );

	/**
	 * 
	 * @param _durationActive 
	 */
//	void setDurationActive( long _durationActive );

	/**
	 * 
	 * @param _durationSpan 
	 */
//	void setDurationSpan( long _durationSpan );

	/**
	 * 
	 * @param _resume 
	 */
	void setResume( long _resume );

	/**
	 * 
	 * @param _stop 
	 */
	void setStop( long _stop );

	/**
	 * 
	 * @param _timesheetClosed 
	 */
	void setTimesheetClosed( boolean _timesheetClosed );

	/**
	 * 
	 * @param _timesheetComplete 
	 */
	void setTimesheetComplete( boolean _timesheetComplete );

	/**
	 * Split a task or assignment by adding dead time between from and to
	 *
	 * @param _eventSource
	 * @param _from
	 * @param _to
	 */
	void split( Object _eventSource, long _from, long _to );

	public static final double INSTANT_COMPLETION = 1E-30D;
}
