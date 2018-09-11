/*
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */
package org.openproj.domain.task;

import com.projity.field.CustomFields;
import com.projity.field.FieldContext;
import com.projity.field.FieldParseException;

import com.projity.grouping.core.Node;
import com.projity.grouping.core.hierarchy.BelongsToHierarchy;

import com.projity.pm.assignment.Assignment;
import com.projity.pm.assignment.HasAssignments;
import com.projity.pm.assignment.HasTimeDistributedData;
import com.projity.pm.assignment.TimeDistributedFields;
import com.projity.pm.assignment.timesheet.UpdatesFromTimesheet;
import org.openproj.domain.calendar.HasCalendar;
import com.projity.pm.calendar.WorkCalendar;
import com.projity.pm.criticalpath.ScheduleWindow;
import com.projity.pm.criticalpath.TaskSchedule;
import com.projity.pm.dependency.HasDependencies;
import com.projity.pm.key.HasKey;
import com.projity.pm.resource.Resource;
import com.projity.pm.scheduling.CanBeLeveled;
import com.projity.pm.scheduling.Schedule;
import com.projity.pm.scheduling.SchedulingRule;
import com.projity.pm.snapshot.BaselineScheduleFields;
import com.projity.pm.snapshot.Snapshottable;
import com.projity.pm.task.*;

import com.projity.server.data.DataObject;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.Predicate;

import org.openproj.domain.costing.HasExpenseType;
import org.openproj.domain.document.BelongsToDocument;
import org.openproj.domain.document.Document;

import java.util.Collection;
import java.util.List;


/**
 *
 */
public interface Task
	extends BaselineScheduleFields,
		BelongsToDocument,
		BelongsToHierarchy,
		CanBeLeveled,
		CustomFields,
		DataObject,
		HasAssignments,
		HasCalendar,
		HasDependencies,
		HasExpenseType,
		HasKey,
		HasNotes,
		HasPriority,
		HasTimeDistributedData,
		Schedule,
		ScheduleWindow,
		Snapshottable,
		UpdatesFromTimesheet,
		TaskLinkReference,
