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

import com.projity.configuration.Configuration;

import com.projity.contrib.util.Log;
import com.projity.contrib.util.LogFactory;

import com.projity.datatype.Duration;
import com.projity.datatype.DurationFormat;
import com.projity.datatype.Hyperlink;
import com.projity.datatype.Money;
import com.projity.datatype.PercentFormat;
import com.projity.datatype.RateFormat;
import com.projity.datatype.Work;

import org.openproj.domain.document.Document;

import com.projity.field.Select.InvalidChoiceException;

import com.projity.grouping.core.hierarchy.BelongsToHierarchy;
import com.projity.grouping.core.summaries.SummaryNames;
import com.projity.grouping.core.summaries.SummaryVisitor;
import com.projity.grouping.core.summaries.SummaryVisitorFactory;

import com.projity.options.CalendarOption;
import com.projity.options.EditOption;

import com.projity.pm.costing.CurrencyRateTable;
import org.openproj.domain.document.BelongsToDocument;
import com.projity.pm.time.Interval;

import com.projity.scripting.ScriptedFormula;

import com.projity.server.data.DataObject;

import com.projity.strings.Messages;

import com.projity.undo.ObjectChange;

import com.projity.util.ClassUtils;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.comparators.ComparableComparator;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

//import javax.swing.JTextField;

/**
 *
 */
