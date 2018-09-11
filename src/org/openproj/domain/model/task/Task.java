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
package org.openproj.domain.model.task;

import com.projity.algorithm.ReverseQuery;
import com.projity.algorithm.TimeIteratorGenerator;
import com.projity.algorithm.buffer.CalculatedValues;

import com.projity.association.AssociationFormatParameters;
import com.projity.association.AssociationList;
import com.projity.association.AssociationListFormat;

import com.projity.configuration.CircularDependencyException;
import com.projity.configuration.Configuration;
import com.projity.configuration.Settings;

import com.projity.datatype.Duration;
import com.projity.datatype.DurationFormat;
import com.projity.datatype.ImageLink;
import com.projity.datatype.Rate;
import com.projity.datatype.TimeUnit;

import com.projity.document.ObjectEvent;

import com.projity.field.*;

import com.projity.functor.IntervalConsumer;
import com.projity.functor.NumberClosure;
import com.projity.functor.ObjectVisitor;

import com.projity.graphic.configuration.HasIndicators;
import com.projity.graphic.configuration.HasTaskIndicators;

import com.projity.grouping.core.Node;
import com.projity.grouping.core.NodeList;
import com.projity.grouping.core.OutlineCollection;
import com.projity.grouping.core.model.NodeModel;
import com.projity.grouping.core.model.NodeModelUtil;
import com.projity.grouping.core.summaries.DeepChildWalker;
import com.projity.grouping.core.summaries.DivisionSummaryVisitor;
import com.projity.grouping.core.summaries.LeafWalker;

import com.projity.options.CalculationOption;
import com.projity.options.CalendarOption;
import com.projity.options.ScheduleOption;

import com.projity.pm.assignment.Allocation;
import com.projity.pm.assignment.Assignment;
import com.projity.pm.assignment.AssignmentFormat;
import com.projity.pm.assignment.AssignmentService;
import com.projity.pm.assignment.TimeDistributedConstants;
import com.projity.pm.assignment.timesheet.TimesheetHelper;
import com.projity.pm.calendar.CalendarService;
import org.openproj.domain.calendar.HasCalendar;
import com.projity.pm.calendar.WorkCalendar;
import com.projity.pm.costing.*;
import com.projity.pm.criticalpath.PredecessorTaskList;
import com.projity.pm.criticalpath.ScheduleWindow;
import com.projity.pm.criticalpath.TaskSchedule;
import com.projity.pm.dependency.*;
import com.projity.pm.key.HasKey;
import com.projity.pm.key.HasKeyImpl;
import com.projity.pm.resource.Resource;
import com.projity.pm.resource.ResourceImpl;
import com.projity.pm.scheduling.BarClosure;
import com.projity.pm.scheduling.ConstraintType;
import com.projity.pm.scheduling.Schedule;
import com.projity.pm.scheduling.ScheduleEvent;
import com.projity.pm.scheduling.ScheduleInterval;
import com.projity.pm.scheduling.ScheduleUtil;
import com.projity.pm.scheduling.SchedulingFields;
import com.projity.pm.scheduling.SchedulingRule;
import com.projity.pm.scheduling.SchedulingType;
import com.projity.pm.snapshot.BaselineScheduleFields;
import com.projity.pm.snapshot.DataSnapshot;
import com.projity.pm.snapshot.SnapshotContainer;
import com.projity.pm.snapshot.Snapshottable;
import com.projity.pm.task.*;
import com.projity.pm.time.HasStartAndEnd;

import com.projity.server.access.ErrorLogger;
import com.projity.server.data.DataObject;

import com.projity.strings.Messages;

import com.projity.util.Alert;
import com.projity.util.ClassUtils;
import com.projity.util.DateTime;
import com.projity.util.Environment;
import com.projity.util.HashMapWithDirtyFlags;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.ParseException;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;

import org.openproj.domain.costing.ExpenseType;
import org.openproj.domain.document.Document;
import org.openproj.domain.model.Model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.*;

import javax.swing.SwingUtilities;


/**
 * @stereotype thing
 */
