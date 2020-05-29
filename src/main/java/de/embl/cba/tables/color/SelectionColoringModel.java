package de.embl.cba.tables.color;

import de.embl.cba.tables.color.AbstractColoringModel;
import de.embl.cba.tables.color.ColoringModel;
import de.embl.cba.tables.select.SelectionModel;
import net.imglib2.type.numeric.ARGBType;

import java.util.Arrays;
import java.util.List;

public class SelectionColoringModel < T > extends AbstractColoringModel< T >
{
	ColoringModel< T > coloringModel;
	SelectionModel< T > selectionModel;

	private SelectionMode selectionMode;
	private ARGBType selectionColor;
	private double brightnessNotSelected;

	public static final ARGBType YELLOW =
			new ARGBType( ARGBType.rgba( 255, 255, 0, 255 ) );
	private final List< SelectionMode > selectionModes;

	public enum SelectionMode
	{
		OnlyShowSelected,
		SelectionColor,
		SelectionColorAndDimNotSelected,
		DimNotSelected
	}

	public SelectionColoringModel(
			ColoringModel< T > coloringModel,
			SelectionModel< T > selectionModel )
	{
		setColoringModel( coloringModel );
		this.selectionModel = selectionModel;
		this.selectionModes = Arrays.asList( de.embl.cba.tables.color.SelectionColoringModel.SelectionMode.values() );

		this.selectionColor = YELLOW;
		this.brightnessNotSelected = 0.1;
		this.selectionMode = SelectionMode.DimNotSelected;
	}

	@Override
	public void convert( T input, ARGBType output )
	{
		coloringModel.convert( input, output );

		if ( selectionModel.isEmpty() ) return;

		final boolean isSelected = selectionModel.isSelected( input );

		switch ( selectionMode )
		{
			case DimNotSelected:

				if ( ! isSelected )
					dim( output, brightnessNotSelected );
				break;

			case OnlyShowSelected:

				if ( ! isSelected )
					dim( output, 0.0 );
				break;

			case SelectionColor:

				if ( isSelected )
					output.set( selectionColor );
				break;

			case SelectionColorAndDimNotSelected:

				if ( isSelected )
					output.set( selectionColor );
				else
					dim( output, brightnessNotSelected );
				break;

			default:
				break;
		}

	}

	public void dim( ARGBType output, double brightnessNotSelected )
	{
		final int colorIndex = output.get();
		output.set( ARGBType.rgba(
				ARGBType.red( colorIndex ),
				ARGBType.green( colorIndex ),
				ARGBType.blue( colorIndex ),
				brightnessNotSelected * 255 )  );
	}

	public void setSelectionMode( SelectionMode selectionMode )
	{
		this.selectionMode = selectionMode;

		switch ( selectionMode )
		{
			case DimNotSelected:
				brightnessNotSelected = 0.2;
				selectionColor = null;
				break;
			case OnlyShowSelected:
				brightnessNotSelected = 0.0;
				selectionColor = null;
				break;
			case SelectionColor:
				brightnessNotSelected = 1.0;
				selectionColor = YELLOW;
				break;
			case SelectionColorAndDimNotSelected:
				brightnessNotSelected = 0.2;
				selectionColor = YELLOW;
				break;
		}
		notifyColoringListeners();
	}

	public SelectionMode getSelectionMode()
	{
		return selectionMode;
	}

	public void setSelectionColor( ARGBType selectionColor )
	{
		this.selectionColor = selectionColor;
		notifyColoringListeners();
	}

	public void setColoringModel( ColoringModel< T > coloringModel )
	{
		this.coloringModel = coloringModel;
		notifyColoringListeners();

		// chain event notification
		coloringModel.listeners().add( () -> de.embl.cba.tables.color.SelectionColoringModel.this.notifyColoringListeners() );
	}

	public ColoringModel< T > getColoringModel()
	{
		return coloringModel;
	}

	public void iterateSelectionMode()
	{
		final int selectionModeIndex = selectionModes.indexOf( selectionMode );

		if ( selectionModeIndex < selectionModes.size() - 1 )
		{
			setSelectionMode( selectionModes.get( selectionModeIndex + 1 ) );
		}
		else
		{
			setSelectionMode( selectionModes.get( 0 ) );
		}
	}
}
