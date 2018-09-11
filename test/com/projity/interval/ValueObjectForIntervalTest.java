/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.projity.interval;

import com.projity.util.DateTime;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author Paul
 */
public class ValueObjectForIntervalTest
{
	public ValueObjectForIntervalTest()
	{
	}

	@BeforeClass
	public static void setUpClass()
		throws Exception
	{
	}

	@AfterClass
	public static void tearDownClass()
		throws Exception
	{
	}
	
	@Before
	public void setUp()
	{
	}
	
	@After
	public void tearDown()
	{
	}

	/**
	 * Test of null constructor of class ValueObjectForInterval.
	 */
	@Test
	public void testNullConstructor()
	{
		System.out.println( "null constructor" );
		ValueObjectForInterval instance = new ValueObjectForIntervalImpl();

		assertTrue( instance.end == DateTime.getMaxDate().getTime() );
		assertTrue( instance.start == ValueObjectForInterval.NA_TIME );
	}

	/**
	 * Test of copy constructor of class ValueObjectForInterval.
	 */
	@Test
	public void testCopyConstructor()
	{
		System.out.println( "copy constructor" );
		ValueObjectForIntervalImpl instance = new ValueObjectForIntervalImpl();
		ValueObjectForInterval copy = new ValueObjectForIntervalImpl( instance );

		assertTrue( copy.end == DateTime.getMaxDate().getTime() );
		assertTrue( copy.start == ValueObjectForInterval.NA_TIME );
	}

	/**
	 * Test of clone method, of class ValueObjectForInterval.
	 */
	@Test
	public void testClone()
	{
		System.out.println( "clone" );
		ValueObjectForInterval instance = new ValueObjectForIntervalImpl();
		ValueObjectForInterval expResult = new ValueObjectForIntervalImpl();
		ValueObjectForInterval result = instance.clone();
		assertEquals( expResult, result );
		assertTrue( instance.end == DateTime.getMaxDate().getTime() );
		assertTrue( instance.start == ValueObjectForInterval.NA_TIME );

		final long startValue = 100;
		instance = new ValueObjectForIntervalImpl( startValue );
		expResult = new ValueObjectForIntervalImpl( startValue );
		result = instance.clone();
		assertEquals( expResult, result );
		assertTrue( instance.end == DateTime.getMaxDate().getTime() );
		assertTrue( instance.start == startValue );
	}

	/**
	 * Test of compareTo method, of class ValueObjectForInterval.
	 */
	@Test
	public void testCompareTo()
	{
		System.out.println( "compareTo" );
		ValueObjectForInterval other = new ValueObjectForIntervalImpl();
		ValueObjectForInterval instance = new ValueObjectForIntervalImpl();
		int expResult = 0;
		int result = instance.compareTo( other );
		assertEquals( expResult, result );

		other = new ValueObjectForIntervalImpl( 100 );
		expResult = -1;
		result = instance.compareTo( other );
		assertEquals( expResult, result );
		
		instance = new ValueObjectForIntervalImpl( 200 );
		expResult = 1;
		result = instance.compareTo( other );
		assertEquals( expResult, result );
	}

	/**
	 * Test of deserializeCompact method, of class ValueObjectForInterval.
	 */
//	@Test
//	public void testDeserializeCompact()
//		throws Exception
//	{
//		System.out.println( "deserializeCompact" );
//		ObjectInputStream s = null;
//		ValueObjectForIntervalTable parentTable = null;
//		ValueObjectForInterval instance = new ValueObjectForIntervalImpl();
//		instance.deserializeCompact( s, parentTable );
//		// TODO review the generated test code and remove the default call to fail.
//		fail( "The test case is a prototype." );
//	}

	/**
	 * Test of equals method, of class ValueObjectForInterval.
	 */
	@Test
	public void testEquals()
	{
		System.out.println( "equals" );
		Object _other = new ValueObjectForIntervalImpl();
		ValueObjectForInterval instance = new ValueObjectForIntervalImpl();
		boolean expResult = true;
		boolean result = instance.equals( _other );
		assertEquals( expResult, result );
	}

	/**
	 * Test of getEnd method, of class ValueObjectForInterval.
	 */
	@Test
	public void testGetEnd()
	{
		System.out.println( "getEnd" );
		ValueObjectForInterval instance = new ValueObjectForIntervalImpl();
		long expResult = DateTime.getMaxDate().getTime();
		long result = instance.getEnd();
		assertEquals( expResult, result );
	}

	/**
	 * Test of getPrimaryValue method, of class ValueObjectForInterval.
	 */
	@Test
	public void testGetPrimaryValue()
	{
		System.out.println( "getPrimaryValue" );
		ValueObjectForInterval instance = new ValueObjectForIntervalImpl();
		double expResult = 0.0D;
		double result = instance.getPrimaryValue();
		assertEquals( expResult, result, 0.0D );
		
		expResult = 5000.0D;
		instance.setPrimaryValue( expResult );
		result = instance.getPrimaryValue();
		assertEquals( expResult, result, 0.0D );
	}

