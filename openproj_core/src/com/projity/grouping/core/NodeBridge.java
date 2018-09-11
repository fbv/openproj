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
package com.projity.grouping.core;

import com.projity.grouping.core.model.NodeModelUtil;
import com.projity.pm.assignment.Allocation;
import com.projity.pm.assignment.Assignment;
import com.projity.pm.resource.Resource;
import com.projity.server.data.DataObject;
import java.util.List;
import java.util.ListIterator;
import javax.swing.tree.DefaultMutableTreeNode;
import org.openproj.domain.task.Task;

/**
 * Bridge of the bridge pattern
 */
public class NodeBridge<V>
	extends DefaultMutableTreeNode
	implements Node<V>
{
	NodeBridge( 
		final V _impl )
	{
		//this.impl = impl;
		setValue( _impl );
	}

	/**
	 * Use NodeFactory instead
	 */
	NodeBridge( 
		final V _impl,
		final boolean _virtual )
	{
		this( _impl );
		myVirtual = _virtual;
	}

	/**
	 * @see com.projity.analysis.core.Node#accept(com.projity.analysis.core.NodeVisitor)
	 */
	@Override
	public void accept( 
		final NodeVisitor<V> _visitor )
	{
		_visitor.execute( this );
	}

	@Override
	public boolean canBeChildOf( 
		final Node<V> _parent )
	{
		if (_parent.isVoid() == true)
		{
			return false;
		}

		return NodeModelUtil.canBeChildOf( _parent, this );
	}

	@Override
	public ListIterator childrenIterator()
	{
		return (children == null)
			? emptyListIterator()
			: children.listIterator();
	}

	@Override
	public ListIterator childrenIterator( 
		final int _i )
	{
		return (children == null)
			? emptyListIterator()
			: children.listIterator( _i );
	}

	static ListIterator emptyListIterator()
	{
		return new ListIterator()
		{
			@Override
			public boolean hasNext()
			{
				return false;
			}

			@Override
			public Object next()
			{
				return null;
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public void add( 
				Object _object )
			{
			}

			@Override
			public boolean hasPrevious()
			{
				return false;
			}

			@Override
			public int nextIndex()
			{
				return 0;
			}

			@Override
			public Object previous()
			{
				return null;
			}

			@Override
			public int previousIndex()
			{
				return -1;
			}

			@Override
			public void set( 
				Object _object )
			{
			}
		};
	}

	@Override
	public List getChildren()
	{
		return children;
	}

	@Override
	public int getSubprojectLevel()
	{
		return mySubprojectLevel;
	}

	/**
	 * @see com.projity.analysis.core.Node#getType()
	 */
	@Override
	public Class getType()
		throws NodeException
	{
		final V value = (V)getUserObject();

		if (value == null)
		{
			throw new NodeException("No Implementation");
		}

		return value.getClass();
	}

	/**
	 * @return Returns the value.
	 */
	@Override
	public V getValue()
	{
		return (V)getUserObject();
	}

	@Override
	public boolean hasNumber()
	{
		final V value = getValue();

		return value instanceof Task || value instanceof Resource;
	}

	@Override
	public boolean isDirty()
	{
		final V value = getValue();

		if (value instanceof DataObject)
		{
			return ((DataObject)value).isDirty();
		}
		else
		{
			return false;
		}
	}

	@Override
	public boolean isInSubproject()
	{
		return getSubprojectLevel() > 0;
	}

	@Override
	public boolean isIndentable( 
		final int _value )
	{
		if (!((_value == 1) || (_value == -1)))
		{
			return false;
		}

		return !myRoot && !myVoidNode && !myVirtual && !(getValue() instanceof Assignment);
	}

	@Override
	public final boolean isLazyParent()
	{
		return getValue() instanceof LazyParent;
	}

//	public void setInSubproject(boolean inSubproject){
//		setSubprojectLevel(inSubproject?1:0);
//	}
	
	@Override
	public boolean isProtected()
	{
		if (myVoidNode || myVirtual)
		{
			return false;
		}

		if (getValue() instanceof Allocation)
		{
			return ((Allocation)getValue()).isProtected();
		}

		return false;
	}

	@Override
	public boolean isRoot()
	{
		return myRoot;
	}

	public final boolean isValidLazyParent()
	{
		return getValue() instanceof LazyParent && ((LazyParent)getValue()).isValid();
	}

	/**
	 * @see com.projity.grouping.core.Node#isVirtual()
	 */
	@Override
	public boolean isVirtual()
	{
		return myVirtual;
	}

	@Override
	public boolean isVoid()
	{
		return myVoidNode;
	}

	@Override
	public void setDirty( 
		final boolean _dirty )
	{
		//System.out.println("NodeBridge _setDirty("+dirty+")");
		final V value = getValue();

		if (value instanceof DataObject)
		{
			((DataObject)value).setDirty( _dirty );
		}
	}

	@Override
	public void setRoot( 
		final boolean _root )
	{
		myRoot = _root;
	}

	@Override
	public void setSubprojectLevel( 
		final int _subprojectLevel )
	{
		mySubprojectLevel = _subprojectLevel;
	}

	@Override
	public final void setValue( 
		final V _value )
	{
		myVirtual = (_value instanceof GroupNodeImpl);
		myVoidNode = (_value instanceof VoidNodeImpl);
		setUserObject( _value );
	}

	/**
	 * @param _virtual The virtual to set.
	 */
	@Override
	public void setVirtual( 
		final boolean _virtual )
	{
		myVirtual = _virtual;
	}

	@Override
	public void setVoid( 
		final boolean _voidNode )
	{
		myVoidNode = _voidNode;
	}

	@Override
	public void showProtectionWarning()
	{
		if (getValue() instanceof Allocation == true)
		{
			((Allocation)getValue()).showProtectionWarning();
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		final Object impl = getUserObject();

		if (impl == null)
		{
			return "null";
		}

		return impl.toString();
	}

	protected transient boolean myLazyParent = false; // for subprojects
	protected boolean myRoot = false;

	//protected Object impl;
	protected boolean myVirtual = false;
	protected boolean myVoidNode = false;
	protected int mySubprojectLevel;
}
