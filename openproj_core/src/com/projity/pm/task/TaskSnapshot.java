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

import com.projity.algorithm.ReverseQuery;
import com.projity.algorithm.TimeIteratorGenerator;
import com.projity.algorithm.buffer.CalculatedValues;

import com.projity.association.AssociationList;

import com.projity.field.FieldContext;

import com.projity.pm.assignment.Assignment;
import com.projity.pm.assignment.HasAssignments;
import com.projity.pm.assignment.AssignmentContainer;
import com.projity.pm.assignment.TimeDistributedConstants;
import com.projity.pm.calendar.WorkCalendar;
import com.projity.pm.costing.Accrual;
import com.projity.pm.criticalpath.TaskSchedule;
import com.projity.pm.resource.Resource;
import com.projity.pm.snapshot.DataSnapshot;
import com.projity.pm.time.HasStartAndEnd;

import org.apache.commons.collections.Closure;

import org.openproj.domain.task.Task;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.Collection;


/**
 *
 */
public class TaskSnapshot
	implements DataSnapshot,
		HasAssignments,
		Cloneable
{
	/**
	 *
	 */
	public TaskSnapshot()
	{
		hasAssignments = new AssignmentContainer();
	}

	public TaskSnapshot( 
		final Collection _details )
	{
		hasAssignments = new AssignmentContainer( _details );
	}

	/**
	 * @param _start
	 * @param _end
	 * @return
	 */
	@Override
	public double actualCost( 
		final long _start,
		final long _end )
	{
		return hasAssignments.actualCost( _start, _end );
	}

	@Override
	public double actualFixedCost( 
		final long _start,
		final long _end )
	{
		return 0;
	}

	/**
	 * @param _start
	 * @param _end
	 * @return
	 */
	@Override
	public long actualWork( 
		final long _start,
		final long _end )
	{
		return hasAssignments.actualWork( _start, _end );
	}

	/**
	* @param _start
	* @param _end
	* @return
	*/
	@Override
	public double acwp( 
		final long _start,
		final long _end )
	{
		return hasAssignments.acwp( _start, _end );
	}

	/**
	 * @param _assignment
	 */
	@Override
	public void addAssignment( 
		final Assignment _assignment )
	{
		hasAssignments.addAssignment( _assignment );
	}

	/**
	 * @param _start
	 * @param _end
	 * @return
	 */
	@Override
	public double bac( 
		final long _start,
		final long _end )
	{
		return hasAssignments.bac( _start, _end );
	}

	/**
	 * @param _start
	 * @param _end
	 * @return
	 */
	@Override
	public double baselineCost( 
		final long _start,
		final long _end )
	{
		return hasAssignments.baselineCost( _start, _end );
	}

	/**
	 * @param _start
	 * @param _end
	 * @return
	 */
	@Override
	public long baselineWork( 
		final long _start,
		final long _end )
	{
		return hasAssignments.baselineWork( _start, _end );
	}

	/**
	 * @param _start
	 * @param _end
	 * @return
	 */
	@Override
	public double bcwp( 
		final long _start,
		final long _end )
	{
		return hasAssignments.bcwp( _start, _end );
	}

	/**
	 * @param _start
	 * @param _end
	 * @return
	 */
	@Override
	public double bcws( 
		final long _start,
		final long _end )
	{
		return hasAssignments.bcws( _start, _end );
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#buildComplexQuery(com.projity.algorithm.ComplexQuery)
	 */
	@Override
	public void buildReverseQuery( 
		final ReverseQuery _reverseQuery )
	{
		hasAssignments.buildReverseQuery( _reverseQuery );
	}

	/**
	 * @param workCalendar
	 * @return
	 */
	@Override
	public long calcActiveAssignmentDuration( 
		final WorkCalendar _workCalendar )
	{
		return hasAssignments.calcActiveAssignmentDuration( _workCalendar );
	}

	/**
	 * @param _type
	 * @param _generator
	 * @param _values
	 */
	@Override
	public void calcDataBetween( 
		final TimeDistributedConstants _type,
		final TimeIteratorGenerator _generator,
		final CalculatedValues _values )
	{
		hasAssignments.calcDataBetween( _type, _generator, _values );
	}

	/**
	 * @return
	 */
	@Override
	public Collection childrenToRollup()
	{
		return hasAssignments.childrenToRollup();
	}

	@Override
	public Object clone()
	{
		TaskSnapshot newOne = null;

		try
		{
			newOne = (TaskSnapshot)super.clone();
			newOne.currentSchedule = (TaskSchedule)currentSchedule.clone();
			newOne.hasAssignments = (HasAssignments)((AssignmentContainer)hasAssignments).cloneWithSchedule( newOne.currentSchedule );
		}
		catch (CloneNotSupportedException e)
		{
			e.printStackTrace();
		}

		return newOne;
	}

	/**
	 * @param _start
	 * @param _end
	 * @return
	 */
	@Override
	public double cost( 
		final long _start,
		final long _end )
	{
		return hasAssignments.cost( _start, _end );
	}

	public Object deepCloneWithTask( 
		final Task _task )
	{
		TaskSnapshot newOne = null;

		try
		{
			newOne = (TaskSnapshot)super.clone();
			newOne.currentSchedule = (TaskSchedule)currentSchedule.cloneWithTask( _task );
			newOne.hasAssignments = (HasAssignments)((AssignmentContainer)hasAssignments).deepCloneWithTask( _task );
		}
		catch (CloneNotSupportedException e)
		{
			e.printStackTrace();
		}

		return newOne;
	}

	//call init to complete initialization
	public static TaskSnapshot deserialize( 
		final ObjectInputStream _stream,
		final int _version )
		throws IOException, 
			ClassNotFoundException
	{
		TaskSnapshot snapshot = new TaskSnapshot();
		TaskSchedule schedule = TaskSchedule.deserialize( _stream );
		snapshot.setCurrentSchedule( schedule );
		
		snapshot.setFixedCost( _stream.readDouble() );
		snapshot.setFixedCostAccrual( _stream.readInt() );
		snapshot.setIgnoreResourceCalendar( _stream.readBoolean() );

		if (_version >= 2)
		{
			snapshot.hasAssignments.setSchedulingType( _stream.readInt() );
			snapshot.hasAssignments.setEffortDriven( _stream.readBoolean() );
		}

		return snapshot;
	}

	/**
	 * @param resource
	 * @return
	 */
	@Override
	public Assignment findAssignment( 
		final Resource _resource )
	{
		return hasAssignments.findAssignment( _resource );
	}

	/**
	 * @param task
	 * @return
	 */
	@Override
	public Assignment findAssignment( 
		final Task _task )
	{
		return hasAssignments.findAssignment( _task );
	}

	@Override
	public double fixedCost( 
		final long _start,
		final long _end )
	{
		if (FieldContext.isFullRange( _start, _end ))
		{
			return fixedCost;
		}

		return 0;
	}

	/**
	 * @param visitor
	 * @return
	 */
	public static Closure forAllAssignments( 
		final Closure _visitor )
	{
		return AssignmentContainer.forAllAssignments( _visitor );
	}

	/**
	 * @param _visitor
	 * @param _mergeWorking
	 * @param _workCalendar
	 */
	@Override
	public void forEachWorkingInterval( 
		final Closure<HasStartAndEnd> _visitor,
		final boolean _mergeWorking,
		final WorkCalendar _workCalendar )
	{
		hasAssignments.forEachWorkingInterval( _visitor, _mergeWorking, _workCalendar );
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasAssignments#getAssignments()
	 */
	@Override
	public AssociationList getAssignments()
	{
		return hasAssignments.getAssignments();
	}

	/**
	 * @return Returns the taskSchedule.
	 */
	public TaskSchedule getCurrentSchedule()
	{
		return currentSchedule;
	}

	@Override
	public long getEarliestAssignmentStart()
	{
		return hasAssignments.getEarliestAssignmentStart();
	}

	/**
	 * @return Returns the fixedCost.
	 */
	public final double getFixedCost()
	{
		return fixedCost;
	}

	/**
	 * @return Returns the fixedCostAccrual.
	 */
	public final int getFixedCostAccrual()
	{
		return fixedCostAccrual;
	}

	public HasAssignments getHasAssignments()
	{
		return hasAssignments;
	}

	/**
	 * @return
	 */
	@Override
	public int getSchedulingType()
	{
		return hasAssignments.getSchedulingType();
	}

	@Override
	public boolean hasActiveAssignment( 
		final long start,
		final long end )
	{
		return hasAssignments.hasActiveAssignment( start, end );
	}

	@Override
	public boolean hasLaborAssignment()
	{
		return hasAssignments.hasLaborAssignment();
	}

	@Override
	public void invalidateAssignmentCalendars()
	{
		hasAssignments.invalidateAssignmentCalendars();
	}

	/**
	 * @return
	 */
	@Override
	public boolean isEffortDriven()
	{
		return hasAssignments.isEffortDriven();
	}

	/**
	 * @return Returns the ignoreResourceCalendar.
	 */
	public final boolean isIgnoreResourceCalendar()
	{
		return ignoreResourceCalendar;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#isLabor()
	 */
	@Override
	public boolean isLabor()
	{
		return true;
	}

	/**
	 * @return
	 */
	@Override
	public boolean isReadOnlyEffortDriven( 
		final FieldContext _fieldContext )
	{
		return hasAssignments.isReadOnlyEffortDriven( _fieldContext );
	}

	/**
	 * @param _start
	 * @param _end
	 * @return
	 */
	@Override
	public long remainingWork( 
		final long _start,
		final long _end )
	{
		return hasAssignments.remainingWork( _start, _end );
	}

	/**
	 * @param assignment
	 */
	@Override
	public void removeAssignment( 
		final Assignment _assignment )
	{
		hasAssignments.removeAssignment( _assignment );
	}

	public void serialize( 
		final ObjectOutputStream _stream )
		throws IOException
	{
		currentSchedule.serialize( _stream );

		//s.writeObject(hasAssignments);
		_stream.writeDouble( fixedCost );
		_stream.writeInt( fixedCostAccrual );
		_stream.writeBoolean( ignoreResourceCalendar );
		_stream.writeInt( hasAssignments.getSchedulingType() );
		_stream.writeBoolean( hasAssignments.isEffortDriven() );
	}

	/** Assign the <i>current schedule</i> attribute.
	 * 
	 * @param _value used as the source of the assignment.
	 */
	public final void setCurrentSchedule( 
		final TaskSchedule _value )
	{
		currentSchedule = _value;
	}

	@Override
	public void setEffortDriven( 
		final boolean _effortDriven )
	{
		hasAssignments.setEffortDriven( _effortDriven );
	}

	/** Assign the <i>fixed cost</i> attribute.
	 * 
	 * @param _value used as the source of the assignment.
	 */
	public final void setFixedCost( 
		final double _value )
	{
		fixedCost = _value;
	}

	/** Assign the <i>fixed cost accrual</i> attribute.
	 * 
	 * @param _value used as the source of the assignment.
	 */
	public final void setFixedCostAccrual( 
		final int _value )
	{
		fixedCostAccrual = _value;
	}

	/** Assign the <i>ignore resource calendar</i> attribute.
	 * 
	 * @param _value used as the source of the assignment.
	 */
	public final void setIgnoreResourceCalendar( 
		final boolean _value )
	{
		ignoreResourceCalendar = _value;
	}

	@Override
	public void setSchedulingType( 
		final int _value )
	{
		hasAssignments.setSchedulingType( _value );
	}

	@Override
	public void updateAssignment( 
		final Assignment _modified )
	{
		hasAssignments.updateAssignment( _modified );
	}

	/**
	 * @param _start
	 * @param _end
	 * @return
	 */
	@Override
	public long work( 
		final long _start,
		final long _end )
	{
		return hasAssignments.work( _start, _end );
	}

	HasAssignments hasAssignments = null;
	TaskSchedule currentSchedule;
	boolean ignoreResourceCalendar = false;
	double fixedCost = 0;
	int fixedCostAccrual = Accrual.END;
}
