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

Attribution Information: Attribution Copyright Notice: Copyright © 2006, 2007
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
package com.projity.grouping.core.model;

import com.projity.configuration.FieldDictionary;
import com.projity.datatype.Duration;
import com.projity.datatype.Money;
import com.projity.datatype.TimeUnit;
import org.openproj.domain.document.Document;
import com.projity.field.Field;
import com.projity.field.FieldContext;
import com.projity.field.FieldParseException;
import com.projity.field.FieldSetOptions;
import com.projity.field.ObjectRef;
import com.projity.grouping.core.GroupNodeImpl;
import com.projity.grouping.core.Node;
import com.projity.grouping.core.VoidNodeImpl;
import com.projity.grouping.core.hierarchy.AbstractMutableNodeHierarchy;
import com.projity.grouping.core.summaries.DeepChildWalker;
import com.projity.grouping.core.summaries.NodeWalker;
import com.projity.grouping.core.summaries.SummaryVisitor;
import com.projity.options.CalendarOption;

import com.projity.pm.assignment.Assignment;
import com.projity.pm.calendar.WorkingCalendar;
import org.openproj.domain.identity.HasId;
import org.openproj.domain.document.BelongsToDocument;
import com.projity.pm.task.Project;
import com.projity.server.data.DataObject;
import com.projity.util.ClassUtils;
import com.projity.util.Environment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.Closure;

import org.openproj.domain.task.Task;


public class NodeModelUtil
{
	public static void cacheWbs( 
		NodeModel nodeModel,
		Node parentNode )
	{
		Object parentImpl = parentNode.getValue();
		List children = nodeModel.getChildren( parentNode );

		if (parentImpl instanceof Task && (children != null) && (children.size() > 0))
		{
			Task parent = (Task)parentImpl;
			parent.setWbsChildrenNodes( children ); // cached values

			Node child;

			for (Iterator i = children.iterator(); i.hasNext();)
			{
				child = (Node)i.next();

				Object impl = child.getValue();

				if (impl instanceof Task)
				{
					((Task)impl).setWbsParent( parent ); // set cached wbs parent
														 // too

					cacheWbs( nodeModel, child );
				}
			}
		}
	}

	public static boolean canBeChildOf( 
		Node parent,
		Node child )
	{
		Object parentImpl = parent.getValue();
		Object childImpl = child.getValue();

		if (nodeIsSubproject( parent ))
		{
			return false;
		}

		if (parentImpl instanceof Task && childImpl instanceof Task)
		{
			return ((Task)parentImpl).getOwningProject() == ((Task)childImpl).getOwningProject();
		}

		return true;
	}

	public static void dump( 
		NodeModel model )
	{
		((AbstractMutableNodeHierarchy)model.getHierarchy()).dump();
	}

	public static void dumpTask( 
		NodeModel nodeModel )
	{
		dumpTask( nodeModel, null, "" );
	}

	public static void enumerateNonAssignments( 
		NodeModel model )
	{
		DeepChildWalker.recursivelyTreatBranch( model, null, new NonAssignmentEnumerator() );
	}

	public static List extractNodeList( 
		NodeModel nodeModel,
		Node root )
	{
		ArrayList l = new ArrayList();
		extractNodeList( nodeModel, root, l );

		return l;
	}

	public static String getOutlineString( 
		NodeModel nodeModel,
		Node node,
		String result )
	{
		Node parent = nodeModel.getParent( node );

		if (parent == null)
		{
			return result;
		}

		int i = nodeModel.getIndexOfChild( parent, node );
		String ancestors = getOutlineString( nodeModel, parent, result );

		return ancestors + ((ancestors.length() == 0)
		? ""
		: ".") + (i + 1);
	}

