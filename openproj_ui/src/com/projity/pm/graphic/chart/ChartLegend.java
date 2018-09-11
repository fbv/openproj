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

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import com.projity.grouping.core.NodeList;
import com.projity.grouping.core.model.NodeModel;
import com.projity.grouping.core.transform.CommonTransformFactory;
import com.projity.grouping.core.transform.ViewConfiguration;

import com.projity.menu.MenuActionConstants;

import com.projity.pm.assignment.AssignmentContainer;
import com.projity.pm.assignment.HasTimeDistributedData;
import com.projity.pm.assignment.TimeDistributedConstants;
import com.projity.pm.graphic.IconManager;
import com.projity.pm.graphic.model.cache.GraphicNode;
import com.projity.pm.graphic.spreadsheet.selection.event.SelectionNodeEvent;
import com.projity.pm.graphic.spreadsheet.selection.event.SelectionNodeListener;
import com.projity.pm.graphic.swing.Util;
import com.projity.pm.resource.Resource;
import com.projity.pm.resource.ResourceImpl;
import com.projity.pm.task.Project;

import com.projity.strings.Messages;

import com.projity.toolbar.TransformComboBox;
import com.projity.toolbar.TransformComboBoxModel;

import com.projity.util.Environment;

import com.projity.workspace.SavableToWorkspace;
import com.projity.workspace.WorkspaceSetting;

import org.apache.commons.collections.Closure;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.basic.BasicListUI;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;

/**
 *
 */
