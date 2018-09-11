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
package com.projity.pm.resource;

import com.projity.algorithm.ReverseQuery;
import com.projity.algorithm.TimeIteratorGenerator;
import com.projity.algorithm.buffer.CalculatedValues;
import com.projity.algorithm.buffer.GroupedCalculatedValues;

import com.projity.association.AssociationList;

import com.projity.company.ApplicationUser;

import com.projity.configuration.CircularDependencyException;

import com.projity.datatype.ImageLink;
import com.projity.datatype.Rate;
import com.projity.datatype.RateFormat;
import com.projity.datatype.TimeUnit;

import org.openproj.domain.document.Document;

import com.projity.field.CustomFields;
import com.projity.field.CustomFieldsImpl;
import com.projity.field.FieldContext;

import com.projity.interval.InvalidValueObjectForIntervalException;

import com.projity.pm.assignment.Assignment;
import com.projity.pm.assignment.HasAssignments;
import com.projity.pm.assignment.AssignmentContainer;
import com.projity.pm.assignment.TimeDistributedConstants;
import com.projity.pm.assignment.timesheet.TimesheetHelper;
import com.projity.pm.availability.AvailabilityTable;
import com.projity.pm.calendar.CalendarService;
import com.projity.pm.calendar.WorkCalendar;
import com.projity.pm.calendar.WorkingCalendar;
import com.projity.pm.costing.Accrual;
import com.projity.pm.costing.CostRateTable;
import com.projity.pm.costing.CostRateTables;
import com.projity.pm.costing.EarnedValueCalculator;
import com.projity.pm.key.HasKeyImpl;
import com.projity.pm.time.HasStartAndEnd;

import com.projity.strings.Messages;

import com.projity.util.Environment;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.Closure;

import org.openproj.domain.task.Task;

/**
 * A global resource that belongs to the enterprise resource pool
 */
