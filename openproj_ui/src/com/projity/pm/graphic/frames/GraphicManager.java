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
package com.projity.pm.graphic.frames;

import org.openproj.gui.dialog.PreferencesDialog;
import apple.dts.samplecode.osxadapter.OSXAdapter;

import com.projity.configuration.Configuration;
import com.projity.configuration.FieldDictionary;

import com.projity.contrib.ClassLoaderUtils;

import com.projity.dialog.*;
import com.projity.dialog.assignment.AssignmentDialog;
import com.projity.dialog.options.CalendarDialogBox;

import org.openproj.domain.document.Document;
import com.projity.document.ObjectEvent;

import com.projity.exchange.ResourceMappingForm;

import com.projity.field.Field;

import com.projity.graphic.configuration.SpreadSheetFieldArray;

import com.projity.grouping.core.Node;
import com.projity.grouping.core.VoidNodeImpl;
import com.projity.grouping.core.model.NodeModel;
import com.projity.grouping.core.transform.ViewTransformer;
import com.projity.grouping.core.transform.filtering.NodeFilter;
import com.projity.grouping.core.transform.filtering.ResourceInTeamFilter;

import com.projity.job.Job;
import com.projity.job.JobQueue;
import com.projity.job.JobRunnable;
import com.projity.job.Mutex;

import com.projity.menu.MenuActionConstants;
import com.projity.menu.MenuActionsMap;
import com.projity.menu.MenuManager;

import com.projity.options.CalendarOption;

import com.projity.pm.assignment.Assignment;
import com.projity.pm.graphic.ChangeAwareTextField;
import com.projity.pm.graphic.IconManager;
import com.projity.pm.graphic.TabbedNavigation;
import com.projity.pm.graphic.frames.workspace.DefaultFrameManager;
import com.projity.pm.graphic.frames.workspace.FrameHolder;
import com.projity.pm.graphic.frames.workspace.FrameManager;
import com.projity.pm.graphic.frames.workspace.NamedFrame;
import com.projity.pm.graphic.frames.workspace.NamedFrameEvent;
import com.projity.pm.graphic.frames.workspace.NamedFrameListener;
import com.projity.pm.graphic.laf.LafManager;
import com.projity.pm.graphic.laf.LafUtils;
import com.projity.pm.graphic.spreadsheet.SpreadSheet;
import com.projity.pm.graphic.spreadsheet.common.CommonSpreadSheet;
import com.projity.pm.graphic.spreadsheet.selection.event.SelectionNodeEvent;
import com.projity.pm.graphic.spreadsheet.selection.event.SelectionNodeListener;
import com.projity.pm.graphic.views.BaseView;
import com.projity.pm.graphic.views.ProjectsDialog;
import com.projity.pm.graphic.views.Searchable;
import com.projity.pm.resource.Resource;
import com.projity.pm.resource.ResourcePool;
import com.projity.pm.task.Project;
import com.projity.pm.task.ProjectFactory;
import com.projity.pm.task.SubProj;
import com.projity.pm.time.HasStartAndEnd;

import com.projity.preference.GlobalPreferences;

import com.projity.print.GraphPageable;
import com.projity.print.PrintDocumentFactory;

import com.projity.server.data.DocumentData;

import com.projity.session.CreateOptions;
import com.projity.session.LoadOptions;
import com.projity.session.LocalSession;
import com.projity.session.SaveOptions;
import com.projity.session.Session;
import com.projity.session.SessionFactory;

import com.projity.strings.Messages;

import com.projity.toolbar.FilterToolBarManager;
import com.projity.toolbar.TransformComboBox;

import com.projity.undo.CommandInfo;
import com.projity.undo.UndoController;

import com.projity.util.Alert;
import com.projity.util.BrowserControl;
import com.projity.util.ClassUtils;
import com.projity.util.Environment;

import com.projity.workspace.SavableToWorkspace;
import com.projity.workspace.WorkspaceSetting;

import org.apache.batik.util.gui.resource.ActionMap;
import org.apache.batik.util.gui.resource.MissingListenerException;

import org.apache.commons.collections.Closure;

import java.applet.Applet;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.lang.reflect.InvocationTargetException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.openproj.domain.task.Task;

/**
 *
 */
