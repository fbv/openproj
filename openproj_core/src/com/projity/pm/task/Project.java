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
package com.projity.pm.task;

import org.openproj.domain.document.BelongsToDocument;
import org.openproj.domain.model.task.TaskBackup;
import com.projity.algorithm.ReverseQuery;

import com.projity.association.AssociationList;
import com.projity.association.InvalidAssociationException;

import com.projity.configuration.CircularDependencyException;
import com.projity.configuration.Configuration;
import com.projity.configuration.FieldDictionary;
import com.projity.configuration.Settings;

import com.projity.datatype.Duration;
import com.projity.datatype.Hyperlink;
import com.projity.datatype.ImageLink;
import com.projity.datatype.Money;
import com.projity.datatype.Work;

import org.openproj.domain.document.Document;
import com.projity.document.ObjectEvent;
import com.projity.document.ObjectEventManager;
import com.projity.document.ObjectSelectionEventManager;

import com.projity.field.Field;
import com.projity.field.FieldContext;
import com.projity.field.HasExtraFields;

import com.projity.functor.IntervalConsumer;

import com.projity.graphic.configuration.GraphicConfiguration;
import com.projity.graphic.configuration.SpreadSheetFieldArray;

import com.projity.grouping.core.Node;
import com.projity.grouping.core.NodeException;
import com.projity.grouping.core.NodeFactory;
import com.projity.grouping.core.NodeList;
import com.projity.grouping.core.NodeVisitor;
import com.projity.grouping.core.OutlineCollection;
import com.projity.grouping.core.OutlineCollectionImpl;
import com.projity.grouping.core.event.HierarchyEvent;
import com.projity.grouping.core.event.HierarchyListener;
import com.projity.grouping.core.hierarchy.NodeHierarchy;
import com.projity.grouping.core.model.AssignmentNodeModel;
import com.projity.grouping.core.model.DefaultNodeModel;
import com.projity.grouping.core.model.NodeModel;
import com.projity.grouping.core.model.NodeModelDataFactory;
import com.projity.grouping.core.model.NodeModelFactory;
import com.projity.grouping.core.model.NodeModelUtil;
import com.projity.grouping.core.transform.filtering.NotAssignmentFilter;

import com.projity.options.CalendarOption;
import com.projity.options.TimesheetOption;

import com.projity.pm.assignment.Assignment;
import com.projity.pm.assignment.AssignmentKey;
import com.projity.pm.assignment.HasTimeDistributedData;
import com.projity.pm.assignment.TimeDistributedDataConsolidator;
import com.projity.pm.assignment.TimeDistributedFields;
import com.projity.pm.assignment.timesheet.TimesheetHelper;
import com.projity.pm.assignment.timesheet.UpdatesFromTimesheet;
import com.projity.pm.calendar.CalendarService;
import org.openproj.domain.calendar.HasBaseCalendar;
import org.openproj.domain.calendar.HasCalendar;
import com.projity.pm.calendar.WorkCalendar;
import com.projity.pm.calendar.WorkingCalendar;
import com.projity.pm.costing.CurrencyRateTable;
import com.projity.pm.costing.EarnedValueCalculator;
import com.projity.pm.costing.EarnedValueFields;
import com.projity.pm.costing.EarnedValueValues;
import org.openproj.domain.costing.ExpenseType;
import org.openproj.domain.costing.HasExpenseType;
import com.projity.pm.criticalpath.CriticalPath;
import com.projity.pm.criticalpath.HasSentinels;
import com.projity.pm.criticalpath.SchedulingAlgorithm;
import com.projity.pm.dependency.Dependency;
import com.projity.pm.dependency.DependencyKey;
import com.projity.pm.dependency.DependencyService;
import com.projity.pm.key.HasKey;
import com.projity.pm.key.HasKeyImpl;
import com.projity.pm.resource.Resource;
import com.projity.pm.resource.ResourcePool;
import com.projity.pm.scheduling.BarClosure;
import com.projity.pm.scheduling.ConstraintType;
import com.projity.pm.scheduling.Schedule;
import com.projity.pm.scheduling.ScheduleEvent;
import com.projity.pm.scheduling.ScheduleEventListener;
import com.projity.pm.scheduling.ScheduleEventManager;
import com.projity.pm.scheduling.ScheduleInterval;
import com.projity.pm.scheduling.ScheduleUtil;
import com.projity.pm.snapshot.BaselineScheduleFields;
import com.projity.pm.snapshot.Snapshottable;
import com.projity.pm.snapshot.SnapshotContainer;
import com.projity.pm.time.HasStartAndEnd;
import com.projity.pm.time.MutableHasStartAndEnd;

import com.projity.print.PrintSettings;

import com.projity.server.access.ErrorLogger;
import com.projity.server.data.DataObject;
import com.projity.server.data.DistributionComparator;
import com.projity.server.data.DistributionConverter;
import com.projity.server.data.DistributionData;

import com.projity.session.FileHelper;

import com.projity.strings.Messages;

import com.projity.transaction.MultipleTransactionManager;

import com.projity.undo.ClearSnapshotEdit;
import com.projity.undo.DataFactoryUndoController;
import com.projity.undo.NodeCreationEdit;
import com.projity.undo.SaveSnapshotEdit;

import com.projity.util.Alert;
import com.projity.util.ClassUtils;
import com.projity.util.DateTime;
import com.projity.util.Environment;

import com.projity.workspace.SavableToWorkspace;
import com.projity.workspace.WorkspaceSetting;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;

import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;
import javax.swing.undo.UndoableEditSupport;
import org.openproj.domain.model.Model;

import org.openproj.domain.model.project.TaskBattery;
import org.openproj.domain.task.Task;

/**
 * Project class
 */
