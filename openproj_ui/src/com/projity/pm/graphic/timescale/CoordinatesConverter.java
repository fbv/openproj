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
package com.projity.pm.graphic.timescale;

import com.projity.graphic.configuration.GraphicConfiguration;

import com.projity.pm.graphic.model.cache.GraphicNode;
import com.projity.pm.scheduling.ScheduleEvent;
import com.projity.pm.scheduling.ScheduleEventListener;
import com.projity.pm.scheduling.ScheduleInterval;
import com.projity.pm.task.Project;

import com.projity.timescale.TimeInterval;
import com.projity.timescale.TimeIterator;
import com.projity.timescale.TimeScaleEvent;
import com.projity.timescale.TimeScaleListener;
import com.projity.timescale.TimeScaleManager;

import com.projity.util.DateTime;

import com.projity.workspace.SavableToWorkspace;
import com.projity.workspace.WorkspaceSetting;

import java.io.Serializable;

import java.util.Calendar;
import java.util.EventListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.event.EventListenerList;

/**
 *
 */
public final class CoordinatesConverter
	implements ScheduleEventListener,
		Serializable,
		SavableToWorkspace
{
	/**
	 * 
	 * @param _project 
	 */
	public CoordinatesConverter( 
		final Project _project )
	{
		this( _project, TimeScaleManager.createInstance() );
	}

	/**
	 * 
	 * @param _project
	 * @param _timescaleManager 
	 */
	public CoordinatesConverter( 
		final Project _project,
		final TimeScaleManager _timescaleManager )
	{
		myProject = _project;
		myTimescaleManager = _timescaleManager;
		
		updateLargeInterval( false );
		_project.addScheduleListener( this );
	}

	/**
	 * 
	 * @param _calendar
	 * @param _event 
	 */
	private void adaptEnd( 
		final Calendar _calendar,
		final boolean _event )
	{
		//System.out.println( "adaptEnd: begin" );
		getTimescaleManager().getScale().ceil1( _calendar, -1 );

		final long tmp = _calendar.getTimeInMillis();

		if (myEnd != tmp)
		{
			//System.out.println( "adaptEnd: change: old=" + CalendarUtil.toString( this.myEnd ) + ", new=" 
			//	+ CalendarUtil.toString( tmp ) );
			myEnd = tmp;

			if (_event == true)
			{
				fireTimeScaleChanged( this, TimeScaleEvent.END_ONLY_CHANGE );
			}
		}

		//System.out.println( "adaptEnd: myEnd" );
	}

	/**
	 * 
	 * @param _origin
	 * @param _end
	 * @param _event 
	 */
	private void adaptInterval( 
		final Calendar _origin,
		final Calendar _end,
		final boolean _event )
	{
		final StringBuilder logMessage = new StringBuilder( "_orgin = " )
			.append( _origin )
			.append( "  _end = " )
			.append( _end );
		log.finest( logMessage.toString() );
		
		int modifType = 0;
		long tmp = myEnd;
		adaptEnd( _end, false );

		if (myEnd != tmp)
		{
			modifType = TimeScaleEvent.END_ONLY_CHANGE;
		}

		tmp = myOrigin;
		adaptOrigin( _origin, false );

		if (myOrigin != tmp)
		{
			modifType = TimeScaleEvent.ORIGIN_AND_END_CHANGE;
		}

		if ((modifType > 0) 
		 && (_event == true))
		{
			fireTimeScaleChanged( this, modifType );
		}
	}

	/**
	 * 
	 * @param _calendar
	 * @param _event 
	 */
	private void adaptOrigin( 
		final Calendar _calendar,
		final boolean _event )
	{
		//System.out.println("adaptOrigin: begin");
		getTimescaleManager().getScale().floor1( _calendar, -1 );

		long tmp = _calendar.getTimeInMillis();

		if (myOrigin != tmp)
		{
			//System.out.println("adaptOrigin: change: old="+CalendarUtil.toString(this.myOrigin)+", new="+CalendarUtil.toString(tmp));
			myOrigin = tmp;

			if (_event == true)
			{
				fireTimeScaleChanged( this, TimeScaleEvent.ORIGIN_AND_END_CHANGE );
			}
		}

		//System.out.println("adaptOrigin: end");
	}

	/**
	 * 
	 * @param _start
	 * @param _end
	 * @param _node
	 * @param _config
	 * @return 
	 */
	public static double adaptSmallBarEndX( 
		final double _start,
		final double _end,
		final GraphicNode _node,
		GraphicConfiguration _config )
	{
		if (_config == null)
		{
			_config = GraphicConfiguration.getInstance();
		}

		if ((_config.getGanttBarMinWidth() == 0) || (_node == null) || (_node.getIntervalCount() > 1))
		{
			return _end;
		}

		if ((_start < _end) && ((_end - _start) < _config.getGanttBarMinWidth()) && (_config.getGanttBarMinWidth() > 0))
		{
			return _start + _config.getGanttBarMinWidth();
		}
		else
		{
			return _end;
		}
	}

	/**
	 * 
	 * @param _interval
	 * @param _node
	 * @param _config
	 * @return 
	 */
	public final ScheduleInterval adaptSmallBarTimeInterval( 
		final ScheduleInterval _interval,
		final GraphicNode _node,
		GraphicConfiguration _config )
	{
		if (_config == null)
		{
			_config = GraphicConfiguration.getInstance();
		}

		if ((_config.getGanttBarMinWidth() == 0) || (_node == null) || (_node.getIntervalCount() > 1))
		{
			return _interval;
		}

		if (_config.getGanttBarMinWidth() > 0)
		{
			final double minT = myTimescaleManager.getScale().toTime( _config.getGanttBarMinWidth() );

			if ((_interval.getStart() != _interval.getEnd()) && ((_interval.getEnd() - _interval.getStart()) < minT))
			{
				return new ScheduleInterval( _interval.getStart(), _interval.getStart() + (long)minT );
			}
		}

		return _interval;
	}

	/**
	 * 
	 * @param _listener 
	 */
	public final void addTimeScaleListener( 
		final TimeScaleListener _listener )
	{
		myListenerList.add( TimeScaleListener.class, _listener );
	}

	/**
	 * 
	 * @return 
	 */
	public final boolean canZoomIn()
	{
		return myTimescaleManager.canZoomIn();
	}

	/**
	 * 
	 * @return 
	 */
	public final boolean canZoomOut()
	{
		return myTimescaleManager.canZoomOut();
	}

	/**
	 * 
	 * @return 
	 */
	public final int countProjectIntervals()
	{
		int count = 0;
		final TimeIterator iterator = getProjectTimeIterator();
		while (iterator.hasNext() == true)
		{
			final TimeInterval interval = iterator.next();
			count++;
		}

		return count;
	}

	/* (non-Javadoc)
	 * 
	 */
	@Override
	public final WorkspaceSetting createWorkspace( 
		final int _context )
	{
		final Workspace ws = new Workspace();
		ws.myCurrentScaleIndex = myTimescaleManager.getCurrentScaleIndex();
		ws.myOrigin = myOrigin;
		ws.myEnd = myEnd;

		return ws;
	}

	/**
	 * 
	 * @param _source
	 * @param _type 
	 */
	private void fireTimeScaleChanged( 
		final Object _source,
		final int _type )
	{
		final TimeScaleListener[] listeners = myListenerList.getListeners( TimeScaleListener.class );
		TimeScaleEvent event = null;

		for (int i = listeners.length - 1; i >= 0; i--)
		{
			if (event == null)
			{
				event = new TimeScaleEvent( _source, _type );
			}

			listeners[ i ].timeScaleChanged( event );
		}
	}

//	public long getCeilEnd() 
//	{
//		long t = getTimescaleManager().getScale().ceil1( myEnd );
//		//System.out.println( "End: " + CalendarUtil.toString( myEnd ) + "/" + CalendarUtil.toString( t ) );
//		return t;
//	}

	/**
	 * 
	 * @return 
	 */	
	public final long getEnd()
	{
		//adaptEnd( getLargeEnd(), true );
		return myEnd;
	}

//	public long getFloorOrigin() 
//	{
//		long t = getTimescaleManager().getScale().floor1( myOrigin );
//		//System.out.println( "Origin: " + CalendarUtil.toString( t ) + "/" + CalendarUtil.toString( myOrigin ) );
//		return t;
//	}

	/**
	 * 
	 * @return 
	 */
	public final long getIntervalDuration()
	{
		return getTimescaleManager().getScale().getIntervalDuration();
	}

	/**
	 * 
	 * @return 
	 */
	private Calendar getLargeEnd()
	{
		final Calendar calendar = DateTime.calendarInstance();
		calendar.setTimeInMillis( getProjectEnd() );
		calendar.add( Calendar.DAY_OF_MONTH, 30 );

		//CalendarUtil.roundTime(calendar);
		return calendar;
	}

	/**
	 * 
	 * @return 
	 */
	private Calendar getLargeStart()
	{
		final Calendar calendar = DateTime.calendarInstance();
		calendar.setTimeInMillis( getProjectStart() );
		calendar.add( Calendar.DAY_OF_MONTH, -3 );

		//CalendarUtil.roundTime(calendar);
		return calendar;
	}

	/**
	 * 
	 * @param _class
	 * @return 
	 */
	public final EventListener[] getListeners( 
		final Class<?> _class )
	{
		return myListenerList.getListeners( (Class)_class );
	}

	/**
	 * @return Returns the origin.
	 */
	public final long getOrigin()
	{
		//adaptOrigin(getLargeStart(),true);
		return myOrigin;
	}

	/**
	 * 
	 * @return 
	 */
	public final Project getProject()
	{
		return myProject;
	}

	/**
	 * 
	 * @return 
	 */
	private long getProjectEnd()
	{
		return myProject.getLatestFinishingTask();
	}

	/**
	 * 
	 * @return 
	 */
	private long getProjectStart()
	{
		final long start = myProject.getEarliestStartingTaskOrStart();

		return (start == 0)
			? System.currentTimeMillis()
			: start;
	}

//	public TimeIterator getTimeIteratorFromDates(long start, long end){
//		return new TimeIterator(start,end,myTimescaleManager.getScale(),getOrigin());
//	}

	/**
	 * 
	 * @return 
	 */	
	public final TimeIterator getProjectTimeIterator()
	{
		return new TimeIterator(getOrigin(), getEnd(), myTimescaleManager.getScale(), getOrigin());
	}

	/**
	 * 
	 * @param _x1
	 * @param _x2
	 * @return 
	 */
	public final TimeIterator getTimeIterator( 
		final double _x1,
		final double _x2 )
	{
		return new TimeIterator(toTime( _x1 ), toTime( _x2 ), myTimescaleManager.getScale(), getOrigin());
	}

	/**
	 * 
	 * @param _x1
	 * @param _x2
	 * @param _largeScale
	 * @return 
	 */
	public final TimeIterator getTimeIterator( 
		final double _x1,
		final double _x2,
		final boolean _largeScale )
	{
		return new TimeIterator( toTime( _x1 ), toTime( _x2 ), myTimescaleManager.getScale(), getOrigin(), _largeScale );
	}

	/**
	 * 
	 * @return 
	 */
	public final TimeScaleListener[] getTimeScaleListeners()
	{
		return (TimeScaleListener[])myListenerList.getListeners( TimeScaleListener.class );
	}

	/**
	 * @return Returns the timescaleManager.
	 */
	public final TimeScaleManager getTimescaleManager()
	{
		return myTimescaleManager;
	}

	/**
	 * 
	 * @return 
	 */
	public final double getWidth()
	{
		return toW( getEnd() - getOrigin() );
	}

	/**
	 * 
	 * @param _listener 
	 */
	public final void removeTimeScaleListener( 
		final TimeScaleListener _listener )
	{
		myListenerList.remove( TimeScaleListener.class, _listener );
	}

	@Override
	public final void restoreWorkspace( 
		final WorkspaceSetting _w,
		final int _context )
	{
		Workspace ws = (Workspace)_w;
		myTimescaleManager.setCurrentScaleIndex( ws.myCurrentScaleIndex );
		myOrigin = ws.myOrigin;
		myEnd = ws.myEnd;
	}

	@Override
	public final void scheduleChanged( 
		final ScheduleEvent _event )
	{
		updateLargeInterval( true );

		//if project start or end have changed, it triggers a TimeScaleEvent
	}

	/**
	 * @param _timescaleManager The timescaleManager to set.
	 */
	public final void setTimescaleManager( 
		final TimeScaleManager _timescaleManager )
	{
		myTimescaleManager = _timescaleManager;
	}

	/**
	 * 
	 * @param _w
	 * @return 
	 */
	public final double toDuration( 
		final double _w )
	{
		return myTimescaleManager.getScale().toTime( _w );
	}

	/**
	 * 
	 * @param _x
	 * @return 
	 */
	public final double toTime( 
		final double _x )
	{
		return getOrigin() + myTimescaleManager.getScale().toTime( _x );
	}

	/**
	 * 
	 * @param _d
	 * @return 
	 */
	public final double toW( 
		final double _d )
	{
		return myTimescaleManager.getScale().toX( _d );
	}

	/**
	 * 
	 * @param _t
	 * @return 
	 */
	public final double toX( 
		final double _t )
	{
		return myTimescaleManager.getScale().toX( _t - getOrigin() );
	}

	/**
	 * 
	 * @param _normal 
	 */
	public final void toggleMinWidth( 
		final boolean _normal )
	{
		if (myTimescaleManager.toggleMinWidth( _normal ) == true)
		{
			fireTimeScaleChanged( this, TimeScaleEvent.SCALE_CHANGE );
		}
	}

	/**
	 * 
	 * @param _event 
	 */
	private void updateLargeInterval( 
		final boolean _event )
	{
		adaptInterval( getLargeStart(), getLargeEnd(), _event );

		//System.out.println( "updateLargeInterval: " + CalendarUtil.toString( getOrigin() ) + ", " 
		//	+ CalendarUtil.toString( getEnd() ) );
	}

	/**
	 * 
	 */
	public final void zoomIn()
	{
		if (myTimescaleManager.zoomIn() == true)
		{
			updateLargeInterval( false );
			fireTimeScaleChanged( this, TimeScaleEvent.SCALE_CHANGE );
		}
	}

	/**
	 * 
	 */
	public final void zoomOut()
	{
		if (myTimescaleManager.zoomOut() == true)
		{
			updateLargeInterval( false );
			fireTimeScaleChanged( this, TimeScaleEvent.SCALE_CHANGE );
		}
	}
	
	/**
	 * 
	 */
	public final void zoomReset()
	{
		if (myTimescaleManager.zoomReset() == true)
		{
			updateLargeInterval( false );
			fireTimeScaleChanged( this, TimeScaleEvent.SCALE_CHANGE );
		}
	}

	/**
	 * 
	 */
	public static class Workspace
		implements WorkspaceSetting
	{
		public final int getCurrentScaleIndex()
		{
			return myCurrentScaleIndex;
		}

		public final long getEnd()
		{
			return myEnd;
		}

		public final long getOrigin()
		{
			return myOrigin;
		}

		public final void setCurrentScaleIndex( 
			final int _currentScaleIndex )
		{
			myCurrentScaleIndex = _currentScaleIndex;
		}

		public final void setEnd( 
			final long _end )
		{
			myEnd = _end;
		}

		public final void setOrigin( 
			final long _origin )
		{
			myOrigin = _origin;
		}

		private static final long serialVersionUID = -6767009284584575457L;
		int myCurrentScaleIndex;
		long myEnd;
		long myOrigin;
	}

	private static final long serialVersionUID = 3657308109433257760L;
	private static final Logger log = Logger.getLogger( "CoordinatesConverter" );
	{
		log.setLevel( Level.FINEST );
	}

	//events handling
	private final EventListenerList myListenerList = new EventListenerList();
	private final Project myProject;
	private TimeScaleManager myTimescaleManager;
	private long myEnd;
	private long myOrigin;
}
