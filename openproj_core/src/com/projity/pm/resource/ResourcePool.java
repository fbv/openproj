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
package com.projity.pm.resource;

import com.projity.configuration.Settings;

import org.openproj.domain.document.Document;
import com.projity.document.ObjectEvent;
import com.projity.document.ObjectEventManager;
import com.projity.document.ObjectSelectionEventManager;
import com.projity.field.Field;

import com.projity.grouping.core.Node;
import com.projity.grouping.core.NodeList;
import com.projity.grouping.core.OutlineCollection;
import com.projity.grouping.core.OutlineCollectionImpl;
import com.projity.grouping.core.model.AssignmentNodeModel;
import com.projity.grouping.core.model.DefaultNodeModel;
import com.projity.grouping.core.model.NodeModel;
import com.projity.grouping.core.model.NodeModelDataFactory;

import com.projity.pm.calendar.CalendarService;
import com.projity.pm.calendar.WorkCalendar;
import com.projity.pm.calendar.WorkingCalendar;
import com.projity.pm.task.Project;

import com.projity.undo.DataFactoryUndoController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 *
 */
public class ResourcePool
	implements Document,
		NodeModelDataFactory
{
	protected ResourcePool( 
		final String _name,
		final DataFactoryUndoController _undo )
	{
		this.name = _name;
		globalPool = this;
		defaultCalendar = CalendarService.getInstance().getDefaultInstance();
		undoController = _undo;

		//initUndo();
	}

	public void add( 
		final Resource _resource )
	{
		resourceList.add( _resource );
	}

// remove - psc1952 - 2012.01.20	
//	public void addAndInitializeId( 
//		final Resource _resource )
//	{
//		add( _resource );
//		initializeId( _resource );
//	}

	/**
	 * @param listener
	 */
	@Override
	public void addObjectListener( 
		final ObjectEvent.Listener _listener )
	{
		objectEventManager.addListener( _listener );
	}

	public void addProject( 
		final Project _project )
	{
		projects.add( _project );

		//		initUndoControlerForAllOutines(project);
	}

	public void addToDefaultOutline( 
		final Node _parentNode,
		final Node _childNode )
	{
		resourceOutlines.addToDefaultOutline( _parentNode, _childNode );
	}

	public void addToDefaultOutline( 
		final Node _parentNode,
		final Node _childNode,
		final int _position,
		final boolean _event )
	{
		resourceOutlines.addToDefaultOutline( _parentNode, _childNode, _position, _event );
	}

	@Override
	public void addUnvalidatedObject( 
		final Object _object,
		final NodeModel _nodeModel,
		final Object _parent )
	{
	}

	@Override
	public NodeModel createNodeModel()
	{
		// Resource pool contains assignments, so create the assignment node model
		return new AssignmentNodeModel( this );
	}

	@Override
	public NodeModel createNodeModel( 
		final Collection _collection )
	{
		final NodeModel model = new AssignmentNodeModel( this );
		model.addImplCollection( null, _collection, NodeModel.SILENT );

		return model;
	}

	public static ResourcePool createRourcePool( 
		final String _name,
		final DataFactoryUndoController _undo )
	{
		ResourcePool pool = new ResourcePool( _name, _undo );
		pool.initializeOutlines();

		return pool;
	}

	public Resource createScriptedResource()
	{
		Resource res = newResourceInstance();
		resourceOutlines.addToAll( res, null ); // update all node models

		return res;
	}

	/* (non-Javadoc)
	 * @see com.projity.grouping.core.NodeModelDataFactory#createUnvalidatedObject(com.projity.grouping.core.NodeModel)
	 */
	@Override
	public Object createUnvalidatedObject( 
		final NodeModel _nodeModel,
		final Object _parent )
	{
		EnterpriseResource globalResource = new EnterpriseResource( isLocal(), this );
		ResourceImpl newOne = new ResourceImpl(globalResource);
		newOne.getGlobalResource().setMaster( isMaster() );
		newOne.getGlobalResource().setLocal( isLocal() );
		addUnvalidatedObject( newOne, _nodeModel, _parent );

		return newOne;
	}

	public ArrayList extractCalendars()
	{
		return WorkingCalendar.extractCalendars( resourceList );
	}

	public Resource findById( 
		final long _id )
	{
		if (idMap == null)
		{
			idMap = new HashMap<Long,Resource>();

			Iterator i = getResourceList().iterator();
			Resource resource;

			while (i.hasNext())
			{
				resource = (Resource)i.next();
				idMap.put( resource.getUniqueId(), resource );
			}
		}

		return idMap.get( _id );
	}

	public static Resource findResource( 
		final String _name )
	{
		return findResourceByName( _name, globalPool );
	}

	public static Resource findResourceByInitials( 
		final Object _idObject,
		final Object _resourcePoolObject )
	{
		Iterator i = ((ResourcePool)_resourcePoolObject).getResourceList().iterator();
		int id = ((Integer)_idObject).intValue();
		Resource resource;

		while (i.hasNext())
		{
			resource = (Resource)i.next();

			if (resource.getId() == id)
			{
				return resource;
			}
		}

		return null;
	}

	public static Resource findResourceByName( 
		final Object _idObject,
		final Object _resourcePoolObject )
	{
		Iterator i = ((ResourcePool)_resourcePoolObject).getResourceList().iterator();
		String id = (String)_idObject;
		Resource resource;

		while (i.hasNext())
		{
			resource = (Resource)i.next();

			if (resource.getName().equals( id ))
			{
				return resource;
			}
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see com.projity.document.Document#fireMultipleTransaction(int, boolean)
	 */
	@Override
	public int fireMultipleTransaction( 
		final int _id,
		final boolean _begin )
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void fireUpdateEvent( 
		final Object _source,
		final Object _object )
	{
		objectEventManager.fireUpdateEvent( _source, _object );
	}

	@Override
	public void fireUpdateEvent(
		final Object _source, 
		final Object _object,
		final Field _field ) 
	{
		objectEventManager.fireUpdateEvent( _source, _object, _field );
	}

	public List getChildrenResoures( 
		final Resource _parent )
	{
		NodeModel resourceModel = getResourceOutline();
		Node node = resourceModel.search( _parent );

		return NodeList.nodeListToImplList( resourceModel.getChildren( node ) );
	}

	/**
	 * @return Returns the defaultCalendar.
	 */
//	@Override
	public final WorkCalendar getDefaultCalendar()
	{
		return defaultCalendar;
	}

	@Override
	public NodeModelDataFactory getFactoryToUseForChildOfParent( 
		final Object _impl )
	{
		return this;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName()
	{
		return name;
	}

	@Override
	public ObjectEventManager getObjectEventManager()
	{
		return objectEventManager;
	}

	@Override
	public ObjectSelectionEventManager getObjectSelectionEventManager()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return Returns the projects.
	 */
	public ArrayList getProjects()
	{
		return projects;
	}

	public Resource getRbsParentResource( 
		final Resource _child )
	{
		NodeModel resourceModel = getResourceOutline();
		Node node = resourceModel.search( _child );
		Node parent = resourceModel.getParent( node );

		if ((parent == null) || parent.isVoid())
		{
			return null;
		}

		return (Resource)parent.getValue();
	}

	/**
	 * @return Returns the resourceList.
	 */
	public ArrayList getResourceList()
	{
		return resourceList;
	}

	public NodeModel getResourceOutline()
	{
		NodeModel model = resourceOutlines.getOutline();

		return model;
	}

	public NodeModel getResourceOutline( 
		final int _outlineNumber )
	{
		NodeModel model = resourceOutlines.getOutline( _outlineNumber );

		return model;
	}

	//	protected void initUndo(){
	//		undoController=new DataFactoryUndoController(this);
	//	}
	@Override
	public DataFactoryUndoController getUndoController()
	{
		return undoController;
	}

	@Override
	public void initOutline( 
		final NodeModel _nodeModel )
	{
		if (_nodeModel != null)
		{
			_nodeModel.setLocal( local );
			_nodeModel.setMaster( master );
		}
	}

	public void initializeId( 
		final Resource _resource )
	{
		long id = ++resourceIdCounter;
		_resource.setId( id ); //starts at 1TODO check for duplicates -
							  //resource.setUniqueId(id); //TODO use a GUID generator
	}

	public void initializeOutlines()
	{
		int count = Settings.numHierarchies();

		for (int i = 0; i < count; i++)
		{
			NodeModel model = resourceOutlines.getOutline( i );

			if (model == null)
			{
				continue;
			}

			if (model instanceof AssignmentNodeModel)
			{
				AssignmentNodeModel aModel = (AssignmentNodeModel)model;
				aModel.setDocument( this );
			}

			initOutline( model );
		}
	}

//	@Override
//	public boolean isDontSummarizeFields()
//	{
//		return false;
//	}

//	@Override
//	public final boolean isGroupDirty()
//	{
//		for (Iterator i=getProjects().iterator();i.hasNext();){
//			Project project=(Project)i.next();
//			if (project.isGroupDirty()) return true;
//		}
//		return false;
//		return isDirty;
//	}

	public boolean isLocal()
	{
		return local;
	}

	public boolean isMaster()
	{
		return master;
	}

	public ResourceImpl newResourceInstance()
	{
		EnterpriseResource globalResource = new EnterpriseResource( isLocal(), this );
		ResourceImpl newOne = new ResourceImpl( globalResource );

//		addAndInitializeId( newOne );
		add( newOne );
		initializeId( newOne );

		return newOne;
	}

	public void remove( 
		final Resource _resource )
	{
		resourceList.remove( _resource );
	}

	//	public void fireCreated(Object newlyCreated){
	//		//objectEventManager.fireCreateEvent(this,newlyCreated);
	//	}
	/* (non-Javadoc)
	 * @see com.projity.grouping.core.NodeModelDataFactory#remove(java.lang.Object)
	 */
	@Override
	public void remove( 
		final Object _toRemove,
		final NodeModel _nodeModel,
		final boolean _deep,
		final boolean _undo,
		final boolean _removeDependencies )
	{
		remove( (Resource)_toRemove );
		resourceOutlines.removeFromAll( _toRemove, _nodeModel ); // update all node models except the one passed in
	}

	/**
	 * @param listener
	 */
	@Override
	public void removeObjectListener( 
		final ObjectEvent.Listener _listener )
	{
		objectEventManager.removeListener( _listener );
	}

	public void removeProject( 
		final Project _project )
	{
		projects.remove( _project );
	}

	@Override
	public void rollbackUnvalidated( 
		final NodeModel _nodeModel,
		final Object _object )
	{
	}

//	@Override
//	public void setAllChildrenDirty( 
//		final boolean _dirty )
//	{
//	}

	@Override
	public final void setGroupDirty( 
		final boolean _isDirty )
	{
		System.out.println( "ResourcePool.setGroupDirty(" + _isDirty + ")" );
		this.isDirty = _isDirty;

		if (_isDirty)
		{
			for (Iterator i = getProjects().iterator(); i.hasNext();)
			{
				Project project = (Project)i.next();
				project.setGroupDirty( true );
			}
		}
	}

	public void setLocal( 
		final boolean _local )
	{
		this.local = _local;
	}

	public void setLocalParent( 
		final Resource _child,
		final Resource _parent )
	{
		Node childNode = getResourceOutline().search( _child );
		Node parentNode = (_parent == null)
			? null
			: getResourceOutline().search( _parent );
		setLocalParent( childNode, parentNode );
	}

	public void setLocalParent( 
		final Node _childNode,
		final Node _parentNode )
	{
		Resource child = (Resource)_childNode.getValue();
		Resource parent = (Resource)((_parentNode == null)
			? null
			: _parentNode.getValue());

		if (getRbsParentResource( child ) == parent)
		{
			return;
		}

		Node oldParentNode = getResourceOutline().search( getRbsParentResource( child ) );

		if (oldParentNode != null)
		{
			oldParentNode.getChildren().remove( _childNode );
		}

		ArrayList temp = new ArrayList();
		temp.add( _childNode );
		getResourceOutline().move( _parentNode, temp, -1, NodeModel.NORMAL );
	}

	public void setMaster( 
		final boolean _master )
	{
		this.master = _master;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName( 
		final String _name )
	{
		this.name = _name;
	}

	@Override
	public void setUndoController( 
		final DataFactoryUndoController _undoController )
	{
		this.undoController = _undoController;
	}

	@Override
	public String toString()
	{
		return name;
	}

	public void updateOutlineTypes()
	{
		NodeModel[] models = resourceOutlines.getOutlines();

		for (int i = 0; i < models.length; i++)
		{
			initOutline( models[ i ] );
		}
	}

	public static Object[] userResources()
	{
		Iterator i = globalPool.getResourceList().iterator();
		Resource resource;
		ArrayList result = new ArrayList();

		while (i.hasNext())
		{
			resource = (Resource)i.next();

			if (resource.isUser())
			{
				result.add( resource );
			}
		}

		return result.toArray();
	}

	/* (non-Javadoc)
	 * @see com.projity.grouping.core.NodeModelDataFactory#validateObject(java.lang.Object, com.projity.grouping.core.NodeModel)
	 */
	@Override
	public void validateObject( 
		final Object _newlyCreated,
		final NodeModel _nodeModel,
		final Object _eventSource,
		final Object _hierarchyInfo,
		final boolean _isNew )
	{
		if (!(_newlyCreated instanceof Resource))
		{
			return; // avoids VoidNodes
		}

		Resource resource = (Resource)_newlyCreated;

		((ResourceImpl)resource).getGlobalResource().setResourcePool( this );

		add( resource );

		if (_isNew)
		{
			initializeId( resource );
		}

		resourceOutlines.addToAll( _newlyCreated, _nodeModel ); // update all node models except the one passed in
															  //objectEventManager.fireCreateEvent(this,newlyCreated);
	}

	private static ResourcePool globalPool = null; // TODO is it ok to be global?
	private ArrayList projects = new ArrayList();
	private ArrayList resourceList = new ArrayList();

	//Undo
	protected transient DataFactoryUndoController undoController;
	private transient HashMap<Long,Resource> idMap = null;
	private ObjectEventManager objectEventManager = new ObjectEventManager();
	private OutlineCollection resourceOutlines = new OutlineCollectionImpl(Settings.numHierarchies(), this);
	private String name = "";
	private WorkingCalendar defaultCalendar;
	private transient boolean isDirty = false;
	protected boolean local;
	protected boolean master;
	private int resourceIdCounter = 0;
}
