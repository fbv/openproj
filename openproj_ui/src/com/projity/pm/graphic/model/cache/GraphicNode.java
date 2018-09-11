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
package com.projity.pm.graphic.model.cache;

import com.projity.functor.IntervalConsumer;
import com.projity.graphic.configuration.GraphicConfiguration;
import com.projity.grouping.core.GroupNodeImpl;
import com.projity.grouping.core.LazyParent;
import com.projity.grouping.core.Node;
import com.projity.grouping.core.transform.HierarchicObject;
import com.projity.pm.assignment.Assignment;
import com.projity.pm.graphic.timescale.CoordinatesConverter;
import com.projity.pm.scheduling.Schedule;
import com.projity.pm.scheduling.ScheduleInterval;
import com.projity.pm.scheduling.ScheduleService;
import com.projity.server.data.CommonDataObject;
import com.projity.server.data.DataObject;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.openproj.domain.task.Task;


/**
 *
 */
public class GraphicNode
	implements HierarchicObject
{
	/**
	 * @param node
	 * @param level
	 */
	public GraphicNode( 
		final Node _node,
		final int _level )
	{
		setNode( _node );
		this.level = _level;
		dirty = false;
		pertLevel = -1;
		setScheduleCaching( false );
	}

	/**
	 *
	 * @param consumer
	 */
	public void consumeIntervals( 
		final IntervalConsumer _consumer )
	{
		if (scheduleCaching == true)
		{
			for (Iterator i = intervals.iterator(); i.hasNext() == true;)
			{
				_consumer.consumeInterval( (ScheduleInterval)i.next() );
			}
		}
		else
		{
			Object impl = node.getValue();

			if (isSchedule() == true)
			{
				ScheduleService.consumeIntervals( (Schedule)impl, _consumer );
			}
		}
	}

	public ScheduleInterval contains( 
		final double _t,
		final double _deltaT1,
		final double _deltaT2,
		final CoordinatesConverter _coord )
	{
		if (scheduleCaching)
		{
			ScheduleInterval interval;

			for (Iterator i = intervals.iterator(); i.hasNext();)
			{
				interval = (ScheduleInterval)i.next();

				if (_coord != null)
				{
					interval = _coord.adaptSmallBarTimeInterval( interval, this, null );
				}

				if ((_t >= (interval.getStart() - _deltaT1)) && (_t <= (interval.getEnd() + _deltaT2)))
				{
					return interval;
				}
			}

			return null;
		}
		else
		{
			if (containsConsumer == null)
			{
				containsConsumer = new ContainsIntervalConsumer();
			}

			containsConsumer.init( _t, _deltaT1, _deltaT2, _coord, this );

			Object impl = node.getValue();

			if (isSchedule())
			{
				ScheduleService.consumeIntervals( (Schedule)impl, containsConsumer );
			}

			return containsConsumer.getInterval();
		}
	}

	public boolean fetch()
	{
		if (node.getValue() instanceof LazyParent)
		{
			return ((LazyParent)node.getValue()).fetchData( node );
		}

		return true;
	}

	public List getChildren()
	{
		return tmpChildren;
	}

	public long getCompleted()
	{
		if (!(getNode().getValue() instanceof Schedule))
		{
			return 0;
		}

		long completedT = ScheduleService.getCompleted( (Schedule)getNode().getValue() );

		return (completedT == 0)
		? getStart()
		: completedT;
	}

	public long getEnd()
	{
		return (scheduleCaching || !isSchedule())
		? end
		: ((Schedule)node.getValue()).getEnd();
	}

	public double getGanttShapeHeight()
	{
		return ganttShapeHeight;
	}

	public double getGanttShapeOffset()
	{
		return ganttShapeOffset;
	}

	public int getIntervalCount()
	{
		return intervalCount;
	}

	/*public ReferenceNodeModelCache getCache() {
	        return cache;
	}*/

	/**
	 * @return Returns the level.
	 */
	public int getLevel()
	{
		return level;
	}

	/**
	 * @return Returns the node.
	 */
	public Node getNode()
	{
		return node;
	}

	public Point2D getPertCenter()
	{
		return pertCenter;
	}

	public int getPertLevel()
	{
		return pertLevel;
	}

	public GeneralPath getPertShape()
	{
		return pertShape;
	}

	public int getRow()
	{
		return row;
	}

	//TODO add recurrent tasks support
	public long getStart()
	{
		return (scheduleCaching || !isSchedule())
		? start
		: ((Schedule)node.getValue()).getStart();
	}

	public int getSubprojectLevel()
	{
		//		if (getNode().getValue() instanceof Task){
		//			Task task=(Task)getNode().getValue();
		//			if (task.isInSubproject()) return 1;
		//		}

		//		Node node=getNode();
		//		int level=0;
		//		while (node.isInSubproject()){
		//			node=(Node)node.getParent();
		//			level+=1;
		//		}
		//		return level;

		//if (getNode().isInSubproject()) return 1;
		//return 0;
		return node.getSubprojectLevel();
	}

	public static Object getValue( 
		final Object _obj )
	{
		if (_obj instanceof GraphicNode)
		{
			return ((GraphicNode)_obj).getNode().getValue();
		}
		else if (_obj instanceof Node)
		{
			return ((Node)_obj).getValue();
		}
		else
		{
			return _obj;
		}
	}

	public Point2D getXbsCenter()
	{
		return xbsCenter;
	}

	public GeneralPath getXbsShape()
	{
		return xbsShape;
	}

	public boolean isAssignment()
	{
		return getNode().getValue() instanceof Assignment;
	}

	/**
	 * @return Returns the collapsed.
	 */
	public boolean isCollapsed()
	{
		return collapsed;
	}

	/**
	 * @return Returns the composite.
	 */
	public boolean isComposite()
	{
		return composite;
	}

	public boolean isDirty()
	{
		return dirty;
	}

	public boolean isFetched()
	{
		if (node.getValue() instanceof LazyParent)
		{
			return ((LazyParent)node.getValue()).isDataFetched();
		}
		else
		{
			return true;
		}
	}

	public boolean isFiltered()
	{
		return tmpFiltered;
	}

	public boolean isGroup()
	{
		return getNode().getValue() instanceof GroupNodeImpl;
	}

	public boolean isLazyParent()
	{
		return node.getValue() instanceof LazyParent;
	}

	public boolean isLinkable()
	{
		Object impl = getNode().getValue();

		if (impl instanceof Assignment)
		{
			return false;
		}

		if (impl instanceof Task && ((Task)impl).isExternal())
		{
			return false;
		}

		return true;
	}

	//	public boolean contains(double t,CoordinatesConverter coord){
	//		return contains(t,0,0,coord)!=null;
	//	}
	public boolean isSchedule()
	{
		return node.getValue() instanceof Schedule;
	}

	public boolean isScheduleCaching()
	{
		return scheduleCaching;
	}

	public boolean isServer()
	{
		Object impl = getNode().getValue();

		if (!(impl instanceof DataObject))
		{
			return false;
		}

		return !CommonDataObject.isLocal( (DataObject)impl );
	}

	public boolean isStarted()
	{
		if (!(getNode().getValue() instanceof Schedule))
		{
			return false;
		}

		return ((Schedule)getNode().getValue()).getPercentComplete() > 0.0D;

		//		return ScheduleService.getInstance().getCompleted((Schedule)getNode().getValue())!=0;
	}

	public boolean isSummary()
	{
		return summary;
	}

	public boolean isValidLazyParent()
	{
		if (node.getValue() instanceof LazyParent)
		{
			return ((LazyParent)node.getValue()).isValid();
		}

		return false;
	}

	public boolean isVoid()
	{
		return voidNode; //getNode().isVoid();
	}

	public static boolean isVoid( 
		final Object _obj )
	{
		if (_obj instanceof GraphicNode)
		{
			return ((GraphicNode)_obj).isVoid();
		}
		else if (_obj instanceof Node)
		{
			return ((Node)_obj).isVoid();
		}
		else
		{
			return _obj == null;
		}
	}

	/**
	 * @param collapsed The collapsed to set.
	 */
	public void setCollapsed( 
		final boolean _collapsed )
	{
		this.collapsed = _collapsed;
		dirty = true;
	}

	/**
	 * @param composite The composite to set.
	 */
	public void setComposite( 
		final boolean _composite )
	{
		this.composite = _composite;
		dirty = true;
	}

	public void setDirty( 
		final boolean _dirty )
	{
		//		System.out.println("GraphicNode _setDirty");
		this.dirty = _dirty;
	}

	public void setFiltered( 
		final boolean _filtered )
	{
		this.tmpFiltered = _filtered;
	}

	public void setGanttShapeHeight( 
		final double _ganttShapeHeight )
	{
		this.ganttShapeHeight = _ganttShapeHeight;
	}

	public void setGanttShapeOffset( 
		final double _ganttShapeOffset )
	{
		this.ganttShapeOffset = _ganttShapeOffset;
	}

	/**
	 * @param node The node to set.
	 */
	public void setNode( 
		final Node _node )
	{
		this.node = _node;
		dirty = true;
	}

	public void setPertShape( 
		final GeneralPath _pertShape,
		final double _centerX,
		final double _centerY )
	{
		this.pertShape = _pertShape;

		if (pertCenter == null)
		{
			pertCenter = new Point2D.Double();
		}

		pertCenter.setLocation( _centerX, _centerY );
	}

	public void setRow( 
		final int _row )
	{
		this.row = _row;
	}

	public void setScheduleCaching( 
		final boolean _scheduleCaching )
	{
		this.scheduleCaching = _scheduleCaching;
		intervals = (_scheduleCaching)
			? new ArrayList()
			: null;

		ContainsIntervalConsumer containsConsumer = null; //clean if it wasn't scheduleCaching before
	}

	public void setSummary( 
		final boolean _summary )
	{
		this.summary = _summary;
		dirty = true;
	}

	public void setVoid( 
		final boolean _voidNode )
	{
		this.voidNode = _voidNode;
		dirty = true;
	}

	private void setXbsCenter( 
		final double _centerX,
		final double _centerY )
	{
		if (xbsCenter == null)
		{
			xbsCenter = new Point2D.Double();
		}

		xbsCenter.setLocation( _centerX, _centerY );
	}

	public void setXbsShape( 
		final GeneralPath _xbsShape,
		final double _centerX,
		final double _centerY )
	{
		this.xbsShape = _xbsShape;
		setXbsCenter( _centerX, _centerY );
	}

	@Override
	public String toString()
	{
		return node.toString();
	}

	public void translatePertShape( 
		final double _dx,
		final double _dy )
	{
		AffineTransform t = AffineTransform.getTranslateInstance( _dx, _dy );
		getPertShape().transform( t );

		Point2D point = getPertCenter();
		point.setLocation( point.getX() + _dx, point.getY() + _dy );
	}

	public void translateXbsShape( 
		final double _dx,
		final double _dy )
	{
		AffineTransform t = AffineTransform.getTranslateInstance( _dx, _dy );
		getXbsShape().transform( t );

		Point2D point = getXbsCenter();
		point.setLocation( point.getX() + _dx, point.getY() + _dy );
	}

	public void updateScheduleCache()
	{
		if (scheduleCaching || (GraphicConfiguration.getInstance().getGanttBarMinWidth() > 0))
		{
			Object impl = node.getValue();

			if (!isSchedule())
			{
				return;
			}

			intervalConsumer.initCache( this, intervals );
			ScheduleService.consumeIntervals( (Schedule)impl, intervalConsumer );
			intervalCount = (intervalConsumer.size > 0)
				? intervalConsumer.size
				: 1;
		}
	}

	/**
	 * @param level The level to set.
	 */
	void setLevel( 
		final int _level )
	{
		this.level = _level;
		dirty = true;
	}

	void setPertLevel( 
		final int _pertLevel )
	{
		this.pertLevel = _pertLevel;
	}

	protected static class CacheIntervalConsumer
		implements IntervalConsumer
	{
		@Override
		public void consumeInterval( 
			final ScheduleInterval _interval )
		{
			if (size++ == 0)
			{
				gnode.start = _interval.getStart();
			}

			gnode.end = _interval.getEnd();

			if (cache != null)
			{
				cache.add( _interval );
			}
		}

		public void initCache( 
			final GraphicNode _gnode,
			final List _cache )
		{
			size = 0;

			if (_cache != null)
			{
				_cache.clear();
			}

			this.cache = _cache;
			this.gnode = _gnode;
		}

		protected GraphicNode gnode = null;
		protected List cache = null;
		int size;
	}

	private static class ContainsIntervalConsumer
		implements IntervalConsumer
	{
		@Override
		public void consumeInterval( 
			ScheduleInterval _interval )
		{
			if (coord != null)
			{
				_interval = coord.adaptSmallBarTimeInterval( _interval, node, null );
			}

			if ((t >= (_interval.getStart() - deltaT1)) && (t <= (_interval.getEnd() + deltaT2)))
			{
				this.interval = _interval;
			}
		}

		public ScheduleInterval getInterval()
		{
			return interval;
		}

		public void init( 
			final double _t,
			final double _deltaT1,
			final double _deltaT2,
			final CoordinatesConverter _coord,
			final GraphicNode _node )
		{
			interval = null;
			this.t = _t;
			this.deltaT1 = _deltaT1;
			this.deltaT2 = _deltaT2;
			this.coord = _coord;
			this.node = _node;
		}

		CoordinatesConverter coord;
		GraphicNode node;
		ScheduleInterval interval = null;
		double deltaT1;
		double deltaT2;
		double t;
	}

	protected static CacheIntervalConsumer intervalConsumer = new CacheIntervalConsumer();
	protected ArrayList intervals = null;

	//contains
	private ContainsIntervalConsumer containsConsumer = null; //need when no schedule caching
	protected GeneralPath pertShape = null;
	protected GeneralPath xbsShape = null;

	//	protected boolean manualPert=false;
	//	protected boolean manualXbs=false;
	//
	//
	//	public boolean isManualPert() {
	//		return manualPert;
	//	}
	//	public void setManualPert(boolean manualPert) {
	//		this.manualPert = manualPert;
	//	}
	//	public boolean isManualXbs() {
	//		return manualXbs;
	//	}
	//	public void setManualXbs(boolean manualXbs) {
	//		this.manualXbs = manualXbs;
	//	}
	protected List tmpChildren = new ArrayList();
	protected Node node;
	protected Point2D pertCenter = null;
	protected Point2D xbsCenter = null;
	protected boolean collapsed;
	protected boolean composite;
	protected boolean dirty;
	protected boolean scheduleCaching;
	protected boolean summary;
	protected boolean tmpFiltered;
	protected boolean voidNode;
	protected double ganttShapeHeight = GraphicConfiguration.getInstance().getGanttBarHeight();
	protected double ganttShapeOffset = 0;
	protected int intervalCount = 1;
	protected int level;
	protected int pertLevel;
	protected int row; //tmp value for performance reasons
	protected long end = -1;
	protected long start = -1;
}