	private static Object getSummarizedValueForField( 
		final Field _field,
		final Node _node,
		final WalkersNodeModel _nodeModel,
		FieldContext _context )
	{
		// group's special summaries handled here
		if (_context == null)
		{
			_context = _field.getSpecialFieldContext();
		}

		Object object = _node.getValue();
		boolean deep = object instanceof Document; // also will be deep if type is SumDeep
		NodeWalker walkingVisitor = (NodeWalker)_field.getSummaryVisitor( (object instanceof GroupNodeImpl)
			? _field.getSummaryForGroup()
			: _field.getSummary(), deep );
		walkingVisitor.setNode( _node );
		walkingVisitor.setNodeModel( _nodeModel );
		walkingVisitor.setContext( _context );
		walkingVisitor.setField( _field );

		Object key = null;
		Object result = null;
		walkingVisitor.setUseKey( false );

		//field.isHasOptions());
//		if (field.isHasOptions()){
//			key = walkingVisitor.getSummary();
//			if (walkingVisitor.getFormat()==null)
//				result = walkingVisitor.getSummary();
//			else{
//				try {
//					result=field.getSelect().getValue(key==null?null:walkingVisitor.getFormat().format(key));
//				} catch (InvalidChoiceException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//		else
		
		result = walkingVisitor.getSummary();

		walkingVisitor.setUseKey( false );
		walkingVisitor.setFormat( null );

		if ((_context != null) && _context.isHideNullValues() && walkingVisitor.getClosure() instanceof SummaryVisitor)
		{ // special case for parents that have children all without value

			if (!((SummaryVisitor)walkingVisitor.getClosure()).isHasValue())
			{
				return null;
			}
		}

		if (_field.isMoney())
		{
			result = Money.getInstance( (Double)result );
		}
		else if (result instanceof Double)
		{ // convert to proper display type
			result = ClassUtils.doubleToObject( (Double)result, _field.getDisplayType() );
		}

		if ( /*(object instanceof GroupNodeImpl) &&*/
			_field.hasOptions())
		{ // TODO should apply to summaries other than group
			result = _field.convertValueToStringUsingOptions( result, object );
		}

//		System.out.println(node.getValue() + " " + field + " value " + result);
		return result;
	}

	public static Object getValue( 
		final Field _field,
		final Node _node,
		final WalkersNodeModel _nodeModel,
		FieldContext context )
	{
		Object result;
		Object object = _node.getValue();

		if (object instanceof VoidNodeImpl == true)
		{
			return null;
		}

//		if ("Field.userRole".equals(id) && object instanceof ResourceImpl){
//			ResourceImpl r=(ResourceImpl)object;
//			if (!r.isUser()) return null;
//		}z
		
		if ((object instanceof GroupNodeImpl == false) 
		 && (_field.isHidden( object, context ) == true))
		{
			return null;
		}

		if (context == null)
		{
			context = _field.getSpecialFieldContext();
		}

		if ("Field.duration".equals( _field.getId() ) && (object instanceof GroupNodeImpl))
		{
			Document document = _nodeModel.getDocument();

			if ((document == null) || !(document instanceof Project))
			{
				return null;
			}

			WorkingCalendar wc = (WorkingCalendar)((Project)document).getWorkCalendar();

			// startDate, endDate calculated twice. Can find better
			Field startField = FieldDictionary.getInstance().getFieldFromId( "Field.start" );
			Field endField = FieldDictionary.getInstance().getFieldFromId( "Field.finish" );
			Date start = (Date)getSummarizedValueForField( startField, _node, _nodeModel, context );
			Date end = (Date)getSummarizedValueForField( endField, _node, _nodeModel, context );

			double t = wc.compare( end.getTime(), start.getTime(), false );
			result = new Duration(Duration.getInstance( t / CalendarOption.getInstance().getMillisPerDay(), TimeUnit.DAYS ));

			// TODO 8 IS A HACK REPLACE ALL THIS SECTION
		}
		else
		{
			if (nodeHasNonSummarizedValue( _field, _node, _nodeModel ) == true)
			{
				// if no summary or leaf
				result = _field.getValue( object, context );

				if (_field.hasOptions() == true)
				{
					result = _field.convertValueToStringUsingOptions( result, _node.getValue() );
				}
			}
			else
			{
				result = getSummarizedValueForField( _field, _node, _nodeModel, context );
			}
		}

		if ((_field.isWork() == true) && (result != null))
		{
			// work must be formatted correctly
			if (result instanceof Duration == true)
			{
				((Duration)result).setWork( true );
			}
		}

//		if (isMoney() && isConvertCurrency()) { // convert currency
//			result = CurrencyRateTable.convertToDisplay((Number)result);
//		}
		
		return result;
	}

