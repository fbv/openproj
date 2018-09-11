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
package com.projity.grouping.core.hierarchy;

import com.projity.association.AssociationList;

import com.projity.configuration.Settings;

import com.projity.grouping.core.LazyParent;
import com.projity.grouping.core.Node;
import com.projity.grouping.core.NodeBridge;
import com.projity.grouping.core.NodeFactory;
import com.projity.grouping.core.model.NodeModel;
import com.projity.grouping.core.model.NodeModelDataFactory;
import com.projity.grouping.core.model.NodeModelUtil;

import com.projity.pm.assignment.Assignment;
import com.projity.pm.dependency.Dependency;
import com.projity.pm.dependency.DependencyService;
import com.projity.pm.resource.Resource;
import com.projity.pm.resource.ResourceImpl;
import com.projity.pm.resource.ResourcePool;
import com.projity.pm.snapshot.Snapshottable;
import com.projity.pm.task.Project;
import com.projity.pm.task.SubProj;
import com.projity.pm.task.TaskLinkReference;
import com.projity.pm.task.TaskSnapshot;

import com.projity.undo.DataFactoryUndoController;
import com.projity.undo.NodeIndentEdit;
import com.projity.undo.NodeUndoInfo;

import com.projity.util.Alert;
import com.projity.util.Environment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;

import org.openproj.domain.task.Task;

/**
 * A map that holds the parent-children relationship.  Also implements TreeModel so it can be used to generate
 * trees, such as in outline cells or pop up trees.
 */
