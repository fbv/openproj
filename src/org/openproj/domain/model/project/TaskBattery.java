/*
 * 
 */
package org.openproj.domain.model.project;

import com.jtsmythe.event.EventEmitter;
import com.jtsmythe.event.Listener;
import com.projity.algorithm.ReverseQuery;
import com.projity.datatype.Duration;
import com.projity.pm.assignment.Assignment;
import com.projity.pm.calendar.WorkCalendar;
import com.projity.pm.dependency.Dependency;
import com.projity.pm.task.Project;
import com.projity.pm.time.HasStartAndEnd;
import com.projity.server.access.ErrorLogger;
import java.util.*;

import org.apache.commons.collections.Closure;
import org.openproj.domain.model.Model;

import org.openproj.domain.task.Task;

/**
 *
 * @author Paul Corbett (psc1952)
 */
public class TaskBattery
{
	/** Constructor for TaskBattery
	 * 
	 */
	public TaskBattery()
	{
		// nothing to do
	}
	
	/**
	 * 
	 * @param _task 
	 */
	public void add( 
		final Task _task )
	{
		myTasks.add( _task );
		myEmitter.fireAdd( _task );
	}

	public void addListener(
		final Listener _listener )
	{
		myEmitter.attachListener( _listener );
	}
	
	public void addRepaired( 
		Task _task )
	{
		if (myRepaired == null)
		{
			myRepaired = new LinkedList<Task>();
		}

		myRepaired.add( _task );
	}

	public void buildReverseQuery( 
		final ReverseQuery _reverseQuery )
	{
		final Iterator<Task> itor = myTasks.iterator();

		while (itor.hasNext() == true)
		{
			itor.next().buildReverseQuery( _reverseQuery );
		}
	}

	/**
	 * 
	 * @return 
	 */
	public Collection<Task> collection()
	{
		return myTasks;
	}
	
	/**
	 * 
	 * @param _task
	 * @return 
	 */
	public boolean contains(
		final Task _task )
	{
		return myTasks.contains( _task );
	}
	
	/** Iterate through all of the tasks, calling the closure's {@code execute} method.
	 * 
	 * @param _closure 
	 */
	public void forEachTask( 
		final Closure<Task> _closure )
	{
		final Iterator<Task> itor = myTasks.iterator();

		while (itor.hasNext() == true)
		{
			_closure.execute( itor.next() );
		}
	}

	public void forEachWorkingInterval( 
		final Closure<HasStartAndEnd> _visitor,
		final boolean _mergeWorking,
		final WorkCalendar _workCalendar )
	{
		final Iterator<Task> itor = myTasks.iterator();

		while (itor.hasNext() == true)
		{
			itor.next().forEachWorkingInterval( _visitor, _mergeWorking, _workCalendar );
		}
	}

	public long getActualFinish()
	{
		long result = 0;

		for (Iterator<Task> itor = myTasks.iterator(); itor.hasNext() == true;)
		{
			final Task task = itor.next();
			final long val = task.getActualFinish();

			if (val == 0)
			{
				break;
			}

			if (val > result)
			{
				result = val;
			}
		}

		return result;
	}

	public long getActualStart()
	{
		long result = Long.MAX_VALUE;

		for (Iterator<Task> itor = myTasks.iterator(); itor.hasNext() == true;)
		{
			final Task task = itor.next();
			final long val = task.getActualStart();

			if ((val != 0) && (val < result))
			{
				result = val;
			}
		}

		if (result == Long.MAX_VALUE)
		{
			result = 0;
		}

		return result;
	}

	public long getBaselineFinish( 
		final int _numBaseline )
	{
		long result = 0;

		for (Iterator<Task> itor = myTasks.iterator(); itor.hasNext() == true;)
		{
			final Task task = (Task)itor.next();
			final long val = task.getBaselineFinish( _numBaseline );

			if (val > result)
			{
				result = val;
			}
		}

		return result;
	}

	public long getBaselineStart( 
		int _numBaseline )
	{
		long result = Long.MAX_VALUE;

		for (Iterator<Task> itor = myTasks.iterator(); itor.hasNext() == true;)
		{
			final Task task = (Task)itor.next();
			final long val = task.getBaselineStart( _numBaseline );

			if ((val != 0) && (val < result))
			{
				result = val;
			}
		}

		if (result == Long.MAX_VALUE)
		{
			result = 0;
		}

		return result;
	}

