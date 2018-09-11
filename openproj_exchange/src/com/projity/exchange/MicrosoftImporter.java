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
package com.projity.exchange;

import com.projity.association.InvalidAssociationException;

import com.projity.configuration.CircularDependencyException;
import com.projity.configuration.Settings;

import com.projity.contrib.util.Log;
import com.projity.contrib.util.LogFactory;

import com.projity.datatype.Duration;
import com.projity.datatype.Rate;

import com.projity.exchange.ResourceMappingForm.MergeField;

import com.projity.field.FieldContext;

import com.projity.functor.StringList;

import com.projity.grouping.core.Node;
import com.projity.grouping.core.NodeFactory;
import com.projity.grouping.core.model.NodeModel;

import com.projity.job.Job;
import com.projity.job.JobCanceledException;
import com.projity.job.JobRunnable;

import com.projity.options.CalendarOption;

import com.projity.pm.assignment.Assignment;
import com.projity.pm.assignment.AssignmentService;
import com.projity.pm.assignment.contour.AbstractContour;
import com.projity.pm.assignment.contour.ContourTypes;
import com.projity.pm.calendar.CalendarService;
import com.projity.pm.calendar.WorkingCalendar;
import com.projity.pm.dependency.Dependency;
import com.projity.pm.dependency.DependencyService;
import org.openproj.domain.identity.HasId;
import com.projity.pm.resource.EnterpriseResource;
import com.projity.pm.resource.Resource;
import com.projity.pm.resource.ResourceImpl;
import com.projity.pm.resource.ResourcePool;
import com.projity.pm.task.Project;
import com.projity.pm.task.ProjectFactory;
import com.projity.pm.time.ImmutableInterval;

import com.projity.server.access.ErrorLogger;
import com.projity.server.data.DataObject;
import com.projity.server.data.EnterpriseResourceData;
import com.projity.server.data.MPXConverter;
import com.projity.server.data.MSPDISerializer;
import com.projity.server.data.Serializer;
//import com.projity.server.data.mspdi.ModifiedMSPDIReader;

import com.projity.session.Session;
import com.projity.session.SessionFactory;

import com.projity.strings.Messages;

import com.projity.util.Alert;
import com.projity.util.DateTime;
import com.projity.util.Environment;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.Predicate;

import org.jdesktop.swing.calendar.DateSpan;

import java.io.FileInputStream;
import java.io.InputStream;

import java.net.URL;
import java.net.URLDecoder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.text.AbstractDocument.Content;
import org.openproj.domain.task.Task;


/**
 * This class is based on the project mpxj http://www.tapsterrock.com/mpxj
 * The enumerated types in openproj currently correspond exactly to the types in mpx, so there is no need to convert them.
 * However, if the openproj enumerations change, it will be necessary to map them to mpx types.
 *
 */
