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

import com.projity.configuration.CircularDependencyException;
import com.projity.configuration.Configuration;

import com.projity.contrib.util.Log;
import com.projity.contrib.util.LogFactory;

import com.projity.datatype.Duration;
import com.projity.datatype.DurationFormat;
import com.projity.datatype.Rate;

import com.projity.exchange.Context;
import com.projity.exchange.ImportedCalendarService;

import com.projity.field.CustomFields;
import com.projity.field.FieldContext;

import com.projity.grouping.core.VoidNodeImpl;

import com.projity.options.CalendarOption;

import com.projity.pm.assignment.Assignment;
import com.projity.pm.calendar.CalendarService;
import com.projity.pm.calendar.WorkCalendar;
import com.projity.pm.calendar.WorkDay;
import com.projity.pm.calendar.WorkRange;
import com.projity.pm.calendar.WorkingCalendar;
import com.projity.pm.calendar.WorkingHours;
import com.projity.pm.resource.Resource;
import com.projity.pm.resource.ResourceImpl;
import com.projity.pm.scheduling.SchedulingType;
import com.projity.pm.task.Project;
import com.projity.pm.time.ImmutableInterval;

import com.projity.strings.Messages;

import com.projity.util.Alert;
import com.projity.util.DateTime;
import com.projity.util.Environment;
import com.projity.util.MathUtils;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.time.DateUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import net.sf.mpxj.DateRange;

/**
 * NOTE: The code has been updated to use mpxj version 4.3.0
 */
public class MPXConverter
{
	public static final String dateToXMLString( 
		final long _time )
	{
		final Calendar date = net.sf.mpxj.mspdi.DatatypeConverter.printDate( new Date( _time ) );
		return com.sun.msv.datatype.xsd.DateTimeType.theInstance.serializeJavaObject( date, null );
	}

	public static void oldToOpenProjTask( 
		net.sf.mpxj.Task mpxTask,
		org.openproj.domain.task.Task _openProjTask,
		Context context )
	{
		//TODO what about resources that are unassigned, do they have a fictive task associated?
		_openProjTask.setName( truncName( mpxTask.getName() ) );

		if (mpxTask.getWBS() != null)
		{
			_openProjTask.setWbs( mpxTask.getWBS() );
		}

		//projityTask.setUniqueId(mpxTask.getUniqueIDValue());
		_openProjTask.setNotes( mpxTask.getNotes() );
		_openProjTask.getCurrentSchedule().setStart( DateTime.gmt( mpxTask.getStart() ) ); // start needs to be correct for assignment import
		_openProjTask.getCurrentSchedule().setFinish( DateTime.gmt( mpxTask.getFinish() ) ); // finish needs to be correct for assignment import

		if (mpxTask.getID() == null)
		{ // if no task id, generate one
			mpxTask.setID( ++autoId );
		}

		_openProjTask.setId( mpxTask.getID() );
		_openProjTask.setCreated( toNormalDate( mpxTask.getCreateDate() ) );
		_openProjTask.setDuration( toProjityDuration( mpxTask.getDuration(), context ) ); // set duration without controls
		_openProjTask.setEstimated( mpxTask.getEstimated() );

		if (mpxTask.getDeadline() != null)
		{
			_openProjTask.setDeadline( DateTime.gmt( mpxTask.getDeadline() ) );
		}

		net.sf.mpxj.Priority priority = mpxTask.getPriority();

		if (priority != null)
		{
			_openProjTask.setPriority( mpxTask.getPriority().getValue() );
		}

		Number fc = mpxTask.getFixedCost();

		if (fc != null)
		{
			_openProjTask.setFixedCost( fc.doubleValue() );
		}

		Date constraintDate = DateTime.gmtDate( mpxTask.getConstraintDate() );
		final net.sf.mpxj.ConstraintType constraintType = mpxTask.getConstraintType();

		if (constraintType != null)
		{
			_openProjTask.setScheduleConstraint( constraintType.ordinal(), (constraintDate == null)
				? 0
				: constraintDate.getTime() );
		}

		net.sf.mpxj.ProjectCalendar mpxCalendar = mpxTask.getCalendar();

		if (mpxCalendar != null)
		{
			WorkCalendar cal = ImportedCalendarService.getInstance().findImportedCalendar( mpxCalendar );

			if (cal == null)
			{
				//System.out.println( "Error finding imported calendar " + mpxCalendar.getName() );
				log.severe( "Error finding imported calendar " + mpxCalendar.getName() );
			}
			else
			{
				_openProjTask.setWorkCalendar( cal );
			}
		}

		//		System.out.println("reading %" + mpxTask.getPercentageComplete().doubleValue());
		//		projityTask.setPercentComplete(mpxTask.getPercentageComplete().doubleValue());

		//use stop and not percent complete because of rounding issues - this is a little odd, but true. setting stop in assignment can set % complete
		if (mpxTask.getStop() != null)
		{
			_openProjTask.setStop( DateTime.gmt( mpxTask.getStop() ) );
		}

		_openProjTask.setLevelingDelay( toProjityDuration( mpxTask.getLevelingDelay(), context ) );

		//		Date bs = toNormalDate(mpxTask.getBaselineStart());
		_openProjTask.setEffortDriven( mpxTask.getEffortDriven() );

		if (mpxTask.getType() != null)
		{
			_openProjTask.setSchedulingType( mpxTask.getType().getValue() );
		}

		Number fixed = mpxTask.getFixedCost();

		if (fixed != null)
		{
			_openProjTask.setFixedCost( fixed.doubleValue() );
		}

		if (mpxTask.getFixedCostAccrual() != null)
		{
			_openProjTask.setFixedCostAccrual( mpxTask.getFixedCostAccrual().ordinal() );
		}

		if (!_openProjTask.isMilestone() && mpxTask.getMilestone()) // only set flag if marked - don't want the flag for 0d tasks
		{
			_openProjTask.setMarkTaskAsMilestone( true );
		}

		if (mpxTask.getEarnedValueMethod() != null)
		{
			_openProjTask.setEarnedValueMethod( mpxTask.getEarnedValueMethod().getValue() );
		}

		_openProjTask.setIgnoreResourceCalendar( mpxTask.getIgnoreResourceCalendar() );

		toProjityCustomFields( _openProjTask.getCustomFields(), mpxTask, CustomFieldsMapper.getInstance().taskMaps, context );
	}

