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

import com.projity.contrib.util.Log;
import com.projity.contrib.util.LogFactory;

import com.projity.field.Field;

import com.projity.pm.assignment.Assignment;
import com.projity.pm.dependency.Dependency;
import com.projity.pm.resource.Resource;
import com.projity.pm.resource.ResourceImpl;
import com.projity.pm.task.Project;

import com.projity.strings.Messages;

import com.projity.util.ClassUtils;
import com.projity.util.Environment;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;
//import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.Predicate;
//import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.digester.Digester;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openproj.domain.model.task.Task;


/**
 * Dictionary of all fields
 */
public class FieldDictionary
{
	public static void addDigesterEvents( 
		final Digester _digester )
	{
//		digester.addObjectCreate("*/fieldDictionary", "com.projity.configuration.FieldDictionary");
		_digester.addFactoryCreate( "*/fieldDictionary", "com.projity.configuration.FieldDictionaryFactory" );
		_digester.addSetNext( "*/fieldDictionary", "setFieldDictionary", "com.projity.configuration.FieldDictionary" ); //TODO can we do this more easily
		_digester.addSetProperties( "*/fieldDictionary/class", "name", "className" ); // object is field dictionary

		_digester.addFactoryCreate( "*/field", "com.projity.field.FieldFactory" );
		_digester.addSetProperties( "*/field" );
		_digester.addSetNext( "*/field", "addField", "com.projity.field.Field" );

		_digester.addObjectCreate( "*/field/select", "com.projity.field.StaticSelect" ); // create a select
		_digester.addSetProperties( "*/field/select" ); // set name of select
		_digester.addSetNext( "*/field/select", "setSelect", "com.projity.field.StaticSelect" ); // attach to field

		_digester.addObjectCreate( "*/field/choice", "com.projity.field.DynamicSelect" ); // create a choice
		_digester.addSetProperties( "*/field/choice" ); // set name of choice, finder and list methods
		_digester.addSetNext( "*/field/choice", "setSelect", "com.projity.field.Select" ); // attach to field

		_digester.addObjectCreate( "*/field/select/option", "com.projity.field.SelectOption" ); // create an option when seeing one
		_digester.addSetProperties( "*/field/select/option" ); // get key and value properties
		_digester.addSetNext( "*/field/select/option", "addOption", "com.projity.field.SelectOption" ); // add option to select

		_digester.addObjectCreate( "*/field/range", "com.projity.field.Range" ); // create an option when seeing one
		_digester.addSetProperties( "*/field/range" ); // get key and value properties
		_digester.addSetNext( "*/field/range", "setRange", "com.projity.field.Range" ); // add option to select

		//non intrusive method to reduce role options, otherwise Select should be modified to depend on a specific object
		_digester.addObjectCreate( "*/field/filter", "com.projity.field.OptionsFilter" );
		_digester.addSetProperties( "*/field/filter" );
		_digester.addSetNext( "*/field/filter", "setFilter", "com.projity.field.OptionsFilter" );

		final String fieldAccessibleClass = Messages.getMetaString( "FieldAccessible" );
		_digester.addObjectCreate( "*/field/permission", fieldAccessibleClass );
		_digester.addSetProperties( "*/field/permission" );
		_digester.addSetNext( "*/field/permission", "setAccessControl", "com.projity.field.FieldAccessible" );

		_digester.addCallMethod( "*/fieldDictionary/removeField", "removeField", 1, 
			new Class[]
		{
			String.class
		} );

		_digester.addCallParam( "*/fieldDictionary/removeField", 0 );
	}

	/**
	 * Extract fields that have extra status, and also optionally that have validOnOjbectCreate status
	 */
	public static LinkedList<Field> extractExtraFields( 
		final Collection<Field> _from,
		final boolean _mustBeValidOnObjectCreate )
	{
		LinkedList<Field> result = new LinkedList<Field>();
		CollectionUtils.select( _from,
			new Predicate<Field>()
		{
			@Override
			public boolean evaluate( 
				final Field _field )
			{
				return _field.isExtra() && (!_mustBeValidOnObjectCreate || _field.isValidOnObjectCreate());
			}
		}, result );

		return result;
	}

