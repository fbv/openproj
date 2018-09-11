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

 Attribution Information: Attribution Copyright Notice: Copyright © 2006, 2007
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

import com.projity.datatype.Duration;
import com.projity.datatype.DurationFormat;
import com.projity.datatype.Hyperlink;
import com.projity.datatype.Money;
import com.projity.datatype.Work;
import com.projity.options.EditOption;
import com.projity.strings.Messages;
import com.projity.util.DateTime;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;

/**
 * This class decorates ConvertUtils to use Projity specific types and validation
 */
public class FieldConverter
{
	public static String toString( 
		final Object pValue,
		final Class pClass,
		final FieldContext pContext )
	{
		return getInstance()._toString( pValue, pClass, pContext );
	}

	public static String toString( 
		final Object pValue )
	{
		return getInstance()._toString( pValue, pValue.getClass(), null );
	}

	public static Object fromString( 
		final String pValue,
		final Class pClass )
	{
		return ConvertUtils.convert( pValue, pClass );
	}

	/**
	 * Convert from an object, usually a string, to another object
	 *
	 * @param. Convert from this value
	 * @param                                                                                                                                         pClass.
	 *                                                                                                                                           Convert
	 *                                                                                                                                           to
	 *                                                                                                                                           this
	 *                                                                                                                                           class
	 *                                                                                                                                           type.
	 * @param context Converter context to use
	 * @return object of type class.
	 * @throws FieldParseException
	 */
	public static Object convert( 
		final Object pValue,
		final Class pClass,
		final FieldContext pContext )
		throws FieldParseException
	{
		return getInstance()._convert( pValue, pClass, pContext );
	}

	public static FieldConverter getInstance()
	{
		if (myInstance == null)
		{
			myInstance = new FieldConverter();
		}
		return myInstance;
	}

	public static void reinitialize()
	{
		myInstance = null;
	}

	/**
	 *
	 * @par. Convert from this value
	 * @par. Convert to this class type.
	 * @return object of type class.
	 * @throws FieldParseException
	 */
	private Object _convert( 
		final Object pValue,
		final Class pClass,
		final FieldContext pContext )
		throws FieldParseException
	{
		try
		{
			if (pValue instanceof String)
			{
				Object result = null;
				if (pContext == null)
				{
					result = ConvertUtils.convert( (String) pValue, pClass );
				}
				else
				{
					Converter contextConverter = null;
					HashMap<Class, Converter> contextMap = myContextMaps.get( pContext );
					if (contextMap != null)
					{
						contextConverter = contextMap.get( pClass );
					}
					if (contextConverter != null)
					{
						contextConverter.convert( pClass, pValue );
					}
					else
					{
						if (pClass != String.class)
						{
							System.out.println( "no context converter found " );
						}
						result = ConvertUtils.convert( (String) pValue, pClass );
					}
				}
				//			if (result instanceof java.util.Date) { //  dates need to be normalized
				//				result = new Date(DateTime.gmt((Date) result));
				//			}
				if (result == null)
				{
					throw new FieldParseException( "Invalid type" );
				}
				return result;
			}

			// Because of stupidity of beanutils which assumes type string, I implement this by hand
			Converter converter = ConvertUtils.lookup( pClass );
			if (converter == null)
			{
				System.out.println( "converter is null for class " + pClass + " myInstance " + myInstance.hashCode() + " resetting" );
				myInstance = new FieldConverter();
				converter = ConvertUtils.lookup( String.class );
			}
			
			return converter.convert( pClass, pValue );
		}
		catch (ConversionException conversionException)
		{
			throw new FieldParseException( conversionException );
		}
	}

	private String _toString( 
		final Object pValue,
		final Class pClass,
		final FieldContext pContext )
	{
		if (pContext == COMPACT_CONVERTER_CONTEXT)
		{
			return (String) myCompactStringConverter.convert( pClass, pValue );
		}
		else
		{
			return (String) myStringConverter.convert( pClass, pValue );
		}
	}
	public static final FieldContext COMPACT_CONVERTER_CONTEXT = new FieldContext();

	static
	{
		COMPACT_CONVERTER_CONTEXT.setCompact( true );
	}

