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

import com.projity.datatype.Hyperlink;
import com.projity.datatype.Money;

import com.projity.dialog.FieldDialog;

import com.projity.field.Field;
import com.projity.field.FieldContext;
import com.projity.field.FieldParseException;
import com.projity.field.ObjectRef;
import com.projity.field.Range;
import com.projity.field.StaticSelect;
import com.projity.grouping.core.model.NodeModelUtil;

import com.projity.options.CalendarOption;
import com.projity.options.EditOption;

import com.projity.pm.graphic.spreadsheet.editor.SpinEditor;
import com.projity.pm.task.AccessControlPolicy;
import com.projity.pm.task.Project;

import com.projity.strings.Messages;

import com.projity.util.Alert;
import com.projity.util.ClassUtils;
import com.projity.util.DateTime;
import com.projity.util.MathUtils;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.text.DateFormat;

import java.util.Date;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.JTextComponent;


/**
 *
 */
public class ComponentFactory
{
	/**
	 *
	 */
	public ComponentFactory()
	{
		super();
	}

	private static JComponent componentFor( 
		final Field _field,
		final Object _value,
		final boolean _readOnly )
	{
		JComponent component = null;
		final Range range = _field.getRange();
		JTextComponent text = null;

		if (_value instanceof Boolean == true)
		{
			component = new JCheckBox( _field.getName(), ((Boolean)_value).booleanValue() );
		}
		else if (_readOnly == true)
		{
			if (_field.isHyperlink() == true)
			{
				component = new LinkLabel( (Hyperlink)_value );
			}
			else
			{
				component = new JLabel();
			}
		}
		else if (_field.isDate() == true)
		{
			ExtDateField d = createDateField( _field );
			Object o = d.getComponents();
			d.getFormattedTextField().addActionListener( new FieldVerifier.VerifierListener() );
			component = d;
			text = (JTextComponent)verifiedComponent( component );

//			text.setEnabled(false);
		}
		else if (_field.getLookupTypes() != null)
		{
			component = new LookupField( _field, null );
		}
		else if (_field.hasOptions() == true)
		{
			final JComboBox combo = new JComboBox( _field.getOptions( null ) );

//			if ("Field.accessControlPolicy".equals(field.getId())){
//				combo.setInputVerifier(new InputVerifier(){
//					@Override
//					public boolean verify(JComponent input) {
//						return Alert.okCancel(Messages.getString("Text.resetRoles"));
//					}
//				});
//			}

//			else
			combo.addActionListener( new FieldVerifier.VerifierListener() );
			component = combo;

			// if the combo can change dynamically, need to rebuild the combo dynamically
			if (_field.isDynamicOptions() == true)
			{
				combo.getComponent( 0 ).addMouseListener( 
					new MouseListener()
				{
					@Override
					public void mouseClicked( 
						final MouseEvent _event )
					{
						// do nothing
					}

					@Override
					public void mouseEntered( 
						final MouseEvent _event )
					{
						// do nothing
					}

					@Override
					public void mouseExited( 
						final MouseEvent _event )
					{
						// do nothing
					}

					@Override
					public void mousePressed( 
						final MouseEvent _event )
					{
						combo.setModel( new DefaultComboBoxModel(_field.getOptions( null )) );
						combo.showPopup();
					}

					@Override
					public void mouseReleased( 
						final MouseEvent _event )
					{
						// do nothing
					}
				} );
			}
		}
		else if (range != null)
		{
			component = SpinEditor.getJSpinnerInstance( _field, ((Number)_value).doubleValue(), false );

			final JSpinner spinner = (JSpinner)component;
			text = getSpinnerTextField( spinner );

			final JTextComponent t = text;
			((JSpinner)component).addChangeListener( new ChangeListener()
			{
				@Override
				public void stateChanged( 
					ChangeEvent _arg0 )
				{
					FieldVerifier v = (FieldVerifier)t.getInputVerifier();
					boolean same = true;

					if (v != null)
					{
						Number spinnerValue = (Number)spinner.getValue();

						if (spinnerValue == null)
						{
							return;
						}

						double spin = MathUtils.roundToDecentPrecision( spinnerValue.doubleValue() );

						if ((v == null) || (v.getValue() == null))
						{
							same = false;
						}
						else
						{
							same = spin == ((Number)v.getValue()).doubleValue();
						}
					}

					t.setForeground( same
						? Color.BLACK
						: Color.BLUE );
				}
			} );
		}
		else
		{
			if (_field.isMemo() == true)
			{
				text = new JTextArea();
				component = new JScrollPane(text);
			}
			else
			{
				text = new JTextField();

				int width = _field.getTextWidth( null, null );

				if (width != Integer.MAX_VALUE)
				{
					((AbstractDocument)text.getDocument()).setDocumentFilter( new FixedSizeFilter(width) );
				}

				component = text;
			}
		}

		if (text != null)
		{
			text.addKeyListener( new KeyListener()
			{
				@Override
				public void keyPressed( 
					KeyEvent _arg0 )
				{
				}

				@Override
				public void keyReleased( 
					KeyEvent _arg0 )
				{
				}

				@Override
				public void keyTyped( 
					KeyEvent _arg0 )
				{
					JTextComponent textComponent = (JTextComponent)_arg0.getComponent();
					textComponent.setForeground( Color.BLUE );

					FieldDialog parentFieldDialog = getParentFieldDialog( textComponent );

					if (parentFieldDialog != null)
					{
						parentFieldDialog.setDirtyComponent( textComponent );
					}
				}
			} );
		}

		if (component instanceof JCheckBox == false)
		{
			setValueOfComponent( component, _value, _readOnly );
		}

		return component;
	}

