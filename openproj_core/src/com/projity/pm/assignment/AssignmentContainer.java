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
package com.projity.pm.assignment;

import com.projity.algorithm.ReverseQuery;
import com.projity.algorithm.TimeIteratorGenerator;
import com.projity.algorithm.buffer.CalculatedValues;
import com.projity.algorithm.buffer.IntervalCallback;
import com.projity.algorithm.buffer.NonGroupedCalculatedValues;

import com.projity.association.AssociationList;

import com.projity.field.FieldContext;

import com.projity.functor.CollectionVisitor;

import com.projity.options.ScheduleOption;

import com.projity.pm.calendar.WorkCalendar;
import com.projity.pm.criticalpath.TaskSchedule;
import com.projity.pm.resource.Resource;
import com.projity.pm.scheduling.SchedulingType;
import com.projity.pm.time.HasStartAndEnd;
import com.projity.pm.time.MutableInterval;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.functors.TruePredicate;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.openproj.domain.task.Task;

/**
 * Implementation of class which contains assignments
 */
public class AssignmentContainer
	implements HasAssignments,
		HasTimeDistributedData,
		Serializable,
		Cloneable
{
	public AssignmentContainer()
	{
		myAssignments = new AssociationList();
	}

	/**
	 * Copy constructor: It does a deep copy of assignments
	 * @param from
	 */
	private AssignmentContainer( 
		AssignmentContainer from )
	{
		this();

		Iterator i = from.myAssignments.iterator();

		while (i.hasNext())
		{
			myAssignments.add( new Assignment((Assignment)i.next()) );
		}
	}

	public AssignmentContainer( 
		Collection details )
	{
		this();

		Iterator i = details.iterator();

		while (i.hasNext())
		{
			myAssignments.add( new Assignment((AssignmentDetail)i.next()) );
		}
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#actualCost(long, long)
	 */
	@Override
	public double actualCost( 
		long start,
		long end )
	{
		return TimeDistributedDataConsolidator.actualCost( start, end, childrenToRollup() );
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#actualFixedCost(long, long)
	 */
	@Override
	public double actualFixedCost( 
		long start,
		long end )
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#actualWork(long, long)
	 */
	@Override
	public long actualWork( 
		long start,
		long end )
	{
		return TimeDistributedDataConsolidator.actualWork( start, end, childrenToRollup(), true );
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.costing.EarnedValueValues#acwp(long, long)
	 */
	@Override
	public double acwp( 
		long start,
		long end )
	{
		return TimeDistributedDataConsolidator.acwp( start, end, childrenToRollup() );
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasAssignments#addAssignment(com.projity.pm.assignment.Assignment)
	 */
	@Override
	public void addAssignment( 
		Assignment assignment )
	{
		myAssignments.add( assignment );
		Collections.sort( myAssignments );
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.costing.EarnedValueValues#bac(long, long)
	 */
	@Override
	public double bac( 
		long start,
		long end )
	{
		return TimeDistributedDataConsolidator.bac( start, end, childrenToRollup() );
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#cost(long, long)
	 */
	@Override
	public double baselineCost( 
		long start,
		long end )
	{
		return TimeDistributedDataConsolidator.baselineCost( start, end, childrenToRollup() );
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#cost(long, long)
	 */
	@Override
	public long baselineWork( 
		long start,
		long end )
	{
		return TimeDistributedDataConsolidator.baselineWork( start, end, childrenToRollup(), true );
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.costing.EarnedValueValues#bcwp(long, long)
	 */
	@Override
	public double bcwp( 
		long start,
		long end )
	{
		return TimeDistributedDataConsolidator.bcwp( start, end, childrenToRollup() );
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.costing.EarnedValueValues#bcws(long, long)
	 */
	@Override
	public double bcws( 
		long start,
		long end )
	{
		return TimeDistributedDataConsolidator.bcws( start, end, childrenToRollup() );
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#collectIntervalGenerators(java.lang.Object, java.util.Collection)
	 */
	@Override
	public void buildReverseQuery( 
		ReverseQuery reverseQuery )
	{
		Iterator i = myAssignments.iterator();
		Assignment assignment;

		while (i.hasNext())
		{
			assignment = (Assignment)i.next();

			if (assignment.isDefault() && !reverseQuery.isAllowDefaultAssignments())
			{
				continue;
			}

			assignment.buildReverseQuery( reverseQuery );
		}
	}

	/**
	 * Compute the sum of active assignment durations.  If there are multiple assignments, then
	 * the calendar time of the union of active periods is used, otherwise, if just one assignment
	 * (which could be the default assignment), use the assignment duration
	 * @param workCalendar
	 * @return
	 */
	@Override
	public long calcActiveAssignmentDuration( 
		final WorkCalendar _workCalendar )
	{
		final AssociationList assignments = getAssignments();

		// Most of the time there is just one assignment. If that's the case, use the assignment duration
		if (assignments.size() == 1)
		{
			return ((Assignment)assignments.getFirst()).getDurationMillis();
		}

		final AssignmentDurationSummer summer = new AssignmentDurationSummer(_workCalendar);
		forEachWorkingInterval( summer, false, _workCalendar );

		return summer.getSum();
	}

	@Override
	public void calcDataBetween( 
		final TimeDistributedConstants _type,
		TimeIteratorGenerator generator,
		CalculatedValues values )
	{
		Iterator i = getAssignments().iterator();

		while (i.hasNext())
		{
			((Assignment)i.next()).calcDataBetween( _type, generator, values );
		}
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#childrenToRollup()
	 */
	@Override
	public Collection childrenToRollup()
	{
		return myAssignments;
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

	public Object cloneWithResource( 
		Resource resource )
	{
		AssignmentContainer clone = (AssignmentContainer)clone();
		clone.myAssignments = new AssociationList();

		Iterator i = myAssignments.iterator();

		while (i.hasNext())
		{
			clone.myAssignments.add( ((Assignment)i.next()).cloneWithResource( resource ) );
		}

		return clone;
	}

	/**
	 * @param schedule
	 * @return
	 */

	//	public HasAssignments cloneWithSchedule(TaskSchedule currentSchedule) {
	//		return cloneWithSchedule(currentSchedule,null);
	//	}
	//	public HasAssignments cloneWithSchedule(TaskSchedule currentSchedule,Collection details) {
	//		HasAssignmentsImpl newOne;
	//		if (details==null) newOne= new HasAssignmentsImpl(this);
	//		else newOne= new HasAssignmentsImpl(details);
	//		newOne.setScheduleForAssignments(currentSchedule);
	//		return newOne;
	//	}
	public HasAssignments cloneWithSchedule( 
		TaskSchedule currentSchedule )
	{
		AssignmentContainer newOne = new AssignmentContainer(this);
		newOne.setScheduleForAssignments( currentSchedule );

		return newOne;
	}

	public Object cloneWithTask( 
		Task task )
	{
		AssignmentContainer clone = (AssignmentContainer)clone();
		clone.myAssignments = new AssociationList();

		//TODO doesn't work when it's copied between projects
		Iterator i = myAssignments.iterator();

		while (i.hasNext())
		{
			clone.myAssignments.add( ((Assignment)i.next()).cloneWithTask( task ) );

//			clone.myAssignments.add(((Assignment)i.next()).cloneWithResourceAndTask(ResourceImpl.getUnassignedInstance(),task));
			//break;
		}

//		Iterator i = myAssignments.iterator();
//		while (i.hasNext()) {
//			clone.myAssignments.add(((Assignment)i.next()).cloneWithTask(task));
//		}
		
		return clone;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#cost(long, long)
	 */
	@Override
	public double cost( 
		long start,
		long end )
	{
		return TimeDistributedDataConsolidator.cost( start, end, childrenToRollup() );
	}

	//very deep copy of assignments contrary to copy constructor which doesn't clone assigments' detail
	public HasAssignments deepCloneWithTask( 
		Task task )
	{ //TODO doesn't

		AssignmentContainer newOne = (AssignmentContainer)cloneWithTask( task );

		return newOne;
	}

	public static List extractOppositeList( 
		List list,
		boolean leftObject )
	{
		Iterator i = list.iterator();
		ArrayList assignments = new ArrayList();

		while (i.hasNext())
		{ // go thru tasks or resources

			Object object = i.next();

			if (!(object instanceof HasAssignments))
			{
				continue; //TODO currently getting voidNodeImpl's.  This should go away when fixed
			}

			HasAssignments hasAssignments = (HasAssignments)object;
			assignments.addAll( hasAssignments.getAssignments() );
		}

		return AssociationList.extractDistinct( assignments, leftObject );
	}

	/**
	 * Finds an assignment given a resource
	 */
	@Override
	public Assignment findAssignment( 
		Resource resource )
	{
		Iterator i = myAssignments.iterator();
		Assignment result = null;

		while (i.hasNext())
		{
			result = (Assignment)i.next();

			if (result.getResource() == resource)
			{
				return result;
			}
		}

		return null;
	}

	/**
	 * Finds an assignment given a task
	 */
	@Override
	public Assignment findAssignment( 
		Task task )
	{
		Iterator i = myAssignments.iterator();
		Assignment result = null;

		while (i.hasNext())
		{
			result = (Assignment)i.next();

			if (result.getTask() == task)
			{
				return result;
			}
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#fixedCost(long, long)
	 */
	@Override
	public double fixedCost( 
		long start,
		long end )
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public static Closure forAllAssignments( 
		Closure visitor,
		Predicate filter )
	{
		return new CollectionVisitor(visitor, filter)
			{
				@Override
				protected final Collection getCollection( 
					Object arg0 )
				{
					return ((HasAssignments)arg0).getAssignments();
				}
			};
	}

	public static Closure forAllAssignments( 
		Closure visitor )
	{
		return forAllAssignments( visitor, TruePredicate.INSTANCE );
	}

	public void forEachInterval( 
		Closure<HasStartAndEnd> x_visitor,
		TimeDistributedConstants _type,
		WorkCalendar _workCalendar )
	{
		NonGroupedCalculatedValues calculatedValues = new NonGroupedCalculatedValues( false, 0 );
		ListIterator i = myAssignments.listIterator();

		while (i.hasNext() == true)
		{ 
			// add in all child groups
			final Assignment assignment = (Assignment)i.next();
			// use this assignments cal because it might work on off calendar time
			myBarCallback.setWorkCalendar( assignment.getEffectiveWorkCalendar() ); 
			assignment.calcDataBetween( _type, null, calculatedValues );
		}

		calculatedValues.makeContiguousNonZero( myBarCallback, _workCalendar );

		//calculatedValues.dump();
	}

	@Override
	public void forEachWorkingInterval( 
		final Closure<HasStartAndEnd> _visitor,
		final boolean _mergeWorking,
		final WorkCalendar _workCalendar )
	{
		// ACTUAL_WORK
		myBarCallback.initialize( _workCalendar, _visitor, true );
		forEachInterval( _visitor, TimeDistributedConstants.ACTUAL_WORK, _workCalendar );

/* if the splitting should be at latest bar use this code
		myBarCallback.finish();
		myBarCallback.initialize( _workCalendar, _visitor, false );
*/
		
		// REMAINING_WORK
		myBarCallback.initialize( _workCalendar, _visitor, true );
		forEachInterval( _visitor, TimeDistributedConstants.REMAINING_WORK, _workCalendar );
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasAssignments#getAssignments()
	 */
	@Override
	public AssociationList getAssignments()
	{
		return myAssignments;
	}

	@Override
	public long getEarliestAssignmentStart()
	{
		long result = Long.MAX_VALUE;
		Iterator i = myAssignments.iterator();

		while (i.hasNext())
		{
			result = Math.min( result, ((Assignment)i.next()).getStart() );
		}

		return result;
	}

	/**
	 * @return Returns the schedulingRule.
	 */
	@Override
	public int getSchedulingType()
	{
		return schedulingRule;
	}

	@Override
	public boolean hasActiveAssignment( 
		long start,
		long end )
	{
		Iterator i = myAssignments.iterator();
		Assignment assignment;

		while (i.hasNext())
		{
			assignment = (Assignment)i.next();

			if (assignment.isActiveBetween( start, end ))
			{
				return true;
			}
		}

		return false;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasAssignments#hasLaborAssignment()
	 */
	@Override
	public boolean hasLaborAssignment()
	{
		Iterator i = myAssignments.iterator();

		while (i.hasNext())
		{
			if (((Assignment)i.next()).isLabor())
			{
				return true;
			}
		}

		return false;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasAssignments#invalidateAssignmentCalendars()
	 */
	@Override
	public void invalidateAssignmentCalendars()
	{
		Iterator i = myAssignments.iterator();

		while (i.hasNext())
		{
			((Assignment)i.next()).invalidateAssignmentCalendar();
		}
	}

	/**
	 * @return Returns the effortDriven.
	 */
	@Override
	public boolean isEffortDriven()
	{
		return effortDriven;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#isLabor()
	 */
	@Override
	public boolean isLabor()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isReadOnlyEffortDriven( 
		FieldContext fieldContext )
	{
		return getSchedulingType() == SchedulingType.FIXED_WORK;
	}

	private void readObject( 
		ObjectInputStream s )
		throws IOException, ClassNotFoundException
	{
		s.defaultReadObject();
		myAssignments = new AssociationList();
	}

	@Override
	public long remainingWork( 
		long start,
		long end )
	{
		return TimeDistributedDataConsolidator.remainingWork( start, end, childrenToRollup(), true );
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasAssignments#removeAssignment(com.projity.pm.assignment.Assignment)
	 */
	@Override
	public void removeAssignment( 
		Assignment assignment )
	{
		myAssignments.remove( assignment );
	}

	/**
	 * @param effortDriven The effortDriven to set.
	 */
	@Override
	public void setEffortDriven( 
		boolean effortDriven )
	{
		this.effortDriven = effortDriven;
	}

	private void setScheduleForAssignments( 
		TaskSchedule currentSchedule )
	{
		Iterator i = myAssignments.iterator();
		Assignment assignment;

		while (i.hasNext())
		{
			assignment = (Assignment)i.next();
			assignment.setTaskSchedule( currentSchedule );
			assignment.convertToBaselineAssignment( false );
		}
	}

	/**
	 * @param schedulingType The schedulingRule to set.
	 */
	@Override
	public void setSchedulingType( 
		int schedulingType )
	{
		this.schedulingRule = schedulingType;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasAssignments#updateAssignment(com.projity.pm.assignment.Assignment)
	 */
	@Override
	public void updateAssignment( 
		Assignment modified )
	{
		ListIterator i = myAssignments.listIterator();
		Assignment current = null;

		while (i.hasNext())
		{
			current = (Assignment)i.next();

			if ((current.getTask() == modified.getTask()) && (current.getResource() == modified.getResource()))
			{
				i.set( modified ); // replace current with new one

				break;
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#work(long, long)
	 */
	@Override
	public long work( 
		long start,
		long end )
	{
		return TimeDistributedDataConsolidator.work( start, end, childrenToRollup(), true );
	}

	private void writeObject( 
		ObjectOutputStream s )
		throws IOException
	{
		s.defaultWriteObject();
	}

	/**
	 *
	 */
	private class AssignmentDurationSummer
		implements Closure<HasStartAndEnd>
	{
		AssignmentDurationSummer( 
			final WorkCalendar _workCalendar )
		{
			myWorkCalendar = _workCalendar;
			mySum = 0;
		}

		@Override
		public void execute( 
			final HasStartAndEnd _interval )
		{
//			HasStartAndEnd interval = (HasStartAndEnd)arg0;
			mySum += myWorkCalendar.compare( _interval.getEnd(), _interval.getStart(), false );
		}

		public long getSum()
		{
			return mySum;
		}

		private WorkCalendar myWorkCalendar;
		private long mySum;
	}

	private static class BarSeriesCallback
		implements IntervalCallback
	{
		@Override
		public void add( 
			int _index,
			long _start,
			long _end,
			double _value )
		{
			if (_value <= ALMOST_ZERO)
			{ 
				// because of rounding errors, treat 0 as something very small
				if (myWorkCalendar.compare( _end, _start, false ) == 0)
				{
					return;
				}

				if (myBarStart > 0)
				{
					_start = myWorkCalendar.adjustInsideCalendar( _start, true );
					executeVisitor( myBarStart, _start );
				}
			}
			else
			{
				if (myBarStart == 0)
				{
//hk				barStart = start;
					myBarStart = myWorkCalendar.adjustInsideCalendar( _start, false );
				}

				if (_index == 0)
				{ 
					// last bar, must draw
					_end = myWorkCalendar.adjustInsideCalendar( _end, true );
					executeVisitor( myBarStart, _end );

//					System.out.println("last bar " + new Date(start) + " " + new Date(end));
				}
			}
		}

		private void executeVisitor( 
			long _start,
			long _end )
		{
			// prevent overlap in case of multiple assignments that do not have same advancement
			_start = Math.max( _start, myPreviousEnd ); 

			if (_start > _end)
			{
				return;
			}

			myInterval.setStart( _start );
			myInterval.setEnd( _end );

			myPreviousEnd = _end;

			//System.out.println("bar " + new Date(start) + " " + new Date(end));
			myVisitor.execute( myInterval );
			myBarStart = 0;
		}

		private void finish()
		{
			if (myBarStart != 0)
			{
//				System.out.println("finishing bar " + new Date(barStart) + " " + new Date(previousEnd));
				executeVisitor( myBarStart, myPreviousEnd );
			}

			myBarStart = 0;
		}

		private void initialize( 
			WorkCalendar _workCalendar,
			Closure<HasStartAndEnd> _visitor,
			boolean firstTime )
		{
			if (firstTime)
			{
				myPreviousEnd = 0;
			}

			myWorkCalendar = _workCalendar;
			myVisitor = _visitor;
		}

		public void setWorkCalendar( 
			WorkCalendar _workCalendar )
		{
			myWorkCalendar = _workCalendar;
		}

		private static double ALMOST_ZERO = 0.00001;
		Closure<HasStartAndEnd> myVisitor;
		MutableInterval myInterval = new MutableInterval(0, 0);
		WorkCalendar myWorkCalendar;
		long myBarStart = 0;
		long myPreviousEnd = 0;
	}

	private static BarSeriesCallback myBarCallback = new BarSeriesCallback();

	//private static Log log = LogFactory.getLog(HasAssignmentsImpl.class);
	transient AssociationList myAssignments;
	boolean effortDriven = ScheduleOption.getInstance().isEffortDriven();

	//TODO scheduling rule and effort driven don't make sense for resources, so make them go away?
	int schedulingRule = ScheduleOption.getInstance().getSchedulingRule();
}
