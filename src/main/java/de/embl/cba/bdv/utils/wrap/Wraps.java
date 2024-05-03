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
package de.embl.cba.bdv.utils.wrap;

import bdv.tools.transformation.TransformedSource;
import bdv.util.RandomAccessibleIntervalSource4D;
import de.embl.cba.bdv.utils.sources.ModifiableRandomAccessibleIntervalSource4D;
import ij.IJ;
import ij.ImagePlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import java.util.ArrayList;

public class Wraps
{
	public static < R extends RealType< R > & NativeType< R > >
	ArrayList< ModifiableRandomAccessibleIntervalSource4D< R > >
	imagePlusAsSource4DChannelList( ImagePlus imagePlus )
	{
		RandomAccessibleInterval< R > wrap = wrapXYZCT( imagePlus );

		final ArrayList< ModifiableRandomAccessibleIntervalSource4D< R > > sources
				= new ArrayList<>();

		for ( int c = 0; c < imagePlus.getNChannels(); c++ )
		{
			RandomAccessibleInterval< R > channel = Views.hyperSlice( wrap, 3, c );

			final ModifiableRandomAccessibleIntervalSource4D source4D =
					new ModifiableRandomAccessibleIntervalSource4D<>(
							channel,
							Util.getTypeFromInterval( channel ),
							getScalingTransform( imagePlus ),
							imagePlus.getTitle() + "-C" + c );

			sources.add( source4D );
		}

		return sources;
	}

	public static AffineTransform3D getScalingTransform( ImagePlus imagePlus )
	{
		final AffineTransform3D scaling = new AffineTransform3D();
		scaling.set( imagePlus.getCalibration().pixelWidth, 0, 0 );
		scaling.set( imagePlus.getCalibration().pixelHeight, 1, 1 );
		scaling.set( imagePlus.getCalibration().pixelDepth, 2, 2 );
		return scaling;
	}

//	public static < R extends RealType< R > & NativeType< R > >
//	RandomAccessibleInterval< R > wrapXYCZT( ImagePlus imagePlus )
//	{
//		RandomAccessibleInterval< R > wrap = ImageJFunctions.wrapRealNative( imagePlus );
//
//		if ( imagePlus.getNFrames() == 1 )
//			wrap = Views.addDimension( wrap, 0, 0 );
//
//		if ( imagePlus.getNSlices() == 1 )
//			wrap = Views.addDimension( wrap, 0, 0 );
//
//		wrap = Views.permute(
//				wrap,
//				wrap.numDimensions() - 1,
//				wrap.numDimensions() - 2 );
//
//		if ( imagePlus.getNChannels() == 1 )
//			wrap = Views.addDimension( wrap, 0, 0 );
//
//		wrap = Views.permute(
//				wrap,
//				wrap.numDimensions() - 1,
//				wrap.numDimensions() - 2 );
//		wrap = Views.permute(
//				wrap,
//				wrap.numDimensions() - 2,
//				wrap.numDimensions() - 3 );
//
//		return wrap;
//	}

	public static < R extends RealType< R > & NativeType< R > >
	RandomAccessibleInterval< R > wrapXYZCT( ImagePlus imagePlus )
	{
		RandomAccessibleInterval< R > wrap = ImageJFunctions.wrapRealNative( imagePlus );

		if ( imagePlus.getNFrames() == 1 )
			wrap = Views.addDimension( wrap, 0, 0 );

		if ( imagePlus.getNChannels() == 1 )
		{
			wrap = Views.addDimension( wrap, 0, 0 );

			wrap = Views.permute(
					wrap,
					wrap.numDimensions() - 1,
					wrap.numDimensions() - 2 );
		}

		if ( imagePlus.getNSlices() == 1 )
		{
			wrap = Views.addDimension( wrap, 0, 0 );

			wrap = Views.permute(
					wrap,
					wrap.numDimensions() - 1,
					wrap.numDimensions() - 2 );
			wrap = Views.permute(
					wrap,
					wrap.numDimensions() - 2,
					wrap.numDimensions() - 3 );
		}
		else if ( imagePlus.getNSlices() > 1  && imagePlus.getNChannels() > 1 )
		{
			wrap = Views.permute(
					wrap,
					wrap.numDimensions() - 2,
					wrap.numDimensions() - 3 );
		}

//		if ( imagePlus.getNSlices() > 1  && imagePlus.getNChannels() == 1 && imagePlus.getNFrames() == 1 )
//		{
//			wrap = Views.permute(
//					wrap,
//					wrap.numDimensions() - 2,
//					wrap.numDimensions() - 3 );
//		}

		return wrap;
	}
}
