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

import com.projity.field.Field;
import com.projity.field.FieldContext;
import com.projity.field.FieldParseException;
import com.projity.field.FieldSetOptions;

import com.projity.grouping.core.Node;
import com.projity.grouping.core.hierarchy.NodeHierarchy;

import com.projity.undo.UndoController;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.tree.TreeModel;
import javax.swing.undo.UndoableEditSupport;


/**
 *
 */
public interface NodeModel
	extends TreeModel,
		WalkersNodeModel
{
	//Node structure modification
	public void add( Node _child, int _actionType );

	public void add( Node _parent, Node _child, int _position, int _actionType );

	public void add( Node _parent, Node _child, int _actionType );

	public void add( Node _parent, List _children, int _actionType );

	public void add( Node _parent, List _children, int _position, int _actionType );

	public void addBefore( LinkedList _siblings, Node _newNode, int _actionType );

	public void addBefore( Node _sibling, Node _newNode, int _actionType );

	public void addBefore( Node _sibling, List _newNodes, int _actionType );

	public void addImplCollection( Node _parent, Collection _collection, int _actionType );

	public Object clone();

	public List copy( List _nodes, int _actionType );

	public List cut( List _nodes, int _actionType );

	//shortcut used by walkers
	@Override
	public List getChildren( Node _parent );

	public NodeModelDataFactory getDataFactory();

	public NodeHierarchy getHierarchy();

	@Override
	public Node getParent( Node _child );

	public Node getSummaryNode();

	public UndoController getUndoController();

	public UndoableEditSupport getUndoableEditSupport();

	public Node getVirtualroot();

	public boolean hasChildren( Node _node );

	public boolean isLocal();

	public boolean isMaster();

	@Override
	public boolean isSummary( Node _node );

	public boolean isSummaryNodeEnabled();

	public Iterator iterator();

	public Iterator iterator( Node _rootNode );

	public void move( Node parent, List nodes, int position, int actionType );

	public Node newNode( Node _parent, int _position, int _actionType );

	public void paste( Node _parent, List _nodes, int _position, int _actionType );

	public void remove( Node _node, int _actionType );

	public void remove( List _nodes, int _actionType );

	public void remove( Node _node, int _actionType, boolean _removeDependencies );

	public void remove( List _nodes, int _actionType, boolean _removeDependencies );

	public void removeAll( int _actionType );

	//internal
	public boolean removeApartFromHierarchy( Node _node, boolean _cleanAssignment, int _actionType, boolean _removeDependencies );

	public Node replaceImpl( Node _node, Object _nodeImpl, Object _eventSource, int _actionType );

	public Node replaceImplAndSetFieldValue( Node _node, LinkedList _previous, Object _newImpl, Field _field, Object _eventSource,
		Object _value, FieldContext _context, int _actionType ) throws FieldParseException;

	public Node replaceImplAndSetFieldValue( Node _node, LinkedList _previous, Field _field, Object _eventSource, Object _value,
		FieldContext _context, int _actionType ) throws FieldParseException;

	public Node search( Object _key, Comparator _comparator );

	@Override
	public Node search( Object _key );

	public void setDataFactory( NodeModelDataFactory _dataFactory );

	//Node implementation or field modifications
	public void setFieldValue( Field _field, Node _node, Object _eventSource, Object _value, FieldContext _context, 
		int _actionType ) throws FieldParseException;

	public void setFieldValue( Field _field, Node _node, Object _eventSource, Object _value, FieldContext _context, 
		FieldSetOptions _options, int _actionType ) throws FieldParseException;

	public void setHierarchy( NodeHierarchy _hierarchy );

	public void setLocal( boolean _local );

	public void setMaster( boolean _master );

	public void setSummaryNodeEnabled( boolean _summayTaskEnabled );

	public void setUndoController( UndoController _undoController );

	public Iterator shallowIterator( int _maxLevel, boolean _returnRoot );

	boolean confirmRemove( List _nodes );

	public static int EVENT = 1;
	public static int UNDO = 2;
	public static int SILENT = 0;
	public static int NORMAL = 3;
}
