/*
 */
package org.openproj.util;

/**
 *
 * @author Paul Corbett
 */
public interface FromClause<T>
{
	/**
	 * 
	 * @return 
	 */
	boolean atEnd();
	
	/**
	 * 
	 * @return 
	 */
	T next();
}