	/**
	 * Test of getStart method, of class ValueObjectForInterval.
	 */
	@Test
	public void testGetStart()
	{
		System.out.println( "getStart" );
		ValueObjectForInterval instance = new ValueObjectForIntervalImpl();
		long expResult = ValueObjectForInterval.NA_TIME;
		long result = instance.getStart();
		assertEquals( expResult, result );
	}

	/**
	 * Test of getValue method, of class ValueObjectForInterval.
	 */
	@Test
	public void testGetValue()
	{
		System.out.println( "getValue" );
		ValueObjectForInterval instance = new ValueObjectForIntervalImpl();
		double expResult = 0.0;
		double result = instance.getValue();
		assertEquals( expResult, result, 0.0 );

		expResult = 5000.0D;
		instance.setPrimaryValue( expResult );
		result = instance.getValue();
		assertEquals( expResult, result, 0.0D );
	}

	/**
	 * Test of hashCode method, of class ValueObjectForInterval.
	 */
	@Test
	public void testHashCode()
	{
		System.out.print( "hashCode - " );
		ValueObjectForInterval instance = new ValueObjectForIntervalImpl();
		int expResult = 96;
		int result = instance.hashCode();
		System.out.println( result );
		assertEquals( expResult, result );
	}

	/**
	 * Test of isDefault method, of class ValueObjectForInterval.
	 */
	@Test
	public void testIsDefault()
	{
		System.out.println( "isDefault" );
		ValueObjectForInterval instance = new ValueObjectForIntervalImpl();
		boolean expResult = true;
		boolean result = instance.isDefault();
		assertEquals( expResult, result );
		
		instance = new ValueObjectForIntervalImpl( DateTime.getMaxDate().getTime() );
		expResult = false;
		result = instance.isDefault();
		assertEquals( expResult, result );
	}

	/**
	 * Test of isFirst method, of class ValueObjectForInterval.
	 */
	@Test
	public void testIsFirst()
	{
		System.out.println( "isFirst" );
		ValueObjectForInterval instance = new ValueObjectForIntervalImpl();
		boolean expResult = true;
		boolean result = instance.isFirst();
		assertEquals( expResult, result );
		
		instance = new ValueObjectForIntervalImpl( DateTime.getMaxDate().getTime() );
		expResult = false;
		result = instance.isFirst();
		assertEquals( expResult, result );
	}

	/**
	 * Test of serializeCompact method, of class ValueObjectForInterval.
	 */
//	@Test
//	public void testSerializeCompact()
//		throws Exception
//	{
//		System.out.println( "serializeCompact" );
//		ObjectOutputStream s = null;
//		ValueObjectForIntervalTable parentTable = null;
//		ValueObjectForInterval instance = new ValueObjectForIntervalImpl();
//		instance.serializeCompact( s, parentTable );
//		// TODO review the generated test code and remove the default call to fail.
//		fail( "The test case is a prototype." );
//	}

	/**
	 * Test of setEnd method, of class ValueObjectForInterval.
	 */
	@Test
	public void testSetEnd()
	{
		System.out.println( "setEnd" );
		long end = 1000L;
		ValueObjectForInterval instance = new ValueObjectForIntervalImpl();
		instance.setEnd( end );
		assertEquals( instance.getEnd(), end );
	}

	/**
	 * Test of setPrimaryValue method, of class ValueObjectForInterval.
	 */
	@Test
	public void testSetPrimaryValue()
	{
		System.out.println( "setPrimaryValue" );
		double value = 10.0D;
		ValueObjectForInterval instance = new ValueObjectForIntervalImpl();
		instance.setPrimaryValue( value );

		assertTrue( instance.getPrimaryValue() == value );
	}

	/**
	 * Test of setStart method, of class ValueObjectForInterval.
	 */
	@Test
	public void testSetStart()
	{
		System.out.println( "setStart" );
		long start = 1200L;
		ValueObjectForInterval instance = new ValueObjectForIntervalImpl();
		instance.setStart( start );
		assertEquals( instance.getStart(), start );
	}

	public static class ValueObjectForIntervalImpl
		extends ValueObjectForInterval
	{
		public ValueObjectForIntervalImpl()
		{
			super();
		}
		
		public ValueObjectForIntervalImpl(
			final long _start )
		{
			super( _start );
		}
		
		public ValueObjectForIntervalImpl(
			final long _start,
			final double _value )
		{
			super( _start );
			myPrimaryValue = _value;
		}
		
		public ValueObjectForIntervalImpl(
			final ValueObjectForIntervalImpl _source )
		{
			super( _source );
		}
		
		@Override
		public double getPrimaryValue()
		{
			return myPrimaryValue;
		}

		@Override
		public void setPrimaryValue( 
			final double _value )
		{
			myPrimaryValue = _value;
		}

		@Override
		public ValueObjectForInterval clone()
		{
			return new ValueObjectForIntervalImpl( this );
		}
		
		private double myPrimaryValue = 0.0D;
	}
}
