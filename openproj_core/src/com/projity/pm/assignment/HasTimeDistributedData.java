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
package com.projity.pm.assignment;

import com.projity.algorithm.ReverseQuery;

import com.projity.pm.calendar.WorkCalendar;
import com.projity.pm.time.HasStartAndEnd;

import org.apache.commons.collections.Closure;

import java.util.Collection;


/**
 * Interface for classes having time distributed data
 */
public interface HasTimeDistributedData
{
	public double actualCost( long _start, long _end );

	public double actualFixedCost( long _start, long _end );

	public long actualWork( long _start, long _end );

	public double baselineCost( long _start, long _end );

	public long baselineWork( long _start, long _end );

	public void buildReverseQuery( ReverseQuery _reverseQuery );

	public Collection childrenToRollup();

	public double cost( long _start, long _end );

	/** Calculate the fixed cost for the task given its accrual type and percent complete
	 * 
	 * @param _start
	 * @param _end
	 * @return 
	 */
	double fixedCost( long _start, long _end );

	/**
	 *
	 * @param _visitor
	 * @param _mergeWorking
	 * @param _workCalendar
	 */
	public void forEachWorkingInterval( Closure<HasStartAndEnd> _visitor, boolean _mergeWorking, WorkCalendar _workCalendar );

	public boolean isLabor();

	public long remainingWork( long _start, long _end );

	public long work( long _start, long _end );

	public static final long NO_VALUE_LONG = 0L;
	public static final double NO_VALUE_DOUBLE = 0.0D;
	public static TimeDistributedConstants[] histogramTypes = 
	{
		TimeDistributedConstants.SELECTED,
		TimeDistributedConstants.THIS_PROJECT,
		TimeDistributedConstants.AVAILABILITY
	};
	public static TimeDistributedConstants[] reverseHistogramTypes = 
	{
		TimeDistributedConstants.THIS_PROJECT,
		TimeDistributedConstants.SELECTED,
		TimeDistributedConstants.AVAILABILITY
	};
	public static TimeDistributedConstants[] serverHistogramTypes = 
	{
		TimeDistributedConstants.SELECTED,
		TimeDistributedConstants.THIS_PROJECT,
		TimeDistributedConstants.AVAILABILITY
	};
	public static TimeDistributedConstants[] serverReverseHistogramTypes = 
	{

		//OTHER_PROJECTS
		//,
		TimeDistributedConstants.THIS_PROJECT,
		TimeDistributedConstants.SELECTED,
		TimeDistributedConstants.AVAILABILITY
	};
	public static int tracesCount = 3;
	public static int serverTracesCount = 3;
	public static TimeDistributedConstants[] workTypes = 
	{
		TimeDistributedConstants.WORK,
		TimeDistributedConstants.ACTUAL_WORK,
		TimeDistributedConstants.REMAINING_WORK,
		TimeDistributedConstants.BASELINE_WORK,
		TimeDistributedConstants.BASELINE1_WORK,
		TimeDistributedConstants.BASELINE2_WORK,
		TimeDistributedConstants.BASELINE3_WORK,
		TimeDistributedConstants.BASELINE4_WORK,
		TimeDistributedConstants.BASELINE5_WORK,
		TimeDistributedConstants.BASELINE6_WORK,
		TimeDistributedConstants.BASELINE7_WORK,
		TimeDistributedConstants.BASELINE8_WORK,
		TimeDistributedConstants.BASELINE9_WORK,
		TimeDistributedConstants.BASELINE10_WORK
	};
	public static TimeDistributedConstants[] costTypes = 
	{
		TimeDistributedConstants.COST,
		TimeDistributedConstants.ACTUAL_COST,
		TimeDistributedConstants.FIXED_COST,
		TimeDistributedConstants.ACTUAL_FIXED_COST,
		TimeDistributedConstants.REMAINING_COST,
		TimeDistributedConstants.BASELINE_COST,
		TimeDistributedConstants.ACWP,
		TimeDistributedConstants.BCWP,
		TimeDistributedConstants.BCWS,
		TimeDistributedConstants.BASELINE1_COST,
		TimeDistributedConstants.BASELINE2_COST,
		TimeDistributedConstants.BASELINE3_COST,
		TimeDistributedConstants.BASELINE4_COST,
		TimeDistributedConstants.BASELINE5_COST,
		TimeDistributedConstants.BASELINE6_COST,
		TimeDistributedConstants.BASELINE7_COST,
		TimeDistributedConstants.BASELINE8_COST,
		TimeDistributedConstants.BASELINE9_COST,
		TimeDistributedConstants.BASELINE10_COST,
	};
	public static TimeDistributedConstants[] baselineWorkTypes = 
	{
		TimeDistributedConstants.BASELINE_WORK,
		TimeDistributedConstants.BASELINE1_WORK,
		TimeDistributedConstants.BASELINE2_WORK,
		TimeDistributedConstants.BASELINE3_WORK,
		TimeDistributedConstants.BASELINE4_WORK,
		TimeDistributedConstants.BASELINE5_WORK,
		TimeDistributedConstants.BASELINE6_WORK,
		TimeDistributedConstants.BASELINE7_WORK,
		TimeDistributedConstants.BASELINE8_WORK,
		TimeDistributedConstants.BASELINE9_WORK,
		TimeDistributedConstants.BASELINE10_WORK
	};
}
