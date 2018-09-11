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
package com.projity.pm.graphic.frames.workspace;

import com.projity.pm.graphic.IconManager;
import com.projity.pm.graphic.frames.DocumentFrame;
import com.projity.pm.graphic.frames.GraphicManager;
import com.projity.pm.task.Project;
import com.projity.pm.task.ProjectFactory;

import com.projity.session.LoadOptions;

import com.projity.strings.Messages;

import com.projity.util.Environment;

import com.projity.workspace.WorkspaceSetting;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.RootPaneContainer;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;


public class DefaultFrameManager
	implements FrameManager
{
	public DefaultFrameManager( 
		final Container _container,
		final Container _emptyPanel,
		final GraphicManager _graphicManager )
	{
		this.container = _container;
		this.emptyPanel = _emptyPanel;
		this.graphicManager = _graphicManager;
		projectComboPanel = new JPanel();
		projectComboPanel.setVisible( false );

		//	projectComboPanel.add(new JLabel(Messages.getString("DefaultFrameManager.Project"))); //$NON-NLS-1$ //$NON-NLS-2$
		GraphicManager.getInstance().getLafManager().setColorScheme( projectComboPanel );
	}

	public void activateFrame( 
		final NamedFrame _frame )
	{
		getProjectComboBox().setSelectedItem( _frame );

		if (previous != null)
		{
			container.remove( previous );
			previous.setActive( false );
			previous.setVisible( false );
		}
		else
		{
			if (container != null)
			{
				container.remove( emptyPanel );
			}
		}

		previous = _frame;

		if (_frame == null)
		{ // happens when closing all
			setLockStatus( Project.LockStatus.NONE, null, null );

			//			setLockStatus(false,false,null);
			return;
		}

		//		setLockStatus(frame.getProject().isLocked(),frame.getProject().isCheckedOutByOther(),frame.getProject().getCheckedOutUser());
		setLockStatus//		setLockStatus(frame.getProject().isLocked(),frame.getProject().isCheckedOutByOther(),frame.getProject().getCheckedOutUser());
		( _frame.getProject().getLockStatus(), _frame.getProject().getPublishInfoHtml(), _frame.getProject().getCheckedOutUser() );

		container.add( _frame, "Center" );
		_frame.setActive( true );
		_frame.setVisible( true );
	}

	public void addFrame( 
		final NamedFrame _frame )
	{
		getProjectComboBox().addItem( _frame );
		_frame.setManager( this );
		activateFrame( _frame );
		projectComboPanel.setVisible( true );
	}

	public void cleanUp( 
		final boolean _justFrames )
	{
		Iterator i = getAllFrames().iterator();

		while (i.hasNext())
		{
			((DocumentFrame)i.next()).cleanUp();
		}

		projectComboBox.removeAll();

		if (_justFrames)
		{
			return;
		}

		container = null;
		emptyPanel = null;
		previous = null;
		workspace = null;

		NamedFrame previous = null;
		graphicManager = null;
	}

	public WorkspaceSetting createWorkspace( 
		final int _context )
	{
		FrameWorkspace ws = new FrameWorkspace();
		ws.list = new LinkedList();

		for (int i = 0; i < getProjectComboBox().getItemCount(); i++)
		{
			DocumentFrame frame = (DocumentFrame)getProjectComboBox().getItemAt( i );
			ws.list.add( frame.createWorkspace( _context ) );
		}

		ws.selectedIndex = getProjectComboBox().getSelectedIndex();

		return ws;
	}

	protected void finalize()
		throws Throwable
	{
		super.finalize();
	}

	public AbstractList getAllFrames()
	{
		LinkedList list = new LinkedList();

		for (int i = 0; i < getProjectComboBox().getItemCount(); i++)
		{
			NamedFrame frame = (NamedFrame)getProjectComboBox().getItemAt( i );
			list.add( frame );
		}

		return list;
	}

	final Container getEmptyPanel()
	{
		return emptyPanel;
	}

	public NamedFrame getFrame( 
		final String _id )
	{
		for (int i = 0; i < getProjectComboBox().getItemCount(); i++)
		{
			NamedFrame frame = (NamedFrame)getProjectComboBox().getItemAt( i );

			if (frame.getId().equals( _id ))
			{
				return frame;
			}
		}

		return null;
	}

	final GraphicManager getGraphicManager()
	{
		return graphicManager;
	}

	private final JComboBox getProjectComboBox()
	{
		if (projectComboBox == null)
		{
			projectComboBox = new JComboBox(new FrameComboBoxModel());
			projectComboBox.setToolTipText( Messages.getString( "DefaultFrameManager.Project" ) ); //$NON-NLS-1$
			projectComboBox.setMinimumSize( new Dimension(100, 28) );
			projectComboBox.setMaximumSize( new Dimension(500, 28) );
			projectComboBox.setPreferredSize( new Dimension(240, 28) );
			projectComboPanel.setVisible( false );

			//			projectComboBox.setBorder(BorderFactory.createMatteBorder(4, 4, 4, 4,Color.WHITE));
			//			projectComboBox.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
			projectComboBox.addActionListener( new ActionListener()
				{
					public void actionPerformed( 
						final ActionEvent _e )
					{
						NamedFrame frame = (NamedFrame)projectComboBox.getSelectedItem();

						if ((frame == null) || (frame == previous))
						{
							return;
						}

						activateFrame( frame );
						frame.fireNamedFrameActivated( new NamedFrameEvent(frame) );
					}
				} );

			if (Environment.isNoPodServer())
			{
				projectComboPanel.setLayout( new BorderLayout() );
				projectComboPanel.setOpaque( false );

				otherLockIcon = IconManager.getIcon( "menu24.lock" );
				lockIcon = IconManager.getIcon( "menu24.lock" );
				unlockIcon = IconManager.getIcon( "menu24.unlock" );
				mspIcon = IconManager.getIcon( "menu24.lock" );
				noLongerLockedIcon = IconManager.getIcon( "redX" );
				lock = new JLabel(lockIcon);
				lock.setLayout( new BorderLayout() );
				projectComboPanel.add( lock, BorderLayout.EAST );
				lock.setVisible( false );
			}
			else
			{
				projectComboPanel.add( projectComboBox );
			}
		}

		return projectComboBox;
	}

	public JPanel getProjectComboPanel()
	{
		return projectComboPanel;
	}

	public Component getSelectedFrame()
	{
		return (Component)getProjectComboBox().getSelectedItem();
	}

	public com.projity.pm.graphic.frames.workspace.Workspace getWorkspace()
	{
		return new com.projity.pm.graphic.frames.workspace.Workspace();
	}

	public void removeFrame( 
		final NamedFrame _frame )
	{
		if (_frame == null) // in case of subproject for example, it didn't have its own frame
		{
			return;
		}

		getProjectComboBox().removeItem( _frame );

		if (container != null)
		{
			container.remove( _frame );
		}

		((DocumentFrame)_frame).cleanUp();

		if (getProjectComboBox().getItemCount() == 0)
		{
			previous = null;
			container.add( emptyPanel, "Center" );
			projectComboPanel.setVisible( false );
		}
		else
		{
			if (previous != null)
			{
				activateFrame( previous ); // try to activate last activated
			}
			else
			{
				activateFrame( (NamedFrame)getProjectComboBox().getItemAt( 0 ) ); // activate first one otherwise
			}
		}
	}

//	final RootPaneContainer getRoot() 
//	{
//		return root;
//	}
	
	@Override
	public void restoreWorkspace( 
		final WorkspaceSetting _w,
		final int _context )
	{
		FrameWorkspace ws = (FrameWorkspace)_w;
		workspace = ws;

		Iterator i = ws.list.iterator();

		while (i.hasNext())
		{
			DocumentFrame.Workspace documentFrameWorkspace = (DocumentFrame.Workspace)i.next();
			long projectId = documentFrameWorkspace.getProjectId();
			Project project = ProjectFactory.getInstance().findFromId( projectId );

			if (project == null)
			{
				LoadOptions opt = new LoadOptions();
				opt.setId( projectId );
				opt.setSync( true );
				ProjectFactory.getInstance().openProject( opt );
			}

			DocumentFrame documentFrame = graphicManager.addProjectFrame( project ); // will add to combo
			documentFrame.restoreWorkspace( documentFrameWorkspace, _context ); // a little ugly, in that the worspace is used above to create the frame
		}

		getProjectComboBox().setSelectedIndex( ws.getSelectedIndex() );
	}

	//	public void setLockStatus(boolean locked, boolean lockedByOther,String tip) {
	public void setLockStatus( 
		final Project.LockStatus _status,
		final String _tip,
		final String _lockedBy )
	{
		//		System.out.println("set lock status " + locked + " other " + lockedByOther +" " + tip);
		if (lock == null)
		{
			return;
		}

		if (_status == Project.LockStatus.NONE)
		{
			lock.setVisible( false );
		}
		else
		{
			lock.setText( "" );

			ImageIcon icon = null;

			if (_status == Project.LockStatus.UNLOCKED)
			{
				icon = unlockIcon;
				lock.setText( Messages.getString( "Text.unlocked" ) );
			}
			else if (_status == Project.LockStatus.LOCKED)
			{
				icon = lockIcon;
				lock.setText( Messages.getStringWithParam( "Text.lockedBy", _lockedBy ) );
			}
			else if (_status == Project.LockStatus.LOCKED_BY_OTHER)
			{
				icon = otherLockIcon;
				lock.setText( Messages.getStringWithParam( "Text.lockedBy", _lockedBy ) );
			}
			else if (_status == Project.LockStatus.MSP)
			{
				icon = mspIcon;
				lock.setText( Messages.getString( "Text.lockedByMSP" ) );
			}
			else if (_status == Project.LockStatus.NO_LONGER_LOCKED)
			{
				icon = noLongerLockedIcon;
				lock.setText( Messages.getString( "Text.noLongerLocked" ) );
			}

			lock.setIcon( icon );
			lock.setVisible( true );
			lock.setToolTipText( _tip );
		}
	}

	public void setTabTitle( 
		final NamedFrame _frame,
		final String _tabTitle )
	{
		// TODO Auto-generated method stub
	}

	public void showFrame( 
		final NamedFrame _frame )
	{
		getProjectComboBox().setSelectedItem( _frame );
	}

	public void update()
	{
		FrameComboBoxModel model = (FrameComboBoxModel)getProjectComboBox().getModel();
		model.update();
	}

	protected class FrameComboBoxModel
		extends DefaultComboBoxModel
	{
		public FrameComboBoxModel()
		{
			super();
		}

		public void addElement( 
			final Object _anObject )
		{
			super.addElement( _anObject );
		}

		public void update()
		{
			fireContentsChanged( this, -1, -1 );
		}
	}

	public static class FrameWorkspace
		implements WorkspaceSetting
	{ // named FrameWorkspace to avoid conflict
		public final LinkedList getList()
		{
			return list;
		}

		public final int getSelectedIndex()
		{
			return selectedIndex;
		}

		public final void setList( 
			final LinkedList _list )
		{
			this.list = _list;
		}

		public final void setSelectedIndex( 
			final int _selectedIndex )
		{
			this.selectedIndex = _selectedIndex;
		}

		private static final long serialVersionUID = -4029197146082617077L;
		LinkedList list;
		int selectedIndex;
	}

	private static final long serialVersionUID = -1835326043838730651L;
	public static final int SPLIT_EAST_WEST_SOUTH_NORTH = 0;

	//RootPaneContainer root;
	Container container;
	Container emptyPanel;
	GraphicManager graphicManager;
	ImageIcon lockIcon;
	ImageIcon mspIcon;
	ImageIcon noLongerLockedIcon;
	ImageIcon otherLockIcon;
	ImageIcon unlockIcon;
	JComboBox projectComboBox;
	JLabel lock = null;
	JPanel projectComboPanel;
	NamedFrame previous = null;
	private FrameWorkspace workspace;
}
