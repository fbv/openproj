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

import com.projity.functor.PairClosure;

import com.projity.grouping.core.Node;
import com.projity.grouping.core.model.AssignmentNodeModel;
import com.projity.grouping.core.model.DefaultNodeModel;
import com.projity.grouping.core.model.NodeModel;
import com.projity.grouping.core.model.NodeModelUtil;
import com.projity.grouping.core.summaries.DeepChildWalker;

import com.projity.job.Job;
import com.projity.job.JobRunnable;

import com.projity.pm.assignment.Assignment;
import com.projity.pm.resource.ResourcePool;
import com.projity.pm.resource.ResourcePoolFactory;

import com.projity.server.data.DataObject;
import com.projity.server.data.DataUtil;

import com.projity.session.CreateOptions;
import com.projity.session.LoadOptions;
import com.projity.session.LocalSession;
import com.projity.session.SaveOptions;
import com.projity.session.Session;
import com.projity.session.SessionFactory;

import com.projity.strings.Messages;

import com.projity.undo.DataFactoryUndoController;

import com.projity.util.Alert;
import com.projity.util.Environment;

import java.text.MessageFormat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.collections.Closure;

import org.openproj.domain.task.Task;

/**
 *
 */
public class ProjectFactory
{
	private ProjectFactory()
	{
		portfolio = new Portfolio(this);
	}

	public synchronized void addClosingProject( 
		long id )
	{
		closingProjects.add( new Long(id) );
	}

	public synchronized void addClosingProjects( 
		Collection ids )
	{
		closingProjects.addAll( ids );
	}

	public synchronized void addLoadingProject( 
		long id )
	{
		loadingProjects.add( new Long(id) );
	}

	public void addProject( 
		Project project,
		boolean verify )
	{
		addProject( project, true, verify );
	}

	public void addProject( 
		Project project,
		boolean createJob,
		boolean verify )
	{
		portfolio.addProject( project, createJob, verify );
	}

	public void cancelChanges( 
		Project project,
		boolean sync )
	{
		reload( project, sync );
	}

	public static void cleanUp()
	{
		projectFactory = null;
		untitledCount = 0;
	}

	public static ProjectFactory createInstance()
	{
		return new ProjectFactory();
	}

	//CREATE PROJECTS

//	public Project createProject(String name,boolean local) {
//		return createProject(name,local,true,true);
//	}
//	public Project createProject(String name, boolean local, boolean addResources,boolean verify) {
//		Project project = createProject(null,local,name,addResources,verify);
//		return project;
//	}
//
	
	public Project createProject()
	{
		CreateOptions opt = new CreateOptions();
		opt.setLocal( Environment.getStandAlone() );
		opt.setName( Messages.getString( "Text.Untitled" ) + " " + ++untitledCount );

		return createProject( opt );
	}

	public Project createProject( 
		final CreateOptions opt )
	{
		JobRunnable runnable = 
			new JobRunnable("Local: create Project")
		{
			@Override
			public Object run()
				throws Exception
			{
				return createProjectAsync( opt );
			}
		};

		if ((opt == null) || opt.isSync())
		{
			Job job = new Job(SessionFactory.getInstance().getJobQueue(), "createProject", "Creating project...", false);
			job.addRunnable( runnable );
			job.addSync();
			SessionFactory.getInstance().schedule( job );

			try
			{
				Project project = (Project)job.waitResult();
				System.out.println( "Project returned end lock" );

				return project;
			}
			catch (Exception e)
			{ //Forward exception + Alert
				e.printStackTrace();

				return null;
			}
		}
		else
		{
			try
			{
				return (Project)runnable.run();
			}
			catch (Exception e)
			{
				e.printStackTrace();

				return null;
			}
		}
	}

//	public Project createProject(boolean addResources,boolean local) {
//		Project project = createProject(Messages.getString("Text.Untitled") + " " + ++untitledCount,local,addResources,true);
//		return project;
//	}

//	public Project createProject(ResourcePool resourcePool, boolean local, String name) {
//		return createProject(resourcePool,local,name,!local,true);
//	}

//	public Project createProject(ResourcePool resourcePool, boolean local, String name, boolean addResources,boolean verify) {
	