public class EnterpriseResource
	implements Resource
{
	public EnterpriseResource( 
		final ResourcePool _resourcePool )
	{
		this((_resourcePool == null) || _resourcePool.isLocal(), _resourcePool);
	}

	public EnterpriseResource( 
		final boolean _local,
		final ResourcePool _resourcePool )
	{
		hasKey = new HasKeyImpl( _local, this );
		this.resourcePool = resourcePool;

		if (resourcePool != null)
		{
			workCalendar = WorkingCalendar.getInstanceBasedOn( resourcePool.getDefaultCalendar() );
			workCalendar.setName( "" );
		}
	}

	/**
	 * @param start
	 * @param end
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
	 * @param assignment
	 */
	@Override
	public void addAssignment( 
		final Assignment _assignment )
	{
		hasAssignments.addAssignment( _assignment );
	}

	@Override
	public boolean applyTimesheet( 
		final Collection _fieldArray,
		final long _timesheetUpdateDate )
	{
		return TimesheetHelper.applyTimesheet( getAssignments(), _fieldArray, _timesheetUpdateDate );
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

	/*public void addDefaultAssignment() {
	        hasAssignments.addAssignment(newDefaultAssignment());
	}
	private Assignment newDefaultAssignment() {
	        return Assignment.getInstance(NormalTask
	                        .getUnassignedInstance(),this, 1.0, 0);
	}*/

	/**
	 * @param reverseQuery
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

	public void cleanClone()
	{
		resourcePool = null;
	}

	@Override
	public Object clone()
	{
		try
		{
			EnterpriseResource resource = (EnterpriseResource)super.clone();
			resource.hasKey = new HasKeyImpl(isLocal() && Environment.getStandAlone(), resource);
			resource.setName( getName() );

			if (notes != null)
			{
				resource.notes = notes;
			}

			if (group != null)
			{
				resource.group = group;
			}

			if (group != null)
			{
				resource.initials = initials;
			}

			if (phonetics != null)
			{
				resource.phonetics = phonetics;
			}

			if (rbsCode != null)
			{
				resource.rbsCode = rbsCode;
			}

			if (emailAddress != null)
			{
				resource.emailAddress = emailAddress;
			}

			if (materialLabel != null)
			{
				resource.materialLabel = materialLabel;
			}

			if (userAccount != null)
			{
				resource.userAccount = userAccount;
			}

			resource.costRateTables = (CostRateTables)costRateTables.clone();
			resource.hasAssignments = (HasAssignments)((AssignmentContainer)hasAssignments).cloneWithResource( resource );
			resource.customFields = (CustomFieldsImpl)customFields.clone();

			resource.availabilityTable = (AvailabilityTable)availabilityTable.clone();
//			resource.availabilityTable.initAfterCloning();

			return resource;
		}
		catch (CloneNotSupportedException e)
		{
			throw new InternalError();
		}
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

	public static void dumpSize( 
		final String _name,
		final Closure _c )
	{
		try
		{
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("/Users/chretien/tmp/r." + _name + "." +
						System.currentTimeMillis()));
			_c.execute( out );

			//out.writeObject(data);
			out.flush();
			out.close();
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean fieldHideActualCost( 
		final FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	@Override
	public boolean fieldHideActualFixedCost( 
		final FieldContext _fieldContext )
	{
		return true;
	}

	@Override
	public boolean fieldHideActualWork( 
		final FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	@Override
	public boolean fieldHideAcwp( 
		final FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	@Override
	public boolean fieldHideBac( 
		final FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	@Override
	public boolean fieldHideBaseCalendar( 
		final FieldContext _fieldContext )
	{
		return !isLabor();
	}

	@Override
	public boolean fieldHideBaselineCost( 
		final int _numBaseline,
		final FieldContext _fieldContext )
	{
		return false; //TODO implement
	}

	@Override
	public boolean fieldHideBaselineWork( 
		final int _numBaseline,
		final FieldContext _fieldContext )
	{
		return false; //TODO implement
	}

	@Override
	public boolean fieldHideBcwp( 
		final FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	@Override
	public boolean fieldHideBcws( 
		final FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	@Override
	public boolean fieldHideCost( 
		final FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	@Override
	public boolean fieldHideCpi( 
		final FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	@Override
	public boolean fieldHideCv( 
		final FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	@Override
	public boolean fieldHideCvPercent( 
		final FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	@Override
	public boolean fieldHideEac( 
		final FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	@Override
	public boolean fieldHideOvertimeRate( 
		final FieldContext _fieldContext )
	{
		return !isLabor();
	}

	@Override
	public boolean fieldHideSpi( 
		final FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	@Override
	public boolean fieldHideSv( 
		final FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	@Override
	public boolean fieldHideSvPercent( 
		final FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	@Override
	public boolean fieldHideTcpi( 
		final FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	@Override
	public boolean fieldHideVac( 
		final FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	@Override
	public boolean fieldHideWork( 
		final FieldContext _fieldContext )
	{
		return isFieldHidden( _fieldContext );
	}

	public void filterRoles( 
		final List _keys,
		final List _values )
	{
		if (authorizedRoles == null)
		{
			return;
		}

		Iterator k = _keys.iterator();
		Iterator<Integer> v = ((List<Integer>)_values).iterator();
		Object inactiveKey = null;

		while (v.hasNext())
		{
			Object key = k.next();
			int r = v.next();

			if (r == ApplicationUser.INACTIVE)
			{
				inactiveKey = key;
			}

			if (((r == ApplicationUser.INACTIVE) && (getAssignments().size() > 0)) || !authorizedRoles.contains( r ))
			{
				k.remove();
			}
		}

		if (_keys.size() == 0)
		{
			_keys.add( inactiveKey ); //occurs when an user becomes "inactive"
		}
	}

	/**
	 * @param _resource
	 * @return
	 */
	@Override
	public Assignment findAssignment( 
		final Resource _resource )
	{
		return hasAssignments.findAssignment( _resource );
	}

	/**
	 * @param _task
	 * @return
	 */
	@Override
	public Assignment findAssignment( 
		final Task _task )
	{
		return hasAssignments.findAssignment( _task );
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#actualFixedCost(long, long)
	 */
	@Override
	public double fixedCost( 
		final long _start,
		final long _end )
	{
		return 0;
	}

	/**
	 * @param _visitor
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
	 * @see com.projity.pm.resource.Resource#getAccrueAt()
	 */
	@Override
	public int getAccrueAt()
	{
		return accrueAt;
	}

	@Override
	public double getActualCost( 
		final FieldContext _fieldContext )
	{
		return actualCost( FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	@Override
	public double getActualFixedCost( 
		final FieldContext _fieldContext )
	{
		return 0;
	}

	@Override
	public long getActualWork( 
		final FieldContext _fieldContext )
	{
		return actualWork( FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	@Override
	public double getAcwp( 
		final FieldContext _fieldContext )
	{
		return acwp( FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	/**
	 * @return
	 */
	@Override
	public AssociationList getAssignments()
	{
		return hasAssignments.getAssignments();
	}

	public Set<Integer> getAuthorizedRoles()
	{
		return authorizedRoles;
	}

	@Override
	public AvailabilityTable getAvailabilityTable()
	{
		//TODO implement this somehow. need to figure out relationship to ResourceImpl version
		// Do projects have their own availability tables or not?
		// TODO Auto-generated method stub
		return availabilityTable;
	}

	@Override
	public double getBac( 
		final FieldContext _fieldContext )
	{
		return bac( FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.resource.ResourceSpecificFields#getBaseCalendar()
	 */
	@Override
	public WorkCalendar getBaseCalendar()
	{
		if (getWorkCalendar() == null)
		{
			return null;
		}

		return (WorkingCalendar)((WorkingCalendar)getWorkCalendar()).getBaseCalendar();
	}

	@Override
	public double getBaselineCost( 
		final int _numBaseline,
		final FieldContext _fieldContext )
	{
		return baselineCost( FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.TimeDistributedFields#getBaselineWork(int, com.projity.field.FieldContext)
	 */
	@Override
	public long getBaselineWork( 
		int _numBaseline,
		final FieldContext _fieldContext )
	{
		return baselineWork( FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	@Override
	public double getBcwp( 
		final FieldContext _fieldContext )
	{
		return bcwp( FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	@Override
	public double getBcws( 
		final FieldContext _fieldContext )
	{
		return bcws( FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	public ImageLink getBudgetStatusIndicator()
	{
		return EarnedValueCalculator.getInstance().getBudgetStatusIndicator( getCpi( null ) );
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#actualWork(long, long)
	 */
	@Override
	public double getCost( 
		final FieldContext _fieldContext )
	{
		return cost( FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.costing.Rate#getCostPerUse()
	 */
	@Override
	public double getCostPerUse()
	{
		return costRateTables.getCostPerUse();
	}

	/**
	 * @return Returns the costRateTable.
	 */
	@Override
	public CostRateTable getCostRateTable( 
		final int _costRateIndex )
	{
		return costRateTables.getCostRateTable( _costRateIndex );
	}

	@Override
	public double getCpi( 
		final FieldContext _fieldContext )
	{
		return EarnedValueCalculator.getInstance().cpi( this, FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
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
		final FieldContext _fieldContext )
	{
		return EarnedValueCalculator.getInstance().csi( this, FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	@Override
	public double getCustomCost( 
		final int _i )
	{
		return customFields.getCustomCost( _i );
	}

	@Override
	public long getCustomDate( 
		final int _i )
	{
		return customFields.getCustomDate( _i );
	}

	@Override
	public long getCustomDuration( 
		final int _i )
	{
		return customFields.getCustomDuration( _i );
	}

	@Override
	public CustomFields getCustomFields()
	{
		return customFields;
	}

	@Override
	public long getCustomFinish( 
		final int _i )
	{
		return customFields.getCustomFinish( _i );
	}

	@Override
	public boolean getCustomFlag( 
		final int _i )
	{
		return customFields.getCustomFlag( _i );
	}

	@Override
	public double getCustomNumber( 
		final int _i )
	{
		return customFields.getCustomNumber( _i );
	}

	@Override
	public long getCustomStart( 
		final int _i )
	{
		return customFields.getCustomStart( _i );
	}

	@Override
	public String getCustomText( 
		final int _i )
	{
		return customFields.getCustomText( _i );
	}

	@Override
	public double getCv( 
		final FieldContext _fieldContext )
	{
		return EarnedValueCalculator.getInstance().cv( this, FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	@Override
	public double getCvPercent( 
		final FieldContext _fieldContext )
	{
		return EarnedValueCalculator.getInstance().cvPercent( this, FieldContext.start( _fieldContext ), 
			FieldContext.end( _fieldContext ) );
	}

	public int getDefaultRole()
	{
		return defaultRole;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.task.BelongsToDocument#getDocument()
	 */
	@Override
	public Document getDocument()
	{
		return resourcePool;
	}

	@Override
	public double getEac( 
		final FieldContext _fieldContext )
	{
		return EarnedValueCalculator.getInstance().eac( this, FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	@Override
	public long getEarliestAssignmentStart()
	{
		return hasAssignments.getEarliestAssignmentStart();
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.costing.Cost#getEffectiveDate()
	 */
	@Override
	public long getEffectiveDate()
	{
		return costRateTables.getEffectiveDate();
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.time.HasCalendar#getEffectiveWorkCalendar()
	 */
	@Override
	public WorkCalendar getEffectiveWorkCalendar()
	{
		return workCalendar; // can be null
	}

	/**
	 * @return Returns the emailAddress.
	 */
	@Override
	public String getEmailAddress()
	{
		return emailAddress;
	}

	@Override
	public long getExternalId()
	{
		return externalId;
	}

	@Override
	public long getFinishOffset()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getFixedCost( 
		final FieldContext _fieldContext )
	{
		return 0;
	}

	@Override
	public EnterpriseResource getGlobalResource()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public GroupedCalculatedValues getGlobalWorkVector()
	{
		return globalWorkVector;
	}

	/**
	 * @return Returns the group.
	 */
	@Override
	public String getGroup()
	{
		return group;
	}

	/**
	 * @return
	 */
	@Override
	public long getId()
	{
		return hasKey.getId();
	}

	/**
	 * @return Returns the initials.
	 */
	@Override
	public String getInitials()
	{
		return initials;
	}

	@Override
	public long getLastTimesheetUpdate()
	{
		return TimesheetHelper.getLastTimesheetUpdate( getAssignments() );
	}

	public int getLicense()
	{
		return license;
	}

	public int getLicenseOptions()
	{
		return licenseOptions;
	}

	/**
	 * @return Returns the materialLabel.
	 */
	@Override
	public String getMaterialLabel()
	{
		return materialLabel;
	}

	/**
	 * @return Returns the maxUnits.
	 */
	@Override
	public double getMaximumUnits()
	{
		return maximumUnits;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public String getName()
	{
		return hasKey.getName();
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public String getName( 
		final FieldContext _context )
	{
		return hasKey.getName( _context );
	}

	/**
	 * @return Returns the notes.
	 */
	@Override
	public String getNotes()
	{
		return notes;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.costing.Rate#getOvertimeRate()
	 */
	@Override
	public Rate getOvertimeRate()
	{
		return costRateTables.getOvertimeRate();
	}

	@Override
	public long getParentId( 
		final int _outlineNumber )
	{
		// currently the model contains ResourceImpls and not enterprise resources
		return 0;
	}

	/**
	 * @return Returns the phonetics.
	 */
	@Override
	public String getPhonetics()
	{
		return phonetics;
	}

	@Override
	public RateFormat getRateFormat()
	{
		return RateFormat.getInstance( getTimeUnitLabel(), false, isLabor(), isLabor() );
	}

	/**
	 * @return Returns the rbsCode.
	 */
	@Override
	public String getRbsCode()
	{
		return rbsCode;
	}

	@Override
	public double getRemainingCost( 
		FieldContext _fieldContext )
	{
		return getCost( _fieldContext ) - getActualCost( _fieldContext );
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.resource.ResourceSpecificFields#getRemainingOvertimeCost()
	 */
	@Override
	public double getRemainingOvertimeCost()
	{
		// TODO implement this
		return -1;
	}

	@Override
	public long getRemainingWork( 
		final FieldContext _fieldContext )
	{
		return remainingWork( FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	@Override
	public String getResourceName()
	{
		return getName();
	}

	/**
	 * @return Returns the resourcePool.
	 */
	public ResourcePool getResourcePool()
	{
		return resourcePool;
	}

	@Override
	public int getResourceType()
	{
		return resourceType;
	}

	@Override
	public int getRole()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public ImageLink getScheduleStatusIndicator()
	{
		return EarnedValueCalculator.getInstance().getBudgetStatusIndicator( getSpi( null ) );
	}

	/**
	 * This is unused
	 * @return
	 */
	@Override
	public int getSchedulingType()
	{
		return hasAssignments.getSchedulingType();
	}

	public Object getServerMeta()
	{
		return serverMeta;
	}

	@Override
	public double getSpi( 
		final FieldContext _fieldContext )
	{
		return EarnedValueCalculator.getInstance().spi( this, FieldContext.start( _fieldContext ), 
			FieldContext.end( _fieldContext ) );
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.costing.Rate#getStandardRate()
	 */
	@Override
	public Rate getStandardRate()
	{
		return costRateTables.getStandardRate();
	}

	@Override
	public long getStartOffset()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getSv( 
		final FieldContext _fieldContext )
	{
		return EarnedValueCalculator.getInstance().sv( this, FieldContext.start( _fieldContext ), 
			FieldContext.end( _fieldContext ) );
	}

	@Override
	public double getSvPercent( 
		final FieldContext _fieldContext )
	{
		return EarnedValueCalculator.getInstance().svPercent( this, FieldContext.start( _fieldContext ), 
			FieldContext.end( _fieldContext ) );
	}

	@Override
	public double getTcpi( 
		final FieldContext _fieldContext )
	{
		return EarnedValueCalculator.getInstance().tcpi( this, FieldContext.start( _fieldContext ),
			FieldContext.end( _fieldContext ) );
	}

	/* (non-Javadoc)
	 * @see com.projity.datatype.CanSupplyRateUnit#getTimeUnit()
	 */
	@Override
	public String getTimeUnitLabel()
	{
		if (getResourceType() == ResourceType.WORK)
		{
			return null;
		}

		return getMaterialLabel();
	}

	@Override
	public int getTimesheetStatus()
	{
		return TimesheetHelper.getTimesheetStatus( getAssignments() );
	}

	@Override
	public String getTimesheetStatusName()
	{
		return TimesheetHelper.getTimesheetStatusName( getTimesheetStatus() );
	}

	/**
	 * @return
	 */
	public static Resource getUnassignedInstance()
	{
		if (UNASSIGNED == null)
		{
			UNASSIGNED = new EnterpriseResource( null ); //local
			UNASSIGNED.setName( Messages.getString( "Text.Unassigned" ) );
			UNASSIGNED.setUniqueId( UNASSIGNED_ID );
		}

		return UNASSIGNED;
	}

	/**
	 * @return
	 */
	@Override
	public long getUniqueId()
	{
		return hasKey.getUniqueId();
	}

	@Override
	public String getUserAccount()
	{
		return userAccount;
	}

	@Override
	public double getVac( 
		final FieldContext _fieldContext )
	{
		return EarnedValueCalculator.getInstance().vac( this, FieldContext.start( _fieldContext ), 
			FieldContext.end( _fieldContext ) );
	}

	public short getVersion()
	{
		return version;
	}

	@Override
	public long getWork( 
		final FieldContext _fieldContext )
	{
		return work( FieldContext.start( _fieldContext ), FieldContext.end( _fieldContext ) );
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.time.HasCalendar#getWorkCalendar()
	 */
	@Override
	public WorkCalendar getWorkCalendar()
	{
		return workCalendar;
	}

	@Override
	public boolean hasActiveAssignment( 
		final long _start,
		final long _end )
	{
		return hasAssignments.hasActiveAssignment( _start, _end );
	}

	//	public boolean isNew() {
	//		return hasKey.isNew();
	//	}
	@Override
	public boolean hasLaborAssignment()
	{
		return isLabor() && !getAssignments().isEmpty();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return hasAssignments.hashCode();
	}

	@Override
	public void invalidateAssignmentCalendars()
	{
		hasAssignments.invalidateAssignmentCalendars();
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.calendar.HasCalendar#invalidateCalendar()
	 */
	@Override
	public Document invalidateCalendar()
	{
		invalidateAssignmentCalendars();

		return getResourcePool();
	}

	public boolean isAdministrator()
	{
		return (licenseOptions & ApplicationUser.ADMINISTRATOR) == ApplicationUser.ADMINISTRATOR;
	}

	@Override
	public boolean isAssignedToSomeProject()
	{
		if (hasAssignments.getAssignments().size() > 0)
		{
			return true;
		}

		// note that this doesn't mean there isn't baseline info assigned
		if ((globalWorkVector == null) || (globalWorkVector.size() == 0)) 
		{
			return false;
		}

		return true;
	}

	@Override
	public boolean isAssignment()
	{ 
		//for filters
		return false;
	}

	@Override
	public boolean isDefault()
	{
		return getUniqueId() == UNASSIGNED_ID;
	}

	@Override
	public boolean isDirty()
	{
		return dirty;
	}

	/**
	 * This is unused - a resource is not effort driven
	 * @return
	 */
	@Override
	public boolean isEffortDriven()
	{
		return false;
	}

	public boolean isExternal()
	{
		return (licenseOptions & ApplicationUser.EXTERNAL) == ApplicationUser.EXTERNAL;
	}

	private boolean isFieldHidden( 
		final FieldContext _fieldContext )
	{
		return false;
	}

	/**
	 * @return Returns the generic.
	 */
	@Override
	public boolean isGeneric()
	{
		return generic;
	}

	/**
	 * @return Returns the active.
	 */
	@Override
	public boolean isInactive()
	{
		return inactive;
	}

	public boolean isInactiveLicense()
	{
		return license == ApplicationUser.INACTIVE;
	}

	@Override
	public boolean isLabor()
	{
		return resourceType == ResourceType.WORK; // work resources are time based
	}

	@Override
	public boolean isLocal()
	{
		return hasKey.isLocal();
	}

	public boolean isMaster()
	{
		return master;
	}

	@Override
	public boolean isMaterial()
	{
		return getResourceType() == ResourceType.MATERIAL;
	}

	@Override
	public boolean isMe()
	{
		if (userAccount == null)
		{
			return false;
		}

		return userAccount.equals( Environment.getLogin() );
	}

	@Override
	public boolean isParent()
	{
		// currently the model contains ResourceImpls and not enterprise resources
		return false;
	}

	@Override
	public boolean isPendingTimesheetUpdate()
	{
		return TimesheetHelper.isPendingTimesheetUpdate( getAssignments() );
	}

	public boolean isReadOnly()
	{
		return !master && !isLocal() && !Environment.getStandAlone();
	}

	@Override
	public boolean isReadOnlyActualWork( 
		final FieldContext _fieldContext )
	{
		return true;
	}

	@Override
	public boolean isReadOnlyCost( 
		final FieldContext _fieldContext )
	{
		return true;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.costing.Cost#isReadOnlyEffectiveDate()
	 */
	@Override
	public boolean isReadOnlyEffectiveDate( 
		final FieldContext _fieldContext )
	{
		return costRateTables.isReadOnlyEffectiveDate( _fieldContext );
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

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.TimeDistributedFields#isReadOnlyFixedCost(com.projity.field.FieldContext)
	 */
	@Override
	public boolean isReadOnlyFixedCost( 
		final FieldContext _fieldContext )
	{
		return true;
	}

	@Override
	public boolean isReadOnlyMaterialLabel( 
		final FieldContext _fieldContext )
	{
		return isLabor();
	}

	@Override
	public boolean isReadOnlyRemainingWork( 
		final FieldContext _fieldContext )
	{
		return true;
	}

	@Override
	public boolean isReadOnlyWork( 
		final FieldContext _fieldContext )
	{
		return true;
	}

	@Override
	public boolean isUser()
	{
		return (userAccount != null) && (userAccount.length() > 0);
	}

	@Override
	public boolean isWork()
	{
		return getResourceType() == ResourceType.WORK;
	}

	private void readObject( 
		final ObjectInputStream _stream )
		throws IOException, 
			ClassNotFoundException
	{
		_stream.defaultReadObject();
		hasKey = HasKeyImpl.deserialize( _stream, this );
		costRateTables = (version >= 3)
			? CostRateTables.deserializeCompact( _stream )
			: CostRateTables.deserialize( _stream );

		try
		{
			customFields = CustomFieldsImpl.deserialize( _stream );
		}
		catch (java.io.OptionalDataException e)
		{
			// to ensure compatibilty with old files
			customFields = new CustomFieldsImpl();
		}

		hasAssignments = new AssignmentContainer();

		if (version >= 2)
		{
			hasAssignments.setSchedulingType( _stream.readInt() );
			hasAssignments.setEffortDriven( _stream.readBoolean() );

			if (version == 2)
			{
				availabilityTable = AvailabilityTable.deserialize( _stream );
			}
			else
			{
				availabilityTable = new AvailabilityTable();
				availabilityTable.deserializeCompact( _stream );
			}
		}
		else
		{
			availabilityTable = new AvailabilityTable(null);
		}

		if (version >= 4)
		{
			if (_stream.readBoolean())
			{
				workCalendar = WorkingCalendar.getInstance(); //assume WorkingCalendar impl
				workCalendar.deserialiseCompact( _stream );
			}
		}

		version = DEFAULT_VERSION;
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

	@Override
	public boolean renumber( 
		final boolean _localOnly )
	{
		return hasKey.renumber( _localOnly );
	}

	/**
	 * @param accrueAt The accrueAt to set.
	 */
	@Override
	public void setAccrueAt( 
		final int _accrueAt )
	{
		this.accrueAt = _accrueAt;
	}

	@Override
	public void setActualWork( 
		final long _work,
		final FieldContext _fieldContext )
	{
		//do nothing
	}

	public void setAuthorizedRoles( 
		final Set<Integer> _authorizedRoles )
	{
		this.authorizedRoles = _authorizedRoles;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.resource.ResourceSpecificFields#setBaseCalendar(com.projity.pm.calendar.WorkingCalendar)
	 */
	@Override
	public void setBaseCalendar( 
		final WorkCalendar _baseCalendar )
		throws CircularDependencyException
	{
		WorkCalendar old = getWorkCalendar();

		if (old == null)
		{
			return;
		}

		CalendarService.getInstance().reassignCalendar( this, old, _baseCalendar );

		((WorkingCalendar)getWorkCalendar()).changeBaseCalendar( _baseCalendar );
		invalidateAssignmentCalendars(); // assignments intersection calendars need to be recalculated
	}

	@Override
	public void setCost( 
		final double _cost,
		final FieldContext _fieldContext )
	{
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.costing.Cost#setCostPerUse(double)
	 */
	@Override
	public void setCostPerUse( 
		final double _costPerUse )
	{
		costRateTables.setCostPerUse( _costPerUse );
	}

//	public void setNew(boolean isNew) {
//		hasKey.setNew(isNew);
//	}
	
	@Override
	public void setCreated( 
		final Date _created )
	{
		hasKey.setCreated( _created );
	}

	@Override
	public void setCustomCost( 
		final int _index,
		final double _cost )
	{
		customFields.setCustomCost( _index, _cost );
	}

	@Override
	public void setCustomDate( 
		final int _index,
		final long _date )
	{
		customFields.setCustomDate( _index, _date );
	}

	@Override
	public void setCustomDuration( 
		final int _index,
		final long _duration )
	{
		customFields.setCustomDuration( _index, _duration );
	}

	@Override
	public void setCustomFinish( 
		final int _index,
		final long _finish )
	{
		customFields.setCustomFinish( _index, _finish );
	}

	@Override
	public void setCustomFlag( 
		final int _index,
		final boolean _flag )
	{
		customFields.setCustomFlag( _index, _flag );
	}

	@Override
	public void setCustomNumber( 
		final int _index,
		final double _number )
	{
		customFields.setCustomNumber( _index, _number );
	}

	@Override
	public void setCustomStart( 
		final int _index,
		final long _start )
	{
		customFields.setCustomStart( _index, _start );
	}

	@Override
	public void setCustomText( 
		final int _index,
		final String _text )
	{
		customFields.setCustomText( _index, _text );
	}

	public void setDefaultRole( 
		final int _defaultRole )
	{
		this.defaultRole = _defaultRole;
	}

	@Override
	public void setDirty( 
		final boolean _dirty )
	{
		//System.out.println("EnterpriseResource _setDirty("+dirty+"): "+getName());
		this.dirty = _dirty;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.costing.Cost#setEffectiveDate(long)
	 */
	@Override
	public void setEffectiveDate( 
		final long _effectiveDate )
		throws InvalidValueObjectForIntervalException
	{
		costRateTables.setEffectiveDate( _effectiveDate );
	}

	/**
	 * @param effortDriven
	 */
	@Override
	public void setEffortDriven( 
		final boolean _effortDriven )
	{
		hasAssignments.setEffortDriven( _effortDriven );
	}

	/**
	 * @param emailAddress The emailAddress to set.
	 */
	@Override
	public void setEmailAddress( 
		final String _emailAddress )
	{
		this.emailAddress = _emailAddress;
	}

	@Override
	public void setExternalId( 
		final long _externalId )
	{
		this.externalId = _externalId;
	}

	@Override
	public void setFixedCost( 
		final double _fixedCost,
		final FieldContext _fieldContext )
	{
	}

	/**
	 * @param generic The generic to set.
	 */
	@Override
	public void setGeneric( 
		final boolean _generic )
	{
		this.generic = _generic;
	}

	public void setGlobalWorkVector( 
		final GroupedCalculatedValues _globalWorkVector )
	{
		this.globalWorkVector = _globalWorkVector;
	}

	/**
	 * @param _group The group to set.
	 */
	@Override
	public void setGroup( 
		final String _group )
	{
		this.group = _group;
	}

	/**
	 * @param id
	 */
	@Override
	public void setId( 
		final long _id )
	{
		hasKey.setId( _id );
	}

	/**
	 * @param inactive The active to set.
	 */
	@Override
	public void setInactive( 
		final boolean _inactive )
	{
		this.inactive = _inactive;
	}

	/**
	 * @param _initials The initials to set.
	 */
	@Override
	public void setInitials( 
		final String _initials )
	{
		this.initials = _initials;

		if (getName() == null) // for the case where the resource is created by entering initials, set name too
		{
			setName( _initials );
		}
	}

	public void setLicense( 
		final int _license )
	{
		this.license = _license;
	}

	public void setLicenseOptions( 
		final int _licenseOptions )
	{
		this.licenseOptions = _licenseOptions;
	}

	@Override
	public void setLocal( 
		final boolean _local )
	{
		hasKey.setLocal( _local );
	}

	public void setMaster( 
		final boolean _master )
	{
		this.master = _master;
	}

	/**
	 * @param materialLabel The materialLabel to set.
	 */
	@Override
	public void setMaterialLabel( 
		final String _materialLabel )
	{
		this.materialLabel = _materialLabel;
	}

	/**
	 * @param maxUnits The maxUnits to set.
	 */
	@Override
	public void setMaximumUnits( 
		final double _maxUnits )
	{
		this.maximumUnits = _maxUnits;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void setName( 
		final String _name )
	{
		hasKey.setName( _name );

		// set initials too to first character of name if initials is empty
		if ((getInitials() == null) || (getInitials().length() == 0))
		{
			if ((_name != null) && (_name.length() > 0))
			{
				setInitials( _name.substring( 0, 1 ) );
			}
		}

		if (workCalendar != null)
		{
			workCalendar.setName( _name );
		}
	}

	/**
	 * @param notes The notes to set.
	 */
	@Override
	public void setNotes( 
		final String _notes )
	{
		this.notes = _notes;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.costing.Cost#setOvertimeRate(double)
	 */
	@Override
	public void setOvertimeRate( 
		final Rate _overtimeRate )
	{
		if (isLabor() == false)
		{
			_overtimeRate.makeUnitless();
		}

		costRateTables.setOvertimeRate( _overtimeRate );
	}

	/**
	 * @param phonetics The phonetics to set.
	 */
	@Override
	public void setPhonetics( 
		final String _phonetics )
	{
		this.phonetics = _phonetics;
	}

	/**
	 * @param rbsCode The rbsCode to set.
	 */
	@Override
	public void setRbsCode( 
		final String _rbsCode )
	{
		this.rbsCode = _rbsCode;
	}

	@Override
	public void setRemainingWork( 
		final long _work,
		final FieldContext _fieldContext )
	{
		//do nothing
	}

	public void setResourcePool( 
		final ResourcePool _resourcePool )
	{
		resourcePool = _resourcePool;
	}

	@Override
	public void setResourceType( 
		final int _resourceType )
	{
		if (_resourceType == this.resourceType)
		{
			return;
		}

		boolean oldIsLabor = isLabor();
		this.resourceType = _resourceType;

		// if resource type changes to/from labor, then initialize rates
		if (oldIsLabor != isLabor())
		{
			setStandardRate( new Rate() );
			setOvertimeRate( new Rate() );

			if (!isLabor())
			{ // Non labor resources have no time unit
				getStandardRate().setTimeUnit( TimeUnit.NON_TEMPORAL );
				getOvertimeRate().setTimeUnit( TimeUnit.NON_TEMPORAL );
			}
		}
	}

	/**
	 * @param schedulingType
	 */
	@Override
	public void setSchedulingType( 
		final int _schedulingType )
	{
		hasAssignments.setSchedulingType( _schedulingType );
	}

	public void setServerMeta( 
		final Object _serverMeta )
	{
		this.serverMeta = _serverMeta;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.costing.Cost#setStandardRate(double)
	 */
	@Override
	public void setStandardRate( 
		final Rate _standardRate )
	{
		if (!isLabor())
		{
			_standardRate.makeUnitless();
		}

		costRateTables.setStandardRate( _standardRate );
	}

	/**
	 * @param id
	 */
	@Override
	public void setUniqueId( 
		final long _id )
	{
		hasKey.setUniqueId( _id );
	}

	@Override
	public final void setUserAccount( 
		final String _userAccount )
	{
		this.userAccount = _userAccount;
	}

	// these fields are not modifiable
	@Override
	public void setWork( 
		final long _work,
		final FieldContext _fieldContext )
	{
		//do nothing
	}

	@Override
	public void setWorkCalendar( 
		final WorkCalendar _workCalendar )
	{
		this.workCalendar = _workCalendar;
	}

	@Override
	public String toString()
	{
		return getName();
	}

	/**
	 * @param modified
	 */
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

	private void writeObject( 
		final ObjectOutputStream _stream )
		throws IOException
	{
		WorkCalendar wc = workCalendar;
		workCalendar = null; //automic serialization too big
		_stream.defaultWriteObject();
		workCalendar = wc;
		hasKey.serialize( _stream );
		costRateTables.serializeCompact( _stream );
		customFields.serialize( _stream );
		_stream.writeInt( hasAssignments.getSchedulingType() );
		_stream.writeBoolean( hasAssignments.isEffortDriven() );
		availabilityTable.serializeCompact( _stream );

		if (version >= 4)
		{
			_stream.writeBoolean( wc != null );

			if (wc != null)
			{
				wc.serialiseCompact( _stream );
			}
		}
	}

	static final long serialVersionUID = 273977742329L;
	private static Resource UNASSIGNED = null;
	public static final int UNASSIGNED_ID = -65535; // correponds to MSDI
	private static short DEFAULT_VERSION = 4;

	private transient CostRateTables costRateTables = new CostRateTables();
	private transient AvailabilityTable availabilityTable = new AvailabilityTable( null );
	private transient HasAssignments hasAssignments = new AssignmentContainer();
	private transient CustomFieldsImpl customFields = new CustomFieldsImpl();
	private transient GroupedCalculatedValues globalWorkVector;
	private transient HasKeyImpl hasKey;
	protected transient Object serverMeta;

	/* (non-Javadoc)
	 * @see com.projity.pm.resource.Resource#getResourceType()
	 */
	protected transient ResourcePool resourcePool;
	private transient Set<Integer> authorizedRoles;
	protected String emailAddress = "";
	protected String group = "";
	protected String initials = "";
	protected String materialLabel = "";
	protected String notes = "";
	protected String phonetics = "";
	protected String rbsCode = "";
	protected String userAccount = "";
	protected WorkCalendar workCalendar = null;
	private transient boolean dirty;
	protected boolean generic = false;
	protected boolean inactive = false;
	protected transient boolean master;
	protected double maximumUnits = 1.0D;
	protected int accrueAt = Accrual.PRORATED;
	private transient int defaultRole;
	private transient int license;
	private transient int licenseOptions;
	protected int resourceType = ResourceType.WORK;
	protected long externalId = -1;
	private short version = DEFAULT_VERSION;
}
