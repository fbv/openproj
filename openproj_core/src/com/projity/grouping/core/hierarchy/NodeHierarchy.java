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

import com.projity.grouping.core.Node;
import com.projity.grouping.core.event.HierarchyListener;
import com.projity.grouping.core.model.NodeModel;
import java.util.Comparator;
import java.util.EventListener;
import java.util.Iterator;
import java.util.List;
import javax.swing.tree.TreeModel;
import org.apache.commons.collections.Closure;
import org.apache.commons.collections.Predicate;

/**
 *
 */
public interface NodeHierarchy
	extends TreeModel
{
//	void add(Node parent,Node child,int actionType);

//	void add(Node parent,Node child,int position,int actionType);

//	void add(Node parent,List children,int actionType);

	void add( Node _parent, List _children, int _position, int _actionType );

	void addHierarchyListener( HierarchyListener _listener );

	void checkEndVoidNodes( int _actionType );

	void checkEndVoidNodes( boolean _subproject, int _actionType );

	void cleanVoidChildren();

	Object clone();

	void fireInsertion( Node[] _nodes );

	void fireRemoval( Node[] _nodes );

	void fireUpdate();

	void fireUpdate( Node[] _nodes );

	List getChildren( Node _parent );

	HierarchyListener[] getHierarchyListeners();

	EventListener[] getHierarchyListeners( Class _listenerType );

	int getIndexOfNode( Node _key, boolean _skipVoid );

	int getLevel( Node _node );

	int getNbEndVoidNodes();

	Node getNext( Node _current );

	Node getParent( Node _child );

	Node getPrevious( Node _current );

	Node getSummaryNode();

	Node getVirtualroot();

//	void indent(Node node,int deltaLevel,int actionType);
	
	void indent( List _nodes, int _deltaLevel, NodeModel _model, int _actionType );

	boolean isSummary( Node _node );

	boolean isSummaryNodeEnabled();

	Iterator iterator();

	Iterator iterator( Node _rootNode );

//	void move( Node _node, Node _newParent );

//	void move( List _nodes, Node _newParent );
	
	void move( Node _node, Node _newParent, int _actionType );

	void paste( Node _parent, List _children, int _position, NodeModel _model, int _actionType );

//	void remove( Node _node, NodeModel _model, int _actionType );
	
	void remove( List _nodes, NodeModel _model, int _actionType, boolean _removeDependencies );

	void removeAll( NodeModel _model, int _actionType );

	void removeHierarchyListener( HierarchyListener _listener );

	void renumber();

	Node search( Object _key, Comparator _comparator );

	void setNbEndVoidNodes( int _nbEndVoidNodes );

	void setSummaryNodeEnabled( boolean _summayTaskEnabled );

	Iterator shallowIterator( int _maxLevel, boolean _returnRoot );

	List toList( boolean _isNode, Predicate _filter );

	<T> void visitAll( Closure<Node<T>> _visitor );

	void visitAll( Node _parent, Closure<Node> _visitor );

	void visitAll( Node _root, boolean _skipLazyParents, Closure<Node> _visitor );

	void visitAllLevelOrder( Node _root, boolean _skipLazyParents, Closure<Node> _visitor );

	void visitLeaves( Node _node, Closure<Node> _visitor );
}
