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
package com.projity.pm.graphic.chart;

import com.projity.configuration.Configuration;

import com.projity.document.ObjectEvent;

import com.projity.field.Field;

import com.projity.graphic.configuration.SpreadSheetFieldArray;

import com.projity.grouping.core.model.NodeModel;
import com.projity.pm.assignment.TimeDistributedConstants;

import com.projity.pm.graphic.model.cache.NodeModelCache;
import com.projity.pm.graphic.model.event.CacheListener;
import com.projity.pm.graphic.model.event.CompositeCacheEvent;
import com.projity.pm.graphic.spreadsheet.selection.event.SelectionNodeEvent;
import com.projity.pm.graphic.spreadsheet.selection.event.SelectionNodeListener;
import com.projity.pm.graphic.timescale.CoordinatesConverter;
import com.projity.pm.graphic.views.ChartView;
import com.projity.pm.resource.Resource;
import com.projity.pm.scheduling.ScheduleEvent;
import com.projity.pm.scheduling.ScheduleEventListener;
import com.projity.pm.task.Project;

import com.projity.timescale.TimeScaleEvent;
import com.projity.timescale.TimeScaleListener;

import com.projity.workspace.SavableToWorkspace;
import com.projity.workspace.WorkspaceSetting;

import org.jfree.chart.JFreeChart;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;

/**
 * This class serves as a moderator between the ChartPanel and the ChartLegend
 */
