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
public class MutableIntervalTest
{
	
	public MutableIntervalTest()
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
	 * Test of setEnd method, of class MutableInterval.
	 */
	@Test
	public void testSetEnd()
	{
		System.out.println( "setEnd" );
		long _end = 1L;
		MutableInterval instance = new MutableInterval( 0, 0 );
		instance.setEnd( _end );

		long expResult = 1L;
		long result = instance.getEnd();
		assertEquals( expResult, result );
	}

	/**
	 * Test of setStart method, of class MutableInterval.
	 */
	@Test
	public void testSetStart()
	{
		System.out.println( "setStart" );
		long _start = 1L;
		MutableInterval instance = new MutableInterval( 0, 0 );
		instance.setStart( _start );

		long expResult = 1L;
		long result = instance.getStart();
		assertEquals( expResult, result );
	}
}
