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
package com.projity.dialog.util;

import com.projity.field.Field;
import com.projity.field.FieldContext;
import com.projity.field.FieldParseException;
import com.projity.field.FieldSetOptions;
import com.projity.field.ObjectRef;

import com.projity.grouping.core.model.NodeModel;
import com.projity.grouping.core.model.NodeModelDataFactory;
import com.projity.grouping.core.model.NodeModelUtil;

import com.projity.strings.Messages;

import com.projity.undo.DataFactoryUndoController;
import com.projity.undo.FieldEdit;

import com.projity.util.Alert;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.undo.UndoableEditSupport;

/**
 * Currently only supports Buttons!
 */
public class FieldChangeListener
	implements ItemListener,
		ChangeListener,
		ActionListener
{
	/**
	 * @param value TODO
	 *
	 */
	public FieldChangeListener( 
		final Field _field,
		final ObjectRef _objectRef )
	{
		super();
		this.field = _field;
		this.objectRef = _objectRef;
	}

	@Override
	public void actionPerformed( 
		final ActionEvent _evt )
	{
		Object source = _evt.getSource();

		//Boolean value = (evt.getStateChange() == ItemEvent.SELECTED) ? Boolean.TRUE : Boolean.FALSE;
		if (source instanceof JCheckBox)
		{
			Boolean value = ((JCheckBox)_evt.getSource()).isSelected();

			try
			{
				if ((objectRef.getNode() != null) && objectRef.getNodeModel() instanceof NodeModel) //use nodeModel with undo
				{
					((NodeModel)objectRef.getNodeModel()).setFieldValue( field, objectRef.getNode(), source, value, context,
						NodeModel.NORMAL );
				}
				else
				{
					FieldSetOptions options = null;
					UndoableEditSupport undoableEditSupport = null;
					Object obj = objectRef.getObject();

					if ((obj != null) && obj instanceof NodeModelDataFactory)
					{
						DataFactoryUndoController undoController = ((NodeModelDataFactory)obj).getUndoController();
						undoableEditSupport = undoController.getEditSupport();
						options = new FieldSetOptions();
					}

					options.setRawUndo( true );
					NodeModelUtil.setValue( field, objectRef, source, value, context, options );

					if (undoableEditSupport == null)
					{
						Alert.warn( Messages.getString( "Text.CannotUndoAction" ) );
					}

					if ((undoableEditSupport != null) && (options.getChange() != null))
					{
						FieldSetOptions undoOptions = new FieldSetOptions();
						undoOptions.setRawProperties( true );
						undoableEditSupport.postEdit( new FieldEdit(field, obj, options.getChange().getNewValue(),
								options.getChange().getOldValue(), source, context, undoOptions) );
					}
				}
			}
			catch (final FieldParseException _e)
			{
				Alert.error( _e.getMessage() );
				((JComponent)source).requestFocus();
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	@Override
	public void itemStateChanged( 
		final ItemEvent _evt )
	{
		//		Object source = evt.getSource();
		//		Boolean value = (evt.getStateChange() == ItemEvent.SELECTED) ? Boolean.TRUE : Boolean.FALSE;
		//		try {
		//			field.setValue(objectRef,source,value,context);
		//		} catch (FieldParseException e) {
		//			Alert.error(e.getMessage());
		//			((JComponent)source).requestFocus();
		//		}
	}

	@Override
	public void stateChanged( 
		final ChangeEvent _e )
	{
		if (_e.getSource() instanceof LookupField)
		{
			LookupField f = (LookupField)_e.getSource();

			try
			{
				NodeModelUtil.setText( field, objectRef, f.getValue(), context );
			}
			catch (final FieldParseException _e1)
			{
				// TODO Auto-generated catch block
				_e1.printStackTrace();
			}
		}

		// TODO Auto-generated method stub
	}

	private Field field;
	private FieldContext context = null;
	private ObjectRef objectRef = null;
}
