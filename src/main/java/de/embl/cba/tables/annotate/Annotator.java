package de.embl.cba.tables.annotate;

import de.embl.cba.tables.SwingUtils;
import de.embl.cba.tables.color.CategoryTableRowColumnColoringModel;
import de.embl.cba.tables.color.ColorUtils;
import de.embl.cba.tables.select.SelectionModel;
import de.embl.cba.tables.tablerow.TableRow;
import ij.gui.GenericDialog;
import net.imglib2.type.numeric.ARGBType;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;

public class Annotator < T extends TableRow > extends JFrame
{
	private final String annotationColumnName;
	private final List< T > tableRows;
	private final SelectionModel< T > selectionModel;
	private final CategoryTableRowColumnColoringModel< T > coloringModel;
	private final JPanel panel;

	public Annotator(
			String annotationColumnName,
			List< T > tableRows,
			SelectionModel< T > selectionModel,
			CategoryTableRowColumnColoringModel< T > coloringModel )
	{
		super("");
		this.annotationColumnName = annotationColumnName;
		this.tableRows = tableRows;
		this.selectionModel = selectionModel;
		this.coloringModel = coloringModel;
		coloringModel.fixedColorMode( true );
		this.panel = new JPanel();
	}

	public void showDialog()
	{
		addAddNewAnnotationButton();
		addAnnotationButtons();
		showFrame();
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

	private void addAddNewAnnotationButton()
	{
		final JButton button = new JButton( "Add New Category" );
		panel.add( button );
		button.addActionListener( e -> {
			final GenericDialog gd = new GenericDialog( "" );
			gd.addStringField( "New Category Name", "", 10 );
			gd.showDialog();
			if ( gd.wasCanceled() ) return;
			addAnnotationButtonPanel( gd.getNextString(), null );
			refreshDialog();
		} );
	}

	private void addAnnotationButtons()
	{
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

			for ( T row : selectionModel.getSelected() )
				row.setCell( annotationColumnName, annotationName );

			selectionModel.clearSelection();
		} );

		final JButton changeColor = new JButton( "Change Color" );
		changeColor.addActionListener( e -> {
			Color color = JColorChooser.showDialog( this.panel, "", null );
			if ( color == null ) return;
			button.setBackground( color );
			coloringModel.putInputToFixedColor( annotationName, ColorUtils.getARGBType( color ) );
		} );

		panel.add( button );
		panel.add( changeColor );
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
		final HashMap< String, T > annotationToTableRow = new HashMap<>();

		for ( int row = 0; row < tableRows.size(); row++ )
		{
			final T tableRow = tableRows.get( row );
			annotationToTableRow.put( tableRow.getCell( annotationColumnName ), tableRow );
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
