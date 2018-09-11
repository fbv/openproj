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
package com.projity.pm.dependency;

import com.projity.association.InvalidAssociationException;

import com.projity.pm.task.SubProj;

import com.projity.strings.Messages;

import com.projity.undo.DataFactoryUndoController;
import com.projity.undo.DependencyCreationEdit;
import com.projity.undo.DependencyDeletionEdit;
import com.projity.undo.DependencySetFieldsEdit;

import com.projity.util.Alert;
import com.projity.util.ClassUtils;

import org.apache.commons.collections.Predicate;

import java.text.MessageFormat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import org.openproj.domain.task.Task;

/**
 * Manages the creation and deleting of dependencies as well as events
 */
public class DependencyService
{
	public static String getCircularCrossProjectLinkMessage( 
		Object _predecessor,
		Object _successor )
	{
		return MessageFormat.format( Messages.getString( "Message.crossProjectCircularDependency.mf" ),
			new Object[]
		{
			_predecessor,
			_successor
		} );
	}

	/**
	 * Warn that a cross project link is disabled. This is invoked later to give time for the gantt to redraw first
	 * @param _predecessor
	 * @param _successor
	 */
	public static void warnCircularCrossProjectLinkMessage( 
		final Object _predecessor,
		final Object _successor )
	{
		if (Alert.allowPopups() == true)
		{
			SwingUtilities.invokeLater( 
				new Runnable()
			{
				@Override
				public void run()
				{
					Alert.warn( getCircularCrossProjectLinkMessage( _predecessor, _successor ) );
				}
			} );
		}
	}

	public void addEndSentinelDependency( 
		final HasDependencies _sentinel,
		final HasDependencies _predecessor )
	{
		final Dependency dependency = Dependency.getInstance( _predecessor, _sentinel, DependencyType.FS, 0 );
		_sentinel.getPredecessorList().add( dependency );

//		System.out.println("adding end sentinel dependency task is " + predecessor);
	}

	public void addStartSentinelDependency( 
		final HasDependencies _sentinel,
		final HasDependencies _successor )
	{
		final Dependency dependency = Dependency.getInstance( _sentinel, _successor, DependencyType.SS, 0 );
		_sentinel.getSuccessorList().add( dependency );

//		System.out.println("adding start sentinel dependency task is " + successor);
	}

	public void connect( 
		final Dependency _dependency,
		final Object _eventSource )
	{
		_dependency.getPredecessor().getSuccessorList().add( _dependency );
		_dependency.getSuccessor().getPredecessorList().add( _dependency );
		updateSentinels( _dependency );

		if (_eventSource != null)
		{
			_dependency.fireCreateEvent( _eventSource );
		}

		_dependency.setDirty( true );

		final UndoableEditSupport undoableEditSupport = getUndoableEditSupport( _dependency );

		if ((undoableEditSupport != null) && (_eventSource != null) && !(_eventSource instanceof UndoableEdit))
		{
			undoableEditSupport.postEdit( new DependencyCreationEdit( _dependency, _eventSource ) );
		}
	}

	/**
	 * Connect tasks sequentially.
	 * Circularities will be tested, and an exception thrown if any circularity would occur
	 *
	 * @param _tasks
	 * @param _eventSource
	 * @throws InvalidAssociationException
	 */
	public void connect( 
		final List _tasks,
		final Object _eventSource,
		final Predicate _canBeSuccessorCondition )
		throws InvalidAssociationException
	{
		final ArrayList newDependencies = new ArrayList();

		// try making new dependencies between all items earlier to all items later, thereby checking all possible circularities
		HasDependencies pred;
		HasDependencies succ;
		Object temp;

		for (int i = 0; i < (_tasks.size() - 1); i++)
		{
			temp = _tasks.get( i );

			if (!(temp instanceof HasDependencies))
			{
				continue;
			}

			pred = (HasDependencies)temp;

			for (int j = i + 1; j < _tasks.size(); j++)
			{
				temp = _tasks.get( j );

				if (!(temp instanceof HasDependencies))
				{
					continue;
				}

				succ = (HasDependencies)temp;

				if ((_canBeSuccessorCondition != null) && !_canBeSuccessorCondition.evaluate( succ )) 
				{
					// allow exclusion of certain nodes that we don't want to be successors
					continue;
				}

				if (succ.getPredecessorList().findLeft( pred ) != null) 
				{
					// if dependency already exists, skip it
					continue;
				}

				if (ClassUtils.isObjectReadOnly( succ ))
				{
					continue;
				}

				final Dependency test = Dependency.getInstance( pred, succ, DependencyType.FS, 0 ); // make a new one
				test.testValid( false ); // test for circularity, throws if bad

				if (j == (i + 1)) // only add sequential ones
				{
					newDependencies.add( test );
				}
			}
		}

		final Iterator d = newDependencies.iterator();

		while (d.hasNext())
		{
			connect( (Dependency)d.next(), _eventSource );
		}
	}

