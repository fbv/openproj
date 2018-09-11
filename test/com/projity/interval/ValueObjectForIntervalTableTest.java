/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.projity.interval;

import com.projity.grouping.core.model.NodeModel;
import com.projity.grouping.core.model.NodeModelDataFactory;
import com.projity.undo.DataFactoryUndoController;
import com.projity.util.DateTime;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
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
public class ValueObjectForIntervalTableTest
{
	
	public ValueObjectForIntervalTableTest()
	{
	}

	@BeforeClass
	public static void setUpClass()
		throws Exception
	{
		myEmptyTable = new ValueObjectForIntervalTableImpl();
	}

	@AfterClass
	public static void tearDownClass()
		throws Exception
	{
		myEmptyTable = null;
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
	 * Test of the null constructor of class ValueObjectForIntervalTable.
	 */
	@Test
	public void testNullConstructor()
	{
		System.out.println( "null constructor" );
		assertEquals( myEmptyTable.getName(), "" );
		assertTrue( myEmptyTable.getValueObjects().isEmpty() == true );
	}

	/**
	 * Test of the name constructor of class ValueObjectForIntervalTable.
	 */
	@Test
	public void testNameConstructor()
	{
		System.out.println( "name constructor" );
		
		final String name = "a name";
		ValueObjectForIntervalTable instance = new ValueObjectForIntervalTableImpl( name );

		assertEquals( instance.getName(), name );
		assertTrue( instance.getValueObjects().isEmpty() == false );
		assertTrue( instance.getValueObjects().size() == 1 );
	}

	/**
	 * Test of the name and values constructor of class ValueObjectForIntervalTable.
	 */
	@Test
	public void testNameAndValuesConstructor()
	{
		System.out.println( "name and values constructor" );
		
		final String name = "a name";
		final ArrayList<ValueObjectForInterval> list = new ArrayList<>();
		list.add( new ValueObjectForIntervalTest.ValueObjectForIntervalImpl() );
			
		ValueObjectForIntervalTable instance = new ValueObjectForIntervalTableImpl( name, list );

		assertEquals( instance.getName(), name );
		assertTrue( instance.getValueObjects().isEmpty() == false );
		assertTrue( instance.getValueObjects().size() == 1 );
	}

	/**
	 * Test of add method, of class ValueObjectForIntervalTable.
	 */
	@Test
	public void testAdd()
	{
		System.out.println( "add" );
		long start = 10L;
		double value = 10.0;
		ValueObjectForIntervalTable instance = new ValueObjectForIntervalTableImpl();
		ValueObjectForInterval result = instance.add( start, value );
		assertTrue( result.getStart() == start );
		assertTrue( result.getValue() == value );
	}

	/**
	 * Test of addInterval method, of class ValueObjectForIntervalTable.
	 */
	@Test
	public void testAddInterval()
	{
		System.out.println( "addInterval" );
		long start = 20L;
		long finish = 30L;
		double value = 40.0;
		ValueObjectForIntervalTable instance = new ValueObjectForIntervalTableImpl();
		ValueObjectForInterval result = instance.addInterval( start, finish, value );
		assertEquals( start, result.getStart() );
		assertEquals( finish, result.getEnd() );
		assertTrue( result.getValue() == value );
	}

	/**
	 * Test of addUnvalidatedObject method, of class ValueObjectForIntervalTable.
	 */
	@Test
	public void testAddUnvalidatedObject()
	{
		System.out.println( "addUnvalidatedObject" );
		Object _object = null;
		NodeModel _nodeModel = null;
		Object _parent = null;
		ValueObjectForIntervalTable instance = new ValueObjectForIntervalTableImpl();
		instance.addUnvalidatedObject( _object, _nodeModel, _parent );
		// TODO review the generated test code and remove the default call to fail.
		fail( "The test case is a prototype." );
	}

	/**
	 * Test of adjustStart method, of class ValueObjectForIntervalTable.
	 */
	@Test
	public void testAdjustStart()
		throws Exception
	{
		System.out.println( "adjustStart" );
		long _newStart = 0L;
		ValueObjectForInterval _valueObject = null;
		ValueObjectForIntervalTable instance = new ValueObjectForIntervalTableImpl();
		instance.adjustStart( _newStart, _valueObject );
		// TODO review the generated test code and remove the default call to fail.
		fail( "The test case is a prototype." );
	}

	/**
	 * Test of clone method, of class ValueObjectForIntervalTable.
	 */
	@Test
	public void testClone()
	{
		System.out.println( "clone" );
		final ValueObjectForIntervalTable instance = new ValueObjectForIntervalTableImpl();
		final ValueObjectForIntervalTable clone = instance.clone();
		assertEquals( instance.getName(), clone.getName() );
	}

	/**
	 * Test of createUnvalidatedObject method, of class ValueObjectForIntervalTable.
	 */
	@Test
	public void testCreateUnvalidatedObject()
	{
		System.out.println( "createUnvalidatedObject" );
		NodeModel _nodeModel = null;
		Object _parent = null;
		ValueObjectForIntervalTable instance = new ValueObjectForIntervalTableImpl();
		Object expResult = null;
		Object result = instance.createUnvalidatedObject( _nodeModel, _parent );
		assertEquals( expResult, result );
		// TODO review the generated test code and remove the default call to fail.
		fail( "The test case is a prototype." );
	}

	/**
	 * Test of deserialize method, of class ValueObjectForIntervalTable.
	 */
//	@Test
//	public void testDeserialize()
//		throws Exception
//	{
//		System.out.println( "deserialize" );
//		ObjectInputStream _stream = null;
//		ValueObjectForIntervalTable _valueTable = null;
//		ValueObjectForIntervalTable expResult = null;
//		ValueObjectForIntervalTable result = ValueObjectForIntervalTable.deserialize( _stream, _valueTable );
//		assertEquals( expResult, result );
//		// TODO review the generated test code and remove the default call to fail.
//		fail( "The test case is a prototype." );
//	}

	/**
	 * Test of deserializeCompact method, of class ValueObjectForIntervalTable.
	 */
//	@Test
//	public void testDeserializeCompact()
//		throws Exception
//	{
//		System.out.println( "deserializeCompact" );
//		ObjectInputStream _stream = null;
//		ValueObjectForIntervalTable instance = new ValueObjectForIntervalTableImpl();
//		instance.deserializeCompact( _stream );
//		// TODO review the generated test code and remove the default call to fail.
//		fail( "The test case is a prototype." );
//	}

	/**
	 * Test of findActive method, of class ValueObjectForIntervalTable.
	 */
	@Test
	public void testFindActive()
	{
		System.out.println( "findActive" );
		long _date = 0L;
		ValueObjectForIntervalTable instance = new ValueObjectForIntervalTableImpl();
		ValueObjectForInterval expResult = null;
		ValueObjectForInterval result = instance.findActive( _date );
		assertEquals( expResult, result );
		// TODO review the generated test code and remove the default call to fail.
		fail( "The test case is a prototype." );
	}

	/**
	 * Test of findCurrent method, of class ValueObjectForIntervalTable.
	 */
	@Test
	public void testFindCurrent()
	{
		System.out.println( "findCurrent" );
		ValueObjectForIntervalTable instance = new ValueObjectForIntervalTableImpl();
		ValueObjectForInterval expResult = null;
		ValueObjectForInterval result = instance.findCurrent();
		assertEquals( expResult, result );
		// TODO review the generated test code and remove the default call to fail.
		fail( "The test case is a prototype." );
	}

	/**
	 * Test of getEnd method, of class ValueObjectForIntervalTable.
	 */
	@Test
	public void testGetEnd()
	{
		System.out.println( "getEnd" );
		ValueObjectForIntervalTable instance = new ValueObjectForIntervalTableImpl();
		long expResult = 0L;
		long result = instance.getEnd();
		assertEquals( expResult, result );
		
		instance.add( 0, 1.0D );
		expResult = DateTime.getMaxDate().getTime();
		result = instance.getEnd();
		assertEquals( expResult, result );
	}

	/**
	 * Test of getFactoryToUseForChildOfParent method, of class ValueObjectForIntervalTable.
	 */
	@Test
	public void testGetFactoryToUseForChildOfParent()
	{
		System.out.println( "getFactoryToUseForChildOfParent" );
		Object _impl = null;
		ValueObjectForIntervalTable instance = new ValueObjectForIntervalTableImpl();
		NodeModelDataFactory expResult = null;
		NodeModelDataFactory result = instance.getFactoryToUseForChildOfParent( _impl );
		assertEquals( expResult, result );
		// TODO review the generated test code and remove the default call to fail.
		fail( "The test case is a prototype." );
	}

	/**
	 * Test of getList method, of class ValueObjectForIntervalTable.
	 */
	@Test
	public void testGetList()
	{
		System.out.println( "getList" );
		ValueObjectForIntervalTable instance = new ValueObjectForIntervalTableImpl();
		List expResult = instance.getValueObjects();
		List result = instance.getList();
		assertTrue( expResult != result );
		assertEquals( expResult.size(), result.size() );
	}

	/**
	 * Test of getName method, of class ValueObjectForIntervalTable.
	 */
	@Test
	public void testGetName()
	{
		System.out.println( "getName" );
		ValueObjectForIntervalTable instance = new ValueObjectForIntervalTableImpl();
		String expResult = "";
		String result = instance.getName();
		assertEquals( expResult, result );
		
		expResult = "test";
		instance.setName( expResult );
		result = instance.getName();
		assertEquals( expResult, result );
	}

	/**
	 * Test of getStart method, of class ValueObjectForIntervalTable.
	 */
	@Test
	public void testGetStart()
	{
		System.out.println( "getStart" );
		ValueObjectForIntervalTable instance = new ValueObjectForIntervalTableImpl();
		long expResult = DateTime.getMaxDate().getTime();
		long result = instance.getStart();
		assertEquals( expResult, result );
	}

	/**
	 * Test of getValueObjects method, of class ValueObjectForIntervalTable.
	 */
	@Test
	public void testGetValueObjects()
	{
		System.out.println( "getValueObjects" );
		ValueObjectForIntervalTable instance = new ValueObjectForIntervalTableImpl();
		ArrayList result = instance.getValueObjects();
		boolean expResult = true;
		assertEquals( expResult, result.isEmpty() );
	}

	/**
	 * Test of newValueObject method, of class ValueObjectForIntervalTable.
	 */
	@Test
	public void testNewValueObject()
		throws Exception
	{
		System.out.println( "newValueObject" );

		final long startDate1 = 100000L;
		final double value1 = 100.0D;
		ValueObjectForIntervalTable instance = new ValueObjectForIntervalTableImpl();
		ValueObjectForInterval result = instance.newValueObject( startDate1, value1 );
		assertEquals( result.getStart(), startDate1 );
		assertEquals( result.getEnd(), DateTime.getMaxDate().getTime() );
		assertTrue( result.getValue() == value1 );
		assertTrue( instance.getList().size() == 1 );
		
		final long startDate2 = 100L;
		final double value2 = 10.0D;
		result = instance.newValueObject( startDate2, value2 );
		assertEquals( result.getStart(), startDate2 );
		assertEquals( result.getEnd(), startDate1 );
		assertTrue( result.getValue() == value2 );
		assertTrue( instance.getList().size() == 2 );
		
		final long startDate3 = 2000000L;
		final double value3 = 9.99D;
		result = instance.newValueObject( startDate3, value3 );
		assertEquals( result.getStart(), startDate3 );
		assertEquals( result.getEnd(), DateTime.getMaxDate().getTime() );
		assertTrue( result.getValue() == value3 );
		assertTrue( instance.getList().size() == 3 );
		
		Iterator<ValueObjectForInterval> itor = instance.getValueObjects().iterator();
		assertTrue( itor.hasNext() );
		final ValueObjectForInterval interval1 = itor.next();
		assertEquals( interval1.getStart(), startDate2 );
		assertEquals( interval1.getEnd(), startDate1 );
		assertTrue( interval1.getValue() == value2 );
		
		assertTrue( itor.hasNext() );
		final ValueObjectForInterval interval2 = itor.next();
		assertEquals( interval2.getStart(), startDate1 );
		assertEquals( interval2.getEnd(), startDate3 );
		assertTrue( interval2.getValue() == value1 );

		assertTrue( itor.hasNext() );
		final ValueObjectForInterval interval3 = itor.next();
		assertEquals( interval3.getStart(), startDate3 );
		assertEquals( interval3.getEnd(), DateTime.getMaxDate().getTime() );
		assertTrue( interval3.getValue() == value3 );
	}

	/**
	 * Test of remove method, of class ValueObjectForIntervalTable.
	 */
	@Test
	public void testRemove_5args()
	{
		System.out.println( "remove" );
		Object _toRemove = null;
		NodeModel _nodeModel = null;
		boolean _deep = false;
		boolean _undo = false;
		boolean _removeDependencies = false;
		ValueObjectForIntervalTable instance = new ValueObjectForIntervalTableImpl();
		instance.remove( _toRemove, _nodeModel, _deep, _undo, _removeDependencies );
		// TODO review the generated test code and remove the default call to fail.
		fail( "The test case is a prototype." );
	}

	/**
	 * Test of remove method, of class ValueObjectForIntervalTable.
	 */
	@Test
	public void testRemove_ValueObjectForInterval()
		throws Exception
	{
		System.out.println( "remove" );
		ValueObjectForInterval _removeMe = null;
		ValueObjectForIntervalTable instance = new ValueObjectForIntervalTableImpl();
		instance.remove( _removeMe );
		// TODO review the generated test code and remove the default call to fail.
		fail( "The test case is a prototype." );
	}

	/**
	 * Test of rollbackUnvalidated method, of class ValueObjectForIntervalTable.
	 */
	@Test
	public void testRollbackUnvalidated()
	{
		System.out.println( "rollbackUnvalidated" );
		NodeModel _nodeModel = null;
		Object _object = null;
		ValueObjectForIntervalTable instance = new ValueObjectForIntervalTableImpl();
		instance.rollbackUnvalidated( _nodeModel, _object );
		// TODO review the generated test code and remove the default call to fail.
		fail( "The test case is a prototype." );
	}

	/**
	 * Test of serialize method, of class ValueObjectForIntervalTable.
	 */
//	@Test
//	public void testSerialize()
//		throws Exception
//	{
//		System.out.println( "serialize" );
//		ObjectOutputStream _stream = null;
//		ValueObjectForIntervalTable instance = new ValueObjectForIntervalTableImpl();
//		instance.serialize( _stream );
//		// TODO review the generated test code and remove the default call to fail.
//		fail( "The test case is a prototype." );
//	}

	/**
	 * Test of serializeCompact method, of class ValueObjectForIntervalTable.
	 */
//	@Test
//	public void testSerializeCompact()
//		throws Exception
//	{
//		System.out.println( "serializeCompact" );
//		ObjectOutputStream _stream = null;
//		ValueObjectForIntervalTable instance = new ValueObjectForIntervalTableImpl();
//		instance.serializeCompact( _stream );
//		// TODO review the generated test code and remove the default call to fail.
//		fail( "The test case is a prototype." );
//	}

	/**
	 * Test of setName method, of class ValueObjectForIntervalTable.
	 */
	@Test
	public void testSetName()
	{
		System.out.println( "set name" );
		final String name = "test";
		ValueObjectForIntervalTable instance = new ValueObjectForIntervalTableImpl( name );
		final String result = instance.getName();
		assertEquals( name, result );
	}

	/**
	 * Test of validateObject method, of class ValueObjectForIntervalTable.
	 */
	@Test
	public void testValidateObject()
	{
		System.out.println( "validateObject" );
		Object _newlyCreated = null;
		NodeModel _nodeModel = null;
		Object _eventSource = null;
		Object _hierarchyInfo = null;
		boolean _isNew = false;
		ValueObjectForIntervalTable instance = new ValueObjectForIntervalTableImpl();
		instance.validateObject( _newlyCreated, _nodeModel, _eventSource, _hierarchyInfo, _isNew );
		// TODO review the generated test code and remove the default call to fail.
		fail( "The test case is a prototype." );
	}

	private static ValueObjectForIntervalTableImpl myEmptyTable = null;
	
	public static class ValueObjectForIntervalTableImpl
		extends ValueObjectForIntervalTable
	{
		public ValueObjectForIntervalTableImpl()
		{
			super();
		}

		public ValueObjectForIntervalTableImpl( 
			final String _name,
			final ArrayList<ValueObjectForInterval> _valueObjects )
		{ 
			super( _name, _valueObjects );
		}

		public ValueObjectForIntervalTableImpl( 
			final String _name )
		{
			super( _name );
		}
		
		public ValueObjectForIntervalTableImpl(
			final ValueObjectForIntervalTableImpl _source )
		{
			super( _source );
		}

		@Override
		public ValueObjectForIntervalTableImpl clone()
		{
			return new ValueObjectForIntervalTableImpl( this );
		}
		
		@Override
		public ValueObjectForInterval createValueObject()
		{
			return new ValueObjectForIntervalTest.ValueObjectForIntervalImpl();
		}

		@Override
		public ValueObjectForInterval createValueObject( 
			final long _date,
			final double _value )
		{
			return new ValueObjectForIntervalTest.ValueObjectForIntervalImpl( _date, _value );
		}

		@Override
		public NodeModel createNodeModel()
		{
			throw new UnsupportedOperationException( "Not supported yet." );
		}

		@Override
		public NodeModel createNodeModel( Collection _collection )
		{
			throw new UnsupportedOperationException( "Not supported yet." );
		}

		@Override
		public DataFactoryUndoController getUndoController()
		{
			throw new UnsupportedOperationException( "Not supported yet." );
		}

		@Override
		public void initOutline( NodeModel _nodeModel )
		{
			throw new UnsupportedOperationException( "Not supported yet." );
		}

//		@Override
//		public boolean isGroupDirty()
//		{
//			throw new UnsupportedOperationException( "Not supported yet." );
//		}

		@Override
		public void setGroupDirty( boolean _value )
		{
			throw new UnsupportedOperationException( "Not supported yet." );
		}

		@Override
		public void setUndoController( DataFactoryUndoController _undoController )
		{
			throw new UnsupportedOperationException( "Not supported yet." );
		}
	}
}
