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
package de.embl.cba.transforms.utils;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.AbstractImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;

public abstract class ImageCreators
{
	public static < T extends RealType< T > & NativeType< T > >
	RandomAccessibleInterval< T > copyAsArrayImg( RandomAccessibleInterval< T > orig )
	{
		RandomAccessibleInterval< T > copy = new ArrayImgFactory( orig.randomAccess().get() ).create( orig );
		copy = Transforms.getWithAdjustedOrigin( orig, copy );
		LoopBuilder.setImages( copy, orig ).forEachPixel( ( c, o ) -> c.set( o ) );

		return copy;
	}

	public static < T extends RealType< T > & NativeType< T > >
	RandomAccessibleInterval< T > createEmptyArrayImg( RandomAccessibleInterval< T > rai )
	{
		RandomAccessibleInterval< T > newImage = new ArrayImgFactory( rai.randomAccess().get() ).create( rai );
		newImage = Transforms.getWithAdjustedOrigin( rai, newImage );
		return newImage;
	}

	public static < T extends RealType< T > & NativeType< T > >
	RandomAccessibleInterval< T > createEmptyCellImg( RandomAccessibleInterval< T > volume )
	{
		final int dimensionX = ( int ) volume.dimension( 0 );
		final int dimensionY = ( int ) volume.dimension( 1 );
		final int dimensionZ = ( int ) volume.dimension( 2 );

		int nz = dimensionZ;
		if ( AbstractImg.numElements( Intervals.dimensionsAsLongArray( volume ) ) >  Integer.MAX_VALUE - 1 )
			nz  = ( Integer.MAX_VALUE / 2 ) / ( dimensionX * dimensionY );

		final int[] cellSize = {
				dimensionX,
				dimensionY,
				nz };

		RandomAccessibleInterval< T > newImage = new CellImgFactory<>(
				volume.randomAccess().get(),
				cellSize ).create( volume );

		newImage = Transforms.getWithAdjustedOrigin( volume, newImage );
		return newImage;
	}
}