public class MutableNodeHierarchy
	extends AbstractMutableNodeHierarchy
{
	public MutableNodeHierarchy()
	{
	}

	//utility
	public static void addDescendants( 
		final Node _node,
		final List _descendants )
	{
		for (Enumeration e = ((NodeBridge)_node).preorderEnumeration(); e.hasMoreElements();)
		{
			_descendants.add( e.nextElement() );
		}
	}

	//nodes are roots of trees
	public static void addDescendants( 
		final List _nodes,
		final List _descendants )
	{
		for (Iterator i = _nodes.iterator(); i.hasNext();)
		{
			addDescendants( (Node)i.next(), _descendants );
		}
	}

	@Override
	public void add( 
		final Node _parent,
		final List _children,
		final int _position,
		final int _actionType )
	{
		Node parent = (_parent == null)
			? myRoot
			: _parent;

//    	ArrayList trees=new ArrayList();
//    	extractParents(children,trees);

		if (_children.isEmpty() == true)
		{
			return;
		}

		int subprojectLevel = getChildrenSubprojectLevel( _parent );

		int childCount = parent.getChildCount();

		if (_position > childCount)
		{
			NodeFactory nodeFactory = NodeFactory.getInstance();

			for (int i = childCount; i < _position; i++)
			{
				Node node = nodeFactory.createVoidNode();
				setSubprojectLevel( node, subprojectLevel );
				parent.add( node );
			}
		}

		int j = _position;

		for (Iterator i =  /*trees*/_children.iterator(); i.hasNext();)
		{
			Node node = (Node)i.next();

			//if (node.getValue() instanceof Task) System.out.println("ADD parent="+parent+":"+(parent==null?"X":parent.isInSubproject())+", node="+node+":"+node.isInSubproject());
			setSubprojectLevel
			//if (node.getValue() instanceof Task) System.out.println("ADD parent="+parent+":"+(parent==null?"X":parent.isInSubproject())+", node="+node+":"+node.isInSubproject());
			( node, subprojectLevel );

			//if (node.getValue() instanceof Task) System.out.println("ADD node in sub="+node.isInSubproject());
			if (_position == -1)
			{
				parent.add( node );
			}
			else
			{
				parent.insert( node, j++ );
			}
		}

		if (isEvent( _actionType ))
		{
			renumber();
			fireNodesInserted( this, addDescendants( _children/*trees*/  ) );
		}
	}

	/**
	 * Adds a listener for the TreeModelEvent posted after the tree changes.
	 *
	 * @see     #removeTreeModelListener
	 * @param   l       the listener to add
	 */
	@Override
	public void addTreeModelListener( 
		final TreeModelListener _listener )
	{
		myListenerList.add( TreeModelListener.class, _listener );
	}

	@Override
	public void checkEndVoidNodes( 
		final int _actionType )
	{
		checkEndVoidNodes( false, _actionType );
	}

	/**
	 * Check if void nodes have to be added to respect myEndVoidNodes
	 * @param event
	 */
	@Override
	public void checkEndVoidNodes( 
		final boolean _subproject,
		final int actionType )
	{
		ArrayList inserted = new ArrayList();

		Node virtualRoot = getVirtualroot();

		if (!_subproject)
		{
			checkSubprojectEndVoidNodes( virtualRoot, inserted );
		}

		int nbEndVoids = _subproject
			? myMultiprojectEndVoidNodes
			: myEndVoidNodes;

		//int nbEndVoids=myEndVoidNodes;
		int count = 0;
		Node node;

		for (ListIterator i = virtualRoot.childrenIterator( virtualRoot.getChildCount() ); i.hasPrevious();)
		{
			node = (Node)i.previous();

			if (node.isVoid())
			{
				count++;
			}
			else
			{
				break;
			}
		}

		if (count < nbEndVoids)
		{
			for (int i = 0; i < (nbEndVoids - count); i++)
			{
				node = NodeFactory.getInstance().createVoidNode();
				virtualRoot.add( node );
				inserted.add( node );
			}
		}
		else if (count > nbEndVoids)
		{ // remove void nodes if they shouldt be there

			int removeCount = count - nbEndVoids;

			for (ListIterator i = virtualRoot.childrenIterator( virtualRoot.getChildCount() ); i.hasPrevious();)
			{
				node = (Node)i.previous();

				if (node.isVoid())
				{
					i.remove();
					removeCount--;

					if (removeCount == 0)
					{
						break;
					}
				}
				else
				{
					break;
				}
			}
		}

		if (isEvent( actionType ) && (inserted.size() > 0))
		{
			fireNodesInserted( this, inserted.toArray() /*null*/ );
		}
	}

//    public void add(Node parent,Node child,int actionType){
//    	add(parent,child,-1,actionType);
//    }
//    public void add(Node parent,List children,int actionType){
//    	add(parent,children,-1,actionType);
//    }
//
//    public void add(Node parent,Node child,int position,int actionType){
//    	LinkedList children=new LinkedList();
//    	children.add(child);
//    	add(parent,children,position,actionType);
//    }
	
	@Override
	public void cleanVoidChildren()
	{
		cleanVoidChildren( myRoot );
	}

	@Override
	public Object clone()
	{
		Alert.error( "clone not implemented" );

		return null;

		//return new MutableNodeHierarchy((HashMap)parents.clone(),(MultiHashMap)children.clone(),(HashMap)voidNodes.clone());
	}

	@Override
	public void fireInsertion( 
		final Node[] _nodes )
	{
		fireNodesInserted( this, _nodes );
	}

	@Override
	public void fireRemoval( 
		final Node[] _nodes )
	{
		fireNodesRemoved( this, _nodes );
	}

	@Override
	public void fireUpdate()
	{
		fireStructureChanged( this );
	}

	@Override
	public void fireUpdate( 
		final Node[] _nodes )
	{
		fireNodesChanged( this, _nodes );
	}

	@Override
	public List getChildren( 
		final Node _parent )
	{
		return (_parent == null)
		? myRoot.getChildren()
		: _parent.getChildren();
	}

	@Override
	public int getLevel( 
		final Node _node )
	{
		int level = 0;

		for (Node current = _node; current != null; current = getParent( current ))
		{
			level++;
		}

		return level - 1;
	}

	/**
	 * @return Returns the end void nodes.
	 */
	@Override
	public int getNbEndVoidNodes()
	{
		return myEndVoidNodes;
	}

	@Override
	public Node getParent( 
		final Node _child )
	{
		if (_child == null)
		{
			return null;
		}

		return (Node)_child.getParent();
	}

	// Below is TreeModel implementation
	@Override
	public Object getRoot()
	{
		return myRoot;
	}

	@Override
	public Node getSummaryNode()
	{
		if (!mySummaryNodeEnabled)
		{
			return null;
		}

		if (mySummaryNode == null)
		{
			Node myRoot = (Node)getRoot();
			List children = getChildren( myRoot );

			if (children != null)
			{
				for (Iterator i = children.iterator(); i.hasNext();)
				{
					Node n = (Node)i.next();

					if (!(n.getValue() instanceof Task))
					{
						continue;
					}

					Task t = (Task)n.getValue();

					if (t.isRoot())
					{
						mySummaryNode = n;

						break;
					}
				}
			}
		}

		return mySummaryNode;
	}

	@Override
	public Node getVirtualroot()
	{
		Node sum = getSummaryNode();

		if (sum == null)
		{
			return (Node)getRoot();
		}
		else
		{
			return sum;
		}
	}

	//indentation
	//    public void indent(Node node,int deltaLevel,int actionType){
	//    	internalIndent(node,deltaLevel,actionType);
	//    }
	@Override
	public void indent( 
		final List _nodes,
		final int _deltaLevel,
		final NodeModel _model,
		final int _actionType )
	{
		if (_deltaLevel < 0)
		{
			Node summaryNode = getSummaryNode();

			if (summaryNode != null)
			{
				for (Iterator i = _nodes.iterator(); i.hasNext();)
				{
					Node node = (Node)i.next();

					if (node.getParent() == summaryNode)
					{
						return; //only works for -1
					}
				}
			}
		}

		int transactionId = DataFactoryUndoController.beginTransaction( _model, _actionType );

		List changedParents = internalIndent( _nodes, _deltaLevel, _actionType );

		if (((_model.getUndoableEditSupport() != null) & isUndo( _actionType )) && (changedParents != null) &&
				(changedParents.size() > 0))
		{
			_model.getUndoableEditSupport().postEdit( new NodeIndentEdit( _model, changedParents, _deltaLevel ) );
		}

		DataFactoryUndoController.endTransaction( _model, _actionType, transactionId );
	}

	public static boolean isEvent( 
		final int _actionType )
	{
		return (_actionType & NodeModel.EVENT) == NodeModel.EVENT;
	}

	@Override
	public boolean isLeaf( 
		final Object _node )
	{
		Collection children = getChildren( (Node)_node );

		return ((children == null) || (children.size() == 0));
	}

	@Override
	public boolean isSummary( 
		final Node _node )
	{
		List children = getChildren( _node );

		if (children == null)
		{
			return false;
		}

		for (Iterator i = children.iterator(); i.hasNext();)
		{
			if (!(((Node)i.next()).getValue() instanceof Assignment))
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean isSummaryNodeEnabled()
	{
		return mySummaryNodeEnabled;
	}

	public static boolean isUndo( 
		final int _actionType )
	{
		return (_actionType & NodeModel.UNDO) == NodeModel.UNDO;
	}

	@Override
	public void move( 
		final Node _node,
		final Node _newParent,
		final int _actionType )
	{
		setSubprojectLevel( _node, getChildrenSubprojectLevel( _newParent ) );
		_newParent.add( _node );

		ArrayList change = new ArrayList();

		for (Enumeration e = ((NodeBridge)_node).preorderEnumeration(); e.hasMoreElements();)
		{
			change.add( e.nextElement() );
		}

		if (isEvent( _actionType ))
		{
			fireNodesChanged( this, change.toArray() );
		}
	}

	@Override
	public void paste( 
		final Node _parent,
		final List _children,
		final int _position,
		final NodeModel _model,
		final int _actionType )
	{
		Node p = (_parent == null)
			? myRoot
			: _parent;

		Project project = null;
		ResourcePool resourcePool = null;

		if (_model.getDataFactory() instanceof Project)
		{
			project = (Project)_model.getDataFactory();
		}
		else if (_model.getDataFactory() instanceof ResourcePool)
		{
			resourcePool = (ResourcePool)_model.getDataFactory();
		}

		int subprojectLevel = getChildrenSubprojectLevel( _parent );

		//    	ArrayList trees=new ArrayList();
		//    	HierarchyUtils.extractParents(children,trees);
		int childCount = p.getChildCount();

		if (_position > childCount)
		{
			NodeFactory nodeFactory = NodeFactory.getInstance();
			Node node = nodeFactory.createVoidNode();

			for (int i = childCount; i < _position; i++)
			{
				setSubprojectLevel( node, subprojectLevel );
				p.add( node );
			}
		}

		int j = _position;

		for (Iterator i =  /*trees*/_children.iterator(); i.hasNext();)
		{
			Node node = (Node)i.next();

			if (((project != null) && node.getValue() instanceof Task) ||
					((resourcePool != null) && node.getValue() instanceof Resource) || node.isVoid())
			{
				//if (node.getValue() instanceof Task) System.out.println("PASTE parent="+parent+":"+(parent==null?"X":parent.isInSubproject())+", node="+node+":"+node.isInSubproject());
				setSubprojectLevel
				//if (node.getValue() instanceof Task) System.out.println("PASTE parent="+parent+":"+(parent==null?"X":parent.isInSubproject())+", node="+node+":"+node.isInSubproject());
				( node, subprojectLevel );

				//if (node.getValue() instanceof Task) System.out.println("PASTE node in sub="+node.isInSubproject());
				if (_position == -1)
				{
					p.add( node );
				}
				else
				{
					p.insert( node, j++ );
				}
			}
		}

		Node[] descendants = addDescendants( _children );

		ArrayList<Dependency> dependencies = new ArrayList<Dependency>();

		boolean doTransaction = (_model.getDocument() != null) && (descendants.length > 0);
		int transactionId = 0;

		if (doTransaction)
		{
			transactionId = _model.getDocument().fireMultipleTransaction( 0, true );
		}

		ArrayList<Node> insertedNodes = new ArrayList<Node>(descendants.length);

		if (project != null)
		{
			HashMap<Long,Resource> resourceMap = new HashMap<Long,Resource>();

			for (Resource r : (Collection<Resource>)project.getResourcePool().getResourceList())
			{
				resourceMap.put( r.getUniqueId(), r );
			}

			HashMap<Long,Task> taskMap = null;

			if (Environment.isKeepExternalLinks() == true)
			{
				taskMap = new HashMap<Long,Task>();

				for (Task task : (Collection<Task>)project.getTasks()) //use model instead?

				{
					taskMap.put( task.getUniqueId(), task );
				}
			}

			Project owningProject;

			if ((_parent != null) && _parent.getValue() instanceof Task)
			{
				Task task = (Task)_parent.getValue();

				if (task.isSubproject() == true)
				{
					owningProject = ((SubProj)task).getSubproject();
				}
				else
				{
					owningProject = task.getOwningProject();
				}
			}
			else
			{
				owningProject = (Project)_model.getDataFactory();
			}

			for (int i = 0; i < descendants.length; i++)
			{
				if (descendants[ i ].getValue() instanceof Task)
				{
					Task task = (Task)descendants[ i ].getValue();

					Node parentSubproject = getParentSubproject( (Node)descendants[ i ].getParent() );

					if (parentSubproject != null)
					{
						owningProject = ((SubProj)parentSubproject.getValue()).getSubproject();
					}

					if (!task.isExternal()) // fixes  subproject bug with external links
					{
						task.setProjectId( owningProject.getUniqueId() ); //useful?
					}

					owningProject.getNodeModelDataFactory().validateObject( task, _model, this, null, false );

					Set<Dependency> depsSet = new HashSet<Dependency>();
					List pdeps = task.getDependencyList( true );

					if ((pdeps != null) && (pdeps.size() > 0))
					{
						if (Environment.isKeepExternalLinks())
						{
							for (Iterator k = pdeps.iterator(); k.hasNext();)
							{
								Dependency d = (Dependency)k.next();

								if (!(d.getPredecessor() instanceof Task))
								{
									TaskLinkReference ref = (TaskLinkReference)d.getPredecessor();
									Task t = taskMap.get( ref.getUniqueId() );

									if (t == null)
									{
										k.remove();

										continue;
									}
									else
									{
										d.setPredecessor( t );
										t.getSuccessorList().add( d );

										//DependencyService.getInstance().updateSentinels(d);
										//DependencyService.getInstance().connect(d, this);
									}
								}

								depsSet.add( d );
							}
						}
					}

					List sdeps = task.getDependencyList( false );

					if ((sdeps != null) && (sdeps.size() > 0))
					{
						if (Environment.isKeepExternalLinks())
						{
							for (Iterator k = sdeps.iterator(); k.hasNext();)
							{
								Dependency d = (Dependency)k.next();

								if (!(d.getSuccessor() instanceof Task))
								{
									TaskLinkReference ref = (TaskLinkReference)d.getSuccessor();
									Task t = taskMap.get( ref.getUniqueId() );

									if (t == null)
									{
										k.remove();

										continue;
									}
									else
									{
										d.setSuccessor( t );
										t.getPredecessorList().add( d );

										//DependencyService.getInstance().updateSentinels(d);
										//DependencyService.getInstance().connect(d, this);
									}
								}

								depsSet.add( d );
							}
						}
					}

					dependencies.addAll( depsSet );

					//check assignments, if resource not present change it to unassigned
					for (int s = 0; s < Settings.numBaselines(); s++)
					{
						TaskSnapshot snapshot = (TaskSnapshot)task.getSnapshot( new Integer(s) );

						if (snapshot == null)
						{
							continue;
						}

						AssociationList snapshotAssignments = snapshot.getHasAssignments().getAssignments();

						if (snapshotAssignments.size() > 0)
						{
							//    			        	ArrayList<Assignment> assignmentsToLink=new ArrayList<Assignment>();
							for (Iterator a = snapshotAssignments.listIterator(); a.hasNext();)
							{
								Assignment assignment = (Assignment)a.next();
								Resource resource = assignment.getResource();

								if (resource == ResourceImpl.getUnassignedInstance())
								{
									continue;
								}

								Resource destResource = resourceMap.get( resource.getUniqueId() );

								if (destResource != null)
								{
									if (Snapshottable.CURRENT.equals( s ))
									{
										if (destResource != resource)
										{ // use destination resource
											resource = destResource;
											assignment.getDetail().setResource( resource );
										}

										//                						assignmentsToLink.add(assignment);
										resource.addAssignment( assignment );

										NodeUndoInfo undo = new NodeUndoInfo(false);
										((ResourcePool)assignment.getResource().getDocument()).getObjectEventManager()
										 .fireCreateEvent( this, assignment, undo );
									}
								}
								else
								{
									assignment.getDetail().setResource( ResourceImpl.getUnassignedInstance() );
								}
							}

							//    			            for (Assignment assignment: assignmentsToLink){
							//    			            	AssignmentService.getInstance().remove(assignmentsToLink, this,false);
							//    			            	AssignmentService.getInstance().connect(assignment, this);
							//    			            }
						}
					}

					project.addPastedTask( task );

					insertedNodes.add( descendants[ i ] );
				}
			}
		}
		else if (resourcePool != null)
		{
			for (int i = 0; i < descendants.length; i++)
			{
				if (descendants[ i ].getValue() instanceof Resource)
				{
					Resource resource = (Resource)descendants[ i ].getValue();
					_model.getDataFactory().validateObject( resource, _model, this, null, false );
					resourcePool.initializeId( resource );
					insertedNodes.add( descendants[ i ] );
				}
			}
		}

		// added april 16 2008 in the hopes it cures the "Calendar value too large for accurate calculations" bug
		if (project != null)
		{
			project.getSchedulingAlgorithm().markBoundsAsDirty();
		}

		if (doTransaction)
		{
			_model.getDocument().fireMultipleTransaction( transactionId, false );
		}

		if (isEvent( _actionType ))
		{
			renumber();
			fireNodesInserted( this, insertedNodes.toArray( new Node[ insertedNodes.size() ] ) );

			//not necessary in case of subproject paste
			for (Dependency dependency : dependencies)
			{
				DependencyService.getInstance().updateSentinels( dependency ); //needed?
				dependency.fireCreateEvent( this );
			}
		}
	}

	/**
	 * Remove nodes.  This will wrap the call in a multiple transaction if there are many calls so as
	 * not to recalculate each time.  In case end void nodes were removed, they will be put back
	 *
	 */
	@Override
	public void remove( 
		final List _nodes,
		final NodeModel _model,
		final int _actionType,
		final boolean _removeDependencies )
	{
		if (_nodes != null)
		{
			boolean doTransaction = (_model.getDocument() != null) && (_nodes.size() > 0) && isEvent( _actionType );
			int transactionId = 0;

			if (doTransaction)
			{
				transactionId = _model.getDocument().fireMultipleTransaction( 0, true );
			}

			ArrayList removed = new ArrayList();

			for (Iterator i = _nodes.iterator(); i.hasNext();)
			{
				LinkedList toRemove = new LinkedList();
				removeSubTree( (Node)i.next(), _model, toRemove, _actionType, _removeDependencies );
				removed.addAll( toRemove );
			}

			if (isEvent( _actionType ))
			{
				renumber();
				fireNodesRemoved( this, removed.toArray() );
			}

			if (doTransaction)
			{
				_model.getDocument().fireMultipleTransaction( transactionId, false );
			}
		}
	}

	@Override
	public void removeAll( 
		final NodeModel _model,
		final int _actionType )
	{
		remove( buildList(), _model, _actionType, true );
	}

	/**
	 * Removes a listener previously added with <B>addTreeModelListener()</B>.
	 *
	 * @see     #addTreeModelListener
	 * @param   l       the listener to remove
	 */
	@Override
	public void removeTreeModelListener( 
		final TreeModelListener _listener )
	{
		myListenerList.remove( TreeModelListener.class, _listener );
	}

	@Override
	public Node search( 
		final Object _key,
		final Comparator _comparator )
	{
		//   	System.out.println("search("+key+", "+c+")");
		return search( myRoot, _key, _comparator );
	}

	/**
	 * @param nbEndVoidNodes The nbEndVoidNodes to set.  If -1, then use default
	 */
	@Override
	public void setNbEndVoidNodes( 
		int nbEndVoidNodes )
	{
		if (nbEndVoidNodes == -1)
		{
			nbEndVoidNodes = DEFAULT_NB_END_VOID_NODES;
		}

		myEndVoidNodes = nbEndVoidNodes;
	}

	@Override
	public void setSummaryNodeEnabled( 
		final boolean _summayTaskEnabled )
	{
		if (mySummaryNodeEnabled != _summayTaskEnabled)
		{
			mySummaryNodeEnabled = _summayTaskEnabled;
			mySummaryNode = null;
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath, java.lang.Object)
	 */
	@Override
	public void valueForPathChanged( 
		final TreePath _path,
		final Object _newValue )
	{
		final Node aNode = (Node)_path.getLastPathComponent();

		//TODO do we need to treat this?
	}

	protected boolean checkSubprojectEndVoidNodes( 
		final Node _parent,
		final List _inserted )
	{
		Node node;
		int count = 0;
		boolean found = false;

		for (Enumeration e = _parent.children(); e.hasMoreElements();)
		{
			node = (Node)e.nextElement();

			if (checkSubprojectEndVoidNodes( node, _inserted ))
			{
				found = true;
			}

			if (NodeModelUtil.nodeIsSubproject( _parent ))
			{
				if (node.isVoid())
				{
					count++;
				}
				else
				{
					count = 0;
				}
			}
		}

		if (NodeModelUtil.nodeIsSubproject( _parent ))
		{
			int nbEndVoids = myMultiprojectEndVoidNodes;

			if (_parent.getValue() instanceof SubProj)
			{
				Project s = ((SubProj)_parent.getValue()).getSubproject();

				if ((s != null) && s.isReadOnly())
				{
					nbEndVoids = 0; //don't add end void nodes for read-only subprojects
				}
			}

			if (count < nbEndVoids)
			{
				LazyParent sub = (LazyParent)_parent.getValue();

				if (sub.isDataFetched())
				{
					int subprojectLevel = getChildrenSubprojectLevel( _parent );

					for (int i = 0; i < (myMultiprojectEndVoidNodes - count); i++)
					{
						node = NodeFactory.getInstance().createVoidNode();
						setSubprojectLevel( node, subprojectLevel );

						//node.setInSubproject(true);
						_parent.add( node );
						_inserted.add( node );
					}
				}
			}
		}

		return found;
	}

	//warning: modify nodes list
	private static Node[] addDescendants( 
		final List _nodes )
	{
		ArrayList descendants = new ArrayList();

		for (ListIterator i = _nodes.listIterator(); i.hasNext();)
		{
			Node node = (Node)i.next();
			extractSameProjectBranch( node, descendants );

			//       		boolean rootNode=true;
			//        	for (Enumeration e=((NodeBridge)node).preorderEnumeration();e.hasMoreElements();rootNode=false){
			//        		Node current=(Node)e.nextElement();
			//        		if (!rootNode) descendants.add(current);
			//        	}
		}

		Node[] descendantsArray = (Node[])descendants.toArray( new Node[ descendants.size() ] );

		return descendantsArray;
	}

	private List buildList()
	{
		return buildList( null );
	}

	private List buildList( 
		final Node _parent )
	{
		List list = new ArrayList();
		buildList( _parent, list );

		return list;
	}

	private void buildList( 
		final Node _parent,
		final List _list )
	{
		Node p = (_parent == null)
			? myRoot
			: _parent;

		if (p != myRoot)
		{
			_list.add( p );
		}

		final Collection children = getChildren( p );

		if (children != null)
		{
			for (Iterator i = children.iterator(); i.hasNext();)
			{
				buildList( (Node)i.next(), _list );
			}
		}
	}

	private boolean contains( 
		final Object _node )
	{
		//return parents.containsKey(node)||children.containsKey(node);
		Alert.error( "contains not implemented" );

		return false;
	}

	private static void extractSameProjectBranch( 
		final Node _parent,
		final ArrayList _descendants )
	{
		//    	if (parent.getValue() instanceof Subproject){
		//    		((NodeBridge)parent).removeAllChildren();
		//    		Subproject subproject=(Subproject)parent.getValue();
		//    		//subproject.setProject(null);
		//
		//    	}else{
		_descendants.add( _parent );

		for (Enumeration e = _parent.children(); e.hasMoreElements();)
		{
			Node current = (Node)e.nextElement();
			extractSameProjectBranch( current, _descendants );
		}

		//    	}
	}

	private void cleanVoidChildren( 
		final Node _node )
	{
		for (Iterator i = _node.childrenIterator(); i.hasNext();)
		{
			Node child = (Node)i.next();

			if (child.isVoid())
			{
				i.remove();
			}
			else
			{
				cleanVoidChildren( child );
			}
		}
	}

	private int getChildrenSubprojectLevel( 
		final Node _parent )
	{
		if ((_parent == null) || _parent.isRoot())
		{
			return 0;
		}

		if (NodeModelUtil.nodeIsSubproject( _parent ))
		{
			return _parent.getSubprojectLevel() + 1;
		}
		else
		{
			return _parent.getSubprojectLevel();
		}
	}

	private Node getParentSubproject( 
		final Node _node )
	{
		if (_node.isRoot())
		{
			return null;
		}
		else if (_node.getValue() instanceof SubProj)
		{
			return _node;
		}
		else
		{
			return getParentSubproject( (Node)_node.getParent() );
		}
	}

	//nodes have to be ordered from first to last
	private List internalIndent( 
		final List _nodes,
		final int _deltaLevel,
		final int _actionType )
	{
		if ((_deltaLevel != 1) && (_deltaLevel != -1))
		{
			return null;
		}

		//Indent only parents
		final LinkedList nodesToChange = new LinkedList();
		HierarchyUtils.extractParents( _nodes, nodesToChange );

		final List modifiedVoids = new ArrayList();

		//exclude Assignments and VoidNodes
		if (_deltaLevel > 0)
		{
			for (ListIterator i = nodesToChange.listIterator(); i.hasNext();)
			{
				if (!internalIndent( (Node)i.next(), _deltaLevel, _actionType & NodeModel.UNDO, modifiedVoids ))
				{
					i.remove();
				}

				for (Iterator j = modifiedVoids.iterator(); j.hasNext();)
				{
					i.add( j.next() );
				}

				modifiedVoids.clear();
			}
		}
		else
		{
			for (ListIterator i = nodesToChange.listIterator( nodesToChange.size() ); i.hasPrevious();)
			{
				if (!internalIndent( (Node)i.previous(), _deltaLevel, _actionType & NodeModel.UNDO, modifiedVoids ))
				{
					i.remove();
				}

				for (Iterator j = modifiedVoids.iterator(); j.hasNext();)
				{
					i.add( j.next() );
				}

				modifiedVoids.clear();
			}
		}

		if (isEvent( _actionType ) && (nodesToChange.size() > 0))
		{
			fireNodesChanged( this, nodesToChange.toArray() );
		}

		return nodesToChange;
	}

	private boolean internalIndent( 
		final Node _node,
		final int _deltaLevel,
		final int _actionType,
		final List _modifiedVoids )
	{ 
		//only +1 -1
		if ((_node == null) || (_node == myRoot) || !_node.isIndentable( _deltaLevel ))
		{
			return false;
		}

		if (_deltaLevel == 1)
		{ 
			//indent
			Node parent = getParent( _node );
			int index = parent.getIndex( _node );

			if (index == 0)
			{
				return false;
			}

			Node sibling;
			Node previous = null;

			for (ListIterator i = parent.childrenIterator( index ); i.hasPrevious();)
			{
				sibling = (Node)i.previous();

				if (_node.canBeChildOf( sibling ))
				{
					previous = sibling;

					break;
				}
				else if (sibling.isVoid())
				{
					_modifiedVoids.add( sibling );
				}
			}

			if ((previous == null) || previous.getValue() instanceof Assignment)
			{
				return false;
			}

			for (Iterator i = _modifiedVoids.iterator(); i.hasNext();)
			{
				previous.add( (Node)i.next() );
			}

			previous.add( _node );

			if (isEvent( _actionType ))
			{
				fireNodesChanged( this, new Node[]
					{
						_node
					} );
			}
		}
		else if (_deltaLevel == -1)
		{ //outdent

			final Node parent = getParent( _node );

			if ((parent == null) || (parent == myRoot))
			{
				return false;
			}

			if (parent.isLazyParent()) // don't allow outdenting of subprojects' children
			{
				return false;
			}

			final Node grandParent = getParent( parent );

			//voids
			int index = parent.getIndex( _node );

			if (index > 0)
			{
				Node sibling;

				for (ListIterator i = parent.childrenIterator( index ); i.hasPrevious();)
				{
					sibling = (Node)i.previous();

					if (sibling.isVoid())
					{
						_modifiedVoids.add( sibling );
					}
					else
					{
						break;
					}
				}
			}

			index = grandParent.getIndex( parent ) + 1;
			grandParent.insert( _node, index );

			for (Iterator i = _modifiedVoids.iterator(); i.hasNext();)
			{
				grandParent.insert( (Node)i.next(), index );
			}

			if (isEvent( _actionType ))
			{
				fireNodesChanged( this, new Node[]
					{
						_node
					} );
			}
		}

		return true;
	}

//    public void remove(Node node,NodeModel model,int actionType){
//    	LinkedList removed=new LinkedList();
//    	removeNoEvent(node,model,removed,actionType);
//    	fireNodesRemoved(this,removed.toArray());
//    }

//    private void removeNoEvent(Node node,NodeModel model,LinkedList toRemove,int actionType){
//
//    	System.out.println("removeNoEvent("+node+")");
//    	//if (!isEvent(actionType)) return;
//    	//node.removeFromParent();
//        Node current;
//        int badCount = 0;
//        LinkedList enumeratedNodes=new LinkedList();
//    	for (Enumeration e=((NodeBridge)node).postorderEnumeration();e.hasMoreElements();){
//    		enumeratedNodes.add(e.nextElement());
//    	}
//		System.out.println("removeApartFromHierarchy("+enumeratedNodes+")");
//    	for (Iterator i=enumeratedNodes.iterator();i.hasNext();){
//    		current=(Node)i.next();
//            if (model.removeApartFromHierarchy(current,actionType))
//            	toRemove.add(current);
//            else
//            	badCount++;
//    	}
////    	for (Enumeration e=((NodeBridge)node).postorderEnumeration();e.hasMoreElements();){
////    		current=(Node)e.nextElement();
////    		System.out.println("removeApartFromHierarchy("+current+")");
////            if (model.removeApartFromHierarchy(current,actionType))
////            	toRemove.add(current);
////            else
////            	badCount++;
////    	}
////    	if (badCount == 0) // if no errors, the
//        	node.removeFromParent();
//
//
//
////         	if (undo){
////				//Undo
////	        	NodeHierarchyVoidLocation location=new NodeHierarchyVoidLocation(new NodeHierarchyLocation(parent,previous),1);
////				UndoableEditSupport undoableEditSupport=model.getUndoableEditSupport();
////				if (undoableEditSupport!=null){
////					undoableEditSupport.postEdit(new NodeDeletionEdit(model,location,node));
////				}
////         	}
//
//
//    	//fireNodesRemoved(this,toRemove.toArray());
//  }

	private void removeSubTree( 
		final Node _node,
		final NodeModel _model,
		final LinkedList _toRemove,
		final int _actionType,
		final boolean _removeDependencies )
	{
		//    	System.out.println("removeSubTree");
		if (getUpdateLevel() == 0)
		{
			//boolean singleRemoval=!(node.getValue() instanceof Assignment);
			try
			{
				_node.removeFromParent();

				/*if (singleRemoval)*/ beginUpdate
				/*if (singleRemoval)*/ ();

				//				System.out.println("removeNoEvent("+node+")");
				Node current;
				int badCount = 0;
				final LinkedList enumeratedNodes = new LinkedList();

				for (Enumeration e = ((NodeBridge)_node).postorderEnumeration(); e.hasMoreElements();)
				{
					enumeratedNodes.add( e.nextElement() );
				}

				//				System.out.println("removeApartFromHierarchy("+enumeratedNodes+")");
				for (Iterator i = enumeratedNodes.iterator(); i.hasNext();)
				{
					current = (Node)i.next();

					if (_model.removeApartFromHierarchy( current, false, _actionType, _removeDependencies ))
					{
						_toRemove.add( current );
					}
					else
					{
						badCount++;
					}
				}

				_node.removeFromParent();
			}
			finally
			{
				/*if (singleRemoval)*/ endUpdate
				/*if (singleRemoval)*/ ();
			}
		}
	}

	private Node search( 
		final Node _node,
		final Object _key,
		final Comparator _c )
	{
		if (_c.compare( (_node == null)
					? myRoot
						: _node, _key ) == 0)
		{
			return _node;
		}

		final Collection children = getChildren( _node );

		if (children == null)
		{
			return null;
		}

		final Iterator i = children.iterator();

		while (i.hasNext())
		{
			Node found = search( (Node)i.next(), _key, _c );

			if (found != null)
			{
				return found;
			}
		}

		return null;
	}

	private Node searchLast( 
		final Node _node )
	{
		Node current = (_node == null)
			? myRoot
			: _node;

		while (!isLeaf( current ))
		{
			final List children = (List)getChildren( current );
			current = (Node)children.get( children.size() - 1 );
		}

		return current;
	}

	private Node searchLast( 
		final int _level )
	{
		Node current = null;

		for (int l = 0; !isLeaf( current ) && (l < _level); l++)
		{
			final List children = (List)getChildren( current );
			current = (Node)children.get( children.size() - 1 );
		}

		return current;
	}

	//
	//    private Iterator iterator(){
	//        return buildList(null).iterator();
	//    }
	private Node searchPrevious( 
		final Node _node )
	{
		if ((_node == null) || (_node == myRoot))
		{
			return null;
		}

		Node previous = getParent( _node );
		final Collection children = getChildren( previous );

		if (children == null)
		{
			return null;
		}

		for (Iterator i = children.iterator(); i.hasNext();)
		{
			final Node currentNode = (Node)i.next();

			if (currentNode.equals( _node ))
			{
				return previous;
			}

			previous = currentNode;
		}

		return null;
	}

	//    private void setInSubproject(Node parent,Node node){
	//    	if (NodeModelUtil.nodeIsSubproject(parent))
	//    		node.setInSubproject(true);
	//    	else
	//    		node.setInSubproject(parent.isInSubproject());
	//    }
	private void setSubprojectLevel( 
		final Node _node,
		final int _level )
	{
		_node.setSubprojectLevel( _level );

		final int subprojectLevel = getChildrenSubprojectLevel( _node );

		for (Enumeration e = _node.children(); e.hasMoreElements();)
		{
			final Node child = (Node)e.nextElement();
			setSubprojectLevel( child, subprojectLevel );
		}
	}

	public static int DEFAULT_NB_END_VOID_NODES = 50;
	public static int DEFAULT_NB_MULTIPROJECT_END_VOID_NODES = 1;

	protected transient EventListenerList myListenerList = new EventListenerList();
	protected Node mySummaryNode;
	protected boolean mySummaryNodeEnabled;
	protected int myEndVoidNodes = DEFAULT_NB_END_VOID_NODES;
	protected int myMultiprojectEndVoidNodes = DEFAULT_NB_MULTIPROJECT_END_VOID_NODES;
	private final Node myRoot = NodeFactory.getInstance().createRootNode();
}
