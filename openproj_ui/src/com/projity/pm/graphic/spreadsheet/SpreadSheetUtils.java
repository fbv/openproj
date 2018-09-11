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

import com.projity.configuration.Dictionary;
import com.projity.configuration.FieldDictionary;

import com.projity.field.Field;
import com.projity.field.FieldContext;

import com.projity.graphic.configuration.SpreadSheetCategories;
import com.projity.graphic.configuration.SpreadSheetFieldArray;

import com.projity.grouping.core.Node;
import com.projity.grouping.core.model.NodeModel;
import com.projity.grouping.core.model.NodeModelDataFactory;
import com.projity.grouping.core.model.NodeModelFactory;
import com.projity.grouping.core.model.NodeModelUtil;
import com.projity.grouping.core.transform.ViewTransformer;
import com.projity.grouping.core.transform.filtering.BelongsToCollectionFilter;
import com.projity.grouping.core.transform.filtering.NodeFilter;

import com.projity.pm.graphic.frames.DocumentFrame;
import com.projity.pm.graphic.model.cache.GraphicNode;
import com.projity.pm.graphic.model.cache.NodeModelCache;
import com.projity.pm.graphic.model.cache.NodeModelCacheFactory;
import com.projity.pm.graphic.model.cache.ReferenceNodeModelCache;
import com.projity.pm.graphic.model.transform.NodeCacheTransformer;
import com.projity.pm.graphic.views.UsageDetailView;

import com.projity.strings.Messages;

import java.awt.Dimension;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Helper methods for working with spreadsheets
 */
public class SpreadSheetUtils
{
	/** Refresh the contents of a collection based spreadsheet
	 * @param ss
	 * @param collection
	 * @param document
	 * @param viewId
	 * @param spreadSheetCategory
	 * @param spreadSheetId
	 * @param leftAssociation
	 * @param nbVoidNodes TODO
	 */
	public static void createCollectionSpreadSheet( 
		final SpreadSheet _ss,
		final Collection _collection,
		final String _viewId,
		final String _spreadSheetCategory,
		final String _spreadSheetId,
		final boolean _leftAssociation,
		final NodeModelDataFactory _dataFactory,
		final int _nbVoidNodes 
//		,boolean local
//		,boolean master 
	)
	{
		NodeModel nodeModel = _dataFactory.createNodeModel( _collection );

//    	nodeModel.setLocal(local);
//    	nodeModel.setMaster(master);
		nodeModel.getHierarchy().setNbEndVoidNodes( _nbVoidNodes );

		ReferenceNodeModelCache refCache = NodeModelCacheFactory.getInstance().createReferenceCache( nodeModel, /*document*/
			null, ((_leftAssociation)
				? NodeModelCache.TASK_TYPE
				: NodeModelCache.RESOURCE_TYPE) | NodeModelCache.ASSIGNMENT_TYPE );
		NodeModelCache cache = NodeModelCacheFactory.getInstance().createFilteredCache( refCache, Messages.getString( _viewId ), 
			null );
		setFieldsAndContext( _ss, cache, _spreadSheetCategory, _spreadSheetId, _leftAssociation );
	}

	/**
	* This one doesn't recreate the cache and all its associated objects since they already exist in the main referenceNodeModelCache.
	* It just applies a filter. (a simplified version of SelectionFilter used by UsageDetail)
	 * @param nbVoidNodes TODO
	 * @param popupActions TODO
	*/
	public static SpreadSheet createFilteredSpreadsheet( 
		final DocumentFrame _df,
		final boolean _task // if task based
	,
		final String _viewId,
		final String _spreadSheetCategory,
		final String _spreadSheetId,
		final boolean _leftAssociation,
		final String[] _actionList )
	{
		NodeModelCache cache = _df.createCache( _task, Messages.getString( _viewId ) );
		cache.update();

		return createFilteredSpreadsheet( cache, _spreadSheetCategory, _spreadSheetId, _leftAssociation, /*nbVoidNodes,*/
			_actionList );
	}

	public static SpreadSheet createFilteredSpreadsheet( 
		final NodeModelCache _cache,
		final String _spreadSheetCategory,
		final String _spreadSheetId,
		final boolean _leftAssociation,
		final String[] _actionList )
	{
		SpreadSheet ss = new SpreadSheet();
		ss.setSpreadSheetCategory( _leftAssociation
			? UsageDetailView.taskAssignmentSpreadsheetCategory
			: UsageDetailView.resourceAssignmentSpreadsheetCategory );

		//cache.getModel().getHierarchy().setNbEndVoidNodes(nbVoidNodes);
		setFieldsAndContext
		//cache.getModel().getHierarchy().setNbEndVoidNodes(nbVoidNodes);
		( ss, _cache, _spreadSheetCategory, _spreadSheetId, _leftAssociation );

		return ss;
	}

	public static Field getFieldInColumn( 
		final int _col,
		final SpreadSheetColumnModel _colModel )
	{
		return _colModel.getFieldInColumn( _col );
	}

