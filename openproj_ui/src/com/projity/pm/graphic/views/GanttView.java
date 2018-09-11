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

import com.projity.field.FieldContext;

import com.projity.graphic.configuration.BarStyles;
import com.projity.graphic.configuration.CellStyle;
import com.projity.graphic.configuration.GraphicConfiguration;
import com.projity.graphic.configuration.SpreadSheetFieldArray;

import com.projity.grouping.core.model.NodeModel;

import com.projity.help.HelpUtil;

import com.projity.menu.MenuActionConstants;
import com.projity.menu.MenuManager;

import com.projity.pm.graphic.frames.DocumentFrame;
import com.projity.pm.graphic.gantt.Gantt;
import com.projity.pm.graphic.model.cache.NodeModelCache;
import com.projity.pm.graphic.model.cache.NodeModelCacheFactory;
import com.projity.pm.graphic.model.cache.ReferenceNodeModelCache;
import com.projity.pm.graphic.spreadsheet.SpreadSheet;
import com.projity.pm.graphic.spreadsheet.SpreadSheetModel;
import com.projity.pm.graphic.spreadsheet.SpreadSheetUtils;
import com.projity.pm.graphic.timescale.CoordinatesConverter;
import com.projity.pm.graphic.timescale.ScaledScrollPane;
import com.projity.pm.graphic.views.synchro.Synchronizer;
import com.projity.pm.scheduling.ScheduleEvent;
import com.projity.pm.scheduling.ScheduleEventListener;
import com.projity.pm.task.Project;
import com.projity.pm.time.HasStartAndEnd;

import com.projity.strings.Messages;

import com.projity.undo.UndoController;

import com.projity.workspace.WorkspaceSetting;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 *
 */
