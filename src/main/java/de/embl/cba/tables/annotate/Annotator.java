package de.embl.cba.tables.annotate;

import de.embl.cba.tables.SwingUtils;
import de.embl.cba.tables.color.CategoryTableRowColumnColoringModel;
import de.embl.cba.tables.color.ColorUtils;
import de.embl.cba.tables.color.SelectionColoringModel;
import de.embl.cba.tables.select.SelectionModel;
import de.embl.cba.tables.tablerow.TableRow;
import ij.gui.GenericDialog;
import net.imglib2.type.numeric.ARGBType;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

public class Annotator < T extends TableRow > extends JFrame
{
	private final String annotationColumnName;
	private final List< T > tableRows;
	private final SelectionModel< T > selectionModel;
	private final CategoryTableRowColumnColoringModel< T > coloringModel;
	private final SelectionColoringModel< T > selectionColoringModel;
	private final JPanel panel;
	private boolean skipNone;

	public Annotator(
			String annotationColumnName,
			List< T > tableRows,
			SelectionModel< T > selectionModel,
			CategoryTableRowColumnColoringModel< T > coloringModel,
			SelectionColoringModel< T > selectionColoringModel )
	{
		super("");
		this.annotationColumnName = annotationColumnName;
		this.tableRows = tableRows;
		this.selectionModel = selectionModel;
		this.coloringModel = coloringModel;
		this.selectionColoringModel = selectionColoringModel;
		coloringModel.fixedColorMode( true );
		this.panel = new JPanel();
	}

	public void showDialog()
	{
		addCreateCategoryButton();
		addAnnotationButtons();
		panel.add( new JSeparator( SwingConstants.HORIZONTAL ) );
		addTableRowBrowserPanel();
		addSkipNonePanel();
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

	private void addCreateCategoryButton()
	{
		final JButton button = new JButton( "Create Category" );
		panel.add( button );
		button.addActionListener( e -> {
			final GenericDialog gd = new GenericDialog( "" );
			gd.addStringField( "Category Name", "", 10 );
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

	private void addTableRowBrowserPanel( )
	{
		final JPanel panel = SwingUtils.horizontalLayoutPanel();

		final ListIterator< T > iterator = tableRows.listIterator();

		final JButton previous = new JButton( "Select Previous" );
		previous.setFont( new Font("monospaced", Font.PLAIN, 12) );
		previous.setAlignmentX( Component.CENTER_ALIGNMENT );

		previous.addActionListener( e ->
		{
			if ( iterator.hasPrevious() )
			{
				T row = null;
				if ( skipNone )
				{
					while ( iterator.hasPrevious() )
					{
						row = iterator.previous();
						if ( row.getCell( annotationColumnName ).toLowerCase().equals( "none" ) )
							continue;
						else
							break;
					}
				}
				else
				{
					row = iterator.previous();
				}

				selectRow( row );
			}
		} );

		final JButton next = new JButton( "Select Next" );
		next.setFont( new Font("monospaced", Font.PLAIN, 12) );
		next.setAlignmentX( Component.CENTER_ALIGNMENT );

		next.addActionListener( e ->
		{
			if ( iterator.hasNext() )
			{
				T row = null;
				if ( skipNone )
				{
					while ( iterator.hasNext() )
					{
						row = iterator.next();
						if ( row.getCell( annotationColumnName ).toLowerCase().equals( "none" ) )
							continue;
						else
							break;
					}
				}
				else
				{
					row = iterator.next();
				}

				selectRow( row );
			}
		} );

		panel.add( previous );
		panel.add( next );
		this.panel.add( panel );
	}

	private void selectRow( T row )
	{
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

		final JCheckBox checkBox = new JCheckBox( "Skip None" );

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
