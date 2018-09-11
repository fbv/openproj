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

import com.projity.association.AssociationList;

import com.projity.company.ApplicationUser;
import com.projity.company.UserUtil;

import com.projity.configuration.CircularDependencyException;
import com.projity.configuration.Configuration;
import com.projity.configuration.FieldDictionary;

import com.projity.datatype.ImageLink;
import com.projity.datatype.Rate;
import com.projity.datatype.RateFormat;

import org.openproj.domain.document.Document;

import com.projity.field.CustomFields;
import com.projity.field.Field;
import com.projity.field.FieldContext;

import com.projity.graphic.configuration.HasIndicators;
import com.projity.graphic.configuration.HasResourceIndicators;

import com.projity.grouping.core.Node;
import com.projity.grouping.core.model.NodeModel;

import com.projity.interval.InvalidValueObjectForIntervalException;

import com.projity.pm.assignment.Assignment;
import com.projity.pm.assignment.TimeDistributedConstants;
import com.projity.pm.availability.AvailabilityTable;
import com.projity.pm.availability.HasAvailability;
import com.projity.pm.calendar.WorkCalendar;
import com.projity.pm.costing.CostRateTable;
import com.projity.pm.key.HasKey;
import com.projity.pm.task.AccessControlPolicy;
import com.projity.pm.task.Project;
import com.projity.pm.time.HasStartAndEnd;

import com.projity.util.Environment;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.Predicate;

import org.openproj.domain.task.Task;

/**
 * This class is used to hold resources assigned to a project.  Currently everything delegates to the global resource's values.
 * In the future, some fields such as costs and availability should be overideable on he project level.
 */
