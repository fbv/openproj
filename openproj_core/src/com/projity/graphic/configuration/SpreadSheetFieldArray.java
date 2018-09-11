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
package com.projity.graphic.configuration;

import com.projity.configuration.Configuration;
import com.projity.configuration.Dictionary;
import com.projity.configuration.NamedItem;

import com.projity.field.Field;

import com.projity.strings.Messages;

import com.projity.workspace.WorkspaceSetting;

import org.apache.commons.digester.Digester;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 */
public class SpreadSheetFieldArray
	extends ArrayList<Field>
	implements Cloneable,
		NamedItem,
		WorkspaceSetting
{
	public SpreadSheetFieldArray()
	{
	}

	//root node needs to be Dictionary
	public static void addDigesterEvents( 
		final Digester _digester )
	{
		_digester.addObjectCreate( "*/spreadsheet", "com.projity.graphic.configuration.SpreadSheetFieldArray" );
		_digester.addSetProperties( "*/spreadsheet" );
		_digester.addSetNext( "*/spreadsheet", "add", "com.projity.configuration.NamedItem" );
		_digester.addSetProperties( "*/spreadsheet/columns/column" );
		_digester.addCallMethod( "*/spreadsheet/columns/column", "addField", 0 );
	}

	public final void addField( 
		final String _fieldId )
	{
		final Field field = Configuration.getFieldFromId( _fieldId );

		if (field != null)
		{
			if (mapFieldTo != null)
			{
				map.put( _fieldId, mapFieldTo );
				mapFieldTo = null;
			}

			add( field );

			//widths.add(field.getColumnWidth());
		}
		else
		{
//			System.out.println("field is null in SpreadSheetFieldArray addField : ");
		}
	}

	@Override
	public final SpreadSheetFieldArray clone()
	{
		return (SpreadSheetFieldArray)super.clone();
	}

//	public static Map<String,Object> convertFields( 
//		final Object _obj,
//		final String _category,
//		final Transformer _typeConverter )
//	{
//		SpreadSheetFieldArray fieldArray = SpreadSheetFieldArray.getFromId( _category, _category );
//
//		if (fieldArray != null)
//		{
//			Map<String,Object> attrs = new HashMap<String,Object>();
//
//			for (Iterator i = fieldArray.iterator(); i.hasNext();)
//			{
//				final Field field = (Field)i.next();
//				String id = field.getId();
//				id = id.substring( id.lastIndexOf( "Field." ) + 6 );
//
//				//String value=field.getText(project,null);
//				Object value = field.getValue( _obj, null );
//
//				if (_typeConverter != null)
//				{
//					value = _typeConverter.transform( value );
//				}
//
//				//	if (value==null) continue;
//				attrs.put( id, value );
//
////				System.out.println("convertFields: id="+id+" value="+value);
//			}
//
//			return attrs;
//		}
//
//		return null;
//	}

	public final WorkspaceSetting createWorkspace( 
		final int _context )
	{
		Workspace ws = new Workspace();
		
		// collect all of the field IDs
		ws.fields.addAll( Configuration.toIdArray( this ) );

		if (widths != null)
		{
			ws.widths.addAll( widths );
		}

		return ws;
	}

//	public void setWidth(int column, int width) 
//	{
//		widths.set(column,width);
//	}

	@Override
	public final boolean equals( 
		Object _other )
	{
		if (_other instanceof SpreadSheetFieldArray == false)
		{
			return false;
		}

		return (name == null
			? ((SpreadSheetFieldArray)_other).getName() == null
			: name.equals( ((SpreadSheetFieldArray)_other).getName() ));
	}

	public final ActionList getActionList()
	{
		final ActionLists actionLists = ActionLists.getInstance();

		if ((actionListId == null) 
		 || (actionListId.length() == 0))
		{
			return actionLists.getDefaultActionList();
		}

		ActionList actionList = actionLists.getActionList( actionListId );

		if (actionList == null)
		{
			actionList = actionLists.getDefaultActionList();
		}

		return actionList;
	}

	public final String getActionListId()
	{
		return actionListId;
	}

	/**
	 * @return Returns the category.
	 */
	@Override
	public final String getCategory()
	{
		return category;
	}

	public final CellStyle getCellStyle()
	{
		final CellStyles cellStyles = CellStyles.getInstance();

		if ((cellStyleId == null) || (cellStyleId.length() == 0))
		{
			return cellStyles.getDefaultStyle();
		}

		CellStyle style = cellStyles.getStyle( cellStyleId );

		if (style == null)
		{
			style = cellStyles.getDefaultStyle();
		}

		return style;
	}

	public final String getCellStyleId()
	{
		return cellStyleId;
	}

	public static SpreadSheetFieldArray getFromId( 
		final String _category,
		final String _id )
	{
		SpreadSheetFieldArray result = (SpreadSheetFieldArray)Dictionary.get( _category, Messages.getString( _id ) );

		if (result == null)
		{
			result = (SpreadSheetFieldArray)Dictionary.get( _category, _id );
		}

		return result;
	}

	public final String getMapFieldTo()
	{
		return mapFieldTo;
	}

	public final String getMappedValue( 
		final String _key )
	{
		return (String)map.get( _key );
	}

	/**
	 * @return Returns the name.
	 */
	@Override
	public final String getName()
	{
		return name;
	}

	public final int getWidth( 
		final int _column )
	{
		return ((widths != null) && (_column >= 0) && (_column < widths.size()))
			? widths.get( _column )
			: (-1);
	}

	public final ArrayList<Integer> getWidths()
	{
		return widths;
	}

	public final SpreadSheetFieldArray insertField( 
		final int _position,
		final Field _field )
	{
		SpreadSheetFieldArray f = makeEditableVersion();
		f.add( _position, _field );

		//f.widths.add(field.getColumnWidth());
		return f;
	}

	public final boolean isUserCreated()
	{
		return userCreated;
	}

	public final boolean isUserDefined()
	{
		return id == null;
	}

	public final SpreadSheetFieldArray makeEditableVersion()
	{
		SpreadSheetFieldArray f = this;

		if (f.isUserDefined() == false)
		{
			f = f.makeUserDefinedCopy();
			Dictionary.add( f );
		}

		return f;
	}

	public final SpreadSheetFieldArray makeUserDefinedCopy()
	{
		final SpreadSheetFieldArray newOne = (SpreadSheetFieldArray)clone();
		newOne.setId( null ); // it's user defined
		newOne.setName( Dictionary.generateUniqueName( this ) );
		newOne.userCreated = true;

		return newOne;
	}

	public final SpreadSheetFieldArray move( 
		final int _oldPosition,
		final int _newPosition )
	{
		final SpreadSheetFieldArray f = makeEditableVersion();
		final Field field = (Field)f.remove( _oldPosition );

		//Integer w = f.widths.remove(oldPosition);
		final SpreadSheetFieldArray result = f.insertField( _newPosition, field );

		//result.widths.set(newPosition,w);
		return result;
	}

	/**
	 * @return
	 */
//	public final Object next()
//	{
//		// TODO Auto-generated method stub
//		return null;
//	}

	public final SpreadSheetFieldArray removeField( 
		final int _position )
	{
		final SpreadSheetFieldArray f = makeEditableVersion();
		f.remove( _position );

		//f.widths.remove(position);
		return f;
	}

	public final void removeField( 
		final String _fieldId )
	{
		if (_fieldId == null)
		{
			return;
		}

		map.remove( _fieldId );

		for (int i = 0; i < size(); i++)
		{
			Field field = (Field)get( i );

			if (_fieldId.equals( field.getId() ))
			{
				remove( i );

				//widths.remove(i);
			}
		}
	}

	public static SpreadSheetFieldArray restore( 
		final WorkspaceSetting _spreadsheetWorkspace,
		final String _name,
		final int _context )
	{
		SpreadSheetFieldArray fieldArray = new SpreadSheetFieldArray();
		fieldArray.setCategory( SpreadSheetCategories.taskSpreadsheetCategory );
		fieldArray.restoreWorkspace( _spreadsheetWorkspace, _context );
		fieldArray.setName( _name );
		Dictionary.add( fieldArray );

		return fieldArray;
	}

	public void restoreWorkspace( 
		final WorkspaceSetting _workspace,
		final int _context )
	{
		final Workspace ws = (Workspace)_workspace;
		addAll( Configuration.fromIdArray( ws.fields ) );

		if ((ws.version > 0.0f) 
		 && (ws.widths != null) 
		 && (ws.widths.size() > 0))
		{
			widths = new ArrayList<Integer>( ws.widths.size() );
			widths.addAll( ws.widths );
		}

		//remove missing fields
		final Iterator<Field> itor = iterator();
		final Iterator<Integer> j = widths.iterator();

		while (itor.hasNext() == true)
		{
			j.next();

			if (itor.next() == null)
			{
				itor.remove();
				j.remove();
			}
		}
	}

	public final void setActionListId( 
		final String _actionListId )
	{
		actionListId = _actionListId;
	}

	/**
	 * @param _category The category to set.
	 */
	public final void setCategory( 
		final String _category )
	{
		category = _category;
	}

	public final void setCellStyleId( 
		final String _cellStyleId )
	{
		cellStyleId = _cellStyleId;
	}

	public final void setId( 
		final String _messageId )
	{
		id = _messageId;

		if (name == null)
		{
			setName( Messages.getString( _messageId ) );
		}
	}

	public final void setMapFieldTo( 
		final String _mapFieldTo )
	{
		mapFieldTo = _mapFieldTo;
	}

	/**
	 * @param name The name to set.
	 */
	public final void setName( 
		final String _name )
	{
		name = _name;
	}

	public final void setUserCreated( 
		final boolean _userCreated )
	{
		userCreated = _userCreated;
	}

	public final void setWidths( 
		final ArrayList<Integer> _widths )
	{
		widths = _widths;
	}

	@Override
	public final String toString()
	{
		return getName();
	}

	public static class Workspace
		implements WorkspaceSetting
	{
		private static final long serialVersionUID = -4517935309304612237L;
		ArrayList<String> fields = new ArrayList<String>();
		ArrayList<Integer> widths = new ArrayList<Integer>();
		float version = 1.0f;
	}

	private static final long serialVersionUID = 6310711336308730391L;
	transient Map map = new LinkedHashMap();
	ArrayList<Integer> widths = null; //new ArrayList<Integer>();
	private String actionListId;
	private String category;
	private String cellStyleId;
	private String id = null;
	public String mapFieldTo;
	private String name = null;
	transient boolean userCreated = false;
}
