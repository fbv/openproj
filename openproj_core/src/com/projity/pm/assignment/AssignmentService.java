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

import com.projity.command.Command;

import com.projity.datatype.TimeUnit;

import com.projity.grouping.core.Node;

import com.projity.pm.resource.Resource;
import com.projity.pm.resource.ResourceImpl;
import com.projity.pm.resource.ResourcePool;
import com.projity.pm.scheduling.SchedulingType;
import com.projity.pm.task.Project;
import com.projity.pm.task.ProjectFactory;

import com.projity.session.SaveOptions;
import com.projity.session.SessionFactory;

import com.projity.strings.Messages;

import com.projity.undo.NodeUndoInfo;
import com.projity.undo.ScheduleBackupEdit;

import com.projity.util.Alert;
import com.projity.util.Environment;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.swing.undo.UndoableEditSupport;
import org.openproj.domain.task.Task;


/**
 * Manages the creation and deleting of assignments as well as events
 */
public class AssignmentService
{
	public void checkProtectedAndremove( 
		final Assignment _assignment,
		final Object _eventSource,
		final boolean _undo )
	{
		if (_assignment.isProtected() == true)
		{
			_assignment.showProtectionWarning();

			return;
		}

		remove( _assignment, true, _eventSource, new NodeUndoInfo(_undo) );
	}

	public boolean connect( 
		final Assignment _assignment,
		final Object _eventSource )
	{
		return connect( _assignment, _eventSource, true );
	}

	public boolean connect( 
		final Assignment _assignment,
		final Object _eventSource,
		final boolean _undo )
	{
		if (!connect( _assignment, _eventSource, new NodeUndoInfo(_undo) ))
		{
			return false;
		}

		//		UndoableEditSupport undoableEditSupport=getUndoableEditSupport(assignment);
		//		if (undoableEditSupport!=null&&undo){
		//			undoableEditSupport.postEdit(new AssignmentCreationEdit(assignment,eventSource));
		//		}
		return true;
	}

	public boolean connect( 
		final Node _node,
		final Object _eventSource,
		final boolean _undo )
	{
		return connect( (Assignment)_node.getValue(), _eventSource, new NodeUndoInfo(_node, _undo) );
	}

	public boolean connect( 
		final Assignment _assignment,
		final Object _eventSource,
		final NodeUndoInfo _undo )
	{
		if (!_assignment.getTask().isAssignable() && !Environment.isImporting())
		{
			return false;
		}

		((Task)_assignment.getTask()).addAssignment( _assignment );
		_assignment.getResource().addAssignment( _assignment );

		if (_eventSource != null)
		{
			_assignment.getDocument().getObjectEventManager().fireCreateEvent( _eventSource, _assignment, _undo );
			((ResourcePool)_assignment.getResource().getDocument()).getObjectEventManager()
			 .fireCreateEvent( _eventSource, _assignment, _undo );
		}

		return true;
	}

	public static AssignmentService getInstance()
	{
		if (instance == null)
		{
			instance = new AssignmentService();
		}

		return instance;
	}

	//undo
	public UndoableEditSupport getUndoableEditSupport( 
		final Assignment _assignment )
	{
		if (_assignment.getTask() == null)
		{
			return null;
		}
		else
		{
			return _assignment.getTask().getProject().getNodeModelDataFactory().getUndoController().getEditSupport();
		}
	}

	public boolean isSubstituting()
	{
		return substituting;
	}

	/**
	 * When importing, we don't update or recalculate duration
	 * @param task
	 * @param resource
	 * @param units
	 * @param delay
	 * @param eventSource
	 * @return
	 */
	public Assignment newAssignment( 
		final Task _task,
		final Resource _resource,
		final double _units,
		final long _delay,
		final Object _eventSource,
		final boolean _undo )
	{
		Assignment assignment = Assignment.getInstance( _task, _resource, _units, _delay );

		if (!connect( assignment, _eventSource, _undo ))
		{
			return null;
		}

		return assignment;
	}

	public Assignment newAssignment( 
		final Task _task,
		final Resource _resource,
		final double _units,
		final long _delay,
		final Object _eventSource )
	{
		return newAssignment( _task, _resource, _units, _delay, _eventSource, true );
	}