	public void fireTaskPredecessors( 
		final Collection _list )
	{
		final Iterator i = _list.iterator();

		while (i.hasNext())
		{
			Iterator j = ((Task)i.next()).getPredecessorList().iterator();

			while (j.hasNext())
			{
				((Dependency)j.next()).fireCreateEvent( this );
			}
		}
	}

	public static DependencyService getInstance()
	{
		if (instance == null)
		{
			instance = new DependencyService();
		}

		return instance;
	}

	//undo
	public UndoableEditSupport getUndoableEditSupport( 
		final Dependency _dependency )
	{
		if (_dependency.getPredecessor() == null)
		{
			return null;
		}
		else
		{
			DataFactoryUndoController c = ((Task)_dependency.getPredecessor()).getProject().getNodeModelDataFactory()
				.getUndoController();

			if (c == null)
			{
				return null;
			}

			return c.getEditSupport();
		}
	}

	//for deserialization
	public void initDependency( 
		final Dependency _dependency,
		final HasDependencies _predecessor,
		final HasDependencies _successor,
		final Object _eventSource )
		throws InvalidAssociationException
	{
		_dependency.setPredecessor( _predecessor );
		_dependency.setSuccessor( _successor );

		if (!_dependency.isDisabled()) // allow for calling a second time once invalidated
		{
			_dependency.testValid( false ); // throws if exception
		}

		connect( _dependency, _eventSource );
	}

	public Dependency newDependency( 
		final HasDependencies _predecessor,
		final HasDependencies _successor,
		final int _dependencyType,
		final long _lead,
		final Object _eventSource )
		throws InvalidAssociationException
	{
		if (_predecessor == _successor)
		{
			throw new InvalidAssociationException( Messages.getString( "Message.cantLinkToSelf" ) );
		}

		final Task successorTask = (Task)_successor;

		if (successorTask.isExternal() == true)
		{
			throw new InvalidAssociationException(Messages.getString( "Message.cantLinkToExternal" ));
		}

		if (successorTask.isSubproject() && !((SubProj)successorTask).isWritable())
		{
			throw new InvalidAssociationException(Messages.getString( "Message.cantLinkToClosedSubproject" ));
		}

		final Dependency dependency = Dependency.getInstance( _predecessor, _successor, _dependencyType, _lead );
		dependency.testValid( false ); // throws if exception
		connect( dependency, _eventSource );
		dependency.setDirty( true );

		return dependency;
	}

	public static <T> void prepareToRemove( 
		final Collection<T> _dependencyList,
		final Collection<T> _toRemove )
	{
		Iterator<T> itor = _dependencyList.iterator();

		while (itor.hasNext() == true)
		{
			_toRemove.add( itor.next() );
		}
	}

	public void remove( 
		final Dependency _dependency,
		final Object _eventSource,
		final boolean _undo )
	{
		_dependency.setDirty( true ); //for setGroupDirty()
		_dependency.getPredecessor().getSuccessorList().remove( _dependency );
		_dependency.getSuccessor().getPredecessorList().remove( _dependency );
		updateSentinels( _dependency );

		if (_eventSource != null)
		{
			_dependency.fireDeleteEvent( _eventSource );
		}

		final UndoableEditSupport undoableEditSupport = getUndoableEditSupport( _dependency );

		if (_undo && (undoableEditSupport != null) && !(_eventSource instanceof UndoableEdit))
		{
			undoableEditSupport.postEdit( new DependencyDeletionEdit( _dependency, _eventSource ) );
		}
	}

