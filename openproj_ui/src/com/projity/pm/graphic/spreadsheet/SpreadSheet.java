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
package com.projity.pm.graphic.spreadsheet;

import com.projity.datatype.Hyperlink;

import com.projity.dialog.ResourceAdditionDialog;

import com.projity.field.Field;

import com.projity.graphic.configuration.ActionList;
import com.projity.graphic.configuration.CellStyle;
import com.projity.graphic.configuration.GraphicConfiguration;
import com.projity.graphic.configuration.shape.Colors;

import com.projity.grouping.core.Node;
import com.projity.grouping.core.NodeBridge;
import com.projity.grouping.core.NodeFactory;
import com.projity.grouping.core.NodeList;

import com.projity.help.HelpUtil;

import com.projity.job.Job;
import com.projity.job.JobRunnable;

import com.projity.menu.MenuActionConstants;

import com.projity.options.GeneralOption;

import com.projity.pm.graphic.ChangeAwareTextField;
import com.projity.pm.graphic.frames.GraphicManager;
import com.projity.pm.graphic.model.cache.GraphicNode;
import com.projity.pm.graphic.model.cache.NodeModelCache;
import com.projity.pm.graphic.spreadsheet.common.CommonSpreadSheet;
import com.projity.pm.graphic.spreadsheet.common.CommonSpreadSheetAction;
import com.projity.pm.graphic.spreadsheet.common.CommonSpreadSheetModel;
import com.projity.pm.graphic.spreadsheet.common.transfer.NodeListTransferHandler;
import com.projity.pm.graphic.spreadsheet.editor.SimpleComboBoxEditor;
import com.projity.pm.graphic.spreadsheet.renderer.CellUtility;
import com.projity.pm.graphic.spreadsheet.renderer.NameCellComponent;
import com.projity.pm.graphic.spreadsheet.selection.SpreadSheetListSelectionModel;
import com.projity.pm.graphic.spreadsheet.selection.SpreadSheetSelectionModel;
import com.projity.pm.graphic.spreadsheet.selection.event.HeaderMouseListener;
import com.projity.pm.resource.ResourceImpl;
import com.projity.pm.resource.ResourcePool;
import com.projity.pm.scheduling.ScheduleUtil;
import com.projity.pm.task.Project;

import com.projity.server.data.EnterpriseResourceData;
import com.projity.server.data.Serializer;

import com.projity.session.Session;
import com.projity.session.SessionFactory;

import com.projity.strings.Messages;

import com.projity.util.Alert;
import com.projity.util.BrowserControl;
import com.projity.util.Environment;

import org.apache.commons.collections.Closure;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;


/**
 *
 */
