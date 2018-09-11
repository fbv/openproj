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
package com.projity.session;

import com.projity.job.Job;
import com.projity.job.JobQueue;

import com.projity.strings.Messages;

import com.projity.util.ClassUtils;
import com.projity.util.Environment;

import java.lang.reflect.InvocationTargetException;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 *
 */
public class SessionFactory
{
	protected SessionFactory()
	{
	}

	public static Object call( 
		final Object _object,
		final String _method,
		final Class[] _argsDesc,
		final Object[] _args )
		throws Exception
	{
		try
		{
			//System.out.println("call, "+method+"..."+object.getClass());
			return _object.getClass().getMethod( _method, _argsDesc ).invoke( _object, _args );
		}
		catch (IllegalArgumentException | SecurityException | IllegalAccessException | InvocationTargetException 
			| NoSuchMethodException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public static Object callNoEx( 
		final Object _object,
		final String _method,
		final Class[] _argsDesc,
		final Object[] _args )
	{
		try
		{
			//System.out.println("callNoEx, "+method+"...");
			return _object.getClass().getMethod( _method, _argsDesc ).invoke( _object, _args );
		}
		catch (IllegalArgumentException | SecurityException | IllegalAccessException | InvocationTargetException 
			| NoSuchMethodException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public static void cleanUp()
	{
		myInstance = null;
	}

	public void clearSessions()
	{
		mySessionImpls = null;
	}

	public static SessionFactory getInstance()
	{
		if (myInstance == null)
		{
			myInstance = new SessionFactory();
		}

		return myInstance;
	}

	public JobQueue getJobQueue()
	{
		return myJobQueue;
	}

	public LocalSession getLocalSession()
	{
		return (LocalSession)getSession( "local" );
	}

	public String getLogin()
	{
		return myCredentials.get( "login" );
	}

	public Session getServerImportSession()
	{
		//if (Environment.isNoPodServer())
		//	local = true;
		return getSession( "serverImport" );
	}

	public String getServerUrl()
	{
		return myCredentials.get( "serverUrl" );
	}

	protected Session getSession( 
		final String _name )
	{
		initSessions();

		Session session = mySessionImpls.get( _name );

		if (!session.isInitialized())
		{
			session.init( myCredentials );
		}

		return session;
	}

	public Session getSession( 
		final boolean _local )
	{
		return getSession( _local, DEFAULT_FORMAT );
	}

	public Session getSession( 
		final boolean _local,
		final int _format )
	{
		//if (Environment.isNoPodServer())
		//	local = true;
		return _local
			? getSession( "local" )
			: ((_format == NO_POD_FORMAT)
				? getSession( "serverImport" )
				: getSession( "server" ));
	}

	protected void initSessions()
	{
		if (mySessionImpls == null)
		{
			mySessionImpls = new HashMap<String,Session>();

			String impls = Messages.getMetaString( "SessionImpls" );

			if (impls != null)
			{
				StringTokenizer st = new StringTokenizer(impls, ";");

				while (st.hasMoreTokens())
				{
					String key = st.nextToken();
					String implClass = Messages.getMetaString( key );

					if (implClass != null)
					{
						try
						{
							Session session = (Session)ClassUtils.forName( implClass ).newInstance();

							//session.init( myCredentials );
							if (session.getJobQueue() == null)
							{
								session.setJobQueue( getJobQueue() ); //because this method is called before myJobQueue is set
							}

							mySessionImpls.put( key.substring( key.lastIndexOf( '.' ) + 1 ), session );
						}
						catch (InstantiationException | IllegalAccessException | ClassNotFoundException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	public void schedule( 
		final Job _job )
	{
		myJobQueue.schedule( _job );
	}

	public void setCredentials( 
		final Map<String,String> _credentials )
	{
		if (_credentials != null)
		{
			myCredentials.clear();
			myCredentials.putAll( _credentials );
		}
	}

	public void setJobQueue( 
		final JobQueue _jobQueue )
	{
		myJobQueue = _jobQueue;

		if (mySessionImpls == null)
		{
			initSessions();
		}

		for (Session session : mySessionImpls.values())
		{
			session.setJobQueue( myJobQueue );
		}
	}

	public static final int DEFAULT_FORMAT = 0;
	public static final int POD_FORMAT = 1;
	public static final int NO_POD_FORMAT = 2;

	protected static SessionFactory myInstance = null;
	protected JobQueue myJobQueue = null;
	private final Map<String,String> myCredentials = new HashMap<String,String>();
	protected Map<String,Session> mySessionImpls = null;
}