	public static String removeInvalidChars( 
		String in )
	{ // had case of user with newlines in task names

		if (in == null)
		{
			return null;
		}

		StringBuffer inBuf = new StringBuffer(in);

		for (int i = 0; i < inBuf.length(); i++)
		{
			char c = inBuf.charAt( i );

			if ((c == '\r') || (c == '\n') || (c == '\t')) // using escape chars of the form &#x0000; is not good - they show up in MSP literally. MSP doesn't seem to support newlines anyway
			{
				inBuf.setCharAt( i, ' ' );
			}
		}

		return inBuf.toString();
	}

	public static void toMPXAssignment( 
		Assignment assignment,
		net.sf.mpxj.ResourceAssignment mpxAssignment )
	{
		//		long work = assignment.isDefault() ? 0 : assignment.getWork(null); // microsoft considers no work on default assignments
		long work = assignment.getWork( null ); // microsoft considers no work on default assignments
		mpxAssignment.setWork( MPXConverter.toMPXDuration( work ) );
		mpxAssignment.setUnits( MathUtils.roundToDecentPrecision( assignment.getUnits() * 100.0D ) );
		mpxAssignment.setRemainingWork( MPXConverter.toMPXDuration( assignment.getRemainingWork() ) ); //2007

		long delay = Duration.millis( assignment.getDelay() );

		if (delay != 0)
		{
			// mpxj uses default options when dealing with assignment delay
			CalendarOption oldOptions = CalendarOption.getInstance();
			CalendarOption.setInstance( CalendarOption.getDefaultInstance() );

			mpxAssignment.setDelay( MPXConverter.toMPXDuration( assignment.getDelay() ) );
			CalendarOption.setInstance( oldOptions );
		}

		long levelingDelay = Duration.millis( assignment.getLevelingDelay() );

		if (levelingDelay != 0)
		{
			// mpxj uses default options when dealing with assignment delay
			CalendarOption oldOptions = CalendarOption.getInstance();
			CalendarOption.setInstance( CalendarOption.getDefaultInstance() );

			mpxAssignment.setDelay( MPXConverter.toMPXDuration( assignment.getLevelingDelay() ) );
			CalendarOption.setInstance( oldOptions );
		}

		mpxAssignment.setWorkContour( net.sf.mpxj.WorkContour.getInstance( assignment.getWorkContourType() ) );
	}

	public static net.sf.mpxj.Duration toMPXDuration( 
		long duration )
	{
		return net.sf.mpxj.Duration.getInstance( Duration.getValue( duration ),
			net.sf.mpxj.TimeUnit.getInstance( Duration.getType( duration ) ) );

		//TODO put the correct formula
	}

	public static void toMPXOptions( 
		net.sf.mpxj.ProjectHeader projectHeader )
	{
		CalendarOption calendarOption = CalendarOption.getInstance();

		//		projectHeader.setDefaultHoursInDay(new Float(calendarOption.getHoursPerDay()));
		projectHeader.setMinutesPerDay( new Integer((int)(60 * calendarOption.getHoursPerDay())) );

		//		projectHeader.setDefaultHoursInWeek(new Float(calendarOption.getHoursPerWeek()));
		projectHeader.setMinutesPerWeek( new Integer((int)(60 * calendarOption.getHoursPerWeek())) );

		projectHeader.setDaysPerMonth( new Integer((int)Math.round( calendarOption.getDaysPerMonth() )) );

		projectHeader.setDefaultStartTime( calendarOption.getDefaultStartTime().getTime() );
		projectHeader.setDefaultEndTime( calendarOption.getDefaultEndTime().getTime() );
	}

	public static void toMPXProject( 
		Project project,
		net.sf.mpxj.ProjectHeader projectHeader )
	{
		WorkCalendar baseCalendar = project.getBaseCalendar();
		projectHeader.setCalendarName( baseCalendar.getName() ); // use unique id for name - this is a hack
		projectHeader.setName( project.getName() );
		projectHeader.setProjectTitle( project.getName() ); //TODO separate title and name
		projectHeader.setComments( project.getNotes() );
		projectHeader.setManager( project.getManager() );
		projectHeader.setComments( removeInvalidChars( project.getNotes() ) );
		projectHeader.setStartDate( DateTime.fromGmt( new Date(project.getStartDate()) ) );
		projectHeader.setFinishDate( DateTime.fromGmt( new Date(project.getFinishDate()) ) );
		projectHeader.setDefaultStartTime( CalendarOption.getInstance().getDefaultStartTime().getTime() );
		projectHeader.setDefaultEndTime( CalendarOption.getInstance().getDefaultEndTime().getTime() );
	}

	public static net.sf.mpxj.Rate toMPXRate( 
		Rate rate )
	{
		double value = rate.getValue() * Duration.timeUnitFactor( rate.getTimeUnit() );

		return new net.sf.mpxj.Rate(value, net.sf.mpxj.TimeUnit.getInstance( rate.getTimeUnit() ));
	}

	public static void toMPXResource( 
		final Resource _projityResource,
		final net.sf.mpxj.Resource _mpxResource )
	{
		_mpxResource.setName( removeInvalidChars( _projityResource.getName() ) );
		_mpxResource.setNotes( removeInvalidChars( _projityResource.getNotes() ) );
		_mpxResource.setAccrueAt( net.sf.mpxj.AccrueType.getInstance( _projityResource.getAccrueAt() ) );
		_mpxResource.setCostPerUse( _projityResource.getCostPerUse() );
		_mpxResource.setStandardRate( toMPXRate( _projityResource.getStandardRate() ) );
		_mpxResource.setOvertimeRate( toMPXRate( _projityResource.getOvertimeRate() ) );

		//TODO set calendar
		_mpxResource.setGroup( _projityResource.getGroup() );
		_mpxResource.setEmailAddress( _projityResource.getEmailAddress() );
		_mpxResource.setIsGeneric( _projityResource.isGeneric() ); // fix for 2024492

		_mpxResource.setInitials( _projityResource.getInitials() );
		_mpxResource.setID( (int)_projityResource.getId() );

		long uid = _projityResource.getExternalId(); // try using external id of one set

		if (uid <= 0)
		{
			uid = _projityResource.getId();
		}

		_mpxResource.setUniqueID( (int)uid ); // note using id and not unique id
		_mpxResource.setMaxUnits( _projityResource.getMaximumUnits() );

		WorkingCalendar projityCalendar = (WorkingCalendar)_projityResource.getWorkCalendar();

		if (projityCalendar != null)
		{
			// there should be a calendar, except for the unassigned instance
			net.sf.mpxj.ProjectCalendar mpxCalendar;

			try
			{
				mpxCalendar = _mpxResource.addResourceCalendar();
			}
			catch (net.sf.mpxj.MPXJException e)
			{
				e.printStackTrace();

				return;
			}

			toMpxCalendar( projityCalendar, mpxCalendar );
		}

		//TODO The follwing only work because the UID of the resource is the id and not the unique id. A big unique id value  overflows the UID element of the custom field.  It works
		// here because the id is small
		toMpxCustomFields//TODO The follwing only work because the UID of the resource is the id and not the unique id. A big unique id value  overflows the UID element of the custom field.  It works
		// here because the id is small
		( _projityResource.getCustomFields(), _mpxResource, CustomFieldsMapper.getInstance().resourceMaps );
	}