	public void remove( 
		final Collection<Dependency> _dependencyList,
		final Object _eventSource )
	{
		final Iterator<Dependency> itor = _dependencyList.iterator();
		while (itor.hasNext() == true)
		{
			final Dependency dependency = itor.next();
			remove( dependency, _eventSource, true );
		}
	}

	/**
	 * Remove all dependencies between all tasks in an array
	 * @param _tasks
	 * @param _eventSource
	 */
	public void removeAnyDependencies( 
		final List _tasks,
		final Object _eventSource )
	{
		HasDependencies pred;
		HasDependencies succ;
		Object temp;

		for (int i = 0; i < (_tasks.size() - 1); i++)
		{
			temp = _tasks.get( i );

			if (temp instanceof HasDependencies == false)
			{
				continue;
			}

			pred = (HasDependencies)temp;

			for (int j = i + 1; j < _tasks.size(); j++)
			{
				temp = _tasks.get( j );

				if (!(temp instanceof HasDependencies))
				{
					continue;
				}

				succ = (HasDependencies)temp;
				removeAnyDependencies( pred, succ, _eventSource );
			}
		}
	}

	public void removeAnyDependencies( 
		final HasDependencies _first,
		final HasDependencies _second,
		final Object _eventSource )
	{
		Dependency dependency;

		if ((_first == null) || (_second == null))
		{
			return;
		}

		if ((dependency = (Dependency)_first.getPredecessorList().findLeft( _second )) != null)
		{
			remove( dependency, _eventSource, true );
		}

		if ((dependency = (Dependency)_second.getPredecessorList().findLeft( _first )) != null)
		{
			remove( dependency, _eventSource, true );
		}

		if ((dependency = (Dependency)_first.getSuccessorList().findRight( _second )) != null)
		{
			remove( dependency, _eventSource, true );
		}

		if ((dependency = (Dependency)_second.getSuccessorList().findRight( _first )) != null)
		{
			remove( dependency, _eventSource, true );
		}
	}

	public boolean removeEndSentinel( 
		final HasDependencies _sentinel,
		final HasDependencies _task )
	{
		Dependency dependency = (Dependency)_sentinel.getPredecessorList().findLeft( _task );

		if (dependency != null)
		{
			_sentinel.getPredecessorList().remove( dependency );

			return true;

			//		System.out.println("removing end sentinel dependency task is " + dependency.getPredecessor());
		}

		return false;
	}

	public boolean removeStartSentinel( 
		final HasDependencies _sentinel,
		final HasDependencies _task )
	{
		final Dependency dependency = (Dependency)_sentinel.getSuccessorList().findRight( _task );

		if (dependency != null)
		{
			_sentinel.getSuccessorList().remove( dependency );

			return true;

//			System.out.println("removing start sentinel dependency task is " + dependency.getSuccessor());
		}

		return false;
	}

	public void setFields( 
		final Dependency _dependency,
		final long _lag,
		final int _type,
		final Object _eventSource )
		throws InvalidAssociationException
	{
//		if (eventSource != null)
//			dependency.getDocument().getObjectEventManager().fireUpdateEvent(eventSource,dependency);

		final long oldLag = _dependency.getLag();
		final int oldType = _dependency.getDependencyType();
		_dependency.setLag( _lag );
		_dependency.setDependencyType( _type );
		_dependency.setDirty( true );

		UndoableEditSupport undoableEditSupport = getUndoableEditSupport( _dependency );

		if ((undoableEditSupport != null) && !(_eventSource instanceof UndoableEdit))
		{
			undoableEditSupport.postEdit( new DependencySetFieldsEdit( _dependency, oldLag, oldType, _eventSource ) );
		}
	}

	public void update( 
		final Dependency _dependency,
		final Object _eventSource )
	{
		if (_eventSource != null)
		{
			_dependency.fireUpdateEvent( _eventSource );
		}

		_dependency.setDirty( true );
	}

	// update the starting and ending sentinels of the project - the sentinels keep track of which
	//	tasks have no preds or no successors
	public void updateSentinels( 
		final Dependency _dependency )
	{
		final Task predecessor = (Task)_dependency.getPredecessor();
		final Task successor = (Task)_dependency.getSuccessor();
		predecessor.updateEndSentinel();
		successor.updateStartSentinel();
	}

	private static DependencyService instance = null;
}
