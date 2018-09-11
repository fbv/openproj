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
package com.projity.pm.assignment.functor;

import com.projity.algorithm.CollectionIntervalGenerator;

import com.projity.pm.assignment.Assignment;
import com.projity.pm.assignment.contour.AbstractContourBucket;
import com.projity.pm.assignment.contour.ContourBucketIntervalGenerator;
import com.projity.pm.calendar.WorkCalendar;
import com.projity.pm.costing.CostRate;
import com.projity.pm.costing.CurrencyRate;
import com.projity.pm.time.HasStartAndEnd;


/**
 * A functor which calculates cost (regular, overtime, fixed, total)
 */
public class CostFunctor
	extends AssignmentFieldOvertimeFunctor
{
	private CostFunctor( 
		Assignment assignment,
		WorkCalendar workCalendar,
		ContourBucketIntervalGenerator contourBucketIntervalGenerator,
		double overtimeUnits,
		CollectionIntervalGenerator costRateGenerator,
		CollectionIntervalGenerator currencyRateGenerator,
		long fixedCostDate,
		boolean proratedCost )
	{
		super(assignment, workCalendar, contourBucketIntervalGenerator, overtimeUnits);
		this.costRateGenerator = costRateGenerator;
		this.currencyRateGenerator = currencyRateGenerator;
		this.fixedCostDate = fixedCostDate;
		this.proratedCost = proratedCost;
	}

	@Override
	public void execute( 
		final HasStartAndEnd _interval )
	{
		AbstractContourBucket bucket = myContourBucketIntervalGenerator.getBucket();

		if (bucket != null)
		{
			CostRate costRate = (CostRate)costRateGenerator.current();
			double bucketUnits = bucket.getEffectiveUnits( myAssignment.getUnits() );

			if (bucketUnits != 0.0)
			{ // there are never values if there is no normal cost.
			  // calculate regular and overtime

				long bucketDuration = myWorkCalendar.compare( _interval.getEnd(), _interval.getStart(), false );

				//When we handle overhead, we need to have another interval generator which keeps overhead in sorted order
				// The bucket duration should be multiplied by 1 - overhead.  Code also needs to exist in workFunctor.  maybe others too
				// double overhead = overheadIntervalGenerator.current();
				// bucketDuration *= (1.0 - overhead);
				regularWork += (bucketUnits * bucketDuration);
				overtimeWork += (myOvertimeUnits * bucketDuration);
				work = regularWork + overtimeWork;

				// JGao - 9/11/2009 Changes for round problem with cost value. The reason is that the work for any bucket
				// is in milli seconds and can have round problems. Since cost rate is always in per hour value, we round
				// the bucket work to hours before times the cost rate.
				double bucketOvertime = costRate.getOvertimeRate().getValue();
				double bucketRegular = costRate.getStandardRate().getValue();
				double currencyRate = 1.0D;

				if (currencyRateGenerator != null)
				{
					currencyRate = ((CurrencyRate)currencyRateGenerator.current()).getRate();
					bucketOvertime *= currencyRate;
					bucketRegular *= currencyRate;
				}

				if (myAssignment.isTemporal())
				{ // for work resources or time based material
					bucketRegular *= WorkCalendar.MILLIS_IN_HOUR;
					bucketRegular *= (Math.round( ((bucketUnits * bucketDuration) / WorkCalendar.MILLIS_IN_HOUR) * 100 ) / 100.0);
					bucketOvertime *= (Math.round( ((myOvertimeUnits * bucketDuration) / WorkCalendar.MILLIS_IN_HOUR) * 100 ) / 100.0);
				}
				else
				{
					bucketRegular *= bucketUnits;
					bucketOvertime *= myOvertimeUnits;
				}

				myOvertimeValue += bucketOvertime;
				myRegularValue += bucketRegular;
				myValue += (bucketOvertime + bucketRegular);

				// Below is fixed cost processing.
				double costPerUse = costRate.getCostPerUse();

				if (costPerUse != 0.0D)
				{
					double fraction = 1.0D; // fraction of fixed cost to use - only relevant if prorated

					if (proratedCost)
					{ // prorated across duration

						long assignmentDuration = myAssignment.getDuration();

						if (assignmentDuration != 0)
						{
							fraction = ((double)bucketDuration) / myAssignment.getDuration();
						}
					}
					else
					{ // at a certain date - start or end
					  // make sure that the start or end date falls within the interval

						if ((_interval.getStart() > fixedCostDate) || (_interval.getEnd() < fixedCostDate))
						{
							return; // not in range
						}
					}

					// Notice how the cost per use is multiplied by the assignment units, which itself is the peak units used.
					double bucketFixed = fraction * costPerUse * myAssignment.getUnits() * currencyRate;

					fixedValue += bucketFixed;
					myValue += bucketFixed;
				}
			}
		}
	}

	/**
	 * @return Returns the fixedValue.
	 */
	public double getFixedValue()
	{
		return fixedValue;
	}

	public static CostFunctor getInstance( 
		Assignment assignment,
		WorkCalendar workCalendar,
		ContourBucketIntervalGenerator contourBucketIntervalGenerator,
		double overtimeUnits,
		com.projity.algorithm.CollectionIntervalGenerator costRateGenerator,
		CollectionIntervalGenerator currencyRateGenerator,
		long fixedCostDate,
		boolean proratedCost )
	{
		return new CostFunctor(assignment, workCalendar, contourBucketIntervalGenerator, overtimeUnits, costRateGenerator,
			currencyRateGenerator, fixedCostDate, proratedCost);
	}

	public final double getOvertimeWork()
	{
		return overtimeWork;
	}

	public final double getRegularWork()
	{
		return regularWork;
	}

	public final double getWork()
	{
		return work;
	}

	/* (non-Javadoc)
	 * @see com.projity.algorithm.CalculationVisitor#reset()
	 */
	@Override
	public void initialize()
	{
		super.initialize();
		fixedValue = 0.0D;
		regularWork = 0.0D;
		overtimeWork = 0.0D;
		work = 0.0D;
	}

	@Override
	public String toString()
	{
		return " total " + myValue + "  regular " + myRegularValue + "  overtime " + myOvertimeValue + "  fixed " + fixedValue;
	}

	CollectionIntervalGenerator costRateGenerator;
	CollectionIntervalGenerator currencyRateGenerator;
	boolean proratedCost;
	double fixedValue = 0.0D;
	double overtimeWork = 0.0D;
	double regularWork = 0.0D;
	double work = 0.0D;
	long fixedCostDate;
}