public class GraphicManager
	implements FrameHolder,
		NamedFrameListener,
		WindowStateListener,
		SelectionNodeListener,
		ObjectEvent.Listener,
		ActionMap,
		MenuActionConstants,
		SavableToWorkspace
{
	public GraphicManager( 
		final Container _container )
	{
		this( myServer, _container);
	}

	/**
	 * @param projectUrl TODO
	 * @param server TODO
	 * @throws java.awt.HeadlessException
	 */
	public GraphicManager( /*String[] projectUrl,*/
		final String _server,
		final Container _container )
		throws HeadlessException
	{
		//this.projectUrl = projectUrl;
		myServer = _server;
		myContainer = _container;

		mylafManager = null;
		myGraphicManagers.add( this );
		myLastGraphicManager = this;
		SessionFactory.getInstance().setJobQueue( getJobQueue() );

		myContainer.addFocusListener( 
			new FocusListener()
		{
			@Override
			public void focusGained( 
				FocusEvent _event )
			{
//				System.out.println("GainFocus " + GraphicManager.this.hashCode());
				setMeAsLastGraphicManager();
			}

			@Override
			public void focusLost( 
				FocusEvent _event )
			{
//				System.out.println("LostFocus " + GraphicManager.this.hashCode());
			}
		} );

		myProjectFactory = ProjectFactory.getInstance();
		myProjectFactory.getPortfolio().addObjectListener( this );

		if (myContainer instanceof Frame == true)
		{
			myFrame = (Frame)myContainer;
		}
		else if (myContainer instanceof JApplet == true)
		{
			myFrame = JOptionPane.getFrameForComponent( myContainer );
		}

		if (myContainer instanceof FrameHolder == true)
		{
			((FrameHolder)myContainer).setGraphicManager( this );
		}

//		else if (myContainer instanceof BootstrapApplet){
		else
		{
			try
			{
				FrameHolder holder = (FrameHolder)Class.forName( "com.projity.bootstrap.BootstrapApplet" )
					.getMethod( "getObject", null ).invoke( myContainer, null );
				holder.setGraphicManager( this );
			}
			catch (Exception e)
			{
				;
			}
		}

		registerForMacOSXEvents();
	}

	private void addCtrlAccel( 
		final int _vk,
		final String _actionConstant,
		Action _action )
	{
		final RootPaneContainer root = (RootPaneContainer)myContainer;
		final InputMap inputMap = root.getRootPane().getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW );

		final KeyStroke key = KeyStroke.getKeyStroke( _vk, InputEvent.CTRL_MASK );
		inputMap.put( key, _actionConstant );

		if (_action == null)
		{
			_action = myMenuManager.getActionFromId( _actionConstant );
		}

		root.getRootPane().getActionMap().put( _actionConstant, _action );
	}

	public void addHandlers()
	{
		myActionsMap = new MenuActionsMap( this, myMenuManager );
		myActionsMap.addHandler( ACTION_NEW_PROJECT, new NewProjectAction() );
		myActionsMap.addHandler( ACTION_OPEN_PROJECT, new OpenProjectAction() );
		myActionsMap.addHandler( ACTION_INSERT_PROJECT, new InsertProjectAction() );
		myActionsMap.addHandler( ACTION_EXIT, new ExitAction() );
		myActionsMap.addHandler( ACTION_IMPORT_MSPROJECT, new ImportMSProjectAction() );
		myActionsMap.addHandler( ACTION_EXPORT_MSPROJECT, new ExportMSProjectAction() );
		myActionsMap.addHandler( ACTION_ABOUT_PROJITY, new AboutAction() );
		myActionsMap.addHandler( ACTION_OPENPROJ, new OpenProjAction() );
		myActionsMap.addHandler( ACTION_PROJITY_DOCUMENTATION, new HelpAction() );
		myActionsMap.addHandler( ACTION_TIP_OF_THE_DAY, new TipOfTheDayAction() );
		myActionsMap.addHandler( ACTION_PROJECT_INFORMATION, new ProjectInformationAction() );
		myActionsMap.addHandler( ACTION_PROJECTS_DIALOG, new ProjectsDialogAction() );
		myActionsMap.addHandler( ACTION_TEAM_FILTER, new TeamFilterAction() );
		myActionsMap.addHandler( ACTION_DOCUMENTS, new DocumentsAction() );
		myActionsMap.addHandler( ACTION_INFORMATION, new InformationAction() );
		myActionsMap.addHandler( ACTION_NOTES, new NotesAction() );
		myActionsMap.addHandler( ACTION_ASSIGN_RESOURCES, new AssignResourcesAction() );

		myActionsMap.addHandler( ACTION_FIND, new FindAction() );
		myActionsMap.addHandler( ACTION_GOTO, new GoToAction() );
		myActionsMap.addHandler( ACTION_INSERT_TASK, new InsertTaskAction() );
		myActionsMap.addHandler( ACTION_INSERT_RESOURCE, new InsertTaskAction() ); // will do resource
		myActionsMap.addHandler( ACTION_SAVE_PROJECT, new SaveProjectAction() );
		myActionsMap.addHandler( ACTION_SAVE_PROJECT_AS, new SaveProjectAsAction() );
		myActionsMap.addHandler( ACTION_REVERT, new RevertAction() );
		myActionsMap.addHandler( ACTION_CANCEL_CHANGES, new CancelChangesAction() );
		myActionsMap.addHandler( ACTION_PUBLISH, new PublishAction() );
		myActionsMap.addHandler( ACTION_RELOAD, new ReloadAction() );
		myActionsMap.addHandler( ACTION_PULL_ACTUALS, new PullActualsAction() );
		myActionsMap.addHandler( ACTION_PRINT, new PrintAction() );
		myActionsMap.addHandler( ACTION_PRINT_PREVIEW, new PrintPreviewAction() );
		myActionsMap.addHandler( ACTION_PDF, new PDFAction() );
		myActionsMap.addHandler( ACTION_CLOSE_PROJECT, new CloseProjectAction() );
		myActionsMap.addHandler( ACTION_UNDO, new UndoAction() );
		myActionsMap.addHandler( ACTION_REDO, new RedoAction() );
		myActionsMap.addHandler( ACTION_SUBSTITUTE_RESOURCE, new SubstituteResourceAction() );

//		myActionsMap.addHandler(ACTION_ENTERPRISE_RESOURCES, new EnterpriseResourcesAction());
		myActionsMap.addHandler( ACTION_CHANGE_WORKING_TIME, new ChangeWorkingTimeAction() );
		myActionsMap.addHandler( ACTION_LEVEL_RESOURCES, new LevelResourcesAction() );
		myActionsMap.addHandler( ACTION_DELEGATE_TASKS, new DelegateTasksAction() );
		myActionsMap.addHandler( ACTION_UPDATE_TASKS, new UpdateTasksAction() );
		myActionsMap.addHandler( ACTION_UPDATE_PROJECT, new UpdateProjectAction() );
		myActionsMap.addHandler( ACTION_BAR, new BarAction() );
		myActionsMap.addHandler( ACTION_INSERT_RECURRING, new RecurringTaskAction() );
		myActionsMap.addHandler( ACTION_SORT, new SortAction() );
		myActionsMap.addHandler( ACTION_GROUP, new GroupAction() );
		myActionsMap.addHandler( ACTION_CALENDAR_OPTIONS, new CalendarOptionsAction() );
		myActionsMap.addHandler( ACTION_SAVE_BASELINE, new SaveBaselineAction() );
		myActionsMap.addHandler( ACTION_CLEAR_BASELINE, new ClearBaselineAction() );
		myActionsMap.addHandler( ACTION_LINK, new LinkAction() );
		myActionsMap.addHandler( ACTION_UNLINK, new UnlinkAction() );
		myActionsMap.addHandler( ACTION_ZOOM_IN, new ZoomInAction() );
		myActionsMap.addHandler( ACTION_ZOOM_OUT, new ZoomOutAction() );
		myActionsMap.addHandler( ACTION_SCROLL_TO_TASK, new ScrollToTaskAction() );
		myActionsMap.addHandler( ACTION_INDENT, new IndentAction() );
		myActionsMap.addHandler( ACTION_OUTDENT, new OutdentAction() );
		myActionsMap.addHandler( ACTION_COLLAPSE, new CollapseAction() );
		myActionsMap.addHandler( ACTION_EXPAND, new ExpandAction() );

		myActionsMap.addHandler( ACTION_CUT, new CutAction() );
		myActionsMap.addHandler( ACTION_COPY, new CopyAction() );
		myActionsMap.addHandler( ACTION_PASTE, new PasteAction() );
		myActionsMap.addHandler( ACTION_DELETE, new DeleteAction() );

		myActionsMap.addHandler( ACTION_GANTT, new ViewAction(ACTION_GANTT) );
		myActionsMap.addHandler( ACTION_TRACKING_GANTT, new ViewAction(ACTION_TRACKING_GANTT) );
		myActionsMap.addHandler( ACTION_TASK_USAGE_DETAIL, new ViewAction(ACTION_TASK_USAGE_DETAIL) );
		myActionsMap.addHandler( ACTION_RESOURCE_USAGE_DETAIL, new ViewAction(ACTION_RESOURCE_USAGE_DETAIL) );
		myActionsMap.addHandler( ACTION_NETWORK, new ViewAction(ACTION_NETWORK) );
		myActionsMap.addHandler( ACTION_WBS, new ViewAction( ACTION_WBS ) );
		myActionsMap.addHandler( ACTION_RBS, new ViewAction( ACTION_RBS ) );
		myActionsMap.addHandler( ACTION_REPORT, new ViewAction(ACTION_REPORT) );
		myActionsMap.addHandler( ACTION_PROJECTS, new ViewAction(ACTION_PROJECTS) );
		myActionsMap.addHandler( ACTION_RESOURCES, myResourceAction = new ViewAction(ACTION_RESOURCES) );
		myActionsMap.addHandler( ACTION_HISTOGRAM, new ViewAction(ACTION_HISTOGRAM) );
		myActionsMap.addHandler( ACTION_CHARTS, new ViewAction(ACTION_CHARTS) );
		myActionsMap.addHandler( ACTION_TASK_USAGE, new ViewAction(ACTION_TASK_USAGE) );
		myActionsMap.addHandler( ACTION_RESOURCE_USAGE, new ViewAction(ACTION_RESOURCE_USAGE) );
		myActionsMap.addHandler( ACTION_NO_SUB_WINDOW, new ViewAction(ACTION_NO_SUB_WINDOW) );

		myActionsMap.addHandler( ACTION_CHOOSE_FILTER, new TransformAction() );
		myActionsMap.addHandler( ACTION_CHOOSE_SORT, new TransformAction() );
		myActionsMap.addHandler( ACTION_CHOOSE_GROUP, new TransformAction() );

		myActionsMap.addHandler( ACTION_PALETTE, new PaletteAction() );
		myActionsMap.addHandler( ACTION_LOOK_AND_FEEL, new LookAndFeelAction() );
		myActionsMap.addHandler( ACTION_FULL_SCREEN, new FullScreenAction() );
		myActionsMap.addHandler( ACTION_REFRESH, new RefreshAction() );
		myActionsMap.addHandler( ACTION_REINITIALIZE, new ReinitializeAction() );
		myActionsMap.addHandler( ACTION_UNDO_STACK, new UndoStackAction() );
		myActionsMap.addHandler( ACTION_PREFERENCES, new PreferencesAction() );
	}

	public void addHistory( 
		final String _command,
		final Object[] _args )
	{
		myHistory.add( new CommandInfo( _command, _args ) );
	}

	public void addHistory( 
		final String _command )
	{
		myHistory.add( new CommandInfo( _command, null ) );
	}

	/**
	     * Adds a new document frame and shows it
	     * @param project
	     * @return
	     */
	public DocumentFrame addProjectFrame( 
		final Project _project )
	{
		final String tabId = getTabIdForProject( _project );

		if (_project == null) // in case of out of memory error
		{
			return null;
		}

		final DocumentFrame frame = new DocumentFrame( this, _project, tabId );

		if (frame == null) // in case of out memory error
		{
			return null;
		}

		getFrameManager().addFrame( frame );

		//		DocumentFrame newDocumentFrame = (DocumentFrame)getFrameManager().getFrame(tabId);
		setTabNameAndTitle( frame, _project );
		frame.setShowTitleBar( false );
		getFrameManager().showFrame( frame ); // show the frame
		frame.addNamedFrameListener( this ); // main frame listens to changes in selection

		_project.addProjectListener( frame );

		if (myProjectListMenu != null)
		{
			final JRadioButtonMenuItem mi = new JRadioButtonMenuItem(new SelectDocumentAction(frame));
			mi.setSelected( true );
			frame.setMenuItem( mi );
			myProjectListMenu.add( mi );
		}

		setCurrentFrame( frame );

		myFrameList.add( frame );
		myFrameMap.put( _project, frame );

		// clear filter/grouping/sort for newly opened or created project
		if (Environment.isPlugin() == false)
		{
			SwingUtilities.invokeLater( 
				new Runnable()
			{
				@Override
				public void run()
				{
					frame.getFilterToolBarManager().clear();
				}
			} );
		}

		//resource pool can not be opened at same time as another proj
		getMenuManager().setActionEnabled( ACTION_OPEN_PROJECT, (frame == null) || !frame.isEditingResourcePool() ); 

		return frame;
	}

	public void beginInitialization()
	{
		showWaitCursor( true );
		myInitializing.lock();
	}

	public void cleanUp()
	{
//		On quitting, a sleep interrupted exception (below) is thrown by Substance. Without changing the source
//		java.lang.InterruptedException: sleep interrupted
//		at java.lang.Thread.sleep(Native Method)
//		at org.jvnet.substance.utils.FadeTracker$FadeTrackerThread.run(FadeTracker.java:210)
//		I have submitted a bug report: https://substance.dev.java.net/issues/show_bug.cgi?id=155 with a proposed fix
		myProjectFactory.getPortfolio().removeObjectListener( this );
		((DefaultFrameManager)myFrameManager).cleanUp( false );
		myCurrentFrame = null;
		myGraphicManagers.remove( this );

		if (myGraphicManagers.isEmpty() == true)
		{
			getLafManager().clean();
		}

		if (myJobQueue != null)
		{
			myJobQueue.cancel();
		}

		myJobQueue = null;

		//cleanUpStatic();
	}

	public static void cleanUpStatic()
	{
		//cleanUp static
		myLastGraphicManager = null;
		myGraphicManagers = new LinkedList();
		mylafManager = null;
		myBadLAF = false;
		myLastWorkspace = null;
		myProject_suffix_count = 1;
		myServer = null;
		System.out.println( "workspace reset" );
	}

	public void closeApplication()
	{
		addHistory( "closeApplication" );

		//		if (Environment.getStandAlone()) {
		//			Frame myFrame=getFrame();
		//			if (myFrame!=null)
		//				myFrame.dispose();
		//			System.exit(0);
		//			return;
		//		}
		(new Thread()
		{
			@Override
			public void run()
			{
				final JobRunnable exitRunnable = new JobRunnable( "Local: closeProjects" )
				{
					@Override
					public Object run()
						throws Exception
					{
						final Frame frame = getFrame();

						if (frame != null)
						{
							frame.dispose();
						}

						System.exit( 0 );

						return null; //return not used anyway
					}
				};

				final Job job = myProjectFactory.getPortfolio().getRemoveAllProjectsJob( exitRunnable, true, null );
				SessionFactory.getInstance().getLocalSession().schedule( job );
			}
		}).start();
	}

	protected void closeProject( 
		final Project _project )
	{
		myProjectFactory.removeProject( _project, true, true, true );
	}

	private void closeProjectFrame( 
		final Project _project )
	{
		final String tabId = getTabIdForProject( _project );
		final DocumentFrame frame = (DocumentFrame)myFrameMap.get( _project );

		if (frame != null)
		{ //TODO why is it sometimes null? Well, in the case of opening a subproject it can be

			if (myCurrentFrame == frame)
			{
				frame.setVisible( false );

				final JMenuItem mi = frame.getMenuItem();

				if ((mi != null) 
				 && (myProjectListMenu != null))
				{
					myProjectListMenu.remove( mi );
				}

				if (myFrameList.size() <= 1)
				{
					frame.refreshViewButtons( false ); // disable old buttons
					myCurrentFrame = null; //TODO open a new one instead
					setTitle( false );
					setEnabledDocumentMenuActions( false );
				}
				else
				{
					DocumentFrame current;
					int index = 0;

					for (Iterator i = myFrameList.iterator(); i.hasNext(); index++)
					{
						current = (DocumentFrame)i.next();

						if (tabId.equals( getTabIdForProject( current.getProject() ) ))
						{
							break;
						}
					}

					setCurrentFrame( (DocumentFrame)myFrameList.get( (index == 0)
						? 1
						: (index - 1) ) );

					//TODO use previous instead
				}
			}

			_project.removeProjectListener( frame ); // hk uncommented this for applet. don't know why it was commented
			frame.removeNamedFrameListener( this ); // main frame listens to changes in selection

			getFrameManager().removeFrame( frame );
			frame.onClose();
			myFrameList.remove( frame );
			myFrameMap.remove( _project );
		}

		setAllButResourceDisabled( false );
		getMenuManager().setActionEnabled( ACTION_OPEN_PROJECT, true ); // no matter what, you can open a project after closing, since if you closed resource pool you can open after
	}

	@Override
	public WorkspaceSetting createWorkspace( 
		final int _context )
	{
		final Workspace ws = new Workspace( getColorThemes(), getFrameManager().createWorkspace( _context ));

		//TODO The active states of BarStyles (and other styles) are currently static. This is ok for applets, but not a general restore workspace feature
		return ws;
	}

	private Workspace decodeWorkspace()
	{
		if (myLastWorkspace == null)
		{
			return null;
		}

		return BINARY_WORKSPACE
			? decodeWorkspaceBinary()
			: decodeWorkspaceXML();
	}

	private Workspace decodeWorkspaceBinary()
	{
		ByteArrayInputStream bin = new ByteArrayInputStream((byte[])myLastWorkspace);
		ObjectInputStream in;

		try
		{
			in = new ObjectInputStream(bin);

			return (Workspace)in.readObject();
		}
		catch (IOException | ClassNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Decode the current workspace (currently using XML though could be binary)
	 * @return workspace object decoded from lastWorkspace static
	 */
	private Workspace decodeWorkspaceXML()
	{
		ByteArrayInputStream stream = new ByteArrayInputStream(((String)myLastWorkspace).getBytes());
		XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(stream));
		Workspace workspace = (Workspace)decoder.readObject();
		decoder.close();

		return workspace;
	}

	public void doFind( 
		final Searchable _searchable,
		final Field _field )
	{
		if ((myCurrentFrame == null) || !getCurrentFrame().isActive())
		{
			return;
		}

		if (_searchable == null)
		{
			return;
		}

		myCurrentFrame.doFind( _searchable, _field );
	}

	public void doInformationDialog( 
		final boolean _notes )
	{
		if (!isDocumentActive())
		{
			return;
		}

		finishAnyOperations();

		List nodes = getCurrentFrame().getSelectedNodes( false );

		if (nodes == null)
		{
			return;
		}

		if (nodes.size() > 1)
		{
			Alert.warn( Messages.getString( "Message.onlySelectOneElement" ), getContainer() ); //$NON-NLS-1$

			return;
		}

		final Node node = (Node)nodes.get( 0 );
		Object impl = node.getValue();

		if (impl instanceof Task || (impl instanceof Assignment && myTaskType))
		{
			Task task = (Task)((impl instanceof Assignment)
				? (((Assignment)impl).getTask())
				: impl);

			if (myTaskInformationDialog == null)
			{
				myTaskInformationDialog = TaskInformationDialog.getInstance( getFrame(), task, _notes );
				myTaskInformationDialog.pack();
				myTaskInformationDialog.setModal( false );
			}
			else
			{
				myTaskInformationDialog.setObject( task );
				myTaskInformationDialog.updateAll();
			}

			myTaskInformationDialog.setLocationRelativeTo( getCurrentFrame() );

			//to center on screen
			if (_notes)
			{
				myTaskInformationDialog.showNotes();
			}
			else if (impl instanceof Assignment)
			{
				myTaskInformationDialog.showResources();
			}

			myTaskInformationDialog.setVisible( true );
		}
		else if (impl instanceof Resource || (impl instanceof Assignment && myResourceType))
		{
			Resource resource = (Resource)((impl instanceof Assignment)
				? (((Assignment)impl).getResource())
				: impl);

			if (myResourceInformationDialog == null)
			{
				myResourceInformationDialog = ResourceInformationDialog.getInstance( getFrame(), resource );
				myResourceInformationDialog.pack();
				myResourceInformationDialog.setModal( false );
			}
			else
			{
				myResourceInformationDialog.setObject( resource );
				myResourceInformationDialog.updateAll();
			}

			myResourceInformationDialog.setLocationRelativeTo( getCurrentFrame() );

			//to center on screen
			if (_notes)
			{
				myResourceInformationDialog.showNotes();
			}

			myResourceInformationDialog.setVisible( true );
		}
		else if (impl instanceof Project)
		{
			doProjectInformationDialog();
		}
	}

	private void doInsertProjectDialog()
	{
		if (myDoingOpenDialog)
		{
			return;
		}

		myDoingOpenDialog = true;

		finishAnyOperations();

		final Project project;
		project = getCurrentFrame().getProject();

//		List nodes=getCurrentFrame().getSelectedNodes();
//		if (nodes==null||nodes.size()==0) return;
//		Node node=(Node)nodes.get(0);
//		if (!node.isInSubproject()) project= getCurrentFrame().getProject();
//		else{
//			while (!(node==null) && !(node.getValue().getClass().getName().equals("com.projity.pm.task.Subproject"))){
//				node=(Node)node.getParent();
//			}
//			if (node==null) return; //shouldn't happen
//			try {
//				project=(Project)node.getValue().getClass().getMethod("getSubproject", null).invoke(node.getValue(), null);
//			} catch (Exception e) {
//				return;
//			}
//		}
		
		final ArrayList descriptors = new ArrayList();

		Session session = SessionFactory.getInstance().getSession( false );
		Job job = (Job)SessionFactory.callNoEx( session, "getLoadProjectDescriptorsJob",
				new Class[]
				{
					boolean.class,
					java.util.List.class,
					boolean.class
				}, new Object[]
				{
					true,
					descriptors,
					true
				} );
		job.addSwingRunnable( new JobRunnable("Local: add")
		{
			@Override
				public Object run()
					throws Exception
				{
					Closure setter = new Closure()
					{
					@Override
						public void execute( 
							Object obj )
						{
						}
					};

					Closure getter = new Closure()
					{
						@Override
						public void execute( 
							Object obj )
						{
							final Object[] r = (Object[])obj;

							if (r != null)
							{
								final DocumentData data = (DocumentData)r[ 0 ];

								if (data.isMaster())
								{
									return;
								}

								insertSubproject( project, data.getUniqueId(), true );

//	    	        		Project openedAlready = ProjectFactory.getInstance().findFromId(data.getUniqueId());
//
//							if (!project.canInsertProject(data.getUniqueId())) {
//								Alert.error("The selected project is already a subproject in this consolidated project.");
//								return;
//							}
//							if (openedAlready != null && openedAlready.isOpenedAsSubproject()) {
//								Alert.error("The selected project is already opened as a subproject in another consolidated project.");
//								return;
//							}
//							Subproject subprojectTask = new Subproject(project,data.getUniqueId());
//							Node subprojectNode = getCurrentFrame().addNodeForImpl(subprojectTask,NodeModel.EVENT);
//							ProjectFactory.getInstance().openSubproject(project, subprojectNode, true);
							}
						}
					};

					try
					{
						OpenProjectDialog dlg = OpenProjectDialog.getInstance( getFrame(), descriptors,
								Messages.getString( "Text.insertProject" ), false, false, project ); //$NON-NLS-1$
						dlg.execute( setter, getter );
					}
					catch (Exception e)
					{
						//TODO need more precise exception
						Alert.error( Messages.getString( "Message.serverUnreachable" ), getContainer() ); //$NON-NLS-1$

						e.printStackTrace();
					}
					finally
					{
						myDoingOpenDialog = false;
					}

					return null;
				}
			} );
		session.schedule( job );
	}

	public void doInsertTask()
	{
		myActionsMap.getActionFromMenuId( GraphicManager.ACTION_INSERT_TASK ).actionPerformed( null );
	}

	public boolean doNewProjectDialog()
	{
		ProjectDialog.Form form = doNewProjectDialog1();

		if (form == null)
		{
			return false;
		}
		else
		{
			return doNewProjectDialog2( form );
		}
	}

	public ProjectDialog.Form doNewProjectDialog1()
	{
		addHistory( "doNewProjectDialog" );
		finishAnyOperations();

		ProjectDialog projectDialog = ProjectDialog.getInstance( getFrame(), null );
		projectDialog.getForm().setManager( Environment.getUser().getName() );

		if (!projectDialog.doModal())
		{
			return null; // if cancelled
		}

		return projectDialog.getForm();
	}

	public boolean doNewProjectDialog2( 
		final ProjectDialog.Form _form )
	{
		showWaitCursor( true );

		ResourcePool resourcePool = _form.getResourcePool();
		boolean local = _form.isLocal();

		if (resourcePool != null)
		{
			resourcePool.setLocal( local );
		}

		CreateOptions opt = new CreateOptions();
		opt.setResourcePool( _form.getResourcePool() );
		opt.setLocal( local );
		opt.setName( _form.getName() );
		opt.setAddResources( !local );

		Project project = myProjectFactory.createProject( opt );

		try
		{
			//createProject above might make a new resource pool, so make sur it is used when copying properties
			//projectDialog.getForm().setResourcePool(project.getResourcePool());
			project.setManager( _form.getManager() );
			project.setName( _form.getName() );
			project.setNotes( _form.getNotes() );
			project.setForward( _form.isForward() );

			if (!_form.isLocal())
			{
				project.setAccessControlPolicy( _form.getAccessControlType() );
				project.resetRoles( _form.getAccessControlType() == 0 );
			}

			if (_form.isLocal())
			{
				project.setLocal( true );
			}
			else
			{
				project.setTemporaryLocal( true );
			}

			if (_form.isForward())
			{
				project.setStartDate( _form.getStartDate() );
			}
			else
			{
				project.setFinishDate( _form.getStartDate() );
			}

			// copy any extra fields to the project
			project.getExtraFields().putAll( _form.getExtra().getExtraFields() );

			//			PropertyUtils.copyProperties(project, projectDialog.getForm());
		}
		catch (Exception propertyException)
		{
			propertyException.printStackTrace();
		}

		showWaitCursor( false );

		return true;
	}

	public boolean doNewProjectNoDialog( 
		final HashMap _opts )
	{
		ProjectDialog.Form form = doNewProjectNoDialog1();

		if (form == null)
		{
			return false;
		}

		if (_opts != null)
		{
			Closure updateViewClosure = (Closure)_opts.get( "updateViewClosure" );

			if (updateViewClosure != null)
			{
				updateViewClosure.execute( form );
			}
		}

		return doNewProjectDialog2( form );
	}

//	protected static ProjectDialog.Form lastNewProjectForm;
//	public ProjectDialog.Form getLastNewProjectForm() 
//	{
//		return lastNewProjectForm;
//	}
	
	public ProjectDialog.Form doNewProjectNoDialog1()
	{
		System.out.println( "doNewProjectNoDialog1 begin" );
		addHistory( "doNewProjectNoDialog" );
		finishAnyOperations();

		ProjectDialog.Form form = new ProjectDialog.Form();
		form.setName( "Project" + (myProject_suffix_count++) );

		//		lastNewProjectForm=form;
		System.out.println( "doNewProjectNoDialog1 end" );

		return form;
	}

	private void doOpenProjectDialog()
	{
		if (myDoingOpenDialog == true)
		{
			return;
		}

		myDoingOpenDialog = true;
		finishAnyOperations();

		final ArrayList descriptors = new ArrayList();

		Session session = SessionFactory.getInstance().getSession( false );
		Job job = (Job)SessionFactory.callNoEx( session, "getLoadProjectDescriptorsJob",
			new Class[]
		{
			boolean.class,
			java.util.List.class,
			boolean.class
		}, 
			new Object[]
		{
			true,
			descriptors,
			!Environment.isAdministrator()
		} );
		
		job.addSwingRunnable( 
			new JobRunnable( "Local: loadDocument" )
		{
			@Override
			public Object run()
				throws Exception
			{
				final Closure setter = 
					new Closure()
				{
					@Override
					public void execute( 
						Object obj )
					{
					}
				};

				final Closure getter = 
					new Closure()
				{
					@Override
					public void execute( 
						Object obj )
					{
						final Object[] r = (Object[])obj;

						if (r != null)
						{
							DocumentData data = (DocumentData)r[ 0 ];
							boolean openAs = (Boolean)r[ 1 ];
							loadDocument( data.getUniqueId(), false, openAs );
						}
					}
				};

				try
				{
					boolean allowMaster = (getCurrentFrame() == null) && Environment.isAdministrator();
					OpenProjectDialog.getInstance( getFrame(), descriptors, Messages.getString( "Text.openProject" ),
						allowMaster, true, null ).execute( setter, getter ); //$NON-NLS-1$
				}
				finally
				{
					myDoingOpenDialog = false;
				}

				return null;
			}
		} );

		session.schedule( job );
	}

	private void doPreferencesDialog()
	{
		if (getCurrentFrame().isActive() == false)
		{
			return;
		}

		finishAnyOperations();

		java.awt.EventQueue.invokeLater( 
			new Runnable()
		{
			@Override
			public void run()
			{
				PreferencesDialog dialog = new PreferencesDialog( getFrame(), true );
				dialog.setVisible( true );
			}
		} );
	}
	
	private void doProjectInformationDialog()
	{
		if (getCurrentFrame().isActive() == false)
		{
			return;
		}

		finishAnyOperations();

		if (myProjectInformationDialog == null)
		{
			myProjectInformationDialog = ProjectInformationDialog.getInstance( getFrame(), getCurrentFrame().getProject() );
			myProjectInformationDialog.pack();
			myProjectInformationDialog.setModal( false );
		}
		else
		{
			myProjectInformationDialog.setObject( getCurrentFrame().getProject() );
		}

		myProjectInformationDialog.setLocationRelativeTo( getCurrentFrame() );

		//to center on screen
		myProjectInformationDialog.setVisible( true );
	}

	public String doRenameProjectDialog( 
		final String _name,
		final Set _projectNames,
		final boolean saveAs )
	{
		finishAnyOperations();

		RenameProjectDialog renameProjectDialog = RenameProjectDialog.getInstance( getFrame(), null );
		renameProjectDialog.getForm().setName( _name );
		renameProjectDialog.getForm().setProjectNames( _projectNames );
		renameProjectDialog.getForm().setSaveAs( saveAs );

		if (renameProjectDialog.doModal())
		{
			return renameProjectDialog.getForm().getName();
		}

		return null;
	}

	public void doUndoRedo( 
		final boolean _isUndo )
	{
		DocumentFrame frame = getCurrentFrame();
		UndoController undoController = getUndoController();
		Object[] args = null;

		if (undoController != null)
		{
			if (_isUndo)
			{
				String name = undoController.getUndoName();

				if (name != null)
				{
					args = new Object[]
						{
							true,
							name
						};
				}
			}
			else
			{
				String name = undoController.getRedoName();

				if (name != null)
				{
					args = new Object[]
						{
							false,
							name
						};
				}
			}
		}

		if (args == null)
		{
			args = new Object[]
				{
					_isUndo
				};
		}

		addHistory( "doUndoRedo", args );
		frame.doUndoRedo( _isUndo );
	}

	public void doWelcomeDialog()
	{
		WelcomeDialog instance = WelcomeDialog.getInstance( getFrame(), getMenuManager() );

		if (instance.doModal() == true)
		{
			waitInitialization();

			if (instance.getForm().isCreateProject() == true)
			{
				doNewProjectDialog();
			}
			else if (instance.getForm().isOpenProject() == true)
			{
				if (Environment.getStandAlone() || Environment.isNoPodServer())
				{
					openLocalProject();
				}
				else
				{
					doOpenProjectDialog();
				}
			}
			else if (instance.getForm().isImportProject() == true)
			{
				if (Environment.getStandAlone() || Environment.isNoPodServer())
				{
					openLocalProject();
				}
				else
				{
					doOpenProjectDialog();
				}
			}
			else if (instance.getForm().isManageResources() == true)
			{
				loadMasterProject();
			}
		}
	}

	public void encodeWorkspace()
	{
		if (BINARY_WORKSPACE)
		{
			encodeWorkspaceBinary();
		}
		else
		{
			encodeWorkspaceXML();
		}
	}

	private void encodeWorkspaceBinary()
	{
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream out;

		try
		{
			out = new ObjectOutputStream(bout);
			out.writeObject( createWorkspace( SavableToWorkspace.VIEW ) );
			out.close();
			bout.close();
			myLastWorkspace = bout.toByteArray();
			System.out.println( "workspace initialized" );
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Encode the current workspace and store it off in lastWorkspace.
	 * Currently I use an XML format for easier debugging. It could be serialized as binary as well since
	 * all objects in the graph implement Serializable
	 *
	 */
	private void encodeWorkspaceXML()
	{
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(stream));
		encoder.writeObject( createWorkspace( SavableToWorkspace.VIEW ) );
		encoder.close();
		myLastWorkspace = stream.toString();
		System.out.println( "workspace initialized" );

		//		System.out.println(myLastWorkspace);
	}

	@Override
	protected void finalize()
		throws Throwable
	{
		//		System.out.println("~~~~~~~~~~~~~~~~ GraphicManager.finalize()");
		super.finalize();
	}

	public void finishAnyOperations()
	{
		if (getCurrentFrame() != null)
		{
			getCurrentFrame().finishAnyOperations();
		}
	}

	public void finishInitialization()
	{
		myContainer.setVisible( true );
		myInitialized = true;
		myInitializing.unlock();
		showWaitCursor( false );
	}

	@Override
	public Action getAction( 
		final String _key )
		throws MissingListenerException
	{
		if (myActionsMap == null)
		{
			addHandlers();
		}

		Action action = myActionsMap.getConcreteAction( _key );

		if (action == null)
		{
			throw new MissingListenerException( "no listener for mainFrame", getClass().getName(), _key ); //$NON-NLS-1$
		}

		return action;
	}

	public ResourceInTeamFilter getAssignmentDialogTransformerInitializationClosure()
	{
		return myAssignmentDialogTransformerInitializationClosure;
	}

	public BaselineDialog getBaselineDialog()
	{
		return myBaselineDialog;
	}

	public HashMap getColorThemes()
	{
		if (myColorThemes == null)
		{
			myColorThemes = new HashMap<String,String>();
			myColorThemes.put( ACTION_GANTT, "Bloody Moon" ); //$NON-NLS-1$
			myColorThemes.put( ACTION_TRACKING_GANTT, "Mahogany" ); //$NON-NLS-1$
			myColorThemes.put( ACTION_NETWORK, "Emerald Grass" ); //$NON-NLS-1$
			myColorThemes.put( ACTION_RESOURCES, "Blue Yonder" ); //$NON-NLS-1$
			myColorThemes.put( ACTION_PROJECTS, "Emerald Grass" ); //$NON-NLS-1$
			myColorThemes.put( ACTION_WBS, "Sepia" ); //$NON-NLS-1$
			myColorThemes.put( ACTION_RBS, "Steel Blue" ); //$NON-NLS-1$
			myColorThemes.put( ACTION_REPORT, "Aqua" ); //$NON-NLS-1$
			myColorThemes.put( ACTION_TASK_USAGE_DETAIL, "Brown Velvet" ); //$NON-NLS-1$
			myColorThemes.put( ACTION_RESOURCE_USAGE_DETAIL, "Earth Fresco" ); //$NON-NLS-1$
		}

		return myColorThemes;
	}

	public Container getContainer()
	{
		return myContainer;
	}

	/**
	 * Methods that are called using reflection to save workspace stuff into project
	 * @return
	 */
	public static SpreadSheetFieldArray getCurrentFieldArray()
	{
		try
		{
			return (SpreadSheetFieldArray)getDocumentFrameInstance().getGanttView().getSpreadSheet()
											  .getFieldArrayWithWidths( getDocumentFrameInstance().getGanttColumns() );
		}
		catch (Exception e)
		{
			System.out.println( "field array not valid " + e.getMessage() );

			return null;
		}
	}

	public DocumentFrame getCurrentFrame()
	{
		return myCurrentFrame;
	}

	public Project getCurrentProject()
	{
		DocumentFrame f = getCurrentFrame();

		if (f == null)
		{
			return null;
		}

		return f.getProject();
	}

	public static DocumentFrame getDocumentFrameInstance()
	{
		return (myLastGraphicManager == null)
			? null
			: myLastGraphicManager.getCurrentFrame();
	}

	public FilterToolBarManager getFilterToolBarManager()
	{
		return myFilterToolBarManager;
	}

	public Frame getFrame()
	{
		return myFrame;
	}

	public static Frame getFrameInstance()
	{
		return myLastGraphicManager.getFrame();
	}

	@Override
	public FrameManager getFrameManager()
	{
		if (myFrameManager == null)
		{
			System.out.println( "frame manager null, so initView being called" );
			initView();
		}

		return myFrameManager;
	}

	public static Container getGlobalContainer()
	{
		return getInstance().getContainer();
	}

	@Override
	public GraphicManager getGraphicManager()
	{
		return this;
	}

	public static LinkedList getGraphicManagers()
	{
		return myGraphicManagers;
	}

	public static List<CommandInfo> getHistory()
	{
		if (myLastGraphicManager == null)
		{
			return null;
		}

		return myLastGraphicManager.myHistory;
	}

	/** determines the parent graphic manager for a component
	 *
	 * @param _component
	 * @return
	 */
	public static GraphicManager getInstance( 
		final Component _component )
	{
		Component c = _component;

		for (c = _component; c != null; c = c.getParent())
		{
			if (c instanceof FrameHolder)
			{
				return ((FrameHolder)c).getGraphicManager();
			}
			else if ((c.getName() != null) && c.getName().endsWith( "BootstrapApplet" ) &&
					c.getClass().getName().endsWith( "BootstrapApplet" ))
			{
				System.out.println( "applet: " + c.getClass().getName() );

				try
				{
					FrameHolder holder = (FrameHolder)Class.forName( "com.projity.bootstrap.BootstrapApplet.class" )
														   .getMethod( "getObject", null ).invoke( c, null );

					return holder.getGraphicManager();
				}
				catch (Exception e)
				{
					return null;
				}
			}
		}

		return myLastGraphicManager; // if none found, use last used one
	}

	public static GraphicManager getInstance()
	{
		//System.out.println("Graphic manager getInstance = " + myLastGraphicManager.hashCode());
		return myLastGraphicManager;
	}

	public JobQueue getJobQueue()
	{
		if (myJobQueue == null)
		{
			myJobQueue = new JobQueue("GraphicManager", false); //$NON-NLS-1$
		}

		return myJobQueue;
	}

	public LafManager getLafManager()
	{
		if (mylafManager == null)
		{
			try
			{
				String lafName = Messages.getMetaString( "LafManager" );
				mylafManager = (LafManager)Class.forName( lafName ).getConstructor( new Class[]
						{
							GraphicManager.class
						} ).newInstance( new Object[]
						{
							this
						} );
			}
			catch (IllegalArgumentException | SecurityException | InstantiationException | IllegalAccessException 
				| InvocationTargetException | NoSuchMethodException | ClassNotFoundException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			//mylafManager=new LafManager(this);
		}

		return mylafManager;
	}

	public String getLastFileName()
	{
		return myLastFileName;
	}

	public static Object getLastWorkspace()
	{
		return myLastWorkspace;
	}

	private Closure getLoadClosure()
	{
		return null;

//		return new Closure() {
//
//			public void execute(Object arg0) {
//				Project proj = (Project)arg0;
//				SpreadSheetFieldArray fieldArray = (SpreadSheetFieldArray) proj.getDocumentWorkspace().getSetting("fieldArray");
//				if (fieldArray != null)
//					getCurrentFrame().getGanttView().getSpreadSheet().setFieldArray(fieldArray);
//			}
//
//		};
	}

	/**
	 * @return Returns the menuManager.
	 */
	public MenuManager getMenuManager()
	{
		if (myMenuManager == null)
		{
			myMenuManager = MenuManager.getInstance( this );
			addHandlers();
		}

		return myMenuManager;
	}

	private LookAndFeel getPlaf()
	{
		return getLafManager().getPlaf();
	}

//	public GlobalPreferences getPreferences()
//	{
//		if (myPreferences == null)
//		{
//			myPreferences = new GlobalPreferences();
//
//			if (Environment.isExternal() == true)
//			{
//				myPreferences.setShowProjectResourcesOnly( true );
//			}
//		}
//
//		return myPreferences;
//	}

	public static Project getProject()
	{
		if (myLastGraphicManager == null)
		{
			return null;
		}

		if (myLastGraphicManager.myCurrentFrame == null)
		{
			return null;
		}

		return myLastGraphicManager.myCurrentFrame.getProject();
	}

	public final ProjectFactory getProjectFactory()
	{
		return myProjectFactory;
	}

	public Action getRawAction( 
		String s )
	{
		return myActionsMap.getActionFromMenuId( s );
	}

	private Closure getSavingClosure()
	{
		return null;

		//		return new Closure() {
		//
		//			public void execute(Object arg0) {
		//				Project proj = (Project)arg0;
		//				SpreadSheetFieldArray fieldArray = (SpreadSheetFieldArray) getCurrentFrame().getGanttView().getSpreadSheet().getFieldArray();
		//				proj.getDocumentWorkspace().setSetting("fieldArray", fieldArray);
		//			}
		//
		//		};
		//
	}

	public StartupFactory getStartupFactory()
	{
		return myStartupFactory;
	}

	@Override
	public String getStringFromAction( 
		final Action _action )
		throws MissingListenerException
	{
		return myActionsMap.getStringFromAction( _action );
	}

	public void initLayout()
	{
		getFrameManager().getWorkspace().setLayout( new BorderLayout() );
	}

	public void initProject()
	{
		//projects loaded in doStartupAction
		//        if (projectUrl == null && !GeneralOption.getInstance().isStartWithBlankProject()) {
		//			//System.out.println("not opening anything");
		//		} else if (projectUrl == null   || projectUrl.length==0 || projectUrl[0].startsWith("http")) { //same as in Main //$NON-NLS-1$
		//			System.out.println("loading local project:" +projectUrl); //$NON-NLS-1$
		//			boolean ok = loadLocalDocument(projectUrl[0],true); //if null then it will create a new project. WebStart will send a file name
		//			if (!ok)
		//				return;
		//		}
		////		else {
		////			loadDownloadedDocument(); //not used anymore
		////		}
		if (myCurrentFrame != null)
		{
			myCurrentFrame.activateView( ACTION_GANTT );
		}
	}

	private void invokeFieldAction( 
		final String _action,
		final Object obj )
	{
		Field f = FieldDictionary.getInstance().getActionField( ACTION_DOCUMENTS );

		if (f != null)
		{
			f.invokeAction( obj );
		}
	}

	public boolean isDocumentActive()
	{
		return (myCurrentFrame != null) && myCurrentFrame.isActive();
	}

	public boolean isDocumentWritable()
	{
		return (myCurrentFrame != null) && myCurrentFrame.isActive() && !myCurrentFrame.getProject().isReadOnly();
	}

	//	protected void loadDownloadedDocument(){
	//		//showWaitCursor(true);
	//
	//		myProjectFactory.openDownloadedProject();
	//		//showWaitCursor(false);
	//	}
	public Document loadDocument( 
		final long _id,
		final boolean _sync,
		final boolean _openAs )
	{
		return loadDocument( _id, _sync, _openAs, null );
	}

	protected Document loadDocument( 
		final long _id,
		final boolean _sync,
		final boolean _openAs,
		final Closure _endSwingClosure )
	{
		addHistory( "loadDocument", new Object[]
		{
			_id,
			_sync,
			_openAs,
			_endSwingClosure == null
		} );

		//showWaitCursor(true);
		if (_id == -1L)
		{
			return null;
		}

		ProjectFactory factory = myProjectFactory;
		factory.setServer( myServer );

		LoadOptions opt = new LoadOptions();
		opt.setId( _id );
		opt.setSync( _sync );
		opt.setOpenAs( _openAs );
		opt.setEndSwingClosure( _endSwingClosure );

		//		if (Environment.isNoPodServer()&&opt.getFormat()==0) opt.setFormat(SessionFactory.NO_POD_FORMAT);
		Document result = factory.openProject( opt );

		//showWaitCursor(false);
		return result;
	}

	protected boolean loadLocalDocument( 
		final String _fileName,
		final boolean _merge,
		final boolean _importCalendars )
	{ 
		//uses server to merge
		addHistory( "loadLocalDocument", 
			new Object[]
		{
			_fileName,
			_merge
		} );

		//showWaitCursor(true);
		Project project;

		if (_fileName == null)
		{
			//System.out.println("creating empty project");
			project = myProjectFactory.createProject();
		}
		else
		{
			LoadOptions opt = new LoadOptions();
			opt.setFileName( _fileName );
			opt.setLocal( true );
			opt.setSync( false );

			if (_merge == true)
			{
				ResourceMappingForm form = 
					new ResourceMappingForm()
				{
					@Override
					public boolean execute()
					{
						if ((getImportedResources() == null) 
						 || (getImportedResources().size() == 0)) 
						{
							// do not show dialog if no resources were imported
							return true;
						}

						if (myResourceMappingDialog == null)
						{
							myResourceMappingDialog = ResourceMappingDialog.getInstance( this );
							myResourceMappingDialog.pack();
							myResourceMappingDialog.setModal( true );
						}
						else
						{
							myResourceMappingDialog.setForm( this );
						}

						myResourceMappingDialog.bind( true );
						myResourceMappingDialog.setLocationRelativeTo( getCurrentFrame() );

						//to center on screen
						myResourceMappingDialog.setVisible( true );

						return myResourceMappingDialog.getDialogResult() == JOptionPane.OK_OPTION;
					}
				};

				form.setExistingProject( getCurrentProject() );
				opt.setResourceMapping( form );
			}

			if (_fileName.endsWith( ".pod" ) == true)
			{
				opt.setImporter( Environment.getStandAlone()
					? LocalSession.LOCAL_PROJECT_IMPORTER
					: LocalSession.SERVER_LOCAL_PROJECT_IMPORTER );
			}
			else
			{
				opt.setImporter( LocalSession.MICROSOFT_PROJECT_IMPORTER );
				opt.setSkipCalendars( !_importCalendars );
			}

			project = myProjectFactory.openProject( opt );
		}

		//showWaitCursor(false);
		return project != null;
	}

	protected Document loadMasterProject()
	{
		return loadDocument( Session.MASTER, false, false );
	}

	@Override
	public void namedFrameActivated( 
		final NamedFrameEvent _event )
	{
		//		System.out.println("Frame activated");
		NamedFrame frame = _event.getNamedFrame();

		if (frame instanceof DocumentFrame)
		{
			DocumentFrame df = (DocumentFrame)frame;
			setCurrentFrame( df );
		}
	}

	@Override
	public void namedFrameShown( 
		final NamedFrameEvent _event )
	{
	}

	@Override
	public void namedFrameTabShown( 
		final NamedFrameEvent _event )
	{
		NamedFrame frame = _event.getNamedFrame();

		if (frame instanceof DocumentFrame)
		{
			DocumentFrame df = (DocumentFrame)frame;
			setCurrentFrame( df );
		}
	}

	/**
	 * 
	 */
	public void openLocalProject()
	{
		openLocalProject( true );
	}

	/**
	 * 
	 * @param _importCalendars 
	 */
	public void openLocalProject( 
		final boolean _importCalendars )
	{
		final String fileName = SessionFactory.getInstance().getLocalSession().chooseFileName( false, null );

		if (fileName != null)
		{
			loadLocalDocument( fileName, !Environment.getStandAlone(), _importCalendars );
		}
	}

	protected void saveLocalDocument( 
		final String _fileName,
		final boolean _saveAs )
	{
		addHistory( "saveLocalDocument", new Object[]
		{
			_fileName,
			_saveAs
		} );

		//showWaitCursor(true);
		SaveOptions opt = new SaveOptions();
		opt.setLocal( true );

		final Project project = getCurrentFrame().getProject();

		if (project.getFileName().equals( _fileName ) == false)
		{
			final DocumentFrame frame = getCurrentFrame();

			if (_saveAs == true)
			{
				opt.setSaveAs( true );
			}

			opt.setPostSaving( 
				new Closure()
			{
				@Override
				public void execute( 
					Object _object )
				{
					if (_saveAs == true)
					{
						frame.setId( project.getUniqueId() + "" );
					}

					refreshSaveStatus( true );
				}
			} );
		}

		if (_fileName.endsWith( ".pod" ) == true)
		{
			opt.setFileName( _fileName );
			opt.setImporter( LocalSession.LOCAL_PROJECT_IMPORTER );
		}
		else
		{
			opt.setFileName( _fileName /*+((_fileName.endsWith(".xml"))?"":".xml")*/ );
			opt.setImporter( LocalSession.MICROSOFT_PROJECT_IMPORTER );

			if (Environment.isOpenProj())
			{
				if (!Alert.okCancel( Messages.getString( "Warn.saveXML" ) ))
				{
					return;
				}
			}
		}

		opt.setPreSaving( getSavingClosure() );
		myProjectFactory.saveProject( getCurrentFrame().getProject(), opt );

		//showWaitCursor(false);
	}

	protected void saveLocalDocument( 
		final Project _project,
		final String _fileName )
	{
		//showWaitCursor(true);
		final SaveOptions opt = new SaveOptions();
		opt.setFileName( _fileName );
		opt.setLocal( true );
		opt.setPreSaving( getSavingClosure() );

		myProjectFactory.saveProject( _project, opt );

		//showWaitCursor(false);
	}

	public void saveLocalProject( 
		final boolean _saveAs )
	{
		String fileName = null;
		Project project = getCurrentFrame().getProject();

		if (!_saveAs)
		{
			fileName = project.getFileName();
		}

		if (fileName == null)
		{
			fileName = SessionFactory.getInstance().getLocalSession().chooseFileName( true, project.getGuessedFileName() );
		}

		if (fileName != null)
		{
			saveLocalDocument( fileName, _saveAs );
		}
	}

	protected void setCurrentFrame( 
		final DocumentFrame _frame )
	{
		if (_frame instanceof DocumentFrame)
		{
			if (myCurrentFrame == _frame)
			{
				return;
			}

			if ((myCurrentFrame != null) && (myProjectListMenu != null) && !Environment.isPlugin())
			{
				myCurrentFrame.getMenuItem().setSelected( false );
			}

			if ((myCurrentFrame != null) && !Environment.isPlugin())
			{
				myCurrentFrame.refreshViewButtons( false ); // disable buttons for old view
			}

			myCurrentFrame = (DocumentFrame)_frame;

			if ((myProjectListMenu != null) && !Environment.isPlugin())
			{
				myCurrentFrame.getMenuItem().setSelected( true );
			}

			if ((myTopTabs != null) && !Environment.isPlugin())
			{
				myTopTabs.setCurrentFrame( myCurrentFrame );
			}

			DocumentSelectedEvent.fire( this, myCurrentFrame );

			if (myProjectInformationDialog != null)
			{
				myProjectInformationDialog.documentSelected( new DocumentSelectedEvent(this, myCurrentFrame) );
			}

			if (myTaskInformationDialog != null)
			{
				myTaskInformationDialog.documentSelected( new DocumentSelectedEvent(this, myCurrentFrame) );
			}

			if (myResourceInformationDialog != null)
			{
				myResourceInformationDialog.documentSelected( new DocumentSelectedEvent(this, myCurrentFrame) );
			}

			setTitle( false );

			if (myCurrentFrame != null)
			{
				myCurrentFrame.refreshViewButtons( true );
			}

			getFrameManager().activateFrame( myCurrentFrame ); // need to force activation in case being activated by closing another

			if (!Environment.isPlugin())
			{
				setEnabledDocumentMenuActions( myCurrentFrame != null );
				setButtonState( null, myCurrentFrame.getProject() );
			}

			if ((myCurrentFrame != null) && (myCurrentFrame.getProject() != null))
			{
				if (!Environment.isPlugin())
				{
					myCurrentFrame.getFilterToolBarManager().transformBasedOnValue();
				}

				CalendarOption calendarOption = myCurrentFrame.getProject().getCalendarOption();

				if (calendarOption != null)
				{
					CalendarOption.setInstance( calendarOption );
				}
			}
			else
			{
				CalendarOption.setInstance( CalendarOption.getDefaultInstance() );
			}
		}
	}

	public synchronized void setEnabledDocumentMenuActions( 
		final boolean _enable )
	{
		if (getProject() == null)
		{
			return;
		}

		if (Environment.isPlugin())
		{
			return;
		}

		myActionsMap.setEnabledDocumentMenuActions( _enable );

		if (getCurrentFrame() != null)
		{
			getCurrentFrame().getFilterToolBarManager().setEnabled( _enable );
			((DefaultFrameManager)getFrameManager()).setLockStatus( getProject().getLockStatus(),
				getProject().getPublishInfoHtml(), getProject().getCheckedOutUser() );

			//        	((DefaultFrameManager)getFrameManager()).setLockStatus(getProject().isLocked(),getProject().isCheckedOutByOther(),getProject().getPublishInfoHtml());
		}
		else
		{
			((DefaultFrameManager)getFrameManager()).setLockStatus( Project.LockStatus.NONE, null, null );

			//        	((DefaultFrameManager)getFrameManager()).setLockStatus(false,false,null);
		}

		if (myTopTabs != null)
		{
			myTopTabs.setTrackingEnabled( _enable && isDocumentWritable() );
		}
	}

	public void showAboutDialog()
	{
		if (myAboutDialog == null)
		{
			myAboutDialog = AboutDialog.getInstance( getFrame() );
			myAboutDialog.pack();
			myAboutDialog.setModal( true );
		}

		myAboutDialog.setLocationRelativeTo( getFrame() );

		//to center on screen
		myAboutDialog.setVisible( true );
	}

	/**
	 * Show or focus the assignment dialog.  If showing, initilize to project
	 * @param project
	 */
	public void showAssignmentDialog( 
		final DocumentFrame _documentFrame )
	{
		if ((myCurrentFrame == null) || !getCurrentFrame().isActive())
		{
			return;
		}

		if (myAssignResourcesDialog == null)
		{
			myAssignResourcesDialog = new AssignmentDialog( _documentFrame );
			myAssignResourcesDialog.pack();
			myAssignResourcesDialog.setModal( false );
		}

		myAssignResourcesDialog.setLocationRelativeTo( _documentFrame );

		//to center on screen
		myAssignResourcesDialog.setVisible( true );
	}

	public void showHelpDialog( /*DocumentFrame documentFrame*/
	)
	{
		if (myHelpDialog == null)
		{
			myHelpDialog = HelpDialog.getInstance( getFrame() );
			myHelpDialog.pack();
			myHelpDialog.setModal( true );
		}

		myHelpDialog.setLocationRelativeTo( getFrame() );

		//to center on screen
		myHelpDialog.setVisible( true );
	}

	public void switchToProject( 
		final long _projectId )
	{
		Project project = ProjectFactory.getInstance().findFromId( _projectId );
		switchToProject( project );
	}

	public void switchToProject( 
		final Project _project )
	{
		if (_project == null)
		{
			return;
		}

		DocumentFrame f = (DocumentFrame)myFrameMap.get( _project );

		if (f == null)
		{
			return;
		}

		setCurrentFrame( f );
	}

	public void windowActivated( 
		final WindowEvent _event )
	{
	}

	public void windowClosed( 
		final WindowEvent _event )
	{
		if (_event.getWindow() == myAssignResourcesDialog)
		{
			myAssignResourcesDialog = null;
		}
	}

	void doCalendarOptionsDialog()
	{
		finishAnyOperations();
		CalendarDialogBox.getInstance( getFrame(), null ).doModal();
	}

	void print()
	{
		final GraphPageable document = PrintDocumentFactory.getInstance().createDocument( getCurrentFrame(), false );

		if (document != null)
		{
			document.print();
		}
	}

	void printPreview()
	{
		final GraphPageable document = PrintDocumentFactory.getInstance().createDocument( getCurrentFrame(), false );

		if (document != null)
		{
			document.preview();
		}
	}

	void savePDF()
	{
		GraphPageable document = PrintDocumentFactory.getInstance().createDocument( getCurrentFrame(), false );

		try
		{
			Class generator = ClassLoaderUtils.forName( "com.projity.image_export.PDFGenerator" );
			generator.getMethod( "generatePDF", 
				new Class[]
			{
				GraphPageable.class,
				Component.class
			} ).invoke( null, 
			new Object[]
			{
				document,
				getContainer()
			} );
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	void setMeAsLastGraphicManager()
	{ 
		// makes this the current graphic manager for job queue and dialogs
		myLastGraphicManager = this;

		if (myJobQueue != null)
		{
			SessionFactory.getInstance().setJobQueue( getJobQueue() );
		}
	}

	void setTabNameAndTitle( 
		final DocumentFrame _frame,
		final Project _project )
	{
		_frame.setTabNameAndTitle( _project );
	}

	void setTitle( 
		final boolean _isSaving )
	{
		final DocumentFrame dframe = getCurrentFrame();
		String title = Messages.getContextString( "Text.ApplicationTitle" );

		if ((dframe != null) 
		 && (dframe.getProject() != null))
		{
			if (Environment.getStandAlone())
			{
				title = dframe.getProject().getTitle();
			}
			else
			{
				title += (" - " + dframe.getProject().getName());
			}

			if ((_isSaving == false) 
			 && (dframe.getProject().needsSaving() == true))
			{
				// modified
				title += " *"; 
			}
		}

		if (myFrame != null)
		{
			myFrame.setTitle( title );
		}
	}

	//    private JFileChooser getFileChooser() {
	//    	if (fileChooser == null)
	//    		fileChooser = new JFileChooser();
	//    	return fileChooser;
	//    }
	private static String getTabIdForProject( 
		final Project _project )
	{
		if (_project == null)
		{
			return null;
		}

		return "" + _project.getUniqueId(); //see later //$NON-NLS-1$
	}

	public String getTopViewId()
	{
		if (getCurrentFrame() == null)
		{
			return ACTION_GANTT;
		}
		else
		{
			return getCurrentFrame().getTopViewId();
		}
	}

	public static UndoController getUndoController()
	{
		final DocumentFrame frame = GraphicManager.getDocumentFrameInstance();

		if (frame == null)
		{
			return null;
		}

		return frame.getUndoController();
	}

	public void initLookAndFeel()
	{
		getLafManager().initLookAndFeel();
	}

	public void initView()
	{
		System.out.println( "initView" );

		final Container c = ((myContainer != null) && (myContainer instanceof RootPaneContainer == true))
			? ((RootPaneContainer)myContainer).getContentPane()
			: myContainer;

		c.setLayout( new BorderLayout() );

		final JPanel panel = new JPanel();
		c.add( panel, "Center" );
		setFrameManager( new DefaultFrameManager( myContainer, panel, this ) );

		initLayout();

		if (Environment.isPlugin() == false)
		{
			setToolBarAndMenus( c );
		}

		setEnabledDocumentMenuActions( false );

		final Workspace workspace = decodeWorkspace();

		if (workspace != null)
		{
			restoreWorkspace( workspace, SavableToWorkspace.VIEW );
		}
		else
		{
			initProject();
		}

		//        myContainer.invalidate();
	}

	public void insertSubproject( 
		final Project _project,
		final long _subprojectUniqueId,
		final boolean _undo )
	{
		addHistory( "insertSubproject", 
			new Object[]
		{
			_project.getName(),
			_project.getUniqueId(),
			_subprojectUniqueId
		} );

		Project openedAlready = ProjectFactory.getInstance().findFromId( _subprojectUniqueId );

		if (_project.getSubprojectHandler().canInsertProject( _subprojectUniqueId ) == false)
		{
			Alert.error( Messages.getString( "GraphicManager.SelectedProjectAlreadySubproject" ) );
			return;
		}

		if ((openedAlready != null) 
		 && (openedAlready.isOpenedAsSubproject() == true))
		{
			Alert.error( Messages.getString( "GraphicManager.SelectedProjectAlreadyOpenedAsSubproject" ) );
			return;
		}

		final SubProj subprojectTask = _project.getSubprojectHandler().createSubProj( _subprojectUniqueId );
		final Node subprojectNode = getCurrentFrame().addNodeForImpl( subprojectTask, NodeModel.EVENT );
		ProjectFactory.getInstance().openSubproject( _project, subprojectNode, true );

		//Undo
		if (_undo == true)
		{
			final UndoController undoContoller = _project.getNodeModelDataFactory().getUndoController();

			if (undoContoller.getEditSupport() != null)
			{
				undoContoller.clear();

				//undoContoller.getEditSupport().postEdit(new CreateSubprojectEdit(project,subprojectNode,subprojectUniqueId));
			}
		}
	}

	public void invalidate()
	{
		myContainer.invalidate();
		((RootPaneContainer)myContainer).getContentPane().invalidate();
		((RootPaneContainer)myContainer).getContentPane().repaint();
	}

	public static boolean is1_6Version()
	{
		final String[] figures = System.getProperty( "java.version" ).split( "\\." );

		if ((figures != null) && (figures.length >= 2))
		{
			return ((Integer.parseInt( figures[ 0 ] ) >= 1) && (Integer.parseInt( figures[ 1 ] ) >= 6));
		}

		return false;
	}

	public boolean isApplet()
	{
		return myContainer instanceof Applet;
	}

	public boolean isEditingMasterProject()
	{
		final Project currentProject = myCurrentFrame.getProject();

		if (currentProject == null)
		{
			return false;
		}

		return currentProject.isMaster() && !currentProject.isReadOnly();
	}

	public boolean isProjectLockedByOther()
	{
		if (getProject() == null)
		{
			return false;
		}

		return getProject().isCheckedOutByOther();
	}

	public boolean lockProject()
	{
		if (getProject().isLocked() == false)
		{
			Session session = SessionFactory.getInstance().getSession( false );

			return session.getLock( getProject(), false );
		}

		return true;
	}

	/* (non-Javadoc)
	 * @see com.projity.document.ObjectEvent.Listener#objectChanged(com.projity.document.ObjectEvent)
	 */
	@Override
	public void objectChanged( 
		final ObjectEvent _objectEvent )
	{
		if (_objectEvent.getObject() instanceof Project == true)
		{
			Project project = (Project)_objectEvent.getObject();

			if (_objectEvent.isCreate() == true)
			{
				if (project.isOpenedAsSubproject() == true)
				{
					closeProjectFrame( project ); // because it's now in a project
				}
				else
				{
					DocumentFrame f = addProjectFrame( project );
				}
			}
			else if (_objectEvent.isDelete() == true)
			{
				closeProjectFrame( project );
			}

			if (myProjectInformationDialog != null)
			{
				myProjectInformationDialog.objectChanged( _objectEvent );
			}

			if (myTaskInformationDialog != null)
			{
				myTaskInformationDialog.objectChanged( _objectEvent );
			}

			if (myResourceInformationDialog != null)
			{
				myResourceInformationDialog.objectChanged( _objectEvent );
			}
		}
	}

	public void openFile( 
		final String _fileName )
	{
		myLastFileName = _fileName;

		if ((_fileName != null) 
		 && (myInitialized == true))
		{
			loadLocalDocument( _fileName, !Environment.getStandAlone(), true );
		}
	}

	public boolean quitApplication()
		throws Exception
	{
		final boolean[] lock = new boolean[]
		{
			false
		};

		JobRunnable exitRunnable = 
			new JobRunnable("Local: closeProjects")
		{
			@Override
			public Object run()
				throws Exception
			{
				synchronized (lock)
				{
					lock[ 0 ] = true;
					lock.notifyAll();
				}

				return null;
			}
		};

		final boolean[] closeStatus = new boolean[]
		{
			false
		};
		final Job job = myProjectFactory.getPortfolio().getRemoveAllProjectsJob( exitRunnable, false, closeStatus );
		SessionFactory.getInstance().getLocalSession().schedule( job );

		synchronized (lock)
		{
			while (lock[ 0 ] == false)
			{
				try
				{
					lock.wait();
				}
				catch (InterruptedException e)
				{
					// ignore
				}
			}
		}

		if (closeStatus[ 0 ] == true)
		{
			final Frame frame = getFrame();

			if (frame != null)
			{
				frame.dispose();
			}

			//System.exit(0);
			return true;
		}
		else
		{
			return false;
		}
	}

	public void registerForMacOSXEvents()
	{
		if (Environment.isMac() == true)
		{
			try
			{
				// Generate and register the OSXAdapter, passing it a hash of all the methods we wish to
				// use as delegates for various com.apple.eawt.ApplicationListener methods
				OSXAdapter.setQuitHandler( this, getClass().getDeclaredMethod( "quitApplication", (Class[])null ) );
				OSXAdapter.setAboutHandler( this, getClass().getDeclaredMethod( "showAboutDialog", (Class[])null ) );

				//OSXAdapter.setPreferencesHandler(this, getClass().getDeclaredMethod("myPreferences", (Class[])null));
				if (Environment.getStandAlone())
				{
					OSXAdapter.setFileHandler( this, getClass().getDeclaredMethod( "openFile", new Class[]
							{
								String.class
							} ) );
				}
			}
			catch (Exception e)
			{
				System.err.println( "Error while loading the OSXAdapter:" );

				//    e.printStackTrace();
			}
		}
	}

	@Override
	public void restoreWorkspace( 
		final WorkspaceSetting _setting,
		final int _context )
	{
		Workspace workspace = (Workspace)_setting;
		myColorThemes = workspace.getColorThemes();
		getFrameManager().restoreWorkspace( workspace.getFrames(), _context );
	}

	@Override
	public void selectionChanged( 
		final SelectionNodeEvent _e )
	{
		if (myAssignResourcesDialog != null)
		{
			myAssignResourcesDialog.selectionChanged( _e );
		}

		Node currentNode = _e.getCurrentNode();
		Object currentImpl = currentNode.getValue();
		setButtonState( currentImpl, myCurrentFrame.getProject() );

		// if on resource view, hide task info and vice versa.  Otherwise just show it
		if ((myLastNode != null) && (myTaskInformationDialog != null) &&
				(myLastNode.getValue() instanceof Task || myLastNode.getValue() instanceof Assignment) &&
				currentNode.getValue() instanceof Resource)
		{
			myTaskInformationDialog.setVisible( false );
			doInformationDialog( false );
		}
		else if ((myLastNode != null) && (myResourceInformationDialog != null) && myLastNode.getValue() instanceof Resource &&
				(currentNode.getValue() instanceof Task || currentNode.getValue() instanceof Assignment))
		{
			myResourceInformationDialog.setVisible( false );
			doInformationDialog( false );
		}
		else
		{
			if (myTaskInformationDialog != null)
			{
				myTaskInformationDialog.selectionChanged( _e );
			}

			if (myResourceInformationDialog != null)
			{
				myResourceInformationDialog.selectionChanged( _e );
			}
		}

		myLastNode = currentNode;
	}

	public void setAllButResourceDisabled( 
		final boolean _disable )
	{
		if (myTopTabs != null)
		{
			myTopTabs.setAllButResourceDisabled( _disable );
		}
	}

	public Closure setAssignmentDialogTransformerInitializationClosure()
	{
		return new Closure()
		{
			@Override
			public void execute( 
				Object arg )
			{
				ViewTransformer transformer = (ViewTransformer)arg;
				NodeFilter hiddenFilter = transformer.getHiddenFilter();

				if ((hiddenFilter != null) && hiddenFilter instanceof ResourceInTeamFilter)
				{
					myAssignmentDialogTransformerInitializationClosure = (ResourceInTeamFilter)hiddenFilter;
					myAssignmentDialogTransformerInitializationClosure.setFilterTeam( 
						GlobalPreferences.getInstance().isShowProjectResourcesOnly() );
				}
				else
				{
					myAssignmentDialogTransformerInitializationClosure = null;
				}
			}
		};
	}

	public void setBaselineDialog( 
		final BaselineDialog _baselineDialog )
	{
		myBaselineDialog = _baselineDialog;
	}

	public void setConnected( 
		boolean _connected )
	{
		getMenuManager().setActionEnabled( ACTION_IMPORT_MSPROJECT, _connected );
		getMenuManager().setActionEnabled( ACTION_OPEN_PROJECT, _connected );
		getMenuManager().setActionEnabled( ACTION_NEW_PROJECT, _connected );

		if (_connected)
		{
			refreshSaveStatus( true );
		}
	}

	public static void setCurrentFieldArray( 
		final Object _fieldArray )
	{
		getDocumentFrameInstance().getGanttView().getSpreadSheet().setFieldArrayWithWidths( (SpreadSheetFieldArray)_fieldArray );
	}

	public void setFrameManager( 
		final FrameManager _frameManager )
	{
		myFrameManager = _frameManager;
	}

	@Override
	public void setGraphicManager( 
		final GraphicManager _manager )
	{
		// TODO Auto-generated method stub
	}

	public void setPaletteText( 
		final String _themeName )
	{
		getMenuManager().setText( ACTION_PALETTE, _themeName );
	}

	public void setStartupFactory( 
		final StartupFactory _startupFactory )
	{
		myStartupFactory = _startupFactory;
	}

	public void setTaskInformation( 
		final boolean _taskType,
		final boolean _resourceType )
	{
		myTaskType = _taskType;
		myResourceType = _resourceType;

		//		JButton button = null;
		//		String infoText = "Task Information";
		//		String notesText = "Task Notes";
		//		String insertText = getMenuManager().getString(ACTION_INSERT_TASK + ButtonFactory.TOOLTIP_SUFFIX);
		//		if (myResourceType&&!myTaskType){
		//			infoText = "Resource Information";
		//			notesText = "Resource Notes";
		//			insertText = "Insert Resource";
		//		}
		//		getMenuManager().setText(ACTION_INFORMATION,infoText);
		//		getMenuManager().setText(ACTION_NOTES,notesText);
		//		getMenuManager().setText(ACTION_INSERT_TASK,insertText);
	}

	protected void setToolBarAndMenus( 
		final Container _contentPane )
	{
		JToolBar toolBar;

		if (Environment.isNoPodServer() == true)
		{
			//icons
			if (myInitialized == true)
			{
				return;
			}

			toolBar = getMenuManager().getToolBar( "MarinerToolBar" );

			toolBar.setBackground( new Color( 238, 238, 238 ) );

			MatteBorder lightBorder = BorderFactory.createMatteBorder( 1, 0, 0, 0, new Color( 247, 247, 247 ) );
			MatteBorder darkBorder = BorderFactory.createMatteBorder( 0, 1, 0, 1, new Color( 192, 192, 192 ) );
			toolBar.setBorder( new CompoundBorder( darkBorder, lightBorder ) );
			toolBar.setFloatable( false );
			myFilterToolBarManager = FilterToolBarManager.create( getMenuManager() );

			myFilterToolBarManager.addButtons( toolBar );
			_contentPane.add( toolBar, BorderLayout.BEFORE_FIRST_LINE );

			//menu
			JMenuBar menu = getMenuManager().getMenu( "MarinerMenuBar" );

			LafUtils.addMouseOverHighlight( menu, UIManager.getColor( "MenuItem.selectionBackground" ), true );
			LafUtils.addMouseOverBorder( menu, new Color(86, 156, 191), 1, false );

			MatteBorder lineBorder = BorderFactory.createMatteBorder( 2, 1, 1, 1, new Color( 192, 192, 192 ) );
			menu.setBorder( new CompoundBorder(lineBorder, new EmptyBorder( 1, 6, 1, 6 )) );

			JPanel comboPanel = ((DefaultFrameManager)getFrameManager()).getProjectComboPanel();
			menu.add( Box.createHorizontalGlue() );
			menu.add( comboPanel );

			if (myContainer instanceof JApplet == false)
			{
				((JFrame)myContainer).setJMenuBar( menu );
			}
			else
			{
				((JApplet)myContainer).setJMenuBar( menu );
			}
		}
		else
		{
			if (Environment.isNewLook() == true)
			{
				if (Environment.isNeedToRestart() == true)
				{
					_contentPane.add( new JLabel(Messages.getString( "Error.restart" )), BorderLayout.CENTER );

					return;
				}

				toolBar = getMenuManager().getToolBar( Messages.getString( "MainToolBarName" ) );

				if (getLafManager().isToolbarOpaque() == false)
				{
					toolBar.setOpaque( false );
				}

				if (isApplet() == false)
				{
					getMenuManager().setActionVisible( ACTION_FULL_SCREEN, false );
				}

				if (Environment.isExternal() == true) // external users only see project team
				{
					getMenuManager().setActionVisible( ACTION_TEAM_FILTER, false );
				}

				toolBar.addSeparator( new Dimension(20, 20) );
				toolBar.add( new Box.Filler(new Dimension(0, 0), new Dimension(0, 0),
						new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE)) );
				toolBar.add( ((DefaultFrameManager)getFrameManager()).getProjectComboPanel() );
				toolBar.add( Box.createRigidArea( new Dimension(20, 20) ) );

				if (Environment.isNewLaf() == true)
				{
					toolBar.setBackground( Color.WHITE );
				}

				toolBar.setFloatable( false );
				toolBar.setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );

				Box top;
				JComponent bottom;

				top = new Box(BoxLayout.Y_AXIS);
				toolBar.setAlignmentX( 0.0f ); // so it is left justified
				top.add( toolBar );

				JToolBar viewToolBar = getMenuManager().getToolBar( MenuManager.VIEW_TOOL_BAR_WITH_NO_SUB_VIEW_OPTION );
				myTopTabs = new TabbedNavigation();

				JComponent tabs = myTopTabs.createContentPanel( getMenuManager(), viewToolBar, 0, JTabbedPane.TOP, true );
				tabs.setAlignmentX( 0.0f ); // so it is left justified

				top.add( tabs );
				bottom = new TabbedNavigation().createContentPanel( getMenuManager(), viewToolBar, 1, JTabbedPane.BOTTOM, false );
				_contentPane.add( top, BorderLayout.BEFORE_FIRST_LINE );
				_contentPane.add( bottom, BorderLayout.AFTER_LAST_LINE );

				if (Environment.isNewLaf() == true)
				{
					_contentPane.setBackground( Color.WHITE );
				}

				if (Environment.isMac() == true)
				{
					//System.setProperty("apple.laf.useScreenMenuBar","true");
					//System.setProperty("com.apple.mrj.application.apple.menu.about.name", Messages.getMetaString("Text.ShortTitle"));
					JMenuBar menu = getMenuManager().getMenu( Environment.getStandAlone()
							? MenuManager.MAC_STANDARD_MENU
							: MenuManager.SERVER_STANDARD_MENU );

					//((JComponent)menu).setBorder(BorderFactory.createEmptyBorder());
					if (myContainer instanceof JApplet == false)
					{
						((JFrame)myContainer).setJMenuBar( menu );
					}

					myProjectListMenu = (JMenu)menu.getComponent( 5 );
				}
			}
			else
			{
				toolBar = getMenuManager().getToolBar( Environment.isMac()
					? MenuManager.MAC_STANDARD_TOOL_BAR
					: MenuManager.STANDARD_TOOL_BAR );
				myFilterToolBarManager = FilterToolBarManager.create( getMenuManager() );
				myFilterToolBarManager.addButtons( toolBar );
				_contentPane.add( toolBar, BorderLayout.BEFORE_FIRST_LINE );

				JToolBar viewToolBar = getMenuManager().getToolBar( MenuManager.VIEW_TOOL_BAR );
				viewToolBar.setOrientation( JToolBar.VERTICAL );
				viewToolBar.setRollover( true );
				_contentPane.add( viewToolBar, BorderLayout.WEST );

				JMenuBar menu = getMenuManager().getMenu( Environment.getStandAlone()
						? (Environment.isMac()
						? MenuManager.MAC_STANDARD_MENU
						: MenuManager.STANDARD_MENU)
						: MenuManager.SERVER_STANDARD_MENU );

				if (Environment.isMac() == false)
				{
					((JComponent)menu).setBorder( BorderFactory.createEmptyBorder() );

					JMenuItem logo = (JMenuItem)menu.getComponent( 0 );
					logo.setBorder( BorderFactory.createEmptyBorder() );
					logo.setMaximumSize( new Dimension(124, 52) );
					logo.setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
				}

				((JFrame)myContainer).setJMenuBar( menu );
				myProjectListMenu = (JMenu)menu.getComponent( Environment.isMac()
					? 5
					: 6 );
			}
		}

		//accelerators
		addCtrlAccel( KeyEvent.VK_G, ACTION_GOTO, null );
		addCtrlAccel( KeyEvent.VK_L, ACTION_GOTO, null );
		addCtrlAccel( KeyEvent.VK_F, ACTION_FIND, null );
		addCtrlAccel( KeyEvent.VK_I, ACTION_INSERT_TASK, null );
		addCtrlAccel( KeyEvent.VK_PERIOD, ACTION_INDENT, null );
		addCtrlAccel( KeyEvent.VK_COMMA, ACTION_OUTDENT, null );
		addCtrlAccel( KeyEvent.VK_PLUS, ACTION_EXPAND, new ExpandAction() );
		addCtrlAccel( KeyEvent.VK_ADD, ACTION_EXPAND, new ExpandAction() );
		addCtrlAccel( KeyEvent.VK_EQUALS, ACTION_EXPAND, new ExpandAction() );
		addCtrlAccel( KeyEvent.VK_MINUS, ACTION_COLLAPSE, new CollapseAction() );
		addCtrlAccel( KeyEvent.VK_SUBTRACT, ACTION_COLLAPSE, new CollapseAction() );
		addCtrlAccel( KeyEvent.VK_Y, ACTION_REDO, null );
		addCtrlAccel( KeyEvent.VK_Z, ACTION_UNDO, null );

		// To force a recalculation. This normally shouldn't be needed.
		addCtrlAccel( KeyEvent.VK_R, ACTION_RECALCULATE, new RecalculateAction() );
	}

	public void setZoomButtons()
	{
		getMenuManager().setActionEnabled( ACTION_ZOOM_IN, (myCurrentFrame != null) && myCurrentFrame.canZoomIn() );
		getMenuManager().setActionEnabled( ACTION_ZOOM_OUT, (myCurrentFrame != null) && myCurrentFrame.canZoomOut() );
	}

	public void showWaitCursor( 
		final boolean _show )
	{
		Frame frame = getFrame();

		if (frame == null)
		{
			return;
		}

		if (_show)
		{
			frame.setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );
		}
		else
		{
			frame.setCursor( Cursor.getDefaultCursor() );
		}
	}

	public void waitInitialization()
	{
		myInitializing.waitUntilUnlocked();
	}

	public void windowClosing( 
		final WindowEvent _event )
	{
	}

	public void windowDeactivated( 
		final WindowEvent _event )
	{
	}

	public void windowDeiconified( 
		final WindowEvent _event )
	{
	}

	public void windowIconified( 
		final WindowEvent _event )
	{
	}

	@Override
	public void windowStateChanged( 
		final WindowEvent _event )
	{
	}

	Set getActionSet()
	{
		Set actions = null;
		DocumentFrame df = getCurrentFrame();

		if (df != null)
		{
			SpreadSheet sp = df.getActiveSpreadSheet();
			actions = new HashSet();

			if (sp != null)
			{
				String[] a = sp.getActionList();

				if (a != null)
				{
					for (int i = 0; i < a.length; i++)
					{
						actions.add( a[ i ] );
					}
				}
			}
		}

		return actions;
	}

	void refreshSaveStatus( 
		final boolean _isSaving )
	{
		getMenuManager()
			.setActionEnabled( ACTION_SAVE_PROJECT, (myCurrentFrame != null) && !_isSaving &&
			myCurrentFrame.getProject().needsSaving() );
		setTitle( _isSaving );

		FrameManager dm = getFrameManager();

		if (dm != null)
		{
			dm.update(); //update project combo
		}
	}

	void setButtonState( 
		final Object _currentImpl,
		final Project _project )
	{
		Set actions = getActionSet();
		boolean infoEnabled = (_currentImpl != null) &&
			(_currentImpl instanceof Assignment || _currentImpl instanceof Task || _currentImpl instanceof Resource);
		boolean notVoid = (_currentImpl != null) && !(_currentImpl instanceof VoidNodeImpl);

		boolean readOnly = !isDocumentWritable();
		boolean pinned = (getProject() != null) && getProject().isDatesPinned();
		getMenuManager().setActionEnabled( ACTION_INFORMATION, infoEnabled );
		getMenuManager().setActionEnabled( ACTION_NOTES, infoEnabled );
		getMenuManager()
			.setActionEnabled( ACTION_INSERT_TASK,
			!readOnly && (myTaskType || myResourceType) && ((actions == null) || actions.contains( ACTION_INSERT_TASK )) );
		getMenuManager()
			.setActionEnabled( ACTION_INSERT_RESOURCE,
			!readOnly && (myTaskType || myResourceType) && ((actions == null) || actions.contains( ACTION_INSERT_TASK )) );
		getMenuManager()
			.setActionEnabled( ACTION_CUT, !readOnly && notVoid && ((actions == null) || actions.contains( ACTION_CUT )) );
		getMenuManager().setActionEnabled( ACTION_COPY, notVoid && ((actions == null) || actions.contains( ACTION_COPY )) );
		getMenuManager().setActionEnabled( ACTION_PASTE, !readOnly && ((actions == null) || actions.contains( ACTION_PASTE )) );
		getMenuManager().setActionEnabled( ACTION_DELETE, !readOnly && ((actions == null) || actions.contains( ACTION_DELETE )) );

		//TODO set state of paste button
		boolean isTask = (_currentImpl != null) && _currentImpl instanceof Task;
		boolean isResource = (_currentImpl != null) && _currentImpl instanceof Resource;
		boolean isHasStartAndEnd = (_currentImpl != null) && _currentImpl instanceof HasStartAndEnd;
		boolean writable = ((_currentImpl != null) && !ClassUtils.isObjectReadOnly( _currentImpl ));
		getMenuManager()
			.setActionEnabled( ACTION_INDENT,
			!readOnly && (isTask || isResource) && ((actions == null) || actions.contains( ACTION_INDENT )) );
		getMenuManager()
			.setActionEnabled( ACTION_OUTDENT,
			!readOnly && (isTask || isResource) && ((actions == null) || actions.contains( ACTION_OUTDENT )) );
		getMenuManager().setActionEnabled( ACTION_LINK, isTask && !pinned );
		getMenuManager().setActionEnabled( ACTION_UNLINK, isTask && !pinned );
		getMenuManager().setActionEnabled( ACTION_ASSIGN_RESOURCES, isTask && writable );
		getMenuManager().setActionEnabled( ACTION_DELEGATE_TASKS, isTask && writable );
		getMenuManager().setActionEnabled( ACTION_UPDATE_TASKS, !readOnly && isTask );

		boolean insertProject = getCurrentFrame().isCurrentRowInMainProject();

		//			myTaskType && (!notVoid || currentImpl == null || ((Task)currentImpl).getOwningProject() == null || ((Task)currentImpl).getOwningProject() == project);
		getMenuManager //			myTaskType && (!notVoid || currentImpl == null || ((Task)currentImpl).getOwningProject() == null || ((Task)currentImpl).getOwningProject() == project);
		//			myTaskType && (!notVoid || currentImpl == null || ((Task)currentImpl).getOwningProject() == null || ((Task)currentImpl).getOwningProject() == project);
		( //			myTaskType && (!notVoid || currentImpl == null || ((Task)currentImpl).getOwningProject() == null || ((Task)currentImpl).getOwningProject() == project);
		).setActionEnabled( ACTION_INSERT_PROJECT, !readOnly && insertProject );

		BaseView view = null;
		DocumentFrame frame = getCurrentFrame();

		if (frame != null)
		{
			view = (BaseView)frame.getMainView().getTopComponent();
		}

		getMenuManager().setActionEnabled( ACTION_SCROLL_TO_TASK, isHasStartAndEnd && view.canScrollToTask() );

		if (myCurrentFrame != null)
		{
			myCurrentFrame.refreshUndoButtons();

			//refreshSaveStatus(false);
		}

		boolean printable = (myCurrentFrame != null) && myCurrentFrame.isPrintable();
		getMenuManager().setActionEnabled( ACTION_PRINT, printable );
		getMenuManager().setActionEnabled( ACTION_PRINT_PREVIEW, printable );

		setZoomButtons();

		Field f = FieldDictionary.getInstance().getActionField( ACTION_DOCUMENTS );
		getMenuManager().setActionVisible( ACTION_DOCUMENTS, (myCurrentFrame != null) && (f != null) );
		getMenuManager()
			.setActionEnabled( ACTION_DOCUMENTS,
			(myCurrentFrame != null) && isEnabledFieldAction( ACTION_DOCUMENTS, myCurrentFrame.getProject() ) );
	}

	void setColorTheme( 
		final String _viewName )
	{
		getLafManager().setColorTheme( _viewName );
	}

	private boolean isEnabledFieldAction( 
		final String _action,
		final Object _obj )
	{
		Field f = FieldDictionary.getInstance().getActionField( ACTION_DOCUMENTS );

		return ((_obj != null) && (f != null) && (f.getValue( _obj, null ) != null));
	}

	public class AboutAction
		extends MenuActionsMap.GlobalMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();
			showAboutDialog();
		}

		private static final long serialVersionUID = 1L;
	}

	public class AssignResourcesAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();
			showAssignmentDialog( getCurrentFrame() );
		}

		private static final long serialVersionUID = 1L;
	}

	public class BarAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();

			if (isDocumentActive())
			{
				getCurrentFrame().doBarDialog();
			}
		}

		private static final long serialVersionUID = 1L;
	}

	public class CalendarOptionsAction
		extends MenuActionsMap.GlobalMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();
			doCalendarOptionsDialog();
		}

		private static final long serialVersionUID = 1L;
	}

	public class CancelChangesAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			ActionEvent _event )
		{
			setMeAsLastGraphicManager();

			if (isDocumentActive())
			{
				Project project = getCurrentFrame().getProject();

				//				    * plan has been modified (diff from published server copy)
				ProjectFactory.getInstance().reload( project, false );
				setEnabledDocumentMenuActions( false );
			}
		}

		@Override
		public boolean allowed( 
			final boolean _enable )
		{
			if (_enable == false)
			{
				return true;
			}

			NamedFrame frame = getCurrentFrame();

			if (frame == null)
			{
				return false;
			}

			Project project = getCurrentFrame().getProject();

			if (project == null)
			{
				return false;
			}

			if (project.isNoLongerLocked())
			{
				return false;
			}

			boolean canRevert = !Environment.isNoPodServer() || project.isLocked();

			//			System.out.println("Cancel changes allowed canRevert " + canRevert + " needs saving " + project.needsSaving());
			return (canRevert && !project.isLocal() && project.needsSaving());
		}

		private static final long serialVersionUID = 1L;
	}

	//	public class EnterpriseResourcesAction extends MenuActionsMap.DocumentMenuAction {
	//		public void actionPerformed(ActionEvent arg0) {
	//			if (isDocumentActive())
	//				getCurrentFrame().doEnterpriseResourcesDialog();
	//		}
	//	}
	public class ChangeWorkingTimeAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();

			JDialog dlg = AbstractDialog.containedInDialog( _event.getSource() );
			boolean restrict = dlg != null;

			if (isDocumentActive())
			{
				getCurrentFrame().doChangeWorkingTimeDialog( restrict );
			}
		}

		private static final long serialVersionUID = 1L;
	}

	public class ClearBaselineAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();

			if (isDocumentActive())
			{
				getCurrentFrame().doBaselineDialog( false );
			}
		}

		@Override
		public boolean allowed( 
			final boolean _enable )
		{
			if (_enable == false)
			{
				return true;
			}

			return isDocumentWritable() && !getCurrentProject().isCheckedOutByOther() &&
			getCurrentProject().getProjectPermission().isWrite() && getCurrentProject().getProjectPermission().isSaveBaseline();
		}

		private static final long serialVersionUID = 1L;
	}

	public class CloseProjectAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();

			if (isDocumentActive())
			{
				closeProject( getCurrentFrame().getProject() );
			}
		}

		private static final long serialVersionUID = 1L;
	}

	public class CollapseAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();

			if (isDocumentActive())
			{
				getCurrentFrame().doCollapse();
			}
		}

		@Override
		public boolean allowed( 
			final boolean _enable )
		{
			if (_enable == false)
			{
				return true;
			}

			return isDocumentWritable();
		}

		private static final long serialVersionUID = 1L;
	}

	public class CopyAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();

			if (isDocumentActive())
			{
				addHistory( "doCopy" );
				getCurrentFrame().doCopy();
			}
		}

		private static final long serialVersionUID = 1L;
	}

	protected class CreateSubprojectEdit
		extends AbstractUndoableEdit
	{
		public CreateSubprojectEdit( 
			final Project _project,
			final Node _subprojectNode,
			final long _subprojectUniqueId )
		{
			super();
			this.project = _project;
			this.subprojectNode = _subprojectNode;
			this.subprojectUniqueId = _subprojectUniqueId;
		}

		@Override
		public void redo()
			throws CannotRedoException
		{
			super.redo();
			insertSubproject( project, subprojectUniqueId, false );
		}

		@Override
		public void undo()
			throws CannotUndoException
		{
			super.undo();
			project.getTaskOutline().remove( subprojectNode, NodeModel.EVENT );

			//			UndoController undoContoller=project.getUndoController();
			//			if (undoContoller.getEditSupport()!=null){
			//				undoContoller.clear();
			//			}
		}

		protected final Node subprojectNode;
		protected Project project;
		protected long subprojectUniqueId;
	}

	public class CutAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();

			if (isDocumentActive() == true)
			{
				addHistory( "doCut" );
				getCurrentFrame().doCut();
			}
		}

		private static final long serialVersionUID = 1L;
	}

	public class DefineCodeAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();

			if (isDocumentActive())
			{
				getCurrentFrame().doDefineCodeDialog();
			}
		}

		private static final long serialVersionUID = 1L;
	}

	public class DelegateTasksAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();

			if (isDocumentActive())
			{
				getCurrentFrame().doDelegateTasksDialog();
			}
		}

		private static final long serialVersionUID = 1L;
	}

	public class DeleteAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();

			if (isDocumentActive())
			{
				getCurrentFrame().doDelete();
			}
		}

		@Override
		public boolean allowed( 
			final boolean _enable )
		{
			if (_enable == false)
			{
				return true;
			}

			return isDocumentWritable();
		}

		private static final long serialVersionUID = 1L;
	}

	public class DocumentsAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();

			if (!isDocumentActive())
			{
				return;
			}

			invokeFieldAction( ACTION_DOCUMENTS, getCurrentFrame().getProject() );
		}

		private static final long serialVersionUID = 1L;
	}

	public class ExitAction
		extends MenuActionsMap.GlobalMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();
			closeApplication();
		}

		private static final long serialVersionUID = 1L;
	}

	public class ExpandAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();

			if (isDocumentActive())
			{
				getCurrentFrame().doExpand();
			}
		}

		@Override
		public boolean allowed( 
			final boolean _enable )
		{
			if (_enable == false)
			{
				return true;
			}

			return isDocumentWritable();
		}

		private static final long serialVersionUID = 1L;
	}

	public class ExportMSProjectAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();
			saveLocalProject( true );
		}

		private static final long serialVersionUID = 1L;
	}

	// Document actions
	public class FindAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();

			if (isDocumentActive())
			{
				doFind( getCurrentFrame().getTopSpreadSheet(), null );
			}
		}

		@Override
		public boolean allowed( 
			final boolean _enable )
		{
			if (_enable == false)
			{
				return true;
			}

			if (myCurrentFrame == null)
			{
				return false;
			}

			return (myCurrentFrame.getActiveSpreadSheet() != null);
		}

		private static final long serialVersionUID = 1L;
	}

	public class FullScreenAction
		extends MenuActionsMap.GlobalMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();
			encodeWorkspace(); // so new window takes this one's preferences
							   // see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5029025

			try
			{
				Class cl = Class.forName( "netscape.javascript.JSObject" );
				Object win = cl.getMethod( "getWindow", new Class[]
						{
							Applet.class
						} ).invoke( null, new Object[]
						{
							myContainer
						} );

				//JSObject win = JSObject.getWindow((Applet) myContainer);
				cl.getMethod( "call", new Class[]
					{
						String.class,
						(new Object[] {  }).getClass()
					} ).invoke( win, new Object[]
					{
						"fullScreen",
						null
					} );

				//win.call("fullScreen", null);		  	     // Call f() in HTML page //$NON-NLS-1$
			}
			catch (IllegalArgumentException | SecurityException | IllegalAccessException | InvocationTargetException 
				| NoSuchMethodException | ClassNotFoundException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private static final long serialVersionUID = 1L;
	}

	public class GoToAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();

			if (isDocumentActive())
			{
				doFind( getCurrentFrame().getTopSpreadSheet(), Configuration.getFieldFromId( "Field.id" ) );
			}
		}

		@Override
		public boolean allowed( 
			final boolean _enable )
		{
			if (_enable == false)
			{
				return true;
			}

			if (myCurrentFrame == null)
			{
				return false;
			}

			return (myCurrentFrame.getActiveSpreadSheet() != null);
		}

		private static final long serialVersionUID = 1L;
	}

	public class GroupAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();

			if (isDocumentActive())
			{
				getCurrentFrame().doGroupDialog();
			}
		}

		private static final long serialVersionUID = 1L;
	}

	public class HelpAction
		extends MenuActionsMap.GlobalMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();
			showHelpDialog();
		}

		private static final long serialVersionUID = 1L;
	}

	public class ImportMSProjectAction
		extends MenuActionsMap.GlobalMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();
			openLocalProject();
		}

		private static final long serialVersionUID = 1L;
	}

	public class IndentAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();

			if (isDocumentActive())
			{
				getCurrentFrame().doIndent();
			}
		}

		@Override
		public boolean allowed( 
			final boolean _enable )
		{
			if (_enable == false)
			{
				return true;
			}

			return isDocumentWritable();
		}

		private static final long serialVersionUID = 1L;
	}

	public class InformationAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();
			doInformationDialog( false );
		}

		private static final long serialVersionUID = 1L;
	}

	public class InsertProjectAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent arg0 )
		{
			setMeAsLastGraphicManager();
			doInsertProjectDialog();
		}

		@Override
		public boolean allowed( 
			final boolean _enable )
		{
			if (_enable == false)
			{
				return true;
			}

			return isDocumentWritable();
		}

		private static final long serialVersionUID = 1L;
	}

	public final class InsertTaskAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public final void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();

			if (isDocumentActive() == true)
			{
				getCurrentFrame().addNodeForImpl( null );
			}
		}

		@Override
		public final boolean allowed( 
			final boolean _enable )
		{
			if (_enable == false)
			{
				return true;
			}

			return isDocumentWritable();
		}

		private static final long serialVersionUID = 1L;
	}

	public class LevelResourcesAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();

			if (isDocumentActive())
			{
				getCurrentFrame().doLevelResourcesDialog();
			}
		}

		private static final long serialVersionUID = 1L;
	}

	public class LinkAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();

			if (isDocumentActive())
			{
				getCurrentFrame().doLinkTasks();
			}
		}

		@Override
		public boolean allowed( 
			final boolean _enable )
		{
			if (!super.allowed( _enable ))
			{
				return false;
			}

			return (getProject() != null) && !getProject().isDatesPinned();
		}

		private static final long serialVersionUID = 1L;
	}

	public class LookAndFeelAction
		extends MenuActionsMap.GlobalMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();
		}

		private static final long serialVersionUID = 1L;
	}

	public class NewProjectAction
		extends MenuActionsMap.GlobalMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();
			doNewProjectDialog();
		}

		@Override
		public boolean allowed( 
			final boolean _enable )
		{
			DocumentFrame dframe = getCurrentFrame();

			return (dframe == null) || !dframe.isEditingResourcePool();
		}

		private static final long serialVersionUID = 1L;
	}

	public class NotesAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();
			doInformationDialog( true );
		}

		private static final long serialVersionUID = 1L;
	}

	public class OpenProjAction
		extends MenuActionsMap.GlobalMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();
			BrowserControl.displayURL( "http://www.projity.com/" );
		}

		private static final long serialVersionUID = 1L;
	}

	public class OpenProjectAction
		extends MenuActionsMap.GlobalMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();

			if (Environment.getStandAlone() == true)
			{
				openLocalProject();
			}
			else
			{
				doOpenProjectDialog();
			}
		}

		@Override
		public boolean allowed( 
			final boolean _enable )
		{
			DocumentFrame dframe = getCurrentFrame();

			return (dframe == null) || !dframe.isEditingResourcePool();
		}

		@Override
		protected boolean needsDocument()
		{
			return !allowed( true ); // force it to be called iff the resource pool is open
		}

		private static final long serialVersionUID = 1L;
	}

	public class OutdentAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();

			if (isDocumentActive())
			{
				getCurrentFrame().doOutdent();
			}
		}

		@Override
		public boolean allowed( 
			final boolean _enable )
		{
			if (_enable == false)
			{
				return true;
			}

			return isDocumentWritable();
		}

		private static final long serialVersionUID = 1L;
	}

	public class PDFAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			if (Environment.isOpenProj())
			{
				PODOnlyFeature.doDialog( getFrame() );

				return;
			}
			else if (Environment.isNoPodServer() && !GraphicManager.is1_6Version())
			{
				FeatureNotAvailableDialog.doDialog( getFrame() );

				return;
			}

			setMeAsLastGraphicManager();

			if (isDocumentActive())
			{
				Component c = (Component)_event.getSource();
				Cursor cur = c.getCursor();
				c.setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );
				savePDF();
				c.setCursor( cur );
			}
		}

		private static final long serialVersionUID = 1L;
	}

	public class PaletteAction
		extends MenuActionsMap.GlobalMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();

			getLafManager().changePalette();
		}

		@Override
		public boolean allowed( 
			final boolean _enable )
		{
			LookAndFeel lookAndFeel = UIManager.getLookAndFeel();

			return getLafManager().isChangePaletteAllowed( lookAndFeel );
		}

		private static final long serialVersionUID = 1L;
	}

	public class PasteAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();

			if (isDocumentActive())
			{
				addHistory( "doPaste" );
				getCurrentFrame().doPaste();
			}
		}

		@Override
		public boolean allowed( 
			final boolean _enable )
		{
			if (_enable == false)
			{
				return true;
			}

			return isDocumentWritable();
		}

		private static final long serialVersionUID = 1L;
	}

	private class PreferencesAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();
			doPreferencesDialog();
		}

		private static final long serialVersionUID = 1L;
	}
	
	public class PrintAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();

			if (isDocumentActive())
			{
				print();
			}
		}

		private static final long serialVersionUID = 1L;
	}

	public class PrintPreviewAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();

			if (isDocumentActive())
			{
				Component c = (Component)_event.getSource();
				Cursor cur = c.getCursor();
				c.setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );
				printPreview();
				c.setCursor( cur );
			}
		}

		private static final long serialVersionUID = 1L;
	}

	public class ProjectInformationAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();
			doProjectInformationDialog();
		}

		private static final long serialVersionUID = 1L;
	}

	public class ProjectsDialogAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();
			ProjectsDialog.show( GraphicManager.this );
		}

		private static final long serialVersionUID = 1L;
	}

	public class PublishAction
		extends SaveProjectAction
	{
		//		public void actionPerformed(ActionEvent arg0) {
		//			super.actionPerformed(arg0);
		//			Project project=getCurrentFrame().getProject();
		//			project.setLocked(false);
		////			Publish(), followed by GetProjectMetadata()
		//
		//		}
		@Override
		public boolean allowed( 
			boolean _enable )
		{
			if (getCurrentFrame() == null)
			{
				return false;
			}

			Project project = getCurrentFrame().getProject();

			return (project != null) && project.isLocked() && !project.isNoLongerLocked();
		}

		@Override
		protected boolean isPublish()
		{
			return true;
		}

		private static final long serialVersionUID = 1L;
	}

	public class PullActualsAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();

			if (isDocumentActive())
			{
				finishAnyOperations();

				Project project = getProject();
				Session session = SessionFactory.getInstance().getSession( false );
				boolean pulled = session.pullActuals( project );

				if (!pulled)
				{
					Toolkit.getDefaultToolkit().beep();
				}

				//		    	if (session.needsReload(project)) {
				//		    		ProjectFactory.getInstance().reload(project, false);
				//		    	} else {
				//		    		Toolkit.getDefaultToolkit().beep();
				//		    	}
			}
		}

		@Override
		public boolean allowed( 
			final boolean _enable )
		{
			if (getCurrentFrame() == null)
			{
				return false;
			}

			Project project = getProject();

			return (project != null) && !project.isMspAssociated() && !project.isCheckedOutByOther() &&
			project.isTimesheetAssociated();
		}

		private static final long serialVersionUID = 1L;
	}

	public class RecalculateAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();

			if (isDocumentActive())
			{
				getCurrentFrame().getProject().recalculate();
			}
		}

		private static final long serialVersionUID = 1L;
	}

	public class RecurringTaskAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();

			if (isDocumentActive())
			{
				getCurrentFrame().doRecurringTaskDialog();
			}
		}

		private static final long serialVersionUID = 1L;
	}

	public class RedoAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();

			if (isDocumentActive())
			{
				doUndoRedo( false );
			}
		}

		private static final long serialVersionUID = 1L;
	}

	public class RefreshAction
		extends MenuActionsMap.GlobalMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();
			getStartupFactory().restart( GraphicManager.this );
		}

		private static final long serialVersionUID = 1L;
	}

	public class ReinitializeAction
		extends MenuActionsMap.GlobalMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();
			getStartupFactory().reinitialize();
		}

		private static final long serialVersionUID = 1L;
	}

	public class ReloadAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();

			if (isDocumentActive())
			{
				Project project = getProject();

				//Session session=SessionFactory.getInstance().getSession(false);
				//if (session.needsReload(project)) {
				boolean canSave = !Environment.isNoPodServer() || project.isLocked();
				canSave = canSave && !project.isNoLongerLocked();

				boolean isDirty = Environment.isOpenProj() || (canSave && !project.isLocal() && project.needsSaving());

				if (isDirty)
				{
					int i = Alert.confirmYesNo( "Do you want to save your changes?" );

					if (JOptionPane.YES_OPTION == i)
					{
						Session session = SessionFactory.getInstance().getSession( false );

						if (!session.isSavable( project ))
						{
							if (JOptionPane.YES_OPTION == Alert.confirmYesNo( Messages.getString( "Text.projectGotUnlocked" ),
										JOptionPane.YES_OPTION ))
							{
								ProjectFactory.getInstance().reload( project, false );
							}
							else
							{
								session.refreshMetadata( project );
								project.setNoLongerLocked( true );
							}

							setEnabledDocumentMenuActions( true );

							return;
						}

						SaveOptions opt = new SaveOptions();
						opt.setSync( true );
						opt.setPublish( false );
						opt.setPostSaving( new Closure()
							{
							@Override
								public void execute( 
									Object arg0 )
								{
									refreshSaveStatus( true );
								}
							} );
						opt.setPreSaving( getSavingClosure() );
						addHistory( "saveProject", new Object[]
							{
								project.getName(),
								project.getUniqueId()
							} );
						myProjectFactory.saveProject( project, opt );
						setEnabledDocumentMenuActions( true );
					}
				}

				ProjectFactory.getInstance().reload( project, false );

				//refreshSaveStatus(true);
				//setEnabledDocumentMenuActions(true);
				//} else {
				//	Toolkit.getDefaultToolkit().beep();
				//}
			}

			//setEnabledDocumentMenuActions(false);
		}

		@Override
		public boolean allowed( 
			final boolean _enable )
		{
			if (getCurrentFrame() == null)
			{
				return false;
			}

			Project project = getProject();

			return project != null /*&& !project.isLocked()*/;
		}

		private static final long serialVersionUID = 1L;
	}

	public class RevertAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();

			//			System.out.println("Revert");
			Project project = getCurrentFrame().getProject();

			if (project == null)
			{
				return;
			}

			ProjectFactory.getInstance().revert( project, false );
			setEnabledDocumentMenuActions( false );
		}

		/*
		 *                   o Revert is only available (ungreyed) if:
		                        + user has the lock AND plan has been modified (different from published server copy)
		                        + Plan is locked AND user has appropriate rights (e.g. Admin)
		 */
		@Override
		public boolean allowed( 
			final boolean _enable )
		{
			if (_enable == false)
			{
				return true;
			}

			if (getCurrentFrame() == null)
			{
				return false;
			}

			Project project = getCurrentFrame().getProject();

			if (project == null)
			{
				return false;
			}

			if (project.isLocked() && isDocumentWritable()) //user has the lock AND plan has been modified (different from published server copy)
			{
				return true;
			}

			if (project.getCheckedOutUser() != null)
			{ // if somebody has it

				return project.getProjectPermission().isBreakLock();
			}

			return false;
		}

		private static final long serialVersionUID = 1L;
	}

	public class SaveBaselineAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();

			if (isDocumentActive())
			{
				getCurrentFrame().doBaselineDialog( true );
			}
		}

		@Override
		public boolean allowed( 
			final boolean _enable )
		{
			if (_enable == false)
			{
				return true;
			}

			return isDocumentWritable() && !getCurrentProject().isCheckedOutByOther() &&
			getCurrentProject().getProjectPermission().isWrite() && getCurrentProject().getProjectPermission().isSaveBaseline();
		}

		private static final long serialVersionUID = 1L;
	}

	public class SaveProjectAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();

			if (Environment.getStandAlone())
			{
				saveLocalProject( false );
			}
			else
			{
				if (isDocumentActive())
				{
					final DocumentFrame frame = getCurrentFrame();
					final Project project = frame.getProject();
					Session session = SessionFactory.getInstance().getSession( false );

					if (!session.isSavable( project ))
					{
						if (JOptionPane.YES_OPTION == Alert.confirmYesNo( Messages.getString( "Text.projectGotUnlocked" ),
									JOptionPane.YES_OPTION ))
						{
							ProjectFactory.getInstance().reload( project, false );
						}
						else
						{
							session.refreshMetadata( project );
							project.setNoLongerLocked( true );
						}

						setEnabledDocumentMenuActions( true );

						return;
					}

					SaveOptions opt = new SaveOptions();
					opt.setPublish( isPublish() );
					opt.setPostSaving( new Closure()
						{
						@Override
							public void execute( 
								Object arg0 )
							{
								refreshSaveStatus( true );
							}
						} );
					opt.setPreSaving( getSavingClosure() );
					addHistory( "saveProject", new Object[]
						{
							project.getName(),
							project.getUniqueId()
						} );
					myProjectFactory.saveProject( project, opt );
					setEnabledDocumentMenuActions( true );
				}
			}
		}

		@Override
		public boolean allowed( 
			final boolean _enable )
		{
			if (_enable == false)
			{
				return true;
			}

			NamedFrame frame = getCurrentFrame();

			if (frame == null)
			{
				return false;
			}

			Project project = getCurrentFrame().getProject();

			if (project == null)
			{
				return false;
			}

			boolean canSave = !Environment.isNoPodServer() || project.isLocked();
			canSave = canSave && !project.isNoLongerLocked();

			boolean res = Environment.isOpenProj() || (canSave && !project.isLocal() && project.needsSaving());

			return res;
		}

		protected boolean isPublish()
		{
			return false;
		}

		private static final long serialVersionUID = 1L;
	}

	public class SaveProjectAsAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();
			finishAnyOperations();

			if (Environment.getStandAlone())
			{
				saveLocalProject( true );
			}
			else
			{
				if (isDocumentActive())
				{
					final DocumentFrame frame = getCurrentFrame();
					final Project project = frame.getProject();
					SaveOptions opt = new SaveOptions();
					opt.setPostSaving( new Closure()
					{
						@Override
						public void execute( 
							Object _event )
						{
							frame.setId( project.getUniqueId() + "" ); //$NON-NLS-1$
							refreshSaveStatus( true );
						}
					} );
					opt.setSaveAs( true );
					opt.setPreSaving( getSavingClosure() );
					myProjectFactory.saveProject( project, opt );
				}
			}
		}

		@Override
		public boolean allowed( 
			final boolean _enable )
		{
			if (_enable == false)
			{
				return true;
			}

			NamedFrame frame = getCurrentFrame();

			if (frame == null)
			{
				return false;
			}

			Project project = getCurrentFrame().getProject();

			if (project == null)
			{
				return false;
			}

			if (project.isMaster() && !Environment.getStandAlone() && !Environment.isOpenProj())
			{
				return false;
			}

			return (project.isSavable());

			//			return true;//!project.isLocal()&&!project.isMaster();
		}

		private static final long serialVersionUID = 1L;
	}

	public class ScrollToTaskAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();

			if (isDocumentActive())
			{
				getCurrentFrame().doScrollToTask();
			}
		}

		private static final long serialVersionUID = 1L;
	}

	public class SelectDocumentAction
		extends MenuActionsMap.GlobalMenuAction
	{
		public SelectDocumentAction( 
			final DocumentFrame _frame )
		{
			myFrame = _frame;
		}

		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();
			GraphicManager.this.setCurrentFrame( myFrame );
		}

		@Override
		public Object getValue( 
			final String key )
		{
			if (key == Action.NAME)
			{
				return myFrame.getProject().getName();
			}

			return super.getValue( key );
		}

		private static final long serialVersionUID = 1L;
		DocumentFrame myFrame;
	}

	public class SortAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();

			if (isDocumentActive())
			{
				getCurrentFrame().doSortDialog();
			}
		}

		private static final long serialVersionUID = 1L;
	}

	public class SubstituteResourceAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();

			if (isDocumentActive())
			{
				lockProject();
				getCurrentFrame().doSubstituteResource();
			}
		}

		private static final long serialVersionUID = 1L;
	}

	public class TeamFilterAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			final GlobalPreferences myPreferences = GlobalPreferences.getInstance();

			//Field field = Configuration.getFieldFromId("Field.showProjectResourcesOnly");
			boolean teamOnly = !myPreferences.isShowProjectResourcesOnly();

			//field.setValue(myPreferences,this,teamOnly);
			myPreferences.setShowProjectResourcesOnly( teamOnly );

			ArrayList buttons = getMenuManager().getToolBarFactory().getButtonsFromId( "TeamFilter" ); //$NON-NLS-1$

			if ((buttons != null) && (buttons.size() == 1))
			{
				JButton b = (JButton)buttons.get( 0 );

				if (Environment.isNewLook())
				{
					b.setIcon( IconManager.getIcon( teamOnly
							? "menu24.showTeamResources"
							: "menu24.showAllResources" ) ); //$NON-NLS-1$ //$NON-NLS-2$
				}
				else
				{
					b.setIcon( IconManager.getIcon( teamOnly
							? "menu.showTeamResourcesSmall"
							: "menu.showAllResourcesSmall" ) ); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}

			myMenuManager.setActionSelected( ACTION_TEAM_FILTER, teamOnly );
		}

		private static final long serialVersionUID = 1L;
	}

	public class TipOfTheDayAction
		extends MenuActionsMap.GlobalMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();
			TipOfTheDay.showDialog( getFrame(), true );
		}

		private static final long serialVersionUID = 1L;
	}

	public class TransformAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();

			if (!isDocumentActive())
			{
				return;
			}

			CommonSpreadSheet spreadSheet = getCurrentFrame().getTopSpreadSheet();

			if (spreadSheet != null)
			{
				if (spreadSheet.isEditing())
				{
					spreadSheet.getCellEditor().stopCellEditing();
				}

				//.cancelCellEditing();
				spreadSheet.clearSelection();
			}

			TransformComboBox combo = (TransformComboBox)_event.getSource();
			combo.transformBasedOnValue();
		}

		private static final long serialVersionUID = 1L;
	}

	public class UndoAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();

			if (isDocumentActive())
			{
				doUndoRedo( true );
			}
		}

		private static final long serialVersionUID = 1L;
	}

	public class UndoStackAction
		extends MenuActionsMap.GlobalMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();

			Project project = getCurrentProject();

			if ((project != null) && (project.getNodeModelDataFactory().getUndoController() != null))
			{
				project.getNodeModelDataFactory().getUndoController().showEditsDialog();
			}
		}

		private static final long serialVersionUID = 1L;
	}

	public class UnlinkAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();

			if (isDocumentActive())
			{
				getCurrentFrame().doUnlinkTasks();
			}
		}

		@Override
		public boolean allowed( 
			final boolean _enable )
		{
			if (!super.allowed( _enable ))
			{
				return false;
			}

			return (getProject() != null) && !getProject().isDatesPinned();
		}

		private static final long serialVersionUID = 1L;
	}

	public class UpdateProjectAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();

			if (isDocumentActive())
			{
				getCurrentFrame().doUpdateProjectDialog();
			}
		}

		@Override
		public boolean allowed( 
			final boolean _enable )
		{
			if (_enable == false)
			{
				return true;
			}

			return isDocumentWritable();
		}

		private static final long serialVersionUID = 1L;
	}

	public class UpdateTasksAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			ActionEvent _event )
		{
			setMeAsLastGraphicManager();

			if (isDocumentActive())
			{
				getCurrentFrame().doUpdateTasksDialog();
			}
		}

		@Override
		public boolean allowed( 
			final boolean _enable )
		{
			if (_enable == false)
			{
				return true;
			}

			return isDocumentWritable();
		}

		private static final long serialVersionUID = 1L;
	}

	public final class ViewAction
		extends MenuActionsMap.DocumentMenuAction
	{
		public ViewAction( 
			final String _viewName )
		{
			myViewName = _viewName;
		}

		@Override
		public final void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();

			if (getCurrentFrame() == null)
			{
				return;
			}

			if ((myViewName.equals( ACTION_HISTOGRAM ) == true) 
			 || (myViewName.equals( ACTION_CHARTS ) == true))
			{
				if ((getProject() == null) 
				 || (getProject().isDatesPinned() == true))
				{
					return;
				}
			}

			setColorTheme( myViewName );
			getCurrentFrame().activateView( myViewName );

			// disable buttons because no selection when first activated
			setButtonState( null, myCurrentFrame.getProject() ); 
		}

		@Override
		public final boolean allowed( 
			final boolean _enable )
		{
			if (super.allowed( _enable ) == false)
			{
				return false;
			}

			if ((myViewName.equals( ACTION_HISTOGRAM ) == true) 
			 || (myViewName.equals( ACTION_CHARTS ) == true))
			{
				return (getProject() != null) && (getProject().isDatesPinned() == false);
			}

			return true;
		}

		public final String getViewName()
		{
			return myViewName;
		}

		private static final long serialVersionUID = 1L;
		private String myViewName;
	}

	private class ZoomInAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();

			if (isDocumentActive())
			{
				getCurrentFrame().doZoomIn();
			}

			setZoomButtons();
		}

		private static final long serialVersionUID = 1L;
	}
	
	private class ZoomOutAction
		extends MenuActionsMap.DocumentMenuAction
	{
		@Override
		public void actionPerformed( 
			final ActionEvent _event )
		{
			setMeAsLastGraphicManager();

			if (isDocumentActive())
			{
				getCurrentFrame().doZoomOut();
			}

			setZoomButtons();
		}

		private static final long serialVersionUID = 1L;
	}

	private static final boolean BINARY_WORKSPACE = true;
	private static GraphicManager myLastGraphicManager = null; // used when displaying a popup but the frame isn't known
	private static String myServer = null;
	private static Object myLastWorkspace = null; // static required - used for copying current workspace to new instance
	private static LinkedList myGraphicManagers = new LinkedList();
	private static LafManager mylafManager;
	public static boolean myBadLAF = false;
	protected static int myProject_suffix_count = 1;

	private MenuActionsMap myActionsMap = null;
	private ProjectFactory myProjectFactory = null;
	private AboutDialog myAboutDialog = null;
	private ArrayList<CommandInfo> myHistory = new ArrayList<CommandInfo>();
	private AssignmentDialog myAssignResourcesDialog = null;
	private BaselineDialog myBaselineDialog = null;
	protected Container myContainer;
	protected GlobalPreferences myPreferences = null;
	protected JobQueue myJobQueue = null;
	TabbedNavigation myTopTabs = null;
	boolean myDoingOpenDialog = false;
	boolean myInitialized = false;
	private DocumentFrame myCurrentFrame = null;
	private FilterToolBarManager myFilterToolBarManager;
