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
package com.projity.pm.graphic.frames;

import com.projity.configuration.Dictionary;

import com.projity.pm.graphic.IconManager;
import com.projity.pm.graphic.frames.workspace.FrameHolder;
import com.projity.pm.graphic.frames.workspace.FrameManager;

import com.projity.util.Environment;

import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.WindowConstants;


public class MainFrame
	extends JFrame
	implements FrameHolder
{
	private MainFrame( 
		final String _name,
		final String _projectUrl,
		final String _server )
		throws HeadlessException
	{
		super( _name );
		setIconImage( IconManager.getImage( "application.icon" ) );
		init();
	}

	public static void cleanUp()
	{
		myInstance = null;
	}

	@Override
	public FrameManager getFrameManager()
	{
		return myGraphicManager.getFrameManager();
	}

	@Override
	public GraphicManager getGraphicManager()
	{
		return myGraphicManager;
	}

	public static MainFrame getInstance()
	{
		return myInstance;
	}

	public static MainFrame getInstance(
		final String _name,
		final String _projectUrl,
		final String _server )
	{
		if (myInstance == null)
		{
			myInstance = new MainFrame( _name, _projectUrl, _server );
		}
		
		return myInstance;
	}
	
	public final void init()
	{
		if (Environment.isWindows() == true) 
		{
			// in windows, this is needed otherwise initial display has no frame
			setSize( 1024, 768 );
		}

		if (Environment.isMac() == true)
		{
			setPreferredSize( new Dimension( 1280, 768 ) );
		}
		else
		{
			setPreferredSize( new Dimension( 1024, 768 ) );
		}

		setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE/*DISPOSE_ON_CLOSE*/  );
		addWindowListener( 
			new WindowAdapter()
		{
			@Override
			public void windowClosing( 
				final WindowEvent e )
			{
				myGraphicManager.closeApplication();
			}
		} );
	}

	@Override
	public void setGraphicManager( 
		final GraphicManager _graphicManager )
	{
		myGraphicManager = _graphicManager;
	}

	@Override
	public void setVisible( 
		final boolean _visible )
	{
		super.setVisible( _visible );
	}

	private static final long serialVersionUID = -5161903673269959353L;
	private static MainFrame myInstance = null;
	protected GraphicManager myGraphicManager;
}
