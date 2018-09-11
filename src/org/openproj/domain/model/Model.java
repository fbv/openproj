/*
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */
package org.openproj.domain.model;

import com.jtsmythe.event.EventEmitter;
import com.projity.pm.task.Project;
import com.projity.strings.Messages;
import org.openproj.domain.model.task.Task;

/** Domain model
 *
 * @author psc1952
 */
public class Model
{
	public org.openproj.domain.task.Task createTask()
	{
		return new org.openproj.domain.model.task.Task();
	}
	
	public org.openproj.domain.task.Task createTask( 
		final Project _project )
	{
		return new org.openproj.domain.model.task.Task( _project );
	}
	
	public org.openproj.domain.task.Task createTask( 
		final boolean _isLocal,
		final Project _project )
	{
		return new org.openproj.domain.model.task.Task( _isLocal, _project );
	}
	
	public static Model getInstance()
	{
		if (myModel == null)
		{
			myModel = new Model();
		}
		
		return myModel;
	}
	
	public static org.openproj.domain.task.Task getUnassignedTaskInstance()
	{
		if (UNASSIGNED == null)
		{
			UNASSIGNED = new Task( true );
			UNASSIGNED.setName( Messages.getString( "Text.Unassigned" ) );
		}

		return UNASSIGNED;
	}

	
	private static Model myModel = null;
	private static Task UNASSIGNED = null;
}
