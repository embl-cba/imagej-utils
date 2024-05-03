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

import net.imglib2.Volatile;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.volatiles.VolatileARGBType;

import java.util.HashMap;
import java.util.List;

// TODO: move to table-utils
public class ListItemsARGBConverter< T > implements LabelsARGBConverter
{
	public static final int OUT_OF_BOUNDS_ROW_INDEX = -1;
	private final ColoringModel< T > coloringModel;
	private final List< T > list;
	private ARGBType singleColor;
	private int frame;
	private int noColorArgbIndex; // default, background color
	private final HashMap< Integer, Integer > indexToColor;

	public ListItemsARGBConverter(
			List< T > list,
			ColoringModel< T > coloringModel )
	{
		this.list = list;
		this.coloringModel = coloringModel;
		noColorArgbIndex = 0;
		indexToColor = new HashMap<>();
	}

	@Override
	public void convert( RealType rowIndex, VolatileARGBType color )
	{
		if ( rowIndex instanceof Volatile )
		{
			if ( ! ( ( Volatile ) rowIndex ).isValid() )
			{
				color.set( noColorArgbIndex );
				color.setValid( false );
				return;
			}
		}

		final int index = ( int ) rowIndex.getRealDouble();

		if ( indexToColor.containsKey( index ))
		{
			color.set( indexToColor.get( index ) );
			return;
		}

		if ( index == OUT_OF_BOUNDS_ROW_INDEX )
		{
			color.set( noColorArgbIndex );
			color.setValid( true );
			return;
		}

		if ( singleColor != null )
		{
			color.setValid( true );
			color.set( singleColor.get() );
			return;
		}

		final T item = list.get( index );

		if ( item == null )
		{
			color.set( noColorArgbIndex );
			color.setValid( true );
		}
		else
		{
			coloringModel.convert( item, color.get() );

			final int alpha = ARGBType.alpha( color.get().get() );
			if( alpha < 255 )
				color.mul( alpha / 255.0 );

			color.setValid( true );
		}
	}

	@Override
	public void timePointChanged( int timePointIndex )
	{
		this.frame = timePointIndex;
	}

	@Override
	public void setSingleColor( ARGBType argbType )
	{
		singleColor = argbType;
	}

	public HashMap< Integer, Integer > getIndexToColor()
	{
		return indexToColor;
	}
}
