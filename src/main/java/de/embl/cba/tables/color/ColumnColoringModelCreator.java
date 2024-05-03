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
package de.embl.cba.tables.color;

import de.embl.cba.bdv.utils.lut.ARGBLut;
import de.embl.cba.bdv.utils.lut.BlueWhiteRedARGBLut;
import de.embl.cba.bdv.utils.lut.ColumnARGBLut;
import de.embl.cba.bdv.utils.lut.GlasbeyARGBLut;
import de.embl.cba.bdv.utils.lut.ViridisARGBLut;
import de.embl.cba.tables.TableRows;
import de.embl.cba.tables.Tables;
import de.embl.cba.tables.tablerow.TableRow;
import ij.gui.GenericDialog;
import net.imglib2.type.numeric.ARGBType;

import javax.swing.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.embl.cba.tables.color.CategoryTableRowColumnColoringModel.TRANSPARENT;

public class ColumnColoringModelCreator< T extends TableRow >
{
	private JTable table;
	private List< T > tableRows;

	private String selectedColumnName;
	private String selectedColoringMode;
	private boolean isZeroTransparent = false;

	private Map< String, double[] > columnNameToMinMax;
	private HashMap< String, double[] > columnNameToRangeSettings;

	public static final String[] COLORING_MODES = new String[]
	{
			ColoringLuts.BLUE_WHITE_RED,
			ColoringLuts.VIRIDIS,
			ColoringLuts.GLASBEY,
			ColoringLuts.ARGB_COLUMN
	};

	public ColumnColoringModelCreator( JTable table )
	{
		this.table = table;

		init();
	}

	public ColumnColoringModelCreator( List< T > tableRows )
	{
		this.tableRows = tableRows;

		init();
	}

	private void init()
	{
		this.columnNameToMinMax = new HashMap<>();
		this.columnNameToRangeSettings = new HashMap<>();
	}

	public ColoringModel< T > showDialog()
	{
		final String[] columnNames = Tables.getColumnNamesAsArray( table );

		final GenericDialog gd = new GenericDialog( "Color by Column" );

		if ( selectedColumnName == null ) selectedColumnName = columnNames[ 0 ];
		gd.addChoice( "Column", columnNames, selectedColumnName );

		if ( selectedColoringMode == null ) selectedColoringMode = COLORING_MODES[ 0 ];
		gd.addChoice( "Coloring Mode", COLORING_MODES, selectedColoringMode );

		gd.addCheckbox( "Paint Zero Transparent", isZeroTransparent );

		gd.showDialog();
		if ( gd.wasCanceled() ) return null;

		selectedColumnName = gd.getNextChoice();
		selectedColoringMode = gd.getNextChoice();
		isZeroTransparent = gd.getNextBoolean();

		if ( isZeroTransparent )
			selectedColoringMode += ColoringLuts.ZERO_TRANSPARENT;

		return createColoringModel( selectedColumnName, selectedColoringMode, null, null );
	}

	public ColoringModel< T > createColoringModel(
			String selectedColumnName,
			String coloringLut,
			Double min,
			Double max)
	{
		rememberChoices( selectedColumnName, coloringLut );

		switch ( coloringLut )
		{
			case ColoringLuts.BLUE_WHITE_RED:
				return createLinearColoringModel(
						selectedColumnName,
						false,
						min, max,
						new BlueWhiteRedARGBLut( 1000 ) );
			case ColoringLuts.BLUE_WHITE_RED + ColoringLuts.ZERO_TRANSPARENT:
				return createLinearColoringModel(
						selectedColumnName,
						true,
						min, max,
						new BlueWhiteRedARGBLut( 1000 ) );
			case ColoringLuts.VIRIDIS:
				return createLinearColoringModel(
						selectedColumnName,
						false,
						min, max,
						new ViridisARGBLut() );
			case ColoringLuts.VIRIDIS + ColoringLuts.ZERO_TRANSPARENT:
				return createLinearColoringModel(
						selectedColumnName,
						true,
						min, max,
						new ViridisARGBLut() );
			case ColoringLuts.GLASBEY:
				return createCategoricalColoringModel(
						selectedColumnName,
						false,
						new GlasbeyARGBLut(), TRANSPARENT );
			case ColoringLuts.GLASBEY + ColoringLuts.ZERO_TRANSPARENT:
				return createCategoricalColoringModel(
						selectedColumnName,
						true,
						new GlasbeyARGBLut(), TRANSPARENT );
			case ColoringLuts.ARGB_COLUMN:
				return createCategoricalColoringModel(
						selectedColumnName,
						false,
						new ColumnARGBLut(), TRANSPARENT );
		}

		return null;
	}