	public long getEarliestStartingTask()
	{
		return myEarliestStartingTask;
	}

	public long getEarliestStop()
	{
		long result = Long.MAX_VALUE;

		for (Iterator<Task> itor = myTasks.iterator(); itor.hasNext() == true;)
		{
			final Task task = itor.next();
			final long val = task.getEarliestStop();

			if (val < result)
			{
				result = val;
			}

			if (val == 0)
			{
				break;
			}
		}

		if (result == Long.MAX_VALUE)
		{
			result = 0;
		}

		return result;
	}

	public long getLatestFinishingTask()
	{
		return myLastestFinishingTask;
	}

	public double getPercentComplete()
	{
		long actual = 0L;
		long total = 0L;

		for (Iterator<Task> itor = myTasks.iterator(); itor.hasNext() == true;)
		{
			final Task task = itor.next();
			actual += Duration.millis( task.getActualDuration() );
			total += Duration.millis( task.getDuration() );
		}

		if (total == 0L)
		{
			return 0D;
		}
		else
		{
			return ((double)actual) / total;
		}
	}

	public long getStop()
	{
		long result = 0;

		for (Iterator<Task> itor = myTasks.iterator(); itor.hasNext() == true;)
		{
			final Task task = itor.next();
			final long val = task.getStop();

			if (val == 0)
			{
				return 0;
			}

			if (val > result)
			{
				result = val;
			}
		}

		return result;
	}

