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
package com.projity.configuration;

import com.projity.field.Field;

import com.projity.graphic.configuration.GraphicConfiguration;
import com.projity.pm.assignment.TimeDistributedHelper;

import com.projity.strings.Messages;

import com.projity.timescale.TimeScaleManager;

import com.projity.util.ClassUtils;
import com.projity.util.Environment;

import org.apache.commons.digester.Digester;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * Main access to objects described in configuration files
 */
public class Configuration
	implements ProvidesDigesterEvents
{
	public Configuration()
	{
	}

	public static void cleanUp()
	{
		instance = null;
	}

	@Override
	public void addDigesterEvents( 
		final Digester _digester )
	{
		addGlobalDigesterEvents( _digester );
		FieldDictionary.addDigesterEvents( _digester );

		//set time scale's zoom levels
		TimeScaleManager.addDigesterEvents( _digester );

		ScriptConfiguration.addDigesterEvents( _digester );

		//graphic config
		GraphicConfiguration.addDigesterEvents( _digester );

		String digesterEventProviderString = Messages.getMetaString( "DigesterEventProviders" );

		if (digesterEventProviderString != null)
		{
			String[] digesterEventProviders = digesterEventProviderString.split( ";" );

			for (String digesterProvider : digesterEventProviders)
			{
				try
				{
					Class.forName( digesterProvider ).getMethod( "addDigesterEvents", 
						new Class[]
					{
						Digester.class
					} ).invoke( null, 
						new Object[]
					{
						_digester
					} );
				}
				catch (Exception e)
				{
					System.out.println( "Could not get digester events for " + digesterProvider );
				}
			}
		}
	}

	public static Collection<Field> fromIdArray( 
		final Collection<String> _fieldArray )
	{
		ArrayList result = new ArrayList( _fieldArray.size() );
		Iterator<String> itor = _fieldArray.iterator();

		while (itor.hasNext() == true)
		{
			result.add( Configuration.getFieldFromId( itor.next() ) );
		}

		return result;
	}

	public static Field[] fromIdArray( 
		final String[] _fieldArray )
	{
		final Field[] result = new Field[ _fieldArray.length ];

		for (int i = 0; i < _fieldArray.length; i++)
		{
			result[ i ] = Configuration.getFieldFromId( _fieldArray[ i ] );
		}

		return result;
	}

	public static Collection getAllFields()
	{
		return getInstance().getFieldDictionary().getAllFields();
	}

	public static final Field getFieldFromShortId( 
		final String _id )
	{
		Field f = getFieldFromId( Field.longName( _id ) );

		if ((f == null) && Environment.isNoPodServer())
		{
			f = getFieldFromId( Field.longCustomName( _id ) );
		}

		if (f == null)
		{
			return getFieldFromId( _id );
		}

		return f;
	}

	public static Field getFieldFromId( 
		final String _id )
	{
		return getInstance().getFieldDictionary().getFieldFromId( _id );
	}

	/**
	 * @return Returns the fieldDictionary.
	 */
	public FieldDictionary getFieldDictionary()
	{
		return myFieldDictionary;
	}

	/**
	 * @return Returns the graphic configuration attribute.
	 */
	public GraphicConfiguration getGraphicConfiguration()
	{
		return myGraphicConfiguration;
	}

	public static synchronized Configuration getInstance()
	{
		if (instance == null)
		{
			instance = new Configuration();

			String[] files = Messages.getMetaString( "ConfigurationFiles" ).split( ";" );

			for (String file : files)
			{
				ConfigurationReader.read( file, instance );
			}

			String additionalConfiguration = Messages.getMetaString( "AdditionalConfiguration" );

			try
			{
				ClassUtils.invokeVoidStaticMethodFromFullName( additionalConfiguration );
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			instance.setDonePopulating(); // makes its hash table fast if using a FastHashMap
		}

		return instance;
	}

	public Set<String> getRemovedMenu()
	{
		return myRemoveMenu;
	}

	public ScriptConfiguration getScriptConfiguration()
	{
		return myScriptConfiguration;
	}

	/**
	 * @return Returns the time scales attribute.
	 */
	public TimeScaleManager getTimeScales()
	{
		return myTimeScales;
	}

	public boolean isRemovedMenu( 
		final String id )
	{
		return myRemoveMenu.contains( id );
	}

	public void removeMenu( 
		final String _id )
	{
		Configuration.getInstance().getRemovedMenu().add( _id );
	}

	//	public static void clear() {
	//		Dictionary.getInstance().clear(); // need to reload everything into dictionary
	//		instance = null;
	//	}
	public void setDonePopulating()
	{
		myFieldDictionary.setDonePopulating(); // makes its hash table fast if using a FastHashMap
	}

	/**
	 * @param fieldDictionary The fieldDictionary to set.
	 */
	public void setFieldDictionary( 
		final FieldDictionary _fieldDictionary )
	{
		myFieldDictionary = _fieldDictionary;
	}

	/**
	 * @param graphicConfiguration The graphicConfiguration to set.
	 */
	public void setGraphicConfiguration( 
		final GraphicConfiguration _graphicConfiguration )
	{
		myGraphicConfiguration = _graphicConfiguration;
	}

	public void setIntConstant( 
		final String _name,
		final int _value )
	{
		ClassUtils.setStaticField( _name, _value );
	}

	public void setScriptConfiguration( 
		final ScriptConfiguration _scriptConfiguration )
	{
		myScriptConfiguration = _scriptConfiguration;
	}

	public void setStringConstant( 
		final String _name,
		final String _value )
	{
		ClassUtils.setStaticField( _name, _value );
	}

	/**
	 * @param timeScales The timeScales to set.
	 */
	public void setTimeScales( 
		final TimeScaleManager _timeScales )
	{
		myTimeScales = _timeScales;
	}

	public static Collection<String> toIdArray(
		final Collection<Field> _fieldArray ) 
	{
		final ArrayList<String> result = new ArrayList<String>( _fieldArray.size() );
		final Iterator<Field> itor = _fieldArray.iterator();
		while (itor.hasNext() == true) 
		{
			result.add( itor.next().getId() );
		}
		
		return result;
	}
	
	public static String[] toIdArray(
		final Field[] _fieldArray) 
	{
		final String[] result = new String[ _fieldArray.length ];
		for (int i = 0; i < _fieldArray.length; i++)
		{
			result[ i ] = _fieldArray[ i ].getId();
		}
		
		return result;
	}

	private void addGlobalDigesterEvents( 
		final Digester _digester )
	{
		_digester.addCallMethod( "configuration/constants/int", "setIntConstant", 2, 
			new Class[]
		{
			String.class,
			Integer.class
		} );
		
		_digester.addCallParam( "configuration/constants/int/name", 0 );
		_digester.addCallParam( "configuration/constants/int/value", 1 );

		_digester.addCallMethod( "configuration/constants/String", "setStringConstant", 2, 
			new Class[]
		{
			String.class,
			String.class
		} );
		
		_digester.addCallParam( "configuration/constants/String/name", 0 );
		_digester.addCallParam( "configuration/constants/String/value", 1 );

		_digester.addCallMethod( "configuration/removeMenu", "removeMenu", 1, 
			new Class[]
		{
			String.class
		} );
		
		_digester.addCallParam( "configuration/removeMenu", 0 );
	}

	private static Configuration instance = null;

	private FieldDictionary myFieldDictionary = null;
	private GraphicConfiguration myGraphicConfiguration = null;
	private ScriptConfiguration myScriptConfiguration = null;
	private Set<String> myRemoveMenu = new HashSet<String>();
	private TimeScaleManager myTimeScales = null;
}
