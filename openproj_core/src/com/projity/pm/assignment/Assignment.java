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

import com.projity.algorithm.*;
import com.projity.algorithm.buffer.CalculatedValues;

import com.projity.association.Association;
import com.projity.association.InvalidAssociationException;

import com.projity.configuration.Configuration;

import com.projity.datatype.CanSupplyRateUnit;
import com.projity.datatype.Duration;
import com.projity.datatype.DurationFormat;
import com.projity.datatype.ImageLink;
import com.projity.datatype.Rate;
import com.projity.datatype.RateFormat;
import com.projity.datatype.TimeUnit;

import org.openproj.domain.document.Document;

import com.projity.field.DelegatesFields;
import com.projity.field.Field;
import com.projity.field.FieldContext;
import com.projity.field.HasExtraFields;

import com.projity.functor.IntervalConsumer;
import com.projity.functor.ObjectVisitor;

import com.projity.graphic.configuration.HasIndicators;
import com.projity.graphic.configuration.HasTaskIndicators;
import com.projity.interval.ValueObjectForInterval;

import com.projity.options.AdvancedOption;

import com.projity.pm.assignment.contour.AbstractContour;
import com.projity.pm.assignment.contour.AbstractContourBucket;
import com.projity.pm.assignment.contour.ContourBucketIntervalGenerator;
import com.projity.pm.assignment.contour.ContourFactory;
import com.projity.pm.assignment.contour.PersonalContour;
import com.projity.pm.assignment.functor.AssignmentFieldClosureCollection;
import com.projity.pm.assignment.functor.AssignmentFieldFunctor;
import com.projity.pm.assignment.functor.CalculatedValuesFunctor;
import com.projity.pm.assignment.functor.CostFunctor;
import com.projity.algorithm.DateAtValueFunctor;
import com.projity.pm.assignment.functor.FixedCostFunctor;
import com.projity.pm.assignment.functor.PercentAllocFunctor;
import com.projity.pm.assignment.functor.PersonalContourMaker;
import com.projity.pm.assignment.functor.PrintValueFunctor;
import com.projity.pm.assignment.functor.ResourceAvailabilityFunctor;
import com.projity.pm.assignment.functor.ValueAtInstant;
import com.projity.pm.assignment.functor.WorkComparator;
import com.projity.pm.assignment.functor.WorkFunctor;
import com.projity.pm.assignment.functor.ZeroFunctor;
import com.projity.pm.assignment.timesheet.AssignmentWorkflowState;
import com.projity.pm.assignment.timesheet.TimesheetHelper;
import com.projity.pm.assignment.timesheet.TimesheetStatus;
import com.projity.pm.assignment.timesheet.UpdatesFromTimesheet;
import com.projity.pm.calendar.WorkCalendar;
import com.projity.pm.costing.Accrual;
import com.projity.pm.costing.CurrencyRateTable;
import com.projity.pm.costing.EarnedValueCalculator;
import com.projity.pm.costing.EarnedValueFields;
import com.projity.pm.costing.EarnedValueValues;
import com.projity.pm.costing.HasCostRateIndex;
import com.projity.pm.criticalpath.TaskSchedule;
import com.projity.pm.key.HasKey;
import com.projity.pm.key.HasKeyImpl;
import com.projity.pm.resource.Resource;
import com.projity.pm.resource.ResourceImpl;
import com.projity.pm.scheduling.BarClosure;
import com.projity.pm.scheduling.ConstraintType;
import com.projity.pm.scheduling.Delayable;
import com.projity.pm.scheduling.Schedule;
import com.projity.pm.scheduling.ScheduleInterval;
import com.projity.pm.scheduling.ScheduleUtil;
import com.projity.pm.scheduling.SchedulingType;
import com.projity.pm.snapshot.BaselineScheduleFields;
import com.projity.pm.snapshot.Snapshottable;
import org.openproj.domain.document.BelongsToDocument;
import com.projity.pm.task.Project;
import com.projity.pm.time.HasStartAndEnd;
import com.projity.pm.time.ImmutableInterval;
import com.projity.pm.time.Interval;
import com.projity.pm.time.MutableInterval;

import com.projity.server.data.DataObject;

import com.projity.strings.Messages;

import com.projity.util.Alert;
import com.projity.util.ClassUtils;
import com.projity.util.DateTime;
import com.projity.util.Environment;
import com.projity.util.HashMapWithDirtyFlags;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.Predicate;

import org.openproj.domain.task.Task;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.openproj.domain.model.Model;


/**
 * Class representing resource assignments
 * @stereotype thing
 */
public final class Assignment
	implements 
		Allocation,
		AssignmentSpecificFields,
		Association,
		BaselineScheduleFields,
		BelongsToDocument,
		CanSupplyRateUnit,
		Cloneable,
		Comparable<Assignment>,
		DataObject,
		Delayable,
