package com.projity.pm.task;

import com.projity.field.CustomFieldsImpl;
import com.projity.pm.costing.EarnedValueMethodType;
import org.openproj.domain.costing.ExpenseType;
import com.projity.pm.criticalpath.TaskSchedule;
import com.projity.pm.key.HasKeyImpl;
import com.projity.pm.scheduling.ConstraintType;
import com.projity.pm.snapshot.Snapshottable;
import java.io.Serializable;

/* This class has been retired. It only still exists to support older versions of serialization.
 * 
 * See: org.openproj.domain.task.Task
 * 
 */
public abstract class Task
	implements Serializable
{
	public Task()
	{
	}

	static final long serialVersionUID = 786665335611L;
	protected transient CustomFieldsImpl customFields = new CustomFieldsImpl();
	protected transient HasKeyImpl hasKey;
	protected transient TaskSchedule myCurrentSchedule = null;
	protected transient Snapshottable snapshots;

	protected String notes = "";
	protected String wbs = "";
	protected boolean markTaskAsMilestone = false;
	protected int earnedValueMethod = EarnedValueMethodType.PERCENT_COMPLETE;
	protected int constraintType = ConstraintType.ASAP;
	protected long deadline = 0;
	protected int expenseType = 0;
	protected long actualStart = 0;
	protected long levelingDelay = 0L;
	protected long windowEarlyFinish = 0;
	protected long windowEarlyStart = 0;
	protected long windowLateFinish = 0;
	protected long windowLateStart = 0;

	protected double physicalPercentComplete = 0.0;
}

