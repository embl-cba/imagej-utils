package de.embl.cba.tables.select;

import de.embl.cba.bdv.utils.lut.GlasbeyARGBLut;
import de.embl.cba.tables.color.LazyCategoryColoringModel;
import de.embl.cba.tables.color.SelectionColoringModel;

public class SelectionModels
{
	public static < T > SelectionColoringModel< T > getDefaultSelectionColoringModel( )
	{
		DefaultSelectionModel< T > selectionModel = new DefaultSelectionModel<>();
		LazyCategoryColoringModel< T > coloringModel = new LazyCategoryColoringModel<>( new GlasbeyARGBLut( 255 ) );
		SelectionColoringModel< T > selectionColoringModel = new SelectionColoringModel<>( coloringModel, selectionModel );
		return selectionColoringModel;
	}
}
