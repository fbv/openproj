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
package com.projity.dialog;

import com.projity.configuration.FieldDictionary;

import com.projity.help.HelpUtil;

import com.projity.menu.HyperLinkToolTip;
import com.projity.menu.MenuManager;

//import com.projity.pm.graphic.frames.DocumentFrame;
//import com.projity.pm.graphic.frames.GraphicManager;
//import com.projity.pm.graphic.model.cache.NodeModelCache;
//import com.projity.pm.graphic.model.cache.ReferenceNodeModelCache;

import com.projity.strings.Messages;

import com.projity.util.BrowserControl;
import com.projity.util.Environment;

import org.apache.commons.collections.Closure;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JToolTip;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;

/**
 *
 */
public abstract class AbstractDialog
	extends JDialog
{
	//protected MainFrame main;
	public AbstractDialog()
	{
		super();
	}

	public AbstractDialog( 
		Frame owner /*, MainFrame main*/,
		String title,
		boolean modal )
	{
		super(owner, title, modal);
		createRootPane();
		setLocationRelativeTo( null );
		this.owner = owner;
	}

	protected void activateListeners()
	{
		listenersActivated = true;
	}

	public void addDocHelp( 
		String helpAddress )
	{
		HelpUtil.addDocHelp( this, helpAddress );
		this.helpAddress = helpAddress;
	}

	protected boolean bind( 
		boolean get )
	{
		return true;
	}

	protected boolean bindAfterOk()
	{
		return isModal() && Environment.isNoPodServer();
	}

	protected void clearComponents()
	{
		if (contentPanel != null)
		{
			getContentPane().remove( contentPanel );
		}

		if (buttonPanel != null)
		{
			getContentPane().remove( buttonPanel );
		}
	}

	public static JDialog containedInDialog( 
		Object object )
	{
		if (!(object instanceof Component))
		{
			return null;
		}

		Component c = (Component)object;

		while (c != null)
		{
			if (c instanceof JDialog)
			{
				return (JDialog)c;
			}

			c = c.getParent();
		}

		return null;
	}

	public JComponent createBannerPanel()
	{
		return null;
	}

	public ButtonPanel createButtonPanel()
	{
		if (!hasOkAndCancelButtons() && !hasCloseButton())
		{
			return null;
		}

		if (hasCloseButton())
		{
			createCloseButton();
		}
		else
		{
			createOkCancelButtons();
		}

		ButtonPanel buttonPanel = new ButtonPanel();
		buttonPanel.addButton( ok );

		if (hasOkAndCancelButtons())
		{
			buttonPanel.addButton( cancel );
		}

		if (hasHelpButton())
		{
			buttonPanel.add( getHelpButton() );
		}

		buttonPanel.setBorder( new EmptyBorder(0, 0, 4, 0) );

		return buttonPanel;
	}

//	public NodeModelCache createCache( 
//		boolean task,
//		String viewName )
//	{
//		//		DocumentFrame df = ((MainFrame) owner).getCurrentFrame();
//		DocumentFrame df = GraphicManager.getInstance( this ).getCurrentFrame();
//
//		return df.createCache( task, viewName );
//	}

	protected void createCloseButton()
	{
		ok = new JButton(Messages.getString( "ButtonText.Close" ));
		ok.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( 
				ActionEvent e )
			{
				AbstractDialog.this.onOk();
			}
		} );
	}

	public abstract JComponent createContentPanel();

	protected void createOkCancelButtons( 
		String okText,
		String cancelText )
	{
		ok = new JButton( okText );
		ok.setEnabled( initialOkEnabledState() );
		ok.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( 
				ActionEvent e )
			{
				AbstractDialog.this.onOk();
			}
		} );

		cancel = new JButton( cancelText );
		cancel.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( 
				ActionEvent e )
			{
				AbstractDialog.this.onCancel();
			}
		} );
	}

	protected void createOkCancelButtons()
	{
		createOkCancelButtons( Messages.getString( "ButtonText.OK" ), Messages.getString( "ButtonText.Cancel" ) );
	}

	// see http://www.javaworld.com/javaworld/javatips/jw-javatip72.html
	@Override
	protected JRootPane createRootPane()
	{
		ActionListener escapeListener = new ActionListener()
		{
			@Override
			public void actionPerformed( 
				ActionEvent actionEvent )
			{
				onCancel();
			}
		};

		ActionListener enterListener = new ActionListener()
		{
			@Override
			public void actionPerformed( 
				ActionEvent actionEvent )
			{
				onOk();
			}
		};

		ActionListener helpListener = new ActionListener()
		{
			@Override
			public void actionPerformed( 
				ActionEvent actionEvent )
			{
				onHelp();
			}
		};

		JRootPane rootPane = new JRootPane();
		KeyStroke escapeStroke = KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0 );
		rootPane.registerKeyboardAction( escapeListener, escapeStroke, JComponent.WHEN_IN_FOCUSED_WINDOW );

		KeyStroke enterStroke = KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, 0 );
		rootPane.registerKeyboardAction( enterListener, enterStroke, JComponent.WHEN_IN_FOCUSED_WINDOW );

		KeyStroke f1Stroke = KeyStroke.getKeyStroke( KeyEvent.VK_F1, 0 );
		rootPane.registerKeyboardAction( helpListener, f1Stroke, JComponent.WHEN_IN_FOCUSED_WINDOW );

		return rootPane;
	}

	protected void desactivateListeners()
	{
		listenersActivated = false;
	}

	public boolean doModal()
	{
		prepareModal( true );

		pack();
		setLocationRelativeTo( getParent() );

		// to center on parent
		if (!showEnabled)
		{
			enableContent( false );
		}

		setVisible( true );

		boolean result = (getDialogResult() != JOptionPane.CANCEL_OPTION);
		prepareModal( false );

		if (!showEnabled)
		{
			enableContent( true );
		}

		return result;
	}

	private void enableContent( 
		boolean enable )
	{
		getContentPanel().setEnabled( enable );

		for (Component c : getContentPanel().getComponents())
		{
			c.setEnabled( enable );
		}

		for (Component c : getButtonPanel().getComponents())
		{
			if (c != cancel) // only cancel button is allowed
			{
				c.setEnabled( enable );
			}
		}
	}

	public int execute( 
		Closure setter,
		Closure getter )
	{
		pack();
		setter.execute( getBean() );
		bind( true );
		setLocationRelativeTo( null );

		// to center on screen
		setVisible
		// to center on screen
		( true );

		if (getDialogResult() != JOptionPane.CANCEL_OPTION)
		{
			// bind(false); //already done in onOk
			if (getter != null)
			{
				getter.execute( getBean() );
			}
		}

		return getDialogResult();
	}

	public Object getBean()
	{
		return null;
	}

	public ButtonPanel getButtonPanel()
	{
		return buttonPanel;
	}

	// see com.projity.configuration.configuration.xml for available fieldId
	public static ComboBoxModel getComboBoxModel( 
		String fieldId )
	{
		Object[] options = FieldDictionary.getInstance().getFieldFromId( fieldId ).getOptions( null );

		return new DefaultComboBoxModel(options);
	}

	public JComponent getContentPanel()
	{
		return contentPanel;
	}

	public int getDialogResult()
	{
		return dialogResult;
	}

	protected JComponent getHelpButton()
	{
		if (help == null)
		{
			//	help= new JLabel(IconManager.getIcon("menu24.help"));
			help = new JButton(MenuManager.getMenuString( "Help.text" )); //,IconManager.getIcon("menu24.help"));

			//			help.setToolTipText(Messages.getString("Text.Help")); //$NON-NLS-1$
			help.addMouseListener( new MouseAdapter()
				{
					public void mouseClicked( 
						MouseEvent arg0 )
					{
						AbstractDialog.this.onHelp();
					}
				} );
		}

		return help;
	}

