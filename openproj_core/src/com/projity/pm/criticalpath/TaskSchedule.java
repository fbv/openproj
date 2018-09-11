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

import com.projity.datatype.Duration;
import com.projity.grouping.core.Node;
import com.projity.options.ScheduleOption;
import com.projity.pm.assignment.Assignment;
import com.projity.pm.dependency.Dependency;
import com.projity.pm.task.SubProj;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.openproj.domain.task.Task;

/**
 * 
 */
public final class TaskSchedule
	implements Cloneable
{
	public TaskSchedule()
	{
	}

	public TaskSchedule( 
		final Task _task,
		final int _type )
	{
		init( _task, _type );
		myStart = 0;
		myFinish = 0;
	}

	/**
	 * Calculate a task's dates and see if the critical path changes.  This means either: The task is currently critical, or the task becomes critical.
	 * This function is useful in seeing whether a backward pass is necessary.  The backward pass is only necessary when the CP is modified.
	 * @param honorRequiredDates
	 * @param boundary
	 * @return
	 */
	final boolean affectsCriticalPath( 
		final CalculationContext _context )
	{
		if (myTask.isOrWasCritical() == true)
		{
			return true;
		}

		calcStartAndFinish( _context ); // for parents, it will examine all children

		long newEnd = getEnd();
		long oppositeEnd = -getOppositeSchedule().getBegin();

//		System.out.println("Affects" + (oppositeEnd < newEnd) + " opposite " + new Date(oppositeEnd) + " new" + new Date(newEnd));
		return (oppositeEnd < newEnd);
	}

	public void assignDatesFromChildren( 
		final CalculationContext _context )
	{
		Collection children = myTask.getWbsChildrenNodes();

		if (children == null)
		{
			return;
		}

		long begin = Long.MAX_VALUE;
		long end = Long.MIN_VALUE;

		Iterator i = children.iterator();
		Task child;
		Object current;
		TaskSchedule childSchedule;
		boolean estimated = false;
		int t = myType;

		if ((_context != null) && (_context.pass == 3))
		{
			t = CURRENT;
		}

		//System.out.println( "assign from children top ass" + assign + " " + this );
		while (i.hasNext() == true)
		{
			current = ((Node)i.next()).getValue();

			if (!(current instanceof Task))
			{
				continue;
			}

			child = (Task)current;
			estimated |= child.isEstimated();

//			if (context !=  null && context.pass == 3 && child.isReverseScheduled()) {
//
//				childSchedule = child.getSchedule(-myType);
//				System.out.println("reverse " + child + " " + childSchedule);
//			} else
			childSchedule = child.getSchedule( t );

//			if (assign && child.isReverseScheduled())
//				childSchedule = childSchedule.getOppositeSchedule();
			long childBegin = childSchedule.getBegin();

			if (childBegin != 0)
			{
				begin = Math.min( begin, childBegin );
			}

			long childEnd = childSchedule.getEnd();

			if (childEnd != 0)
			{
				end = Math.max( end, childEnd );
			}
		}

		if ((begin != Long.MAX_VALUE) && (begin != 0)) // in case of invalid subproject having no children
		{
			setBegin( begin );
		}
		else
		{
			return;
		}

		if ((end != Long.MIN_VALUE) && (end != 0))
		{
			setEnd( end );
		}
		else
		{
			return;
		}

		//System.out.println( "begin is " + new Date( begin ) + " end " + new Date( end ) );

		long duration = myTask.getEffectiveWorkCalendar().compare( end, begin, false );
		duration = Duration.setAsEstimated( duration, estimated );
		((Task)myTask).setEstimated( estimated );
		setRawDuration( duration );
	}

	final void calcDates( 
		final CalculationContext _context )
	{
		long oldBegin = getBegin();
		long oldEnd = getEnd();
		long newBegin = 0L;
		long newEnd = 0L;
		final long needsCalculation = Dependency.NEEDS_CALCULATION;
		boolean unopenedSubproject = myTask.isSubproject() && !((SubProj)myTask).isValidAndOpen();
		boolean external = myTask.isExternal();

		if (external || myTask.isPinned())
		{ // external

			TaskSchedule currentSchedule = myTask.getCurrentSchedule();
			newBegin = currentSchedule.getBegin();
			newEnd = currentSchedule.getEnd();
			oldBegin = 0;
		}
		else if (!unopenedSubproject)
		{
			if (_context.taskReferenceType == PredecessorTaskList.TaskReference.PARENT_END)
			{
				assignDatesFromChildren( _context );
			}
			else
			{
				calcStartAndFinish( _context ); // for parents, it will examine all children
			}

			newBegin = getBegin();
			newEnd = getEnd();

			boolean reverseScheduled = myTask.isReverseScheduled();

			// if not just calculating early dates, check if reverse scheduled
			if (!_context.earlyOnly && reverseScheduled)
			{
				TaskSchedule oppositeSchedule = getOppositeSchedule();
				newBegin = -oppositeSchedule.getEnd();
				newEnd = -oppositeSchedule.getBegin();
			}

			if (_context.assign && !unopenedSubproject)
			{
				TaskSchedule currentSchedule = myTask.getCurrentSchedule();

				if (newBegin < 0)
				{
					currentSchedule.setStart( -newEnd );
					currentSchedule.setFinish( -newBegin );
					currentSchedule.setRemainingDependencyDate( -myRemainingDependencyDate );
				}
				else
				{
					currentSchedule.setStart( newBegin );
					currentSchedule.setFinish( newEnd );
					currentSchedule.setRemainingDependencyDate( myRemainingDependencyDate );
				}

				currentSchedule.setDependencyDate( myDependencyDate );

				// for parents, set current schedule's duration
				if (_context.taskReferenceType == PredecessorTaskList.TaskReference.PARENT_END)
				{
					//TODO this only needs to be done if advancement changed on a task
					currentSchedule.updateDurationFromDates(); // calculate duration based on parent start/end
					currentSchedule.myTask.assignActualDatesFromChildren();
				}

//	 			System.out.println(myTask.getName() + " Set current " + new Date(currentSchedule.getStart()) + " " + new Date(currentSchedule.getFinish()));
			}
		}

		if ((oldBegin == newBegin) && (oldEnd == newEnd))
		{
//			System.out.println("no change");
			if (!unopenedSubproject)
			{
				return;
			}
		}

		if (unopenedSubproject)
		{ 
			// need to put back old dates because we want the reverse pass to work right
			newBegin = oldBegin;
			newEnd = oldEnd;
		}

		Collection list = myTask.getDependencyList( !myForward );
		Task parent = myTask.getWbsParentTask();
		TaskSchedule parentSchedule = null;
		long parentEnd = 0;

		if (parent != null)
		{
			parentSchedule = parent.getSchedule( myType );
			parentEnd = parentSchedule.getEnd();
		}

		if (_context.taskReferenceType == PredecessorTaskList.TaskReference.PARENT_BEGIN)
		{
			if (oldBegin != newBegin)
			{ // if parent start (finish) changed, then all of its children need to me marked
				flagChildren();
				setDependencyDate( newBegin ); //This fixes a problem in incorrect propagation of constraints to children hk 16/8/05

//				 make sure that in second pass over this, the schedule will change so it will be marked in backward pass.  However, we don't want to lose
//				information - specificially whether this task affects its parent's task.  In case it does, it is marked with a special value (Dependency.NEEDS_CALCULATION)
//				This is a bit of a hack, but it's for optimization purposes.
				if (parentEnd == oldEnd)
				{
					setEnd( needsCalculation );
				}
				else
				{
					setEnd( 0 );
				}
			}

			return;
		}

		Dependency dependency;

		if (list.isEmpty())
		{
			if (!myTask.isExternal() && (myTask != _context.sentinel))
			{ // When the task is the sentinel, do nothing, otherwise find dependency and update it
				dependency = (Dependency)_context.sentinel.getDependencyList( myForward ).find( myForward, myTask ); // find sentinel's dependency concerning this task

				if (dependency != null)
				{ // tasks in a subproject won't have a sentinel dependency
					dependency.calcDependencyDate( myForward, newBegin, newEnd, false ); // calculate it to store off value
					_context.sentinel.setCalculationStateCount( _context.stateCount ); // need to process successor(predecessor) later on in pass
					_context.sentinel.getSchedule( myType ).setDependencyDate( needsCalculation ); //sentinel needs dependencies calculated - I assume more than one
				}
			}
		}
		else
		{
//			Go Thru Successors (Predecessors) and calculate a dependency date for them and mark them for further treatment.  There is an optimization here:
//			If the successor(pred) task only has one predecessor(succ), then just set its dependency date instead of calculating it.  This avoids reprocessing
//			the predecessor(successor) list of that task later on.  Since in most cases, a task has only one predecessor, this saves time.
			for (Iterator d = list.iterator(); d.hasNext();)
			{
				Task dependencyTask;
				TaskSchedule dependencyTaskSchedule;

				dependency = (Dependency)d.next();

				if (dependency.isDisabled())
				{
					continue;
				}

				dependencyTask = (Task)dependency.getTask( !myForward ); // get the successor(pred) task
				dependencyTaskSchedule = dependencyTask.getSchedule( myType );

				// if this task is the only predecessor(successor) for the successor(predecessor) task, avoid long calculation and just calculate the date, otherwise
				// flag the task for later calculation
				long dependencyCount = dependencyTask.getDependencyList( myForward ).size();

				long dep = newBegin; // by default (if no preds for example)

				if (dependencyCount > 0)
				{
					boolean useSooner = !dependencyTask.isWbsParent() && dependencyTask.hasDuration();
					dep = dependency.calcDependencyDate( myForward, newBegin, newEnd, useSooner ); // calculate it and store off value

					if (dependencyCount > 1) // can't just set date directly because more than one
					{
						dep = needsCalculation; // it will need to be calculated later
					}
				}

				dependencyTaskSchedule.setDependencyDate( dep );
				dependencyTask.setCalculationStateCount( _context.stateCount ); // need to process successor(predecessor) later on in pass
			}
		}

		// mark parent also if it is affected and isn't marked already
		if ((parent != null) && (parent.getCalculationStateCount() != _context.stateCount))
		{
			long parentBegin = parentSchedule.getBegin();

			if ((oldEnd == 0) || (oldEnd == Dependency.NEEDS_CALCULATION)) // in case a parent itself modifies its parent or this task has been invalidated (if it is the task modified)
			{
				parent.setCalculationStateCount( _context.stateCount ); // mark its parent
			}
			else if (((oldEnd != newEnd) && ((newEnd > parentEnd) || (oldEnd == parentEnd))) // if this task previously determined the parent end date, the parent will need to be recalculated
					 ||((oldBegin != newBegin) && ((newBegin < parentBegin) || (oldBegin == parentBegin))))
			{ // if this task previously determined parent start date
				parent.setCalculationStateCount( _context.stateCount ); // mark parent
			}
		}

		if (_context.pass == 1) // only mark during first pass.
		{
			myTask.setCalculationStateCount( _context.stateCount + 1 ); // signal that backward pass needs to be done on this
		}
	}

	/**
	 * During the forward pass, begin is early dates, during backward pass, it is late dates.
	 * When reverse scheduling, the backward pass is executed first, then the forward.
	 * The late schedule uses a trick: dates are returned as negative values.  This lets me to use the same min/max code.  Also
	 * the calendar code knows to reverse durations when the date is negative.
	 * @param boundary
	 * @param honorRequiredDates
	 * @param early
	 * @return
	 */
	private void calcStartAndFinish( 
		final CalculationContext _context )
	{
		long begin = getBeginDependency();
		long dependencyBegin = begin;
		Task parent = myTask.getWbsParentTask();

		//boolean useSooner = (myForward != myTask.hasDuration()); // shortcut: if forward and is milestone, use sooner, otherwise later.  And conversely, if reverse and isn't milestone, use sooner, othewise later
		boolean useSooner = !myTask.hasDuration();

		if (parent != null)
		{
			TaskSchedule parentSchedule = parent.getSchedule( myType );
			long parentDependency = parentSchedule.getBeginDependency();
			long parentWindow = parentSchedule.getWindowBegin();

			if ((parentDependency == 0) || ((parentWindow != 0) && (parentWindow > parentDependency)))
			{
				parentDependency = parentWindow;
			}

			// in case where parent determines start time, make sure that this
			// task starts either at day end if milesetone, or at next working
			// day otherwise if parent is at day end
			if ((parentDependency != 0) && ((begin == 0) || (parentDependency > begin)))
			{
				begin = myTask.getEffectiveWorkCalendar().add( parentDependency, 0, useSooner );
			}
		}

		if (myTask.isInSubproject())
		{
			begin = Math.max( begin, (_context.myForward == true)
				? myTask.getOwningProject().getStartConstraint()
				: (-myTask.getOwningProject().getEnd()) );
		}

		// Soft constraints
		long windowBegin = getWindowBegin();

		// Make sure the task starts after its early start window date. This
		// is a soft constraint during forward pass.
		if (windowBegin != 0)
		{
			if (begin == 0)
			{
				begin = windowBegin;
			}
			else if (windowBegin < begin)
			{
				if (myTask.startsBeforeProject()) // case of task starting before project start but has SNET constraint
				{
					begin = windowBegin;
				}
			}
			else
			{
				begin = windowBegin;
			}
		}

		// For FNET
		long windowEnd = getWindowEnd();

		if (windowEnd != 0)
		{
			if (begin == 0)
			{
				begin = Long.MIN_VALUE;
			}

			begin = Math.max( begin, myTask.calcOffsetFrom( windowEnd, windowEnd, false, false, useSooner ) );

//			System.out.println("Applying FNET " + myTask + " " + d(windowEnd) + " begin is now " + d(begin));
		}

		// Hard constraints
		long softBegin = begin;
		long hardBegin = 0;

		if (_context.honorRequiredDates)
		{
			// If honoring required dates, check the hard constraint that the
			// task is finished by its late finish date.
			// Note that currently late finish has priority over early start.
			long oppositeEnd = -getOppositeSchedule().getWindowBegin(); // For FNLT

			if (oppositeEnd != 0)
			{
				if (begin == 0)
				{
					begin = Long.MAX_VALUE;
				}

				begin = Math.min( begin, myTask.calcOffsetFrom( oppositeEnd, myDependencyDate, false, false, useSooner ) );

//				System.out.println("Applying FNLT " + myTask + " " + d(oppositeEnd) + " begin is now " + d(begin));
			}

			// For SNLT
			long oppositeBegin = -getOppositeSchedule().getWindowEnd();

			if (oppositeBegin != 0)
			{
				if (begin == 0)
				{
					begin = Long.MAX_VALUE;
				}

				begin = Math.min( begin, oppositeBegin );

//				System.out.println("Applying SNLT " + myTask + " " + d(oppositeBegin) + " begin is now " + d(begin));
			}

			hardBegin = begin;
		}

		if (begin == 0)
		{
			if (!myTask.isWbsParent()) // if no constraints at all
			{
				begin = _context.boundary;
			}
		}

		if (myTask.isSubproject())
		{ // subprojects can't start before their project start

			SubProj subProj = (SubProj)myTask;

			if (!subProj.isValidAndOpen())
			{
				return;
			}

			if ((myTask.getPredecessorList().size() == 0) && (myTask.getConstraintDate() == 0))
			{
				return;
			}

			begin = Math.max( begin, (_context.myForward == true)
				? subProj.getSubproject().getStartConstraint()
				: (-subProj.getSubproject().getEnd()) );
		}

		long levelingDelay = myTask.getLevelingDelay();

		if (Duration.millis( levelingDelay ) != 0)
		{
			begin = myTask.getEffectiveWorkCalendar().add( begin, levelingDelay, useSooner );
		}

		long remainingBegin = begin;

		if (_context.myForward == true)
		{
			long actualStart = myTask.getActualStart();

			if (actualStart != 0)
			{
				begin = actualStart;

				if (ScheduleOption.getInstance().isKeepTasksContiguous()) // dont split if contiguous
				{
					remainingBegin = begin;
				}
			}
		}

		if (myForward == _context.myForward)
		{
			setRemainingDependencyDate( remainingBegin ); // the date which predecessors push the task to start at.  Actuals can override this
		}

		setBegin( begin );

		long end = ((Task)myTask).calcOffsetFrom( begin, remainingBegin, true, true, true );
		setEnd( end );
	}

	@Override
	public Object clone()
	{
		try
		{
			return super.clone();
		}
		catch (CloneNotSupportedException e)
		{
			throw new InternalError();
		}
	}

	public Object cloneWithTask( 
		final Task _task )
	{
		TaskSchedule ts = (TaskSchedule)clone();
		ts.setTask( _task );
		ts.invalidate();

		return ts;
	}

	public void copyDatesAfterClone( 
		final TaskSchedule _from )
	{
		myStart = _from.myStart;
		myFinish = _from.myFinish;
		myDependencyDate = _from.myDependencyDate;
	}

//
//	void copyDatesFrom(TaskSchedule from) {
//		myStart = from.myStart;
//		myFinish = from.myFinish;
//		if (myTask.isWbsParent())
//			myRawDuration = from.myRawDuration;
//		myDependencyDate = from.myDependencyDate;
//		myRemainingDependencyDate = from.myRemainingDependencyDate;
//	}
	
	/**
	 * Function used for tracing dates when debugging
	 * @param l date either positive or negative
	 * @return a date using the absolute value of the input
	 */
	public static Date d( 
		final long _l )
	{
		return new Date( Math.abs( _l ) );
	}

	/** Call init to complete initialization
	 * 
	 * @param _stream
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException 
	 */
	public static TaskSchedule deserialize( 
		final ObjectInputStream _stream )
		throws IOException, 
			ClassNotFoundException
	{
		TaskSchedule schedule = new TaskSchedule();
		schedule.setPercentComplete( _stream.readDouble() );
		schedule.setRawDuration( _stream.readLong() );
		schedule.setStart( _stream.readLong() );
		schedule.setFinish( _stream.readLong() );

		return schedule;
	}

	public void dump()
	{
		System.out.println( "Task " + myTask + " schedule " + myType + " myStart " + new Date(myStart) + " finish " + new Date(myFinish) );
	}

	private void flagChildren()
	{
		int stateCount = myTask.getCalculationStateCount();
		Collection children = myTask.getWbsChildrenNodes();

		if (children == null)
		{
			return;
		}

		Object current;
		Iterator i = children.iterator();

		while (i.hasNext())
		{
			current = ((Node)i.next()).getValue();

			if (!(current instanceof Task))
			{
				continue;
			}

			((Task)current).setCalculationStateCount( stateCount ); // mark parent
		}
	}

	public long getBegin()
	{
		return (myForward == true)
			? myStart
			: (-myFinish);
	}

	public long getBeginDependency()
	{
		if (myDependencyDate == Dependency.NEEDS_CALCULATION)
		{
			myDependencyDate = calcDependencyDate();
		}

		return myDependencyDate;
	}

	public final long getDependencyDate()
	{
		return myDependencyDate;
	}

	public long getEnd()
	{
		return (myForward == true)
			? myFinish
			: (-myStart);
	}

	public final long getFinish()
	{
		return myFinish;
	}

	public TaskSchedule getOppositeSchedule()
	{
		return myTask.getSchedule( (myForward == true)
			? LATE
			: EARLY );
	}

	/**
	 * @return Returns the percentComplete.
	 */
	public final double getPercentComplete()
	{
		return myPercentComplete;
	}

	/**
	 * @return Returns the rawDuration.
	 */
	public final long getRawDuration()
	{
		return myRawDuration;
	}

	/**
	 * @return Returns the remainingDependencyDate.
	 */
	public final long getRemainingDependencyDate()
	{
		return myRemainingDependencyDate;
	}

	public final long getStart()
	{
		return myStart;
	}

	public long getWindowBegin()
	{
		return (myForward == true)
			? myTask.getWindowEarlyStart()
			: (-myTask.getWindowLateFinish());
	}

	public long getWindowEnd()
	{
		return (myForward == true)
			? myTask.getWindowEarlyFinish()
			: (-myTask.getWindowLateStart());
	}

	public void init( 
		final Task _task,
		final int _type )
	{
		myTask = _task;
		myType = _type;

		if (myType == EARLY)
		{
			myForward = true;
		}
		else if (myType == LATE)
		{
			myForward = false;
		}

		myDependencyDate = Dependency.NEEDS_CALCULATION;
		invalidate();
	}

	public void initSerialized( 
		final Task _task,
		final int _type )
	{
		myTask = _task;
		myType = _type;

		if (myType == EARLY)
		{
			myForward = true;
		}
		else if (myType == LATE)
		{
			myForward = false;
		}
	}

	public final void invalidate()
	{
		if ((myTask != null) && myTask.isSubproject() && !((SubProj)myTask).isValidAndOpen()) // until they are open, don't touch subprojects
		{
			return;
		}

		myStart = 0;
		myFinish = 0;
		myDependencyDate = Dependency.NEEDS_CALCULATION;
	}

	final void invalidateDependencyDate()
	{
		myDependencyDate = Dependency.NEEDS_CALCULATION;
	}

	/**
	 * @return Returns the forward.
	 */
	public final boolean isForward()
	{
		return myForward;
	}

	private final boolean isLate()
	{
		return myType == LATE;
	}

	public void serialize( 
		final ObjectOutputStream _s )
		throws IOException
	{
		_s.writeDouble( myPercentComplete );
		_s.writeLong( myRawDuration );
		_s.writeLong( myStart );
		_s.writeLong( myFinish );
	}

	public void setBegin( 
		final long _begin )
	{
		if (myForward == true)
		{
			myStart = _begin;
		}
		else
		{
			myFinish = -_begin;
		}
	}

	public final void setDependencyDate( 
		final long _dependencyDate )
	{
		myDependencyDate = _dependencyDate;
	}

	public void setEnd( 
		final long _end )
	{
		if (myForward == true)
		{
			myFinish = _end;
		}
		else
		{
			myStart = -_end;
		}
	}

	public final void setFinish( 
		final long _finish )
	{
		myFinish = _finish;
	}

	public void setForward( 
		final boolean _forward )
	{
		if (myForward != _forward)
		{
			myForward = _forward;

			long s = myStart;
			myStart = -myFinish;
			myFinish = -s;
			myDependencyDate = -myDependencyDate;
			myRemainingDependencyDate = -myRemainingDependencyDate;
		}
	}

	/** Assign the <i>percentage complete</i> attribute.
	 * 
	 * @param _value used as the source of the assignment.
	 */
	public final void setPercentComplete( 
		final double _value )
	{
		myPercentComplete = _value;
	}

	/** Assign the <i>raw duration</i> attribute.
	 * 
	 * @param _value used as the source of the assignment.
	 */
	public final void setRawDuration( 
		final long _value )
	{
		myRawDuration = _value;
	}

	/** Assign the <i>remaining dependency date</i> attribute.
	 * 
	 * @param _value used as the source of the assignment.
	 */
	public final void setRemainingDependencyDate( 
		final long _value )
	{
		myRemainingDependencyDate = _value;
	}

	public final void setStart( 
		final long _start )
	{
		// JGao 8/25/2009 Need to adjust all personal contours of all assignments if new start is earlier
		// JGao 9/27/2009 Need to adjust work contour of all assignments that have protected actual work. Otherwise, the actual work
		// will not be preserved on the same date range.
		if (_start < myStart)
		{
			Assignment assignment;
			Iterator i = myTask.getAssignments().iterator();

			while (i.hasNext())
			{
				assignment = (Assignment)i.next();

				if (!assignment.isDefault() && (assignment.getPercentComplete() > 0) &&
						assignment.getTask().getProject().isActualsProtected())
				{
					assignment.adjustWorkContourForEarlierTaskStart( _start, myStart );
				}
			}
		}

		myStart = _start;
		//System.out.println( "TaskSchedule.setStart(): _start = " + new Date( _start ) );
	}

	public void setTask( 
		final Task _task )
	{
		myTask = _task;
	}

	@Override
	public String toString()
	{
		return "Task " + myTask + " schedule " + myType + " begin " + new Date( getBegin() ) + " end " + new Date( getEnd() ) +
		" myStart " + new Date(myStart) + " finish " + new Date( myFinish ) + " forward " + myForward;
	}

	/**
	 * Calculate the date which predecessors(successors) push this task to start by looping thru all of its predecesors(succ) 
	 * and choosing the max value
	 * @return max date
	 */
	long calcDependencyDate()
	{
		long result = 0;
		Dependency dependency;
		long current;
		Collection list = myTask.getDependencyList( myForward );

		for (Iterator i = list.iterator(); i.hasNext();)
		{
			dependency = (Dependency)i.next();

			if (dependency.isDisabled())
			{
				continue;
			}

			current = dependency.getDate( myForward );

			if (result == 0)
			{
				result = current;
			}
			else
			{
				result = Math.max( result, current );
			}
		}

		setDependencyDate( result );

		return result;
	}

	private void updateDurationFromDates()
	{
		setRawDuration( myTask.getEffectiveWorkCalendar().compare( getFinish(), getStart(), false ) );
	}

	/**
	 * Structure used to store variables related to the pass
	 */
	static class CalculationContext
	{
		@Override
		public String toString()
		{
			return ToStringBuilder.reflectionToString( this );
		}

		Task sentinel;
		boolean assign;
		boolean earlyOnly;
		boolean myForward;
		boolean honorRequiredDates;
		int pass;
		int scheduleType;
		int stateCount;
		int taskReferenceType;
		long boundary;
	}

	public static final int CURRENT = 0;
	public static final int EARLY = -1;
	public static final int LATE = 1;

	// Calculated fields that are transient
	//TODO don't bother serializing these.  When I make them transient, the program hangs
	private Task myTask;
	private boolean myForward = true;

	//Persisted fields
	private double myPercentComplete = 0D;
	private int myType;
	private long myDependencyDate = Dependency.NEEDS_CALCULATION;
	private long myFinish;

	// these are calculated, but are persisted anyway for reporting
	private long myRawDuration;
	private long myRemainingDependencyDate = 0;
	private long myStart;
}