	private Project createProjectAsync( 
		CreateOptions opt )
	{
		DataFactoryUndoController undoController = new DataFactoryUndoController();
		ResourcePool resourcePool = opt.getResourcePool();

		if (resourcePool == null)
		{
			resourcePool = ResourcePoolFactory.getInstance().createResourcePool( opt.getName(), undoController );
			resourcePool.setLocal( opt.isLocal() );
		}

		Project project = Project.createProject( resourcePool, undoController );
		undoController.setDataFactory( project.getNodeModelDataFactory() );
		project.setName( opt.getName() );

		if (opt.isLocal())
		{
			project.setMaster( true );
		}

		//Don't forget to modify Serializer.deserialize too
		if (opt.isAddResources() && !project.isLocal())
		{
			try
			{
				Session session = SessionFactory.getInstance().getSession( false );
				List resources;

				if (((Boolean)SessionFactory.callNoEx( session, "isLocalAccess", null, null )).booleanValue())
				{
					resources = (List)SessionFactory.call( session, "retrieveResourceHierarchy", null, null );
				}
				else
				{
					resources = new ArrayList();

					Job job = (Job)SessionFactory.callNoEx( session, "getLoadResourceHierarchyJob",
							new Class[]
							{
								boolean.class,
								List.class
							}, new Object[]
							{
								true,
								resources
							} );
					job.addSync();
					session.schedule( job );

					//job.waitResult();
				}

				DataUtil.setEnterpriseResources( resources, resourcePool );
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		project.setInitialized( true );

		if (Environment.isAddSummaryTask() /*&&Environment.isScripting()*/)
		{ //only for scripting
			project.setSummaryTaskEnabled( true );

			Task task = project.createScriptedTask( true, !Environment.isTesting() );
			task.setRoot( true );
		}

		//two following lines inverted to have a NodeModel with dataFactory set in DocumentFrame
		((DefaultNodeModel)project.getTaskOutline()).setDataFactory( project.getNodeModelDataFactory() );
		addProject( project, !opt.isSync() && !Environment.isTesting(), opt.isVerify() );
		System.out.println( "Project returned" );

		return project;
	}

	public void doRemoveProject( 
		Project project,
		boolean calledFromSwing )
	{
		Job job = projectFactory.getPortfolio().getRemoveProjectJob( project, calledFromSwing );

		if (job != null)
		{
			SessionFactory.getInstance().getSession( project.isLocal() ).schedule( job );
			portfolio.handleExternalTasks( project, false, false ); // external link handling
																	//			project.setLocked(false);
		}
	}

	public Project findFromId( 
		long id )
	{
		return portfolio.findByUniqueId( id );
	}

	public void forceRemove( 
		Project p )
	{
		p.getResourcePool().removeProject( p );
		p.disconnect();
		portfolio.getObjectEventManager().fireDeleteEvent( this, p );

		Node node = portfolio.getNodeModel().search( p );
		portfolio.getNodeModel().remove( node, NodeModel.EVENT );

		//		p.setLocked(false);
	}

	//CLOSE PROJECTS
	public Job getCloseProjectsOnServerJob( 
		Project project )
	{
		// Save the project and all of its subprojects
		final List projects = new ArrayList();
		DeepChildWalker.recursivelyTreatBranch( portfolio.getNodeModel(), project,
			new Closure()
			{
				public void execute( 
					Object arg0 )
				{
					Object impl = ((Node)arg0).getValue();

					if (impl instanceof Project)
					{
						projects.add( impl );
					}
				}
			} );

		if (projects.size() > 0)
		{
			Session session = SessionFactory.getInstance().getSession( project.isLocal() ); //assume same type for subprojets
			Job job = session.getCloseProjectsJob( projects );

			return job;
		}

		return null;
	}

	public Job getCloseProjectsOnServerJob( 
		Collection projects )
	{
		List<Project> localProjects = new ArrayList<Project>();
		List<Project> serverProjects = new ArrayList<Project>();

		for (Project project : (Collection<Project>)projects)
		{
			if (project.isReadOnly())
			{
				continue;
			}

			if (project.isLocal())
			{
				localProjects.add( project );
			}
			else
			{
				serverProjects.add( project );
			}
		}

		Job job = null;

		if (localProjects.size() > 0)
		{
			job = SessionFactory.getInstance().getLocalSession().getCloseProjectsJob( projects );
		}

		if (serverProjects.size() > 0)
		{
			Job j = SessionFactory.getInstance().getSession( false ).getCloseProjectsJob( projects );

			if (job == null)
			{
				job = j;
			}
			else
			{
				job.addJob( j );
			}
		}

		return job;
	}

	public Collection getDirtyProjectList()
	{
		return portfolio.getDirtyProjectList();
	}

	public Project getImportInto()
	{
		return portfolio.getImportInto();
	}

	public static synchronized ProjectFactory getInstance()
	{
		if (projectFactory == null)
		{
			projectFactory = new ProjectFactory();
		}

		return projectFactory;
	}

	public boolean getLock( 
		Project project,
		boolean force )
	{
		Session session = SessionFactory.getInstance().getSession( false );

		return session.getLock( project, force );
	}

	public synchronized Set getOpenOrLoadingProjects()
	{
		final Set projectIds = new HashSet();
		ProjectFactory.getInstance().getPortfolio().forProjects( new Closure()
			{
				public void execute( 
					Object impl )
				{
					Project project = (Project)impl;
					projectIds.add( new Long(project.getUniqueId()) );
				}
			} );
		projectIds.addAll( loadingProjects );
		projectIds.removeAll( closingProjects );

		return projectIds;
	}

	/**
	 * @return Returns the portfolio.
	 */
	public Portfolio getPortfolio()
	{
		return portfolio;
	}

	public static Object getProjectData( 
		long projectId )
	{
		Session session = SessionFactory.getInstance().getSession( false );

		return SessionFactory.callNoEx( session, "getProjectData", new Class[]
			{
				Long.class
			}, new Object[]
			{
				projectId
			} );

		//	getProjectData(projectId);
	}

	/**
	 * @param project
	 * @param allowCancel
	 * @param prompt
	 * @return null if cancelled
	 */
	public Job getRemoveProjectJob( 
		final Project project,
		boolean allowCancel,
		boolean prompt,
		boolean calledFromSwing )
	{
		Job job = null;

		if (prompt && project.needsSaving() && !promptDisabled)
		{
			//			final boolean[] lock=new boolean[]{false};
			//				SwingUtilities.invokeLater(new Runnable(){
			//					public void run(){
			//						Alert.okCancel("test");
			//						synchronized (lock) {
			//							lock[0]=true;
			//							lock.notifyAll();
			//						}
			//				    }
			//				});
			//			synchronized(lock){
			//				while (!lock[0]){
			//					try{
			//							lock.wait();
			//						}catch (InterruptedException e) {}
			//				}
			//			}
			int promptResult = promptForSave( project, allowCancel );

			if (promptResult == JOptionPane.YES_OPTION)
			{
				SaveOptions opt = new SaveOptions();
				opt.setLocal( project.isLocal() );

				if (project.isLocal())
				{
					String fileName = project.getFileName();

					if (fileName == null)
					{
						fileName = SessionFactory.getInstance().getLocalSession()
												 .chooseFileName( true, project.getGuessedFileName() );
					}

					if (fileName == null)
					{
						return null;
					}

					project.setFileName( fileName );
					opt.setFileName( fileName );
					opt.setImporter( LocalSession.getImporter( project.getFileType() ) );
				}

				job = getSaveProjectJob( project, opt );
			}
			else if (promptResult == JOptionPane.CANCEL_OPTION)
			{
				return null;
			}
		}

		final ArrayList toRemove = new ArrayList();
		final ArrayList projects = new ArrayList();
		DeepChildWalker.recursivelyTreatBranch( portfolio.getNodeModel(), project,
			new Closure()
			{
				public void execute( 
					Object arg0 )
				{
					Node node = (Node)arg0;

					if (node == null)
					{
						System.out.println( "Null node in treating branch in proj factory" );

						return;
					}

					Object impl = node.getValue();

					if (!(impl instanceof Project))
					{
						return;
					}

					final Project p = (Project)impl;
					toRemove.add( node );

					if (Environment.getStandAlone() || project.isLockable())
					{
						projects.add( p );
					}
				}
			} );

		Job closeProjectJob = getCloseProjectsOnServerJob( projects );

		if (closeProjectJob == null)
		{
			closeProjectJob = new Job(SessionFactory.getInstance().getJobQueue(), "closeProjects", "Closing...", false);
		}

		if (job == null)
		{
			job = closeProjectJob;
		}
		else
		{
			job.addJob( closeProjectJob );
		}

		job.addRunnable( new JobRunnable("Local: closeProjects")
			{
				public Object run()
					throws Exception
				{
					Iterator i = toRemove.iterator();

					while (i.hasNext())
					{
						Node node = (Node)i.next();
						Project p = (Project)node.getValue();
						portfolio.handleExternalTasks( p, false, false ); // external link handling
						p.getResourcePool().removeProject( p );
						p.disconnect();
						portfolio.getObjectEventManager().fireDeleteEvent( this, p );

						//if (!(Environment.isNoPodServer()&&Environment.isScripting()))
						portfolio.getNodeModel().remove( node, NodeModel.EVENT );

						// bug with scripting
						removeClosingProject
						// bug with scripting
						( project.getUniqueId() );
					}

					System.gc(); // clean up memory used by projects

					return null; //return not used anyway
				}
			}, 
		/*!calledFromSwing*/ false, false, calledFromSwing, false );

		return job;
	}

	public Job getSaveProjectJob( 
		final Project project,
		final SaveOptions opt )
	{
		opt.setSync( opt.isSync() || Environment.isScripting() );

		// Save the project and all of its subprojects
		final List projects = new ArrayList();
		DeepChildWalker.recursivelyTreatBranch( portfolio.getNodeModel(), project,
			new Closure()
			{
				boolean dirty = false;

				@Override
				public void execute( 
					Object arg0 )
				{
					Node n = (Node)arg0;

					if (n == null)
					{
						System.out.println( 
							"null node found in project branch of getSaveProjectJob-recursively treat branch - ignoring" );

						return;
					}

					Object impl = n.getValue();

					if (impl instanceof Project)
					{
						Project project = (Project)impl;

						if (((Node)n.getParent()).isRoot())
						{
							dirty = project.needsSaving(); //&& !p.isReadOnly();
						}

						if (dirty || opt.isSaveAs() || opt.isPublish() || (opt.getImporter() != null))
						{
							project.setEarliestAndLatestDatesFromSchedule(); // we want subprojects to have their dates set by external constraints if any
							project.setPersistedId( project.getUniqueId() );
							projects.add( project );
						}
					}
				}
			} );

		if (projects.size() > 0)
		{
			final Session session = SessionFactory.getInstance().getSession( opt.isLocal() );
			final SaveOptions o = (SaveOptions)opt.clone();
			o.setPostSaving( new Closure()
				{
					public void execute( 
						Object obj )
					{
						Project p = (Project)obj;
						p.setAllTasksAsUnchangedFromPersisted( true );
						p.validateNewTaskAndAssignments();
						p.forObjects( null, null, null, null,
							new Project.AssignmentPredicate()
							{
								public boolean evaluate( 
									Assignment assignment,
									int snapshotId )
								{ //closure
									assignment.setCreatedFromTimesheet( false ); //in case it was timesheet-created

									return true;
								}
							}, null );
						p.validateNewDistributionMap();
						portfolio.handleExternalTasks( p, false, true ); // external link handling

						if (opt.getPostSaving() != null)
						{
							opt.getPostSaving().execute( obj ); //id, combobox update
						}

						if (opt.isPublish())
						{
							session.publish( project, opt.isReadActualsFirst() );
						}

						session.refreshMetadata( project );
					}
				} );

			Job job = session.getSaveProjectJob( projects, o );

			return job;
		}

		return null;
	}

	/**
	 * @return Returns the server.
	 */
	public final String getServer()
	{
		return server;
	}

	public Collection getWritableProjectsList()
	{
		return portfolio.getWritableProjectList();
	}

	public boolean isPromptDisabled()
	{
		return promptDisabled;
	}

	public boolean isResourcePoolOpenAndWritable()
	{
		return portfolio.isResourcePoolOpenAndWritable();
	}

	//OPEN PROJECTS
	public Project openProject( 
		final LoadOptions _loadOptions )
	{
		_loadOptions.setSync( _loadOptions.isSync() || Environment.isScripting() );

		final Session session = SessionFactory.getInstance().getSession( _loadOptions.isLocal(), _loadOptions.getFormat() );
		final Closure sessionClosure = session.getPostOpenAction( _loadOptions );

		if (sessionClosure != null)
		{
			if (_loadOptions.getEndSwingClosure() == null)
			{
				_loadOptions.setEndSwingClosure( sessionClosure );
			}
			else
			{
				_loadOptions.setEndSwingClosure( new PairClosure( _loadOptions.getEndSwingClosure(), sessionClosure ) );
			}
		}

		Job job = null;
		final boolean recover;

		if (_loadOptions.getId() > 0)
		{
			Project p = findFromId( _loadOptions.getId() );

			if (((Environment.isNoPodServer() == false) 
			  || (Environment.isTesting() == true)) 
			 && ((p != null) 
			  && (_loadOptions.isOpenAs() == false)))
			{ 
				// always loading the project for Mariner
				job = session.getEmptyJob( "Recover project", p );
				recover = true;
			}
			else
			{
				addLoadingProject( _loadOptions.getId() );
				recover = false;
			}
		}
		else
		{
			recover = false;
		}

		if (job == null)
		{
			job = session.getLoadProjectJob( _loadOptions );
		}

		job.addSwingRunnable( 
			new JobRunnable( "Local: addProject" )
		{
			@Override
			public Object run()
				throws Exception
			{
				final Project project = (Project)getPreviousResult();

				if (recover == false)
				{
					if (project != null)
					{
						addProject( project, false, true );
					}

					if (_loadOptions.getId() > 0)
					{
						removeLoadingProject( _loadOptions.getId() );
					}
				}

				if (_loadOptions.getEndSwingClosure() != null)
				{
					SwingUtilities.invokeLater( 
						new Runnable()
					{
						@Override
						public void run()
						{
							_loadOptions.getEndSwingClosure().execute( project );
						}
					} );
				}

				session.refreshMetadata( project );
				session.readCurrencyData( project );

				if ((project != null) 
				 && (_loadOptions.isOpenAs() == true) 
				 && (project.isMaster() == true))
				{
					project.setReadOnly( true ); // don't allow copy of master
				}

				if ((project != null) 
				 && (_loadOptions.isOpenAs() == true))
				{
					project.setReadOnly( true );
					project.setLocal( true );
				}

				return project;
			}
		}, false );

		if (_loadOptions.isSync())
		{
			job.addSync();
		}

		session.schedule( job );

		try
		{
			return (_loadOptions.isSync())
				? (Project)job.waitResult()
				: null;
		}
		catch (Exception e)
		{ 
			//Forward exception + Alert
			return null;
		}
	}

	public Project openSubproject( 
		final Project parent,
		final Node subprojectNode,
		final boolean creating )
	{
		final SubProj subprojectTask = (SubProj)subprojectNode.getValue();
		final long id = subprojectTask.getSubprojectUniqueId();
		Project openSubproject = portfolio.findByUniqueId( id );

		if (openSubproject != null)
		{
			parent.getSubprojectHandler().addSubproject( openSubproject, subprojectNode, creating, true );
			portfolio.handleExternalTasks( openSubproject, true, false ); // resolve external links if any

			return openSubproject;
		}

		final Session session = SessionFactory.getInstance().getSession( false ); // never local

		if (!session.projectExists( id ))
		{
			Alert.error( Messages.getString( "Error.projectDoesNotExist" ) );

			return null;
		}

		addLoadingProject( id );

		LoadOptions opt = new LoadOptions();
		opt.setSubproject( true );
		opt.setId( id );

		Job job = session.getLoadProjectJob( opt );
		subprojectTask.setFetching( true );

		job.addSwingRunnable( new JobRunnable("Local: insertProject")
			{
				public Object run()
					throws Exception
				{
					try
					{
						Project subproject = (Project)getPreviousResult();

						//add assignments in the outline, paste uses only assignments present in the nodeModel
						AssignmentNodeModel parentModel = (AssignmentNodeModel)subproject.getTaskOutline();
						parentModel.addAssignments( parentModel.iterator() ); // assignments

						if (subproject != null)
						{ // is it possible it can be null?
							parent.getSubprojectHandler().addSubproject( subproject, subprojectNode, creating, false );

							if (subproject.isReadOnly())
							{
								Alert.warn( MessageFormat.format( Messages.getString( "Message.readOnlySubproject" ),
										new Object[]
										{
											subproject.getName()
										} ) );
							}

							//
							//						subproject.setGroupDirty(true);
							//						//TODO something more precise here
						}
					}
					catch (Exception e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
						throw e;
					}
					finally
					{
						subprojectTask.setFetching( false );
						removeLoadingProject( id );
					}

					return null; //return not used anyway
				}
			}, false );

		session.schedule( job );

		return ((Task)subprojectTask).getProject();
	}

	public int promptForSave( 
		Project project,
		boolean allowCancel )
	{
		String text = Messages.getString( "Message.saveProjectBeforeClosing1" ) + " " + project.getName() + " " +
			Messages.getString( "Message.saveProjectBeforeClosing2" );

		if (allowCancel)
		{
			return Alert.confirm( text );
		}
		else
		{
			return Alert.confirmYesNo( text );
		}
	}

	public Project reload( 
		Project project,
		boolean sync )
	{
		sync = sync || Environment.isScripting();

		//before remove promopt		Job job=getRemoveProjectJob(project,true,true,true);
		Job job = getRemoveProjectJob( project, false, false, true );

		if (job == null)
		{
			return null;
		}

		final LoadOptions opt = new LoadOptions();
		opt.setId( project.getUniqueId() );
		opt.setSync( sync );

		//final Session session=SessionFactory.getInstance().getSession(project.isLocal());
		final Session session = SessionFactory.getInstance().getSession( opt.isLocal(), opt.getFormat() );
		Closure sessionClosure = session.getPostOpenAction( opt );

		if (sessionClosure != null)
		{
			if (opt.getEndSwingClosure() == null)
			{
				opt.setEndSwingClosure( sessionClosure );
			}
			else
			{
				opt.setEndSwingClosure( new PairClosure(opt.getEndSwingClosure(), sessionClosure) );
			}
		}

		Job loadJob = session.getLoadProjectJob( opt );
		job.addJob( loadJob );
		job.addSwingRunnable( new JobRunnable("Local: addProject")
			{
				public Object run()
					throws Exception
				{
					Project project = (Project)getPreviousResult();

					if (project != null)
					{
						session.readCurrencyData( project );
						addProject( project, false, true );
					}

					if (opt.getId() > 0)
					{
						removeLoadingProject( opt.getId() );
					}

					if (opt.getEndSwingClosure() != null)
					{
						opt.getEndSwingClosure().execute( project );
					}

					if (project != null)
					{
						session.refreshMetadata( project );
					}

					if ((project != null) && opt.isOpenAs() && project.isMaster())
					{
						project.setReadOnly( true ); // don't allow copy of master
					}

					if ((project != null) && opt.isOpenAs())
					{
						project.setReadOnly( true );
						project.setLocal( true );
					}

					project.setDirty( false );
					project.setGroupDirty( false );

					return project;
				}
			}, false );

		if (opt.isSync())
		{
			job.addSync();
		}

		session.schedule( job );

		try
		{
			return (opt.isSync())
			? (Project)job.waitResult()
			: null;
		}
		catch (Exception e)
		{ //Forward exception + Alert

			return null;
		}

		//Project reloadedProject=openProject(opt);

		//Alert.warn("Code not present - reload project  - doing nothing");
		//return reloadedProject;
	}

	public synchronized void removeClosingProject( 
		long id )
	{
		closingProjects.remove( new Long(id) );
	}

	public synchronized void removeLoadingProject( 
		long id )
	{
		loadingProjects.remove( new Long(id) );
	}

	public void removeProject( 
		final Project project,
		boolean allowCancel,
		boolean prompt,
		boolean calledFromSwing )
	{
		Job job = getRemoveProjectJob( project, allowCancel, prompt, calledFromSwing );

		if (job != null)
		{ // if not cancelled

			Session session = SessionFactory.getInstance().getSession( project.isLocal() );
			session.schedule( job );

			//			project.setLocked(false);
		}
	}

	public boolean revert( 
		Project project,
		boolean sync )
	{
		sync = sync || Environment.isScripting();

		Session session = SessionFactory.getInstance().getSession( false );
		boolean ok = session.revert( project );

		if (ok)
		{
			reload( project, sync );
		}

		return ok;
	}

	//SAVE PROJECTS
	public void saveProject( 
		final Project project,
		final SaveOptions opt )
	{
		opt.setSync( opt.isSync() || Environment.isScripting() );

		Job job = getSaveProjectJob( project, opt );
		Session session = SessionFactory.getInstance().getSession( opt.isLocal() );

		if (job != null)
		{
			if (opt.isSync())
			{
				job.addSync();
			}

			session.schedule( job );

			try
			{
				if (opt.isSync())
				{
					job.waitResult();
				}
				else
				{
					System.out.println( "____________Sync is false" );
				}

				if (!opt.isClosing())
				{
					session.refreshMetadata( project );
				}
			}
			catch (Exception e)
			{;
			}
		}
	}

	public void setImportInto( 
		Project project )
	{
		portfolio.setImportInto( project );
	}

	public void setPromptDisabled( 
		boolean promptDisabled )
	{
		this.promptDisabled = promptDisabled;
	}

	/**
	 * @param server The server to set.
	 */
	public final void setServer( 
		String server )
	{
		this.server = server;
	}

	private static int untitledCount = 0;
	private static ProjectFactory projectFactory;
	protected Set closingProjects = new HashSet();

	//	public Project openDownloadedProject() {
	//		return null;
	//	}
	protected Set loadingProjects = new HashSet();
	Portfolio portfolio; // for now just one portfolio.  Perhaps portfolio should reference project factory and not like this
	private String server = null;
	private boolean promptDisabled = false;
}