	public static void toMPXTask( 
		org.openproj.domain.task.Task _openProjTask,
		net.sf.mpxj.Task mpxTask )
	{
		mpxTask.setName( removeInvalidChars( _openProjTask.getName() ) );

		if (_openProjTask.getWbs() != null)
		{
			mpxTask.setWBS( removeInvalidChars( _openProjTask.getWbs() ) );
		}

		mpxTask.setNotes( removeInvalidChars( _openProjTask.getNotes() ) );
		mpxTask.setID( (int)_openProjTask.getId() );
		mpxTask.setUniqueID( (int)_openProjTask.getId() ); // note using id for unique id
		mpxTask.setCreateDate( _openProjTask.getCreated() );
		mpxTask.setDuration( toMPXDuration( _openProjTask.getDuration() ) ); // set duration without controls
		mpxTask.setStart( DateTime.fromGmt( new Date(_openProjTask.getStart()) ) );
		mpxTask.setFinish( DateTime.fromGmt( new Date(_openProjTask.getEnd()) ) );
		mpxTask.setCritical( Boolean.valueOf(_openProjTask.isCritical()) );
		mpxTask.setEstimated( _openProjTask.isEstimated() );
		mpxTask.setEffortDriven( _openProjTask.isEffortDriven() );
		mpxTask.setType( net.sf.mpxj.TaskType.getInstance( _openProjTask.getSchedulingType() ) );
		mpxTask.setConstraintType( net.sf.mpxj.ConstraintType.getInstance( _openProjTask.getConstraintType() ) );
		mpxTask.setConstraintDate( DateTime.fromGmt( new Date(_openProjTask.getConstraintDate()) ) );
		mpxTask.setPriority( net.sf.mpxj.Priority.getInstance( _openProjTask.getPriority() ) );
		mpxTask.setFixedCost( _openProjTask.getFixedCost() );
		mpxTask.setFixedCostAccrual( net.sf.mpxj.AccrueType.getInstance( _openProjTask.getFixedCostAccrual() ) );
		mpxTask.setMilestone( _openProjTask.isMarkTaskAsMilestone() );

		//		mpxTask.setPercentageComplete(projityTask.getPercentComplete()/100.0D);
		mpxTask.setLevelingDelay( toMPXDuration( _openProjTask.getLevelingDelay() ) );

		if (_openProjTask.getDeadline() != 0)
		{
			mpxTask.setDeadline( DateTime.fromGmt( new Date(_openProjTask.getDeadline()) ) );
		}

		mpxTask.setEarnedValueMethod( net.sf.mpxj.EarnedValueMethod.getInstance( _openProjTask.getEarnedValueMethod() ) );
		mpxTask.setIgnoreResourceCalendar( _openProjTask.isIgnoreResourceCalendar() );

		//2007
		mpxTask.setTotalSlack( toMPXDuration( _openProjTask.getTotalSlack() ) );
		mpxTask.setRemainingDuration( toMPXDuration( _openProjTask.getRemainingDuration() ) );

		if (_openProjTask.getStop() != 0)
		{
			mpxTask.setStop( DateTime.fromGmt( new Date(_openProjTask.getStop()) ) );
		}

		//		if (projityTask.getResume() != 0)
		//			mpxTask.setResume(DateTime.fromGmt(new Date(projityTask.getResume())));
		WorkCalendar cal = _openProjTask.getWorkCalendar();

		if (cal != null)
		{
			mpxTask.setCalendar( ImportedCalendarService.getInstance().findExportedCalendar( cal ) );
		}

		//	Not needed - it will be set when hierarchy is done		mpxTask.setOutlineLevel(new Integer(projityTask.getOutlineLevel()));
		toMpxCustomFields//	Not needed - it will be set when hierarchy is done		mpxTask.setOutlineLevel(new Integer(projityTask.getOutlineLevel()));
		( _openProjTask.getCustomFields(), mpxTask, CustomFieldsMapper.getInstance().taskMaps );
	}

	public static void toMPXVoid( 
		VoidNodeImpl projityVoid,
		net.sf.mpxj.Task mpxTask )
	{
		mpxTask.setID( (int)projityVoid.getId() );
		mpxTask.setUniqueID( (int)projityVoid.getId() );
		mpxTask.setNull( true );

		// below is for mpxj 2007. These values need to be set
		mpxTask.setCritical( false );
		mpxTask.setTotalSlack( toMPXDuration( 0 ) );
	}

	public static void toMpxCalendar( 
		WorkingCalendar workCalendar,
		net.sf.mpxj.ProjectCalendar _mpxProjectCalendar )
	{
		_mpxProjectCalendar.setName( workCalendar.getName() );

		//		mpx.setUniqueID((int) workCalendar.getId()); // TODO watch out for int overrun
		WorkingCalendar wc = workCalendar;

		if (workCalendar.isBaseCalendar())
		{
			wc = (WorkingCalendar)workCalendar.getBaseCalendar();
		}

		for (int i = 0; i < 7; i++)
		{ // MPX days go from SUNDAY=1 to SATURDAY=7

			WorkDay day = workCalendar.isBaseCalendar()
				? workCalendar.getDerivedWeekDay( i )
				: workCalendar.getWeekDay( i );
			net.sf.mpxj.ProjectCalendarHours mpxDay = null;
			net.sf.mpxj.Day d = net.sf.mpxj.Day.getInstance( i + 1 );

			if (day == null)
			{
//				mpx.setWorkingDay( d, net.sf.mpxj.ProjectCalendar.DEFAULT );
			}
			else
			{
				_mpxProjectCalendar.setWorkingDay( d, day.isWorking() );

				if (day.isWorking())
				{
					mpxDay = _mpxProjectCalendar.addCalendarHours( net.sf.mpxj.Day.getInstance( i + 1 ) );
					toMpxCalendarDay( day, mpxDay );
				}
			}
		}

		WorkDay[] workDays = workCalendar.getExceptionDays();

		if (workDays != null)
		{
			for (int i = 0; i < workDays.length; i++)
			{
				if ((workDays[ i ] == null) || (workDays[ i ].getStart() == 0L) || (workDays[ i ].getStart() == Long.MAX_VALUE))
				{
					continue;
				}

//				ProjectCalendarException exception = mpx.addCalendarException();
//				Date start = new Date(workDays[ i ].getStart());
//				exception.setFromDate( start );
				final Date start = new Date( workDays[ i ].getStart() );

				GregorianCalendar cal = DateTime.calendarInstance();

				// days go from 00:00 to 23:59
				cal.setTime( start );
				cal.set( Calendar.HOUR, 23 );
				cal.set( Calendar.MINUTE, 59 );

				net.sf.mpxj.ProjectCalendarException exception = _mpxProjectCalendar.addCalendarException( start, 
					DateTime.fromGmt( cal.getTime() ) );
				toMpxExceptionDay( workDays[ i ], exception );
//				exception.setWorking( workDays[ i ].isWorking() );
			}
		}

		WorkCalendar baseCalendar = workCalendar.getBaseCalendar();

		if (baseCalendar != null)
		{
			_mpxProjectCalendar.setParent( ImportedCalendarService.getInstance().findExportedCalendar( baseCalendar ) );
		}

		//mpx.setUniqueID((int)workCalendar.getUniqueId());
	}