	public static Object getValue( 
		final Field _field,
		final ObjectRef _objectRef,
		FieldContext _context )
	{
		if (_context == null)
		{
			_context = _field.getSpecialFieldContext();
		}

		if (_objectRef.getCollection() != null)
		{
			return _field.getCommonValue( _objectRef.getCollection(), true, false );
		}

		if (_objectRef.getNode() != null)
		{
			return getValue( _field, _objectRef.getNode(), _objectRef.getNodeModel(), _context );
		}
		else
		{
			return _field.getValue( _objectRef.getObject(), _context );
		}
	}

	/**
	 * See if a node is read only. Before calling the object-based version of
	 * read-only, it checks to see if the node is a summarized parent and if the
	 * field is a summarized value, thus read only
	 *
	 * @param node
	 * @param nodeModel
	 * @param context
	 * @return
	 */
	public static boolean isReadOnly( 
		final Field _field,
		final Node _node,
		final WalkersNodeModel _nodeModel,
		FieldContext _context )
	{
		if (_context == null)
		{
			_context = _field.getSpecialFieldContext();
		}

		if (_node.getValue() instanceof GroupNodeImpl == true)
		{
			return true;
		}

		if ((_field.getSummary() == Field.THIS) && _nodeModel.isSummary( _node )) // for parents with
															  // This summary type
		{

			return true;
		}

		if (nodeHasNonSummarizedValue( _field, _node, _nodeModel ) == false)
		{
			return true;
		}

		return _field.isReadOnly( _node.getValue(), _context );
	}

	public static boolean isReadOnly( 
		final Field _field,
		final ObjectRef _objectRef,
		FieldContext _context )
	{
		if (_context == null)
		{
			_context = _field.getSpecialFieldContext();
		}

		if (_objectRef.getCollection() != null)
		{
			Iterator i = _objectRef.getCollection().iterator();

			while (i.hasNext())
			{
				if (_field.isReadOnly( i.next(), _context ))
				{
					return true;
				}
			}

			return false;
		}

		if (_objectRef.getNode() != null)
		{
			return isReadOnly( _field, _objectRef.getNode(), _objectRef.getNodeModel(), _context );
		}
		else
		{
			return _field.isReadOnly( _objectRef.getObject(), _context );
		}
	}

	/**
	 * See if this node displays its own data for the field or uses summary
	 * value
	 *
	 * @param node
	 * @param nodeModel
	 * @return true if the node displays own value
	 */
	private static boolean nodeHasNonSummarizedValue( 
		final Field _field,
		final Node _node,
		final WalkersNodeModel _nodeModel )
	{
		// special behaviour for groups
		int sum = _field.getSummary();

		if (_node.getValue() instanceof GroupNodeImpl == true)
		{
			sum = _field.getSummaryForGroup();
		}

		if (_node.getValue() instanceof BelongsToDocument)
		{
			// for projects which don't roll up
			final Document document = ((BelongsToDocument)_node.getValue()).getDocument();

			if ((document instanceof Project == true) 
			 && (((Project)document).isDontSummarizeFields() == true))
			{
				return true;
			}
		}

		return ((sum == Field.NONE) || (sum == Field.THIS) || _nodeModel.isSummary( _node ) == false);
	}

	public static boolean nodeIsSubproject( 
		Node node )
	{
		Object impl = node.getValue();

		return impl instanceof Task && ((Task)impl).isSubproject();
	}