public class SpreadSheet
	extends CommonSpreadSheet
	implements Cloneable
{
	public SpreadSheet()
	{
		super();
		
		NodeListTransferHandler.registerWith( this );
	}

	private void addAction( 
		String _action,
		String _spreadSheetActionId,
		CommonSpreadSheetAction _spreadSheetAction )
	{
		if (_spreadSheetActionId.equals( _action ) == true)
		{
			actionMap.put( _spreadSheetActionId, _spreadSheetAction );
		}
	}

	private void addActions( 
		String[] _actions )
	{
//		System.out.println( "SpreadSheet " + spreadSheetCategory+", " + hashCode() + " addActions(" + dumpActions( actions ) 
//			+ ")" );
		NodeListTransferHandler handler = null;

		if (getTransferHandler() instanceof NodeListTransferHandler)
		{
			handler = (NodeListTransferHandler)getTransferHandler();
		}

		if (_actions != null)
		{
			for (int i = 0; i < _actions.length; i++)
			{
				String action = _actions[ i ];
				addAction( action, MenuActionConstants.ACTION_NEW, newAction );
				addAction( action, MenuActionConstants.ACTION_INDENT, indentAction );
				addAction( action, MenuActionConstants.ACTION_OUTDENT, outdentAction );

				if (handler != null)
				{
					addAction( action, MenuActionConstants.ACTION_CUT, handler.getNodeListCutAction() );
					addAction( action, MenuActionConstants.ACTION_COPY, handler.getNodeListCopyAction() );
					addAction( action, MenuActionConstants.ACTION_PASTE, handler.getNodeListPasteAction() );
				}

				addAction( action, MenuActionConstants.ACTION_DELETE, deleteAction );
				addAction( action, MenuActionConstants.ACTION_EXPAND, expandAction );
				addAction( action, MenuActionConstants.ACTION_COLLAPSE, collapseAction );

				if (Environment.isNoPodServer())
				{
					addAction( action, MenuActionConstants.ACTION_COMPLETE_AND_CLOSE, completeAndCloseAction );
				}
			}
		}
	}

	@Override
	public void cleanUp()
	{
		if (getModel() instanceof CommonSpreadSheetModel)
		{
			((CommonSpreadSheetModel)getModel()).getCache().removeNodeModelListener( this );
		}

		super.cleanUp();
	}

	public void clearActions()
	{
		actionMap = null;
		actionList = null;
		popup = null;
		((CommonSpreadSheetModel)getModel()).clearActions();
	}

	public void createDefaultColumnsFromModel( 
		ArrayList _fieldArray )
	{
		// Remove any current columns
		TableColumnModel cm = getColumnModel();

		while (cm.getColumnCount() > 0)
		{
			cm.removeColumn( cm.getColumn( 0 ) );
		}

		// Create new columns from the data model info
		int colCount = _fieldArray.size();

		for (int i = 0; i < colCount; i++)
		{
			TableColumn newColumn = new TableColumn(i);
			addColumn( newColumn );
		}

//		TableModel m = getModel();
//		if (m != null) {
//			// Remove any current columns
//			TableColumnModel cm = getColumnModel();
//			while (cm.getColumnCount() > 0) {
//				cm.removeColumn(cm.getColumn(0));
//			}
//
//			// Create new columns from the data model info
//			for (int i = 0; i < m.getColumnCount(); i++) {
//				TableColumn newColumn = new TableColumn(i);
//				addColumn(newColumn);
//			}
//		}
	}

	public void doClick( 
		int _row,
		int _col )
	{
		// override to treat cell clicks
	}

	public void doDoubleClick( 
		int _row,
		int _col )
	{
		GraphicManager.getInstance( this ).doInformationDialog( false );
	}

	// gui actions
	public void executeAction( 
		String _actionId )
	{
		CommonSpreadSheetAction action = getAction( _actionId );

		if (action == null)
		{
			System.out.println( "No action for " + _actionId );

			return;
		}

		action.setSpreadSheet( this );
		action.execute();
	}

	@Override
	protected void finalize()
	{
		System.out.println( "SpreadSheet.finalize()" + this );
	}

	public CommonSpreadSheetAction getAction( 
		String _actionId )
	{
		if (actionMap == null)
		{
			actionMap = new HashMap();
			addActions( getActionList() );
		}

		return (CommonSpreadSheetAction)actionMap.get( _actionId );
	}

	public String[] getActionList()
	{
		if (actionList == null)
		{
			actionList = ((SpreadSheetModel)getModel()).getActionList();
		}

		return actionList;
	}

	@Override
	public TableCellEditor getCellEditor( 
		int _row,
		int _column )
	{
		SpreadSheetModel model = (SpreadSheetModel)getModel();
		Field field = model.getFieldInColumn( _column + 1 );
		GraphicNode node = model.getNode( _row );

		if ((field != null) && (field.isDynamicOptions() || field.hasFilter()))
		{
			return new SimpleComboBoxEditor(new DefaultComboBoxModel(field.getOptions( node.getNode().getValue() )));
		}
		else
		{
			return super.getCellEditor( _row, _column );
		}
	}

	public SpreadSheetAction getCopyAction()
	{
		return copyAction;
	}

	public SpreadSheetAction getCutAction()
	{
		return cutAction;
	}

	public SpreadSheetAction getPasteAction()
	{
		return pasteAction;
	}

	public SpreadSheetPopupMenu getPopup()
	{
		if (popup == null)
		{
			popup = hasRowPopup()
				? new SpreadSheetPopupMenu(this)
				: null;
		}

		return popup;
	}

	// Actions on selected nodes
	public List getSelectedGraphicNodes()
	{
		return rowsToGraphicNodes( getSelectedRows() );
	}
	
	public boolean hasRowPopup()
	{
		getAction( null );

		return (actionMap != null) && (actionMap.size() > 0);
	}

	@Override
	protected void initListeners()
	{
		addKeyListener( new KeyAdapter()
			{ // TODO need to fix focus problems elsewhere for this to always work
			@Override
				public void keyPressed( 
					KeyEvent e )
				{
					int row = getSelectedRow();

					if (row < 0)
					{
						return;
					}

					CommonSpreadSheetModel model = (CommonSpreadSheetModel)getModel();

					if (e.getKeyCode() == KeyEvent.VK_F3)
					{
						GraphicManager.getInstance().doFind( SpreadSheet.this, null );
					}
					else if ((e.getKeyCode() == KeyEvent.VK_F) && (e.getModifiers() == KeyEvent.CTRL_MASK))
					{
						GraphicManager.getInstance().doFind( SpreadSheet.this, null );
					}
				}
			} );
	}

	@Override
	public boolean isCellEditable( 
		int _row,
		int _col )
	{
		if (CellUtility.isHidden( this, _row, _col ))
		{
			return false;
		}

		return super.isCellEditable( _row, _col );
	}

	public boolean isOnIcon( 
		MouseEvent _event )
	{
		Point p = _event.getPoint();
		int row = rowAtPoint( p );
		int col = columnAtPoint( p );
		Rectangle bounds = getCellRect( row, col, false );
		SpreadSheetModel model = (SpreadSheetModel)getModel();
		GraphicNode node = model.getNode( row );

		return NameCellComponent.isOnIcon( new Point((int)(p.getX() - bounds.getX()), (int)(p.getY() - bounds.getY())),
			bounds.getSize(), model.getCache().getLevel( node ) );
	}

	public boolean isOnText( 
		MouseEvent _event )
	{
		Point p = _event.getPoint();
		int row = rowAtPoint( p );
		int col = columnAtPoint( p );
		Rectangle bounds = getCellRect( row, col, false );
		SpreadSheetModel model = (SpreadSheetModel)getModel();
		GraphicNode node = model.getNode( row );

		return NameCellComponent.isOnText( new Point((int)(p.getX() - bounds.getX()), (int)(p.getY() - bounds.getY())),
			bounds.getSize(), model.getCache().getLevel( node ) );
	}

	public boolean isReadOnly()
	{
		return ((SpreadSheetModel)getModel()).isReadOnly();
	}

	private void makeCustomTableHeader( 
		final TableColumnModel _columnModel )
	{
		final JTableHeader h = new JTableHeader( _columnModel )
		{
			@Override
			public String getToolTipText( 
				final MouseEvent _event )
			{
				if (isHasColumnHeaderPopup() == true)
				{
					final int col = columnAtPoint( _event.getPoint() );
					final Field field = ((SpreadSheetModel)getModel()).getFieldInNonTranslatedColumn( col + 1 );

					if (field != null)
					{
						return "<html>" + field.getName() + "<br>" +
						Messages.getString( "Text.rightClickToInsertRemoveColumns" ) + "</html>";
					}
				}

				return super.getToolTipText( _event );
			}
		};

		setTableHeader( h );
	}

	// init actions
	public final CommonSpreadSheetAction prepareAction( 
		final String _actionId )
	{
		final CommonSpreadSheetAction action = getAction( _actionId );
		action.setSpreadSheet( this );

		return action;
	}

	@Override
	public void resizeAndRepaintHeader()
	{
		final JTableHeader header = getTableHeader();
		final SpreadSheetColumnModel tm = ((SpreadSheetColumnModel)getColumnModel());
		final int colWidth = tm.getColWidth(); // tm.getTotalColumnWidth(); //Hack,
										 // colWidth isn't enough why?

		header.setPreferredSize( new Dimension( colWidth, header.getPreferredSize().height ) );
		header.resizeAndRepaint();
	}

	public List rowsToGraphicNodes( 
		int[] _rows )
	{
		if ((_rows == null) || (_rows.length == 0))
		{
			return new LinkedList();
		}

		NodeModelCache cache = ((SpreadSheetModel)getModel()).getCache();

		return cache.getElementsAt( _rows );
	}

	public void setActions( 
		String[] _actions )
	{
		//replace default actions
		actionList = _actions;

		if (actionMap == null)
		{
			actionMap = new HashMap();
		}
		else
		{
			actionMap.clear();
		}

		addActions( _actions );
	}

	public void setActions( 
		String _actions )
	{
		addActions( CommonSpreadSheetModel.convertActions( _actions ) );
	}

	public void setCache( 
		NodeModelCache _cache,
		ArrayList<Field> _fieldArray,
		CellStyle _cellStyle,
		ActionList _actionList )
	{
		// if (getCache()!=null) getCache().close();
		if (getCache() != null)
		{
			getCache().getReference().close(); // deepClose
		}

		SpreadSheetColumnModel colModel;
		TableColumnModel oldColModel = getColumnModel();

		if ((oldColModel != null) && oldColModel instanceof SpreadSheetColumnModel)
		{
			if (((SpreadSheetColumnModel)oldColModel).getFieldArray() == _fieldArray)
			{
				colModel = (SpreadSheetColumnModel)oldColModel;
			}
			else
			{
				colModel = new SpreadSheetColumnModel( _fieldArray );
			}
		}
		else
		{
			colModel = new SpreadSheetColumnModel( _fieldArray );
		}

		setModel( new SpreadSheetModel( _cache, colModel, _cellStyle, _actionList ), (colModel == oldColModel)
			? null
			: colModel );
	}

	@Override
	public void setFieldArray( 
		ArrayList _fieldArray )
	{
		((SpreadSheetColumnModel)getColumnModel()).setFieldArray( _fieldArray );
		createDefaultColumnsFromModel( _fieldArray );
		resizeAndRepaintHeader();
	}

	public void setModel( 
		SpreadSheetModel _spreadSheetModel,
		SpreadSheetColumnModel _spreadSheetColumnModel )
	{
		makeCustomTableHeader( _spreadSheetColumnModel );

		TableModel oldModel = getModel();
		setModel( _spreadSheetModel );

		if (_spreadSheetColumnModel != null)
		{
			//System.out.println("creating new ColModel");
			setColumnModel( _spreadSheetColumnModel );

			selection = new SpreadSheetSelectionModel(this);
			selection.setRowSelection( new SpreadSheetListSelectionModel(selection, true) );
			selection.setColumnSelection( new SpreadSheetListSelectionModel(selection, false) );
			setSelectionModel( selection.getRowSelection() );
			createDefaultColumnsFromModel( _spreadSheetModel.getFieldArray() ); //Consume memory
			getColumnModel().setSelectionModel( selection.getColumnSelection() );
		}

		registerEditors(); //Consume memory
		initRowHeader( _spreadSheetModel );
		initModel();
		initListeners();

		GraphicConfiguration config = GraphicConfiguration.getInstance();

		//fix for substance
		setTableHeader
		//fix for substance
		( createDefaultTableHeader() );

		JTableHeader header = getTableHeader();
		header.setPreferredSize( new Dimension((int)header.getPreferredSize().getWidth(), config.getColumnHeaderHeight()) );
		header.addMouseListener( new HeaderMouseListener(this) );

		//add key listener
		addKeyListener//add key listener
		( new KeyAdapter()
			{
				@Override
				public void keyReleased( 
					KeyEvent e )
				{
					if (e.getKeyCode() == KeyEvent.VK_INSERT)
					{
						GraphicManager.getInstance().doInsertTask();
						e.consume();
					}
				}

				@Override
				public void keyTyped( 
					KeyEvent e )
				{
					if ((char)e.getKeyChar() == KeyEvent.VK_DELETE)
					{
						GraphicManager.getInstance().getCurrentFrame().doDelete();
						e.consume();
					}
				}
			} );

		addMouseListener( new MouseAdapter()
			{
	//			Cursor oldCursor = null;
	//			public void mouseEntered(MouseEvent e) {
	//				Point p = e.getPoint();
	//				int col = columnAtPoint(p);
	//				Field field = ((SpreadSheetModel) getModel()).getFieldInNonTranslatedColumn(col + 1);
	//				System.out.println("mouse entered field " + field);
	//				if (field != null && field.isHyperlink()) {
	//					oldCursor = getCursor();
	//					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	//					System.out.println("setting new cursor to " + Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) + " old is " + oldCursor);
	//				} else
	//					super.mouseEntered(e);
	//
	//			}
	//
	//			public void mouseExited(MouseEvent e) {
	//				Point p = e.getPoint();
	//				int col = columnAtPoint(p);
	//				Field field = ((SpreadSheetModel) getModel()).getFieldInNonTranslatedColumn(col + 1);
	//				System.out.println("mouse exited field " + field);
	//				if (field != null && field.isHyperlink()) {
	//					setCursor(oldCursor);
	//					System.out.println("setting old cursor to " + oldCursor);
	//					e.consume();
	//				} else
	//					super.mouseEntered(e);
	//			}
			@Override
				public void mousePressed( 
					MouseEvent e )
				{ // changed to mousePressed instead of mouseClicked() for snappier handling 17/5/04 hk

					Point p = e.getPoint();
					int row = rowAtPoint( p );
					int col = columnAtPoint( p );
					SpreadSheetPopupMenu popup = getPopup();

					if (SwingUtilities.isLeftMouseButton( e ))
					{
						SpreadSheetColumnModel columnModel = (SpreadSheetColumnModel)getColumnModel();
						Field field = ((SpreadSheetModel)getModel()).getFieldInNonTranslatedColumn( col + 1 );
						SpreadSheetModel model = (SpreadSheetModel)getModel();

						if (field.isNameField())
						{
							// if (col == columnModel.getNameIndex()) {
							GraphicNode node = model.getNode( row );

							if (isOnIcon( e ))
							{
								if (model.getCellProperties( node ).isCompositeIcon())
								{
									finishCurrentOperations();
									selection.getRowSelection().clearSelection();

									boolean change = true;

									if (!node.isFetched()) // for subprojects
									{
										change = node.fetch();
									}

									if (change)
									{
										model.changeCollapsedState( row );
									}

									e.consume(); // prevent dbl click treatment below

									// because editor may have already been
									// installed we
									// have to update its collapsed state
									// updateNameCellEditor(node);
								}
							}
						}
						else if ((field != null) && field.isHyperlink())
						{
							//change to editable
							e.consume(); // prevent dbl click treatment below

							if (e.getClickCount() == 2)
							{
								String[] values = ((String)model.getValueAt( row, col + 1 )).split( "\\@\\|\\@\\|\\@" );
								Hyperlink link = new Hyperlink(values[ 1 ], values[ 0 ]);
								link.invoke();
							}
						}

						if (!e.isConsumed())
						{
							if (e.getClickCount() == 2) // if above code didn't treat and is dbl click
							{
								doDoubleClick( row, col );
							}
							else
							{
								doClick( row, col );
							}
						}
					}
					else if ((popup != null) && SwingUtilities.isRightMouseButton( e ))
					{ // e.isPopupTrigger() can be used too
					  //					selection.getRowSelection().clearSelection();
					  //					selection.getRowSelection().addSelectionInterval(row, row);
						popup.setRow( row );
						popup.setCol( col );
						popup.show( SpreadSheet.this, e.getX(), e.getY() );
					}
				}
			} );

		if ((oldModel != _spreadSheetModel) && oldModel instanceof CommonSpreadSheetModel)
		{
			((CommonSpreadSheetModel)getModel()).getCache().removeNodeModelListener( this );
		}

		_spreadSheetModel.getCache().addNodeModelListener( this );

//		getColumnModel().addColumnModelListener(new TableColumnModelListener(){
//			public void columnAdded(TableColumnModelEvent e) {
//				// TODO Auto-generated method stub
//
//			}
//			public void columnMarginChanged(ChangeEvent e) {
//				// TODO Auto-generated method stub
//
//			}
//			public void columnMoved(TableColumnModelEvent e) {
//				// TODO Auto-generated method stub
//
//			}
//			public void columnRemoved(TableColumnModelEvent e) {
//				// TODO Auto-generated method stub
//
//			}
//			public void columnSelectionChanged(ListSelectionEvent e) {
//				System.out.println(((e.getValueIsAdjusting())?"lse=":"LSE=")+e.getFirstIndex()+", "+e.getLastIndex());
//				SpreadSheet.this.revalidate();
//				//SpreadSheet.this.paintImmediately(0, 0, getWidth(), GraphicConfiguration.getInstance().getColumnHeaderHeight());
//			}
//		});
	}

	public void setReadOnly( 
		final boolean _readOnly )
	{
		((SpreadSheetModel)getModel()).setReadOnly( _readOnly );
	}

	public void updateNameCellEditor( 
		GraphicNode _node )
	{
		SpreadSheetColumnModel columnModel = (SpreadSheetColumnModel)getColumnModel();

		// if (isEditing() && getEditingColumn() == columnModel.getNameIndex()
		// && editorComp != null) {
		if (isEditing() && (editorComp != null) &&
				((SpreadSheetModel)getModel()).getFieldInColumn( getEditingColumn() + 1 ).isNameField())
		{
			NameCellComponent c = (NameCellComponent)editorComp;
			SpreadSheetModel model = (SpreadSheetModel)getModel();

			// GraphicNode node = model.getNode(row);
			if (model.getCellProperties( _node ).isCompositeIcon())
			{
				c.setCollapsed( _node.isCollapsed() );
			}
		}
	}

	public abstract static class SpreadSheetAction
		extends AbstractAction
		implements Closure,
			CommonSpreadSheetAction
	{
		public SpreadSheetAction( 
			String _id,
			SpreadSheet _spreadSheet )
		{
			super(Messages.getString( _id ));
			this.spreadSheet = _spreadSheet;
		}

		@Override
		public void actionPerformed( 
			ActionEvent _X )
		{
			execute();
		}

		public void doPreShow( 
			JMenuItem _X )
		{
		}

		@Override
		public void execute( 
			Object _X )
		{
			executeFirst();
			execute();
		}

		@Override
		public abstract void execute();

		public void executeFirst()
		{
			rows = spreadSheet.finishCurrentOperations();
		}

		public NodeModelCache getCache()
		{
			return ((SpreadSheetModel)spreadSheet.getModel()).getCache();
		}

		public List getSelected()
		{
			return spreadSheet.rowsToGraphicNodes( (rows == null)
				? spreadSheet.getSelectedRows()
				: rows );
		}

		@Override
		public CommonSpreadSheet getSpreadSheet()
		{
			return spreadSheet;
		}

		@Override
		public void setSpreadSheet( 
			final CommonSpreadSheet _spreadSheet )
		{
			spreadSheet = (SpreadSheet)_spreadSheet;
		}

		protected SpreadSheet spreadSheet;
		protected int[] rows;
	}

	private static final long serialVersionUID = 5958334223191182318L;
	private Map actionMap = null;
	protected SpreadSheetAction collapseAction = 
		new SpreadSheetAction( "Spreadsheet.Action.collapse", this )
	{
		@Override
		public void execute()
		{
			finishCurrentOperations();
			getCache().expandNodes( getSelected(), false );
		}
	};

	protected SpreadSheetAction completeAndCloseAction = 
		new SpreadSheetAction( "Spreadsheet.Action.completeAndClose", this )
	{
		@Override
		public void execute()
		{
			finishCurrentOperations();

			Project project = (Project)getCache().getModel().getDocument();
			ScheduleUtil.completeAndClose( NodeList.filterNodeList( getSelectedNodes(), false ), project );
		}

		@Override
		public void doPreShow( 
			JMenuItem _item )
		{
			Project project = (Project)getCache().getModel().getDocument();
			String text = project.isTimesheetClosed()
				? Messages.getString( "Spreadsheet.Action.completeAndClose" )
				: Messages.getString( "Spreadsheet.Action.complete" );
			_item.setText( text );
		}
	};

	protected SpreadSheetAction copyAction = 
		new SpreadSheetAction("Spreadsheet.Action.copy", this)
	{
		/**
		 *
		 */
		private static final long serialVersionUID = -7593036949653490043L;

		@Override
		public void execute()
		{
			execute( getSelectedNodes() );
		}

		@Override
		public void execute( 
			Object object )
		{
			if ((object != null) && object instanceof List)
			{
				finishCurrentOperations();

				List nodes = (List)object;
				executeFirst();
				getCache().copyNodes( nodes );
			}
		}
	};

	protected SpreadSheetAction cutAction = 
		new SpreadSheetAction("Spreadsheet.Action.cut", this)
	{
		private static final long serialVersionUID = -7928292866527615772L;

		@Override
		public void execute()
		{
			finishCurrentOperations();
			execute( getSelectedRows() );
		}

		@Override
		public void execute( 
			Object object )
		{
			if ((object != null) && object instanceof List)
			{
				finishCurrentOperations();

				List nodes = getSelectedCuttableRows( (List)object );

				if (nodes.isEmpty())
				{
					return;
				}

				executeFirst();
				getCache().cutNodes( nodes );
			}
		}
	};

	protected SpreadSheetAction deleteAction = 
		new SpreadSheetAction("Spreadsheet.Action.delete", this)
	{
		private static final long serialVersionUID = 1561847977122331970L;

		@Override
		public void execute()
		{
			finishCurrentOperations();

			List l = getSelectedDeletableRows();

			if (l.isEmpty() == true)
			{
				return;
			}

			if (!GeneralOption.getInstance().isConfirmDeletes() ||
					Alert.okCancel( Messages.getString( "Message.confirmDeleteRows" ) ))
			{
				getCache().deleteNodes( l );
			}
		}
	};

	protected SpreadSheetAction expandAction = 
		new SpreadSheetAction("Spreadsheet.Action.expand", this)
	{
		@Override
		public void execute()
		{
			finishCurrentOperations();
			getCache().expandNodes( getSelected(), true );
		}
	};

	protected SpreadSheetAction indentAction = 
		new SpreadSheetAction("Spreadsheet.Action.indent", this)
	{
		@Override
		public void execute()
		{
			finishCurrentOperations();
			getCache().indentNodes( getSelected() );
		}
	};

	protected SpreadSheetAction newAction = 
		new SpreadSheetAction("Spreadsheet.Action.new", this)
	{
		@Override
		public void execute()
		{
			List nodes = getSelected();

			if ((nodes == null) || (nodes.size() == 0))
			{
				int row = getCurrentRow();

				if (row == -1)
				{
					return;
				}

				getCache().newNode( (GraphicNode)getCache().getElementAt( row ) );
			}
			else
			{
				getCache().newNode( (GraphicNode)nodes.get( nodes.size() - 1 ) );
			}
		}
	};

	//will be used later
	protected SpreadSheetAction newResourceAction = 
		new SpreadSheetAction( "Spreadsheet.Action.new", this )
	{
		@Override
		public void execute()
		{
			List nodes = getSelected();
			final ResourcePool resourcePool = (ResourcePool)getCache().getModel().getDataFactory();
			Project project = (Project)resourcePool.getProjects().get( 0 );

			if ((nodes == null) || (nodes.size() == 0))
			{
				return;
			}

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
				true
			} );
			
			job.addSwingRunnable( 
				new JobRunnable( "Local: addNodes" )
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
							ResourceAdditionDialog.Form form = (ResourceAdditionDialog.Form)obj;
							List nodes = new ArrayList();

							for (Iterator i = form.getSelectedResources().iterator(); i.hasNext();)
							{
								try
								{
									nodes.add( NodeFactory.getInstance().createNode( Serializer.deserializeResourceAndAddToPool( 
										(EnterpriseResourceData)i.next(), resourcePool, null ) ) );
								}
								catch (IOException e)
								{
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								catch (ClassNotFoundException e)
								{
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

							getCache().addNodes( ((GraphicNode)getSelected().get( 0 )).getNode(), nodes );
							getCache().update();
						}
					};

					ResourceAdditionDialog.Form form = new ResourceAdditionDialog.Form();

					try
					{
						List resources = (List)SessionFactory.call( SessionFactory.getInstance().getSession( false ),
								"retrieveResourceDescriptors", null, null );
						HashMap resourceMap = new HashMap();

						for (Iterator i = resources.iterator(); i.hasNext();)
						{
							EnterpriseResourceData data = (EnterpriseResourceData)i.next();
							resourceMap.put( new Long(data.getUniqueId()), data );
						}

						List currentResources = resourcePool.getResourceList();

						for (Iterator i = currentResources.iterator(); i.hasNext();)
						{
							ResourceImpl resource = (ResourceImpl)i.next();
							Long key = new Long(resource.getUniqueId());

							if (resourceMap.containsKey( key ))
							{
								resourceMap.remove( key );
							}
						}

						form.getSelectedResources().addAll( resourceMap.values() );
					}
					catch (Exception e)
					{
						// Ignore
					}

					ResourceAdditionDialog.getInstance( (JFrame)SwingUtilities.getRoot( SpreadSheet.this ), form )
						.execute( setter, getter );

					return null;
				}
			} );
			session.schedule( job );
		}
	};

	protected SpreadSheetAction outdentAction = 
		new SpreadSheetAction("Spreadsheet.Action.outdent", this)
	{
		@Override
		public void execute()
		{
			finishCurrentOperations();
			getCache().outdentNodes( getSelected() );
		}
	};

	protected SpreadSheetAction pasteAction = 
		new SpreadSheetAction("Spreadsheet.Action.paste", this)
	{
		private static final long serialVersionUID = 5904764895696983803L;

		@Override
		public void execute()
		{
			execute( getSelectedNodes() );
		}

		@Override
		public void execute( 
			Object object )
		{
			if ((object != null) && object instanceof List)
			{
				finishCurrentOperations();

				List selectedNodes = getSelectedNodes();
				Node parent = null;
				int position = 0;

				if (selectedNodes.size() > 0)
				{
					Node node = (Node)selectedNodes.get( 0 );
					parent = (Node)node.getParent();
					position = ((NodeBridge)parent).getIndex( node );
				}

				List nodes = (List)object;
				executeFirst();
				spreadSheet.clearSelection();
				getCache().pasteNodes( parent, nodes, position );

//				if (nodes.size() > 0) {
//					int row = ((SpreadSheetModel) spreadSheet.getModel()).findGraphicNodeRow(spreadSheet.getCache().getGraphicNode(nodes.get(0)));
//					changeSelection(row, 0, false, false);
//					if (nodes.size() > 1)
//						changeSelection(row + nodes.size() - 1, getColumnCount(), false, true);
//				}
			}
		}
	};

	protected SpreadSheetPopupMenu popup = null;
	private String[] actionList = null;