	public static void toMpxCalendarDay( 
		WorkDay day,
		net.sf.mpxj.ProjectCalendarHours mpxDay )
	{
		if (day == null)
		{
			return;
		}

		WorkingHours workingHours = day.getWorkingHours();
		WorkRange range;

		for (int i = 0; i < 3; i++)
		{
			range = workingHours.getInterval( i );

			if (range != null)
			{
				mpxDay.addRange( new net.sf.mpxj.DateRange(DateTime.fromGmt( range.getNormalizedStartTime() ),
						DateTime.fromGmt( range.getNormalizedEndTime() )) );
			}
		}
	}

	public static void toMpxCustomFields( 
		CustomFields projityFields,
		net.sf.mpxj.FieldContainer mpx,
		CustomFieldsMapper.Maps maps )
	{
		for (int i = 0; i < maps.costMap.length; i++)
		{
			double cost = projityFields.getCustomCost( i );

			if (cost != 0.0D)
			{
				mpx.set( maps.costMap[ i ], new Double(cost) );
			}
		}

		for (int i = 0; i < maps.dateMap.length; i++)
		{
			long d = projityFields.getCustomDate( i );

			if (d != 0)
			{
				mpx.set( maps.dateMap[ i ], new Date(d) );
			}
		}

		for (int i = 0; i < maps.durationMap.length; i++)
		{
			long d = projityFields.getCustomDuration( i );

			if (Duration.millis( d ) != 0)
			{
				mpx.set( maps.durationMap[ i ], toMPXDuration( d ) );
			}
		}

		for (int i = 0; i < maps.finishMap.length; i++)
		{
			long d = projityFields.getCustomFinish( i );

			if (d != 0)
			{
				mpx.set( maps.finishMap[ i ], new Date(d) );
			}
		}

		for (int i = 0; i < maps.flagMap.length; i++)
		{
			boolean b = projityFields.getCustomFlag( i );

			if (b == true)
			{
				mpx.set( maps.flagMap[ i ], Boolean.TRUE );
			}
		}

		for (int i = 0; i < maps.numberMap.length; i++)
		{
			double n = projityFields.getCustomNumber( i );

			if (n != 0.0D)
			{
				mpx.set( maps.numberMap[ i ], new Double(n) );
			}
		}

		for (int i = 0; i < maps.startMap.length; i++)
		{
			long d = projityFields.getCustomStart( i );

			if (d != 0)
			{
				mpx.set( maps.startMap[ i ], new Date(d) );
			}
		}

		for (int i = 0; i < maps.textMap.length; i++)
		{
			String s = projityFields.getCustomText( i );

			if (s != null)
			{
				mpx.set( maps.textMap[ i ], MPXConverter.removeInvalidChars( s ) );
			}
		}
	}

	public static void toMpxExceptionDay( 
		final WorkDay _day,
		final net.sf.mpxj.ProjectCalendarException _mpxDay )
	{
		if (_day == null)
		{
			return;
		}

		WorkingHours workingHours = _day.getWorkingHours();
		WorkRange range;

		range = workingHours.getInterval( 0 );

		if (range != null)
		{
//			_mpxDay.setFromTime1( DateTime.fromGmt( range.getNormalizedStartTime() ) );
//			_mpxDay.setToTime1( DateTime.fromGmt( range.getNormalizedEndTime() ) );
			_mpxDay.addRange( new DateRange( DateTime.fromGmt( range.getNormalizedStartTime() ),
				DateTime.fromGmt( range.getNormalizedEndTime() ) ) );
		}

		range = workingHours.getInterval( 1 );

		if (range != null)
		{
//			_mpxDay.setFromTime2( DateTime.fromGmt( range.getNormalizedStartTime() ) );
//			_mpxDay.setToTime2( DateTime.fromGmt( range.getNormalizedEndTime() ) );
			_mpxDay.addRange( new DateRange( DateTime.fromGmt( range.getNormalizedStartTime() ),
				DateTime.fromGmt( range.getNormalizedEndTime() ) ) );
		}

		range = workingHours.getInterval( 2 );

		if (range != null)
		{
//			_mpxDay.setFromTime3( DateTime.fromGmt( range.getNormalizedStartTime() ) );
//			_mpxDay.setToTime3( DateTime.fromGmt( range.getNormalizedEndTime() ) );
			_mpxDay.addRange( new DateRange( DateTime.fromGmt( range.getNormalizedStartTime() ),
				DateTime.fromGmt( range.getNormalizedEndTime() ) ) );
		}
	}

	/*
	 * Because MpxDate is too big to serialize
	 */
	public static Date toNormalDate( 
		Date mpxDate )
	{
		if (mpxDate == null)
		{
			return null;
		}

		return new Date(DateTime.gmt( mpxDate ));
	}