	public static String getText( 
		final Field _field,
		final ObjectRef _objectRef,
		FieldContext _context )
	{
		if (_context == null)
		{
			_context = _field.getSpecialFieldContext();
		}

		if (_objectRef.getCollection() != null)
		{
			return "" + _field.getCommonValue( _objectRef.getCollection(), true, true );
		}

		if ( _objectRef.getNode() != null)
		{
			return getText( _field, _objectRef.getNode(), _objectRef.getNodeModel(), _context );
		}
		else
		{
			return _field.getText( _objectRef.getObject(), _context );
		}
	}

	public static void setText( 
		final Field _field,
		final Node _node,
		final WalkersNodeModel _nodeModel,
		final String _textValue,
		FieldContext _context,
		final FieldSetOptions _options )
		throws FieldParseException
	{
		if (_context == null)
		{
			_context = _field.getSpecialFieldContext();
		}

		if (FieldContext.isForceValue( _context ) && isReadOnly( _field, _node, _nodeModel, _context ) == false)
		{ // don't allow setting of
		  // read only fields
		  // log.warn("Tried to set text of read only field" + getId());

			return;
		}

		Object object = _node.getValue();
		Object value = _field.preprocessText( object, _textValue, _context );

		if (value == _textValue)
		{
			value = _field.setTextValue( object, _textValue, _context );
		}

		setValue( _field, _node, _nodeModel, _field, value, _context, _options );
	}

	public static String getText( 
		final Field _field,
		final Node _node,
		final WalkersNodeModel _nodeModel,
		FieldContext _context )
	{
		Object object = _node.getValue();

		if (_field.isApplicable( object ) == false)
		{
			return Field.NOT_APPLICABLE;
		}

		if (_field.getPassword() == true) // don't show passwords
		{
			return Field.PASSWORD_MASK;
		}

		if (_context == null)
		{
			_context = _field.getSpecialFieldContext();
		}

		Object value = null;

		try
		{
			value = getValue( _field, _node, _nodeModel, _context );

			if (_field.hasOptions() == true)
			{
				return _field.convertValueToStringUsingOptions( value, object );
			}
		}
		catch (IllegalArgumentException e1)
		{
			e1.printStackTrace();
		}

		return _field.toText( value, object );
	}

	public static void setText( 
		final Field _field,
		Node _node,
		WalkersNodeModel _nodeModel,
		String _textValue,
		FieldContext _context )
		throws FieldParseException
	{
		setText( _field, _node, _nodeModel, _textValue, _context, null );
	}

	public static void setText( 
		final Field _field,
		ObjectRef _objectRef,
		String _textValue,
		FieldContext _context )
		throws FieldParseException
	{
		setText( _field, _objectRef, _textValue, _context, null );
	}

	public static void setText( 
		final Field _field,
		ObjectRef _objectRef,
		String _textValue,
		FieldContext _context,
		FieldSetOptions _options )
		throws FieldParseException
	{
		if (_context == null)
		{
			_context = _field.getSpecialFieldContext();
		}

		if (_objectRef.getCollection() != null)
		{
			Iterator i = _objectRef.getCollection().iterator();

			while (i.hasNext())
			{
				_field.setText( i.next(), _textValue, _context, _options );
			}
		}
		else if (_objectRef.getNode() != null)
		{
			setText( _field, _objectRef.getNode(), _objectRef.getNodeModel(), _textValue, _context, _options );
		}
		else
		{
			_field.setText( _objectRef.getObject(), _textValue, _context, _options );
		}
	}

	/**
	 *
	 * @param _node
	 * @param _nodeModel
	 * @param _source
	 * @param _value
	 * @param _context
	 * @throws FieldParseException
	 */
	public static void setValue( 
		final Field _field,
		final Node<?> _node,
		final WalkersNodeModel _nodeModel,
		final Object _source,
		final Object _value,
		final FieldContext _context )
		throws FieldParseException
	{
		setValue( _field, _node, _nodeModel, _source, _value, _context, null );
	}