//	public static final String INDENT = "Action.Indent";
//	public static final String OUTDENT = "Action.Outdent";
//	public static final String NEW = "Action.New";
//	public static final String DELETE = "Action.Delete";
//	public static final String CUT = "Action.Cut";
//	public static final String COPY = "Action.Copy";
//	public static final String PASTE = "Action.Paste";

//	public static final int INDENT = 0;
//
//	public static final int OUTDENT = 1;
//
//	public static final int NEW = 2;
//
//	public static final int DELETE = 3;
//
//	public static final int CUT = 4;
//
//
//	public static final int COPY = 5;
//
//	public static final int PASTE = 6;
//    public void columnSelectionChanged(ListSelectionEvent e) {
//		System.out.println("JTable: "+((e.getValueIsAdjusting())?"lse=":"LSE=")+e.getFirstIndex()+", "+e.getLastIndex());
//    	super.columnSelectionChanged(e);
//    }
	
	/*
	 * public SpreadSheetPopupMenu getPopup() { return popup; } public void
	 * setPopup(SpreadSheetPopupMenu popup) { this.popup = popup; }
	 */

//	private static final int[] DEFAULT_POPUP_OPTIONS = new int[] {INDENT,OUTDENT,NEW,DELETE,CUT,COPY,PASTE};
//	private int[] popupActions = DEFAULT_POPUP_OPTIONS;
//	public final void setPopupActions(int[] popupActions) {
//		this.popupActions = popupActions;
//	}

//	public boolean supportsAction(int option) {
//		if (popupActions == null)
//			return false;
//		for (int i = 0; i<popupActions.length; i++) {
//			if (popupActions[i] == option)
//				return true;
//		}
//		return false;
//	}
//	public boolean hasRowPopup() {
//		return popupActions != null && popupActions.length > 0;
//	}

//	private static String dumpActions(String[] actions){
//		if (actions==null) return null;
//		StringBuffer sb=new StringBuffer();
//		for (int i=0;i<actions.length;i++){
//			sb.append(actions[i]).append(',');
//		}
//		return sb.toString();
//	}
}
