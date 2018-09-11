package com.projity.pm.task;

public class ProjectPermission
{
	public void denyAll()
	{
		read = false;
		write = false;
		saveBaseline = false;
		breakLock = false;
		deleteActuals = false;
	}

	public boolean isBreakLock()
	{
		return breakLock;
	}

	public boolean isDeleteActuals()
	{
		return deleteActuals;
	}

	public boolean isRead()
	{
		return read;
	}

	public boolean isSaveBaseline()
	{
		return saveBaseline;
	}

	public boolean isWrite()
	{
		return write;
	}

	public void setBreakLock( 
		boolean breakLock )
	{
		this.breakLock = breakLock;
	}

	public void setDeleteActuals( 
		boolean deleteActuals )
	{
		this.deleteActuals = deleteActuals;
	}

	public void setRead( 
		boolean read )
	{
		this.read = read;
	}

	public void setSaveBaseline( 
		boolean saveBaseline )
	{
		this.saveBaseline = saveBaseline;
	}

	public void setWrite( 
		boolean write )
	{
		this.write = write;
	}

	boolean breakLock = true;
	boolean deleteActuals = true;
	boolean read = true;
	boolean saveBaseline = true;
	boolean write = true;
}