	public static void generateFieldDoc( 
		final String _fileName )
	{
		final StringBuffer result = new StringBuffer();
		result.append( "<html><body>" );
		fieldsToHtmlTable( result, "Project Fields", FieldDictionary.getInstance().getProjectFields() );
		fieldsToHtmlTable( result, "Resource Fields", FieldDictionary.getInstance().getResourceFields() );
		fieldsToHtmlTable( result, "Task Fields", FieldDictionary.getInstance().getTaskFields() );
		fieldsToHtmlTable( result, "Assignment Fields", FieldDictionary.getInstance().getAssignmentFields() );
		fieldsToHtmlTable( result, "Dependency Fields", FieldDictionary.getInstance().getDependencyFields() );
		result.append( "</body></html>" );

		try
		{
			new FileOutputStream( _fileName ).write( result.toString().getBytes() );
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void generateSpreadsheetDefinition( 
		final String _fileName )
	{
		final StringBuffer result = new StringBuffer();
		generateSpreadsheetDefinition( result, "All Task", "taskSpreadsheet", FieldDictionary.getInstance().getTaskFields() );

		try
		{
			new FileOutputStream( _fileName ).write( result.toString().getBytes() );
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static HashMap<String,String> getAliasMap()
	{
		final HashMap<String,String> aliasMap = new HashMap<String,String>();
		final Iterator<Map.Entry<String,Field>> itor = getInstance().myMap.entrySet().iterator();

		while (itor.hasNext() == true)
		{
			final Map.Entry<String,Field> entry = itor.next();
			final String key = entry.getKey();
			final Field field = entry.getValue();

			if (field.getAlias() != null)
			{
				aliasMap.put( field.getId(), field.getAlias() );
			}
		}

		return aliasMap;
	}

	public static FieldDictionary getInstance()
	{
		return Configuration.getInstance().getFieldDictionary();
	}

	public static void main( 
		final String[] _args )
	{
		generateFieldDoc( "/opt/fields.html" );
	}

	public static void setAliasMap( 
		final HashMap<String,String> _aliasMap )
	{
		if (_aliasMap == null)
		{
			return;
		}

		final Iterator<String> itor = _aliasMap.keySet().iterator();

		while (itor.hasNext() == true)
		{
			final String fieldId = itor.next();
			final Field f = Configuration.getFieldFromId( fieldId );

			if (f != null)
			{
				f.setAlias( _aliasMap.get( fieldId ) );
			}
		}
	}

	public void addField( 
		final Field _field )
	{
		if (Configuration.getFieldFromId( _field.getId() ) != null) // if already exists
		{
			return;
		}

		if ((_field.isServer() == true) 
		 && (Environment.getStandAlone() == true))
		{
			return;
		}

		_field.setClass( myClass );

		if (_field.build() == true)
		{
			if (_field.isIndexed() == true)
			{
				for (int i = 0; i < _field.getIndexes(); i++)
				{
					final Field indexField = _field.createIndexedField( i );
					log.debug( "adding indexfield " + myClass.getName() + "." + indexField.getName() + " id " + indexField.getId() +
						" field " + indexField );
					myMap.put( indexField.getId(), indexField );
				}
			}
			else
			{
				log.debug( "adding field " + myClass.getName() + "." + _field.getName() + " " + _field );
				myMap.put( _field.getId(), _field );

				if (_field.getAction() != null)
				{
					myActionMap.put( _field.getAction(), _field );
				}
			}
		}
		else
		{
			log.warn( "Field not added" + _field.getId() );
		}
	}

	public Field getActionField( 
		final String _action )
	{
		return myActionMap.get( _action );
	}

	/**
	 * @return Returns the assignmentFields.
	 */
	public LinkedList<Field> getAssignmentFields()
	{
		return myAssignmentFields;
	}

	/**
	 * @return Returns the dependencyFields.
	 */
	public LinkedList<Field> getDependencyFields()
	{
		return myDependencyFields;
	}

	public Field getFieldFromId( 
		final String _id )
	{
		return myMap.get( _id );
	}

	/**
	 * @return Returns the projectFields.
	 */
	public LinkedList<Field> getProjectFields()
	{
		return myProjectFields;
	}

	/**
	 * @return Returns the resourceAndAssignmentFields.
	 */
	public LinkedList<Field> getResourceAndAssignmentFields()
	{
		return myResourceAndAssignmentFields;
	}

	/**
	 * @return Returns the resourceFields.
	 */
	public LinkedList<Field> getResourceFields()
	{
		return myResourceFields;
	}

	/**
	 * @return Returns the taskAndAssignmentFields.
	 */
	public LinkedList<Field> getTaskAndAssignmentFields()
	{
		return myTaskAndAssignmentFields;
	}

	/**
	 * @return Returns the taskFields.
	 */
	public LinkedList<Field> getTaskFields()
	{
		return myTaskFields;
	}

	public void populateListWithFieldsOfType( 
		final List<Field> _list,
		final Class _class )
	{
		populateListWithFieldsOfType( _list, 
			new Class[]
		{
			_class
		} );
	}

	/** Fill a collection with all fields that are applicable to one or more types
	 * specified by class.  The collection is sorted alpha-numerically by field name.
	 * Lists by type should probably just be cached in static variables.
	 * @param _list - list to fill
	 * @param _class - array of class types
	 */
	public void populateListWithFieldsOfType( 
		final List<Field> _list,
		final Class[] _class )
	{
		final Iterator<Map.Entry<String,Field>> itor = myMap.entrySet().iterator();

		while (itor.hasNext() == true)
		{
			final Map.Entry<String,Field> entry = itor.next();
			final String key = entry.getKey();
			final Field field = entry.getValue();

			if (field.isNotChoosable() == true)
			{
				continue;
			}

			if (field.isApplicable( _class ) == true)
			{
				_list.add( field );
			}
		}

		Collections.sort( _list );
	}

	public void removeField( 
		final String _id )
	{
		myMap.remove( _id );
	}

	public void setClassName( 
		final String _className )
	{
		//System.out.println( "FieldDictionary.setClassName( " + _className + " )" );
		try
		{
			myClass = ClassUtils.forName( _className );
		}
		catch (ClassNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	Collection<Field> getAllFields()
	{
		return myMap.values();
	}

	void setDonePopulating()
	{
		makeAlternateFieldsNotChoosable();

		// in case we use a FastHashMap do this:		map.setFast(true);
		myTaskFields = new LinkedList<Field>();
		myResourceFields = new LinkedList<Field>();
		myAssignmentFields = new LinkedList<Field>();
		myDependencyFields = new LinkedList<Field>();
		myProjectFields = new LinkedList<Field>();
		myTaskAndAssignmentFields = new LinkedList<Field>();
		myResourceAndAssignmentFields = new LinkedList<Field>();

		populateListWithFieldsOfType( myTaskFields, Task.class );
		populateListWithFieldsOfType( myResourceFields, ResourceImpl.class );
		populateListWithFieldsOfType( myAssignmentFields, Assignment.class );
		populateListWithFieldsOfType( myDependencyFields, Dependency.class );
		populateListWithFieldsOfType( myProjectFields, Project.class );
		populateListWithFieldsOfType( myTaskAndAssignmentFields, 
			new Class[]
		{
			Task.class,
			Assignment.class
		} );

		populateListWithFieldsOfType( myResourceAndAssignmentFields, 
			new Class[]
		{
			Resource.class,
			Assignment.class
		} );
	}

	private static void fieldsToHtmlTable( 
		final StringBuffer _result,
		final String _title,
		final Collection _fields )
	{
		_result.append( "<p><b>" ).append( _title ).append( "</b><br />" );
		_result.append( "<table border='1'>" );
		tabbedStringToHtmlRow( _result, Field.getMetadataStringHeader(), true );
		CollectionUtils.forAllDo( _fields,
			new Closure()
		{
			@Override
			public void execute( 
				Object arg0 )
			{
				Field f = (Field)arg0;

				if (f.isCustom())
				{
					return;
				}

				tabbedStringToHtmlRow( _result, f.getMetadataString(), false );
			}
		} );

		_result.append( "</table>" );
		_result.append( "</p>" );
	}

	private static void generateSpreadsheetDefinition( 
		final StringBuffer _result,
		final String _name,
		final String _category,
		final Collection _fields )
	{
		_result.append( "<spreadsheet name=" + "\"" + _name + "\" category=" + "\"" + _category + "\">\n" );
		_result.append( "\t<columns>\n" );

		Iterator i = _fields.iterator();

		while (i.hasNext())
		{
			Field f = (Field)i.next();

			if (f.isCustom() || f.isExtra())
			{
				continue;
			}

			_result.append( "\t\t<column>" + f.getId() + "</column>\n" );
		}

		_result.append( "\t</columns>\n" );
		_result.append( "</spreadsheet>\n" );
	}

	private static void tabbedStringToHtmlRow( 
		final StringBuffer _result,
		final String _colString,
		final boolean _header )
	{
		_result.append( "<tr>" );

		String[] cols = _colString.split( "\t" );

		for (String col : cols)
		{
			_result.append( _header
				? "<th>"
				: "<td>" ).append( col ).append( _header
				? "</th>"
				: "</td>" );
		}

		_result.append( "</tr>" );
	}

	/**
	 * Fields with alternates replace them, so the alternates should no longer show up
	 */
	private void makeAlternateFieldsNotChoosable()
	{
		for (Field f : (Collection<Field>)getAllFields())
		{
			final String alternate = f.getAlternateId();

			if (alternate != null)
			{
				Field alt = getFieldFromId( alternate );

				if (alt != null)
				{
					alt.setNotChoosable( true );
				}
			}
		}
	}

	private static Log log = LogFactory.getLog( FieldDictionary.class );
	
	private Class<?> myClass;
	private HashMap<String,Field> myActionMap = new HashMap<String,Field>();
	private HashMap<String,Field> myMap = new HashMap<String,Field>();
	private LinkedList<Field> myAssignmentFields = new LinkedList<Field>();
	private LinkedList<Field> myDependencyFields = new LinkedList<Field>();
	private LinkedList<Field> myProjectFields = new LinkedList<Field>();
	private LinkedList<Field> myResourceAndAssignmentFields = new LinkedList<Field>();
	private LinkedList<Field> myResourceFields = new LinkedList<Field>();
	private LinkedList<Field> myTaskAndAssignmentFields = new LinkedList<Field>();
	private LinkedList<Field> myTaskFields = new LinkedList<Field>();
}