public class Task
	implements Allocation,
		EarnedValueFields,
		EarnedValueValues,
		HasExtraFields,
		HasTaskIndicators,
		SchedulingFields,
		org.openproj.domain.task.Task
{
	public Task()
	{
		super();
	}

	public Task( 
		final Project _project )
	{
		this(_project.isLocal(), _project);
	}

	public Task( 
		final boolean _local )
	{
		super();

		hasKey = new HasKeyImpl(_local, this);
		myCurrentSchedule = new TaskSchedule();
		initializeTransientTaskObjects();
	}

	public Task( 
		final boolean _local,
		final Project _project )
	{
		this(_local);

		project = _project;

		initializeDates();
		addDefaultAssignment();
	}

	//	/**
	//	 * This constructor is used to create dummy tasks, such as the UNASSIGNED
	//	 * instance. We do not want to perform standard initialization on it.
	//	 *
	//	 * @param _dummy
	//	 */
	//	private NormalTask( 
	//		final boolean _dummy )
	//	{
	//		super( true );
	//	}

	/**
	 *
	 * @param _source
	 */
	public Task( 
		final Task _source )
	{
		//		barClosureInstance = new BarClosure();
		if (extraFields != null)
		{
			extraFields = (HashMapWithDirtyFlags)ClassUtils.deepCopy( extraFields );
			setInvestmentMilestone( null ); // this is not cloneable as must be unique
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double actualCost( 
		final long _start,
		final long _end )
	{
		if (isParentWithoutAssignments())
		{
			return 0.0D;
		}

		return ((TaskSnapshot)getCurrentSnapshot()).actualCost( _start, _end );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double actualFixedCost( 
		final long _start,
		final long _end )
	{
		return fixedCost( _start, Math.min( getStop(), _end ) );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long actualWork( 
		final long _start,
		final long _end )
	{
		if (isParentWithoutAssignments())
		{
			return 0L;
		}

		return ((TaskSnapshot)getCurrentSnapshot()).actualWork( _start, _end );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double acwp( 
		final long _start,
		final long _end )
	{
		return ((TaskSnapshot)getCurrentSnapshot()).acwp( _start, _end );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addAssignment( 
		final Assignment _assignment )
	{
		//project.beginUndoUpdate();
		boolean recalculateDuration = !_assignment.isDefault() && _assignment.isInitialized() && _assignment.isLabor();
		Assignment defaultAssignment = findAssignment( ResourceImpl.getUnassignedInstance() );

		if (!_assignment.isDefault())
		{
			// get rid of any default
			if (defaultAssignment != null)
			{ //Remove any default assignment

				if (project.isActualsProtected())
				{
					defaultAssignment.setPercentComplete( 0 ); // do not keep actuals
					recalculateDuration = false;
				}

				_assignment.usePropertiesOf( defaultAssignment ); // the new assignment must take on properties of the default assignment
				AssignmentService.getInstance().remove( defaultAssignment, null, true );
			}
			else
			{
				// if the task is started already, then only apply to remaining duration.  This means added delay to new assignment
				if (getActualStart() != 0L)
				{
					_assignment.setDelay( Duration.millis( getActualDuration() ) );
				}

				_assignment.adjustRemainingDuration( Duration.millis( getRemainingDuration() ), false );
			}

			if (Environment.isNoPodServer()) // fixed cost should be cleared if entering real assignments if mariner
			{
				setFixedCost( 0 );
			}
		}
		else
		{
			if (defaultAssignment != null) //Remove any default assignment.  This happens importing if the imported task just has no assignments
			{
				AssignmentService.getInstance().remove( defaultAssignment, null, true );
			}

			// use default task duration for the default assignment duraiton
			_assignment.setDuration( getRawDuration() );
		}

		// must calculate these two values before adding assignment!
		double mostLoadedAssignmentUnits = getMostLoadedAssignmentUnits();

		// Get details of current assignments before change
		double assignedRate = getRemainingUnits();

		// add assignment
		((TaskSnapshot)getCurrentSnapshot()).addAssignment( _assignment );

		if (!_assignment.isInitialized()) // if reading in, then don't recalc duration
		{
			return;
		}

		// if effort driven then set duration
		if (recalculateDuration && isEffortDriven())
		{
			if (assignedRate != 0)
			{ //

				if (getSchedulingType() == SchedulingType.FIXED_DURATION) // fixed duration effort driven has complicated rule - a new assignment is weighted the same as the most loaded assignment, unless that assignment is over 100%
				{
					_assignment.adjustRemainingUnits( Math.min( 1.0, mostLoadedAssignmentUnits ), 1, false, false );
				}

				double newRemainingUnits = assignedRate + _assignment.getRemainingLaborUnits();

				getSchedulingRule().adjustRemainingUnits( this, newRemainingUnits, assignedRate, true, true ); // conserve total units
			}
		}

		setDirty( true );

		//project.endUndoUpdate();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Assignment addDefaultAssignment()
	{
		Assignment ass = newDefaultAssignment();
		addAssignment( ass );

		return ass;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void adjustActualStartFromAssignments()
	{
		Assignment assignment;
		Iterator i = getAssignments().iterator();
		long start = 0L;

		while (i.hasNext() == true)
		{
			assignment = (Assignment)i.next();

			if (assignment.getPercentComplete() > 0.0D)
			{
				start = getStart();

				break;
			}
		}

		//		System.out.println("adjusting actual start to " + new java.util.Date(start));

		// JGao 9/17/2009 Added this logic here to fix the issue for pulling timesheet entries
		// with Mariner integration. The reason being that it is a batch operation, the actualStart of the
		// task will get adjusted as more actual work is being set. Don't adjust it here unless no actual work at all.
		boolean setActualStartRightAway = ((start == 0L) || !Environment.isUpdatingTimesheet());

		if (setActualStartRightAway)
		{
			setActualStart( start );
		}
		else
		{
			setActualStartNoEvent( actualStart );
			markTaskAsNeedingRecalculation();
			project.fireScheduleChanged( this, ScheduleEvent.ACTUAL, this );
		}

		assignParentActualDatesFromChildren();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void adjustRemainingDuration( 
		final long _newDuration,
		final boolean _doChildren )
	{
		//hk		long newRemainingDuration = Duration.millis(newDuration) - getActualDuration(); // assignments dont treqt
		long newRemainingDuration = Duration.millis( _newDuration ); // - getActualDuration(); // assignments dont treqt
																	 // units

		// JGao 12/7/2009 Changed the logic to handle the special case when there is zero work to begine with and there is no labor assignment.
		// In this case, the default assignment units is 0 and needs to be set to default of 1.0 in order for the adjusting remaining duration to be correct.
		boolean resetAssignmentUnits = false;
		AssociationList assignments = this.getAssignments();

		if ((assignments.size() == 1) && ((Assignment)assignments.getFirst()).isDefault() && (getWork() == 0.0) &&
				(getRemainingUnits() == 0.0))
		{
			resetAssignmentUnits = true;
		}

		Iterator i = assignments.iterator();

		while (i.hasNext() == true)
		{
			Assignment assignment = (Assignment)i.next();

			if (resetAssignmentUnits)
			{
				assignment.getDetail().setRate( new Rate(1.0, assignment.getRate().getTimeUnit()) );
			}

			assignment.adjustRemainingDurationIfWorkingAtTaskEnd( newRemainingDuration );
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void adjustRemainingUnits( 
		final double _newRemainingUnits,
		final double _oldRemainingUnits,
		final boolean _doChildren,
		final boolean _conserveTotalUnits )
	{
		if (!_doChildren)
		{
			return;
		}

		double multiplier = 1;

		if (_conserveTotalUnits)
		{
			multiplier = _oldRemainingUnits / _newRemainingUnits;
		}

		double u = _newRemainingUnits;
		double remaining = getRemainingUnits();
		double factor = u / remaining;
		Iterator i = getAssignments().iterator();

		while (i.hasNext() == true)
		{
			Assignment assignment = (Assignment)i.next();
			double r = assignment.getLaborUnits();

			//			if (!assignment.isLabor())
			//				continue;
			if (_conserveTotalUnits)
			{
				getSchedulingRule()
					.adjustRemainingUnits( assignment, assignment.getRemainingLaborUnits() * multiplier,
					assignment.getRemainingLaborUnits(), false, false );
			}
			else
			{
				getSchedulingRule().adjustRemainingUnits( assignment, factor * r, r, false, false );
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void adjustRemainingWork( 
		final double _multiplier,
		final boolean _doChildren )
	{
		//		long newDuration = (long) (getDurationMillis() * multiplier);

		//need to always do children regardless of doChildren flag
		Iterator i = getAssignments().iterator();

		while (i.hasNext() == true)
		{
			Assignment assignment = (Assignment)i.next();

			if (!assignment.isLabor())
			{
				continue;
			}

			getSchedulingRule().adjustRemainingWork( assignment, (long)(assignment.getRemainingWork() * _multiplier), false );
		}
	}

	/**
	 * Called when an assignment value is modified. We want the task details to
	 * be modified without changing the assignment details
	 *
	 * @param deltaAdded
	 */
	public void adjustUnitsDelta( 
		final double _deltaAdded )
	{
		getSchedulingRule().adjustRemainingUnits( this, getRemainingUnits() + _deltaAdded, getRemainingUnits(), false, false );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean applyTimesheet( 
		final Collection _fieldArray,
		final long _timesheetUpdateDate )
	{
		return TimesheetHelper.applyTimesheet( getAssignments(), _fieldArray, _timesheetUpdateDate );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void arrangeChildren( 
		final Collection _addTo,
		final boolean _markerStatus,
		final int _depth )
	{
		//note that it is possible that this is called for non parents
		Collection children = getWbsChildrenNodes(); // I depend on my predecessors children

		if (children != null)
		{
			Iterator p = children.iterator();
			Object current;
			org.openproj.domain.task.Task child;

			while (p.hasNext())
			{
				current = ((Node)p.next()).getValue();

				if (!(current instanceof org.openproj.domain.task.Task))
				{
					continue;
				}

				child = (org.openproj.domain.task.Task)current;
				child.arrangeTask( _addTo, _markerStatus, _depth + 1 );
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void arrangeTask( 
		final Collection _addTo,
		final boolean _markerStatus,
		final int _depth )
	{
		if (this.markerStatus == _markerStatus) // if task has been added, don't treat it again
		{
			return;
		}

		if (Environment.isImporting() && (_depth >= 1000)) // in case circular link in imported project - TODO this is not a perfect solution
		{
			throw new RuntimeException(CircularDependencyException.RUNTIME_EXCEPTION_TEXT);
		}

		// Arrange my parent

		// Arrange my predecessors
		Iterator i = getPredecessorList().iterator();
		org.openproj.domain.task.Task predecessor;
		Dependency dep;

		if (wbsParentTask != null)
		{
			wbsParentTask.arrangeTask( _addTo, _markerStatus, _depth + 1 );
		}

		//May 2 2008. this seems to be right place for setting marker status
		// If it is after adding this task below, you can get into an endless loop
		// If it is put at the top, you can get into situations where the PARENT_END is added before all children are added
		markerStatus = _markerStatus;

		while (i.hasNext() == true)
		{
			dep = (Dependency)i.next();

			if (dep.isDisabled())
			{
				continue;
			}

			predecessor = (org.openproj.domain.task.Task)dep.getPredecessor();
			predecessor.arrangeTask( _addTo, _markerStatus, _depth + 1 );
			predecessor.arrangeChildren( _addTo, _markerStatus, _depth );
		}

		// Process current task
		PredecessorTaskList.TaskReference taskReference = new PredecessorTaskList.TaskReference(this);
		_addTo.add( taskReference );

		//This was the old place for the above		this.markerStatus = markerStatus;  Move it back if bugs happen

		// Arrange my children
		if (isWbsParent() == true)
		{
			taskReference.setParentBegin();
			arrangeChildren( _addTo, _markerStatus, _depth );
			taskReference = new PredecessorTaskList.TaskReference(this);
			taskReference.setParentEnd();
			_addTo.add( taskReference );
		}
	}

	boolean areActuals()
	{
		return ((getPercentComplete() > 0D) || (getActualStart() > 0) || (getActualFinish() > 0));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void assignActualDatesFromChildren()
	{
		long computedActualStart = Long.MAX_VALUE;
		long stop = 0;
		Collection children = wbsChildrenNodes;

		if (children == null)
		{
			return;
		}

		Iterator i = children.iterator();
		Task child;
		long currentActualStart;
		long oldActualDuration = Duration.millis( getActualDuration() );
		Object current;

		while (i.hasNext() == true)
		{
			current = ((Node)i.next()).getValue();

			if (!(current instanceof Task))
			{
				continue;
			}

			child = (Task)current;

			if (child.getActualStart() == 0) // || (child.isZeroDuration() && child.getPercentComplete() > 0)) // changd from percent complete check -hk 1/9/09
			{
				continue;
			}

			if ((currentActualStart = child.getActualStart()) != 0) // if any task has actual start, use the earliest value
			{
				computedActualStart = Math.min( computedActualStart, currentActualStart );
			}

			stop = Math.max( stop, child.getStop() );
		}

		long actualDuration = 0;

		if ((computedActualStart != Long.MAX_VALUE) && (stop != 0))
		{
			actualDuration = getEffectiveWorkCalendar().compare( stop, getStart(), false );
		}

		if (computedActualStart != Long.MAX_VALUE)
		{
			setActualStartNoEvent( computedActualStart );
		}
		else
		{
			setActualStartNoEvent( 0L );
		}

		if (actualDuration != oldActualDuration)
		{
			double percentComplete = ((double)actualDuration) / getDurationMillis();

			//			System.out.println(this + " setting percent complete to " + percentComplete);
			myCurrentSchedule.setPercentComplete( percentComplete );
			markTaskAsNeedingRecalculation(); // so it redraws
		}
	}

	/**
	 * set actual start and completion date for parents
	 *
	 */
	protected void assignParentActualDatesFromChildren()
	{
		Task parent = this;

		while ((parent = (Task)parent.getWbsParentTask()) != null)
		{
			parent.assignActualDatesFromChildren();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double bac( 
		final long _start,
		final long _end )
	{
		return ((TaskSnapshot)getCurrentSnapshot()).bac( _start, _end );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object backupDetail()
	{
		return backupDetail( null );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object backupDetail( 
		final Object _snapshotId )
	{
		TaskSnapshot snapshot = (TaskSnapshot)((_snapshotId == null)
			? getCurrentSnapshot()
			: getSnapshot( _snapshotId ));
		TaskSnapshotBackup snapshotBackup = TaskSnapshotBackup.backup( snapshot, /*snapshotId!=null*/
				true );
		TaskBackup backup = new TaskBackup();
		backup.snapshot = snapshotBackup;
		backup.windowEarlyFinish = windowEarlyFinish;
		backup.windowEarlyStart = windowEarlyStart;
		backup.windowLateFinish = windowLateFinish;
		backup.windowLateStart = windowLateStart;
		backup.actualStart = actualStart;

		//TODO Backup other fields?
		return backup;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double baselineCost( 
		final long _start,
		final long _end )
	{
		if (getBaselineSnapshot() == null)
		{
			return 0;
		}

		return getBaselineSnapshot().cost( _start, _end ) + getBaselineSnapshot().fixedCost( _start, _end );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long baselineWork( 
		final long _start,
		final long _end )
	{
		if (getBaselineSnapshot() == null)
		{
			return 0;
		}

		if (isParentWithoutAssignments())
		{
			return 0L;
		}

		return getBaselineSnapshot().work( _start, _end );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double bcwp( 
		final long _start,
		final long _end )
	{
		return ((TaskSnapshot)getCurrentSnapshot()).bcwp( _start, _end );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double bcws( 
		final long _start,
		final long _end )
	{
		return ((TaskSnapshot)getCurrentSnapshot()).bcws( _start, _end );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void buildReverseQuery( 
		final ReverseQuery _reverseQuery )
	{
		//Do this ones assignments
		((TaskSnapshot)getCurrentSnapshot()).buildReverseQuery( _reverseQuery );

		Object current;

		if (wbsChildrenNodes != null)
		{
			//  do for all children as well
			Iterator i = wbsChildrenNodes.iterator();
			Task child;

			while (i.hasNext() == true)
			{
				current = ((Node)i.next()).getValue();

				if (!(current instanceof Task))
				{
					continue;
				}

				child = (Task)current;
				child.buildReverseQuery( _reverseQuery );
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long calcActiveAssignmentDuration( 
		final WorkCalendar _workCalendarToUse )
	{
		return ((TaskSnapshot)getCurrentSnapshot()).calcActiveAssignmentDuration( _workCalendarToUse );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void calcDataBetween( 
		final TimeDistributedConstants _type,
		final TimeIteratorGenerator _generator,
		final CalculatedValues _values )
	{
		((TaskSnapshot)getCurrentSnapshot()).calcDataBetween( _type, _generator, _values );
	}

	private static long calcFreeSlack( 
		final Dependency _dependency )
	{
		ScheduleWindow predecessor = (ScheduleWindow)_dependency.getPredecessor();
		ScheduleWindow successor = (ScheduleWindow)_dependency.getSuccessor();
		long t = 0;
		WorkCalendar cal = _dependency.getEffectiveWorkCalendar();

		if (_dependency.getDependencyType() == DependencyType.FS)
		{
			t = cal.compare( cal.add( successor.getEarlyStart(), -_dependency.getLeadValue(), true ),
					predecessor.getEarlyFinish(), false );
		}
		else if (_dependency.getDependencyType() == DependencyType.FF)
		{
			t = cal.compare( cal.add( successor.getEarlyFinish(), -_dependency.getLeadValue(), true ),
					predecessor.getEarlyFinish(), false );
		}
		else if (_dependency.getDependencyType() == DependencyType.SS)
		{
			t = cal.compare( cal.add( successor.getEarlyStart(), -_dependency.getLeadValue(), true ),
					predecessor.getEarlyStart(), false );
		}
		else if (_dependency.getDependencyType() == DependencyType.SF)
		{
			t = cal.compare( cal.add( successor.getEarlyFinish(), -_dependency.getLeadValue(), true ),
					predecessor.getEarlyStart(), false );
		}

		return t;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long calcOffsetFrom( 
		final long _startDate,
		final long _dependencyDate,
		final boolean _ahead,
		final boolean _remainingOnly,
		final boolean _useSooner )
	{
		//		This is a task based implementation- for parents dont use their assignments
		if (isWbsParent())
		{
			long d = _remainingOnly
				? Duration.millis( getRemainingDuration() )
				: getDurationMillis();

			if (!_ahead)
			{
				d = -d;
			}

			return getEffectiveWorkCalendar().add( _startDate, d, _useSooner );
		}

		//
		//
		//		This is an assignment based implementation
		Iterator i = getAssignments().iterator();
		long result;
		Assignment assignment;

		if (_startDate < 0)
		{
			result = _ahead
				? Long.MIN_VALUE
				: 0;
		}
		else
		{
			result = _ahead
				? 0
				: Long.MAX_VALUE;
		}

		while (i.hasNext() == true)
		{
			assignment = (Assignment)i.next();

			long offsetDate = assignment.calcOffsetFrom( _startDate, _dependencyDate, _ahead, _remainingOnly, _useSooner );
			result = _ahead
				? Math.max( result, offsetDate )
				: Math.min( result, offsetDate );
		}

		return result;
	}

	private long calcSummedActualWork()
	{
		NodeModel nodeModel = project.getTaskOutline();
		Node node = nodeModel.search( this );
		final Field field = Configuration.getFieldFromId( "Field.actualWork" );
		Number value = (Number)NodeModelUtil.getValue( field, node, nodeModel, null );

		return value.longValue();
	}

	private long calcSummedWork()
	{
		NodeModel nodeModel = project.getTaskOutline();
		Node node = nodeModel.search( this );

		if (node == null)
		{
			return 0;
		}

		final Field field = Configuration.getFieldFromId( "Field.work" );
		Number value = (Number)NodeModelUtil.getValue( field, node, nodeModel, null );

		return value.longValue();
	}

	private long calcTimeToStartFromNow()
	{
		return getStart() - System.currentTimeMillis();
	}

	public long calcWork()
	{
		if (!hasRealAssignments()) // avoid treating dummy assignment
		{
			return 0;
		}

		return getWork( null );
	}

	boolean canDeleteActuals()
	{
		return project.getProjectPermission().isDeleteActuals();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection childrenToRollup()
	{
		return ((TaskSnapshot)getCurrentSnapshot()).getHasAssignments().childrenToRollup();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void cleanClone()
	{
		owningProject = null;
		project = null;
	}

//	public Collection getScheduleChildren() {
//		return getWbsChildrenNodes();
//	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void cleanUp( 
		final Object _eventSource,
		final boolean _deep,
		final boolean _undo,
		final boolean _cleanDependencies )
	{
//		super.cleanUp( eventSource, deep, undo, cleanDependencies ); // gets rid of dependencies
		//if (!cleanDependencies) return; //TODO was for undo of paste of linked tasks, but doesn't work
		markAllDependentTasksAsNeedingRecalculation( false );

		// remove sentinel dependencies if any
		project.removeStartSentinelDependency( this );
		project.removeEndSentinelDependency( this );

		// remove all links to or from
		LinkedList toRemove = new LinkedList(); //fix
		DependencyService.prepareToRemove( getPredecessorList(), toRemove );

		for (Iterator j = toRemove.iterator(); j.hasNext();)
		{
			DependencyService.getInstance().remove( (Dependency)j.next(), _eventSource, _undo ); //fix
		}

		toRemove.clear();
		DependencyService.prepareToRemove( getSuccessorList(), toRemove );

		for (Iterator j = toRemove.iterator(); j.hasNext();)
		{
			DependencyService.getInstance().remove( (Dependency)j.next(), _eventSource, _undo ); //fix
		}

		// for all snapshots
		if (_deep)
		{
			TaskSnapshot snapshot;

			for (int i = 0; i < Settings.numBaselines(); i++)
			{
				Integer snapshotId = new Integer(i);
				snapshot = (TaskSnapshot)getSnapshot( snapshotId );

				if (snapshot != null)
				{
					// send events only for current snapshot
					Object useEventSource = (getCurrentSnapshot() == snapshot)
						? _eventSource
						: null;

					LinkedList toRemove2 = new LinkedList(); //fix
					AssignmentService.getInstance().remove( snapshot.getAssignments(), toRemove2 );
					AssignmentService.getInstance().remove( toRemove2, useEventSource, false );

					if (snapshot != getCurrentSnapshot())
					{
						project.fireBaselineChanged( _eventSource, this, snapshotId, false );
					}
				}
			}
		}
	}

	private void clearDateConstraints()
	{
		windowEarlyStart = 0;
		windowLateStart = 0;
		windowEarlyFinish = 0;
		windowLateFinish = 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clearDuration()
	{
		setRawDuration( 0 );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clearSnapshot( 
		final Object _snapshotId )
	{
		snapshots.clearSnapshot( _snapshotId );

		// for redraw purpooses, not for recalc.
		markTaskAsNeedingRecalculation// for redraw purpooses, not for recalc.
		();
		touchBaseline( _snapshotId );
		setDirty( true );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Task clone()
	{
		return new Task(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataSnapshot cloneSnapshot( 
		final DataSnapshot _snapshot )
	{
		return (DataSnapshot)((TaskSnapshot)_snapshot).clone();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void cloneTo( 
		final org.openproj.domain.task.Task _task )
	{
		if (_task instanceof Task == false)
		{
			return;
		}

		Task modelTask = (Task)_task;
		modelTask.estimated = estimated;
		modelTask.priority = priority;
		modelTask.version = version;
		modelTask.workCalendar = workCalendar;

		//		super.cloneTo( _task );
		modelTask.project = project;
		modelTask.owningProject = owningProject;

		modelTask.debugDependencyOrder = debugDependencyOrder;
		modelTask.markTaskAsMilestone = markTaskAsMilestone;
		modelTask.external = external;
		modelTask.projectId = projectId;
		modelTask.physicalPercentComplete = physicalPercentComplete;

		modelTask.windowEarlyStart = windowEarlyStart;
		modelTask.windowEarlyFinish = windowEarlyFinish;
		modelTask.windowLateStart = windowLateStart;
		modelTask.windowLateFinish = windowLateFinish;

		modelTask.actualStart = actualStart;

		modelTask.levelingDelay = levelingDelay;
		modelTask.calculationStateCount = calculationStateCount;
		modelTask.markerStatus = markerStatus;
		modelTask.earnedValueMethod = earnedValueMethod;

		modelTask.constraintType = constraintType;
		modelTask.deadline = deadline;
		modelTask.expenseType = expenseType;
		modelTask.inSubproject = inSubproject;
		modelTask.lastSavedStart = lastSavedStart;
		modelTask.lastSavedFinish = lastSavedFinish;
		modelTask.dirty = dirty;

		modelTask.delegatedTo = delegatedTo;

		//		_cloneTo( _task );
		modelTask.hasKey = new HasKeyImpl(isLocal() && Environment.getStandAlone(), _task);
		modelTask.setName( getName() );
		modelTask.setRawDuration( getRawDuration() );

		modelTask.myEarlySchedule = (TaskSchedule)myEarlySchedule.cloneWithTask( _task );
		modelTask.myLateSchedule = (TaskSchedule)myLateSchedule.cloneWithTask( _task );
		modelTask.customFields = (CustomFieldsImpl)customFields.clone();
		modelTask.snapshots = (Snapshottable)((SnapshotContainer)snapshots).cloneWithTask( _task );
		modelTask.myCurrentSchedule = ((TaskSnapshot)_task.getCurrentSnapshot()).getCurrentSchedule();
		modelTask.notes = notes;
		modelTask.wbs = wbs;
		modelTask.wbsChildrenNodes = null;
		modelTask.wbsParentTask = null;

		modelTask.dependencies = new HasDependenciesImpl(_task);

		modelTask.myCurrentSchedule.copyDatesAfterClone( myCurrentSchedule );
		modelTask.setDirty( true );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void connectToProject()
	{
		project.connectTask( this );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void consumeIntervals( 
		final IntervalConsumer _consumer )
	{
		if ((isWbsParent() == true) || (isSubproject() == true))
		{
			//TODO this shouldn't be needed since default assignment should be ok.  See why
			_consumer.consumeInterval( new ScheduleInterval(getStart(), getEnd()) );

			return;
		}

		myBarClosureInstance.initialize( _consumer, this );
		forEachWorkingInterval( myBarClosureInstance, true, getEffectiveWorkCalendar() );

		// Below is a hack to prevent hanging on void node promotion
		if (myBarClosureInstance.getCount() == 0)
		{
			// if no bars drawn
			_consumer.consumeInterval( new ScheduleInterval(getStart(), getEnd()) );
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void copyScheduleTo( 
		final org.openproj.domain.task.Task _to )
	{
		_to.getCurrentSchedule().setStart( getCurrentSchedule().getStart() );
		_to.getCurrentSchedule().setFinish( getCurrentSchedule().getFinish() );
		_to.setRawDuration( getDuration() );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double cost( 
		final long _start,
		final long _end )
	{
		if (isParentWithoutAssignments())
		{
			return 0.0D;
		}

		return ((TaskSnapshot)getCurrentSnapshot()).cost( _start, _end );
	}

	private TaskSnapshot createSnapshot( 
		final Object _snapshotId )
	{
		TaskSnapshot newOne = new TaskSnapshot();
		setSnapshot( _snapshotId, newOne );

		return newOne;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean dependsOn( 
		final HasDependencies _other )
	{
		org.openproj.domain.task.Task task = (org.openproj.domain.task.Task)_other;

		if (isAncestorOrDescendent( task ))
		{
			return true;
		}

		HashSet set = new HashSet();

		return dependsOn( (org.openproj.domain.task.Task)_other, this, set, "" );
	}

	/**
	 * This rather complex function determines whether one task depends on another. Because of the rules for parent tasks, the
	 * algorithm is rather complicated. Basically: - A (parent) task depends on its children's predecessors, as these are
	 * potentially equivalent - A task depends on its parents predecessors - in the case of a link to its parent task, the links
	 * applies to this task too - A task depends on its parent, of course - A task depends on its predecessors (obviously) - A
	 * task depends on its predecessors children
	 *
	 * @param other Task to compare to
	 * @param set   - A set used to prevent treating same task twice
	 *
	 * @return true if linking to other would cause a circular link
	 */
	private boolean dependsOn( 
		final org.openproj.domain.task.Task _other,
		final org.openproj.domain.task.Task _me,
		final HashSet _set,
		String _taskNames )
	{
		// To avoid infinite loops which can occur under certain circumstances, use a set to prevent looking up twice
		if (_set.contains( this ))
		{
			return false;
		}

		_set.add( this );

		// Here is the primary exit point.  We have arrived back at the other node, so it is circular
		if (this == _other)
		{
			if (_taskNames != null)
			{
				System.out.println( "Circular: \n" + _taskNames );
			}

			return true;
		}

		if (_taskNames != null)
		{
			_taskNames += (getId() + ": " + getName() + '\n');
		}

		Task predecessor;
		Dependency dep;

		Collection children;
		Iterator i;

		i = getPredecessorList().iterator();

		while (i.hasNext() == true)
		{
			dep = (Dependency)i.next();

			if (dep.isDisabled())
			{
				continue;
			}

			predecessor = (Task)dep.getPredecessor(); // I depend on my predecessors

			if (predecessor.dependsOn( _other, _me, _set, (_taskNames == null)
				? null
				: (_taskNames + "Pred-") ) == true)
			{
				return true;
			}
		}

		//parent
		if (_other.getWbsParentTask() != wbsParentTask)
		{ // only do parents if they are different
		  //			if (!this.isAncestorOrDescendent(other))

			if (wbsParentTask != null)
			{
				//			if ( !other.wbsDescendentOf(parent))
				if (((Task)wbsParentTask).dependsOn( _other, _me, _set, (_taskNames == null)
					? null
					: (_taskNames + "Parent- ") ) == true)
				{
					return true;
				}
			}
		}

		children = getWbsChildrenNodes();

		org.openproj.domain.task.Task child;
		Object current;
		Iterator j;

		// I depend on my children's preds
		if (children != null)
		{
			i = children.iterator();

			while (i.hasNext() == true)
			{
				current = ((Node)i.next()).getValue();

				if (current instanceof org.openproj.domain.task.Task == false)
				{
					continue;
				}

				child = (org.openproj.domain.task.Task)current;

				j = child.getPredecessorList().iterator();

				while (j.hasNext())
				{
					dep = (Dependency)j.next();

					if (dep.isDisabled())
					{
						continue;
					}

					predecessor = (Task)dep.getPredecessor(); // I depend on my predecessors

					if (predecessor.wbsDescendentOf( this ))
					{
						// skip if already belongs to parent thru an ancestry relation
						continue;
					}

					if (predecessor.dependsOn( _other, _me, _set,
								(_taskNames == null)
								? null
									: (_taskNames + "pred-child " + child.getId()) ))
					{
						return true;
					}
				}
			}
		}

		return false;
	}

//	@Override
//	public boolean delegates( 
//		Field field )
//	{
//		if (delegatedFields == null)
//		{
//			return false;
//		}
//
//		return delegatedFields.containsKey( field );
//	}

	/* The serialization version must be private. This lets subclasses call this code */
	protected void doWriteObject( 
		final ObjectOutputStream _s )
		throws IOException
	{
		/*
		 * Version 3 implementation
		 */
		_s.defaultWriteObject();
		hasKey.serialize( _s );
		customFields.serialize( _s );

		int sCount = 0;

		for (int i = 0; i < Settings.numBaselines(); i++)
		{
			TaskSnapshot snapshot = (TaskSnapshot)getSnapshot( new Integer(i) );

			if (snapshot != null)
			{
				sCount++;
			}
		}

		_s.writeInt( sCount );

		for (int i = 0; i < Settings.numBaselines(); i++)
		{
			TaskSnapshot snapshot = (TaskSnapshot)getSnapshot( new Integer(i) );

			if (snapshot == null)
			{
				continue;
			}

			_s.writeInt( i );
			snapshot.serialize( _s );
		}
	}

	public void dumpTest()
	{
		System.out.println( "Task: " + getName() + "\nstart " +
			(DateTime.dayFloor( getStart() ) == DateTime.dayFloor( getCustomDate( 0 ) )) + " \nend " +
			(DateTime.dayFloor( getEnd() ) == DateTime.dayFloor( getCustomDate( 1 ) )) + " \nend prj " +
			DateTime.medString( DateTime.dayFloor( getEnd() ) ) + " mar " +
			DateTime.medString( DateTime.dayFloor( getCustomDate( 1 ) ) ) + " \ndur " +
			(getDurationMillis() == Duration.millis( getCustomDuration( 0 ) )) + " \noutline " + getOutlineString() + " " +
			getCustomText( 0 ) );
		System.out.println( "start---end " + DateTime.medString( getStart() ) + "  " + DateTime.medString( getEnd() ) );
		System.out.println( "dura proj --- mar " + DurationFormat.format( getDurationMillis() ) + "  " +
			DurationFormat.format( getCustomDuration( 0 ) ) );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean fieldHideActualCost( 
		final FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean fieldHideActualFixedCost( 
		final FieldContext _fieldContext )
	{
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean fieldHideActualWork( 
		final FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean fieldHideAcwp( 
		final FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean fieldHideBac( 
		final FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean fieldHideBaselineCost( 
		final int _numBaseline,
		final FieldContext _fieldContext )
	{
		return isBaselineFieldHidden( _numBaseline, _fieldContext );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean fieldHideBaselineWork( 
		final int _numBaseline,
		final FieldContext _fieldContext )
	{
		return isBaselineFieldHidden( _numBaseline, _fieldContext );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean fieldHideBcwp( 
		final FieldContext _fieldContext )
	{
		return isEarnedValueFieldHidden( _fieldContext );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean fieldHideBcws( 
		final FieldContext _fieldContext )
	{
		return isEarnedValueFieldHidden( _fieldContext );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean fieldHideCost( 
		final FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean fieldHideCpi( 
		final FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean fieldHideCv( 
		final FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean fieldHideCvPercent( 
		final FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean fieldHideEac( 
		final FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean fieldHideSpi( 
		final FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean fieldHideSubprojectFile( 
		final FieldContext _fieldContext )
	{
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean fieldHideSubprojectReadOnly( 
		final FieldContext _fieldContext )
	{
		return true;
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public boolean fieldHideSv( 
		final FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean fieldHideSvPercent( 
		final FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean fieldHideTcpi( 
		final FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean fieldHideTimesheetClosed( 
		final FieldContext _fieldContext )
	{
		return !project.isTimesheetClosed();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean fieldHideVac( 
		final FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean fieldHideWork( 
		final FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Assignment findAssignment( 
		final Resource _resource )
	{
		return ((TaskSnapshot)getCurrentSnapshot()).findAssignment( _resource );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Assignment findAssignment( 
		final org.openproj.domain.task.Task _task )
	{
		return ((TaskSnapshot)getCurrentSnapshot()).findAssignment( _task );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double fixedCost( 
		long _start,
		long _end )
	{
		long taskStart = getStart();
		long taskEnd = getEnd();
		double fixed = 0.0;
		double fixedCost = getFixedCost();

		if (getFixedCostAccrual() == Accrual.START)
		{
			if ((taskStart >= _start) && (taskStart <= _end)) // if task starts in this range
			{
				fixed = fixedCost;
			}
		}
		else if (getFixedCostAccrual() == Accrual.PRORATED)
		{
			// find overlapping actual time
			_start = Math.max( _start, taskStart );
			_end = Math.min( _end, taskEnd );

			if (_start < _end)
			{
				// if valid range
				long overlappingDuration = getEffectiveWorkCalendar().compare( _end, _start, false );
				double fraction = ((double)overlappingDuration) / getDurationMillis();
				fixed = fixedCost * fraction;
			}
		}
		else
		{
			// END accrual by default
			if ((taskEnd >= _start) && (taskEnd <= _end)) // if task ends in this range
			{
				fixed = fixedCost;
			}
		}

		if (project.hasCurrencyRateTable() == true)
		{
			// just use the end date - hard to do prorated
			CurrencyRate rate = (CurrencyRate)project.getCurrencyRateTable().findActive( getEnd() );
			fixed *= rate.getRate();
		}

		return fixed;
	}

	public static Closure forAllAssignments( 
		final Closure _visitor )
	{
		return new ObjectVisitor(_visitor)
			{
				@Override
				protected Object getObject( 
					final Object _arg0 )
				{
					return ((TaskSnapshot)((Task)_arg0).getCurrentSnapshot()).getHasAssignments();
				}
			};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void forEachWorkingInterval( 
		final Closure<HasStartAndEnd> _visitor,
		final boolean _mergeWorking,
		final WorkCalendar _workCalendarToUse )
	{
		((TaskSnapshot)getCurrentSnapshot()).forEachWorkingInterval( _visitor, _mergeWorking, _workCalendarToUse );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void forSnapshots( 
		final Closure _c )
	{
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void forSnapshots( 
		final Closure _c,
		final int _s )
	{
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void forSnapshotsAssignments( 
		final Closure _c,
		final boolean _onlyCurrent )
	{
		if (_onlyCurrent)
		{
			forSnapshotsAssignments( _c, -1 );
		}
		else
		{
			for (int s = 0; s < Settings.numBaselines(); s++)
			{
				forSnapshotsAssignments( _c, s );
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void forSnapshotsAssignments( 
		final Closure _c,
		final int _s )
	{
		TaskSnapshot snapshot;

		if (_s == -1)
		{
			snapshot = (TaskSnapshot)getCurrentSnapshot();
		}
		else
		{
			snapshot = (TaskSnapshot)getSnapshot( new Integer(_s) );
		}

		if (snapshot == null)
		{
			return;
		}

		AssociationList snapshotAssignments = snapshot.getHasAssignments().getAssignments();

		if (snapshotAssignments.size() > 0)
		{
			for (Iterator j = snapshotAssignments.iterator(); j.hasNext();)
			{
				Assignment assignment = (Assignment)j.next();
				_c.execute( assignment );
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getActualCost( 
		final FieldContext _fieldContext )
	{
		return getActualFixedCost( _fieldContext ) +
		actualCost( FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getActualDuration()
	{
		long duration = getDuration();
		long result = Math.round( getPercentComplete() * Duration.millis( duration ) );

		return Duration.useTimeUnitOfInNone( result, duration );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getActualFinish()
	{
		if (isZeroDuration())
		{
			return getActualStart();
		}

		// nij 2009-09-01 - Check to make sure there is an actual start.  If there isn't then
		// there can't be an actual finish.
		if ((getPercentComplete() == 1.0) && (getActualStart() > 0))
		{
			return getEnd();
		}

		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getActualFixedCost( 
		final FieldContext _fieldContext )
	{
		return fixedCost( FieldContext.start( _fieldContext ),
			Math.min( getStop(), FieldContext // only up to completion
		.end( _fieldContext ) ) );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getActualStart()
	{
		return actualStart;

		//		if (myCurrentSchedule.getPercentComplete() == 0.0D && getPercentComplete() == 0)
		//			return 0;
		//		return getStart();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getActualWork( 
		final FieldContext _fieldContext )
	{
		return actualWork( FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getAcwp( 
		final FieldContext _fieldContext )
	{
		return acwp( FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AssociationList getAssignments()
	{
		return ((TaskSnapshot)getCurrentSnapshot()).getAssignments();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getBac( 
		final FieldContext _fieldContext )
	{
		return bac( FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Assignment getBaselineAssignment( 
		final Resource _resource )
	{
		TaskSnapshot baseline = getBaselineSnapshot();

		if (baseline == null)
		{
			return null;
		}

		return getBaselineSnapshot().findAssignment( _resource );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Assignment getBaselineAssignment( 
		final Resource _resource,
		final Object _snapshot,
		final boolean _createIfDoesntExist )
	{
		TaskSnapshot baselineSnapshot = (TaskSnapshot)getSnapshot( _snapshot );

		if (baselineSnapshot == null)
		{
			if (_createIfDoesntExist)
			{
				baselineSnapshot = createSnapshot( _snapshot );
			}
			else
			{
				return null;
			}
		}

		Assignment assignment = baselineSnapshot.findAssignment( _resource );

		if ((assignment == null) && _createIfDoesntExist)
		{
			assignment = Assignment.getInstance( this, _resource, 1.0, 0 );
			baselineSnapshot.addAssignment( assignment );

			TaskSchedule baselineSchedule = new TaskSchedule(this, TaskSchedule.CURRENT);

			//baselineSnapshot.set
			baselineSnapshot.setCurrentSchedule( baselineSchedule );
			assignment.setTaskSchedule( baselineSchedule );
			assignment.convertToBaselineAssignment( true );
		}

		return assignment;
	}

	public double getBaselineCost( 
		final int _numBaseline,
		final long _start,
		final long _end )
	{
		TaskSnapshot snapshot = ((TaskSnapshot)getSnapshot( new Integer(_numBaseline) ));

		if (snapshot == null)
		{
			return 0;
		}

		return snapshot.cost( _start, _end );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getBaselineCost( 
		final int _numBaseline,
		final FieldContext _fieldContext )
	{
		TaskSnapshot snapshot = (TaskSnapshot)getSnapshot( new Integer(_numBaseline) );

		if (snapshot == null)
		{
			if ((_fieldContext != null) && _fieldContext.isHideNullValues())
			{
				return ClassUtils.NULL_DOUBLE;
			}

			return 0.0D;
		}

		double cost = snapshot.cost( FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
		double fixedCost = getBaselineFixedCost( _numBaseline, _fieldContext );

		//System.out.println(getName() + " baseline cost " + numBaseline + "    " + (cost+fixedCost));
		return cost + fixedCost;
	}

	public double getBaselineCost()
	{
		return getBaselineCost( 0, null );
	}

	public double getBaselineCost( 
		final int _number )
	{
		return getBaselineCost( _number, null );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getBaselineDuration( 
		final int _numBaseline,
		final FieldContext _fieldContext )
	{
		TaskSnapshot snapshot = ((TaskSnapshot)getSnapshot( new Integer(_numBaseline) ));

		if (snapshot == null)
		{
			if ((_fieldContext != null) && _fieldContext.isHideNullValues())
			{
				return ClassUtils.NULL_LONG;
			}

			return 0L;
		}

		return snapshot.getCurrentSchedule().getRawDuration();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getBaselineFinish()
	{
		TaskSnapshot baseline = getBaselineSnapshot();

		if (baseline == null)
		{
			return getEnd();
		}

		return baseline.getCurrentSchedule().getFinish();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getBaselineFinish( 
		final int _numBaseline )
	{
		TaskSnapshot snapshot = ((TaskSnapshot)getSnapshot( new Integer(_numBaseline) ));

		if (snapshot == null)
		{
			return 0;
		}

		return snapshot.getCurrentSchedule().getEnd();
	}

	public long getBaselineFinishOrZero()
	{
		TaskSnapshot baseline = getBaselineSnapshot();

		if (baseline == null)
		{
			return 0L;
		}

		return getBaselineFinish();
	}

	protected double getBaselineFixedCost( 
		final int _numBaseline,
		final FieldContext _fieldContext )
	{
		TaskSnapshot snapshot = (TaskSnapshot)getSnapshot( new Integer(_numBaseline) );

		if (snapshot == null)
		{
			return 0.0D;
		}

		if (!FieldContext.hasInterval( _fieldContext ))
		{
			return snapshot.getFixedCost();
		}
		else
		{
			return 0; // this is not reallycorrect - should have a value, but not worried about it for now. 3/11/09 hk
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TaskSnapshot getBaselineSnapshot()
	{
		return (TaskSnapshot)getSnapshot( CalculationOption.getInstance().getEarnedValueBaselineId() );
	}

	public long getBaselineStart()
	{
		TaskSnapshot baseline = getBaselineSnapshot();

		if (baseline == null)
		{
			return getStart();
		}

		return baseline.getCurrentSchedule().getStart();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getBaselineStart( 
		final int _numBaseline )
	{
		TaskSnapshot snapshot = ((TaskSnapshot)getSnapshot( new Integer(_numBaseline) ));

		if (snapshot == null)
		{
			return 0;
		}

		return snapshot.getCurrentSchedule().getStart();
	}

	public long getBaselineStartOrZero()
	{
		TaskSnapshot baseline = getBaselineSnapshot();

		if (baseline == null)
		{
			return 0L;
		}

		return getBaselineStart();
	}

	public double getBaselineWork( 
		final int _numBaseline,
		final long _start,
		final long _end )
	{
		TaskSnapshot snapshot = ((TaskSnapshot)getSnapshot( new Integer(_numBaseline) ));

		if (snapshot == null)
		{
			return 0;
		}

		return snapshot.work( _start, _end );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getBaselineWork( 
		final int _numBaseline,
		final FieldContext _fieldContext )
	{
		TaskSnapshot snapshot = (TaskSnapshot)getSnapshot( new Integer(_numBaseline) );

		if (snapshot == null)
		{
			if ((_fieldContext != null) && _fieldContext.isHideNullValues())
			{
				return ClassUtils.NULL_LONG;
			}

			return 0L;
		}

		return ((TaskSnapshot)getSnapshot( new Integer(_numBaseline) )).work( FieldContext.start( _fieldContext ),
			FieldContext.end( _fieldContext ) );
	}

	public double getBaselineWork()
	{
		return getBaselineWork( 0, null );
	}

	public double getBaselineWork( 
		final int _number )
	{
		return getBaselineWork( _number, null );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getBcwp( 
		final FieldContext _fieldContext )
	{
		return bcwp( FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getBcws( 
		final FieldContext _fieldContext )
	{
		return bcws( FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	public ImageLink getBudgetStatusIndicator()
	{
		return EarnedValueCalculator.getInstance().getBudgetStatusIndicator( getCpi( null ) );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int getCalculationStateCount()
	{
		return calculationStateCount;
	}

	public boolean getCapEx()
	{
		if (getExtraFields() == null)
		{
			return false;
		}

		Field f = Configuration.getFieldFromId( "Field.capEx" );

		if (f == null)
		{
			return false;
		}

		Object result = getExtraFields().get( f.getAlternateId() );

		return (result == null)
		? false
		: (Boolean)result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getCompletedThrough()
	{
		final long start = getStart();

		if (start == 0)
		{
			return 0;
		}

		final long actualDuration = DateTime.closestDate( getDurationMillis() * getPercentComplete() );

		return getEffectiveWorkCalendar().add( start, actualDuration, true );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getConstraintDate()
	{
		if (constraintType == ConstraintType.FNET)
		{
			return windowEarlyFinish;
		}
		else if (constraintType == ConstraintType.FNLT)
		{
			return windowLateFinish;
		}
		else if (constraintType == ConstraintType.SNET)
		{
			return windowEarlyStart;
		}
		else if (constraintType == ConstraintType.SNLT)
		{
			return windowLateStart;
		}
		else if (constraintType == ConstraintType.MSO)
		{
			return windowEarlyStart;
		}
		else if (constraintType == ConstraintType.MFO)
		{
			return windowEarlyFinish;
		}
		else
		{
			return 0;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getConstraintType()
	{
//		if (constraintType != ConstraintType.ASAP && constraintType != ConstraintType.ALAP) {
//			if (getConstraintDate() < 1000000) {
//				System.out.println("fixing bad constraint date: " + this + "  " + getId() );
//				clearDateConstraints();
//				constraintType = ConstraintType.ASAP;
//			}
//		}
//
//
//
		return constraintType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getCost( 
		final FieldContext _fieldContext )
	{
		return getFixedCost( _fieldContext ) + cost( FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	// some functions useful for API
	public double getCost()
	{
		return getCost( null );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getCpi( 
		final FieldContext _fieldContext )
	{
		return EarnedValueCalculator.getInstance()
									.cpi( this, FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Date getCreated()
	{
		return hasKey.getCreated();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getCsi( 
		final FieldContext _fieldContext )
	{
		return EarnedValueCalculator.getInstance()
									.csi( this, FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TaskSchedule getCurrentSchedule()
	{
		return myCurrentSchedule;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataSnapshot getCurrentSnapshot()
	{
		return snapshots.getCurrentSnapshot();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getCustomCost( 
		final int _index )
	{
		return customFields.getCustomCost( _index );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getCustomDate( 
		final int _index )
	{
		return customFields.getCustomDate( _index );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getCustomDuration( 
		final int _index )
	{
		return customFields.getCustomDuration( _index );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CustomFields getCustomFields()
	{
		return customFields;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getCustomFinish( 
		final int _index )
	{
		return customFields.getCustomFinish( _index );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean getCustomFlag( 
		final int _index )
	{
		return customFields.getCustomFlag( _index );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getCustomNumber( 
		final int _index )
	{
		return customFields.getCustomNumber( _index );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getCustomStart( 
		final int _index )
	{
		return customFields.getCustomStart( _index );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getCustomText( 
		final int _index )
	{
		return customFields.getCustomText( _index );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getCv( 
		final FieldContext _fieldContext )
	{
		return EarnedValueCalculator.getInstance().cv( this, FieldContext.start( _fieldContext ),
			FieldContext.end( _fieldContext ) );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getCvPercent( 
		final FieldContext _fieldContext )
	{
		return EarnedValueCalculator.getInstance()
									.cvPercent( this, FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getDeadline()
	{
		return deadline;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int getDebugDependencyOrder()
	{
		return debugDependencyOrder;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Resource getDelegatedTo()
	{
		if (delegatedTo == null)
		{
			if (wbsParentTask != null)
			{
				return wbsParentTask.getDelegatedTo();
			}
		}

		return delegatedTo;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDelegatedToName()
	{
		Resource del = getDelegatedTo();

		return (del == null)
		? ""
		: del.getName();
	}

//	@Override
//	public Object getDelegatedFieldValue( 
//		Field field )
//	{
//		return delegatedFields.get( field );
//	}

//	public HashMap<Field,Object> getDelegatedFields()
//	{
//		return delegatedFields;
//	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AssociationList getDependencyList( 
		final boolean _pred )
	{
		return dependencies.getDependencyList( _pred );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getDependencyStart()
	{
		return myCurrentSchedule.getRemainingDependencyDate();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Document getDocument()
	{
		return project;
	}

	/**
	 * @return
	 */
	@Override
	public long getDuration()
	{
		long duration;

		if (isWbsParent() || isExternal() || isSubproject() || isPinned())
		{
			long raw = getRawDuration();

			if (raw >= 0)
			{
				duration = Duration.millis( raw );
			}
			else
			{
				project.getTasks().addRepaired( this );
				ErrorLogger.logOnce( "raw parent", "repaired bad raw duration" + this, null );
				duration = 0;
			}
		}
		else
		{
			AssociationList assignments = getAssignments();

			if (assignments.size() == 1)
			{
				duration = ((Assignment)assignments.getFirst()).getDurationMillis();
			}
			else
			{
				Iterator i = assignments.iterator();
				long end = 0;

				// get the latest ending assignment
				while (i.hasNext() == true)
				{
					end = Math.max( end, ((Assignment)i.next()).getEnd() );
				}

				// duration is calendar time between assignment end and task start
				duration = getEffectiveWorkCalendar().compare( end, getStart(), false );
			}
		}

		duration = Duration.setAsEstimated( duration, estimated );

		return duration;

		//		return calcActiveAssignmentDuration(getEffectiveWorkCalendar());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getDurationMillis()
	{
		return Duration.millis( getDuration() );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getEac( 
		final FieldContext _fieldContext )
	{
		return EarnedValueCalculator.getInstance()
									.eac( this, FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getEarliestAssignmentStart()
	{
		return ((TaskSnapshot)getCurrentSnapshot()).getEarliestAssignmentStart();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final long getEarliestStop()
	{
		long stop = Long.MAX_VALUE;
		Schedule s;
		Object nodeImpl;

		if (isWbsParent())
		{
			Iterator i = wbsChildrenNodes.iterator();

			while (i.hasNext() == true)
			{
				Object x = i.next();

				if (!(x instanceof Node))
				{
					continue;
				}

				nodeImpl = ((Node)x).getValue();

				if (!(nodeImpl instanceof Schedule))
				{
					continue;
				}

				s = (Schedule)nodeImpl;
				stop = Math.min( stop, s.getEarliestStop() );
			}
		}
		else
		{
			Iterator i = getAssignments().iterator();

			while (i.hasNext() == true)
			{
				Assignment ass = (Assignment)i.next();
				stop = Math.min( stop, ass.getEarliestStop() );
			}
		}

		return stop;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getEarlyFinish()
	{
		return myEarlySchedule.getFinish();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final TaskSchedule getEarlySchedule()
	{
		return myEarlySchedule;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getEarlyStart()
	{
		return myEarlySchedule.getStart();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getEarnedValueMethod()
	{
		return earnedValueMethod;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExpenseType getEffectiveExpenseType()
	{
		ExpenseType result = expenseType;

		if (result == ExpenseType.NONE)
		{
			if (wbsParentTask != null)
			{
				result = wbsParentTask.getEffectiveExpenseType();
			}
		}

		if (result == ExpenseType.NONE)
		{
			result = owningProject.getEffectiveExpenseType();
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public WorkCalendar getEffectiveWorkCalendar()
	{
		if (workCalendar == null)
		{
			if (project == null)
			{
				System.out.println( "------No project in getting calendar for task " + getUniqueId() + " " + getName() );

				return CalendarService.getInstance().getDefaultInstance();
			}

			return project.getEffectiveWorkCalendar();
		}

		return workCalendar;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getElapsedDuration()
	{
		return Math.round( getEffectiveWorkCalendar().compare( getEnd(), getStart(), true ) * CalendarOption.getInstance()
																											.getFractionOfDayThatIsWorking() );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Project getEnclosingProject()
	{
		if (isSubproject() == true)
		{
			return ((SubProj)this).getSubproject();
		}

		if (wbsParentTask == null)
		{
			return project;
		}

		return wbsParentTask.getEnclosingProject();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SubProj getEnclosingSubproject()
	{
		if (wbsParentTask == null)
		{
			return null;
		}

		if (wbsParentTask.isSubproject() == true)
		{
			return (SubProj)wbsParentTask;
		}

		return wbsParentTask.getEnclosingSubproject();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Node getEnclosingSubprojectNode()
	{
		SubProj s = getEnclosingSubproject();

		if (s == null)
		{
			return null;
		}

		return ((org.openproj.domain.task.Task)s).getProject().getTaskOutline().search( s );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getEnd()
	{
		return myCurrentSchedule.getFinish();
	}

	/**
	 *
	 * @return
	 */
	public static Field getEndField()
	{
		return Configuration.getFieldFromId( "Field.finish" );
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public ExpenseType getExpenseType()
	{
		return expenseType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Map getExtraFields()
	{
		if (extraFields == null)
		{
			extraFields = new HashMapWithDirtyFlags();
		}

		return extraFields;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getFinishOffset()
	{
		return EarnedValueCalculator.getInstance().getFinishOffset( this );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final long getFinishSlack()
	{
		return getEffectiveWorkCalendar().compare( getLateFinish(), getEarlyFinish(), false ); // note that this is same as total float
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getFixedCost()
	{
		double fixedCost = ((TaskSnapshot)getCurrentSnapshot()).getFixedCost();

		return fixedCost;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getFixedCost( 
		final FieldContext _fieldContext )
	{
		if (!FieldContext.hasInterval( _fieldContext ))
		{
			double fixed = ((TaskSnapshot)getCurrentSnapshot()).getFixedCost();

			if (project.getCurrencyRateTable() != null)
			{
				double rate = project.getCurrencyRateTable()
									 .calculateWeightedRateOverPeriod( getStart(), getEnd(), getEffectiveWorkCalendar() );
				fixed *= rate;
			}

			return fixed;
		}

		return fixedCost( FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int getFixedCostAccrual()
	{
		return ((TaskSnapshot)getCurrentSnapshot()).getFixedCostAccrual();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getFreeSlack()
	{
		long least = getTotalSlack(); // free slack is at most the total slack
		Dependency dependency;

		for (Iterator i = getSuccessorList().iterator(); i.hasNext();)
		{
			dependency = (Dependency)i.next();
			least = Math.min( least, calcFreeSlack( dependency ) );
		}

		return least;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public HasCalendar getHasCalendar()
	{
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getId()
	{
		return hasKey.getId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public HasIndicators getIndicators()
	{
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getInvestmentMilestone()
	{
		Field f = Configuration.getFieldFromId( "Field.investmentMilestone" );

		return (String)getExtraFields().get( f.getAlternateId() );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final long getLastSavedFinish()
	{
		return lastSavedFinish;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final long getLastSavedParentId()
	{
		return lastSavedParentId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final long getLastSavedPosistion()
	{
		return lastSavedPosistion;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final long getLastSavedStart()
	{
		return lastSavedStart;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getLastTimesheetUpdate()
	{
		return TimesheetHelper.getLastTimesheetUpdate( getAssignments() );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getLateFinish()
	{
		return myLateSchedule.getFinish();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final TaskSchedule getLateSchedule()
	{
		return myLateSchedule;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getLateStart()
	{
		return myLateSchedule.getStart();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final long getLevelingDelay()
	{
		return levelingDelay;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Document getMasterDocument()
	{
		if (project.getSchedulingAlgorithm() == null)
		{
			return null;
		}

		return project.getSchedulingAlgorithm().getMasterDocument();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getMostLoadedAssignmentUnits()
	{
		double result = 0;
		Iterator i = getAssignments().iterator();

		while (i.hasNext() == true)
		{
			result = Math.max( result, ((Assignment)i.next()).getLaborUnits() );
		}

		return result;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public String getName( 
		final FieldContext _context )
	{
		return hasKey.getName( _context );
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public String getName()
	{
		return hasKey.getName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getNotes()
	{
		return notes;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getOutlineLevel()
	{
		return getOutlineLevel( OutlineCollection.DEFAULT_OUTLINE );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getOutlineLevel( 
		final int _outlineNumber )
	{
		NodeModel model = project.getTaskOutline( _outlineNumber );

		if (model == null)
		{
			return 0;
		}

		Node node = model.getParent( model.search( this ) );

		return model.getHierarchy().getLevel( node );
	}

	public String getOutlineString()
	{
		NodeModel nodeModel = project.getTaskOutline();
		Node node = nodeModel.search( this );
		String result = NodeModelUtil.getOutlineString( nodeModel, node, "" );

		if (result.length() < 2) // The "root" task has no outline string.
		{
			return null;
		}

		// remove first 2 chars of root;
		return result.substring( 2 );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Project getOwningProject()
	{
		return owningProject;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getParentId( 
		final int _outlineNumber )
	{
		NodeModel model = project.getTaskOutline( _outlineNumber );

		if (model == null)
		{
			return 0;
		}

		Node node = model.getParent( model.search( this ) );
		Object impl = node.getValue();

		if ((impl != null) && impl instanceof HasKey)
		{
			return ((HasKey)impl).getId();
		}

		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getPercentComplete()
	{
		if (isZeroDuration())
		{ // special case for completion on milestones

			int count = 0;
			double pc = 0;
			Assignment ass;
			Iterator i = getAssignments().iterator();

			while (i.hasNext() == true)
			{
				ass = ((Assignment)i.next());
				pc += ass.getPercentComplete();
				count++;
			}

			if (count == 0) // shouldn't happen
			{
				return 0;
			}

			return pc / count;
		}
		else
		{
			boolean parent = isWbsParent();
			DivisionSummaryVisitor divisionClosure = ScheduleUtil.percentCompleteClosureInstance( parent );
			Project proj = (Project)((getMasterDocument() == null)
				? project
				: getMasterDocument());
			NodeModel nodeModel = proj.getTaskOutline();

			if (isWbsParent())
			{
				try
				{
					LeafWalker.recursivelyTreatBranch( nodeModel, this, divisionClosure );
				}
				catch (final NullPointerException _n)
				{
					ErrorLogger.logOnce( "getPercentComplete", "getPercentComplete() Task: " + this + " Project " + project, _n );

					return 0; // better this than crashing
				}
			}
			else
			{
				CollectionUtils.forAllDo( ((Task)this).getAssignments(), divisionClosure );
			}

			double val = divisionClosure.getValue();

			if (val >= COMPLETE_THRESHOLD) // adjust for rounding
			{
				val = 1.0D;
			}

			return val;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getPercentWorkComplete()
	{
		//		NodeModel nodeModel = project.getTaskOutline();
		//		Node node = nodeModel.search(this);
		//		Number value = (Number)
		//		Configuration.getFieldFromId("Field.work").getValue(node,nodeModel,null);
		//		if (value.doubleValue() == 0)
		//			return 0;
		//		Number actualValue = (Number)
		//		Configuration.getFieldFromId("Field.actualWork").getValue(node,nodeModel,null);
		//		return actualValue.doubleValue() / value.doubleValue();
		long work = calcSummedWork();

		if (work == 0)
		{
			return 0;
		}
		else
		{
			return ((double)calcSummedActualWork()) / work;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List getPersistedAssignments()
	{
		return persistedAssignments;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getPhysicalPercentComplete()
	{
		return physicalPercentComplete;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AssociationList getPredecessorList()
	{
		return dependencies.getPredecessorList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPredecessorOrder()
	{
		int[] pos = ((com.projity.pm.criticalpath.CriticalPath)project.getSchedulingAlgorithm()).findTaskPosition( this );
		String result = "" + pos[ 0 ];

		if (pos.length == 2)
		{
			result += (", " + pos[ 1 ]);
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPredecessors()
	{
		return AssociationListFormat.getInstance( DependencyFormat.getInstance( AssociationFormatParameters.getInstance( this,
					true, Configuration.getFieldFromId( "Field.id" ), false, true ) ) ).format( getPredecessorList() );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getPriority()
	{
		return priority;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Project getProject()
	{
		return project;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public WorkCalendar getProjectCalendar()
	{
		return project.getWorkCalendar();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final long getProjectId()
	{
		return projectId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getRawDuration()
	{
		return myCurrentSchedule.getRawDuration();
	}

	public AssociationList getRealAssignments()
	{
		if (hasRealAssignments())
		{
			return getAssignments();
		}
		else
		{
			return new AssociationList(); //empty list
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getRemainingCost( 
		final FieldContext _fieldContext )
	{
		return getCost( _fieldContext ) - getActualCost( _fieldContext );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getRemainingDuration()
	{
		long actualDuration = getActualDuration();
		long result = getDurationMillis() - Duration.millis( actualDuration );

		return Duration.useTimeUnitOfInNone( result, actualDuration );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getRemainingUnits()
	{
		if (getAssignments().isEmpty())
		{
			return 0;
		}

		long duration = Duration.millis( getRemainingDuration() );

		if (duration == 0.0)
		{
			return 1.0D; // degeneratate case
		}

		if (!isInitialized()) // the case when reading a file, don't boether to
							  // calculate
		{
			return 1.0;
		}

		long work = getRemainingWork( null );

		//		if (work == 0) // degenerate case with no work yet
		//			return 1.0;
		return ((double)work) / duration;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getRemainingWork( 
		final FieldContext _fieldContext )
	{
		return remainingWork( FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getRemainingWork()
	{
		return getRemainingWork( null );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getResourceGroup()
	{
		return AssociationListFormat.getInstance( AssignmentFormat.getInstance( AssociationFormatParameters.getInstance( this,
					true, Configuration.getFieldFromId( "Field.group" ), false, false ) ) ).format( getAssignments() );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getResourceInitials()
	{
		return AssociationListFormat.getInstance( AssignmentFormat.getInstance( AssociationFormatParameters.getInstance( this,
					true, Configuration.getFieldFromId( "Field.initials" ), false, false ) ) ).format( getAssignments() );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getResourceNames()
	{
		return AssociationListFormat.getInstance( AssignmentFormat.getInstance( AssociationFormatParameters.getInstance( this,
					true, Configuration.getFieldFromId( "Field.name" ), true, true ) ) ).format( getAssignments() );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getResourcePhonetics()
	{
		return AssociationListFormat.getInstance( AssignmentFormat.getInstance( AssociationFormatParameters.getInstance( this,
					true, Configuration.getFieldFromId( "Field.phonetics" ), false, true ) ) ).format( getAssignments() );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getResume()
	{
		long resume = Long.MAX_VALUE;
		Assignment assignment;
		Iterator i = getAssignments().iterator();

		while (i.hasNext() == true)
		{
			assignment = (Assignment)i.next();
			resume = Math.min( resume, assignment.getResume() );
		}

		return resume;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Project getRootProject()
	{
		if (wbsParentTask == null)
		{
			return project;
		}

		return wbsParentTask.getRootProject();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final TaskSchedule getSchedule( 
		final int _scheduleType )
	{
		if (_scheduleType == TaskSchedule.CURRENT)
		{
			return myCurrentSchedule;
		}
		else if (_scheduleType == TaskSchedule.EARLY)
		{
			return myEarlySchedule;
		}
		else
		{
			return myLateSchedule;
		}
	}

	public ImageLink getScheduleStatusIndicator()
	{
		return EarnedValueCalculator.getInstance().getBudgetStatusIndicator( getSpi( null ) );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SchedulingRule getSchedulingRule()
	{
		return SchedulingType.getSchedulingRuleInstance( getSchedulingType() );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getSchedulingType()
	{
		return ((TaskSnapshot)getCurrentSnapshot()).getSchedulingType();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataSnapshot getSnapshot( 
		final Object _snapshotId )
	{
		return snapshots.getSnapshot( _snapshotId );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getSpi( 
		final FieldContext _fieldContext )
	{
		return EarnedValueCalculator.getInstance()
									.spi( this, FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getSplitDuration()
	{
		long r = getResume();

		if (r == 0)
		{
			return 0;
		}

		return getEffectiveWorkCalendar().compare( r, getStop(), false );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getStart()
	{
		return myCurrentSchedule.getStart();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getStartOffset()
	{
		return EarnedValueCalculator.getInstance().getStartOffset( this );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final long getStartSlack()
	{
		return getEffectiveWorkCalendar().compare( getLateStart(), getEarlyStart(), false );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getStop()
	{
//		if (isWbsParent( )) {
//			long start = getStart();
//			if (start == 0)
//				return 0;
//			long actualDuration = DateTime.closestDate(getDurationMillis() * getPercentComplete());
//			return getEffectiveWorkCalendar().add(start,actualDuration,true);
//		}
		return getEarliestStop();

//&&&&&
//		long stop = 0;
//		Assignment assignment;
//		Iterator i = getAssignments().iterator();
//		while (i.hasNext() == true) {
//			assignment = (Assignment)i.next();
//			stop = Math.max(stop,assignment.getStop());
//		}
//		return stop;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSubprojectFile()
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AssociationList getSuccessorList()
	{
		return dependencies.getSuccessorList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSuccessors()
	{
		return AssociationListFormat.getInstance( DependencyFormat.getInstance( AssociationFormatParameters.getInstance( this,
					false, Configuration.getFieldFromId( "Field.id" ), false, true ) ) ).format( getSuccessorList() );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getSv( 
		final FieldContext _fieldContext )
	{
		return EarnedValueCalculator.getInstance().sv( this, FieldContext.start( _fieldContext ),
			FieldContext.end( _fieldContext ) );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getSvPercent( 
		final FieldContext _fieldContext )
	{
		return EarnedValueCalculator.getInstance()
									.svPercent( this, FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getTaskAndProjectName()
	{
		Project p = owningProject;

		if (p == null)
		{
			p = project;
		}

		return getName() + " (" + p.getName() + ")";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public WorkCalendar getTaskCalendar()
	{
		return getWorkCalendar();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getTcpi( 
		final FieldContext _fieldContext )
	{
		return EarnedValueCalculator.getInstance()
									.tcpi( this, FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getTimesheetStatus()
	{
		return TimesheetHelper.getTimesheetStatus( getAssignments() );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getTimesheetStatusName()
	{
		return TimesheetHelper.getTimesheetStatusName( getTimesheetStatus() );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final long getTotalSlack()
	{
		return getEffectiveWorkCalendar().compare( getLateFinish(), getEarlyFinish(), false );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getTotalSlackEnd()
	{
		return (getConstraintType() == ConstraintType.ALAP)
			? getLateStart()
			: getLateFinish();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getTotalSlackStart()
	{
		return (getConstraintType() == ConstraintType.ALAP)
		? getEarlyStart()
		: getEarlyFinish();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getUniqueId()
	{
		return hasKey.getUniqueId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getUniqueIdPredecessors()
	{
		return AssociationListFormat.getInstance( DependencyFormat.getInstance( AssociationFormatParameters.getInstance( this,
					true, Configuration.getFieldFromId( "Field.uniqueId" ), false, true ) ) ).format( getPredecessorList() );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getUniqueIdSuccessors()
	{
		return AssociationListFormat.getInstance( DependencyFormat.getInstance( AssociationFormatParameters.getInstance( this,
					false, Configuration.getFieldFromId( "Field.uniqueId" ), false, true ) ) ).format( getSuccessorList() );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getUnits()
	{
		if (getAssignments().isEmpty())
		{
			return 0;
		}

		long duration = getDurationMillis();

		if (duration == 0.0)
		{
			return 1.0D; // degeneratate case
		}

		if (!isInitialized()) // the case when reading a file, don't boether to
							  // calculate
		{
			return 1.0;
		}

		long work = calcWork();

		if (work == 0) // degenerate case with no work yet
		{
			return 1.0;
		}

		return ((double)work) / duration;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getVac( 
		final FieldContext _fieldContext )
	{
		return EarnedValueCalculator.getInstance()
									.vac( this, FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public short getVersion()
	{
		return version;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getWbs()
	{
		return wbs;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection getWbsChildrenNodes()
	{
		return wbsChildrenNodes;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List getWbsChildrenTasks()
	{
		List children = (List)wbsChildrenNodes;
		List impls = NodeList.nodeListToImplList( children );
		Iterator i = impls.iterator();

		while (i.hasNext() == true)
		{
			if (!(i.next() instanceof org.openproj.domain.task.Task))
			{
				i.remove();
			}
		}

		return impls;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getWbsParentName()
	{
		if (wbsParentTask == null)
		{
			return "";
		}

		return wbsParentTask.getName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.openproj.domain.task.Task getWbsParentTask()
	{
		return wbsParentTask;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getWbsPredecessors()
	{
		return AssociationListFormat.getInstance( DependencyFormat.getInstance( AssociationFormatParameters.getInstance( this,
					true, Configuration.getFieldFromId( "Field.wbs" ), true, true ) ) ).format( getPredecessorList() );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getWbsSuccessors()
	{
		return AssociationListFormat.getInstance( DependencyFormat.getInstance( AssociationFormatParameters.getInstance( this,
					false, Configuration.getFieldFromId( "Field.wbs" ), true, true ) ) ).format( getSuccessorList() );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getWindowEarlyFinish()
	{
		return windowEarlyFinish;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getWindowEarlyStart()
	{
		return windowEarlyStart;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getWindowLateFinish()
	{
		return windowLateFinish;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getWindowLateStart()
	{
		return windowLateStart;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getWork( 
		final FieldContext _fieldContext )
	{
		return work( FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	public double getWork()
	{
		return getWork( null );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public WorkCalendar getWorkCalendar()
	{
		return workCalendar;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasActiveAssignment( 
		final long _start,
		final long _end )
	{
		return ((TaskSnapshot)getCurrentSnapshot()).hasActiveAssignment( _start, _end );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasDuration()
	{
		if (isWbsParent())
		{
			return getRawDuration() != 0;
		}
		else
		{
			AssociationList assignments = getAssignments();

			if (assignments.size() == 1)
			{
				return ((Assignment)assignments.getFirst()).hasDuration();
			}

			Iterator i = assignments.iterator();

			while (i.hasNext() == true)
			{
				if (((Assignment)i.next()).hasDuration())
				{
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasLaborAssignment()
	{
		return ((TaskSnapshot)getCurrentSnapshot()).hasLaborAssignment();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasRealAssignments()
	{
		return (null == findAssignment( ResourceImpl.getUnassignedInstance() ));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean inProgress()
	{
		double percentComplete = getPercentComplete();

		return ((percentComplete > 0.0D) && (percentComplete < 1.0D));
	}

	/**
	 * Used when creating a task to set initial date and duration conditions
	 *
	 */
	void initializeDates()
	{
		setRawConstraintType( (project == null)
			? ConstraintType.ASAP
			: project.getDefaultConstraintType() );

		long duration = CalendarOption.getInstance().getDefaultDuration(); //MS uses 1 day estimated
		setRawDuration( duration );
		setWorkCalendar( null );

		// initialize start and end to avoid 0 dates in calculations
		long start = project.getStart();
		myCurrentSchedule.setStart( start );
		myCurrentSchedule.setFinish( start );

		if (ScheduleOption.getInstance().isNewTasksStartToday())
		{
			setWindowEarlyStart( CalendarOption.getInstance().makeValidStart( DateTime.midnightToday(), true ) );
		}
	}

	public void initializeTransientTaskObjects()
	{
		myCurrentSchedule.initSerialized( this, TaskSchedule.CURRENT );
		myEarlySchedule = new TaskSchedule(this, TaskSchedule.EARLY);
		myLateSchedule = new TaskSchedule(this, TaskSchedule.LATE);
		snapshots = new SnapshotContainer(Settings.numBaselines());
		dependencies = new HasDependenciesImpl(this);

		createSnapshot( CURRENT );

		// put the current schedule in the snapshot
		((TaskSnapshot)getCurrentSnapshot()).setCurrentSchedule( myCurrentSchedule );
		setLastSavedStart( myCurrentSchedule.getStart() );
		setLastSavedFinish( myCurrentSchedule.getFinish() );
	}

	public void initializeTransientTaskObjectsAfterDeserialization()
	{
		myEarlySchedule = new TaskSchedule(this, TaskSchedule.EARLY);
		myLateSchedule = new TaskSchedule(this, TaskSchedule.LATE);
		dependencies = new HasDependenciesImpl(this);

		myCurrentSchedule = ((TaskSnapshot)getCurrentSnapshot()).getCurrentSchedule();
		myCurrentSchedule.initSerialized( this, TaskSchedule.CURRENT );

		setLastSavedStart( myCurrentSchedule.getStart() );
		setLastSavedFinish( myCurrentSchedule.getFinish() );

		//		validateConstraints();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void invalidateAssignmentCalendars()
	{
		((TaskSnapshot)getCurrentSnapshot()).invalidateAssignmentCalendars();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Document invalidateCalendar()
	{
		invalidateAssignmentCalendars();
		markTaskAsNeedingRecalculation();

		return project;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void invalidateSchedules()
	{
		myEarlySchedule.invalidate();
		myLateSchedule.invalidate();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isActualsProtected()
	{
		if (!project.isActualsProtected())
		{
			return false;
		}

		if (hasRealAssignments())
		{
			return true;
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAncestorOrDescendent( 
		final org.openproj.domain.task.Task _other )
	{
		return (wbsDescendentOf( _other ) || _other.wbsDescendentOf( this ));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAssignable()
	{
		return !isReadOnly();
	}

	public boolean isAssignedToMe()
	{
		for (Iterator i = getAssignments().iterator(); i.hasNext();)
		{
			Assignment a = (Assignment)i.next();

			if (a.isMine())
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAssignment()
	{
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setMustFinishOn( 
		final long _mustFinish )
	{
		windowEarlyFinish = _mustFinish;
		windowLateFinish = _mustFinish;
	}

	/** 
	 * {@inheritDoc}
	 */ 
	@Override
	public void setName( 
		final String _name )
	{
		hasKey.setName( _name );
	}

	public void setParentDuration()
	{
		if (!isWbsParent())
		{
			return;
		}

		myCurrentSchedule.assignDatesFromChildren( null );

		long duration = getDurationMillis();
		getSchedulingRule().adjustRemainingDuration( this, duration - Duration.millis( getActualDuration() ), true );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setPercentComplete( 
		double _percentComplete )
	{
		if (_percentComplete > 1.0)
		{
			System.out.println( "percent complete more than 100%" );
			_percentComplete = 1.0;
		}
		else if (_percentComplete < 0)
		{
			System.out.println( "percent complete less than 0%" );
			_percentComplete = 0.0;
		}

		if (isZeroDuration())
		{ // special case for completion on milestones

			final double pc = _percentComplete;
			Iterator i = getAssignments().iterator();

			while (i.hasNext() == true)
			{
				((Assignment)i.next()).setPercentComplete( pc );
			}
		}
		else
		{
			long actualDuration = DateTime.closestDate( getDurationMillis() * _percentComplete );
			setActualDuration( actualDuration );

			long stop = getEffectiveWorkCalendar().add( getStart(), actualDuration, false );
			DeepChildWalker.recursivelyTreatBranch( project.getTaskOutline(), this,
				new NumberClosure(stop)
				{
					@Override
					public void execute( 
						final Object _arg0 )
					{
						if (_arg0 == null)
						{
							return;
						}

						Object nodeObject = ((Node)_arg0).getValue();

						if (nodeObject instanceof Task)
						{ // do not treat assignments

							Task task = ((Task)nodeObject);
							task.setStop( Math.min( longValue(), task.getEnd() ) ); // do within range of task
						}
					}
				} );
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setPercentWorkComplete( 
		double _percentWorkComplete )
	{
		if (_percentWorkComplete < 0)
		{
			_percentWorkComplete = 0;
		}

		if (_percentWorkComplete > 1)
		{
			_percentWorkComplete = 1;
		}

		double workValue = _percentWorkComplete * calcSummedWork();

		//		System.out.println("work value is " +
		// DurationFormat.format((long)workValue) +" get work null is " +
		// DurationFormat.format(calcSummedWork()));
		long date = ReverseQuery.getDateAtValue( TimeDistributedConstants.WORK, this, workValue, true ); // allow use of default assignments
		DeepChildWalker.recursivelyTreatBranch( project.getTaskOutline(), this,
			new NumberClosure(date)
			{
				@Override
				public void execute( 
					final Object _arg0 )
				{
					if (_arg0 == null)
					{
						return;
					}

					Object nodeObject = ((Node)_arg0).getValue();

					if (nodeObject instanceof Task) // do not treat assignments
					{
						((Task)nodeObject).setStopNoExtend( getLongValue() );
					}
				}
			} );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setRawConstraintType( 
		final int _constraintType )
	{
		final long d = getConstraintDate(); // save off old date, it will be reused
		clearDateConstraints(); // get rid of all constraints
		setScheduleConstraint( _constraintType, d ); // set new constraint with old date
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setRawDuration( 
		final long _duration )
	{
		myCurrentSchedule.setRawDuration( _duration );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setRemainingDuration( 
		long _remainingDuration )
	{
		_remainingDuration = DateTime.closestDate( Duration.millis( _remainingDuration ) );

		if (_remainingDuration < 0)
		{
			_remainingDuration = 0;
		}

		long oldRemaining = getRemainingDuration();
		setDuration( getDuration() + (_remainingDuration - oldRemaining) );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setRemainingWork( 
		long _remainingWork,
		final FieldContext _fieldContext )
	{
		if (_remainingWork < 0)
		{
			_remainingWork = 0;
		}

		long oldRemaining = getRemainingWork();
		setWork( getWork( _fieldContext ) + (_remainingWork - oldRemaining) );

		//
		//		setActualWork(getWork(fieldContext) - Duration.millis(remainingWork), fieldContext);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setTaskAssignementAndPredsDirty()
	{
		setDirty( true );

		Iterator a = getAssignments().iterator();

		while (a.hasNext())
		{
			((Assignment)a.next()).setDirty( true );
		}

		Iterator d = getDependencyList( true ).iterator();

		while (d.hasNext())
		{
			((Dependency)d.next()).setDirty( false );
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setTimesheetClosed( 
		final boolean _timesheetClosed )
	{
		if (!hasRealAssignments())
		{
			return;
		}

		// check my own assignments
		Iterator i = getAssignments().iterator();

		while (i.hasNext() == true)
		{
			Assignment ass = ((Assignment)i.next());
			ass.setTimesheetClosed( _timesheetClosed );
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setTimesheetComplete( 
		final boolean _resourceComplete )
	{
		// for debugging
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void showProtectionWarning()
	{
		if (isCrossProject() == true)
		{
			Alert.error( Messages.getString( "Message.cannotDeleteTaskWithCrossProjectDependency" ) );
		}
		else
		{
			Alert.error( Messages.getString( "Message.deletionOfTaskWithProtectedActuals" ) );
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void split( 
		final Object _eventSource,
		long _from,
		long _to )
	{
		_from = getEffectiveWorkCalendar().adjustInsideCalendar( _from, false );
		_to = getEffectiveWorkCalendar().adjustInsideCalendar( _to, false );

		if (_from == _to)
		{ // if from is same as two, split one day
			_to = getEffectiveWorkCalendar().add( _from, CalendarOption.getInstance().getMillisPerDay(), false );
		}

		Iterator i = getAssignments().iterator();
		Assignment assignment;

		while (i.hasNext() == true)
		{
			assignment = (Assignment)i.next();
			assignment.split( _eventSource, _from, _to );
		}

		recalculate( _eventSource ); // need to recalculate
		assignParentActualDatesFromChildren();
	}

	boolean isInRange( 
		final long _start,
		final long _finish )
	{
		long s = getStart();

		return ((_finish > s) && (_start < getEnd()));
	}

	private boolean isBaselineFieldHidden( 
		final int _numBaseline,
		final FieldContext _fieldContext )
	{
		TaskSnapshot baseline = (TaskSnapshot)getSnapshot( new Integer(_numBaseline) );

		if (baseline == null)
		{
			return true;
		}

		if (_fieldContext == null) // the baseline exists, but no time range
		{
			return false;
		}

		return ((_fieldContext.getStart() >= baseline.getCurrentSchedule().getFinish()) ||
		(_fieldContext.getEnd() <= baseline.getCurrentSchedule().getStart()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isBaselineTouched( 
		final Object _numBaseline )
	{
		if (baselineTouched == null)
		{
			return false;
		}

		int baseline = (Integer)_numBaseline;

		return baselineTouched[ baseline ];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isComplete()
	{
		return getPercentComplete() >= 1.0D;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isCritical()
	{
		if (myCurrentSchedule.isForward() == true)
		{
			return getEarlyFinish() >= getLateFinish(); //TODO hook into preference
		}
		else
		{
			// reverse schedule
			return getLateStart() <= getEarlyStart();
		}
	}

	public boolean isCrossProject()
	{
		if (getExtraFields() == null)
		{
			return false;
		}

		Field f = Configuration.getFieldFromId( "Field.crossProject" );

		if (f == null)
		{
			return false;
		}

		Object result = getExtraFields().get( f.getAlternateId() );

		return (result == null)
		? false
		: (Boolean)result;
	}

	private boolean isDatelessConstraintType()
	{
		return (constraintType == ConstraintType.ALAP) || (constraintType == ConstraintType.ASAP);
	}

	public boolean isDefault()
	{
		return this == Model.getUnassignedTaskInstance();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDelegatedToUser()
	{
		Resource del = getDelegatedTo();

		return (del != null) && Environment.getLogin().equals( del.getUserAccount() );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDirty()
	{
		return dirty || (getStart() != getLastSavedStart()) || (getEnd() != getLastSavedFinish());
	}

	private boolean isEarnedValueFieldHidden( 
		final FieldContext _fieldContext )
	{
		if (isFieldHidden( _fieldContext ))
		{ // reverse schedule

			return true;
		}

		if (_fieldContext == null)
		{
			return false;
		}

		return project.getStatusDate() < _fieldContext.getStart();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEffortDriven()
	{
		return ((TaskSnapshot)getCurrentSnapshot()).isEffortDriven();
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public final boolean isEstimated()
	{
		return estimated;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean isExternal()
	{
		return external;
	}

	public boolean isExternallyVisible()
	{
		if (extraFields == null)
		{
			return false;
		}

		Field f = Configuration.getFieldFromId( "Field.externallyVisible" );
		Object result = getExtraFields().get( f.getAlternateId() );

		return (result == null)
		? false
		: (Boolean)result;
	}

	private boolean isFieldHidden( 
		final FieldContext _fieldContext )
	{
		return (_fieldContext != null) && !isInRange( _fieldContext.getStart(), _fieldContext.getEnd() );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isIgnoreResourceCalendar()
	{
		return ((TaskSnapshot)getCurrentSnapshot()).isIgnoreResourceCalendar();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean isInSubproject()
	{
		return inSubproject;
	}

	private boolean isInitialized()
	{
		return (project != null) && project.isInitialized();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isInvalidIntersectionCalendar()
	{
		Iterator i = getAssignments().iterator();

		while (i.hasNext() == true)
		{
			if (((Assignment)i.next()).isInvalidIntersectionCalendar())
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isJustModified()
	{
		if (project.getSchedulingAlgorithm() == null)
		{
			return false;
		}

		return (calculationStateCount + PredecessorTaskList.CALCULATION_STATUS_STEP) >= project.getSchedulingAlgorithm()
																							   .getCalculationStateCount();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isLabor()
	{
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isLateStarting()
	{
		if (getPercentComplete() > 0.0D)
		{
			return false;
		}

		long diff = calcTimeToStartFromNow();

		return diff < 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isLocal()
	{
		return hasKey.isLocal();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isMarkTaskAsMilestone()
	{
		return markTaskAsMilestone;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isMilestone()
	{
		//	return !hasDuration();
		return (Duration.millis( getRawDuration() ) == 0) || isMarkTaskAsMilestone();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isMissedDeadline()
	{
		if (deadline == 0)
		{
			return false;
		}
		else
		{
			return deadline < getEnd();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isNormal()
	{
		return !isSummary() && !isMilestone() && !isExternal();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isOrWasCritical()
	{
		if (isCritical())
		{
			return true;
		}

		if (myCurrentSchedule.isForward())
		{
			return getEnd() >= getLateFinish();
		}
		else // reverse schedule
		{
			return getStart() <= getEarlyStart();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isParent()
	{
		return isWbsParent();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isParentWithAssignments()
	{
		return isWbsParent() && hasRealAssignments();
	}

	/**
	 * For parent tasks, we don't want to count their one day of work
	 */
	private boolean isParentWithoutAssignments()
	{
		return (isWbsParent() && !hasRealAssignments());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPendingTimesheetUpdate()
	{
		return TimesheetHelper.isPendingTimesheetUpdate( getAssignments() );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPinned()
	{
		return pinned;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isProtected()
	{
		if (isCrossProject())
		{
			return true;
		}

		if (!project.isActualsProtected())
		{
			return false;
		}

		if (areActuals() && !canDeleteActuals())
		{
			return true;
		}

		AssociationList assignments = getRealAssignments();

		for (final Assignment assignment : (Collection<Assignment>)assignments)
		{ //needed? done below and in hierachy anyway

			if (assignment.isProtected())
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isReadOnly()
	{
		return isExternal() || isSubproject() || ((getOwningProject() != null) && getOwningProject().isReadOnly()) || isRoot();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isReadOnlyActualDuration( 
		final FieldContext _fieldContext )
	{
		return isActualsProtected();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isReadOnlyActualFinish( 
		final FieldContext _fieldContext )
	{
		return isActualsProtected();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isReadOnlyActualStart( 
		final FieldContext _fieldContext )
	{
		return isActualsProtected();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isReadOnlyActualWork( 
		final FieldContext _fieldContext )
	{
		return isActualsProtected();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isReadOnlyComplete( 
		final FieldContext _context )
	{
		return isActualsProtected();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isReadOnlyCompletedThrough( 
		final FieldContext _context )
	{
		return isActualsProtected();
	}

	@Override
	public boolean isReadOnlyConstraintDate( 
		final FieldContext _fieldContext )
	{
		return (getConstraintType() == ConstraintType.ALAP) || (getConstraintType() == ConstraintType.ASAP);
	}

	//	task.setDirty(false);
	//	task.setLastSavedStart(task.getStart());
	//	task.setLastSavedFinish(task.getEnd());
	//	Iterator j = task.getAssignments().iterator();
	//	while (j.hasNext())
	//		((Assignment)j.next()).setDirty(false);
	//	j=task.getDependencyList(true).iterator();
	//	while (j.hasNext())
	//		((Dependency)j.next()).setDirty(false);
	//	}
	//	public double getTotalCost() {
	//		return getCost() + getFixedCost();
	//	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isReadOnlyCost( 
		final FieldContext _fieldContext )
	{
		return (!Environment.isNoPodServer() || hasRealAssignments() || isReadOnlyFixedCost( _fieldContext ));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isReadOnlyEffortDriven( 
		final FieldContext _fieldContext )
	{
		return ((TaskSnapshot)getCurrentSnapshot()).isReadOnlyEffortDriven( _fieldContext );
	}

	public boolean isReadOnlyExternallyVisible( 
		final FieldContext _fieldContext )
	{ // readonly if milestone

		if (isCrossProject())
		{
			return true;
		}

		String milestone = getInvestmentMilestone();

		if ((milestone != null) && (milestone.trim().length() > 0))
		{
			return true;
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isReadOnlyFixedCost( 
		final FieldContext _fieldContext )
	{
		return FieldContext.hasInterval( _fieldContext );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isReadOnlyPercentComplete( 
		final FieldContext _fieldContext )
	{
		return isActualsProtected();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isReadOnlyPercentWorkComplete( 
		final FieldContext _context )
	{
		return isActualsProtected();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isReadOnlyRemainingWork( 
		final FieldContext _fieldContext )
	{
		return isActualsProtected() || isReadOnlyWork( _fieldContext );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isReadOnlyTimesheetClosed( 
		final FieldContext _fieldContext )
	{
		return !project.isTimesheetAssociated() || (!isZeroDuration() && !isComplete());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isReadOnlyWork( 
		final FieldContext _fieldContext )
	{
		if (!hasLaborAssignment())
		{
			return true;
		}

		if (_fieldContext == null)
		{
			return false;
		}

		return !hasActiveAssignment( _fieldContext.getStart(), _fieldContext.getEnd() );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean isReverseScheduled()
	{
		if (project.isForward() == true)
		{
			return constraintType == ConstraintType.ALAP;
		}
		else
		{
			return constraintType == ConstraintType.ASAP;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isRoot()
	{
		return getUniqueId() == DataObject.SUMMARY_UNIQUE_ID;
	}

	public boolean isSlipped()
	{
		long bf = getBaselineFinish();

		return (bf != 0) && (getEnd() > bf);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isStartingWithinOneWeek()
	{
		if (getPercentComplete() > 0.0D)
		{
			return false;
		}

		long diff = calcTimeToStartFromNow();

		return (diff > 0) && (diff < millisInWeek);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isStartingWithinTwoWeeks()
	{
		if (getPercentComplete() > 0.0D)
		{
			return false;
		}

		long diff = calcTimeToStartFromNow();

		return (diff > 0) && (diff < (2L * millisInWeek));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSubproject()
	{
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSubprojectReadOnly()
	{
		return false;
	}

//	@Override
//	public boolean isReadOnlyUnits( 
//		FieldContext fieldContext )
//	{
//		return true;
//	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSummary()
	{
		return isWbsParent(); //TODO need to somehow hook into view and see if parent in view's node model.  yuck!
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isTestImportOk()
	{
		if (this.getOutlineLevel() == 0)
		{
			return true; //todo handle this properly
		}

		boolean ok = (DateTime.dayFloor( getStart() ) == DateTime.dayFloor( getCustomDate( 0 ) )) &&
			(DateTime.dayFloor( getEnd() ) == DateTime.dayFloor( getCustomDate( 1 ) )) &&
			(getDurationMillis() == Duration.millis( getCustomDuration( 0 ) )) &&
			((getOutlineString() == getCustomText( 0 )) || getOutlineString().equals( getCustomText( 0 ) )); // tests for nulls first

		if (!ok)
		{
			System.out.println( getName() + " " + getId() +
				" not ok " //					+ DurationFormat.format(getDurationMillis()) + "    -    " + DurationFormat.format(getCustomDuration(0)));
				 +"start " + (DateTime.dayFloor( getStart() ) == DateTime.dayFloor( getCustomDate( 0 ) )) + " \nend " +
				(DateTime.dayFloor( getEnd() ) == DateTime.dayFloor( getCustomDate( 1 ) )) + " \nend prj " +
				DateTime.medString( DateTime.dayFloor( getEnd() ) ) + " mar " +
				DateTime.medString( DateTime.dayFloor( getCustomDate( 1 ) ) ) + " \ndur " +
				(getDurationMillis() == Duration.millis( getCustomDuration( 0 ) )) + " \noutline " + getOutlineString() + " " +
				getCustomText( 0 ) );
			System.out.println( "start---end " + DateTime.medString( getStart() ) + "  " + DateTime.medString( getEnd() ) );
			System.out.println( "dura proj --- mar " + DurationFormat.format( getDurationMillis() ) + "  " +
				DurationFormat.format( getCustomDuration( 0 ) ) );
		}

		//		else
		//			dumpTest();
		return ok;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isTimesheetClosed()
	{
		if (!hasRealAssignments())
		{
			return false;
		}

		Iterator i = getAssignments().iterator();

		while (i.hasNext() == true)
		{
			Assignment ass = ((Assignment)i.next());

			if (!ass.isTimesheetClosed())
			{
				return false;
			}
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isTimesheetComplete()
	{
		if (!hasRealAssignments() && !isWbsParent())
		{
			return false;
		}

		// check my own assignments
		Iterator i = getAssignments().iterator();

		while (i.hasNext() == true)
		{
			Assignment ass = ((Assignment)i.next());

			if (ass.isDefault())
			{
				continue;
			}

			if (!ass.isTimesheetComplete())
			{
				return false;
			}
		}

		// check my children
		if (isWbsParent() == true)
		{
			Iterator j = getWbsChildrenTasks().iterator();

			while (j.hasNext())
			{
				Task t = (Task)j.next();

				if (!t.isTimesheetComplete())
				{
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isUnstarted()
	{
		return getPercentComplete() == 0.0D;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isWbsParent()
	{
		if ((wbsChildrenNodes == null) || (wbsChildrenNodes.isEmpty() == true))
		{
			//a task has at least one assignment
			//			System.out.println(this + " is not a wbs parent " + wbsChildrenNodes);
			return false;
		}

		Iterator i = wbsChildrenNodes.iterator();
		Object current;

		while (i.hasNext() == true)
		{
			current = ((Node)i.next()).getValue();

			if (current instanceof org.openproj.domain.task.Task)
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isZeroDuration()
	{
		return Duration.millis( getRawDuration() ) == 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean liesInSubproject()
	{
		if (wbsParentTask == null)
		{
			return false;
		}

		if (wbsParentTask.isSubproject() == true)
		{
			return true;
		}

		return wbsParentTask.liesInSubproject();
	}

	private int makeValidConstraintType( 
		final int _type )
	{
		if (isWbsParent())
		{ // parents have a limited choice of constraint types

			if ((_type == ConstraintType.FNLT) || (_type == ConstraintType.SNET))
			{
				return _type;
			}
			else
			{
				return project.getDefaultConstraintType();
			}
		}

		return _type;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void markAllDependentTasksAsNeedingRecalculation( 
		final boolean _doSelf )
	{
		if (_doSelf == true)
		{
			markTaskAsNeedingRecalculation();
		}

		Iterator succ = getSuccessorList().iterator();

		if (!succ.hasNext())
		{
			project.getSchedulingAlgorithm().markBoundsAsDirty();

			//TODO what about reverse schedulded?
		}
		else
		{
			org.openproj.domain.task.Task successor;

			// mark successors as dirty
			while (succ.hasNext())
			{
				successor = (org.openproj.domain.task.Task)((Dependency)succ.next()).getSuccessor();
				successor.markTaskAsNeedingRecalculation();
			}
		}

		// mark parent as dirty
		org.openproj.domain.task.Task parent = wbsParentTask;

		while (parent != null)
		{
			parent.markTaskAsNeedingRecalculation();
			parent = parent.getWbsParentTask();
		}

		// mark children as dirty
		Collection children = wbsChildrenNodes;

		if (children != null)
		{
			Iterator i = children.iterator();
			Object child;

			while (i.hasNext() == true)
			{
				child = ((Node)i.next()).getValue();

				if (!(child instanceof org.openproj.domain.task.Task))
				{
					continue;
				}

				((org.openproj.domain.task.Task)child).markTaskAsNeedingRecalculation();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void markTaskAsNeedingRecalculation()
	{
		int nextStateCount = project.getCalculationStateCount() + 1;
		setCalculationStateCount( nextStateCount );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void moveInterval( 
		final Object _eventSource,
		long _start,
		final long _end,
		final ScheduleInterval _oldInterval,
		final boolean _isChild )
	{
		WorkCalendar cal = getEffectiveWorkCalendar();
		_start = cal.adjustInsideCalendar( _start, false );

		boolean shifting = cal.compare( _start, _oldInterval.getStart(), false ) != 0;
		long assignmentStart = getEarliestAssignmentStart();
		long amountFromStart = cal.compare( _oldInterval.getStart(), assignmentStart, false ); // possible that they are not the same but there is no working time between them

		if (shifting && (amountFromStart == 0L))
		{ // see if first bar shifted -The first bar is drawn from the first assignment and not from the task start.
		  // To figure out the new task start, see how much the shift of this bar is, then apply that difference to the task start

			long shift = cal.compare( _start, assignmentStart, false );
			long newTaskStart = cal.add( getStart(), shift, false );
			setStart( newTaskStart );
		}
		else
		{
			long amount = cal.compare( _end, _oldInterval.getEnd(), false );

			if (amount == 0L) // skip if nothing moved
			{
				return;
			}

			Iterator i = getAssignments().iterator();
			Assignment assignment;

			while (i.hasNext() == true)
			{
				assignment = (Assignment)i.next();
				assignment.moveInterval( _eventSource, _start, _end, _oldInterval, true );
			}
		}

		setRawDuration( getDurationMillis() ); // this fixes all sorts of pbs

		recalculate( _eventSource ); // need to recalculate
		assignParentActualDatesFromChildren();

//		//Undo
//		UndoableEditSupport undoableEditSupport=project.getUndoController().getEditSupport();
//		if (undoableEditSupport!=null&&!(eventSource instanceof UndoableEdit)){
//			undoableEditSupport.postEdit(new ScheduleEdit(this,new ScheduleInterval(start,end),oldInterval,isChild,eventSource));
//		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void moveRemainingToDate( 
		long _date )
	{
		_date = getEffectiveWorkCalendar().adjustInsideCalendar( _date, false );

		if (getActualStart() == 0L)
		{
			setStart( _date ); // if not started, change start
		}
		else if (inProgress())
		{
			Iterator i = getAssignments().iterator();
			Assignment assignment;

			while (i.hasNext() == true)
			{
				assignment = (Assignment)i.next();
				assignment.moveRemainingToDate( _date );
			}
		} // do nothing for completed tasks
	}

	private Assignment newDefaultAssignment()
	{
		return Assignment.getInstance( this, ResourceImpl.getUnassignedInstance(), 1.0, 0 );
	}

	private void readObject( 
		final ObjectInputStream _stream )
		throws IOException, ClassNotFoundException
	{
		/*
		 * Version 3 implementation
		 */
		_stream.defaultReadObject();

		hasKey = HasKeyImpl.deserialize( _stream, this );
		customFields = CustomFieldsImpl.deserialize( _stream );

		snapshots = new SnapshotContainer(Settings.numBaselines());

		int sCount = _stream.readInt();

		for (int i = 0; i < sCount; i++)
		{
			int snapshotId = _stream.readInt();
			TaskSnapshot snapshot = TaskSnapshot.deserialize( _stream, getVersion() );
			snapshot.getCurrentSchedule().setTask( this );
			setSnapshot( Integer.valueOf( snapshotId ), snapshot );
		}

		myEarlySchedule = new TaskSchedule(this, TaskSchedule.EARLY);
		myLateSchedule = new TaskSchedule(this, TaskSchedule.LATE);
		dependencies = new HasDependenciesImpl(this);

		myCurrentSchedule = ((TaskSnapshot)getCurrentSnapshot()).getCurrentSchedule();
		myCurrentSchedule.initSerialized( this, TaskSchedule.CURRENT );

		setLastSavedStart( myCurrentSchedule.getStart() );
		setLastSavedFinish( myCurrentSchedule.getFinish() );

		//		validateConstraints();

		//	    barClosureInstance = new BarClosure();
		//	    This shouldn't be called -hk 4/feb/05
		//	    initializeDates();
		version = CURRENT_VERSION;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void recalculate( 
		final Object _eventSource )
	{
		((Project)getDocument()).updateScheduling( _eventSource, this, ObjectEvent.UPDATE, getEndField() );

		//getDocument().getObjectEventManager().fireUpdateEvent(eventSource,this,getEndField());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void recalculateLater( 
		final Object _eventSource )
	{
		markTaskAsNeedingRecalculation(); // task needs to be recalculated

		SwingUtilities.invokeLater( new Runnable()
			{
				@Override
				public void run()
				{
					recalculate( _eventSource );
				}
			} );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long remainingWork( 
		final long _start,
		final long _end )
	{
		if (isParentWithoutAssignments())
		{
			return 0L;
		}

		return ((TaskSnapshot)getCurrentSnapshot()).remainingWork( _start, _end );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeAssignment( 
		final Assignment _assignment )
	{
		//project.beginUndoUpdate();
		boolean recalculateDuration = !_assignment.isDefault() && _assignment.isInitialized(); // && assignment.isLabor();

		// JGao 9/24/2009 Redid the change I made yesterday. The reason is that the adjusting logic is working correctly if all the assignments
		// start and finish on the same date. Therefore I uncommented the code for adjusting the rest of the assignment but added additional
		// condition of all assignments have same start and finish date.
		boolean adjustRestOfAssignments = true;
		Iterator i = getAssignments().iterator();

		while (i.hasNext() == true)
		{
			Assignment tempAssignment = (Assignment)i.next();

			if ((tempAssignment.getStart() != getStart()) || (tempAssignment.getFinish() != getEnd()))
			{
				adjustRestOfAssignments = false;

				break;
			}
		}

		double assignedRate = getRemainingUnits();
		((TaskSnapshot)getCurrentSnapshot()).removeAssignment( _assignment );

		if (!_assignment.isDefault())
		{
			if (recalculateDuration && isEffortDriven() && adjustRestOfAssignments)
			{
				double newUnits = assignedRate - _assignment.getLaborUnits();

				if (newUnits != 0)
				{
					getSchedulingRule().adjustRemainingUnits( this, newUnits, assignedRate, true, true ); // conserve total units
				}
			}

			if (getAssignments().isEmpty())
			{
				Assignment newDefault = newDefaultAssignment();
				newDefault.usePropertiesOf( _assignment ); // the default assignment must take on properties of the removed assignment
				AssignmentService.getInstance().connect( newDefault, null );
			}
		}

		setDirty( true );

		//project.endUndoUpdate();
	}

	public void removeRemainingWork()
	{
		Iterator i = getAssignments().iterator();

		while (i.hasNext() == true)
		{
			Assignment ass = ((Assignment)i.next());
			ass.removeRemainingWork();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean renumber( 
		final boolean _localOnly )
	{
		ResultClosure c = new ResultClosure()
			{
				@Override
				public void execute( 
					final Object _arg0 )
				{
					result |= ((Assignment)_arg0).renumber( _localOnly );
				}
			};

		boolean r = c.result;
		forSnapshotsAssignments( c, true );

		return r | hasKey.renumber( _localOnly );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void restoreDetail( 
		final Object _source,
		final Object _backup,
		final boolean _isChild )
	{
		restoreDetail( _source, _backup, _isChild, (TaskSnapshot)getCurrentSnapshot() );
	}

	public void restoreDetail( 
		final Object _source,
		final Object _backup,
		final boolean _isChild,
		final TaskSnapshot _snapshot )
	{
		TaskBackup b = (TaskBackup)_backup;
		windowEarlyFinish = b.windowEarlyFinish;
		windowEarlyStart = b.windowEarlyStart;
		windowLateFinish = b.windowLateFinish;
		windowLateStart = b.windowLateStart;
		actualStart = b.actualStart;
		TaskSnapshotBackup.restore( _snapshot, b.snapshot );

		if (!_isChild)
		{
			recalculate( _source ); //to send update event
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void restoreSnapshot( 
		final Object _snapshotId,
		final Object _b )
	{
		TaskBackup backup = (TaskBackup)_b;

		if (backup.snapshot == null)
		{
			return;
		}

		TaskSnapshot snapshot = (TaskSnapshot)((TaskSnapshot)getSnapshot( CURRENT )).clone();

		//snapshot.setCurrentSchedule(getCurrentSchedule());
		restoreDetail//snapshot.setCurrentSchedule(getCurrentSchedule());
		( this, backup, true, snapshot );
		setSnapshot( _snapshotId, snapshot );
		markTaskAsNeedingRecalculation(); // for redraw purpooses, not for recalc.
		touchBaseline( _snapshotId );
		setDirty( true );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void restrictToValidConstraintType()
	{
		setRawConstraintType( makeValidConstraintType( getConstraintType() ) );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void saveCurrentToSnapshot( 
		final Object _snapshotId )
	{
		setSnapshot( _snapshotId, cloneSnapshot( getSnapshot( CURRENT ) ) );
		markTaskAsNeedingRecalculation(); // for redraw purpooses, not for recalc.
		touchBaseline( _snapshotId );
		setDirty( true );
	}

	public void serialize( 
		final ObjectOutputStream _s )
		throws IOException
	{
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setActualDuration( 
		long _actualDuration )
	{
		_actualDuration = DateTime.closestDate( Duration.millis( _actualDuration ) );

		if (_actualDuration == Duration.millis( getActualDuration() ))
		{
			return;
		}

		long stop = getEffectiveWorkCalendar().add( getStart(), _actualDuration, true );

		if (_actualDuration > getDurationMillis())
		{
			setDurationWithActualsHandling( _actualDuration, false );
			myCurrentSchedule.setEnd( stop ); // need to set this so setStop below works properly
		}

		setStop( stop );
		System.out.println( "duration " + getDurationMillis() + " ac " + getActualDuration() + " ddd " + _actualDuration );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setActualFinish( 
		final long _actualFinish )
	{
		long old = getActualFinish();

		if (_actualFinish == old)
		{
			return;
		}

		setEnd( _actualFinish );
		setPercentComplete( 1.0 );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setActualStart( 
		long _actualStart )
	{
		_actualStart = getEffectiveWorkCalendar().adjustInsideCalendar( _actualStart, false ); //TODO not good if it starts off calendar

		setActualStartNoEvent( _actualStart );
		markTaskAsNeedingRecalculation();
		project.fireScheduleChanged( this, ScheduleEvent.ACTUAL, this );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setActualStartNoEvent( 
		final long _actualStart )
	{
		long old = getActualStart();

		if (_actualStart == old)
		{
			return;
		}

		//		System.out.println( "setActualStartNoEvent " + this + " " + new java.util.Date( actualStart ) );
		//		if (actualStart != 0) 
		//		{
		//			if (getPercentComplete() == 0) 
		//		{
		//				currentSchedule.setPercentComplete( INSTANT_COMPLETION );
		//				setPercentComplete( INSTANT_COMPLETION );
		//			}
		//			currentSchedule.setStart( actualStart );
		//		}
		this.actualStart = _actualStart;
		assignParentActualDatesFromChildren();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setActualWork( 
		final long _actualWork,
		final FieldContext _context )
	{
		if (FieldContext.hasInterval( _context ))
		{
			Iterator i = getAssignments().iterator();

			while (i.hasNext() == true)
			{
				Assignment assignment = (Assignment)i.next();
				assignment.setActualWork( _actualWork, _context );
			}
		}
		else
		{
			long workValue = Duration.millis( _actualWork );

			if (workValue == 0L)
			{
				setPercentComplete( 0 );
			}
			else
			{
				boolean increased = false;

				if (workValue > getWork( _context ))
				{
					setWork( _actualWork );
					increased = true;
				}

				boolean allowDefault = !isWbsParent();
				long date = ReverseQuery.getDateAtValue( TimeDistributedConstants.WORK, this, workValue, allowDefault );
				setStop( date, !increased );
			}
		}
	}

	/**
	 * Set all schedules to a fixed start and end
	 *
	 * @param _start
	 * @param _finish
	 */
	private void setAllSchedules( 
		final long _start,
		final long _finish )
	{
		myCurrentSchedule.setStart( _start );
		myCurrentSchedule.setFinish( _finish );
		myEarlySchedule.setStart( _start );
		myEarlySchedule.setFinish( _finish );
		myLateSchedule.setStart( _start );
		myLateSchedule.setFinish( _finish );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setAllSchedulesToCurrentDates()
	{
		setAllSchedules( myCurrentSchedule.getStart(), myCurrentSchedule.getFinish() );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setCalculationStateCount( 
		int _calculationStateCount )
	{
//		if (this.calculationStateCount == calculationStateCount) return;
//		System.out.println("setCalculationStateCount");
//		setDirty(true);
		_calculationStateCount = _calculationStateCount;
	}

	public void setCapEx( 
		final boolean _value )
	{
		Field f = Configuration.getFieldFromId( "Field.capEx" );
		getExtraFields().put( f.getAlternateId(), _value );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setComplete( 
		final boolean _complete )
	{
		ScheduleUtil.setComplete( this, _complete );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setCompletedThrough( 
		long _completedThrough )
	{
		_completedThrough = DateTime.closestDate( _completedThrough );
		_completedThrough = Math.min( _completedThrough, getEnd() );

		if (_completedThrough == getCompletedThrough())
		{
			return;
		}

		Iterator i = getAssignments().iterator();
		Assignment assignment;
		long computedActualStart = Long.MAX_VALUE;
		long assignmentActualStart;

		while (i.hasNext() == true)
		{
			assignment = (Assignment)i.next();
			assignment.setCompletedThrough( _completedThrough );
			assignmentActualStart = assignment.getActualStart();

			if ((assignmentActualStart != 0) && (assignmentActualStart < computedActualStart))
			{
				computedActualStart = assignmentActualStart;
			}
		}

		if (computedActualStart == Long.MAX_VALUE)
		{
			computedActualStart = 0;
		}

		setActualStart( computedActualStart );
		assignParentActualDatesFromChildren();

		// if % complete went down to 0, then the plan changed and need to recalculate all.
		if (computedActualStart == 0)
		{
			getDocument().fireUpdateEvent( this, this, Configuration.getFieldFromId( "Field.start" ) );
		}
		else
		{
			//TODO duplicate event
			//TODO in the case of progress update this event is useless since critical path runs after.
			project.fireScheduleChanged( this, ScheduleEvent.ACTUAL, this );
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setConstraintDate( 
		long _date )
	{
		if (_date == 0)
		{
			clearDateConstraints();
			constraintType = (project == null)
				? ConstraintType.ASAP
				: project.getDefaultConstraintType();
		}
		else
		{
			_date = getEffectiveWorkCalendar().adjustInsideCalendar( _date, false ); // make date valid
		}

		setScheduleConstraint( constraintType, _date );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setConstraintType( 
		final int _constraintType )
		throws FieldParseException
	{
		int newConstraintType = makeValidConstraintType( _constraintType ); // limit to valid options

		if (newConstraintType != _constraintType)
		{
			throw new FieldParseException(Messages.getString( "Message.parentConstraintType" ));
		}

		setRawConstraintType( _constraintType );

		long d = getConstraintDate(); // save off old date, it will be reused

		// if the constraint requires a date and there is none, set a date to today. added a bit of margin 5/5/08
		if (!isDatelessConstraintType() && (d < (5 * WorkCalendar.MILLIS_IN_DAY)))
		{
			setConstraintDate( getEffectiveWorkCalendar().adjustInsideCalendar( DateTime.midnightToday(), false ) );
		}
	}

	/** Assign the <i>contraintType</i> attribute.
	 *
	 * @param _value used as the source of the assignment.
	 */
	public void setContraintTypeOnly( 
		final int _value )
	{
		constraintType = _value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setCost( 
		final double _cost,
		final FieldContext _context )
	{
		setFixedCost( _cost );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setCreated( 
		final Date _created )
	{
		hasKey.setCreated( _created );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setCurrentSnapshot( 
		final DataSnapshot _snapshot )
	{
		snapshots.setCurrentSnapshot( _snapshot );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setCustomCost( 
		final int _index,
		final double _cost )
	{
		customFields.setCustomCost( _index, _cost );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setCustomDate( 
		final int _index,
		final long _date )
	{
		customFields.setCustomDate( _index, _date );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setCustomDuration( 
		final int _index,
		final long _duration )
	{
		customFields.setCustomDuration( _index, _duration );
	}

	/** Assign the <i>custom fields</i> attribute.
	 *
	 * @param _value used as the source of the assignment.
	 */
	public void setCustomFields( 
		final CustomFieldsImpl _value )
	{
		customFields = _value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setCustomFinish( 
		final int _index,
		final long _finish )
	{
		customFields.setCustomFinish( _index, _finish );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setCustomFlag( 
		final int _index,
		final boolean _flag )
	{
		customFields.setCustomFlag( _index, _flag );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setCustomNumber( 
		final int _index,
		final double _number )
	{
		customFields.setCustomNumber( _index, _number );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setCustomStart( 
		final int _index,
		final long _start )
	{
		customFields.setCustomStart( _index, _start );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setCustomText( 
		final int _index,
		final String _text )
	{
		customFields.setCustomText( _index, _text );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDeadline( 
		long _deadline )
	{
		if (_deadline != 0L)
		{
			_deadline = CalendarOption.getInstance().makeValidEnd( _deadline, false );
		}

		deadline = _deadline;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setDebugDependencyOrder( 
		final int _debugDependencyOrder )
	{
		debugDependencyOrder = _debugDependencyOrder;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDelegatedTo( 
		final Resource _delegatedTo )
	{
		Resource old = delegatedTo;
		delegatedTo = _delegatedTo;

//		if (old == null) {
//			SwingUtilities.invokeLater(new Runnable() {
//
//				public void run() {
//					Task newOne = getOwningProject().cloneTask(Task.this);
//				}});
//		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDependencyStart( 
		final long _dependencyStart )
	{
		myCurrentSchedule.setRemainingDependencyDate( _dependencyStart );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDirty( 
		final boolean _dirty )
	{
//		super.setDirty( dirty );
		dirty = _dirty;

		if (dirty == false)
		{
			setLastSavedStart( myCurrentSchedule.getStart() );
			setLastSavedFinish( myCurrentSchedule.getFinish() );
		}

		if (dirty && (project != null))
		{
			project.setGroupDirty( true );
		}

		if ((dirty == false) 
		 && (extraFields != null))
		{
			extraFields.markAsClean();
		}

		if (dirty == false)
		{
			baselineTouched = null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDuration( 
		final long _duration )
	{
		//,FieldContext context) {
		// JGao 12/8/2009 Added the following logic for handling changing a fixed duration task which has 0 work (no assignment). 
		// This is due to the change I am also making to fix the behavior of changing 0 work fixed duration task. Basically, the 
		// units of the default assignment is 0 for fixed duration task which has 0 work. The change is to reset the units of the 
		// default assignment to 1.0 in order for the duration to be changed correctly. However, this change is also used by the 
		// code path here. As a result, the work will also changed according to the new duration. Just set to 0 if it fits
		// the creteria of mentioned case.
		boolean resetAssignmentUnits = false;
		AssociationList assignments = this.getAssignments();

		if ((assignments.size() == 1) && ((Assignment)assignments.getFirst()).isDefault() && (getWork() == 0.0) &&
				(getRemainingUnits() == 0.0))
		{
			resetAssignmentUnits = true;
		}

		if (getActualDuration() > Duration.millis( _duration ))
		{ // dont allow reduction of actuals if actuals are protected

			if (isActualsProtected())
			{
				setRemainingDuration( 0 );
			}
			else
			{
				setPercentComplete( 0 );
				setDuration( _duration );
				setPercentComplete( 1 );
			}

			return;
		}

		setDurationWithActualsHandling( _duration, true );

		if (resetAssignmentUnits)
		{
			setWork( 0 );
		}
	}

	/**
	 * @param duration
	 */

	//	public void setDuration(long duration) {
	//		System.out.println("calling NO context set duration");
	//		setDurationWithActualsHandling(duration,true);
	//	}
	private void setDurationWithActualsHandling( 
		long _duration,
		final boolean _reduceActualsIfReducingDurationToLessThanActuals )
	{
		setRawDuration( _duration ); // set the schedule duration, primariy for use when reading a file
		estimated = Duration.isEstimated( _duration );
		_duration = Duration.millis( _duration );

		long actualDurationMillis = Duration.millis( getActualDuration() );

		if (_reduceActualsIfReducingDurationToLessThanActuals)
		{
			if (_duration < actualDurationMillis) // if reducing duration to shorter than the current actual duration
			{
				setPercentComplete( 1 );
			}
		}

		if (isWbsParent() == false)
		{
			long remainingDuration = _duration - actualDurationMillis;
			getSchedulingRule().adjustRemainingDuration( this, remainingDuration, true );
		}

		updateCachedDuration();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setEarnedValueMethod( 
		final int _earnedValueMethod )
	{
		earnedValueMethod = _earnedValueMethod;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setEffortDriven( 
		final boolean _effortDriven )
	{
		((TaskSnapshot)getCurrentSnapshot()).setEffortDriven( _effortDriven );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setEnd( 
		long _end,
		final FieldContext _context )
	{
		if (isActualsProtected() && (_end < getStop()))
		{
			_end = getStop();
		}

		setEnd( _end );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setEnd( 
		long _end )
	{
		long start = getStart();

		if (start == 0)
		{ // if the end date is entered on a new line creating the task need to set the start correctly
			start = CalendarOption.getInstance().makeValidStart( DateTime.midnightToday(), true );
			myCurrentSchedule.setStart( start );
		}

		_end = CalendarOption.getInstance().makeValidEnd( _end, true );

		if (_end < start)
		{
			_end = start;
		}

		long oldEnd = getEnd();

		if (_end != oldEnd)
		{
//			super.setEnd( end );
			myCurrentSchedule.setEnd( _end );

			Iterator i = getAssignments().iterator();
			Assignment assignment;

			while (i.hasNext() == true)
			{
				assignment = (Assignment)i.next();
				assignment.setEnd( _end );
			}

//			System.out.println("Old End"  + new Date(oldEnd) + " input end " + new Date(end )+ " resulting End " + new Date(getEnd()) + " duration " + DurationFormat.format(getDuration()));
			setRawDuration( getDurationMillis() );
		}

		assignParentActualDatesFromChildren();
	}

	/** 
	 * {@inheritDoc}
	 * 
	 * <p>
	 * First level parents will have their status set by the CP.  Higher levels will need to be set recursively. 
	 * Note that a parent will only be asked to updated its estimated status if one of its children has had its estimated status 
	 * change.
	 */
	@Override
	public void setEstimated( 
		final boolean _estimated )
	{
		boolean changed = this.estimated != _estimated;
		this.estimated = _estimated;

		if ((changed == true) 
		 && (isWbsParent() == true))
		{ 
			// only deal with parents already since CP handles children and sets first parent level
			Task parent = (Task)this.getWbsParentTask();

			if (parent != null)
			{
				parent.updateEstimatedStatus();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setExpenseType( 
		final ExpenseType _budgetType )
	{
		expenseType = _budgetType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setExternal( 
		final boolean _external )
	{
		external = _external;
	}

	public void setExternallyVisible( 
		final boolean _externallyVisible )
	{
		Field f = Configuration.getFieldFromId( "Field.externallyVisible" );
		getExtraFields().put( f.getAlternateId(), _externallyVisible );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setExtraFields( 
		final Map _extraFields )
	{
		extraFields = (HashMapWithDirtyFlags)_extraFields;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFinishNoEarlierThan( 
		final long _date )
	{
		windowEarlyFinish = _date;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFinishNoLaterThan( 
		final long _date )
	{
		windowLateFinish = _date;
		windowEarlyFinish = 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFixedCost( 
		final double _fixedCost,
		final FieldContext _fieldContext )
	{
		if (!FieldContext.hasInterval( _fieldContext ))
		{
			setFixedCost( _fixedCost );
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFixedCost( 
		final double _fixedCost )
	{
		if (project.getCurrencyRateTable() != null)
		{
// TODO: make this work - psc1952 2012.01.10
//			final double rate = project.getCurrencyRateTable().calculateWeightedRateOverPeriod( getStart(), getEnd(), 
//				getEffectiveWorkCalendar() );

// convert to native currency
//			_fixedCost /= rate; 
		}

		((TaskSnapshot)getCurrentSnapshot()).setFixedCost( _fixedCost );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setFixedCostAccrual( 
		final int _fixedCostAccrual )
	{
		((TaskSnapshot)getCurrentSnapshot()).setFixedCostAccrual( _fixedCostAccrual );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setForward( 
		final boolean _forward )
	{
		myCurrentSchedule.setForward( _forward );
		restrictToValidConstraintType();
	}

	public void setHasKey( 
		final HasKeyImpl _value )
	{
		hasKey = _value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setId( 
		final long _id )
	{
		hasKey.setId( _id );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setIgnoreResourceCalendar( 
		final boolean _ignoreResourceCalendar )
	{
		((TaskSnapshot)getCurrentSnapshot()).setIgnoreResourceCalendar( _ignoreResourceCalendar );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setInSubproject( 
		final boolean _inSubproject )
	{
		inSubproject = _inSubproject;
	}

	public void setInvestmentMilestone( 
		final String _investmentMilestone )
	{
		Field f = Configuration.getFieldFromId( "Field.investmentMilestone" );

		if ((_investmentMilestone != null) && (_investmentMilestone.trim().length() > 0))
		{
			Task found = (Task)f.findFirstInCollection( _investmentMilestone, project.getTasks().collection() );

			if ((found != null) && (found != this))
			{
				String val = (String)f.getSelect().getKey( _investmentMilestone, this );
				throw new ParseException(Messages.getStringWithParam( "Text.milestoneInUse", new Object[]
						{
							val,
							found.getName()
						} ), 0);
			}

			setExternallyVisible( true );
		}

		getExtraFields().put( f.getAlternateId(), _investmentMilestone );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setLastSavedFinish( 
		final long _currendFinish )
	{
		lastSavedFinish = _currendFinish;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setLastSavedParentId( 
		final long _lastSavedParentId )
	{
		lastSavedParentId = _lastSavedParentId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setLastSavedPosistion( 
		final long _lastSavedPosistion )
	{
		lastSavedPosistion = _lastSavedPosistion;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setLastSavedStart( 
		final long _currentStart )
	{
		lastSavedStart = _currentStart;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setLevelingDelay( 
		final long _levelingDelay )
	{
		levelingDelay = _levelingDelay;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setLocal( 
		final boolean _local )
	{
		hasKey.setLocal( _local );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setMarkTaskAsMilestone( 
		final boolean _markTaskAsMilestone )
	{
		markTaskAsMilestone = _markTaskAsMilestone;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setMarkerStatus( 
		final boolean _markerStatus )
	{
		markerStatus = _markerStatus;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setMeAndAncestorsDirty( 
		final boolean _dirty )
	{
		setDirty( _dirty );

		if (wbsParentTask != null)
		{
			wbsParentTask.setMeAndAncestorsDirty( _dirty );
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setMustStartOn( 
		final long _mustStart )
	{
		windowEarlyStart = _mustStart;
		windowLateStart = _mustStart;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setNotes( 
		final String _notes )
	{
		notes = _notes;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setOwningProject( 
		final Project _owningProject )
	{
		owningProject = _owningProject;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setPersistedAssignments( 
		final List _persistedAssignments )
	{
		persistedAssignments = _persistedAssignments;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setPhysicalPercentComplete( 
		final double _physicalPercentComplete )
	{
		physicalPercentComplete = _physicalPercentComplete;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setPinned( 
		final boolean _pinned )
	{
		pinned = _pinned;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setPredecessors( 
		final String _predecessors )
		throws FieldParseException
	{
		getPredecessorList()
			.setAssociations( _predecessors,
			DependencyFormat.getInstance( AssociationFormatParameters.getInstance( this, true,
					Configuration.getFieldFromId( "Field.id" ), false, true ) ) );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setPriority( 
		final int _priority )
	{
		priority = _priority;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setProject( 
		final Project _project )
	{
		project = _project;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setProjectId( 
		final long _projectId )
	{
		projectId = _projectId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setResourceInitials( 
		final String _resourceInitials )
		throws FieldParseException
	{
		getAssignments().setAssociations( _resourceInitials, AssignmentFormat.getInstance( AssociationFormatParameters
			.getInstance( this, true, Configuration.getFieldFromId( "Field.initials" ), false, false ) ) );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setResourceNames( 
		final String _resourceNames )
		throws FieldParseException
	{
		getAssignments().setAssociations( _resourceNames, AssignmentFormat.getInstance( AssociationFormatParameters
			.getInstance( this, true, Configuration.getFieldFromId( "Field.name" ), true, true ) ) );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setResume( 
		final long _resume )
	{
		Assignment assignment;
		Iterator i = getAssignments().iterator();

		while (i.hasNext() == true)
		{
			assignment = (Assignment)i.next();
			assignment.setResume( _resume );
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setRoot( 
		final boolean _root )
	{
		if ((getUniqueId() != DataObject.SUMMARY_UNIQUE_ID) && _root)
		{
			setUniqueId( DataObject.SUMMARY_UNIQUE_ID );
		}

		//		else if (getUniqueId()==DataObject.SUMMARY_UNIQUE_ID&&!root)
		//			hasKey.renumber(hasKey.isLocal()); //not tested, Shoudn't occur anyway
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setScheduleConstraint( 
		final int _constraintType,
		final long _date )
	{
		constraintType = _constraintType;

		if (constraintType == ConstraintType.FNET)
		{
			setFinishNoEarlierThan( _date );
		}
		else if (constraintType == ConstraintType.FNLT)
		{
			setFinishNoLaterThan( _date );
		}
		else if (constraintType == ConstraintType.SNET)
		{
			setStartNoEarlierThan( _date );
		}
		else if (constraintType == ConstraintType.SNLT)
		{
			setStartNoLaterThan( _date );
		}
		else if (constraintType == ConstraintType.MSO)
		{
			setMustStartOn( _date );
		}
		else if (constraintType == ConstraintType.MFO)
		{
			setMustFinishOn( _date );
		}
		else
		{
			clearDateConstraints();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setScheduleConstraintAndUpdate( 
		final int _constraintType,
		final long _date )
	{
		setScheduleConstraint( _constraintType, _date );
		getDocument().fireUpdateEvent( this, this, Configuration.getFieldFromId( "Field.constraintType" ) );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSchedulingType( 
		final int _schedulingType )
	{
		((TaskSnapshot)getCurrentSnapshot()).setSchedulingType( _schedulingType );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSnapshot( 
		final Object _snapshotId,
		final DataSnapshot _snapshot )
	{
		if (snapshots == null)
		{
			snapshots = new SnapshotContainer(Settings.numBaselines());
		}

		snapshots.setSnapshot( _snapshotId, _snapshot );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setStart( 
		final long _start )
	{
		setStart( _start, null );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setStart( 
		long _start,
		final FieldContext _fieldContext )
	{
		if (getActualStart() != 0) // you can't change the start date of an in progress task
		{
			return;
		}

		_start = CalendarOption.getInstance().makeValidStart( _start, false );

		if ((_start != getStart()) && !Environment.isImporting())
		{
			long projectStart = project.getStart();

			if (projectStart > _start)
			{
				if (!Alert.okCancel( Messages.getString( "Message.allowTaskStartBeforeProjectStart" ) ))
				{
					return;
				}

				setScheduleConstraint( ConstraintType.SNLT, _start );
			}
			else
			{
				setScheduleConstraint( ConstraintType.SNET, _start );
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setStartNoEarlierThan( 
		final long _date )
	{
		windowEarlyStart = _date;
		windowLateStart = 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setStartNoLaterThan( 
		final long _date )
	{
		windowLateStart = _date;
		windowEarlyStart = 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setStop( 
		final long _stop )
	{
		setStop( _stop, true );
	}

	/**
	 * @param stop
	 */
	private void setStop( 
		long _stop,
		final boolean _limitToEnd )
	{
		if (_stop == getStop())
		{
			return;
		}

		_stop = DateTime.closestDate( _stop );

		if (_limitToEnd)
		{
			_stop = Math.min( _stop, getEnd() );
		}

		Iterator i = getAssignments().iterator();
		Assignment assignment;
		long computedActualStart = Long.MAX_VALUE;
		long assignmentActualStart;

		while (i.hasNext() == true)
		{
			assignment = (Assignment)i.next();
			assignment.setStop( _stop );
			assignmentActualStart = assignment.getActualStart();

			if ((assignmentActualStart != 0) && (assignmentActualStart < computedActualStart))
			{
				computedActualStart = assignmentActualStart;
			}
		}

		if (computedActualStart == Long.MAX_VALUE)
		{
			computedActualStart = 0;
		}

		setActualStart( computedActualStart );
		assignParentActualDatesFromChildren();

		// if % complete went down to 0, then the plan changed and need to recalculate all.
		if (computedActualStart == 0)
		{
			getDocument().fireUpdateEvent( this, this, Configuration.getFieldFromId( "Field.start" ) );
		}
		else
		{
			//TODO duplicate event
			//TODO in the case of progress update this event is useless since critical path runs after.
			project.fireScheduleChanged( this, ScheduleEvent.ACTUAL, this );
		}
	}

	private void setStopNoExtend( 
		long _stop )
	{
		//TODO figure out
		long start = getStart();

		if (_stop < start)
		{ 
			// don't allow completion before start
			setActualDuration( 0 );
			_stop = start;
		}
		else
		{
			long duration = getEffectiveWorkCalendar().compare( _stop, start, false );
			duration = Math.min( duration, getDurationMillis() ); // don't ever
																  // change finish

			setActualDuration( duration );
		}

//		scheduleWindow.setStop(stop);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSubprojectFile( 
		final String _sub )
	{
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSuccessors( 
		final String _successors )
		throws FieldParseException
	{
		getSuccessorList().setAssociations( _successors, DependencyFormat.getInstance( AssociationFormatParameters
			.getInstance( this, false, Configuration.getFieldFromId( "Field.id" ), false, true ) ) );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setTaskCalendar( 
		final WorkCalendar _taskCalendar )
	{
		if (workCalendar == _taskCalendar)
		{
			return;
		}

		CalendarService.getInstance().reassignCalendar( this, workCalendar, _taskCalendar );
		setWorkCalendar( _taskCalendar );
		invalidateAssignmentCalendars(); // assignments intersection calendars need to be recalculated
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setUniqueId( 
		final long _id )
	{
		hasKey.setUniqueId( _id );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setUniqueIdPredecessors( 
		final String _predecessors )
		throws FieldParseException
	{
		getPredecessorList().setAssociations( _predecessors, DependencyFormat.getInstance( AssociationFormatParameters
			.getInstance( this, true, Configuration.getFieldFromId( "Field.uniqueId" ), false, true ) ) );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setUniqueIdSuccessors( 
		final String _successors )
		throws FieldParseException
	{
		getSuccessorList().setAssociations( _successors, DependencyFormat.getInstance( AssociationFormatParameters
			.getInstance( this, false, Configuration.getFieldFromId( "Field.uniqueId" ), false, true ) ) );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setWbs( 
		final String _wbs )
	{
		wbs = _wbs;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setWbsChildrenNodes( 
		final Collection _wbsChildrenNodes )
	{
		//System.out.println(this + " setWbsChildrenNodes " + wbsChildrenNodes);
		wbsChildrenNodes = _wbsChildrenNodes;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setWbsParent( 
		final org.openproj.domain.task.Task _wbsParentTask )
	{
		wbsParentTask = _wbsParentTask;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setWindowEarlyFinish( 
		final long _windowEarlyFinish )
	{
		windowEarlyFinish = _windowEarlyFinish;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setWindowEarlyStart( 
		final long _windowEarlyStart )
	{
		windowEarlyStart = _windowEarlyStart;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setWindowLateFinish( 
		final long _windowLateFinish )
	{
		windowLateFinish = _windowLateFinish;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setWindowLateStart( 
		final long _windowLateStart )
	{
		windowLateStart = _windowLateStart;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setWork( 
		long _work,
		final FieldContext _context )
	{
		if (isActualsProtected() && (_work < getActualWork( _context )))
		{
			_work = getActualWork( _context );
		}

		if (FieldContext.hasInterval( _context ))
		{
			Iterator i = getAssignments().iterator();

			while (i.hasNext() == true)
			{
				Assignment assignment = (Assignment)i.next();
				assignment.setWork( _work, _context );
			}
		}
		else
		{
			setWork( _work );
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setWork( 
		long _work )
	{
		_work = Duration.millis( _work );

		if (hasLaborAssignment() && (_work < 60000))
		{
			_work *= Duration.timeUnitFactor( TimeUnit.HOURS );
		}

		long remainingWork = _work - getActualWork( null );
		getSchedulingRule().adjustRemainingWork( this, remainingWork, true );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setWorkCalendar( 
		final WorkCalendar _workCalendar )
	{
		workCalendar = _workCalendar;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean startsBeforeProject()
	{
		// special case for SNLT tasks that start before project
		return ((getConstraintType() == ConstraintType.SNLT) // this was changed from SNET - MSP mistakenly displays SNET for these tasks
		 &&getPredecessorList().isEmpty() && (getConstraintDate() < getOwningProject().getStart()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void touchBaseline( 
		final Object _numBaseline )
	{
		if (baselineTouched == null)
		{
			baselineTouched = new boolean[ Settings.numBaselines() ];
		}

		int baseline = (Integer)_numBaseline;
		baselineTouched[ baseline ] = true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateAssignment( 
		final Assignment _modified )
	{
		((TaskSnapshot)getCurrentSnapshot()).updateAssignment( _modified );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateCachedDuration()
	{
		setRawDuration( getDuration() );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateEndSentinel()
	{
		if (getSuccessorList().isEmpty())
		{ // if pred has no successors, tell end sentinel about it
			project.addEndSentinelDependency( this );
		}
		else
		{ // make sure not in sentinel's list
			project.removeEndSentinelDependency( this );
		}
	}

	private void updateEstimatedStatus()
	{
		Iterator i = wbsChildrenNodes.iterator();
		Object current;
		Task child;
		boolean childEstimated = false;

		while (i.hasNext() == true)
		{
			current = ((Node)i.next()).getValue();

			if (!(current instanceof Task))
			{
				continue;
			}

			child = (Task)current;
			childEstimated |= child.isEstimated();
		}

		setEstimated( childEstimated );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean updateProjectTask( 
		final long _date,
		final boolean _updateWorkAsCompleteThrough,
		final boolean _setFractionalPercentComplete )
	{
		long start = getStart();
		long end = getEnd();
		long completedDate = getStop();
		boolean updated = false;

		if (_updateWorkAsCompleteThrough)
		{
			if (_setFractionalPercentComplete)
			{
				if (completedDate != end)
				{
					// if task is not finished, adjust its completion.  This may actually reduce % complete
					setStop
					// if task is not finished, adjust its completion.  This may actually reduce % complete
					( _date );
					updated = true;
				}
			}
			else if (_date >= end)
			{ // if date is equal or later to end date of task, set its percent complete to 100%, otherwise do nothing
				setPercentComplete( 1.0 );
				updated = true;
			}
		}
		else
		{
			if (_date > start)
			{ // move remaining after status date
				moveRemainingToDate( _date );
				updated = true;
			}
		}

		if (updated)
		{
			recalculate( this );
			setDirty( true );
		}

		return updated;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateStartSentinel()
	{
		if (getPredecessorList().isEmpty())
		{ // if pred has no successors, tell end sentinel about it
			project.addStartSentinelDependency( this );
		}
		else
		{ // make sure not in sentinel's list
			project.removeStartSentinelDependency( this );
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean validateConstraints()
	{
		long little = WorkCalendar.MILLIS_IN_DAY * 10;

		if (!isDatelessConstraintType())
		{
			if ((windowEarlyStart < little) && (windowEarlyFinish < little) && (windowLateStart < little) &&
					(windowLateFinish < little))
			{
				setConstraintDate( 0L );
				ErrorLogger.log( "Repairing invalid constraints " + getId() + " " + getName() ); // under unknown circumstances, constraints are becoming invalid. this repairs them.
				setDirty( true );

				return true;
			}
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean wbsDescendentOf( 
		final org.openproj.domain.task.Task _potentialParentTask )
	{
		if (this == _potentialParentTask)
		{
			return true;
		}

		if (wbsParentTask == null)
		{
			return false;
		}

		return wbsParentTask.wbsDescendentOf( _potentialParentTask );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long work( 
		final long _start,
		final long _end )
	{
		if (isParentWithoutAssignments() == true)
		{
			return 0L;
		}

		return ((TaskSnapshot)getCurrentSnapshot()).work( _start, _end );
	}

	private void writeObject( 
		final ObjectOutputStream _stream )
		throws IOException
	{
		doWriteObject( _stream );
	}

	private abstract static class ResultClosure
		implements Closure
	{
		boolean result = false;
	}

	static final long serialVersionUID = 273898992929L;
	private static transient BarClosure myBarClosureInstance = new BarClosure();
	private static short CURRENT_VERSION = 3;
	private static final double COMPLETE_THRESHOLD = 0.9999D;
	private static long millisInWeek = 1000L * 60L * 60L * 24L * 7L;
	private transient org.openproj.domain.task.Task wbsParentTask = null;
	private transient Collection wbsChildrenNodes = null;
	private transient CustomFieldsImpl customFields = new CustomFieldsImpl();
	private transient HasDependencies dependencies;
	private transient HasKeyImpl hasKey;

	//	transient HashMap<Field,Object> delegatedFields = new HashMap<Field,Object>();
	private transient HashMapWithDirtyFlags extraFields = null;
	private List persistedAssignments;
	private transient Project owningProject;
	private transient Project project;
	private transient Resource delegatedTo = null;
	private transient Snapshottable snapshots;
	private String notes = "";
	private String wbs = "";
	private transient TaskSchedule myCurrentSchedule = null;
	private transient TaskSchedule myEarlySchedule;
	private transient TaskSchedule myLateSchedule;

	/********************************************************************************
	 * Calendars
	 ***********************************************************************************/
	private WorkCalendar workCalendar = null;
	private transient boolean[] baselineTouched = null;
	private transient boolean dirty;

	//	Schedule schedule = null;

	/** Indication of whether or not the duration of the task has only been estimated
	 *
	 * @serialField 
	 */
	private boolean estimated = true;
	
	private transient boolean external = false;
	private transient boolean inSubproject = false;
	private boolean markTaskAsMilestone = false;
	private transient boolean markerStatus = false;
	private transient boolean pinned = false;
	private double physicalPercentComplete = 0.0;
	private transient int calculationStateCount = 0;
	private int constraintType = ConstraintType.ASAP;
	private transient int debugDependencyOrder = -1;
	private int earnedValueMethod = EarnedValueMethodType.PERCENT_COMPLETE;

	/**
	 * @serialField 
	 */
	private ExpenseType expenseType = ExpenseType.NONE;

	private int priority = 500;
	private long actualStart = 0;
	private long deadline = 0;
	private transient long lastSavedFinish = 0L;
	private transient long lastSavedParentId = -1L;
	private transient long lastSavedPosistion = 0L;
	private transient long lastSavedStart = 0L;
	private long levelingDelay = 0L;
	private transient long projectId = 0;
	private long windowEarlyFinish = 0;
	private long windowEarlyStart = 0;
	private long windowLateFinish = 0;
	private long windowLateStart = 0;
	private short version = CURRENT_VERSION;
}