	public static void toProjityCalendar( 
		net.sf.mpxj.ProjectCalendar _mpxProjectCalendar,
		WorkingCalendar workCalendar,
		Context context )
	{
		if (_mpxProjectCalendar.getName() != null)
		{
			workCalendar.setName( _mpxProjectCalendar.getName() );
		}

		workCalendar.setId( _mpxProjectCalendar.getUniqueID() );

		//			if (baseCalendarName.equals(ProjectCalendar.DEFAULT_BASE_CALENDAR_NAME)) {
		net.sf.mpxj.ProjectCalendar baseCalendar = null;
		WorkingCalendar standardCal = CalendarService.getInstance().getStandardInstance();

		if (_mpxProjectCalendar.isDerived() == true)
		{
			baseCalendar = _mpxProjectCalendar.getParent();

			if (baseCalendar == null)
			{
//				System.out.println("imported calendar " + mpx.getName() + " base cal name " + mpx.getBaseCalendarName() + " not found");
				baseCalendar = context.getDefaultMPXCalendar();
				_mpxProjectCalendar.setParent( baseCalendar );
			}

			WorkCalendar base = ImportedCalendarService.getInstance().findImportedCalendar( baseCalendar );

			try
			{
				if (base == null)
				{
					//System.out.println( "null base calendar" );
					log.warning( "null base calendar" );
					base = standardCal;
				}

				workCalendar.setBaseCalendar( base );
			}
			catch (CircularDependencyException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		WorkDay weekDay;
		net.sf.mpxj.Day d;

		for (int i = 1; i <= 7; i++)
		{ // MPX days go from SUNDAY=1 to SATURDAY=7
			d = net.sf.mpxj.Day.getInstance( i );

			net.sf.mpxj.ProjectCalendarHours mpxDay = _mpxProjectCalendar.getCalendarHours( d );

			if (mpxDay == null)
			{
				weekDay = null; // means use default for day

				if (_mpxProjectCalendar.isDerived() == false)
				{ // in base calendars, it means non working day

					if (_mpxProjectCalendar.isWorkingDay( d ) == false)
					{ // if base calendar is working, then this is an exceptoin

						if (standardCal.getWeekDay( i - 1 ).isWorking() == true)
						{
							weekDay = WorkDay.getNonWorkingDay();
						}
					}
				}
				else
				{
					if (!_mpxProjectCalendar.isWorkingDay( d ))
					{ // if base calendar is working, then this is an exception

						if ((baseCalendar != null) && baseCalendar.isWorkingDay( d ))
						{
							weekDay = WorkDay.getNonWorkingDay();
						}
					}
				}
			}
			else
			{
				weekDay = new WorkDay();
				toProjityCalendarDay( mpxDay, weekDay );

				if (_mpxProjectCalendar.isDerived() == false)
				{
					if (standardCal.getWeekDay( i - 1 ).hasSameWorkHours( weekDay ) == true)
					{
						weekDay = null;
					}
				}
			}

			workCalendar.setWeekDay( i - 1, weekDay );
		}

		List mpxExceptions = _mpxProjectCalendar.getCalendarExceptions();
		Iterator iter = mpxExceptions.iterator();

		while (iter.hasNext())
		{
			net.sf.mpxj.ProjectCalendarException exception = (net.sf.mpxj.ProjectCalendarException)iter.next();
			long start = DateTime.gmt( exception.getFromDate() );
			long end = DateTime.gmt( exception.getToDate() );

			for (long time = start; time < end; time = DateTime.nextDay( time ))
			{
				WorkDay day = new WorkDay(time, time); // projity does not do ranges - need an entry per date
				toProjityExceptionDay( exception, day );
				workCalendar.addOrReplaceException( day );
			}
		}

		workCalendar.removeEmptyDays(); // fixes problem with days with no working hours
	}

	// Note that there is no common interface or base class between ProjectCalendarHours and ProjectCalendarException, so this code is repeated for each
	public static void toProjityCalendarDay( 
		net.sf.mpxj.ProjectCalendarHours mpxDay,
		WorkDay day )
	{
		WorkingHours workingHours = new WorkingHours();

		if (mpxDay == null)
		{
			return;
		}

		net.sf.mpxj.DateRange mpxDateRange = mpxDay.getRange( 0 );

		if (mpxDateRange != null)
		{
			workingHours.setInterval( 0, mpxDateRange.getStart(), mpxDateRange.getEnd() );
		}

		mpxDateRange = mpxDay.getRange( 1 );

		if (mpxDateRange != null)
		{
			workingHours.setInterval( 1, mpxDateRange.getStart(), mpxDateRange.getEnd() );
		}

		mpxDateRange = mpxDay.getRange( 2 );

		if (mpxDateRange != null)
		{
			workingHours.setInterval( 2, mpxDateRange.getStart(), mpxDateRange.getEnd() );
		}

		day.setWorkingHours( workingHours );
	}

	public static void toProjityCustomFields( 
		CustomFields projityFields,
		net.sf.mpxj.FieldContainer mpx,
		CustomFieldsMapper.Maps maps,
		Context context )
	{
		for (int i = 0; i < maps.costMap.length; i++)
		{
			Number c = (Number)mpx.getCurrentValue( maps.costMap[ i ] );

			if (c != null)
			{
				projityFields.setCustomCost( i, c.doubleValue() );
			}
		}

		for (int i = 0; i < maps.dateMap.length; i++)
		{
			Date d = (Date)mpx.getCurrentValue( maps.dateMap[ i ] );

			if (d != null)
			{
				projityFields.setCustomDate( i, d.getTime() );
			}
		}

		for (int i = 0; i < maps.durationMap.length; i++)
		{
			net.sf.mpxj.Duration d = (net.sf.mpxj.Duration)mpx.getCurrentValue( maps.durationMap[ i ] );

			if (d != null)
			{
				projityFields.setCustomDuration( i, toProjityDuration( d, context ) );
			}
		}

		for (int i = 0; i < maps.finishMap.length; i++)
		{
			Date d = (Date)mpx.getCurrentValue( maps.finishMap[ i ] );

			if (d != null)
			{
				projityFields.setCustomFinish( i, d.getTime() );
			}
		}

		for (int i = 0; i < maps.flagMap.length; i++)
		{
			Boolean b = (Boolean)mpx.getCurrentValue( maps.flagMap[ i ] );

			if (b != null)
			{
				projityFields.setCustomFlag( i, b.booleanValue() );
			}
		}

		for (int i = 0; i < maps.numberMap.length; i++)
		{
			Number n = (Number)mpx.getCurrentValue( maps.numberMap[ i ] );

			if (n != null)
			{
				projityFields.setCustomNumber( i, n.doubleValue() );
			}
		}

		for (int i = 0; i < maps.startMap.length; i++)
		{
			Date d = (Date)mpx.getCurrentValue( maps.startMap[ i ] );

			if (d != null)
			{
				projityFields.setCustomStart( i, d.getTime() );
			}
		}

		for (int i = 0; i < maps.textMap.length; i++)
		{
			String s = (String)mpx.getCurrentValue( maps.textMap[ i ] );

			if (s != null)
			{
				projityFields.setCustomText( i, s );
			}
		}
	}

	/*
	 * Helper function to convert an mpx date into a projity long
	 */
	public static long toProjityDate( 
		Date mpxDate )
	{
		if (mpxDate == null)
		{
			return 0;
		}

		return mpxDate.getTime();
	}

	/**
	 * Helper function to convert an mpx duration into a projity duration
	 * @param duration
	 * @return
	 */
	public static long toProjityDuration( 
		net.sf.mpxj.Duration duration,
		Context context )
	{
		long result = 0;

		if (duration == null)
		{
			return 0;
		}

		//		MPXDuration d = duration.convertUnits(TimeUnit.HOURS);
		//		System.out.println("to projty dura " + d.getDuration() + " unit " + d.getUnits().getValue());
		//		return Duration.getInstance(d.getDuration()/3.0D,d.getUnits().getValue());
		// mpxj uses default options when importing link leads and lags
		if (context.isXml())
		{
			CalendarOption oldOptions = CalendarOption.getInstance();
			CalendarOption.setInstance( CalendarOption.getDefaultInstance() );

			result = Duration.getInstance( duration.getDuration(), duration.getUnits().getValue() );
			CalendarOption.setInstance( oldOptions );
		}
		else
		{
			result = Duration.getInstance( duration.getDuration(), duration.getUnits().getValue() );
		}

		return result;
	}

	/** Note that there is no common interface or base class between ProjectCalendarHours and ProjectCalendarException, 
	 * so this code is repeated for each
	 *
	 * @parm _mpxDay
	 * @parm _day
	 */
	public static void toProjityExceptionDay( 
		net.sf.mpxj.ProjectCalendarException _mpxDay,
		WorkDay _day )
	{
		WorkingHours workingHours = new WorkingHours();

		if (_mpxDay == null)
		{
			return;
		}
		
		net.sf.mpxj.DateRange range = _mpxDay.getRange( 0 );
		workingHours.setInterval( 0, range.getStart(), range.getEnd() );

		range = _mpxDay.getRange( 1 );
		workingHours.setInterval( 1, range.getStart(), range.getEnd() );

		range = _mpxDay.getRange( 2 );
		workingHours.setInterval( 2, range.getStart(), range.getEnd() );

		_day.setWorkingHours( workingHours );
	}

	public static void toProjityOptions( 
		net.sf.mpxj.ProjectHeader projectHeader,
		Context context )
	{
		CalendarOption calendarOption = CalendarOption.getInstance();
		calendarOption.setHoursPerDay( projectHeader.getMinutesPerDay().doubleValue() / 60.0D );
		calendarOption.setHoursPerWeek( projectHeader.getMinutesPerWeek().doubleValue() / 60.0D );

		calendarOption.setDaysPerMonth( projectHeader.getDaysPerMonth().doubleValue() );

		Date d = projectHeader.getDefaultStartTime();

		if (d != null)
		{
			GregorianCalendar defaultStart = new GregorianCalendar();
			defaultStart.setTime( d );
			calendarOption.setDefaultStartTime( defaultStart );
		}

		Date e = projectHeader.getDefaultEndTime();

		if (e != null)
		{
			GregorianCalendar defaultEnd = new GregorianCalendar();
			defaultEnd.setTime( e );
			calendarOption.setDefaultEndTime( defaultEnd );
		}
	}

	public static void toProjityProject( 
		net.sf.mpxj.ProjectHeader projectHeader,
		Project project,
		Context context )
	{
		autoId = 0;

		WorkCalendar cal = null; // = CalendarService.getInstance().findDocumentCalendar(projectHeader.getCalendarName(),project);

		String calName = projectHeader.getCalendarName();

		if (cal == null)
		{
			net.sf.mpxj.ProjectCalendar mpxCal = ImportedCalendarService.getInstance().findImportedMPXCalendar( projectHeader.getCalendarName() );
			cal = ImportedCalendarService.getInstance().findImportedCalendar( mpxCal );
		}

		try
		{
			if (cal != null)
			{
				project.setBaseCalendar( cal );
			}
		}
		catch (CircularDependencyException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String name = Messages.getString( "Text.Untitled" ); //2007 name treatment here and below

		if (context.isXml())
		{
			name = projectHeader.getProjectTitle(); // not always present

			if ((name == null) || (name.trim().length() == 0))
			{ // if no title, then use file name
				name = projectHeader.getName();

				if ((name != null) && name.endsWith( ".xml" ))
				{
					name = name.substring( 0, name.lastIndexOf( ".xml" ) );
				}
			}
		}
		else
		{
			name = projectHeader.getName();
		}

		if ((name == null) || (name.length() == 0))
		{
			name = Messages.getString( "Text.Untitled" );
		}

		// only change name if not existing now.
		if ((project.getName() == null) || project.getName().equals( "" ))
		{
			project.setName( truncName( name ) );
		}

		project.setManager( projectHeader.getManager() );
		project.setNotes( projectHeader.getComments() );

		final Project proj = project;

		// This code was moved after the end of import. Caused major bug otherwise
		//		if (projectHeader.getScheduleFrom() == ScheduleFrom.FINISH)
		//			project.setForward(false);
		Date d = projectHeader.getStatusDate();

		if (d != null)
		{
			project.setStatusDate( DateTime.gmt( d ) );
		}
	}

	/**
	 * Helper function to convert an mpx value into a projity value
	 * @param value
	 * @return
	 */
	public static Rate toProjityRate( 
		net.sf.mpxj.Rate rate )
	{
		double value = rate.getAmount() / Duration.timeUnitFactor( rate.getUnits().getValue() );

		return new Rate(value, rate.getUnits().getValue());
	}

	/**
	 * Convert an mpx resource into a projity resource
	 * @param mpxResource
	 * @param projityResource
	 * @param context
	 */
	public static void toProjityResource( 
		net.sf.mpxj.Resource mpxResource,
		Resource projityResource,
		Context context )
	{
		projityResource.setName( truncName( mpxResource.getName() ) );
		projityResource.setNotes( mpxResource.getNotes() );

		if (mpxResource.getAccrueAt() != null)
		{
			projityResource.setAccrueAt( mpxResource.getAccrueAt().ordinal() );
		}

		if (mpxResource.getCostPerUse() != null)
		{
			projityResource.setCostPerUse( mpxResource.getCostPerUse().doubleValue() );
		}

		if (mpxResource.getStandardRate() != null)
		{
			projityResource.setStandardRate( toProjityRate( mpxResource.getStandardRate() ) );
		}

		if (mpxResource.getOvertimeRate() != null)
		{
			projityResource.setOvertimeRate( toProjityRate( mpxResource.getOvertimeRate() ) );
		}

		projityResource.setGeneric( mpxResource.getGeneric() ); // fix for 2024492
		projityResource.setGroup( mpxResource.getGroup() );
		projityResource.setInitials( mpxResource.getInitials() );
		projityResource.setEmailAddress( mpxResource.getEmailAddress() );
		projityResource.setId( mpxResource.getID() );
		projityResource.setExternalId( mpxResource.getUniqueID() );

		if (mpxResource.getMaxUnits() != null)
		{
			projityResource.setMaximumUnits( mpxResource.getMaxUnits().doubleValue() );
		}

		net.sf.mpxj.ProjectCalendar cal = mpxResource.getResourceCalendar();
		WorkingCalendar workCalendar = WorkingCalendar.getInstance();
		workCalendar.setName( projityResource.getName() );

		if (cal != null)
		{
			toProjityCalendar( cal, workCalendar, context );
		}
		else
		{
			try
			{
				workCalendar.setBaseCalendar( CalendarService.getInstance().getDefaultInstance() );
			}
			catch (CircularDependencyException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		projityResource.setWorkCalendar( workCalendar );

		toProjityCustomFields( projityResource.getCustomFields(), mpxResource, CustomFieldsMapper.getInstance().resourceMaps,
			context );
	}

	/**
	 * Convert an mpx task into a openproj task
	 * @param mpxTask
	 * @param projityTask
	 */
	public static void toOpenProjTask( 
		net.sf.mpxj.Task mpxTask,
		org.openproj.domain.task.Task _openProjTask,
		Context context )
	{
		//TODO what about resources that are unassigned, do they have a fictive task associated?
		boolean isNotPodServer = Environment.isNoPodServer();

		if (!isNotPodServer)
		{
			oldToOpenProjTask( mpxTask, _openProjTask, context );

			return;
		}

		// JGao - 9/16/2009 Added leaf task which is a task that doesn't have any assignment and child task
		boolean isLeafTask = mpxTask.getResourceAssignments().isEmpty() && (mpxTask.getChildTasks().size() == 0);
		_openProjTask.setName( truncName( mpxTask.getName() ) );

		if (mpxTask.getWBS() != null)
		{
			_openProjTask.setWbs( mpxTask.getWBS() );
		}

		//projityTask.setUniqueId(mpxTask.getUniqueIDValue());
		_openProjTask.setNotes( mpxTask.getNotes() );

		Date s;
		Date f;

		if (context.isDatesWithoutTimeZone())
		{
			if (mpxTask.getStart() == null)
			{
				System.out.println( "Null start found for task " + mpxTask.getName() + " uid " + mpxTask.getUniqueID() );
				mpxTask.setStart( new Date(DateTime.midnightToday()) );
				mpxTask.setFinish( new Date(DateTime.midnightToday()) );
			}

			if (!context.isDatesPinned())
			{
				Date start = new Date(CalendarOption.getInstance().makeValidStart( mpxTask.getStart().getTime(), true ));
				mpxTask.setStart( start );

				if ((mpxTask.getDuration() == null) || (mpxTask.getDuration().getDuration() == 0))
				{
					mpxTask.setFinish( start );
				}
				else
				{
					mpxTask.setFinish( new Date(CalendarOption.getInstance().makeValidEnd( mpxTask.getFinish().getTime(), true )) );
				}
			}
			else
			{
				mpxTask.setStart( DateTime.gmtDate( mpxTask.getStart() ) );
				mpxTask.setFinish( DateTime.gmtDate( mpxTask.getFinish() ) );
			}

			_openProjTask.getCurrentSchedule().setStart( mpxTask.getStart().getTime() );
		}
		else
		{
			_openProjTask.getCurrentSchedule().setStart( DateTime.gmt( mpxTask.getStart() ) ); // start needs to be correct for assignment import/		projityTask.getCurrentSchedule().setFinish(DateTime.gmt(mpxTask.getFinish())); // finish needs to be correct for assignment import
		}

		//		projityTask.getCurrentSchedule().setStart(mpxTask.getStart().getTime()); // start needs to be correct for assignment import
		//
		//
		_openProjTask.getCurrentSchedule().setFinish( mpxTask.getFinish().getTime() ); // finish needs to be correct for assignment import

		if (mpxTask.getID() == null)
		{ // if no task id, generate one
			mpxTask.setID( ++autoId );
		}

		_openProjTask.setId( mpxTask.getID() );

		if (isNotPodServer)
		{
			long uniqueId = mpxTask.getUniqueID();

			//			System.out.println("Importing task uniqueId="+uniqueId);
			if (uniqueId > 0)
			{
				_openProjTask.setUniqueId( uniqueId );
			}
			else if (uniqueId < 0)
			{
				_openProjTask.setRoot( true );
			}
		}

		_openProjTask.setCreated( toNormalDate( mpxTask.getCreateDate() ) );

		//	_openProjTask.setRawDuration(toProjityDuration(mpxTask.getDuration(),context)); // set duration without controls
		_openProjTask.setDuration( toProjityDuration( mpxTask.getDuration(), context ) ); // set duration without controls

		//	System.out.println("Task prj-mpx " + DurationFormat.format(projityTask.getDuration()) + " " +  mpxTask.getName() + " dura " + DurationFormat.format(toProjityDuration(mpxTask.getDuration(),context)))	;
		_openProjTask.setEstimated( mpxTask.getEstimated() );

		if (mpxTask.getDeadline() != null)
		{
			_openProjTask.setDeadline( DateTime.gmt( mpxTask.getDeadline() ) );
		}

		net.sf.mpxj.Priority priority = mpxTask.getPriority();

		if (priority != null)
		{
			_openProjTask.setPriority( mpxTask.getPriority().getValue() );
		}

		Number fc = mpxTask.getFixedCost();

		if (fc != null)
		{
			_openProjTask.setFixedCost( fc.doubleValue() );
		}

		Date constraintDate = DateTime.gmtDate( mpxTask.getConstraintDate() );
		net.sf.mpxj.ConstraintType constraintType = mpxTask.getConstraintType();

		if (constraintType != null)
		{
			long ctime = (constraintDate == null)
				? 0
				: constraintDate.getTime();

			if ((constraintDate != null) && context.isDatesWithoutTimeZone())
			{
				if ((constraintType.equals( net.sf.mpxj.ConstraintType.MUST_START_ON ) == true)
				 || (constraintType.equals( net.sf.mpxj.ConstraintType.START_NO_EARLIER_THAN ) == true) 
				 ||	(constraintType.equals( net.sf.mpxj.ConstraintType.START_NO_LATER_THAN ) == true))
				{
					ctime = CalendarOption.getInstance().makeValidStart( ctime, true );
				}
				else
				{
					ctime = CalendarOption.getInstance().makeValidEnd( ctime, true );
				}
			}

			_openProjTask.setScheduleConstraint( constraintType.ordinal(), ctime );
		}

		net.sf.mpxj.ProjectCalendar mpxCalendar = mpxTask.getCalendar();

		if (mpxCalendar != null)
		{
			WorkCalendar cal = ImportedCalendarService.getInstance().findImportedCalendar( mpxCalendar );

			if (cal == null)
			{
				System.out.println( "Error finding imported calendar " + mpxCalendar.getName() );
			}
			else
			{
				_openProjTask.setWorkCalendar( cal );
			}
		}

//		System.out.println("reading %" + mpxTask.getPercentageComplete().doubleValue());
//		projityTask.setPercentComplete(mpxTask.getPercentageComplete().doubleValue());
		_openProjTask.setLevelingDelay( toProjityDuration( mpxTask.getLevelingDelay(), context ) );

//		Date bs = toNormalDate(mpxTask.getBaselineStart());
		_openProjTask.setEffortDriven( mpxTask.getEffortDriven() );

		long planWork = -1;

		if (mpxTask.getWork() != null)
		{
			planWork = toProjityDuration( mpxTask.getWork(), context );
		}

		long actualWork = -1;

		if (mpxTask.getActualWork() != null)
		{
			actualWork = toProjityDuration( mpxTask.getActualWork(), context );
		}

		// JGao 9/16/2009 If task is leaf task and actual work is greater than plan work, it is better to keep the default task type instead of fixed duration
		boolean needToKeepDefaultSchedulingType = isNotPodServer && isLeafTask && ((planWork != -1) && (actualWork > planWork)) &&
			(mpxTask.getStop() == null);

//		if (needToSetWorkToActualWork)
//			projityTask.setWork( actualWork );

		if (mpxTask.getType() != null)
		{
			_openProjTask.setSchedulingType( mpxTask.getType().getValue() );
		}
		else if (context.isUseFixedDuration() && !needToKeepDefaultSchedulingType)
		{
			_openProjTask.setSchedulingType( SchedulingType.FIXED_DURATION );

//			_openProjTask.setWork(toProjityDuration(mpxTask.getWork(), context));
		}

		//		System.out.println("sched type " + projityTask.getSchedulingType());
		if (mpxTask.getResourceAssignments().isEmpty() && (mpxTask.getChildTasks().size() == 0))
		{
			if (planWork == -1)
			{
				planWork = 0;
			}

			// JGao 9/25/2009 Added logic to surpress the fixed duration change message
			boolean batchMode = Environment.isBatchMode();
			Environment.setBatchMode( true );
			_openProjTask.setWork( planWork );
			Environment.setBatchMode( batchMode );
		}

		//use stop and not percent complete because of rounding issues - this is a little odd, but true. setting stop in assignment can set % complete
		if (mpxTask.getStop() != null)
		{
			if (isNotPodServer && !mpxTask.getResourceAssignments().isEmpty() && context.isActualsProtected())
			{
				if (!context.isShowedActualWarning())
				{ // show just once
					Alert.warn( Messages.getString( "Message.importActualsRemoved" ) );
					context.setShowedActualWarning( true );
				}
			}
			else if (!isNotPodServer || (isNotPodServer && isLeafTask))
			{
				// JGao - 9/15/2009 For the mariner integration, only set task stop if it is a leaf task without any assignments
				_openProjTask.setStop( DateTime.gmt( mpxTask.getStop() ) );
			}
		}

		if ((mpxTask.getActualStart() != null) && isNotPodServer)
		{
			_openProjTask.setActualStart( DateTime.gmt( mpxTask.getActualStart() ) );
		}

		// JGao - 9/3/2009 This section is added to deal with actuals being logged directly on task
		//		long actualWork = toProjityDuration(mpxTask.getActualWork(), context);
		if ((actualWork > 0) && isNotPodServer && isLeafTask)
		{
			FieldContext actualWorkContext = null;

			if (mpxTask.getStop() != null)
			{
				actualWorkContext = new FieldContext();
				actualWorkContext.setScripting( true );
				actualWorkContext.setForceValue( true );
				actualWorkContext.setInterval( new ImmutableInterval(DateTime.gmt( mpxTask.getActualStart() ),
						DateTime.gmt( mpxTask.getStop() )) );
			}

			_openProjTask.setActualWork( actualWork, actualWorkContext );

			if ((planWork >= 0) && (actualWork > planWork))
			{
				_openProjTask.setFixedCost( mpxTask.getActualCost().doubleValue() );
			}

			if (mpxTask.getActualFinish() != null)
			{
				_openProjTask.setComplete( true );
			}
		}

		//		Number fixed = mpxTask.getFixedCost();
		//		if (fixed != null)
		//			projityTask.setFixedCost(fixed.doubleValue());
		if (mpxTask.getFixedCostAccrual() != null)
		{
			_openProjTask.setFixedCostAccrual( mpxTask.getFixedCostAccrual().ordinal() );
		}

		if (!_openProjTask.isMilestone() && mpxTask.getMilestone()) // only set flag if marked - don't want the flag for 0d tasks
		{
			_openProjTask.setMarkTaskAsMilestone( true );
		}

		if (mpxTask.getEarnedValueMethod() != null)
		{
			_openProjTask.setEarnedValueMethod( mpxTask.getEarnedValueMethod().getValue() );
		}

		_openProjTask.setIgnoreResourceCalendar( mpxTask.getIgnoreResourceCalendar() );

		toProjityCustomFields( _openProjTask.getCustomFields(), mpxTask, CustomFieldsMapper.getInstance().taskMaps, context );
	}

	private static String truncName( 
		String name )
	{
		if (name == null)
		{
			return null;
		}

		if (name.length() > nameFieldWidth)
		{
			name = name.substring( 0, nameFieldWidth );
		}

		return name;
	}

	private static final Logger log = Logger.getLogger( MPXConverter.class.getName() );
	public static int nameFieldWidth = Configuration.getFieldFromId( "Field.name" ).getTextWidth();
	private static int autoId = 0;
}