	public void newAssignments( 
		final Collection _tasks,
		final Collection _resources,
		final double _units,
		final long _delay,
		final Object _eventSource,
		final boolean _undo )
	{
		if ((_tasks.size() == 0) || (_resources.size() == 0))
		{
			return;
		}

		int transactionId = 0;
		Project transactionProject = null;

		for (Iterator i = _tasks.iterator(); i.hasNext();)
		{
			Task task = (Task)i.next();

			//			if (!task.isAssignable())
			//				continue;
			if (transactionId == 0)
			{
				transactionProject = task.getProject();
				transactionProject.beginUndoUpdate();
				transactionId = transactionProject.fireMultipleTransaction( 0, true );

				//backup before any assignment operation
				transactionProject.getNodeModelDataFactory().getUndoController().getEditSupport()
								  .postEdit( new ScheduleBackupEdit(_tasks, this) );
			}

			// if task currently has no assignments, then we should not change duration if adding several at once
			boolean taskHasNoAssignments = !task.hasRealAssignments() || !task.hasLaborAssignment();
			int oldSchedulingType = task.getSchedulingType();
			boolean oldEffortDriven = task.isEffortDriven();

			if (taskHasNoAssignments)
			{ // if adding for first time
				task.setSchedulingType( SchedulingType.FIXED_DURATION );
				task.setEffortDriven( false );
			}

			Iterator r = _resources.iterator();

			while (r.hasNext())
			{
				Resource resource = (Resource)r.next();

				if (null == task.findAssignment( resource ))
				{
					//					double units = 1.0D;
					//TODO Bug 330: this is slow and uses tons of memory when assigning many at once. optimizing by doing just one update
					//The result is that AssignmentNodeModel.objectChanged(ObjectEvent objectEvent) is called for each assignment
					//This needs to be batched as its current memory usage is unacceptable and it takes very long
					//Perhaps one solution would be to replace hierarchy search() with a hash table for mapping impls to nodes

					//TODO It throws an event for assignment. A service for updating all the assignments at once should be added.
					Assignment assignment = newAssignment( task, resource, _units, 0, _eventSource, true );

					if (!resource.isLabor()) // for assigning non temporal resources, use the value of 1
					{
						assignment.setRateUnit( TimeUnit.NON_TEMPORAL );
					}
				}
			}

			if (taskHasNoAssignments)
			{ // if adding for first time, put back effort driven value
				task.setSchedulingType( oldSchedulingType );
				task.setEffortDriven( oldEffortDriven );
			}
		}

		if (transactionId != 0)
		{
			transactionProject.fireMultipleTransaction( transactionId, false );
			transactionProject.endUndoUpdate();
		}
	}

	public void remove( 
		final Node _node,
		final Object _eventSource,
		final boolean _undo )
	{
		remove( (Assignment)_node.getValue(), true, _eventSource, new NodeUndoInfo(_node, _undo) );
	}

	//	public void remove(Assignment assignment, Object eventSource) {
	//		remove(assignment, eventSource,true);
	//	}
	public void remove( 
		final Assignment _assignment,
		final Object _eventSource,
		final boolean _undo )
	{
		remove( _assignment, true, _eventSource, new NodeUndoInfo(_undo) );
	}

	public void remove( 
		final Assignment _assignment,
		final boolean _cleanTaskLink,
		final Object _eventSource,
		final boolean _undo )
	{
		remove( _assignment, _cleanTaskLink, _eventSource, new NodeUndoInfo(_undo) );

		//		remove(assignment,(undo)?UNDO:eventSource);
		//		UndoableEditSupport undoableEditSupport=getUndoableEditSupport(assignment);
		//		if (undoableEditSupport!=null&&undo){
		//			undoableEditSupport.postEdit(new AssignmentDeletionEdit(assignment,eventSource));
		//		}
	}

	public void remove( 
		final Collection _assignments,
		final Object _eventSource,
		final boolean _undo )
	{
		UndoableEditSupport undoableEditSupport = null;

		try
		{
			for (Iterator i = _assignments.iterator(); i.hasNext();)
			{
				Assignment assignment = (Assignment)i.next();

				//				if (undoableEditSupport==null&&undo){
				//					undoableEditSupport=getUndoableEditSupport(assignment);
				//					if (undoableEditSupport!=null){
				//						undoableEditSupport.beginUpdate();
				//					}
				//				}
				remove
				//				if (undoableEditSupport==null&&undo){
				//					undoableEditSupport=getUndoableEditSupport(assignment);
				//					if (undoableEditSupport!=null){
				//						undoableEditSupport.beginUpdate();
				//					}
				//				}
				( assignment, true, _eventSource, _undo );
			}
		}
		finally
		{
			//			if (undoableEditSupport!=null&&undo){
			//				undoableEditSupport.endUpdate();
			//			}
		}
	}

