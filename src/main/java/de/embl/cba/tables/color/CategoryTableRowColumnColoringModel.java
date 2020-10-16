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
package de.embl.cba.tables.color;

import de.embl.cba.bdv.utils.lut.ARGBLut;
import de.embl.cba.tables.color.AbstractColoringModel;
import de.embl.cba.tables.color.CategoryColoringModel;
import de.embl.cba.tables.color.ColumnColoringModel;
import de.embl.cba.tables.tablerow.TableRow;
import net.imglib2.type.numeric.ARGBType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static de.embl.cba.bdv.utils.converters.RandomARGBConverter.goldenRatio;

public class CategoryTableRowColumnColoringModel< T extends TableRow >
		extends AbstractColoringModel< T > implements CategoryColoringModel< T >, ColumnColoringModel
{
	public static final ARGBType TRANSPARENT = new ARGBType( ARGBType.rgba( 0, 0, 0, 0 ) );

	// TODO: The maps could go to int instead of ARGBType
	private Map< Object, ARGBType > inputToFixedColor;
	private Map< Object, ARGBType > inputToRandomColor;
	private final String columnName;
	private ARGBLut argbLut;
	private int randomSeed;
	private boolean fixedColorMode = false;

	/**
	 *
	 * @param argbLut
	 */
	public CategoryTableRowColumnColoringModel( String columnName, ARGBLut argbLut )
	{
		this.columnName = columnName;
		this.argbLut = argbLut;
		this.inputToRandomColor = new ConcurrentHashMap<>(  );
		this.inputToFixedColor = new ConcurrentHashMap<>(  );
		this.randomSeed = 50;
		// TODO: make this configurable
		this.inputToFixedColor.put( "NaN", TRANSPARENT );
		this.inputToFixedColor.put( "None", TRANSPARENT );
	}

	@Override
	public void convert( T input, ARGBType output )
	{
		convert( input.getCell( columnName ), output );
	}

	public void convert( String cellContent, ARGBType output )
	{
		if ( inputToFixedColor.keySet().contains( cellContent ) )
		{
			final int color = inputToFixedColor.get( cellContent ).get();
			output.set( color );
		}
 		else if ( inputToRandomColor.keySet().contains( cellContent ) )
		{
			final int color = inputToRandomColor.get( cellContent ).get();
			output.set( color );
		}
		else
		{
			// final double random = createRandom( inputToRandomColor.size() + 1 );
			final double random = createRandom( cellContent.hashCode() );
			final int color = argbLut.getARGB( random );
			inputToRandomColor.put( cellContent, new ARGBType( color ) );
			output.set( color );
			return;
		}
	}

	private double createRandom( double x )
	{
		double random = ( x * randomSeed ) * goldenRatio;
		random = random - ( long ) Math.floor( random );
		return random;
	}

	@Override
	public void incRandomSeed( )
	{
		if ( fixedColorMode ) return;

		inputToRandomColor.clear();
		this.randomSeed++;
		notifyColoringListeners();
	}

	public void fixedColorMode( boolean fixedColorMode )
	{
		this.fixedColorMode = fixedColorMode;
	}

	public void putInputToFixedColor( Object input, ARGBType color )
	{
		inputToFixedColor.put( input, color );
		notifyColoringListeners();
	}

	public void removeInputToFixedColor( Object input )
	{
		inputToFixedColor.remove( input );
		notifyColoringListeners();
	}

	public String getColumnName()
	{
		return columnName;
	}

	@Override
	public ARGBLut getARGBLut() {
		return this.argbLut;
	}
}