//	public ReferenceNodeModelCache getReferenceCache( 
//		boolean task )
//	{
//		//		DocumentFrame df = ((MainFrame) owner).getCurrentFrame();
//		DocumentFrame df = GraphicManager.getInstance( this ).getCurrentFrame();
//
//		return df.getReferenceCache( task );
//	}

	protected boolean hasCloseButton()
	{
		return false;
	}

	protected boolean hasHelp()
	{
		return helpAddress != null;
	}

	protected boolean hasHelpButton()
	{
		return hasHelp();
	}

	protected boolean hasOkAndCancelButtons()
	{
		return !hasCloseButton();
	}

	protected void initComponents()
	{
		buttonPanel = createButtonPanel();
		contentPanel = createContentPanel();
		getContentPane().setLayout( new BorderLayout() );

		if (contentPanel != null)
		{
			getContentPane().add( contentPanel, BorderLayout.CENTER );
		}

		if (buttonPanel != null)
		{
			getContentPane().add( buttonPanel, BorderLayout.AFTER_LAST_LINE );
		}
	}

	protected boolean initialOkEnabledState()
	{
		return true;
	}

	public boolean isShowEnabled()
	{
		return showEnabled;
	}

	/**
	 *
	 */
	protected void onCancel()
	{
		setVisible( false );
		setDialogResult( JOptionPane.CANCEL_OPTION );

		// desactivateListeners();
	}

	protected void onHelp()
	{
		if (helpAddress != null)
		{
			BrowserControl.displayURL( HelpUtil.getHelpURL( helpAddress ) );
		}
		else
		{
			System.out.println( "no help available" );
		}

		// TODO Auto-generated method stub
	}

	/**
	 *
	 */
	public void onOk()
	{
		if (showEnabled == false)
		{ // enter key for example
			onCancel();

			return;
		}

		if (bind( false ) == false)
		{
			return;
		}

		setDialogResult( JOptionPane.OK_OPTION );
		setVisible( false );

//		if (!bindAfterOk()) {
//			if (!bind(false))
//				return;
//			setDialogResult(JOptionPane.OK_OPTION);
//			setVisible(false);
//		} else {
//			System.out.println("Setting modal and visible false");
//			setModal(false);
//			setVisible(false);
//			getOwner().requestFocus();
//			try {
//				System.out.println("Sleeping");
//				Thread.sleep(1);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			if (!bind(false)) 
//				setVisible(true);
//			else 
//				setDialogResult(JOptionPane.OK_OPTION);
//			setModal(true);
//		}
	}

	@Override
	public void pack()
	{
		initComponents();
		super.pack();
	}

	//	protected JComponent createButtonPanel() {
	//	    JPanel panel = new JPanel(new BasicOptionPaneUI.ButtonAreaLayout(true, 6)) {
	//	        public Dimension getMaximumSize() {
	//	            return getPreferredSize();
	//	        }
	//	    };
	//
	//	    panel.setBorder(BorderFactory.createEmptyBorder(9, 0, 0, 0));
	//
	//
	//	    Action findAction = getAction(EXECUTE_ACTION_COMMAND);
	//	    Action closeAction = getAction(CLOSE_ACTION_COMMAND);
	//
	//	    JButton findButton = new JButton(findAction);
	//	    panel.add(findButton);
	//	    if (findAction != closeAction) {
	//	        panel.add(new JButton(closeAction));
	//	    }
	//
	//
	//	    KeyStroke enterKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false);
	//	    KeyStroke escapeKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
	//
	//	    InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
	//	    inputMap.put(enterKey, EXECUTE_ACTION_COMMAND);
	//	    inputMap.put(escapeKey, CLOSE_ACTION_COMMAND);
	//
	//	    getRootPane().setDefaultButton(findButton);
	//	    return panel;
	//	}
	public void prepareModal( 
		boolean start )
	{
		//		Project p = GraphicManager.getInstance().getCurrentProject();
		//		if (p == null)
		//			return;
		//		if (p.isLocked())
		//			return;
		//		if (start) {
		//			SessionFactory.getInstance().getSession(false).ping(); // may issue popup if timeout
		////			SessionFactory.getInstance().getSession(false).heartbeat(true);
		//		} else {
		////			SessionFactory.getInstance().getSession(false).heartbeat(false);
		//		}
		//		
	}

	public void setButtonPanel( 
		ButtonPanel buttonPanel )
	{
		this.buttonPanel = buttonPanel;
	}

	public void setContentPanel( 
		JComponent contentPanel )
	{
		this.contentPanel = contentPanel;
	}

	public void setDialogResult( 
		int dialogResult )
	{
		this.dialogResult = dialogResult;
	}

	public void setShowEnabled( 
		boolean showEnabled )
	{
		this.showEnabled = showEnabled;
	}

	public void setVisible( 
		boolean b )
	{
		if (b && !listenersActivated)
		{
			activateListeners();
		}
		else if (!b && listenersActivated)
		{
			desactivateListeners();
		}

		super.setVisible( b );
	}

	public class DoubleClickRadio
		extends JRadioButton
		implements MouseListener
	{
		public DoubleClickRadio( 
			String label,
			String tooltip )
		{
			super(label);
			this.setToolTipText( tooltip );
			addMouseListener( this );
		}

		public JToolTip createToolTip()
		{
			JToolTip tip = new HyperLinkToolTip();
			tip.setComponent( this );

			return tip;
		}

		public Point getToolTipLocation( 
			MouseEvent event )
		{ // the tip MUST be touching the button if html because you can click on links

			return new Point(getWidth() - 2, -20);
		}

		public void mouseClicked( 
			MouseEvent e )
		{
			if (e.getClickCount() == 2)
			{
				bind( false );
				((JRadioButton)e.getSource()).setSelected( true );
				AbstractDialog.this.onOk();
			}
		}

		public void mouseEntered( 
			MouseEvent e )
		{
		}

		public void mouseExited( 
			MouseEvent e )
		{
		}

		public void mousePressed( 
			MouseEvent e )
		{
		}

		public void mouseReleased( 
			MouseEvent e )
		{
		}

		private static final long serialVersionUID = 1L;
	}

	protected ButtonPanel buttonPanel = null;
	protected Frame owner;
	protected JButton cancel;
	protected JButton ok;
	protected JComponent contentPanel = null;
	protected JComponent help;
	private String helpAddress = null;
	protected boolean listenersActivated = true;
	private boolean showEnabled = true;
	private int dialogResult = JOptionPane.CANCEL_OPTION;
}
