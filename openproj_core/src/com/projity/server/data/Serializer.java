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

import com.projity.association.InvalidAssociationException;

import com.projity.company.ApplicationUser;
import com.projity.company.UserUtil;

import com.projity.configuration.CircularDependencyException;
import com.projity.configuration.Configuration;
import com.projity.configuration.FieldDictionary;

import com.projity.field.FieldValues;

import com.projity.grouping.core.Node;
import com.projity.grouping.core.NodeFactory;
import com.projity.grouping.core.VoidNodeImpl;
import com.projity.grouping.core.model.DefaultNodeModel;

import com.projity.pm.assignment.Assignment;
import com.projity.pm.assignment.PersistedAssignment;
import com.projity.pm.calendar.CalendarService;
import com.projity.pm.calendar.WorkCalendar;
import com.projity.pm.calendar.WorkingCalendar;
import com.projity.pm.dependency.Dependency;
import com.projity.pm.dependency.DependencyService;
import org.openproj.domain.identity.HasId;
import com.projity.pm.key.HasKey;
import com.projity.pm.key.uniqueid.UniqueIdException;
import com.projity.pm.resource.EnterpriseResource;
import com.projity.pm.resource.Resource;
import com.projity.pm.resource.ResourceImpl;
import com.projity.pm.resource.ResourcePool;
import com.projity.pm.resource.ResourcePoolFactory;
import com.projity.pm.snapshot.Snapshottable;
import com.projity.pm.task.Project;
import com.projity.pm.task.SubProj;
import com.projity.pm.task.TaskSnapshot;

import com.projity.server.access.ErrorLogger;
import com.projity.server.data.linker.Linker;
import com.projity.server.data.linker.ResourceLinker;
import com.projity.server.data.linker.TaskLinker;

import com.projity.session.Session;
import com.projity.session.SessionFactory;

import com.projity.strings.Messages;

import com.projity.undo.DataFactoryUndoController;

import com.projity.util.Environment;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;

import org.openproj.domain.task.Task;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.openproj.domain.model.Model;

/**
 * 
 * @author avigil (Fri Oct 2 21:21:33 2009 UTC)
 */
public class Serializer
{
	public Serializer()
	{
		System.out.println( "Serializer(): version 1.0.1" );
	}
	
	public static void buildAssignmentsStructure( 
		ProjectData projectData,
		Collection assignments )
	{
		buildAssignmentsStructure( projectData, assignments, null, null, false );
	}

	public static void buildAssignmentsStructure( 
		ProjectData projectData,
		Collection assignments,
		Map rMap,
		Map tMap,
		boolean ignoreResourcesForAssignments )
	{
		Map resourceMap = (rMap == null)
			? createIdMap( projectData.getResources() )
			: rMap;
		Map taskMap = (tMap == null)
			? createIdMap( projectData.getTasks() )
			: tMap;

		if (assignments != null)
		{
			for (Iterator i = assignments.iterator(); i.hasNext();)
			{
				AssignmentData assignment = (AssignmentData)i.next();
				ResourceData resource = (ResourceData)resourceMap.get( new Long(assignment.getResourceId()) );

				if (!ignoreResourcesForAssignments)
				{
					assignment.setResource( (resource == null)
						? null
						: resource.getEnterpriseResource() );
				}

				TaskData taskData = (TaskData)taskMap.get( new Long(assignment.getTaskId()) );

				if (taskData == null)
				{
					//System.out.println("null task data ("+assignment.getTaskId()+")- project " + projectData.getName());
					ErrorLogger.logOnce( "null task data", "null task data - project " + projectData.getName(), null );
				}
				else
				{
					taskData.addAssignment( assignment );
				}
			}
		}
	}

	public static void buildLinksStructure( 
		ProjectData projectData,
		Collection links )
	{
		buildLinksStructure( projectData, links, null );
	}

	public static void buildLinksStructure( 
		ProjectData projectData,
		Collection links,
		Map tMap )
	{
		Map taskMap = (tMap == null)
			? createIdMap( projectData.getTasks() )
			: tMap;

		if (links != null)
		{
			for (Iterator i = links.iterator(); i.hasNext();)
			{
				LinkData link = (LinkData)i.next();
				TaskData predecessor = (TaskData)taskMap.get( new Long(link.getPredecessorId()) );
				TaskData successor = (TaskData)taskMap.get( new Long(link.getSuccessorId()) );

				if ((predecessor == null) || (successor == null))
				{
					continue; //external links
				}

				successor.addPredecessor( link );
				link.setPredecessor( predecessor );
			}
		}
	}

//    public static void renumber(Map renumbered){
//        HasUniqueIdImpl.update(renumbered);
//    }
	
// ??? is this method used anywhere?  2012.01.06 psc1952
	public static void buildStructure( 
		ProjectData projectData,
		List<ResourceData> _resources,
		Collection _tasks,
		Collection assignments,
		Collection links,
		Collection externalTasks,
		Collection referringSubprojectTasks,
		boolean ignoreResourcesForAssignments )
	{
		if (externalTasks != null)
		{
			_tasks.addAll( externalTasks );
		}

		Map resourceMap = createIdMap( _resources );
		Map taskMap = createIdMap( _tasks );
		buildTaskStructure( projectData, _tasks, taskMap );
		projectData.setTasks( _tasks );
		projectData.setResources( _resources );
		projectData.setReferringSubprojectTasks( referringSubprojectTasks );

		buildAssignmentsStructure( projectData, assignments, resourceMap, taskMap, ignoreResourcesForAssignments );
		buildLinksStructure( projectData, links, taskMap );
	}

	private void buildTaskDataHierarchy( 
		long key,
		String prefix,
		Map taskMap,
		final StringBuffer b )
	{
		Object o = taskMap.get( new Long(key) );

		if (o == null)
		{
			return;
		}

		TreeSet children = (TreeSet)((TreeSet)o).clone();

		for (Iterator i = children.iterator(); i.hasNext();)
		{
			TaskData taskData = (TaskData)i.next();

			//System.out.println("name: "+taskData.getName());
			b.append( prefix ).append( taskData.getName() ).append( ',' ).append( taskData.getUniqueId() ).append( '\n' );

			if (taskData.getUniqueId() != -1L) //avoid voids
			{
				buildTaskDataHierarchy( taskData.getUniqueId(), prefix + "\t", taskMap, b );
			}
		}
	}

	public static void buildTaskStructure( 
		ProjectData projectData,
		Collection tasks,
		Map tMap )
	{
		if (tasks != null)
		{
			for (Iterator i = tasks.iterator(); i.hasNext();)
			{
				TaskData task = (TaskData)i.next();

				if ((task.getParentTask() == null) & (task.getParentTaskId() != -1))
				{
					//not built yet, building outline
					TaskData parentTask = (TaskData)tMap.get( task.getParentTaskId() );
					task.setParentTask( parentTask );
				}
			}
		}
	}

	public static void connectDependency( 
		Dependency dependency,
		Task predecessor,
		Task successor )
	{
		try
		{
			DependencyService.getInstance().initDependency( dependency, predecessor, successor, null );
		}
		catch (InvalidAssociationException e)
		{
			dependency.setDisabled( true );

			try
			{ // try a second time now that it's disabled
				DependencyService.getInstance().initDependency( dependency, predecessor, successor, null );
			}
			catch (InvalidAssociationException e1)
			{
				e1.printStackTrace();
			}

			DependencyService.warnCircularCrossProjectLinkMessage( predecessor, successor );
		}
	}

	public static Map createIdMap( 
		Collection c )
	{
		Map map = new HashMap();

		if (c != null)
		{
			for (Iterator i = c.iterator(); i.hasNext();)
			{
				DataObject d = (DataObject)i.next();
				map.put( new Long(d.getUniqueId()), d );
			}
		}

		return map;
	}

	private static ResourceImpl createResourceFromEnterpriseResource( 
		EnterpriseResource enterpriseResource )
	{
		ResourceImpl resource = new ResourceImpl();

		return resource;
	}

	protected Collection deserialize( 
		Collection objs,
		Session reindex )
		throws IOException, ClassNotFoundException
	{
		Collection r = new ArrayList(objs.size());

		for (Iterator i = objs.iterator(); i.hasNext();)
		{
			r.add( SerializeUtil.deserialize( (SerializedDataObject)i.next(), reindex ) );
		}

		return r;
	}

	public static DataObject deserialize( 
		DataObject _object,
		Session _reindex )
		throws IOException, 
			   ClassNotFoundException
	{
		return SerializeUtil.deserialize( (SerializedDataObject)_object, _reindex );
	}

