package com.projity.dialog.util;

import com.projity.dialog.assignment.AssignmentDialog;

import com.projity.field.Field;

import com.projity.pm.graphic.IconManager;
import com.projity.pm.graphic.frames.GraphicManager;

/**
 *
 */
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.openproj.domain.task.Task;


/**
 * @author avigil
 *
 */
public class ResourceNamesField
	extends JPanel
{
	public ResourceNamesField( 
		String resourceNames )
	{
		display = createLinkLabel( resourceNames );
		setLayout( new BorderLayout() );
		add( display, BorderLayout.CENTER );
	}

	/**
	 *
	 */
	public ResourceNamesField( 
		String resourceNames,
		Field f,
		Task task )
	{
		display = createLinkLabel( resourceNames );
		setLayout( new BorderLayout() );
		add( display, BorderLayout.CENTER );
		add( createLookupButton( resourceNames, f, task ), BorderLayout.EAST );
	}

	public JLabel createLinkLabel( 
		final String resourceNames )
	{
		JLabel label = new JLabel(resourceNames);

		label.addMouseListener( new MouseAdapter()
			{
				@Override
				public void mouseClicked( 
					MouseEvent e )
				{
					super.mouseClicked( e );

					AssignmentDialog dialog = new AssignmentDialog(GraphicManager.getDocumentFrameInstance());
					dialog.doModal();
				}
			} );

		return label;
	}

	public JButton createLookupButton( 
		final String resourceNames,
		final Field f,
		final Task task )
	{
		JButton edit = new JButton();

		edit.setToolTipText( "Assign Resources" );

		ImageIcon icon = IconManager.getIcon( "marinerMenu.assignResources" );

		edit.setIcon( icon );
		edit.setPreferredSize( new Dimension(20, 20) );
		edit.addActionListener( new ActionListener()
			{
				public void actionPerformed( 
					ActionEvent arg0 )
				{
					AssignmentDialog dialog = new AssignmentDialog(GraphicManager.getDocumentFrameInstance());
					dialog.doModal();
				}
			} );

		return edit;
	}

	public String getResourceNames()
	{
		return resourceNames;
	}

	private JLabel display;
	private String resourceNames;
}