public class ResourceImpl
	implements Resource,
		HasAvailability,
		HasResourceIndicators
{
	public ResourceImpl()
	{
	}

	/**
	 * @param pool
	 */
	public ResourceImpl( 
		final EnterpriseResource _globalResource )
	{
		this.globalResource = _globalResource;
	}

	@Override
	public double actualCost( 
		final long _start,
		final long _end )
	{
		return globalResource.actualCost( _start, _end );
	}

	/**
	 * @param _start
	 * @param _end
	 * @return
	 */
	@Override
	public double actualFixedCost( 
		final long _start,
		final long _end )
	{
		return globalResource.actualFixedCost( _start, _end );
	}

	@Override
	public long actualWork( 
		final long _start,
		final long _end )
	{
		return globalResource.actualWork( _start, _end );
	}

	@Override
	public double acwp( 
		final long _start,
		final long _end )
	{
		return globalResource.acwp( _start, _end );
	}

	@Override
	public void addAssignment( 
		final Assignment _assignment )
	{
		globalResource.addAssignment( _assignment );
		addInTeam();
	}

	public void addInTeam()
	{
		if (!isRoleAllowed( getRole() ))
		{
			setRole( isUser()
				? ApplicationUser.TEAM_MEMBER
				: ApplicationUser.TEAM_RESOURCE );
		}
	}

	@Override
	public boolean applyTimesheet( 
		final Collection _fieldArray,
		final long _timesheetUpdateDate )
	{
		return globalResource.applyTimesheet( _fieldArray, _timesheetUpdateDate );
	}

	/*public void addDefaultAssignment() {
	        globalResource.addDefaultAssignment();
	}*/
	
	@Override
	public double bac( 
		final long _start,
		final long _end )
	{
		return globalResource.bac( _start, _end );
	}

	@Override
	public double baselineCost( 
		final long _start,
		final long _end )
	{
		return globalResource.baselineCost( _start, _end );
	}

	@Override
	public long baselineWork( 
		final long _start,
		final long _end )
	{
		return globalResource.baselineWork( _start, _end );
	}

	@Override
	public double bcwp( 
		final long _start,
		final long _end )
	{
		return globalResource.bcwp( _start, _end );
	}

	@Override
	public double bcws( 
		final long _start,
		final long _end )
	{
		return globalResource.bcws( _start, _end );
	}

	@Override
	public void buildReverseQuery( 
		final ReverseQuery _reverseQuery )
	{
		globalResource.buildReverseQuery( _reverseQuery );
	}

	@Override
	public long calcActiveAssignmentDuration( 
		final WorkCalendar _workCalendar )
	{
		return globalResource.calcActiveAssignmentDuration( _workCalendar );
	}

	@Override
	public void calcDataBetween( 
		final TimeDistributedConstants _type,
		final TimeIteratorGenerator _generator,
		final CalculatedValues _values )
	{
		globalResource.calcDataBetween( _type, _generator, _values );
	}

	@Override
	public Collection childrenToRollup()
	{
		return globalResource.childrenToRollup();
	}

	public void cleanClone()
	{
		globalResource.cleanClone();
	}

	@Override
	public Object clone()
	{
		try
		{
			ResourceImpl resource = (ResourceImpl)super.clone();
			resource.globalResource = (EnterpriseResource)globalResource.clone();

			return resource;
		}
		catch (CloneNotSupportedException e)
		{
			throw new InternalError();
		}
	}

	@Override
	public double cost( 
		final long _start,
		final long _end )
	{
		return globalResource.cost( _start, _end );
	}

	@Override
	public boolean fieldHideActualCost( 
		final FieldContext _fieldContext )
	{
		return globalResource.fieldHideActualCost( _fieldContext );
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
		return globalResource.fieldHideActualWork( _fieldContext );
	}

	@Override
	public boolean fieldHideAcwp( 
		final FieldContext _fieldContext )
	{
		return globalResource.fieldHideAcwp( _fieldContext );
	}

	@Override
	public boolean fieldHideBac( 
		final FieldContext _fieldContext )
	{
		return globalResource.fieldHideBac( _fieldContext );
	}

	@Override
	public boolean fieldHideBaseCalendar( 
		final FieldContext _fieldContext )
	{
		return globalResource.fieldHideBaseCalendar( _fieldContext );
	}

	@Override
	public boolean fieldHideBaselineCost( 
		final int _numBaseline,
		final FieldContext _fieldContext )
	{
		return globalResource.fieldHideBaselineCost( _numBaseline, _fieldContext );
	}

	@Override
	public boolean fieldHideBaselineWork( 
		final int _numBaseline,
		final FieldContext _fieldContext )
	{
		return globalResource.fieldHideBaselineWork( _numBaseline, _fieldContext );
	}

	@Override
	public boolean fieldHideBcwp( 
		final FieldContext _fieldContext )
	{
		return globalResource.fieldHideBcwp( _fieldContext );
	}

	@Override
	public boolean fieldHideBcws( 
		final FieldContext _fieldContext )
	{
		return globalResource.fieldHideBcws( _fieldContext );
	}

	@Override
	public boolean fieldHideCost( 
		final FieldContext _fieldContext )
	{
		return globalResource.fieldHideCost( _fieldContext );
	}

	@Override
	public boolean fieldHideCpi( 
		final FieldContext _fieldContext )
	{
		return globalResource.fieldHideCpi( _fieldContext );
	}

	@Override
	public boolean fieldHideCv( 
		final FieldContext _fieldContext )
	{
		return globalResource.fieldHideCv( _fieldContext );
	}

	@Override
	public boolean fieldHideCvPercent( 
		final FieldContext _fieldContext )
	{
		return globalResource.fieldHideCvPercent( _fieldContext );
	}

	@Override
	public boolean fieldHideEac( 
		final FieldContext _fieldContext )
	{
		return globalResource.fieldHideEac( _fieldContext );
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.availability.HasAvailability#fieldHideMaximumUnits(com.projity.field.FieldContext)
	 */
	@Override
	public boolean fieldHideMaximumUnits( 
		final FieldContext _fieldContext )
	{
		return fieldHideOvertimeRate( _fieldContext );
	}

	@Override
	public boolean fieldHideOvertimeRate( 
		final FieldContext _fieldContext )
	{
		return globalResource.fieldHideOvertimeRate( _fieldContext );
	}

	@Override
	public boolean fieldHideSpi( 
		final FieldContext _fieldContext )
	{
		return globalResource.fieldHideSpi( _fieldContext );
	}

	@Override
	public boolean fieldHideSv( 
		final FieldContext _fieldContext )
	{
		return globalResource.fieldHideSv( _fieldContext );
	}

	@Override
	public boolean fieldHideSvPercent( 
		final FieldContext _fieldContext )
	{
		return globalResource.fieldHideSvPercent( _fieldContext );
	}

	@Override
	public boolean fieldHideTcpi( 
		final FieldContext _fieldContext )
	{
		return globalResource.fieldHideTcpi( _fieldContext );
	}

	@Override
	public boolean fieldHideVac( 
		final FieldContext _fieldContext )
	{
		return globalResource.fieldHideVac( _fieldContext );
	}

	@Override
	public boolean fieldHideWork( 
		final FieldContext _fieldContext )
	{
		return globalResource.fieldHideWork( _fieldContext );
	}

	public void filterRoles( 
		final List _keys,
		final List _values )
	{
		globalResource.filterRoles( _keys, _values );
	}

	@Override
	public Assignment findAssignment( 
		final Resource _resource )
	{
		return globalResource.findAssignment( _resource );
	}

	@Override
	public Assignment findAssignment( 
		final Task _task )
	{
		return globalResource.findAssignment( _task );
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasTimeDistributedData#actualFixedCost(long, long)
	 */
	@Override
	public double fixedCost( 
		final long _start,
		final long _end )
	{
		// TODO Auto-generated method stub
		return 0;
	}

	// Delegated methods
	public static Closure forAllAssignments( 
		final Closure _visitor )
	{
		return EnterpriseResource.forAllAssignments( _visitor );
	}

	@Override
	public void forEachWorkingInterval( 
		final Closure<HasStartAndEnd> _visitor,
		final boolean _mergeWorking,
		final WorkCalendar _workCalendar )
	{
		globalResource.forEachWorkingInterval( _visitor, _mergeWorking, _workCalendar );
	}

	@Override
	public int getAccrueAt()
	{
		return globalResource.getAccrueAt();
	}

	@Override
	public double getActualCost( 
		final FieldContext fieldContext )
	{
		return globalResource.getActualCost( fieldContext );
	}

	@Override
	public double getActualFixedCost( 
		final FieldContext fieldContext )
	{
		return 0;
	}

	@Override
	public long getActualWork( 
		final FieldContext fieldContext )
	{
		return globalResource.getActualWork( fieldContext );
	}

	@Override
	public double getAcwp( 
		final FieldContext fieldContext )
	{
		return globalResource.getAcwp( fieldContext );
	}

	@Override
	public AssociationList getAssignments()
	{
		return globalResource.getAssignments();
	}

	public Set<Integer> getAuthorizedRoles()
	{
		return globalResource.getAuthorizedRoles();
	}

	/**
	 * @return Returns the availabilityTable.
	 */
	@Override
	public AvailabilityTable getAvailabilityTable()
	{
		return globalResource.getAvailabilityTable();
	}

	@Override
	public long getAvailableFrom()
	{
		return getAvailabilityTable().getAvailableFrom();
	}

	@Override
	public long getAvailableTo()
	{
		return getAvailabilityTable().getAvailableTo();
	}

	@Override
	public double getBac( 
		final FieldContext _fieldContext )
	{
		return globalResource.getBac( _fieldContext );
	}

	/**
	 * @return
	 */
	@Override
	public WorkCalendar getBaseCalendar()
	{
		return globalResource.getBaseCalendar();
	}

	@Override
	public double getBaselineCost( 
		final int _numBaseline,
		final FieldContext _fieldContext )
	{
		return globalResource.getBaselineCost( _numBaseline, _fieldContext );
	}

	@Override
	public long getBaselineWork( 
		final int _numBaseline,
		final FieldContext _fieldContext )
	{
		return globalResource.getBaselineWork( _numBaseline, _fieldContext );
	}

	@Override
	public double getBcwp( 
		final FieldContext _fieldContext )
	{
		return globalResource.getBcwp( _fieldContext );
	}

	@Override
	public double getBcws( 
		final FieldContext _fieldContext )
	{
		return globalResource.getBcws( _fieldContext );
	}

	//Methods not in EnterpriseResources
	public int getBookingType()
	{
		return bookingType;
	}

	public ImageLink getBudgetStatusIndicator()
	{
		return globalResource.getBudgetStatusIndicator();
	}

	@Override
	public double getCost( 
		final FieldContext _fieldContext )
	{
		return globalResource.getCost( _fieldContext );
	}

	@Override
	public double getCostPerUse()
	{
		return globalResource.getCostPerUse();
	}

	@Override
	public CostRateTable getCostRateTable( 
		final int _costRateIndex )
	{
		return globalResource.getCostRateTable( _costRateIndex );
	}

	@Override
	public double getCpi( 
		final FieldContext _fieldContext )
	{
		return globalResource.getCpi( _fieldContext );
	}

	@Override
	public Date getCreated()
	{
		return globalResource.getCreated();
	}

	@Override
	public double getCsi( 
		final FieldContext _fieldContext )
	{
		return globalResource.getCsi( _fieldContext );
	}

	@Override
	public double getCustomCost( 
		final int _i )
	{
		return globalResource.getCustomCost( _i );
	}

	@Override
	public long getCustomDate( 
		final int _i )
	{
		return globalResource.getCustomDate( _i );
	}

	@Override
	public long getCustomDuration( 
		final int _i )
	{
		return globalResource.getCustomDuration( _i );
	}

	@Override
	public CustomFields getCustomFields()
	{
		return globalResource.getCustomFields();
	}

	@Override
	public long getCustomFinish( 
		final int _i )
	{
		return globalResource.getCustomFinish( _i );
	}

	@Override
	public boolean getCustomFlag( 
		final int _i )
	{
		return globalResource.getCustomFlag( _i );
	}

	@Override
	public double getCustomNumber( 
		final int _i )
	{
		return globalResource.getCustomNumber( _i );
	}

	@Override
	public long getCustomStart( 
		final int _i )
	{
		return globalResource.getCustomStart( _i );
	}

	@Override
	public String getCustomText( 
		final int _i )
	{
		return globalResource.getCustomText( _i );
	}

	@Override
	public double getCv( 
		final FieldContext _fieldContext )
	{
		return globalResource.getCv( _fieldContext );
	}

	@Override
	public double getCvPercent( 
		final FieldContext _fieldContext )
	{
		return globalResource.getCvPercent( _fieldContext );
	}

	public int getDefaultRole()
	{
		return globalResource.getDefaultRole();
	}

	@Override
	public Document getDocument()
	{
		return globalResource.getDocument();
	}

	@Override
	public double getEac( 
		final FieldContext _fieldContext )
	{
		return globalResource.getEac( _fieldContext );
	}

	@Override
	public long getEarliestAssignmentStart()
	{
		return globalResource.getEarliestAssignmentStart();
	}

	/**
	 * @return
	 */
	@Override
	public long getEffectiveDate()
	{
		return globalResource.getEffectiveDate();
	}

	@Override
	public WorkCalendar getEffectiveWorkCalendar()
	{
		return globalResource.getEffectiveWorkCalendar();
	}

	@Override
	public String getEmailAddress()
	{
		return globalResource.getEmailAddress();
	}

	public int getExtendedRole()
	{ //more information for field combo

		return UserUtil.toExtendedRole( role, isUser() );
	}

	@Override
	public long getExternalId()
	{
		return globalResource.getExternalId();
	}

	@Override
	public long getFinishOffset()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @param fieldContext
	 * @return
	 */
	@Override
	public double getFixedCost( 
		final FieldContext _fieldContext )
	{
		return globalResource.getFixedCost( _fieldContext );
	}

	@Override
	public EnterpriseResource getGlobalResource()
	{
		return globalResource;
	}

	@Override
	public String getGroup()
	{
		return globalResource.getGroup();
	}

	@Override
	public long getId()
	{
		return globalResource.getId();
	}

	@Override
	public HasIndicators getIndicators()
	{
		return this;
	}

	@Override
	public String getInitials()
	{
		return globalResource.getInitials();
	}

	@Override
	public long getLastTimesheetUpdate()
	{
		return globalResource.getLastTimesheetUpdate();
	}

	@Override
	public int getLicense()
	{
		return globalResource.getLicense();
	}

	@Override
	public String getMaterialLabel()
	{
		return globalResource.getMaterialLabel();
	}

	@Override
	public double getMaximumUnits()
	{
		return getAvailabilityTable().getMaximumUnits();

		//		return globalResource.getMaximumUnits();
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public String getName()
	{
		return globalResource.getName();
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public String getName( 
		final FieldContext _context )
	{
		return globalResource.getName( _context );
	}

	@Override
	public String getNotes()
	{
		return globalResource.getNotes();
	}

	@Override
	public Rate getOvertimeRate()
	{
		return globalResource.getOvertimeRate();
	}

	@Override
	public long getParentId( 
		int _outlineNumber )
	{
		NodeModel model = getResourcePool().getResourceOutline( _outlineNumber );

		if (model == null)
		{
			return 0;
		}

		Node node = model.getParent( model.search( this ) );
		Object impl = node.getValue();

		if ((impl != null) && impl instanceof HasKey)
		{
			return ((HasKey)impl).getId();
		}

		return 0;
	}

	@Override
	public String getPhonetics()
	{
		return globalResource.getPhonetics();
	}

	@Override
	public RateFormat getRateFormat()
	{
		return globalResource.getRateFormat();
	}

	@Override
	public String getRbsCode()
	{
		return globalResource.getRbsCode();
	}

	private HashSet getReadOnlyUserFields()
	{
		if (readOnlyUserFields == null)
		{
			readOnlyUserFields = new HashSet();
			readOnlyUserFields.add( Configuration.getFieldFromId( "Field.name" ) );

			//		readOnlyUserFields.add(Configuration.getFieldFromId("Field.emailAddress"));
		}

		return readOnlyUserFields;
	}

	@Override
	public double getRemainingCost( 
		final FieldContext _fieldContext )
	{
		return globalResource.getRemainingCost( _fieldContext );
	}

	@Override
	public double getRemainingOvertimeCost()
	{
		return globalResource.getRemainingOvertimeCost();
	}

	@Override
	public long getRemainingWork( 
		final FieldContext _fieldContext )
	{
		return globalResource.getRemainingWork( _fieldContext );
	}

	@Override
	public String getResourceName()
	{
		return globalResource.getResourceName();
	}

	public ResourcePool getResourcePool()
	{
		return globalResource.getResourcePool();
	}

	@Override
	public int getResourceType()
	{
		return globalResource.getResourceType();
	}

	@Override
	public int getRole()
	{
		return role;
	}

	public ImageLink getScheduleStatusIndicator()
	{
		return globalResource.getScheduleStatusIndicator();
	}

	@Override
	public int getSchedulingType()
	{
		return globalResource.getSchedulingType();
	}

	public Object getServerMeta()
	{
		return globalResource.getServerMeta();
	}

	@Override
	public double getSpi( 
		final FieldContext _fieldContext )
	{
		return globalResource.getSpi( _fieldContext );
	}

	@Override
	public Rate getStandardRate()
	{
		return globalResource.getStandardRate();
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
		return globalResource.getSv( _fieldContext );
	}

	@Override
	public double getSvPercent( 
		final FieldContext _fieldContext )
	{
		return globalResource.getSvPercent( _fieldContext );
	}

	@Override
	public double getTcpi( 
		final FieldContext _fieldContext )
	{
		return globalResource.getTcpi( _fieldContext );
	}

	@Override
	public String getTimeUnitLabel()
	{
		return globalResource.getTimeUnitLabel();
	}

	@Override
	public int getTimesheetStatus()
	{
		return globalResource.getTimesheetStatus();
	}

	@Override
	public String getTimesheetStatusName()
	{
		return globalResource.getTimesheetStatusName();
	}

	/**
	 * @return
	 */
	public static Resource getUnassignedInstance()
	{
		if (UNASSIGNED == null)
		{
			UNASSIGNED = new ResourceImpl( (EnterpriseResource)EnterpriseResource.getUnassignedInstance() );
		}

		return UNASSIGNED;
	}

	@Override
	public long getUniqueId()
	{
		return globalResource.getUniqueId();
	}

	@Override
	public String getUserAccount()
	{
		return globalResource.getUserAccount();
	}

	@Override
	public double getVac( 
		final FieldContext _fieldContext )
	{
		return globalResource.getVac( _fieldContext );
	}

	@Override
	public long getWork( 
		final FieldContext _fieldContext )
	{
		return globalResource.getWork( _fieldContext );
	}

	@Override
	public WorkCalendar getWorkCalendar()
	{
		return globalResource.getWorkCalendar();
	}

	@Override
	public boolean hasActiveAssignment( 
		final long _start,
		final long _end )
	{
		return globalResource.hasActiveAssignment( _start, _end );
	}

//	public boolean isNew() { //TODO does this have its own ID and not the global resource id?
//		return globalResource.isNew();
//	}
//	public void setNew(boolean isNew) {
//		globalResource.setNew(isNew);
//	}
	
	/* (non-Javadoc)
	 * @see com.projity.pm.assignment.HasAssignments#hasLaborAssignment()
	 */
	@Override
	public boolean hasLaborAssignment()
	{
		return globalResource.hasLaborAssignment();
	}

	@Override
	public int hashCode()
	{
		return globalResource.hashCode();
	}

	public boolean inProgress()
	{
		for (Iterator i = getAssignments().iterator(); i.hasNext();)
		{
			Assignment a = (Assignment)i.next();

			if (a.inProgress())
			{
				return true;
			}
		}

		return false;
	}

	public static Predicate instanceofPredicate()
	{
		return new Predicate()
		{
			@Override
			public boolean evaluate( 
				Object arg0 )
			{
				return arg0 instanceof Resource;
			}
		};
	}

	@Override
	public void invalidateAssignmentCalendars()
	{
		globalResource.invalidateAssignmentCalendars();
	}

	@Override
	public Document invalidateCalendar()
	{
		return globalResource.invalidateCalendar();
	}

	@Override
	public boolean isAdministrator()
	{
		return globalResource.isAdministrator();
	}

	@Override
	public boolean isAssignedToSomeProject()
	{
		return globalResource.isAssignedToSomeProject();
	}

	@Override
	public boolean isAssignment()
	{
		return globalResource.isAssignment();
	}

	public boolean isComplete()
	{
		for (Iterator i = getAssignments().iterator(); i.hasNext();)
		{
			Assignment a = (Assignment)i.next();

			if (!a.isComplete())
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean isDefault()
	{
		return this == UNASSIGNED;
	}

	@Override
	public boolean isDirty()
	{
		return dirty;
	}

	@Override
	public boolean isEffortDriven()
	{
		return globalResource.isEffortDriven();
	}

	@Override
	public boolean isExternal()
	{
		return globalResource.isExternal();
	}

	@Override
	public boolean isGeneric()
	{
		return globalResource.isGeneric();
	}

	@Override
	public boolean isInTeam()
	{
		return (getRole() != ApplicationUser.INACTIVE) || (getAssignments().size() > 0);
	}

	@Override
	public boolean isInactive()
	{
		return globalResource.isInactive();
	}

	public boolean isInactiveLicense()
	{
		return globalResource.isInactiveLicense();
	}

	@Override
	public boolean isLabor()
	{
		return globalResource.isLabor();
	}

	@Override
	public boolean isLocal()
	{
		return globalResource.isLocal();
	}

	@Override
	public boolean isMaterial()
	{
		return globalResource.isMaterial();
	}

	@Override
	public boolean isMe()
	{
		return globalResource.isMe();
	}

	@Override
	public boolean isParent()
	{
		NodeModel model = getResourcePool().getResourceOutline();

		return model.hasChildren( model.search( this ) );
	}

	@Override
	public boolean isPendingTimesheetUpdate()
	{
		return globalResource.isPendingTimesheetUpdate();
	}

	public boolean isReadOnly()
	{
		return false; //roles
					  //return globalResource.isReadOnly();
	}

	public boolean isReadOnly( 
		final Field _f )
	{
		// roles
		boolean roleField = "Field.userRole".equals( _f.getId() );

		//if (roleField&&!isUser()) return true;
		if (!roleField && globalResource.isReadOnly())
		{
			return true;
		}

		if (!isUser())
		{
			return false;
		}

		return getReadOnlyUserFields().contains( _f );
	}

	@Override
	public boolean isReadOnlyActualWork( 
		final FieldContext _fieldContext )
	{
		return globalResource.isReadOnlyActualWork( _fieldContext );
	}

	@Override
	public boolean isReadOnlyAvailableFrom( 
		final FieldContext _fieldContext )
	{
		return true;
	}

	@Override
	public boolean isReadOnlyAvailableTo( 
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

	/**
	 * @return
	 */
	@Override
	public boolean isReadOnlyEffectiveDate( 
		final FieldContext _fieldContext )
	{
		return globalResource.isReadOnlyEffectiveDate( _fieldContext );
	}

	@Override
	public boolean isReadOnlyEffortDriven( 
		final FieldContext _fieldContext )
	{
		return globalResource.isReadOnlyEffortDriven( _fieldContext );
	}

	public boolean isReadOnlyExtendedRole( 
		final FieldContext _fieldContext )
	{ // moved out of spreadsheet model

		if (Environment.getStandAlone())
		{
			return true;
		}

		return Environment.getUser().getResourceId() == getUniqueId(); // prevents a user from losing access to his project
	}

	/**
	 * @param fieldContext
	 * @return
	 */
	@Override
	public boolean isReadOnlyFixedCost( 
		final FieldContext _fieldContext )
	{
		return globalResource.isReadOnlyFixedCost( _fieldContext );
	}

	@Override
	public boolean isReadOnlyMaterialLabel( 
		final FieldContext _fieldContext )
	{
		return globalResource.isReadOnlyMaterialLabel( _fieldContext );
	}

	@Override
	public boolean isReadOnlyRemainingWork( 
		final FieldContext _fieldContext )
	{
		return globalResource.isReadOnlyRemainingWork( _fieldContext );
	}

	@Override
	public boolean isReadOnlyWork( 
		final FieldContext _fieldContext )
	{
		return globalResource.isReadOnlyWork( _fieldContext );
	}

	public boolean isRoleAllowed( 
		final int role )
	{
		if ((role == ApplicationUser.INACTIVE) && (getAssignments().size() > 0))
		{
			return false;
		}

		return true;
	}

	public boolean isUnstarted()
	{
		for (Iterator i = getAssignments().iterator(); i.hasNext();)
		{
			Assignment a = (Assignment)i.next();

			if (a.isUnstarted())
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean isUser()
	{
		return globalResource.isUser();
	}

	@Override
	public boolean isWork()
	{
		return globalResource.isWork();
	}

	private void readObject( 
		final ObjectInputStream _stream )
		throws IOException, 
			ClassNotFoundException
	{
		_stream.defaultReadObject();
	}

	@Override
	public long remainingWork( 
		final long _start,
		final long _end )
	{
		return globalResource.remainingWork( _start, _end );
	}

	@Override
	public void removeAssignment( 
		final Assignment _assignment )
	{
		globalResource.removeAssignment( _assignment );
	}

	@Override
	public boolean renumber( 
		final boolean _localOnly )
	{
		return globalResource.renumber( _localOnly );
	}

	@Override
	public void setAccrueAt( 
		final int _accrueAt )
	{
		globalResource.setAccrueAt( _accrueAt );
	}

	@Override
	public void setActualWork( 
		final long _actualWork,
		final FieldContext _fieldContext )
	{
		globalResource.setActualWork( _actualWork, _fieldContext );
	}

	public void setAuthorizedRoles( 
		final Set<Integer> _authorizedRoles )
	{
		globalResource.setAuthorizedRoles( _authorizedRoles );
	}

	@Override
	public void setAvailableFrom( 
		final long _availableFrom )
		throws InvalidValueObjectForIntervalException
	{
		getAvailabilityTable().setAvailableFrom( _availableFrom );
	}

	@Override
	public void setAvailableTo( 
		final long _availableTo )
	{
		getAvailabilityTable().setAvailableTo( _availableTo );
	}

	/**
	 * @param baseCalendar
	 * @throws CircularDependencyException
	 */
	@Override
	public void setBaseCalendar( 
		final WorkCalendar _baseCalendar )
		throws CircularDependencyException
	{
		globalResource.setBaseCalendar( _baseCalendar );
	}

	public void setBookingType( 
		final int _bookingType )
	{
		this.bookingType = _bookingType;
	}

	@Override
	public void setCost( 
		final double _cost,
		final FieldContext _fieldContext )
	{
	}

	@Override
	public void setCostPerUse( 
		final double _costPerUse )
	{
		globalResource.setCostPerUse( _costPerUse );
	}

	@Override
	public void setCreated( 
		final Date _created )
	{
		globalResource.setCreated( _created );
	}

	@Override
	public void setCustomCost( 
		final int _index,
		final double _cost )
	{
		globalResource.setCustomCost( _index, _cost );
	}

	@Override
	public void setCustomDate( 
		final int _index,
		final long _date )
	{
		globalResource.setCustomDate( _index, _date );
	}

	@Override
	public void setCustomDuration( 
		final int _index,
		final long _duration )
	{
		globalResource.setCustomDuration( _index, _duration );
	}

	@Override
	public void setCustomFinish( 
		final int _index,
		final long _finish )
	{
		globalResource.setCustomFinish( _index, _finish );
	}

	@Override
	public void setCustomFlag( 
		final int _index,
		final boolean _flag )
	{
		globalResource.setCustomFlag( _index, _flag );
	}

	@Override
	public void setCustomNumber( 
		final int _index,
		final double _number )
	{
		globalResource.setCustomNumber( _index, _number );
	}

	@Override
	public void setCustomStart( 
		final int _index,
		final long _start )
	{
		globalResource.setCustomStart( _index, _start );
	}

	@Override
	public void setCustomText( 
		final int _index,
		final String _text )
	{
		globalResource.setCustomText( _index, _text );
	}

	public void setDefaultRole( 
		final int _defaultRole )
	{
		globalResource.setDefaultRole( _defaultRole );
	}

	@Override
	public void setDirty( 
		final boolean _dirty )
	{
		//System.out.println("ResourceImpl _setDirty("+dirty+"): "+getName());
		this.dirty = _dirty;
	}

	/**
	 * @param effectiveDate
	 */
	@Override
	public void setEffectiveDate( 
		final long _effectiveDate )
		throws InvalidValueObjectForIntervalException
	{
		globalResource.setEffectiveDate( _effectiveDate );
	}

	@Override
	public void setEffortDriven( 
		final boolean _effortDriven )
	{
		globalResource.setEffortDriven( _effortDriven );
	}

	@Override
	public void setEmailAddress( 
		final String _emailAddress )
	{
		globalResource.setEmailAddress( _emailAddress );
	}

	public void setExtendedRole( 
		final int _role )
	{
		setRole( UserUtil.toNormalRole( _role ) );
	}

	@Override
	public void setExternalId( 
		final long _id )
	{
		if (globalResource != null)
		{
			globalResource.setExternalId( _id );
		}
	}

	/**
	 * @param fixedCost
	 * @param fieldContext
	 */
	@Override
	public void setFixedCost( 
		final double _fixedCost,
		final FieldContext _fieldContext )
	{
		globalResource.setFixedCost( _fixedCost, _fieldContext );
	}

	@Override
	public void setGeneric( 
		final boolean _generic )
	{
		globalResource.setGeneric( _generic );
	}

	public void setGlobalResource( 
		final EnterpriseResource _globalResource )
	{
		this.globalResource = _globalResource;
	}

	@Override
	public void setGroup( 
		final String _group )
	{
		globalResource.setGroup( _group );
	}

	@Override
	public void setId( 
		final long _id )
	{
		if (globalResource != null)
		{
			globalResource.setId( _id );
		}
	}

	@Override
	public void setInactive( 
		final boolean _inactive )
	{
		globalResource.setInactive( _inactive );
	}

	@Override
	public void setInitials( 
		final String _initials )
	{
		globalResource.setInitials( _initials );
	}

	@Override
	public void setLocal( 
		final boolean _local )
	{
		globalResource.setLocal( _local );
	}

	@Override
	public void setMaterialLabel( 
		final String _materialLabel )
	{
		globalResource.setMaterialLabel( _materialLabel );
	}

	@Override
	public void setMaximumUnits( 
		final double _maxUnits )
	{
		getAvailabilityTable().setMaximumUnits( _maxUnits );

		//		globalResource.setMaximumUnits(maxUnits);
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void setName( 
		final String _name )
	{
		if (globalResource != null)
		{
			globalResource.setName( _name );
		}
	}

	@Override
	public void setNotes( 
		final String _notes )
	{
		globalResource.setNotes( _notes );
	}

	@Override
	public void setOvertimeRate( 
		final Rate _overtimeRate )
	{
		globalResource.setOvertimeRate( _overtimeRate );
	}

	@Override
	public void setPhonetics( 
		final String _phonetics )
	{
		globalResource.setPhonetics( _phonetics );
	}

	@Override
	public void setRbsCode( 
		final String _wbsCode )
	{
		globalResource.setRbsCode( _wbsCode );
	}

	@Override
	public void setRemainingWork( 
		final long _remainingWork,
		final FieldContext _fieldContext )
	{
		globalResource.setRemainingWork( _remainingWork, _fieldContext );
	}

	@Override
	public void setResourceType( 
		final int _resourceType )
	{
		globalResource.setResourceType( _resourceType );
	}

	public void setRole( 
		final int _role )
	{
		if (this.role != _role)
		{
			int defaultRole = getDefaultRole();

			if (globalResource != null)
			{
				if ((!isExternal() && (_role != defaultRole)) || (isExternal() && (_role != ApplicationUser.INACTIVE)))
				{
					ResourcePool resourcePool = getResourcePool();

					if (resourcePool != null)
					{
						Collection projects = resourcePool.getProjects();

						if ((projects != null) && (projects.size() > 0))
						{
							Project project = (Project)projects.iterator().next();
							Field field = FieldDictionary.getInstance().getFieldFromId( "Field.accessControlPolicy" );

							if (field != null)
							{
								field.setValue( project, project, AccessControlPolicy.RESTRICTED );
							}
						}
					}
				}
			}

			this.role = _role;

			//System.out.println("New role for "+getName()+": "+role);
		}
	}

	@Override
	public void setSchedulingType( 
		final int _schedulingType )
	{
		globalResource.setSchedulingType( _schedulingType );
	}

	public void setServerMeta( 
		final Object _object )
	{
		globalResource.setServerMeta( _object );
	}

	@Override
	public void setStandardRate( 
		final Rate _standardRate )
	{
		globalResource.setStandardRate( _standardRate );
	}

	@Override
	public void setUniqueId( 
		final long _id )
	{
		if (globalResource != null)
		{
			globalResource.setUniqueId( _id );
		}
	}

	@Override
	public final void setUserAccount( 
		final String _userAccount )
	{
		globalResource.setUserAccount( _userAccount );
	}

	@Override
	public void setWork( 
		final long _work,
		final FieldContext _fieldContext )
	{
		globalResource.setWork( _work, _fieldContext );
	}

	@Override
	public void setWorkCalendar( 
		final WorkCalendar _workCalendar )
	{
		globalResource.setWorkCalendar( _workCalendar );
	}

	@Override
	public String toString()
	{
		return globalResource.toString();
	}

	@Override
	public void updateAssignment( 
		final Assignment _modified )
	{
		globalResource.updateAssignment( _modified );
	}

	@Override
	public long work( 
		final long _start,
		final long _end )
	{
		return globalResource.work( _start, _end );
	}

	private void writeObject( 
		final ObjectOutputStream _stream )
		throws IOException
	{
		_stream.defaultWriteObject();
	}

	static final long serialVersionUID = 9485792329492L;
	private static ResourceImpl UNASSIGNED = null;
	static HashSet readOnlyUserFields = null;

	transient EnterpriseResource globalResource = null;
	
	private transient boolean dirty;
	private int bookingType = BookingType.COMMITTED;
	private transient int role;
}