	public void remove( 
		final Assignment _assignment,
		final boolean _cleanTaskLink,
		final Object _eventSource,
		final NodeUndoInfo _undo )
	{
		Task task = (Task)_assignment.getTask();
		Resource resource = _assignment.getResource();

		if (task.findAssignment( resource ) == null)
		{
			return; // avoids endless loop 9/1/06 hk
		}

		if (_cleanTaskLink)
		{
			task.removeAssignment( _assignment );
		}

		resource.removeAssignment( _assignment );

		//		//remove assignment snapshots too 18/7/2006 lc
		//		//if (resource!=ResourceImpl.getUnassignedInstance())
		//        for (int s=0;s<Settings.numBaselines();s++){
		//            TaskSnapshot snapshot=(TaskSnapshot)task.getSnapshot(new Integer(s));
		//            if (snapshot==null) continue;
		//            AssociationList snapshotAssignments=snapshot.getHasAssignments().getAssignments();
		//            if (snapshotAssignments.size()>0){
		//                for (Iterator j=snapshotAssignments.iterator();j.hasNext();){
		//                    Assignment snapshotAssignment=(Assignment)j.next();
		//                    if (snapshotAssignment.getTask()==assignment.getTask()&&snapshotAssignment.getResource()==assignment.getResource())
		//                    	j.remove();
		//                }
		//            }
		//            //if (snapshotAssignments.size()==0&&s!=Snapshottable.CURRENT.intValue()) task.setSnapshot(new Integer(s), null);
		//        }

		//			if (eventSource == null){ //case when default assignment is removed
		//				if ((undo==null||(undo!=null&&undo.isUndo()))){
		//					UndoableEditSupport undoableEditSupport=getUndoableEditSupport(assignment);
		//					if (undoableEditSupport!=null){
		//						undoableEditSupport.postEdit(new AssignmentDeletionEdit(assignment));
		//					}
		//				}
		//
		//			}else {
		if (_eventSource != null)
		{
			if (_cleanTaskLink)
			{
				_assignment.getDocument().getObjectEventManager().fireDeleteEvent( _eventSource, _assignment, _undo );
			}

			if (_assignment.getResource().getDocument() != null) // it's null if local project
			{
				((ResourcePool)_assignment.getResource().getDocument()).getObjectEventManager()
				 .fireDeleteEvent( _eventSource, _assignment );
			}
		}
	}

	public void remove( 
		final Collection _assignmentList,
		final Object _eventSource )
	{
		Assignment assignment;
		Iterator i = _assignmentList.iterator();

		while (i.hasNext())
		{
			assignment = (Assignment)i.next();
			remove( assignment, true, _eventSource, null );
		}
	}

	//fix
	public void remove( 
		final Collection _assignmentList,
		final Collection _toRemove )
	{
		Assignment assignment;
		Iterator i = _assignmentList.iterator();

		while (i.hasNext())
		{
			_toRemove.add( i.next() );
		}
	}

	public boolean replaceResource( 
		final long _projectId,
		final long _oldResourceId,
		final long _newResourceId,
		final Date _startingFrom,
		final int[] _taskIds )
	{
		final Project project = ProjectFactory.getInstance().findFromId( _projectId );

		if (!SessionFactory.getInstance().getSession( false ).getLock( project, true ))
		{ // note calling with true
			Alert.error( Messages.getString( "cannotLockDuringResourceSubstitution" ) );

			return false;
		}

		if (_taskIds != null)
		{
			Arrays.sort( _taskIds ); // for binary search later
		}

		final long startingTime = (_startingFrom == null)
			? 0
			: _startingFrom.getTime();
		final Resource oldResource = project.getResourcePool().findById( _oldResourceId );
		final Resource newResource = project.getResourcePool().findById( _newResourceId );

		System.out.println( "old resource " + oldResource + " id " + _oldResourceId );
		System.out.println( "new resource " + newResource + " id " + _newResourceId );

		Command command = new Command( Messages.getString( "ResourceSubstitutionDialogBox.title" ), project )
		{
			@Override
			public void execute( 
				final Object _arg0 )
			{
				Task task = (Task)_arg0;

				if ((_taskIds != null) 
					&& (Arrays.binarySearch( _taskIds, (int)task.getUniqueId() ) < 0)) // skip if not in list
				{
					return;
				}

				AssignmentService.getInstance().replaceResourceOnTask( task, oldResource, newResource, false, startingTime );
			}
		};

		int transactionId = project.fireMultipleTransaction( 0, true );
		project.forTasks( command );
		project.fireMultipleTransaction( transactionId, false );

		// publish
		SaveOptions opt = new SaveOptions();
		opt.setPublish( true );
		opt.setSync( true );
		opt.setReadActualsFirst( false );
		ProjectFactory.getInstance().saveProject( project, opt );

//		SessionFactory.getInstance().getSession(false).publish(project, false);
//		SessionFactory.getInstance().getSession(false).refreshMetadata(project);
		Alert.warn( Messages.getString( "Message.afterSubstitution" ) );

		boolean completelyRemoved = oldResource.getAssignments().isEmpty();

		if (completelyRemoved)
		{
			SessionFactory.getInstance().getSession( false ).removeAllocation( project, oldResource );
			project.getResourcePool().remove( oldResource );
		}

		return completelyRemoved;
	}