	private FieldConverter()
	{
// psc1952 - this statement seems unnecessary
//		myInstance = this;
// psc1952 - end
		
		myStringConverter = new StringConverter( false );
		myCompactStringConverter = new StringConverter( true );
		ConvertUtils.register( myStringConverter, String.class );   // Wrapper class
		ConvertUtils.register( new DateConverter(), Date.class );   // Wrapper class
		ConvertUtils.register( new CalendarConverter(), GregorianCalendar.class );   // Wrapper class
		ConvertUtils.register( new DurationConverter(), Duration.class );   // Wrapper class
		ConvertUtils.register( new WorkConverter(), Work.class );   // Wrapper class
		ConvertUtils.register( new MoneyConverter(), Money.class );   // Wrapper class
		Converter intConverter = new IntegerConverter();
		ConvertUtils.register( intConverter, Integer.TYPE );    // Native type
		ConvertUtils.register( intConverter, Integer.class );   // Wrapper class
		Converter longConverter = new LongConverter();
		ConvertUtils.register( longConverter, Long.TYPE );    // Native type
		ConvertUtils.register( longConverter, Long.class );   // Wrapper class
		Converter doubleConverter = new DoubleConverter();
		ConvertUtils.register( doubleConverter, Double.TYPE );    // Native type
		ConvertUtils.register( doubleConverter, Double.class );   // Wrapper class
		ConvertUtils.register( new HyperlinkConverter(), Hyperlink.class );   // Wrapper class


		// short context converters
		HashMap<Class, Converter> compactMap = new HashMap<>();
		myContextMaps.put( COMPACT_CONVERTER_CONTEXT, compactMap );
		compactMap.put( String.class, myCompactStringConverter );
		// no need for duration or money as parsing is done in long form

	}

	private static class StringConverter
		implements Converter
	{
		StringConverter( 
			final boolean pCompact )
		{
			myCompact = pCompact;
		}

		@Override
		public Object convert( 
			final Class pClass,
			final Object pValue )
		{
			if (pValue instanceof Work)
			{
				if (myCompact)
				{
					return ((DurationFormat) DurationFormat.getWorkInstance()).formatCompact( pValue );
				}
				else
				{
					return ((DurationFormat) DurationFormat.getWorkInstance()).format( pValue );
				}
			}
			else if (pValue instanceof Duration)
			{
				if (myCompact)
				{
					return ((DurationFormat) DurationFormat.getInstance()).formatCompact( pValue );
				}
				else
				{
					return ((DurationFormat) DurationFormat.getInstance()).format( pValue );
				}
			}
			else if (pValue instanceof Money)
			{
				return Money.formatCurrency( ((Money) pValue).doubleValue(), myCompact );
			}
			else if (pValue instanceof Date)
			{
				if (pValue.equals( DateTime.getZeroDate() ))
				{
					return null;
				}
				return EditOption.getInstance().getDateFormat().format( pValue );
			}
			else
			{
				if (pValue == null)
				{
					return null;
				}
				else
				{
					return pValue.toString();
				}
			}
		}

		private boolean myCompact = false;
	}