//	private FindDialog myFindDialog = null;
	protected Frame myFrame;
	private FrameManager myFrameManager;
	private HashMap<String,String> myColorThemes = null;
	private HashMap<Project,NamedFrame> myFrameMap = new HashMap<Project,NamedFrame>();
	private HelpDialog myHelpDialog = null;
	private JMenu myProjectListMenu = null;
	private List myFrameList = new ArrayList();
	private MenuManager myMenuManager;
	private Mutex myInitializing = new Mutex();

//    private JFileChooser fileChooser = null;
//	private NamedFrame myViewBarFrame;

	/**
	 * React to selection changed events and forward them on to any bottom window
	 */
	protected Node myLastNode = null;
	private ProjectInformationDialog myProjectInformationDialog = null;

	//for AssignmentDialog
	private ResourceInTeamFilter myAssignmentDialogTransformerInitializationClosure;
	private ResourceInformationDialog myResourceInformationDialog = null;
	private ResourceMappingDialog myResourceMappingDialog = null;
	private StartupFactory myStartupFactory = null;
//	private String myFileName = "../projity_exchange/testdata/New Product.mpp";
	protected String myLastFileName;
	private TaskInformationDialog myTaskInformationDialog = null;
	private ViewAction myResourceAction;
	protected boolean myResourceType = false;
	protected boolean myTaskType = false;
}
