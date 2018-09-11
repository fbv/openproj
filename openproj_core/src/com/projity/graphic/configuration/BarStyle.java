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

import com.projity.configuration.Dictionary;

import com.projity.field.InvalidFormulaException;

import com.projity.scripting.Formula;
import com.projity.scripting.FormulaFactory;

import com.projity.strings.Messages;

import org.apache.commons.collections.Predicate;


/**
 *
 */
public class BarStyle
	implements Predicate
{
	public BarStyle()
	{
	}

	@Override
	public boolean evaluate( 
		Object object )
	{
		if (!active)
		{
			return false;
		}

		try
		{
			if (formula == null)
			{
				return true;
			}

			return ((Boolean)formula.evaluate( object )).booleanValue();
		}
		catch (InvalidFormulaException e)
		{
//			log.warn("Error evaluating formula in BarMappingRow" + name);
			return false;
		}
	}

	/**
	 * @return Returns the barFormat.
	 */
	public BarFormat getBarFormat()
	{
		return barFormat;
	}

	/**
	 * @return Returns the barFormat.
	 */
	public String getBarFormatName()
	{
		return barFormatName;
	}

	public String getFormulaClass()
	{
		return formulaClass;
	}

	/**
	 * @return Returns the formula.
	 */
	public String getFormulaText()
	{
		return formulaText;
	}

	/**
	 * @return Returns the id.
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @return Returns the type.
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * @return Returns the active.
	 */
	public boolean isActive()
	{
		return active;
	}

	public boolean isAnnotation()
	{
		return annotation;
	}

	public boolean isCalendar()
	{
		return calendar;
	}

	public boolean isHorizontalGrid()
	{
		return horizontalGrid;
	}

	public boolean isLink()
	{
		return link;
	}

	/**
	 * @param active The active to set.
	 */
	public void setActive( 
		boolean active )
	{
		this.active = active;
	}

	public void setAnnotation( 
		boolean annotation )
	{
		this.annotation = annotation;
	}

	public void setBelongsTo( 
		BarStyles styles )
	{
		this.belongsTo = styles;
	}

	public void setCalendar( 
		boolean calendar )
	{
		this.calendar = calendar;
	}

	/**
	 * @param barFormat The barFormat to set.
	 */
	public void setFormatId( 
		String formatId )
	{
		this.formatId = formatId;

		String name = Messages.getString( formatId );
		barFormat = (BarFormat)Dictionary.get( BarFormat.category, name );
	}

	public void setFormulaClass( 
		String formulaClass )
	{
		this.formulaClass = formulaClass;
	}

	/**
	 * @param formula The formula to set.
	 */
	public void setFormulaText( 
		String formulaText )
	{
		this.formulaText = formulaText;
	}

	public void setHorizontalGrid( 
		boolean horizontalGrid )
	{
		this.horizontalGrid = horizontalGrid;
	}

	/**
	 * @param id The id to set.
	 */
	public void setId( 
		String id )
	{
		this.id = id;
		setName( Messages.getString( id ) );
	}

	public void setLink( 
		boolean link )
	{
		this.link = link;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName( 
		String name )
	{
		this.name = name;
	}

	/**
	 * @param type The type to set.
	 */
	public void setType( 
		String type )
	{
		this.type = type;
	}

	void build()
	{
		if ((formulaText == null) && (formulaClass == null))
		{
			formulaClass = "com.projity.scripting.formulas.TrueFormula";
		}

		if (formulaClass != null)
		{
			formula = FormulaFactory.addNormal( formulaClass, name );
		}
		else if (formulaText != null)
		{
			formula = FormulaFactory.addScripted( FORMULA_PREFIX + belongsTo.getName(), name, type, formulaText );
		}

		String idName = Messages.getString( formatId );
		barFormat = (BarFormat)Dictionary.get( BarFormat.category, idName );
	}

	//	static Log log = LogFactory.getLog(BarStyle.class);
	public static final String FORMULA_PREFIX = "BarStyle";

	BarStyles belongsTo;
	String barFormatName = null;
	String formatId = null;
	String formulaClass = null;
	String formulaText = null;
	String id = null;
	String name = null;
	String type = null; // type is actually only used to construct formula
	boolean active = true;
	boolean annotation = false;
	boolean calendar = false;
	boolean horizontalGrid = false;
	boolean link = false;
	private BarFormat barFormat = null;
	private Formula formula = null;
}
