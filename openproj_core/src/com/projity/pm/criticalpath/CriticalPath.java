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

import com.projity.configuration.Configuration;

import org.openproj.domain.document.Document;
import com.projity.document.ObjectEvent;

import com.projity.field.Field;

import com.projity.options.ScheduleOption;

import com.projity.pm.assignment.Assignment;
import com.projity.pm.calendar.WorkingCalendar;
import com.projity.pm.dependency.Dependency;
import com.projity.pm.dependency.DependencyService;
import com.projity.pm.scheduling.ConstraintType;
import com.projity.pm.scheduling.ScheduleEvent;
import org.openproj.domain.document.BelongsToDocument;
import com.projity.pm.task.Project;
import com.projity.pm.task.SubProj;

import com.projity.strings.Messages;

import com.projity.transaction.MultipleTransaction;

import com.projity.util.DateTime;
import com.projity.util.Environment;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Iterator;
import java.util.ListIterator;
import org.openproj.domain.model.Model;

import org.openproj.domain.task.Task;

/**
 * The critical path calculation
 */
public class CriticalPath
	implements SchedulingAlgorithm
{
	public CriticalPath( 
		final Project _project )
	{
		myProject = _project;
		myProject.setSchedulingAlgorithm( this );

//		myProject.addObjectListener(this);
//		myProject.getMultipleTransactionManager().addListener( this );
		fieldUpdater = CriticalPathFields.getInstance( this, myProject );

		startSentinel = Model.getInstance().createTask( true, myProject ); //local
		startSentinel.setDuration( 0 );
		startSentinel.setName( "<Start>" ); // name doesn't matter - useful for debugging purposes

		finishSentinel = Model.getInstance().createTask( true, myProject ); //local
		finishSentinel.setDuration( 0 );
		finishSentinel.setName( "<End>" ); // name doesn't matter - useful for debugging purposes

		setForward( isForward() );
		setProjectBoundaries();
		initEarliestAndLatest();
	}

	public static String getTrace()
	{
		StringBuilder buf = new StringBuilder();
		buf.append( ToStringBuilder.reflectionToString( lastInstance ) );
		buf.append( "\nProject: " ).append( lastInstance.myProject ).append( " Task: " ).append( traceTask ).append( " reverse=" ).
			append( traceTask.isReverseScheduled() ).append( " parent =" ).append( traceTask.isParent());

		return buf.toString();
	}

	public final long getEarliestStart()
	{
		return myEarliestStart;
	}

	public final long getLatestFinish()
	{
		return latestFinish;
	}

	public final Project getProject()
	{
		return myProject;
	}

	@Override
	public void addEndSentinelDependency( 
		Task task )
	{
		if ((task.getOwningProject() == myProject) && !task.isExternal())
		{
			DependencyService.getInstance().addEndSentinelDependency( finishSentinel, task );
		}
	}

	/**
	 * To add a new object such as when pasting
	 */
	@Override
	public void addObject( 
		final Object _task )
	{
//		NormalTask newTask = (NormalTask)_task;
		Task newTask = (Task)_task;

		if (newTask.getSuccessorList().isEmpty() == true)
		{ 
			// if pred has no successors, tell end sentinel about it
			addEndSentinelDependency( newTask );
		}
		else
		{ 
			// make sure not in sentinel's list
			removeEndSentinelDependency( newTask );
		}

		if (newTask.getPredecessorList().isEmpty() == true)
		{ 
			// if pred has no successors, tell end sentinel about it
			addStartSentinelDependency( newTask );
		}
		else
		{ 
			// make sure not in sentinel's list
			removeStartSentinelDependency( newTask );
		}

		newTask.markTaskAsNeedingRecalculation();
		myPredecessorTaskList.arrangeTask( newTask );
	}

	@Override
	public void addStartSentinelDependency( 
		Task task )
	{
		if ((task.getOwningProject() == myProject) && !task.isExternal())
		{
			DependencyService.getInstance().addStartSentinelDependency( startSentinel, task );
		}
	}

	@Override
	public void addSubproject( 
		Task subproject )
	{
		myPredecessorTaskList.addSubproject( subproject );
	}

	@Override
	public void calculate( 
		boolean update )
	{
		calculate( update, null );
	}

	public void dumpPredecessorList()
	{
		myPredecessorTaskList.dump();
	}

	public int[] findTaskPosition( 
		final Task _task )
	{ 
		// for debugging
		return myPredecessorTaskList.findTaskPosition( _task );
	}

	@Override
	public int getCalculationStateCount()
	{
		return myPredecessorTaskList.getCalculationStateCount();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.projity.pm.criticalpath.SchedulingAlgorithm#getDefaultTaskConstraintType()
	 */
	@Override
	public int getDefaultTaskConstraintType()
	{
		return ConstraintType.ASAP;
	}

	@Override
	public CriticalPathFields getFieldUpdater()
	{
		return fieldUpdater;
	}

	/**
	 * @return
	 */
	@Override
	public boolean getMarkerStatus()
	{
		return myPredecessorTaskList.getMarkerStatus();
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.criticalpath.SchedulingAlgorithm#getDocument()
	 */
	@Override
	public Document getMasterDocument()
	{
		return myProject;
	}

	@Override
	public String getName()
	{
		return Messages.getString( "Text.forwardScheduled" );
	}

	public long getStartConstraint()
	{
		return startSentinel.getConstraintDate();
	}

	@Override
	public void initEarliestAndLatest()
	{
		long date = myProject.getStartConstraint();

		if (date == 0)
		{
			date = DateTime.midnightToday(); // this repairs empty start bug
		}

		myEarliestStart = latestFinish = date;
	}

	@Override
	public void initialize( 
		final Object _object )
	{
		myProject = (Project)_object;
		myPredecessorTaskList.getList().clear(); // get rid of sentinels that are in the list

//		final Closure<Task> closure = myPredecessorTaskList.addAllClosure();
//		myProject.getTasks().forEachTask( closure );
		
		myProject.getTasks().forEachTask( myPredecessorTaskList.addAllClosure() );
//		myPredecessorTaskList.addAll( myProject.getTasks().collection() );

		initSentinelsFromTasks();
		setProjectBoundaries(); // put back sentinels

		calculate( false );
	}

	public boolean isCriticalPathJustChanged()
	{
		return criticalPathJustChanged;
	}

	/**
	 * @return
	 */
	@Override
	public boolean isForward()
	{
		return myProject.isForward();
	}

	@Override
	public void markBoundsAsDirty()
	{
		startSentinel.markTaskAsNeedingRecalculation();
		finishSentinel.markTaskAsNeedingRecalculation();

		// mark all tasks without preds or without succs as dirty 
		// the purpose of this is to handle cases where a task that determines the project bounds is deleted.
		Iterator i = startSentinel.getSuccessorList().iterator();
		Task task;

		while (i.hasNext())
		{
			task = ((Task)((Dependency)i.next()).getTask( false ));
			task.invalidateSchedules();
			task.markTaskAsNeedingRecalculation();
		}

		i = finishSentinel.getPredecessorList().iterator();

		while (i.hasNext())
		{
			task = ((Task)((Dependency)i.next()).getTask( true ));
			task.invalidateSchedules();
			task.markTaskAsNeedingRecalculation();
		}
	}

	/* (non-Javadoc)
	 * @see com.projity.transaction.MultipleTransaction.Listener#multipleTransaction(com.projity.transaction.MultipleTransaction)
	 */
	@Override
	public void multipleTransaction( 
		MultipleTransaction objectEvent )
	{
		if (objectEvent.isFinalEnd())
		{
			suspendUpdates = false;

			if (needsReset)
			{
				reset();
			}

			calculate( true, null );
		}
		else
		{
			suspendUpdates = true;
		}
	}

//
//	public void scheduleTask(NormalTask task) {
//		calcEarlyStartAndFinish(task, task.getProject().getStart());
//		calcLateStartAndLateFinish(task, task.getProject().getEnd(), false);
//		task.calcStartAndFinish();
//	}

	/**
	 * Respond to object create/delete events
	 */
	@Override
	public void objectChanged( 
		ObjectEvent objectEvent )
	{
		if (!myProject.isInitialized() && !Environment.isImporting())
		{
			System.out.println( "Error - Message received when Project is not init" + myProject );

			return;
		}

		if (objectEvent.getSource() == this)
		{
			return;
		}

		Object changedObject = objectEvent.getObject();
		Task task = null;

		if (changedObject instanceof Task)
		{
			if (objectEvent.isCreate())
			{
				myPredecessorTaskList.arrangeTask( (Task)changedObject );

				return; // let the hierarchy event that follow run the CP
			}
			else if (objectEvent.isDelete())
			{
				Task removedTask = (Task)changedObject;
				myPredecessorTaskList.removeTask( removedTask );
				reset(); // Fix of bug 91 31/8/05.  This ensures the ancestors of this task that are no longer parents will be replaced as single entries in pred list
			}
			else if (objectEvent.isUpdate())
			{
				task = (Task)changedObject;

				Field field = objectEvent.getField();

				if ((field != null) && !fieldUpdater.inputContains( field ))
				{
					return;
				}

				if (field == constraintTypeField)
				{
					reset();
					task.invalidateSchedules();
					task.markTaskAsNeedingRecalculation();
				}
			}

			calculate( true, task );
		}
		else if (changedObject instanceof Dependency)
		{ // dependency added or
		  // removed

			Dependency dependency = (Dependency)changedObject;

			if (!dependency.refersToDocument( myProject ))
			{
				return;
			}

			if (!objectEvent.isUpdate())
			{
				reset(); // refresh predecssor list - the whold thing may change drastically no matter what the link because of parents
			}

			task = (Task)dependency.getPredecessor();

			Task successor = (Task)dependency.getSuccessor(); // the successor needs to be scheduled

			// to fix a bug, I am invalidating both early and late schedules
			task.invalidateSchedules();
			task.markTaskAsNeedingRecalculation();

			if (successor.isSubproject())
			{ // special case for subprojects - need to reset all

				SubProj sub = (SubProj)successor;

				if (sub.isSubprojectOpen())
				{
					sub.getSubproject().markAllTasksAsNeedingRecalculation( true );
				}
			}

			successor.invalidateSchedules();
			successor.markTaskAsNeedingRecalculation();

			//			The line below fixes a bug with nested parents of the sort pred->grand par sib1->sib2. Of course, it means most of the code above is redundant (except for subproject stuff)
			myProject.markAllTasksAsNeedingRecalculation( true );
			calculate( true, null ); // Run both passes, since the CP might be modified and it's hard to tell if so
		}
		else if (changedObject == myProject)
		{ // if whole project changed, such
		  // as hierarchy event
			reset();
			calculate( true, null );
		}
		else if (changedObject instanceof WorkingCalendar)
		{ // if whole project changed, such
		  //TODO for now just invalidating all projects, eventually be smarter
			myProject.markAllTasksAsNeedingRecalculation( false );
			calculate( true, null );
		}
		else if (changedObject instanceof Assignment)
		{
			Assignment assignment = (Assignment)changedObject;
			task = assignment.getTask();

			if (task.getProject().getSchedulingAlgorithm() != this)
			{
				return;
			}

//			if (((NormalTask)task).isEffortDriven())
			calculate( true, task );
		}
		else if (changedObject instanceof BelongsToDocument)
		{ // for other things, such as assignment entry

			if (((BelongsToDocument)changedObject).getDocument() instanceof Project)
			{
				Project proj = (Project)((BelongsToDocument)changedObject).getDocument();

				if (proj.getSchedulingAlgorithm() != this)
				{
					return;
				}
			}

			Field field = objectEvent.getField();

			if ((field != null) && fieldUpdater.inputContains( field ))
			{
				calculate( true, null );
			}
		}
	}

	@Override
	public boolean removeEndSentinelDependency( 
		final Task _task )
	{
		if ((_task.getOwningProject() == myProject) 
		 && (_task.isExternal() == false))
		{
			return DependencyService.getInstance().removeEndSentinel( finishSentinel, _task );
		}

		return false;
	}

	@Override
	public boolean removeStartSentinelDependency( 
		final Task _task )
	{
		if ((_task.getOwningProject() == myProject) 
		 && (_task.isExternal() == false))
		{
			return DependencyService.getInstance().removeStartSentinel( startSentinel, _task );
		}

		return false;
	}

	@Override
	public void reset()
	{
		if (suspendUpdates == true)
		{
			needsReset = true;

			return;
		}

		needsReset = false;
		initEarliestAndLatest();
		myPredecessorTaskList.rearrangeAll();
	}

	@Override
	public void setEarliestAndLatest( 
		final long _earliest,
		final long _latest )
	{
		myEarliestStart = _earliest;
		latestFinish = _latest;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.criticalpath.HasSentinels#setEndConstraint(long)
	 */
	@Override
	public void setEndConstraint( 
		long date )
	{
		finishSentinel.setScheduleConstraint( ConstraintType.FNLT, date );
		markBoundsAsDirty();
	}

	@Override
	public void setForward( 
		boolean forward )
	{
		if (forward)
		{
			setStartConstraint( myProject.getStartConstraint() );
			finishSentinel.setRawConstraintType( ConstraintType.ASAP );
		}
		else
		{
			setEndConstraint( myProject.getEnd() );
			startSentinel.setRawConstraintType( ConstraintType.ASAP );
		}

		startSentinel.setForward( forward );
		finishSentinel.setForward( forward );
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.criticalpath.HasSentinels#setStartConstraint(long)
	 */
	@Override
	public void setStartConstraint( 
		long date )
	{
		startSentinel.setScheduleConstraint( ConstraintType.SNET, date );
		markBoundsAsDirty();
	}

	private void _calculate( 
		boolean update,
		Task task )
	{
		long t = System.currentTimeMillis();

		if (myPredecessorTaskList.getList().size() < 3)
		{ 
			// if no tasks, nothing to calculate.  This is needed to avoid a null pointer execption because of sentinels 
			//...not having any preds/succs
			if (isForward() == true)
			{
				myProject.setEnd( myProject.getStartConstraint() );
			}
			else
			{
				myProject.setStart( myProject.getEnd() );
			}

			return;
		}

		if (task == null)
		{
			task = getBeginSentinel( isForward() );
		}

		fastCalc( task );

		if (update == true)
		{
			fireScheduleChanged();
		}
	}

	private void calculate( 
		final boolean update,
		final Task task )
	{
		if (suspendUpdates == true)
		{
			return;
		}

		_calculate( update, task );

// instead of calculating immediately, we can perhaps delay the calculation till the end of all other updates.  This may
// cause problems in other cases where an immediate update is required, so I am commenting it out for now. See bug 225
//		SwingUtilities.invokeLater(new Runnable() {
//			public void run() {
//				_calculate(update,task);
//			}
//		});
	}

	private void doPass( 
		Task startTask,
		TaskSchedule.CalculationContext context )
	{
		if (startTask != null)
		{
			startTask.getSchedule( context.scheduleType ).invalidate();
			startTask.setCalculationStateCount( getCalculationStateCount() );
		}

		PredecessorTaskList.TaskReference taskReference;
		boolean forward = context.myForward;
		ListIterator i = forward
			? myPredecessorTaskList.listIterator()
			: myPredecessorTaskList.reverseIterator();
		Task task;
		TaskSchedule schedule;

		//		int count = 0;
		//		long z = System.currentTimeMillis();
		boolean projectForward = myProject.isForward();

		while ((forward == true)
			? i.hasNext()
			: i.hasPrevious())
		{
			taskReference = (PredecessorTaskList.TaskReference)(forward
				? i.next()
				: i.previous());
			traceTask = task = taskReference.getTask();
			context.taskReferenceType = taskReference.getType();
			schedule = task.getSchedule( context.scheduleType );

			if (!forward)
			{
				context.taskReferenceType = -taskReference.getType();
			}

			if (task.isReverseScheduled())
			{ 
				//  reverse scheduled must always be calculated
				schedule.invalidate();
				task.setCalculationStateCount( context.stateCount );
			}

			if (task.getCalculationStateCount() >= context.stateCount)
			{
				schedule.calcDates( context );

				if (context.assign && (projectForward || !task.isWbsParent()))
				{ 
					// in reverse scheduling, I see some parents have 0 or 1 as their dates. This is a workaround.
					if ((schedule.getBegin() != 0L) && !isSentinel( task ))
					{
						myEarliestStart = Math.min( myEarliestStart, schedule.getStart() );
					}

					if ((schedule.getEnd() != 0) && !isSentinel( task ))
					{
						latestFinish = Math.max( latestFinish, schedule.getFinish() );
					}
				}

//				schedule.dump();
			}
		}

//		System.out.println("pass forward=" + forward + " tasks:" + count + " time " + (System.currentTimeMillis() -z) + " ms");
	}

	/**
	 * Run the critical path.  There are three possibilities:
	 * 1) The task that is modified does not affect the Critical Path.  In this case, only a single pass is performed 
	 *		and dates are set
	 * 2) The CP is modified, but the project contains no ALAP tasks.  In which case early and current dates are set 
	 *		in the first pass, and late in the second
	 * 3) The CP is modified and the project has ALAP tasks.  In which case, after both forward and backward passes are 
	 *		performed, a third pass sets current dates
	 * 
	 * @param _startTask
	 */
	private void fastCalc( 
		final Task _startTask )
	{
		lastInstance = this;

		final Task beginSentinel = getBeginSentinel( isForward() );
		final Task endSentinel = getEndSentinel( isForward() );

		long firstBoundary = (isForward() == true)
			? myProject.getStartConstraint()
			: (-myProject.getEnd());
		
		boolean hasReverseScheduledTasks = myPredecessorTaskList.hasReverseScheduledTasks();

		context = new TaskSchedule.CalculationContext();
		context.stateCount = getNextCalculationStateCount();
		context.honorRequiredDates = isHonorRequiredDates();
		context.myForward = isForward();
		context.boundary = firstBoundary;
		context.sentinel = endSentinel;
		context.earlyOnly = false;
		context.assign = false;
		context.scheduleType = isForward()
			? TaskSchedule.EARLY
			: TaskSchedule.LATE;

		context.pass = 0;

		boolean affectsCriticalPath = (_startTask == beginSentinel) ||
			_startTask.getSchedule( context.scheduleType ).affectsCriticalPath( context );

		boolean worstCase = affectsCriticalPath && hasReverseScheduledTasks;
		context.earlyOnly = worstCase;
		context.assign = true;
		context.pass = 1;
		criticalPathJustChanged = affectsCriticalPath;
		doPass( _startTask, context ); // always assign in first pass.  Dates may change in third pass

		if (affectsCriticalPath == true)
		{
			context.stateCount = getNextCalculationStateCount(); // backward pass treats next increment
			context.sentinel = endSentinel;

			long secondBoundary = -endSentinel.getSchedule( context.scheduleType ).getBegin(); // sent bounds of end sentinel for backward pass
			context.boundary = secondBoundary;
			context.sentinel = beginSentinel;
			context.myForward = !context.myForward;
			context.assign = false;
			context.scheduleType = -context.scheduleType;
			context.pass++;
			doPass( null, context );

			//set project fields
			myProject.setStart( startSentinel.getEarlyStart() );
			myProject.setEnd( finishSentinel.getEarlyFinish() );

			if (hasReverseScheduledTasks == true)
			{
				context.stateCount = getNextCalculationStateCount(); // backward pass treats next increment
				context.myForward = !context.myForward;
				context.boundary = firstBoundary;
				context.sentinel = endSentinel;
				context.earlyOnly = false;
				context.assign = true;
				context.scheduleType = -context.scheduleType;
				context.pass++;
				doPass( null, context );
			}
		}

		getFreshCalculationStateCount(); // For next time;
	}

	private void fireScheduleChanged()
	{
		((Project)myProject).fireScheduleChanged( this, ScheduleEvent.SCHEDULE );
	}

	private Task getBeginSentinel( 
		boolean _forward )
	{
		return _forward
			? startSentinel
			: finishSentinel;
	}

	private Task getEndSentinel( 
		final boolean _forward )
	{
		return _forward
			? finishSentinel
			: startSentinel;
	}

	private int getFreshCalculationStateCount()
	{
		return myPredecessorTaskList.getFreshCalculationStateCount();
	}

	private int getNextCalculationStateCount()
	{
		return myPredecessorTaskList.getNextCalculationStateCount();
	}

	private synchronized CriticalPathFields getOrClearUpdater( 
		boolean _get )
	{
		if (_get == true)
		{
			if (updating == true)
			{
				System.out.println( "interrupting update thread" );
				fieldUpdater.interrupt(); // interrupt existing thread
				fieldUpdater = CriticalPathFields.getInstance( this, myProject ); // make new one
			}

			updating = true;

			return fieldUpdater;
		}
		else
		{
			updating = false;

			return null;
		}
	}

	/**
	 * Initialize the sentinels so that the start sentinel has all start tasks as successors, 
	 * and the end sentinel has all end tasks as predecessors
	 *
	 */
	private void initSentinelsFromTasks()
	{
		Iterator i = myPredecessorTaskList.listIterator();
		Task task;

		while (i.hasNext())
		{
			task = ((PredecessorTaskList.TaskReference)i.next()).getTask();

			if (task.getPredecessorList().isEmpty() == true)
			{
				addStartSentinelDependency( task );
			}

			if (task.getSuccessorList().isEmpty() == true)
			{
				addEndSentinelDependency( task );
			}
		}

//		System.out.println("start sentinel successors");
//		startSentinel.getSuccessorList().dump(false);
//		System.out.println("end sentinel preds");
//		finishSentinel.getPredecessorList().dump(true);
	}

	private boolean isHonorRequiredDates()
	{
		return ScheduleOption.getInstance().isHonorRequiredDates();
	}

	private boolean isSentinel( 
		Task task )
	{
		return (task == startSentinel) || (task == finishSentinel);
	}

	private void setProjectBoundaries()
	{
		// update sentinels based on read in project
		if (isForward() == true)
		{
			startSentinel.setWindowEarlyStart( myProject.getStartConstraint() );
			finishSentinel.setWindowLateFinish( 0 ); //no end constraint
		}
		else
		{
			startSentinel.setWindowEarlyStart( 0 ); // no start constraint
			finishSentinel.setWindowLateFinish( myProject.getEnd() );
		}

		myPredecessorTaskList.getList().add( 0, new PredecessorTaskList.TaskReference( startSentinel ) );
		myPredecessorTaskList.getList().add( new PredecessorTaskList.TaskReference( finishSentinel ) );
	}

	private static Task traceTask;
	private static CriticalPath lastInstance;
	private TaskSchedule.CalculationContext context;
	private Task finishSentinel;
	private Task startSentinel;
	private PredecessorTaskList myPredecessorTaskList = new PredecessorTaskList(this);
	private Project myProject;
	private boolean needsReset = false; // flag to indicate that a reset is pending during suspended updates
	private boolean suspendUpdates = false; // flag to suspend updates when multiple objects are treated
	private long myEarliestStart;
	private long latestFinish;
	private CriticalPathFields fieldUpdater = null;
	private Field constraintTypeField = Configuration.getFieldFromId( "Field.constraintType" );
	private boolean criticalPathJustChanged = false;
	private boolean updating = false;
}
