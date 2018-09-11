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
package com.projity.pm.graphic.views;

import com.projity.configuration.Dictionary;

import com.projity.graphic.configuration.BarStyles;

import com.projity.grouping.core.model.NodeModel;

import com.projity.help.HelpUtil;

import com.projity.menu.MenuActionConstants;
import com.projity.menu.MenuManager;

import com.projity.pm.graphic.frames.DocumentFrame;
import com.projity.pm.graphic.model.cache.NodeModelCache;
import com.projity.pm.graphic.model.cache.NodeModelCacheFactory;
import com.projity.pm.graphic.model.cache.ReferenceNodeModelCache;
import com.projity.pm.graphic.spreadsheet.SpreadSheet;
import com.projity.pm.graphic.xbs.Xbs;
import com.projity.pm.task.Project;

import com.projity.undo.UndoController;

import com.projity.workspace.WorkspaceSetting;

import org.apache.commons.collections.Closure;

import javax.swing.JScrollPane;
import javax.swing.JViewport;

/**
 *
 */
public class TreeView
	extends JScrollPane
	implements BaseView
{
	/**
	 *
	 */
	public TreeView( 
		final DocumentFrame _documentFrame,
		final MenuManager _manager )
	{
		super();
		
		documentFrame = _documentFrame;
		project = _documentFrame.getProject();
	}

	@Override
	public boolean canScrollToTask()
	{
		return false;
	}

	@Override
	public boolean canZoomIn()
	{
		return tree.canZoomIn();
	}

	@Override
	public boolean canZoomOut()
	{
		return tree.canZoomOut();
	}

	@Override
	public void cleanUp()
	{
		tree.cleanUp();
		tree = null;
		model = null;
		project = null;
		documentFrame = null;
	}

	@Override
	public WorkspaceSetting createWorkspace( 
		final int _context )
	{
		Workspace ws = new Workspace();
		ws.network = tree.createWorkspace( _context );

		return ws;
	}

	@Override
	public NodeModelCache getCache()
	{
		return cache;
	}

	@Override
	public int getScale()
	{
		return tree.getZoom();
	}

	@Override
	public SpreadSheet getSpreadSheet()
	{
		return null;
	}

	@Override
	public UndoController getUndoController()
	{
		if (showsTasks() == true)
		{
			return project.getNodeModelDataFactory().getUndoController();
		}
		else
		{
			return project.getResourcePool().getUndoController();
		}
	}

	@Override
	public String getViewName()
	{
		return viewName;
	}

	@Override
	public boolean hasNormalMinWidth()
	{
		return true;
	}

	public void init( 
		final ReferenceNodeModelCache _cache,
		final NodeModel _model,
		final String _viewName,
		final Closure _transformerClosure )
	{
		tree = new Xbs( project, _viewName );
		viewName = _viewName;
		cache = NodeModelCacheFactory.getInstance().createAntiAssignmentFilteredCache( (ReferenceNodeModelCache)_cache, 
			viewName, _transformerClosure );
		tree.setCache( cache );
		tree.setBarStyles( (BarStyles)Dictionary.get( BarStyles.category, _viewName ) );

		JViewport viewport = createViewport();
		viewport.setView( tree );
		setViewport( viewport );

		//this is not required by certain views 
		_cache.update(); 
		
		HelpUtil.addDocHelp( this, (viewName.equals( MenuActionConstants.ACTION_RBS ) == true)
			? "RBS_Chart"
			: "WBS_Chart" );

		//tree.insertCacheData();
	}

	@Override
	public boolean isPrintable()
	{
		return true;
	}

	@Override
	public void onActivate( 
		final boolean _activate )
	{
	}

	@Override
	public void restoreWorkspace( 
		final WorkspaceSetting _setting,
		final int context )
	{
		Workspace ws = (Workspace)_setting;
		tree.restoreWorkspace( ws.network, context );
	}

	@Override
	public void scrollToTask()
	{
		// TODO Auto-generated method stub
	}

	@Override
	public boolean showsResources()
	{
		return viewName.equals( MenuActionConstants.ACTION_RBS );
	}

	@Override
	public boolean showsTasks()
	{
		return viewName.equals( MenuActionConstants.ACTION_WBS );
	}

	@Override
	public void zoomIn()
	{
		tree.zoomIn();
	}

	@Override
	public void zoomOut()
	{
		tree.zoomOut();
	}

	public static class Workspace
		implements WorkspaceSetting
	{
		public WorkspaceSetting getNetwork()
		{
			return network;
		}

		public void setNetwork( 
			WorkspaceSetting _network )
		{
			network = _network;
		}

		private static final long serialVersionUID = 7828075902711289247L;
		private WorkspaceSetting network;
	}

	private static final long serialVersionUID = 2390048109591199408L;

	private NodeModel model;
	private NodeModelCache cache;
	private Project project;
	private Xbs tree;
	private DocumentFrame documentFrame;
	private String viewName = null;
}
