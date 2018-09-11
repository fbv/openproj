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
package com.projity.pm.assignment;

import java.util.Collection;
import java.util.Iterator;

import com.projity.pm.costing.EarnedValueValues;

/**
 *
 */
public class TimeDistributedDataConsolidator {


	/* (non-Javadoc)
	 * @see com.projity.pm.costing.EarnedValueValues#acwp(long, long)
	 */
	public static double acwp(long start, long end, Collection collection) {
		double result = 0.0;
		Iterator i = collection.iterator();
		while (i.hasNext()) {
			result += ((EarnedValueValues)i.next()).acwp(start,end);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.costing.EarnedValueValues#bac(long, long)
	 */
	public static double bac(long start, long end, Collection collection){
		double result = 0.0;
		Iterator i = collection.iterator();
		while (i.hasNext()) {
			result += ((EarnedValueValues)i.next()).bac(start,end);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.costing.EarnedValueValues#bcwp(long, long)
	 */
	public static double bcwp(long start, long end, Collection collection){
		double result = 0.0;
		Iterator i = collection.iterator();
		while (i.hasNext()) {
			result += ((EarnedValueValues)i.next()).bcwp(start,end);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.costing.EarnedValueValues#bcws(long, long)
	 */
	public static double bcws(long start, long end, Collection collection){
		double result = 0.0;
		Iterator i = collection.iterator();
		while (i.hasNext()) {
			result += ((EarnedValueValues)i.next()).bcws(start,end);
		}
		return result;
	}


	/* (non-Javadoc)
	 * @see com.projity.pm.HasTimeDistributedData.HasTimeDistributedData#cost(long, long)
	 */
	public static double cost(long start, long end, Collection collection){
		double result = 0.0;
		Iterator i = collection.iterator();
		while (i.hasNext()) {
			result += ((HasTimeDistributedData)i.next()).cost(start,end);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.HasTimeDistributedData.HasTimeDistributedData#actualCost(long, long)
	 */
	public static double actualCost(long start, long end, Collection collection){
		double result = 0.0;
		Iterator i = collection.iterator();
		while (i.hasNext()) {
			result += ((HasTimeDistributedData)i.next()).actualCost(start,end);
		}
		return result;
	}
	
	/* (non-Javadoc)
	 * @see com.projity.pm.HasTimeDistributedData.HasTimeDistributedData#actualFixedCost(long, long)
	 */
	public static double actualFixedCost(long start, long end, Collection collection){
		double result = 0.0;
		Iterator i = collection.iterator();
		while (i.hasNext()) {
			result += ((HasTimeDistributedData)i.next()).actualFixedCost(start,end);
		}
		return result;
	}	
	
	/* (non-Javadoc)
	 * @see com.projity.pm.HasTimeDistributedData.HasTimeDistributedData#fixedCost(long, long)
	 */
	public static double fixedCost(
		long _start, 
		long _end, 
		Collection<? extends HasTimeDistributedData> _collection )
	{
		double result = 0.0;
		Iterator<? extends HasTimeDistributedData> itor = _collection.iterator();
		while (itor.hasNext() == true) 
		{
			result += itor.next().fixedCost( _start, _end );
		}
		return result;
	}

	public static long work(
		long _start, 
		long _end, 
		Collection<? extends HasTimeDistributedData> _collection, 
		boolean _laborOnly )
	{
		long result = 0;
		Iterator<? extends HasTimeDistributedData> itor = _collection.iterator();
		while (itor.hasNext() == true) 
		{
			final HasTimeDistributedData data = itor.next();
			if ((_laborOnly == true) 
			 && (data.isLabor() == false))
			{
				continue;
			}
			
			result += data.work( _start, _end );
		}
		
		return result;
	}

	public static long actualWork(long start, long end, Collection collection, boolean laborOnly){
		long result = 0;
		Iterator i = collection.iterator();
		HasTimeDistributedData data;
		while (i.hasNext()) {
			data = (HasTimeDistributedData)i.next();
			if (laborOnly && !data.isLabor())
				continue;
			result += data.actualWork(start,end);
		}
		return result;
	}
	

	public static long remainingWork(long start, long end, Collection collection, boolean laborOnly){
		long result = 0;
		Iterator i = collection.iterator();
		HasTimeDistributedData data;
		while (i.hasNext()) {
			data = (HasTimeDistributedData)i.next();
			if (laborOnly && !data.isLabor())
				continue;
			result += data.remainingWork(start,end);
		}
		return result;
	}
		
	/* (non-Javadoc)
	 * @see com.projity.pm.HasTimeDistributedData.HasTimeDistributedData#actualWork(long, long)
	 */
	public static double baselineCost(long start, long end, Collection collection){
		long result = 0;
		Iterator i = collection.iterator();
		while (i.hasNext()) {
			result += ((HasTimeDistributedData)i.next()).baselineCost(start,end);
		}
		return result;
	}
		
	public static long baselineWork(long start, long end, Collection collection, boolean laborOnly){
		long result = 0;
		Iterator i = collection.iterator();
		HasTimeDistributedData data;
		while (i.hasNext()) {
			data = (HasTimeDistributedData)i.next();
			if (laborOnly && !data.isLabor())
				continue;
			result += data.baselineWork(start,end);
		}
		return result;
	}
		
	

}