public class MicrosoftImporter
	extends ServerFileImporter
{
	public MicrosoftImporter()
	{
		//System.out.println( "-------MicrosoftImporter()" );
		log.info( "MicrosoftImporter()" );
	}

	/**
	 * Add a new openproj assignment given an mpx assignment
	 * @param mpxAssignment
	 * @return new assignment
	 */
	protected Assignment addAssignment( 
		final net.sf.mpxj.ResourceAssignment _mpxAssignment )
	{
		final Task task = myTaskMap.get( _mpxAssignment.getTask() );
		final boolean isNotPodServer = Environment.isNoPodServer();

		// for some reason, mpxj returns a units value that is multiplied by 100
		if (task == null)
		{
			//System.out.println( "null task in assignment - dummy is " + (_mpxAssignment.getTask() == this.dummyFirstTask) );
			log.severe( "null task in assignment - dummy is " + (_mpxAssignment.getTask() == this.dummyFirstTask) );
			return null;
		}

		com.projity.pm.resource.Resource resource;

		if (_mpxAssignment.getResourceUniqueID().intValue() == EnterpriseResource.UNASSIGNED_ID) 
		{
			// use unassigned id.  Relevant for msdi imports
			resource = ResourceImpl.getUnassignedInstance();
		}
		else
		{
			resource = myResourceMap.get( (long)_mpxAssignment.getResourceUniqueID() );
		}

		if (resource == null)
		{
			//System.out.println( "null resource in assignment - ignored. resource id was " + _mpxAssignment.getResourceUniqueID() );
			log.warning( "null resource in assignment - ignored. resource id was " + _mpxAssignment.getResourceUniqueID() );
			return null;
		}

		double assignmentUnit = _mpxAssignment.getUnits().doubleValue() / assignmentPercentFactor();

		//The delay will be calculated by the setStart below.  This code is not needed: MPXConverter.toProjityDuration(mpxAssignment.getDelay(),context) //TODO maybe use default calendar options as in dependency import
		Assignment assignment = AssignmentService.getInstance().newAssignment( task, resource, assignmentUnit, 0, null );

		Date dtStart = _mpxAssignment.getStart();
		long start = 0;

		if (dtStart != null)
		{
			start = DateTime.gmt( dtStart );
		}

		Date dtFinish = _mpxAssignment.getFinish();
		long end = 0;

		if (dtFinish != null)
		{
			end = DateTime.gmt( dtFinish );
		}

		List timephasedList = null;

		if (context.isMariner() == false)
		{
			timephasedList = getTimephasedList( _mpxAssignment );
		}

		// JGao - 9/10/2009 In case the assignment starts and end on non working days, need to add them as working time
		if ((dtStart != null) && context.isMariner() && !context.isDatesPinned())
		{
			assignment.addWorkingTimeIfRequired( start );
		}

		if ((dtFinish != null) && context.isMariner() && !context.isDatesPinned())
		{
			assignment.addWorkingTimeIfRequired( end );
		}

		if (dtStart != null)
		{
			assignment.setStart( start ); // will take care of delay
		}

		if ((dtFinish != null) && (timephasedList == null))
		{
			assignment.setEnd( end ); // in case assignment doesn't span full task, must truncate it
		}

		if (timephasedList == null)
		{
			// JGao - 9/2/2009 Set the work on assignment to get the finish date correct
			// JGao 9/27/2009 If work is null for Mariner, it means that there is no work. Need to set the unit to zero to make it happen.
			net.sf.mpxj.Duration mppWork = _mpxAssignment.getWork();

			if (mppWork == null)
			{
				if (context.isMariner())
				{
					assignment.getDetail().setRate( new Rate(0.0, assignment.getRate().getTimeUnit()) );
				}
			}
			else
			{
				// JGao 9/24/2009 Added this for the rounding problem with mpp import. For some reason, the value is not quite matching what's in
				// mpp. For example, 16 hrs could be 16.002. Since work is set as millisconds, this could cause the schedule to shift unexpectedly.
				net.sf.mpxj.TimeUnit workUnitType = mppWork.getUnits();

				if ((workUnitType == net.sf.mpxj.TimeUnit.HOURS) || (workUnitType == net.sf.mpxj.TimeUnit.ELAPSED_HOURS))
				{
					mppWork = net.sf.mpxj.Duration.getInstance( Math.round( mppWork.getDuration() * 100.0 ) / 100.0, workUnitType );
				}

				long importedWork = Duration.millis( MPXConverter.toProjityDuration( mppWork, context ) );

				if (isNotPodServer && !context.isDatesPinned())
				{
					assignment.adjustRemainingWork( (long)(importedWork / assignmentUnit) );
				}
			}

			// JGao - 9/14/2009	If timesheet disabled, need to set the actual work on the assignment
			if ((_mpxAssignment.getActualWork() != null) && isNotPodServer && !context.isActualsProtected() &&
					!context.isDatesPinned())
			{
				assignment.setActualWork( MPXConverter.toProjityDuration( _mpxAssignment.getActualWork(), context ), null );
			}
		}

		net.sf.mpxj.WorkContour contour = _mpxAssignment.getWorkContour();

		if (contour == null)
		{
			assignment.setWorkContourType( ContourTypes.FLAT );
		}
		else if (contour.getValue() == net.sf.mpxj.WorkContour.CONTOURED.ordinal())
		{
			assignment.makeContourPersonal();
		}
		else
		{
			assignment.setWorkContourType( contour.getValue() );
		}

		if (timephasedList != null)
		{
			// TODO: fix time phased list
			//ModifiedMSPDIReader.readAssignmentBaselinesAndTimephased( assignment, timephasedList );
		}

		if (context.isMariner() && !context.isDatesPinned())
		{
			// JGao 9/26/2009 If the unit is not 100% as that of the default unit, need to make the contour personal
			// to have the work and unit match.
			if ((assignmentUnit != 1.0) && !assignment.getWorkContour().isPersonal())
			{
				assignment.makeContourPersonal();
			}
		}

		return assignment;
	}

	/**
	 * Add a new dependency into the openproj model based on mpx task and predecessor
	 * @param _mpxTask
	 * @param _mpxRelation
	 * @return
	 */
	private Dependency addDependency( 
		final net.sf.mpxj.Task _mpxTask,
		final net.sf.mpxj.Relation _mpxRelation )
	{
		final org.openproj.domain.task.Task predecessor = myTaskMap.get( mpx.getTaskByUniqueID( _mpxRelation.getTargetTask().getUniqueID() ) );
		final org.openproj.domain.task.Task successor = myTaskMap.get( _mpxTask );

		if ((predecessor == null) || (successor == null))
		{
			//System.out.println( "invalid dependency -pred task not found - maybe duplicate task UIDs" //$NON-NLS-1$
			//	 +" pred UID=" + _mpxRelation.getSourceTask().getUniqueID() );
			log.warning( "invalid dependency -pred task not found - maybe duplicate task UIDs" 
				 +" pred UID=" + _mpxRelation.getSourceTask().getUniqueID() );

			return null;
		}

		Dependency dependency;

		try
		{
			dependency = DependencyService.getInstance().newDependency( predecessor, successor, _mpxRelation.getType().ordinal(),
				MPXConverter.toProjityDuration( _mpxRelation.getLag(), context ), null );
		}
		catch (InvalidAssociationException e)
		{
			log.severe( "Error adding dependency:" + e.getMessage() ); //$NON-NLS-1$
			dependency = null;
		}

		return dependency;
	}

	protected double assignmentPercentFactor()
	{
		return 100.0;
	}

	private Project convertToProjity()
		throws Exception
	{
		Project importInto = ProjectFactory.getInstance().getImportInto();

		/*DEF165642: Projity: Imported task dependency not saving
		 * this is happening because the serializer looks at the projectIds on each task (and dependency)
		 * If they don't match the currentId, they are skipped.
		 * This is probably not the best place to fix this, but it works. --TAF090792*/
		if (importInto != null)
		{
			getProject().setUniqueId( importInto.getUniqueId() );
			getProject().setId( importInto.getId() );
		}

		// JGao 9/16/2009 If importing from Mariner, the acutals should have already been set
		// Need to skip it here, otherwise, it will be reset
		if (context.isMariner() == false)
		{
			boolean actualsProtected = (importInto != null) && importInto.isActualsProtected();
			getProject().setActualsProtected( actualsProtected );
			context.setActualsProtected( actualsProtected );
		}

		log.info( "import options" );
		importOptions();

		setProgress( 0.45f );

//		log.info( "import calendars" );
//		if (isSkipCalendars() == false)
//		{
//			importCalendars();
//		}

		setProgress( 0.5f );
		log.info( "import tasks" );
		importTasks();
		
		setProgress( 0.6f );
		log.info( "import project fields" );
		importProjectFields();
		
		setProgress( 0.7f );
		log.info( "import dependencies" );
		importDependencies();
		
		setProgress( 0.8f );
		log.info( "import hierarchy" );
		importHierarchy();
		
		setProgress( 0.85f );
		log.info( "import assignments" );
		importAssignments();

		setProgress( 0.9f );
		importExtra();
		log.info( "about to initialize" );

		if (getProject().getName() == null)
		{
			getProject().setName( "error - name not set on import" );
		}

//			CalendarService.getInstance().renameImportedBaseCalendars(getProject().getName());
		try
		{
			getProject().initialize( false, false ); // will run critical path
		}
		catch (RuntimeException e)
		{
			if (e.getMessage() == CircularDependencyException.RUNTIME_EXCEPTION_TEXT)
			{
				Environment.setImporting( false ); // will avoid certain popups
				Alert.error( e.getMessage() );
				mpx = null;
				setProject( null );
				throw new Exception(e.getMessage());
			}
		}

		//project.setGroupDirty(!Environment.getStandAlone());
		if (!Environment.getStandAlone())
		{
			getProject().setAllDirty();
		}

		getProject().setBoundsAfterReadProject();

		if ((mpx.getProjectHeader().getScheduleFrom() == net.sf.mpxj.ScheduleFrom.FINISH) && !context.isMariner())
		{
			getProject().setForward( false );
		}

		Environment.setImporting( false ); // will avoid certain popups
		setProgress( 1.0f );
		mpx = null;

		// remove reference
		getProject
		// remove reference
		().setWasImported( true );

		return getProject();
	}

	/**
	 * Currently not implemented
	 */
	public Job getExportFileJob()
	{
		Session session = SessionFactory.getInstance().getLocalSession();
		Job job = new Job(session.getJobQueue(), "exportFile", "Exporting...", true); //$NON-NLS-1$ //$NON-NLS-2$
		job.addRunnable( new JobRunnable("Local: export", 1.0f)
		{ 
			public Object run()
				throws Exception
			{
				MSPDISerializer serializer = new MSPDISerializer();
				serializer.setJob( this );
				serializer.saveProject( getProject(), fileName );

				return null;
			}
		} );

		//session.schedule(job);
		return job;
	}

	/**
	 * This method imports an entire mpx, mpp or xml file
	 *
	 * @param filename
	 *            name of the input file
	 * @throws Exception
	 *             on file read error
	 */
	@Override
	public Job getImportFileJob()
	{
		//System.out.println( "MicrosoftImporter.getImportFileJob()" );
		log.entering( "MicrosoftImporter", "getImportFileJob" );

		subprojects = new ArrayList();
		errorDescription = null;
		lastException = null;

		Session session = SessionFactory.getInstance().getSession( resourceMapping == null );
		Job job = new Job(session.getJobQueue(), "importFile", Messages.getString( "MicrosoftImporter.Importing" ), true);
		job.addRunnable( 
			new JobRunnable(Messages.getString( "MicrosoftImporter.PrepareResources" ), 1.0f)
		{ 
			@Override
			public Object run()
				throws Exception
			{
				MicrosoftImporter.this.jobRunnable = this;
				parse();

				return null;
			}
		} );
		
		job.addSwingRunnable( 
			new JobRunnable( "Import resources", 1.0f )
		{ 
			@Override
			public Object run()
				throws Exception
			{
				final ResourceMappingForm form = getResourceMapping();

				if ((form != null) && form.isLocal()) 
				{
					//if form==null we are in a case were have no server access. popup not needed
					if (!job.okCancel( Messages.getString( "Message.ServerUnreacheableReadOnlyProject" ), true ))
					{
						setProgress( 1.0f );
						errorDescription = ABORT;
						Environment.setImporting( false ); // will avoid certain popups
						throw new Exception(ABORT);
					}
				}

				log.info( "import calendars" );
				if (isSkipCalendars() == false)
				{
					importCalendars();
				}

				log.info( "import resources" );

				if (importResources() == false)
				{
					setProgress( 1.0f );
					errorDescription = ABORT;
					Environment.setImporting( false ); // will avoid certain popups
					throw new Exception(ABORT);
				}

				//setProgress(0.4f);
				setProgress( 1f );

				return null;
			}
		} );

		job.addRunnable( 
			new JobRunnable( "Finish import", 1.0f )
		{ 
			@Override
			public Object run()
				throws Exception
			{
				return convertToProjity();
			}
		} );

//    	job.addSwingRunnable(new JobRunnable("Local: addProject"){
//    		public Object run() throws Exception{
//    			Project project=(Project)getPreviousResult();
//    			if (project!=null) projectFactory.addProject(project,true);
//    			return project;
//    		}
//    	});
//
//		session.schedule(job);

		return job;
	}

	public HashMap<Long,Resource> getResourceMap()
	{
		return myResourceMap;
	}

	protected InputStream getStream()
		throws Exception
	{
		//System.out.println( "MicrosoftImporter.getStream()" );
		log.entering( "MicrosoftImporter", "getStream" );

		InputStream stream = null;

		if (fileName.startsWith( "http" ))
		{ //$NON-NLS-1$
			fileName = URLDecoder.decode( fileName, "UTF-8" ); //$NON-NLS-1$

			if (fileName.startsWith( "http://" ))
			{ //$NON-NLS-1$
				stream = new URL(fileName).openStream();
			}
		}
		else
		{
			String originalName = fileName;

			for (int i = 0; i < fileSuffixes.length; i++)
			{
				fileName = originalName + fileSuffixes[ i ];

				try
				{
					stream = new FileInputStream(fileName);

					break;
				}
				catch (java.io.FileNotFoundException e)
				{;
				}
			}
		}

		return stream;
	}

	private List getTimephasedList( 
		final net.sf.mpxj.ResourceAssignment _mpxAssignment )
	{
		if (context.isXml() == false) 
		{
			// mpp format does not have timephased data
			return null;
		}

		// TODO: fix time phased list
//		return ((net.sf.mpxj.mspdi.MSPDIReader /*ModifiedMSPDIReader*/)reader).getTimephasedList( _mpxAssignment );
		return null;
	}

	/**
	 * Import mpx assignments into projity model
	 *
	 */
	protected void importAssignments()
	{
		List allAssignments = mpx.getAllResourceAssignments();
		Iterator iter = allAssignments.iterator();
		net.sf.mpxj.ResourceAssignment assignment;

		while (iter.hasNext() == true)
		{
			assignment = (net.sf.mpxj.ResourceAssignment)iter.next();

			if (assignment.getUnits() == null)
			{
				assignment.setUnits( 100.0 );
			}

			if (context.isNoAssignmentDelays())
			{
				assignment.setStart( null );
			}

			addAssignment( assignment );
		}
	}

	protected void importCalendars()
	{
		log.entering( "MicrosoftImporter", "importCalendars" );
		
		List<net.sf.mpxj.ProjectCalendar> calendars = mpx.getBaseCalendars();
		final ImportedCalendarService service = ImportedCalendarService.getInstance();
		String importedDuplicateText = " " + Settings.LEFT_BRACKET + mpx.getProjectHeader().getProjectTitle() +
			Settings.RIGHT_BRACKET;

		Iterator<net.sf.mpxj.ProjectCalendar> iter = calendars.iterator();
		while (iter.hasNext() == true)
		{
			net.sf.mpxj.ProjectCalendar cal = iter.next();

			if (net.sf.mpxj.ProjectCalendar.DEFAULT_BASE_CALENDAR_NAME.equals( cal.getName() ) == true) 
			{
				// bug fix - name can be null
				context.setDefaultMPXCalendar( cal );
			}

			final WorkingCalendar workCalendar = WorkingCalendar.getStandardBasedInstance();
			MPXConverter.toProjityCalendar( cal, workCalendar, context );

			if (CalendarService.findBaseCalendar( workCalendar.getName() ) != null)
			{ 
				// if calendar with that name exists already, change the name of this one

				//TODO eventually avoid duplicating if calendars are truly identical
				workCalendar.setName( workCalendar.getName() + importedDuplicateText );
			}

			service.addImportedCalendar( workCalendar, cal );
		}
	}

	/**
	 * Import dependencies. Must be done after importing tasks
	 *
	 * @throws Exception
	 */
	public void importDependencies()
		throws Exception
	{
		log.entering( "MicrosoftImporter", "importDependencies" );
		
		final Iterator<net.sf.mpxj.Task> taskIter = allTasks.iterator();

		// mpxj uses default options when importing link leads and lags, even when mpp format
		CalendarOption oldOptions = CalendarOption.getInstance();
		CalendarOption.setInstance( CalendarOption.getDefaultInstance() );

		// go thru all tasks
		while (taskIter.hasNext() == true)
		{ 

			net.sf.mpxj.Task task = taskIter.next();

			if (task == dummyFirstTask)
			{
				continue;
			}

			if (task == null)
			{
				//System.out.println( "null task" );
				log.warning( "null task" );
			}

			List rels = task.getPredecessors();

			if (rels != null)
			{
				Iterator relIter = rels.iterator();

				while (relIter.hasNext())
				{ // go thru all predecessors

					net.sf.mpxj.Relation relation = (net.sf.mpxj.Relation)relIter.next();
					addDependency( task, relation );
				}
			}
		}

		CalendarOption.setInstance( oldOptions );
	}

	protected void importExtra()
	{
	}

	/**
	 * Import the hierarchy information into the projity model
	 * reflecting the parent-child relationships between the tasks.
	 *
	 */
	private void importHierarchy()
	{
		final List childTasks = mpx.getChildTasks();
		Node parentNode = null;

		//add missing summary task
		if (ADD_SUMMARY_TASK && !getProject().isSummaryTaskEnabled() && false)
		{ //needed for import, add other conditions?

			org.openproj.domain.task.Task openProjTask = getProject().newTaskInstance( false );
			openProjTask.setName( Messages.getString( "Text.DefaultSummaryTaskName" ) );
			openProjTask.setUniqueId( DataObject.SUMMARY_UNIQUE_ID );
			openProjTask.setOwningProject( getProject() );
			openProjTask.setProjectId( getProject().getUniqueId() );
			parentNode = NodeFactory.getInstance().createNode( openProjTask ); // get a node for this task
			getProject().addToDefaultOutline( null, parentNode );
			getProject().setSummaryTaskEnabled( true );
		}

		Iterator iter = childTasks.iterator();
		net.sf.mpxj.Task task = null;

		while (iter.hasNext() == true)
		{
			task = (net.sf.mpxj.Task)iter.next();
			importHierarchy( task, parentNode );
		}

		//renumber ids
		getProject//renumber ids
		().getTaskOutline().getHierarchy().visitAll( new Closure()
			{
				int id = 1;

			@Override
				public void execute( 
					Object o )
				{
					Node node = (Node)o;

					if (node.getValue() instanceof HasId)
					{ //renumber

						HasId impl = (HasId)node.getValue();
						impl.setId( id++ ); //if id=0 means id not used
					}
				}
			} );

		insertTaskVoids();

		//		((AbstractMutableNodeHierarchy)getProject().getTaskOutline().getHierarchy()).dump();
	}

	/**
	 * Helper method called recursively to import the hierarchy of child tasks.
	 *
	 * @param task
	 *            task whose children are to be imported
	 * @param parent
	 *            the parent of the task
	 */
	private void importHierarchy( 
		final net.sf.mpxj.Task _task,
		final Node _parentNode )
	{
		Node taskNode = null;
		final org.openproj.domain.task.Task openProjTask = myTaskMap.get( _task );

		if (openProjTask != null)
		{
			taskNode = NodeFactory.getInstance().createNode( openProjTask ); // get a node for this task
			getProject().addToDefaultOutline( _parentNode, taskNode );
		}

		final Iterator<net.sf.mpxj.Task> iter = _task.getChildTasks().iterator();
		while (iter.hasNext() == true)
		{
			final net.sf.mpxj.Task child = iter.next();
			importHierarchy( child, taskNode );
		}
	}

	/**
	 * This method imports all resources defined in the file into the projity model
	 *
	 */
	protected void importLocalResources()
	{
		Resource projityResource;
		ResourcePool resourcePool = getProject().getResourcePool();
		getProject().setLocal( !Environment.isNoPodServer() );
		resourcePool.setLocal( true );
		resourcePool.setMaster( false );
		resourcePool.updateOutlineTypes();

		Iterator<net.sf.mpxj.Resource> iter = allResources.iterator();
		while (iter.hasNext() == true)
		{
			final net.sf.mpxj.Resource resource = iter.next();
			makeValidResourceId( resource );

			if ((resource.getNull() == true) 
			 || (resource.getID() == null)) 
			{
				// skip empty lines.  they are created later by examining ids
				continue;
			}

			if (resource.getID() == 0)
			{ 
				// if is special unassigned resource, map to unassigned resource singleton
				mapResource( (long)resource.getUniqueID(), ResourceImpl.getUnassignedInstance() );
			}
			else
			{
				projityResource = resourcePool.newResourceInstance();
				mapResource( (long)resource.getUniqueID(), projityResource );
				MPXConverter.toProjityResource( resource, projityResource, context );
				projityResource.getGlobalResource().setLocal( true );
				projityResource.getGlobalResource().setResourcePool( resourcePool );

				// imported resources without a calendar should use project calendar as base
				if (projityResource.getBaseCalendar() == null)
				{
					try
					{
						projityResource.setBaseCalendar( getProject().getBaseCalendar() );
					}
					catch (CircularDependencyException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				// Add to resource hierarchy.  MSProject does not actually have a hierarchy
				Node resourceNode = NodeFactory.getInstance().createNode( projityResource ); // get a node for this resource
				resourcePool.addToDefaultOutline( null, resourceNode );
			}
		}

		insertResourceVoids();
	}

	protected void importOptions()
	{
		// JGao 9/23/2009 Added the checking logic to skip import calendar options if import calendar is skipped. Otherwise, the calendar option
		// values will be reset by the calendar information in the project header of the imported file.
		if (!this.isSkipCalendars())
		{
			net.sf.mpxj.ProjectHeader projectHeader = mpx.getProjectHeader();
			MPXConverter.toProjityOptions( projectHeader, context );
		}
	}

	public void importProject( 
		final Project _project )
		throws Exception
	{
		//System.out.println( "MicrosoftImporter.importProject()" );
		log.entering( "MicrosoftImporter", "importProject" );

		subprojects = new ArrayList();
		setProject( _project );
		parse();
		convertToProjity();
	}

	private void importProjectFields()
	{
		net.sf.mpxj.ProjectHeader projectHeader = mpx.getProjectHeader();
		MPXConverter.toProjityProject( projectHeader, getProject(), context );

		//project.setWorkCalendar((WorkCalendar) Dictionary.get(WorkCalendar.CALENDAR_CATEGORY,projectHeader.getCalendarName()));
		Date projectStart = null; // this value is wrong sometimes! MPXConverter.toNormalDate(projectHeader.getStartDate());
								  // if a start found in project header, use it, otherwise use earliest starting task for start

		if (context.isMariner())
		{
			long d = DateTime.gmt( projectHeader.getStartDate() );
			getProject().setStartDate( getProject().getWorkCalendar().adjustInsideCalendar( d, true ) );
		}
		else
		{
			if (projectStart != null)
			{
				getProject().setStart( DateTime.gmt( projectStart ) );
			}
			else
			{
				getProject().setStart( earliestStart.getTime() ); // earliest start already made gmt
			}
		}

		//getProject().setManager(projectHeader.getManager());
		//getProject().setNotes(projectHeader.getComments());
	}

	protected boolean importResources()
		throws Exception
	{
		return importResources( myResourceMap,
			new Closure<Object>()
		{
			@Override
			public void execute( 
				Object _X )
			{
				importLocalResources();
			}
		} );
	}

	protected boolean importResources( 
		final HashMap _resourceMap,
		final Closure<?> _importLocalResources )
		throws Exception
	{
		ResourceMappingForm form = getResourceMapping();

		if ((form == null) 
		 || (form.isLocal() == true)
		 || (Environment.isNoPodServer() == true))
		{
			if (form != null)
			{
				//importLocalResources.execute(null);
				Project existingProject = form.getExistingProject();
				getProject().setResourcePool( existingProject.getResourcePool() );
				retrieveResourcesForMerge( existingProject.getResourcePool().getResourceList() );

				if (form.execute() == false)
				{
					return false;
				}

				net.sf.mpxj.Resource srcResource;
				com.projity.pm.resource.Resource projityResource;
				Iterator ir = form.getImportedResources().iterator();
				Iterator sr = form.getSelectedResources().iterator();

				while (ir.hasNext() == true)
				{
					srcResource = (net.sf.mpxj.Resource)ir.next();

					Object obj = sr.next();

					if (obj instanceof com.projity.pm.resource.Resource == false)
					{
						continue;
					}

					projityResource = (com.projity.pm.resource.Resource)obj;

					if (projityResource != null)
					{
						mapResource( (long)srcResource.getUniqueID(), projityResource );
					}
				}
			}
			else
			{
				_importLocalResources.execute( null );
			}
		}
		else
		{
			if (form.execute() == false)
			{
				return false;
			}

			if (form.isLocal() == true)
			{
				_importLocalResources.execute( null );

				/*ResourcePool resourcePool = getProject().getResourcePool();
				getProject().setTemporaryLocal(true);
				resourcePool.setLocal(true);
				resourcePool.setMaster(false);
				resourcePool.updateOutlineTypes();*/
				return true;
			}

			com.projity.pm.resource.Resource projityResource = null;
			int projityResourceCount = 0;
			ResourcePool resourcePool = getProject().getResourcePool();
			getProject().setTemporaryLocal( true );

			Object srcResource;
			EnterpriseResourceData data;
			Map enterpriseResourceDataMap = new HashMap();

			for (Iterator i = form.getResources().iterator(); i.hasNext();)
			{
				data = (EnterpriseResourceData)i.next();

				if (data.isLocal() == true)
				{
					projityResource = ResourceImpl.getUnassignedInstance();
				}
				else
				{
//					try {
					projityResource = Serializer.deserializeResourceAndAddToPool( data, resourcePool, null );

					//Handles only flat outlines
					Node node = NodeFactory.getInstance().createNode( projityResource );
					resourcePool.addToDefaultOutline( null, node, projityResourceCount++, false );
					projityResource.getGlobalResource().setResourcePool( resourcePool );

//					} catch (Exception e) {}
				}

				enterpriseResourceDataMap.put( data, projityResource );
			}

			Iterator ir = form.getImportedResources().iterator();
			Iterator sr = form.getSelectedResources().iterator();

			while (ir.hasNext())
			{
				srcResource = ir.next();
				data = (EnterpriseResourceData)sr.next();
				projityResource = (com.projity.pm.resource.Resource)enterpriseResourceDataMap.get( data );
				mapResource( (Long)projityResource.getUniqueId(), projityResource );
			}

			resourcePool.setMaster( false );
			resourcePool.updateOutlineTypes();

			getProject().setAccessControlPolicy( form.getAccessControlType() );
			getProject().resetRoles( form.getAccessControlType() == 0 );

//			Iterator ir = form.getImportedResources().iterator();
//			Iterator sr = form.getSelectedResources().iterator();
//			while (ir.hasNext()) {
//				resource = (Resource) ir.next();
//				data=(EnterpriseResourceData)sr.next();
//				if (enterpriseResourceDataMap.containsKey(data))
//					projityResource=(com.projity.pm.resource.Resource)enterpriseResourceDataMap.get(data);
//				else{
//					if (data.isLocal()) {
//						projityResource=ResourceImpl.getUnassignedInstance();
//					} else {
////						try {
//							projityResource=Serializer.deserializeResourceAndAddToPool(data,resourcePool);
//
//							//Handles only flat outlines
//							Node node=NodeFactory.getInstance().createNode(projityResource);
//							resourcePool.addToDefaultOutline(null,node,projityResourceCount++,false);
//			                ((ResourceImpl)projityResource).getGlobalResource().setResourcePool(resourcePool);
////						} catch (Exception e) {}
//					}
//					enterpriseResourceDataMap.put(data,projityResource);
//				}
//				myResourceMap.put(resource,projityResource );
//			}
		}

		return true;
	}

	/**
	 * This method imports all tasks defined in the file into the projity model
	 *
	 */
	private void importTasks()
	{
		allTasks = mpx.getAllTasks();

		final Iterator<net.sf.mpxj.Task> iter = allTasks.iterator();
		boolean isRoot = false;

		while (iter.hasNext() == true)
		{
			final net.sf.mpxj.Task task = iter.next();

			if (task.getNull() == true)
			{
				//System.out.println( "skipping null task" + task.getName() + " " + task.getID() ); 
				log.info( "skipping null task '" + task.getName() + "', ID: " + task.getID() );

				continue;
			}

			if (task.getSubProject() != null)
			{
				subprojects.add( task.getName() );
			}

			if ((task.getOutlineNumber() != null) 
			 && (task.getOutlineLevel() == 0) 
			 && (!context.isMariner() || context.isDatesPinned()))
			{ // there is a dummy first task with outline level 0 that is used to hold project info

				if (dummyFirstTask != null)
				{
					log.warning( "Encountered more than one dummy first tasks" );
				}

				dummyFirstTask = task;

				/* we don't need to name the summary task.  in Mariner, it should just keep the default name
				if (!context.isMariner())
				        getProject().setName(task.getName()); // project name is first task's name
				*/
				if (ADD_SUMMARY_TASK == false)
				{	
					// mpp files have an initial task that should only be ignored if specified
					myTaskMap.put( task, null );

					continue;
				}

				isRoot = true;
			}

			org.openproj.domain.task.Task openProjTask = getProject().newTaskInstance( false );
			openProjTask.setOwningProject( getProject() );
			openProjTask.setProjectId( getProject().getUniqueId() );

			myTaskMap.put( task, openProjTask );

//			if (task.getCalendarName() != null)
//				System.out.println("task calendar for :" + task.getName() + " is " + task.getCalendarName());

//			System.out.println("Task " + task.getID() + " level " + task.getOutlineLevelValue() + " number " + task.getOutlineNumber() +  " wbs " + task.getWBS());
			toOpenProjTask( task, openProjTask, context );

			//DEF164189 Projity: Summary Task creation upon file import  -> this will avoid this by setting first msp task as the projity root task
			if (isRoot)
			{
				openProjTask.setRoot( true );

				//change this to use a constant
				openProjTask.setName( "Summary Task" );
				isRoot = false;
			}

			Date start = DateTime.gmtDate( task.getStart() );
			earliestStart = (earliestStart == null)
				? start
				: DateTime.min( earliestStart, start );

			if (openProjTask.isRoot() == true)
			{
				getProject().setSummaryTaskEnabled( true );
			}

			//	System.out.println("imported task #" + count++ + " name " + projityTask );
		}

		if (!subprojects.isEmpty())
		{
			Alert.warn( Messages.getString( "MicrosoftImporter.ImportWithSubprojects" ) + StringList.list( subprojects ) ); //$NON-NLS-1$
		}
	}

	/** Same as insertTaskVoids.  Because MPXJ does not provide a base interface, I have to copy/paste this code (it could be done with callbacks, but it's not that complicated)
	 *
	 */
	private void insertResourceVoids()
	{
		long previousId = 0;
		Iterator iter = mpx.getAllResources().iterator();
		NodeModel nodeModel = getProject().getResourcePool().getResourceOutline();

		while (iter.hasNext())
		{ // go thru all tasks

			net.sf.mpxj.Resource mpxjResource = (net.sf.mpxj.Resource)iter.next();

			if (mpxjResource.getNull() || (mpxjResource.getID() == null)) 
			{
				// when importing mspdi format, nulls appear.  However to simplify, I do same treatment as .mpp files, using the id.
				continue;
			}

			int id = mpxjResource.getID();
			int blankLines = (int)(id - previousId - 1); // how many void nodes to insert
			previousId = id;

			if (blankLines == 0)
			{
				continue;
			}

			final Node nextNode = nodeModel.search( myResourceMap.get( mpxjResource.getUniqueID() ) );

			for (int i = 0; i < blankLines; i++)
			{ 
				// insert void siblings before
				final Node voidNode = NodeFactory.getInstance().createVoidNode();
				nodeModel.addBefore( nextNode, voidNode, NodeModel.SILENT );
			}
		}
	}

	/**
	 * Insert blank lines into the task list.  this is accomplished by looking for gaps in the ids.  Note that this assumes
	 * that the tasks are numbered sequentially!  No sorting by id is done.
	 *
	 */
	private void insertTaskVoids()
	{
		long previousId = 0;
		Iterator taskIter = allTasks.iterator();
		NodeModel nodeModel = getProject().getTaskOutline();

		while (taskIter.hasNext())
		{ // go thru all tasks

			net.sf.mpxj.Task task = (net.sf.mpxj.Task)taskIter.next();

			if (task.getNull()) // when importing mspdi format, nulls appear.  However to simplify, I do same treatment as .mpp files, using the id.
			{
				continue;
			}

			if (task.getID() == null)
			{
				continue;
			}

			int id = task.getID();
			int blankLines = (int)(id - previousId - 1); // how many void nodes to insert
			previousId = id;

			if (blankLines == 0)
			{
				continue;
			}

			final org.openproj.domain.task.Task openProjTask = myTaskMap.get( task );
			final Node nextNode = nodeModel.search( openProjTask );

			for (int i = 0; i < blankLines; i++)
			{ 
				// insert void siblings before
				Node voidNode = NodeFactory.getInstance().createVoidNode();
				nodeModel.addBefore( nextNode, voidNode, NodeModel.SILENT );
			}
		}
	}

	protected void makeValidResourceId( 
		final net.sf.mpxj.Resource _resource )
	{
	}

	protected void mapResource( 
		final Long _id,
		final Resource _resource )
	{
//		System.out.println("Mapping res " + id + "   " + value);
		myResourceMap.put( _id, _resource );
	}

	public void parse()
		throws Exception
	{
//		System.out.println( "MicrosoftImporter.parse()" );
		log.entering( "MicrosoftImporter", "parse" );

		Environment.setImporting( true ); // will avoid certain popups
		log.info( "doing file import" );

		InputStream stream = null;
		setProgress( 0.05f );

		int dot = fileName.lastIndexOf( "." );
		stream = getStream();

		if (stream == null)
		{
			Alert.warn( Messages.getString( "Warn.fileNotFound" ) );
			throw (new JobCanceledException(ABORT));
		}

		String extension;

		if (dot == -1) // assume xml if nothing set
		{
			extension = "xml"; //$NON-NLS-1$
		}
		else
		{
			extension = fileName.substring( dot + 1 ).toLowerCase(); // get part after .
		}

		if (extension.equals( "mpp" ))
		{ 
			reader = new net.sf.mpxj.mpp.MPPReader();

			try
			{
				mpx = reader.read( stream );
			}
			catch (Exception ex)
			{
				lastException = ex;
				ErrorLogger.log( "Exception importing " + extension + " file", ex ); //$NON-NLS-1$ //$NON-NLS-2$
				mpx = null;
			}
		}
		else if (extension.equals( "mpx" ))
		{ 
			reader = new net.sf.mpxj.mpx.MPXReader();

			try
			{
				mpx = reader.read( stream );
			}
			catch (Exception ex)
			{
				lastException = ex;
				ErrorLogger.log( "Exception importing " + extension + " file", ex );
				mpx = null;
			}
		}
		else if (extension.equals( "xml" ))
		{ 
			// XML
			try
			{
				context.setXml( true );
				reader = new net.sf.mpxj.mspdi.MSPDIReader(); //ModifiedMSPDIReader();
				mpx = reader.read( stream );
			}
			catch (Exception ex)
			{
				lastException = ex;
				//System.out.println( "Can't read xml: " + ex.getMessage() );
				log.severe( "Can't read xml: " + ex.getMessage() );
				
				ex.printStackTrace();
				mpx = null;
				errorDescription = Messages.getString( "MicrosoftImporter.ErrorImportingXML" );
			}
		}
		else if (extension.equals( "planner" ))
		{ 
			reader = new net.sf.mpxj.planner.PlannerReader();

			try
			{
				mpx = reader.read( stream );
			}
			catch (Exception ex)
			{
				lastException = ex;
				ErrorLogger.log( "Exception importing " + extension + " file", ex ); //$NON-NLS-1$ //$NON-NLS-2$
				mpx = null;
			}
		}

		if (stream != null) // close the stream
		{
			stream.close();
		}

//JAXB is not on classpath right now
//		if (mpx == null) {
//			try {
//				mpx = new MSPDIFile(fileName);
//			} catch (Exception ex) {
//				mpx = null;
//			}
//		}
		
		if (mpx == null)
		{
			String errorText = (errorDescription == null)
				? Messages.getString( "Message.ImportError" )
				: errorDescription; //$NON-NLS-1$

			if (jobRunnable != null)
			{
				jobRunnable.getJob().error( errorText, false );
				jobRunnable.getJob().cancel();
			}

			Environment.setImporting( false ); // will avoid certain popups
			throw (lastException == null)
			? new Exception("Failed to import file")
			: lastException; //$NON-NLS-1$
		}

		setProgress( 0.2f );
		log.info( "prepare resources" );
		allResources = mpx.getAllResources();

		if (!Environment.isNoPodServer())
		{
			prepareResources( mpx.getAllResources(),
				new Predicate()
				{
				@Override
					public boolean evaluate( 
						Object arg )
					{
						net.sf.mpxj.Resource resource = (net.sf.mpxj.Resource)arg;

						if (resource != null)
						{
							Integer id = resource.getID();

							if ((id != null) && (id.longValue() != 0L))
							{
								return true;
							}
						}

						return false;
					}
				}, true );
		}
		else
		{
//			importResources();
		}

		setProgress( 1f );
	}

	protected void retrieveResourcesForMerge( 
		final List _existingResources )
		throws Exception
	{
		final ResourceMappingForm form = getResourceMapping();

		if (form == null)
		{
			return;
		}

		//resource pool resources
		final Vector projityResources = new Vector();
		final EnterpriseResourceData unassigned = new EnterpriseResourceData();
		unassigned.setUniqueId( EnterpriseResource.UNASSIGNED_ID );
		unassigned.setName( Messages.getString( "Text.Unassigned" ) ); //$NON-NLS-1$
		form.setUnassignedResource( unassigned );
		projityResources.add( unassigned );

		projityResources.addAll( _existingResources );
		form.setResources( projityResources );

		//imported resources
		final List resourcesToMap = new ArrayList();
		net.sf.mpxj.Resource resource;

		if (allResources != null)
		{
			for (Iterator<net.sf.mpxj.Resource> i = allResources.iterator(); i.hasNext();)
			{
				resource = i.next();

				if ((resource.getNull() == true)
			     || (resource.getID() == null) // skip empty lines.  they are created later by examining ids
				 || (resource.getID() == 0)) // unassigned resource
				{
					continue;
				}

				resourcesToMap.add( resource );
			}
		}

		form.setImportedResources( resourcesToMap );

		MergeField mergeField = new ResourceMappingForm.MergeField("name", "name", "name"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		form.addMergeField( mergeField );

		if (!form.isJunit())
		{
			form.setMergeField( mergeField );
		}

		mergeField = new ResourceMappingForm.MergeField("emailAddress", "emailAddress", "email"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		form.addMergeField( mergeField );
		mergeField = new ResourceMappingForm.MergeField("uniqueId", "externalId", "id"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		form.addMergeField( mergeField );
	}

	private void setProgress( 
		final float _progress )
	{
		if (jobRunnable == null)
		{
			log.info( "Progress " + (100 * _progress) + "%" );
		}
		else
		{
			jobRunnable.setProgress( _progress );
		}
	}

	protected void toOpenProjTask( 
		final net.sf.mpxj.Task _mpxTask,
		final org.openproj.domain.task.Task _openProjTask,
		final Context _context )
	{
		MPXConverter.toOpenProjTask( _mpxTask, _openProjTask, _context );
	}

	static final Logger log = Logger.getLogger( "Microsoft importer" );

	// whether to automatically add an extra project summary task or not
	public static boolean ADD_SUMMARY_TASK = Environment.isAddSummaryTask(); 
	
	private static final String ABORT = "Job aborted"; //$NON-NLS-1$

//	public void run() {
//		try {
//			throw new NullPointerException("Broken by refactorisation?");
//			//importFile();
//		} catch (Exception e) {
//			if (e.getMessage() == CircularDependencyException.RUNTIME_EXCEPTION_TEXT)
//				return;
//			if (errorDescription != ABORT)
//  			   ServerLogger.log("Import Exception: " + errorDescription,lastException == null ? e : lastException);
//			e.printStackTrace();
//		} finally {
//			Environment.setImporting(false);
//		}
//	}

	private static String[] fileSuffixes = 
	{
		"",
		".xml",
		".mpp",
		".mpx",
		".planner"
	};

	protected Context context = new Context();

	// keeps track of mapping mpx tasks to projity tasks
	protected HashMap<net.sf.mpxj.Task,org.openproj.domain.task.Task> myTaskMap = 
		new HashMap<net.sf.mpxj.Task,org.openproj.domain.task.Task>(); 
	
	protected net.sf.mpxj.ProjectFile mpx = null;
	private net.sf.mpxj.reader.AbstractProjectReader reader;
	private ArrayList subprojects;
	private List<net.sf.mpxj.Resource> allResources = null;
	private List<net.sf.mpxj.Task> allTasks = null;
	private net.sf.mpxj.Task dummyFirstTask = null;
	private Date earliestStart = DateTime.getMaxDate();
	private Exception lastException = null;

	// keeps track of mappy mpx resources to projity resources
	private HashMap<Long,Resource> myResourceMap = new HashMap<Long,Resource>(); 
	
	private JobRunnable jobRunnable = null;
	private String errorDescription = null;
}