//		DelegatesFields,
		EarnedValueFields,
		EarnedValueValues,
		HasCostRateIndex,
		HasExtraFields,
		HasKey,
		HasRequestDemandType,
		HasTaskIndicators,
		HasTimeDistributedData,
		Schedule,
		TimeDistributedFields,
		UpdatesFromTimesheet
{
	/**
	 * Construct an assignment.  The arguments are those that are presented in the assign resource dialog
	 * @param _task
	 * @param _resource
	 * @param _units
	 * @param _requestDemandType  -Normally empty
	 */
	private Assignment( 
		Task _task,
		Resource _resource,
		double _units,
		int _requestDemandType )
	{
		detail = new AssignmentDetail( _task, _resource, _units, _requestDemandType, 0 );
	}

	public Assignment( 
		Assignment _from )
	{
		detail = _from.detail;
	}

	public Assignment( 
		AssignmentDetail detail )
	{
		this.detail = detail;
	}

	/**
	 * Constructor adapted to mpx
	 * @param task
	 * @param resource
	 * @param units
	 * @param delay
	 */
	private Assignment( 
		Task task,
		Resource resource,
		double units,
		long delay )
	{
		detail = new AssignmentDetail(task, resource, units, RequestDemandType.NONE, delay);
	}

	@Override
	public double actualCost( 
		long start,
		long end )
	{
		if (!isInRange( start, end ))
		{
			return NO_VALUE_DOUBLE;
		}

		Query query = Query.getInstance();
		SelectFrom clause = SelectFrom.getInstance().whereInRange( Math.max( start, detail.getStart() ),
				Math.min( end, getStop() ) );
		query.selectFrom( clause ).action( cost( clause, false ) ).execute();

		return ((DoubleValue)query.getActionVisitor()).getValue();
	}

	@Override
	public double actualFixedCost( 
		long start,
		long end )
	{
		return ((Task)getTask()).actualFixedCost( start, end );
	}

	@Override
	public long actualWork( 
		long start,
		long end )
	{
		if (!isInRange( start, end ))
		{
			return NO_VALUE_LONG;
		}

		if (!isLabor()) // TODO this right?
		{
			return 0;
		}

		Query query = Query.getInstance();
		SelectFrom clause = SelectFrom.getInstance().whereInRange( Math.max( start, detail.getStart() ),
				Math.min( end, getStop() ) );
		query.selectFrom( clause ).action( work( clause ) ).execute();

		return (long)((DoubleValue)query.getActionVisitor()).getValue();
	}

	//Costs and earned value
	@Override
	public double acwp( 
		long start,
		long end )
	{
		if (!isInRange( start, end ))
		{
			return NO_VALUE_DOUBLE;
		}

		Query query = Query.getInstance();
		SelectFrom clause = SelectFrom.getInstance().whereInRange( Math.max( start, detail.effectiveBaselineStart() ),
				Math.min( end, getCompletedOrStatusDate() ) );
		query.selectFrom( clause ).action( cost( clause, false ) ).execute();

		return ((DoubleValue)query.getActionVisitor()).getValue();
	}

	public void addCalendarTime( 
		long start,
		long end )
	{
		newDetail().addCalendarTime( start, end );
	}

	private boolean addWorkingTimeIfRequired( 
		long work,
		FieldContext context,
		boolean actual )
	{
		WorkCalendar cal = getEffectiveWorkCalendar();

		// adjust start and end inside the calendar
		long start = cal.adjustInsideCalendar( context.getStart(), false );
		long end = cal.adjustInsideCalendar( context.getEnd(), true );

		if ((work != 0) && !verifyBounds( start, end ))
		{
			return false;
		}

		// if adding during non calendar time
		if ((end <= start) && (work != 0))
		{
			start = context.getStart();
			end = context.getEnd();

			//		example 10 day task -	MTWTFSSMTWTF
			//		initial bar:        	=====  =====
			//		calendar time add    	====== ====   here I make a saturday working, so the task finishes sooner
			//        shift the saturday   	=====  =====  shifting the saturday puts the task back as it was
			//		set the interval     	====== =====  now I set the saturday value
			addCalendarTime( start, end );
			cal = getEffectiveWorkCalendar();

			if (actual && (getTask().getActualStart() == 0))
			{
				long taskStart = cal.adjustInsideCalendar( start, false );
				Task task = (Task)getTask();
				task.setActualStartNoEvent( taskStart );
				task.getCurrentSchedule().setStart( taskStart );
			}

			long addedDuration = cal.compare( end, start, false );

			// shift the remaining contour to the right by the duration of the inserted time
//			System.out.println("before shift" + getWorkContour());
			shift( start, end, addedDuration ); 
//			System.out.println("after shift" + getWorkContour());

			getTask().recalculateLater( this ); // task needs to be recalculated

//			SwingUtilities.invokeLater(new Runnable());
		}

		return true;
	}

	public void addWorkingTimeIfRequired( 
		long date )
	{
		WorkCalendar cal = getEffectiveWorkCalendar();
		date = DateTime.dayFloor( date );

		// adjust start and end inside the calendar
		long adjustedStart = cal.adjustInsideCalendar( date, false );
		long adjustedEnd = cal.adjustInsideCalendar( DateTime.nextDay( date ), true );

		if (!verifyBounds( date, date ))
		{
			return;
		}

		// if adding during non calendar time
		if (adjustedEnd <= adjustedStart)
		{
			newDetail().addCalendarTime( date );
		}
	}

	/**
	 * @param duration The duration to set.
	 */
	@Override
	public void adjustRemainingDuration( 
		long newRemainingDuration,
		boolean doChildren )
	{
		long old = getRemainingDuration();
		newRemainingDuration = Duration.millis( newRemainingDuration ); // don't use time unit

		if (getUnits() == 0) // take care of degenerate case
		{
			newRemainingDuration = 0;
		}

		newDetail().adjustRemainingDuration( newRemainingDuration );

		//TODO commented out may 8 2007
		//		if (getTaskSchedulingType() == SchedulingType.FIXED_WORK && newRemainingDuration != 0) {
		//			double multiplier = ((double)old) / newRemainingDuration;
		//			if (multiplier > 0) {
		//				detail.setWorkContour(detail.getWorkContour().adjustUnits(multiplier, getActualDuration()));
		//				detail.setUnits(getUnits() * multiplier);
		//			}
		//		}
	}

	public void adjustRemainingDurationIfWorkingAtTaskEnd( 
		long newRemainingDuration )
	{
		if ((getEnd() >= getTask().getEnd()) || !isInitialized())
		{
			adjustRemainingDuration( newRemainingDuration, false );
		}
	}

	/**
	 * @param units The units to set.
	 */
	@Override
	public void adjustRemainingUnits( 
		double newRemainingUnits,
		double oldRemainingUnits,
		boolean doChildren,
		boolean conserveTotalUnits )
	{
		if (!isTemporal()) // for absolute quantities, don't change
		{
			return;
		}

		oldRemainingUnits = getRemainingUnits();

		final Task task = (Task)getTask();

		//	task.adjustUnitsDelta(newRemainingUnits - oldRemainingUnits);
		if (doChildren)
		{
			task.getSchedulingRule().adjustRemainingUnits( this, newRemainingUnits, oldRemainingUnits, false, false );
		}
		else
		{ // just treat the assignment

			if (getPercentComplete() > 0.0D) // if actuals, then remaining may have different contour
			{
				makeContourPersonal();
			}

			newDetail().adjustRemainingUnits( newRemainingUnits );
		}
	}

	@Override
	public void adjustRemainingWork( 
		double multiplier,
		boolean doChildren )
	{
		if (getPercentComplete() > 0.0D) // if actuals, then remaining may have different contour
		{
			makeContourPersonal(); //fix?
		}

		newDetail().adjustRemainingWork( multiplier );
	}

	public void adjustRemainingWork( 
		long remainingWork )
	{
		newDetail().adjustRemainingDuration( remainingWork );
	}

	public void adjustWorkContourForEarlierTaskStart( 
		long earlierStart,
		long lateStart )
	{
		AbstractContour currentContour = getWorkContour();
		WorkCalendar cal = getEffectiveWorkCalendar();
		long shiftAmount = cal.compare( lateStart, earlierStart, false );
		this.setDelay( this.getDelay() + shiftAmount );

		//		if (!currentContour.isPersonal()){
		//		}
		//		else{
		//			AbstractContour contour = PersonalContour.addEmptyBucket(currentContour, shiftAmount, false); // add empty space before
		//			newDetail().setWorkContour(contour);
		//		}
	}

	/**
	 * Allow setting of working duration. MSProject displays working duration (excludes non-work intervals) except when
	 * task type is fixed duration, in which case it shows duration with non-work intervals
	 * @param newWorkingDuration
	 */
	public void adjustWorkingDuration( 
		long newWorkingDuration )
	{
		newDetail().adjustWorkingDuration( newWorkingDuration );
	}

	@Override
	public boolean applyTimesheet( 
		Collection fieldArray,
		long timesheetUpdateDate )
	{
		boolean updated = copyFieldsFromTimesheet( fieldArray );

		if (updated)
		{
			setTimesheetStatus( TimesheetStatus.INTEGRATED );
			this.lastTimesheetUpdate = timesheetUpdateDate;

			Assignment ts = getTimesheetAssignment();
			ts.setTimesheetStatus( TimesheetStatus.INTEGRATED );
			ts.lastTimesheetUpdate = timesheetUpdateDate;
		}

		return updated;
	}

	private AssignmentFieldFunctor availability( 
		final SelectFrom _clause )
	{
		//TODO this is untested, as histogram is currently broken 8/12/04 hk
		final ResourceAvailabilityFunctor functor = ResourceAvailabilityFunctor.getInstance( this );
		final CollectionIntervalGenerator<ValueObjectForInterval> availability = CollectionIntervalGenerator.getInstance( 
			((ResourceImpl)detail.getResource()).getAvailabilityTable().getList() );

		//use range?
		_clause.select( functor ).from( availability ); 

		return functor;
	}

	@Override
	public double bac( 
		long start,
		long end )
	{
		if (!isInRange( start, end ))
		{
			return NO_VALUE_DOUBLE;
		}

		Query query = Query.getInstance();
		SelectFrom clause = SelectFrom.getInstance().whereInRange( start, end );
		query.selectFrom( clause ).action( baselineData( TimeDistributedConstants.COST, clause ) ).execute();

		return ((DoubleValue)query.getActionVisitor()).getValue();
	}

	@Override
	public Object backupDetail()
	{
		return detail.backupDetail();
	}

	@Override
	public double baselineCost( 
		long start,
		long end )
	{
		//		if (!isInRange(start,end))
		//			return NO_VALUE_DOUBLE;
		Query query = Query.getInstance();
		SelectFrom clause = SelectFrom.getInstance().whereInRange( start, end );
		query.selectFrom( clause ).action( baselineData( TimeDistributedConstants.COST, clause ) ).execute();

		return ((DoubleValue)query.getActionVisitor()).getValue();
	}

	private AssignmentFieldFunctor baselineData( 
		final TimeDistributedConstants _type,
		final SelectFrom baselineSelectFrom )
	{
		Assignment baselineAssignment = detail.getBaselineAssignment();
		AssignmentFieldFunctor baselineFunctor;

		if (baselineAssignment == null)
		{
			baselineFunctor = ZeroFunctor.getInstance();
			baselineSelectFrom.select( baselineFunctor ).from( RangeIntervalGenerator.empty() );
		}
		else
		{
			baselineFunctor = baselineAssignment.getDataSelect( _type, baselineSelectFrom, false );
		}

		return baselineFunctor;
	}

	private AssignmentFieldFunctor baselineData( 
		final TimeDistributedConstants _type,
		SelectFrom baselineSelectFrom,
		Object baseline )
	{
		Assignment baselineAssignment = detail.getBaselineAssignment( baseline, false );
		AssignmentFieldFunctor baselineFunctor;

		if (baselineAssignment == null)
		{
			baselineFunctor = ZeroFunctor.getInstance();
			baselineSelectFrom.select( baselineFunctor ).from( RangeIntervalGenerator.empty() );
		}
		else
		{
			baselineFunctor = baselineAssignment.getDataSelect( _type, baselineSelectFrom, false );
		}

		return baselineFunctor;
	}

	@Override
	public long baselineWork( 
		long start,
		long end )
	{
		//		if (!isInRange(start,end))
		//			return NO_VALUE_LONG;
		if (!isLabor()) // TODO this right?
		{
			return 0;
		}

		Query query = Query.getInstance();
		SelectFrom clause = SelectFrom.getInstance().whereInRange( start, end );
		query.selectFrom( clause ).action( baselineData( TimeDistributedConstants.WORK, clause ) ).execute();

		return (long)((DoubleValue)query.getActionVisitor()).getValue();
	}

	//[(Actual % of completion / Expected % of completion) of an activity for a given period] * Actual cost of activity
	@Override
	public double bcwp( 
		long start,
		long end )
	{
		if (!isInRange( start, end ))
		{
			return NO_VALUE_DOUBLE;
		}

		end = Math.min( end, getStatusDate() );

		if (end == 0)
		{
			return 0.0D;
		}

		if (AdvancedOption.getInstance().isEarnedValueFieldsCumulative())
		{
			start = getStart(); // start from the beginning of the task and ignore the range start
		}

		double cost = actualCost( start, end );

		if (cost == 0)
		{
			return 0;
		}

		return efficiency() * cost;

		//
		//		Query query = Query.getInstance();
		//		long boundaryStart = detail.getStart(); // always use assignment start and never start
		//		long boundaryEnd = Math.min(getStatusDate(),baselineAssignment.getEffectiveWorkCalendar().add(boundaryStart, (long) (baselineAssignment.getDuration() * getPercentComplete()),false));
		//		SelectFrom clause = SelectFrom.getInstance().whereInRange(boundaryStart,Math.min(end,boundaryEnd));
		//		query.selectFrom(clause)
		//			.action(baselineData(COST,clause))
		//			.execute();
		//		return ((DoubleValue)query.getActionVisitor()).getValue();
	}

	@Override
	public double bcws( 
		long start,
		long end )
	{
		if (!isInRange( start, end ))
		{
			return NO_VALUE_DOUBLE;
		}

		if (AdvancedOption.getInstance().isEarnedValueFieldsCumulative())
		{
			start = getStart(); // start from the beginning of the task and ignore the range start
		}

		Query query = Query.getInstance();
		SelectFrom clause = SelectFrom.getInstance()
									  .whereInRange( Math.max( start, detail.effectiveBaselineStart() ),
				Math.min( end, getStatusDate() ) );
		query.selectFrom( clause ).action( baselineData( TimeDistributedConstants.COST, clause ) ).execute();

		return ((DoubleValue)query.getActionVisitor()).getValue();
	}

	//	public void forEach(Object type, Closure actionVisitor) {
	//		SelectFrom clause = SelectFrom.getInstance();
	//		AssignmentFieldFunctor dataFunctor = getDataSelect(type,clause,false);
	//		Query.getInstance().selectFrom(clause)
	//		.action(actionVisitor)
	//		.execute();
	//	}
	@Override
	public void buildReverseQuery( 
		ReverseQuery reverseQuery )
	{
		SelectFrom clause = SelectFrom.getInstance();
		reverseQuery.addField( getDataSelect( reverseQuery.getType(), clause, false ) );
		reverseQuery.addGroupBy( IntervalGeneratorSet.extractUnshared( clause.getFromIntervalGenerators() ) );
		reverseQuery.addSelectFrom( clause );
	}

	/**
	 * Calculate the total cost
	 * @return
	 */
	public double calcAll( 
		final TimeDistributedConstants _type )
	{
		SelectFrom clause = SelectFrom.getInstance();
		AssignmentFieldFunctor dataFunctor = getDataSelect( _type, clause, true );
		Query.getInstance().selectFrom( clause ).execute();

		return dataFunctor.getValue();
	}

	public void calcDataBetween( 
		final TimeDistributedConstants _type,
		long start,
		long end )
	{
		SelectFrom clause = SelectFrom.getInstance().whereInRange( start, end ); // automatically also adds a generator to limit range
		AssignmentFieldFunctor dataFunctor = getDataSelect( _type, clause, false );

		RangeIntervalGenerator dailyInRange = RangeIntervalGenerator.getInstance( start, end, 1000 * 60 * 60 * 24 );
		PrintValueFunctor print = PrintValueFunctor.getInstance( dataFunctor );
		Query.getInstance().selectFrom( clause ).groupBy( dailyInRange ).action( print ).execute();
	}

	public void calcDataBetween( 
		final TimeDistributedConstants _type,
		final TimeIteratorGenerator _generator,
		final CalculatedValues _values )
	{
		final SelectFrom clause = SelectFrom.getInstance();
		final AssignmentFieldFunctor dataFunctor = getDataSelect( _type, clause, false );
		calcDataBetween( dataFunctor, clause, _generator, _values );
	}

	public static void calcDataBetween( 
		final AssignmentFieldFunctor _dataFunctor,
		final SelectFrom _clause,
		final TimeIteratorGenerator _generator,
		final CalculatedValues _values )
	{
		if (_generator != null)
		{
			// automatically; also adds a generator to limit range
			_clause.whereInRange( _generator.getStart(), _generator.getEnd() ); 
		}

		final CalculatedValuesFunctor visitor = CalculatedValuesFunctor.getInstance( _dataFunctor, _values, _generator );

		final Query query = Query.getInstance();
		query.selectFrom( _clause );

		if ((_generator != null) && _generator instanceof TimeIteratorGenerator)
		{
			query.groupBy( (TimeIteratorGenerator)_generator ).action( visitor );
		}
		else
		{
			// replace with my visitor
			_clause.select( visitor ); 
		}

		query.execute();
	}

	public long calcOffsetFrom( 
		long startDate,
		long dependencyDate,
		boolean ahead,
		boolean remainingOnly,
		boolean useSooner )
	{
		long start;

		if (ahead)
		{
			start = computeStart( startDate, dependencyDate );
		}
		else
		{
			start = startDate;
		}

		if (getPercentComplete() > 0)
		{
			start = getEffectiveWorkCalendar().add( start, getActualDuration(), useSooner );
		}

		long duration = remainingOnly
			? detail.getRemainingDuration()
			: detail.getDuration(); // + this.detail.getSplitDuration();
									//TODO integrate split - still needed?

		long amount = (ahead
			? duration
			: (-duration));

		return getEffectiveWorkCalendar().add( start, amount, useSooner );
	}

	public static void calcResourceAvailabilityBetween( 
		final Resource _resource,
		final TimeIteratorGenerator _generator,
		final CalculatedValues _values )
	{
		final SelectFrom clause = SelectFrom.getInstance();
		final AssignmentFieldFunctor dataFunctor = resourceAvailability( clause, _resource );
		calcDataBetween( dataFunctor, clause, _generator, _values );
	}

	/* (non-Javadoc)
	 * @see
	 */
	@Override
	public long calcTotalDelay()
	{
		return detail.calcTotalDelay();
	}

	/**
	 * @return
	 */
	public long calcWork()
	{
		if (!isLabor())
		{
			return 0;
		}

		return detail.calcWork();
	}

	@Override
	public Collection childrenToRollup()
	{
		return null;
	}

	@Override
	public void clearDuration()
	{
		newDetail().clearDuration();
	}

	@Override
	public Object clone()
	{
		try
		{
			Assignment a = (Assignment)super.clone();
			a.hasKey = new HasKeyImpl(true, a);
			a.setName( getName() );

			//			myBarClosureInstance = new BarClosure();
			a.detail = (AssignmentDetail)detail.clone();

			if (extraFields != null)
			{
				a.extraFields = (HashMapWithDirtyFlags)ClassUtils.deepCopy( extraFields );
			}

			return a;
		}
		catch (CloneNotSupportedException e)
		{
			throw new InternalError();
		}
	}

	public Object cloneWithResource( 
		Resource resource )
	{
		Assignment a = (Assignment)clone();
		a.detail.setResource( resource );

		return a;
	}

	public Object cloneWithResourceAndTask( 
		Resource resource,
		Task task )
	{
		Assignment a = (Assignment)clone();
		a.detail.setResource( resource );
		a.detail.setTask( task );

		return a;
	}

	public Object cloneWithTask( 
		Task task )
	{
		Assignment a = (Assignment)clone();
		a.detail.setTask( task );

		return a;
	}

	@Override
	public int compareTo( 
		final Assignment _other )
	{
		return getResourceName().compareTo( _other.getResourceName() );
	}

	/**
	 * Offset a date by its duration or remaining duration
	 * @param date
	 * @param ahead - true if we want to add duration, false otherwise
	 * @param remainingOnly - true if use remaining duration, otherwise all duration
	 * @param useSooner - Used for end/beginning of day issues
	 * @return new date
	 */
	private long computeStart( 
		long startDate,
		long dependencyDate )
	{
		long delay = this.calcTotalDelay();
		long s = startDate;

		if (getPercentComplete() == 0)
		{ // if no completion, use task dependency date if needed

			if (dependencyDate > s)
			{
				s = dependencyDate;
			}
		}

		if (delay > 0)
		{
			s = getEffectiveWorkCalendar().add( s, delay, true );
		}

		//TODO check if above should use task calendar or assignment calendar
		return s;
	}

	@Override
	public void consumeIntervals( 
		IntervalConsumer consumer )
	{
		myBarClosureInstance.initialize( consumer, this );

		boolean inProgress = getPercentComplete() > 0.0D;
		MutableInterval bounds = null;

		if (inProgress)
		{
			bounds = new MutableInterval(getStart(), getStop());
			myBarClosureInstance.setBounds( bounds );
		}

		forEachWorkingInterval( myBarClosureInstance, true, getEffectiveWorkCalendar() );

		if (inProgress == true)
		{
			//			myBarClosureInstance.initCount();
			bounds.setStart( bounds.getEnd() ); // shift for second half
			bounds.setEnd( getEnd() );

			forEachWorkingInterval( myBarClosureInstance, true, getEffectiveWorkCalendar() );
		}

		myBarClosureInstance.setBounds( null );
	}

	/**
	 * Gets an instance of a generator that acts on the cost contour
	 * @return instance of generator
	 */
	public ContourBucketIntervalGenerator contourGeneratorInstance( 
		Object type )
	{
		return contourGeneratorInstance( type, getStart() );
	}

	/**
	 * Gets an instance of a generator that acts on the cost contour
	 * @return instance of generator
	 */
	public ContourBucketIntervalGenerator contourGeneratorInstance( 
		Object type,
		long start )
	{
		return ContourBucketIntervalGenerator.getInstance( this, type ); //getEffectiveWorkCalendar(), detail.getContour(type), detail.getDurationMillis(), getStart());
																		 //TODO actual cost treatment if entered manually
	}

	public void convertToBaselineAssignment( 
		boolean useDefaultCalendar )
	{
		detail.convertToBaselineAssignment( useDefaultCalendar );
	}

	public boolean copyFieldsFromTimesheet( 
		Collection fieldArray )
	{
		Assignment ts = getTimesheetAssignment();

		if (ts == null)
		{
			return false;
		}

		if (ts.getTimesheetStatus() != TimesheetStatus.VALIDATED) // only incorporate validated data
		{
			return false;
		}

		Field.copyData( fieldArray, this, ts );

		return true;
	}

	/* (non-Javadoc)
	 * @see com.projity.util.Association#copyPrincipalFieldsFrom(com.projity.util.Association)
	 */
	@Override
	public void copyPrincipalFieldsFrom( 
		Association from )
	{
		Assignment fromAssignment = (Assignment)from;
		double units = fromAssignment.getUnits();

		if (fromAssignment.isLabor())
		{
			adjustRemainingUnits( fromAssignment.getUnits(), 0, true, false );
			detail.setUnits( units );
		}
		else
		{
			detail.rate = fromAssignment.detail.getRate();
			detail.setDuration( getDuration() ); // reset duration
		}

		getTask().updateCachedDuration(); // needed for checking if milestone
	}

	//	PercentAllocFunctor(Assignment assignment, WorkCalendar workCalendar, ContourBucketIntervalGenerator contourBucketIntervalGenerator) {
	private AssignmentFieldFunctor cost( 
		SelectFrom clause,
		boolean all )
	{
		if (detail.getCostContour().isPersonal())
		{ // in the case where a cost contour has already been saved, use it

			ContourBucketIntervalGenerator costContour = contourGeneratorInstance( TimeDistributedConstants.COST );
			WorkFunctor workF = WorkFunctor.getInstance( this, getEffectiveWorkCalendar(), costContour, detail.calcOvertimeUnits() ); // note that is is a work contour
			clause.select( workF ).from( costContour );

			return workF;
		}
		else
		{
			boolean prorated = isProratedCost();

			// if prorated, or if calculating a total, then treat as prorated
			if (all || prorated)
			{
				ContourBucketIntervalGenerator workContour = contourGeneratorInstance( TimeDistributedConstants.WORK );
				CollectionIntervalGenerator<ValueObjectForInterval> costRate = CollectionIntervalGenerator.getInstance( 
					detail.getResource().getCostRateTable( detail.getCostRateIndex() ).getList() );

				// maybe add currency rate table after
				clause.from( costRate ).from( workContour ); 

				CurrencyRateTable currencyRateTable = getProject().getCurrencyRateTable();
				CollectionIntervalGenerator<ValueObjectForInterval> currencyRate = null;

				if ((currencyRateTable != null) 
				 && (currencyRateTable.isActive() == true))
				{
					// go through project which will delegate to global value. Should realy be project related, not global
					currencyRate = CollectionIntervalGenerator.getInstance( currencyRateTable.getList() ); 
					clause.from( currencyRate );
				}

				// Note that the getStart() parameter implies cost per use is applied at start
				CostFunctor costF = CostFunctor.getInstance( this, getEffectiveWorkCalendar(), workContour,
						detail.calcOvertimeUnits(), costRate, currencyRate, getStart(), prorated );
				clause.select( costF );

				return costF;
			}
			else
			{ // accrue start or end

				long triggerDate = (detail.getResource().getAccrueAt() == Accrual.START)
					? getStart()
					: getFinish(); // use start or end
				AssignmentFieldFunctor constantCost = ValueAtInstant.getInstance( triggerDate, calcAll( TimeDistributedConstants.COST ) );
				clause.select( constantCost ).from( InstantIntervalGenerator.getInstance( triggerDate ) ); // just one instant

				return constantCost;
			}
		}
	}

	@Override
	public double cost( 
		long start,
		long end )
	{
		if (!isInRange( start, end ))
		{
			return NO_VALUE_DOUBLE;
		}

		Query query = Query.getInstance();
		SelectFrom clause = SelectFrom.getInstance().whereInRange( start, end );
		query.selectFrom( clause ).action( cost( clause, false ) ).execute();

		return ((DoubleValue)query.getActionVisitor()).getValue();
	}

	/**
	 * @param contour The contour to set.
	 * TODO get rid of this
	 */
	public void debugSetWorkContour( 
		AbstractContour contour )
	{
		newDetail().setWorkContour( contour );
	}

//	@Override
//	public boolean delegates( 
//		final Field _field )
//	{
//		if (delegatedFields == null)
//		{
//			return false;
//		}
//
//		return delegatedFields.containsKey( _field );
//	}

	@Override
	public void doAddService( 
		Object eventSource )
	{
		AssignmentService.getInstance().connect( this, eventSource );
	}

	@Override
	public void doRemoveService( 
		Object eventSource )
	{
		AssignmentService.getInstance().remove( this, eventSource, true );
	}

	/* (non-Javadoc)
	 * @see com.projity.association.Association#doUpdateService(java.lang.Object)
	 */
	@Override
	public void doUpdateService( 
		Object eventSource )
	{
		getUnitsField().fireEvent( this, this, null ); // need to update
	}

	public double efficiency()
	{
		Assignment baselineAssignment = detail.getBaselineAssignment();

		if (baselineAssignment == null)
		{
			return 0.0D;
		}

		long baselineWork = baselineAssignment.work();

		if (baselineWork == 0)
		{
			return 0.0;
		}

		long work = work();

		if (work == 0.0D)
		{
			return 1.0D;
		}

		return ((double)baselineWork) / work;
	}

	public void extend( 
		long start,
		long end,
		long extendDuration )
	{
		if (end < getStop())
		{
			return;
		}

		WorkCalendar cal = getEffectiveWorkCalendar();
		long startOffset = cal.compare( start, getStart(), false );
		long endOffset = cal.compare( end, getStart(), false );

//		if (start >= getResume()) {
//			long splitDuration = detail.getSplitDuration();
//			startOffset -= splitDuration;
//			endOffset -= splitDuration;
//		}
		
		Interval range = getRangeThatIntervalCanBeMoved( start, startOffset, endOffset );

		if (extendDuration > 0)
		{
			extendDuration = Math.min( extendDuration, range.getEnd() - endOffset ); // don't allow to shift more than possible
		}
		else
		{
			extendDuration = Math.max( extendDuration, startOffset - endOffset );
		}

		newDetail().extend( endOffset, extendDuration );
	}

	public void extendBefore( 
		long start,
		long end,
		long extendDuration )
	{
		if (start < getStop())
		{
			return;
		}

		WorkCalendar cal = getEffectiveWorkCalendar();

		boolean firstBar = (cal.compare( getStart(), start, false ) == 0);
		long startOffset = cal.compare( start, getStart(), false );
		long endOffset = cal.compare( end, getStart(), false );
		Interval range = getRangeThatIntervalCanBeMoved( start, startOffset, startOffset );

		if (extendDuration > 0)
		{
			extendDuration = Math.min( extendDuration, endOffset - startOffset );
		}
		else
		{
			extendDuration = Math.max( extendDuration, range.getStart() - startOffset ); // don't allow to shift more than possible
		}

		if (firstBar)
		{
			setTotalDelay( calcTotalDelay() + extendDuration );
		}

		newDetail().extendBefore( startOffset, extendDuration );
	}

	private void extractDelayFromContour( 
		PersonalContour newContour )
	{
		long delay = newContour.extractDelay(); // the case when adding 0 units at start

		if (delay > 0)
		{
			newDetail();
			detail.setDelay( delay );

			//			detail.recalculateDuration();
		}
	}

	@Override
	public boolean fieldHideActualCost( 
		FieldContext fieldContext )
	{
		return isFieldHidden( fieldContext );
	}

	@Override
	public boolean fieldHideActualFixedCost( 
		FieldContext fieldContext )
	{
		return true;
	}

	@Override
	public boolean fieldHideActualWork( 
		FieldContext fieldContext )
	{
		return fieldHideWork( fieldContext );
	}

	@Override
	public boolean fieldHideAcwp( 
		FieldContext fieldContext )
	{
		return isFieldHidden( fieldContext );
	}

	@Override
	public boolean fieldHideBac( 
		FieldContext fieldContext )
	{
		return isFieldHidden( fieldContext );
	}

	@Override
	public boolean fieldHideBaselineCost( 
		int numBaseline,
		FieldContext fieldContext )
	{
		return isBaselineFieldHidden( numBaseline, fieldContext );
	}

	@Override
	public boolean fieldHideBaselineWork( 
		int numBaseline,
		FieldContext fieldContext )
	{
		return isBaselineFieldHidden( numBaseline, fieldContext );
	}

	@Override
	public boolean fieldHideBcwp( 
		FieldContext fieldContext )
	{
		return isEarnedValueFieldHidden( fieldContext );
	}

	@Override
	public boolean fieldHideBcws( 
		FieldContext fieldContext )
	{
		return isEarnedValueFieldHidden( fieldContext );
	}

	/***************************************************************************************
	 * Time Distributed Fields
	 **************************************************************************************/
	@Override
	public boolean fieldHideCost( 
		FieldContext fieldContext )
	{
		return isFieldHidden( fieldContext );
	}

	@Override
	public boolean fieldHideCpi( 
		FieldContext fieldContext )
	{
		return isFieldHidden( fieldContext );
	}

	@Override
	public boolean fieldHideCv( 
		FieldContext fieldContext )
	{
		return isFieldHidden( fieldContext );
	}

	@Override
	public boolean fieldHideCvPercent( 
		FieldContext fieldContext )
	{
		return isFieldHidden( fieldContext );
	}

	@Override
	public boolean fieldHideEac( 
		FieldContext fieldContext )
	{
		return isFieldHidden( fieldContext );
	}

	@Override
	public boolean fieldHideSpi( 
		FieldContext fieldContext )
	{
		return isFieldHidden( fieldContext );
	}

	@Override
	public boolean fieldHideSv( 
		FieldContext fieldContext )
	{
		return isFieldHidden( fieldContext );
	}

	@Override
	public boolean fieldHideSvPercent( 
		FieldContext fieldContext )
	{
		return isFieldHidden( fieldContext );
	}

	@Override
	public boolean fieldHideTcpi( 
		FieldContext fieldContext )
	{
		return isFieldHidden( fieldContext );
	}

	@Override
	public boolean fieldHideTimesheetClosed( 
		FieldContext fieldContext )
	{
		return detail.fieldHideTimesheetClosed( fieldContext );
	}

	@Override
	public boolean fieldHideVac( 
		FieldContext fieldContext )
	{
		return isFieldHidden( fieldContext );
	}

	@Override
	public boolean fieldHideWork( 
		FieldContext fieldContext )
	{
		return isFieldHidden( fieldContext );
	}

	private AssignmentFieldFunctor fixedCost( 
		SelectFrom clause )
	{
		ContourBucketIntervalGenerator contour = contourGeneratorInstance( TimeDistributedConstants.COST );
		FixedCostFunctor functor = FixedCostFunctor.getInstance( this );
		clause.select( functor ).from( contour );

		return functor;
	}

	@Override
	public double fixedCost( 
		long start,
		long end )
	{
		return ((Task)getTask()).fixedCost( start, end );
	}

	/**
	* calls back on for all "gantt bar" intervals having matching rates.  Matching is determined by a comparator.
	* @param _visitor : callback
	* @param _mergeWorking : if true, then matching occurs if the periods have non-zero work, otherwise the work
	* must be an exact match.
	*/
	@Override
	public void forEachWorkingInterval( 
		final Closure<HasStartAndEnd> _visitor,
		final boolean _mergeWorking,
		final WorkCalendar _workCalendar )
	{
		// if a value of null is used , then the bars will be grouped based on time only
		final Query query = Query.getInstance();
		final Merge merge = Merge.getInstance( _visitor, query.getGroupByGenerator(), (_mergeWorking == true)
			? null
			: WorkComparator.getInstance() );
		query.groupBy( contourGeneratorInstance( TimeDistributedConstants.WORK ) ).action( merge ).execute();
	}

	public Closure forResource( 
		Closure visitor )
	{
		return new ObjectVisitor(visitor)
		{
			@Override
			protected final Object getObject( 
				Object caller )
			{
				return ((Assignment)caller).getResource();
			}
		};
	}

	public Closure forTask( 
		Closure visitor )
	{
		return new ObjectVisitor(visitor)
		{
			@Override
			protected final Object getObject( 
				Object caller )
			{
				return ((Assignment)caller).getTask();
			}
		};
	}

	public void forceUnits( 
		double units )
	{
		newDetail().setUnits( units );
	}

	@Override
	public double getActualCost( 
		FieldContext fieldContext )
	{
		return actualCost( FieldContext.start( fieldContext ), FieldContext.end( fieldContext ) );
	}

	@Override
	public long getActualDuration()
	{
		return detail.getActualDuration();
	}

	@Override
	public long getActualFinish()
	{
		return detail.getActualFinish();
	}

	@Override
	public double getActualFixedCost( 
		FieldContext fieldContext )
	{
		return 0;
	}

	@Override
	public long getActualStart()
	{
		return detail.getActualStart();
	}

	@Override
	public long getActualWork( 
		FieldContext fieldContext )
	{
		return actualWork( FieldContext.start( fieldContext ), FieldContext.end( fieldContext ) );
	}

	/***************************************************************************************
	 * Earned Value Fields
	 **************************************************************************************/
	@Override
	public double getAcwp( 
		FieldContext fieldContext )
	{
		return acwp( FieldContext.start( fieldContext ), FieldContext.end( fieldContext ) );
	}

	@Override
	public double getBac( 
		FieldContext fieldContext )
	{
		return bac( FieldContext.start( fieldContext ), FieldContext.end( fieldContext ) );
	}

	public Assignment getBaselineAssignment( 
		Object baseline,
		boolean createIfDoesntExist )
	{
		return detail.getBaselineAssignment( baseline, createIfDoesntExist );
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.TimeDistributedFields#getBaselineCost(int, com.projity.field.FieldContext)
	 */
	@Override
	public double getBaselineCost( 
		int numBaseline,
		FieldContext fieldContext )
	{
		Assignment baseline = getBaselineAssignment( Integer.valueOf( numBaseline ), false );

		if (baseline == null)
		{
			if ((fieldContext != null) && fieldContext.isHideNullValues())
			{
				return ClassUtils.NULL_DOUBLE;
			}
			else
			{
				return 0.0D;
			}
		}

		return baseline.cost( FieldContext.start( fieldContext ), FieldContext.end( fieldContext ) );
	}

	@Override
	public long getBaselineDuration( 
		int numBaseline,
		FieldContext fieldContext )
	{
		Assignment baseline = getBaselineAssignment( Integer.valueOf( numBaseline ), false );

		if (baseline == null)
		{
			if ((fieldContext != null) && fieldContext.isHideNullValues())
			{
				return ClassUtils.NULL_LONG;
			}
			else
			{
				return 0L;
			}
		}

		return baseline.getDuration();
	}

	@Override
	public long getBaselineFinish( 
		int numBaseline )
	{
		Assignment baseline = getBaselineAssignment( Integer.valueOf( numBaseline ), false );

		if (baseline == null)
		{
			return 0L;
		}

		return baseline.getFinish();
	}

	@Override
	public long getBaselineStart( 
		int numBaseline )
	{
		Assignment baseline = getBaselineAssignment( Integer.valueOf( numBaseline ), false );

		if (baseline == null)
		{
			return 0L;
		}

		return baseline.getStart();
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.TimeDistributedFields#getBaselineWork(int, com.projity.field.FieldContext)
	 */
	@Override
	public long getBaselineWork( 
		int numBaseline,
		FieldContext fieldContext )
	{
		Assignment baseline = getBaselineAssignment( Integer.valueOf( numBaseline ), false );

		if (baseline == null)
		{
			if ((fieldContext != null) && fieldContext.isHideNullValues())
			{
				return ClassUtils.NULL_LONG;
			}
			else
			{
				return 0L;
			}
		}

		return baseline.work( FieldContext.start( fieldContext ), FieldContext.end( fieldContext ) );
	}

	@Override
	public double getBcwp( 
		FieldContext fieldContext )
	{
		return bcwp( FieldContext.start( fieldContext ), FieldContext.end( fieldContext ) );
	}

	@Override
	public double getBcws( 
		FieldContext fieldContext )
	{
		return bcws( FieldContext.start( fieldContext ), FieldContext.end( fieldContext ) );
	}

	public ImageLink getBudgetStatusIndicator()
	{
		return EarnedValueCalculator.getInstance().getBudgetStatusIndicator( getCpi( null ) );
	}

	public final Date getCachedEnd()
	{
		return cachedEnd;
	}

	public final Date getCachedStart()
	{
		return cachedStart;
	}

	public AbstractContourBucket[] getContour( 
		Object type )
	{
		return detail.getContour( type );
	}

	public AssignmentFieldFunctor getDataSelect( 
		final TimeDistributedConstants _type,
		SelectFrom clause,
		boolean all )
	{
		if (_type == TimeDistributedConstants.PERCENT_ALLOC)
		{
			return percentAlloc( clause, false );
		}
		else if (_type == TimeDistributedConstants.AVAILABILITY)
		{
			return availability( clause );
		}
		else if (_type == TimeDistributedConstants.COST)
		{
			return cost( clause, all );
		}
		else if (_type == TimeDistributedConstants.OVERALLOCATED)
		{
			return work( clause );
		}
		else if ((_type == TimeDistributedConstants.WORK) || (_type == TimeDistributedConstants.THIS_PROJECT))
		{
			return work( clause );
		}
		else if (_type == TimeDistributedConstants.ACTUAL_WORK)
		{
			clause.whereInRange( detail.getStart(), getStop() );

			return work( clause );
		}
		else if (_type == TimeDistributedConstants.REMAINING_WORK)
		{
			clause.whereInRange( getResume(), detail.getFinish() );

			return work( clause );
		}
		else if (_type == TimeDistributedConstants.ACTUAL_COST)
		{
			clause.whereInRange( detail.getStart(), getStop() );

			return cost( clause, all );
		}
		else if (_type == TimeDistributedConstants.FIXED_COST)
		{
			clause.whereInRange( detail.getStart(), getEnd() );

			return fixedCost( clause );
		}
		else if (_type == TimeDistributedConstants.ACTUAL_FIXED_COST)
		{
			clause.whereInRange( detail.getStart(), getStop() );

			return fixedCost( clause );
		}
		else if (_type == TimeDistributedConstants.REMAINING_COST)
		{
			clause.whereInRange( getResume(), detail.getFinish() );

			return cost( clause, all );
		}
		else if (_type == TimeDistributedConstants.BASELINE_COST)
		{
			return baselineData( TimeDistributedConstants.COST, clause );
		}
		else if (_type == TimeDistributedConstants.BASELINE_WORK)
		{
			return baselineData( TimeDistributedConstants.WORK, clause );
		}
		else if (_type == TimeDistributedConstants.ACWP)
		{
			clause.whereInRange( detail.effectiveBaselineStart(), getCompletedOrStatusDate() );

			return cost( clause, all );
		}
		else if (_type == TimeDistributedConstants.BCWP)
		{
			clause.whereInRange( detail.getStart(), getStatusDate() );

			AssignmentFieldFunctor costF = cost( clause, all );
			costF.setMultiplier( efficiency() );

			return costF;
		}
		else if (_type == TimeDistributedConstants.BCWS)
		{
			clause.whereInRange( detail.effectiveBaselineStart(), getStatusDate() );

			return baselineData( TimeDistributedConstants.COST, clause );
		}
//		else if (_type instanceof Field)
//		{ // treat all baselines
//
//			Field field = (Field)_type;
//
//			if (field.isIndexed())
//			{
//				Integer index = new Integer(field.getIndex());
//
//				if (field.isWork())
//				{
//					return baselineData( TimeDistributedConstants.WORK, clause, index );
//				}
//				else
//				{
//					return baselineData( TimeDistributedConstants.COST, clause, index );
//				}
//			}
//		}

		return null;
	}

	/**
	 * Accessor for the assignment's delay
	 * @return delay
	 */
	@Override
	public long getDelay()
	{
		return detail.getDelay();
	}

	/**
	 * @return Returns the detail.
	 */
	public AssignmentDetail getDetail()
	{
		return detail;
	}

	/**
	 * @return Returns the duration.
	 */
	public long getDurationMillis()
	{
		return detail.getDuration();
	}

	public WorkCalendar getEffectiveWorkCalendar()
	{
		return detail.getEffectiveWorkCalendar();
	}

	public long getFinish()
	{
		return detail.getFinish();
	}

	public static Assignment getInstance( 
		Task task,
		Resource resource,
		double units,
		int requestDemandType )
	{
		return new Assignment(task, resource, units, requestDemandType);
	}

	public static Assignment getInstance( 
		Task task,
		Resource resource,
		double units,
		long delay )
	{
		return new Assignment(task, resource, units, delay);
	}

	public final double getLaborUnits()
	{
		if (isLabor())
		{
			return getUnits();
		}

		return 0.0D;
	}

	public long getLevelingDelay()
	{
		return detail.getLevelingDelay();
	}

	public static Field getRateField()
	{
		return Configuration.getFieldFromId( "Field.rate" );
	}

	public int getRequestDemandType()
	{
		return detail.getRequestDemandType();
	}

	public static Field getRequestDemandTypeField()
	{
		return Configuration.getFieldFromId( "Field.requestDemandType" );
	}

	/**
	 * @return Returns the resource.
	 */
	public Resource getResource()
	{
		return detail.getResource();
	}

	public String getResourceId()
	{
		return "" + getResource().getId();
	}

	public String getResourceName()
	{
		return getResource().getName();
	}

	public long getStart()
	{
		return detail.getStart();
	}

	/**
	 * @return Returns the task.
	 */
	public Task getTask()
	{
		return detail.getTask();
	}

	public String getTaskId()
	{
		return "" + getTask().getId();
	}

	public String getTaskName()
	{
		return getTask().getName();
	}

	/**
	 * Accessor for units (value of work)
	 * @return units
	 */
	public final double getUnits()
	{
		return detail.getUnits();
	}

	//private
	public static Field getUnitsField()
	{
		return Configuration.getFieldFromId( "Field.assignmentUnits" );
	}

	/**
	 * @return Returns the contour.
	 */
	public AbstractContour getWorkContour()
	{
		return detail.getWorkContour();
	}

	public static Predicate instanceofPredicate()
	{
		return new Predicate()
			{
				public boolean evaluate( 
					Object arg0 )
				{
					return arg0 instanceof Assignment;
				}
			};
	}

	public boolean isExternal()
	{
		return getTask().isExternal();
	}

	public boolean isInitialized()
	{
		return getProject().isInitialized();
	}

	public boolean isProratedCost()
	{
		return detail.getResource().getAccrueAt() == Accrual.PRORATED;
	}

	public boolean isReadOnly()
	{
		return getTask().isReadOnly();
	}

	public void makeFlatIfPossible()
	{
		if (getWorkContour().isPersonal())
		{
			detail.recalculateDuration(); // set duration from contour
			detail.setWorkContour( ((PersonalContour)getWorkContour()).convertToFlatIfPossible() );
		}
	}

	public void setActualWork( 
		long actualWork,
		FieldContext fieldContext )
	{
		long workValue = Duration.millis( actualWork );

		if (FieldContext.hasInterval( fieldContext ))
		{
			boolean isComplete = getPercentComplete() == 1.0D;
			long oldWork = work();

			if (!addWorkingTimeIfRequired( actualWork, fieldContext, true ))
			{
				return;
			}

			WorkCalendar cal = getEffectiveWorkCalendar();

			Task currentTask = this.getTask();
			long fieldStart = fieldContext.getStart();
			long adjustedFieldStart = cal.adjustInsideCalendar( fieldContext.getStart(), false );
			long taskActualStart = currentTask.getActualStart();

			//			long actualStart = this.getActualStart();

			//if the task isn't yet started and setting work, move to start date
			if (actualWork != 0)
			{
				//				if (taskActualStart == 0){
				//					// JGao - 8/25/2009 If this is the first time actual logged, any delay needs to be deleted. This is due to the fact
				//					// that task and this assignment will have same start date, i.e. no delay any longer.
				//					if (this.getDelay() > 0)
				//						this.setDelay( 0 );
				//
				//					currentTask.getCurrentSchedule().setStart(adjustedFieldStart);
				//				}
				if ((taskActualStart == 0) || (adjustedFieldStart < taskActualStart))
				{
					currentTask.getCurrentSchedule().setStart( adjustedFieldStart );
					currentTask.setActualStart( adjustedFieldStart );
					this.setActualStart( adjustedFieldStart );

					if (this.getDelay() > 0)
					{
						this.setDelay( 0 );
					}
				}

				long actualStart = this.getActualStart();

				if (actualStart == 0)
				{
					this.setActualStart( adjustedFieldStart );
				}

				//				getTask().setActualStart(adjustedFieldStart);
				//				this.setActualStart(adjustedFieldStart);
			}

			long stop = getStop();

			// JGao 8/13/2009 Added logic below to align contour bucket duration if there are gaps in timesheet entries.
			// This way ensures that they will be zeroed out correctly after the actual work is set.
			long actualGap = cal.compare( fieldStart, stop, false );
			boolean isActualsProtected = isActualsProtected();
			long currentStart = getStart();

			if (isActualsProtected && (actualGap > 0) && (stop > 0))
			{
				if (!getWorkContour().isPersonal())
				{
					makeContourPersonal();
				}

				PersonalContour pc = (PersonalContour)getWorkContour();
				long extraDuration = cal.compare( fieldStart, currentStart, false );

				newDetail().setWorkContour( pc.adjustDuration( extraDuration, 0, getUnits() ) );
			}

			setWork( actualWork, fieldContext ); //when entering time phased actuals, first thing is adjust the work contour

			// need to 0 out any time between stop and the intervals start, if any
			if ((stop > 0) && (actualGap > 0))
			{
				FieldContext empty = new FieldContext();
				empty.setInterval( new ImmutableInterval((stop == 0)
						? getStart()
						: stop, fieldContext.getStart()) );
				empty.setForceValue( isActualsProtected );
				setWork( 0L, empty );
			}

			if (workValue > 0)
			{
				if (fieldContext.getEnd() > stop) // if will set stop later because new work is after stop
				{
					setStop( fieldContext.getEnd() );
				}
				else
				{
					setStop( stop ); // use current stop - for case when setting actuals before current stop
				}
			}
			else
			{
				if ((fieldContext.getStart() < stop) && (fieldContext.getEnd() >= stop))
				{ //if overlapping current stop
					setStop( fieldContext.getStart() );
				}
				else
				{
					if (stop != 0)
					{
						setStop( stop ); // make sure stop stays where it is
					}
				}
			}

			// Need to get the start again as it might have been changed
			currentStart = getStart();

			if ((workValue > 0) && (stop == 0) && (fieldStart > currentStart))
			{
				long shiftAmount = cal.compare( fieldStart, currentStart, false );
				long oldDelay = detail.getDelay();
				newDetail().setDelay( oldDelay + shiftAmount );
			}

			long remainingWork = oldWork - getActualWork( null );

			if (remainingWork < 0)
			{
				remainingWork = 0;
			}

			if (isComplete == false)
			{
				((Task)getTask()).getSchedulingRule().adjustRemainingWork( this, remainingWork, false );
			}

			getTask().recalculateLater( this ); // task needs to be recalculated
		}
		else
		{
			if (workValue == 0)
			{
				setPercentComplete( 0 );

				return;
			}

			if (workValue > work()) // if greater than existing work, extended
			{
				setWork( workValue, null );
			}

			long date = ReverseQuery.getDateAtValue( TimeDistributedConstants.WORK, this, workValue, false );
			setStop( date );
		}
	}

	@Override
	public void setDelay( 
		long delay )
	{
		newDetail().setDelay( delay );
	}

	public void setDuration( 
		long duration,
		FieldContext context )
	{
		if (isActualsProtected() && (getActualDuration() > duration)) // dont allow reduction of actuals if actuals are protected
		{
			duration = getActualDuration();
		}

		setDuration( duration );
	}

	@Override
	public void setDuration( 
		long duration )
	{
		adjustRemainingDuration( duration - getActualDuration(), false );
	}

	TaskSchedule getTaskScheduleOfAssignment()
	{
		return detail.getTaskSchedule();
	}

	boolean isInRange( 
		long start,
		long finish )
	{
		long s = getStart();

		return ((finish > s) && (start < getEffectiveWorkCalendar().add( s, detail.getDuration(), true )));
	}

	/**
	 * Fills in a SelectFrom clause with "select work from contour"
	 * @param clause to fill in
	 * @return work field functor
	 */
	AssignmentFieldFunctor work( 
		SelectFrom clause )
	{
		ContourBucketIntervalGenerator contour = contourGeneratorInstance( TimeDistributedConstants.WORK );
		WorkFunctor workF = WorkFunctor.getInstance( this, contour.getWorkCalendar(), contour, detail.calcOvertimeUnits() );
		clause.select( workF ).from( contour );

		return workF;
	}

	private long getCompletedOrStatusDate()
	{
		return Math.min( getStatusDate(), detail.getStop() );
	}

	@Override
	public final long getCompletedThrough()
	{
		return detail.getCompletedThrough();
	}

	@Override
	public long getConstraintDate()
	{
		return 0;
	}

	@Override
	public int getConstraintType()
	{
		return 0;
	}

	public double getCost()
	{
		return calcAll( TimeDistributedConstants.COST );
	}

	@Override
	public double getCost( 
		FieldContext fieldContext )
	{
		return cost( FieldContext.start( fieldContext ), FieldContext.end( fieldContext ) );
	}

	@Override
	public int getCostRateIndex()
	{
		return detail.getCostRateIndex();
	}

	@Override
	public double getCpi( 
		FieldContext fieldContext )
	{
		return EarnedValueCalculator.getInstance().cpi( this, FieldContext.start( fieldContext ), FieldContext.end( fieldContext ) );
	}

	/**
	 * @return
	 */
	@Override
	public Date getCreated()
	{
		return hasKey.getCreated();
	}

	@Override
	public double getCsi( 
		FieldContext fieldContext )
	{
		return EarnedValueCalculator.getInstance().csi( this, FieldContext.start( fieldContext ), FieldContext.end( fieldContext ) );
	}

	@Override
	public double getCv( 
		FieldContext fieldContext )
	{
		return EarnedValueCalculator.getInstance().cv( this, FieldContext.start( fieldContext ), FieldContext.end( fieldContext ) );
	}

	@Override
	public double getCvPercent( 
		FieldContext fieldContext )
	{
		return EarnedValueCalculator.getInstance()
									.cvPercent( this, FieldContext.start( fieldContext ), FieldContext.end( fieldContext ) );
	}

	/**
	 * This function will calculate the exact date/time when the value is achieved
	 * @param type
	 * @param value
	 * @return
	 */
	public long getDateAtValue( 
		Object type,
		double value )
	{
		SelectFrom clause = SelectFrom.getInstance();
		AssignmentFieldFunctor dataFunctor = (type == TimeDistributedConstants.COST)
			? cost( clause, true )
			: work( clause );

		DateAtValueFunctor dateAtValue = DateAtValueFunctor.getInstance( value,
				AssignmentFieldClosureCollection.getInstance( dataFunctor ) );
		clause.select( dateAtValue ); // override existing select

		Query.getInstance().selectFrom( clause ).execute();

		return dateAtValue.getDate();
	}

	public long getDateAtWorkFraction( 
		double workFraction )
	{
		//		System.out.println("all work is " + calcAll(WORK) / (1000*60*60*8));
		return getDateAtValue( TimeDistributedConstants.WORK, calcAll( TimeDistributedConstants.WORK ) * workFraction );
	}

	public long getDeadline()
	{ // needed for indicators

		return 0;
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

	public String getDelegatedToName()
	{
		return getTask().getDelegatedToName();
	}

	@Override
	public long getDependencyStart()
	{
		return detail.getDependencyStart();
	}

	@Override
	public Document getDocument()
	{
		return getProject();
	}

	public Document getDocument( 
		boolean leftObject )
	{
		return (leftObject)
		? getTask().getDocument()
		: getResource().getDocument();
	}

	@Override
	public long getDuration()
	{
		return detail.getDuration();
	}

	@Override
	public double getEac( 
		FieldContext fieldContext )
	{
		return EarnedValueCalculator.getInstance().eac( this, FieldContext.start( fieldContext ), FieldContext.end( fieldContext ) );
	}

	@Override
	public final long getEarliestStop()
	{
		return detail.getEarliestStop();
	}

	@Override
	public long getElapsedDuration()
	{
		return detail.getElapsedDuration();
	}

	@Override
	public long getEnd()
	{
		return detail.getEnd();
	}

	@Override
	public long getEstimatedFinish()
	{
		return estimatedFinish;
	}

	@Override
	public double getEstimatedPercentComplete()
	{
		return estimatedPercentComplete;
	}

	@Override
	public long getEstimatedRemainingWork()
	{
		return estimatedRemainingWork;
	}

	@Override
	public long getEstimatedStart()
	{
		return estimatedStart;
	}

	@Override
	public long getEstimatedWork()
	{
		return estimatedWork;
	}

	@Override
	public final Map getExtraFields()
	{
		if (extraFields == null)
		{
			extraFields = new HashMapWithDirtyFlags();
		}

		return extraFields;
	}

	@Override
	public long getFinishOffset()
	{
		return EarnedValueCalculator.getInstance().getFinishOffset( this );
	}

	@Override
	public double getFixedCost( 
		FieldContext fieldContext )
	{
		return 0;
	}

	/**
	 * @return
	 */
	@Override
	public long getId()
	{
		return hasKey.getId();
	}

	@Override
	public HasIndicators getIndicators()
	{
		return this;
	}

	@Override
	public final long getLastTimesheetUpdate()
	{
		return lastTimesheetUpdate;
	}

	/* (non-Javadoc)
	 * @see com.projity.util.Association#getLeft()
	 */
	@Override
	public Object getLeft()
	{
		return getTask();
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.Allocation#getMostLoadedAssignmentUnits()
	 */
	@Override
	public double getMostLoadedAssignmentUnits()
	{
		return getUnits();
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public String getName()
	{
		return "" + getLeft() + " " + getRight();
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public String getName( 
		FieldContext context )
	{
		if (context == null)
		{
			return "???"; //fix
		}

		if (context.isLeftAssociation())
		{
			return getRight().toString();
		}
		else
		{
			return getLeft().toString();
		}
	}

	@Override
	public String getNotes()
	{
		return null;
	}

	public Project getOwningProject()
	{
		Project p = getTask().getOwningProject();

		if (p == null)
		{
			p = getProject();
		}

		return p;
	}

	@Override
	public double getPercentComplete()
	{
		return detail.getPercentComplete();
	}

	@Override
	public double getPercentWorkComplete()
	{
		return (double)(getActualWork( null )) / getWork( null );
	}

	@Override
	public Project getProject()
	{
		return getTask().getProject();
	}

	@Override
	public String getProjectName()
	{
		return getOwningProject().getName();
	}

	private MutableInterval getRangeThatIntervalCanBeMoved( 
		long start,
		long startOffset,
		long endOffset )
	{
		MutableInterval range = getRangeThatIntervalCanBeMoved( startOffset, endOffset );

		if (getEffectiveWorkCalendar().compare( getStart(), start, false ) == 0)
		{
			range.setStart( range.getStart() - calcTotalDelay() );
		}

		return range;
	}

	public MutableInterval getRangeThatIntervalCanBeMoved( 
		long startOffset,
		long endOffset )
	{
		return detail.getRangeThatIntervalCanBeMoved( startOffset, endOffset );
	}

	@Override
	public final Rate getRate()
	{
		return detail.getRate();
	}

	public RateFormat getRateFormat()
	{
		return getResource().getRateFormat();
	}

	//	public boolean isNew() {
	//		return hasKey.isNew();
	//	}
	@Override
	public double getRemainingCost( 
		FieldContext fieldContext )
	{
		return getCost( fieldContext ) - getActualCost( fieldContext );
	}

	@Override
	public long getRemainingDuration()
	{
		return detail.getRemainingDuration();
	}

	public final double getRemainingLaborUnits()
	{
		if (isLabor())
		{
			return getRemainingUnits();
		}

		return 0.0D;
	}

	@Override
	public final double getRemainingUnits()
	{
		if (!isLabor())
		{
			return 1.0D;
		}

		long duration = getRemainingDuration();

		if (duration == 0.0)
		{
			//			return getWorkContour().getLastBucketUnits();
			return getUnits(); // degeneratate case
		}

		long work = getRemainingWork();

		if (work == 0) // degenerate case with no work yet
		{
			return 1.0;
		}

		return ((double)work) / duration;
	}

	@Override
	public long getRemainingWork( 
		FieldContext fieldContext )
	{
		return remainingWork( FieldContext.start( fieldContext ), FieldContext.end( fieldContext ) );
	}

	@Override
	public long getRemainingWork()
	{
		return getRemainingWork( null );
	}

	@Override
	public long getResourceAvailability()
	{
		return detail.getResourceAvailability();
	}

	@Override
	public long getResume()
	{
		return detail.getResume();
	}

	/* (non-Javadoc)
	 * @see com.projity.util.Association#getRight()
	 */
	@Override
	public Object getRight()
	{
		return getResource();
	}

	public ImageLink getScheduleStatusIndicator()
	{
		return EarnedValueCalculator.getInstance().getBudgetStatusIndicator( getSpi( null ) );
	}

	@Override
	public double getSpi( 
		FieldContext fieldContext )
	{
		return EarnedValueCalculator.getInstance().spi( this, FieldContext.start( fieldContext ), FieldContext.end( fieldContext ) );
	}

	@Override
	public long getStartOffset()
	{
		return EarnedValueCalculator.getInstance().getStartOffset( this );
	}

	private long getStatusDate()
	{
		return getProject().getStatusDate();
	}

	@Override
	public long getStop()
	{
		return detail.getStop();
	}

	@Override
	public double getSv( 
		FieldContext fieldContext )
	{
		return EarnedValueCalculator.getInstance().sv( this, FieldContext.start( fieldContext ), FieldContext.end( fieldContext ) );
	}

	@Override
	public double getSvPercent( 
		FieldContext fieldContext )
	{
		return EarnedValueCalculator.getInstance()
									.svPercent( this, FieldContext.start( fieldContext ), FieldContext.end( fieldContext ) );
	}

	private int getTaskSchedulingType()
	{
		return ((Task)getTask()).getSchedulingType();
	}

	@Override
	public double getTcpi( 
		FieldContext fieldContext )
	{
		return EarnedValueCalculator.getInstance().tcpi( this, FieldContext.start( fieldContext ),
			FieldContext.end( fieldContext ) );
	}

	@Override
	public String getTimeUnitLabel()
	{
		return getResource().getTimeUnitLabel();
	}

	public Assignment getTimesheetAssignment()
	{
		if (isTimesheetAssignment())
		{
			return this;
		}

		return detail.getBaselineAssignment( Snapshottable.TIMESHEET, false );
	}

	@Override
	public long getTimesheetFinish()
	{
		return getCachedEnd().getTime();
	}

	@Override
	public long getTimesheetStart()
	{
		return getCachedStart().getTime();
	}

	@Override
	public final int getTimesheetStatus()
	{
		Assignment ts = getTimesheetAssignment();

		if (ts != null)
		{
			return ts.timesheetStatus;
		}

		return timesheetStatus;
	}

	@Override
	public String getTimesheetStatusName()
	{
		return TimesheetHelper.getTimesheetStatusName( getTimesheetStatus() );
	}

	public String getTimesheetStatusStyle()
	{ // used for display style in web

		return TimesheetHelper.getTimesheetStatusStyle( getTimesheetStatus() );
	}

	/**
	 * @return
	 */
	@Override
	public long getUniqueId()
	{
		return hasKey.getUniqueId();
	}

	public String getUniqueIdString()
	{
		return getTask().getUniqueId() + "." + getResource().getUniqueId();
	}

	@Override
	public double getVac( 
		FieldContext fieldContext )
	{
		return EarnedValueCalculator.getInstance().vac( this, FieldContext.start( fieldContext ), FieldContext.end( fieldContext ) );
	}

	@Override
	public long getWork( 
		FieldContext fieldContext )
	{
		long w = work( FieldContext.start( fieldContext ), FieldContext.end( fieldContext ) );

		if (!isLabor())
		{
			//			System.out.println("work before setting non temporal" + DurationFormat.format(w));
			w = Duration.setAsNonTemporal( w );
		}

		return w;
	}

	@Override
	public WorkCalendar getWorkCalendar()
	{
		return this.getEffectiveWorkCalendar();
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.AssignmentSpecificFields#getWorkContourId()
	 */
	@Override
	public int getWorkContourType()
	{
		return getWorkContour().getType();
	}

	public final int getWorkflowState()
	{
		return workflowState;
	}

	/**
	 * See if assignment duration is empty
	 * @return
	 */
	public boolean hasDuration()
	{
		return detail.hasDuration();
	}

	public boolean inProgress()
	{
		double percentComplete = getPercentComplete();

		return ((percentComplete > 0.0D) && (percentComplete < 1.0D));
	}

	public void invalidateAssignmentCalendar()
	{
		detail.invalidateAssignmentCalendar();
	}

	public boolean isActiveBetween( 
		long start,
		long end )
	{
		return getEffectiveWorkCalendar().compare( end, start, false ) > 0;
	}

	public boolean isActualsProtected()
	{
		return getProject().isActualsProtected() && (getResource() != ResourceImpl.getUnassignedInstance());
	}

	public boolean isAssignment()
	{
		return true;
	}

	private boolean isBaselineFieldHidden( 
		int numBaseline,
		FieldContext fieldContext )
	{
		Assignment baselineAssignment = getBaselineAssignment( new Integer(numBaseline), false );

		if (baselineAssignment == null)
		{
			return true;
		}

		if (fieldContext == null) // the baseline exists, but no time range
		{
			return false;
		}

		return ((fieldContext.getStart() >= baselineAssignment.getFinish()) ||
		(fieldContext.getEnd() <= baselineAssignment.getStart()));
	}

	@Override
	public boolean isComplete()
	{
		return getPercentComplete() == 1.0D;
	}

	public boolean isCreatedFromTimesheet()
	{
		return createdFromTimesheet;
	}

	public boolean isCritical()
	{
		return false;
	}

	@Override
	public boolean isDefault()
	{
		return (getTask() == Model.getUnassignedTaskInstance()) || (getResource() == ResourceImpl.getUnassignedInstance());
	}

	@Override
	public boolean isDirty()
	{
		return dirty;
	}

	private boolean isEarnedValueFieldHidden( 
		FieldContext fieldContext )
	{
		if (isFieldHidden( fieldContext ))
		{
			return true;
		}

		return getStatusDate() < fieldContext.getStart();
	}

	private boolean isFieldHidden( 
		FieldContext fieldContext )
	{
		return (fieldContext != null) && !isInRange( fieldContext.getStart(), fieldContext.getEnd() );
	}

	@Override
	public boolean isInvalidIntersectionCalendar()
	{
		return detail.isInvalidIntersectionCalendar();
	}

	@Override
	public boolean isJustModified()
	{
		Task task = getTask();

		if (task == null)
		{
			return false;
		}
		else
		{
			return task.isJustModified();
		}
	}

	@Override
	public boolean isLabor()
	{
		return getResource().isLabor();
	}

	@Override
	public boolean isLocal()
	{
		return true;
	}

	@Override
	public boolean isMaterial()
	{
		return getResource().isMaterial();
	}

	public boolean isMilestone()
	{
		return false;
	}

	public boolean isMine()
	{
		return getResource().isMe();
	}

	//for gantt bar formula
	//probably to add in a common interface with task
	public boolean isNormal()
	{
		return false;
	}

	public boolean isParentWithAssignments()
	{
		return getTask().isWbsParent();
	}

	public boolean isPendingTimesheetUpdate()
	{
		return (getTimesheetStatus() == TimesheetStatus.VALIDATED);
	}

	/**
	 * Protected against deletion
	 */
	public boolean isProtected()
	{
		if (!isActualsProtected())
		{
			return false;
		}

		if (areActuals() && !canDeleteActuals())
		{
			return true;
		}

		return false;
	}

	public boolean isReadOnlyActualDuration( 
		FieldContext fieldContext )
	{
		return detail.isReadOnlyActualDuration( fieldContext );
	}

	public boolean isReadOnlyActualFinish( 
		FieldContext fieldContext )
	{
		return detail.isReadOnlyActualFinish( fieldContext );
	}

	public boolean isReadOnlyActualStart( 
		FieldContext fieldContext )
	{
		return detail.isReadOnlyActualStart( fieldContext );
	}

	public boolean isReadOnlyActualWork( 
		FieldContext fieldContext )
	{
		return isActualsProtected();
	}

	public boolean isReadOnlyComplete( 
		FieldContext context )
	{
		return detail.isReadOnlyComplete( context );
	}

	public boolean isReadOnlyCompletedThrough( 
		FieldContext context )
	{
		return detail.isReadOnlyCompletedThrough( context );
	}

	public boolean isReadOnlyCost( 
		FieldContext fieldContext )
	{
		return true;
	}

	public boolean isReadOnlyFixedCost( 
		FieldContext fieldContext )
	{
		return true;
	}

	public boolean isReadOnlyPercentComplete( 
		FieldContext context )
	{
		return isActualsProtected();
	}

	@Override
	public boolean isReadOnlyRemainingWork( 
		FieldContext fieldContext )
	{
		return isActualsProtected() || isReadOnlyWork( fieldContext );
	}

	@Override
	public boolean isReadOnlyTimesheetClosed( 
		FieldContext fieldContext )
	{
		return detail.isReadOnlyTimesheetClosed( fieldContext );
	}

//	@Override
//	public boolean isReadOnlyUnits( 
//		FieldContext fieldContext )
//	{
//		return false;
//	}

	@Override
	public boolean isReadOnlyWork( 
		FieldContext fieldContext )
	{
		if (fieldContext == null)
		{
			return isMaterial(); //// material resource assignments cannot have their total work modified
		}

		// see if there is some calendar time
		return !isActiveBetween( fieldContext.getStart(), fieldContext.getEnd() );
	}

	public boolean isResourceComplete()
	{
		return isTimesheetComplete() && (getPercentComplete() == 1.0D);
	}

	public boolean isSubproject()
	{
		return false;
	}

	public boolean isSummary()
	{
		return false;
	}

	public boolean isTaskComplete()
	{
		return isTimesheetClosed() && (getPercentComplete() == 1.0D);
	}

	public boolean isTemporal()
	{
		return detail.isTemporal();
	}

	public final boolean isTimesheetAssignment()
	{
		return timesheetAssignment;
	}

	public boolean isTimesheetClosed()
	{
		if (!isComplete() && (getDurationMillis() > 0L)) // if any remaining work, then must be able to enter timesheet info
		{
			return false;
		}

		if (getExtraFields() == null)
		{
			return false;
		}

		Field f = Configuration.getFieldFromId( "Field.timesheetClosed" );

		if (f == null)
		{
			return false;
		}

		Object result = getExtraFields().get( f.getAlternateId() );

		return (result == null)
		? false
		: (Boolean)result;
	}

	public boolean isTimesheetComplete()
	{
		return detail.isTimesheetComplete();
	}

	public boolean isTimesheetEditable()
	{
		return getTimesheetStatus() != TimesheetStatus.INTEGRATED;
	}

	public boolean isTimesheetEntered()
	{
		return getTimesheetStatus() != TimesheetStatus.ENTERED;
	}

	public boolean isTimesheetRejected()
	{
		return getTimesheetStatus() != TimesheetStatus.REJECTED;
	}

	public boolean isTimesheetValidated()
	{
		return getTimesheetStatus() != TimesheetStatus.VALIDATED;
	}

	public boolean isUnstarted()
	{
		return getPercentComplete() == 0.0D;
	}

	/**
	 * @return
	 */
	public void makeContourPersonal()
	{
//		if (getWorkContour().isPersonal())
//			return;
		Object type = TimeDistributedConstants.WORK;
		ContourBucketIntervalGenerator contourGenerator = contourGeneratorInstance( type ); //contour
		PersonalContourMaker contourBuilder = PersonalContourMaker.getInstance( this, contourGenerator );
		Query.getInstance().selectFrom( SelectFrom.getInstance().select( contourBuilder ).from( contourGenerator ).all() )
			 .execute();

		newDetail().setContour( type, contourBuilder.getList() );
		detail.recalculateDuration();
	}

	private void moveDelayToContour()
	{
		// Remove delay and add at beginning of contour
		long oldDelay = calcTotalDelay();

		if (oldDelay == 0)
		{
			return;
		}

		makeContourPersonal();
		setTotalDelay( 0 );
		setDurationMillis( getDurationMillis() + oldDelay );

		AbstractContour contour = PersonalContour.addEmptyBucket( getWorkContour(), oldDelay, false ); // add empty space before
		newDetail().setWorkContour( contour );
	}

	@Override
	public void moveInterval( 
		Object eventSource,
		long start,
		long end,
		ScheduleInterval oldInterval,
		boolean isChild )
	{
		long startShiftDuration = getEffectiveWorkCalendar().compare( start, oldInterval.getStart(), false );
		long endShiftDuration = getEffectiveWorkCalendar().compare( end, oldInterval.getEnd(), false );
		boolean shifting = (startShiftDuration != 0L) && (endShiftDuration != 0L);
		long stop = getStop(); // Store old completion

		if (shifting)
		{
			shift( oldInterval.getStart(), oldInterval.getEnd(), startShiftDuration );
		}
		else
		{
			if (endShiftDuration != 0)
			{
				extend( oldInterval.getStart(), oldInterval.getEnd(), endShiftDuration );
			}
			else
			{
				extendBefore( oldInterval.getStart(), oldInterval.getEnd(), startShiftDuration );
			}
		}

		setStop( stop ); // put back old completion
						 //Need to update schedule if the assignment bar was moved

		if (!isChild)
		{
			getTask().updateCachedDuration();
			getTask().recalculate( eventSource );
		}

		//		//Undo
		//		UndoableEditSupport undoableEditSupport=getProject().getUndoController().getEditSupport();
		//		if (undoableEditSupport!=null&&!(eventSource instanceof UndoableEdit)){
		//			undoableEditSupport.postEdit(new ScheduleEdit(this,new ScheduleInterval(start,end),oldInterval,isChild,eventSource));
		//		}
	}

	/**
	 * Move remaining work to the date given.  It will either be moved later or sooner.  Also, if at the date, there
	 * is a gap in the work, the gap is reduced so that the work begins on the date
	 * @param date
	 */
	public void moveRemainingToDate( 
		long date )
	{
		long resume = getResume();
		long end = getEnd();
		long shiftDuration = getEffectiveWorkCalendar().compare( date, resume, false ); // get shift time
		shift( resume, end, shiftDuration );

		long duration = getEffectiveWorkCalendar().compare( date, getStart(), false ); // get offset from start

		if (duration > 0)
		{
			newDetail().removeEmptyBucketAtDuration( duration );
		}
	}

	/**
	 * Clone the detail and set it
	 * @return cloned assignment detail
	 */
	private AssignmentDetail newDetail()
	{
		//TODO store off detail for undo
		//System.out.println("before clone " + new Date(detail.getStop()));
		detail = (AssignmentDetail)detail.clone();

		if (getTask() != null)
		{
			getTask().setDirty( true );
		}

		setDirty( true );

		//System.out.println("after clone " + new Date(detail.getStop()));
		return detail;
	}

	//	public AssignmentFieldFunctor work(SelectFrom clause, AssignmentFieldFunctor using) {
	//		ContourBucketIntervalGenerator contour = using.getContourBucketIntervalGenerator();
	//		WorkFunctor workF = WorkFunctor.getInstance(this, contour.getWorkCalendar(), contour, detail.calcOvertimeUnits());
	//		clause.select(workF);
	//		return workF;
	//	}
	private AssignmentFieldFunctor percentAlloc( 
		SelectFrom clause,
		boolean threshold )
	{
		ContourBucketIntervalGenerator contour = contourGeneratorInstance( TimeDistributedConstants.WORK );
		PercentAllocFunctor functor = PercentAllocFunctor.getInstance( this, contour.getWorkCalendar(), contour, threshold );
		clause.select( functor ).from( contour );

		return functor;
	}

	private void readObject( 
		ObjectInputStream s )
		throws IOException, ClassNotFoundException
	{
		s.defaultReadObject();
		hasKey = HasKeyImpl.deserialize( s, this );

		//	    myBarClosureInstance = new BarClosure();
	}

	@Override
	public long remainingWork( 
		long start,
		long end )
	{
		if (!isInRange( start, end ))
		{
			return NO_VALUE_LONG;
		}

		if (!isLabor()) // TODO this right?
		{
			return 0;
		}

		Query query = Query.getInstance();
		SelectFrom clause = SelectFrom.getInstance().whereInRange( Math.max( start, detail.getStop() ), Math.min( end, getEnd() ) );
		query.selectFrom( clause ).action( work( clause ) ).execute();

		return (long)((DoubleValue)query.getActionVisitor()).getValue();
	}

	public void removeRemainingWork()
	{
		long actualDuration = getActualDuration();
		setDuration( actualDuration );

		// JGao 8/4/2009 Need to set actual start and finish information if no actual hours logged
		if (actualDuration == 0)
		{
			long s = this.getStart();

			if (getTask().getActualStart() == 0)
			{
				getTask().setActualStart( s ); //TODO handle if setting non calendar time
			}

			this.setActualStart( s );
		}
	}

	@Override
	public boolean renumber( 
		boolean localOnly )
	{
		return hasKey.renumber( localOnly );
	}

	@Override
	public void replace( 
		Object newOne,
		boolean leftObject )
	{
		if (leftObject)
		{
			newDetail().setTask( (Task)newOne );
		}
		else
		{
			newDetail().setResource( (Resource)newOne );
		}
	}

	private void setDurationMillis( 
		long durationMillis )
	{
		newDetail().setDuration( durationMillis );
	}

	/**
	 * This treats just a single interval to merge in
	 * @param type - WORK or COST. can also be a time distributed field
	 * @param start - start of interval
	 * @param end - end of interval
	 * @param value - value of interval
	 */
	public void setInterval( 
		Object type,
		long start,
		long end,
		double value )
	{
		//TODO treat cost values - they are currently ignored
		if ((type == TimeDistributedConstants.ACTUAL_WORK) 
		 || (type == TimeDistributedConstants.WORK) 
		 || (type == TimeDistributedConstants.REMAINING_WORK))
		{
			if ((value != 0) && !verifyBounds( start, end ))
			{
				return;
			}

			if (Environment.isImporting())
			{
				WorkCalendar cal = getEffectiveWorkCalendar();
				start = cal.adjustInsideCalendar( start, false );

				// JGao - 9/16/2009 Added the following for importing into Mariner. This
				// should correct the end time as Mariner calendar have no break time
				if ((type != TimeDistributedConstants.ACTUAL_WORK) && Environment.isNoPodServer())
				{
					end = cal.add( start, (long)(value / getUnits()), true );
				}

				end = cal.adjustInsideCalendar( end, true );
			}

			setWorkInterval( start, end, value, false );

			if ((type == TimeDistributedConstants.ACTUAL_WORK) && (end > getStop()) && (value > 0.0D)) // bug fix - actuals were lost
			{
				setStop( end );
			}
		}
		else
		{
			if (TimeDistributedHelper.isWork( type ))
			{
				Object baseline = TimeDistributedHelper.baselineForData( type );

				//System.out.println("baseline " + baseline + " " + new Date(start) + " " + new Date(end) + " " + value);
				Assignment baselineAssignment = getBaselineAssignment( baseline, true ); // get or create baseline assignment

				// update the task schedule too
				TaskSchedule schedule = baselineAssignment.getTaskScheduleOfAssignment();
				long baselineStart = schedule.getStart();
				long baselineFinish = schedule.getFinish();

				if ((baselineStart == 0) || (baselineStart > start))
				{
					schedule.setStart( start );
				}

				if ((baselineFinish == 0) || (baselineFinish < end))
				{
					schedule.setEnd( end );
				}

				baselineAssignment.setWorkInterval( start, end, value, false );
			}
		}
	}

	@Override
	public void setLevelingDelay( 
		long levelingDelay )
	{
		newDetail().setLevelingDelay( levelingDelay );
	}

	public void setOvertimeWork( 
		long overtimeWork )
	{
		newDetail().setOvertimeWork( overtimeWork );
	}

	@Override
	public void setRequestDemandType( 
		int requestDemandType )
	{
		newDetail().setRequestDemandType( requestDemandType );
	}

	public void setStart( 
		long start )
	{
		detail.setStart( start );
	}

	public void setWork( 
		long work,
		FieldContext context )
	{
		work = Duration.millis( work );

		if (isActualsProtected() && !FieldContext.isForceValue( context ) && (work < getActualWork( context )))
		{
			work = getActualWork( context );
		}

		// JGao -- 7/20/2009 I added the abs to safe guard negative value. The value should not be negative
		// but just in case.
		if (isLabor() && (Math.abs( work ) < 60000) && (work != 0))
		{
			work *= Duration.timeUnitFactor( TimeUnit.HOURS );
			System.out.println( "modifying invalid work to make it hours" );
		}

		long remainingWork = work - getActualWork( null );
		WorkCalendar cal = getEffectiveWorkCalendar();

		if (!FieldContext.hasInterval( context ))
		{
			long currentRemainingWork = Duration.millis( getWork( context ) ) - Duration.millis( getActualWork( context ) );

			if ((currentRemainingWork == 0) && (remainingWork == 0))
			{
				adjustRemainingUnits( 0, 0, true, false ); //TODO is this ok?
			}
			else
			{
				if (getTaskSchedulingType() == SchedulingType.FIXED_UNITS)
				{
					adjustRemainingDuration( remainingWork, false );
				}
				else if ((getTaskSchedulingType() == SchedulingType.FIXED_DURATION) && (currentRemainingWork > 0))
				{
					adjustRemainingWork( ((double)remainingWork) / currentRemainingWork, true );
				}

				//					newDetail().setWorkContour(getWorkContour().adjustUnits((((double)remainingWork) / currentRemainingWork), getActualDuration()));
				else
				{
					newDetail().adjustRemainingDuration( remainingWork ); // just set duration
				}
			}
		}
		else
		{
			long start = cal.adjustInsideCalendar( context.getStart(), false );
			long end = cal.adjustInsideCalendar( context.getEnd(), true );

			if (end > start)
			{
				setWorkInterval( start, end, work, false );
			}

			if (getRate().getValue() == 0.0D)
			{ // special case if units was 0

				double rate = ((double)work) / cal.compare( end, start, false ); // use rate for the period as the assignment's rate
				this.forceUnits( rate );
			}
		}
	}

	/**
	 * This treats just a single interval to merge in
	 * @param start - start of interval
	 * @param end - end of interval
	 * @param value - value of interval (an wmount of work (duration), not units)
	 * @param forceValue TODO
	 * @param type - WORK or COST
	 */
	private void setWorkInterval( 
		long start,
		long end,
		double value,
		boolean forceValue )
	{
		moveDelayToContour();

		long assignmentStart = getStart(); // the start including any delay
		WorkCalendar cal = getEffectiveWorkCalendar();

		if (end > getFinish())
		{ //  add end if necessary

			if ((value == 0) && !forceValue)
			{
				this.setEnd( start );

				return; // don't extend if value is 0
			}

			makeContourPersonal();

			long extraDuration = cal.compare( end, getFinish(), false );
			setDurationMillis( getDurationMillis() + extraDuration );

			AbstractContour contour = PersonalContour.addEmptyBucket( getWorkContour(), extraDuration, true );
			newDetail().setWorkContour( contour );

			// adjust task end too
			if (end > getTask().getEnd())
			{
				getTask().setEnd( end );
			}
		}
		else if (start < assignmentStart)
		{ // if before assignment start
		  //			if (value == 0)
		  //				return; // don't extend if value is 0

			long taskStart = getTask().getStart();

			if (start < taskStart)
			{
				long shiftAmount = cal.compare( taskStart, start, false );
				setDurationMillis( getDurationMillis() + shiftAmount );

				if (!getWorkContour().isPersonal())
				{
					makeContourPersonal();
				}

				newDetail().setWorkContour( PersonalContour.addEmptyBucket( getWorkContour(), shiftAmount, false ) ); // add empty space before
				getTask().setScheduleConstraintAndUpdate( ConstraintType.SNLT, start ); // this can set start before project duration.
				taskStart = getTask().getStart();
			}

			assignmentStart = taskStart;
		}

		long startDuration = cal.compare( start, assignmentStart, false );
		long endDuration = cal.compare( end, assignmentStart, false );

		if (startDuration == endDuration)
		{
			return;
		}

		if (!getWorkContour().isPersonal())
		{
			makeContourPersonal();
		}

		value /= (endDuration - startDuration);

		PersonalContour newContour = ((PersonalContour)getWorkContour()).setInterval( startDuration, endDuration, value );

		extractDelayFromContour( newContour ); // move delay back into delay field from contour
		newDetail().setWorkContour( newContour );
		newDetail().recalculateDuration();
		getTask().updateCachedDuration();
	}

	public void shift( 
		long start,
		long end,
		long shiftDuration )
	{
		//get bounds.
		WorkCalendar cal = getEffectiveWorkCalendar();

		if (getTask().inProgress() && (cal.compare( getStop(), start, false ) > 0))
		{ // if stop greater than start, can't change it
		  //	System.out.println("Not shifting - stop = " + new Date(getStop()) + "start " + new Date(start));

			return;
		}

		long taskStart = getTask().getStart();
		start = Math.max( start, getStop() ); //only shift remaining
		start = Math.max( start, taskStart ); // dont shift before task start of course

		long startOffset = cal.compare( start, taskStart, false );
		long endOffset = cal.compare( end, taskStart, false );

		if ((getResume() != 0) && (start >= getResume()))
		{
			long splitDuration = detail.getSplitDuration();
			startOffset -= splitDuration;
			endOffset -= splitDuration;
		}

		if (startOffset >= endOffset) // this prevents moving a completed interval
		{
			return;
		}

		boolean firstBar = (cal.compare( getStart(), start, false ) == 0);
		MutableInterval range = getRangeThatIntervalCanBeMoved( start, startOffset, endOffset );

		if (firstBar)
		{
			range.setStart( range.getStart() - calcTotalDelay() );
		}

		if (shiftDuration > 0)
		{
			shiftDuration = Math.min( shiftDuration, range.getEnd() - endOffset ); // don't allow to shift more than possible
		}
		else
		{
			shiftDuration = Math.max( shiftDuration, range.getStart() /*+ (firstBar ? -calcTotalDelay() : 0)*/ - startOffset ); // don't allow to shift more than possible
		}

		if (firstBar)
		{
			setTotalDelay( calcTotalDelay() + shiftDuration );
		}
		else
		{
			newDetail().shift( startOffset, endOffset, shiftDuration );
		}
	}

	/**
	 * For use in sharing with external systems such as salesforce. Uniquely identifies object
	 * @return
	 */
	public String toExternalId()
	{
		return getResourceId() + "." + getTaskId();
	}

	public String toString()
	{
		return getTask() + "/" + getResource(); //super.toString();
												//		return "[start] " + new java.util.Date(getStart())
												//		      + "\n[end] " +  new java.util.Date(getEnd())
												//			  +"\n[units] " + getUnits()// in hours
												//			  +"\n[work] " + getWork() / (1000*60*60); // in hours
	}

	public boolean verifyBounds( 
		long start,
		long end )
	{
		if (Environment.isImporting()) // always accept time distributed edits when importing
		{
			return true;
		}

		if (getProject().getStart() > start) // if setting before project start
		{
			return Alert.okCancel( Messages.getString( "Message.allowDistrbutedStartBeforeProjectStart" ) );
		}
		else if (start < getTask().getStart())
		{
			return Alert.okCancel( Messages.getString( "Message.allowDistrbutedStartBeforeTaskStart" ) );
		}
		else
		{
			return true;
		}
	}

	/**
	 * Set the total delay for the assignment.  Priority is given to changing the assignment delay over the
	 * leveling delay.
	 * @param newDelay
	 */
	void setTotalDelay( 
		long newDelay )
	{
		/*
		 *   DDDDDDDLLLLLL
		 *   NNNNNNNNNN
		 */

		//		long delay = getDelay();
		//		long levelingDelay = getLevelingDelay();
		//		long oldTotalDelay = calcTotalDelay();
		//		if (newDelay == oldTotalDelay)
		//			return;
		//
		//		if (newDelay <= delay) {
		//			delay = newDelay;
		//			levelingDelay = 0;
		//		}
		//		else {
		//			delay = 0;
		//			levelingDelay =
		//		}
		//		if (newDelay < oldTotalDelay) {
		//			delay -= newDelay;
		//			if (delay < 0) {
		//				levelingDelay += delay;
		//				delay = 0;
		//			}
		//		} else {
		//			delay += (newDelay - oldTotalDelay);
		//		}
		//		setDelay(delay);
		//		setLevelingDelay(levelingDelay);
		setDelay
		/*
		 *   DDDDDDDLLLLLL
		 *   NNNNNNNNNN
		 */

		//		long delay = getDelay();
		//		long levelingDelay = getLevelingDelay();
		//		long oldTotalDelay = calcTotalDelay();
		//		if (newDelay == oldTotalDelay)
		//			return;
		//
		//		if (newDelay <= delay) {
		//			delay = newDelay;
		//			levelingDelay = 0;
		//		}
		//		else {
		//			delay = 0;
		//			levelingDelay =
		//		}
		//		if (newDelay < oldTotalDelay) {
		//			delay -= newDelay;
		//			if (delay < 0) {
		//				levelingDelay += delay;
		//				delay = 0;
		//			}
		//		} else {
		//			delay += (newDelay - oldTotalDelay);
		//		}
		//		setDelay(delay);
		//		setLevelingDelay(levelingDelay);
		( newDelay );
		setLevelingDelay( 0 );
	}

	/** This version does not depend on the assignment
	 *
	 * @param clause
	 * @param resource
	 * @return
	 */
	private static AssignmentFieldFunctor resourceAvailability( 
		SelectFrom clause,
		Resource resource )
	{
		//TODO this is untested, as histogram is currently broken 8/12/04 hk
		ResourceAvailabilityFunctor functor = ResourceAvailabilityFunctor.getInstance( resource );
		CollectionIntervalGenerator<ValueObjectForInterval> availability = CollectionIntervalGenerator.getInstance( 
			resource.getAvailabilityTable().getList() );
		clause.select( functor ).from( availability ); //use range?

		return functor;
	}

	//use when updating only this assignment
	@Override
	public void restoreDetail( 
		Object source,
		Object detail,
		boolean isChild )
	{
		restoreDetail( detail );
		getTask().recalculate( source );
		getTask().updateCachedDuration();
	}

	public void restoreDetail( 
		Object detail )
	{
		this.detail = (AssignmentDetail)detail;
	}

	public void setActualDuration( 
		long actualDuration )
	{
		if (actualDuration > getDuration())
		{
			newDetail().setDuration( actualDuration );
		}

		newDetail().setActualDuration( actualDuration );
	}

	public void setActualFinish( 
		long actualFinish )
	{
		newDetail().setActualFinish( actualFinish );
	}

	public void setActualStart( 
		long actualStart )
	{
		newDetail().setActualStart( actualStart );
	}

	public final void setCachedEnd( 
		Date savedEnd )
	{
		this.cachedEnd = savedEnd;
	}

	public final void setCachedStart( 
		Date savedStart )
	{
		this.cachedStart = savedStart;
	}

	public void setComplete( 
		boolean complete )
	{
		ScheduleUtil.setComplete( this, complete );
	}

	public void setCompletedThrough( 
		long completedThrough )
	{
		setStop( completedThrough );
	}

	public void setCost( 
		double cost,
		FieldContext fieldContext )
	{
	}

	public void setCostRateIndex( 
		int val )
	{
		newDetail().setCostRateIndex( val );
	}

	//	public void setNew(boolean isNew) {
	//		hasKey.setNew(isNew);
	//	}
	public void setCreated( 
		Date created )
	{
		hasKey.setCreated( created );
	}

	public void setCreatedFromTimesheet( 
		boolean createdFromTimesheet )
	{
		this.createdFromTimesheet = createdFromTimesheet;
	}

	public void setDependencyStart( 
		long dependencyStart )
	{
		detail.setDependencyStart( dependencyStart ); //TODO is it ok to modify schedule directly?  Should be if it is transient
	}

	public void setDirty( 
		boolean dirty )
	{
		//System.out.println("Assignment _setDirty("+dirty+"): "+getName());
		this.dirty = dirty;

		if (dirty)
		{
			Task task = getTask();

			if (task != null)
			{
				task.setDirty( true );
			}
		}
		else if (extraFields != null)
		{
			extraFields.markAsClean();
		}
	}

	public void setEnd( 
		long end,
		FieldContext context )
	{
		if (isActualsProtected() && (end < getStop()))
		{
			end = getStop();
		}

		setEnd( end );
	}

	public void setEnd( 
		long end )
	{
		detail.setEnd( end );
	}

	public void setEstimatedFinish( 
		long estimatedFinish )
	{
		this.estimatedFinish = estimatedFinish;
	}

	public void setEstimatedPercentComplete( 
		double estimatedPercentComplete )
	{
		this.estimatedPercentComplete = estimatedPercentComplete;
	}

	public void setEstimatedRemainingWork( 
		long estimatedRemainingWork )
	{
		this.estimatedRemainingWork = estimatedRemainingWork;
	}

	public void setEstimatedStart( 
		long estimatedStart )
	{
		this.estimatedStart = estimatedStart;
	}

	public boolean setEstimatedValues( 
		long estimatedRemainingWork,
		long estimatedWork,
		long estimatedStart,
		long estimatedFinish,
		double estimatedPercentComplete )
	{
		boolean changed = false;

		if (this.estimatedRemainingWork != estimatedRemainingWork)
		{
			this.estimatedRemainingWork = estimatedRemainingWork;
			changed = true;
		}

		if (this.estimatedWork != estimatedWork)
		{
			this.estimatedWork = estimatedWork;
			changed = true;
		}

		if (this.estimatedStart != estimatedStart)
		{
			this.estimatedStart = estimatedStart;
			changed = true;
		}

		if (this.estimatedFinish != estimatedFinish)
		{
			this.estimatedFinish = estimatedFinish;
			changed = true;
		}

		if (this.estimatedPercentComplete != estimatedPercentComplete)
		{
			this.estimatedPercentComplete = estimatedPercentComplete;
			changed = true;
		}

		if (changed)
		{
			setDirty( true );
		}

		return changed;
	}

	public void setEstimatedWork( 
		long estimatedWork )
	{
		this.estimatedWork = estimatedWork;
	}

	public final void setExtraFields( 
		Map extraFields )
	{
		this.extraFields = (HashMapWithDirtyFlags)extraFields;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.TimeDistributedFields#setFixedCost(double, com.projity.field.FieldContext)
	 */
	public void setFixedCost( 
		double fixedCost,
		FieldContext fieldContext )
	{
	}

	/**
	 * @param id
	 */
	public void setId( 
		long id )
	{
		hasKey.setId( id );
	}

	public final void setLastTimesheetUpdate( 
		long lastTimesheetUpdate )
	{
		this.lastTimesheetUpdate = lastTimesheetUpdate;
		;
	}

	@Override
	public void setLocal( 
		boolean local )
	{
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void setName( 
		String name )
	{
		hasKey.setName( name );
	}

	@Override
	public void setPercentComplete( 
		double percentComplete )
	{
		newDetail().setPercentComplete( percentComplete );
	}

	@Override
	public final void setRate( 
		Rate rate )
	{
		if (rate.isNonTemporal() == getRate().isNonTemporal())
		{ // normal case.  If a material resource is modified from temporal to non (or vice versa), just set the rate

			double old = getRate().getValue();

			if (rate.getValue() == old) // if no change
			{
				return;
			}

			long oldRemaining = getRemainingDuration();
			newDetail();

			double multiplier = rate.getValue() / old;

			if (getPercentComplete() > 0)
			{
				makeContourPersonal();
			}

			detail.adjustRemainingUnits( rate.getValue() );

			if (getTaskSchedulingType() != SchedulingType.FIXED_DURATION)
			{
				detail.adjustRemainingDuration( (long)(oldRemaining / multiplier) );
			}
		}

		detail.setRate( rate );

		getTask().updateCachedDuration(); // needed for checking if milestone
	}

	/**
	 * @param timeUnit
	 */
	public void setRateUnit( 
		int timeUnit )
	{
		detail.setRateUnit( timeUnit );
		getTask().updateCachedDuration(); // needed for checking if milestone - happens when changing units in dialog
	}

	public void setRawDurationOfDetail( 
		long duration )
	{
		newDetail().setDuration( duration );
	}

	public void setRemainingDuration( 
		long remainingDuration )
	{
		newDetail().setRemainingDuration( remainingDuration );
	}

	public void setRemainingWork( 
		long remainingWork,
		FieldContext fieldContext )
	{
		if (remainingWork < 0)
		{
			remainingWork = 0;
		}

		long oldRemaining = getRemainingWork();
		setWork( getWork( fieldContext ) + (remainingWork - oldRemaining), fieldContext );

		//		setActualWork(getWork(fieldContext) - Duration.millis(remainingWork), fieldContext);
	}

	public void setResume( 
		long resume )
	{
		newDetail().setResume( resume );
	}

	public void setStop( 
		long stop )
	{
		if (stop < getStart()) // make sure in assignment's range
		{
			stop = getStart();
		}
		else if (stop > getEnd())
		{
			stop = getEnd();
		}

		long currentStop = getStop();

		if (currentStop == stop)
		{
			return;
		}

		if (stop < currentStop) // if uncompleting
		{
			newDetail().removeFillerAfter( stop );
		}

		if ((currentStop > 0) && (getDependencyStart() > currentStop) && (getDependencyStart() < stop))
		{ // if setting stop incorporates split due to dependency
			makeContourPersonal();
		}

		newDetail().setStop( stop );
	}

	public void setTaskAndResource( 
		Task task,
		Resource resource )
	{
		detail.setTask( task );
		detail.setResource( resource );
	}

	public void setTaskSchedule( 
		TaskSchedule taskSchedule )
	{
		newDetail().setTaskSchedule( taskSchedule );
	}

	public final void setTimesheetAssignment( 
		boolean timesheetAssignment )
	{
		this.timesheetAssignment = timesheetAssignment;
	}

	public void setTimesheetClosed( 
		boolean timesheetClosed )
	{
		Field f = Configuration.getFieldFromId( "Field.timesheetClosed" );

		if ((f != null) && (getExtraFields() != null))
		{
			getExtraFields().put( f.getAlternateId(), timesheetClosed );
		}
	}

	public void setTimesheetComplete( 
		boolean timesheetComplete )
	{
		detail.setTimesheetComplete( timesheetComplete );
	}

	public final void setTimesheetStatus( 
		int timesheetStatus )
	{
		this.timesheetStatus = timesheetStatus;
	}

	/**
	 * @param id
	 */
	public void setUniqueId( 
		long id )
	{
		hasKey.setUniqueId( id );
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.AssignmentSpecificFields#setWorkContourId(int)
	 */
	@Override
	public void setWorkContourType( 
		int workContourType )
	{
		newDetail().setWorkContour( ContourFactory.getInstance( workContourType ) );

		// TODO need to actually transform contour correctly when changing
	}

	public final void setWorkflowState( 
		int workflowState )
	{
		this.workflowState = workflowState;
	}

	@Override
	public void showProtectionWarning()
	{
		Alert.error( Messages.getString( "Message.deletionOfAssignmentWithProtectedActuals" ) );
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.scheduling.Schedule#split(java.lang.Object, long, long)
	 */
	@Override
	public void split( 
		Object eventSource,
		long from,
		long to )
	{
		from = Math.max( from, getResume() );

		if (from >= to)
		{
			return;
		}

		long duration = getEffectiveWorkCalendar().compare( to, from, false ); // calculate shift duration
		shift( from, getEnd(), duration ); // shift the remaining contour to the right by the duration
	}

	/* (non-Javadoc)
	 * @see com.projity.util.Association#testValid()
	 */
	public void testValid( 
		boolean allowDuplicate )
		throws InvalidAssociationException
	{
	}

	/**
	 * Copy the properies from another assignment.  This also includes the contour. However, it does NOT
	 * include the resource or the units.  This is called in the context of replacing an assignment.
	 * @param from Assignment to copy from
	 */
	public void usePropertiesOf( 
		Assignment from )
	{
		boolean compatibleTypes = isLabor() == from.isLabor();
		double units = getUnits();
		Rate r = getRate();
		Resource resource = getResource();
		detail = (AssignmentDetail)from.detail.clone();
		detail.replaceResourceAndUnits( units, resource );

		if (!compatibleTypes) // don't want to set units if not compatible
		{
			detail.setRate( r );
		}
	}

	public String valuesString()
	{
		return "Duration=" + DurationFormat.format( getDuration() ) + " Work=" + DurationFormat.format( getWork( null ) ) +
		" Units=" + getUnits() + " contour=" + getWorkContour().toString( getDuration() );
	}

	@Override
	public long work( 
		long _start,
		long _end )
	{
		if (isInRange( _start, _end ) == false)
		{
			return NO_VALUE_LONG;
		}

//		if (!isLabor()) // TODO this right?
//			return 0;
		
		Query query = Query.getInstance();
		SelectFrom clause = SelectFrom.getInstance().whereInRange( _start, _end );
		query.selectFrom( clause ).action( work( clause ) ).execute();

		return (long)((DoubleValue)query.getActionVisitor()).getValue();
	}

	public long work()
	{
		return work( FieldContext.DEFAULT_START, FieldContext.DEFAULT_END );
	}

	public Query workQuery()
	{
		Query query = Query.getInstance();
		SelectFrom clause = SelectFrom.getInstance();
		query.selectFrom( clause ).action( work( clause ) );

		return query;
	}

	boolean areActuals()
	{
		return ((getPercentComplete() > 0D) || (getActualStart() > 0) || (getActualFinish() > 0));
	}

	boolean canDeleteActuals()
	{
		return getProject().getProjectPermission().isDeleteActuals();
	}

	private void writeObject( 
		ObjectOutputStream s )
		throws IOException
	{
		s.defaultWriteObject();
		hasKey.serialize( s );
	}

	static final long serialVersionUID = 56779404923241L;
	private static transient BarClosure myBarClosureInstance = new BarClosure();
	AssignmentDetail detail = null;
	double estimatedPercentComplete = 0.0;
	long estimatedFinish = 0L;
	long estimatedRemainingWork = 0L;
	long estimatedStart = 0L;
	long estimatedWork = 0L;
	private transient Date cachedEnd = null;
	private transient Date cachedStart = null;
	private transient HasKeyImpl hasKey = new HasKeyImpl(true, this); //local ids only for assignments
//	transient HashMap<Field,Object> delegatedFields = new HashMap<Field,Object>();
	private transient HashMapWithDirtyFlags extraFields = null;
	private transient boolean createdFromTimesheet = false;
	private transient boolean dirty = true;
	private transient boolean timesheetAssignment = false;
	private transient int timesheetStatus = TimesheetStatus.NO_DATA;
	private transient int workflowState = AssignmentWorkflowState.NEW;
	private transient long lastTimesheetUpdate = 0;
}