	public Project deserializeLocalDocument( 
		final DocumentData _documentData )
		throws IOException, 
			   ClassNotFoundException
	{ 
		System.out.println( "deserializeLocalDocument():" );
		return deserializeProject( (ProjectData)_documentData, false, SessionFactory.getInstance().getLocalSession(), null, null );
	}

	/**
	 * enterpriseResources to use instead of enterprise resources given by projectData
	 */
	public Project deserializeProject( 
		ProjectData projectData,
		final boolean subproject,
		final Session reindex,
		Map enterpriseResources )
		throws IOException, ClassNotFoundException
	{
		return deserializeProject( projectData, subproject, reindex, enterpriseResources, null, true );
	}

	public Project deserializeProject( 
		ProjectData projectData,
		final boolean subproject,
		final Session reindex,
		Map enterpriseResources,
		Closure loadResources )
		throws IOException, ClassNotFoundException
	{
		return deserializeProject( projectData, subproject, reindex, enterpriseResources, loadResources, true );
	}

	public Project deserializeProject( 
		ProjectData _projectData,
		final boolean _subproject,
		final Session _reindex,
		Map _enterpriseResources,
		Closure _loadResources,
		boolean _updateDistribution )
		throws IOException, 
			ClassNotFoundException
	{
		DataFactoryUndoController undoController = new DataFactoryUndoController();
		Project project = (Project)deserialize( _projectData, _reindex );
		project.getNodeModelDataFactory().setUndoController( undoController );
		project.setMaster( _projectData.isMaster() ); //not necessary
		project.setLocal( _projectData.isLocal() );
		project.setReadOnly( !_projectData.canBeUsed() );
		project.setCreationDate( _projectData.getCreationDate() );
		project.setLastModificationDate( _projectData.getLastModificationDate() );

		//project.setExternalId(projectData.getExternalId());
		boolean fixCorruption = false;

		//IncrementalData incremental=new IncrementalData();

		//calendar
//  	WorkCalendar calendar = project.getWorkCalendar();
//  	if (projectData.getCalendar()==null) {
//  	System.out.println("deserializing null project calendar");
//  	calendar= CalendarService.getInstance().getStandardBasedInstance(project);
//  	} else {
//  	calendar= (WorkingCalendar)deserializeCalendar(projectData.getCalendar());
//  	calendar.setDocument(project);
//  	CalendarService.getInstance().add(calendar);
//  	}
//  	CalendarService.getInstance().add((WorkingCalendar) calendar);

		//calendar
		//TODO this code only exists to guarantee that older projects wont crash when read 25/8/05
		WorkCalendar calendar = project.getWorkCalendar();

		if (calendar == null)
		{
			calendar = CalendarService.getInstance().getDefaultInstance();
		}

		project.setWorkCalendar( calendar ); // needed for objects using
		project.setExtraFields( _projectData.getExtraFields() );

		project.setGroup( _projectData.getGroup() );
		project.setDivision( _projectData.getDivision() );
		project.setExpenseType( _projectData.getExpenseType() );
		project.setProjectType( _projectData.getProjectType() );
		project.setProjectStatus( _projectData.getProjectStatus() );
		project.setAccessControlPolicy( _projectData.getAccessControlPolicy() );

		project.postDeserialization();

		//resources
		final Map<EnterpriseResourceData,Node<Resource>> resourceNodeMap = new HashMap<EnterpriseResourceData,Node<Resource>>();
		
		final ResourcePool resourcePool = ResourcePoolFactory.createResourcePool( project.getName(), undoController );
		resourcePool.setMaster( project.isMaster() );
		resourcePool.setLocal( project.isLocal() );
		resourcePool.updateOutlineTypes();

		Collection resources = _projectData.getResources();
//		List<ResourceData> resources = _projectData.getResources();

		if (resources != null)
		{
			//order by position parents don't matter
			Collections.sort( (List<ResourceData>)resources,
				new Comparator<ResourceData>()
			{
				@Override
				public int compare( 
					ResourceData resource1,
					ResourceData resource2 )
				{
					if (resource1.getChildPosition() > resource2.getChildPosition())
					{
						return 1;
					}
					else
					{
						return -1;
					}
				}
			} );
		}

		if (resources != null)
		{
			for (Iterator<ResourceData> itor = resources.iterator(); itor.hasNext() == true;)
			{
				final ResourceData resourceData = itor.next();

				ResourceImpl resource = deserializeResourceAndAddToPool( resourceData, resourcePool, _reindex, 
					_enterpriseResources );
				resourceNodeMap.put( resourceData.getEnterpriseResource(), NodeFactory.getInstance().createNode( resource ) );
				
//				final Resource resource = myLocalResourceMap.get( resourceData.getUniqueId() );
//				resourceNodeMap.put( resourceData.getEnterpriseResource(), NodeFactory.getInstance().createNode( resource ) );
			}
		}

		project.setResourcePool( resourcePool );

		//resource outline
		/* version with outline on project resource
	     * if (resources!=null){
		    for (Iterator i=resources.iterator();i.hasNext();){
		        ResourceData resourceData=(ResourceData)i.next();
		        ResourceData parentData=(ResourceData)resourceData.getParentResource();
		        Node node=(Node)resourceNodeMap.get(resourceData.getEnterpriseResource());
		        Node parentNode=(parentData==null)?
		                        null:
		                        ((Node)resourceNodeMap.get(parentData.getEnterpriseResource()));
		        project.getResourcePool().addToDefaultOutline(parentNode,node,(int)resourceData.getChildPosition());
		    }
		}*/
		
		if (resources != null)
		{
			for (Iterator<ResourceData> itor = resources.iterator(); itor.hasNext() == true;)
			{
				ResourceData resourceData = itor.next();
				EnterpriseResourceData enterpriseResourceData = resourceData.getEnterpriseResource();
				EnterpriseResourceData parentData = enterpriseResourceData.getParentResource();
				
				final Node<Resource> node = resourceNodeMap.get( enterpriseResourceData );
				
				final Node<?> parentNode = (parentData == null)
					? null
					: ((Node<?>)resourceNodeMap.get( parentData ));

				project.getResourcePool().addToDefaultOutline( parentNode, node, (int)enterpriseResourceData.getChildPosition(), 
					false );
				((ResourceImpl)node.getValue()).getGlobalResource().setResourcePool( project.getResourcePool() );
			}

			project.getResourcePool().getResourceOutline().getHierarchy().cleanVoidChildren();

			//renumber resources
			project.getResourcePool().getResourceOutline().getHierarchy().visitAll( 
				new Closure<Node<Resource>>()
			{
				@Override
				public void execute( 
					final Node<Resource> _node )
				{
					if (_node.getValue() instanceof HasId == true)
					{
						HasId impl = (HasId)_node.getValue();

						if (impl.getId() > 0)
						{
							impl.setId( id++ ); //if id=0 means id not used
						}
					}
				}

				private int id = 1;
			} );
		}

		final Map<Long,Node<Resource>> resourceIdMap = new HashMap<Long,Node<Resource>>();
		if (_loadResources != null)
		{
			_loadResources.execute( project );
			project.getResourcePool().getResourceOutline().getHierarchy().visitAll( 
				new Closure<Node<Resource>>()
			{
				@Override
				public void execute( 
					final Node<Resource> _node )
				{
					final HasKey key = (HasKey)_node.getValue();
					resourceIdMap.put( key.getUniqueId(), _node );
				}
			} );
		}

		//tasks
		Collection tasks = _projectData.getTasks();
		Map taskNodeMap = new HashMap();
		long projectId = project.getUniqueId();
//		NormalTask task;
		Task task;

		if (tasks != null)
		{
			//order by position parents don't matter
			Collections.sort( (List<TaskData>)tasks,
				new Comparator<TaskData>()
			{
				@Override
				public int compare( 
					TaskData task1,
					TaskData task2 )
				{
					if ((task1.isExternal() == false) 
					 && (task2.isExternal() == true))
					{
						//keep external tasks at the end
						return -1; 
					}
					else if ((task1.isExternal() == true) 
						  && (task2.isExternal() == false))
					{
						//keep external tasks at the end
						return 1;
					}
					else if (task1.getChildPosition() > task2.getChildPosition())
					{
						return 1;
					}
// begin - psc1952 - 2012.01.05 - see issue: 3464506
//	code was added to prevent exception in sorter				
					else if (task1.getChildPosition() == task2.getChildPosition())
					{
						return 0;
					}
// end - psc1952 - 2012.01.05
					else
					{
						return -1;
					}
				}
			} );

			//Set<Long> initialTaskIds=new HashSet<Long>();
			//project.setInitialTaskIds(initialTaskIds);
			for (Iterator i = tasks.iterator(); i.hasNext() == true;)
			{
				task = null;

				TaskData taskData = (TaskData)i.next();

//  			initialTaskIds.add(taskData.getUniqueId());
				if (taskData.isDirty() == true)
				{
					fixCorruption = true; //recovers errors
				}

				if (Environment.isAddSummaryTask() && (taskData.getUniqueId() == Task.SUMMARY_UNIQUE_ID) &&
						(taskData.getSerialized() == null))
				{
					System.out.println( "Fixing null binary summary task" );
					task = Model.getInstance().createTask( project );
					task.setName( taskData.getName() );
					task.setUniqueId( taskData.getUniqueId() );
				}
				else if (taskData.getSerialized() == null)
				{
					if (taskData.isTimesheetCreated() == true)
					{
						task = Model.getInstance().createTask( project );
						task.setName( taskData.getName() );
						System.out.println( "made new task in serializer " + task + " parent " +
							taskData.getParentTask().getName() );
					}
					else
					{
						continue; // void node
					}
				}
				else
				{
					try
					{
						task = (Task)deserialize( taskData, _reindex );
					}
					catch (Exception e)
					{
						if (taskData.isSubproject() == true)
						{	
							//For migration
							try
							{
								task = (Task)Class.forName( Messages.getMetaString( "Subproject" ) ).getConstructor( 
									new Class[]
								{
									Project.class,
									Long.class
								} ).newInstance( project, taskData.getSubprojectId() );
							}
							catch (Exception e1)
							{
								e1.printStackTrace();
							}

//  						task = new Subproject( project, taskData.getSubprojectId() );
							task.setUniqueId( taskData.getUniqueId() );
							task.setName( taskData.getName() );
							((SubProj)task).setSubprojectFieldValues( taskData.getSubprojectFieldValues() );
						}
						else
						{
							e.printStackTrace();
							throw new IOException( "Subproject:" + e );
						}
					}
				}

				taskNodeMap.put( taskData, NodeFactory.getInstance().createNode( task ) );
				task.setProject( project );
				project.getTasks().initializeId( task );
				project.getTasks().add( task );

				if (taskData.isExternal() == true)
				{
					task.setExternal( true );
					task.setProjectId( taskData.getProjectId() );
					task.setAllSchedulesToCurrentDates();
					project.addExternalTask( task );
				}
				else
				{
					task.setOwningProject( project );
					task.setProjectId( projectId );
				}

				if (taskData.isSubproject() == true)
				{
					SubProj sub = (SubProj)task;
					sub.setSubprojectUniqueId( taskData.getSubprojectId() );
					sub.setSubprojectFieldValues( taskData.getSubprojectFieldValues() );
					sub.setSchedulesFromSubprojectFieldValues();
				}

				if (task.isRoot() == true)
				{
					project.setSummaryTaskEnabled( true );
				}

				WorkingCalendar cal = (WorkingCalendar)task.getWorkCalendar();

				if (cal != null)
				{ 
					// use global one
					WorkingCalendar newCal = (WorkingCalendar)CalendarService.findBaseCalendar( cal.getName() );

					if ((newCal != null) && (newCal != cal))
					{
						task.setWorkCalendar( newCal );
					}
				}

				//project.addToDefaultOutline(null,);

				//assignments
				List assignments = new ArrayList();

				if (Environment.isNoPodServer() && (task.getPersistedAssignments() != null))
				{
					assignments.addAll( task.getPersistedAssignments() );
				}

				if (taskData.getAssignments() != null)
				{
					assignments.addAll( taskData.getAssignments() );
				}

				if (assignments.size() > 0)
				{
					for (Iterator j = assignments.iterator(); j.hasNext();)
					{
						Object obj = j.next();
						AssignmentData assignmentData;

						if ((_loadResources != null) && obj instanceof PersistedAssignment)
						{
							;
						}
						else
						{
							assignmentData = (AssignmentData)obj;

							if (assignmentData.getSerialized() == null)
							{ // timesheet created
								System.out.println( "==== no cached start found " + task.getName() );

								if (assignments.size() == 1)
								{
									assignmentData.setResourceId( -1L );
								}
								else
								{
									j.remove();
								}
							}
						}
					}
				}

				if (assignments.size() > 0)
				{
					for (Iterator j = assignments.iterator(); j.hasNext();)
					{
						Object obj = j.next();
						AssignmentData assignmentData = null;
						Assignment assignment = null;
						Resource resource;
						boolean assigned = true;
						int s;

						if ((_loadResources != null) && obj instanceof PersistedAssignment == true)
						{
							PersistedAssignment pa = (PersistedAssignment)obj;
							assignment = pa.getAssignment();
							s = pa.getSnapshot();

							long resId = pa.getResourceId();
							Node node = (Node)resourceIdMap.get( resId );
							resource = (node == null)
								? ResourceImpl.getUnassignedInstance()
								: (Resource)node.getValue();

							if (resource == null)
							{
								assigned = false;
							}
						}
						else
						{
							assignmentData = (AssignmentData)obj;

							if (_loadResources == null)
							{
								EnterpriseResourceData r = assignmentData.getResource();

								if (r == null)
								{
									assigned = false;
								}

								resource = (r == null)
									? ResourceImpl.getUnassignedInstance()
									: (Resource)((Node)resourceNodeMap.get( r )).getValue();
							}
							else
							{
								long resId = assignmentData.getResourceId();
								Node node = (Node)resourceNodeMap.get( resId );
								resource = (node == null)
									? ResourceImpl.getUnassignedInstance()
									: (Resource)node.getValue();
							}

							if (assignmentData.getSerialized() != null)
							{
								try
								{
									assignment = (Assignment)deserialize( assignmentData, _reindex );
								}
								catch (Exception e)
								{
									e.printStackTrace();
								}
							}

							if ((assignmentData.getSerialized() == null) ||
									((assignmentData.getSerialized() != null) && (assignment == null)))
							{ // timesheet created
								assignment = Assignment.getInstance( task, resource, 1.0, 0 );

								if (assignment.getCachedStart() == null)
								{ //doesn't occur filtered above
									System.out.println( "==== no cached start found " + task.getName() );
								}
								else
								{
									task.setActualStart( assignment.getCachedStart().getTime() );
									task.setActualFinish( assignment.getCachedEnd().getTime() );
								}
							}

							assignment.setCachedStart( assignmentData.getCachedStart() );
							assignment.setCachedEnd( assignmentData.getCachedEnd() );
							assignment.setTimesheetStatus( assignmentData.getTimesheetStatus() );

							long lastUpdate = (assignmentData.getLastTimesheetUpdate() == null)
								? 0
								: assignmentData.getLastTimesheetUpdate().getTime();
							assignment.setLastTimesheetUpdate( lastUpdate );
							assignment.setWorkflowState( assignmentData.getWorkflowState() );
							s = assignmentData.getSnapshotId();
						}

						assignment.getDetail().setTask( task );
						assignment.getDetail().setResource( resource );

						Object snapshotId = new Integer(s);
						TaskSnapshot snapshot = (TaskSnapshot)task.getSnapshot( snapshotId );

						//TODO was commented but needed for loading  because task.getSnapshot(snapshotId)==null
						//for snapshots other than CURRENT
						if (snapshot == null)
						{
							snapshot = new TaskSnapshot();
							snapshot.setCurrentSchedule( task.getCurrentSchedule() );
							task.setSnapshot( snapshotId, snapshot );
						}

						if (Snapshottable.TIMESHEET.equals( snapshotId ) == true)
						{
							assignment.setTimesheetAssignment( true );
						}

						//
						snapshot.addAssignment( assignment );

						if (assigned == true && Snapshottable.CURRENT.equals( snapshotId ) == true)
						{
							resource.addAssignment( assignment );
						}

						if (assignmentData != null)
						{
							assignmentData.emtpy();
						}

						//incremental.addAssignment(assignmentData);
					}
				}

				task.setPersistedAssignments( null );
			}

			// the collection which holds a list of corresponding subproject tasks for projects which include this project
			// note that their task names have been transformed to hold the name of the project
			Collection referringSubprojectTaskData = _projectData.getReferringSubprojectTasks();

			if ((tasks != null) && (referringSubprojectTaskData != null))
			{
				ArrayList referringSubprojectTasks = new ArrayList(referringSubprojectTaskData.size());
				project.setReferringSubprojectTasks( referringSubprojectTasks );

				for (Iterator i = referringSubprojectTaskData.iterator(); i.hasNext();)
				{
					TaskData taskData = (TaskData)i.next();
					String projectName = taskData.getName(); // it was set to the referrig project name by synchronizer
					task = null;

					try
					{
						task = (Task)deserialize( taskData, _reindex );
					}
					catch (Exception e)
					{
						if (taskData.isSubproject() == true)
						{ //For migration
							task = (Task)project.getSubprojectHandler().createSubProj( taskData.getSubprojectId() );
							task.setUniqueId( taskData.getUniqueId() );
							task.setName( taskData.getName() );
							((SubProj)task).setSubprojectFieldValues( taskData.getSubprojectFieldValues() );
						}
						else
						{
							throw new IOException("Subproject:" + e);
						}
					}

					task.setName( projectName );
					task.setProjectId( taskData.getProjectId() );
					referringSubprojectTasks.add( task );
				}
			}

			//dependencies
			//Set<DependencyKey> initialLinkIds=null;
			for (Iterator i = _projectData.getTasks().iterator(); i.hasNext() == true;)
			{
				TaskData successorssorData = (TaskData)i.next();

				if (successorssorData.getPredecessors() != null)
				{
					final Task successor = (Task)((Node)taskNodeMap.get( successorssorData )).getValue();

					for (Iterator j = successorssorData.getPredecessors().iterator(); j.hasNext();)
					{
						LinkData linkData = (LinkData)j.next();

//  					if (initialLinkIds==null){
//  					initialLinkIds=new HashSet<DependencyKey>();
//  					project.setInitialLinkIds(initialLinkIds);
//  					}
//  					initialLinkIds.add(new DependencyKey(linkData.getPredecessorId(),linkData.getSuccessorId()/*,externalId*/));
						Dependency dependency = (Dependency)deserialize( linkData, _reindex );

						if (linkData.getPredecessor() == null)
						{
							System.out.println( "null pred - this shouldn't happen. skipping" ); // todo treat it

							continue;
						}

						final Task predecessor = (Task)((Node)taskNodeMap.get( linkData.getPredecessor() )).getValue();
						connectDependency( dependency, predecessor, successor );

						linkData.emtpy(); //why is this there?
					}
				}
			}
		}

		//task outline
		if (tasks != null)
		{
			//add missing summary task
			Node summaryNode = null;

			if (Environment.isAddSummaryTask() && !project.isSummaryTaskEnabled() //needed for import, add other conditions?
					 &&((tasks.size() == 0) || (((TaskData)tasks.iterator().next()).getUniqueId() != Task.SUMMARY_UNIQUE_ID)))
			{
				Task projityTask = project.newTaskInstance( false );
				projityTask.setName( Messages.getString( "Text.DefaultSummaryTaskName" ) );
				projityTask.setUniqueId( DataObject.SUMMARY_UNIQUE_ID );
				projityTask.setOwningProject( project );
				projityTask.setProjectId( project.getUniqueId() );
				summaryNode = NodeFactory.getInstance().createNode( projityTask ); // get a node for this task
				project.addToDefaultOutline( null, summaryNode );
				project.setSummaryTaskEnabled( true );
			}

			Map<Long,Node> subprojectsMap = new HashMap<Long,Node>();

			for (Iterator i = tasks.iterator(); i.hasNext();)
			{
				TaskData taskData = (TaskData)i.next();
				TaskData parentData = taskData.getParentTask();

//  			if (taskData.isTimesheetCreated())
//  			System.out.println("timesheet created parent is  " + parentData == null ? null : parentData.getName());
				Node node;

				if ((taskData.getSerialized() == null) && (taskData.getUniqueId() != Task.SUMMARY_UNIQUE_ID) &&
						!taskData.isTimesheetCreated()) //void node
				{
					node = NodeFactory.getInstance().createVoidNode();
				}
				else
				{
					node = (Node)taskNodeMap.get( taskData );
				}

				Node parentNode = null;
				int position = -1;

				if (taskData.isExternal() == true)
				{
					Node previous = subprojectsMap.get( taskData.getProjectId() );

					if (previous != null)
					{
						parentNode = (Node)previous.getParent();
					}

					if (parentNode != null)
					{
						position = parentNode.getIndex( previous ) + 1;

						if (parentNode.isRoot())
						{
							parentNode = null;
						}
					}
				}

				if (position == -1)
				{
					if ((parentData == null) && (summaryNode != null))
					{
						parentNode = summaryNode;
					}
					else
					{
						parentNode = (parentData == null)
							? null
							: ((Node)taskNodeMap.get( parentData ));
					}

					position = (int)taskData.getChildPosition();
				}

				if (taskData.isTimesheetCreated() == true)
				{
					System.out.println( "new task " + node + "parent node is " + parentNode );
				}

				if (node.getValue() instanceof SubProj == true)
				{
					SubProj sub = (SubProj)node.getValue();
					subprojectsMap.put( sub.getSubprojectUniqueId(), node );
				}

				project.addToDefaultOutline( parentNode, node, position, false );

				taskData.emtpy();

				//incremental.addTask( taskData );
			}

			//renumber tasks and save outline
			project.getTaskOutline().getHierarchy().visitAll( 
				new Closure()
			{
				int id = 1;

				@Override
				public void execute( 
					final Object _object )
				{
					final Node node = (Node)_object;
					if (node.getValue() instanceof HasId)
					{ 
						//renumber
						final HasId impl = (HasId)node.getValue();

						if (impl.getId() > 0)
						{
							impl.setId( id++ ); //if id=0 means id not used
						}
					}

//  				if (node.getValue() instanceof Task){ //save outline
//  				Task t=(Task)node.getValue();
//  				Node parent=(Node)node.getParent();
//  				if (parent==null||parent.isRoot()) t.setLastSavedParentId(-1L);
//  				else t.setLastSavedParentId(((Task)parent.getValue()).getUniqueId());
//  				t.setLastSavedPosistion(parent.getIndex(node));
//  				}
					//done in setAllTasksAsUnchangedFromPersisted
				}
			} );
		}

		if (resources != null)
		{
			for (Iterator i = resources.iterator(); i.hasNext();)
			{
				ResourceData resourceData = (ResourceData)i.next();
				EnterpriseResourceData enterpriseResourceData = resourceData.getEnterpriseResource();
				resourceData.emtpy();

				//incremental.addResource(resourceData);
				enterpriseResourceData.emtpy();

				//incremental.addEnterpriseResource(enterpriseResourceData);
			}
		}

		((DefaultNodeModel)project.getTaskOutline()).setDataFactory( project.getNodeModelDataFactory() );

		project.initialize( _subproject, _updateDistribution && !fixCorruption );

		_projectData.emtpy();

		//incremental.setProject(projectData); //remove
		(new DistributionConverter()).substractDistributionFromProject( project );

		//distribution map
		//project.updateDistributionMap();
		if (fixCorruption == true)
		{
			project.setForceNonIncremental( true );
		}

		if (project.getVersion() < 1.2)
		{
			project.setForceNonIncrementalDistributions( true );
		}

		project.setVersion( Project.CURRENT_VERSION );

		return project;
	}