	// make a converter for long that can process dates and durations
	private static class LongConverter
		implements Converter
	{
		@Override
		public Object convert( 
			final Class pType,
			final Object pValue )
			throws ConversionException
		{
			if (pValue == null)
			{
				return null;
			}
			
			if (pValue != null)
			{
				if (pValue instanceof Date)
				{
					return new Long( ((Date) pValue).getTime() );
				}
				else if (pValue instanceof GregorianCalendar)
				{
					return new Long( ((GregorianCalendar) pValue).getTimeInMillis() );
				}
				else if (pValue instanceof Duration || pValue instanceof Work)
				{
					return new Long( ((Duration) pValue).getEncodedMillis() );
				}
				else if (pValue instanceof String)
				{
					NumberFormat nf = NumberFormat.getIntegerInstance();
					String valueString = (String) pValue;

					try
					{
						DecimalFormat df = (DecimalFormat) nf;

						if (!valueString.contains( "" + (char) 160 ))
						{
							DecimalFormatSymbols symbols = df.getDecimalFormatSymbols();
							char sym = symbols.getGroupingSeparator();

							if (sym == 160)
							{
								symbols.setGroupingSeparator( ' ' );
								df.setDecimalFormatSymbols( symbols );
							}
						}
						return df.parse( (String) pValue );
					}
					catch (ParseException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			return baseConverter.convert( pType, pValue );
		}

		private final Converter baseConverter = new org.apache.commons.beanutils.converters.LongConverter();
	};

//	 make a converter for int
	private static class IntegerConverter
		implements Converter
	{
		@Override
		public Object convert( 
			final Class pType,
			final Object pValue )
			throws ConversionException
		{
			if (pValue == null)
			{
				return null;
			}
			if (pValue != null)
			{
				if (pValue instanceof Date)
				{
					return new Long( ((Date) pValue).getTime() );
				}
				else if (pValue instanceof GregorianCalendar)
				{
					return new Long( ((GregorianCalendar) pValue).getTimeInMillis() );
				}
				else if (pValue instanceof Duration || pValue instanceof Work)
				{
					return new Long( ((Duration) pValue).getEncodedMillis() );
				}
				else if (pValue instanceof String)
				{
					NumberFormat nf = NumberFormat.getIntegerInstance();

					String valueString = (String) pValue;

					try
					{
						DecimalFormat df = (DecimalFormat) nf;

						if (!valueString.contains( "" + (char) 160 ))
						{
							DecimalFormatSymbols symbols = df.getDecimalFormatSymbols();
							char sym = symbols.getGroupingSeparator();

							if (sym == 160)
							{
								symbols.setGroupingSeparator( ' ' );
								df.setDecimalFormatSymbols( symbols );
							}
						}

// psc1952 - 2011.12.4 - old code - returns Long which generates an exception                                              
//						return df.parse((String) value);
// psc1952 - 2011.12.4 - new code - should return int                                           
						return df.parse( (String) pValue ).intValue();
// psc1952 - 2011.12.4 - end 
					}
					catch (ParseException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			return baseConverter.convert( pType, pValue );
		}

		private final Converter baseConverter = new org.apache.commons.beanutils.converters.IntegerConverter();
	};

	private static class DateConverter
		implements Converter
	{
		@Override
		public Object convert( 
			final Class pType,
			final Object pValue )
			throws ConversionException
		{
			if (pValue == null)
			{
				return null;
			}
			
			if (pValue instanceof Long)
			{
				long longValue = ((Long) pValue).longValue();
				if (longValue == 0)
				{
					return null;
				}
				return new Date( longValue );
			}
			else if (pValue instanceof Date)
			{
				return pValue;
			}
			else if (pValue instanceof Calendar)
			{
				return ((Calendar) pValue).getTime();
			}
			else if (pValue instanceof String)
			{
				try
				{
					return EditOption.getInstance().getDateFormat().parse( (String) pValue );
				}
				catch (ParseException e)
				{
					try
					{
						return DateTime.utcShortDateFormatInstance().parse( (String) pValue ); // try without time
					}
					catch (ParseException e1)
					{
						throw new ConversionException( Messages.getString( "Message.invalidDate" ) );
					}
				}
			}

			throw new ConversionException( "Error: no conversion from " + pValue.getClass().getName() + " to " 
				+ pType.getName() + " for value" + pValue );
		}
	};

	// GregorianCalendar converter
	private static class CalendarConverter
		implements Converter
	{
		@Override
		public Object convert( 
			final Class pType,
			final Object pValue )
			throws ConversionException
		{
			GregorianCalendar cal = DateTime.calendarInstance();
			if (pValue == null)
			{
				return null;
			}
			else if (pValue instanceof Long)
			{
				long longValue = ((Long) pValue).longValue();
				if (longValue == 0)
				{
					return null;
				}

				cal.setTimeInMillis( longValue );
				return cal;
			}
			else if (pValue instanceof Date)
			{
				cal.setTime( (Date) pValue );
				return cal;
			}
			else if (pValue instanceof String)
			{
				Date d = (Date) dateConverter.convert( Date.class, pValue );
				cal.setTime( d );
				return cal;
			}
			
			throw new ConversionException( "Error: no conversion from " + pValue.getClass().getName() + " to " 
				+ pType.getName() + " for value" + pValue );
		}
	
		private static DateConverter dateConverter = new DateConverter();
};

	private static class DurationConverter
		implements Converter
	{
		@Override
		public Object convert( 
			final Class pType,
			final Object pValue )
			throws ConversionException
		{
			if (pValue == null)
			{
				return Duration.getInstanceFromDouble( null );
			}

			if (pValue instanceof Number)
			{
				return new Duration( ((Number) pValue).longValue() );
			}
			else if (pValue instanceof Work)
			{
				return new Duration( ((Work) pValue).longValue() );
			}
			else if (pValue instanceof Duration)
			{
				return pValue;
			}
			else if (pValue instanceof String)
			{
				try
				{
					return DurationFormat.getInstance().parseObject( (String) pValue );
				}
				catch (ParseException e)
				{
					throw new ConversionException( Messages.getString( "Message.invalidDuration" ) );
				}
			}
			
			throw new ConversionException( "Error: no conversion from " + pValue.getClass().getName() + " to " 
				+ pType.getName() + " for value" + pValue );
		}
	};

	private static class WorkConverter
		implements Converter
	{
		@Override
		public Object convert( 
			final Class pType,
			final Object pValue )
			throws ConversionException
		{
			if (pValue == null)
			{
				return Duration.getInstanceFromDouble( null );
			}

			if (pValue instanceof Number)
			{
				return new Work( ((Number) pValue).longValue() );
			}
			else if (pValue instanceof Work)
			{
				return new Work( ((Work) pValue).longValue() );
			}
			else if (pValue instanceof Duration)
			{
				return pValue;
			}
			else if (pValue instanceof String)
			{
				try
				{
					return DurationFormat.getWorkInstance().parseObject( (String) pValue );
				}
				catch (ParseException e)
				{
					throw new ConversionException( Messages.getString( "Message.invalidDuration" ), e );
				}
			}
			
			throw new ConversionException( "Error: no conversion from " + pValue.getClass().getName() + " to " 
				+ pType.getName() + " for value" + pValue );
		}
	};

	private static class DoubleConverter
		implements Converter
	{
		@Override
		public Object convert( 
			final Class pType,
			final Object pValue )
			throws ConversionException
		{
			if (pValue != null)
			{
				if (pValue instanceof Double)
				{
					return pValue;
				}
				else if (pValue instanceof Money)
				{
					double num = ((Number) pValue).doubleValue();
					if (Double.isInfinite( num ) || Double.isNaN( num ))
					{
						System.out.println( "Error: number is invalid double in MoneyConverter " + pValue );
						num = 0.0;
					}
					return new Double( num );
				}
				else if (pValue instanceof String)
				{
					NumberFormat nf = NumberFormat.getInstance();
					try
					{
						DecimalFormat df = (DecimalFormat) nf;
						String valueString = (String) pValue;
						if (!valueString.contains( "" + (char) 160 ))
						{
							DecimalFormatSymbols symbols = df.getDecimalFormatSymbols();
							char sym = symbols.getGroupingSeparator();
							if (sym == 160)
							{
								symbols.setGroupingSeparator( ' ' );
								df.setDecimalFormatSymbols( symbols );
							}
						}
						
						return df.parse( (String) pValue );
					}
					catch (ParseException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
			else
			{
				return Double.valueOf( 0.0 );
			}
			
			return baseConverter.convert( pType, pValue );
		}

		private final Converter baseConverter = new org.apache.commons.beanutils.converters.DoubleConverter();
	};

	/*
	 * TODO I have also experimented with the JADE library's Money class. It is probably more useful for
	 * performing currency conversions than as a datatype. A possible source for currency exchange rates is
	 * the web service here:
	 * http://www.bindingpoint.com/service.aspx?skey=377e6659-061f-4956-8edb-19b5023bc33b
	 *
	 */
	private static class MoneyConverter
		implements Converter
	{
		@Override
		public Object convert( 
			final Class pType,
			final Object pValue )
			throws ConversionException
		{
			if (pValue == null)
			{
				return Money.getInstance( 0 );
			}
			
			if (pValue instanceof Money)
			{
				return pValue;
			}
			else if (pValue instanceof Number)
			{
				double num = ((Number) pValue).doubleValue();
				if (Double.isInfinite( num ) || Double.isNaN( num ))
				{
					System.out.println( "Error: number is invalid double in MoneyConverter " + pValue );
					num = 0.0;
				}
				return Money.getInstance( num );
			}
			else if (pValue instanceof String)
			{
				try
				{
					return Money.getFormat( false ).parseObject( (String) pValue );
				}
				catch (ParseException e)
				{
					throw new ConversionException( Messages.getString( "Message.invalidDuration" ), e );
				}
			}
			
			throw new ConversionException( "Error: no conversion from " + pValue.getClass().getName() + " to " 
				+ pType.getName() + " for value" + pValue );
		}
	}

	private static class HyperlinkConverter
		implements Converter
	{
		@Override
		public Object convert( 
			final Class pType,
			final Object pValue )
		{
			// TODO Auto-generated method stub
			if (pValue instanceof Hyperlink)
			{
				return pValue;
			}
			else if (pValue instanceof String)
			{
				String[] values = ((String) pValue).split( "\\@\\|\\@\\|\\@" );
				String address = values[ 0 ];
				if (values.length == 1)
				{
					return new Hyperlink( address, address );
				}
				else
				{
					String label = values[ 1 ];
					return new Hyperlink( label, address );
				}
			}

			throw new ConversionException( "Error: no conversion from " + pValue.getClass().getName() + " to " 
				+ pType.getName() + " for value" + pValue );
		}
	}

	public static void cleanUp()
	{
		myInstance = null;
	}

	private static FieldConverter myInstance = null;

	private final HashMap<FieldContext, HashMap<Class, Converter>> myContextMaps = new HashMap<>();
	private StringConverter myStringConverter;
	private StringConverter myCompactStringConverter;
}