public class Field
	implements SummaryNames,
		Cloneable,
		Comparable,
		Finder,
		Comparator
{
	/**
	 * Fields are constructed using chained properties
	 *
	 */
	public Field()
	{
	}

	/** Copy constructor for Field
	 * 
	 * @param _source used as the source of information to copy
	 */
	protected Field(
		final Field _source )
	{
		super();
	}
	
	public boolean build()
	{
		configurationId = id; // id can change if array field, so store off
							  // initial value

		boolean result = true;

		if (id == null)
		{
			log.error( "Field has no id!" );
			result = false;
		}

		if (property == null)
		{
			log.error( "Field has no property:" + id );
			result = false;
		}

		if (name == null) // if not explicitly set, use id as string id
		{
			name = Messages.getString( id );
		}

		setAccessorMethods();
		map = Map.class.isAssignableFrom( internalType ); // see if map

		if (isWork() == true)
		{
			setExternalType( Work.class );
		}
		else if (isDuration() == true)
		{
			setExternalType( Duration.class );
		}
		else if (isDate() == true)
		{
			setExternalType( Date.class );
		}
		else if ((isMoney() == true) && (isRate() == false))
		{
			setExternalType( Money.class );
		}

		displayType = (externalType == null)
			? internalType
			: externalType;

		if ((displayType != null) && displayType.isPrimitive())
		{
			displayType = ClassUtils.primitiveToObjectClass( displayType );
			externalType = displayType; // is this necessary?
		}

		if (finder != null)
		{
			finderMethod = ClassUtils.staticMethodFromFullName( finder, new Class[]
					{
						Object.class,
						Object.class
					} );

			if (finderMethod == null)
			{
				Field.log.error( "invalid finder method " + finder + " for field" + name );
			}
		}

		if (columnWidth == 0)
		{
			columnWidth = getDefaultColumnWidth();
		}

		if (svgColumnWidth == 0)
		{
			svgColumnWidth = getSvgDefaultColumnWidth();
		}

		return result;
	}

	public void clearFormula()
	{
		formula = null;
	}

	@Override
	public Field clone()
	{
		try
		{
			return (Field)super.clone();
		} 
		catch (CloneNotSupportedException e) 
		{
			return null;
		}
	}

	/**
	 * Compare two objects using this field. In this way i
	 */
	@Override
	public int compare( 
		final Object _arg0,
		final Object _arg1 )
	{
		return getComparator().compare( getValue( _arg0, null ), getValue( _arg1, null ) );
	}

	/**
	 * Compares two fields. Normally a simple String compareTo is used, but in
	 * the case of array fields, I compare their indexes - we want such fields
	 * to sort numerically and not alphabetically so that for example, Cost11
	 * appears after Cost2 and not before.
	 */
	@Override
	public int compareTo( 
		final Object _to )
	{
		if (_to == null)
		{
			throw new NullPointerException();
		}

		if (!(_to instanceof Field))
		{
			throw new ClassCastException();
		}

		Field toField = (Field)_to;

		if (configurationId == toField.configurationId)
		{ // if array field,
		  // then compare
		  // indexes

			return index - toField.index;
		}
		else
		{
			return getName().compareTo( toField.getName() );
		}
	}

	public int compareValues( 
		final Object _value1,
		final Object _value2 )
	{
		return getComparator().compare( _value1, _value2 );
	}

	public final String convertIdToString( 
		final Object _id )
	{
		if (select == null)
		{
			log.error( "calling convertIdToString on non select field" + getName() );

			return null;
		}

		return (String)select.getKey( _id, null );
	}

	public Object convertValueForExport( 
		Object _value )
	{
		if (_value instanceof Duration)
		{
			_value = Double.valueOf( ((Duration)_value).getAsDays() );
		}

		return _value;
	}

	public String convertValueToStringUsingOptions( 
		final Object _value,
		final Object _object )
	{
		final String result = (String)select.getKey( _value, _object );

		if (result != null)
		{
			return result;
		}
		else if (_value instanceof String == true)
		{
			return _value.toString();
		}

		if (dontLimitToChoices == false)
		{
			return NO_CHOICE;
		}

		return _value.toString();
	}

	/**
	 * Copies field data from one object to another. Does not copy read only
	 * fields
	 *
	 * @param to
	 * @param from
	 */
	public void copyData( 
		final Object _to,
		final Object _from )
	{
		if (isReadOnly( _to, null ))
		{
			return;
		}

		Object value = getValue( _from, null );
		setValue( _to, null, value );
	}

	/**
	 * Copies multiple fields from one object to another
	 *
	 * @param fieldArray
	 * @param to
	 * @param from
	 */
	public static void copyData( 
		final Collection _fieldArray,
		final Object _to,
		final Object _from )
	{
		Iterator i = _fieldArray.iterator();

		while (i.hasNext())
		{
			((Field)i.next()).copyData( _to, _from );
		}
	}

	/**
	 * Copies a set of fields, defined by the fieldArray to a map with their
	 * values
	 *
	 * @param toMap
	 * @param from
	 * @param fieldArray
	 */
	public static void copyData( 
		final Map _toMap,
		final Object _from,
		final Collection _fieldArray )
	{
		FieldContext context = null;
		Iterator i = _fieldArray.iterator();

		while (i.hasNext())
		{
			Field field = (Field)i.next();
			context = field.specialFieldContext;

			String value = field.getText( _from, context );
			_toMap.put( field.getId(), value );
		}
	}

	/**
	 * Copies data from a map which contains Field Ids (e.g. Field.work) as keys
	 * and values (either string or object) to the destination to. Read Only
	 * fields are ignored.
	 *
	 * @param to
	 * @param fromMap
	 * @throws FieldParseException
	 */
	public static void copyData( 
		final Object _to,
		final Map<? extends String,? extends Object> _fromMap )
		throws FieldParseException
	{
		Iterator<? extends String> itor = _fromMap.keySet().iterator();
		FieldContext context;

		while (itor.hasNext() == true)
		{
			final String fieldId = itor.next();
			final Field field = Configuration.getFieldFromId( fieldId );
			context = field.specialFieldContext;

			if (field.isReadOnly( _to, context ) == false)
			{
				Object data = _fromMap.get( fieldId );

				if (data instanceof String == true)
				{
					field.setText( _to, (String)data, context );
				}
				else
				{
					field.setValue( _to, data, context );
				}
			}
		}
	}

	public Field createIndexedField( 
		final int _index )
	{
		Field indexedField = clone();
		indexedField.setIndex( _index );

		String indexSuffix = "";

		if (indexedField.isZeroBasedIndex() == true)
		{
			if (_index > 0)
			{
				indexSuffix += _index;
			}
		}
		else
		{
			indexSuffix += (_index + 1);
		}

		indexedField.setId( getId().replaceFirst( "#", indexSuffix ) );
		indexedField.setName( getName().replaceFirst( "#", indexSuffix ) );

		return indexedField;
	}

	/**
	 * Make a new field with an integrated context that uses the given interval
	 * @param _field
	 * @param interval
	 * @return
	 */
	public static Field createIntervalField( 
		final Field _field,
		final Interval _interval )
	{
		FieldContext fieldContext = new FieldContext();
		fieldContext.setInterval( _interval );

		final Field newField = _field.clone();
		newField.specialFieldContext = new FieldContext();

		return newField;
	}

	public String dump()
	{
		return ToStringBuilder.reflectionToString( this );
	}

	private String errorMessage( 
		final Object _value,
		final Object _object )
	{
		String message;

		if (errorMessage != null)
		{
			message = errorMessage;
		}
		else if (isDuration() == true)
		{
			message = "Message.invalidDuration";
		}
		else if (isDate() == true)
		{
			message = "Message.invalidDate";
		}
		else if (isRate() == true)
		{
			message = "Message.invalidRate";
		}
		else if (isMoney() == true)
		{
			message = "Message.invalidCost";
		}
		else
		{
			message = "Message.invalidInput";
		}

		return Messages.getString( message );
	}

	public Object evaluateFormula( 
		final Object _object )
	{
		try
		{
			return formula.evaluate( _object );
		}
		catch (final InvalidFormulaException _e)
		{
			log.error( "Formula is invalid " + formula.getText() );

			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.projity.field.Finder#find(java.lang.Object)
	 */
	@Override
	public Object find( 
		final Object _key,
		final Collection _container )
	{
		if (finderMethod == null)
		{
			return findFirstInCollection( _key, _container );
		}

		try
		{
			return finderMethod.invoke( null, new Object[]
				{
					_key,
					_container
				} );
		}
		catch (final Exception _e)
		{
			return null;
		}
	}

	public Object[] findAllInCollection( 
		final Object _value,
		final Collection _collection )
	{
		ArrayList result = new ArrayList();
		Iterator i = _collection.iterator();
		Object current;

		while (i.hasNext())
		{
			current = i.next();

			if (0 == getComparator().compare( getValue( current, null ), _value ))
			{
				result.add( current );
			}
		}

		return result.toArray();
	}

	/**
	 *
	 * @param _value
	 * @param _collection
	 * @return
	 */
	public Object findFirstInCollection( 
		final Object _value,
		final Collection<? > _collection )
	{
		Iterator<? > itor = _collection.iterator();

		while (itor.hasNext() == true)
		{
			final Object current = itor.next();

			if (0 == getComparator().compare( getValue( current, null ), _value ))
			{
				return current;
			}
		}

		return null;
	}

	public void fireEvent( 
		final Object _object,
		final Object _source,
		FieldContext _context )
	{
		if (_context == null)
		{
			_context = specialFieldContext;
		}

		if ((_object instanceof BelongsToDocument == true)
		 && (_source != null))
		{ 
			// if no source then no update
			if (FieldContext.isNoUpdate( _context ) == false)
			{
				Document document = ((BelongsToDocument)_object).getDocument();
				document.fireUpdateEvent( _source, _object, this );

//				if (isDirtiesWholeDocument() == true)
//				{
//					document.setAllChildrenDirty( true );
//				}
			}
		}
	}

	public FieldAccessible getAccessControl()
	{
		return accessControl;
	}

	public final String getAction()
	{
		return action;
	}

	public String getAlias()
	{
		return alias;
	}

	public String getAlternateId()
	{
		return alternateId;
	}

	public final Class getClazz()
	{
		return clazz;
	}

	/**
	 * @return Returns the .
	 */
	public int getColumnWidth()
	{
		return columnWidth;
	}

	public int getColumnWidth( 
		final boolean _svg )
	{
		return (_svg)
		? getSvgColumnWidth()
		: getColumnWidth();
	}

	/**
	 * Given a collection, if each elements shares the same value for this
	 * field, the value is returned, Otherwise null is returned.
	 *
	 * @param collection
	 * @param useMultipleValue
	 *            If true, the default value will be used if no elements or
	 *            values differ
	 * @return
	 */
	public Object getCommonValue( 
		final Collection _collection,
		final boolean _useMultipleValue,
		final boolean _text )
	{
		if (_collection == null)
		{
			return null;
		}

		Iterator i = _collection.iterator();
		Object value = null;
		Object current;
		Object currentValue;
		Comparator comparatorToUse = (_text
			? ComparableComparator.INSTANCE
			: getComparator());

		while (i.hasNext())
		{
			current = i.next();

			if (_text)
			{
				currentValue = getText( current, null );
			}
			else
			{
				currentValue = getValue( current, null );
			}

			if (value == null)
			{
				value = currentValue;
			}
			else if (0 != comparatorToUse.compare( currentValue, value ))
			{
				value = null;

				break;
			}
		}

		if ((value == null) && _useMultipleValue)
		{
			value = getMultipleValueForType();
		}

		return value;
	}

	/**
	 * Given a collection, return the text for a field if each elements shares
	 * the same value, otherwise return "--"
	 *
	 * @param collection
	 * @return
	 */
	public String getCommonValueString( 
		final Collection _collection )
	{
		if ((_collection == null) || (_collection.size() == 0))
		{
			return null;
		}

		Object common = getCommonValue( _collection, false, true );

		if (common == null)
		{
			return MULTIPLE_VALUES;
		}
		else
		{
			return common.toString();
		}
	}

	public Comparator getComparator()
	{
		if (comparator == null)
		{
			return ClassUtils.getComparator( getDisplayType() );
		}

		return comparator;
	}

	public Comparator getComparator( 
		final boolean _ascending )
	{
		if (_ascending == true)
		{
			return this;
		}
		else
		{
			return new Comparator()
				{
					@Override
					public int compare( 
						final Object _o1,
						final Object _o2 )
					{
						return Field.this.compare( _o2, _o1 );
					}
				};
		}
	}

	private int getDefaultColumnWidth()
	{
		if (isDuration())
		{
			return 75;
		}
		else if (isDate())
		{
			return 115;
		}
		else if (isMoney())
		{
			return 100;
		}
		else if (isRate())
		{
			return 75;
		}
		else if (getDisplayType() == Boolean.class)
		{
			return 40;
		}
		else
		{
			return 150;
		}
	}

	public String getDefaultName()
	{
		return name;
	}

	/**
	 * @return Returns the defaultValue.
	 */
	public Object getDefaultValue()
	{
		return defaultValue;
	}

	/**
	 * @return Returns the type to use for this field in spreadsheets and
	 *         dialogs.
	 *
	 */
	public Class getDisplayType()
	{
		return displayType;
	}

	public final String getErrorMessage()
	{
		return errorMessage;
	}

	public final String getExtraCategory()
	{
		return extraCategory;
	}

	public OptionsFilter getFilter()
	{
		return filter;
	}

	public Format getFormat()
	{
		return getFormat( null );
	}

	public Format getFormat( 
		final Object _object )
	{
		if (isWork())
		{
			return DurationFormat.getWorkInstance();
		}
		else if (isRate())
		{
			return RateFormat.getInstance( _object, isMoney(), isPercent(), true );
		}
		else if (isMoney())
		{
			return Money.getMoneyFormatInstance();
		}
		else if (isDuration())
		{
			return DurationFormat.getInstance();
		}
		else if (isPercent())
		{
			return PercentFormat.getInstance();
		}
		else if (isDate())
		{
			return EditOption.getInstance().getDateFormat();
		}
		else if ((displayType == Double.class) || (displayType == Float.class) || (displayType == Integer.class))
		{
			NumberFormat nf = NumberFormat.getInstance();
			nf.setGroupingUsed( true );
			nf.setMaximumFractionDigits( 2 );

			return nf;
		}

		return null;
	}

	public int getGroupSummary()
	{
		return groupSummary;
	}

	public String getHelp()
	{
		return help;
	}

//	public int getHorizontalAlignment()
//	{
//		if (isImage() || isBoolean())
//		{
//			return JTextField.CENTER;
//		}
//		else if (isWork() || isRate() || isMoney() || isDuration() || isDate() || isPercent() || isNumber())
//		{
//			return JTextField.RIGHT;
//		}
//		else
//		{
//			return JTextField.LEFT;
//		}
//	}

	/**
	 * @return Returns the id.
	 */
	public String getId()
	{
		return id;
	}

	public String getIdWithoutPrefix()
	{
		return Field.shortName( id );
	}

	/**
	 * @return Returns the index.
	 */
	public int getIndex()
	{
		return index;
	}

	/**
	 * @return Returns the indexes.
	 */
	public int getIndexes()
	{
		return indexes;
	}

	public String getLabel()
	{
		if (specialFieldContext == null)
		{
			return getName();
		}

		long start = specialFieldContext.getInterval().getStart();

		return f.format( new Date(start) );
	}

	public Field getListElementsField()
	{
		return listElementsField;
	}

	public String getLookupTypes()
	{
		return lookupTypes;
	}

	private String getMapEntryId()
	{
		if (alternateId != null)
		{
			return alternateId;
		}

		return id;
	}

	public String getMetadataString()
	{
		String result = getName() + "\t" + getIdWithoutPrefix() + "\t" + internalTypeName() + "\t" + typeName() + "\t" +
			isReadOnly() + "\t" + ((getSummaryType() == null)
			? "-"
			: getSummaryType()) + "\t";

		if (hasDynamicSelect())
		{
			result += "Choices are dynamic";
		}
		else if (select != null)
		{
			result += select.documentOptions();
		}

		return result;
	}

	public static String getMetadataStringHeader()
	{
		return "Name" + "\t" + "Id (for API)" + "\t" + "API type" + "\t" + "POD type" + "\t" + "Read Only" + "\t" + "Rollup" +
		"\t" + "Notes";
	}

	public Object getMultipleValueForType()
	{
		if (isDuration())
		{
			return Duration.ZERO;
		}
		else if (isDate())
		{
			return null;
		}
		else if (isPercent())
		{
			return ClassUtils.PERCENT_MULTIPLE_VALUES;
		}
		else
		{
			return ClassUtils.getMultipleValueForType( internalType );
		}
	}

	/**
	 * @return Returns the name.
	 */
	public String getName()
	{
		if (getAlias() != null)
		{
			return getAlias();
		}

		return name;
	}

	/**
	 * For use in populating a list box
	 *
	 * @param object
	 *            TODO
	 * @return
	 */
	public Object[] getOptions( 
		final Object _object )
	{
		if (select == null)
		{
			return null;
		}

		Object[] options = select.getKeyArray( _object );

		if (filter == null)
		{
			return options;
		}
		else
		{
			return filter.getOptions( options, select.getValueList( _object ), _object );
		}
	}

	public boolean getPassword()
	{
		return password;
	}

	/**
	 * @return Returns the property.
	 */
	public String getProperty()
	{
		return property;
	}

	private Object getPropertyValue( 
		final Object _object,
		FieldContext _context )
	{
		Object result = null;

		if (_context == null)
		{
			_context = specialFieldContext;
		}

		if (isFormula() == true)
		{
			// for now not time distrib
			result = this.evaluateFormula( _object );
		}
		else
		{
			if (myGetMethod == null)
			{
				return null;
			}

			try
			{
				if (getHasNoContext == true)
				{
					result = myGetMethod.invoke( _object,
							((isIndexed() == true)
							? new Object[]
							{
								Integer.valueOf( index )
							}
							: new Object[] {  }) );
				}
				else
				{
					result = myGetMethod.invoke( _object,
							((isIndexed() == true)
							? new Object[]
							{
								Integer.valueOf( index ),
								_context
							}
							: new Object[]
							{
								_context
							}) );
				}
			}
			catch (final IllegalArgumentException _e)
			{
				System.out.println( "Error invoking get method for field '" + this + "' - " + myGetMethod );

				//				e.printStackTrace();
			}
			catch (final IllegalAccessException _e)
			{
				System.out.println( "Error invoking get method for field '" + this + "' - " + myGetMethod );

				//				e.printStackTrace();
			}
			catch (final InvocationTargetException _e)
			{
				System.out.println( "Error invoking get method for field '" + this + "' - " + myGetMethod );

				if (_e.getTargetException() != null)
				{
					System.out.println( "...target exception '" + _e.getTargetException() + "'" );
				}
			}
		}

		return result;
	}

	public Range getRange()
	{
		return range;
	}

	public Long getReferencedId( 
		final Object _obj )
	{
		Long result = null;

		if (referencedIdProperty != null)
		{
			try
			{
				result = (Long)PropertyUtils.getProperty( _obj, referencedIdProperty );

				//				System.out.println("____ref id = " + result);
			}
			catch (final IllegalAccessException _e)
			{
				;
			}
			catch (final InvocationTargetException _e)
			{
				;
			}
			catch (final NoSuchMethodException _e)
			{
				;
			}
		}

		return result;
	}

	/**
	 * @return Returns the referencedIdProperty.
	 */
	public String getReferencedIdProperty()
	{
		return referencedIdProperty;
	}

	public Object getReferencedObject( 
		final Object _obj )
	{
		if (referencedObjectProperty == null)
		{
			return null;
		}

		try
		{
			return PropertyUtils.getProperty( _obj, referencedObjectProperty );
		}
		catch (final IllegalAccessException _e)
		{
			;
		}
		catch (final InvocationTargetException _e)
		{
			;
		}
		catch (final NoSuchMethodException _e)
		{
			;
		}

		return null;
	}

	/**
	 * @return Returns the referencedObjectProperty.
	 */
	public String getReferencedObjectProperty()
	{
		return referencedObjectProperty;
	}

	public double getScaleFactor()
	{
		if (isWork() || isDuration())
		{
			return CalendarOption.getInstance().getMillisPerDay();
		}
		else
		{
			return 1.0;
		}
	}

	private Format getSecondaryFormat( 
		final Object _object )
	{
		if (isMoney())
		{
			return NumberFormat.getInstance();
		}

		return null;
	}

	/**
	 * @return Returns the select.
	 */
	public Select getSelect()
	{
		return select;
	}

	public FieldContext getSpecialFieldContext()
	{
		return specialFieldContext;
	}

	public final int getSummary()
	{
		return summary;
	}

	public int getSummaryForGroup()
	{
		if (groupSummary != NONE)
		{
			return groupSummary;
		}

		//		else
		return summary;
	}

	public String getSummaryType()
	{
		if (isStartValue())
		{
			return "min";
		}

		if (isEndValue())
		{
			return "max";
		}

		int summary = getGroupSummary();

		if (summary == SummaryNames.NONE)
		{
			summary = getSummary();
		}

		//System.out.println("Field "+ this + " group " + getGroupSummary() +  " sum " + getSummary());
		switch (summary)
		{
		case SummaryNames.SUM:
			return "sum";

		case SummaryNames.COUNT_ALL:
		case SummaryNames.COUNT_FIRST_SUBLEVEL:
		case SummaryNames.COUNT_NONSUMMARIES:
			return "count";

		case SummaryNames.AVERAGE:
		case SummaryNames.AVERAGE_FIRST_SUBLEVEL:
			return "average";
		}

		return null;
	}

	// added for groups
	public SummaryVisitor getSummaryVisitor( 
		final int _summary,
		final boolean _forceDeep )
	{
		return SummaryVisitorFactory.getInstance( _summary, getDisplayType(), _forceDeep ); // TODO
																							// use
																							// internalType
																							// instead?
	}

	public SummaryVisitor getSummaryVisitor( 
		final boolean _forceDeep )
	{
		return getSummaryVisitor( summary, _forceDeep );
	}

	public int getSvgColumnWidth()
	{
		return svgColumnWidth;
	}

	private int getSvgDefaultColumnWidth()
	{
		if (isDate() == true)
		{
			return 130;
		}
		else
		{
			return getDefaultColumnWidth();
		}
	}

	public final String getText( 
		final Object _object,
		FieldContext _context )
	{
		if (isApplicable( _object ) == false)
		{
			return NOT_APPLICABLE;
		}

		if (password == true) // don't show passwords
		{
			return PASSWORD_MASK;
		}

		if (_context == null)
		{
			_context = specialFieldContext;
		}

		Object value = null;

		try
		{
			value = getValue( _object, _context );

			if (hasOptions() == true)
			{
				return convertValueToStringUsingOptions( value, _object );
			}
		}
		catch (final IllegalArgumentException _e1)
		{
			_e1.printStackTrace();
		}

		return toText( value, _object );
	}

	public int getTextWidth( 
		final Object _object,
		final FieldContext _context )
	{ // can
	  // override
	  // in
	  // weird
	  // circumstances

		return textWidth;
	}

	public int getTextWidth()
	{
		return textWidth;
	}

	public final String getUrl()
	{
		return url;
	}

	public Object getValue( 
		final Object _object )
	{
		return getValue( _object, null );
	}

	public Object getValue( 
		final Object _object,
		FieldContext _context )
	{
		if (isApplicable( _object ) == false)
		{
			return null;
		}

		if (_context == null)
		{
			_context = specialFieldContext;
		}

		if (_object instanceof DelegatesFields == true)
		{
			final DelegatesFields delegator = (DelegatesFields)_object;

			if (delegator.delegates( this ) == true)
			{
				return delegator.getDelegatedFieldValue( this );
			}
		}

		Object result = getPropertyValue( _object, _context );

		if (isMap() == true)
		{
			if (result == null) // if no map
			{
				return null;
			}

			result = ((Map)result).get( getMapEntryId() );

			if (result == null)
			{
				//Need to return null instead of false to detect a value is missing in the map. 
				//...It's used by setInternalValue: if (value != null && value.equals(getValue(object, context)))
				return null;
			}
		}

		if (hasExternalType() == true)
		{
			// need to convert once more
			if (FieldContext.isScripting( _context ) == true)
			{
				//Doesn't work
				//				if (isDuration()) // for durations get rid of unit when scripting
				//					result = Long.valueOf(Duration.millis(((Long)result).longValue()));
			}
			else
			{
				try
				{
					// convert to external type
					//System.out.println("converting to external type.  externalType: "+externalType);
					result = FieldConverter.convert( result, externalType, _context ); // convert a long to date for example
				}
				catch (final FieldParseException _e1)
				{
					_e1.printStackTrace();
					result = null;
				}
			}
		}

		if (hideZeroValues == true && isZero( result ) == true)
		{
			return null;
		}
		else if ((_context != null) && _context.isHideNullValues() == true && ClassUtils.isNull( result ) == true)
		{
			return null;
		}

		if (isMoney() == true && isConvertCurrency() == true)
		{
			// convert currency
			//System.out.println("Currency is being converted");
			result = CurrencyRateTable.convertToDisplay( (Number)result );
		}

//		if (result != null && url != null) {
//			result = "<html><a href=\"" + result + "\">" + url + "</a></html>";
//		}
		//System.out.println("isMoney(): "+isMoney()+"\nisConvertCurrency(): "+isConvertCurrency());
		if (isMoney() == true && (result instanceof Money == true))
		{
			String moneyResult = ((Money)result).getFormattedValue();

			//System.out.println("moneyResult: "+ moneyResult);
			//return moneyResult;
		}

		//System.out.println("Value returned from Field is: "+result);
		return result;
	}

	public Object getValueFromProperty( 
		final Object _obj )
	{
		if (property == null)
		{
			return null;
		}

		try
		{
			return PropertyUtils.getProperty( _obj, property );
		}
		catch (final IllegalAccessException _e)
		{
			;
		}
		catch (final InvocationTargetException _e)
		{
			;
		}
		catch (final NoSuchMethodException _e)
		{
			;
		}

		return null;
	}

	public boolean hasDynamicSelect()
	{
		return (select != null) && !select.isStatic();
	}

	private boolean hasExternalType()
	{
		return ((externalType != null) && (externalType != internalType));
	}

	public boolean hasFilter()
	{
		return (filter != null);
	}

	public boolean hasOptions()
	{
		return (select != null);
	}

	public boolean hasSummary()
	{
		return summary != NONE;
	}

	public String internalTypeName()
	{
		String t = internalType.toString();
		int i = t.lastIndexOf( "." );

		if (i != -1)
		{
			return t.substring( i + 1 );
		}

		return t;
	}

	public void invokeAction( 
		final Object _obj )
	{
		if ((action == null) || (_obj == null))
		{
			return;
		}

		Object value = getValue( _obj, null );

		if (value instanceof Hyperlink == true)
		{
			((Hyperlink)value).invoke();
		}
	}

	private final Object invokeContextMethod( 
		final Method _method,
		final Object _object,
		FieldContext _context,
		final boolean _noContext,
		final boolean _dumpException )
	{
		if (_context == null)
		{
			_context = specialFieldContext;
		}

		if (_method == null)
		{
			return null;
		}

		try
		{
			if (_noContext == true)
			{
				if (isIndexed() == true)
				{
					return _method.invoke( _object, new Object[]
						{
							new Integer(getIndex()),
							null
						} );
				}
				else
				{
					return _method.invoke( _object, (Object[])null );
				}
			}
			else
			{
				if (isIndexed() == true)
				{
					return _method.invoke( _object, new Object[]
						{
							new Integer(getIndex()),
							_context
						} );
				}
				else
				{
					return _method.invoke( _object, new Object[]
						{
							_context
						} );
				}
			}
		}
		catch (final Exception _e)
		{
			if (_dumpException)
			{
				_e.printStackTrace();
			}
		}

		return null;
	}

	public boolean isAllowNull()
	{
		return allowNull;
	}

	/**
	 * To see if the field applies to the object, see if the field's type is a
	 * super type or same as object
	 *
	 * @param _object
	 * @return
	 */
	public boolean isApplicable( 
		final Object _object )
	{
		if (_object == null)
		{
			return false;
		}

		if (_object instanceof DelegatesFields == true)
		{
			// for objects that delegate, they should be able to display anything
			if (((DelegatesFields)_object).delegates( this ) == true)
			{
				return true;
			}
		}

		return isApplicable( _object.getClass() );
	}

	/**
	 *
	 * @param _type
	 * @return
	 */
	public boolean isApplicable( 
		final Class<?> _type )
	{
		return clazz.isAssignableFrom( _type );
	}

	/**
	 * Is field applicable to any type in types array
	 *
	 * @param types
	 * @return
	 */
	public boolean isApplicable( 
		final Class[] _types )
	{
		for (int i = 0; i < _types.length; i++)
		{
			if (isApplicable( _types[ i ] ) == true)
			{
				return true;
			}
		}

		return false;
	}

	public boolean isAuthorized( 
		final int _role )
	{
		return (accessControl == null) || accessControl.isAuthorized( _role );
	}

	public final boolean isBoolean()
	{
		return getDisplayType() == Boolean.class;
	}

	/**
	 * @return Returns the callValidateOnClear.
	 */
	public final boolean isCallValidateOnClear()
	{
		return callValidateOnClear;
	}

	/**
	 * @return Returns the cantReset.
	 */
	public final boolean isCantReset()
	{
		return cantReset;
	}

	public final boolean isComparable()
	{
		return !isImage();
	}

	public final boolean isConvertCurrency()
	{
		return convertCurrency;
	}

	public final boolean isCustom()
	{
		return custom;
	}

	/**
	 * @return Returns the date.
	 */
	public final boolean isDate()
	{
		return date;
	}

	public final boolean isDateOnly()
	{
		return dateOnly;
	}

	public final boolean isDirtiesWholeDocument()
	{
		return dirtiesWholeDocument;
	}

	/**
	 * @return Returns the dontLimitToChoices.
	 */
	public final boolean isDontLimitToChoices()
	{
		return dontLimitToChoices;
	}

	/**
	 * @return Returns the duration.
	 */
	public final boolean isDuration()
	{
		return duration;
	}

	public final boolean isDurationOrWork()
	{
		return (isWork() == true) || (isDuration() == true);
	}

	public final boolean isDynamicOptions()
	{
		return dynamicOptions;
	}

	public final boolean isEndValue()
	{
		return endValue;
	}

	public final boolean isExtra()
	{
		return extraCategory != null;
	}

	/**
	 * @return Returns the formula.
	 */
	public final boolean isFormula()
	{
		return formula != null;
	}

	public final boolean isGraphical()
	{
		return graphical;
	}

	public final boolean isHasFilter()
	{
		return hasFilter();
	}

	public final boolean isHasOptions()
	{
		return hasOptions();
	}

	public final boolean isHasToolTip()
	{
		return hasToolTip;
	}

	public final boolean isHidden( 
		final Object _object,
		FieldContext _context )
	{
		if (_context == null)
		{
			_context = specialFieldContext;
		}

		if (methodHide == null)
		{
			return false;
		}

		Boolean value = (Boolean)invokeContextMethod( methodHide, _object, _context, hideHasNoContext, false );

		if (value != null)
		{
			return value.booleanValue();
		}

		return true;

//		// TODO maybe test if objet itself is hidden
//		return false;
	}

	/**
	 * @return Returns the hideZeroValues.
	 */
	public final boolean isHideZeroValues()
	{
		return hideZeroValues;
	}

	public final boolean isHyperlink()
	{
		return getUrl() != null;
	}

	public final boolean isImage()
	{
		return image;
	}

	public final boolean isIndexed()
	{
		return indexes != 0;
	}

	public final boolean isLink()
	{
		return (referencedObjectProperty != null) || (referencedIdProperty != null);
	}

	public final boolean isMap()
	{
		return map;
	}

	/**
	 * @return Returns the memo.
	 */
	public final boolean isMemo()
	{
		return memo;
	}

	/**
	 * @return Returns the money.
	 */
	public final boolean isMoney()
	{
		return money;
	}

	/**
	 * @return Returns the nameField.
	 */
	public final boolean isNameField()
	{
		return nameField;
	}

	public final boolean isNotChoosable()
	{
		return notChoosable;
	}

	public final boolean isNumber()
	{
		return (displayType == Double.class) || (displayType == Float.class) || (displayType == Integer.class) ||
		(displayType == Long.class);
	}

	public final boolean isOnlyChangeOnPublish()
	{
		return onlyChangeOnPublish;
	}

	/**
	 * @return Returns the password.
	 */
	public final boolean isPassword()
	{
		return password;
	}

	/**
	 * @return Returns the percent.
	 */
	public final boolean isPercent()
	{
		return percent;
	}

	/**
	 * @return Returns the value.
	 */
	public final boolean isRate()
	{
		return rate;
	}

	/**
	 * @return Returns the readOnly. It's a static vlue
	 */
	public final boolean isReadOnly()
	{
		return readOnly;
	}

	/**
	 * See if the field is read only
	 *
	 * @param object
	 * @param context
	 * @return
	 */
	public final boolean isReadOnly( 
		final Object _object,
		FieldContext _context )
	{
		if (_context == null)
		{
			_context = specialFieldContext;
		}

		//if (context!=null&&context.isSoap()) return false; //is it the right behavior
		if (readOnly == true)
		{
			return true;
		}

		if (isApplicable( _object ) == false)
		{ // if the object doesn't treat this
		  // field

			return true;
		}

		if (isFormula() == true)
		{
			return true;
		}

		// if (isHidden(object,context))
		// return true;
		if (_object instanceof BelongsToHierarchy)
		{ // for dialogs
		  // for parents with This summary type
		  // System.out.println("summary is " + summary + " THIS " + THIS + " NONE
		  // " + NONE + " parent " + ((BelongsToHierarchy)object).isParent());

			if (summary != NONE)
			{
				if (((BelongsToHierarchy)_object).isParent() == true)
				{
					return true;
				}
			}
		}

		if (ClassUtils.isObjectReadOnly( _object ))
		{
			return true;
		}

		if (ClassUtils.isObjectFieldReadOnly( _object, this ))
		{
			return true;
		}

		if ((_object instanceof DelegatesFields) 
		 && ((DelegatesFields)_object).delegates( this ))
		{
			return true;
		}

		Boolean value = (Boolean)invokeContextMethod( methodReadOnly, _object, _context, readOnlyHasNoContext, true );

		if (value != null)
		{
			return value.booleanValue();
		}

		return false;
	}

	public final boolean isResourceNames()
	{
		return resourceNames;
	}

	/**
	 * @return Returns the scalar.
	 */
	public final boolean isScalar()
	{
		return scalar;
	}

	public final boolean isServer()
	{
		return server;
	}

	public final boolean isStandardType()
	{
		boolean nonStandard = isDuration() || isRate() || hasOptions() || isMoney();

		return !nonStandard;
	}

	public final boolean isStartValue()
	{
		return startValue;
	}

	public final boolean isString()
	{
		return (externalType != null) && externalType.getName().equals( "java.lang.String" );
	}

	public final boolean isTransientValue()
	{
		return transientValue;
	}

	public final boolean isValidChoice( 
		final String _textValue )
	{
		try
		{
			preprocessText( null, _textValue, null );
		}
		catch (final FieldParseException _e)
		{
			return false;
		}

		return true;
	}

	public final boolean isValidOnObjectCreate()
	{
		return validOnObjectCreate;
	}

	/**
	 * @return Returns the vector.
	 */
	public final boolean isVector()
	{
		return vector;
	}

	/**
	 * @return Returns the work.
	 */
	public final boolean isWork()
	{
		return work;
	}

	public final boolean isZero( 
		final Object _value )
	{
		if (_value instanceof Number)
		{
			return (((Number)_value).doubleValue() == 0.0);
		}
		else if (_value instanceof String)
		{
			return (((String)_value).length() == 0);
		}

//		else if (value instanceof Date)
//			return ((Date)value).getTime() == 0L;
//		else if (value instanceof Duration)
//			return Duration.millis(((Duration)value).getEncodedMillis()) == 0L;
		return false;
	}

	/**
	 * @return Returns the zeroBasedIndex.
	 */
	public final boolean isZeroBasedIndex()
	{
		return zeroBasedIndex;
	}

	public static String longCustomName( 
		final String _shortFieldName )
	{
		return "CustomField." + _shortFieldName;
	}

	public static String longName( 
		final String _shortFieldName )
	{
		//		int pos = shortFieldName.indexOf(".");
		//		if (pos == -1)
		//			return "Field."+shortFieldName;
		//		else
		//			return shortFieldName;
		return "Field." + _shortFieldName;
	}

	public Object mapStringToValue( 
		final String _textValue )
	{
		if (select == null)
		{
			return null;
		}

		try
		{
			return select.getValue( _textValue );
		}
		catch (final InvalidChoiceException _e)
		{
			return null;
		}
	}

	public String mapValueToString( 
		final Object _value )
	{
		if (select == null)
		{
			return null;
		}

		return (String)select.getKey( _value, null );
	}

	public Object preprocessText( 
		final Object _object,
		String _textValue,
		FieldContext _context )
		throws FieldParseException
	{
		if (_context == null)
		{
			_context = specialFieldContext;
		}

		Object value;

		if (select != null)
		{
			if (_textValue == null)
			{
				return null;
			}

			if ((_textValue.trim().length() == 0) && select.isAllowNull()) // special
																		   // case
			{
				_textValue = Select.EMPTY;
			}

			try
			{
				select.setObject( _object );
				value = select.getValue( _textValue );
			}
			catch (final InvalidChoiceException _e)
			{
				throw new FieldParseException(Messages.getString( "Message.invalidChoice" ) + ": " + _textValue);
			}

			if ((value == null) && (!select.isAllowNull() || (_textValue != Select.EMPTY)))
			{
				throw new FieldParseException(Messages.getString( "Message.invalidChoice" ) + ": " + _textValue);
			}
		}
		else if (this.isBoolean())
		{
			value = Boolean.valueOf( _textValue );
		}
		else
		{
			value = _textValue;
		}

		return value;
	}

	public void setAccessControl( 
		final FieldAccessible _accessControl )
	{
		this.accessControl = _accessControl;
	}

	private final void setAccessorMethods()
	{
		if ((clazz != null) && (property != null))
		{
			StringBuffer javaName = new StringBuffer(property);
			javaName.setCharAt( 0, Character.toUpperCase( javaName.charAt( 0 ) ) );

			// First look for a getter that has a context (indexed or not)
			myGetMethod = MethodUtils.getAccessibleMethod( clazz, "get" + javaName,
					(isIndexed()
					? getterIndexedContextParams
					: getterContextParams) );

			if (myGetMethod == null) // try is instead of get
			{
				myGetMethod = MethodUtils.getAccessibleMethod( clazz, "is" + javaName,
						(isIndexed()
						? getterIndexedContextParams
						: getterContextParams) );
			}

			// If not found, then use standard getter (indexed or not)
			if (myGetMethod == null)
			{
				getHasNoContext = true;
				myGetMethod = MethodUtils.getAccessibleMethod( clazz, "get" + javaName,
						(isIndexed()
						? getterIndexedParams
						: getterParams) );

				if (myGetMethod == null) // try is instead of get
				{
					myGetMethod = MethodUtils.getAccessibleMethod( clazz, "is" + javaName,
							(isIndexed()
							? getterIndexedParams
							: getterParams) );
				}
			}

			if (myGetMethod != null)
			{
				internalType = myGetMethod.getReturnType();
			}
			else
			{
				log.error( "Not getter found for field " + getId() );
			}

			// First look for a setter that has a context (indexed or not)
			methodSet = MethodUtils.getAccessibleMethod( clazz, "set" + javaName,
					(isIndexed()
					? new Class[]
					{
						int.class,
						internalType,
						FieldContext.class
					}
					: new Class[]
					{
						internalType,
						FieldContext.class
					}) );

			// If not found, then use standard setter (indexed or not)
			if (methodSet == null)
			{
				setHasNoContext = true;
				methodSet = MethodUtils.getAccessibleMethod( clazz, "set" + javaName,
						(isIndexed()
						? new Class[]
						{
							int.class,
							internalType
						}
						: new Class[]
						{
							internalType
						}) );
			}

			if ((methodSet == null) && !readOnly)
			{
				log.warn( "No setter found for non-read-only field: " + getId() );
			}

			methodReset = MethodUtils.getAccessibleMethod( clazz, "fieldReset" + javaName, getterContextParams );

			if (resetHasNoContext = (methodReset == null))
			{
				methodReset = MethodUtils.getAccessibleMethod( clazz, "fieldReset" + javaName, getterParams );
			}

			methodReadOnly = MethodUtils.getAccessibleMethod( clazz, "isReadOnly" + javaName, getterContextParams );

			if (readOnlyHasNoContext = (methodReadOnly == null))
			{
				methodReadOnly = MethodUtils.getAccessibleMethod( clazz, "isReadOnly" + javaName, getterParams );
			}

			//lc
			//			methodObjectReadOnly = MethodUtils.getAccessibleMethod(clazz, "isReadOnly", getterParams);
			methodHide = MethodUtils.getAccessibleMethod( clazz, "fieldHide" + javaName,
					(isIndexed()
					? getterIndexedContextParams
					: getterContextParams) );

			if (hideHasNoContext = (methodHide == null))
			{
				methodHide = MethodUtils.getAccessibleMethod( clazz, "fieldHide" + javaName,
						(isIndexed()
						? getterIndexedParams
						: getterParams) );
			}

			methodOptions = MethodUtils.getAccessibleMethod( clazz, "fieldOptions" + javaName, getterContextParams );

			if (optionsHasNoContext = (methodOptions == null))
			{
				methodOptions = MethodUtils.getAccessibleMethod( clazz, "fieldOptions" + javaName, getterParams );
			}
		}
	}

	public final void setAction( 
		final String _action )
	{
		this.action = _action;
	}

	public void setAlias( 
		final String _alias )
	{
		this.alias = _alias;
	}

	public void setAllowNull( 
		final boolean _allowNull )
	{
		this.allowNull = _allowNull;
	}

	public void setAlternateId( 
		final String _alternateId )
	{
		this.alternateId = _alternateId;
	}

	/**
	 * Set the array size of the custom field this applies to
	 *
	 * @param boundsField
	 */
	public void setBoundsField( 
		final String _boundsField )
	{
		if (indexes > 0) //Pb with IBM JDK, some boundsField with indexes=0 are reseting some settings
		{
			ClassUtils.setStaticField( _boundsField, indexes );
		}
	}

	/**
	 * @param callValidateOnClear
	 *            The callValidateOnClear to set.
	 */
	public void setCallValidateOnClear( 
		final boolean _callValidateOnClear )
	{
		this.callValidateOnClear = _callValidateOnClear;
	}

	/**
	 * @param cantReset
	 *            The cantReset to set.
	 */
	public void setCantReset( 
		final boolean _cantReset )
	{
		this.cantReset = _cantReset;
	}

	public final void setClass( 
		final Class<?> _clazz )
	{
		this.clazz = _clazz;
	}

	/**
	 * @param columnWidth
	 *            The columnWidth to set.
	 */
	public void setColumnWidth( 
		final int _columnWidth )
	{
		this.columnWidth = _columnWidth;
	}

	public final void setComparator( 
		final Comparator _comparator )
	{
		this.comparator = _comparator;
	}

	public void setConvertCurrency( 
		final boolean _convertCurrency )
	{
		this.convertCurrency = _convertCurrency;
	}

	public void setCustom( 
		final boolean _custom )
	{
		this.custom = _custom;
	}

	/**
	 * @param date
	 *            The date to set.
	 */
	public void setDate( 
		final boolean _date )
	{
		this.date = _date;
	}

	public void setDateOnly( 
		final boolean _dateOnly )
	{
		this.dateOnly = _dateOnly;
	}

	/**
	 * @param defaultValue
	 *            The defaultValue to set.
	 */
	public void setDefaultValue( 
		final Object _defaultValue )
	{
		this.defaultValue = _defaultValue;
	}

	public final void setDirtiesWholeDocument( 
		final boolean _dirtiesWholeDocument )
	{
		this.dirtiesWholeDocument = _dirtiesWholeDocument;
	}

	/**
	 * @param dontLimitToChoices
	 *            The dontLimitToChoices to set.
	 */
	public void setDontLimitToChoices( 
		final boolean _dontLimitToChoices )
	{
		this.dontLimitToChoices = _dontLimitToChoices;
	}

	/**
	 * @param duration
	 *            The duration to set.
	 */
	public void setDuration( 
		final boolean _duration )
	{
		this.duration = _duration;
	}

	public final void setDynamicOptions( 
		final boolean _dynamicOptions )
	{
		this.dynamicOptions = _dynamicOptions;
	}

	public void setEndValue( 
		final boolean _endValue )
	{
		this.endValue = _endValue;
	}

	public final void setEnglishName( 
		final String _englishName )
	{
		this.englishName = _englishName;
	}

	public final void setErrorMessage( 
		final String _errorMessage )
	{
		this.errorMessage = _errorMessage;
	}

	public final void setExternalType( 
		final Class _externalType )
	{
		this.externalType = _externalType;
	}

	public final void setExtraCategory( 
		final String _extraCategory )
	{
		this.extraCategory = _extraCategory;
	}

	public void setFilter( 
		final OptionsFilter _filter )
	{
		this.filter = _filter;
	}

	@Override
	public void setFinder( 
		final String _finder )
	{
		this.finder = _finder;
	}

	/**
	 * @param formula
	 *            The formula to set.
	 */
	public void setFormula( 
		final String _formulaName,
		final String _variableName,
		final String _formulaText )
	{
		//		formula = FormulaFactory.addScripted("Field", formulaName, variableName, formulaText);
		throw new RuntimeException("setFormula"); //TODO if used, need to handle addNormal too
	}

	public void setGraphical( 
		final boolean _graphical )
	{
		this.graphical = _graphical;
	}

	public final void setGroupSum( 
		final String _summaryType )
	{
		this.groupSummary = SummaryVisitorFactory.getSummaryId( _summaryType );

		//		if (groupSummary == NONE)
		//			log.warn("unknown summary type: " + summaryType + " for field " + getName());
	}

	public final void setHasToolTip( 
		final boolean _hasToolTip )
	{
		this.hasToolTip = _hasToolTip;
	}

	public void setHelp( 
		final String _help )
	{
		this.help = _help;
	}

	/**
	 * @param hideZeroValues
	 *            The hideZeroValues to set.
	 */
	public void setHideZeroValues( 
		final boolean _hideZeroValues )
	{
		this.hideZeroValues = _hideZeroValues;
	}

	/**
	 * @param id
	 *            The id to set.
	 */
	public void setId( 
		final String _id )
	{
		this.id = _id;
	}

	public final void setImage( 
		final boolean _image )
	{
		this.image = _image;
	}

	/**
	 * @param index
	 *            The index to set.
	 */
	public void setIndex( 
		final int _index )
	{
		this.index = _index;
	}

	/**
	 * @param indexes
	 *            The indexes to set.
	 */
	public void setIndexes( 
		final int _indexes )
	{
		this.indexes = _indexes;
	}

	public boolean setInternalValue( 
		final Object _object,
		Object _value,
		FieldContext _context,
		final FieldSetOptions _options )
		throws FieldParseException
	{
		if (_context == null)
		{
			_context = specialFieldContext;
		}

		if (FieldContext.isForceValue( _context ) == false && isReadOnly( _object, _context ) == true)
		{ 
			// don't allow setting of read only fields
			// log.warn("Tried to set value of read only field" + getId());
			return false;
		}

		if ((_options == null) || !_options.isRawProperties())
		{
			if ((_value != null) && _value.equals( getValue( _object, _context ) )) // if
																					// not																		  // change,																		// do																		 // nothing
			{
				return false; // TODO certain time-distibued fields need to be
							  // changed
			}

			if (hasExternalType() == true)
			{ // does the second pass, converting from
			  // say, Date to long

				Class type = isMap()
					? externalType
					: internalType;

				if ((_value != null) || !isAllowNull())
				{
					_value = FieldConverter.convert( _value, type, _context ); // convert from date to long for example
				}

				if ((_value == null) && !isMap()) // TODO is this how to treat null values?
				{
					return false;
				}

				if (isMoney() == true && isConvertCurrency() == true)
				{ // convert currency
					_value = CurrencyRateTable.convertFromDisplay( (Number)_value );
				}
			}

			if (range != null)
			{
				range.validate( _value, this );
			}
		}

		if (methodSet == null)
		{
			return false;
		}

		if (FieldContext.isParseOnly( _context ) == true) // if just parsing, do not set
		{
			return false;
		}

		try
		{
			if (isMap() == true)
			{
				Map map = (Map)getPropertyValue( _object, _context );
				String id = getMapEntryId();
				Object oldValue = map.get( id );
				map.put( id, _value );

				if ((_options != null) && _options.isRawUndo())
				{
					_options.setChange( new ObjectChange(_value, oldValue) );
				}
			}
			else
			{
				Object oldValue = null;

				if ((_options != null) && _options.isRawUndo())
				{
					if (getHasNoContext)
					{
						oldValue = myGetMethod.invoke( _object,
								(isIndexed()
								? new Object[]
								{
									new Integer(index)
								}
								: new Object[] {  }) );
					}
					else
					{
						oldValue = myGetMethod.invoke( _object,
								(isIndexed()
								? new Object[]
								{
									new Integer(index),
									_context
								}
								: new Object[]
								{
									_context
								}) );
					}
				}

				if (setHasNoContext == true)
				{
					methodSet.invoke( _object,
						(isIndexed()
						? new Object[]
						{
							new Integer(index),
							_value
						}
						: new Object[]
						{
							_value
						}) );
				}
				else
				{
					methodSet.invoke( _object,
						(isIndexed()
						? new Object[]
						{
							new Integer(index),
							_value,
							_context
						}
						: new Object[]
						{
							_value,
							_context
						}) );

					if ((_options != null) && _options.isRawUndo() == true)
					{
						;
					}
				}

				if ((_options != null) && _options.isRawUndo() == true)
				{
					Object newValue;

					if (getHasNoContext == true)
					{
						newValue = myGetMethod.invoke( _object,
							(isIndexed()
							? new Object[]
							{
								new Integer( index )
							}
							: new Object[] 
							{  
							}) );
					}
					else
					{
						newValue = myGetMethod.invoke( _object,
							(isIndexed()
							? new Object[]
							{
								new Integer( index ),
								_context
							}
							: new Object[]
							{
								_context
							}) );
					}

					_options.setChange( new ObjectChange( newValue, oldValue ) );
				}
			}

			//LC
			if (_object instanceof DataObject == true)
			{
				if ((_context == null) || _context.isNoDirty() == false)
				{
					((DataObject)_object).setDirty( true );
				}
			}
		}
		catch (final IllegalArgumentException _e)
		{
			throw new FieldParseException( _e );
		}
		catch (final IllegalAccessException _e)
		{
			_e.printStackTrace();
		}
		catch (final InvocationTargetException _e)
		{
			Throwable cause = _e.getCause();
			_e.printStackTrace();

			if ((cause != null) && cause instanceof FieldParseException == true)
			{
				throw (FieldParseException)cause;
			}
			else
			{
				// setters can throw other values, so don't treat as bad exception
				throw new FieldParseException( cause.getMessage() );
			}
		}

		return true;
	}

	private void setInternalValueAndUpdate( 
		final Object _object,
		final Object _source,
		final Object _value,
		FieldContext _context,
		final FieldSetOptions _options )
		throws FieldParseException
	{
		if (_context == null)
		{
			_context = specialFieldContext;
		}

		if (setInternalValue( _object, _value, _context, _options ) == true)
		{ // if succeeded in setting value

			if ((_context == null) || !_context.isNoUpdate())
			{
				fireEvent( _object, _source, _context );
			}
		}
	}

	public void setListElementsField( 
		final Field _listElementsField )
	{
		this.listElementsField = _listElementsField;
	}

	public void setLookupTypes( 
		final String _lookupTypes )
	{
		this.lookupTypes = _lookupTypes;
	}

	/**
	 * @param memo
	 *            The memo to set.
	 */
	public void setMemo( 
		final boolean _memo )
	{
		this.memo = _memo;
	}

	/**
	 * @param money
	 *            The money to set.
	 */
	public void setMoney( 
		final boolean _money )
	{
		this.money = _money;
	}

	public final void setName( 
		final String _name )
	{
		this.name = _name;
	}

	/**
	 * @param nameField
	 *            The nameField to set.
	 */
	public void setNameField( 
		final boolean _nameField )
	{
		this.nameField = _nameField;
	}

	public void setNotChoosable( 
		final boolean _notChoosable )
	{
		this.notChoosable = _notChoosable;
	}

	public void setOnlyChangeOnPublish( 
		final boolean _onlyChangeOnPublish )
	{
		this.onlyChangeOnPublish = _onlyChangeOnPublish;
	}

	/**
	 * @param password
	 *            The password to set.
	 */
	public void setPassword( 
		final boolean _password )
	{
		this.password = _password;
	}

	/**
	 * @param percent
	 *            The percent to set.
	 */
	public void setPercent( 
		final boolean _percent )
	{
		this.percent = _percent;
	}

	public final void setProperty( 
		final String _property )
	{
		this.property = _property;
	}

	/**
	 * @param range
	 *            The range to set.
	 */
	public void setRange( 
		final Range _range )
	{
		this.range = _range;
	}

	/**
	 * @param value
	 *            The value to set.
	 */
	public void setRate( 
		final boolean _rate )
	{
		this.rate = _rate;
	}

	/**
	 * @param readOnly
	 *            The readOnly to set.
	 */
	public void setReadOnly( 
		final boolean _readOnly )
	{
		this.readOnly = _readOnly;
	}

	/**
	 * @param referencedIdProperty
	 *            The referencedIdProperty to set.
	 */
	public void setReferencedIdProperty( 
		final String _referencedIdProperty )
	{
		this.referencedIdProperty = _referencedIdProperty;
	}

	/**
	 * @param referencedObjectProperty
	 *            The referencedObjectProperty to set.
	 */
	public void setReferencedObjectProperty( 
		final String _referencedObjectProperty )
	{
		this.referencedObjectProperty = _referencedObjectProperty;
	}

	public void setResourceNames( 
		final boolean _resourceNames )
	{
		this.resourceNames = _resourceNames;
	}

	/**
	 * @param scalar
	 *            The scalar to set.
	 */
	public final void setScalar( 
		final boolean _scalar )
	{
		scalar = _scalar;
	}

	public final void setSelect( 
		final Select _select )
	{
		select = _select;
	}

	public final void setSelectPopulateMethod( 
		final String _populateMethod )
	{
		final StaticSelect select = new StaticSelect();
		select.setPopulateMethod( _populateMethod );
		setSelect( select );
	}

	public void setServer( 
		final boolean _server )
	{
		this.server = _server;
	}

	public void setSpecialFieldContext( 
		final FieldContext _specialFieldContext )
	{
		this.specialFieldContext = _specialFieldContext;
	}

	public void setStartValue( 
		final boolean _startValue )
	{
		this.startValue = _startValue;
	}

	// Strange: digester doesn't set summary property. sum is used instead
	public final void setSum( 
		final String _summaryType )
	{
		setSummary( _summaryType );
	}

	public final void setSummary( 
		final String _summaryType )
	{
		this.summary = SummaryVisitorFactory.getSummaryId( _summaryType );

//		if (summary == NONE)
//			log.warn("unknown summary type: " + summaryType + " for field " + getName());
	}

	public void setSvgColumnWidth( 
		final int _svgColumnWidth )
	{
		this.svgColumnWidth = _svgColumnWidth;
	}

	public final void setText( 
		final Object _object,
		final String _textValue,
		final FieldContext _context )
		throws FieldParseException
	{
		setText( _object, _textValue, _context, null );
	}

	public final void setText( 
		final Object _object,
		final String _textValue,
		FieldContext _context,
		final FieldSetOptions _options )
		throws FieldParseException
	{
		if (_context == null)
		{
			_context = specialFieldContext;
		}

		if (FieldContext.isForceValue( _context ) == false && isReadOnly( _object, _context ) == true)
		{ 
			// don't allow setting of read only fields
			//log.warn( "Tried to set text of read only field" + getId() );

			return;
		}

		Object value = preprocessText( _object, _textValue, _context );

		if (value == _textValue)
		{
			value = setTextValue( _object, _textValue, _context );
		}

		setInternalValueAndUpdate( _object, this, value, _context, _options );
	}

	public Object setTextValue( 
		final Object _object,
		final String _textValue,
		final FieldContext _context )
		throws FieldParseException
	{
		Format format = getFormat( _object );
		Object value = null;

		if (format != null)
		{
			try
			{
				value = format.parseObject( _textValue );
			}
			catch (final ParseException _e)
			{
				format = getSecondaryFormat( _object ); // allow money to parse as number too

				boolean secondOK = false;

				if (format != null)
				{
					try
					{
						value = format.parseObject( _textValue );
						secondOK = true;
					}
					catch (final ParseException _e1)
					{
						// ignore
					}
				}

				if (secondOK == false)
				{
					throw new FieldParseException( _e );
				}
			}
		}
		else
		{
			value = FieldConverter.convert( _textValue, hasExternalType()
					? externalType
					: internalType, _context ); // converts
		}

		return value;
	}

	public final void setTextWidth( 
		final int _textWidth )
	{
		this.textWidth = _textWidth;
	}

	public void setTransientValue( 
		final boolean _transientValue )
	{
		this.transientValue = _transientValue;
	}

	public void setType( 
		final String _type )
	{
		try
		{
			setExternalType( Class.forName( _type ) );
		}
		catch (final ClassNotFoundException _e)
		{
			// TODO Auto-generated catch block
			_e.printStackTrace();
		}
	}

	public final void setUrl( 
		final String _url )
	{
		this.url = _url;
	}

	public final void setValidOnObjectCreate( 
		final boolean _validOnObjectCreate )
	{
		this.validOnObjectCreate = _validOnObjectCreate;
	}

	public boolean setValue( 
		final Object _object,
		final Object _source,
		final Object _value )
	{
		try
		{
			setValue( _object, _source, _value, null );

			return true;
		}
		catch (final FieldParseException _e)
		{
			return false;
		}
	}

	/**
	 * 
	 * @param _object
	 * @param _source
	 * @param _value
	 * @param _context
	 * @param _options
	 * @throws FieldParseException 
	 */
	public void setValue( 
		final Object _object,
		final Object _source,
		Object _value,
		FieldContext _context,
		final FieldSetOptions _options )
		throws FieldParseException
	{
		if (_context == null)
		{
			_context = specialFieldContext;
		}

//		Object oldValue = null;
//		if (context!=null&&context.getUndo() != null)
//			oldValue = getValue(object,context);
//		if (value==null&&isBoolean()) value=false; //setting a boolean as null fails, it's possible with the undo of boolean field initialized at null
		if ((_options == null) || _options.isRawProperties() == false)
		{
			if (hasOptions() == true)
			{
				if (_value instanceof String == true)
				{
					_value = preprocessText( _object, (String)_value, _context );
				}
			}
			else
			{
				if (_value instanceof String == true && hasExternalType() == true) // do a first pass,													  // converting													  // say, from													 // string to													// Duration
				{
					_value = FieldConverter.convert( _value, externalType, _context );
				}

//				if (isBoolean() && value == null) value = false;
				if ((_value == null) && isAllowNull() == false)
				{
					throw new FieldParseException(errorMessage( _value, _object ));
				}
			}
		}

		setInternalValueAndUpdate( _object, _source, _value, _context, _options );

//		if (context!=null&&context.getUndo() != null) {
//			context.getUndo().postEdit(new FieldEdit(this,object,value,oldValue,this,context));
//
//		}
	}

	public void setValue( 
		final Object _object,
		final Object _source,
		final Object _value,
		final FieldContext _context )
		throws FieldParseException
	{
		setValue( _object, _source, _value, _context, null );
	}

	/**
	 * Sets each object in a collection to value. Exceptions are ignored
	 *
	 * @param collection
	 * @param value
	 */
	public void setValueForEach( 
		final Collection _collection,
		final Object _value,
		FieldContext _context,
		final Object _eventSource )
	{
		if (_context == null)
		{
			_context = specialFieldContext;
		}

		if (_collection == null)
		{
			return;
		}

		Iterator i = _collection.iterator();
		Object current;

		while (i.hasNext() == true)
		{
			current = i.next();

			try
			{
				setValue( current, _eventSource, _value, _context );
			}
			catch (final FieldParseException _e)
			{
				// TODO Auto-generated catch block
				_e.printStackTrace();
			}
		}
	}

	/**
	 * @param vector
	 *            The vector to set.
	 */
	public void setVector( 
		final boolean _vector )
	{
		this.vector = _vector;
	}

	/**
	 * @param work
	 *            The work to set.
	 */
	public void setWork( 
		final boolean _work )
	{
		setDuration( _work ); // work is always a duration too
		this.work = _work;
	}

	/**
	 * @param zeroBasedIndex
	 *            The zeroBasedIndex to set.
	 */
	public void setZeroBasedIndex( 
		final boolean _zeroBasedIndex )
	{
		this.zeroBasedIndex = _zeroBasedIndex;
	}

	public static String shortName( 
		final String _fieldName )
	{
		int pos = _fieldName.indexOf( "." );

		if (pos == -1)
		{
			return _fieldName;
		}
		else
		{
			return _fieldName.substring( pos + 1 );
		}
	}

	public String syntaxErrorForField()
	{
		return errorMessage( null, null );
	}

	public String toExternalText( 
		final Object _value,
		final Object _obj )
	{
		if (hasOptions() == true)
		{
			return convertValueToStringUsingOptions( _value, _obj );
		}
		else
		{
			return toText( _value, _obj );
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return getName();
	}

	public String toText( 
		final Object _value,
		final Object _object )
	{
		if (_value == null)
		{
			return EMPTY_STRING;
		}

		if ((defaultValue != null) && hideZeroValues == true && defaultValue.equals( _value ) == true)
		{
			return EMPTY_STRING;
		}

		Format format = getFormat( _object );

		if (isMoney() == true)
		{
			return ((Money)_value).getFormattedValue();
		}
		else if (format != null)
		{
			return format.format( _value );
		}
		else
		{
			if (isHyperlink() == true)
			{
				return ((Hyperlink)_value).toString();
			}

			// Convert to string
			return FieldConverter.toString( _value, getDisplayType(), null ); 
		}
	}

	/**
	 * Return the name in shortened form: e.g. Long, String, etc.
	 * @return
	 */
	public String typeName()
	{
		if (isPercent() == true)
		{
			return "Percent";
		}

		if (isImage() == true)
		{
			return "Image";
		}

		if (isDate() == true)
		{
			return "Date";
		}

		if (isDuration() == true)
		{
			return "Duration";
		}

		String t = getDisplayType().toString();
		int i = t.lastIndexOf( "." );

		if (i != -1)
		{
			return t.substring( i + 1 );
		}

		return t;
	}

	/*
	 * update the keys in the select so they will be formatted for display
	 */
	public void updateKeys()
	{
//		((FormattedStaticSelect)select).updateKeys();
	}

	public static Object value( 
		final Field _field,
		final Object _object )
	{
		return _field.getValue( _object, null );
	}

	static Log log = LogFactory.getLog( Field.class );
	private static final String EMPTY_STRING = "";
	public static final String PASSWORD_MASK = "********";
	private static final String NON_IMPLEMENTED = "<not implemented>";
	public static final String NOT_APPLICABLE = "<N/A>";
	public static final String NO_CHOICE = "";
	public static final String MULTIPLE_VALUES = Messages.getString( "Symbol.multipleValues" );

	// reflection info - these do not change, so that can be reused across all
	// fields
	private static Class[] getterParams = new Class[] {  };
	private static Class[] getterIndexedParams = new Class[]
		{
			int.class
		};
	private static Class[] getterContextParams = new Class[]
		{
			FieldContext.class
		}; // context
	private static Class[] getterIndexedContextParams = new Class[]
		{
			int.class,
			FieldContext.class
		}; // context
	static SimpleDateFormat f = new SimpleDateFormat("E");
	boolean dateOnly = false; // for date fields, whether to show time
	private Class clazz = null;
	private Class displayType = null;
	private Class externalType = null; // if non null then its the logical
									   // type. For example, externalType=Date,
									   // internalType=long for date values
	private Class internalType = null; // return type of getter
	private Comparator comparator = null;
	private Field listElementsField = null;
	private FieldAccessible accessControl = null;
	private FieldContext specialFieldContext = null; // for web to set exact context
	private Integer ZERO = Integer.valueOf( 0 );
	private Method finderMethod = null;
	private Method methodHide = null;
	private Method methodOptions = null;
	private Method methodReadOnly = null;
	private Method methodReset = null;
	private Method methodSet = null;
	private Method myGetMethod = null;
	private Object defaultValue = null;
	private OptionsFilter filter = null;
	private Range range = null;
	private ScriptedFormula formula = null;
	private Select select = null;
	private String action = null;
	private String alias = null;
	private String alternateId = null;
	private String configurationId; // what's read in from config file.
	private String englishName;
	private String errorMessage = null;
	private String extraCategory = null; // for extra fields, such as those from salesforce
	private String finder = null;
	private String help = null;
	private String id; // id read from config or modified if array field
	public String lookupTypes = null; // types separated by semicolons
	private String name; // id converted to string from properties file
	private String property;
	private String referencedIdProperty = null;
	private String referencedObjectProperty = null;
	private String url = null;
	private boolean allowNull = false;
	private boolean callValidateOnClear = false; // if the value must be
												 // validated on clearing
	private boolean cantReset = false; // if the field can't be reset
	private boolean convertCurrency = false;
	private boolean custom = false;
	private boolean date = false; // if the field holds a date
	private boolean dirtiesWholeDocument = false; // wehter modifying this field causes all parts of its document to be dirty
	private boolean dontLimitToChoices = false; // if a choice list exists and
												// value need not be in the list
	private boolean duration = false; // if the field holds a duration
	private boolean dynamicOptions = false; // whether a combo needs to be
											// evaluated every time shown
	private boolean endValue = false; // for dates, whether to end of day
	private boolean getHasNoContext = false; // if the getter is just a property getter
	private boolean graphical = false; // to flag for fields like indicator
	private boolean hasToolTip = false;
	private boolean hideHasNoContext = false; // if hide is just a getter
	private boolean hideZeroValues = false; // if the field text should be null
											// if value is default
	private boolean image = false;
	private boolean map = false;
	private boolean memo = false; // if the field is a multiline edit.
	private boolean money = false; // if the value is a money
	private boolean nameField = false;
	private boolean notChoosable = false;
	private boolean onlyChangeOnPublish = false;
	private boolean optionsHasNoContext = false; // if options is just a getter
	private boolean password = false; // if the value is a password and should
									  // not be displayed
	private boolean percent = false; // if the value is a percentage and
									 // should be displayed as such
	private boolean rate = false; // if the field holds a value

	// properties that can be set from XML
	private boolean readOnly = false; // if value is always read only
	private boolean readOnlyHasNoContext = false; // if read-only is just a getter
	private boolean resetHasNoContext = false; // if reset is just a getter
	private boolean resourceNames = false; // if the field holds resource names
	private boolean scalar = true; // if the value can be accessed as a scalar
								   // (normal case)
	private boolean server;
	private boolean setHasNoContext = false; // if the setter is just a property setter
	private boolean startValue = false; // for dates, whether to use start  of day
	private boolean transientValue = false;
	private boolean validOnObjectCreate = true; // whether this field can appear on New Item Dialogs.
	private boolean vector = false; // if the value can be accessed as a vector
	private boolean work = false; // if the field holds a work value
	private boolean zeroBasedIndex = false; // if should create a 0 index too.
	private int columnWidth = 0; // column width for spreadsheet
	private int groupSummary = NONE;
	private int index = 0; // index of this field in indexed property
	private int indexes = 0; // nonzero number of elements
	private int summary = NONE;
	private int svgColumnWidth = 0; //due to font conversion svg column width can different
	private int textWidth = Integer.MAX_VALUE;
}
