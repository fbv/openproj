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

import com.projity.grouping.core.VoidNodeImpl;

import com.projity.job.JobRunnable;

import com.projity.pm.key.uniqueid.UniqueIdException;
import com.projity.pm.resource.Resource;
import com.projity.pm.task.Project;

import com.projity.server.data.linker.Linker;
import com.projity.server.data.linker.ResourceLinker;
import com.projity.server.data.linker.TaskLinker;
//import com.projity.server.data.mspdi.ModifiedMSPDIWriter;



/**
 *
 */
public class MSPDISerializer
	implements ProjectSerializer
{
	public JobRunnable getJob()
	{
		return job;
	}

	public void makeGLobal( 
		DataObject data )
		throws UniqueIdException
	{
		CommonDataObject.makeGlobal( data );
	}

	@Override
	public boolean saveProject( 
		Project project,
		String fileName )
	{
		try
		{
			//MSPDISerializer serializer=new MSPDISerializer();
//			ModifiedMSPDIWriter data =  /*serializer.*/serializeProject( project );
//
//			if (job != null)
//			{
//				job.setProgress( 0.9f );
//			}
//
//			data.write( data.getProjectFile(), new FileOutputStream(fileName) );
//
//			if (job != null)
//			{
//				job.setProgress( 1.0f );
//			}
		}
		catch (Exception e)
		{
			e.printStackTrace();

			return false;
		}

		return true;
	}

//	protected Map saveResources( 
//		Project project,
//		ModifiedMSPDIWriter projectData )
//		throws Exception
//	{
//		NodeModelUtil.enumerateNonAssignments( project.getResourcePool().getResourceOutline() );
//		myResourceLinker.setParent( project );
//		myResourceLinker.setTransformedParent( projectData );
//
//		//myResourceLinker.setGlobalIdsOnly( globalIdsOnly );
//		myResourceLinker.init();
//		myResourceLinker.addTransformedObjects( ResourceImpl.getUnassignedInstance() );
//		myResourceLinker.addTransformedObjects();
//		myResourceLinker.addOutline( null );
//
//		//    	myResourceLinker.getTransformationMap().put(new Long(ResourceImpl.getUnassignedInstance().getUniqueId()),ResourceImpl.getUnassignedInstance());
//		return myResourceLinker.getTransformationMap();
//	}

//	protected Map saveTasks( 
//		Project project,
//		ModifiedMSPDIWriter projectData,
//		Map resourceMap )
//		throws Exception
//	{
//		NodeModelUtil.enumerateNonAssignments( project.getTaskOutline() ); // to fix bug, I moved this before tasks are saved. 16.2.06 hk
//		taskLinker.setParent( project );
//		taskLinker.setTransformedParent( projectData );
//
//		//taskLinker.setGlobalIdsOnly(globalIdsOnly);
//		taskLinker.setArgs( new Object[]
//			{
//				resourceMap
//			} );
//		taskLinker.init();
//		taskLinker.addTransformedObjects();
//		taskLinker.addOutline( null );
//
//		//dependencies
//		// mpxj uses default options when importing link leads and lags
//		CalendarOption oldOptions = CalendarOption.getInstance();
//		CalendarOption.setInstance( CalendarOption.getDefaultInstance() );
//
//		int taskCount = 0;
//		LinkedList voidTasksQueue = new LinkedList(); // we do not want to export nulls lines at end, so once all tasks done, stop
//
//		for (Iterator i = project.getTaskOutline().iterator(); i.hasNext();)
//		{
//			Object obj = ((Node)i.next()).getValue();
//
//			if ((voidTasksQueue.size() > 0) && !(obj instanceof VoidNodeImpl))
//			{
//				//insert voids
//				for (Object voidTask : voidTasksQueue)
//				{
//					net.sf.mpxj.Task taskData = projectData.getProjectFile().addTask();
//					MPXConverter.toMPXVoid( (VoidNodeImpl)voidTask, taskData );
//				}
//
//				voidTasksQueue.clear();
//			}
//
//			if (obj instanceof Assignment)
//			{
//				continue;
//			}
//
//			if (obj instanceof VoidNodeImpl)
//			{
//				if (taskCount == 0) //TODO see why there is a void node at the beginning always
//				{
//					continue;
//				}
//
//				voidTasksQueue.add( obj );
//			}
//			else
//			{
//				Task task = (Task)obj; //ResourceImpl to have the EnterpriseResource link
////	            task.setUniqueId(task.getId()); // set unique id and id to the same thing on export. Ensures unique id is unique
//
//				net.sf.mpxj.Task taskData = (net.sf.mpxj.Task)taskLinker.getTransformationMap().get( task );
//				projectData.putProjityTaskMap( taskData, task );
//
//				for (Iterator j = task.getPredecessorList().iterator(); j.hasNext();)
//				{
//					Dependency dependency = (Dependency)j.next();
//					Task pred = (Task)dependency.getPredecessor();
//					net.sf.mpxj.Task predData = (net.sf.mpxj.Task)taskLinker.getTransformationMap().get( pred );
//
////					net.sf.mpxj.Relation rel = taskData.addUniqueIdPredecessor( predData );
////					rel.setType( net.sf.mpxj.RelationType.getInstance( dependency.getDependencyType() ) );
////					rel.setDuration( MPXConverter.toMPXDuration( dependency.getLag() ) );
//				}
//
//				taskCount++;
//			}
//		}
//
//		CalendarOption.setInstance( oldOptions );
//
//		return taskLinker.getTransformationMap();
//	}

//	public ModifiedMSPDIWriter serializeProject( 
//		Project project )
//		throws Exception
//	{
//		return serializeProject( project, false );
//	}

//	public ModifiedMSPDIWriter serializeProject( 
//		Project project,
//		boolean globalIdsOnly )
//		throws Exception
//	{
//		if (globalIdsOnly)
//		{
//			makeGLobal( project );
//		}
//
//		ModifiedMSPDIWriter projectData = new ModifiedMSPDIWriter();
//		net.sf.mpxj.ProjectFile projectFile = new net.sf.mpxj.ProjectFile();
//		projectData.setProjectFile( projectFile );
//
//		projectData.setProjityProject( project );
//
//		//this doesn't appear in 2007 version of mpxj        projectData.setMicrosoftProjectCompatibleOutput(true);
//		projectFile.setAutoTaskUniqueID( true );
//		projectFile.setAutoResourceUniqueID( true );
//
//		//project
//		net.sf.mpxj.ProjectHeader projectHeader = projectFile.getProjectHeader();
//
//		MPXConverter.toMPXOptions( projectHeader );
//
//		MPXConverter.toMPXProject( project, projectHeader );
//
//		if (job != null)
//		{
//			job.setProgress( 0.2f );
//		}
//
//		//calendars
//		//        WorkCalendar calendar=project.getWorkCalendar();
//		//        if (calendar!=null){
//		//            ProjectCalendar calendarData=projectData.addDefaultBaseCalendar();
//		//            calendarData.setName(calendar.getName());
//		//        }
//		projectFile.setAutoCalendarUniqueID( true );
//
//		CalendarService service = CalendarService.getInstance();
//		Object[] calendars = CalendarService.allBaseCalendars();
//
//		if (calendars != null)
//		{
//			for (int i = 0; i < calendars.length; i++)
//			{
//				WorkingCalendar workCalendar = (WorkingCalendar)calendars[ i ];
//				net.sf.mpxj.ProjectCalendar cal = projectFile.addBaseCalendar();
//				MPXConverter.toMpxCalendar( workCalendar, cal );
//				ImportedCalendarService.getInstance().addExportedCalendar( cal, workCalendar );
//			}
//		}
//
//		if (job != null)
//		{
//			job.setProgress( 0.3f );
//		}
//
//		//resources
//		Map resourceMap = saveResources( project, projectData );
//
//		if (job != null)
//		{
//			job.setProgress( 0.5f );
//		}
//
//		//tasks
//		saveTasks//tasks
//		( project, projectData, resourceMap );
//
//		if (job != null)
//		{
//			job.setProgress( 0.7f );
//		}
//
//		return projectData;
//	}

	public void setJob( 
		JobRunnable job )
	{
		this.job = job;
	}

	public static final boolean TMP_FILES = false;
	protected JobRunnable job = null;
	protected Linker<Resource,net.sf.mpxj.Resource> myResourceLinker = new ResourceLinker<Resource,net.sf.mpxj.Resource>()
		{
			//    	int count = 0; // unassigned should start at 0
			@Override
			public net.sf.mpxj.Resource addTransformedObjects( 
				final Resource _from )
				throws Exception
			{
				//    		Project project = (Project)myParent;
//				final ModifiedMSPDIWriter projectData = (ModifiedMSPDIWriter)myTransformedParent;
//
//				//    		resource.setId( count++ ); // enumerate them
//				final net.sf.mpxj.Resource resourceData = projectData.getProjectFile().addResource();
//				MPXConverter.toMPXResource( _from, resourceData );
//
//				myTransformationMap.put( _from, resourceData );
//
//				return resourceData;
				return null;
			}

			@Override
			public boolean addOutlineElement( 
				Object outlineChild,
				Object outlineParent,
				long position )
			{
				return true;
			}
		};

	protected Linker taskLinker = new TaskLinker()
		{
			@Override
			public Object addTransformedObjects( 
				Object child )
				throws Exception
			{
//				Project project = (Project)myParent;
//				ModifiedMSPDIWriter projectData = (ModifiedMSPDIWriter)myTransformedParent;
//				NormalTask task = (NormalTask)child;
//
//				net.sf.mpxj.Task taskData = projectData.getProjectFile().addTask();
//				MPXConverter.toMPXTask( task, taskData );
//
//				Map resourceMap = (Map)myArgs[ 0 ];
//
//				for (int s = 0; s < Settings.numBaselines(); s++)
//				{
//					TaskSnapshot snapshot = (TaskSnapshot)task.getSnapshot( new Integer(s) );
//
//					if (snapshot == null)
//					{
//						continue;
//					}
//
//					AssociationList snapshotAssignments = snapshot.getHasAssignments().getAssignments();
//
//					if (snapshotAssignments.size() > 0)
//					{
//						for (Iterator j = snapshotAssignments.iterator(); j.hasNext();)
//						{
//							Assignment assignment = (Assignment)j.next();
//							ResourceImpl r = (ResourceImpl)assignment.getResource();
//
//							if (s != Snapshottable.CURRENT.intValue())
//							{
//								continue;
//							}
//
//							net.sf.mpxj.Resource resourceData = (net.sf.mpxj.Resource)resourceMap.get( r );
//
//							net.sf.mpxj.ResourceAssignment assignmentData = taskData.addResourceAssignment( resourceData );
//
//							projectData.putProjityAssignmentMap( assignmentData, assignment );
//							projectData.putProjitySnapshotIdMap( assignmentData, Integer.valueOf( s ) );
//
//							if (s == Snapshottable.CURRENT.intValue())
//							{
//								MPXConverter.toMPXAssignment( assignment, assignmentData );
//							}
//						}
//					}
//				}
//
//				myTransformationMap.put( task, taskData );
//
//				return taskData;
				return null;
			}

			@Override
			public boolean addOutlineElement( 
				Object outlineChild,
				Object outlineParent,
				long position )
			{
				if (outlineChild instanceof VoidNodeImpl) // skip void nodes
				{
					return false;
				}

				net.sf.mpxj.Task taskData = (net.sf.mpxj.Task)getTransformationMap().get( outlineChild );
				net.sf.mpxj.Task parentData = (outlineParent == null)
					? null
					: ((net.sf.mpxj.Task)getTransformationMap().get( outlineParent ));
				taskData.setOutlineLevel( new Integer(((parentData == null)
						? 1
						: (parentData.getOutlineLevel().intValue() + 1))) ); // outline levels start at 1
				taskData.setOutlineNumber( ((parentData == null)
					? ""
					: (parentData.getOutlineNumber() + ".")) + (position + 1) );

				return true;
			}
		};
}
