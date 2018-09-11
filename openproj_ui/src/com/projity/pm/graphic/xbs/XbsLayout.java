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
package com.projity.pm.graphic.xbs;

import com.projity.graphic.configuration.BarFormat;
import com.projity.graphic.configuration.GraphicConfiguration;
import com.projity.graphic.configuration.TexturedShape;

import com.projity.pm.graphic.model.cache.GraphicDependency;
import com.projity.pm.graphic.model.cache.GraphicNode;
import com.projity.pm.graphic.model.event.CompositeCacheEvent;
import com.projity.pm.graphic.network.NetworkParams;
import com.projity.pm.graphic.network.layout.AbstractNetworkLayout;

import org.apache.commons.collections.Closure;

import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;


/**
 *
 */
public final class XbsLayout
	extends AbstractNetworkLayout
{
	/**
	 * 
	 * @param _network 
	 */
	public XbsLayout( 
		final NetworkParams _network )
	{
		super( _network );
	}

	/**
	 * 
	 * @param _node
	 * @return 
	 */
	private TexturedShape findShape( 
		final GraphicNode _node )
	{
		myTexturedShapeFinder.initialize( _node );
		myBarStyles.apply( _node.getNode().getValue(), myTexturedShapeFinder );

		return myTexturedShapeFinder.getShape();
	}

	/**
	 * 
	 * @return 
	 */
	public final List<GraphicDependency> getDependencies()
	{
		return myDependencies;
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.graphic.network.layout.AbstractNetworkLayout#graphicNodesCompositeEvent(CompositeCacheEvent)
	 */
	@Override
	public final void graphicNodesCompositeEvent( 
		final CompositeCacheEvent _compositeEvent )
	{
		if (_compositeEvent.isNodeHierarchy() == false)
		{
			return;
		}

		updateBounds();
	}

	/**
	 * 
	 * @param _node
	 * @param _ref
	 * @param _centerX
	 * @param _centerY 
	 */
	private void setShape( 
		final GraphicNode _node,
		final Rectangle2D _ref,
		final double _centerX,
		final double _centerY )
	{
		TexturedShape texturedShape = findShape( _node );

		if (texturedShape == null)
		{
			return;
		}

		GeneralPath shape = texturedShape.toGeneralPath( _ref.getWidth(), _ref.getHeight(), _centerX - (_ref.getWidth() / 2),
			_centerY, null );
		_node.setXbsShape( shape, _centerX, _centerY );
		Rectangle.union( myBounds, myNetwork.scale( shape.getBounds() ), myBounds );
	}

	/**
	 * 
	 * @param _origin
	 * @param _ref
	 * @return 
	 */
	private int updateBounds( 
		final Point2D _origin,
		final Rectangle2D _ref )
	{ 
		//cache in current version isn't a tree
		double x = _origin.getX() + (_ref.getWidth() / 2);
		final double y = _origin.getY() + (_ref.getHeight() / 2);
		GraphicNode node;
		GraphicNode previous = null;
		int maxLevel = 0;

		for (ListIterator i = myCache.getIterator(); i.hasNext();)
		{
			node = (GraphicNode)i.next();

			if (node.getLevel() > maxLevel)
			{
				maxLevel = node.getLevel();
			}

			if ((previous != null) && (node.getLevel() <= previous.getLevel()))
			{
				setShape( previous, _ref, x, y + ((previous.getLevel() - 1) * (_ref.getMaxY())) );
				x += _ref.getMaxX();
			}

			previous = node;
		}

		if (previous != null)
		{
			setShape( previous, _ref, x, y + ((previous.getLevel() - 1) * (_ref.getMaxY())) );
		}

		return maxLevel;
	}

	/**
	 * 
	 * @param _level
	 * @param _origin
	 * @param _ref 
	 */
	private void updateBounds( 
		final int _level,
		final Point2D _origin,
		final Rectangle2D _ref )
	{ 
		//cache in current version isn't a tree
		final double y = _origin.getY() + (_ref.getHeight() / 2) + (_ref.getMaxY() * (_level - 1));
		Point2D childCenter;
		Point2D center;
		double x0;
		double x1;
		GraphicNode node;
		GraphicNode child;
		boolean hasChild;

		for (ListIterator i = myCache.getIterator(); i.hasNext();)
		{
			node = (GraphicNode)i.next();

			if (node.getLevel() == _level)
			{
				x0 = -1;
				x1 = -1;
				hasChild = false;

				while (i.hasNext())
				{
					child = (GraphicNode)i.next();

					if (child.getLevel() <= _level)
					{
						i.previous();

						break;
					}
					else if (child.getLevel() == (_level + 1))
					{
						hasChild = true;
						childCenter = child.getXbsCenter();

						if ((x0 == -1) || (childCenter.getX() < x0))
						{
							x0 = childCenter.getX();
						}

						if ((x1 == -1) || (childCenter.getX() > x1))
						{
							x1 = childCenter.getX();
						}

						myDependencies.add( new GraphicDependency( node, child, null ) );
					}
				}

				if (hasChild == true)
				{
					setShape( node, _ref, (x0 + x1) / 2, y );
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.projity.pm.graphic.network.layout.AbstractNetworkLayout#updateBounds()
	 */
	@Override
	public final void updateBounds()
	{
		final GraphicConfiguration config = GraphicConfiguration.getInstance();

		final Point2D origin = new Point2D.Double( config.getTreeXOffset(), config.getTreeYOffset() );
		final Rectangle2D ref = new Rectangle2D.Double( config.getTreeXOffset(), config.getTreeYOffset(), 
			config.getTreeCellWidth(), config.getTreeCellHeight() );
		setEmpty();
		myDependencies.clear();

		myBounds.setFrame( 0.0, 0.0, 0.0, 0.0 );

		final int maxLevel = updateBounds( origin, ref );

		if (maxLevel == 0)
		{
			return;
		}

		for (int level = maxLevel - 1; level > 0; level--)
		{
			updateBounds( level, origin, ref );
		}

		fireLayoutChanged();
	}

	/**
	 * 
	 */
	private final class TexturedShapeFinder
		implements Closure<BarFormat>
	{
		@Override
		public final void execute( 
			final BarFormat _format )
		{
			if (_format.getMiddle() != null)
			{
				shape = _format.getMiddle();
			}
		}

		public final TexturedShape getShape()
		{
			return shape;
		}

		public final void initialize( 
			final GraphicNode _node )
		{
			node = _node;
			shape = null;
		}

//		private BarFormat format;
		private GraphicNode node;
		private TexturedShape shape;
	}

	private List<GraphicDependency> myDependencies = new ArrayList<GraphicDependency>();
	private final TexturedShapeFinder myTexturedShapeFinder = new TexturedShapeFinder();
}
