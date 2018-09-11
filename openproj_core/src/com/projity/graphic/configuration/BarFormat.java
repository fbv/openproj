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

import com.projity.configuration.Configuration;
import com.projity.configuration.FieldDictionary;
import com.projity.configuration.NamedItem;

import com.projity.field.Field;

import com.projity.functor.ScheduleIntervalGenerator;

import com.projity.strings.Messages;

import org.apache.commons.digester.Digester;


/**
 *
 */
public class BarFormat
	implements NamedItem
{
	public BarFormat()
	{
	}

	/**
	 * Add digester events for the bar as well as the three sections.
	 * The XML root is * /bar/shape
	 *
	 */
	public static void addDigesterEvents( 
		final Digester _digester )
	{
		// main properties of bar
		_digester.addObjectCreate( "*/bar/format", "com.projity.graphic.configuration.BarFormat" );
		_digester.addSetProperties( "*/bar/format" );
		_digester.addSetNext( "*/bar/format", "add", "com.projity.configuration.NamedItem" ); // add to dictionary

		// start section
		_digester.addObjectCreate( "*/bar/format/start", "com.projity.graphic.configuration.TexturedShape" );
		_digester.addSetProperties( "*/bar/format/start" );
		_digester.addSetNext( "*/bar/format/start", "setStart", "com.projity.graphic.configuration.TexturedShape" );

		//middle section
		_digester.addObjectCreate( "*/bar/format/middle", "com.projity.graphic.configuration.TexturedShape" );
		_digester.addSetProperties( "*/bar/format/middle" );
		_digester.addSetNext( "*/bar/format/middle", "setMiddle", "com.projity.graphic.configuration.TexturedShape" );

		//end section
		_digester.addObjectCreate( "*/bar/format/end", "com.projity.graphic.configuration.TexturedShape" );
		_digester.addSetProperties( "*/bar/format/end" );
		_digester.addSetNext( "*/bar/format/end", "setEnd", "com.projity.graphic.configuration.TexturedShape" );

		//form section
		_digester.addObjectCreate( "*/bar/format/form", "com.projity.graphic.configuration.FormFormat" );
		_digester.addSetProperties( "*/bar/format/form" );
		_digester.addSetNext( "*/bar/format/form", "setForm", "com.projity.graphic.configuration.FormFormat" );

		FormFormat.addDigesterEvents( _digester );
	}

	@Override
	public String getCategory()
	{
		return category;
	}

	/**
	 * @return Returns the end.
	 */
	public final TexturedShape getEnd()
	{
		return myEndShape;
	}

	public final Field getField()
	{
		if ((myField == null) || (myField.getId() != myFieldId))
		{
			if (myFieldId == null)
			{
				myField = null;
			}

			myField = FieldDictionary.getInstance().getFieldFromId( myFieldId );
		}

		return myField;
	}

	public final String getFieldId()
	{
		return myFieldId;
	}

	public final FormFormat getForm()
	{
		return myForm;
	}

	/**
	 * @return Returns the from.
	 */
	public final String getFrom()
	{
		return myFrom;
	}

	/**
	 * @return Returns the category.
	 */

	/**
	 * @return Returns the fromField.
	 */
	public final Field getFromField()
	{
		return myFromField;
	}

	public final String getId()
	{
		return myId;
	}

	public final String getIntervalGenerator()
	{
		return myIntervalGeneratorName;
	}

	public final int getLayer()
	{
		return myLayer;
	}

	/**
	 * @return Returns the middle.
	 */
	public final TexturedShape getMiddle()
	{
		return myMiddleShape;
	}

	@Override
	public final String getName()
	{
		return myName;
	}

	public final int getNumberOfSections()
	{
		int count = 0;

		if (myStartShape != null)
		{
			count++;
		}

		if (myMiddleShape != null)
		{
			count++;
		}

		if (myEndShape != null)
		{
			count++;
		}

		return count;
	}

	/**
	 * @return Returns the line.
	 */
	public final int getRow()
	{
		return myRow;
	}

	public final ScheduleIntervalGenerator getScheduleIntervalGenerator()
	{
		if ((myIntervalGeneratorName != null) && (myScheduleIntervalGenerator == null))
		{
			try
			{
				myScheduleIntervalGenerator = (ScheduleIntervalGenerator)Class.forName( myIntervalGeneratorName ).newInstance();
			}
			catch (InstantiationException | IllegalAccessException | ClassNotFoundException e)
			{
				e.printStackTrace();
			}
		}

		return myScheduleIntervalGenerator;
	}

	/**
	 * @return Returns the start.
	 */
	public final TexturedShape getStart()
	{
		return myStartShape;
	}

	/**
	 * @return Returns the to.
	 */
	public final String getTo()
	{
		return myTo;
	}

	/**
	 * @return Returns the myToField.
	 */
	public final Field getToField()
	{
		return myToField;
	}

	public final boolean isMain()
	{ 
		//compatibily
		return myMain || (myIntervalGeneratorName != null);
	}

	/**
	 * @param _end The end to set.
	 */
	public final void setEnd( 
		final TexturedShape _end )
	{
		_end.build();
		myEndShape = _end;
	}

	public final void setFieldId( 
		final String _fieldId )
	{
		myFieldId = _fieldId;
		getField();
	}

	public final void setForm( 
		final FormFormat _form )
	{
		myForm = _form;
	}

	/**
	 * @param _from The from to set.
	 */
	public final void setFrom( 
		final String _from )
	{
		myFrom = _from;
		myFromField = Configuration.getFieldFromId( myFrom );
	}

	public final void setId( 
		final String _id )
	{
		myId = _id;
		setName( Messages.getString( myId ) );
	}

	public final void setIntervalGenerator( 
		final String _intervalGenerator )
	{
		myIntervalGeneratorName = _intervalGenerator;
	}

	public final void setLayer( 
		final int _layer )
	{
		myLayer = _layer;
	}

	public final void setMain( 
		final boolean _main )
	{
		myMain = _main;
	}

	/**
	 * @param _middle The middle to set.
	 */
	public final void setMiddle( 
		final TexturedShape _middle )
	{
		_middle.build();
		myMiddleShape = _middle;
	}

	/**
	 * @param _name The name to set.
	 */
	public final void setName( 
		final String _name )
	{
		myName = _name;
	}

	/**
	 * @param _line The line to set.
	 */
	public final void setRow( 
		final int _line )
	{
		myRow = _line;
	}

	/**
	 * @param _start The start to set.
	 */
	public final void setStart( 
		final TexturedShape _start )
	{
		_start.build();
		myStartShape = _start;
	}

	/**
	 * @param _to The to to set.
	 */
	public final void setTo( 
		final String _to )
	{
		myTo = _to;
		myToField = Configuration.getFieldFromId( _to );
	}

	public static final String category = "BarFormatCategory";
	public static final int MIN_FOREGROUND_LAYER = 1;
	public static final int MAX_FOREGROUND_LAYER = 499;
	public static final int MIN_LINK_LAYER = 500;
	public static final int MAX_LINK_LAYER = 999;
	public static final int MIN_BACKGROUND_LAYER = 1000;
	public static final int MAX_BACKGROUND_LAYER = 1499;

	private Field myField = null; //for annotations
	private Field myFromField = null;
	private Field myToField = null;
	private FormFormat myForm = null;
	private ScheduleIntervalGenerator myScheduleIntervalGenerator = null;
	private String myFieldId = null;
	private String myFrom;
	private String myId = null;
	private String myIntervalGeneratorName = null;
	private String myName = null;
	private String myTo;
	private TexturedShape myEndShape = null;
	private TexturedShape myMiddleShape = null;
	private TexturedShape myStartShape = null;
	private boolean myMain = false;
	private int myLayer = MIN_BACKGROUND_LAYER;
	private int myRow = 0;
}
