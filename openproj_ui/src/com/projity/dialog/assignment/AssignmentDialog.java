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
package com.projity.dialog.assignment;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import com.projity.configuration.Settings;

import com.projity.dialog.AbstractDialog;
import com.projity.dialog.ButtonPanel;

import com.projity.document.ObjectEvent;

import com.projity.grouping.core.NodeList;
import com.projity.grouping.core.transform.filtering.NodeFilter;
import com.projity.grouping.core.transform.filtering.NotAssignmentFilter;

import com.projity.help.HelpUtil;

import com.projity.pm.assignment.Assignment;
import com.projity.pm.assignment.AssignmentService;
import com.projity.pm.graphic.IconManager;
import com.projity.pm.graphic.frames.DocumentFrame;
import com.projity.pm.graphic.frames.DocumentSelectedEvent;
import com.projity.pm.graphic.frames.GraphicManager;
import com.projity.pm.graphic.spreadsheet.SpreadSheet;
import com.projity.pm.graphic.spreadsheet.selection.event.SelectionNodeEvent;
import com.projity.pm.graphic.spreadsheet.selection.event.SelectionNodeListener;
import com.projity.pm.graphic.views.GanttView;
import com.projity.pm.resource.Resource;
import com.projity.pm.task.Project;

import com.projity.preference.GlobalPreferences;

import com.projity.strings.Messages;

import com.projity.util.Alert;
import com.projity.util.DataUtils;
import com.projity.util.Environment;

import java.awt.event.ActionEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openproj.domain.task.Task;

