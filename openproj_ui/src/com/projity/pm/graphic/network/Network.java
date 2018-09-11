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
package com.projity.pm.graphic.network;

import com.projity.graphic.configuration.GraphicConfiguration;

import com.projity.pm.graphic.graph.Graph;
import com.projity.pm.graphic.graph.GraphParams;
import com.projity.pm.graphic.graph.LinkRouting;
import com.projity.pm.graphic.network.layout.NetworkLayout;
import com.projity.pm.graphic.network.layout.NetworkLayoutEvent;
import com.projity.pm.graphic.network.layout.NetworkLayoutListener;
import com.projity.pm.graphic.network.link_routing.DefaultNetworkLinkRouting;
import com.projity.pm.task.Project;

import com.projity.workspace.SavableToWorkspace;
import com.projity.workspace.WorkspaceSetting;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import javax.swing.JViewport;


/**
 *
 */
public class Network
	extends Graph
	implements NetworkLayoutListener,
		NetworkParams,
		SavableToWorkspace
{
	public Network( 
		final Project _project,
		final String _viewName )
	{
		this( new NetworkModel( _project, _viewName ), _project );
		myTransform = new AffineTransform();
	}

	protected Network( 
		final NetworkModel _model,
		final Project _project )
	{
		super( _model, _project );
	}

	public boolean canZoomIn()
	{
		return myZoom != barStyles.getMaxZoom();
	}

	public boolean canZoomOut()
	{
		return myZoom != barStyles.getMinZoom();
	}

	@Override
	public GraphParams createSafePrintCopy()
	{
		return this;
	}

	@Override
	public WorkspaceSetting createWorkspace( 
		final int _context )
	{
		Workspace ws = new Workspace();
		ws.setZoom( myZoom );

		final Container p = getParent();

		if (p instanceof JViewport == true)
		{
			ws.setViewPosition( ((JViewport)p).getViewPosition() );
		}

		return ws;
	}

	@Override
	public NetworkLayout getNetworkLayout()
	{
		// TODO Auto-generated method stub
		return ((NetworkModel)getModel()).getNetworkLayout();
	}

	@Override
	public Rectangle getPrintBounds()
	{
		return null;
	}

	@Override
	public int getPrintCols()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getPrintRows()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public LinkRouting getRouting()
	{
		return myRouting;
	}

	@Override
	public AffineTransform getTransform()
	{
		return myTransform;
	}

	@Override
	public int getZoom()
	{
		return myZoom;
	}

	@Override
	public boolean isLeftPartVisible()
	{
		return true;
	}

	@Override
	public boolean isRightPartVisible()
	{
		return true;
	}

	@Override
	public boolean isSupportLeftAndRightParts()
	{
		return false;
	}

	@Override
	public void layoutChanged( 
		final NetworkLayoutEvent _event )
	{
		updateSize();
		revalidate();
		repaint();
	}

	private void makeZoom( 
		final int _newZoom )
	{
		final int factor = _newZoom - myZoom;

		if (factor > 0)
		{
			for (int i = 0; i < factor; i++)
			{
				zoomIn();
			}
		}
		else
		{
			for (int i = 0; i > factor; i--)
			{
				zoomOut();
			}
		}
	}

	@Override
	public void restoreWorkspace( 
		final WorkspaceSetting _w,
		final int _context )
	{
		final Workspace ws = (Workspace)_w;
		makeZoom( ws.getZoom() );

		final Container p = getParent();

		if (p instanceof JViewport && (ws.getViewPosition() != null))
		{
			try
			{
				((JViewport)p).setViewPosition( ws.getViewPosition() );
			}
			catch (RuntimeException e)
			{
				System.out.println( "problem restoring viewport to point " + ws.getViewPosition() );
			}
		}
	}

	@Override
	public GeneralPath scale( 
		final GeneralPath _path )
	{
		if (_path == null)
		{
			return null;
		}

		final GeneralPath transformed = (GeneralPath)_path.clone();
		transformed.transform( getTransform() );

		return transformed;
	}

	@Override
	public Point2D scale( 
		final Point2D _p )
	{
		final AffineTransform t = getTransform();

		return new Point2D.Double( (_p.getX() * t.getScaleX()) + t.getTranslateX(), (_p.getY() * t.getScaleY()) 
			+ t.getTranslateY() );
	}

	@Override
	public Rectangle scale( 
		final Rectangle _r )
	{
		final AffineTransform t = getTransform();

		if (t == null)
		{
			return _r;
		}

		final Rectangle sr = new Rectangle();
		sr.setFrameFromDiagonal( (_r.getMinX() * t.getScaleX()) + t.getTranslateX(),
			(_r.getMinY() * t.getScaleY()) + t.getTranslateY(), (_r.getMaxX() * t.getScaleX()) + t.getTranslateX(),
			(_r.getMaxY() * t.getScaleY()) + t.getTranslateY() );

		return sr;
	}

	@Override
	public Point2D scaleVector( 
		final Point2D _p )
	{
		AffineTransform t = getTransform();

		return new Point2D.Double( _p.getX() * t.getScaleX(), _p.getY() * t.getScaleY() );
	}

	@Override
	public Point2D scaleVector_1( 
		final Point2D _p )
	{
		AffineTransform t = getTransform();

		return new Point2D.Double( _p.getX() / t.getScaleX(), _p.getY() / t.getScaleY() );
	}

	@Override
	public void setLeftPartVisible( 
		final boolean _visible )
	{
	}

	@Override
	public void setPrintBounds( 
		final Rectangle _printBounds )
	{
	}

	@Override
	public void setRightPartVisible( 
		final boolean _visible )
	{
	}

	@Override
	public void setRouting( 
		final LinkRouting _routing )
	{
		myRouting = _routing;
	}

	@Override
	public void setSupportLeftAndRightParts( 
		final boolean _supports )
	{
	}

	public void updateSize()
	{
		final Rectangle bounds = ((NetworkModel)getModel()).getBounds();
		final GraphicConfiguration config = GraphicConfiguration.getInstance();

		setPreferredSize( new Dimension( bounds.x + bounds.width + config.getPertXOffset(),
				bounds.y + bounds.height + config.getPertYOffset() ) );
	}

	@Override
	public boolean useTextures()
	{
		return true;
	}

	public void zoomIn()
	{
		if (myZoom == barStyles.getMaxZoom())
		{
			return;
		}

		((NetworkUI)ui).resetForms();
		myTransform.concatenate( AffineTransform.getScaleInstance( barStyles.getRatioX( myZoom, true ),
				barStyles.getRatioY( myZoom++, true ) ) );
		((NetworkModel)getModel()).updateCellBounds();
	}

	public void zoomOut()
	{
		if (myZoom == barStyles.getMinZoom())
		{
			return;
		}

		((NetworkUI)ui).resetForms();
		myTransform.concatenate( AffineTransform.getScaleInstance( barStyles.getRatioX( myZoom, false ),
				barStyles.getRatioY( myZoom--, false ) ) );
		((NetworkModel)getModel()).updateCellBounds();
	}

	public static class Workspace
		implements WorkspaceSetting
	{
		public Point getViewPosition()
		{
			return myViewPosition;
		}

		public int getZoom()
		{
			return myZoom;
		}

		public void setViewPosition( 
			Point _viewPosition )
		{
			myViewPosition = _viewPosition;
		}

		public void setZoom( 
			int _zoom )
		{
			myZoom = _zoom;
		}

		private static final long serialVersionUID = 7804032466144588065L;
		private Point myViewPosition = null;
		private int myZoom;
	}

	private static final long serialVersionUID = -7976852605189565105L;
	protected AffineTransform myTransform;
	protected LinkRouting myRouting = new DefaultNetworkLinkRouting();
	protected int myZoom;
}
