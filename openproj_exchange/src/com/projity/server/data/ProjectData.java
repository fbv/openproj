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
package com.projity.server.data;

import com.projity.company.ApplicationUser;

import com.projity.configuration.Configuration;

import com.projity.datatype.ImageLink;

import com.projity.field.DelegatesFields;
import com.projity.field.Field;
import com.projity.field.FieldContext;

import com.projity.pm.costing.EarnedValueCalculator;
import com.projity.pm.costing.EarnedValueIndicatorFields;
import org.openproj.domain.identity.HasName;

import com.projity.session.SessionFactory;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openproj.domain.costing.ExpenseType;


/**
 *
 */
public class ProjectData
	extends DocumentData
	implements Comparable,
		DelegatesFields,
		EarnedValueIndicatorFields,
		HasName
{
	public boolean canBeUsed()
	{
		if (lockedById <= 0)
		{
			return true;
		}

		final ApplicationUser user = SessionFactory.getInstance().getSession( false ).getUser();

		if ((user == null) /*for offline gantt*/ 
		 || (lockedById == user.getUniqueId()))
		{
			return true;
		}

		return (idleTime > allowedIdleTime);
	}

	@Override
	public int compareTo( 
		final Object _other )
	{
		return getName().compareTo( ((HasName)_other).getName() );
	}

	@Override
	public boolean delegates( 
		final Field _field )
	{
		if ((_field == getGanttSnapshotField()) 
		 || (_field == getNetworkSnapshotField()) 
		 || (_field.getId().equals( "Field.creationDate" ) == true) 
		 || (_field.getId().equals( "Field.lastModificationDate" ) == true)
		 || (_field.getId().equals( "Field.lockedByName" ) == true) 
		 || (_field.getId().equals( "Field.locked" ) == true) 
		 || (_field.getId().equals( "Field.name" ) == true) 
		 || (_field.getId().equals( "Field.scheduleStatusIndicator" ) == true) 
		 || (_field.getId().equals( "Field.statusIndicator" ) == true) 
		 || (_field.getId().equals( "Field.budgetStatusIndicator" ) == true))
		{
			return false;
		}

		return true;
	}

	@Override
	public void emtpy()
	{
		super.emtpy();

		calendar = null;
		resources = null;
		tasks = null;
	}

	public int getAccessControlPolicy()
	{
		return accessControlPolicy;
	}

	public long getAllowedIdleTime()
	{
		return allowedIdleTime;
	}

	//	public long getExternalId() {
	//		return externalId;
	//	}
	//
	//	public void setExternalId(long externalId) {
	//		this.externalId = externalId;
	//	}
	public Map getAttributes()
	{
		return attributes;
	}

	public long getAvailableImages()
	{
		return availableImages;
	}

	@Override
	public ImageLink getBudgetStatusIndicator()
	{
		Double cpi = (Double)fieldValues.get( "Field.cpi" );

		if (cpi == null)
		{
			cpi = new Double(0.0D);
		}

		return EarnedValueCalculator.getInstance().getScheduleStatusIndicator( cpi.doubleValue() );
	}

	public CalendarData getCalendar()
	{
		return calendar;
	}

	public long getCalendarId()
	{
		return calendarId;
	}

	public Date getCreationDate()
	{
		return creationDate;
	}

	@Override
	public Object getDelegatedFieldValue( 
		final Field _field )
	{
		if (fieldValues == null)
		{
			return null;
		}

		return fieldValues.get( _field.getId() );
	}

	public Collection getDistributions()
	{
		return distributions;
	}

	public String getDivision()
	{
		return division;
	}

	public ExpenseType getExpenseType()
	{
		return ExpenseType.values()[ expenseType ];
	}

	public final Map getExtraFields()
	{
		return extraFields;
	}

	public final Map getFieldValues()
	{
		return fieldValues;
	}

	public ImageLink getGanttSnapshot()
	{
		return new ImageLink("Gantt Snapshot", "gantt", ((availableImages & GANTT_SVG) == GANTT_SVG)
			? "/img/littleGantt.jpg"
			: "", "application.icon", "" + getUniqueId(), true);
	}

	public static Field getGanttSnapshotField()
	{
		if (ganttSnapshotFieldInstance == null)
		{
			ganttSnapshotFieldInstance = Configuration.getFieldFromId( "Field.ganttSnapshot" );
		}

		return ganttSnapshotFieldInstance;
	}

	public String getGroup()
	{
		return group;
	}

	public long getIdleTime()
	{
		return idleTime;
	}

	public Date getLastModificationDate()
	{
		return lastModificationDate;
	}

	public long getLockedById()
	{
		return lockedById;
	}

	public String getLockedByName()
	{
		return lockedByName;
	}

	public String getLockerInfo()
	{
		String lockerName = getLockedByName();

		if (lockerName == null)
		{
			return null;
		}

		if (getIdleTime() > allowedIdleTime)
		{
			lockerName += ("(" + "Idle: " + (getIdleTime() / 60000) + "min)");
		}

		return lockerName;
	}

	@Override
	public String getName( 
		final FieldContext _context )
	{
		return getName();
	}

	public ImageLink getNetworkSnapshot()
	{
		return new ImageLink("Network Snapshot", "network",
			((availableImages & NETWORK_SVG) == NETWORK_SVG)
			? "/img/littleNetwork.png"
			: "", "network.icon", "" + getUniqueId(), true);
	}

	public static Field getNetworkSnapshotField()
	{
		if (networkSnapshotFieldInstance == null)
		{
			networkSnapshotFieldInstance = Configuration.getFieldFromId( "Field.networkSnapshot" );
		}

		return networkSnapshotFieldInstance;
	}

	public int getProjectStatus()
	{
		return projectStatus;
	}

	public int getProjectType()
	{
		return projectType;
	}

	public final Collection getReferringSubprojectTasks()
	{
		return referringSubprojectTasks;
	}

	public Collection getResources()
	{
		return resources;
	}

	@Override
	public ImageLink getScheduleStatusIndicator()
	{
		Double spi = (Double)fieldValues.get( "Field.spi" );

		if (spi == null)
		{
			spi = new Double(0.0D);
		}

		return EarnedValueCalculator.getInstance().getScheduleStatusIndicator( spi.doubleValue() );
	}

	@Override
	public ImageLink getStatusIndicator()
	{
		Double csi = (Double)fieldValues.get( "Field.csi" );

		if (csi == null)
		{
			Double spi = (Double)fieldValues.get( "Field.spi" );
			Double cpi = (Double)fieldValues.get( "Field.cpi" );

			if ((spi == null) || (cpi == null))
			{
				csi = new Double(0.0D);
			}
			else
			{
				csi = new Double(spi.doubleValue() * cpi.doubleValue());
			}
		}

		return EarnedValueCalculator.getInstance().getStatusIndicator( csi.doubleValue() );
	}

	public Collection getTasks()
	{
		return tasks;
	}

	@Override
	public int getType()
	{
		return DataObjectConstants.PROJECT_TYPE;
	}

	public long[] getUnchangedLinks()
	{
		return unchangedLinks;
	}

	public long[] getUnchangedTasks()
	{
		return unchangedTasks;
	}

	public float getVersion()
	{
		return version;
	}

	public boolean isIncrementalDistributions()
	{
		return incrementalDistributions;
	}

	public boolean isLocked()
	{
		return (lockedByName != null) && (lockedByName.length() > 0);
	}

	public void setAccessControlPolicy( 
		final int _accessControlPolicy )
	{
		accessControlPolicy = _accessControlPolicy;
	}

	public void setAllowedIdleTime( 
		final long _allowedIdleTime )
	{
		allowedIdleTime = _allowedIdleTime;
	}

	public void setAttributes( 
		final Map _attributes )
	{
		attributes = _attributes;
	}

	public void setAvailableImages( 
		final long _availableImages )
	{
		availableImages = _availableImages;
	}

	public void setCalendar( 
		final CalendarData _calendar )
	{
		calendar = _calendar;
		setCalendarId( (_calendar == null)
			? (-1L)
			: _calendar.getUniqueId() );
	}

	public void setCalendarId( 
		final long _calendarId )
	{
		calendarId = _calendarId;
	}

	public void setCreationDate( 
		final Date _creationDate )
	{
		creationDate = _creationDate;
	}

	public void setDistributions( 
		final Collection _distributions )
	{
		distributions = _distributions;
	}

	public void setDivision( 
		final String _division )
	{
		division = _division;
	}

	public void setExpenseType( 
		final ExpenseType _expenseType )
	{
		expenseType = _expenseType.ordinal();
	}

	public final void setExtraFields( 
		final Map _extraFields )
	{
		extraFields = _extraFields;
	}

	public final void setFieldValues( 
		final Map _fieldValues )
	{
		fieldValues = _fieldValues;
	}

	public void setGroup( 
		final String _group )
	{
		group = _group;
	}

	public void setIdleTime( 
		final long _idleTime )
	{
		idleTime = _idleTime;
	}

	public void setIncrementalDistributions( 
		final boolean _incrementalDistributions )
	{
		incrementalDistributions = _incrementalDistributions;
	}

	public void setLastModificationDate( 
		final Date _lastModificationDate )
	{
		lastModificationDate = _lastModificationDate;
	}

	public void setLockedById( 
		final long _lockedById )
	{
		lockedById = _lockedById;
	}

	public void setLockedByName( 
		final String _lockedByName )
	{
		lockedByName = _lockedByName;
	}

	public void setProjectStatus( 
		final int _projectStatus )
	{
		projectStatus = _projectStatus;
	}

	public void setProjectType( 
		final int _projectType )
	{
		projectType = _projectType;
	}

	public final void setReferringSubprojectTasks( 
		final Collection _referringSubprojectTasks )
	{
		referringSubprojectTasks = _referringSubprojectTasks;
	}

	public void setResources( 
		final List<ResourceData> _resources )
	{ 
		resources = _resources;
	}

	public void setTasks( 
		final Collection _tasks )
	{
		tasks = _tasks;
	}

	public void setUnchangedLinks( 
		final long[] _unchangedLinks )
	{
		unchangedLinks = _unchangedLinks;
	}

	public void setUnchangedTasks( 
		final long[] _unchangedTasks )
	{
		unchangedTasks = _unchangedTasks;
	}

	public void setVersion( 
		final float _version )
	{
		version = _version;
	}

	static final long serialVersionUID = 722537477839L;

	//web
	public static final long GANTT = 1L;
	public static final long NETWORK = 16L;
	public static final long SVG = 32L;
	public static final long PNG = 64L;
	public static final long PDF = 128L;

	//database
	public static final long GANTT_SVG = GANTT | SVG;
	public static final long GANTT_PDF = GANTT | PDF;
	public static final long GANTT_PNG = GANTT | PNG;
	public static final long NETWORK_SVG = NETWORK | SVG;
	public static final long NETWORK_PDF = NETWORK | PDF;
	public static final long NETWORK_PNG = NETWORK | PNG;
	public static final SerializedDataObjectFactory FACTORY = new SerializedDataObjectFactory()
	{
		@Override
		public SerializedDataObject createSerializedDataObject()
		{
			return new ProjectData();
		}
	};

	private static Field ganttSnapshotFieldInstance = null;
	private static Field networkSnapshotFieldInstance = null;
	protected CalendarData calendar;
	protected Collection distributions;
	protected Collection referringSubprojectTasks;
	protected List<ResourceData> resources;
	protected Collection tasks;
	protected Date creationDate;
	protected Date lastModificationDate;

	//protected transient long externalId=-1L;
	protected transient Map attributes;
	protected Map extraFields;
	protected Map fieldValues;
	protected String division;
	protected String group;
	protected String lockedByName;
	protected long[] unchangedLinks;
	protected long[] unchangedTasks;
	protected boolean incrementalDistributions;
	protected float version = 1.2f;
	protected int accessControlPolicy;
	protected int expenseType;
	protected int projectStatus;
	protected int projectType;
	protected long allowedIdleTime;
	protected long availableImages = GANTT_SVG | GANTT_PNG | NETWORK_SVG | NETWORK_PNG;
	protected long calendarId = -1;
	protected long idleTime;
	protected long lockedById;
}
