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
package com.projity.field;

import com.projity.grouping.functors.NodeFieldList;

import com.projity.pm.time.HasStartAndEnd;

import com.projity.util.DateTime;

/**
 * This class holds context specific information necessary for interacting with field data.
 */
public class FieldContext
	implements HasStartAndEnd
{
	/**
	 * 
	 * @param _context
	 * @return 
	 */
	public static long end( 
		final FieldContext _context )
	{
		if (_context == null)
		{
			return DEFAULT_END;
		}

		return _context.getEnd();
	}

	/**
	 * @return
	 */
	@Override
	public final long getEnd()
	{
		if (myInterval == null)
		{
			return DEFAULT_END;
		}

		return myInterval.getEnd();
	}

	/**
	 * @return Returns the interval.
	 */
	public final HasStartAndEnd getInterval()
	{
		return myInterval;
	}

	/**
	 * 
	 * @return 
	 */
	public static FieldContext getNoDirtyInstance()
	{
		if (myNoDirtyInstance == null)
		{
			myNoDirtyInstance = new FieldContext();
			myNoDirtyInstance.setNoDirty( true );
		}

		return myNoDirtyInstance;
	}

	/**
	 * 
	 * @return 
	 */
	public static FieldContext getNoUpdateInstance()
	{
		if (myNoUpdateInstance == null)
		{
			myNoUpdateInstance = new FieldContext();
			myNoUpdateInstance.setNoUpdate( true );
		}

		return myNoUpdateInstance;
	}

	/**
	 * 
	 * @return 
	 */
	public static FieldContext getScriptingInstance()
	{
		if (myScriptingInstance == null)
		{
			myScriptingInstance = new FieldContext();
			myScriptingInstance.setScripting( true );
		}

		return myScriptingInstance;
	}

	/**
	 * 
	 * @return 
	 */
	public static FieldContext getSoapInstance()
	{
		if (mySoapInstance == null)
		{
			mySoapInstance = new FieldContext();
			mySoapInstance.setSoap( true );
			mySoapInstance.setForceValue( true );
		}

		return mySoapInstance;
	}

	/**
	 * @return
	 */
	@Override
	public final long getStart()
	{
		if (myInterval == null)
		{
			return DEFAULT_START;
		}

		return myInterval.getStart();
	}

	/**
	 * 
	 * @param _context
	 * @return 
	 */
	public static boolean hasInterval( 
		final FieldContext _context )
	{
		if (_context == null)
		{
			return false;
		}

		if (_context.getInterval() == null)
		{
			return false;
		}

		return true;
	}

	/**
	 * 
	 * @return 
	 */
	public final boolean isCompact()
	{
		return myCompact;
	}

	/**
	 * 
	 * @return 
	 */
	public final boolean isConvertResult()
	{
		return myConvertResult;
	}

	/**
	 * 
	 * @param _context
	 * @return 
	 */
	public static boolean isForceValue( 
		final FieldContext _context )
	{
		if (_context == null)
		{
			return false;
		}

		return _context.isForceValue();
	}

	/**
	 * 
	 * @return 
	 */
	public final boolean isForceValue()
	{
		return myForceValue;
	}

	/**
	 * 
	 * @param _start
	 * @param _end
	 * @return 
	 */
	public static boolean isFullRange( 
		final long _start,
		final long _end )
	{
		return (_start == DEFAULT_START) && (_end == DEFAULT_END);
	}

	/**
	 * 
	 * @return 
	 */
	public final boolean isHideNullValues()
	{
		return myHideNullValues;
	}

	/**
	 * @return Returns the leftAssociation.
	 */
	public final boolean isLeftAssociation()
	{
		return myLeftAssociationn;
	}

	/**
	 * 
	 * @return 
	 */
	public final boolean isNoDirty()
	{
		return myNoDirty;
	}

	/**
	 * @return Returns noUpdate flag which indicates that the field should be set but no update message sent
	 */
	public final boolean isNoUpdate()
	{
		return myNoUpdate;
	}

	/**
	 * 
	 * @param _context
	 * @return 
	 */
	public static boolean isNoUpdate( 
		final FieldContext _context )
	{
		if (_context == null)
		{
			return false;
		}

		return _context.isNoUpdate();
	}

	/**
	 * 
	 * @return 
	 */
	public final boolean isNullIfUnchanged()
	{
		return myNullIfUnchanged;
	}

	/**
	 * @return Returns parseOnly flag which indicates that the text should be parsed, errors thrown if necessary, 
	 * but never set values
	 */
	public final boolean isParseOnly()
	{
		return myParseOnly;
	}

	/**
	 * 
	 * @param _context
	 * @return 
	 */
	public static boolean isParseOnly( 
		final FieldContext _context )
	{
		if (_context == null)
		{
			return false;
		}

		return _context.isParseOnly();
	}

	/**
	 * 
	 * @return 
	 */
	public final boolean isRound()
	{
		return myRound;
	}

	/**
	 * 
	 * @param _start
	 * @param _end
	 * @return 
	 */
	public static boolean isScalar( 
		final long _start,
		final long _end )
	{ 
		// see if range is all time
		return (_start == DEFAULT_START) && (_end == DEFAULT_END);
	}

	/**
	 * 
	 * @param _context
	 * @return 
	 */
	public static boolean isScripting( 
		final FieldContext _context )
	{
		if (_context == null)
		{
			return false;
		}

		return _context.isScripting();
	}

	/**
	 * 
	 * @return 
	 */
	public final boolean isScripting()
	{
		return myScripting;
	}

	/**
	 * 
	 * @return 
	 */
	public final boolean isSoap()
	{
		return mySoap;
	}

	/**
	 * 
	 * @param _compact 
	 */
	public final void setCompact( 
		final boolean _compact )
	{
		myCompact = _compact;
	}

	/**
	 * 
	 * @param _convertResult 
	 */
	public final void setConvertResult( 
		final boolean _convertResult )
	{
		myConvertResult = _convertResult;
	}

	/**
	 * 
	 * @param _forceValue 
	 */
	public final void setForceValue( 
		final boolean _forceValue )
	{
		myForceValue = _forceValue;
	}

	/**
	 * 
	 * @param _hideNullValues 
	 */
	public final void setHideNullValues( 
		final boolean _hideNullValues )
	{
		myHideNullValues = _hideNullValues;
	}

	/**
	 * @param _interval The interval to set.
	 */
	public final void setInterval( 
		final HasStartAndEnd _interval )
	{
		myInterval = _interval;
	}

	/**
	 * @param _leftAssociation The leftAssociation to set.
	 */
	public final void setLeftAssociation( 
		final boolean _leftAssociation )
	{
		myLeftAssociationn = _leftAssociation;
	}

	/**
	 * 
	 * @param _noDirty 
	 */
	public final void setNoDirty( 
		final boolean _noDirty )
	{
		myNoDirty = _noDirty;
	}

	/**
	 * @param _noUpdate The noUpdate to set.
	 */
	public final void setNoUpdate( 
		final boolean _noUpdate )
	{
		myNoUpdate = _noUpdate;
	}

	/**
	 * 
	 * @param _nullIfUnchanged 
	 */
	public final void setNullIfUnchanged( 
		final boolean _nullIfUnchanged )
	{
		myNullIfUnchanged = _nullIfUnchanged;
	}

	/**
	 * @param _parseOnly The parseOnly to set.
	 */
	public final void setParseOnly( 
		final boolean _parseOnly )
	{
		myParseOnly = _parseOnly;
	}

	/**
	 * 
	 * @param _round 
	 */
	public final void setRound( 
		final boolean _round )
	{
		myRound = _round;
	}

	/**
	 * 
	 * @param _scripting 
	 */
	public final void setScripting( 
		final boolean _scripting )
	{
		myScripting = _scripting;
	}

	/**
	 * 
	 * @param _soap 
	 */
	public final void setSoap( 
		final boolean _soap )
	{
		mySoap = _soap;
	}

	/**
	 * 
	 * @param _context
	 * @return 
	 */
	public static long start( 
		final FieldContext _context )
	{
		if (_context == null)
		{
			return DEFAULT_START;
		}

		return _context.getStart();
	}

	public static final long DEFAULT_START = 0;
	public static final long DEFAULT_END = DateTime.getMaxDate().getTime();
	public static final FieldContext DEFAULT_CONTEXT = new FieldContext();

	private static FieldContext myNoUpdateInstance = null;
	private static FieldContext myScriptingInstance = null;
	private static FieldContext mySoapInstance = null;
	private static FieldContext myNoDirtyInstance = null;

	private HasStartAndEnd myInterval = null;
	private boolean myCompact = false;
	private boolean myConvertResult = true;
	private boolean myForceValue = false;
	private boolean myHideNullValues = false;
	private boolean myLeftAssociationn = true;
	private boolean myNoDirty = false;
	private boolean myNoUpdate;
	private boolean myNullIfUnchanged = false;
	private boolean myParseOnly;
	private boolean myRound = false; //for start date when pasting from a string clipboard
	private boolean myScripting = false;
	private boolean mySoap = false;
}
