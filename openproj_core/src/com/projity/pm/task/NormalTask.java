package com.projity.pm.task;

import com.projity.configuration.Settings;
import com.projity.field.CustomFieldsImpl;
import com.projity.field.FieldContext;
import com.projity.pm.calendar.WorkCalendar;
import com.projity.pm.criticalpath.TaskSchedule;
import com.projity.pm.key.HasKeyImpl;
import com.projity.pm.snapshot.SnapshotContainer;
import com.projity.server.data.DataObject;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import org.openproj.domain.costing.ExpenseType;


/* This class has been retired. It only still exists to support older versions of serialization.
 * 
 * See: org.openproj.domain.task.Task
 * 
 */
public class NormalTask
	extends Task
	implements DataObject
{
	public NormalTask()
	{
		super();
	}

	private void readObject( 
		final ObjectInputStream _stream )
		throws IOException, ClassNotFoundException
	{
		_stream.defaultReadObject();

		myNewTask = new org.openproj.domain.model.task.Task();

		hasKey = HasKeyImpl.deserialize( _stream, myNewTask );
		myNewTask.setHasKey( hasKey );
		
		/*
		 * com.projity.pm.task.NormalTask attributes
		 */
		myNewTask.setEstimated( estimated );
		myNewTask.setPriority( priority );
		myNewTask.setWorkCalendar( workCalendar );

		/*
		 * com.projity.pm.task.Task attirbutes
		 */
		// protected String notes = "";
		myNewTask.setNotes( notes );
		
		// protected String wbs = "";
		myNewTask.setWbs( wbs );
		
		// protected boolean markTaskAsMilestone = false;
		myNewTask.setMarkTaskAsMilestone( markTaskAsMilestone );
		
		// protected int earnedValueMethod = EarnedValueMethodType.PERCENT_COMPLETE;
		myNewTask.setEarnedValueMethod( earnedValueMethod );

		// protected int constraintType = ConstraintType.ASAP;
		myNewTask.setContraintTypeOnly( constraintType );
		
		// protected long deadline = 0;
		myNewTask.setDeadline( deadline );
		
		// protected int expenseType = ExpenseType.NONE;
		myNewTask.setExpenseType( ExpenseType.values()[ expenseType ] );
		
		// protected long actualStart = 0;
		myNewTask.setActualStartNoEvent( actualStart );
		
		// protected long levelingDelay = 0L;
		myNewTask.setLevelingDelay( levelingDelay );
		
		// protected long windowEarlyFinish = 0;
		myNewTask.setWindowEarlyFinish( windowEarlyFinish );
		
		// protected long windowEarlyStart = 0;
		myNewTask.setWindowEarlyStart( windowEarlyStart );
		
		// protected long windowLateFinish = 0;
		myNewTask.setWindowLateFinish( windowLateFinish );
		
		// protected long windowLateStart = 0;
		myNewTask.setWindowLateStart( windowLateStart );
		
		// private double physicalPercentComplete = 0.0;
		myNewTask.setPhysicalPercentComplete( physicalPercentComplete );

		
		customFields = CustomFieldsImpl.deserialize( _stream );
		myNewTask.setCustomFields( customFields );
		

		if (version < 1)
		{
			myCurrentSchedule = TaskSchedule.deserialize( _stream );

		}
		else
		{
			snapshots = new SnapshotContainer( Settings.numBaselines() );

			int sCount = _stream.readInt();

			for (int i = 0; i < sCount; i++)
			{
				int snapshotId = _stream.readInt();
				TaskSnapshot snapshot = TaskSnapshot.deserialize( _stream, version );
				myNewTask.setSnapshot( Integer.valueOf( snapshotId ), snapshot );
			}
		}

		if (version < 1)
		{
			myNewTask.initializeTransientTaskObjects();
		}
		else
		{
			myNewTask.initializeTransientTaskObjectsAfterDeserialization();
		}

		//	    barClosureInstance = new BarClosure();
		//	    This shouldn't be called -hk 4/feb/05
		//	    initializeDates();
		version = DEFAULT_VERSION;
	}
	
	private Object readResolve()
		throws ObjectStreamException
	{
		return myNewTask;
	}

	static final long serialVersionUID = 273898992929L;
	private static short DEFAULT_VERSION = 2;

	private transient org.openproj.domain.model.task.Task myNewTask;

	boolean estimated = true;
	int priority = 500;

	private WorkCalendar workCalendar = null;
	private short version = DEFAULT_VERSION;

	@Override
	public String getName()
	{
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	@Override
	public long getUniqueId()
	{
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	@Override
	public boolean isDirty()
	{
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	@Override
	public void setDirty( boolean _dirty )
	{
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	@Override
	public void setName( String _name )
	{
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	@Override
	public void setUniqueId( long _id )
	{
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	@Override
	public String getName( FieldContext _context )
	{
		throw new UnsupportedOperationException( "Not supported yet." );
	}
}
