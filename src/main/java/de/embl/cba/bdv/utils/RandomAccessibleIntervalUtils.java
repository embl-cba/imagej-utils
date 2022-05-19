/*-
 * #%L
 * Various Java code for ImageJ
 * %%
 * Copyright (C) 2018 - 2022 EMBL
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
package de.embl.cba.bdv.utils;

import net.imglib2.*;
import net.imglib2.algorithm.util.Grids;
import net.imglib2.img.AbstractImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import java.util.List;

public class RandomAccessibleIntervalUtils
{
	/**
	 *
	 * Create a copyVolumeRAI, thereby forcing computation of a potential
	 * cascade of views.
	 *
	 * @param volume
	 * @param numThreads
	 * @param <R>
	 * @return
	 */
	public static < R extends RealType< R > & NativeType< R > >
	RandomAccessibleInterval< R > copyVolumeRAI( RandomAccessibleInterval< R > volume,
												 int numThreads )
	{
		final int dimensionX = ( int ) volume.dimension( 0 );
		final int dimensionY = ( int ) volume.dimension( 1 );
		final int dimensionZ = ( int ) volume.dimension( 2 );

		final long numElements =
				AbstractImg.numElements( Intervals.dimensionsAsLongArray( volume ) );

		final IntervalView< R > zeroMin = Views.zeroMin( volume );

		R type = getType( zeroMin );

		final RandomAccessibleInterval< R > copy =
				createEmptyRAI( zeroMin, dimensionX, dimensionY, numElements, type );

		// LoopBuilder.setImages( copy, volume ).forEachPixel( Type::set );

		final int[] blockSize = {
				dimensionX,
				dimensionY,
				( int ) ( Math.ceil( dimensionZ / numThreads ) + 1 )};

		final List< Interval > intervals = Grids.collectAllContainedIntervals(
				Intervals.dimensionsAsLongArray( volume ), blockSize );

		intervals.parallelStream().forEach(
				interval -> copyAssumingSameIterationOrder( zeroMin, Views.interval( copy, interval ) ) );

		final IntervalView< R > translate = Views.translate( copy, Intervals.minAsLongArray( volume ) );

		return translate;
	}

	public static < R extends RealType< R > & NativeType< R > > RandomAccessibleInterval< R > createEmptyRAI( RandomAccessibleInterval< R > volume, int dimensionX, int dimensionY, long numElements, R type )
	{
		RandomAccessibleInterval< R > copy;

		if ( numElements < Integer.MAX_VALUE - 1 )
		{
			copy = new ArrayImgFactory( type ).create( volume );
		}
		else
		{
			int nz = (int) ( numElements / ( volume.dimension( 0  ) * volume.dimension( 1 ) ) );

			final int[] cellSize = {
					dimensionX,
					dimensionY,
					nz };

			copy = new CellImgFactory( type, cellSize ).create( volume );
		}

		return copy;
	}

	public static < T extends Type< T > > void copy(
			final RandomAccessible< T > source,
			final IterableInterval< T > target )
	{
		// create a cursor that automatically localizes itself on every move
		Cursor< T > targetCursor = target.localizingCursor();
		RandomAccess< T > sourceRandomAccess = source.randomAccess();

		// iterate over the target cursor
		while ( targetCursor.hasNext() )
		{
			// move target cursor forward
			targetCursor.fwd();

			// set the source access to the position of the target cursor
			sourceRandomAccess.setPosition( targetCursor );

			// set the value of the target pixel
			targetCursor.get().set( sourceRandomAccess.get() );
		}
	}

	public static < T extends Type< T > > void copyAssumingSameIterationOrder(
			final RandomAccessible< T > source,
			final IterableInterval< T > target )
	{
		// create a cursor that automatically localizes itself on every move
		Cursor< T > targetCursor = target.localizingCursor();
		final IntervalView< T > interval = Views.interval( source, target );
		final Cursor< T > sourceCursor = interval.cursor();

		// iterate over the target cursor
		while ( targetCursor.hasNext() )
		{
			// move target cursor forward
			targetCursor.fwd();

			// set the value of the target pixel
			targetCursor.get().set( sourceCursor.next() );
		}
	}


	private static < R extends RealType< R > & NativeType< R > >
	R getType( RandomAccessibleInterval< R > rai )
	{
		R type = null;
		try
		{
			type = Util.getTypeFromInterval( rai );
		}
		catch ( Exception e )
		{
			System.err.println( e );
		}
		return type;
	}

}
