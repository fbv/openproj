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

Attribution Information: Attribution Copyright Notice: Copyright � 2006, 2007
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
package com.projity.pm.scheduling;

import com.projity.datatype.Duration;
import com.projity.pm.assignment.Allocation;
import com.projity.pm.assignment.Assignment;
import com.projity.strings.Messages;

/**
 * @stereotype strategy
 * Work = Units * Duration
 * In a... If you revise units... If you revise duration... If you revise work...
 * Fixed-units task Duration is recalculated. Work is recalculated. Duration is recalculated.
 */

public class FixedUnits implements SchedulingRule {
	public String toString() {
		return Messages.getString("FixedUnits.FixedUnits"); //$NON-NLS-1$
	}

	/*
	 * If you revise units, Duration is recalculated. They are inversely proportional
	 */
	public void adjustRemainingUnits(Allocation allocation, double newRemainingUnits, double oldRemainingUnits, boolean doChildren, boolean conserveTotalUnits) {
		Assignment result;
		if (newRemainingUnits == 0.0) {// treat degenerate case.  Assigning 0 to units makes the task a milestone, but units becomes 1.0
			allocation.adjustRemainingDuration(0, doChildren);
			allocation.adjustRemainingUnits(1.0, oldRemainingUnits, doChildren, false);
		} else {
			if (oldRemainingUnits == 0.0) {// special case
				allocation.adjustRemainingUnits(1.0, oldRemainingUnits, false, false);
			} else {
				allocation.adjustRemainingWork(oldRemainingUnits / newRemainingUnits, doChildren);
			}
		}
	}

	/*
	 * If you revise duration, Work is recalculated
	 */
	public void adjustRemainingDuration(Allocation allocation, long newRemainingDuration, boolean doChildren) {
		allocation.adjustRemainingDuration(newRemainingDuration, doChildren);
	}

	/*
	 * If you revise work, Duration is recalculated
	 */
	public void adjustRemainingWork(Allocation allocation, long newRemainingWork, boolean doChildren) {
		long oldRemainingWork = allocation.getRemainingWork();
		adjustRemainingWork(allocation,newRemainingWork,oldRemainingWork,doChildren);
	}

	public void adjustRemainingWork(Allocation allocation, long newRemainingWork, long oldRemainingWork, boolean doChildren) {
		long newDuration;
		double remainingUnits = allocation.getRemainingUnits();

		if (oldRemainingWork == 0) { // degenerate case
			if (remainingUnits == 0)
				newDuration = (long)newRemainingWork;
			else
				newDuration = (long) (newRemainingWork / allocation.getRemainingUnits());
		} else {
			newRemainingWork = Duration.millis(newRemainingWork);
			newDuration = (long) (allocation.getRemainingDuration() * ((double)newRemainingWork) / oldRemainingWork);
		}
		allocation.adjustRemainingDuration(newDuration, doChildren);
	}


	private FixedUnits() {}
	private static FixedUnits instance = null;

	public static FixedUnits getInstance() {
		if (instance == null)
			instance = new FixedUnits();
		return instance;
	}

}
