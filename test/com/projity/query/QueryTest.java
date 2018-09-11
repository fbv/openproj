/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.projity.query;

import org.openproj.util.CountAction;
import org.openproj.util.FromClause;
import org.openproj.util.Query;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.commons.collections.Closure;
import org.apache.commons.collections.Predicate;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Paul
 */
public class QueryTest
{
	
	public QueryTest()
	{
	}

	@BeforeClass
	public static void setUpClass()
		throws Exception
	{
		myStringsArray.add( "first" );
		myStringsArray.add( "second" );
		myStringsArray.add( "third" );
		myStringsArray.add( "fourth" );
		myStringsArray.add( "fifth" );
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
	 * 
	 */
	@Test
	public void testStringTest()
	{
		System.out.println( "string test" );

		Query query = Query.selectFrom( new FromStrings() )
			.where( new StringPredicate( "second" ) )
			.action( new Closure<String>()
			{
				@Override
				public void execute(
					final String _subject )
				{
					assertTrue( _subject.equals( "second" ) );
				}
			} )
			.execute();
	}
	
	/**
	 * 
	 */
	@Test
	public void testMultipleWheres()
	{
		System.out.println( "multiple wheres" );

		Query query = Query.selectFrom( new FromStrings() )
			.where( new StringPredicate( "second" ), 
				new StringPredicate( "third" ) )
			.action( new Closure<String>()
			{
				@Override
				public void execute(
					final String _subject )
				{
					assertTrue( _subject.equals( "second" ) || _subject.equals( "third" ) );
				}
			} )
			.execute();
	}
	
	/**
	 * 
	 */
	@Test
	public void testCountAction()
	{
		System.out.print( "count action: " );

		final CountAction countAction = new CountAction<String>();
		Query query = Query.selectFrom( new FromStrings() )
			.where( new StringPredicate( "second" ), new StringPredicate( "third" ) )
			.action( countAction )
			.execute();
		
		System.out.println( countAction.getCount() );
		assertTrue( countAction.getCount() == 2 );
	}

	/**
	 * 
	 */
	@Test
	public void testCountAllAction()
	{
		System.out.print( "count all action: " );

		final CountAction countAction = new CountAction<String>();
		Query query = Query.selectFrom( new FromStrings() )
			.action( countAction )
			.execute();
		
		System.out.println( countAction.getCount() );
		assertTrue( countAction.getCount() == 5 );
	}

	static class FromStrings 
		implements FromClause<String>
	{
		@Override
		public final boolean atEnd()
		{
			return !myItor.hasNext();
		}

		@Override
		public final String next()
		{
			return myItor.next();
		}

		private final Iterator<String> myItor = myStringsArray.iterator();
	}
				
	static class StringPredicate
		implements Predicate<String>
	{
		public StringPredicate(
			final String _test )
		{
			myTest = _test;
		}
		
		@Override
		public boolean evaluate(
			final String _testee )
		{
			return _testee.equals( myTest );
		}
		
		private final String myTest;
	}
				
	private static final ArrayList<String> myStringsArray = new ArrayList<String>();
}