//		TaskSpecificFields,
		TimeDistributedFields
{
	/**
	 * Add an assignment to the task.  A task always has at least one assignment, whether or not
	 * it has any true assignments.  This is because a default assignment is always present.  This
	 * greatly facilitates other calculations.  This method takes care to either create or delete
	 * the default assignment.
	 *
	 */
	Assignment addDefaultAssignment();

	/** Used when an assignment advancement changes
	 *
	 */
	void adjustActualStartFromAssignments();

	/**
	 *
	 * @param _addTo
	 * @param _markerStatus
	 * @param _depth
	 */
	void arrangeChildren( 
		final Collection _addTo,
		final boolean _markerStatus,
		final int _depth );

	/** Arranges a task in predecessor/parent order in a collection
	 *
	 * @param _addTo
	 * @param _markerStatus
	 * @param _depth
	 */
	void arrangeTask( 
		final Collection _addTo,
		final boolean _markerStatus,
		final int _depth );

	/**
	 * Assigns the actual start and completed date fields of parents based on
	 * children values
	 *
	 */
	void assignActualDatesFromChildren();

	/**
	 *
	 * @param _snapshotId
	 * @return
	 */
	Object backupDetail( Object _snapshotId );

	/**
	 *
	 */
	void cleanClone();

	/**
	 * Cleans up all links to and form this task and removes all assignments,
	 * including baseline ones.
	 *
	 * @param _eventSource if not null, then events will be sent indicating the removal of links and assignments
	 * @param _deep
	 * @param _undo
	 * @param _cleanDependencies
	 */
	void cleanUp( Object _eventSource, boolean _deep, boolean _undo, boolean _cleanDependencies );

	/**
	 *
	 * @return
	 */
	Task clone();

	/**
	 *
	 * @param _task
	 */
	void cloneTo( Task _task );

	/**
	 *
	 */
	void connectToProject();

	/**
	 *
	 * @param _to
	 */
	void copyScheduleTo( 
		final Task _to );

	boolean fieldHideSubprojectFile( 
		final FieldContext _fieldContext );

	boolean fieldHideSubprojectReadOnly( 
		final FieldContext _fieldContext );

	//	/**
	//	 * 
	//	 * @return 
	//	 */
	//	Object[] fieldOptionsScheduleConstraint();

	/**
	 *
	 * @param _c
	 */
	void forSnapshots( 
		final Closure _c );

	/**
	 *
	 * @param _c
	 * @param _s
	 */
	void forSnapshots( 
		final Closure _c,
		final int _s );

	/**
	 *
	 * @param _c
	 * @param _onlyCurrent
	 */
	void forSnapshotsAssignments( 
		final Closure _c,
		final boolean _onlyCurrent );

	/**
	 *
	 * @param _c
	 * @param _s
	 */
	void forSnapshotsAssignments( 
		final Closure _c,
		final int _s );

	//	/**
	//	 * @return
	//	 */
	//	long getActualDuration();

	/**
	 *
	 * @param _resource
	 * @return
	 */
	Assignment getBaselineAssignment( 
		final Resource _resource );

	/**
	 *
	 * @param _resource
	 * @param _snapshot
	 * @param _createIfDoesntExist
	 * @return
	 */
	Assignment getBaselineAssignment( 
		final Resource _resource,
		final Object _snapshot,
		final boolean _createIfDoesntExist );

	/**
	 *
	 * @return
	 */
	long getBaselineFinish();

	/*
	 *
	 */
	TaskSnapshot getBaselineSnapshot();

	/**
	 * @return Returns the calculationStateCount.
	 */
	int getCalculationStateCount();

	/**
	 * @return Returns the taskSchedule.
	 */
	TaskSchedule getCurrentSchedule();

	/**
	 *
	 * @return
	 */
	CustomFields getCustomFields();

	/**
	 *
	 * @return
	 */
	int getDebugDependencyOrder();

	Resource getDelegatedTo();

	/**
	 *
	 * @return
	 */
	String getDelegatedToName();

	/**
	 *
	 * @return
	 */
	long getDurationMillis();

	/**
	 *
	 * @return
	 */
	TaskSchedule getEarlySchedule();

	int getEarnedValueMethod();

	/**
	 *
	 * @return
	 */
	Project getEnclosingProject();

	/**
	 *
	 * @return
	 */
	SubProj getEnclosingSubproject();

	/**
	 * Will return a node in the master project that holds he subproject
	 *
	 * @return
	 */
	Node getEnclosingSubprojectNode();

	double getFixedCost();

	int getFixedCostAccrual();

	/**
	 *
	 * @return
	 */
	String getInvestmentMilestone();

	/**
	 *
	 * @return
	 */
	long getLastSavedFinish();

	/**
	 *
	 * @return
	 */
	long getLastSavedParentId();

	/**
	 *
	 * @return
	 */
	long getLastSavedPosistion();

	/**
	 *
	 * @return
	 */
	long getLastSavedStart();

	/**
	 *
	 * @return
	 */
	TaskSchedule getLateSchedule();

	/**
	 *
	 * @return
	 */
	Document getMasterDocument();

	/**
	 *
	 * @param _outlineNumber
	 * @return
	 */
	int getOutlineLevel( 
		final int _outlineNumber );

	/**
	 *
	 * @return
	 */
	int getOutlineLevel();

	/**
	 *
	 * @return
	 */
	Project getOwningProject();

	/**
	 *
	 * @return
	 */
	List getPersistedAssignments();

	/**
	 * @return Returns the physicalPercentComplete.
	 */
	double getPhysicalPercentComplete();

	/**
	 * Get a task's position in the predecessor-ordered list of the critical path. Parent tasks will have a pair indicating the
	 * parent start and parent end
	 *
	 * @return
	 */
	String getPredecessorOrder();

	/**
	 *
	 * @return
	 */
	String getPredecessors();

	/**
	 *
	 * @return
	 */
	WorkCalendar getProjectCalendar();

	/**
	 *
	 * @return
	 */
	long getProjectId();

	String getResourceGroup();

	String getResourceInitials();

	String getResourceNames();

	String getResourcePhonetics();

	/**
	 *
	 * @return
	 */
	Project getRootProject();

	/**
	 *
	 */
	TaskSchedule getSchedule( 
		final int _scheduleType );

	/**
	 * Gets a (singleton) instance of the scheduling rule to use for the task
	 *
	 * @return scheduling rule to use in adjust...() calculations
	 */
	SchedulingRule getSchedulingRule();

	String getSubprojectFile();

	/**
	 *
	 * @return
	 */
	String getSuccessors();

	/**
	 *
	 * @return
	 */
	String getTaskAndProjectName();

	WorkCalendar getTaskCalendar();

	long getTotalSlackEnd();

	long getTotalSlackStart();

	/**
	 *
	 * @return
	 */
	String getUniqueIdPredecessors();

	/**
	 *
	 * @return
	 */
	String getUniqueIdSuccessors();

	/**
	 *
	 * @return
	 */
	short getVersion();

	/**
	 * @return Returns the wbs.
	 */
	String getWbs();

	/**
	 * @return Returns the wbsChildrenNodes.
	 */
	Collection getWbsChildrenNodes();

	/**
	 *
	 * @return
	 */
	List getWbsChildrenTasks();

	String getWbsParentName();

	/**
	 * @return Returns the wbsParent.
	 */
	Task getWbsParentTask();

	/**
	 *
	 * @return
	 */
	String getWbsPredecessors();

	/**
	 *
	 * @return
	 */
	String getWbsSuccessors();

	/**
	 *
	 * @return
	 */
	boolean hasDuration();

	/**
	 *
	 * @return
	 */
	boolean hasRealAssignments();

	/**
	 *
	 * @return
	 */
	boolean inProgress();

	/**
	 *
	 */
	void invalidateSchedules();

	/**
	 *
	 * @return
	 */
	boolean isActualsProtected();

	/**
	 *
	 * @param _other
	 * @return
	 */
	boolean isAncestorOrDescendent( 
		final Task _other );

	/**
	 *
	 * @return
	 */
	boolean isAssignable();

	/**
	 *
	 * @return
	 */
	boolean isAssignment();

	/**
	 *
	 * @param _numBaseline
	 * @return
	 */
	boolean isBaselineTouched( 
		final Object _numBaseline );

	/**
	 *
	 * @return
	 */
	boolean isCritical();

	/**
	 *
	 * @return
	 */
	boolean isDelegatedToUser();

	/** Return an indication whether or not the duration of the task has only been estimated
	 *
	 * @return
	 */
	boolean isEstimated();

	/**
	 *
	 * @return
	 */
	boolean isExternal();

	boolean isIgnoreResourceCalendar();

	/**
	 *
	 * @return
	 */
	boolean isInSubproject();

	/**
	 *
	 * @return
	 */
	boolean isLateStarting();

	// task type is taken care of by schedulingRule
	boolean isMarkTaskAsMilestone();

	/**
	 *
	 * @return
	 */
	boolean isMilestone();

	/**
	 *
	 * @return
	 */
	boolean isMissedDeadline();

	//	/**
	//	 * 
	//	 * @return 
	//	 */
	//	boolean isNew();

	/**
	 *
	 * @return
	 */
	boolean isNormal();

	/**
	 * if task is currently critical or will become critical after CP
	 */
	boolean isOrWasCritical();

	/**
	 *
	 * @return
	 */
	boolean isPinned();

	//	/**
	//	 * @param other
	//	 * @return
	//	 */
	//	boolean isPredecessorOfDescendent( Task _other ); 

	/**
	 *
	 * @return
	 */
	boolean isReadOnly();

	boolean isReadOnlyPercentWorkComplete( 
		final FieldContext _context );

	/**
	 * Is this task reverse scheduled: Is it ALAP in forward scheduling or ASAP in reverse?
	 *
	 * @return
	 */
	boolean isReverseScheduled();

	/**
	 *
	 * @return
	 */
	boolean isRoot();

	/**
	 *
	 * @return
	 */
	boolean isStartingWithinOneWeek();

	/**
	 *
	 * @return
	 */
	boolean isStartingWithinTwoWeeks();

	/**
	 *
	 * @return
	 */
	boolean isSubproject();

	boolean isSubprojectReadOnly();

	/**
	 *
	 * @return
	 */
	boolean isSummary();

	/**
	 *
	 * @return
	 */
	boolean isTestImportOk();

	/**
	 *
	 * @return
	 */
	boolean isUnstarted();

	boolean isWbsParent();

	/**
	 *
	 * @return
	 */
	boolean isZeroDuration();

	/**
	 *
	 * @return
	 */
	boolean liesInSubproject();

	/**
	 * Flags all tasks which depend on this one for scheduling as dirty
	 *
	 * @param doSelf TODO
	 *
	 */
	void markAllDependentTasksAsNeedingRecalculation( 
		final boolean _doSelf );

	/**
	 *
	 */
	void markTaskAsNeedingRecalculation();

	/**
	 *
	 * @param _eventSource
	 */
	void recalculate( 
		final Object _eventSource );

	/**
	 *
	 * @param _eventSource
	 */
	void recalculateLater( 
		final Object _eventSource );

	/**
	 *
	 * @param _snapshotId
	 * @param _b
	 */
	void restoreSnapshot( 
		final Object _snapshotId,
		final Object _b );

	/**
	 * Parent tasks have only 3 possible constraint types ASAP/ALAP (depending on forward or reverse scheduling), SNET, and FNLT
	 */
	void restrictToValidConstraintType();

	/**
	 *
	 * @param _actualStart
	 */
	void setActualStartNoEvent( 
		final long _actualStart );

	/**
	 *
	 */
	void setAllSchedulesToCurrentDates();

	/**
	 * @param calculationStateCount The calculationStateCount to set.
	 */
	void setCalculationStateCount( 
		final int _calculationStateCount );

	/**
	 *
	 * @param _debugDependencyOrder
	 */
	void setDebugDependencyOrder( 
		final int _debugDependencyOrder );

	void setDelegatedTo( 
		final Resource _delegatedTo );

	void setEarnedValueMethod( 
		final int _earnedValueMethod );

	/**
	 *
	 * @param _end
	 * @param _fieldContext
	 */
	void setEnd( 
		final long _end,
		final FieldContext _fieldContext );

	/** Assign an indication whether or not the duration of the task has only been estimated
	 *
	 * @param _estimated used as the source of the assignment.
	 */
	void setEstimated( 
		final boolean _estimated );

	/**
	 *
	 * @param _external
	 */
	void setExternal( 
		final boolean _external );

	/**
	 * Set constraint FNET
	 *
	 * @param date
	 */
	void setFinishNoEarlierThan( 
		final long _date );

	/**
	 * Set constraint FNLT
	 *
	 * @param date
	 */
	void setFinishNoLaterThan( 
		final long _date );

	void setFixedCost( 
		final double _fixedCost );

	void setFixedCostAccrual( 
		final int _fixedCostAccrual );

	/**
	 * Set the task to be forward or reverse scheduled
	 *
	 * @param forward
	 */
	void setForward( 
		final boolean _forward );

	void setIgnoreResourceCalendar( 
		final boolean _ignoreResourceCalendar );

	/**
	 *
	 * @param _inSubproject
	 */
	void setInSubproject( 
		final boolean _inSubproject );

	/**
	 *
	 * @param _currendFinish
	 */
	void setLastSavedFinish( 
		final long _currendFinish );

	/**
	 *
	 * @param _lastSavedParentId
	 */
	void setLastSavedParentId( 
		final long _lastSavedParentId );

	/**
	 *
	 * @param _lastSavedPosistion
	 */
	void setLastSavedPosistion( 
		final long _lastSavedPosistion );

	/**
	 *
	 * @param _currentStart
	 */
	void setLastSavedStart( 
		final long _currentStart );

	void setMarkTaskAsMilestone( 
		final boolean _markTaskAsMilestone );

	/**
	 * @param markerStatus The markerStatus to set.
	 */
	void setMarkerStatus( 
		final boolean _markerStatus );

	/**
	 *
	 * @param _dirty
	 */
	void setMeAndAncestorsDirty( 
		final boolean _dirty );

	/**
	 *
	 * @param _owningProject
	 */
	void setOwningProject( 
		final Project _owningProject );

	void setPercentWorkComplete( 
		final double _percentWorkComplete );

	/**
	 *
	 * @param _persistedAssignments
	 */
	void setPersistedAssignments( 
		final List _persistedAssignments );

	/**
	 * @param physicalPercentComplete The physicalPercentComplete to set.
	 */
	void setPhysicalPercentComplete( 
		final double _physicalPercentComplete );

	/**
	 *
	 * @param _pinned
	 */
	void setPinned( 
		final boolean _pinned );

	/**
	 *
	 * @param _predecessors
	 * @throws FieldParseException
	 */
	void setPredecessors( 
		final String _predecessors )
		throws FieldParseException;

	/**
	 * @param _project The project to set.
	 */
	void setProject( 
		final Project _project );

	/**
	 *
	 * @param _projectId
	 */
	void setProjectId( 
		final long _projectId );

	/**
	 *
	 * @param _constraintType
	 */
	void setRawConstraintType( 
		final int _constraintType );

	/**
	 * Sets the duration without controls that setDuration performs
	 *
	 * @param _duration
	 */
	void setRawDuration( 
		final long _duration );

	void setResourceInitials( 
		final String _resourceInitials )
		throws FieldParseException;

	void setResourceNames( 
		final String _resourceNames )
		throws FieldParseException;

	/**
	 *
	 * @param _root
	 */
	void setRoot( 
		final boolean _root );

	/**
	 *
	 * @param _constraintType
	 * @param _date
	 */
	void setScheduleConstraintAndUpdate( 
		final int _constraintType,
		final long _date );

	//	Don't know if need this or not.  In any case, need to work out hierarchy treatment.
	//	NodeHierarchy getNodeHierarchy();
	void setStart( 
		final long _start,
		final FieldContext _fieldContext );

	/**
	 * Set constraint SNET
	 *
	 * @param date
	 */
	void setStartNoEarlierThan( 
		final long _date );

	/**
	 * Set constraint SNLT
	 *
	 * @param date
	 */
	void setStartNoLaterThan( 
		final long _date );

	void setSubprojectFile( 
		final String _sub );

	/**
	 *
	 * @param _successors
	 * @throws FieldParseException
	 */
	void setSuccessors( 
		final String _successors )
		throws FieldParseException;

	/**
	 *
	 */
	void setTaskAssignementAndPredsDirty();

	void setTaskCalendar( 
		final WorkCalendar _workCalendar );

	/**
	 *
	 * @param _predecessors
	 * @throws FieldParseException
	 */
	void setUniqueIdPredecessors( 
		final String _predecessors )
		throws FieldParseException;

	/**
	 *
	 * @param _successors
	 * @throws FieldParseException
	 */
	void setUniqueIdSuccessors( 
		final String _successors )
		throws FieldParseException;

	/**
	 * @param wbs The wbs to set.
	 */
	void setWbs( 
		final String _wbs );

	/**
	 * @param wbsChildrenNodes The wbsChildrenNodes to set.
	 */
	void setWbsChildrenNodes( 
		final Collection _wbsChildrenNodes );

	/**
	 * @param wbsParent The wbsParent to set.
	 */
	void setWbsParent( 
		final Task _wbsParentTask );

	/**
	 *
	 * @param _work
	 */
	void setWork( 
		final long _work );

	/**
	 *
	 * @return
	 */
	boolean startsBeforeProject();

	/**
	 *
	 * @param _numBaseline
	 */
	void touchBaseline( 
		final Object _numBaseline );

	/**
	 *
	 */
	void updateCachedDuration();

	/**
	 *
	 */
	void updateEndSentinel();

	/**
	 * Update a task from the Update Project dialog. There are several options:
	 *
	 * @param _date                         Date to either update completion to, or to move remaining work to
	 * @param _updateWorkAsCompleteThrough  If true, then update % complete, if false, then move remaining to the date
	 * @param _setFractionalPercentComplete If true, then allow setting of % complete for uncompleted tasks, otherwise, if a task
	 *                                        is not completed, it's current completion will not be modified
	 *
	 * @return True if task was updated, false if unchanged
	 */
	boolean updateProjectTask( 
		final long _date,
		final boolean _updateWorkAsCompleteThrough,
		final boolean _setFractionalPercentComplete );

	/**
	 *
	 */
	void updateStartSentinel();

	/**
	 *
	 * @return
	 */
	boolean validateConstraints();

	/**
	 * See if a this task is a child, grandchild... of another
	 *
	 * @param potentialParentTask - task to see if parent
	 *
	 * @return true if the task descends from potentialParentTask
	 */
	boolean wbsDescendentOf( 
		final Task _potentialParentTask );

	/**
	 *
	 */
	public static class Predicates
	{
		public static Predicate instanceofPredicate()
		{
			return new Predicate()
				{
					@Override
					public boolean evaluate( 
						final Object _arg0 )
					{
						return _arg0 instanceof org.openproj.domain.task.Task;
					}
				};
		}
	}
}
