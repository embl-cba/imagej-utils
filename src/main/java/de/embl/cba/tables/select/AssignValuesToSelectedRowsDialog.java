/*-
 * #%L
 * Various Java code for ImageJ
 * %%
 * Copyright (C) 2018 - 2024 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package de.embl.cba.tables.select;

import de.embl.cba.tables.SwingUtils;
import de.embl.cba.tables.TableRows;
import de.embl.cba.tables.tablerow.TableRow;
import de.embl.cba.tables.view.TableRowsTableView;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AssignValuesToSelectedRowsDialog< T extends TableRow > extends JPanel
{
	public static final String NEW_ATTRIBUTE = "None";
	final TableRowsTableView< T > tableView;
	Set< T > selectedRows;
	private JComboBox attributeComboBox;
	private JComboBox columnComboBox;
	private JFrame frame;

	private String selectedColumn;
	private String selectedAttribute;
	private Set< String > selectedAttributes;
	private Point location;


	// TODO: make this only work on TableRows (sources rid of TableView dependency)

	public AssignValuesToSelectedRowsDialog( TableRowsTableView tableView )
	{
		this.tableView = tableView;
		this.selectedAttributes = new HashSet<>();

		this.setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );

		final JPanel attributeSelectionUI = createAttributeSelectionUI();
		final JPanel columnSelectionUI = createColumnSelectionUI();
		final JButton okButton = createOkButton();

		this.add( columnSelectionUI );
		this.add( attributeSelectionUI );
		this.add( okButton );
	}

	public void showUI( Set< T > selectedRows )
	{
		this.selectedRows = selectedRows;
		updateUIComponents();
		showFrame();
	}

	private JButton createOkButton()
	{
		final JButton okButton = new JButton( "OK" );
		okButton.addActionListener( e -> {

			selectedColumn = ( String ) columnComboBox.getSelectedItem();
			selectedAttribute = ( String ) attributeComboBox.getSelectedItem();

			TableRows.assignValues(
					selectedColumn,
					selectedRows,
					selectedAttribute,
					tableView.getTable()
			);

			location = frame.getLocation();
			frame.dispose();
		} );

		return okButton;
	}

	public void updateUIComponents()
	{
		System.out.println( "AssignValuesToTableRowsUI.Debug.updateColumnComboBox" );
		updateColumnComboBox();
		System.out.println( "AssignValuesToTableRowsUI.Debug.updateAttributeComboBox" );
		updateAttributeComboBox();
	}

	private void updateAttributeComboBox()
	{
		if ( ! selectedAttributes.contains( selectedAttribute ) )
		{
			selectedAttributes.add( selectedAttribute );
			attributeComboBox.addItem( selectedAttribute );
		}
	}

	private void showFrame()
	{
		frame = new JFrame();
		if ( location != null ) frame.setLocation( location );
		frame.add( this );
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		frame.pack();
		frame.setVisible( true );
	}

	private JPanel createAttributeSelectionUI()
	{
		final JPanel horizontalLayoutPanel = SwingUtils.horizontalLayoutPanel();

		horizontalLayoutPanel.add( new JLabel( "Attribute: " ) );

		attributeComboBox = new JComboBox();
		attributeComboBox.setEditable( true );
		attributeComboBox.addItem( NEW_ATTRIBUTE );
		horizontalLayoutPanel.add( attributeComboBox );

		return horizontalLayoutPanel;
	}

	private JPanel createColumnSelectionUI()
	{
		final JPanel horizontalLayoutPanel = SwingUtils.horizontalLayoutPanel();

		horizontalLayoutPanel.add( new JLabel( "Column: " ) );

		columnComboBox = new JComboBox();

		horizontalLayoutPanel.add( columnComboBox );

		updateColumnComboBox();

		columnComboBox.addActionListener( e -> {
			// TODO: maybe change content of attributeComboBox
		} );

		return horizontalLayoutPanel;
	}

	private void updateColumnComboBox()
	{
		final List< String > columnNames = tableView.getColumnNames();

		columnComboBox.removeAllItems();

		for ( String name : columnNames )
			columnComboBox.addItem( name );

		if ( selectedColumn != null )
			columnComboBox.setSelectedItem( selectedColumn );
	}


	private int getColumnIndex( String column )
	{
		return tableView.getTable().getColumnModel().getColumnIndex( column );
	}
}