public class ChartLegend
	implements SelectionNodeListener,
		Serializable,
		SavableToWorkspace
{
	public ChartLegend( 
		final ChartInfo _chartInfo )
	{
		super();
		this.chartInfo = _chartInfo;
		this.simple = _chartInfo.isSimple();
	}

	public JComponent createContentPanel()
	{
		// Separating the component initialization and configuration
		// from the layout code makes both parts easier to read.
		initControls
		// Separating the component initialization and configuration
		// from the layout code makes both parts easier to read.
		();

		FormLayout layout = new FormLayout("p:grow, 3dlu,100dlu:grow,5dlu, default, 5dlu", // cols //$NON-NLS-1$
				"p, 3dlu, p, 3dlu, p, 3dlu, " + (simple
				? ""
				: "fill:") + "p:grow, 5dlu"); // rows		 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		// Create a builder that assists in adding components to the container.
		// Wrap the panel with a standardized border.
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setBorder( BorderFactory.createEmptyBorder( 0, 5, 0, 0 ) );

		CellConstraints cc = new CellConstraints();

		builder.addLabel( Messages.getString( "ChartLegend.ResourceFilter" ), cc.xy( 1, 1 ) ); //$NON-NLS-1$
		builder.add( filterComboBox, cc.xy( 3, 1 ) );
		builder.add( treeScrollPane, cc.xywh( 1, 3, 3, 5 ) );
		builder.add( tracesScrollPane, cc.xy( 5, 5 ) );

		if (simple)
		{
			builder.add( selectedOnTop, cc.xy( 5, 1 ) );
		}
		else
		{
			builder.add( cumulative, cc.xy( 5, 1 ) );
			builder.add( histogram, cc.xy( 5, 3 ) );
			builder.add( workCostRadioPanel(), cc.xy( 5, 5 ) );
		}

		return builder.getPanel();
	}

	@Override
	public WorkspaceSetting createWorkspace( 
		final int _context )
	{
		Workspace ws = new Workspace();
		ws.treeViewPosition = treeScrollPane.getViewport().getViewPosition();
		ws.tracesViewPosition = tracesScrollPane.getViewport().getViewPosition();

		return ws;
	}

	@Override
	public void restoreWorkspace( 
		final WorkspaceSetting _w,
		final int _context )
	{
		Workspace ws = (Workspace)_w;
		treeScrollPane.getViewport().setViewPosition( ws.treeViewPosition );
		tracesScrollPane.getViewport().setViewPosition( ws.tracesViewPosition );
	}

	@Override
	public void selectionChanged( 
		final SelectionNodeEvent _e )
	{
		if (!chartInfo.isVisible())
		{
			return;
		}

		List nodes = _e.getNodes();
		selectedObjects = getListFromNodeList( nodes );

		List resList = extractResources( selectedObjects );

		if (resList.isEmpty())
		{
			selectedResourcesFromTasks = AssignmentContainer.extractOppositeList( selectedObjects, false );
		}
		else
		{
			selectedResourcesFromTasks = resList; // top view is resource list
			selectedObjects = null;
		}

		setTreeSelection( selectedResourcesFromTasks );
		chartInfo.updateChart( selectedObjects, selectedResourcesOnTree );
	}

	public void setControlValues()
	{
		if (simple == true)
		{
			selectedOnTop.setSelected( chartInfo.isSelectedOnTop() ); // start off as histogram
		}
		else
		{
			cumulative.setSelected( chartInfo.isCumulative() ); // start off as histogram
			histogram.setSelected( chartInfo.isHistogram() ); // start off as histogram
			work.setSelected( chartInfo.isWork() );
			cost.setSelected( !chartInfo.isWork() );
			Util.setSelectedValues( tracesList, chartInfo.traces );
		}
	}

	public Component workCostRadioPanel()
	{
		JPanel panel = new JPanel();
		panel.add( work );
		panel.add( cost );

		return panel;
	}

	JList getListInstance( 
		final boolean _cost )
	{
		final JList<TimeDistributedConstants> list = new JList<TimeDistributedConstants>()
		{ 
			// do not want to update the UI. see below also
			private static final long serialVersionUID = 1L;

			@Override
			public void updateUI()
			{
				if (Environment.isNewLook() == false)
				{
					super.updateUI();
				}
			}
		};

		if (Environment.isNewLook() == true) // The PLAF can override the custom renderer. This avoids that
		{
			list.setUI( new BasicListUI() );
		}

		list.setSelectionMode( DefaultListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
		list.setCellRenderer( new ListRenderer() );
		setListFields( list, _cost );

		if (simple == false)
		{
			list.setSelectedIndex( 0 ); // start off with first choice selected			
			list.addListSelectionListener( 
				new ListSelectionListener()
			{
				@Override
				public void valueChanged( 
					final ListSelectionEvent _event )
				{
					if (chartInfo.isRestoring() == true) // don't want to listen if updating from workspace
					{
						return;
					}

					if (_event.getValueIsAdjusting() == false)
					{
						chartInfo.setTraces( list.getSelectedValuesList() );
					}
				}
			} );
		}

		return list;
	}

	void initControls()
	{
		chartInfo.setAxisPanel( new AxisPanel(chartInfo) );
		filterComboBox = new TransformComboBox(null, MenuActionConstants.ACTION_CHOOSE_FILTER, TransformComboBoxModel.FILTER);
		filterComboBox.setView( ViewConfiguration.getView( MenuActionConstants.ACTION_CHARTS ) );
		filterComboBox.addActionListener( 
			new ActionListener()
		{
			@Override
			public void actionPerformed( 
				final ActionEvent _e )
			{
				if (chartInfo.isRestoring())
				{
					return;
				}

				final TransformComboBox combo = (TransformComboBox)_e.getSource();
				final CommonTransformFactory factory = (CommonTransformFactory)combo.getSelectedItem();
				((TransformComboBoxModel)combo.getModel()).changeTransform( factory );
			}
		} );

		initTree();

		final TimeDistributedConstants[] fields = getFields( false );

		workTraces = getListInstance( false );
		tracesList = workTraces; // start off work
		tracesScrollPane = new JScrollPane(workTraces);
		workTraces.setVisibleRowCount( Environment.getStandAlone()
			? HasTimeDistributedData.tracesCount
			: HasTimeDistributedData.serverTracesCount );

//		final ViewTransformer transformer=ViewConfiguration.getView(MenuActionConstants.ACTION_CHARTS).getTransform();
//		final ResourceInTeamFilter hiddenFilter=(ResourceInTeamFilter)transformer.getHiddenFilter();		
//		teamResources= new JCheckBox(Messages.getString("Text.ShowTeamResourcesOnly"));
//		teamResources.addItemListener(new ItemListener() 
//		{
//			public void itemStateChanged(ItemEvent e) 
//			{
//				hiddenFilter.setFilterTeam(e.getStateChange() == ItemEvent.SELECTED);
//				transformer.update();
//			}
//		});
//		teamResources.setSelected(hiddenFilter.isFilterTeam());
		
		if (simple == true)
		{
			chartInfo.setTraces( fields );
			tree.getSelectionModel().setSelectionMode( DefaultTreeSelectionModel.SINGLE_TREE_SELECTION ); // allow only 1 for histogram

			selectedOnTop = new JCheckBox(Messages.getString( "Text.ShowSelectedOnTop" )); //$NON-NLS-1$
			selectedOnTop.addItemListener( new ItemListener()
			{
				@Override
				public void itemStateChanged( 
					final ItemEvent _event )
				{
					chartInfo.setSelectedOnTop( _event.getStateChange() == ItemEvent.SELECTED );

					final TimeDistributedConstants[] traces = getFields( false );
					chartInfo.setTraces( traces );
					workTraces = getListInstance( false );
					tracesScrollPane.getViewport().add( workTraces );
					workTraces.setVisibleRowCount( (Environment.getStandAlone() == true)
						? HasTimeDistributedData.tracesCount
						: HasTimeDistributedData.serverTracesCount );
				}
			} );
			selectedOnTop.setSelected( chartInfo.isSelectedOnTop() ); // start off as histogram

			return;
		}

		costTraces = getListInstance( true );

		cumulative = new JCheckBox(Messages.getString( "Text.Cumulative" )); //$NON-NLS-1$
		cumulative.setSelected( chartInfo.isCumulative() ); // start off as histogram
		cumulative.addItemListener( new ItemListener()
		{
			@Override
			public void itemStateChanged( 
				final ItemEvent _e )
			{
				chartInfo.setCumulative( _e.getStateChange() == ItemEvent.SELECTED );
			}
		} );

		histogram = new JCheckBox( Messages.getString( "Text.Histogram" ) ); //$NON-NLS-1$
		histogram.setSelected( chartInfo.isHistogram() ); // start off as histogram
		histogram.addItemListener( new ItemListener()
		{
			@Override
			public void itemStateChanged( 
				final ItemEvent _e )
			{
				final boolean histogramSelected = _e.getStateChange() == ItemEvent.SELECTED;
				chartInfo.setHistogram( histogramSelected );

				if (histogramSelected)
				{
					workTraces.setSelectionMode( DefaultListSelectionModel.SINGLE_SELECTION ); // allow only 1 for histogram
					costTraces.setSelectionMode( DefaultListSelectionModel.SINGLE_SELECTION ); // allow only 1 for histogram
				}
				else
				{
					workTraces.setSelectionMode( DefaultListSelectionModel.MULTIPLE_INTERVAL_SELECTION ); // allow many
					costTraces.setSelectionMode( DefaultListSelectionModel.MULTIPLE_INTERVAL_SELECTION ); // allow only 1 for histogram					
				}
			}
		} );

		work = new JRadioButton(Messages.getString( "Text.work" )); //$NON-NLS-1$
		work.setSelected( chartInfo.isWork() );
		cost = new JRadioButton(Messages.getString( "Text.cost" )); //$NON-NLS-1$

		final ItemListener costWork = new ItemListener()
		{
			@Override
			public void itemStateChanged( 
				final ItemEvent _event )
			{
				final boolean isCost = _event.getSource() == cost;
				chartInfo.setWork( !isCost );
				tracesList = (isCost == true)
					? costTraces
					: workTraces;
				tracesScrollPane.getViewport().add( tracesList );

				if (chartInfo.isRestoring() == false)
				{
					chartInfo.setTraces( (TimeDistributedConstants [])(tracesList.getSelectedValues()) );
				}
			}
		};

		cost.addItemListener( costWork );
		work.addItemListener( costWork );

		final ButtonGroup group = new ButtonGroup();
		group.add( cost );
		group.add( work );

		// by default, always select first item
		chartInfo.setTraces( new TimeDistributedConstants[]
		{
			fields[ 0 ]
		} );
	}

	void rebuildTree()
	{
		//		System.out.println("rebuilding tree");
		initTree
		//		System.out.println("rebuilding tree");
		();

		//		((AbstractMutableNodeHierarchy)chartInfo.getCache().getReference().getModel().getHierarchy()).dump();
	}

	private List extractResources( 
		final List _list )
	{
		ArrayList resList = new ArrayList();
		Iterator i = _list.iterator();
		Object obj;

		while (i.hasNext())
		{
			obj = i.next();

			if (obj instanceof Resource)
			{
				resList.add( obj );
			}
		}

		return resList;
	}

	private TimeDistributedConstants[] getFields( 
		final boolean _cost )
	{
		if (simple == true)
		{
			return (chartInfo.isSelectedOnTop() == true)
			? ((Environment.getStandAlone() == true)
				? HasTimeDistributedData.histogramTypes
				: HasTimeDistributedData.serverHistogramTypes)
			: ((Environment.getStandAlone() == true)
				? HasTimeDistributedData.reverseHistogramTypes
				: HasTimeDistributedData.serverReverseHistogramTypes);
		}

		return (_cost == true)
			? HasTimeDistributedData.costTypes
			: HasTimeDistributedData.workTypes;
	}

	private List getListFromNodeList( 
		final List _nodes )
	{
		List implList = NodeList.nodeListToImplList( _nodes );

		// normally it is tasks or resources, but if project, make sure its works too
		if (implList.isEmpty() || !(implList.get( 0 ) instanceof Project))
		{
			return implList;
		}

		final List resultList = new ArrayList();
		Iterator i = implList.iterator();

		while (i.hasNext())
		{
			((Project)i.next()).forTasks( new Closure()
				{
					@Override
					public void execute( 
						final Object _arg0 )
					{
						resultList.add( _arg0 );
					}
				} );

			//resultList.addAll( ((Project)i.next()).getTasks());
		}

		return resultList;
	}

	private void initTree()
	{
		//		tree = new JTree(chartInfo.getNodeModel());
		tree = new JTree(chartInfo.getCache());
		tree.setExpandsSelectedPaths( true );

		final JTree finalTree = tree;
		tree.addTreeSelectionListener( new TreeSelectionListener()
		{
			@Override
			public void valueChanged( 
				final TreeSelectionEvent _event )
			{
				if (selecting == true)
				{
					return;
				}

				final TreePath[] paths = ((JTree)_event.getSource()).getSelectionPaths(); //_event.getPaths();
				chartInfo.updateChart( selectedObjects, pathsToList( paths ) );
			}
		} );
		
		tree.setCellRenderer( new TreeRenderer() );

		if (treeScrollPane == null)
		{
			treeScrollPane = new JScrollPane( tree );
		}
		else
		{
			treeScrollPane.getViewport().add( tree );
		}
	}

	private List pathsToList( 
		final TreePath[] _paths )
	{
		List list = new ArrayList();

		if (_paths != null)
		{
			for (int i = 0; i < _paths.length; i++)
			{
				list.add( ((GraphicNode)_paths[ i ].getLastPathComponent()).getNode().getValue() );
			}
		}

		return list;
	}

	private void setListFields( 
		final JList<TimeDistributedConstants> _list,
		final boolean _cost )
	{
		TimeDistributedConstants[] items = getFields( _cost );
		_list.setListData( items );
		_list.setVisibleRowCount( items.length );

		if (simple == true)
		{
			_list.setSelectionInterval( 0, items.length );
			_list.setEnabled( false ); // in simple mode, no selection allowed
		}
	}

	private void setTreeSelection( 
		final List _resources )
	{
		selecting = true;
		selectedResourcesOnTree.clear();
		selectedResourcesOnTree.addAll( _resources );
		selectedResourcesOnTree.remove( ResourceImpl.getUnassignedInstance() );

		int[] sel = new int[ selectedResourcesOnTree.size() ]; // if simple can only select 1
		Object resource;
		NodeModel nodeModel = chartInfo.getNodeModel();

		int topRow = Integer.MAX_VALUE;
		Object topResource = null;

		for (int i = 0; i < selectedResourcesOnTree.size(); i++)
		{
			resource = selectedResourcesOnTree.get( i );

			int row = nodeModel.getHierarchy().getIndexOfNode( nodeModel.search( resource ), false );
			sel[ i ] = row;

			if (row < topRow)
			{
				topRow = row;
				topResource = resource;
			}
		}

		tree.clearSelection();
		tree.setExpandsSelectedPaths( true );

		if (simple && (topResource != null))
		{
			tree.setSelectionRow( topRow );
			selectedResourcesOnTree.clear();
			selectedResourcesOnTree.add( topResource );
		}
		else
		{
			tree.setSelectionRows( sel );
		}

		tree.setExpandsSelectedPaths( true );
		tree.repaint();
		selecting = false;
	}

	public static class Workspace
		implements WorkspaceSetting
	{
		public Point getTracesViewPosition()
		{
			return tracesViewPosition;
		}

		public Point getTreeViewPosition()
		{
			return treeViewPosition;
		}

		public void setTracesViewPosition( 
			final Point _tracesViewPosition )
		{
			this.tracesViewPosition = _tracesViewPosition;
		}

		public void setTreeViewPosition( 
			final Point _treeViewPosition )
		{
			this.treeViewPosition = _treeViewPosition;
		}

		private static final long serialVersionUID = -8581691622116505516L;
		Point tracesViewPosition;
		Point treeViewPosition;
	}

	class ListRenderer
		extends JLabel
		implements ListCellRenderer
	{
		public ListRenderer()
		{
			// Don't paint behind the component
			setOpaque
			// Don't paint behind the component
			( true );
		}

		@Override
		public Component getListCellRendererComponent( 
			final JList _list,
			final Object _value, // value to display
			final int _index, // cell index
			final boolean _iss, // is selected
			final boolean _chf )
		{ // cell has focus?
			setText( (_value == null)
				? ""
				: _value.toString() );

			if (_iss)
			{
				Color color = ChartHelper.getColorForField( _value );
				setBackground( color );

				if ((color.getRed() + color.getGreen() + color.getBlue()) < 450) // draw dark with white foreground
				{
					setForeground( Color.white );
				}
				else
				{
					setForeground( _list.getForeground() );
				}
			}
			else
			{
				setBackground( _list.getBackground() );
				setForeground( _list.getForeground() );
			}

			// Set a border if the 
			//list item is selected
			if (_iss)
			{
				setBorder( BorderFactory.createLineBorder( Color.black, 1 ) );
			}
			else
			{
				setBorder( BorderFactory.createLineBorder( _list.getBackground(), 1 ) );
			}

			return this;
		}

		private static final long serialVersionUID = 1L;
	}

	//	 This class is a custom renderer based
	//	 on DefaultTreeCellRenderer
	class TreeRenderer
		extends DefaultTreeCellRenderer
	{
		@Override
		public Component getTreeCellRendererComponent( 
			final JTree _tree,
			final Object _value,
			final boolean _selected,
			final boolean _expanded,
			final boolean _leaf,
			final int _row,
			final boolean _hasFocus )
		{
			// Allow the original renderer to set up the label
			Component c = super.getTreeCellRendererComponent( _tree, _value, _selected, _expanded, _leaf, _row, _hasFocus );

			if (selectedResourcesFromTasks.contains( ((GraphicNode)_value).getNode().getValue() ))
			{
				setIcon( activeIcon );
			}

			return c;
		}

		private static final long serialVersionUID = 1L;
		private Icon activeIcon = IconManager.getIcon( "greenCircle" ); //$NON-NLS-1$
	}

	private static final long serialVersionUID = 5098599798868391983L;
	ChartInfo chartInfo;
	JCheckBox cumulative;
	JCheckBox histogram;

	//	JList traces;
	JCheckBox selectedOnTop;
	JList costTraces;
	JList tracesList;
	JList workTraces;
	JRadioButton cost;
	JRadioButton work;
	JScrollPane tracesScrollPane = null;
	JScrollPane treeScrollPane = null;
	JTree tree;
	List selectedObjects = new ArrayList();
	List selectedResourcesFromTasks = new ArrayList();
	List selectedResourcesOnTree = new ArrayList();
	TransformComboBox filterComboBox = null;
	boolean selecting = false;
	boolean simple;
}