	/**
	 * 
	 * @param _numBaseline
	 * @return 
	 */
	public boolean hasBaseline( 
		final int _numBaseline )
	{ // see if any task has a baseline

		final Integer snapshotId = Integer.valueOf( _numBaseline );
		final Iterator<Task> itor = myTasks.iterator();

		while (itor.hasNext() == true)
		{
			final Task task = itor.next();

			if (task.getSnapshot( snapshotId ) != null)
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * 
	 * @param _task 
	 */
	public void initializeId( 
		final Task _task )
	{
		long id = ++myTaskIdCounter;
		_task.setId( id ); //starts at 1TODO check for duplicates -
						  //task.setUniqueId(id); //TODO use a GUID generator
	}

	public boolean isTimesheetComplete()
	{
		Iterator<Task> itor = myTasks.iterator();

		while (itor.hasNext() == true)
		{
			if (!((Task)itor.next()).isTimesheetComplete() == true)
			{
				// really should just do root task
			}

			return false;
		}

		return true;
	}

	/**
	 * 
	 * @return 
	 */
	public Iterator<Task> iterator()
	{
		return myTasks.iterator();
	}
	
	public void markAsNeedingRecalculation(
		final boolean _markerStatus )
	{
		final Iterator<Task> itor = myTasks.iterator();

		while (itor.hasNext() == true)
		{
			final Task task = itor.next();
			task.setMarkerStatus( _markerStatus );
			task.markTaskAsNeedingRecalculation();
		}
	}
	
	public void markRepairedTasksAsDirty()
	{
		// in case any tasks were repaired (rare), mark them as dirty
		if (myRepaired != null)
		{
			Iterator<Task> itor = myRepaired.iterator();

			while (itor.hasNext() == true)
			{
				final Task task = (Task)itor.next();
				task.setTaskAssignementAndPredsDirty();
			}

			myRepaired = null;
		}
	}

	public Task newTaskInstance(
		final Project _project )
	{
		final Task newOne = Model.getInstance().createTask( _project );

		myTasks.add( newOne );
		initializeId( newOne );

		return newOne;
	}

	/**
	 * Used when creating a task on spreadsheet that may not be valid
	 * @return
	 */
//	public NormalTask newStandaloneNormalTaskInstance( 
	public Task newStandaloneNormalTaskInstance( 
		final Project _project,
		final WorkCalendar _workCalendar )
	{
		final Task newOne = Model.getInstance().createTask( _project );

		newOne.getCurrentSchedule().setStart( _workCalendar.adjustInsideCalendar( newOne.getCurrentSchedule().getStart(), 
			false ) );
		initializeId( newOne );

		//		newOne.initializeDates();
		return newOne;
	}

	/**
	 * 
	 * @param _task 
	 */
	public void remove(
		final Task _task )
	{
		myTasks.remove( _task );
		myEmitter.fireRemove( _task );
	}
	
	public void removeListener(
		final Listener _listener )
	{
		myEmitter.detachListener( _listener );
	}
	
	public void repairTasks()
	{
		final Iterator<Task> itor = myTasks.iterator();

		while (itor.hasNext() == true)
		{
//			final NormalTask task = (NormalTask)itor.next();
			final Task task = (Task)itor.next();

			if (task.validateConstraints())
			{
				addRepaired( task );
			}

			if (task.getAssignments().isEmpty() == true)
			{
				Assignment ass = task.addDefaultAssignment();
				ass.setDirty( true );
				task.setDirty( true );
				ErrorLogger.logOnce( "NoAssignment", "Repaired task with no assignments", null );
				System.out.println( "added default ass for " + task );
				addRepaired( task );
			}
		}
	}

	public void setAllChildrenDirty( 
		boolean _dirty )
	{ // used when changing field dirties all tasks

		final Iterator<Task> itor = myTasks.iterator();

		while (itor.hasNext() == true)
		{
			final Task task = itor.next();
			task.setDirty( _dirty );
		}
	}

	public void setAllDirty()
	{
		final Iterator<Task> itor = myTasks.iterator();

		// update all of the tasks schedules to reflect their scheduling direction
		while (itor.hasNext() == true)
		{
			final Task task = (Task)itor.next();
			task.setDirty( false );

			Iterator j = task.getAssignments().iterator();

			while (j.hasNext())
			{
				((Assignment)j.next()).setDirty( true );
			}

			j = task.getDependencyList( true ).iterator();

			while (j.hasNext() == true)
			{
				((Dependency)j.next()).setDirty( true );
			}
		}
	}

	public void setCalculationStateCount(
		final int _nextStateCount,
		final boolean _invalidateSchedules )
	{
		Iterator<Task> itor = myTasks.iterator();

		// update all of the tasks schedules to reflect their scheduling direction
		while (itor.hasNext() == true)
		{
			final Task task = itor.next();
			task.setCalculationStateCount( _nextStateCount );

			if (_invalidateSchedules == true)
			{
				task.invalidateSchedules();
			}
		}
	}

	/**
	 * This will set the start and end date of a project to the earliest starting task and the latest finishing
	 * Its purpose is for use in handling sub projects, when we'd like the sub project's external constraints to determine 
	 * its start and end, and also have it the subproject show up with correct start and end dates when shown unopened in 
	 * another project
	 *
	 * @param _defaultStart
	 * @param _defaultEnd
	 */
	public void setEarliestAndLatestDatesFromSchedule(
		final long _defaultStart,
		final long _defaultEnd )
	{
		final Iterator<Task> itor = myTasks.iterator();
		long s = Long.MAX_VALUE;
		long e = 0;

		while (itor.hasNext() == true)
		{
			final Task task = itor.next();

//			if ((task.isExternal() == true) 
//			 || (task.getOwningProject() != this))
			if (task.isExternal() == true) 
			{
				continue;
			}

			s = Math.min( s, task.getStart() );
			e = Math.max( e, task.getEnd() );
		}

		if (s != Long.MAX_VALUE)
		{
			myEarliestStartingTask = s;
		}
		else
		{
			myEarliestStartingTask = _defaultStart;
		}

		if (e != 0)
		{
			myLastestFinishingTask = e;
		}
		else
		{
			myLastestFinishingTask = _defaultEnd;
		}
	}

	public void setForward(
		final boolean _forward )
	{
		final Iterator<Task> itor = myTasks.iterator();

		// update all of the tasks schedules to reflect their scheduling direction
		while (itor.hasNext() == true)
		{
			itor.next().setForward( _forward );
		}
	}

	public void setTimesheetComplete( 
		boolean _timesheetComplete )
	{
		Iterator<Task> itor = myTasks.iterator();

		while (itor.hasNext() == true)
		{
			((Task)itor.next()).setTimesheetComplete( _timesheetComplete ); // really should just do root task
		}
	}

	/** Return the number of tasks.
	 * 
	 * @return integer value equal to the number of tasks
	 */
	public int size()
	{
		return myTasks.size();
	}
	
	private final ArrayList<Task> myTasks = new ArrayList<>();

	private transient EventEmitter myEmitter = new EventEmitter( this );
	private transient List<Task> myRepaired = null;
	private transient int myTaskIdCounter = 0;
	private transient long myEarliestStartingTask = 0L; // used for subprojects
	private transient long myLastestFinishingTask = 0L; // used for subprojects
}
