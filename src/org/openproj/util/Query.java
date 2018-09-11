/*
 */
package org.openproj.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import org.apache.commons.collections.Closure;
import org.apache.commons.collections.Predicate;

/**
 *
 * @author Paul Corbett
 */
public class Query<T>
{
	public Query()
	{
		// nothing to do
	}
	
	public Query(
		final FromClause<T>... _fromClauses )
	{
		myFromClauses.addAll( Arrays.asList( _fromClauses ) );
	}
	
	public Closure<T> action()
	{
		return myAction;
	}
	
	public Query<T> action(
		final Closure<T> _action )
	{
		myAction = _action;
		return this;
	}
	
	public Query<T> execute()
	{
		final Iterator<FromClause<T>> fromItor = myFromClauses.iterator();
		while (fromItor.hasNext() == true)
		{
			final FromClause<T> from = fromItor.next();
			while (from.atEnd() == false)
			{
				final T testee = from.next();
				
				// Are there any where clause?
				if (myWhereClauses.isEmpty() == true)
				{
					// No, test passes
					myAction.execute( testee );
				}
				else
				{
					final Iterator<Predicate<T>> whereItor = myWhereClauses.iterator();
					while (whereItor.hasNext() == true)
					{
						if (whereItor.next().evaluate( testee ) == true)
						{
							myAction.execute( testee );
						}
					}
				}
			}
		}
		
		return this;
	}
	
	public static <T> Query<T> selectFrom(
		final FromClause<T> _fromClauses )
	{
		return new Query<T>( _fromClauses );
	}
	
	public Query<T> where(
		final Predicate<T>... _whereClauses )
	{
		myWhereClauses.addAll( Arrays.asList( _whereClauses ) );
		return this;
	}
	
	private Closure<T> myAction = null;
	private final ArrayList<FromClause<T>> myFromClauses = new ArrayList<FromClause<T>>();
	private final ArrayList<Predicate<T>> myWhereClauses = new ArrayList<Predicate<T>>();
}
