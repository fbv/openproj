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

import com.projity.association.AssociationList;
import org.openproj.domain.document.Document;
import com.projity.field.Field;
import com.projity.field.FieldContext;
import com.projity.field.FieldParseException;
import com.projity.field.FieldSetOptions;
import com.projity.grouping.core.Node;
import com.projity.grouping.core.NodeBridge;
import com.projity.grouping.core.NodeFactory;
import com.projity.grouping.core.VoidNodeImpl;
import com.projity.grouping.core.hierarchy.HierarchyUtils;
import com.projity.grouping.core.hierarchy.MutableNodeHierarchy;
import com.projity.grouping.core.hierarchy.NodeHierarchy;
import com.projity.pm.assignment.Assignment;
import com.projity.pm.assignment.AssignmentService;
import com.projity.pm.assignment.HasAssignments;
import com.projity.pm.dependency.Dependency;
import com.projity.pm.dependency.HasDependencies;
import com.projity.pm.resource.ResourceImpl;
import com.projity.pm.task.TaskLinkReference;
import com.projity.pm.task.TaskLinkReferenceImpl;
import com.projity.undo.DataFactoryUndoController;
import com.projity.undo.ModelFieldEdit;
import com.projity.undo.NodeCreationEdit;
import com.projity.undo.NodeDeletionEdit;
import com.projity.undo.NodeImplChangeAndValueSetEdit;
import com.projity.undo.NodeImplChangeEdit;
import com.projity.undo.NodePasteEdit;
import com.projity.undo.UndoController;
import com.projity.util.Environment;
import java.util.*;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import org.openproj.domain.task.Task;

/**
 *
 */