public class GanttView
	extends SplittedView
	implements BaseView,
		ScheduleEventListener
{
	/**
	 * @param project
	 * @param manager
	 *
	 */
	public GanttView( 
		final DocumentFrame _documentFrame,
		final MenuManager _manager,
		final Synchronizer _synchronizer )
	{
		super( _synchronizer );
		this.documentFrame = _documentFrame;
		this.project = _documentFrame.getProject();
		HelpUtil.addDocHelp( this, "Gantt_Chart" );
		setNeedVoidBar( true );

		//setScaled(true);
	}

	public void activateEmptyRowHeader( 
		final boolean _activate )
	{
		ganttScrollPane.activateEmptyRowHeader( _activate );
	}

	/* (non-Javadoc)
	 * @see
	 */
	@Override
	public boolean canScrollToTask()
	{
		return true;
	}

	/* (non-Javadoc)
	 * @see
	 */
	@Override
	public boolean canZoomIn()
	{
		return coord.canZoomIn();
	}

	/* (non-Javadoc)
	 * @see
	 */
	@Override
	public boolean canZoomOut()
	{
		return coord.canZoomOut();
	}

	/* (non-Javadoc)
	 * @see
	 */
	@Override
	public void cleanUp()
	{
		super.cleanUp();
		coord.removeTimeScaleListener( ganttScrollPane );
		project.removeScheduleListener( this );
		spreadSheet.cleanUp();
		gantt.cleanUp();
		spreadSheet = null;
		gantt = null;
		baseLines = null;
		ganttScrollPane = null;

//		model = null;
		cache = null;
		coord = null;
		project = null;
		documentFrame = null;
		fieldContext = null;
		cellStyle = null;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.graphic.views.SplittedView#createLeftScrollPane()
	 */
	@Override
	protected JScrollPane createLeftScrollPane()
	{
		spreadSheet = new SpreadSheet()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCanSelectFieldArray()
			{
				if (project.isDatesPinned() == true) 
				{
					// don't allow changing field array if fields are pinned since many not relevant
					return false;
				}

				// TODO Auto-generated method stub
				return super.isCanSelectFieldArray();
			}
		};

		spreadSheet.setName( project.getName() );
		spreadSheet.setSpreadSheetCategory( spreadsheetCategory ); // for columns.  Must do first

		SpreadSheetFieldArray fields = getFields();

		if (project.getFieldArray() != null)
		{
			fields = project.getFieldArray();
		}

		spreadSheet.setCache( cache, fields, fields.getCellStyle(), fields.getActionList() );

		if (project.getFieldArray() != null)
		{
			spreadSheet.setFieldArrayWithWidths( fields );
		}

		((SpreadSheetModel)spreadSheet.getModel()).setFieldContext( fieldContext );
		project.removeScheduleListener( this ); // in case was already attached and recreating (applet)
		project.addScheduleListener( this );

		if (project.isReadOnly() == true)
		{
			spreadSheet.setReadOnly( true );
		}

		return SpreadSheetUtils.makeSpreadsheetScrollPane( spreadSheet );
	}

	/* (non-Javadoc)
	 * @see
	 */
	@Override
	protected JScrollPane createRightScrollPane()
	{
		gantt = new Gantt(project, "Gantt");
		gantt.setCache( cache );
		gantt.setBarStyles( (BarStyles)Dictionary.get( BarStyles.category, "standard" ) );
		ganttScrollPane = new ScaledScrollPane(gantt, coord, documentFrame, spreadSheet.getRowHeight());

		return ganttScrollPane;
	}

	/* (non-Javadoc)
	 * @see
	 */
	@Override
	public WorkspaceSetting createWorkspace( 
		final int _context )
	{
		final Workspace ws = new Workspace();
		ws.spreadSheet = spreadSheet.createWorkspace( _context );
		ws.scrollPane = ganttScrollPane.createWorkspace( _context );
		ws.dividerLocation = getDividerLocation();

		return ws;
	}

	/* (non-Javadoc)
	 * @see
	 */
	@Override
	public NodeModelCache getCache()
	{
		return cache;
	}

	//spreadsheet fields
	private static SpreadSheetFieldArray getFields()
	{
		//TODO don't hardcode
		return (SpreadSheetFieldArray)Dictionary.get( spreadsheetCategory, Messages.getString( "Spreadsheet.Task.entry" ) );
	}

	public Gantt getGantt()
	{
		return gantt;
	}

	/* (non-Javadoc)
	 * @see
	 */
	@Override
	public int getScale()
	{
		return coord.getTimescaleManager().getCurrentScaleIndex();
	}

	/* (non-Javadoc)
	 * @see
	 */
	@Override
	public SpreadSheet getSpreadSheet()
	{
		return spreadSheet;
	}

	/* (non-Javadoc)
	 * @see
	 */
	@Override
	public UndoController getUndoController()
	{
		return project.getNodeModelDataFactory().getUndoController();
	}

	/* (non-Javadoc)
	 * @see
	 */
	@Override
	public String getViewName()
	{
		return MenuActionConstants.ACTION_GANTT;
	}

	/* (non-Javadoc)
	 * @see
	 */
	@Override
	public boolean hasNormalMinWidth()
	{
		return true;
	}

	/**
	 *
	 * @param _cache
	 * @param _model
	 * @param _coord
	 */
	public void init( 
		final ReferenceNodeModelCache _cache,
		final NodeModel _model,
		final CoordinatesConverter _coord )
	{
		coord = _coord;
		cache = NodeModelCacheFactory.getInstance().createFilteredCache( (ReferenceNodeModelCache)_cache, getViewName(), null );

		fieldContext = new FieldContext();
		fieldContext.setLeftAssociation( true );

		/*cellStyle=new CellStyle(){
		        CellFormat cellProperties=new CellFormat();
		        public CellFormat getCellProperties(GraphicNode node){
		                        cellProperties.setBold(node.isSummary());
		                        cellProperties.setItalic(node.isAssignment());
		                        //cellProperties.setBackground((node.isAssignment())?"NORMAL_LIGHT_YELLOW":"NORMAL_YELLOW");
		                        cellProperties.setCompositeIcon(node.isComposite());
		                        return cellProperties;
		        }
		
		};*/
		super.init();
		updateHeight( project );
		updateSize();

		//sync the height of spreadsheet and gantt
		leftScrollPane.getViewport().addChangeListener( new ChangeListener()
		{
			private Dimension olddl = null;

			@Override
			public void stateChanged( 
				final ChangeEvent _event )
			{
				Dimension dl = leftScrollPane.getViewport().getViewSize();

				if (dl.equals( olddl ))
				{
					return;
				}

				olddl = dl;

//				Dimension dr=rightScrollPane.getViewport().getViewSize();
//				((Gantt)rightScrollPane.getViewport().getView()).setPreferredSize(new Dimension((int)dr.getWidth(),(int)dl.getHeight()));
//				rightScrollPane.getViewport().revalidate();
				((Gantt)rightScrollPane.getViewport().getView()).setPreferredSize( new Dimension(
						rightScrollPane.getViewport().getViewSize().width, dl.height) );
			}
		} );

//TODO automatic scrolling to add as an option
//		spreadSheet.getRowHeader().getSelectionModel().addListSelectionListener(new ListSelectionListener()
//		{
//			public void valueChanged(ListSelectionEvent e) 
//			{
//				if (!e.getValueIsAdjusting()&&spreadSheet.getRowHeader().getSelectedRowCount()==1)
//				{
//					List impls=spreadSheet.getSelectedNodesImpl();
//					if (impls.size()!=1) return;
//					Object impl=impls.get(0);
//					if (!(impl instanceof HasStartAndEnd)) return;
//					HasStartAndEnd interval=(HasStartAndEnd)impl;
//					gantt.scrollToTask(interval, true);
//				}
//			}
//		});
		
		leftScrollPane.getViewport().addMouseWheelListener( new MouseWheelListener()
		{
			@Override
			public void mouseWheelMoved( 
				final MouseWheelEvent _event )
			{
				Component component = gantt.getParent();

				if (component instanceof JViewport)
				{
					JViewport vp = (JViewport)component;
					Point p = vp.getViewPosition();
					int newY = p.y + (_event.getUnitsToScroll() * gantt.getRowHeight());

					if (newY > 0)
					{
						p.y = newY;
					}
					else
					{
						p.y = 0;
					}

					vp.setViewPosition( p );
				}
			}
		} );

		gantt.addMouseWheelListener( new MouseWheelListener()
		{
			@Override
			public void mouseWheelMoved( 
				final MouseWheelEvent _event )
			{
				Component component = gantt.getParent();

				if (component instanceof JViewport)
				{
					JViewport vp = (JViewport)component;
					Point p = vp.getViewPosition();
					int newY = p.y + (_event.getUnitsToScroll() * gantt.getRowHeight());

					if (newY > 0)
					{
						p.y = newY;
					}
					else
					{
						p.y = 0;
					}

					vp.setViewPosition( p );
				}
			}
		} );

		_cache.update();

		//Call this last to be sure everything is initialized
		//gantt.insertCacheData(); //useless?
	}

	/* (non-Javadoc)
	 * @see
	 */
	@Override
	public boolean isPrintable()
	{
		return true;
	}

	public boolean isTracking()
	{
		return tracking;
	}

	/* (non-Javadoc)
	 * @see
	 */
	@Override
	public void onActivate( 
		final boolean _activate )
	{
	}

	public void reinitialize()
	{ // applet
		createLeftScrollPane();
	}

	/* (non-Javadoc)
	 * @see
	 */
	@Override
	public void restoreWorkspace( 
		final WorkspaceSetting _w,
		final int _context )
	{
		Workspace ws = (Workspace)_w;
		spreadSheet.restoreWorkspace( ws.spreadSheet, _context );
		ganttScrollPane.restoreWorkspace( ws.scrollPane, _context );
		setDividerLocation( ws.dividerLocation );
	}

	/* (non-Javadoc)
	 * @see
	 */
	@Override
	public void scheduleChanged( 
		final ScheduleEvent _evt )
	{
		if (_evt.getType() == ScheduleEvent.SCHEDULE)
		{
			//gantt.updateSize(); //done throught cache
		}
		else if (_evt.getType() == ScheduleEvent.ACTUAL)
		{
			;
		}
		else if (_evt.getType() == ScheduleEvent.BASELINE)
		{
			updateHeight( _evt.getSnapshot(), _evt.isSaveSnapshot() );

			//Warning: listeners order is important.
			//This one must be before GanttModel one which calls updateAll after the height is setted
		}
	}

	/* (non-Javadoc)
	 * @see
	 */
	@Override
	public void scrollToTask()
	{
		List impls = spreadSheet.getSelectedNodesImpl();

		if (impls.size() == 0)
		{
			return;
		}

		Object impl = impls.get( 0 );

		if (!(impl instanceof HasStartAndEnd))
		{
			return;
		}

		HasStartAndEnd interval = (HasStartAndEnd)impl;
		gantt.scrollToTask( interval, false );
	}

	public void setBarStyles( 
		final String _styleName )
	{
		if (gantt == null)
		{
			return;
		}

		gantt.setBarStyles( (BarStyles)Dictionary.get( BarStyles.category, _styleName ) );
	}

	/**
	 *
	 * @param name
	 * @return old field array
	 */
	public ArrayList setColumns( 
		final String _name )
	{
		ArrayList old = spreadSheet.getFieldArray();
		setColumns( (ArrayList)Dictionary.get( spreadsheetCategory, Messages.getString( _name ) ) );

		return old;
	}

	public void setColumns( 
		final ArrayList _fields )
	{
		spreadSheet.setFieldArray( _fields );
	}

	public void setTracking( 
		final boolean _tracking )
	{
		this.tracking = _tracking;
		HelpUtil.addDocHelp( this, _tracking
			? "Tracking_Gantt_Chart"
			: "Gantt_Chart" );
	}

	/* (non-Javadoc)
	 * @see
	 */
	@Override
	public boolean showsResources()
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see
	 */
	@Override
	public boolean showsTasks()
	{
		return true;
	}

	public void updateHeight( 
		final Integer _snapshotId,
		final boolean _add )
	{
		if (_add)
		{
			baseLines.add( _snapshotId );
		}
		else
		{
			baseLines.remove( _snapshotId );
		}

		int num = (baseLines.size() == 0)
			? 0
			: (((Integer)baseLines.last()).intValue() + 1);
		int rowHeight = GraphicConfiguration.getInstance().getRowHeight() +
			(num * GraphicConfiguration.getInstance().getBaselineHeight());
		spreadSheet.setRowHeight( rowHeight );
		gantt.setRowHeight( rowHeight );
	}

	public void updateHeight( 
		final Project _project )
	{
		baseLines.clear();

		int rowHeight = _project.getRowHeight( baseLines );

//        for (Iterator i=project.getTaskOutlineIterator();i.hasNext();){
//            Task task=(Task)i.next();
//            int current=Snapshottable.CURRENT.intValue();
//            for (int s=0;s<Settings.numGanttBaselines();s++){
//                if (s==current) continue;
//                TaskSnapshot snapshot=(TaskSnapshot)task.getSnapshot(new Integer(s));
//                if (snapshot!=null) baseLines.add(new Integer(s));
//            }
//        }
//		int num=(baseLines.size()==0)?0:(((Integer)baseLines.last()).intValue()+1);
//		int rowHeight=GraphicConfiguration.getInstance().getRowHeight()
//				+num*GraphicConfiguration.getInstance().getBaselineHeight();
		spreadSheet.setRowHeight( rowHeight );
		gantt.setRowHeight( rowHeight );
	}

	/* (non-Javadoc)
	 * @see
	 */
	@Override
	public void updateSize()
	{
		gantt.updateSize();
	}

	/* (non-Javadoc)
	 * @see
	 */
	@Override
	public void zoomIn()
	{
		coord.zoomIn();
	}

	/* (non-Javadoc)
	 * @see
	 */
	@Override
	public void zoomOut()
	{
		coord.zoomOut();
	}

	public static class Workspace
		implements WorkspaceSetting
	{
		public final int getDividerLocation()
		{
			return dividerLocation;
		}

		public final WorkspaceSetting getScrollPane()
		{
			return scrollPane;
		}

		public final WorkspaceSetting getSpreadSheet()
		{
			return spreadSheet;
		}

		public final void setDividerLocation( 
			final int _dividerLocation )
		{
			dividerLocation = _dividerLocation;
		}

		public final void setScrollPane( 
			final WorkspaceSetting _scrollPane )
		{
			scrollPane = _scrollPane;
		}

		public final void setSpreadSheet( 
			final WorkspaceSetting _spreadSheet )
		{
			spreadSheet = _spreadSheet;
		}

		private static final long serialVersionUID = -407561451956813994L;
		private WorkspaceSetting scrollPane;
		private WorkspaceSetting spreadSheet;
		private int dividerLocation;
	}

	/**
	 *
	 */
	private static final long serialVersionUID = 514828655690086836L;
	public static final String spreadsheetCategory = taskSpreadsheetCategory;
	protected CoordinatesConverter coord;
	protected Gantt gantt;

	//	protected NodeModel model;
	protected NodeModelCache cache;
	CellStyle cellStyle;
	DocumentFrame documentFrame;
	FieldContext fieldContext;
	private Project project;
	protected ScaledScrollPane ganttScrollPane;
	protected SortedSet baseLines = new TreeSet();
	protected SpreadSheet spreadSheet;
	private boolean tracking = false;
}