public class ChartInfo
	implements CacheListener,
		ObjectEvent.Listener,
		SavableToWorkspace,
		ScheduleEventListener,
		SelectionNodeListener,
		Serializable,
		TimeScaleListener
{
	/**
	 *
	 */
	public ChartInfo()
	{
		super();
	}

	@Override
	public WorkspaceSetting createWorkspace( 
		final int _context )
	{
		Workspace ws = new Workspace();
		ws.cumulative = cumulative;
		ws.histogram = histogram;
		ws.selectedOnTop = selectedOnTop;
		ws.work = work;
		ws.verticalScroll = chartPanel.isVerticalScrolling();

		if (simple == false)
		{
			ws.setTraces( traces );
		}

		return ws;
	}

	/**
	 * @return Returns the axisPanel.
	 */
	public AxisPanel getAxisPanel()
	{
		return axisPanel;
	}

	public final NodeModelCache getCache()
	{
		return cache;
	}

	/**
	 * @return Returns the chart.
	 */
	public JFreeChart getChart()
	{
		return chart;
	}

	/**
	 * @return Returns the chartLegend.
	 */
	public ChartLegend getChartLegend()
	{
		return chartLegend;
	}

	/**
	 * @return Returns the chartPanel.
	 */
	public TimeChartPanel getChartPanel()
	{
		return chartPanel;
	}

	/**
	 * @return Returns the chartView.
	 */
	public ChartView getChartView()
	{
		return chartView;
	}

	/**
	 * @return Returns the coord.
	 */
	public CoordinatesConverter getCoord()
	{
		return coord;
	}

	public double getFooterHeight()
	{
		return chartPanel.getNonPlotHeight();
	}

	public double getHeaderHeight()
	{
		return chartView.getHeaderComponentHeight();
	}

	/**
	 * @return Returns the model.
	 */
	public ChartModel getModel()
	{
		return model;
	}

	/**
	 * @return Returns the nodeModel.
	 */
	public NodeModel getNodeModel()
	{
		return nodeModel;
	}

	/**
	 * @return Returns the project.
	 */
	public Project getProject()
	{
		return project;
	}

	/**
	 * @return Returns the resources.
	 */
	public List getResources()
	{
		return resources;
	}

	/**
	 * @return Returns the selectedObjects.
	 */
	public List getSelectedObjects()
	{
		return selectedObjects;
	}

	/**
	 * @return Returns the tasks.
	 */
	public List getTasks()
	{
		return tasks;
	}

	/**
	 * @return Returns the traces.
	 */
	public Object[] getTraces()
	{
		return traces;
	}

	@Override
	public void graphicNodesCompositeEvent( 
		final CompositeCacheEvent _e )
	{
		if (!isVisible())
		{
			return;
		}

		chartLegend.rebuildTree();
	}

	/**
	 * @return Returns the cumulative.
	 */
	public boolean isCumulative()
	{
		return cumulative;
	}

	/**
	 * @return Returns the histogram.
	 */
	public boolean isHistogram()
	{
		return histogram;
	}

	public boolean isRestoring()
	{
		return restoring;
	}

	public boolean isSelectedOnTop()
	{
		return selectedOnTop;
	}

	/**
	 * @return Returns the simple.
	 */
	public boolean isSimple()
	{
		return simple;
	}

	public boolean isWork()
	{
		return work;
	}

	/* (non-Javadoc)
	 * @see com.projity.field.ObjectEvent.Listener#objectChanged(com.projity.field.ObjectEvent)
	 */
	@Override
	public void objectChanged( 
		final ObjectEvent _objectEvent )
	{
		if (!isVisible())
		{
			return;
		}

		if (_objectEvent.getObject() instanceof Resource)
		{
			chartLegend.rebuildTree(); // take into account different resources
			updateChart( tasks, resources );
		}
	}

	@Override
	public void restoreWorkspace( 
		final WorkspaceSetting _workspace,
		final int _context )
	{
		Workspace ws = (Workspace)_workspace;
		cumulative = ws.cumulative;
		histogram = ws.histogram;
		selectedOnTop = ws.selectedOnTop;
		work = ws.work;
		chartPanel.setVerticalScrolling( ws.verticalScroll );
		chartPanel.verticalScrollingItem.setSelected( ws.verticalScroll );

		if (simple == false)
		{
			setTraces( ws.getTraces() );
		}
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.scheduling.ScheduleEventListener#scheduleChanged(com.projity.pm.scheduling.ScheduleEvent)
	 */
	@Override
	public void scheduleChanged( 
		final ScheduleEvent _evt )
	{
		if (!isVisible())
		{
			return;
		}

		updateChart( tasks, resources );
	}

	@Override
	public void selectionChanged( 
		final SelectionNodeEvent _e )
	{
		if (!isVisible())
		{
			return;
		}

		chartLegend.selectionChanged( _e ); // pass it along
	}

	/**
	 * @param axisPanel The axisPanel to set.
	 */
	public void setAxisPanel( 
		final AxisPanel _axisPanel )
	{
		this.axisPanel = _axisPanel;
	}

	public void setCache( 
		final NodeModelCache _cache )
	{
		if (this.cache == null)
		{
			_cache.removeNodeModelListener( this );
		}

		this.cache = _cache;
		_cache.update();
		_cache.addNodeModelListener( this );
	}

	/**
	 * @param chart The chart to set.
	 */
	public JFreeChart setChart( 
		final JFreeChart _chart )
	{
		this.chart = _chart;

		return _chart;
	}

	/**
	 * @param chartLegend The chartLegend to set.
	 */
	public void setChartLegend( 
		final ChartLegend _chartLegend )
	{
		this.chartLegend = _chartLegend;
	}

	/**
	 * @param chartPanel The chartPanel to set.
	 */
	public void setChartPanel( 
		final TimeChartPanel _chartPanel )
	{
		this.chartPanel = _chartPanel;
	}

	/**
	 * @param chartView The chartView to set.
	 */
	public void setChartView( 
		final ChartView _chartView )
	{
		this.chartView = _chartView;
	}

	/**
	 * @param coord The coord to set.
	 */
	public void setCoord( 
		final CoordinatesConverter _coord )
	{
		if (this.coord != null)
		{
			this.coord.removeTimeScaleListener( this );
		}

		this.coord = _coord;
		model = new ChartModel(_coord);
		_coord.addTimeScaleListener( this );
	}

	public void setCumulative( 
		final boolean _cumulative )
	{
		this.cumulative = _cumulative;
		updateChart( tasks, resources );
	}

	public void setCumulativeCostMode()
	{
		cumulative = true;
		histogram = false;
	}

	/**
	 * @param histogram The histogram to set.
	 */
	public void setHistogram( 
		final boolean _histogram )
	{
		this.histogram = _histogram;
		updateChart( tasks, resources );
	}

	/**
	 * @param model The model to set.
	 */
	public void setModel( 
		final ChartModel _model )
	{
		this.model = _model;
	}

	/**
	 * @param nodeModel The nodeModel to set.
	 */
	public void setNodeModel( 
		final NodeModel _nodeModel )
	{
		this.nodeModel = _nodeModel;
	}

	/**
	 * @param project The project to set.
	 */
	public void setProject( 
		final Project _project )
	{
		if (this.project != null)
		{
			this.project.removeScheduleListener( this );
			this.project.getResourcePool().removeObjectListener( this );
		}

		this.project = _project;
		_project.addScheduleListener( this );
		_project.getResourcePool().addObjectListener( this );
		nodeModel = _project.getResourcePool().getResourceOutline();
	}

	/**
	 * @param resources The resources to set.
	 */
	public void setResources( 
		final List _resources )
	{
		this.resources = _resources;
	}

	public void setRestoring( 
		final boolean _restoring )
	{
		this.restoring = _restoring;
	}

	/**
	 * @param selectedObjects The selectedObjects to set.
	 */
	public void setSelectedObjects( 
		final List _selectedObjects )
	{
		this.selectedObjects = _selectedObjects;
	}

	public void setSelectedOnTop( 
		final boolean _multiproject )
	{
		this.selectedOnTop = _multiproject;
	}

	/**
	 * @param simple The simple to set.
	 */
	public void setSimple( 
		final boolean _simple )
	{
		this.simple = _simple;
	}

	/**
	 * @param tasks The tasks to set.
	 */
	public void setTasks( 
		final List _tasks )
	{
		this.tasks = _tasks;
	}

	/**
	 * 
	 * @param _traces 
	 */
	public void setTraces( 
		final TimeDistributedConstants[] _traces )
	{
		if (_traces.length == 0) // will happen if changing from cost to work.  Don't change it
		{
			return;
		}

		this.traces = _traces;

		if (chartPanel != null) // the very first time on histogram we need to set traces before chart panel is created
		{
			updateChart( tasks, resources );
		}
	}

	/**
	 * 
	 * @param _traces 
	 */
	public void setTraces( 
		final List<TimeDistributedConstants> _traces )
	{
		if (_traces.isEmpty() == true) // will happen if changing from cost to work.  Don't change it
		{
			return;
		}

		this.traces = _traces.toArray( this.traces );

		if (chartPanel != null) // the very first time on histogram we need to set traces before chart panel is created
		{
			updateChart( tasks, resources );
		}
	}

	/**
	 * 
	 * @param _work 
	 */
	public void setWork( 
		final boolean _work )
	{
		this.work = _work;
	}

	@Override
	public void timeScaleChanged( 
		final TimeScaleEvent _event )
	{
		if (isVisible() == false)
		{
			return;
		}

		updateChart( tasks, resources );
	}

	public void updateChart( 
		final List _tasks,
		final List _resources )
	{
		this.tasks = _tasks;
		this.resources = _resources;

		if (isSimple() == true)
		{
			model.computeHistogram( getProject(), _tasks, _resources, traces );
		}
		else
		{
			model.computeValues( _tasks, _resources, cumulative, traces, histogram );
		}

		chart = chartPanel.buildChart();
		setChart( chart );
		chartPanel.updateChart();
		axisPanel.setAxis( getChart().getXYPlot().getRangeAxis() );
		axisPanel.repaint();
	}

	boolean isVisible()
	{
		return chartView.isVisible();
	}

	public static class Workspace
		implements WorkspaceSetting
	{
		public TimeDistributedConstants[] getTraces()
		{
			final TimeDistributedConstants[] results = new TimeDistributedConstants[ traces.length ];
			
			for (int index = 0; index < traces.length; index++)
			{
				results[ index ] = Enum.valueOf( TimeDistributedConstants.class, traces[ index ] );
			}
			
			return results;
		}

		public boolean isCumulative()
		{
			return cumulative;
		}

		public boolean isHistogram()
		{
			return histogram;
		}

		public boolean isSelectedOnTop()
		{
			return selectedOnTop;
		}

		public boolean isVerticalScroll()
		{
			return verticalScroll;
		}

		public boolean isWork()
		{
			return work;
		}

		public void setCumulative( 
			final boolean _cumulative )
		{
			this.cumulative = _cumulative;
		}

		public void setHistogram( 
			final boolean _histogram )
		{
			this.histogram = _histogram;
		}

		public void setSelectedOnTop( 
			final boolean _selectedOnTop )
		{
			this.selectedOnTop = _selectedOnTop;
		}

		public void setTraces( 
			final TimeDistributedConstants[] _traces )
		{
			this.traces = new String[ _traces.length ];
			
			for (int index = 0; index < _traces.length; index++)
			{
				traces[ index ] = _traces[ index ].toString();
			}
		}

		public void setVerticalScroll( 
			final boolean _verticalScroll )
		{
			this.verticalScroll = _verticalScroll;
		}

		public void setWork( 
			final boolean _work )
		{
			this.work = _work;
		}

		private static final long serialVersionUID = -1369065811123053002L;
		String[] traces;
		boolean cumulative;
		boolean histogram;
		boolean selectedOnTop;
		boolean verticalScroll;
		boolean work;
	}

	private static final long serialVersionUID = -6593093924980192805L;
	AxisPanel axisPanel;
	ChartLegend chartLegend;
	ChartModel model;
	ChartView chartView;
	CoordinatesConverter coord;
	JFreeChart chart;
	List resources;
	List selectedObjects = new ArrayList();
	List tasks;
	NodeModel nodeModel;
	NodeModelCache cache = null; // for resources
	Project project;
	TimeChartPanel chartPanel;
	TimeDistributedConstants[] traces = {  };
	boolean cumulative = false;
	boolean histogram = true;
	boolean selectedOnTop = true;
	boolean simple;
	boolean work = true;
	private boolean restoring = false;
}