	/** Get the component to use for the field
	 *
	 * @param _field
	 * @param _objectRef
	 * @param _flag can be READ_ONLY to force the field as a label, or SOMETIMES_READ_ONLY, in which case the component will
	 *				not be a label even if the field is read only.  value can also be 0 for default case
	 * @return
	 */
	public static JComponent componentFor( 
		final Field _field,
		final ObjectRef _objectRef,
		final int _flag )
	{
		final Object value = getFieldValue( _field, _objectRef );

		boolean readOnly = (_flag & READ_ONLY) != 0;
		boolean sometimesReadOnly = (_flag & SOMETIMES_READ_ONLY) != 0;
		boolean fieldReadOnly = NodeModelUtil.isReadOnly( _field, _objectRef, myContext );
		readOnly |= fieldReadOnly;

		if (sometimesReadOnly == true)
		{
			readOnly = false;
		}

		final JComponent component = componentFor( _field, value, readOnly );

		if (component instanceof LookupField == true)
		{ 
			//checkboxes update immediately on clicking
			((LookupField)component).addChangeListener( new FieldChangeListener( _field, _objectRef ) );
		}
		else if (readOnly == false)
		{
			if (component instanceof JCheckBox == true)
			{	
				//checkboxes update immediately on clicking
				((JCheckBox)component).addActionListener( new FieldChangeListener( _field, _objectRef ) );

				//((JCheckBox)component).addItemListener(new FieldChangeListener(field,objectRef));
			}
			else
			{
				//An exception for accessControlPolicy to have the correct behaviour
				if ("Field.accessControlPolicy".equals( _field.getId() ))
				{ 
					//TODO remove this hack and do it properly. This code should all be generic
					component.setInputVerifier( 
						new FieldVerifier( _field, _objectRef, getValueFromComponent( component, _field ))
					{
						@Override
						public boolean verify( 
							JComponent component )
						{
							final JComboBox c = (JComboBox)component;
							final Object newValue = (Object)ComponentFactory.getValueFromComponent( component, field );
							final Object publicValue = c.getItemAt( AccessControlPolicy.PUBLIC );

							if (newValue != value)
							{
								final Integer oldValue = (Integer)field.getValue( objectRef, myContext );

								try
								{
									//testing = true;
									if ((oldValue == AccessControlPolicy.RESTRICTED) && publicValue.equals( newValue ))
									{
										if (Alert.okCancel( Messages.getString( "Text.resetRoles" ) ))
										{
											final Project project = (Project)objectRef.getObject();
											project.resetRoles( true );
											field.setValue( objectRef, source, newValue, myContext );

											return true;
										}
										else
										{
											c.setSelectedIndex( oldValue );

											return false;
										}
									}
									else
									{
										field.setValue( objectRef, source, newValue, myContext );
									}
								}
								catch (FieldParseException e)
								{
									//never happen
								}
							}

							return true;
						}

						@Override
						public boolean shouldYieldFocus( 
							final JComponent _component )
						{
							return true;
						}
					} );
				}
				else
				{
					setVerifier( component, new FieldVerifier( _field, _objectRef, getValueFromComponent( component, _field )) );
				}
			}
		}
		else
		{
			if (component instanceof JLabel == true)
			{
				((JLabel)component).setText( NodeModelUtil.getText( _field, _objectRef, myContext ) );
			}
		}

		// if field may be read only, set its status based on field read only
		if ((sometimesReadOnly == true) 
		 && (fieldReadOnly == true))
		{
			component.setEnabled( false );
		}

		return component;
	}

	public static ExtDateField createDateField()
	{
		return createDateField( null );
	}

