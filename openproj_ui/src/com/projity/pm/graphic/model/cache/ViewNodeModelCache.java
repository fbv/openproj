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
package com.projity.pm.graphic.model.cache;

import com.projity.association.InvalidAssociationException;

import com.projity.grouping.core.Node;
import com.projity.grouping.core.model.NodeModel;
import com.projity.grouping.core.model.WalkersNodeModel;
import com.projity.grouping.core.transform.TransformList;
import com.projity.grouping.core.transform.ViewTransformerEvent;
import com.projity.grouping.core.transform.ViewTransformerListener;

import com.projity.pm.graphic.model.event.CacheListener;
import com.projity.pm.graphic.model.event.CompositeCacheEvent;
import com.projity.pm.graphic.model.transform.DependencyCacheTransformer;
import com.projity.pm.graphic.model.transform.NodeCacheTransformer;
import com.projity.pm.task.Project;
import com.projity.pm.task.SubProj;

import com.projity.server.data.mspdi.ProjectContentHandler;

import com.projity.strings.Messages;

import com.projity.util.Alert;

import org.apache.commons.collections.Closure;

import org.openproj.domain.task.Task;

import java.text.MessageFormat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;


/**
 *
 */
public class ViewNodeModelCache
	implements NodeModelCache,
		ViewTransformerListener,
		CacheListener
{
	ViewNodeModelCache( 
		final ReferenceNodeModelCache _reference,
		final String _viewName,
		final Closure _transformerClosure )
	{
		this( _reference, new VisibleNodes( _viewName, new NodeCacheTransformer( _viewName, _reference, _transformerClosure ) ),
			new VisibleDependencies( _viewName, new DependencyCacheTransformer( _viewName, _reference ) ) );
		myViewName = _viewName;
	}

	/**
	 * @param _reference
	 * @param _visibleNodes
	 * @param _visibleDependencies
	 */
	private ViewNodeModelCache( 
		final ReferenceNodeModelCache _reference,
		final VisibleNodes _visibleNodes,
		final VisibleDependencies _visibleDependencies )
	{
		myReference = _reference;
		myVisibleNodes = _visibleNodes;
		myVisibleDependencies = _visibleDependencies;
		addNodeModelListener( this );
		_visibleDependencies.setVisibleNodes( _visibleNodes );
		_visibleNodes.setVisibleDependencies( _visibleDependencies );
		_reference.bindView( _visibleNodes, _visibleDependencies );
		((NodeCacheTransformer)_visibleNodes.getTransformer()).getTransformer().addViewTransformerListener( this );
	}

	@Override
	public void addNodeModelListener( 
		final CacheListener _listener )
	{
		myVisibleNodes.addNodeModelListener( _listener );
	}

	@Override
	public void addNodes( 
		final Node _sibling,
		final List _nodes )
	{
		getModel().addBefore( _sibling, _nodes, NodeModel.NORMAL );
	}

	@Override
	public void addTreeModelListener( 
		final TreeModelListener _listener )
	{
		myTreeModelListenerList.add( TreeModelListener.class, _listener );
	}

	@Override
	public void changeCollapsedState( 
		final GraphicNode _node )
	{
		myReference.changeCollapsedState( _node );
	}

	@Override
	public void close()
	{
	}

	//returns same list with converted elements
	private List convertToBase( 
		final List _gnodes )
	{
		if (_gnodes == null)
		{
			return null;
		}

		for (ListIterator i = _gnodes.listIterator(); i.hasNext();)
		{
			i.set( ((GraphicNode)i.next()).getNode() );
		}

		return _gnodes;
	}

	@Override
	public void copyNodes( 
		final List _nodes )
	{
		final List newNodes = getModel().copy( _nodes, NodeModel.NORMAL );
		_nodes.clear();
		_nodes.addAll( newNodes );
	}

	@Override
	public void createDependency( 
		final GraphicNode _startNode,
		final GraphicNode _endNode )
		throws InvalidAssociationException
	{
		myReference.createDependency( _startNode, _endNode );
	}

	@Override
	public void createHierarchyDependency( 
		final GraphicNode startNode,
		final GraphicNode endNode )
		throws InvalidAssociationException
	{
		myReference.createDependency( startNode, endNode );
	}

	@Override
	public void cutNodes( 
		final List _nodes )
	{
		if (!isAllowedAction( _nodes, false ))
		{
			return;
		}

		//error message for deletion of nodes that are protected
		//usually the node is simply ignored but here it's a total cancel of the cut
		final boolean[] protectedFound = new boolean[]
		{
			false
		};

		for (Iterator i = _nodes.iterator(); i.hasNext() && !protectedFound[ 0 ];)
		{
			Node parent = (Node)i.next();
			getModel().getHierarchy().visitAll( parent,
				new Closure()
			{
				@Override
				public void execute( 
					Object arg )
				{
					if (protectedFound[ 0 ])
					{
						return; //iterators would be better here
					}

					Node node = (Node)arg;

					if (node.isProtected())
					{
						node.showProtectionWarning();
						protectedFound[ 0 ] = true;
					}
				}
			} );
		}

		if (protectedFound[ 0 ])
		{
			return;
		}

		List newNodes = getModel().cut( _nodes, NodeModel.NORMAL );
		_nodes.clear();
		_nodes.addAll( newNodes );
	}

	@Override
	public void deleteNodes( 
		final List _nodes )
	{
		if (!isAllowedAction( _nodes, false ))
		{
			return;
		}

		//error message for deletion of nodes that are protected
		//usually the node is simply ignored but here it's a total cancel of the remove
		final boolean[] protectedFound = new boolean[]
		{
			false
		};

		for (Iterator i = _nodes.iterator(); i.hasNext() && !protectedFound[ 0 ];)
		{
			Node parent = (Node)i.next();
			getModel().getHierarchy().visitAll( parent,
				new Closure()
			{
				@Override
				public void execute( 
					Object _arg )
				{
					if (protectedFound[ 0 ])
					{
						return; //iterators would be better here
					}

					final Node node = (Node)_arg;

					if (node.isProtected())
					{
						node.showProtectionWarning();
						protectedFound[ 0 ] = true;
					}
				}
			} );
		}

		if (protectedFound[ 0 ])
		{
			return;
		}

		getModel().remove( _nodes, NodeModel.NORMAL );
	}

	@Override
	public void expandNodes( 
		final List _nodes,
		final boolean _expand )
	{
		if (_nodes == null)
		{
			return;
		}

		if (_nodes.size() > 0)
		{
			final Iterator i = _nodes.iterator();

			while (i.hasNext())
			{
				final GraphicNode gnode = (GraphicNode)i.next();

				if (_expand && !gnode.isFetched()) // for subprojects
				{
					gnode.fetch();
				}

				if (gnode.isCollapsed() == _expand)
				{
					changeCollapsedState( gnode );
				}
			}
		}
	}

	protected void fireTreeModelUpdate( 
		final Object _source )
	{
		final Object[] listeners = myTreeModelListenerList.getListenerList();
		TreeModelEvent e = null;

		for (int i = listeners.length - 2; i >= 0; i -= 2)
		{
			if (listeners[ i ] == TreeModelListener.class)
			{
				if (e == null)
				{
					e = new TreeModelEvent( _source, new Object[]
					{
						getRoot()
					});
				}

				((TreeModelListener)listeners[ i + 1 ]).treeStructureChanged( e );
			}
		}
	}

	//TreeModel
	@Override
	public Object getChild( 
		final Object _obj,
		final int _index )
	{
		final ListIterator i = getIterator();
		GraphicNode node;
		GraphicNode ref = null;

		if (_obj == getRoot())
		{
			ref = (GraphicNode)getRoot();
		}
		else
		{
			while (i.hasNext())
			{
				node = (GraphicNode)i.next();

				if (node == _obj)
				{
					ref = node;

					break;
				}
			}
		}

		if (ref == null)
		{
			return null;
		}

		int count = 0;

		while (i.hasNext())
		{
			node = (GraphicNode)i.next();

			if (node.getLevel() <= ref.getLevel())
			{
				break;
			}
			else if (node.getLevel() == (ref.getLevel() + 1))
			{
				if (count == _index)
				{
					return node;
				}

				count++;
			}
		}

		return null;
	}

	@Override
	public int getChildCount( 
		final Object _obj )
	{
		final ListIterator i = getIterator();
		GraphicNode node;
		GraphicNode ref = null;

		if (_obj == getRoot())
		{
			ref = (GraphicNode)getRoot();
		}
		else
		{
			while (i.hasNext())
			{
				node = (GraphicNode)i.next();

				if (node == _obj)
				{
					ref = node;

					break;
				}
			}
		}

		int count = 0;

		if (ref != null)
		{
			while (i.hasNext())
			{
				node = (GraphicNode)i.next();

				if (node.getLevel() <= ref.getLevel())
				{
					break;
				}
				else if (node.getLevel() == (ref.getLevel() + 1))
				{
					count++;
				}
			}
		}

		return count;
	}

	@Override
	public List getChildren( 
		final GraphicNode _node )
	{
		final List children = myReference.getChildren( _node );

		if (children == null)
		{
			return null;
		}

		final List elements = myVisibleNodes.getElements();

		for (Iterator i = children.iterator(); i.hasNext();)
		{
			if (!elements.contains( i.next() ))
			{
				i.remove();
			}
		}

		return children;
	}

	@Override
	public Object getEdgeElementAt( 
		final int _i )
	{
		return myVisibleDependencies.getElementAt( _i );
	}

	@Override
	public ListIterator getEdgesIterator()
	{
		return myVisibleDependencies.getIterator();
	}

	@Override
	public ListIterator getEdgesIterator( 
		final int _i )
	{
		return myVisibleDependencies.getIterator( _i );
	}

	@Override
	public int getEdgesSize()
	{
		return myVisibleDependencies.getSize();
	}

	@Override
	public Object getElementAt( 
		final int _i )
	{
		return myVisibleNodes.getElementAt( _i );
	}

	@Override
	public List getElementsAt( 
		final int[] _i )
	{
		final ArrayList list = new ArrayList(_i.length);

		for (int j = 0; j < _i.length; j++)
		{
			final Object element = getElementAt( _i[ j ] );

			if (element != null)
			{
				list.add( element );
			}
		}

		return list;
	}

	@Override
	public Object getGraphicDependency( 
		final Object _base )
	{
		return myReference.getGraphicDependency( _base );
	}

	@Override
	public Object getGraphicNode( 
		final Object _base )
	{
		return myReference.getGraphicNode( _base );
	}

	@Override
	public int getIndexOfChild( 
		final Object _parent,
		final Object _child )
	{
		final ListIterator i = getIterator();
		GraphicNode node;
		GraphicNode ref = null;

		if (_parent == getRoot())
		{
			ref = (GraphicNode)getRoot();
		}
		else
		{
			while (i.hasNext())
			{
				node = (GraphicNode)i.next();

				if (node == _parent)
				{
					ref = node;

					break;
				}
			}
		}

		if (ref == null)
		{
			return -1;
		}

		int count = 0;

		while (i.hasNext())
		{
			node = (GraphicNode)i.next();

			if (node.getLevel() <= ref.getLevel())
			{
				break;
			}
			else if (node.getLevel() == (ref.getLevel() + 1))
			{
				if (node == _child)
				{
					return count;
				}

				count++;
			}
		}

		return -1;
	}

	@Override
	public ListIterator getIterator()
	{
		return myVisibleNodes.getIterator();
	}

	@Override
	public ListIterator getIterator( 
		final int _i )
	{
		return myVisibleNodes.getIterator( _i );
	}

	private int getLastNormalRow()
	{
		for (int i = myVisibleNodes.getSize() - 1; i >= 0; i--)
		{
			final GraphicNode current = (GraphicNode)myVisibleNodes.getElementAt( i );

			if (!current.isVoid())
			{
				return i;
			}
		}

		return -1;
	}

//    /**
//	 * Returns the parent/previous,position identification of the position
//	 * to insert a void node.
//	 * The fist normal node preceding it.
//	 * Same level if the node isn't composite and collapsed
//	 * @param row
//	 * @return
//	 */
//	private NodeHierarchyVoidLocation getVoidNodeCreationInfoObject(GraphicNode refNode){
//	    int row=getRowAt(refNode);
//		if (row==0){
//		    int lastRow=getLastNormalRow();
//		    if (row>lastRow) return new NodeHierarchyVoidLocation(NodeHierarchyLocation.END_LOCATION,row-lastRow);
//		    return new NodeHierarchyVoidLocation(new NodeHierarchyLocation(null,null),1);
//		}
//    	GraphicNode node=(GraphicNode)myVisibleNodes.getElementAt(row-1);
//    	if (node.isVoid()){
//    		NodeHierarchyVoidLocation info=getVoidNodeInfoObject(row-1);
//    		info.setPosition(info.getPosition()+1);
//    		return info;
//    	}else{
//    	    Node parent;
//    	    if (node.isSummary()&&!(node.isCollapsed())){
//    	    	parent=node.getNode();
//    	    }else{
//    	    	parent=getModel().getHierarchy().getParent(node.getNode());
//    	    }
//    	    return new NodeHierarchyVoidLocation(new NodeHierarchyLocation(parent,node.getNode()),1);
//    	}
//	}
//
//	/**
//	 * Returns the parent/previous,position identification of the void node at row
//	 * Apply this to a void node row only
//	 * @param row
//	 * @return
//	 */
//	public NodeHierarchyVoidLocation getVoidNodeInfoObject(GraphicNode refNode){
//	    int row=getRowAt(refNode);
//	    return getVoidNodeInfoObject(row);
//	}
//	private NodeHierarchyVoidLocation getVoidNodeInfoObject(int row){
//
//	    int lastRow=getLastNormalRow();
//	    if (row>lastRow){
//		    GraphicNode gnode=(lastRow>=0)?(GraphicNode)myVisibleNodes.getElementAt(lastRow):null;
//	    	return new NodeHierarchyVoidLocation(NodeHierarchyLocation.END_LOCATION,row-lastRow,(gnode==null)?1:gnode.getLevel());
//	    }
//
//	    //Find the normal node just before the series of void nodes
//	    //It must be a sibling or a parent
//	    GraphicNode gnode=null;
//	    GraphicNode node0=(GraphicNode)myVisibleNodes.getElementAt(row);
//        for (int i=row-1;i>-1;i--){
//        	GraphicNode current=(GraphicNode)myVisibleNodes.getElementAt(i);
//	        if (!current.isVoid()&&getLevel(current)<=getLevel(node0)){
//	        	gnode=current;
//	        	break;
//	        }
//	    }
//
//	    //find the position of the void node in the series
//        //1 is the first one
//        int pos=1;
//        int voidLevel=getLevel(node0);
//        for (;pos<=row;pos++){
//           	GraphicNode current=(GraphicNode)myVisibleNodes.getElementAt(row-pos);
//           	if (!(current.isVoid()&&getLevel(current)==voidLevel))
//           		break;
//        }
//
//
//
//	    if (gnode==null) return new NodeHierarchyVoidLocation(new NodeHierarchyLocation(null,null),pos);
//
//	    //find the first non void node of level>level of void node
//	    //It is gnode or the parent of gnode
//	    Node parent;
//	    if (getLevel(gnode)<getLevel(node0)){
//	    	parent=gnode.getNode();
//	    }else{
//	    	parent=getModel().getHierarchy().getParent(gnode.getNode());
//	    }
//
//	    return new NodeHierarchyVoidLocation(new NodeHierarchyLocation(parent,gnode.getNode()),pos);
//	}

	@Override
	public int getLevel( 
		final GraphicNode _node )
	{
		if (_node.isGroup() == true)
		{
			return _node.getLevel();
		}

		NodeCacheTransformer transformer = (NodeCacheTransformer)myVisibleNodes.getTransformer();

		return _node.getLevel() + transformer.getLevelOffset();
	}

//	public void forEach(CacheClosure c){
//	   Stack stack=new Stack();
//	   GraphicNode node=null,nextNode=null,history;
//	   if (getSize()==0) return;
//	   ListIterator i=getIterator();
//	   node=(GraphicNode)i.next();
//	   while (i.hasNext()){
//	   		nextNode=(GraphicNode)i.next();
//	   		if (nextNode.getLevel()>node.getLevel()){
//	   			stack.push(node);
//	   		}else{
//	   			c.execute(node,nextNode.getLevel()-node.getLevel());
//	   			while (((GraphicNode)stack.peek()).getLevel()>=nextNode.getLevel())
//					c.execute((GraphicNode)stack.pop(),nextNode.getLevel()-node.getLevel());
//	   		}
//	   		node=nextNode;
//	   }
//	   if (nextNode!=null) c.execute(nextNode,nextNode.getLevel()-node.getLevel());
//	}
	
	@Override
	public int getMaxLevel()
	{
		int level = 0;
		GraphicNode node;

		for (Iterator i = getIterator(); i.hasNext();)
		{
			node = (GraphicNode)i.next();

			if (node.getLevel() > level)
			{
				level = node.getLevel();
			}
		}

		return level;
	}

	@Override
	public NodeModel getModel()
	{
		return myReference.getModel();
	}

	@Override
	public CacheListener[] getNodeModelListeners()
	{
		return myVisibleNodes.getNodeModelListeners();
	}

	@Override
	public List getNodesAt( 
		final int[] _i )
	{
		final ArrayList list = new ArrayList( _i.length );

		for (int j = 0; j < _i.length; j++)
		{
			Object base = ((GraphicNode)getElementAt( _i[ j ] )).getNode();

			if (base != null)
			{
				list.add( base );
			}
		}

		return list;
	}

	@Override
	public GraphicNode getParent( 
		final GraphicNode _node )
	{
		final GraphicNode parent = myReference.getParent( _node );

		if (myVisibleNodes.getElements().contains( _node ))
		{
			return parent;
		}
		else
		{
			return null;
		}
	}

	@Override
	public int getPertLevel( 
		final GraphicNode _node )
	{
		return _node.getPertLevel();
	}

	@Override
	public ReferenceNodeModelCache getReference()
	{
		return myReference;
	}

	@Override
	public Object getRoot()
	{
		return myReference.getRoot();
	}

	@Override
	public int getRowAt( 
		Object node )
	{
		return myVisibleNodes.getRow( node );
	}

	@Override
	public int getSize()
	{
		return myVisibleNodes.getSize();
	}

	@Override
	public int getType()
	{
		return myReference.getType();
	}

	public String getViewName()
	{
		return myViewName;
	}

	@Override
	public VisibleDependencies getVisibleDependencies()
	{
		return myVisibleDependencies;
	}

	@Override
	public VisibleNodes getVisibleNodes()
	{
		return myVisibleNodes;
	}

	@Override
	public WalkersNodeModel getWalkersModel()
	{
		final NodeCacheTransformer transformer = (NodeCacheTransformer)myVisibleNodes.getTransformer();

		return transformer.getWalkersModel();
	}

	@Override
	public void graphicNodesCompositeEvent( 
		final CompositeCacheEvent _event )
	{
		fireTreeModelUpdate( this );
	}

	@Override
	public void indentNodes( 
		final List _nodes )
	{
		if (_nodes == null)
		{
			return;
		}

		if (!isAllowedAction( _nodes, false ))
		{
			return;
		}

		final List validNodes = TransformList.getNotVoidFilter().filterList( convertToBase( _nodes ) );

		if (validNodes.size() > 0)
		{
			getModel().getHierarchy().indent( validNodes, 1, getModel(), NodeModel.NORMAL );
		}
	}

	private boolean isAllowedAction( 
		final Node _node,
		final boolean _isParent )
	{
		if ((_node != null) && (_node.getValue() instanceof Task))
		{
			boolean r = true;
			final Task t = (Task)_node.getValue();

			if (t instanceof SubProj)
			{
				final Project p = _isParent
					? ((SubProj)t).getSubproject()
					: t.getOwningProject();

				if ((p != null) && p.isReadOnly())
				{
					r = false;
				}
			}
			else if (t.isRoot())
			{
				r = true;
			}
			else
			{
				r = !t.isReadOnly();
			}

			if (!r)
			{
				Alert.error( MessageFormat.format( Messages.getString( "Message.readOnlyTask" ), new Object[]
				{
					t.getName()
				} ) );
			}

			return r;
		}

		return true;
	}

	private boolean isAllowedAction( 
		final List _nodes,
		final boolean _checkForROSubproject )
	{
		if (_nodes != null)
		{
			for (Iterator i = _nodes.iterator(); i.hasNext();)
			{
				Object o = i.next();

				if (o == null)
				{
					continue;
				}

				if (o instanceof GraphicNode)
				{
					o = ((GraphicNode)o).getNode();
				}

				if (!isAllowedAction( (Node)o, _checkForROSubproject ))
				{
					return false;
				}
			}
		}

		return true;
	}

	@Override
	public boolean isLeaf( 
		final Object _obj )
	{
		ListIterator i = getIterator();
		GraphicNode node;
		GraphicNode ref = null;

		if (_obj == getRoot())
		{
			ref = (GraphicNode)getRoot();
		}
		else
		{
			while (i.hasNext())
			{
				node = (GraphicNode)i.next();

				if (node == _obj)
				{
					ref = node;

					break;
				}
			}
		}

		if (ref == null)
		{
			return true;
		}

		if (i.hasNext())
		{
			node = (GraphicNode)i.next();

			if (node.getLevel() > ref.getLevel())
			{
				return false;
			}
			else
			{
				return true;
			}
		}

		return true;
	}

	@Override
	public boolean isReceiveEvents()
	{
		return myReference.isReceiveEvents();
	}

	@Override
	public void newNode( 
		final GraphicNode _gnode )
	{
		Node node = _gnode.getNode();

		if (!isAllowedAction( node, false ))
		{
			return;
		}

		if ((node != null) && (node.getValue() instanceof Task) && ((Task)node.getValue()).isReadOnly())
		{
			return; //read only subprojects
		}

		Node parent = getModel().getParent( node );
		int index = parent.getIndex( node );
		getModel().newNode( parent, index, NodeModel.NORMAL );
	}

	@Override
	public void outdentNodes( 
		final List _nodes )
	{
		if (_nodes == null)
		{
			return;
		}

		if (!isAllowedAction( _nodes, false ))
		{
			return;
		}

		List validNodes = TransformList.getNotVoidFilter().filterList( convertToBase( _nodes ) );

		if (validNodes.size() > 0)
		{
			getModel().getHierarchy().indent( validNodes, -1, getModel(), NodeModel.NORMAL );
		}
	}

	@Override
	public void pasteNodes( 
		final Node _parent,
		final List _nodes,
		final int _position )
	{
		if (!isAllowedAction( _parent, true ))
		{
			return;
		}

		getModel().paste( _parent, _nodes, _position, NodeModel.NORMAL );
	}

	@Override
	public void removeNodeModelListener( 
		final CacheListener _listener )
	{
		myVisibleNodes.removeNodeModelListener( _listener );
	}

	@Override
	public void removeTreeModelListener( 
		final TreeModelListener _listener )
	{
		myTreeModelListenerList.remove( TreeModelListener.class, _listener );
	}

	@Override
	public void setModel( 
		final NodeModel _model )
	{
		myReference.setModel( _model );
	}

	@Override
	public void setPertLevel( 
		final GraphicNode _node,
		final int _level )
	{
		_node.setPertLevel( _level );
	}

	@Override
	public void setReceiveEvents( 
		final boolean _receiveEvents )
	{
		myReference.setReceiveEvents( _receiveEvents );
	}

	@Override
	public void setType( 
		final int _type )
	{
		myReference.setType( _type );
	}

	@Override
	public void transformerChanged( 
		final ViewTransformerEvent _event )
	{
		update();
	}

	@Override
	public void update()
	{
//		System.out.println("ViewNodeModelCache update "+getViewName());
		myReference.updateVisibleElements( myVisibleNodes );
	}

	@Override
	public void valueForPathChanged( 
		final TreePath _path,
		final Object _obj )
	{
	}

	//TreeModel events
	protected EventListenerList myTreeModelListenerList = new EventListenerList();
	protected ReferenceNodeModelCache myReference;
	protected String myViewName;
	protected VisibleDependencies myVisibleDependencies;
	protected VisibleNodes myVisibleNodes;
}
