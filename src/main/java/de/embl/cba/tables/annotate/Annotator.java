/*-
 * #%L
 * TODO
 * %%
 * Copyright (C) 2018 - 2020 EMBL
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
package de.embl.cba.tables.annotate;

import de.embl.cba.tables.SwingUtils;
import de.embl.cba.tables.color.CategoryTableRowColumnColoringModel;
import de.embl.cba.tables.color.ColorUtils;
import de.embl.cba.tables.color.SelectionColoringModel;
import de.embl.cba.tables.select.SelectionModel;
import de.embl.cba.tables.tablerow.TableRow;
import de.embl.cba.tables.tablerow.TableRowImageSegment;
import ij.IJ;
import net.imglib2.type.numeric.ARGBType;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class Annotator < T extends TableRow > extends JFrame
{
	public static final String LAST = "You are already at the last object in table.";
	public static final String NO_MORE_SEGMENTS = "No more segments.";
	private final String annotationColumnName;
	private final List< T > tableRows;
	private final SelectionModel< T > selectionModel;
	private final CategoryTableRowColumnColoringModel< T > coloringModel;
	private final SelectionColoringModel< T > selectionColoringModel;
	private final RowSorter< ? extends TableModel > rowSorter;
	private final JPanel panel;
	private boolean skipNone;
	private boolean isSingleRowBrowsingMode = false; // TODO: think about how to get out of this mode!
	private JTextField goToRowIndexTextField;
	private HashMap< String, T > annotationToTableRow;
	private JPanel annotationButtonsPanel;
	private T currentlySelectedRow;

	public Annotator(
			String annotationColumnName,
			List< T > tableRows,
			SelectionModel< T > selectionModel,
			CategoryTableRowColumnColoringModel< T > coloringModel,
			SelectionColoringModel< T > selectionColoringModel,
			RowSorter< ? extends TableModel > rowSorter )
	{
		super("");
		this.annotationColumnName = annotationColumnName;
		this.tableRows = tableRows;
		this.selectionModel = selectionModel;
		this.coloringModel = coloringModel;
		this.selectionColoringModel = selectionColoringModel;
		this.rowSorter = rowSorter;
		this.currentlySelectedRow = tableRows.get( rowSorter.convertRowIndexToModel( 0 ) );
		coloringModel.fixedColorMode( true );
		this.panel = new JPanel();
	}

	public void showDialog()
	{
		createDialog();
		showFrame();
	}

	private void createDialog()
	{
		addCreateCategoryButton();
		panel.add( new JSeparator( SwingConstants.HORIZONTAL ) );
		addAnnotationButtons();
		panel.add( new JSeparator( SwingConstants.HORIZONTAL ) );
		addTableRowBrowserSelectPanel();
		addTableRowBrowserSelectPreviousAndNextPanel();
		addSkipNonePanel();
	}

	private void showFrame()
	{
		this.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		panel.setOpaque( true ); //content panes must be opaque
		panel.setLayout( new BoxLayout( panel, BoxLayout.Y_AXIS ) );
		this.setContentPane( panel );
		this.setLocation( MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y );
		this.pack();
		this.setVisible( true );
	}

	private void addCreateCategoryButton()
	{
		final JPanel panel = SwingUtils.horizontalLayoutPanel();
		final JButton button = new JButton( "Create new category" );
		final JTextField textField = new JTextField( "Class A" );
		panel.add( button );
		panel.add( textField );
		button.addActionListener( e -> {
			addAnnotationButtonPanel( textField.getText(), null );
			refreshDialog();
		} );
		this.panel.add( panel );
	}

	private void addAnnotationButtons()
	{
		annotationButtonsPanel = new JPanel(  );
		annotationButtonsPanel.setLayout( new BoxLayout(annotationButtonsPanel, BoxLayout.Y_AXIS ) );
		annotationButtonsPanel.setBorder( BorderFactory.createEmptyBorder(0,10,10,10) );
		this.panel.add( annotationButtonsPanel );

		final JPanel panel = SwingUtils.horizontalLayoutPanel();
		panel.add( new JLabel( "Annotate selected segment(s) as:" ) );
		panel.add( new JLabel( "      " ) );
		annotationButtonsPanel.add( panel );

		final HashMap< String, T > annotations = getAnnotations();
		for ( String annotation : annotations.keySet() )
			addAnnotationButtonPanel( annotation, annotations.get( annotation ) );
	}

	private void addAnnotationButtonPanel( String annotationName, T tableRow )
	{
		final JPanel panel = SwingUtils.horizontalLayoutPanel();

		final JButton button = new JButton( String.format("%1$15s", annotationName) );
		button.setFont( new Font("monospaced", Font.PLAIN, 12) );
		button.setOpaque( true );
		setButtonColor( button, tableRow );
		button.setAlignmentX( Component.CENTER_ALIGNMENT );

		final ARGBType argbType = new ARGBType();
		coloringModel.convert( annotationName, argbType );
		button.setBackground( ColorUtils.getColor( argbType ) );

		button.addActionListener( e -> {
			if ( selectionModel.isEmpty() ) return;

			final Set< T > selected = selectionModel.getSelected();

			for ( T row : selected )
			{
				row.setCell( annotationColumnName, annotationName );
			}

			if ( selected.size() > 1 )
				isSingleRowBrowsingMode = false;

			if( ! isSingleRowBrowsingMode )
			{
				selectionModel.clearSelection();
			}
			else
			{
				// Hack to notify all listeners that the coloring might have changed.
				selectionModel.clearSelection();
				selectionModel.setSelected( selected, true );
			}
		} );

		final JButton changeColor = new JButton( "C" );
		changeColor.addActionListener( e -> {
			Color color = JColorChooser.showDialog( this.panel, "", null );
			if ( color == null ) return;
			button.setBackground( color );
			coloringModel.putInputToFixedColor( annotationName, ColorUtils.getARGBType( color ) );
		} );

		panel.add( button );
		panel.add( changeColor );
		annotationButtonsPanel.add( panel );
		annotationButtonsPanel.revalidate();
	}

	private void addTableRowBrowserSelectPreviousAndNextPanel( )
	{
		final JPanel panel = SwingUtils.horizontalLayoutPanel();
		final JButton previous = createSelectPreviousButton();
		final JButton next = createSelectNextButton();
		panel.add( previous );
		panel.add( next );
		this.panel.add( panel );
	}

	private void addTableRowBrowserSelectPanel( )
	{
		final JPanel panel = SwingUtils.horizontalLayoutPanel();
		final JButton button = createSelectButton();
		goToRowIndexTextField = new JTextField( "" );
		goToRowIndexTextField.setText( "1" );
		panel.add( button );
		panel.add( goToRowIndexTextField );
		this.panel.add( panel );
	}

	private JButton createSelectNextButton()
	{
		final JButton next = new JButton( "Select next" );
		//next.setFont( new Font("monospaced", Font.PLAIN, 12) );
		next.setAlignmentX( Component.CENTER_ALIGNMENT );

		next.addActionListener( e ->
		{
			isSingleRowBrowsingMode = true;

			// rowIndex in sorted "units"
			int rowIndex = rowSorter.convertRowIndexToView( currentlySelectedRow.rowIndex() );
			if ( rowIndex < tableRows.size() - 1 )
			{
				T row = null;
				if ( skipNone )
				{
					while ( rowIndex < tableRows.size() - 1 )
					{
						row = tableRows.get( rowSorter.convertRowIndexToModel( ++rowIndex ) );
						if ( isNoneOrNan( row ) )
						{
							row = null;
							continue;
						}
						else
						{
							break;
						}
					}

					if ( row == null )
					{
						IJ.showMessage( NO_MORE_SEGMENTS );
						return; // All following rows are None or NaN
					}

					selectRow( row, rowIndex );
				}
				else
				{
					row = tableRows.get( rowSorter.convertRowIndexToModel( ++rowIndex ) );
					selectRow( row, rowIndex );
				}
			}
			else
			{
				IJ.showMessage( NO_MORE_SEGMENTS );
			}
		} );
		return next;
	}

	private JButton createSelectPreviousButton()
	{
		final JButton previous = new JButton( "Select previous" );
		//previous.setFont( new Font("monospaced", Font.PLAIN, 12) );
		previous.setAlignmentX( Component.CENTER_ALIGNMENT );

		previous.addActionListener( e ->
		{
			isSingleRowBrowsingMode = true;

			// row index in sorted "units"
			int rowIndex = rowSorter.convertRowIndexToView( currentlySelectedRow.rowIndex() );
			if ( rowIndex > 0 )
			{
				T row = null;
				if ( skipNone )
				{
					while ( rowIndex > 0 )
					{
						row = tableRows.get( rowSorter.convertRowIndexToModel( --rowIndex ) );
						if ( isNoneOrNan( row ) )
						{
							row = null;
							continue;
						}
						else
							break;
					}

					if ( row == null )
					{
						IJ.showMessage( NO_MORE_SEGMENTS );
						return; // None of the previous rows is not None
					}

					selectRow( row, rowIndex );
				}
				else
				{
					row = tableRows.get( rowSorter.convertRowIndexToModel( --rowIndex ) );
					selectRow( row, rowIndex );
				}
			}
			else
			{
				IJ.showMessage( NO_MORE_SEGMENTS );
			}
		} );

		return previous;
	}

	private JButton createSelectButton()
	{
		final JButton button = new JButton( "Select segment with label id" );
		button.setAlignmentX( Component.CENTER_ALIGNMENT );

		button.addActionListener( e ->
		{
			isSingleRowBrowsingMode = true;

			T selectedRow = getSelectedRow();

			if ( selectedRow != null )
				selectRow( selectedRow, rowSorter.convertRowIndexToView( selectedRow.rowIndex() ) );
		} );
		return button;
	}

	private T getSelectedRow()
	{
		// TODO: in principle a flaw in logic as it assumes that all tableRows are of same type...
		if ( tableRows.get( 0 ) instanceof TableRowImageSegment )
		{
			final double selectedLabelId = Double.parseDouble( goToRowIndexTextField.getText() );
			for ( T tableRow : tableRows )
			{
				final double labelId = ( ( TableRowImageSegment ) tableRow ).labelId();
				if ( labelId == selectedLabelId )
				{
					return tableRow;
				}
			}
			throw new UnsupportedOperationException( "Could not find segment with label " + selectedLabelId );
		}
		else
		{
			final int rowIndex = Integer.parseInt( goToRowIndexTextField.getText() );
			return tableRows.get( rowSorter.convertRowIndexToModel( rowIndex ) );
		}
	}

	private boolean isNoneOrNan( T row )
	{
		return row.getCell( annotationColumnName ).toLowerCase().equals( "none" )
			|| row.getCell( annotationColumnName ).toLowerCase().equals( "nan" );
	}

	private void selectRow( T row, int sortedRowIndex )
	{
		//currentlySelectedRowIndex = sortedRowIndex;
		currentlySelectedRow = row;

		if ( ! row.getCell( annotationColumnName ).toLowerCase().equals( "none" ) )
		{
			selectionColoringModel.setSelectionColoringMode( SelectionColoringModel.SelectionColoringMode.OnlyShowSelected );
		}
		else
		{
			selectionColoringModel.setSelectionColoringMode( SelectionColoringModel.SelectionColoringMode.SelectionColor );
		}

		selectionModel.clearSelection();
		selectionModel.setSelected( row, true );
		selectionModel.focus( row );
	}

	private void addSkipNonePanel( )
	{
		final JPanel panel = SwingUtils.horizontalLayoutPanel();

		final JCheckBox checkBox = new JCheckBox( "Skip \"None\" & \"NaN\"" );
		checkBox.setSelected( true );
		skipNone = checkBox.isSelected();

		checkBox.addActionListener( e -> {
			skipNone = checkBox.isSelected();
		}  );

		panel.add( checkBox );
		this.panel.add( panel );
	}

	private void setButtonColor( JButton button, T tableRow )
	{
		if ( tableRow != null )
		{
			final ARGBType argbType = new ARGBType();
			coloringModel.convert( tableRow, argbType );
			button.setBackground( new Color( argbType.get() ) );
		}
	}

	private HashMap< String, T > getAnnotations()
	{
		if ( annotationToTableRow == null )
		{
			annotationToTableRow = new HashMap<>();

			for ( int row = 0; row < tableRows.size(); row++ )
			{
				final T tableRow = tableRows.get( row );
				annotationToTableRow.put( tableRow.getCell( annotationColumnName ), tableRow );
			}
		}

		return annotationToTableRow;
	}

	private void refreshDialog()
	{
		panel.revalidate();
		panel.repaint();
		this.pack();
	}
}
