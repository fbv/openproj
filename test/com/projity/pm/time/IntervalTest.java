/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.projity.pm.time;

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
public class IntervalTest
{
	
	public IntervalTest()
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
	 * Test of equals method, of class Interval.
	 */
	@Test
	public void testEquals()
	{
		System.out.println( "equals" );
		Object _other = new Interval( 0, 1 );
		Interval instance = new Interval( 0, 0 );
		boolean expResult = false;
		boolean result = instance.equals( _other );
		assertEquals( expResult, result );

		_other = new Interval( 0, 0 );
		expResult = true;
		result = instance.equals( _other );
		assertEquals( expResult, result );
	}

	/**
	 * Test of getElapsedDuration method, of class Interval.
	 */
	@Test
	public void testGetElapsedDuration()
	{
		System.out.println( "getElapsedDuration" );
		Interval instance = new Interval( 0, 1 );
		long expResult = 1L;
		long result = instance.getElapsedDuration();
		assertEquals( expResult, result );
	}

	/**
	 * Test of getEnd method, of class Interval.
	 */
	@Test
	public void testGetEnd()
	{
		System.out.println( "getEnd" );
		Interval instance = new Interval( 0, 1 );
		long expResult = 1L;
		long result = instance.getEnd();
		assertEquals( expResult, result );
	}

	/**
	 * Test of getStart method, of class Interval.
	 */
	@Test
	public void testGetStart()
	{
		System.out.println( "getStart" );
		Interval instance = new Interval( 1, 2 );
		long expResult = 1L;
		long result = instance.getStart();
		assertEquals( expResult, result );
	}
}
