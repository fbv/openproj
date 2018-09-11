/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.projity.pm.costing;

import com.projity.datatype.Rate;
import com.projity.field.FieldContext;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author pcorbett
 */
public class CostRateTablesTest
{
	
	public CostRateTablesTest()
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
	 * Test of clone method, of class CostRateTables.
	 */
	@Test
	public void testClone()
	{
		System.out.println( "clone" );
		CostRateTables instance = new CostRateTables();
		Object expResult = instance;
		Object result = instance.clone();
		assertEquals( expResult, result );
	}

	/**
	 * Test of deserialize method, of class CostRateTables.
	 */
	@Test
	public void testDeserialize()
		throws Exception
	{
		System.out.println( "deserialize" );
		ObjectInputStream _stream = null;
		CostRateTables expResult = null;
		CostRateTables result = CostRateTables.deserialize( _stream );
		assertEquals( expResult, result );
		// TODO review the generated test code and remove the default call to fail.
		fail( "The test case is a prototype." );
	}

	/**
	 * Test of deserializeCompact method, of class CostRateTables.
	 */
	@Test
	public void testDeserializeCompact()
		throws Exception
	{
		System.out.println( "deserializeCompact" );
		ObjectInputStream _stream = null;
		CostRateTables expResult = null;
		CostRateTables result = CostRateTables.deserializeCompact( _stream );
		assertEquals( expResult, result );
		// TODO review the generated test code and remove the default call to fail.
		fail( "The test case is a prototype." );
	}

	/**
	 * Test of fieldHideOvertimeRate method, of class CostRateTables.
	 */
	@Test
	public void testFieldHideOvertimeRate()
	{
		System.out.println( "fieldHideOvertimeRate" );
		FieldContext _fieldContext = null;
		CostRateTables instance = new CostRateTables();
		boolean expResult = false;
		boolean result = instance.fieldHideOvertimeRate( _fieldContext );
		assertEquals( expResult, result );
		// TODO review the generated test code and remove the default call to fail.
		fail( "The test case is a prototype." );
	}

	/**
	 * Test of getCostPerUse method, of class CostRateTables.
	 */
	@Test
	public void testGetCostPerUse()
	{
		System.out.println( "getCostPerUse" );
		CostRateTables instance = new CostRateTables();
		double expResult = 0.0;
		double result = instance.getCostPerUse();
		assertEquals( expResult, result, 0.0 );
		// TODO review the generated test code and remove the default call to fail.
		fail( "The test case is a prototype." );
	}

	/**
	 * Test of getCostRateTable method, of class CostRateTables.
	 */
	@Test
	public void testGetCostRateTable()
	{
		System.out.println( "getCostRateTable" );
		int _index = 0;
		CostRateTables instance = new CostRateTables();
		CostRateTable expResult = null;
		CostRateTable result = instance.getCostRateTable( _index );
		assertEquals( expResult, result );
		// TODO review the generated test code and remove the default call to fail.
		fail( "The test case is a prototype." );
	}

	/**
	 * Test of getEffectiveDate method, of class CostRateTables.
	 */
	@Test
	public void testGetEffectiveDate()
	{
		System.out.println( "getEffectiveDate" );
		CostRateTables instance = new CostRateTables();
		long expResult = 0L;
		long result = instance.getEffectiveDate();
		assertEquals( expResult, result );
		// TODO review the generated test code and remove the default call to fail.
		fail( "The test case is a prototype." );
	}

	/**
	 * Test of getOvertimeRate method, of class CostRateTables.
	 */
	@Test
	public void testGetOvertimeRate()
	{
		System.out.println( "getOvertimeRate" );
		CostRateTables instance = new CostRateTables();
		Rate expResult = null;
		Rate result = instance.getOvertimeRate();
		assertEquals( expResult, result );
		// TODO review the generated test code and remove the default call to fail.
		fail( "The test case is a prototype." );
	}

	/**
	 * Test of getStandardRate method, of class CostRateTables.
	 */
	@Test
	public void testGetStandardRate()
	{
		System.out.println( "getStandardRate" );
		CostRateTables instance = new CostRateTables();
		Rate expResult = null;
		Rate result = instance.getStandardRate();
		assertEquals( expResult, result );
		// TODO review the generated test code and remove the default call to fail.
		fail( "The test case is a prototype." );
	}