	public boolean replaceResourceOnTask( 
		final Task _task,
		Resource _oldResource,
		final Resource _newResource,
		final boolean _skipInProgress,
		final long _startingFrom )
	{
		if (_newResource == null)
		{
			System.out.println( "No new resource found. Aborting replace!" );

			return false;
		}

		if (_oldResource == _newResource)
		{
			return false;
		}

		if (_oldResource == null)
		{
			_oldResource = ResourceImpl.getUnassignedInstance();
		}

		Assignment oldAssignment = _task.findAssignment( _oldResource );

		if (oldAssignment == null)
		{
			return false;
		}

		if (oldAssignment.isComplete())
		{
			return false; // if complete, then don't replace
		}

		Assignment existingAssignment = _task.findAssignment( _newResource ); // see if already assigned, so need to increase units
		long oldActualWork = oldAssignment.getActualWork( null );

		if (_skipInProgress && (oldActualWork > 0))
		{
			return false;
		}

		if (oldAssignment.getEnd() <= _startingFrom) // if this assigment is before the date
		{
			return false;
		}

		double oldUnits = oldAssignment.getRemainingUnits();
		boolean effortDriven = _task.isEffortDriven();
		_task.setEffortDriven( false );

		int schedulingType = _task.getSchedulingType();

		long splitDate = Math.max( oldAssignment.getResume(), _startingFrom ); // date on which to break the assignment in two
		long oldAssignmentKeep = oldAssignment.work( 0, splitDate ); // work to preserve on original

		if ((_oldResource == ResourceImpl.getUnassignedInstance()) && _task.getProject().isActualsProtected())
		{ // special case of unassigned with percent complete - need project's version of actuals protected
			oldAssignmentKeep = 0; // percent complete must be removed since can't add it to a resource if protected
			_task.setPercentComplete( 0 );
			oldAssignment.setDirty( true );
		}

		long amountToTransfer = oldAssignment.work() - oldAssignmentKeep;

		//		System.out.println("old Units " + oldUnits + " old resume " + new java.util.Date(oldAssignment.getResume()) + " split " + new java.util.Date(splitDate) +  "keep on old " + DurationFormat.format(oldAssignmentKeep) + " transfer" + DurationFormat.format(amountToTransfer));
		if (existingAssignment != null)
		{ // increase work if already assigned
			existingAssignment.setWork( existingAssignment.work() + amountToTransfer, null );
			existingAssignment.setDirty( true );
		}
		else
		{ // replace the resource
			_task.setSchedulingType( SchedulingType.FIXED_WORK );

			Assignment newAssignment = newAssignment( _task, _newResource, oldUnits, 0, this, true );

			// JGao - 6/4/2009 Need to acount the Units are not 100 %
			amountToTransfer /= oldUnits;
			newAssignment.getDetail().setDuration( amountToTransfer );
			newAssignment.setStart( splitDate );
			System.out.println( "Made new assignment " + newAssignment + " " + amountToTransfer );

			//			System.out.println("ass delay after set start "  + DurationFormat.format(newAssignment.getDelay()));
		}

		if (oldAssignmentKeep == 0)
		{ // get rid of old assignment if not started
			System.out.println( "Removed old assignment " + oldAssignment );
			remove( oldAssignment, this, true );
		}
		else
		{ // get rid of remaining work
			System.out.println( "Keeping old assignment " + oldAssignment + " " + oldAssignmentKeep );
			oldAssignment.setDuration( (long)(oldAssignmentKeep / oldUnits) ); //
			oldAssignment.setDirty( true );
		}

		_task.setSchedulingType( schedulingType );
		_task.setEffortDriven( effortDriven );
		_task.setDirty( true );

		return true;
	}

	public boolean replaceResourceOnTaskWithTransaction( 
		final Task _task,
		final Resource _oldResource,
		final Resource _newResource,
		final boolean _skipInProgress,
		final long _startingFrom )
	{
		int transactionId = _task.getProject().fireMultipleTransaction( 0, true );
		_task.getProject().beginUndoUpdate();

		boolean result = replaceResourceOnTask( _task, _oldResource, _newResource, _skipInProgress, _startingFrom );
		_task.getProject().endUndoUpdate();
		_task.getProject().fireMultipleTransaction( transactionId, false );

		return result;
	}

	public void setSubstituting( 
		final boolean _substituting )
	{
		this.substituting = _substituting;
	}

	private static AssignmentService instance = null;
	private boolean substituting = false;
}