	public static ResourceImpl deserializeResourceAndAddToPool( 
		EnterpriseResourceData enterpriseResourceData,
		ResourcePool resourcePool,
		Session reindex )
		throws IOException, 
			ClassNotFoundException
	{
		ResourceData resourceData = new ResourceData();
		resourceData.setEnterpriseResource( enterpriseResourceData );

		ResourceImpl resource = deserializeResourceAndAddToPool( resourceData, resourcePool, reindex, null );
		setRoles( resource, resourceData );

		return resource;
	}

	public static ResourceImpl deserializeResourceAndAddToPool( 
		ResourceData resourceData,
		ResourcePool resourcePool,
		Session reindex,
		Map enterpriseResources )
		throws IOException, ClassNotFoundException
	{
		EnterpriseResourceData enterpriseResourceData = resourceData.getEnterpriseResource();
		EnterpriseResource enterpriseResource;

		if (enterpriseResources == null)
		{
			enterpriseResource = (EnterpriseResource)deserialize( enterpriseResourceData, reindex );
			enterpriseResource.setUserAccount( enterpriseResourceData.getUserAccount() );
		}
		else
		{
			EnterpriseResourceData erd = (EnterpriseResourceData)enterpriseResources.get( Long.valueOf(
				enterpriseResourceData.getUniqueId()) );

			if (erd == null)
			{
				return null; //TODO handle this
			}

			enterpriseResource = (EnterpriseResource)deserialize( erd, reindex );
			enterpriseResource.setUserAccount( erd.getUserAccount() );
		}

		enterpriseResource.setGlobalWorkVector( enterpriseResourceData.getGlobalWorkVector() );
		enterpriseResource.setMaster( resourcePool.isMaster() );

		ResourceImpl resource = (resourceData.getSerialized() == null)
			? createResourceFromEnterpriseResource( enterpriseResource )
			: (ResourceImpl)deserialize( resourceData, reindex );

		resource.setGlobalResource( enterpriseResource );
		setRoles( resource, resourceData );

		// to ensure older projects import correctly
		WorkingCalendar cal = (WorkingCalendar)enterpriseResource.getWorkCalendar();

		if (cal == null)
		{
			enterpriseResource.setWorkCalendar( WorkingCalendar.getInstanceBasedOn( resourcePool.getDefaultCalendar() ) );
		}
		else
		{
			try
			{
				// avoids multiple instances
//				cal.setBaseCalendar(CalendarService.findBaseCalendar(cal.getBaseCalendar().getName()));
				WorkCalendar baseCal = CalendarService.findBaseCalendar( cal.getBaseCalendar().getName() );

				//TODO verification in case the name isn't found, import problem
				if (baseCal != null)
				{
					cal.setBaseCalendar( baseCal ); // avoids multiple instances
				}
			}
			catch (CircularDependencyException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		resourcePool.initializeId( enterpriseResource );
		resourcePool.add( resource );

		return resource;
	}

	public static void forProjectDataDo( 
		ProjectData project,
		Closure c )
	{
		c.execute( project );

		if (project.getCalendar() != null)
		{
			c.execute( project.getCalendar() );

			//base calendars to handle?
		}

		for (Iterator i = project.getResources().iterator(); i.hasNext();)
		{
			ResourceData r = (ResourceData)i.next();
			c.execute( r );
			c.execute( r.getEnterpriseResource() );

			//calendars?
		}

		for (Iterator i = project.getTasks().iterator(); i.hasNext();)
		{
			TaskData t = (TaskData)i.next();
			c.execute( t );
			CollectionUtils.forAllDo( t.getAssignments(), c );
			CollectionUtils.forAllDo( t.getPredecessors(), c );

			//calendars?
		}
	}

	public static void forProjectDataReversedDo( 
		ProjectData project,
		Closure c )
	{
		for (Iterator i = project.getTasks().iterator(); i.hasNext();)
		{
			TaskData t = (TaskData)i.next();
			CollectionUtils.forAllDo( t.getAssignments(), c );
			CollectionUtils.forAllDo( t.getPredecessors(), c );
			c.execute( t );

			//calendars?
		}

		for (Iterator i = project.getResources().iterator(); i.hasNext();)
		{
			ResourceData r = (ResourceData)i.next();
			c.execute( r.getEnterpriseResource() );
			c.execute( r );

			//calendars?
		}

		if (project.getCalendar() != null)
		{
			c.execute( project.getCalendar() );

			//base calendars to handle?
		}

		c.execute( project );
	}

	protected void initTmpDir()
		throws IOException
	{
		tmpDir = new File(System.getProperty( "user.home" ), "projity_tmp");

		if (tmpDir.isDirectory())
		{
			File[] files = tmpDir.listFiles();

			if (files != null)
			{
				for (int i = 0; i < files.length; i++)
				{
					files[ i ].delete();
				}
			}
		}
		else if (!tmpDir.exists())
		{
			tmpDir.mkdir();
		}
	}

	private void markAncestorsOfDirtyTasksDirty( 
		final Project _project )
	{
		for (Object otask : _project.getTasks().collection())
		{
			Task task = (Task)otask;

			if (task.isDirty() == true)
			{
				Task parent = task.getWbsParentTask();

				while ((parent != null) && !parent.isDirty())
				{
					parent.setDirty( true );
					parent = parent.getWbsParentTask();
				}
			}
		}
	}

//    public void makeGLobal(DataObject data) throws UniqueIdException{
//    	CommonDataObject.makeGlobal(data);
//     }
	
	public void printTaskDataHierarchy( 
		Collection tasks )
	{
		StringBuffer b = new StringBuffer();
		printTaskDataHierarchy( tasks, b );
		System.out.println( b );
	}

	public void printTaskDataHierarchy( 
		Collection tasks,
		final StringBuffer b )
	{
		Map taskMap = new HashMap();

		for (Iterator i = tasks.iterator(); i.hasNext();)
		{
			TaskData taskData = (TaskData)i.next();

			if (taskData == null)
			{
				continue;
			}

			Long key = new Long(taskData.getParentTaskId());
			Set set = (Set)taskMap.get( key );

			if (set == null)
			{
				set = new TreeSet(new Comparator()
				{
					@Override
					public int compare( 
						Object arg0,
						Object arg1 )
					{
						TaskData task0 = (TaskData)arg0;
						TaskData task1 = (TaskData)arg1;
						int value = (task0.getChildPosition() < task1.getChildPosition())
							? (-1)
							: ((task0.getChildPosition() == task1.getChildPosition())
							? 0
							: 1);

						if (value == 0)
						{
							b.append( "Duplicates: task0=" ).append( task0.getName() ).append( ", " ).
								append( task0.getParentTaskId() ).append( ", " ).append( task0.getChildPosition() ).
								append( " task1=" ).append( task1.getName() ).append( ", " ).append( task1.getParentTaskId() ).
								append( ", " ).append( task1.getChildPosition() ).append( "\n");
						}

						return value;
					}
				});
			}

			set.add( taskData );
			taskMap.put( key, set );
		}

		buildTaskDataHierarchy( -1L, "\t", taskMap, b );
	}

	public static void renumberProject( 
		ProjectData project )
	{
		forProjectDataDo( project,
			new IdClosure()
		{
			@Override
			public void execute( 
				Object arg0 )
			{
				((CommonDataObject)arg0).setUniqueId( id++ );
			}
		} );
	}

	public Map saveResources( 
		Project _project,
		ProjectData _projectData )
		throws Exception
	{
		myResourceLinker.setParent( _project );
		myResourceLinker.setTransformedParent( _projectData );
		myResourceLinker.init();
		myResourceLinker.addTransformedObjects();
		myResourceLinker.addOutline( null ); // root is null

		return myResourceLinker.getTransformationMap();
	}

	//flatAssignments and flatLinks mustn't be null if incremental
	protected void saveTasks( 
		Project project,
		ProjectData projectData,
		Map resourceMap,
		Collection flatAssignments,
		Collection flatLinks,
		boolean incremental,
		SerializeOptions options )
		throws Exception
	{
		ArrayList<Long> unchangedTasks = null;
		ArrayList<Long> unchangedLinks = null;

		if (incremental == true)
		{
			unchangedTasks = new ArrayList<Long>();
			unchangedLinks = new ArrayList<Long>();

			//myTaskLinker.setUnchanged(unchangedTasks);
		}

		this.markAncestorsOfDirtyTasksDirty( project );

		myTaskLinker.setIncremental( incremental );
		myTaskLinker.setFlatAssignments( flatAssignments );
		myTaskLinker.setParent( project );
		myTaskLinker.setTransformedParent( projectData );

		//myTaskLinker.setGlobalIdsOnly(globalIdsOnly);
		myTaskLinker.setArgs( 
			new Object[]
		{
			resourceMap
		} );

		myTaskLinker.init();
		myTaskLinker.setOptions( options );
		myTaskLinker.addTransformedObjects();
		myTaskLinker.addOutline( project.getTaskOutlineRoot() );

		long projectId = project.getUniqueId();

		//dependencies
		//Count depCount=new Count("Dependencies");
		for (Iterator i = project.getTaskOutlineIterator(); i.hasNext() == true;)
		{
			Task task = (Task)i.next(); //ResourceImpl to have the EnterpriseResource link

			if ((task.getProjectId() != projectId) || task.isExternal()) // skip if in another project, don't write externals to server
			{
				continue;
			}

			TaskData taskData = (TaskData)myTaskLinker.getTransformationMap().get( task );

			if (taskData == null)
			{
				continue;
			}

			Iterator j = task.getPredecessorList().iterator();

			if (j.hasNext())
			{
				List predecessors = new ArrayList();

				while (j.hasNext())
				{
					Dependency dependency = (Dependency)j.next();
					LinkData linkData;
					boolean dirty = !incremental || dependency.isDirty();

					if (dirty)
					{
						linkData = (LinkData)serialize( dependency, LinkData.FACTORY, null );
					}
					else
					{
						//linkData=new LinkData();
						unchangedLinks.add( dependency.getPredecessorId() );
						unchangedLinks.add( dependency.getSuccessorId() );

						continue;
					}

					linkData.setDirty( dependency.isDirty() );

					//linkData.setExternalId(dependency.getExternalId());
					if (flatLinks == null)
					{
						linkData.setSuccessor( taskData );
					}
					else
					{
						linkData.setSuccessorId( taskData.getUniqueId() );
					}

					Task pred = (Task)dependency.getPredecessor();
					TaskData predData = (TaskData)myTaskLinker.getTransformationMap().get( pred );

					if (flatLinks == null)
					{
						if ((predData != null) && !predData.isExternal())
						{
							linkData.setPredecessor( predData );
						}
						else
						{
							linkData.setPredecessorId( pred.getUniqueId() ); // external link
						}

						predecessors.add( linkData );
					}
					else
					{
						linkData.setPredecessorId( pred.getUniqueId() );
						flatLinks.add( linkData );
					}
				}

				if (flatLinks == null)
				{
					taskData.setPredecessors( predecessors );
				}
			}
		}

		//depCount.dump();
		if (incremental)
		{
			//if (unchangedTasks.size()>0){
			Collection tasks = projectData.getTasks();

			if (tasks != null)
			{
				for (Iterator i = tasks.iterator(); i.hasNext();)
				{
					TaskData t = (TaskData)i.next();

					if (!t.isDirty() && !t.isMoved())
					{
						unchangedTasks.add( t.getUniqueId() );
						i.remove();
					}
				}
			}

			if (unchangedTasks.size() > 0)
			{
				long[] a = new long[ unchangedTasks.size() ];
				int i = 0;

				for (long l : unchangedTasks)
				{
					a[ i++ ] = l;
				}

				projectData.setUnchangedTasks( a );
			}

			if (unchangedLinks.size() > 0)
			{
				long[] a = new long[ unchangedLinks.size() ];
				int i = 0;

				for (long l : unchangedLinks)
				{
					a[ i++ ] = l;
				}

				projectData.setUnchangedLinks( a );
			}
		}
	}

	public DataObject serialize( 
		DataObject obj,
		SerializedDataObjectFactory factory,
		Count count )
		throws IOException
	{
		SerializedDataObject data = SerializeUtil.serialize( obj, factory );

		if (TMP_FILES)
		{
			writeTmpFile( data, count );
		}

		byte[] bytes = data.getSerialized();

		if (count != null)
		{
			count.add( (bytes == null)
				? 0
				: bytes.length );
		}

		return data;
	}

	protected Collection serialize( 
		Collection objs,
		SerializedDataObjectFactory factory )
		throws IOException
	{
		if (objs == null)
		{
			return new ArrayList(); // a user crashed here due to null objs.
		}

		Collection r = new ArrayList(objs.size());

		for (Iterator i = objs.iterator(); i.hasNext();)
		{
			r.add( SerializeUtil.serialize( (DataObject)i.next(), factory ) );
		}

		return r;
	}

	public DocumentData serializeDocument( 
		Project project )
		throws Exception
	{
		return serializeProject( project, null, null, false, null );
	}

	public ProjectData serializeProject( 
		Project project )
		throws Exception
	{
		return serializeProject( project, null, null, false, null );
	}

	public ProjectData serializeProject( 
		final Project _project,
		final Collection _flatAssignments,
		final Collection _flatLinks,
		boolean _incremental,
		final SerializeOptions _options )
		throws Exception
	{
		if (TMP_FILES)
		{
			initTmpDir();
		}

		if (_project.isForceNonIncremental() == true)
		{
			_incremental = false;
		}

		final boolean incrementalDistributions = _incremental && !_project.isForceNonIncrementalDistributions();

		//   	calendars.clear();
		final Count projectCount = new Count( "Project" );

		//if (globalIdsOnly) makeGLobal(project);
		final ProjectData projectData = (ProjectData)serialize( _project, ProjectData.FACTORY, projectCount );

		if (_project.isForceNonIncremental() == true)
		{
			projectData.setVersion( 0 );
		}

		projectData.setMaster( _project.isMaster() );

//      projectData.setExternalId( project.getExternalId() );

		//exposed attributes
//      projectData.setAttributes(SpreadSheetFieldArray.convertFields(project, "projectExposed", 
//			new Transformer()
//		{
//			public Object transform(Object value) 
//			{
//				if (value instanceof Money) 
//				{
//					return ((Money)value).doubleValue();
//				}
//        		
//				return null;
//        	}
//        }));
		
		projectCount.dump();

		//resources
		final Map resourceMap = saveResources( _project, projectData );

		//tasks
		saveTasks( _project, projectData, resourceMap, _flatAssignments, _flatLinks, _incremental, _options );

		//distribution
		final long t = System.currentTimeMillis();
		Collection<DistributionData> dist = (Collection<DistributionData>)(new DistributionConverter()).createDistributionData( 
			_project, incrementalDistributions );

		if (dist == null)
		{
			dist = new ArrayList<DistributionData>();
		}

		projectData.setDistributions( dist );
		projectData.setIncrementalDistributions( incrementalDistributions );

		TreeMap<DistributionData,DistributionData> distMap = _project.getDistributionMap();

		if (distMap == null)
		{
			distMap = new TreeMap<DistributionData,DistributionData>( new DistributionComparator() );
			_project.setDistributionMap( distMap );
		}

		final TreeMap<DistributionData,DistributionData> newDistMap = new TreeMap<DistributionData,DistributionData>(
			new DistributionComparator() );

		//ArrayList<DistributionData> toInsertInOld = new ArrayList<DistributionData>();

		//insert, update dist
		for (Iterator<DistributionData> i = dist.iterator(); i.hasNext();)
		{
			DistributionData d = i.next();

			if (incrementalDistributions)
			{
				DistributionData oldD = distMap.get( d );

				if (oldD == null)
				{
					d.setStatus( DistributionData.INSERT );
				}
				else
				{
					if ((oldD.getWork() == d.getWork()) && (oldD.getCost() == d.getCost()))
					{
						//System.out.println(d+" did not change");
						d.setStatus( 0 );
						i.remove();
					}
					else
					{
						d.setStatus( DistributionData.UPDATE );
					}
				}
			}
			else
			{
				d.setStatus( DistributionData.INSERT );
			}

			newDistMap.put( d, d );
		}

		//remove dist
		if (incrementalDistributions && (distMap.size() > 0))
		{
			Set<Long> noChangeTaskIds = new HashSet<Long>();

			Task task;

			for (Iterator i = _project.getTaskOutlineIterator(); i.hasNext();)
			{
				task = (Task)i.next();

				if (_incremental && !task.isDirty())
				{
					noChangeTaskIds.add( task.getUniqueId() );
				}
			}

//        	for (Iterator i=projectData.getTasks().iterator();i.hasNext();){
//        		TaskData task=(TaskData)i.next();
//        		if (!task.isDirty()) noChangeTaskIds.add(task.getUniqueId());
//        	}
			
			for (Iterator<DistributionData> i = distMap.values().iterator(); i.hasNext();)
			{
				DistributionData d = i.next();

				if (newDistMap.containsKey( d ))
				{
					continue;
				}

				if (noChangeTaskIds.contains( d.getTaskId() ))
				{
					d.setStatus( 0 );
					newDistMap.put( d, d );
				}
				else
				{
					d.setStatus( DistributionData.REMOVE );
					dist.add( d );
				}
			}
		}

		_project.setNewDistributionMap( newDistMap );
		System.out.println( "Distributions generated in " + (System.currentTimeMillis() - t) + " ms" );

		// send project field values to server too
		final HashMap<String,Object> fieldValues = FieldValues.getValues( FieldDictionary.getInstance().getProjectFields(), 
			_project );

		if (_project.getContainingSubprojectTask() != null)
		{ 
			// special case in which we want to use the duration from subproject task
			Object durationFieldValue = Configuration.getFieldFromId( "Field.duration" ).getValue( 
				_project.getContainingSubprojectTask(), null );
			fieldValues.put( "Field.duration", durationFieldValue );
		}

		projectData.setFieldValues( fieldValues );
		projectData.setGroup( _project.getGroup() );
		projectData.setDivision( _project.getDivision() );
		projectData.setExpenseType( _project.getExpenseType() );
		projectData.setProjectType( _project.getProjectType() );
		projectData.setProjectStatus( _project.getProjectStatus() );
		projectData.setExtraFields( _project.getExtraFields() );
		projectData.setAccessControlPolicy( _project.getAccessControlPolicy() );
		projectData.setCreationDate( _project.getCreationDate() );
		projectData.setLastModificationDate( _project.getLastModificationDate() );

		//  	System.out.println("done serialize project " + project);

		//        Collection<DistributionData> dis=(Collection<DistributionData>)projectData.getDistributions();
		//        for (DistributionData d: dis) System.out.println("Dist: "+d.getTimeId()+", "+d.getType()+", "+d.getStatus());

		//        project.setNewTaskIds(null);
		//        if (projectData.getTasks()!=null){
		//        	Set<Long> ids=new HashSet<Long>();
		//        	project.setNewTaskIds(ids);
		//        	for (TaskData task:(Collection<TaskData>)projectData.getTasks()){
		//        		ids.add(task.getUniqueId());
		//        	}
		//        }
		//        long[] unchangedTasks=projectData.getUnchangedTasks();
		//        if (unchangedTasks!=null){
		//        	Set<Long> ids=project.getNewTaskIds();
		//        	if (ids==null){
		//        		ids=new HashSet<Long>();
		//        		project.setNewTaskIds(ids);
		//        	}
		//        	for (int i=0;i<unchangedTasks.length;i++) ids.add(unchangedTasks[i]);
		//        }
		//
		//        project.setNewLinkIds(null);
		//        if (flatLinks!=null){
		//        	Set<DependencyKey> ids=new HashSet<DependencyKey>();
		//        	project.setNewLinkIds(ids);
		//        	for (LinkData link:(Collection<LinkData>)flatLinks){
		//        		ids.add(new DependencyKey(link.getPredecessorId(),link.getSuccessorId()/*,link.getExternalId()*/));
		//        	}
		//        }
		//        long[] unchangedLinks=projectData.getUnchangedLinks();
		//        if (unchangedLinks!=null){
		//        	Set<DependencyKey> ids=project.getNewLinkIds();
		//        	if (ids==null){
		//        		ids=new HashSet<DependencyKey>();
		//        		project.setNewLinkIds(ids);
		//        	}
		//        	for (int i=0;i<unchangedLinks.length;i+=2) ids.add(new DependencyKey(unchangedLinks[i],unchangedLinks[i+1]));
		//        }
		_project.setNewIds();

		return projectData;
	}

	public static DataObject serializeSingle( 
		DataObject obj,
		SerializedDataObjectFactory factory,
		Count count )
		throws IOException
	{
		SerializedDataObject data = SerializeUtil.serialize( obj, factory );
		byte[] bytes = data.getSerialized();

		if (count != null)
		{
			count.add( (bytes == null)
				? 0
				: bytes.length );
		}

		return data;
	}

	public static void setEnterpriseResources( 
		Collection<EnterpriseResourceData> _resources,
		ResourcePool _resourcePool,
		Session _reindex )
		throws IOException, 
			ClassNotFoundException
	{
		if (_resources != null)
		{
			Map<EnterpriseResourceData,Node<?>> resourceNodeMap = new HashMap();

			for (Iterator<EnterpriseResourceData> i = _resources.iterator(); i.hasNext() == true;)
			{
				final EnterpriseResourceData resourceData = i.next();
				final ResourceImpl resource = deserializeResourceAndAddToPool( resourceData, _resourcePool, _reindex );

				//TODO Lolo - why is this line here given the line below?
				//resourceNodeMap.put( resourceData,resource.getGlobalResource() ); 
				resourceNodeMap.put( resourceData, NodeFactory.getInstance().createNode( resource ) );
			}

			//NodeModel model=resourcePool.getResourceOutline();
			for (Iterator<EnterpriseResourceData> i = _resources.iterator(); i.hasNext() == true;)
			{
				final EnterpriseResourceData resourceData = i.next();
				final EnterpriseResourceData parentData = resourceData.getParentResource();
				final Node<?> node = resourceNodeMap.get( resourceData );
				final Node parentNode = (parentData == null)
					? null
					: ((Node)resourceNodeMap.get( parentData ));

				//model.add(parentNode,node,(int)resourceData.getChildPosition(),NodeModel.SILENT); //global update instead
				_resourcePool.addToDefaultOutline( parentNode, node, (int)resourceData.getChildPosition(), false );
				((ResourceImpl)node.getValue()).getGlobalResource().setResourcePool( _resourcePool );
			}

			_resourcePool.getResourceOutline().getHierarchy().cleanVoidChildren();
		}
	}

	private static void setRoles( 
		ResourceImpl resource,
		ResourceData resourceData )
	{
		resource.setRole( resourceData.getRole() );

		int[] authRoles = resourceData.getEnterpriseResource().getAuthorizedRoles();

		if (authRoles != null)
		{
			EnterpriseResource globalResource = resource.getGlobalResource();
			globalResource.setDefaultRole( (authRoles.length > 0)
				? authRoles[ 0 ]
				: ApplicationUser.INACTIVE );

			Set<Integer> roles = new HashSet<Integer>();

			for (int i = 0; i < authRoles.length; i++)
			{
				roles.add( UserUtil.toExtendedRole( authRoles[ i ], resource.isUser() ) );
			}

			globalResource.setAuthorizedRoles( roles );
			globalResource.setLicense( resourceData.getEnterpriseResource().getLicense() );
			globalResource.setLicenseOptions( resourceData.getEnterpriseResource().getLicenseOptions() );
		}
	}

	public void setStuffForPODDeserialization( 
		Project _existingProject,
		Map<Long,Resource> _localResourceMap )
	{
		myExistingProject = _existingProject;
		myLocalResourceMap = _localResourceMap;
	}

	protected void writeTmpFile( 
		SerializedDataObject data,
		Count count )
		throws IOException
	{
		if ((tmpDir != null) && (count != null))
		{
			try
			{
				File f = new File(tmpDir, data.getPrefix() + "_" + count.count);
				FileOutputStream out = new FileOutputStream( f );

				if (data.getSerialized() != null)
				{
					out.write( data.getSerialized() );
				}

				out.close();
			}
			catch (FileNotFoundException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	class Count
	{
		public Count( 
			String typeLabel )
		{
			this.typeLabel = typeLabel;
		}

		void add( 
			int s )
		{
			count++;
			size += s;

			if (s < min)
			{
				min = s;
			}

			if (s > max)
			{
				max = s;
			}
		}

		void dump()
		{
			System.out.println( "Serialized " + count + " " + typeLabel + ", total=" + size + ", average=" +
				((count == 0)
				? 0
				: (size / count)) + ", min=" + min + ", max=" + max );
		}

		void reset()
		{
			count = 0;
			size = 0;
			max = 0;
			min = Integer.MAX_VALUE;
		}

		String typeLabel;
		int count;
		int max;
		int min = Integer.MAX_VALUE;
		int size;
	}

	private abstract static class IdClosure
		implements Closure
	{
		long id = 1;
	}

	public static final boolean TMP_FILES = false;
	protected String myTestString = "This is a test.";
	protected File tmpDir = null;
	
	protected Linker<Resource,ResourceData> myResourceLinker = 
		new ResourceLinker<Resource,ResourceData>()
	{
		@Override
		public ResourceData addTransformedObjects( 
			final Resource _from )
			throws IOException, 
				UniqueIdException
		{
			Project project = (Project)myParent;
//			ResourceImpl resource = (ResourceImpl)_from;

			final ResourceData resourceData = new ResourceData();
			resourceData.setUniqueId( _from.getUniqueId() );
			resourceData.setRole( _from.getRole() );

			//ResourceImpl doesn't contain anything. Not serialized in V1
			//ResourceData resourceData = (ResourceData)serialize( (ResourceImpl)child, ResourceData.FACTORY, null );
			EnterpriseResourceData enterpriseResourceData;

			if (_from.isDefault() == true)
			{
				return null;
			}

//        	return transformationMap;//enterpriseResourceData=null;
			else if (project.isMaster() == true)
			{
				enterpriseResourceData = (EnterpriseResourceData)serialize( _from.getGlobalResource(),
						EnterpriseResourceData.FACTORY, null );
			}
			else
			{
				enterpriseResourceData = new EnterpriseResourceData(); //no need to save data
				enterpriseResourceData.setUniqueId( _from.getGlobalResource().getUniqueId() );
			}

			String emailAddress = _from.getGlobalResource().getEmailAddress();
			enterpriseResourceData.setEmailAddress( ((emailAddress == null) || (emailAddress.length() == 0))
				? null
				: emailAddress ); //this is used to map a new user to an existing resource
			resourceData.setEnterpriseResource( enterpriseResourceData );

			// the resource map uses ids now
			myTransformationMap.put( Long.valueOf( _from.getUniqueId() ), resourceData ); 

			return resourceData;
		}

		@Override
		public void executeFinally()
		{
			((ProjectData)getTransformedParent()).setResources( myTransformed );
		}

		@Override
		public boolean addOutlineElement( 
			Object outlineChild,
			Object outlineParent,
			long position )
		{
			if (outlineChild instanceof VoidNodeImpl == true)
			{
				return false;
			}

			ResourceData resourceData = (ResourceData)getTransformationMap().get( 
				Long.valueOf( ((Resource)outlineChild).getUniqueId() ) );
			ResourceData parentData = (outlineParent == null)
				? null
				: (ResourceData)getTransformationMap().get( new Long(((Resource)outlineParent).getUniqueId()) );

			// enterprise resource version
			EnterpriseResourceData enterpriseResourceData = resourceData.getEnterpriseResource(); 
			enterpriseResourceData.setParentResource( (parentData == null)
				? null
				: parentData.getEnterpriseResource() );
			enterpriseResourceData.setChildPosition( position );

			return true;
		}
	};

	protected final TaskLinker myTaskLinker = new TaskLinker()
	{
		@Override
		public Object addTransformedObjects( 
			Object child )
			throws IOException, UniqueIdException
		{
			//Project project=(Project)parent;
			//ProjectData projectData=(ProjectData)transformedParent;
//			NormalTask task = (NormalTask)child;
			Task task = (Task)child;
			final Project project = ((Project)getParent());

			if ((task.getOwningProject() != project) || task.isExternal()) // don't do tasks in subprojects, dont include externals
			{
				return null;
			}

			final Map resourceMap = (Map)myArgs[ 0 ];
			final TaskData taskData;
			final boolean taskDirty = !myIncremental || task.isDirty();

			if (taskDirty || Environment.isNoPodServer())
			{
				if (Environment.isNoPodServer())
				{
					final List persistedAssignments = new ArrayList();
					Project.forAssignments( task,
						new Project.AssignmentClosure()
					{
						@Override
						public void execute( 
							Assignment assignment,
							int s )
						{
							ResourceImpl r = (ResourceImpl)assignment.getResource();

							//if (r.isDefault()&&s==Snapshottable.CURRENT){
							if (r.isDefault())
							{
								persistedAssignments.add( new PersistedAssignment(assignment, s) ); //save the default assignment in the task
							}
							else if (s != Snapshottable.CURRENT)
							{
								persistedAssignments.add( new PersistedAssignment(assignment, s, r.getUniqueId()) );
							}
						}
					} );

					if (persistedAssignments.size() > 0)
					{
						task.setPersistedAssignments( persistedAssignments );
					}
				}

				taskData = (TaskData)serialize( task, TaskData.FACTORY, null );
				task.setPersistedAssignments( null );

				taskData.setNotes( task.getNotes() ); //assignments notification
														// this code is to set fields which are exposed in database
														//    	        taskData.setStart(task.getStart());
														//    	        taskData.setFinish(task.getEnd());
														//    	        taskData.setBaselineStart(task.getBaselineStartOrZero());
														//    	        taskData.setBaselineFinish(task.getBaselineFinishOrZero());
														//    	        taskData.setCompletedThrough(task.getCompletedThrough());
														//    	        taskData.setPercentComplete(task.getPercentComplete());

				if (!taskDirty && Environment.isNoPodServer())
				{
					taskData.setSerialized( null );
				}
			}
			else
			{
				taskData = new TaskData();
				taskData.setUniqueId( task.getUniqueId() );

				//            	getUnchanged().add(task.getUniqueId());
				//            	return null;
			}

			// set the status of the task using dirty flag
			taskData.setStatus( taskDirty
				? SerializedDataObject.UPDATE
				: 0 );

			taskData.setProjectId( task.getProjectId() );

			if (task.isSubproject())
			{
				taskData.setSubprojectId( ((SubProj)task).getSubprojectUniqueId() );
			}

			//exposed attributes
			if (Environment.isNoPodServer())
			{
				addPreparedAttributes( taskData, task, project.getTaskOutline(), myOptions );
			}

			//assignments
			final Collection assignments = (flatAssignments == null)
				? new ArrayList()
				: flatAssignments;

			if (taskDirty)
			{
				Project.forAssignments( task,
					new Project.AssignmentClosure()
				{
					@Override
					public void execute( 
						Assignment assignment,
						int s )
					{
						try
						{
							ResourceImpl r = (ResourceImpl)assignment.getResource();

							//if (r.isDefault()) continue;
							AssignmentData assignmentData = (AssignmentData)serialize( assignment,
									AssignmentData.FACTORY, null );
							assignmentData.setStatus( SerializedDataObject.UPDATE );

							//                    assignmentData.setStatus(assignment.isDirty() || taskDirty ? SerializedDataObject.UPDATE : 0);
							if (flatAssignments == null)
							{
								assignmentData.setTask( taskData );
							}
							else
							{
								assignmentData.setTaskId( taskData.getUniqueId() );
							}

							EnterpriseResourceData enterpriseResourceData = (r.isDefault())
								? null
								: ((ResourceData)resourceMap.get( new Long(r.getUniqueId()) )).getEnterpriseResource();

							if (flatAssignments == null)
							{
								assignmentData.setResource( enterpriseResourceData );
							}
							else
							{
								assignmentData.setResourceId( (enterpriseResourceData == null)
									? (-1L)
									: enterpriseResourceData.getUniqueId() );
							}

							assignmentData.setSnapshotId( s );

							assignmentData.setCachedStart( new Date(assignment.getStart()) );
							assignmentData.setCachedEnd( new Date(assignment.getEnd()) );
							assignmentData.setTimesheetStatus( assignment.getTimesheetStatus() );
							assignmentData.setLastTimesheetUpdate( new Date(assignment.getLastTimesheetUpdate()) );
							assignmentData.setWorkflowState( assignment.getWorkflowState() );
							assignmentData.setPercentComplete( assignment.getPercentComplete() ); //assignments notification
							assignmentData.setDuration( assignment.getDuration() ); //assignments notification

							assignments.add( assignmentData );

							//exposed attributes
							if (Environment.isNoPodServer())
							{
								addPreparedAttributes( assignmentData, assignment, project.getTaskOutline(), myOptions );
							}
						}
						catch (IOException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				} );
			}

			if (flatAssignments == null)
			{
				taskData.setAssignments( assignments );
			}

			myTransformationMap.put( task, taskData );

			return taskData;
		}

		@Override
		public void executeFinally()
		{
			((ProjectData)getTransformedParent()).setTasks( myTransformed );
		}

		@Override
		public boolean addOutlineElement( 
			Object outlineChild,
			Object outlineParent,
			long position )
		{
			TaskData taskData = (TaskData)getTransformationMap().get( outlineChild );

			//voidNodes
			if (outlineChild instanceof VoidNodeImpl)
			{ //TODO remove not called?
				taskData = new TaskData();
				((ProjectData)getTransformedParent()).getTasks().add( taskData );
			}

			if (taskData == null) // in case belongs to different project
			{
				return false;
			}

			TaskData parentData = (outlineParent == null)
				? null
				: ((TaskData)getTransformationMap().get( outlineParent ));

			//			System.out.println("parent "+parentData);
			if ((parentData != null) && parentData.isSubproject())
			{
				//				System.out.println("sub " + parentData.getName());
				parentData = null;
			}

			//if (taskData.isDirty()){
			taskData.setParentTask( parentData );
			taskData.setChildPosition( position );

			//}
			if (outlineChild instanceof Task)
			{
				Task task = (Task)outlineChild;
				long parentId = (parentData == null)
					? (-1L)
					: parentData.getUniqueId();

				if ((parentId != task.getLastSavedParentId()) || (position != task.getLastSavedPosistion()))
				{
					taskData.setMoved( true );
				}
			}

			return true;
		}
	};

	Map<Long,Resource> myLocalResourceMap;

	//DEF165936: 	Projity: .pod file import fails mapped to resource with modified calendar
	//the only way i found to make this work was to pass over the original ResourceImpls mapped by selected resource Id
	private Project myExistingProject = null;
}