	/**
	 * Test of isReadOnlyEffectiveDate method, of class CostRateTables.
	 */
	@Test
	public void testIsReadOnlyEffectiveDate()
	{
		System.out.println( "isReadOnlyEffectiveDate" );
		FieldContext _fieldContext = null;
		CostRateTables instance = new CostRateTables();
		boolean expResult = false;
		boolean result = instance.isReadOnlyEffectiveDate( _fieldContext );
		assertEquals( expResult, result );
		// TODO review the generated test code and remove the default call to fail.
		fail( "The test case is a prototype." );
	}

	/**
	 * Test of serialize method, of class CostRateTables.
	 */
	@Test
	public void testSerialize()
		throws Exception
	{
		System.out.println( "serialize" );
		ObjectOutputStream _stream = null;
		CostRateTables instance = new CostRateTables();
		instance.serialize( _stream );
		// TODO review the generated test code and remove the default call to fail.
		fail( "The test case is a prototype." );
	}

	/**
	 * Test of serializeCompact method, of class CostRateTables.
	 */
	@Test
	public void testSerializeCompact()
		throws Exception
	{
		System.out.println( "serializeCompact" );
		ObjectOutputStream _stream = null;
		CostRateTables instance = new CostRateTables();
		instance.serializeCompact( _stream );
		// TODO review the generated test code and remove the default call to fail.
		fail( "The test case is a prototype." );
	}

	/**
	 * Test of setCostPerUse method, of class CostRateTables.
	 */
	@Test
	public void testSetCostPerUse()
	{
		System.out.println( "setCostPerUse" );
		double _costPerUse = 0.0;
		CostRateTables instance = new CostRateTables();
		instance.setCostPerUse( _costPerUse );
		// TODO review the generated test code and remove the default call to fail.
		fail( "The test case is a prototype." );
	}

	/**
	 * Test of setCostRateTable method, of class CostRateTables.
	 */
	@Test
	public void testSetCostRateTable()
	{
		System.out.println( "setCostRateTable" );
		int _index = 0;
		CostRateTable _table = null;
		CostRateTables instance = new CostRateTables();
		instance.setCostRateTable( _index, _table );
		// TODO review the generated test code and remove the default call to fail.
		fail( "The test case is a prototype." );
	}

	/**
	 * Test of setEffectiveDate method, of class CostRateTables.
	 */
	@Test
	public void testSetEffectiveDate()
		throws Exception
	{
		System.out.println( "setEffectiveDate" );
		long _effectiveDate = 0L;
		CostRateTables instance = new CostRateTables();
		instance.setEffectiveDate( _effectiveDate );
		// TODO review the generated test code and remove the default call to fail.
		fail( "The test case is a prototype." );
	}

	/**
	 * Test of setOvertimeRate method, of class CostRateTables.
	 */
	@Test
	public void testSetOvertimeRate()
	{
		System.out.println( "setOvertimeRate" );
		Rate _overtimeRate = null;
		CostRateTables instance = new CostRateTables();
		instance.setOvertimeRate( _overtimeRate );
		// TODO review the generated test code and remove the default call to fail.
		fail( "The test case is a prototype." );
	}

	/**
	 * Test of setStandardRate method, of class CostRateTables.
	 */
	@Test
	public void testSetStandardRate()
	{
		System.out.println( "setStandardRate" );
		Rate standardRate = null;
		CostRateTables instance = new CostRateTables();
		instance.setStandardRate( standardRate );
		// TODO review the generated test code and remove the default call to fail.
		fail( "The test case is a prototype." );
	}

	/**
	 * Test of getName method, of class CostRateTables.
	 */
	@Test
	public void testGetName()
	{
		System.out.println( "getName" );
		int _index = 0;
		CostRateTables instance = new CostRateTables();
		String expResult = "";
		String result = instance.getName( _index );
		assertEquals( expResult, result );
		// TODO review the generated test code and remove the default call to fail.
		fail( "The test case is a prototype." );
	}
}
