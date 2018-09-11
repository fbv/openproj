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

Attribution Information: Attribution Copyright Notice: Copyright ? 2006, 2007
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
package com.projity.graphic.configuration;

import com.projity.configuration.NamedItem;

import com.projity.strings.Messages;

import org.apache.commons.collections.Closure;
import org.apache.commons.digester.Digester;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;


/**
 * Styles of bars on the gantt chart.  Holds a collection of bar formats.
 */
public final class BarStyles
	implements NamedItem
{
	public BarStyles()
	{
	}

	public static void addDigesterEvents( 
		Digester _digester )
	{
		// main properties of bar
		_digester.addFactoryCreate( "*/bar/styles", "com.projity.graphic.configuration.BarStylesFactory" );
		_digester.addSetProperties( "*/bar/styles" );
		_digester.addSetNext( "*/bar/styles", "add", "com.projity.configuration.NamedItem" );

		// start section
		_digester.addObjectCreate( "*/bar/styles/style", "com.projity.graphic.configuration.BarStyle" );
		_digester.addSetProperties( "*/bar/styles/style" );
		_digester.addSetNext( "*/bar/styles/style", "addStyle", "com.projity.graphic.configuration.BarStyle" );
	}

	public final void addStyle( 
		final BarStyle _style )
	{
		_style.setBelongsTo( this );
		_style.build(); // set references
		myRows.add( _style );
	}

	/**
	 * Applies a closure to all bars which should be displayed.  The renderer is called back
	 * with the BarFormat to apply for bars which meet their display conditions.
	 * @param ganttable - A task, resource, assignment... whatever can be displayed in gantt
	 * @param action - Callback - The callback parameters are BarFormats
	 */
	public final void apply( 
		final Object _ganttable,
		final Closure<BarFormat> _action )
	{
		apply( _ganttable, _action, false, false, false, false );
	}

	public final void apply( 
		final Object _ganttable,
		final Closure<BarFormat> _action,
		final boolean _link,
		final boolean _annotation,
		final boolean _calendar,
		final boolean _horizontalGrid )
	{
		final Iterator<BarStyle> itor = myRows.iterator();
		while (itor.hasNext() == true)
		{
			final BarStyle row = itor.next();

			if ((row.isLink() == _link) 
			 && (row.isHorizontalGrid() == _horizontalGrid) 
			 && (row.isAnnotation() == _annotation) 
			 && (row.isCalendar() == _calendar) 
			 && (row.evaluate( _ganttable ) == true))
			{
				// see if meets filter
				_action.execute( row.getBarFormat() );
			}
		}
	}

	@Override
	public final String getCategory()
	{
		return category;
	}

	/**
	 * @return Returns the id.
	 */
	public final String getId()
	{
		return myId;
	}

	public final int getMaxZoom()
	{
		initZoomX();
		initZoomY();

		if ((myZoomRatioX == null) || (myZoomRatioY == null))
		{
			return 0;
		}

		return Math.min( myZoomRatioX.length - myDefaultZoomIndexX - 1, myZoomRatioY.length - myDefaultZoomIndexY - 1 );
	}

	public final int getMinZoom()
	{
		initZoomX();
		initZoomY();

		if ((myZoomRatioX == null) || (myZoomRatioY == null))
		{
			return 0;
		}

		return Math.min( -myDefaultZoomIndexX, -myDefaultZoomIndexY );
	}

	/**
	 * @return Returns the name.
	 */
	@Override
	public final String getName()
	{
		return myName;
	}

	public final double getRatioX( 
		final int _zoom,
		final boolean _in )
	{
		initZoomX();

		if (myZoomX == null)
		{
			return 1.0;
		}

		final int index = (myDefaultZoomIndexX + _zoom) - ((_in == true)
			? 0
			: 1);

		if ((index < 0) || (index >= myZoomRatioX.length))
		{
			return 1.0;
		}

		return (_in == true)
			? myZoomRatioX[ index ]
			: (1.0 / myZoomRatioX[ index ]);
	}

	public final double getRatioY( 
		final int _zoom,
		final boolean _in )
	{
		initZoomY();

		if (myZoomY == null)
		{
			return 1.0;
		}

		final int index = (myDefaultZoomIndexY + _zoom) - ((_in == true)
			? 0
			: 1);

		if ((index < 0) || (index >= myZoomRatioY.length))
		{
			return 1.0;
		}

		return (_in == true)
			? myZoomRatioY[ index ]
			: (1.0 / myZoomRatioY[ index ]);
	}

	/**
	 * @return Returns the rows.
	 */
	public final ArrayList<BarStyle> getRows()
	{
		return myRows;
	}

	public final String getZoomX()
	{
		return myZoomX;
	}

	public final String getZoomY()
	{
		return myZoomY;
	}

	protected final void initZoomX()
	{
		if (myZoomRatioX == null)
		{
			if (myZoomX == null)
			{
				return;
			}

			final StringTokenizer st = new StringTokenizer( myZoomX, ",;:|" );
			myZoomRatioX = new double[ st.countTokens() ];

			int index = 0;

			while (st.hasMoreTokens() == true)
			{
				final String s = st.nextToken();

				if ("*".equals( s ) == true)
				{
					myDefaultZoomIndexX = index;
				}
				else
				{
					myZoomRatioX[ index++ ] = Double.parseDouble( s );
				}
			}
		}
	}

	protected final void initZoomY()
	{
		if (myZoomRatioY == null)
		{
			if (myZoomY == null)
			{
				return;
			}

			final StringTokenizer st = new StringTokenizer( myZoomY, ",;:|" );
			myZoomRatioY = new double[ st.countTokens() ];

			int index = 0;

			while (st.hasMoreTokens() == true)
			{
				String s = st.nextToken();

				if ("*".equals( s ) == true)
				{
					myDefaultZoomIndexY = index;
				}
				else
				{
					myZoomRatioY[ index++ ] = Double.parseDouble( s );
				}
			}
		}
	}

	/** Assign the <i>it</i> attribute.
	 * 
	 * @param _id used as the source of the assignment
	 */
	public final void setId( 
		final String _id )
	{
		myId = _id;
		setName( Messages.getString( _id ) );
	}

	/** Assign the <i>name</i> attribute.
	 * 
	 * @param _name used as the source of the assignment
	 */
	public final void setName( 
		final String _name )
	{
		myName = _name;
	}

	/** Assign the <i>zoom X</i> attribute.
	 * 
	 * @param _zoomX used as the source of the assignment
	 */
	public final void setZoomX( 
		final String _zoomX )
	{
		myZoomX = _zoomX;
	}

	/** Assign the <i>zoom Y</i> attribute.
	 * 
	 * @param _zoomY used as the source of the assignment
	 */
	public final void setZoomY( 
		final String _zoomY )
	{
		myZoomY = _zoomY;
	}

	//	static Log log = LogFactory.getLog(BarStyles.class);
	public static final String category = "BarStylesCategory";
	
	private ArrayList<BarStyle> myRows = new ArrayList<BarStyle>();
	private String myId = null;
	private String myName = null;
	private String myZoomX = null;
	private String myZoomY = null;
	private double[] myZoomRatioX = null;
	private double[] myZoomRatioY = null;
	private int myDefaultZoomIndexX;
	private int myDefaultZoomIndexY;
}