	public static ExtDateField createDateField( 
		final Field _field )
	{
		long date = DateTime.midnightToday();
		DateFormat format;

		if (_field != null)
		{
			if (_field.isDateOnly() == true)
			{
				format = EditOption.getInstance().getShortDateFormat();
			}
			else
			{
				format = EditOption.getInstance().getDateFormat();
			}

			if (_field.isStartValue() == true)
			{
				date = CalendarOption.getInstance().makeValidStart( date, true );
			}
			else if (_field.isEndValue() == true)
			{
				date = CalendarOption.getInstance().makeValidEnd( date, true );
			}
		}
		else
		{
			format = EditOption.getInstance().getShortDateFormat();
		}

		ExtDateField df = new ExtDateField(format);

		//	    df.setValue(new Date(date));
		df.getFormattedTextField().addPropertyChangeListener( 
			new PropertyChangeListener()
		{
			@Override
			public void propertyChange( 
				PropertyChangeEvent _event )
			{
				JFormattedTextField field = (JFormattedTextField)_event.getSource();

				if ((_event.getPropertyName().equals( "value" ) == true) 
				 && (_event.getNewValue() != _event.getOldValue()))
				{
					if (field.getInputVerifier() != null)
					{
						field.getInputVerifier().verify( field );
					}
				}
			}
		} );

		return df;
	}

	private static JComponent getFieldComponent( 
		JComponent _component )
	{
		if (_component instanceof JScrollPane == true)
		{
			_component = (JComponent)((JScrollPane)_component).getViewport().getComponent( 0 );
		}

		return _component;
	}

	public static Object getFieldValue( 
		final Field _field,
		final ObjectRef _objectRef )
	{
		if (_field.hasOptions() == true)
		{
			return NodeModelUtil.getText( _field, _objectRef, myContext );
		}

		Object value = NodeModelUtil.getValue( _field, _objectRef, myContext );

		if ((value != null) 
		 && (_field.isDate() == true))
		{
			value = new Date( DateTime.dayFloor( ((Date)value).getTime() ) );
		}

		return value;
	}

	public static FieldDialog getParentFieldDialog( 
		Component _component )
	{
		while ((_component != null) && !(_component instanceof FieldDialog))
		{
			_component = _component.getParent();
		}

		return (FieldDialog)_component;
	}

	static JTextField getSpinnerTextField( 
		final JSpinner _spinner )
	{
		return ((JSpinner.DefaultEditor)_spinner.getEditor()).getTextField();
	}

	public static Object getValueFromComponent( 
		JComponent _component,
		final Field _field )
	{
		_component = getFieldComponent( _component );

		if (_component instanceof JTextField == true)
		{
			return ((JTextField)_component).getText();
		}
		else if (_component instanceof JTextArea == true)
		{
			return ((JTextArea)_component).getText();
		}
		else if (_component instanceof JCheckBox == true)
		{
			return Boolean.valueOf(((JCheckBox)_component).isSelected());
		}
		else if (_component instanceof ExtDateField)
		{
			return ((ExtDateField)_component).getDateValue();
		}
		else if (_component instanceof JComboBox == true)
		{
			return ((JComboBox)_component).getSelectedItem();
		}
		else if (_component instanceof JSpinner == true)
		{
			return SpinEditor.getValue( (JSpinner)_component, _field );
		}

		return null;
	}

	static void markComponentAsUnmodified( 
		final JComponent _component )
	{
		_component.setForeground( Color.BLACK );
		verifiedComponent( _component ).setForeground( Color.BLACK );
	}

	public static void setValueOfComponent( 
		JComponent _component,
		Object _value,
		final boolean _readOnly )
	{
		final boolean isMultipleValues = ClassUtils.isMultipleValue( _value );
		_component = getFieldComponent( _component );

		if (_component instanceof JTextField == true)
		{
			((JTextField)_component).setText( (_value == null)
				? ""
				: (isMultipleValues
					? ""
					: _value.toString()) );
		}
		else if (_component instanceof JTextArea == true)
		{
			((JTextArea)_component).setText( (_value == null)
				? ""
				: (isMultipleValues
					? ""
					: _value.toString()) );
		}

//		((JTextArea)component).setText(isMultipleValues ? "" : value.toString());
		//TODO fix?
		else if (_component instanceof JCheckBox == true)
		{
			((JCheckBox)_component).setSelected( (_value == null)
				? false
				: ((Boolean)_value).booleanValue() );
		}
		else if (_component instanceof ExtDateField == true)
		{
			if (DateTime.getZeroDate().equals( _value ))
			{
				_value = null;
			}

			((ExtDateField)_component).setValue( (_value == null)
				? null
				: _value );
		}
		else if (_component instanceof JComboBox == true)
		{
			if ((_value == null) || "".equals( _value ))
			{
				((JComboBox)_component).setSelectedIndex( 0 );
			}
			else
			{
				((JComboBox)_component).setSelectedItem( _value );
			}
		}
		else if ((_component instanceof JSpinner == true) && 
			     (_value != null))
		{
			((JSpinner)_component).setValue( _value );

			if (isMultipleValues == true)
			{ 
				// set editor to empty.  Unfortunately, this disables the spinner
				getSpinnerTextField( (JSpinner)_component ).setText( "" );
			}
			else
			{
				//TODO make escape key work properly by putting back original value
				//				getSpinnerTextField((JSpinner)component).setText(value.toString());
				//				System.out.println("setting spinner text " + value);
			}
		}
		else if (_component instanceof LinkLabel == true)
		{
			((LinkLabel)_component).setHyperlink( (Hyperlink)_value );
		}
		else if (_component instanceof JLabel == true)
		{
			((JLabel)_component).setText( (_value == null)
				? ""
				: _value.toString() );
		}
		else if (_component instanceof LookupField == true)
		{
			((LookupField)_component).setText( (_value == null)
				? ""
				: _value.toString() );
		}

		_component.setEnabled( !_readOnly );
		markComponentAsUnmodified( _component );
	}