public final class AssignmentDialog
	extends AbstractDialog
	implements DocumentSelectedEvent.Listener,
		SelectionNodeListener,
		ResourceAssigner,
		ObjectEvent.Listener,
		CommonAssignmentDialog
{
	public AssignmentDialog( 
		final DocumentFrame _documentFrame )
	{
		super(_documentFrame.getGraphicManager().getFrame(), Messages.getString( "Text.AssignResources" ), false); //$NON-NLS-1$
		setDocumentFrame( _documentFrame );
		DocumentSelectedEvent.addListener( this );
		addDocHelp( "Assign_Resources" );

		//createContentPanel();
	}

	@Override
	public void assign( 
		final Resource _resource,
		final double _units )
	{
		ArrayList list = new ArrayList();
		list.add( _resource );
		assign( list, _units );
	}

	@Override
	public ButtonPanel createButtonPanel()
	{
		return null;
	}

	public JComponent createButtons()
	{
		FormLayout layout = new FormLayout("default", // cols //$NON-NLS-1$
				"p,3dlu,p, 3dlu, p, 3dlu, p, 3dlu, p"); // rows //$NON-NLS-1$
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.append( assignButton );
		builder.nextLine( 2 );
		builder.append( removeButton );
		builder.nextLine( 2 );
		builder.append( replaceButton );
		builder.nextLine( 2 );
		builder.append( getHelpButton() );
		builder.nextLine( 2 );
		createCloseButton();
		builder.add( ok );

		return builder.getPanel();
	}

	@Override
	public JComponent createContentPanel()
	{
		// Separating the component initialization and configuration
		// from the layout code makes both parts easier to read.
		initControls
		// Separating the component initialization and configuration
		// from the layout code makes both parts easier to read.
		();

		FormLayout layout = new FormLayout("p, 1dlu, default ,3dlu, default", // cols //$NON-NLS-1$
				"p, 3dlu," + (Environment.getStandAlone()
				? ""
				: "p, 3dlu,") + "fill:200dlu:grow"); // rows //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		// Create a builder that assists in adding components to the container.
		// Wrap the panel with a standardized border.
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();

		CellConstraints cc = new CellConstraints();

		// task names span whole dialog
		builder.add( taskNames, cc.xyw( builder.getColumn(), builder.getRow(), builder.getColumnCount() ) );
		builder.nextLine( 2 );

		if (!Environment.getStandAlone())
		{
			if (!Environment.isExternal() && !Environment.isNoPodServer())
			{
				builder.add( getTeamOrAllLabel(), cc.xyw( builder.getColumn(), builder.getRow(), builder.getColumnCount() ) );
			}

			builder.nextLine( 2 );
		}

		//		builder.append(projectName);
		//		builder.nextLine(2);
		builder.append( spreadSheetPane, createButtons() );

		return builder.getPanel();
	}

	public JComponent createEditorsButtons()
	{
		FormLayout layout = new FormLayout("20px", // cols //$NON-NLS-1$
				"20dlu,20px, 3dlu, 20px"); // rows //$NON-NLS-1$
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.nextLine( 1 );
		builder.append( stopEditorButton );
		builder.nextLine( 2 );
		builder.append( cancelEditorButton );
		editorsButtons = builder.getPanel();

		return editorsButtons;
	}

	@Override
	public void documentSelected( 
		final DocumentSelectedEvent _evt )
	{
		setDocumentFrame( _evt.getCurrent() );
		spreadSheetPane.setProject( getDocumentFrame().getProject() );
		setSelectedTasks( emptyList );
	}

	/**
	 * @return Returns the project.
	 */
	public DocumentFrame getDocumentFrame()
	{
		return documentFrame;
	}

	public List getSelectedResources()
	{
		return spreadSheetPane.getSelectedResources( false );
	}

	@Override
	protected boolean hasCloseButton()
	{
		return true;
	}

	/**
	 * Creates, intializes and configures the UI components. Real applications
	 * may further bind the components to underlying models.
	 */
	protected void initControls()
	{
		GraphicManager mf = documentFrame.getGraphicManager();
		spreadSheetPane = new AssignmentEntryPane(this, documentFrame.getProject(), this, false,
				mf.setAssignmentDialogTransformerInitializationClosure());

		//        projectName = new JLabel();
		taskNames = new JLabel();

		Project project = documentFrame.getProject();
		spreadSheetPane.setProject( project ); //init content of spreadsheet
		setSelectedTasks( mf.getCurrentFrame().getTopSpreadSheet().getSelectedNodes() ); //update

		//        projectName.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		//        projectName.setText(project == null ? "" : "Resources from: " + project.getName());
		AbstractAction assignAction = new AbstractAction(Messages.getString( "Text.Assign" ))
			{ //$NON-NLS-1$

				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed( 
					final ActionEvent _e )
				{
					AssignmentDialog.this.assign();
				}
			};

		assignButton = new JButton(assignAction);

		AbstractAction removeAction = new AbstractAction(Messages.getString( "Text.Remove" ))
			{ //$NON-NLS-1$

				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed( 
					final ActionEvent _e )
				{
					AssignmentDialog.this.remove();
				}
			};

		removeButton = new JButton(removeAction);

		AbstractAction replaceAction = new AbstractAction(Messages.getString( "Text.Replace" ) + Settings.ELLIPSIS)
			{ //$NON-NLS-1$

				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed( 
					final ActionEvent _e )
				{
					AssignmentDialog.this.replace();
				}
			};

		replaceButton = new JButton(replaceAction);

		stopEditorButton = new JButton(new AbstractAction(null, IconManager.getIcon( "dialog.ok" ))
				{ //$NON-NLS-1$

					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed( 
						final ActionEvent _e )
					{
						if (spreadSheetPane.getSpreadSheet().getCellEditor() != null)
						{
							spreadSheetPane.getSpreadSheet().getCellEditor().stopCellEditing();
						}
					}
				});
		cancelEditorButton = new JButton(new AbstractAction(null, IconManager.getIcon( "dialog.cancel" ))
		{ 

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed( 
				final ActionEvent _e )
			{
				SpreadSheet sp = spreadSheetPane.getSpreadSheet();

				if (sp.getCellEditor() != null)
				{
					sp.getCellEditor().cancelCellEditing();
				}

				sp.clearSelection();
			}
		});
		setEditorButtonsVisible( false );

		documentFrame.getProject().addObjectListener( this );
		GlobalPreferences.getInstance().addObjectListener( this );
	}

	/* (non-Javadoc)
	 * @see com.projity.field.ObjectEvent.Listener#objectChanged(com.projity.field.ObjectEvent)
	 */
	@Override
	public void objectChanged( 
		final ObjectEvent _objectEvent )
	{
		if (_objectEvent.getObject() instanceof Resource)
		{
			spreadSheetPane.setProject( documentFrame.getProject() );
		}
		else if (_objectEvent.getObject() instanceof Assignment)
		{
			//TODO one more objectEvent catch. Need something to react to undo and spreadsheet modifications
			spreadSheetPane.updateTable(); 
		}
		else if (_objectEvent.getObject() instanceof GlobalPreferences)
		{
			getTeamOrAllLabel();
		}
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.graphic.spreadsheet.selection.SelectionNodeListener#selectionChanged(com.projity.pm.graphic.spreadsheet.selection.SelectionNodeEvent)
	 */
	@Override
	public void selectionChanged( 
		final SelectionNodeEvent _e )
	{
		if (_e.getCategory() != GanttView.spreadsheetCategory) //TODO this is kinda ugly.
		{
			return;
		}

		List selectedNodes = _e.getNodes();
		setSelectedTasks( selectedNodes );
	}

	/**
	 * @param project The project to set.
	 */
	public void setDocumentFrame( 
		final DocumentFrame _documentFrame )
	{
		if ((_documentFrame != null) && (_documentFrame.getProject() != null))
		{
			_documentFrame.getProject().removeObjectListener( this );
		}

		this.documentFrame = _documentFrame;

		Project project = _documentFrame.getProject();
		project.getResourcePool().addObjectListener( this );

		//		if (projectName != null)
		//			projectName.setText(project == null ? "" : "Resources from: " + project.getName());
	}

	@Override
	public void setEditorButtonsVisible( 
		final boolean _visible )
	{
		stopEditorButton.setEnabled( _visible );
		cancelEditorButton.setEnabled( _visible );
	}

	void assign()
	{
		SpreadSheet sp = spreadSheetPane.getSpreadSheet();

		if (sp.isEditing())
		{
			sp.getCellEditor().stopCellEditing();
		}

		assign( getSelectedResources(), 1.0D );
		spreadSheetPane.updateTable();
	}

	void assign( 
		final List _resourceList,
		final double _units )
	{
		if (selectedTasks == null) // if no selection, do nothing
		{
			return;
		}

		Task task;
		Iterator t = selectedTasks.iterator();
		Object current = null;
		List taskList = new ArrayList();

		while (t.hasNext())
		{ // go thru all selected tasks
			current = t.next();

			if (!(current instanceof Task))
			{
				continue;
			}

			task = (Task)current;

			if (!task.isAssignable())
			{
				continue;
			}

			taskList.add( task );
		}

		AssignmentService.getInstance().newAssignments( taskList, _resourceList, _units, 0, this, true );
		spreadSheetPane.updateTable();
	}

	/**
	 * Removes given the current task and resource selection
	 *
	 */
	void remove()
	{
		remove( getSelectedResources() );
	}

	/**
	 * Removes given the current task selection for the given resource lsit
	 * @param resourceList
	 */
	void remove( 
		final List _resourceList )
	{
		Resource resource;
		Iterator r = getSelectedResources().iterator();

		while (r.hasNext())
		{
			remove( (Resource)r.next() );
		}

		spreadSheetPane.updateTable();
	}

	/**
	 * Removes given resource from current task selection
	 * @param resource
	 * @param selectedTasks
	 */
	void remove( 
		final Resource _resource )
	{
		Iterator t = selectedTasks.iterator();
		Task task;
		Assignment assignment;

		while (t.hasNext())
		{
			task = (Task)t.next();
			assignment = task.findAssignment( _resource );

			if (assignment != null)
			{
				AssignmentService.getInstance().checkProtectedAndremove( assignment, this, true );
			}
		}
	}

	void replace()
	{
		List list = spreadSheetPane.getSelectedResources( true );

		if (list.size() > 1)
		{
			Alert.warn( Messages.getString( "Message.onlyReplaceOneResourceAtATime" ), this ); //$NON-NLS-1$

			return;
		}
		else if (list.size() == 0)
		{
			return;
		}

		Resource resource = (Resource)list.get( 0 );
		List replacementList = ReplaceAssignmentDialog.getReplacementFromDialog( documentFrame, resource );

		if ((replacementList == null) || replacementList.isEmpty()) // cancelled or nothing chosen
		{
			return;
		}

		if (!replacementList.contains( resource )) // if resource was replaced, remove it
		{
			remove( resource );
		}
		else{ // resource is in new list too, so don't touch it
			replacementList.remove( resource );
		}

		assign( replacementList, 1.0 ); //TODO assign what the old one was assigned
	}

	// Building *************************************************************

	/**
	 * Builds the panel. Initializes and configures components first, then
	 * creates a FormLayout, configures the layout, creates a builder, sets a
	 * border, and finally adds the components.
	 *
	 * @return the built panel
	 */
	private JLabel getTeamOrAllLabel()
	{
		if (showingTeamAll == null){ // resource is in new list too, so don't touch it
			showingTeamAll = new JLabel(Messages.getString( "AssignmentDialog.ShowingAllResources" ),
					IconManager.getIcon( "menu24.showAllResources" ), JLabel.LEFT); //$NON-NLS-1$ //$NON-NLS-2$
		}

		if (GlobalPreferences.getInstance().isShowProjectResourcesOnly() == true)
		{
			showingTeamAll.setIcon( IconManager.getIcon( "menu24.showTeamResources" ) ); //$NON-NLS-1$
			showingTeamAll.setText( Messages.getString( "AssignmentDialog.ShowingOnlyResourcesOnTheProjectTeam" ) ); //$NON-NLS-1$
		}
		else
		{
			showingTeamAll.setIcon( IconManager.getIcon( "menu24.showAllResources" ) ); //$NON-NLS-1$
			showingTeamAll.setText( Messages.getString( "AssignmentDialog.ShowingAllResources" ) ); //$NON-NLS-1$
		}

		HelpUtil.addDocHelp( showingTeamAll, "Project_Team" );

		return showingTeamAll;
	}

	private void setSelectedTasks( 
		final List _selectedNodes )
	{
		selectedTasks = NodeList.nodeListToImplList( _selectedNodes, filter ); // set member

		String names;

		if ((selectedTasks.size() == 0) || !(selectedTasks.get( 0 ) instanceof Task))
		{
			names = Messages.getString( "AssignmentDialog.none" ); //$NON-NLS-1$
		}
		else
		{
			names = DataUtils.stringListWithMaxAndMessage( selectedTasks, Settings.STRING_LIST_LIMIT,
					Messages.getString( "Message.tooManyTasksSelectedToList" ) ); //$NON-NLS-1$
		}

		taskNames.setText( Messages.getString( "Text.Tasks" ) + ": " + names ); //$NON-NLS-1$ //$NON-NLS-2$
		spreadSheetPane.setSelectedTasks( selectedTasks );

		//		setEnabled(!selectedTasks.isEmpty());
	}

	private static final long serialVersionUID = 1L;
	private static ArrayList emptyList = new ArrayList();
	private static final NodeFilter filter = NotAssignmentFilter.getWritableInstance();
	AssignmentEntryPane spreadSheetPane;
	DocumentFrame documentFrame;

	//	JLabel projectName;
	JButton assignButton;
	JButton cancelEditorButton;
	JButton removeButton;
	JButton replaceButton;
	JButton stopEditorButton;
	JLabel showingTeamAll = null;
	JLabel taskNames;
	JPanel editorsButtons = null;
	List selectedTasks = null;
}
