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
package com.projity.dialog.util;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;

import com.projity.configuration.Configuration;

//import com.projity.dialog.FieldDialog;

import com.projity.field.Field;
import com.projity.field.ObjectRef;

import com.projity.grouping.core.Node;
import com.projity.grouping.core.model.NodeModel;
import com.projity.grouping.core.model.NodeModelDataFactory;
import com.projity.grouping.core.model.WalkersNodeModel;

import com.projity.help.HelpUtil;

import org.openproj.domain.document.BelongsToDocument;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JCheckBox;
import javax.swing.JComponent;


/**
 *
 */
public class FieldComponentMap
	implements ObjectRef
{
	public FieldComponentMap( 
		final Object _object )
	{
		myObject = _object;
		setDataFactoryFromObject( _object );
	}

	public FieldComponentMap( 
		final Node _node,
		final NodeModel _nodeModel )
	{
		myNode = _node;
		myNodeModel = _nodeModel;
		myDataFactory = _nodeModel.getDataFactory();
	}

	public FieldComponentMap( 
		final Object _object,
		final NodeModelDataFactory _factory )
	{
		myObject = _object;
		myDataFactory = _factory;
	}

	public FieldComponentMap( 
		final Collection _collection )
	{
		myCollection = _collection;

		if ((_collection != null) 
		 && (_collection.size() > 0))
		{
			setDataFactoryFromObject( _collection.iterator().next() );
		}
	}

	public JComponent append( 
		final DefaultFormBuilder _builder,
		final String _fieldId )
	{
		return appendField( _builder, _fieldId, 0 );
	}

	public void append( 
		DefaultFormBuilder _builder,
		Collection _fields )
	{
		final Iterator i = _fields.iterator();

		while (i.hasNext() == true)
		{
			appendField( _builder, ((Field)i.next()).getId(), 0 );
			_builder.nextLine( 2 );
		}
	}

	public JComponent append( 
		final DefaultFormBuilder _builder,
		final String _fieldId,
		final int _span )
	{
		final Field field = Configuration.getFieldFromId( _fieldId );

		if (field == null)
		{
			return null;
		}

		final JComponent component = getComponent( _fieldId, 0 );
		final boolean isCheckbox = component instanceof JCheckBox;
		CellConstraints cc = new CellConstraints().xyw( _builder.getColumn() + (isCheckbox
				? 0
				: 2), _builder.getRow(), _span );

		if (component instanceof JCheckBox)
		{ 
			// checkboxes already have a label to the right
			_builder.add( component, cc );
		}
		else
		{
			_builder.addLabel( getLabel( _fieldId ) + ":" );
			_builder.nextColumn( 2 );
			_builder.add( component, cc );
			_builder.nextColumn( 1 );
		}

		final String fieldDoc = field.getHelp();

		if (fieldDoc != null)
		{
			HelpUtil.addDocHelp( component, fieldDoc );
		}

		return component;
	}

	public JComponent appendField( 
		final DefaultFormBuilder _builder,
		final String _fieldId,
		final int _flag )
	{
		final Field field = Configuration.getFieldFromId( _fieldId );

		if (field == null)
		{
			return null;
		}

		final JComponent component = getComponent( _fieldId, _flag );

		if (component instanceof JCheckBox == true) 
		{
			// checkboxes already have a label to the right
			_builder.append( component );
		}
		else
		{
			// add a label
			_builder.append( getLabel( _fieldId ) + ":", component );
		}

		final String fieldDoc = field.getHelp();

		if (fieldDoc != null)
		{
			HelpUtil.addDocHelp( component, fieldDoc );
		}

		return component;
	}

	public JComponent appendReadOnly( 
		final DefaultFormBuilder _builder,
		final String _fieldId )
	{
		return appendField( _builder, _fieldId, ComponentFactory.READ_ONLY );
	}

	public JComponent appendSometimesReadOnly( 
		final DefaultFormBuilder _builder,
		final String _fieldId )
	{
		return appendField( _builder, _fieldId, ComponentFactory.SOMETIMES_READ_ONLY );
	}

	/* (non-Javadoc)
	 * @see com.projity.field.ObjectRef#getCollection()
	 */
	@Override
	public Collection getCollection()
	{
		return myCollection;
	}

	public JComponent getComponent( 
		final String _fieldId,
		final int _flag )
	{
		JComponent component = myMap.get( _fieldId );

		if (component == null)
		{
			final Field field = Configuration.getFieldFromId( _fieldId );
			component = ComponentFactory.componentFor( field, this, _flag );
			myMap.put( _fieldId, component );
		}

		return component;
	}

	/* (non-Javadoc)
	 * @see com.projity.field.ObjectRef#getDataFactory()
	 */
	@Override
	public NodeModelDataFactory getDataFactory()
	{
		return myDataFactory;
	}

	public String getLabel( 
		final String _fieldId )
	{
		Field field = Configuration.getFieldFromId( _fieldId );

		return field.getName();
	}

	/* (non-Javadoc)
	 * @see com.projity.field.ObjectRef#getNode()
	 */
	@Override
	public Node getNode()
	{
		return myNode;
	}

	/* (non-Javadoc)
	 * @see com.projity.field.ObjectRef#getNodeModel()
	 */
	@Override
	public WalkersNodeModel getNodeModel()
	{
		return myNodeModel;
	}

	/* (non-Javadoc)
	 * @see com.projity.field.ObjectRef#getObject()
	 */
	@Override
	public Object getObject()
	{
		return myObject;
	}

	/**
	 * @param collection The collection to set.
	 */
	public void setCollection( 
		final Collection _collection )
	{
		myCollection = _collection;
	}

	private void setDataFactoryFromObject( 
		final Object _object )
	{
		if (_object instanceof BelongsToDocument)
		{
			myDataFactory = (NodeModelDataFactory)((BelongsToDocument)_object).getDocument();
		}
	}

//	public void setFieldDialog( 
//		FieldDialog fieldDialog )
//	{
//		this.fieldDialog = fieldDialog;
//	}

	public void setNode( 
		final Node _node )
	{
		myNode = _node;
	}

	public void setNodeModel( 
		final NodeModel _nodeModel )
	{
		myNodeModel = _nodeModel;
	}

	public void setObject( 
		final Object _object )
	{
		myObject = _object;
	}

	// updates all components
	public void updateAll()
	{
		final Iterator<String> itor = myMap.keySet().iterator();

		while (itor.hasNext() == true)
		{
			final String fieldId = itor.next();
			final Field field = Configuration.getFieldFromId( fieldId );
			final JComponent component = getComponent( fieldId, 0 ); // argument 0 shouldn't matter because exists already
			ComponentFactory.updateValueOfComponent( component, field, this );
		}
	}

	private Collection myCollection = null;
//	private FieldDialog fieldDialog;
	private HashMap<String,JComponent> myMap = new HashMap<String,JComponent>();
	private Node myNode = null;
	private NodeModel myNodeModel = null;
	private NodeModelDataFactory myDataFactory;
	private Object myObject;
}
