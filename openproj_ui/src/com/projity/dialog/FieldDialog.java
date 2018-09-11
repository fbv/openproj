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
package com.projity.dialog;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import com.projity.dialog.util.FieldComponentMap;

import org.openproj.domain.document.Document;
import com.projity.document.ObjectEvent;

import com.projity.grouping.core.Node;
import com.projity.grouping.core.model.NodeModel;

import com.projity.pm.graphic.frames.DocumentSelectedEvent;
import com.projity.pm.graphic.spreadsheet.selection.event.SelectionNodeEvent;
import com.projity.pm.graphic.spreadsheet.selection.event.SelectionNodeListener;
import com.projity.pm.resource.Resource;
import com.projity.pm.resource.ResourcePool;
import com.projity.pm.scheduling.Schedule;
import com.projity.pm.scheduling.ScheduleEvent;
import com.projity.pm.scheduling.ScheduleEventListener;
import org.openproj.domain.document.BelongsToDocument;
import com.projity.pm.task.Project;

import com.projity.strings.Messages;

import com.projity.util.DataUtils;

import org.apache.commons.lang.StringUtils;

import java.awt.Frame;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.undo.UndoableEditSupport;

import org.openproj.domain.task.Task;

/**
 *
 */
public abstract class FieldDialog
	extends AbstractDialog
	implements ObjectEvent.Listener,
		ScheduleEventListener,
		SelectionNodeListener,
		DocumentSelectedEvent.Listener
{
	protected FieldDialog( 
		final Frame _owner,
		final String _title,
		final boolean _modal,
		final boolean _multipleObjects 
		/*,final UndoableEditSupport _undoableEditSupport*/ )
	{
		super( _owner, _title, _modal );
		myMultipleObjects = _multipleObjects;

		//myUndoableEditSupport = _undoableEditSupport;

		// need to update all initially, but do later on once things are set
		SwingUtilities.invokeLater( new Runnable()
		{
			@Override
			public void run()
			{
				updateAll();
			}
		} );
	}

	@Override
	protected void activateListeners()
	{
	}

	@Override
	public JComponent createContentPanel()
	{
		// TODO Auto-generated method stub
		return null;
	}

	protected JComponent createFieldsPanel( 
		final FieldComponentMap _map,
		final Collection _fields )
	{
		if ((_fields == null) 
		 || (_fields.size() == 0))
		{
			return null;
		}

		// repeats and gets rid of last comma  
		final FormLayout layout = new FormLayout( "p, 3dlu, fill:160dlu:grow", StringUtils.chomp( StringUtils.repeat( "p,3dlu,", 
			_fields.size() ) ) ); 

		final DefaultFormBuilder builder = new DefaultFormBuilder( layout );
		_map.append( builder, _fields );

		return builder.getPanel();
	}

	protected FieldComponentMap createMap()
	{
		FieldComponentMap map;

		if (myMultipleObjects == true)
		{
			map = new FieldComponentMap( myCollection );
		}
		else if (myNode != null)
		{
			map = new FieldComponentMap( myNode, myNodeModel );
		}
		else
		{
			map = new FieldComponentMap( myObject );
		}

		myMaps.add( map );

		return map;
	}

	@Override
	protected void desactivateListeners()
	{
		setObject( null );
	}

	@Override
	public void documentSelected( 
		final DocumentSelectedEvent _event )
	{
		System.out.println( Messages.getString( "FieldDialog.document" ) + _event.getCurrent() ); //$NON-NLS-1$
	}

	protected Collection getCollection()
	{
		return myCollection;
	}

	protected Object getFirstObject()
	{
		if (myCollection == null)
		{
			return myObject;
		}

		Iterator i = myCollection.iterator();

		if (i.hasNext() == true)
		{
			return i.next();
		}

		return null;
	}

	protected Object getObject()
	{
		return myObject;
	}

	public Class<?> getObjectClass()
	{
		return myObjectClass;
	}

	@Override
	public void objectChanged( 
		final ObjectEvent _objectEvent )
	{
		if (isVisible() == false)
		{
			return;
		}

		if (myMultipleObjects && myCollection.contains( _objectEvent.getObject() ))
		{
			updateAll(); // if in list, need to update all
		}
		else if (_objectEvent.getObject() == getObject())
		{
			updateAll();
		}
	}

//	public void hide() 
//	{
//		setObject( null );
//		super.hide();
//	}
	
	@Override
	protected void onCancel()
	{
		updateAll();
	}

	/**
	 * On pressing enter key, check any unvalidated component
	 */
	@Override
	public void onOk()
	{
		if (myDirtyComponent != null)
		{
			InputVerifier verifier = myDirtyComponent.getInputVerifier();

			if (verifier.shouldYieldFocus( myDirtyComponent ) == false)
			{
				return;
			}
		}

		super.onOk();
	}

	@Override
	public void scheduleChanged( 
		final ScheduleEvent _scheduleEvent )
	{
		if (isVisible() == false)
		{
			return;
		}

		if (myMultipleObjects == true)
		{
			//to be more precise could see if one of the collection's objects is dirty
			// if in list, need to update all
			updateAll(); 
		}
		else if ((getObject() != null) 
			  && (((Schedule)getObject()).isJustModified() == true))
		{
			updateAll();
		}
	}

	@Override
	public void selectionChanged( 
		final SelectionNodeEvent _event )
	{
		if (isVisible() == false)
		{
			return;
		}

		if (myMultipleObjects == true)
		{
			setCollection( _event.getNodes() );
		}
		else
		{
			final Node selected = _event.getCurrentNode();

			if (selected == null)
			{
				return;
			}

			setObject( DataUtils.extractObjectOfClass( selected.getValue(), myObjectClass ) );
			updateAll();
		}
	}

	public void setCollection( 
		final Collection _nodeList )
	{
		DataUtils.extractObjectsOfClassFromNodeList( myCollection, _nodeList, myObjectClass );
	}

	public void setDirtyComponent( 
		final JComponent _dirtyComponent )
	{
		myDirtyComponent = _dirtyComponent;
	}

	/**
	 * 
	 * @param _object 
	 */
	public void setObject( 
		final Object _object )
	{
		if (_object == myObject)
		{
			return;
		}

		if ((myObject != null) 
		 && (myObject instanceof BelongsToDocument == true))
		{
			final Document document = ((BelongsToDocument)myObject).getDocument();
			document.removeObjectListener( this );

			if (document instanceof Project)
			{
				((Project)document).removeScheduleListener( this );
			}
		}

		myObject = _object;

		if ((_object != null) 
		 && (_object instanceof BelongsToDocument == true))
		{
			final Document document = ((BelongsToDocument)myObject).getDocument();
			document.addObjectListener( this );

			if (document instanceof Project == true)
			{
				final Project project = (Project)document;
				myNodeModel = project.getTaskOutline();
				myNode = myNodeModel.search( _object ); //dialogs are base on object not node, need to search
				project.addScheduleListener( this );
			}
			else if (document instanceof ResourcePool == true)
			{
				final ResourcePool resourcePool = (ResourcePool)document;
				myNodeModel = resourcePool.getResourceOutline();
				myNode = myNodeModel.search( _object );
			}
			else
			{
				myNode = null;
				myNodeModel = null;
			}
		}
		else
		{
			myNode = null;
			myNodeModel = null;
		}

//		if (_object != null && _object instanceof BelongsToDocument) 
//		{
//			Document document=((BelongsToDocument)myObject).getDocument();
//			document.addObjectListener(this);
//			if (document instanceof Project)
//				((Project)document).addScheduleListener(this);
//		}
	}

	public void setObjectClass( 
		final Class<?> _objectClass )
	{
		myObjectClass = _objectClass;
	}

	public void setType( 
		final boolean _task )
	{
		myObjectClass = (_task)
			? Task.class
			: Resource.class;
		setTitle( (_task)
			? Messages.getString( "FieldDialog.TaskInformation" )
			: Messages.getString( "FieldDialog.ResourceInformation" ) );
	}

	protected void setVisibleAndEnabledState()
	{
		boolean showing = (myObject != null);

		if (myMainComponent != null)
		{
			myMainComponent.setEnabled( showing );
		}
	}

	protected void updateAll()
	{
		setVisibleAndEnabledState();

		final Iterator<FieldComponentMap> i = myMaps.iterator();

		while (i.hasNext() == true)
		{
			final FieldComponentMap map = i.next();
			map.setObject( myObject );
			map.setNode( myNode );
			map.setNodeModel( myNodeModel );
			map.updateAll();
		}
	}

	protected ArrayList myCollection = new ArrayList();
	protected ArrayList<FieldComponentMap> myMaps = new ArrayList<FieldComponentMap>();
	private Class<?> myObjectClass;
	private JComponent myDirtyComponent;
	protected JComponent myMainComponent = null;
	protected Node myNode;
	protected NodeModel myNodeModel;
	protected Object myObject;
	private UndoableEditSupport myUndoableEditSupport;
	private boolean myMultipleObjects;
}
