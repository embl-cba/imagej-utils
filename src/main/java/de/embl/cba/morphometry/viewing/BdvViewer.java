/*-
 * #%L
 * Various Java code for ImageJ
 * %%
 * Copyright (C) 2018 - 2021 EMBL
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
package de.embl.cba.morphometry.viewing;

import bdv.util.*;
import de.embl.cba.morphometry.Algorithms;
import de.embl.cba.morphometry.Utils;
import net.imagej.ImgPlus;
import net.imagej.axis.LinearAxis;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.util.List;

public class BdvViewer
{
	public static < T extends RealType< T > & NativeType< T > >
	void show( RandomAccessibleInterval< T > rai )
	{
		show( rai, "", null, null, false );
	}

	public static < T extends RealType< T > & NativeType< T > >
	void show( RandomAccessibleInterval< T > rai, String title )
	{
		show( rai, title, null, null, false );
	}

	public static < T extends RealType< T > & NativeType< T > >
	void show( RandomAccessibleInterval rai, String title, List< RealPoint > points, double calibration )
	{
		show( rai, title, points, Utils.as3dDoubleArray( calibration ), false );
	}

	public static < T extends RealType< T > & NativeType< T > >
	void show( RandomAccessibleInterval rai,
			   String title,
			   List< RealPoint > points,
			   double[] calibration,
			   boolean resetViewTransform )
	{
		final Bdv bdv;
		final BdvSource bdvSource;

		if ( rai.numDimensions() ==  2 )
			bdvSource = BdvFunctions.show( rai, title, BdvOptions.options().is2D().sourceTransform( calibration ) );
		else
			bdvSource = BdvFunctions.show( rai, title, BdvOptions.options().sourceTransform( calibration ) );

		bdv = bdvSource.getBdvHandle();


		if ( points != null )
		{
			BdvOverlay bdvOverlay = new BdvPointListOverlay( points, 5.0 );
			BdvFunctions.showOverlay( bdvOverlay, "overlay", BdvOptions.options().addTo( bdv ) );
		}

		if ( resetViewTransform )
		{
			resetViewTransform( bdv );
		}

		bdvSource.setDisplayRange( 0, Algorithms.getMaximumValue( rai ) );

	}

	private static void resetViewTransform( Bdv bdv )
	{
		final AffineTransform3D affineTransform3D = new AffineTransform3D();

		affineTransform3D.scale( 2.5D );

		bdv.getBdvHandle().getViewerPanel().setCurrentViewerTransform( affineTransform3D );

	}

	private static BdvStackSource show3DImgPlusInBdv( ImgPlus imgPlus )
	{

		BdvStackSource bdvStackSource = BdvFunctions.show(
				imgPlus,
				imgPlus.getName(),
				Bdv.options().sourceTransform(
						((LinearAxis)imgPlus.axis( 0 )).scale(),
						((LinearAxis)imgPlus.axis( 1 )).scale(),
						((LinearAxis)imgPlus.axis( 2 )).scale() )
		);

		return bdvStackSource;
	}
}