public class DefaultNodeModel
	implements NodeModel
{
	/**
	 *
	 */
	public DefaultNodeModel()
	{
		myHierarchy = new MutableNodeHierarchy();
	}

	public DefaultNodeModel( 
		NodeModelDataFactory _dataFactory )
	{
		this();
		myDataFactory = _dataFactory;
	}

	//for clone()
	DefaultNodeModel( 
		final NodeHierarchy _hierarchy,
		final NodeModelDataFactory _dataFactory )
	{
		myHierarchy = _hierarchy;
		myDataFactory = _dataFactory;
	}

	@Override
	public void add( 
		final Node _parent,
		final Node _child,
		final int _actionType )
	{
		add( _parent, _child, -1, _actionType );
	}

	@Override
	public void add( 
		final Node _parent,
		final Node _child,
		final int _position,
		final int _actionType )
	{
		ArrayList children = new ArrayList();
		children.add( _child );
		add( _parent, children, _position, _actionType );

		//myHierarchy.add(parent,child,position,actionType);
	}

	@Override
	public void add( 
		final Node _parent,
		final List _children,
		final int _actionType )
	{
		add( _parent, _children, -1, _actionType );

		//myHierarchy.add(parent,children,actionType);
	}

	@Override
	public void add( 
		Node _parent,
		final List _children,
		final int _position,
		final int _actionType )
	{
		Node summaryNode = getSummaryNode();

		if ((summaryNode != null) && (_parent == getHierarchy().getRoot()))
		{
			_parent = summaryNode;
		}

		myHierarchy.add( _parent, _children, _position, _actionType );

		//Undo
		if (isUndo( _actionType ))
		{
			postEdit( new NodeCreationEdit( this, _parent, _children, _position ) );
		}
	}

	@Override
	public void add( 
		final Node _child,
		final int _actionType )
	{
		add( (Node)myHierarchy.getRoot(), _child, _actionType );
	}

	@Override
	public void addBefore( 
		final LinkedList _siblings,
		final Node _newNode,
		final int _actionType )
	{
		Node previous;
		Node next;
		Node parent;
		boolean firstChild;

		if (_siblings.size() == 0)
		{
			return;
		}
		else if (_siblings.size() == 1)
		{
			previous = null;
			next = (Node)_siblings.removeLast();
			parent = null;
			firstChild = true;
		}
		else
		{
			previous = (Node)_siblings.removeFirst(); //no need to clone, list used only here, see CommonSpreadSheetModel
			next = (Node)_siblings.removeLast();
			parent = (Node)next.getParent();
			firstChild = (parent == previous);

			if (firstChild)
			{
				parent = previous;
			}
			else
			{
				parent = (Node)previous.getParent();
			}

			remove( _siblings, NodeModel.SILENT );
		}

		_siblings.add( _newNode );
		add( parent, _siblings, (firstChild)
			? 0
			: (parent.getIndex( previous ) + 1), NodeModel.SILENT );

		getDataFactory().setGroupDirty( true );

		//TODO need undo here
	}

	@Override
	public void addBefore( 
		final Node _sibling,
		final Node _newNode,
		final int _actionType )
	{
		Node parent = (Node)_sibling.getParent();
		add( parent, _newNode, parent.getIndex( _sibling ), _actionType );
	}

	@Override
	public void addBefore( 
		final Node _sibling,
		final List _newNodes,
		final int _actionType )
	{
		Node parent = (Node)_sibling.getParent();
		add( parent, _newNodes, parent.getIndex( _sibling ), _actionType );
	}

	/**
	 * Convenience method to add a collection of objects (not nodes) to the node model
	 *
	 * @param _parent
	 * @param _collection
	 */
	@Override
	public void addImplCollection( 
		final Node _parent,
		final Collection _collection,
		final int _actionType )
	{
		Iterator i = _collection.iterator();
		Node child;

		while (i.hasNext())
		{
			child = NodeFactory.getInstance().createNode( i.next() );
			add( _parent, child, _actionType );
		}
	}

	// Below is for tree model
	@Override
	public void addTreeModelListener( 
		final TreeModelListener _arg0 )
	{
		myHierarchy.addTreeModelListener( _arg0 );
	}

	@Override
	public Object clone()
	{
		return new DefaultNodeModel((NodeHierarchy)myHierarchy.clone(), myDataFactory);
	}

	@Override
	public boolean confirmRemove( 
		final List nodes )
	{
		return true; //!Environment.isScripting();
	}

	public static boolean isEvent( 
		final int _actionType )
	{
		return (_actionType & NodeModel.EVENT) == NodeModel.EVENT;
	}

	public static boolean isUndo( 
		final int _actionType )
	{
		return (_actionType & NodeModel.UNDO) == NodeModel.UNDO;
	}

	@Override
	public List copy( 
		final List _nodes,
		final int _actionType )
	{
		return copy( _nodes, true, _actionType );
	}

	public List copy( 
		List _nodes,
		final boolean _clone,
		final int _actionType )
	{
		Node summary = getSummaryNode();

		if ((summary != null) && _nodes.contains( summary ))
		{
			ArrayList nodes2 = new ArrayList( _nodes.size() - 1 );

			for (Object o : _nodes)
			{
				if (o != summary)
				{
					nodes2.add( o );
				}
			}

			_nodes = nodes2;
		}

		ArrayList parentNodes = new ArrayList( _nodes.size() );
		HierarchyUtils.extractParents( _nodes, parentNodes );

		if (!_clone)
		{
			return parentNodes;
		}

		Set assignedNodes = new HashSet();
		Map implMap = new HashMap();
		Set<Dependency> predecessors = new HashSet<Dependency>();
		Set<Dependency> successors = new HashSet<Dependency>();

		for (ListIterator i = parentNodes.listIterator(); i.hasNext();)
		{
			Node parent = (Node)i.next();
			Node newParent = cloneNode( parent, null, implMap, predecessors, successors );
			cloneBranch( parent, newParent, assignedNodes, implMap, predecessors, successors );
			i.remove();
			i.add( newParent );
		}

		//rebuild dependencies
		if (Environment.isKeepExternalLinks())
		{
			for (Dependency dependency : successors)
			{
				TaskLinkReference pt = (TaskLinkReference)dependency.getPredecessor();
				TaskLinkReference st = (TaskLinkReference)dependency.getSuccessor();

				HasDependencies predecessor = (Task)implMap.get( pt );
				HasDependencies successor = (Task)implMap.get( st );

				if (predecessor == null)
				{
					predecessor = new TaskLinkReferenceImpl(pt.getUniqueId(), pt.getProject());
				}

				if (successor == null)
				{
					successor = new TaskLinkReferenceImpl(st.getUniqueId(), st.getProject());
				}

				Dependency d = Dependency.getInstance( predecessor, successor, dependency.getDependencyType(), dependency.getLag() );
				d.setDirty( true );
				predecessor.getDependencyList( false ).add( d );
				successor.getDependencyList( true ).add( d );
				predecessors.remove( d );
			}

			for (Dependency dependency : predecessors)
			{
				TaskLinkReference pt = (TaskLinkReference)dependency.getPredecessor();
				TaskLinkReference st = (TaskLinkReference)dependency.getSuccessor();

				TaskLinkReference predecessor = (TaskLinkReference)implMap.get( pt );
				TaskLinkReference successor = (TaskLinkReference)implMap.get( st );

				if (predecessor == null)
				{
					predecessor = new TaskLinkReferenceImpl(pt.getUniqueId(), pt.getProject());
				}

				if (successor == null)
				{
					successor = new TaskLinkReferenceImpl(st.getUniqueId(), st.getProject());
				}

				Dependency d = Dependency.getInstance( predecessor, successor, dependency.getDependencyType(), dependency.getLag() );
				d.setDirty( true );
				predecessor.getDependencyList( false ).add( d );
				successor.getDependencyList( true ).add( d );

				//successors.remove(d);
			}
		}
		else
		{
			for (Dependency dependency : predecessors)
			{
				if (successors.contains( dependency ))
				{
					Task predecessor = (Task)implMap.get( dependency.getPredecessor() );
					Task successor = (Task)implMap.get( dependency.getSuccessor() );

					if ((predecessor != null) && (successor != null))
					{
						Dependency d = Dependency.getInstance( predecessor, successor, dependency.getDependencyType(),
								dependency.getLag() );
						d.setDirty( true );

						//Serializer.connectDependency(dependency, predecessor, successor);
						predecessor.getDependencyList( false ).add( d );
						successor.getDependencyList( true ).add( d );
					}
				}
			}
		}

		for (Iterator i = assignedNodes.iterator(); i.hasNext();)
		{
			addAssignments( (Node)i.next() );
		}

		for (ListIterator i = parentNodes.listIterator(); i.hasNext();)
		{
			Node node = (Node)i.next();
			cleanBranch( node );
		}

		return parentNodes;
	}

	@Override
	public List cut( 
		final List _nodes,
		final int _actionType )
	{
		return cut( _nodes, true, _actionType );
	}

	public List cut( 
		final List _nodes,
		final boolean _clone,
		final int _actionType )
	{
		List newNodes = copy( _nodes, _clone, _actionType );
		remove( _nodes, _actionType );

		return newNodes;

		//		ArrayList parentNodes=new ArrayList(nodes.size());
		//		HierarchyUtils.extractParents(nodes,parentNodes);
		//		remove(parentNodes,actionType);
		//		//TODO check parent is null
		//		return parentNodes;
	}

	/**
	 * @param arg0
	 * @param arg1
	 *
	 * @return
	 */
	@Override
	public Object getChild( 
		final Object _arg0,
		final int _arg1 )
	{
		return myHierarchy.getChild( _arg0, _arg1 );
	}

	/**
	 * @param arg0
	 *
	 * @return
	 */
	@Override
	public int getChildCount( 
		final Object _arg0 )
	{
		return myHierarchy.getChildCount( _arg0 );
	}

	@Override
	public List getChildren( 
		final Node _parent )
	{
		return getHierarchy().getChildren( _parent );
	}

	@Override
	public NodeModelDataFactory getDataFactory()
	{
		return myDataFactory;
	}

	@Override
	public Document getDocument()
	{
		return null;
	}

	public static ImplComparator getImplComparatorInstance()
	{
		if (myImplComparatorInstance == null)
		{
			myImplComparatorInstance = new ImplComparator();
		}

		return myImplComparatorInstance;
	}

	/**
	 * @return Returns the hierarchy.
	 */
	@Override
	public NodeHierarchy getHierarchy()
	{
		return myHierarchy;
	}

	/**
	 * @param arg0
	 * @param arg1
	 *
	 * @return
	 */
	@Override
	public int getIndexOfChild( 
		final Object _arg0,
		final Object _arg1 )
	{
		return myHierarchy.getIndexOfChild( _arg0, _arg1 );
	}

	@Override
	public Node getParent( 
		final Node _child )
	{
		return getHierarchy().getParent( _child );
	}

	/**
	 * @return
	 */
	@Override
	public Object getRoot()
	{
		return myHierarchy.getRoot();
	}

	//	protected int updateLevel=0;
	//	protected synchronized void beginUpdate(){
	//		updateLevel++;
	//	}
	//	protected synchronized void endUpdate(){
	//		updateLevel--;
	//	}
	//	protected synchronized int getUpdateLevel(){
	//		return updateLevel;
	//	}
	@Override
	public Node getSummaryNode()
	{
		return myHierarchy.getSummaryNode();
	}

	@Override
	public UndoController getUndoController()
	{
		return myUndoController;
	}

	@Override
	public UndoableEditSupport getUndoableEditSupport()
	{
		if (myUndoController == null)
		{
			return null;
		}

		return myUndoController.getEditSupport();
	}

	@Override
	public Node getVirtualroot()
	{
		return myHierarchy.getVirtualroot();
	}

	/*
	 * (non-Javadoc) @see com.projity.grouping.core.NodeModel#hasChildren(com.projity.grouping.core.Node)
	 */
	@Override
	public boolean hasChildren( 
		final Node _node )
	{
		return !myHierarchy.isLeaf( _node );
	}

	public boolean isAncestor( 
		final Node _parent,
		final Node _child )
	{
		if (_child == null)
		{
			return false;
		}

		if (_parent == _child)
		{
			return true;
		}

		return isAncestor( _parent, getParent( _child ) );
	}

	public boolean isAncestorOrDescendant( 
		final Node _one,
		final Node _two )
	{
		return isAncestor( _one, _two ) || isAncestor( _two, _one );
	}

	/**
	 * @param arg0
	 *
	 * @return
	 */
	@Override
	public boolean isLeaf( 
		final Object _arg0 )
	{
		return myHierarchy.isLeaf( _arg0 );
	}

	@Override
	public boolean isLocal()
	{
		return myLocal;
	}

	@Override
	public boolean isMaster()
	{
		return myMaster;
	}

	@Override
	public boolean isSummary( 
		final Node _node )
	{
		return myHierarchy.isSummary( _node );
	}

	@Override
	public boolean isSummaryNodeEnabled()
	{
		return myHierarchy.isSummaryNodeEnabled();
	}

	@Override
	public Iterator iterator()
	{
		return myHierarchy.iterator();
	}

	@Override
	public Iterator iterator( 
		final Node _rootNode )
	{
		return myHierarchy.iterator( _rootNode );
	}

	@Override
	public void move( 
		final Node _parent,
		final List _nodes,
		final int _position,
		final int _actionType )
	{
		if (!testAncestorOrDescendant( _parent, _nodes )) // don't allow circular
		{
			return;
		}

		int transactionId = DataFactoryUndoController.beginTransaction( this, _actionType );

		List cutNodes = cut( _nodes, false, _actionType );

		//List cutNodes=cut(nodes,actionType&UNDO); //TODO fixes bug 225 but it's probably breaking undo
		paste
		//List cutNodes=cut(nodes,actionType&UNDO); //TODO fixes bug 225 but it's probably breaking undo
		( _parent, cutNodes, _position, _actionType );

		DataFactoryUndoController.endTransaction( this, _actionType, transactionId );
	}

	@Override
	public Node newNode( 
		final Node _parent,
		final int _position,
		final int _actionType )
	{
		//check if position is correct
		Node node;
		int p = _position;
		int i = 0;

		for (Enumeration e = _parent.children(); e.hasMoreElements(); i++)
		{
			node = (Node)e.nextElement();

			if (i == p)
			{
				if (node.getValue() instanceof Assignment)
				{
					p++;
				}
				else
				{
					Node newNode = NodeFactory.getInstance().createVoidNode();
					add( _parent, newNode, p, NodeModel.NORMAL );

					return newNode;
				}
			}
		}

		Node newNode = NodeFactory.getInstance().createVoidNode();
		add( _parent, newNode, -1, NodeModel.NORMAL );

		return newNode;
	}

	@Override
	public void paste( 
		Node _parent,
		final List _nodes,
		final int _position,
		final int _actionType )
	{
		//nodes=copy(nodes,NodeModel.SILENT); //make an other copy, in case it is copied more than one time
		//done in transfert handler
		Node summary = getSummaryNode();

		if ((summary != null) && ((_parent == null) || _parent.isRoot()))
		{
			_parent = summary;
		}

		myHierarchy.paste( _parent, _nodes, _position, this, _actionType );

		//Undo
		if (isUndo( _actionType ))
		{
			postEdit( new NodePasteEdit( this, _parent, _nodes, _position ) );
		}
	}

	//	public void setUndoableEditSupport(UndoableEditSupport undoableEditSupport) {
	//		this.undoableEditSupport = undoableEditSupport;
	//	}
	public void postEdit( 
		final UndoableEdit _edit )
	{
		if (getUndoableEditSupport() != null)
		{
			getUndoableEditSupport().postEdit( _edit );
		}
	}

	@Override
	public void remove( 
		final Node _node,
		final int _actionType )
	{
		remove( _node, _actionType, true, true );
	}

	@Override
	public void remove( 
		final Node _node,
		final int _actionType,
		final boolean _removeDependencies )
	{
		remove( _node, _actionType, true, _removeDependencies );
	}

	public void remove( 
		final Node _node,
		final int _actionType,
		final boolean _filterAssignments,
		final boolean _removeDependencies )
	{
		ArrayList nodes = new ArrayList();
		nodes.add( _node );
		remove( nodes, _actionType, _filterAssignments, _removeDependencies );

		//myHierarchy.remove(node,this,actionType);
		//it calls back removeApartFromHierarchy for each node to remove
	}

	@Override
	public void remove( 
		final List _nodes,
		final int _actionType )
	{
		remove( _nodes, _actionType, true );
	}

	@Override
	public void remove( 
		final List _nodes,
		final int _actionType,
		final boolean _removeDependencies )
	{
		remove( _nodes, _actionType, true, _removeDependencies );
	}

	public void remove( 
		List _nodes,
		final int _actionType,
		final boolean _filterAssignments,
		final boolean _removeDependencies )
	{
		if ((myUndoController != null) && isUndo( _actionType ))
		{
			myUndoController.getEditSupport().beginUpdate();
		}

		try
		{
			Node summary = getSummaryNode();

			if ((summary != null) && _nodes.contains( summary ))
			{
				ArrayList nodes2 = new ArrayList( _nodes.size() - 1 );

				for (Object o : _nodes)
				{
					if (o != summary)
					{
						nodes2.add( o );
					}
				}

				_nodes = nodes2;
			}

			ArrayList roots = new ArrayList();
			HierarchyUtils.extractParents( _nodes, roots );

			if (_filterAssignments)
			{
				for (Iterator i = roots.iterator(); i.hasNext();)
				{
					Node node = (Node)i.next();

					if (node.getValue() instanceof Assignment)
					{
						i.remove();

						//AssignmentService.getInstance().remove(node, this, true);
					}
				}
			}

			boolean containsSubprojects = false;
			List parents = new ArrayList();
			List positions = new ArrayList();
			NodeBridge node;
			NodeBridge parent;

			for (Iterator i = roots.iterator(); i.hasNext();)
			{
				node = (NodeBridge)i.next();

				if (NodeModelUtil.nodeIsSubproject( node ))
				{
					containsSubprojects = true;
				}

				parent = (NodeBridge)node.getParent();
				parents.add( parent );
				positions.add( new Integer(parent.getIndex( node )) );
			}

			if (!confirmRemove( roots ))
			{
				return;
			}

			if ((myUndoController != null) && isUndo( _actionType ))
			{
				if (containsSubprojects)
				{
					myUndoController.clear();
				}
			}

			myHierarchy.remove( roots, this, _actionType, _removeDependencies );

			//it calls back removeApartFromHierarchy for each node to remove
			myHierarchy.checkEndVoidNodes( _actionType );

			//Undo
			if (myUndoController != null)
			{
				if (!containsSubprojects && isUndo( _actionType ))
				{
					postEdit( new NodeDeletionEdit(this, parents, roots, positions) );
				}
			}
		}
		finally
		{
			if (myUndoController != null)
			{
				if (isUndo( _actionType ))
				{
					myUndoController.getEditSupport().endUpdate();
				}
			}
		}
	}

	@Override
	public void removeAll( 
		final int _actionType )
	{
		myHierarchy.removeAll( this, _actionType );
	}

	@Override
	public boolean removeApartFromHierarchy( 
		final Node _node,
		final boolean _cleanAssignment,
		final int _actionType,
		final boolean _removeDependencies )
	{
		if (!isEvent( _actionType ))
		{
			return true;
		}

		//		try {
		//			beginUpdate();
		if (_node.getValue() instanceof Assignment)
		{
			Assignment assignment = (Assignment)_node.getValue();

			//				if (cleanAssignment)
			AssignmentService.getInstance().remove( assignment, _cleanAssignment, this, isUndo( _actionType ) ); //LC 8/4/2006 - hk 7/8/2006 changed null to this so event will be fired
																											   //				else if (assignment.getResource()!=ResourceImpl.getUnassignedInstance()){
																											   //					assignment.getResource().removeAssignment(assignment);
																											   //				}

			//AssignmentService.getInstance().remove((Assignment)node.getValue(),this);
		}
		else if ((myDataFactory != null) && !_node.isVoid())
		{
			myDataFactory.remove( _node.getValue(), this, false, isUndo( _actionType ), _removeDependencies ); //TODO make this work properly with subproject
		} //		} finally {
		  //			endUpdate();
		  //		}

		return true;
	}

	/**
	 * @param arg0
	 */
	@Override
	public void removeTreeModelListener( 
		final TreeModelListener _arg0 )
	{
		myHierarchy.removeTreeModelListener( _arg0 );
	}

	@Override
	public Node replaceImpl( 
		final Node _node,
		final Object _newImpl,
		final Object _eventSource,
		final int _actionType )
	{
		final Node parent = getParent( _node );
		final Object parentImpl = (parent == getHierarchy().getRoot())
			? null
			: parent.getValue();
		final NodeModelDataFactory factory = getFactory( parentImpl );

		factory.addUnvalidatedObject( _newImpl, this, parentImpl );

		Object oldImpl = _node.getValue();
		_node.setValue( _newImpl );
		factory.validateObject( _newImpl, this, _eventSource, null, false );

		myHierarchy.renumber();

//		myDataFactory.fireCreated(newImpl);
		getHierarchy().fireRemoval( 
			new Node[]
		{
			_node
		} ); //TODO Cause critical path to run twice

		myHierarchy.checkEndVoidNodes( _actionType );

		//Undo
		if (isUndo( _actionType ) == true)
		{
			postEdit( new NodeImplChangeEdit( this, _node, oldImpl, _eventSource ) );
		}

		return _node;
	}

	@Override
	public Node replaceImplAndSetFieldValue( 
		final Node _node,
		final LinkedList _previous,
		final Field _field,
		final Object _eventSource,
		final Object _value,
		final FieldContext _context,
		final int _actionType )
		throws FieldParseException
	{
		//the line following a subproject is connected to the main project
		if ((_previous != null) && (_previous.size() > 0))
		{
			Node p = (Node)_previous.getFirst();

			if ((p != null) && p.isInSubproject() && (_node.getSubprojectLevel() < p.getSubprojectLevel()))
			{
				while (_node.getSubprojectLevel() < p.getSubprojectLevel())
				{
					p = (Node)p.getParent();
				}

				LinkedList newPrevious = new LinkedList();
				newPrevious.add( p );

				Node vn;
				Node pvn;

				for (Iterator i = _previous.iterator(); i.hasNext();)
				{
					vn = (Node)i.next();
					pvn = (Node)vn.getParent();

					while ((pvn != null) && (pvn != p))
					{
						pvn = (Node)pvn.getParent();
					}

					if (pvn != p)
					{
						newPrevious.add( vn );
					}
				}

				Object parentImpl = p.getValue();
				NodeModelDataFactory factory = getFactory( parentImpl );

				return replaceImplAndSetFieldValue( _node, newPrevious, factory.createUnvalidatedObject( this, parentImpl ),
					_field, _eventSource, _value, _context, _actionType );
			}

			//			if (p!=null&&p.getValue() instanceof NormalTask){
			//				Task task=(Task)p.getValue();
			//				boolean subprojectParent=false;
			//				while (task.getOwningProject()!=task.getProject()){
			//					Node pParent=(Node)p.getParent();
			//					if (pParent.getIndex(p)==pParent.getChildCount()-1){
			//						p=pParent;
			//						subprojectParent=true;
			//					}else{
			//						subprojectParent=false;
			//						break;
			//					}
			//				}
			//				if (subprojectParent){
			//					LinkedList newPrevious=(LinkedList)previous.clone();
			//					newPrevious.set(0, p);
			//					Object parentImpl = p.getValue();
			//					NodeModelDataFactory factory = getFactory(parentImpl);
			//					return replaceImplAndSetFieldValue(node,newPrevious,factory.createUnvalidatedObject(this, parentImpl),field,eventSource,value,context,actionType);
			//
			//				}
			//			}
		}

		Node parent = (Node)_node.getParent();

//		Node summaryNode=getSummaryNode();
//		if (summaryNode!=null&&parent==getHierarchy().getRoot()){
//			parent=summaryNode;
//			node.setParent(parent);
//		}
		
		Object parentImpl = (parent == getHierarchy().getRoot())
			? null
			: parent.getValue();
		NodeModelDataFactory factory = getFactory( parentImpl );

		return replaceImplAndSetFieldValue( _node, _previous, factory.createUnvalidatedObject( this, parentImpl ), _field,
			_eventSource, _value, _context, _actionType );
	}

	@Override
	public Node replaceImplAndSetFieldValue( 
		final Node _node,
		final LinkedList _previous,
		final Object _newImpl,
		final Field _field,
		final Object _eventSource,
		final Object _value,
		final FieldContext _context,
		final int _actionType )
		throws FieldParseException
	{
		List previousPosition = null;

		//move in hierarchy
		if (_previous != null)
		{
			LinkedList p = (LinkedList)_previous.clone();
			Node sibling = (Node)p.removeFirst();
			Node parent = (sibling == getSummaryNode())
				? sibling
				: (Node)sibling.getParent();

			p.add( _node );

			if ((getUndoableEditSupport() != null) & isUndo( _actionType ))
			{
				previousPosition = new ArrayList(p.size());

				for (Iterator i = p.iterator(); i.hasNext();)
				{
					Node n = (Node)i.next();
					previousPosition.add( new NodeImplChangeAndValueSetEdit.Position((Node)n.getParent(), n,
							n.getParent().getIndex( n )) );
				}
			}

			remove( p, NodeModel.SILENT );
			add( parent, p, parent.getIndex( sibling ) + 1, NodeModel.SILENT );

			//TODO need undo here
		}

		Node parent = (Node)_node.getParent();

		Object parentImpl = (parent == getHierarchy().getRoot())
			? null
			: parent.getValue();
		NodeModelDataFactory factory = getFactory( parentImpl );
		factory.addUnvalidatedObject( _newImpl, this, parentImpl );

		Object oldImpl = _node.getValue();
		_node.setValue( _newImpl );

		try
		{
			NodeModelUtil.setValue( _field, _node, this, null, _value, _context ); // will throw if error
		}
		catch (FieldParseException e)
		{
			factory.rollbackUnvalidated( this, _newImpl ); // in some cases, such as ValueObjectForInterval, some cleanup is needed
			throw e;
		}

		// if no exception was thrown, then validate the object and hook it into model
		factory.validateObject( _newImpl, this, _eventSource, null, true );

		myHierarchy.renumber();

		//		myDataFactory.fireCreated(newImpl);
		myHierarchy.checkEndVoidNodes( _actionType ^ NodeModel.EVENT );
		getHierarchy().fireInsertion( new Node[]
			{
				_node
			} ); //TODO Cause critical path to run twice

		//Undo
		if (isUndo( _actionType ))
		{
			postEdit( new NodeImplChangeAndValueSetEdit( this, _node, _previous, previousPosition, oldImpl, _field, _value, 
				_context, _eventSource ) );
		}

		return _node;
	}

	/**
	 * @param _key
	 * @param _comparator
	 *
	 * @return
	 */
	@Override
	public Node search( 
		final Object _key,
		final Comparator _comparator )
	{
		return myHierarchy.search( _key, _comparator );
	}

	@Override
	public Node search( 
		final Object _key )
	{
		//TODO consider using a hashtable instead of searching like this
		return search( _key, getImplComparatorInstance() );
	}

	/**
	 * @param _dataFactory The dataFactory to set.
	 */
	@Override
	public void setDataFactory( 
		final NodeModelDataFactory _dataFactory )
	{
		myDataFactory = _dataFactory;
	}

	@Override
	public void setFieldValue( 
		final Field _field,
		final Node _node,
		final Object _eventSource,
		final Object _value,
		final FieldContext _context,
		final int _actionType )
		throws FieldParseException
	{
		setFieldValue( _field, _node, _eventSource, _value, _context, null, _actionType );
	}

	/*
	 * (non-Javadoc) @see com.projity.grouping.core.NodeModel#setFieldValue(com.projity.field.Field,
	 * com.projity.grouping.core.Node, java.lang.Object, java.lang.Object, com.projity.field.FieldContext)
	 */
	@Override
	public void setFieldValue( 
		final Field _field,
		final Node _node,
		final Object _eventSource,
		final Object _value,
		final FieldContext _context,
		FieldSetOptions _options,
		final int _actionType )
		throws FieldParseException
	{
		//Object oldValue=field.getValue(node,this,context);

		//		// this prevents the field from sending an update message.  However, ideally the field will send the message and the hiearchy event wont
		//		if (context != null)
		//			context.setUserObject(FieldContext.getNoUpdateInstance());
		if (isUndo( _actionType ))
		{
			if (_options == null)
			{
				_options = new FieldSetOptions();
			}

			_options.setRawUndo( true );
		}

		NodeModelUtil.setValue( _field, _node, this, _eventSource, _value, _context, _options );

		//		No longer sending update event
		//		if (isEvent(actionType)) myHierarchy.fireUpdate(new Node[]{node});
		//TODO treat the ObjectEvent instead
		if ((_options != null) && (_options.getChange() != null))
		{
			FieldSetOptions undoOptions = new FieldSetOptions();
			undoOptions.setRawProperties( true );

			ModelFieldEdit edit = new ModelFieldEdit( this, _field, _node, _eventSource, _options.getChange().getNewValue(),
					_options.getChange().getOldValue(), _context, undoOptions);
			postEdit( edit );
		}
	}

	/**
	 * @param hierarchy The hierarchy to set.
	 */
	@Override
	public void setHierarchy( 
		final NodeHierarchy _hierarchy )
	{
		myHierarchy = _hierarchy;
	}

	@Override
	public void setLocal( 
		final boolean _local )
	{
		myLocal = _local;
	}

	@Override
	public void setMaster( 
		final boolean _master )
	{
		myMaster = _master;
	}

	@Override
	public void setSummaryNodeEnabled( 
		final boolean _summayTaskEnabled )
	{
		myHierarchy.setSummaryNodeEnabled( _summayTaskEnabled );
	}

	@Override
	public void setUndoController( 
		final UndoController _undoController )
	{
		myUndoController = _undoController;
	}

	@Override
	public Iterator shallowIterator( 
		final int _maxLevel,
		boolean _returnRoot )
	{
		return myHierarchy.shallowIterator( _maxLevel, _returnRoot );
	}

	public boolean testAncestorOrDescendant( 
		final Node _one,
		final List _nodes )
	{
		Iterator i = _nodes.iterator();

		while (i.hasNext())
		{
			if (isAncestorOrDescendant( _one, (Node)i.next() ))
			{
				return false;
			}
		}

		return true;
	}

	/**
	 * @param _arg0
	 * @param _arg1
	 */
	@Override
	public void valueForPathChanged( 
		final TreePath _arg0,
		final Object _arg1 )
	{
		myHierarchy.valueForPathChanged( _arg0, _arg1 );
	}

	private void addAssignments( 
		final Node _node )
	{
		if (_node.getValue() instanceof HasAssignments)
		{
			AssociationList assignments = ((HasAssignments)_node.getValue()).getAssignments();

			if (assignments == null)
			{
				return;
			}

			for (ListIterator i = assignments.listIterator( assignments.size() ); i.hasPrevious();)
			{
				Assignment assignment = (Assignment)i.previous();

				if (assignment.isDefault())
				{
					continue;
				}

				Node assignmentNode = NodeFactory.getInstance().createNode( assignment );
				_node.insert( assignmentNode, 0 );
			}
		}
	}

	private void cleanBranch( 
		final Node _parent )
	{
		for (Iterator i = _parent.childrenIterator(); i.hasNext();)
		{
			Node child = (Node)i.next();
			cleanNodeImpl( child.getValue() );
			cleanBranch( child );
		}
	}

	private void cleanNodeImpl( 
		final Object _impl )
	{
		if (_impl instanceof Task)
		{
			((Task)_impl).cleanClone();
		}
		else if (_impl instanceof ResourceImpl)
		{
			((ResourceImpl)_impl).cleanClone();
		}
	}

	private void cloneBranch( 
		final Node _parent,
		final Node _newParent,
		final Set _assignedNodes,
		final Map _implMap,
		final Set<Dependency> _predecessors,
		final Set<Dependency> _successors )
	{
		for (Iterator i = _parent.childrenIterator(); i.hasNext();)
		{
			Node child = (Node)i.next();

			if (child.getValue() instanceof Assignment)
			{
				_assignedNodes.add( _newParent );
			}
			else
			{
				Node newChild = cloneNode( child, _newParent, _implMap, _predecessors, _successors );
				cloneBranch( child, newChild, _assignedNodes, _implMap, _predecessors, _successors );
			}
		}
	}

	private Node cloneNode( 
		final Node _oldNode,
		final Node _newParent,
		final Map _implMap,
		final Set<Dependency> _predecessors,
		final Set<Dependency> _successors )
	{
		Object oldNodeImpl = _oldNode.getValue();
		Object newNodeImpl = cloneNodeImpl( oldNodeImpl );
		_implMap.put( oldNodeImpl, newNodeImpl );

		if (oldNodeImpl instanceof Task)
		{
			Task t = (Task)oldNodeImpl;
			_predecessors.addAll( t.getDependencyList( true ) );
			_successors.addAll( t.getDependencyList( false ) );
		}

		Object parentImpl = (_newParent == null)
			? null
			: _newParent.getValue();
		NodeModelDataFactory factory = getFactory( parentImpl );

		factory.addUnvalidatedObject( newNodeImpl, this, parentImpl );

		Node newNode = NodeFactory.getInstance().createNode( newNodeImpl );

		if (_newParent != null)
		{
			_newParent.add( newNode );
		}

		if ((parentImpl != null) && parentImpl instanceof Task)
		{
			((Task)parentImpl).setWbsChildrenNodes( getHierarchy().getChildren( _newParent ) ); //rebuild children task's wbs cache
		}

		return newNode;
	}

	private Object cloneNodeImpl( 
		final Object _impl )
	{
		if (_impl instanceof VoidNodeImpl)
		{
			return new VoidNodeImpl();
		}
		else if (_impl instanceof Task)
		{
			return ((Task)_impl).clone();
		}
		else if (_impl instanceof ResourceImpl)
		{
			return ((ResourceImpl)_impl).clone();
		}

		//TOTO assignments
		return null;
	}

	private NodeModelDataFactory getFactory( 
		final Object _parentImpl )
	{
		if (_parentImpl == null)
		{
			return myDataFactory;
		}
		else
		{
			return myDataFactory.getFactoryToUseForChildOfParent( _parentImpl );
		}
	}

	public static class ImplComparator
		implements Comparator
	{
		ImplComparator()
		{
		}

		@Override
		public int compare( 
			final Object _node,
			final Object _impl )
		{
			if (((Node)_node).getValue() == _impl)
			{
				return 0;
			}
			else
			{
				return 1;
			}
		}
	}

	private static ImplComparator myImplComparatorInstance = null;
	protected NodeHierarchy myHierarchy;
	protected NodeModelDataFactory myDataFactory = null;
	protected UndoController myUndoController;
	protected boolean myLocal;
	protected boolean myMaster = true;
}