public class Project
	implements BaselineScheduleFields,
		BelongsToDocument,
		DataObject,
		Document,
		EarnedValueFields,
		EarnedValueValues,
		HasBaseCalendar,
		HasCalendar,
		HasExpenseType,
		HasExtraFields,
		HasKey,
		HasNotes,
		HasPriority,
		HasSentinels,
		HasTimeDistributedData,
		HierarchyListener,
		MutableHasStartAndEnd,
		ProjectSpecificFields,
		SavableToWorkspace,
		Schedule,
		TimeDistributedFields,
		UpdatesFromTimesheet
{
	private Project( 
		final boolean _local )
	{
		super();

		// get the appropriate subproject handler
		initSubprojectHandler();
		myHasKey = new HasKeyImpl( _local, this );

		WorkCalendar wc = CalendarService.getInstance().getDefaultInstance();
		setWorkCalendar( wc );

		myStart = CalendarOption.getInstance().makeValidStart( DateTime.midnightToday(), true );
		myStart = getEffectiveWorkCalendar().adjustInsideCalendar( myStart, false );
		myEnd = myStart;

		// JGao 7/1/2009 - If company calendar presents, use the non default calendar option
		myCalendarOption = (wc.getName() == "Company")
			? CalendarOption.getInstance()
			: CalendarOption.getDefaultInstance();
	}

	private Project( 
		final ResourcePool _resourcePool,
		final DataFactoryUndoController _undo )
	{
		this( _resourcePool.isLocal() );
		myResourcePool = _resourcePool;
		
		myNodeModelDataFactory.setUndoController( _undo );
	}

	/* (non-Javadoc)
	 * @see com.projity.grouping.core.Node#accept(com.projity.grouping.core.NodeVisitor)
	 */
	public void accept( 
		final NodeVisitor _visitor )
	{
		_visitor.execute( this );
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#actualCost(long, long)
	 */
	@Override
	public double actualCost( 
		final long _start,
		final long _end )
	{
		return TimeDistributedDataConsolidator.actualCost( _start, _end, childrenToRollup() );
	}

	@Override
	public double actualFixedCost( 
		final long _start,
		final long _end )
	{
		return TimeDistributedDataConsolidator.actualFixedCost( _start, _end, childrenToRollup() );
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#actualWork(long, long)
	 */
	@Override
	public long actualWork( 
		final long _start,
		final long _end )
	{
		return TimeDistributedDataConsolidator.actualWork( _start, _end, childrenToRollup(), true );
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.costing.EarnedValueValues#acwp(long, long)
	 */
	@Override
	public double acwp( 
		final long _start,
		final long _end )
	{
		return TimeDistributedDataConsolidator.acwp( _start, _end, childrenToRollup() );
	}

	/**
	 * @param _listener
	 */
	@Override
	public void addObjectListener( 
		final ObjectEvent.Listener _listener )
	{
		myObjectEventManager.addListener( _listener );
	}

	public void addProjectListener( 
		final ProjectListener _listener )
	{
		myProjectListenerList.add( ProjectListener.class, _listener );
	}

	/**
	 * @param listener
	 */
	public void addScheduleListener( 
		final ScheduleEventListener _listener )
	{
		myScheduleEventManager.addListener( _listener );
	}

	public void addToDefaultOutline( 
		final Node _parentNode,
		final Node _childNode )
	{
		myTaskOutlines.addToDefaultOutline( _parentNode, _childNode );

		if (_parentNode == null)
		{
			return;
		}

		setDefaultRelationship( _parentNode, _childNode );
	}

	public void addToDefaultOutline( 
		final Node _parentNode,
		final Node _childNode,
		final int _position,
		final boolean _event )
	{
		myTaskOutlines.addToDefaultOutline( _parentNode, _childNode, _position, _event );

		if ((_parentNode == null) 
		 || (_childNode.isVoid() == true))
		{
			return;
		}

		setDefaultRelationship( _parentNode, _childNode );
	}

	public String[] availableInvestmentMilestones()
	{
		final ArrayList<String> list = new ArrayList<String>();

		for (String i : myInvestmentMilestones.keySet())
		{
			if (findFromInvestmentMilestone( i ) == null)
			{
				list.add( i );
			}
		}

		return (String[])list.toArray();
	}

	public static void forAssignments( 
		final Task _task,
		final AssignmentClosure _closure )
	{
		for (int s = 0; s < Settings.numBaselines(); s++)
		{
			final TaskSnapshot snapshot = (TaskSnapshot)_task.getSnapshot( Integer.valueOf( s ) );

			if (snapshot == null)
			{
				continue;
			}

			final AssociationList snapshotAssignments = snapshot.getHasAssignments().getAssignments();

			if (snapshotAssignments.size() > 0)
			{
				for (Iterator j = snapshotAssignments.iterator(); j.hasNext();)
				{
					_closure.execute( (Assignment)j.next(), s );
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.costing.EarnedValueValues#bac(long, long)
	 */
	@Override
	public double bac( 
		final long _start,
		final long _end )
	{
		return TimeDistributedDataConsolidator.bac( _start, _end, childrenToRollup() );
	}

	@Override
	public Object backupDetail()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#cost(long, long)
	 */
	@Override
	public double baselineCost( 
		final long _start,
		final long _end )
	{
		return TimeDistributedDataConsolidator.baselineCost( _start, _end, childrenToRollup() );
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#cost(long, long)
	 */
	@Override
	public long baselineWork( 
		final long _start,
		final long _end )
	{
		return TimeDistributedDataConsolidator.baselineWork( _start, _end, childrenToRollup(), true );
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.costing.EarnedValueValues#bcwp(long, long)
	 */
	@Override
	public double bcwp( 
		final long _start,
		final long _end )
	{
		return TimeDistributedDataConsolidator.bcwp( _start, _end, childrenToRollup() );
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.costing.EarnedValueValues#bcws(long, long)
	 */
	@Override
	public double bcws( 
		final long _start,
		final long _end )
	{
		return TimeDistributedDataConsolidator.bcws( _start, _end, childrenToRollup() );
	}

	public void beginUndoUpdate()
	{
		if (myUndoController != null)
		{
			myUndoController.beginUpdate();
		}
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#buildReverseQuery(com.projity.algorithm.ReverseQuery)
	 */
	@Override
	public void buildReverseQuery( 
		final ReverseQuery _reverseQuery )
	{
		myTasks.buildReverseQuery( _reverseQuery );
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#childrenToRollup()
	 */
	@Override
	public Collection<Task> childrenToRollup()
	{
		return myTasks.collection();
	}

	public void clearAllSnapshots()
	{
		for (int i = 0; i < Settings.NUM_ARRAY_BASELINES; i++)
		{
			clearSnapshot( Integer.valueOf( i ), true, null, false );
		}
	}

	public void clearSnapshot( 
		final Object _snapshotId,
		final boolean _entireProject,
		final List _selection,
		final boolean _undo )
	{
		Iterator i;

		if (_entireProject == true)
		{
			i = getTaskOutlineIterator();
		}
		else
		{
			i = (_selection == null)
				? null
				: _selection.iterator();
		}

		final Collection snapshotDetails;
		final boolean[] foundSnapshot = new boolean[ 1 ]; //no undo edit of there is no snapshot

		if (_undo && (i != null) && i.hasNext())
		{
			snapshotDetails = new ArrayList();

			while (i.hasNext())
			{
				Task task = (Task)i.next();
				TaskBackup taskBackup = (TaskBackup)task.backupDetail( _snapshotId );

				if (taskBackup.getSnapshot() != null)
				{
					foundSnapshot[ 0 ] = true;
				}

				snapshotDetails.add( taskBackup );
			}
		}
		else
		{
			snapshotDetails = null;
		}

		if (_entireProject == true)
		{
			forTasks( new SnapshotContainer.ClearSnapshotClosure( _snapshotId ) );
		}
		else
		{
			CollectionUtils.forAllDo( _selection, new SnapshotContainer.ClearSnapshotClosure( _snapshotId ) );
		}

		//		FieldEvent.fire(this, Configuration.getFieldFromId("Field.baseline" + snapshotId + "Cost"), null);
		fireBaselineChanged//		FieldEvent.fire(this, Configuration.getFieldFromId("Field.baseline" + snapshotId + "Cost"), null);
		( this, null, (Integer)_snapshotId, false );

		if (foundSnapshot[ 0 ])
		{
			UndoableEditSupport undoableEditSupport = myNodeModelDataFactory.getUndoController().getEditSupport();

			if (undoableEditSupport != null)
			{
				undoableEditSupport.postEdit( new ClearSnapshotEdit( this, _snapshotId, _entireProject, _selection, snapshotDetails ) );
			}
		}
	}

	public List copy( 
		final List<Node> _nodes )
	{
		if ((_nodes == null) 
		 || (_nodes.isEmpty() == true))
		{
			return null;
		}

		NodeModel model = getTaskOutline();
		List result = model.copy( _nodes, NodeModel.NORMAL );

		return result;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#cost(long, long)
	 */
	@Override
	public double cost( 
		final long _start,
		final long _end )
	{
		return TimeDistributedDataConsolidator.cost( _start, _end, childrenToRollup() );
	}

	public List cut( 
		List<Node> _nodes )
	{
		if ((_nodes == null) 
		 || (_nodes.size() == 0))
		{
			return null;
		}

		NodeModel model = getTaskOutline();
		List result = model.cut( _nodes, NodeModel.NORMAL );

		return result;
	}

	public void endUndoUpdate()
	{
		if (myUndoController != null)
		{
			myUndoController.endUpdate();
		}
	}

	@Override
	public boolean fieldHideActualCost( 
		FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	@Override
	public boolean fieldHideActualFixedCost( 
		FieldContext _fieldContext )
	{
		return false;
	}

	@Override
	public boolean fieldHideActualWork( 
		FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	@Override
	public boolean fieldHideAcwp( 
		FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	@Override
	public boolean fieldHideBac( 
		FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	@Override
	public boolean fieldHideBaselineCost( 
		int _numBaseline,
		FieldContext _fieldContext )
	{
		return false; //TODO implement
	}

	@Override
	public boolean fieldHideBaselineWork( 
		int _numBaseline,
		FieldContext _fieldContext )
	{
		return false; //TODO implement
	}

	@Override
	public boolean fieldHideBcwp( 
		FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	@Override
	public boolean fieldHideBcws( 
		FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	@Override
	public boolean fieldHideCost( 
		FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	@Override
	public boolean fieldHideCpi( 
		FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	@Override
	public boolean fieldHideCv( 
		FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	@Override
	public boolean fieldHideCvPercent( 
		FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	@Override
	public boolean fieldHideEac( 
		FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	@Override
	public boolean fieldHideSpi( 
		FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	@Override
	public boolean fieldHideSv( 
		FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	@Override
	public boolean fieldHideSvPercent( 
		FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	@Override
	public boolean fieldHideTcpi( 
		FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	@Override
	public boolean fieldHideVac( 
		FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	@Override
	public boolean fieldHideWork( 
		FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	//	public static Task findTaskByUniqueId(Object idObject, Collection taskList) {
	//		Iterator i = taskList.iterator();
	//		int id = ((Number)idObject).intValue();
	//		Task task;
	//		while (i.hasNext()) {
	//			task = (Task)i.next();
	//			if (task.getUniqueId() == id)
	//				return task;
	//		}
	//		return null;
	//	}
	
	public Task findByUniqueId( 
		long _id )
	{
		Iterator i = getTaskOutlineIterator();
		Task task;

		while (i.hasNext())
		{
			task = (Task)i.next();

			if (task.getUniqueId() == _id)
			{
				return task;
			}
		}

		return null;
	}

	/**
	 * Quick function to find a task by id.  Should probably replaced with hash table
	 * @param _idObject
	 * @param _project
	 * @return
	 */
	public static Task findTaskById( 
		Object _idObject,
		Collection _taskList )
	{
		Iterator i = _taskList.iterator();
		int id = ((Number)_idObject).intValue();
		Task task;

		while (i.hasNext())
		{
			task = (Task)i.next();

			if (task.getId() == id)
			{
				return task;
			}
		}

		return null;
	}

	public Task findTaskByName( 
		String _name )
	{
		Iterator i = getTaskOutlineIterator();
		Task task;

		while (i.hasNext())
		{
			task = (Task)i.next();

			if (_name.equals( task.getName() ))
			{
				return task;
			}
		}

		return null;
	}

	/**
	 * 
	 * @param _source
	 * @param _object 
	 */
	@Override
	public void fireUpdateEvent( 
		final Object _source,
		final Object _object )
	{
		myObjectEventManager.fireUpdateEvent( _source, _object );
	}

	@Override
	public void fireUpdateEvent(
		final Object _source, 
		final Object _object,
		final Field _field ) 
	{
		myObjectEventManager.fireUpdateEvent( _source, _object, _field );
		
		if (_field.isDirtiesWholeDocument() == true)
		{
			myTasks.setAllChildrenDirty( true );
		}
	}

	public void fireBaselineChanged( 
		Object _source,
		Object _object,
		Integer _baselineNumber,
		boolean _save )
	{
		myScheduleEventManager.fireBaselineChanged( _source, null, _baselineNumber, _save );
	}

	public void fireScheduleChanged( 
		Object _source,
		String _type )
	{
		myScheduleEventManager.fire( _source, _type );
	}

	public void fireScheduleChanged( 
		Object _source,
		String _type,
		Object _object )
	{
		myScheduleEventManager.fire( _source, _type, _object );
	}

	@Override
	public double fixedCost( 
		long _start,
		long _end )
	{
		return TimeDistributedDataConsolidator.fixedCost( _start, _end, childrenToRollup() );
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#forEachWorkingInterval(org.apache.commons.collections.Closure, boolean)
	 */
	@Override
	public void forEachWorkingInterval( 
		final Closure<HasStartAndEnd> _visitor,
		final boolean _mergeWorking,
		final WorkCalendar _workCalendar )
	{
		myTasks.forEachWorkingInterval( _visitor, _mergeWorking, _workCalendar );
	}

	public int getAccessControlPolicy()
	{
		return myAccessControlPolicy;
	}

	@Override
	public double getActualCost( 
		FieldContext _fieldContext )
	{
		return getActualFixedCost( _fieldContext ) +
		actualCost( FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	@Override
	public double getActualFixedCost( 
		FieldContext _fieldContext )
	{
		return actualFixedCost( FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	@Override
	public long getActualWork( 
		FieldContext _fieldContext )
	{
		return actualWork( FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	@Override
	public double getAcwp( 
		FieldContext _fieldContext )
	{
		return acwp( FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	@Override
	public double getBac( 
		FieldContext _fieldContext )
	{
		return bac( FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	//TODO other baselines
	@Override
	public double getBaselineCost( 
		int _numBaseline,
		FieldContext _fieldContext )
	{
		if ((_fieldContext != null) 
		 && (_fieldContext.isHideNullValues() == true) 
		 && (myTasks.hasBaseline( _numBaseline ) == false))
		{
			return ClassUtils.NULL_DOUBLE;
		}

		if (Environment.isNoPodServer())
		{
			Field field = Configuration.getFieldFromId( "Field.baseline" + ((_numBaseline == 0)
				? ""
				: ("" + _numBaseline)) + "Cost" );
			Money value = (Money)NodeModelUtil.getValue( field, getSummaryTaskNode(), myTaskModel, _fieldContext );

			if (value == null)
			{
				if ((_fieldContext != null) && _fieldContext.isHideNullValues())
				{
					return ClassUtils.NULL_DOUBLE;
				}

				return 0.0D;
			}

			return value.doubleValue();
		}

		return baselineCost( FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) ); // this is not correct
	}

	@Override
	public long getBaselineDuration( 
		int _numBaseline,
		FieldContext _fieldContext )
	{
		// note that I am using the current calendar no matter what.
		if ((_fieldContext != null) 
		 && (_fieldContext.isHideNullValues() == true) 
		 && (myTasks.hasBaseline( _numBaseline ) == false))
		{
			return ClassUtils.NULL_LONG;
		}

		return getEffectiveWorkCalendar().compare( getBaselineFinish( _numBaseline ), getBaselineStart( _numBaseline ), false );
	}

	@Override
	public long getBaselineFinish( 
		final int _numBaseline )
	{
		return myTasks.getBaselineFinish( _numBaseline );
	}

	@Override
	public long getBaselineStart( 
		int _numBaseline )
	{
		return myTasks.getBaselineStart( _numBaseline );
	}

	public int getBenefit()
	{
		return myBenefit;
	}

	public final long getCurrentDate()
	{
		return myCurrentDate;
	}

	public ImageLink getBudgetStatusIndicator()
	{
		return EarnedValueCalculator.getInstance().getBudgetStatusIndicator( getCpi( null ) );
	}

	public final Task getContainingSubprojectTask()
	{
		return mySubprojectHandler.getContainingSubprojectTask();
	}

	public final long getFinishDate()
	{
		return getEnd();
	}

	public Date getCreationDate()
	{
		return myCreationDate;
	}

	@Override
	public String getDivision()
	{
		return myDivision;
	}

	public final Hyperlink getDocumentFolderUrl()
	{
		return myDocumentFolderUrl;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExpenseType getEffectiveExpenseType()
	{
		return getExpenseType();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExpenseType getExpenseType()
	{
		return myExpenseType;
	}

	@Override
	public final Map getExtraFields()
	{
		if (myExtraFields == null)
		{
			myExtraFields = new HashMap();
		}

		return myExtraFields;
	}

	public String getFileName()
	{
		return myFileName;
	}

	@Override
	public long getFinishOffset()
	{
		return EarnedValueCalculator.getInstance().getFinishOffset( this );
	}

	public String getGroup()
	{
		return myGroup;
	}

	public String getGuessedFileName()
	{
		if (myFileName != null)
		{
			return myFileName;
		}

		String name = getName();

		if (name == null)
		{
			return null;
		}

		return getName() + "." + FileHelper.getFileExtension( myFileType );
	}

	public Date getLastModificationDate()
	{
		return myLastModificationDate;
	}

	/**
	 * @return Returns the myMultipleTransactionManager.
	 */
	public final MultipleTransactionManager getMultipleTransactionManager()
	{
		return myMultipleTransactionManager;
	}

	@Override
	public double getNetPresentValue()
	{
		return myNetPresentValue;
	}

	@Override
	public ObjectEventManager getObjectEventManager()
	{
		return myObjectEventManager;
	}

	@Override
	public int getProjectStatus()
	{
		return myProjectStatus;
	}

	@Override
	public int getProjectType()
	{
		return myProjectType;
	}

	public final Collection getReferringSubprojectTasks()
	{
		return mySubprojectHandler.getReferringSubprojectTasks();
	}

	@Override
	public double getRisk()
	{
		return myRisk;
	}

	public List<Resource> getRootResources()
	{
		List<Node> children = getResourceModel().getChildren( null );

		return NodeList.nodeListToImplList( children );
	}

	public List<Task> getRootTasks()
	{
		Node summary = getvirtualTaskRoot();
		List<Node> children = summary.getChildren();

		return NodeList.nodeListToImplList( children );
	}

	public ImageLink getScheduleStatusIndicator()
	{
		return EarnedValueCalculator.getInstance().getBudgetStatusIndicator( getSpi( null ) );
	}

	@Override
	public final long getStartDate()
	{
		return getStart();
	}

	@Override
	public long getStartOffset()
	{
		return EarnedValueCalculator.getInstance().getStartOffset( this );
	}

	public boolean isCriticalPathJustChanged()
	{
		return ((CriticalPath)getSchedulingAlgorithm()).isCriticalPathJustChanged();
	}

	public final boolean isReadOnly()
	{
		return myReadOnly;
	}

	public final boolean isReadOnlyStartDate( 
		FieldContext _fieldContext )
	{
		return (getSchedulingAlgorithm() == null) || !getSchedulingAlgorithm().isForward();
	}

	/**
	 * Automatically link all siblings at all levels
	 * A condition can be applied. The condition tests the task and sees whether it can be a successor task or not
	 * @param parent - should be null if whole project
	 */
	public void linkAllSiblings( 
		Node _parent,
		Predicate _canBeSuccessorCondition,
		Object _eventSource )
	{
		List<Node> children = getTaskModel().getChildren( _parent );

		if (children == null)
		{
			return;
		}

		try
		{
			DependencyService.getInstance().connect( NodeList.nodeListToImplList( children, NotAssignmentFilter.getInstance() ), 
				_eventSource, _canBeSuccessorCondition );
		}
		catch (InvalidAssociationException e)
		{
			e.printStackTrace();
		}

		for (Node n : children) // recursively do children

		{
			linkAllSiblings( n, _canBeSuccessorCondition, _eventSource );
		}
	}

	@Override
	public boolean renumber( 
		boolean _localOnly )
	{
		boolean r = false;
		long uniqueId = getUniqueId();

		for (Iterator i = getTaskOutlineIterator(); i.hasNext();)
		{
			Task task = (Task)i.next(); //ResourceImpl to have the EnterpriseResource link

			if (task.getProjectId() != uniqueId) // skip if in another project
			{
				continue;
			}

			r |= task.renumber( _localOnly );
		}

		r |= myHasKey.renumber( _localOnly );

		if (!r)
		{
			return false;
		}

		uniqueId = getUniqueId();

		for (Iterator i = getTaskOutlineIterator(); i.hasNext();)
		{
			Task task = (Task)i.next();
			task.setProjectId( uniqueId );
		}

		return true;
	}

	public boolean renumberProjectOnly( 
		long _newUniqueId )
	{
		return myHasKey.renumber( false, _newUniqueId );
	}

	@Override
	public void restoreDetail( 
		Object _source,
		Object _detail,
		boolean _isChild )
	{
		// TODO Auto-generated method stub
	}

	public void setAccessControlPolicy( 
		int _accessControlPolicy )
	{
		myAccessControlPolicy = _accessControlPolicy;
	}

	public void setBenefit( 
		int _benefit )
	{
		myBenefit = _benefit;
	}

	public final void setContainingSubprojectTask( 
		Task _subprojectTask )
	{
		this.mySubprojectHandler.setContainingSubprojectTask( _subprojectTask );
	}

	public void setCreationDate( 
		Date _creationDate )
	{
		myCreationDate = _creationDate;
	}

	public final void setCurrentDate( 
		long _currentDate )
	{
		myCurrentDate = _currentDate;
	}

	/**
	 * @return
	 */
	@Override
	public Date getCreated()
	{
		return myHasKey.getCreated();
	}

	@Override
	public void setDivision( 
		String _division )
	{
		myDivision = _division;
	}

	public final void setDocumentFolderUrl( 
		Hyperlink _documentFolderUrl )
	{
		myDocumentFolderUrl = _documentFolderUrl;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setExpenseType( 
		final ExpenseType _value )
	{
		myExpenseType = _value;
	}

	@Override
	public final void setExtraFields( 
		Map _extraFields )
	{
		myExtraFields = _extraFields;
	}

	//	public String getDefaultExtension(){
	//		return Environment.getStandAlone()?FileHelper.DEFAULT_FILE_EXTENSION:"xml";
	//	}
	public void setFileName( 
		String _fileName )
	{
		myFileName = _fileName;

		if (myFileName != null)
		{
			setFileType( FileHelper.getFileType( myFileName ) );
		}
	}

	public void setGroup( 
		String _group )
	{
		myGroup = _group;
	}

	public void setLastModificationDate( 
		Date _lastModificationDate )
	{
		myLastModificationDate = _lastModificationDate;
	}

	@Override
	public void setNetPresentValue( 
		double _netPresentValue )
	{
		myNetPresentValue = _netPresentValue;
	}

	@Override
	public void setProjectStatus( 
		int _projectStatus )
	{
		myProjectStatus = _projectStatus;
	}

	public final void setReadOnly( 
		boolean _readOnly )
	{
		myReadOnly = _readOnly;
	}

	public final void setReferringSubprojectTasks( 
		Collection _referringSubprojectTasks )
	{
		mySubprojectHandler.setReferringSubprojectTasks( _referringSubprojectTasks );
	}

	public void addExternalTask( 
		final Task _task )
	{
		getExternalTaskManager().add( _task );
		_task.markTaskAsNeedingRecalculation();
	}

	@Override
	public final long getCompletedThrough()
	{
		long start = getStart();

		if (start == 0)
		{
			return 0;
		}

		long actualDuration = DateTime.closestDate( getDuration() * getPercentComplete() );

		return getEffectiveWorkCalendar().add( start, actualDuration, true );
	}

	@Override
	public final boolean isDirty()
	{
		return myIsDirty;
	}

	public final boolean isOpenedAsSubproject()
	{
		return myOpenedAsSubproject;
	}

	@Override
	public final void setCompletedThrough( 
		long _completedThrough )
	{
		// do nothing
	}

	public void setAllDirty()
	{
		myTasks.setAllDirty();
		setDirty( true );
		setGroupDirty( true );
	}

	@Override
	public final void setDirty( 
		boolean _isDirty )
	{
		myIsDirty = _isDirty;

		if (_isDirty == true)
		{
			setGroupDirty( true );
		}
	}

//	@Override
	public void setGroupDirty( 
		final boolean _isGroupDirty )
	{
		boolean old = myIsGroupDirty;
		myIsGroupDirty = _isGroupDirty;

		if (myUndoController != null)
		{
			myUndoController.updateDirty();
		}

		if (old != _isGroupDirty)
		{
//			System.out.println("Project["+hashCode()+"].setGroupDirty("+_isGroupDirty+")");
			fireGroupDirtyChanged( this, old );
		}
	}

	public final void setOpenedAsSubproject( 
		boolean _openedAsSubproject )
	{
		myOpenedAsSubproject = _openedAsSubproject;
	}

	@Override
	public final void setStartDate( 
		long _start )
	{
		_start = getEffectiveWorkCalendar().adjustInsideCalendar( _start, false );
		setStart( _start );
		getSchedulingAlgorithm().setStartConstraint( _start );
	}

	@Override
	public void addEndSentinelDependency( 
		Task _task )
	{
		if (!_task.isInSubproject()) // subprojects have fixed dates, and their children do not depend on master project's sentinels
		{
			mySchedulingAlgorithm.addEndSentinelDependency( _task );
		}
	}

	public void addPastedTask( 
		final Task _task )
	{
		myTasks.initializeId( _task );
		getSchedulingAlgorithm().addObject( _task );
	}

	@Override
	public void addStartSentinelDependency( 
		Task _task )
	{
		if (_task.isInSubproject() == false) // subprojects have fixed dates, and their children do not depend on master project's sentinels
		{
			mySchedulingAlgorithm.addStartSentinelDependency( _task );
		}
	}

	public boolean applyTimesheet( 
		Collection _fieldArray )
	{
		return applyTimesheet( _fieldArray, System.currentTimeMillis() );
	}

	@Override
	public boolean applyTimesheet( 
		Collection _fieldArray,
		long _timesheetUpdateDate )
	{
		return TimesheetHelper.applyTimesheet( getTasks().collection(), _fieldArray, _timesheetUpdateDate );
	}

	@Override
	public void clearDuration()
	{
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.scheduling.Schedule#consumeIntervals(com.projity.functor.IntervalConsumer)
	 */
	@Override
	public void consumeIntervals( 
		IntervalConsumer _consumer )
	{
		_consumer.consumeInterval( new ScheduleInterval( getStart(), getEnd() ) );
	}

	/* (non-Javadoc)
	 * @see
	 */
	@Override
	public WorkspaceSetting createWorkspace( 
		final int _context )
	{
		final Workspace ws = new Workspace();

		if ((Environment.isClientSide() == true) 
		 && (Environment.isTesting() == false))
		{
			myFieldArray = (SpreadSheetFieldArray)Alert.getGraphicManagerMethod( "getCurrentFieldArray" );

			if (myFieldArray != null)
			{
				ws.spreadsheetWorkspace = myFieldArray.createWorkspace( _context );
			}

			if (myPrintSettings != null)
			{
				ws.printSettings = myPrintSettings;
				myPrintSettings.updateWorkspace();
			}

			if (myCalendarOption != null)
			{
				ws.calendarOption = myCalendarOption;
			}
		}

		ws.fieldAliasMap = FieldDictionary.getAliasMap();

		return ws;
	}

	public void dump( 
		Collection _tasks,
		String _indent )
	{
		if (_tasks != null)
		{
			for (Iterator i = _tasks.iterator(); i.hasNext();)
			{
				Node node = (Node)i.next();
				Task task = (Task)node.getValue();
				System.out.println( _indent + task.getWbsParentTask() + "->" + task );
				dump( task.getWbsChildrenNodes(), _indent + "-" );
			}
		}
	}

	@Override
	public boolean equals( 
		Object _obj )
	{
		if (_obj instanceof DataObject)
		{
			if (Environment.isNoPodServer())
			{
				return getUniqueId() == (((DataObject)_obj).getUniqueId());
			}

			return getName().equals( ((DataObject)_obj).getName() );
		}

		return false;
	}

	@Override
	public boolean fieldHideBaseCalendar( 
		FieldContext _fieldContext )
	{
		return false;
	}

	@Override
	public boolean fieldHideTimesheetClosed( 
		FieldContext _fieldContext )
	{
		return !isTimesheetClosed();
	}

	/* (non-Javadoc)
	 * @see com.projity.document.Document#fireMultipleTransaction(int, boolean)
	 */
	@Override
	public int fireMultipleTransaction( 
		int _id,
		boolean _begin )
	{
		return myMultipleTransactionManager.fire( this, _id, _begin );
	}

	public void forObjects( 
		final Set<Long> _taskIds,
		final Set<AssignmentKey> _assignmentIds,
		final Set<DependencyKey> _linkIds,
		final Predicate _taskFilter,
		final AssignmentPredicate _assignmentFilter,
		final Predicate _linkFilter )
	{
		forTasks( 
			new Closure<Task>()
		{
			@Override
			public void execute( 
				final Task _task )
			{
//					NormalTask task = (NormalTask)arg;

				if ((_taskFilter == null) || _taskFilter.evaluate( _task ))
				{
					if (_taskIds != null)
					{
						_taskIds.add( _task.getUniqueId() );
					}

					if ((_assignmentIds != null) 
						|| (_assignmentFilter != null))
					{
						forAssignments( _task,
							new AssignmentClosure()
						{
							@Override
							public void execute( 
								final Assignment _assignment,
								final int _s )
							{
								if (((_assignmentFilter == null) 
									|| (_assignmentFilter.evaluate( _assignment, _s ) == true)) 
									&& (_assignmentIds != null))
								{
									_assignmentIds.add( new AssignmentKey( _assignment.getTask().getUniqueId(),
											_assignment.getResource().getUniqueId(), _s ) );
								}
							}
						} );
					}

					if ((_linkIds != null) 
						|| (_linkFilter != null))
					{
						for (Iterator i = _task.getPredecessorList().iterator(); i.hasNext();)
						{
							Dependency dependency = (Dependency)i.next();

							if (((_linkFilter == null) 
								|| (_linkFilter.evaluate( dependency ) == true)) 
								&& (_linkIds != null))
							{
								_linkIds.add( new DependencyKey(dependency.getPredecessorId(), dependency.getSuccessorId() 
									/*,dependency.getExternalId()*/) );
							}
						}
					}
				}
			}
		} );
	}

	public void forTasks( 
		Closure<Task> _closure )
	{
		for (Iterator<Task> i = getTaskOutlineIterator(); i.hasNext() == true;)
		{
			_closure.execute( i.next() );
		}
	}

	public Set<AssignmentKey> generateInitialAssignmentIdsIncludingExceptions()
	{
		Set<AssignmentKey> all = new HashSet<AssignmentKey>();

		if (myInitialAssignmentIds != null)
		{
			all.addAll( myInitialAssignmentIds );
		}

		forObjects( null, all, null, null,
			new AssignmentPredicate()
		{
			@Override
			public boolean evaluate( 
				Assignment assignment,
				int snapshotId )
			{
				return assignment.isCreatedFromTimesheet();
			}
		}, null );

		return all;
	}

	@Override
	public long getActualDuration()
	{
		if (Environment.isNoPodServer())
		{
			return Duration.millis( getSummaryTask().getActualDuration() );
		}

		long stop = getStop();

		if (stop == 0)
		{
			return 0;
		}

		long s = Environment.isNoPodServer()
			? getEarliestStartingTaskOrStart()
			: getStartDate();

		return getEffectiveWorkCalendar().compare( stop, s, false );
	}

	@Override
	public long getActualFinish()
	{
		return myTasks.getActualFinish();
	}

	@Override
	public long getActualStart()
	{
		return myTasks.getActualStart();
	}

	@Override
	public WorkCalendar getBaseCalendar()
	{
		return getWorkCalendar();
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.TimeDistributedFields#getBaselineWork(int, com.projity.field.FieldContext)
	 */
	@Override
	public long getBaselineWork( 
		final int _numBaseline,
		final FieldContext _fieldContext )
	{
		if ((_fieldContext != null) 
		 && (_fieldContext.isHideNullValues() == true) 
		 && (myTasks.hasBaseline( _numBaseline ) == false))
		{
			return ClassUtils.NULL_LONG;
		}

		if (Environment.isNoPodServer() == true)
		{
			Field field = Configuration.getFieldFromId( "Field.baseline" + ((_numBaseline == 0)
				? ""
				: ("" + _numBaseline)) + "Work" );
			Work value = (Work)NodeModelUtil.getValue( field, getSummaryTaskNode(), myTaskModel, _fieldContext );

			if (value == null)
			{
				if ((_fieldContext != null) && _fieldContext.isHideNullValues())
				{
					return ClassUtils.NULL_LONG;
				}

				return 0L;
			}

			return value.longValue();
		}

		return baselineWork( FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) ); // this is not correct
	}

	@Override
	public double getBcwp( 
		FieldContext _fieldContext )
	{
		return bcwp( FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	@Override
	public double getBcws( 
		FieldContext _fieldContext )
	{
		return bcws( FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	//	public void savePrintSettings() {
	//		if (myPrintSettings!=null){
	//			myPrintSettings = (PrintSettings)myTmpSettings.clone();
	//			myPrintSettings.updateWorkspace();
	//			setGroupDirty(true);
	//		}
	//	}
	public CalendarOption getCalendarOption()
	{
		return myCalendarOption;
	}

	public Calendar getCheckedOutDate()
	{
		return myCheckedOutDate;
	}

	public String getCheckedOutUser()
	{
		return myCheckedOutUser;
	}

	public int getCheckedOutUserId()
	{
		return myCheckedOutUserId;
	}

	@Override
	public double getCost( 
		FieldContext _fieldContext )
	{
		return getFixedCost( _fieldContext ) + cost( FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	@Override
	public double getCpi( 
		FieldContext _fieldContext )
	{
		return EarnedValueCalculator.getInstance().cpi( this, FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	@Override
	public double getCsi( 
		FieldContext _fieldContext )
	{
		return EarnedValueCalculator.getInstance().csi( this, FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	public CurrencyRateTable getCurrencyRateTable()
	{
		return CurrencyRateTable.getInstance();
	}

	@Override
	public double getCv( 
		FieldContext _fieldContext )
	{
		return EarnedValueCalculator.getInstance().cv( this, FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	@Override
	public double getCvPercent( 
		FieldContext _fieldContext )
	{
		return EarnedValueCalculator.getInstance()
									.cvPercent( this, FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

//	/* (non-Javadoc)
//	 * @see com.projity.document.Document#getDefaultCalendar()
//	 */
//	@Override
//	public WorkCalendar getDefaultCalendar()
//	{
//		return getWorkCalendar();
//	}

	@Override
	public long getDependencyStart()
	{
		return 0;
	}

	public TreeMap<DistributionData,DistributionData> getDistributionMap()
	{
		return myDistributionMap;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.task.BelongsToDocument#getDocument()
	 */
	@Override
	public Document getDocument()
	{
		return this;
	}

	/**
	 * @return
	 */
	@Override
	public long getDuration()
	{
		if (Environment.isNoPodServer())
		{
			return Duration.millis( getSummaryTask().getDuration() );

			//			return getEffectiveWorkCalendar().compare(getLatestFinishingTask(),getEarliestStartingTaskOrStart(),false);
		}

		return getEffectiveWorkCalendar().compare( myEnd, myStart, false );
	}

	@Override
	public double getEac( 
		FieldContext _fieldContext )
	{
		return EarnedValueCalculator.getInstance().eac( this, FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	public long getEarliestStartingTaskOrStart()
	{
		if (isOpenedAsSubproject() == true)
		{
			return myTasks.getEarliestStartingTask();
		}

		long early = ((CriticalPath)getSchedulingAlgorithm()).getEarliestStart();

		if (early == 0)
		{
			early = getStart();
			System.out.println( "0 earliest start for project. Forward = " + isForward() + " using proj start " +
				new Date( early ) );
		}

		return early;
	}

	@Override
	public long getEarliestStop()
	{
		return myTasks.getEarliestStop();
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.time.HasCalendar#getEffectiveWorkCalendar()
	 */
	@Override
	public WorkCalendar getEffectiveWorkCalendar()
	{
		return myWorkCalendar;
	}

	@Override
	public long getElapsedDuration()
	{
		return Math.round( getEffectiveWorkCalendar().compare( getEnd(), getStart(), true ) * 
			CalendarOption.getInstance().getFractionOfDayThatIsWorking() );
	}

	@Override
	public long getEnd()
	{
		return myEnd;
	}

	//protected transient long externalId=-1L;
	public long getExternalId()
	{
		return getUniqueId();
	}

	public SpreadSheetFieldArray getFieldArray()
	{
		return myFieldArray;
	}

	public int getFileType()
	{
		return myFileType;
	}

	@Override
	public double getFixedCost( 
		FieldContext _fieldContext )
	{
		return fixedCost( FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	public Set<AssignmentKey> getInitialAssignmentIds()
	{
		return myInitialAssignmentIds;
	}

	public Set<DependencyKey> getInitialLinkIds()
	{
		return myInitialLinkIds;
	}

	public Set<Long> getInitialTaskIds()
	{
		return myInitialTaskIds;
	}

	public HashMap<String,String> getInvestmentMilestones()
	{
		return myInvestmentMilestones;
	}

	public long getLastTimeTimesheetPolled()
	{
		return myLastTimeTimesheetPolled;
	}

	@Override
	public long getLastTimesheetUpdate()
	{
		return TimesheetHelper.getLastTimesheetUpdate( getTasks().collection() );
	}

	public long getLatestFinishingTask()
	{
		if (isOpenedAsSubproject())
		{
			return myTasks.getLatestFinishingTask();
		}

		long late = ((CriticalPath)getSchedulingAlgorithm()).getLatestFinish();

		if ((late == 0) || (late == Long.MAX_VALUE))
		{
			late = getEnd();
			System.out.println( "" + late + " latest finish for project. Forward = " + isForward() + " using proj end " +
				new Date(myEnd) );
		}

		return late;
	}

	public LockStatus getLockStatus()
	{
		if (isNoLongerLocked())
		{
			return LockStatus.NO_LONGER_LOCKED;
		}

		if (isMspAssociated())
		{
			return LockStatus.MSP;
		}

		if (isCheckedOutByOther())
		{
			return LockStatus.LOCKED_BY_OTHER;
		}

		if (isCheckedOutByMe())
		{
			return LockStatus.LOCKED;
		}

		return LockStatus.UNLOCKED;
	}

	public Set<AssignmentKey> getNewAssignmentIds()
	{
		return myNewAssignmentIds;
	}

	public TreeMap<DistributionData,DistributionData> getNewDistributionMap()
	{
		return myNewDistributionMap;
	}

	public Set<DependencyKey> getNewLinkIds()
	{
		return myNewLinkIds;
	}

	public Set<Long> getNewTaskIds()
	{
		return myNewTaskIds;
	}
	
	public NodeModelDataFactory getNodeModelDataFactory()
	{
		return myNodeModelDataFactory;
	}
	
	@Override
	public ObjectSelectionEventManager getObjectSelectionEventManager()
	{
		return myObjectSelectionEventManager;
	}

	@Override
	public double getPercentComplete()
	{
		return myTasks.getPercentComplete();
	}

	@Override
	public double getPercentWorkComplete()
	{
		return getPercentComplete();
	}

	public long getPersistedId()
	{
		return myPersistedId;
	}

	public PrintSettings getPrintSettings( 
		int context )
	{
		return (context == SavableToWorkspace.PERSIST)
		? myPrintSettings
		: myTmpSettings;
	}

	@Override
	public final int getPriority()
	{
		return myPriority;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.scheduling.Schedule#getHasCalendar()
	 */
	public HasCalendar getHasCalendar()
	{
		return this;
	}

	/**
	 * @return
	 */
	@Override
	public long getId()
	{
		return myHasKey.getId();
	}

	/**
	 * @return Returns the manager.
	 */
	@Override
	public String getManager()
	{
		return myManager;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public String getName()
	{
		return myHasKey.getName();
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public String getName( 
		FieldContext _context )
	{
		return myHasKey.getName( _context );
	}

	/**
	 * @return Returns the notes.
	 */
	@Override
	public String getNotes()
	{
		return myNotes;
	}

	public ProjectListener[] getProjectListeners()
	{
		return (ProjectListener[])myProjectListenerList.getListeners( ProjectListener.class );
	}

	public EventListener[] getProjectListeners( 
		final Class _listenerType )
	{
		return myProjectListenerList.getListeners( _listenerType );
	}

	public ProjectPermission getProjectPermission()
	{
		if (myProjectPermission == null)
		{
			myProjectPermission = new ProjectPermission();
		}

		return myProjectPermission;
	}

	//	public void setLocked(boolean locked) {
	//		this.locked = locked;
	//	}
	public String getPublishInfo()
	{
		return "Locked=" + isLocked() + " published date = " + ((getPublishedDate() == null)
		? null
		: getPublishedDate().getTime()) + " by " + getPublishedUser() + " checked out date " +
		((getCheckedOutDate() == null)
		? null
		: getCheckedOutDate().getTime()) + " by " + getCheckedOutUser();
	}

	public String getPublishInfoHtml()
	{
		//System.out.println(Messages.getString("Text.LockedTooltip"));
		String result = "<html>Published date:" + ((getPublishedDate() == null)
			? null
			: getPublishedDate().getTime()) + "<br>by " + getPublishedUser();

		if (getCheckedOutUser() != null)
		{
			result += ("<br>Checked out date " + ((getCheckedOutDate() == null)
			? null
			: getCheckedOutDate().getTime()) + "<br>by " + getCheckedOutUser());
		}

		result += "</html>";

		return result;
	}

	public Calendar getPublishedDate()
	{
		return myPublishedDate;
	}

	public String getPublishedUser()
	{
		return myPublishedUser;
	}

	public long getReferringSubprojectTaskDependencyDate()
	{
		return mySubprojectHandler.getReferringSubprojectTaskDependencyDate();
	}

	@Override
	public double getRemainingCost( 
		FieldContext _fieldContext )
	{
		return getCost( _fieldContext ) - getActualCost( _fieldContext );
	}

	@Override
	public long getRemainingDuration()
	{
		if (Environment.isNoPodServer() == true)
		{
			return Duration.millis( getSummaryTask().getRemainingDuration() );
		}

		long stop = getStop();

		if (stop == 0)
		{
			stop = getStartDate();
		}

		return getEffectiveWorkCalendar().compare( getFinishDate(), stop, false );
	}

	@Override
	public long getRemainingWork( 
		FieldContext _fieldContext )
	{
		return remainingWork( FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

//	public static Closure forAllTasks(Closure visitor, Predicate filter) {
//		return new CollectionVisitor(visitor,filter) {
//			protected Collection getCollection(Object arg0) {
//				return ((Project)arg0).getTasks();
//			}
//		};
//	}

//	public int testCount() {
//		ReflectionPredicate taskPredicate;
//		try {
//			taskPredicate = ReflectionPredicate.getInstance(NormalTask.class.getMethod("isVirtual",null));
//			Closure t = Project.forAllTasks(NormalTask.forAllAssignments(PrintString.INSTANCE),taskPredicate);
//			t.execute(this);
//		} catch (SecurityException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (NoSuchMethodException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return 0;
//	}

	/**
	 * @return Returns the resource pool.
	 */
	public ResourcePool getResourcePool()
	{
		return myResourcePool;
	}

	@Override
	public long getResume()
	{
		return 0;
	}

//	public List getRootNodes( 
//		List _tasks )
//	{
//		List roots = new LinkedList();
//
//		for (Iterator i = _tasks.iterator(); i.hasNext();)
//		{
//			Task task = (Task)i.next();
//
//			if (task.getWbsParentTask() == null)
//			{
//				roots.add( myTaskOutlines.getDefaultOutline().search( task ) );
//			}
//		}
//
//		return roots;
//	}

	/** Calculate the row height
	 * 
	 * @param _baseLines
	 * @return 
	 */
	public int getRowHeight( 
		SortedSet _baseLines )
	{
		for (Iterator i = getTaskOutlineIterator(); i.hasNext() == true;)
		{
			final Task task = (Task)i.next();
			final int current = Snapshottable.CURRENT.intValue();

			for (int s = 0; s < Settings.numGanttBaselines(); s++)
			{
				if (s == current)
				{
					continue;
				}

				final TaskSnapshot snapshot = (TaskSnapshot)task.getSnapshot( Integer.valueOf( s ) );

				if (snapshot != null)
				{
					_baseLines.add( Integer.valueOf( s ) );
				}
			}
		}

		final int num = (_baseLines.size() == 0)
			? 0
			: (((Integer)_baseLines.last()).intValue() + 1);
		final int rowHeight = GraphicConfiguration.getInstance().getRowHeight() +
			(num * GraphicConfiguration.getInstance().getBaselineHeight());

		return rowHeight;
	}

	public SchedulingAlgorithm getSchedulingAlgorithm()
	{
		return mySchedulingAlgorithm;
	}

	public String getSchedulingMethod()
	{
		return mySchedulingAlgorithm.getName();
	}

	@Override
	public double getSpi( 
		FieldContext _fieldContext )
	{
		return EarnedValueCalculator.getInstance().spi( this, FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	@Override
	public long getStop()
	{
		return myTasks.getStop();
	}

	public SubprojectHandler getSubprojectHandler()
	{
		return mySubprojectHandler;
	}

	public String getSubprojectOf()
	{
		return mySubprojectHandler.getSubprojectOf();
	}

	@Override
	public double getSv( 
		FieldContext _fieldContext )
	{
		return EarnedValueCalculator.getInstance().sv( this, FieldContext.start( _fieldContext ), 
			FieldContext.end( _fieldContext ) );
	}

	@Override
	public double getSvPercent( 
		FieldContext _fieldContext )
	{
		return EarnedValueCalculator.getInstance().svPercent( this, FieldContext.start( _fieldContext ), 
			FieldContext.end( _fieldContext ) );
	}

	public Iterator<Task> getTaskOutlineIterator()
	{
		return new TaskIterator();
	}

	public Node getTaskOutlineRoot()
	{
		if (isOpenedAsSubproject() == true)
		{ // when doing subprojects, we must treat the suproject parent as the root node

			if (getTasks().size() > 0)
			{
				Task task = (Task)getTasks().iterator().next(); // get any task in the project

				return task.getEnclosingSubprojectNode(); // this will fetch the enclosing subproject task which will be the hierarchy root
			}
		}

		return null;
	}

	@Override
	public double getTcpi( 
		FieldContext _fieldContext )
	{
		return EarnedValueCalculator.getInstance().tcpi( this, FieldContext.start( _fieldContext ),
			FieldContext.end( _fieldContext ) );
	}

	@Override
	public int getTimesheetStatus()
	{
		return TimesheetHelper.getTimesheetStatus( getTasks().collection() );
	}

	@Override
	public String getTimesheetStatusName()
	{
		return TimesheetHelper.getTimesheetStatusName( getTimesheetStatus() );
	}

	public String getTitle()
	{
		return getName() + ((myFileName == null)
		? ""
		: (" - " + myFileName));
	}

	public PrintSettings getTmpSettings()
	{
		return myTmpSettings;
	}

//	protected void initUndo(){
//		myUndoController=new DataFactoryUndoController(this);
//	}
	
	/**
	 * @return
	 */
	@Override
	public long getUniqueId()
	{
		return myHasKey.getUniqueId();
	}

	@Override
	public double getVac( 
		FieldContext _fieldContext )
	{
		return EarnedValueCalculator.getInstance().vac( this, FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	public float getVersion()
	{
		return myVersion;
	}

	public boolean getWasImported()
	{
		return myWasImported;
	}

	@Override
	public long getWork( 
		FieldContext _fieldContext )
	{
		return work( FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	public void handleExternalTasks( 
		Project project,
		boolean opening,
		boolean saving )
	{
		getExternalTaskManager().handleExternalTasks( project, opening, saving );
		project.getExternalTaskManager().handleExternalTasks( this, opening, saving );
	}

	public static Predicate instanceofPredicate()
	{
		return new Predicate()
		{
			@Override
			public boolean evaluate( 
				Object _arg0 )
			{
				return _arg0 instanceof Project;
			}
		};
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.calendar.HasCalendar#invalidateCalendar()
	 */
	@Override
	public Document invalidateCalendar()
	{
		markAllTasksAsNeedingRecalculation( false );

		return this;
	}

	public boolean isActualsProtected()
	{
		return isDatesPinned() || myActualsProtected;
	}

	public boolean isCheckedOutByMe()
	{
		if (!Environment.isNoPodServer()) // if not mariner
		{
			return true;
		}

		return getCheckedOutUserId() == Environment.getUser().getUniqueId();
	}

	public boolean isCheckedOutByOther()
	{
		return ((getCheckedOutUserId() != 0) && (getCheckedOutUserId() != Environment.getUser().getUniqueId()));
	}

	@Override
	public boolean isComplete()
	{
		return getPercentComplete() == 1.0D;
	}

	public boolean isCompletionFromTimesheet()
	{
		return myCompletionFromTimesheet;
	}

	public boolean isDatesPinned()
	{
		return isMspAssociated();
	}

//	@Override
	public boolean isDontSummarizeFields()
	{
		return isDatesPinned();
	}

	public boolean isForceNonIncremental()
	{
		return myForceNonIncremental;
	}

	public boolean isForceNonIncrementalDistributions()
	{
		return myForceNonIncrementalDistributions;
	}

	/**
	 * @return Returns the forward.
	 */
	@Override
	public boolean isForward()
	{
		return myForward;
	}

	public boolean isFromTemplate()
	{
		return getUniqueId() != getPersistedId();
	}

//	@Override
//	public boolean isGroupDirty()
//	{
//		return myIsGroupDirty;
//	}

//	public Task findByUniqueId(long id) {
//		return findTaskByUniqueId(new Long(id), getTasks());
//	}
	
	public boolean isInitialized()
	{
		return myInitialized;
	}

	@Override
	public boolean isJustModified()
	{
		return true; //Not used
	}

	//	public boolean isNew() {
	//		return myHasKey.isNew();
	//	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#isLabor()
	 */
	@Override
	public boolean isLabor()
	{
		return true;
	}

	@Override
	public boolean isLocal()
	{
		return myHasKey.isLocal();
	}

	public boolean isLockable()
	{
		return !(myTemporaryLocal || isLocal());
	}

	//	transient boolean locked = false;
	public boolean isLocked()
	{
		return isCheckedOutByMe();

		//		return locked;
	}

	public boolean isMaster()
	{
		return myMaster;
	}

	public boolean isMspAssociated()
	{
		//	System.out.println("myMspAssociated=true  for testing!!!!");
		//	myMspAssociated = true;
		return myMspAssociated;
	}

	public boolean isNoLongerLocked()
	{
		return noLongerLocked;
	}

	@Override
	public boolean isPendingTimesheetUpdate()
	{
		return TimesheetHelper.isPendingTimesheetUpdate( getTasks().collection() );
	}

	@Override
	public boolean isReadOnlyActualDuration( 
		FieldContext _fieldContext )
	{
		return true;
	}

	@Override
	public boolean isReadOnlyActualFinish( 
		FieldContext _fieldContext )
	{
		return true;
	}

	@Override
	public boolean isReadOnlyActualStart( 
		FieldContext _fieldContext )
	{
		return true;
	}

	@Override
	public boolean isReadOnlyActualWork( 
		FieldContext _fieldContext )
	{
		return true;
	}

	@Override
	public boolean isReadOnlyComplete( 
		FieldContext _context )
	{
		return isReadOnlyPercentComplete( _context );
	}

	@Override
	public boolean isReadOnlyCompletedThrough( 
		FieldContext _context )
	{
		return true;
	}

	@Override
	public boolean isReadOnlyCost( 
		FieldContext _fieldContext )
	{
		return true;
	}

	public boolean isReadOnlyFinishDate( 
		FieldContext _fieldContext )
	{
		return (getSchedulingAlgorithm() == null) || getSchedulingAlgorithm().isForward();
	}

	@Override
	public boolean isReadOnlyFixedCost( 
		FieldContext _fieldContext )
	{
		return true;
	}

	//	public TaskCompletionOptions getTaskCompletionOption() {
	//		return taskCompletionOption;
	//	}
	//
	//	public void setTaskCompletionOption(TaskCompletionOptions taskCompletionOption) {
	//		this.taskCompletionOption = taskCompletionOption;
	//	}
	@Override
	public boolean isReadOnlyPercentComplete( 
		FieldContext _context )
	{
		return true;
	}

	@Override
	public boolean isReadOnlyRemainingWork( 
		FieldContext _fieldContext )
	{
		return true;
	}

	@Override
	public boolean isReadOnlyTimesheetClosed( 
		FieldContext _fieldContext )
	{
		return true;
	}

	@Override
	public boolean isReadOnlyWork( 
		FieldContext _fieldContext )
	{
		return true;
	}

	public boolean isSavable()
	{
		return Environment.isOpenProj() || (!isLocal() && !isReadOnly());
	}

	public boolean isTemporaryLocal()
	{
		return myTemporaryLocal;
	}

	public boolean isTestImportOk()
	{
		boolean result = true;
		Iterator i = getTasks().iterator();

		while (i.hasNext())
		{
			if (!((Task)i.next()).isTestImportOk())
			{
				result = false; // not returning anymore for tracing purposes
			}
		}

		return result;
	}

	public boolean isTimesheetAssociated()
	{
		return myActualsProtected;
	}

	@Override
	public boolean isTimesheetClosed()
	{
		return myTimesheetClosed;
	}

	@Override
	public boolean isTimesheetComplete()
	{
		return myTasks.isTimesheetComplete();
	}

	public void markAllTasksAsNeedingRecalculation( 
		boolean _invalidateSchedules )
	{
		myTasks.setCalculationStateCount( getCalculationStateCount() + 1, _invalidateSchedules );
		getSchedulingAlgorithm().initEarliestAndLatest();
	}

	public boolean move( 
		Node _parent,
		List<Node> _nodes,
		int _position )
	{
		if ((_nodes == null) || (_nodes.isEmpty() == true))
		{
			return false;
		}

		NodeModel model = getTaskOutline();
		model.move( _parent, _nodes, _position, NodeModel.NORMAL );

		return true;
	}

	public void moveInterval( 
		Object _eventSource,
		long _start,
		long _end,
		ScheduleInterval _oldInterval )
	{
		if (_start != _oldInterval.getStart())
		{
			setStart( _start ); // allow for changing start of subproject.  need to add  test for actuals
		}
	}

	@Override
	public void moveInterval( 
		Object _eventSource,
		long _start,
		long _end,
		ScheduleInterval _oldInterval,
		boolean _isChild )
	{
	}

	@Override
	public void moveRemainingToDate( 
		long _date )
	{
	}

	public boolean needsSaving()
	{
		return (isSavable() && myIsGroupDirty /*||getResourcePool().myIsGroupDirty()*/);
	}

	public Task newTaskInstance()
	{
		return myTasks.newTaskInstance( this );
	}
	
	public Task newTaskInstance( 
		final boolean _userCreated )
	{
		final Task newOne = myTasks.newTaskInstance( this );

		if (_userCreated == true)
		{
			myObjectEventManager.fireCreateEvent( this, newOne );
		}

		return newOne;
	}
	
	//	public double getBaselineCost(FieldContext fieldContext) {
	//		return baselineCost(FieldContext.start(fieldContext),FieldContext.end(fieldContext));
	//	}
	//	public long getBaselineWork(FieldContext fieldContext) {
	//		return baselineWork(FieldContext.start(fieldContext),FieldContext.end(fieldContext));
	//	}
	public void nodeRemoved( 
		HierarchyEvent _e )
	{
	}

	@Override
	public void nodesChanged( 
		HierarchyEvent _e )
	{
		//	    System.out.println("Node changed ...");
		//	    if ("NO_PROJECT_UPDATE".equals(e.getFlag())) return;
		Node node;

		//	    System.out.println("Node changed ...");
		//	    if ("NO_PROJECT_UPDATE".equals(e.getFlag())) return;
		Node previousParentNode;

		//	    System.out.println("Node changed ...");
		//	    if ("NO_PROJECT_UPDATE".equals(e.getFlag())) return;
		Node newParentNode;
		Task task;
		Task previousParentTask;
		Task newParentTask;
		int count = _e.getNodes().length;

		if (count == 0)
		{
			return;
		}

		for (int i = 0; i < count; i++)
		{
			node = (Node)_e.getNodes()[ i ];

			if (!(node.getValue() instanceof Task))
			{
				continue;
			}

			task = (Task)node.getValue();

			//TODO verify that this is ok when pasting for bug 426
			//			task.setProject((Project) getSchedulingAlgorithm().getMasterDocument());
			//			task.setOwningProject(this);
			//moved to validateObject
			previousParentTask = task.getWbsParentTask();
			previousParentNode = myTaskOutlines.getDefaultOutline().search( previousParentTask );

			// refresh the previous parent's children
			if (previousParentTask != null)
			{
				previousParentTask.markAllDependentTasksAsNeedingRecalculation( true ); // flag this and dependent tasks as dirty
				previousParentTask.setWbsChildrenNodes( myTaskOutlines.getDefaultOutline().getHierarchy()
																	.getChildren( previousParentNode ) );
			}

			// refresh the new parent's children
			NodeHierarchy hierarchy = myTaskOutlines.getDefaultOutline().getHierarchy();
			newParentNode = hierarchy.getParent( node );
			newParentTask = null;

			if (newParentNode != hierarchy.getRoot())
			{
				newParentTask = (Task)newParentNode.getValue();
				newParentTask.setWbsChildrenNodes( myTaskOutlines.getDefaultOutline().getHierarchy().getChildren( newParentNode ) );
				newParentTask.restrictToValidConstraintType();

				//		newParentTask.setParentDuration(); //hk
				newParentTask.markAllDependentTasksAsNeedingRecalculation( true ); // flag this and dependent tasks as dirty
			}

			//refresh this node to point to new parent
			task.setWbsParent( newParentTask );

			final Task _newParentTask = newParentTask;
			final Object eventSource = _e.getSource();

			// recursively remove all dependencies between new parent and any children, grandchildren, etc.
			myTaskOutlines.getDefaultOutline().getHierarchy().visitAll( newParentNode,
				new Closure<Node>()
			{
				@Override
				public void execute( 
					final Node _node )
				{
//					Node node = (Node)_arg;

					if (_node.getValue() instanceof Task == false)
					{
						return;
					}

					Task task = (Task)_node.getValue();
					DependencyService.getInstance().removeAnyDependencies( task, _newParentTask, eventSource );
				}
			} );
		}

		if (_e.isVoid() == false)
		{ 
			// if the event was not the promotion of a void node
			// The critical path needs resetting because its internal list must be rebuilt as it depends on hierarchy
			//			myObjectEventManager.fireUpdateEvent(e.getSource(),this);
			_e.consume();

			// will cause critical path to reset and to run and send schedule events
			updateScheduling( _e.getSource(), this, ObjectEvent.CREATE ); 
		}
	}

	@Override
	public void nodesInserted( 
		HierarchyEvent _e )
	{
		nodesChanged( _e );
	}

	@Override
	public void nodesRemoved( 
		HierarchyEvent _e )
	{
	}

	public boolean paste( 
		Node _parent,
		List<Node> _nodes,
		int _position )
	{
		if ((_nodes == null) || (_nodes.isEmpty() == true))
		{
			return false;
		}

		NodeModel model = getTaskOutline();

		if (_parent == null)
		{
			return false;
		}

		model.paste( _parent, _nodes, _position, NodeModel.NORMAL );

		return true;
	}

	public void postDeserialization()
	{
		myLastDeserialized = this;
		initSubprojectHandler(); //this is created transiently
		setSchedulingAlgorithm( new CriticalPath( this ) ); // Critical path needs myObjectEventManager

		int count = Settings.numHierarchies();

		for (int i = 0; i < count; i++)
		{
			NodeModel model = myTaskOutlines.getOutline( i );

			if (model == null)
			{
				continue;
			}

			if (model instanceof AssignmentNodeModel)
			{
				AssignmentNodeModel aModel = (AssignmentNodeModel)model;
				aModel.setContainsLeftObjects( true );
				aModel.setDocument( this );
			}

			model.setUndoController( myUndoController );
		}

		//setEnd(getStart());
		//setInitialized(true);
		//initialize();
		initializeDefaultOutline();
		setInitialized( true );
		setGroupDirty( false );

		if (myWorkspace != null)
		{
			restoreWorkspace( myWorkspace, SavableToWorkspace.PERSIST );
		}

		if (myCalendarOption == null)
		{
			myCalendarOption = CalendarOption.getDefaultInstance();
		}
	}

	public void recalculate()
	{
		markAllTasksAsNeedingRecalculation( true );
		mySchedulingAlgorithm.reset();
		mySchedulingAlgorithm.calculate( true );
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#actualWork(long, long)
	 */
	@Override
	public long remainingWork( 
		long _start,
		long _end )
	{
		return TimeDistributedDataConsolidator.remainingWork( _start, _end, childrenToRollup(), true );
	}

//	public boolean removeNode(Node node){
//		NodeModel model=getTaskOutline();
//		model.remove(node,NodeModel.EVENT);
//		return true;
//	}
	
	public boolean remove( 
		Node _toRemove )
	{
		NodeModel model = getTaskOutline();

		if (_toRemove == null)
		{
			return false;
		}

		model.remove( _toRemove, NodeModel.NORMAL );

		return true;
	}

	@Override
	public boolean removeEndSentinelDependency( 
		Task _task )
	{
		// subprojects have fixed dates, and their children do not depend on master project's sentinels
		if (_task.isInSubproject() == false) 
		{
			return mySchedulingAlgorithm.removeEndSentinelDependency( _task );
		}

		return false;
	}

	/* (non-Javadoc)
	 * @see com.projity.grouping.core.DataFactory#remove(java.lang.Object)
	 */
	public void removeExternal( 
		final Task _toRemove )
	{
//		removeStartSentinelDependency(toRemove);
//		removeEndSentinelDependency(toRemove);
		myTasks.remove( _toRemove );
		
		// update all node models except the one passed in
		myTaskOutlines.removeFromAll( _toRemove, null ); 
		myObjectEventManager.fireDeleteEvent( this, _toRemove );
	}

	/**
	 * @param listener
	 */
	@Override
	public void removeObjectListener( 
		final ObjectEvent.Listener _listener )
	{
		myObjectEventManager.removeListener( _listener );
	}

	public void removeProjectListener( 
		ProjectListener _listener )
	{
		myProjectListenerList.remove( ProjectListener.class, _listener );
	}

	/**
	 * @param listener
	 */
	public void removeScheduleListener( 
		ScheduleEventListener _listener )
	{
		myScheduleEventManager.removeListener( _listener );
	}

	@Override
	public boolean removeStartSentinelDependency( 
		Task _task )
	{
		if (!_task.isInSubproject()) // subprojects have fixed dates, and their children do not depend on master project's sentinels
		{
			return mySchedulingAlgorithm.removeStartSentinelDependency( _task );
		}

		return false;
	}

	public void resetRoles( 
		boolean _publicRoles )
	{
		try
		{
			Class.forName( Messages.getMetaString( "ProjectRoleManager" ) )
				 .getDeclaredMethod( "resetRoles", new Class[]
				{
					Project.class,
					Boolean.class
				} ).invoke( null, new Object[]
				{
					this,
					_publicRoles
				} );
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println( "ProjectRoleManager not valid in meta.properties" );
			System.exit( -1 );
		}
	}

	@Override
	public void restoreWorkspace( 
		WorkspaceSetting _w,
		int _context )
	{
		Workspace ws = (Workspace)_w;

		if (ws.spreadsheetWorkspace != null)
		{
			myFieldArray = SpreadSheetFieldArray.restore( ws.spreadsheetWorkspace, getName(), _context );
		}

		if (ws.printSettings != null)
		{
			myPrintSettings = ws.printSettings;

			if (myPrintSettings != null)
			{
				myPrintSettings.init();
				myTmpSettings = (PrintSettings)myPrintSettings.clone();
			}
		}

		if (ws.fieldAliasMap != null)
		{
			FieldDictionary.setAliasMap( ws.fieldAliasMap );
		}

		if (ws.calendarOption != null)
		{
			myCalendarOption = ws.calendarOption;
			CalendarOption.setInstance( myCalendarOption );
		}

		//	Alert.setGraphicManagerMethod("setCurrentFieldArray",f);
	}

	@Override
	public void setActualDuration( 
		long _actualDuration )
	{
	}

	@Override
	public void setActualFinish( 
		long _actualFinish )
	{
	}

	@Override
	public void setActualStart( 
		long _actualStart )
	{
	}

	@Override
	public void setActualWork( 
		long _work,
		FieldContext _fieldContext )
	{
		//do nothing
	}

	public void setActualsProtected( 
		boolean _actualsProtected )
	{
		myActualsProtected = _actualsProtected;
	}

//	@Override
//	public void setAllChildrenDirty( 
//		boolean _dirty )
//	{ 
//		myTasks.setAllChildrenDirty( _dirty );
//	}

	public void setAllNodesInSubproject( 
		boolean _b )
	{
		// TODO Auto-generated method stub
	}

	/**
	 * When opening a project or just after saving, need to put all tasks back to their undirty state.
	 * This means the task is considered as being untouched since the last save.
	 *
	 */
	public void setAllTasksAsUnchangedFromPersisted( 
		boolean _justSaved )
	{
		getTaskOutline().getHierarchy().visitAll( 
			new Closure<Node<Task>>()
		{
			@Override
			public void execute( 
				Node<Task> _node )
			{
				if (_node.getValue() instanceof Task == true)
				{
					Task task = (Task)_node.getValue();
					task.setDirty( false );
					task.setLastSavedStart( task.getStart() );
					task.setLastSavedFinish( task.getEnd() );

					Iterator j = task.getAssignments().iterator();

					while (j.hasNext() == true)
					{
						((Assignment)j.next()).setDirty( false );
					}

					j = task.getDependencyList( true ).iterator();

					while (j.hasNext() == true)
					{
						((Dependency)j.next()).setDirty( false );
					}

					Node parent = (Node)_node.getParent();

					if ((parent == null) || parent.isRoot())
					{
						task.setLastSavedParentId( -1L );
					}
					else
					{
						task.setLastSavedParentId( ((Task)parent.getValue()).getUniqueId() );
					}

					task.setLastSavedPosistion( parent.getIndex( _node ) );
				}
			}

			int id = 1;
		} );

		if (_justSaved == false)
		{
			myTasks.markRepairedTasksAsDirty();
		}
	}	

	public void setAllTasksInSubproject( 
		boolean _b,
		Project _masterProject )
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void setBaseCalendar( 
		WorkCalendar _baseCalendar )
		throws CircularDependencyException
	{
		setWorkCalendar( _baseCalendar );
	}

	public void setBoundsAfterReadProject()
	{
		getSchedulingAlgorithm().setEarliestAndLatest( getStart(), getEnd() );
		fireScheduleChanged( this, ScheduleEvent.SCHEDULE );
	}

	public void setCalendarOption( 
		CalendarOption _calendarOption )
	{
		myCalendarOption = _calendarOption;
		setGroupDirty( true );
	}

	public void setCheckedOutDate( 
		Calendar _checkedOutDate )
	{
		myCheckedOutDate = _checkedOutDate;
	}

	public void setCheckedOutUser( 
		String _checkedOutUser )
	{
		myCheckedOutUser = _checkedOutUser;
	}

	public void setCheckedOutUserId( 
		int _checkedOutUserId )
	{
		myCheckedOutUserId = _checkedOutUserId;
		System.out.println( "Project checked out user id = " + _checkedOutUserId + " locked by self " + this.isLocked() );
	}

	@Override
	public void setComplete( 
		boolean _complete )
	{
		ScheduleUtil.setComplete( this, _complete );
	}

	public void setCompletionFromTimesheet( 
		boolean _completionFromTimesheet )
	{
		myCompletionFromTimesheet = _completionFromTimesheet;
	}

	@Override
	public void setCost( 
		double _cost,
		FieldContext _fieldContext )
	{
	}

	public void setCurrencyRateTable( 
		CurrencyRateTable _table )
	{
		CurrencyRateTable.setInstance( _table ); // it is global
	}

	@Override
	public void setDependencyStart( 
		long _dependencyStart )
	{
	}

	public void setDistributionMap( 
		TreeMap<DistributionData,DistributionData> _distributionMap )
	{
		myDistributionMap = _distributionMap;
	}

	@Override
	public void setDuration( 
		long _duration )
	{
	}

	/**
	 * @param date
	 */
	@Override
	public void setEndConstraint( 
		long _date )
	{
		mySchedulingAlgorithm.setEndConstraint( _date );
	}

	public void setEarliestAndLatestDatesFromSchedule()
	{
		myTasks.setEarliestAndLatestDatesFromSchedule( myStart, myEnd );
	}

	public void setFieldArray( 
		SpreadSheetFieldArray _fieldArray )
	{
		myFieldArray = _fieldArray;
	}

	public void setFileType( 
		int _fileType )
	{
		myFileType = _fileType;
	}

	@Override
	public void setFinishDate( 
		long _finish )
	{
		_finish = getEffectiveWorkCalendar().adjustInsideCalendar( _finish, true );
		setEnd( _finish );
		getSchedulingAlgorithm().setEndConstraint( _finish );
	}

	@Override
	public void setFixedCost( 
		double _fixedCost,
		FieldContext _fieldContext )
	{
	}

	public void setForceNonIncremental( 
		boolean _forceNonIncremental )
	{
		myForceNonIncremental = _forceNonIncremental;
	}

	public void setForceNonIncrementalDistributions( 
		boolean _forceNonIncrementalDistributions )
	{
		myForceNonIncrementalDistributions = _forceNonIncrementalDistributions;
	}

	/**
	 * @param forward The forward to set.
	 */
	@Override
	public void setForward( 
		boolean _forward )
	{
		if (_forward == myForward)
		{
			return;
		}

		myForward = _forward;
		myTasks.setForward( myForward );
		
		markAllTasksAsNeedingRecalculation( false );
		mySchedulingAlgorithm.setForward( _forward );
		mySchedulingAlgorithm.reset();
		mySchedulingAlgorithm.calculate( true );
	}

	public void setInitialAssignmentIds( 
		Set<AssignmentKey> _initialAssignmentIds )
	{
		myInitialAssignmentIds = _initialAssignmentIds;
	}

	public void setInitialIds()
	{
		myInitialTaskIds = new HashSet<Long>();
		myInitialLinkIds = new HashSet<DependencyKey>();
		myInitialAssignmentIds = new HashSet<AssignmentKey>();
		forObjects( myInitialTaskIds, myInitialAssignmentIds, myInitialLinkIds, null, null, null );
	}

	public void setInitialLinkIds( 
		Set<DependencyKey> _initialLinkIds )
	{
		myInitialLinkIds = _initialLinkIds;
	}

	public void setInitialTaskIds( 
		Set<Long> _initialTaskIds )
	{
		myInitialTaskIds = _initialTaskIds;
	}

	/**
	 * @param _initialized The initialized to set.
	 */
	public void setInitialized( 
		boolean _initialized )
	{
		myInitialized = _initialized;
	}

	public void setLastTimeTimesheetPolled( 
		long _lastTimeTimesheetPolled )
	{
		myLastTimeTimesheetPolled = _lastTimeTimesheetPolled;
	}

	@Override
	public void setLocal( 
		boolean _local )
	{
		myHasKey.setLocal( _local );
	}

	public void setMaster( 
		boolean _master )
	{
		myMaster = _master;
	}

	public void setMspAssociated( 
		boolean _mspAssociated )
	{
		myMspAssociated = _mspAssociated;
	}

	public void setNewAssignmentIds( 
		Set<AssignmentKey> _newAssignmentIds )
	{
		myNewAssignmentIds = _newAssignmentIds;
	}

	public void setNewDistributionMap( 
		TreeMap<DistributionData,DistributionData> _newDistributionMap )
	{
		myNewDistributionMap = _newDistributionMap;
	}

	public void setNewIds()
	{
		myNewTaskIds = new HashSet<Long>();
		myNewLinkIds = new HashSet<DependencyKey>();
		myNewAssignmentIds = new HashSet<AssignmentKey>();
		forObjects( myNewTaskIds, myNewAssignmentIds, myNewLinkIds, null, null, null );
	}

	public void setNewLinkIds( 
		Set<DependencyKey> _newLinkIds )
	{
		myNewLinkIds = _newLinkIds;
	}

	public void setNewTaskIds( 
		Set<Long> _newTaskIds )
	{
		myNewTaskIds = _newTaskIds;
	}

	public void setNoLongerLocked( 
		boolean _noLongerLocked )
	{
		this.noLongerLocked = _noLongerLocked;
	}

	@Override
	public void setPercentComplete( 
		double _percentComplete )
	{
	}

	public void setPersistedId( 
		long _persistedId )
	{
		myPersistedId = _persistedId;
	}

	public void setPrintSettings( 
		PrintSettings _printSettings )
	{
		myPrintSettings = _printSettings; //==null?null:(PrintSettings)myPrintSettings.clone();
		setGroupDirty( true );
	}

	@Override
	public final void setPriority( 
		int _priority )
	{
		myPriority = _priority;
	}

	public void connectTask( 
		final Task _task )
	{
		// tasks were being added twice.
		if ((isOpenedAsSubproject() == false) 
		 || (myTasks.contains( _task ) == false)) 
		{
			myTasks.add( _task );
		}

		if (_task.getOwningProject() == null)
		{
			_task.setOwningProject( this );
		}

		if (_task.getProjectId() == 0)
		{
			_task.setProjectId( getUniqueId() );
		}

		Project masterProject = (Project)_task.getMasterDocument();

		if (masterProject == this)
		{
			_task.setProject( this );

			// initially, the task has no predecessors or successors, so signify that it is both a starting point and an ending point
			if (_task.getSuccessorList().size() == 0)
			{
				addEndSentinelDependency( _task );
			}

			if (_task.getPredecessorList().size() == 0)
			{
				addStartSentinelDependency( _task );
			}
		}
		else
		{
			masterProject.getTasks().add( _task );
			_task.setProject( masterProject );
		}
	}

	public Node createLocalTaskNode( 
		final Node _parentNode )
	{
		final Task task = Model.getInstance().createTask( this );
		Node childNode = NodeFactory.getInstance().createNode( task ); // get a node for this task
		connectTask( task );
		addToDefaultOutline( _parentNode, childNode );
		getSchedulingAlgorithm().addObject( task );

		return childNode;
	}

	public static Project createProject( 
		ResourcePool _resourcePool,
		DataFactoryUndoController _undo )
	{
		Project project = new Project( _resourcePool, _undo );
		project.initializeProject();

		//undo not properly initialized in new Project(myResourcePool,undo)
//		project.setUndoController( _undo ); 

		return project;
	}

	public Node createScriptedNode( 
		boolean _rootLevel,
		boolean _undo )
	{
		final org.openproj.domain.task.Task task = myTasks.newStandaloneNormalTaskInstance( this, getWorkCalendar() );
		task.markTaskAsNeedingRecalculation();

		return connectScriptedTask( task, _rootLevel, _undo );
	}

	public Task createScriptedTask( 
		boolean _rootLevel,
		boolean _undo )
	{
		final org.openproj.domain.task.Task task = myTasks.newStandaloneNormalTaskInstance( this, getWorkCalendar() );
		task.markTaskAsNeedingRecalculation();
		connectScriptedTask( task, _rootLevel, _undo );

		return task;
	}

	public void disconnect()
	{
		disconnectOutlines();
		removeObjectListener( getSchedulingAlgorithm() );
		mySchedulingAlgorithm = null; // help with gc
	}

	public void disconnectOutlines()
	{
		int count = Settings.numHierarchies();

		for (int i = 0; i < count; i++)
		{
			NodeModel model = myTaskOutlines.getOutline( i );

			if (model instanceof AssignmentNodeModel)
			{
				AssignmentNodeModel aModel = (AssignmentNodeModel)model;
				aModel.setDocument( null ); //remove ObjectListener
			}
		}

		disconnectDefaultOutline();
	}

	public static Project getDummy()
	{
		if (myDummy == null)
		{
			myDummy = new Project(true);
		}

		return myDummy;
	}

	public void dispose()
	{
		System.out.println( "disposing project " + this );
	}

	public Object getResourceCache()
	{
		return myResourceCache;
	}

	public NodeModel getResourceModel()
	{
		if (myResourceModel == null)
		{
			myResourceModel = NodeModelFactory.createResourceModel( this );
		}

		return myResourceModel;
	}

	@Override
	public long getStart()
	{
		return myStart;
	}

	public long getStartConstraint()
	{
		long result;
		long constraint = getReferringSubprojectTaskDependencyDate();

		if (constraint > getStart())
		{
			result = getEffectiveWorkCalendar().adjustInsideCalendar( constraint, false );
		}
		else
		{
			result = getStart();
		}

		return result;
	}

	/**
	 * @return Returns the status date.
	 */
	@Override
	public long getStatusDate()
	{
		if (myStartDate == 0) // if date not set, then use last instant of this day incude all of today
		{
			return myWorkCalendar.adjustInsideCalendar( DateTime.midnightTomorrow() - 1, true );
		}

		return myStartDate;
	}

	public Task getSummaryTask()
	{
		return (Task)getSummaryTaskNode().getValue();
	}

	public Node getSummaryTaskNode()
	{
		if (myRootNode != null)
		{
			return myRootNode;
		}

		return getTaskModel().getSummaryNode();
	}

	public Object getTaskCache()
	{
		return myTaskCache;
	}

	public NodeModel getTaskModel()
	{
		if (myTaskModel == null)
		{
			myTaskModel = NodeModelFactory.createTaskModel( this );
		}

		return myTaskModel;
	}

	public NodeModel getTaskOutline()
	{
		return myTaskOutlines.getOutline();
	}

	public NodeModel getTaskOutline( 
		int outlineNumber )
	{
		return myTaskOutlines.getOutline( outlineNumber );
	}

	public OutlineCollection getTaskOutlines()
	{
		return myTaskOutlines;
	}

	/**
	 * @return Returns the tasks.
	 */
	public TaskBattery getTasks()
	{
		return myTasks;
	}

	/* (non-Javadoc)
	 * @see com.projity.grouping.core.Node#getType()
	 */
	public Class getType()
		throws NodeException
	{
		return getClass();
	}

	/**
	 * @return
	 */
	@Override
	public WorkCalendar getWorkCalendar()
	{
		return myWorkCalendar;
	}

	public Node getvirtualTaskRoot()
	{
		return getTaskModel().getVirtualroot();
	}

	public void initialize( 
		final boolean _subproject,
		boolean _updateDistribution )
	{
		myInitialized = true;
		myTasks.repairTasks();

		if (_subproject == false)
		{
			mySchedulingAlgorithm.initialize( this );
		}

		if (getStart() == 0L)
		{
			System.out.println( "no start so using earliest" );
			SwingUtilities.invokeLater( new Runnable()
			{
				@Override
				public void run()
				{
					recalculate();
					setStart( getEarliestStartingTaskOrStart() );
				}
			} );
		}

		initializeDefaultOutline();

		if (TimesheetOption.getInstance().isAutomaticallyIntegrateTimecardData())
		{
			applyTimesheet( TimesheetOption.getInstance().getTimesheetFieldArray() );
		}

		setAllTasksAsUnchangedFromPersisted( false );

		System.out.println( "setting group dirty to false because no assignments coming in and the creation of dummy ones is marking project dirty" );
		setGroupDirty( false ); //TODO only for testing

		if (_updateDistribution == true)
		{
			updateDistributionMap();
		}
	}

	public void initializeOutlines()
	{
		int count = Settings.numHierarchies();

		for (int i = 0; i < count; i++)
		{
			NodeModel model = myTaskOutlines.getOutline( i );

			if (model == null)
			{
				continue;
			}

			if (model instanceof AssignmentNodeModel)
			{
				AssignmentNodeModel aModel = (AssignmentNodeModel)model;
				aModel.setContainsLeftObjects( true );
				aModel.setDocument( this );
			}

			model.setUndoController( myUndoController );
		}

		initializeDefaultOutline();
	}

	public void initializeProject()
	{
		setSchedulingAlgorithm( new CriticalPath(this) );

		/**TODO fix calendar handling  should be created by factory*/
		initializeOutlines
		/**TODO fix calendar handling  should be created by factory*/
		();
	}

	public boolean isSummaryTaskEnabled()
	{
		return mySummaryTaskEnabled; //getTaskModel().isSummaryNodeEnabled();
	}

	/* (non-Javadoc)
	 * @see com.projity.grouping.core.Node#isVirtual()
	 */
	public boolean isVirtual()
	{
		return false;
	}

	//	public Task cloneTask(Task from) {
	//		//TODO this does not copy fields correctly
	//		NormalTask newOne = (NormalTask) from.clone();
	//		add(newOne);
	//		initializeId(newOne);
	//		newOne.setWbsParent(from.getWbsParentTask());
	//		Node node = NodeFactory.getInstance().createNode(newOne);
	//		getTaskModel().addBefore(getTaskModel().search(from),node,NodeModel.NORMAL);
	//		myObjectEventManager.fireCreateEvent(this,newOne);
	//		return newOne;
	//
	//	}

	public void restoreSnapshot( 
		Object _snapshotId,
		boolean _entireProject,
		List _selection,
		Collection _snapshotDetails )
	{
		Iterator i;

		if (_entireProject)
		{
			i = getTaskOutlineIterator();
		}
		else
		{
			if (_selection == null)
			{
				return;
			}

			i = _selection.iterator();
		}

		Iterator j = _snapshotDetails.iterator();

		while (i.hasNext())
		{
			Task task = (Task)i.next();
			task.restoreSnapshot( _snapshotId, j.next() );
		}

		fireBaselineChanged( this, null, (Integer)_snapshotId, true );
	}

	public void saveCurrentToSnapshot( 
		Object _snapshotId,
		boolean _entireProject,
		List _selection,
		boolean _undo )
	{
		if (_entireProject)
		{
			forTasks( new SnapshotContainer.SaveCurrentToSnapshotClosure(_snapshotId) );
		}
		else
		{
			CollectionUtils.forAllDo( _selection, new SnapshotContainer.SaveCurrentToSnapshotClosure( _snapshotId ) );
		}

		fireBaselineChanged( this, null, (Integer)_snapshotId, true );

		if (_undo == true)
		{
			final UndoableEditSupport undoableEditSupport = myNodeModelDataFactory.getUndoController().getEditSupport();

			if (undoableEditSupport != null)
			{
				undoableEditSupport.postEdit( new SaveSnapshotEdit(this, _snapshotId, _entireProject, _selection) );
			}
		}
	}

	//	public void setNew(boolean isNew) {
	//		myHasKey.setNew(isNew);
	//	}
	@Override
	public void setCreated( 
		Date _created )
	{
		myHasKey.setCreated( _created );
	}

	/**
	 * @param _end
	 */
	@Override
	public void setEnd( 
		long _end )
	{
		myEnd = _end;

		//		setEndConstraint(myEnd);
	}

	/**
	 * @param id
	 */
	@Override
	public void setId( 
		long _id )
	{
		myHasKey.setId( _id );
	}

	public void setLocalParent( 
		Task _child,
		Task _parent )
	{
		Node childNode = getTaskModel().search( _child );
		Node parentNode = (_parent == null)
			? null
			: getTaskModel().search( _parent );
		setLocalParent( childNode, parentNode );
	}

	public void setLocalParent( 
		Node _childNode,
		Node _parentNode )
	{
		if (_childNode.getParent() == _parentNode)
		{
			return;
		}

		if ((_parentNode == null) && Environment.isAddSummaryTask())
		{
			_parentNode = getvirtualTaskRoot();
		}

		ArrayList temp = new ArrayList();
		temp.add( _childNode );
		getTaskModel().move( _parentNode, temp, -1, NodeModel.NORMAL );
		setDefaultRelationship( _parentNode, _childNode );
	}

	/**
	 * @param manager The manager to set.
	 */
	@Override
	public void setManager( 
		String _manager )
	{
		myManager = _manager;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void setName( 
		String _name )
	{
		if ((_name == null) || (_name.length() == 0))
		{
			return;
		}

		String oldName = getName();
		myHasKey.setName( _name );

		if (((oldName == null) && (_name != null)) || (!oldName.equals( _name )))
		{
			fireNameChanged( this, oldName );
		}

		if (getWorkCalendar() == null)
		{
			System.out.println( "error work calendar is null on project" );
		}
	}

	/**
	 * @param _notes The notes to set.
	 */
	@Override
	public void setNotes( 
		final String _notes )
	{
		myNotes = _notes;
	}

	public void setProjectPermission( 
		final ProjectPermission _projectPermission )
	{
		myProjectPermission = _projectPermission;
	}

	public void setProjectType( 
		final int _projectType )
	{
		myProjectType = _projectType;
	}

	public void setPublishedDate( 
		final Calendar _publishedDate )
	{
		//		System.out.println("=========== set publish date " + (myPublishedDate == null ? null : myPublishedDate.getTime()));
		myPublishedDate = _publishedDate;
	}

	public void setPublishedUser( 
		final String _publishedUser )
	{
		myPublishedUser = _publishedUser;
	}

	//TODO to avoid risks of breaking obfuscation, Project will implement all of Schedule for now
	@Override
	public void setRemainingDuration( 
		final long _remainingDuration )
	{
	}

	@Override
	public void setRemainingWork( 
		final long _work,
		final FieldContext _fieldContext )
	{
		//do nothing
	}

	public void setResourceCache( 
		final Object _resourceCache )
	{
		myResourceCache = _resourceCache;
	}

	public void setResourcePool( 
		final ResourcePool _resourcePool )
	{
		myResourcePool = _resourcePool;
	}

	@Override
	public void setResume( 
		final long _resume )
	{
	}

	public void setRisk( 
		final double _risk )
	{
		myRisk = _risk;
	}

	public void setSchedulingAlgorithm( 
		final SchedulingAlgorithm _schedulingAlgorithm )
	{
		if (mySchedulingAlgorithm != null)
		{
			removeObjectListener( mySchedulingAlgorithm );
			getMultipleTransactionManager().removeListener( mySchedulingAlgorithm );
		}

		mySchedulingAlgorithm = _schedulingAlgorithm;
		addObjectListener( _schedulingAlgorithm );
		getMultipleTransactionManager().addListener( _schedulingAlgorithm );
	}

	/**
	 * @param _start
	 */
	@Override
	public void setStart( 
		final long _start )
	{
		//		System.out.println("setting project " + this + " start " + new Date( _start ) + " previous " + new Date( myStart ) );
		//if (this.getName().equals("xxx"))
		//	System.out.println("bah");
		myStart = _start;
	}

	/**
	 * @param date
	 */
	@Override
	public void setStartConstraint( 
		final long _date )
	{
		//		System.out.println("setStartConstraint " + new Date(date));
		mySchedulingAlgorithm.setStartConstraint( _date );
	}

	/**
	 * @param myStartDate The status date to set.
	 */
	public void setStatusDate( 
		long _statusDate )
	{
		_statusDate = DateTime.midnightNextDay( _statusDate ) - 1; // last instant of today
		_statusDate = myWorkCalendar.adjustInsideCalendar( _statusDate, true );
		myStartDate = _statusDate;
	}

	@Override
	public void setStop( 
		long _stop )
	{
	}

	public void setSummaryTaskEnabled( 
		boolean _summaryTaskEnabled )
	{
		mySummaryTaskEnabled = _summaryTaskEnabled;
	}

	public void setTaskCache( 
		Object _taskCache )
	{
		this.myTaskCache = _taskCache;
	}

	public void setTemporaryLocal( 
		boolean _temporaryLocal )
	{
		myTemporaryLocal = _temporaryLocal;
	}

	@Override
	public void setTimesheetClosed( 
		boolean _timesheetClosed )
	{ // this means something different for project than for task
		myTimesheetClosed = _timesheetClosed;
	}

	@Override
	public void setTimesheetComplete( 
		boolean _timesheetComplete )
	{
		myTasks.setTimesheetComplete( _timesheetComplete );
	}

	public void setTmpSettings( 
		PrintSettings _tmpSettings )
	{
		myTmpSettings = _tmpSettings;
	}

	/**
	 * @param id
	 */
	@Override
	public void setUniqueId( 
		long _id )
	{
		myHasKey.setUniqueId( _id );
	}

	public void setVersion( 
		float _version )
	{
		myVersion = _version;
	}

	/* (non-Javadoc)
	 * @see com.projity.grouping.core.Node#setVirtual(boolean)
	 */
	public void setVirtual( 
		boolean _virtual )
	{
	}

	public void setWasImported( 
		boolean _value )
	{
		myWasImported = _value;
	}

	// these fields are not modifiable
	@Override
	public void setWork( 
		long _work,
		FieldContext _fieldContext )
	{
		//do nothing
	}

	/**
	 * @param _workCalendar
	 */
	@Override
	public void setWorkCalendar( 
		final WorkCalendar _workCalendar )
	{
		if (myWorkCalendar != null)
		{
			((WorkingCalendar)myWorkCalendar).removeObjectUsing( this );
		}

		myWorkCalendar = _workCalendar;
		((WorkingCalendar)myWorkCalendar).addObjectUsing( this );
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.scheduling.Schedule#split(java.lang.Object, long, long)
	 */
	@Override
	public void split( 
		Object _eventSource,
		long _from,
		long _to )
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void structureChanged( 
		HierarchyEvent _e )
	{
	}

	@Override
	public String toString()
	{
		return getName();
	}

	public void updateDistributionMap()
	{
		long t = System.currentTimeMillis();
		List dist = (new DistributionConverter()).createDistributionData( this, false );

		if (dist == null)
		{
			return;
		}

		TreeMap<DistributionData, DistributionData> distMap = new TreeMap<DistributionData, DistributionData>( 
			new DistributionComparator() );
		setDistributionMap( distMap );

		long projectId = getUniqueId();

		for (Iterator i = dist.iterator(); i.hasNext();)
		{
			DistributionData d = (DistributionData)i.next();

			if (d.getProjectId() == projectId)
			{
				distMap.put( d, d );
			}
		}

		System.out.println( "DistributionMap: " + dist.size() + " elements, updated in " + (System.currentTimeMillis() - t) +
			" ms" );
	}

	public void updateScheduling( 
		Object _source,
		Object _newlyCreated,
		int _type )
	{
		final ObjectEvent event = ObjectEventManager.getInstance( _source, _newlyCreated, _type, null );
		getSchedulingAlgorithm().objectChanged( event );
		ObjectEventManager.recycle( event );
	}

	public void updateScheduling( 
		Object _source,
		Object _newlyCreated,
		int _type,
		Field _field )
	{
		ObjectEvent event = ObjectEventManager.getInstance( _source, _newlyCreated, _type, null );
		event.setField( _field );
		getSchedulingAlgorithm().objectChanged( event );
		ObjectEventManager.recycle( event );
	}

	public void validateNewDistributionMap()
	{
		if (myDistributionMap != null)
		{
			myDistributionMap.clear();
		}

		setDistributionMap( getNewDistributionMap() );
		setNewDistributionMap( null );
		setForceNonIncrementalDistributions( false );
	}

	public void validateNewTaskAndAssignments()
	{
		if ((myNewTaskIds != null) && (myNewTaskIds.size() > 0))
		{
			if (myInitialTaskIds == null)
			{
				myInitialTaskIds = new HashSet<Long>();
			}
			else
			{
				myInitialTaskIds.clear();
			}

			myInitialTaskIds.addAll( myNewTaskIds );
			myNewTaskIds = null;
		}

		if ((myNewLinkIds != null) && (myNewLinkIds.size() > 0))
		{
			if (myInitialLinkIds == null)
			{
				myInitialLinkIds = new HashSet<DependencyKey>();
			}
			else
			{
				myInitialLinkIds.clear();
			}

			myInitialLinkIds.addAll( myNewLinkIds );
			myNewLinkIds = null;
		}

		if ((myNewAssignmentIds != null) && (myNewAssignmentIds.size() > 0))
		{
			if (myInitialAssignmentIds == null)
			{
				myInitialAssignmentIds = new HashSet<AssignmentKey>();
			}
			else
			{
				myInitialAssignmentIds.clear();
			}

			myInitialAssignmentIds.addAll( myNewAssignmentIds );
			myNewAssignmentIds = null;
		}
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#work(long, long)
	 */
	@Override
	public long work( 
		final long _start,
		final long _end )
	{
		return TimeDistributedDataConsolidator.work( _start, _end, childrenToRollup(), true );
	}

	protected void fireGroupDirtyChanged( 
		final Object _source,
		final boolean _oldName )
	{
		Object[] listeners = myProjectListenerList.getListenerList();
		ProjectEvent e = null;

		for (int i = 0; i < listeners.length; i += 2)
		{
			if (listeners[ i ] == ProjectListener.class)
			{
				if (e == null)
				{
					e = new ProjectEvent( _source, ProjectEvent.GROUP_DIRTY_CHANGED, this, Boolean.valueOf( _oldName ) );
				}

				((ProjectListener)listeners[ i + 1 ]).groupDirtyChanged( e );
			}
		}
	}

	protected void fireNameChanged( 
		Object _source,
		String _oldName )
	{
		Object[] listeners = myProjectListenerList.getListenerList();
		ProjectEvent e = null;

		for (int i = 0; i < listeners.length; i += 2)
		{
			if (listeners[ i ] == ProjectListener.class)
			{
				if (e == null)
				{
					e = new ProjectEvent( _source, ProjectEvent.NAME_CHANGED, this, _oldName );
				}

				((ProjectListener)listeners[ i + 1 ]).nameChanged( e );
			}
		}
	}

	public int getCalculationStateCount()
	{
		if (mySchedulingAlgorithm == null)
		{
			return 0;
		}

		return mySchedulingAlgorithm.getCalculationStateCount();
	}

	public int getDefaultConstraintType()
	{
		if (isForward() == true)
		{
			return ConstraintType.ASAP;
		}
		else
		{
			return ConstraintType.ALAP;
		}
	}

	boolean isInRange( 
		long _start,
		long _finish )
	{
		long s = getStart();

		return ((_finish > s) && (_start < getEnd()));
	}

	private Node connectScriptedTask( 
		final Task _task,
		final boolean _rootLevel,
		final boolean _undo )
	{
		Node child = NodeFactory.getInstance().createNode( _task );
		NodeModel model = getTaskModel();
		Node parent = (Node)model.getRoot();
		List children = model.getChildren( parent );
		boolean foundSummary = false;

		if ((children != null) && !_rootLevel && Environment.isAddSummaryTask())
		{
			for (Iterator i = children.iterator(); i.hasNext();)
			{
				Node n = (Node)i.next();

				if (!(n.getValue() instanceof Task))
				{
					continue;
				}

				Task t = (Task)n.getValue();

				if (t.isRoot())
				{
					foundSummary = true;
					parent = n;

					break;
				}
			}
		}

		if (foundSummary)
		{
			children = parent.getChildren();
		}

		int count = (children == null)
			? 0
			: children.size(); //getTaskModel().getChildCount(parent);
		ArrayList l = new ArrayList();
		l.add( child );

		int pos = -1;
		paste( parent, l, pos );

		if (_undo)
		{
			((DefaultNodeModel)model).postEdit( new NodeCreationEdit(model, parent, l, pos) );
		}

		if (_rootLevel)
		{
			myRootNode = child;
		}

		return child;
	}

	private void disconnectDefaultOutline()
	{
		myTaskOutlines.getDefaultOutline().getHierarchy().removeHierarchyListener( this );
	}

	//	public void setExternalId(long externalId) {
	//		this.externalId = externalId;
	//	}
	private Task findFromInvestmentMilestone( 
		final String _id )
	{
		for (Iterator<Task> itor = myTasks.iterator(); itor.hasNext() == true;)
		{
			final Task task = (Task)itor.next();

			if ((task.getInvestmentMilestone() == null)
				? _id == null
				: task.getInvestmentMilestone().equals( _id ) == true)
			{
				return task;
			}
		}

		return null;
	}

	private ExternalTaskManager getExternalTaskManager()
	{
		if (myExternalTaskManager == null)
		{
			myExternalTaskManager = new ExternalTaskManager();
		}

		return myExternalTaskManager;
	}
	
	public final boolean hasCurrencyRateTable()
	{
		return CurrencyRateTable.isActive();
	}

	private void initSubprojectHandler()
	{
		try
		{
			mySubprojectHandler = (SubprojectHandler)Class.forName( Messages.getMetaString( "SubprojectHandler" ) )
														.getConstructor( new Class[]
					{
						Project.class
					} ).newInstance( this );
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println( "SubprojectHandler not valid in meta.properties" );
			System.exit( -1 );
		}
	}

	private void initializeDefaultOutline()
	{
		myTaskOutlines.getDefaultOutline().getHierarchy().addHierarchyListener( this );
	}

	private boolean isFieldHidden( 
		FieldContext _fieldContext )
	{
		return (_fieldContext != null) && !isInRange( _fieldContext.getStart(), _fieldContext.getEnd() );
	}

	private void readObject( 
		final ObjectInputStream _stream )
		throws IOException, 
			   ClassNotFoundException
	{
		final ObjectInputStream.GetField fields = _stream.readFields();

		try
		{
			// get the version using old attribute name
			float version = fields.get( "version", 0f );
			
			if (version == 1.2f)
			{
				readVersion12( fields );
			}
		}
		catch (IllegalArgumentException e)
		{
			try
			{
				// get the version using the new attribute name
				float version = fields.get( "myVersion", 0f );
				
				if (version == 2.0f)
				{
					readVersion20( fields );
				}
			}
			catch (IllegalArgumentException e2)
			{
				log.severe( "field not found: " + e2 );
			}
		}
		
		//_stream.defaultReadObject();
		
		myHasKey = HasKeyImpl.deserialize( _stream, this );

		//initUndo();
		myTasks = new TaskBattery();
		myObjectEventManager = new ObjectEventManager();
		myObjectSelectionEventManager = new ObjectSelectionEventManager();
		myScheduleEventManager = new ScheduleEventManager();
		myMultipleTransactionManager = new MultipleTransactionManager();
		myProjectListenerList = new EventListenerList();
		myNodeModelDataFactory = new MyNodeModelDataFactory();
		myTaskOutlines = new OutlineCollectionImpl( Settings.numHierarchies(), myNodeModelDataFactory );
		myBarClosureInstance = new BarClosure();
	}

	private void readVersion12( 
		final ObjectInputStream.GetField _fields )
		throws IOException, 
			   ClassNotFoundException
	{	
		try
		{
			myBenefit = _fields.get( "benefit", 0 );
			myCurrentDate = _fields.get( "currentDate", 0L );
			myDuration = _fields.get( "duration", 0L );
			myEnd = _fields.get( "end", 0L );
			myExtraFields = (Map)_fields.get( "extraFields", null );
			myForward = _fields.get( "forward", true );
			myManager = (String)_fields.get( "manager", "" );
			myMaster = _fields.get( "master", false );
			myNetPresentValue = _fields.get( "netPresentValue", 0.0D );
			myNotes = (String)_fields.get( "notes", "" );
			myPriority = _fields.get( "priority", 500 );
			myRisk = _fields.get( "risk", 0.0D );
			myStart = _fields.get( "start", 0L );
			myStartDate = _fields.get( "statusDate", 0L );
			myVersion = _fields.get( "version", 0f );
			myWorkCalendar = (WorkCalendar)_fields.get( "workCalendar", null );
			myWorkspace = (Workspace)_fields.get( "workspace", null );
		}
		catch (IllegalArgumentException e)
		{
			log.severe( "field not found: " + e );
		}
	}	

	private void readVersion20( 
		final ObjectInputStream.GetField _fields )
		throws IOException, 
			   ClassNotFoundException
	{	
		try
		{
			myBenefit = _fields.get( "myBenefit", 0 );
			myCurrentDate = _fields.get( "myCurrentDate", 0L );
			myDuration = _fields.get( "myDuration", 0L );
			myEnd = _fields.get( "myEnd", 0L );
			myExtraFields = (Map)_fields.get( "myExtraFields", null );
			myForward = _fields.get( "myForward", true );
			myManager = (String)_fields.get( "myManager", "" );
			myMaster = _fields.get( "myMaster", false );
			myNetPresentValue = _fields.get( "myNetPresentValue", 0.0D );
			myNotes = (String)_fields.get( "myNotes", "" );
			myPriority = _fields.get( "myPriority", 500 );
			myRisk = _fields.get( "myRisk", 0.0D );
			myStart = _fields.get( "myStart", 0L );
			myStartDate = _fields.get( "myStartDate", 0L );
			myVersion = _fields.get( "myVersion", 0f );
			myWorkCalendar = (WorkCalendar)_fields.get( "myWorkCalendar", null );
			myWorkspace = (Workspace)_fields.get( "myWorkspace", null );
		}
		catch (IllegalArgumentException e)
		{
			log.severe( "field not found: " + e );
		}
	}	

	private void setDefaultRelationship( 
		Node _parentNode,
		Node _childNode )
	{
		Task childTask = (Task)_childNode.getValue();

		if (_parentNode == null)
		{
			childTask.setWbsParent( null );
		}
		else
		{
			Task parentTask = (Task)_parentNode.getValue();
			childTask.setWbsParent( parentTask );

			if (parentTask != null)
			{
				parentTask.setWbsChildrenNodes( myTaskOutlines.getDefaultOutline().getHierarchy().getChildren( _parentNode ) );
			}
		}
	}

	private void writeObject( 
		ObjectOutputStream _s )
		throws IOException
	{
		myWorkspace = (Workspace)createWorkspace( SavableToWorkspace.PERSIST );
		_s.defaultWriteObject();
		myHasKey.serialize( _s );
	}

	@Override
	public long getEarliestStartingTask()
	{
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	public static interface AssignmentClosure
	{
		public void execute( Assignment _assignment, int _snapshotId );
	}

	public static interface AssignmentPredicate
	{
		public boolean evaluate( Assignment _assignment, int _snapshotId );
	}

	public class Workspace
		implements WorkspaceSetting
	{
		private static final long serialVersionUID = 6909144693873463556L;
		CalendarOption calendarOption;
		HashMap fieldAliasMap;
		PrintSettings printSettings;
		WorkspaceSetting spreadsheetWorkspace;
	}

	private class TaskIterator
		implements Iterator<Task>
	{
		TaskIterator()
		{
			myIterator = getTaskOutline().iterator( getTaskOutlineRoot() );
			nextElement();
		}

		@Override
		public boolean hasNext()
		{
			return myNext != null;
		}

		@Override
		public Task next()
		{
			final Task n = myNext;
			nextElement();

			return n;
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}

		private Task nextElement()
		{
			Node node = null;

			while ((myIterator.hasNext() == true)
				&& !((node = (Node)myIterator.next()).getValue() instanceof Task))
			{
				;
			}

			if ((node != null) && node.getValue() instanceof Task)
			{
				myNext = (Task)node.getValue();
			}
			else
			{
				myNext = null;
			}

			return myNext;
		}

		private Iterator<Task> myIterator;
		private Task myNext = null;
	}
	
	public enum LockStatus
	{
		LOCKED, 
		LOCKED_BY_OTHER, 
		MSP, 
		NONE, 
		NO_LONGER_LOCKED, 
		UNLOCKED;
	}
	
	public enum TaskCompletionOptions
	{
		PERCENT_COMPLETE, 
		PM, 
		TIMESHEET;
	}

	private class MyNodeModelDataFactory 
		implements NodeModelDataFactory
	{
		@Override
		public NodeModel createNodeModel()
		{
			// Projects contain assignments, so create the assignment node model
			return new AssignmentNodeModel( this );
		}

		@Override
		public NodeModel createNodeModel( 
			final Collection _collection )
		{
			final NodeModel model = new AssignmentNodeModel( this );
			model.addImplCollection( null, _collection, NodeModel.SILENT );

			return model;
		}

		@Override
		public DataFactoryUndoController getUndoController()
		{
			return myUndoController;
		}

		@Override
		public void initOutline( 
			final NodeModel _nodeModel )
		{
		}

//		@Override
//		public boolean isGroupDirty()
//		{
//			return myIsGroupDirty;
//		}

		@Override
		public void rollbackUnvalidated( 
			final NodeModel _nodeModel,
			final Object _object )
		{
		}

		@Override
		public void setGroupDirty( 
			final boolean _isGroupDirty )
		{
			Project.this.setGroupDirty( _isGroupDirty );
		}

		@Override
		public void setUndoController( 
			DataFactoryUndoController _undoController )
		{
			myUndoController = _undoController;

			if (_undoController != null)
			{
				_undoController.setDataFactory( this );
			}
		}

		@Override
		public void addUnvalidatedObject( 
			final Object _object,
			final NodeModel _nodeModel,
			final Object _parent )
		{
			if (_object instanceof Task == false)
			{
				return; // avoids VoidNodes
			}

//			final NormalTask task = (NormalTask)_object;
			final Task task = (Task)_object;

			//task.getCurrentSchedule().setStart(getWorkCalendar().adjustInsideCalendar(task.getCurrentSchedule().getStart(),false));
			task.setWbsParent( (Task)_parent );
			task.setInSubproject( task.liesInSubproject() );
		}

		@Override
		public Object createUnvalidatedObject( 
			final NodeModel _nodeModel,
			final Object _parent )
		{
//			final NormalTask task = myTasks.newStandaloneNormalTaskInstance( Project.this, getWorkCalendar() );
			final Task task = myTasks.newStandaloneNormalTaskInstance( Project.this, getWorkCalendar() );
			task.setWbsParent( (Task)_parent );

			return task;
		}

		@Override
		public NodeModelDataFactory getFactoryToUseForChildOfParent( 
			final Object _impl )
		{
			if ((_impl == null) || !(_impl instanceof Task))
			{
				return this;
			}

			return ((Task)_impl).getEnclosingProject().getNodeModelDataFactory();
		}

		@Override
		public void remove( 
			final Object _toRemove,
			final NodeModel _nodeModel,
			final boolean _deep,
			final boolean _undo,
			final boolean _cleanDependencies )
		{
			Object eventSource = _nodeModel;

			if (_toRemove instanceof Task == false)
			{
				return; // avoid VoidNodes
			}

			Task task = (Task)_toRemove;
			Project owningProject = task.getOwningProject();

			if (owningProject != Project.this)
			{
				owningProject.myTaskOutlines.removeFromAll( _toRemove, null );
				owningProject.myTasks.remove( task );
			}

			task.cleanUp( /*null*/ eventSource, _deep, _undo, _cleanDependencies ); //lc
			myTasks.remove( task );
			myTaskOutlines.removeFromAll( task, _nodeModel ); // update all node models except the one passed in

			if (task.isSubproject() == true)
			{ // remove subproject from portfolio so it won't be saved - fixes bug with it being saved empty

				Project sub = ((SubProj)task).getSubproject();
				ProjectFactory.getInstance().removeProject( sub, false, false, true );
			}

			myObjectEventManager.fireDeleteEvent( eventSource, task );
		}

		@Override
		public void validateObject( 
			final Object _newlyCreated,
			final NodeModel _nodeModel,
			final Object _eventSource,
			final Object _hierarchyInfo,
			final boolean _isNew )
		{
			if (_newlyCreated instanceof Task == false)
			{
				return; // avoids VoidNodes
			}

			Task newTask = (Task)_newlyCreated;
			newTask.setProject( (Project)getSchedulingAlgorithm().getMasterDocument() );
			newTask.setOwningProject( Project.this );

			// put task in project list
			connectTask( newTask ); 

			// update all node models except the one passed in
			myTaskOutlines.addToAll( _newlyCreated, _nodeModel ); 

			Task parentTask = newTask.getWbsParentTask();
			Node parentNode = (parentTask == null)
				? null
				: _nodeModel.search( newTask.getWbsParentTask() );
			Node childNode = _nodeModel.search( newTask );
			setDefaultRelationship( parentNode, childNode );
			newTask.markTaskAsNeedingRecalculation();
			updateScheduling( this, _newlyCreated, ObjectEvent.CREATE );

			//myObjectEventManager.fireCreateEvent(eventSource,newlyCreated,hierarchyInfo);
		}
		
	}
	
	/*
	        public void clearDuration() {
	                schedule.clearDuration();
	        }
	        public long getDurationActive() {
	                return schedule.getDurationActive();
	        }
	        public long getDurationSpan() {
	                return schedule.getDurationSpan();
	        }
	        public double getPercentComplete() {
	                return schedule.getPercentComplete();
	        }
	        public void setDurationActive(long durationActive) {
	                schedule.setDurationActive(durationActive);
	        }
	        public void setDurationSpan(long durationSpan) {
	                schedule.setDurationSpan(durationSpan);
	        }
	        public void setPercentComplete(double percentComplete) {
	                schedule.setPercentComplete(percentComplete);
	        }
	        public long getDependencyStart() {
	                return schedule.getDependencyStart();
	        }
	        public void setDependencyStart(long dependencyStart) {
	                schedule.setDependencyStart(dependencyStart);
	        }
	*/

	public static final float CURRENT_VERSION = 1.2f;
	static final long serialVersionUID = 17283790404932L;

	public static transient Project myLastDeserialized = null;
	private static Project myDummy = null;
	private static final Logger log = Logger.getLogger( "Project" );
	
	public transient CalendarOption myCalendarOption = null;
	public transient PrintSettings myPrintSettings = null;
	public transient PrintSettings myTmpSettings = null;
	public transient SpreadSheetFieldArray myFieldArray = null;

	//Undo
	protected transient DataFactoryUndoController myUndoController;
	protected transient EventListenerList myProjectListenerList = new EventListenerList();
	protected transient Set<AssignmentKey> myInitialAssignmentIds;
	protected transient Set<DependencyKey> myInitialLinkIds;
	protected transient Set<Long> myInitialTaskIds;
	protected transient Set<AssignmentKey> myNewAssignmentIds;
	protected transient Set<DependencyKey> myNewLinkIds;
	protected transient Set<Long> myNewTaskIds;
	protected transient String myFileName;
	protected transient boolean myForceNonIncremental;
	protected transient boolean myForceNonIncrementalDistributions;
	protected transient boolean myTemporaryLocal;
	protected transient int myAccessControlPolicy;
	protected transient int myFileType = FileHelper.PROJITY_FILE_TYPE;
	protected boolean myMaster = false;
	protected boolean mySummaryTaskEnabled = false;
	protected float myVersion = CURRENT_VERSION;
	transient Calendar myCheckedOutDate;
	transient Calendar myPublishedDate;
	transient HashMap<String,String> myInvestmentMilestones;

	//	public static final WorkCalendar findBaseCalendar(String name) {
	//		return getInstance().findBaseCalendar(name,false);
	//	}
	transient String myCheckedOutUser;
	transient String myDivision; // exposed in database
	transient String myGroup; // exposed in database
	transient String myPublishedUser;
	transient boolean myMspAssociated;
	transient int myCheckedOutUserId = 0;
	private WorkCalendar myWorkCalendar = null;
	private Workspace myWorkspace;
	transient ExpenseType myExpenseType = ExpenseType.NONE; // exposed in database
	transient int myProjectStatus = ProjectStatus.PLANNING; // exposed in database
	transient int myProjectType = ProjectType.OTHER; // exposed in database
	private boolean myForward = true;
	boolean myWasImported = false; //used to see if it originated from an MSP or POD import
	int myPriority = 500;
	long myCurrentDate = 0;
	long myDuration;
	long myEnd;
	long myLastTimeTimesheetPolled = 0; // NOT transient
	long myPersistedId = 0; // used to see if copied from template
	long myStart;

	private transient BarClosure myBarClosureInstance = new BarClosure();
	private transient Date myCreationDate;
	private transient Date myLastModificationDate;
	private transient ExternalTaskManager myExternalTaskManager = null;
	private transient HasKeyImpl myHasKey;
	private transient Hyperlink myDocumentFolderUrl = null;
	private transient TaskBattery myTasks = new TaskBattery();
	private transient MultipleTransactionManager myMultipleTransactionManager = new MultipleTransactionManager();
	private transient Node myRootNode = null;
	private transient NodeModel myResourceModel = null;
	private transient NodeModel myTaskModel = null;
	private transient Object myResourceCache = null;
	private transient Object myTaskCache = null;
	private transient ObjectEventManager myObjectEventManager = new ObjectEventManager();
	private transient ObjectSelectionEventManager myObjectSelectionEventManager = new ObjectSelectionEventManager();
	private transient ProjectPermission myProjectPermission = null;
	private transient ResourcePool myResourcePool = null;
	private transient ScheduleEventManager myScheduleEventManager = new ScheduleEventManager();

	//private transient Schedule schedule = null; //used?
	private transient SchedulingAlgorithm mySchedulingAlgorithm = null;
	private transient SubprojectHandler mySubprojectHandler;
	private transient TreeMap<DistributionData,DistributionData> myDistributionMap;
	private transient TreeMap<DistributionData,DistributionData> myNewDistributionMap;
	private transient boolean myActualsProtected = false;

	//	private transient TaskCompletionOptions taskCompletionOption = TaskCompletionOptions.PERCENT_COMPLETE;
	private transient boolean myCompletionFromTimesheet = false;
	private transient boolean myInitialized = false;
	private transient boolean myIsDirty = false;
	private transient boolean myIsGroupDirty = false;
	private transient boolean noLongerLocked = false;
	private transient boolean myOpenedAsSubproject = false;
	private transient boolean myReadOnly = false;
	private transient boolean myTimesheetClosed = false;
	private Map myExtraFields = null;
	private String myManager = "";
	private String myNotes = "";
	private double myNetPresentValue = 0.0D;
	private double myRisk = 0.0D;
	private int myBenefit = 0;
	private long myStartDate = 0;
	
	// need strict order of creation of the following
	private transient MyNodeModelDataFactory myNodeModelDataFactory = new MyNodeModelDataFactory();
	private transient OutlineCollection myTaskOutlines = new OutlineCollectionImpl( Settings.numHierarchies(), myNodeModelDataFactory );
}