	public void rememberChoices( String selectedColumnName, String selectedColoringMode )
	{
		this.selectedColumnName = selectedColumnName;
		this.selectedColoringMode = selectedColoringMode;

		if ( selectedColoringMode.contains( ColoringLuts.ZERO_TRANSPARENT ) )
			this.isZeroTransparent = true;
		else
			this.isZeroTransparent = false;
	}

	private void populateColoringModelFromArgbColumn (String selectedColumnName, CategoryTableRowColumnColoringModel<T> coloringModel) {

		int selectedColumnIndex = -1;
		int rowCount;
		if ( table != null )
		{
			selectedColumnIndex = table.getColumnModel().getColumnIndex( selectedColumnName );
			rowCount = table.getRowCount();
		}
		else
		{
			rowCount = tableRows.size();
		}

		for ( int i = 0; i < rowCount; i++)
		{
			String argbString;
			if ( table != null )
				argbString = (String) table.getValueAt(i, selectedColumnIndex);
			else
				argbString = tableRows.get( i ).getCell( selectedColumnName );

			if ( ! argbString.equals("NaN") & !argbString.equals("None") ) {
				String[] splitArgbString = argbString.split("-");

				int[] argbValues = new int[4];
				for (int j = 0; j < splitArgbString.length; j++) {
					argbValues[j] = Integer.parseInt(splitArgbString[j]);
				}

				coloringModel.putInputToFixedColor(argbString, new ARGBType(ARGBType.rgba(argbValues[1], argbValues[2], argbValues[3], argbValues[0])));
			}
		}
	}

	public CategoryTableRowColumnColoringModel< T > createCategoricalColoringModel(
			String selectedColumnName,
			boolean isZeroTransparent,
			ARGBLut argbLut,
			ARGBType colorForNoneOrNaN )
	{
		final CategoryTableRowColumnColoringModel< T > coloringModel
				= new CategoryTableRowColumnColoringModel< >(
						selectedColumnName,
						argbLut );

		coloringModel.putInputToFixedColor( "Infinity", colorForNoneOrNaN );
		coloringModel.putInputToFixedColor( "NaN", colorForNoneOrNaN );
		coloringModel.putInputToFixedColor( "None", colorForNoneOrNaN );

		if ( isZeroTransparent )
		{
			coloringModel.putInputToFixedColor( "0", TRANSPARENT );
			coloringModel.putInputToFixedColor( "0.0", TRANSPARENT );

			if (argbLut != null) {
				argbLut.setName(argbLut.getName() + ColoringLuts.ZERO_TRANSPARENT);
			}
		}

		if ( argbLut instanceof ColumnARGBLut )
		{
			populateColoringModelFromArgbColumn( selectedColumnName, coloringModel );
		}

		return coloringModel;
	}

	private NumericTableRowColumnColoringModel< T > createLinearColoringModel(
			String selectedColumnName,
			boolean isZeroTransparent,
			Double min,
			Double max,
			ARGBLut argbLut )
	{
		double[] valueRange;

		if ( table != null )
			valueRange = getValueRange( table, selectedColumnName );
		else
			valueRange = getValueRange( tableRows, selectedColumnName );

		double[] valueSettings = getValueSettings( selectedColumnName, valueRange );

		final NumericTableRowColumnColoringModel< T > coloringModel
				= new NumericTableRowColumnColoringModel(
						selectedColumnName,
						argbLut,
						valueSettings,
						valueRange,
						isZeroTransparent );

		if ( isZeroTransparent ) {
			argbLut.setName( argbLut.getName() + ColoringLuts.ZERO_TRANSPARENT );
		}

		if ( min != null )
			coloringModel.setMin( min );

		if ( max != null )
			coloringModel.setMax( max );

		SwingUtilities.invokeLater( () ->
				new NumericColoringModelDialog( selectedColumnName, coloringModel, valueRange ) );

		return coloringModel;
	}

	private double[] getValueSettings( String columnName, double[] valueRange )
	{
		double[] valueSettings;

		if ( columnNameToRangeSettings.containsKey( columnName ) )
			valueSettings = columnNameToRangeSettings.get( columnName );
		else
			valueSettings = valueRange.clone();

		columnNameToRangeSettings.put( columnName, valueSettings );

		return valueSettings;
	}

	private double[] getValueRange( JTable table, String column )
	{
		if ( ! columnNameToMinMax.containsKey( column ) )
		{
			final double[] minMaxValues = Tables.minMax( column, table );
			columnNameToMinMax.put( column, minMaxValues );
		}

		return columnNameToMinMax.get( column );
	}

	private double[] getValueRange( List< ? extends TableRow > tableRows, String column )
	{
		if ( ! columnNameToMinMax.containsKey( column ) )
		{
			final double[] minMaxValues = TableRows.minMax( tableRows, column );
			columnNameToMinMax.put( column, minMaxValues );
		}

		return columnNameToMinMax.get( column );
	}
}
