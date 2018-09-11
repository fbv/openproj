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
package com.projity.field;

import com.projity.util.ClassUtils;

import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.apache.commons.lang.ObjectUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * This class manages a fixed list of values and their associated options, similar to an html select
 * It is used by Field class
 */
public class StaticSelect
	extends Select
{
	public StaticSelect()
	{
	}

	public void add( 
		String _key,
		Object _value )
	{
		put( _key, _value );
	}

	public void addOption( 
		final SelectOption _option )
	{
		if (myIntegerValues == true)
		{
			_option.value = Integer.valueOf( _option.value.toString() );
		}

		add( _option.key, _option.value );

		Object staticObject = _option.getStaticObject();

		if (staticObject != null)
		{ 
			// if object associated, use it
			if (myObjectMap == null)
			{
				myObjectMap = new DualHashBidiMap();
			}

			myObjectMap.put( _option.value, staticObject );
		}
	}

	public void addString( 
		String _string )
	{
		add( _string, _string );
	}

	/* (non-Javadoc)
	 * @see com.projity.field.Select#getKey(java.lang.Object)
	 */
	@Override
	public Object getKey( 
		Object _value,
		Object _object )
	{
		populateIfLazy( _object );

		return myStringMap.getKey( _value );
	}

	@Override
	public Object[] getKeyArrayWithoutNull( 
		Object _object )
	{
		populateIfLazy( _object );

		synchronized (this)
		{
			if (myKeyArray == null)
			{
				myKeyArray = new Object[ myOrderedValueList.size() ];

				Iterator i = myOrderedValueList.iterator();
				int index = 0;

				while (i.hasNext())
				{
					Object n = i.next();
					myKeyArray[ index++ ] = myStringMap.getKey( n );
				}
			}
		}

		return myKeyArray;
	}

	/* (non-Javadoc)
	 * @see com.projity.field.Select#get(java.lang.Object)
	 */
	@Override
	public final Object getValue( 
		final Object _key )
		throws InvalidChoiceException
	{
		populateIfLazy( null );

		if ((_key == EMPTY) 
		 && (isAllowNull() == true))
		{
			return null;
		}

		final Object result = myStringMap.get( _key );

		if (result == null)
		{
			throw new InvalidChoiceException( ObjectUtils.toString( _key ) );
		}

		return result;
	}

	@Override
	public List getValueListWithoutNull( 
		Object _object )
	{
		populateIfLazy( _object );

		return myOrderedValueList;
	}

	/* (non-Javadoc)
	 * @see java.util.Map#isEmpty()
	 */
	@Override
	public boolean isEmpty()
	{
		return myStringMap.isEmpty();
	}

	public final boolean isIntegerValues()
	{
		return myIntegerValues;
	}

	/* (non-Javadoc)
	 * @see com.projity.field.Select#isStatic()
	 */
	@Override
	public boolean isStatic()
	{
		return myIsStatic;
	}

	private synchronized void populateIfLazy( 
		Object _object )
	{
		if (myPopulateMethod != null)
		{
			Boolean result = false;
			Method m = ClassUtils.staticMethodFromFullName( myPopulateMethod, 
				new Class[]
			{
				getClass(),
				Object.class
			} );

			try
			{
				result = (Boolean)m.invoke( null, 
					new Object[]
				{
					this,
					_object
				} );
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			if (result)
			{
				myPopulateMethod = null;
			}
		}
	}

	/**
	 * @param _key
	 * @param _value
	 * @return
	 */
	@Override
	public Object put( 
		final String _key,
		final Object _value )
	{
		myOrderedValueList.add( _value );

		return myStringMap.put( _key, _value );
	}

	public final void setIntegerValues( 
		final boolean _integerValues )
	{
		myIntegerValues = _integerValues;
	}

	public final void setPopulateMethod( 
		String _populateMethod )
	{
		myPopulateMethod = _populateMethod;
	}

	public final void setStatic( 
		boolean _isStatic )
	{
		myIsStatic = _isStatic;
	}

	/* (non-Javadoc)
	 * @see java.util.Map#size()
	 */
	@Override
	public int size()
	{
		return myStringMap.size();
	}

	@Override
	public final String toString()
	{
		final MapIterator i = myStringMap.mapIterator();
		final StringBuffer result = new StringBuffer();

		while (i.hasNext() == true)
		{
			i.next();
			result.append( "[key]" + i.getKey() + " [value]" + i.getValue() + "\n" );
		}

		return result.toString();
	}

	private ArrayList<Object> myOrderedValueList = new ArrayList<Object>();
	private DualHashBidiMap myObjectMap = null;
	private DualHashBidiMap<String,Object> myStringMap = new DualHashBidiMap<String,Object>();
	private String myPopulateMethod = null;
	private Object[] myKeyArray = null;
	private boolean myIntegerValues = true;
	private boolean myIsStatic = true;

	@Override
	public boolean containsKey( Object o )
	{
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	@Override
	public boolean containsValue( Object o )
	{
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	@Override
	public String remove( Object o )
	{
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	@Override
	public void putAll( Map<? extends String, ? extends Object> map )
	{
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	@Override
	public void clear()
	{
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	@Override
	public Set<String> keySet()
	{
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	@Override
	public Collection<Object> values()
	{
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	@Override
	public Set<Entry<String, Object>> entrySet()
	{
		throw new UnsupportedOperationException( "Not supported yet." );
	}
}
