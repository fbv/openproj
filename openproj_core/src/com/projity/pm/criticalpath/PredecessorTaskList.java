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

import com.projity.pm.task.SubProj;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import org.apache.commons.collections.Closure;

import org.openproj.domain.task.Task;

/**
* This class implements a task list in predecessor/parent order.  That is, the successors of any given
* task are guaranteed to be after that task in the list. Also wbs children are after their parents.
*  This ordering is needed for the critical path algorithm.
*/
public class PredecessorTaskList
{
	PredecessorTaskList( 
		SchedulingAlgorithm _schedulingAlgorithm )
	{
//		mySchedulingAlgorithm = _schedulingAlgorithm;
	}

	/**
	 * @return Returns the markerStatus.
	 */
	public final boolean getMarkerStatus()
	{
		return myMarkerStatus;
	}

	/**
	 * Add a subproject. It will convert the existing task into a parent and add all children
	 * @param subproject
	 */
	public void addSubproject( 
		final Task _subproject )
	{
		// remove sentinels 
		final TaskReference startSentinel = (TaskReference)myList.removeFirst();
		final TaskReference endSentinel = (TaskReference)myList.removeLast();

		// mark tasks to be added as not yet treated
		final boolean m = !getMarkerStatus();
		_subproject.setMarkerStatus( m );
		_subproject.markTaskAsNeedingRecalculation();

		((SubProj)_subproject).getSubproject().getTasks().markAsNeedingRecalculation( m );

		removeTask( _subproject ); // remove existing one
		arrangeSingleTask( _subproject ); // add it back - it will become a parent
										 // add child tasks

		((SubProj)_subproject).getSubproject().getTasks().forEachTask( arrangeSingleTaskClosure() );

		// put back sentinels
		myList.addFirst( startSentinel );
		myList.addLast( endSentinel );
	}

	public void dump()
	{
		ListIterator<TaskReference> itor = myList.listIterator();

		while (itor.hasNext() == true)
		{
			System.out.println( itor.next() );
		}
	}

	// for debugging - finds position(s) in pred list of a task
	public int[] findTaskPosition( 
		final Task _task )
	{
		int[] result;

		if (_task.isWbsParent() == true)
		{
			result = new int[ 2 ];
		}
		else
		{
			result = new int[ 1 ];
		}

		Iterator<TaskReference> itor = myList.iterator();
		int resultIndex = 0;
		int pos = 0;

		while (itor.hasNext() == true)
		{
			final Task task = itor.next().getTask();

			if (task == _task)
			{
				result[ resultIndex++ ] = pos;

				if (resultIndex == result.length)
				{
					break;
				}
			}

			pos++;
		}

		return result;
	}

	final boolean toggleMarkerStatus()
	{
		myMarkerStatus = !myMarkerStatus;

		return myMarkerStatus;
	}

//	boolean addAll( 
//		final Collection _tasks )
//	{
//		myList.clear();
//		toggleMarkerStatus();
//
//		Iterator<Task> itor = _tasks.iterator();
//
//		while (itor.hasNext() == true)
//		{
//			final Task task = itor.next();
//			task.arrangeTask( myList, myMarkerStatus, 0 );
//
//			if (task.isReverseScheduled() == true)
//			{
//				myNumberOfReverseScheduledTasks++;
//			}
//		}
//
//		return true;
//	}

	Closure<Task> addAllClosure()
	{
		myList.clear();
		toggleMarkerStatus();

		return new Closure<Task>()
		{
			@Override
			public void execute(
				final Task _task )
			{
				_task.arrangeTask( myList, myMarkerStatus, 0 );

				if (_task.isReverseScheduled() == true)
				{
					myNumberOfReverseScheduledTasks++;
				}
			}
		};
	}
	
	/**
	 * Insert a task into the list.  Go thru and insert it after its parent
	 * The task being inserted is a new task and as such has no preds/succs. Just insert it after its parent
	 *  * @param _task
	 */
	void arrangeTask( 
		final Task _task )
	{
		if (_task.isReverseScheduled() == true)
		{
			myNumberOfReverseScheduledTasks++;
		}

		_task.setMarkerStatus( myMarkerStatus );

		// go thru in reverse order inserting after first predecessor or parent encountered
		final ListIterator<TaskReference> itor = myList.listIterator();
		final TaskReference taskReference = new TaskReference( _task );

		while (itor.hasNext() == true)
		{
			final Task previousTask = itor.next().getTask();

			if (_task.getWbsParentTask() == previousTask)
			{
				itor.add( taskReference );

				return;
			}
		}

		itor.previous(); // add before end sentinel
		itor.add( taskReference );
	}

	int getCalculationStateCount()
	{
		return myCalculationStateCount;
	}

	synchronized int getFreshCalculationStateCount()
	{
		// go by 3s so we can see what happens during different passes
		while ((myCalculationStateCount % CALCULATION_STATUS_STEP) != 0) 

		{
			myCalculationStateCount++;
		}

		return myCalculationStateCount;
	}

