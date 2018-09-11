/*
 */
package org.openproj.util;

import org.apache.commons.collections.Closure;

/**
 *
 * @author Paul Corbett
 */
public class CountAction<T>
	implements Closure<T>
{
	@Override
	public void execute(
		final T X )
	{
		myCount++;
	}

	public int getCount()
	{
		return myCount;
	}

	private int myCount = 0;
}