	/**
	 * Use java 1.4s InputVerifier class to check value on focus loss
	 * @param _component
	 * @param _verifier
	 */
	private static void setVerifier( 
		JComponent _component,
		FieldVerifier _verifier )
	{
		_component = getFieldComponent( _component );

		final JComponent verifiedComponent = verifiedComponent( _component );
		verifiedComponent.setInputVerifier( _verifier );
	}

	public static void updateValueOfComponent( 
		final JComponent _component,
		final Field _field,
		final ObjectRef _objectRef )
	{
		Object value;
		boolean readOnly = _component instanceof JLabel /*|| field.isObjectReadOnly(objectRef.getObject())*/ 
			|| NodeModelUtil.isReadOnly( _field, _objectRef, myContext );

		// This is ugly
		if (_component instanceof LinkLabel == true)
		{
			value = NodeModelUtil.getValue( _field, _objectRef, myContext );
		}
		else if (_component instanceof LookupField == true)
		{
			value = NodeModelUtil.getValue( _field, _objectRef, myContext );
		}
		else if (_component instanceof JLabel == true)
		{
			value = NodeModelUtil.getText( _field, _objectRef, myContext );
		}
		else if ((_field.hasOptions() == true) 
			  && (_field.getSelect() instanceof StaticSelect == true) 
			  && (_field.getSelect().isStatic() == true))
		{
			value = NodeModelUtil.getText( _field, _objectRef, myContext );

			if (value instanceof String && (((String)value).length() == 0))
			{
				value = null;
			}
		}
		else if ((_field.isDuration() == true) 
			  || (_field.isRate() == true))
		{
			value = NodeModelUtil.getText( _field, _objectRef, myContext );
		}
		else
		{
			value = NodeModelUtil.getValue( _field, _objectRef, myContext );
		}

		if ((readOnly == false) 
		 && (_field.isDate() == true) 
		 && (value != null))
		{
			if (value instanceof Date == false)
			{
				System.out.println( "bad date" );
				value = NodeModelUtil.getValue( _field, _objectRef, myContext );

				return;
			}
		}

		if (value instanceof Money == true)
		{
			value = ((Money)value).getFormattedValue();
		}

		final JComponent verifiedComponent = verifiedComponent( _component );
		final FieldVerifier verifier = (FieldVerifier)verifiedComponent.getInputVerifier();

		if (verifier != null)
		{
			verifier.setUpdating( true );
		}

		setValueOfComponent( _component, value, readOnly );

		// Need to update cached value of field verifier also
		if (verifier != null)
		{
			verifier.setValue( value );
			verifier.setUpdating( false );
		}

		markComponentAsUnmodified( _component );
	}

	static JComponent verifiedComponent( 
		final JComponent _component )
	{
		if (_component instanceof ExtDateField == true)
		{ 
			// The editor component is what we want
			return (JComponent)((ExtDateField)_component).getFormattedTextField();
		}
		else if (_component instanceof JSpinner == true)
		{ 
			// spinners are strange.  See http://mindprod.com/jgloss/focus.html
			return getSpinnerTextField( (JSpinner)_component );
		}
		else if (_component instanceof LookupField == true)
		{
			return ((LookupField)_component).getDisplay();
		}
		else
		{
			// for normal fields, the component itself is verified
			return _component; 
		}
	}

	public static final int READ_ONLY = 1;
	public static final int SOMETIMES_READ_ONLY = 2;
	private static double MAX_VALUE = 60000000.0;

	private static FieldContext myContext = null;
}
