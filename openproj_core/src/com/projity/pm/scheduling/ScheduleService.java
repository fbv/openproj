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

import com.projity.configuration.Configuration;

import com.projity.field.Field;
import com.projity.field.FieldParseException;
import com.projity.field.FieldSetOptions;

import com.projity.functor.IntervalConsumer;

import com.projity.undo.FieldEdit;
import com.projity.undo.ScheduleEdit;
import com.projity.undo.SplitEdit;

import com.projity.util.ClassUtils;
import com.projity.util.DateTime;

import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;


/**
 * Singleton service for manipulating a schedule, such as by Gantt chart modifications
 */
public class ScheduleService
{
	/**
	 * Private constructor
	 */
	private ScheduleService()
	{
		super();
	}

//	public static void cleanUp()
//	{
//		instance = null;
//	}

	/**
	 * Calls back the consumer for each interval in the schedule.  Currently in only treats splits due to
	 * stop/resume. In the future it will also call back for splits in the work contour itself
	 * @param schedule
	 * @param consumer
	 */
	public static void consumeIntervals( 
		final Schedule _schedule,
		final IntervalConsumer _consumer )
	{
		if (myConsuming == true)
		{
			return;
		}

		myConsuming = true;
		_schedule.consumeIntervals( _consumer );
		myConsuming = false;
	}

	public static long getCompleted( 
		final Schedule _schedule )
	{
		// this is used for drawing completion on the gantt also. see GanttUI
		return _schedule.getCompletedThrough();
	}

	public static Field getCompletedField()
	{
		return Configuration.getFieldFromId( "Field.stop" );
	}

	/**
	 * @return Returns the singleton instance.
	 */
//	public static ScheduleService getInstance()
//	{
//		if (instance == null)
//		{
//			instance = new ScheduleService();
//		}
//
//		return instance;
//	}

	public static boolean isReadOnly( 
		final Schedule _schedule )
	{
		return ClassUtils.isObjectReadOnly( _schedule );
	}

	public static void setCompleted( 
		Object _eventSource,
		Schedule _schedule,
		long _completed,
		UndoableEditSupport _undoableEditSupport )
	{
		if (isReadOnly( _schedule ) == true)
		{
			return;
		}

		Field completedField = getCompletedField();

//		Object oldValue=completedField.getValue(schedule);
//		if (oldValue==null) oldValue=new Long(schedule.getActualStart());
		Object value = new Long( _completed );

		FieldSetOptions options = new FieldSetOptions();
		options.setRawUndo( true );

		try
		{
			completedField.setValue( _schedule, _eventSource, value, null, options );

			if ((options.getChange() != null) && (_undoableEditSupport != null) && !(_eventSource instanceof UndoableEdit))
			{
				FieldSetOptions undoOptions = new FieldSetOptions();
				undoOptions.setRawProperties( true );
				_undoableEditSupport.postEdit( new FieldEdit(completedField, _schedule, options.getChange().getNewValue(),
						options.getChange().getOldValue(), _eventSource, null, undoOptions) );
			}
		}
		catch (FieldParseException e)
		{
			// ignore
		}
	}

	/**
	 * Set the start or the end of the schedule and fire field event which will cause the critical path to run.  The method
	 * checks to see which of the two - start or end, was modified and only updates the modified one
	 * @param eventSource - the object which is the event source, such as GanttModel
	 * @param schedule - the task or assignment
	 * @param start - start date millisecond
	 * @param end - end date millisecond         
	 * @param interval
	 * @param undoableEditSupport
	 */
	public static void setInterval( 
		Object _eventSource,
		Schedule _schedule,
		long _start,
		long _end,
		ScheduleInterval _interval,
		UndoableEditSupport _undoableEditSupport )
	{
		if (isReadOnly( _schedule ) == true)
		{
			return;
		}

		Object detailBackup = null;
		_start = DateTime.hourFloor( _start );
		_end = DateTime.hourFloor( _end );

		if ((_interval.getStart() == _start) && (_interval.getEnd() == _end)) // if no move do nothing
		{
			return;
		}

		if ((_undoableEditSupport != null) && !(_eventSource instanceof UndoableEdit))
		{
			detailBackup = _schedule.backupDetail();
		}

		_schedule.moveInterval( _eventSource, _start, _end, _interval, false );

		//Undo
		if (detailBackup != null)
		{
			_undoableEditSupport.postEdit( new ScheduleEdit( _schedule, detailBackup, _start, _end, _interval, false, 
				_eventSource ) );
		}
	}

	/**
	 * Split a task/assignment by adding a nonworking interval.  If there is actual work during the split,
	 * only the nonworking part will be moved.  Unlike other products, we don't let you move actuals.
	 * @param eventSource- the object which is the event source, such as GanttModel
	 * @param schedule - the task or assignment
	 * @param from - beginning of nonwork interval
	 * @param to - end of nonwork interval
	 */
	public static void split( 
		Object _eventSource,
		Schedule _schedule,
		long _from,
		long _to,
		UndoableEditSupport _undoableEditSupport )
	{
		if (isReadOnly( _schedule ) == true)
		{
			return;
		}

		Object detailBackup = null;

		if ((_undoableEditSupport != null) && !(_eventSource instanceof UndoableEdit))
		{
			detailBackup = _schedule.backupDetail();
		}

		_schedule.split( _eventSource, DateTime.hourFloor( _from ), DateTime.hourFloor( _to ) );

		//Undo
		if (detailBackup != null)
		{
			_undoableEditSupport.postEdit( new SplitEdit( _schedule, detailBackup, _from, _to, _eventSource ) );
		}
	}

//	private static ScheduleService instance = null;
	private static boolean myConsuming = false;
}
