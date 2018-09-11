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

import com.projity.association.InvalidAssociationException;

import com.projity.datatype.Duration;

import com.projity.field.Field;
import com.projity.field.FieldParseException;

import com.projity.graphic.configuration.ActionList;
import com.projity.graphic.configuration.CellStyle;

import com.projity.grouping.core.Node;
import com.projity.grouping.core.model.NodeModel;
import com.projity.grouping.core.model.NodeModelUtil;

import com.projity.pm.dependency.Dependency;
import com.projity.pm.dependency.DependencyService;
import com.projity.pm.dependency.DependencyType;
import com.projity.pm.graphic.model.cache.NodeModelCache;
import com.projity.pm.graphic.spreadsheet.common.CommonSpreadSheetModel;

import com.projity.util.ClassUtils;
import com.projity.util.Environment;

import java.util.LinkedList;

/**
 *
 */
public class SpreadSheetModel
	extends CommonSpreadSheetModel
{
	/**
	 *
	 */
	public SpreadSheetModel( 
		final NodeModelCache _cache,
		final SpreadSheetColumnModel _colModel,
		final CellStyle _cellStyle,
		final ActionList _actionList )
	{
		super( _cache, _colModel, _cellStyle, _actionList );
	}

	private int findFieldColumn( 
		final Field _field )
	{
		return colModel.findFieldColumn( _field );
	}

	@Override
	public int getColumnCount()
	{
		return colModel.getFieldColumnCount();
	}

	@Override
	public String getColumnName( 
		final int _col )
	{
		if (_col == 0)
		{
			return "";
		}

		return getFieldInColumn( _col ).getName();
	}

	@Override
	public Field getFieldInColumn( 
		final int _col )
	{
		return SpreadSheetUtils.getFieldInColumn( _col, colModel );

		//return colModel.getFieldInColumn(col);
	}

	public Field getFieldInNonTranslatedColumn( 
		final int _col )
	{
		return colModel.getFieldInNonTranslatedColumn( _col );
	}

	@Override
	public Object getValueAt( 
		final int _row,
		final int _col )
	{
		return SpreadSheetUtils.getValueAt( _row, _col, getRowMultiple(), cache, colModel, fieldContext );
	}

	@Override
	public boolean isCellEditable( 
		final int _row,
		final int _col )
	{
		if (isReadOnly())
		{
			return false;
		}

		if (_col == 0)
		{
			return false;
		}

		Field field = getFieldInColumn( _col );

		if (field.getLookupTypes() != null)
		{
			return false;
		}

		Node node = getNodeInRow( _row );
		NodeModel nodeModel = getCache().getModel();

		// 		if (!nodeModel.isLocal()&&!nodeModel.isMaster()&&!Environment.getStandAlone()) return false;
		if (node.isVoid() && !(nodeModel.isLocal() || nodeModel.isMaster()) && "Field.userRole".equals( field.getId() ))
		{
			return false;
		}

		if (node.isVoid())
		{
			return true;
		}

		return !NodeModelUtil.isReadOnly( field, node, getCache().getWalkersModel(), null );
	}

	public boolean isReadOnly()
	{
		return readOnly;
	}

	@Override
	public boolean isRowEditable( 
		final int _row )
	{
		if (isReadOnly())
		{
			return false;
		}

		NodeModel nodeModel = getCache().getModel();

		//if (!nodeModel.isLocal()&&!nodeModel.isMaster()&&!Environment.getStandAlone()) return false;
		Node node = getNodeInRow( _row );

		if (node.isVoid())
		{
			return true;
		}

		return !ClassUtils.isObjectReadOnly( node.getValue() );
	}

	public void setReadOnly( 
		final boolean _readOnly )
	{
		this.readOnly = _readOnly;
	}

	@Override
	public void setValueAt( 
		final Object _value,
		final int _row,
		final int _col )
	{
		if (isReadOnly())
		{
			return;
		}

		if (_col == 0)
		{
			return;
		}

		Field field = getFieldInColumn( _col );
		boolean roleField = "Field.userRole".equals( field.getId() ); //an exception for roles //TODO get rid of this
		NodeModel nodeModel = getCache().getModel();

		if (!nodeModel.isLocal() && !nodeModel.isMaster() && !Environment.getStandAlone() && !roleField)
		{
			return;
		}

		// System.out.println("Field " + getFieldInColumn(col) +
		// "setValueAt("+value+","+row+","+col+")");
		Object oldValue = getValueAt( _row, _col );

		// if (oldValue==null&&(value==null||"".equals(value))) return;
		if ((oldValue == null) && ("".equals( _value )))
		{
			return;
		}

		Node rowNode = getNodeInRow( _row );

		//Field field = getFieldInColumn(col);
		try
		{
			if (rowNode.isVoid())
			{
				if (_value == null)
				{ // null means parse error, so generate error here
					getCache().getModel().setFieldValue( field, rowNode, this, _value, fieldContext, NodeModel.NORMAL );
				}
				else
				{
					//boolean previousIsParent=false;
					LinkedList previousNodes = getPreviousVisibleNodesFromRow( _row );

					if (previousNodes != null)
					{
						Node nextSibling = getNextNonVoidSiblingFromRow( _row );

						if ((nextSibling != null) && (nextSibling.getParent() == previousNodes.getFirst()))
						{
							previousNodes = null;
						}
					}

					getCache().getModel()
						.replaceImplAndSetFieldValue( rowNode, previousNodes, getFieldInColumn( _col ), this, _value,
						fieldContext, NodeModel.NORMAL );
				}
			}
			else if (rowNode.getValue() instanceof Dependency)
			{ // dependencies
			  // need
			  // specific
			  // handling
			  // at least
			  // for undo
			  // TODO this code is a hack and does not belong here.

				Dependency dependency = (Dependency)rowNode.getValue();
				DependencyService dependencyService = DependencyService.getInstance();

				try
				{
					Duration duration = (Duration)((_col == 4)
						? _value
						: getValueAt( _row, 4 )); // TODO
												  // can
												  // not
												  // assume
												  // column
												  // positions

					int type = ((Number)DependencyType.mapStringToValue( (String)((_col == 3)
							? _value
							: getValueAt( _row, 3 )) )).intValue();

					dependencyService.setFields( dependency, duration.getEncodedMillis(), type, this );
					dependencyService.update( dependency, this );
				}
				catch (final InvalidAssociationException _e1)
				{
					// TODO Auto-generated catch block
					_e1.printStackTrace();
				}
			}
			else
			{
				getCache().getModel().setFieldValue( field, rowNode, this, _value, fieldContext, NodeModel.NORMAL );
			}
		}
		catch (final FieldParseException _e)
		{
			// exceptions will be treated by the spreadsheet, not the model, because there is a popup.  
			//...Because this method doesn't have an exception, a runtime exception will be caught by the spreadsheet
			throw new RuntimeException( _e ); 
		}
	}

	protected boolean readOnly;
}