	public static List getFieldsForCategory( 
		final String _category )
	{
		if (_category.equals( SpreadSheetCategories.projectSpreadsheetCategory ))
		{
			return FieldDictionary.getInstance().getProjectFields();
		}
		else if (_category.equals( SpreadSheetCategories.taskSpreadsheetCategory ))
		{
			return FieldDictionary.getInstance().getTaskFields();
		}
		else if (_category.equals( SpreadSheetCategories.resourceSpreadsheetCategory ))
		{
			return FieldDictionary.getInstance().getResourceFields();
		}
		else if (_category.equals( SpreadSheetCategories.taskAssignmentSpreadsheetCategory ) ||
				_category.equals( SpreadSheetCategories.resourceAssignmentSpreadsheetCategory ))
		{
			return FieldDictionary.getInstance().getAssignmentFields();
		}
		else if (_category.equals( SpreadSheetCategories.dependencySpreadsheetCategory ))
		{
			return FieldDictionary.getInstance().getDependencyFields();
		}

		//TODO resource usage should use resource and assignment fields, and task usage should do task and assignment fields
		return null;
	}

	public static GraphicNode getNodeFromCacheRow( 
		final int _row,
		final int _rowMultiple,
		final NodeModelCache _cache )
	{
		return (GraphicNode)_cache.getElementAt( _row / _rowMultiple );
	}

	public static Node getNodeInRow( 
		final int _row,
		final int _rowMultiple,
		final NodeModelCache _cache )
	{
		GraphicNode gnode = getNodeFromCacheRow( _row, _rowMultiple, _cache );

		if (gnode == null)
		{
			return null;
		}

		return gnode.getNode();
	}

	public static Object getValueAt( 
		final int _row,
		final int _col,
		final int _rowMultiple,
		final NodeModelCache _cache,
		final SpreadSheetColumnModel _colModel,
		final FieldContext _context )
	{
		Node node = getNodeInRow( _row, _rowMultiple, _cache );

		return getValueAt( node, _col, _cache, _colModel, _context );
	}

	public static Object getValueAt( 
		final Node _node,
		final int _col,
		final NodeModelCache _cache,
		final SpreadSheetColumnModel _colModel,
		final FieldContext _context )
	{
		if (_node.isVoid())
		{
			return (_col == 0)
			? ""
			: null;
		}

		// TODO change when Field supports void
		return NodeModelUtil.getValue( getFieldInColumn( _col, _colModel ), _node, _cache.getWalkersModel(), _context );
	}

	/** put a spreadsheet in a scroll pane and fix problems with scrolling header
	 *
	 * @param spreadSheet
	 * @return
	 */
	public static JScrollPane makeSpreadsheetScrollPane( 
		final SpreadSheet _spreadSheet )
	{
		final JScrollPane spreadSheetScrollPane = new JScrollPane(_spreadSheet);

		//a fix to resize column header when viewport size changes
		spreadSheetScrollPane.getViewport().addChangeListener( new ChangeListener()
			{
				private Dimension olddmain = null;

				public void stateChanged( 
					final ChangeEvent _e )
				{
					//				Dimension dmain=spreadSheetScrollPane.getViewport().getViewSize();
					//				if (dmain.equals(olddmain)) return;
					//				olddmain=dmain;
					//				System.out.println("pref size #1="+spreadSheetScrollPane.getColumnHeader().getPreferredSize());
					//				spreadSheetScrollPane.getColumnHeader().setPreferredSize(new Dimension(dmain.width,spreadSheetScrollPane.getColumnHeader().getPreferredSize().height));
					//				System.out.println("pref size #2="+spreadSheetScrollPane.getColumnHeader().getPreferredSize());
					//				spreadSheetScrollPane.getColumnHeader().revalidate();
					//				System.out.println("pref size #3="+spreadSheetScrollPane.getColumnHeader().getPreferredSize());
					//

					//				Dimension d=spreadSheetScrollPane.getColumnHeader().getPreferredSize();
					//				d.setSize(dmain.getWidth(),d.getHeight());
					//				spreadSheetScrollPane.getColumnHeader().revalidate();
				}
			} );

		return spreadSheetScrollPane;
	}

	public static void setFieldsAndContext( 
		final SpreadSheet _ss,
		final NodeModelCache _cache,
		final String _spreadSheetCategory,
		final String _spreadSheetId,
		final boolean _leftAssociation )
	{
		SpreadSheetFieldArray fields = (SpreadSheetFieldArray)Dictionary.get( _spreadSheetCategory,
				Messages.getString( _spreadSheetId ) );
		_ss.setCache( _cache, fields, fields.getCellStyle(), fields.getActionList() );

		FieldContext fieldContext = new FieldContext();
		fieldContext.setLeftAssociation( _leftAssociation );
		((SpreadSheetModel)_ss.getModel()).setFieldContext( fieldContext );
		((SpreadSheetModel)_ss.getModel()).getCache().update();
	}

	public static void updateCollectionSpreadSheet( 
		final SpreadSheet _ss,
		final Collection _collection,
		final NodeModelDataFactory _dataFactory,
		final int _nbVoidNodes )
	{
		_ss.clearActions();

		NodeModel nodeModel = _ss.getCache().getModel();
		NodeModelFactory.getInstance().updateNodeModelFromCollection( nodeModel, _collection, _dataFactory, _nbVoidNodes );
	}

	/**
	 * changes filter's collection
	 */
	public static void updateFilteredSpreadsheet( 
		final SpreadSheet _ss,
		final Collection _collection )
	{
		ViewTransformer transformer = ((NodeCacheTransformer)_ss.getCache().getVisibleNodes().getTransformer()).getTransformer();
		NodeFilter filter = transformer.getHiddenFilter();

		if (filter instanceof BelongsToCollectionFilter)
		{
			((BelongsToCollectionFilter)filter).setSelectedNodesImpl( _collection, true );
		}
	}
}
