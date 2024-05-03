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
import de.embl.cba.tables.Utils;
import de.embl.cba.tables.color.AbstractColoringModel;
import de.embl.cba.tables.color.ColumnColoringModel;
import de.embl.cba.tables.color.NumericColoringModel;
import de.embl.cba.tables.tablerow.TableRow;
import net.imglib2.type.numeric.ARGBType;

// TODO: extract abstract class NumericFeatureColoringModel
public class NumericTableRowColumnColoringModel< T extends TableRow >
		extends AbstractColoringModel< T > implements NumericColoringModel< T >, ColumnColoringModel, ARBGLutSupplier
{
	private final String columnName;
	private final ARGBLut lut;
	private double[] lutMinMax;
	private double[] lutRange;
	// TODO: also capture this with  inputToFixedColor logic
	private final boolean isZeroTransparent;

	public NumericTableRowColumnColoringModel(
			String columnName,
			ARGBLut lut,
			double[] lutMinMax,
			double[] lutRange,
			boolean isZeroTransparent )
	{
		this.columnName = columnName;
		this.lut = lut;
		this.lutMinMax = lutMinMax;
		this.lutRange = lutRange;
		this.isZeroTransparent = isZeroTransparent;
	}

	@Override
	public void convert( T tableRow, ARGBType output )
	{
		final String cell = tableRow.getCell( columnName );
		final Double value = Utils.parseDouble( cell );
		setColorLinearly( value, output );
	}

	@Override
	public double getMin()
	{
		return lutMinMax[ 0 ];
	}


	@Override
	public double getMax()
	{
		return lutMinMax[ 1 ];
	}


	@Override
	public void setMin( double min )
	{
		this.lutMinMax[ 0 ] = min;
		notifyColoringListeners();
	}

	@Override
	public void setMax( double max )
	{
		this.lutMinMax[ 1 ] = max;
		notifyColoringListeners();
	}

	private void setColorLinearly( Double value, ARGBType output )
	{
		if ( isZeroTransparent )
		{
			if ( value == 0 )
			{
				output.set( ARGBType.rgba( 0, 0, 0, 0 ) );
				return;
			}
		}

		if ( value.isNaN() )
		{
			output.set( ARGBType.rgba( 0, 0, 0, 0 ) );
			return;
		}

		double normalisedValue = computeLinearNormalisedValue( value );
		final int colorIndex = lut.getARGB( normalisedValue );
		output.set( colorIndex );
	}

	private double computeLinearNormalisedValue( double value )
	{
		double normalisedValue = 0;
		if ( lutMinMax[ 1 ] == lutMinMax[ 0 ] )
		{
			if ( lutMinMax[ 1 ] == lutRange[ 0 ] )
				normalisedValue = 1.0;
			else if ( lutMinMax[ 1 ] == lutRange[ 1 ] )
				normalisedValue = 0.0;
		}
		else
		{
			normalisedValue =
					Math.max(
							Math.min(
									( value - lutMinMax[ 0 ] )
											/ ( lutMinMax[ 1 ] - lutMinMax[ 0 ] ), 1.0 ), 0.0 );
		}
		return normalisedValue;
	}

	@Override
	public String getColumnName()
	{
		return columnName;
	}

	@Override
	public ARGBLut getARGBLut() {
		return this.lut;
	}
}