	LinkedList<TaskReference> getList()
	{
		return myList;
	}

	synchronized int getNextCalculationStateCount()
	{
		myCalculationStateCount += 1; // just get next one

		return myCalculationStateCount;
	}

	boolean hasReverseScheduledTasks()
	{
		return (myNumberOfReverseScheduledTasks > 0);
	}

	/**
	 * Return a list iterator - delegates to internal list
	 * @return list iterator
	 */
	ListIterator<TaskReference> listIterator()
	{
		return myList.listIterator();
	}

	void rearrangeAll()
	{
		LinkedList<TaskReference> oldList = myList;

		// store off sentinels to put them back later
		final TaskReference startSentinel = myList.removeFirst();
		final TaskReference endSentinel = myList.removeLast();
		myList = new LinkedList<TaskReference>();

		final Iterator<TaskReference> itor = oldList.iterator();
		toggleMarkerStatus();

		while (itor.hasNext() == true)
		{
			final Task task = itor.next().getTask();
			arrangeSingleTask( task );
		}

		myList.addFirst( startSentinel );
		myList.addLast( endSentinel );

		//		setDebugDependencyOrder();
	}

	/**
	 * Refresh the Reverse schedule count - called in response to change in constraint type field
	 */
	void recalculateReverseScheduledCount()
	{
		myNumberOfReverseScheduledTasks = 0;

		final Iterator<TaskReference> itor = myList.iterator();

		while (itor.hasNext() == true)
		{
			final Task task = itor.next().getTask();

			if (task.isReverseScheduled())
			{
				myNumberOfReverseScheduledTasks++;
			}
		}
	}

	void removeTask( 
		Task _task )
	{
		if (_task.isReverseScheduled())
		{
			myNumberOfReverseScheduledTasks--;
		}

		final Iterator<TaskReference> itor = myList.iterator();

		// the item may be in the list once or twice.  It may be that it is in twice, but the
		// task is no longer a parent
		while (itor.hasNext() == true)
		{
			final TaskReference reference = itor.next();

			if (reference.getTask() == _task)
			{
				itor.remove();
			}
		}
	}

	ListIterator<TaskReference> reverseIterator()
	{
		return myList.listIterator( myList.size() );
	}

	/**
	 * Helper to arrange one task
	 * @param task
	 */
	private void arrangeSingleTask( 
		final Task _task )
	{
		_task.arrangeTask( myList, myMarkerStatus, 0 );

		if (_task.isReverseScheduled() == true)
		{
			myNumberOfReverseScheduledTasks++;
		}
	}
	
	private Closure<Task> arrangeSingleTaskClosure()
	{
		return new Closure<Task>()
		{
			@Override
			public void execute(
				final Task _task )
			{
				_task.arrangeTask( myList, myMarkerStatus, 0 );

				if (_task.isReverseScheduled() == true)
				{
					myNumberOfReverseScheduledTasks++;
				}
			}
		};
	}

	private void setDebugDependencyOrder()
	{
		int count = 0;
		Iterator<TaskReference> itor = myList.iterator();
		Task task;
		TaskReference ref;

		while (itor.hasNext() == true)
		{
			final TaskReference reference = itor.next();

			if (reference.getType() == TaskReference.PARENT_END)
			{
				continue;
			}

			task = reference.getTask();
			task.setDebugDependencyOrder( count++ );
		}
	}

	public static final class TaskReference
		implements Comparable
	{
		public TaskReference( 
			final Task _task )
		{
			myTask = _task;
		}

		/* (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo( 
			final Object _other )
		{
			if (_other instanceof Task)
			{
				return ((getTask() == _other)
					? 0
					: (-1));
			}

			return ((_other == this)
				? 0
				: (-1));
		}

		/**
		 * @return Returns the task.
		 */
		public Task getTask()
		{
			return myTask;
		}

		/**
		 * @return Returns the type.
		 */
		public int getType()
		{
			return myType;
		}

		public void setParentBegin()
		{
			myType = PARENT_BEGIN;
		}

		public void setParentEnd()
		{
			myType = PARENT_END;
		}

		@Override
		public String toString()
		{
			String result = myTask.toString();

			if (myType == PARENT_BEGIN)
			{
				result += " begin";
			}
			else if (myType == PARENT_END)
			{
				result += " end";
			}

			return result;
		}

		static final int PARENT_BEGIN = -1;
		static final int CHILD = 0;
		static final int PARENT_END = 1;

		private final Task myTask;
		private TaskReference opposite = null;
		private int myType = CHILD;
		private long myCalculationStateCount = 0;
	}

	public static final int CALCULATION_STATUS_STEP = 3;

	private LinkedList<TaskReference> myList = new LinkedList<TaskReference>();
//	private SchedulingAlgorithm schedulingAlgorithm;
	private boolean myMarkerStatus;
	private int myCalculationStateCount = 0;
	private int myNumberOfReverseScheduledTasks = 0;
}