	public static void setValue( 
		final Field _field,
		ObjectRef _objectRef,
		Object _source,
		Object _value,
		FieldContext _context )
		throws FieldParseException
	{
		setValue( _field, _objectRef, _source, _value, _context, null );
	}

	/**
	 * Called from spreadsheet
	 *
	 * @param node
	 *            Node modified
	 * @param nodeModel
	 *            nodeModel where node lives
	 * @param source
	 *            Source of change (for event)
	 * @param value
	 *            value
	 * @param context
	 * @throws FieldParseException
	 */
	public static void setValue( 
		final Field _field,
		Node _node,
		WalkersNodeModel _nodeModel,
		Object _source,
		Object _value,
		FieldContext _context,
		FieldSetOptions _options )
		throws FieldParseException
	{
		if (_context == null)
		{
			_context = _field.getSpecialFieldContext();
		}

		Object object = _node.getValue();
		_field.setValue( object, _source, _value, _context, _options );

		if (Environment.isNoPodServer() && object instanceof DataObject)
		{ //need to set ancestors dirty for rollup. The exposed fields have to be sent in this case

			if ((_context == null) || !_context.isNoDirty())
			{
				for (Node parent = _nodeModel.getParent( _node ); (parent != null) && !parent.isRoot();
						parent = _nodeModel.getParent( parent ))
				{
					parent.setDirty( true );
				}
			}
		}
	}

	public static void setValue( 
		final Field _field,
		ObjectRef _objectRef,
		Object _source,
		Object _value,
		FieldContext _context,
		FieldSetOptions _options )
		throws FieldParseException
	{
		if (_context == null)
		{
			_context = _field.getSpecialFieldContext();
		}

		if (_objectRef.getCollection() != null)
		{
			Iterator i = _objectRef.getCollection().iterator();

			while (i.hasNext())
			{
				_field.setValue( i.next(), _source, _value, _context, _options );
			}
		}
		else if (_objectRef.getNode() != null)
		{
			setValue( _field, _objectRef.getNode(), _objectRef.getNodeModel(), _source, _value, _context, _options );
		}
		else
		{
			_field.setValue( _objectRef.getObject(), _source, _value, _context, _options );
		}
	}

	public static Object value( 
		final Field _field,
		final Node _node,
		final NodeModel _nodeModel )
	{
		return getValue( _field, _node, _nodeModel, null );
	}

	private static void dumpTask( 
		NodeModel nodeModel,
		Node parent,
		String indent )
	{
		if (parent != null)
		{
			System.out.println( indent + ">" + parent.toString() );
		}

		Collection children = nodeModel.getChildren( parent );

		if (children != null)
		{
			Iterator i = children.iterator();

			while (i.hasNext())
			{
				Node n = (Node)i.next();
				Object impl = n.getValue();

				if (impl instanceof Task)
				{
					if (((Task)impl).getWbsParentTask() != ((parent == null)
							? null
								: parent.getValue()))
					{
						System.out.println( "cached hierarchy error - child " + impl + " cached parent" +
							((Task)impl).getWbsParentTask() + " parent " + parent.getValue() );
					}
				}

				dumpTask( nodeModel, n, indent + "--" );
			}
		}
	}

	private static void extractNodeList( 
		NodeModel nodeModel,
		Node parent,
		Collection result )
	{
		if (parent != null)
		{
			result.add( parent );
		}

		Collection children = nodeModel.getChildren( parent );

		if (children != null)
		{
			Iterator i = children.iterator();

			while (i.hasNext())
			{
				Node n = (Node)i.next();
				extractNodeList( nodeModel, n, result );
			}
		}
	}

	static class NonAssignmentEnumerator
		implements Closure
	{
		@Override
		public void execute( 
			Object node )
		{
			if (node == null)
			{
				return;
			}

			Object impl = ((Node)node).getValue();

			if ((impl != null) && !(impl instanceof Assignment))
			{
				((HasId)impl).setId( ++count );
			}
		}

		int count = 0;
	}
}
