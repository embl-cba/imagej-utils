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
package examples;

import bdv.util.BdvFunctions;
import bdv.util.BdvHandle;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import de.embl.cba.bdv.utils.BdvDialogs;
import de.embl.cba.bdv.utils.BdvUtils;
import de.embl.cba.bdv.utils.measure.PixelValueStatistics;
import net.imglib2.RealPoint;
import net.imglib2.img.array.ArrayCursor;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.type.numeric.integer.UnsignedIntType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

import java.util.HashMap;
import java.util.Map;

public class ExamplePixelStatisticsOfVisibleSources
{
	public static void main( String[] args )
	{
		/**
		 * show first image
		 */
		final ArrayImg< UnsignedIntType, IntArray > img0 = createImage();
		final BdvStackSource stackSource0 = BdvFunctions.show(
				img0, "image 0",
				BdvOptions.options().is2D() );
		stackSource0.setDisplayRange( 0, 300 );

		// get the bdvHandle
		final BdvHandle bdv = stackSource0.getBdvHandle();

		/**
		 * add second image
		 */
		final ArrayImg< UnsignedIntType, IntArray > img1 = createImage();

		// shift location
		final IntervalView< UnsignedIntType > translated
				= Views.translate( img1, new long[]{ 100, 100, 0 } );

		final BdvStackSource stackSource1 = BdvFunctions.show(
				translated, "image 1",
				BdvOptions.options().is2D().addTo( bdv ) );
		stackSource1.setDisplayRange( 0, 300 );

		final HashMap< Integer, PixelValueStatistics > statistics = BdvUtils.getPixelValueStatisticsOfActiveSources( bdv, new RealPoint( 100, 100, 0 ), 3, 0 );

		for ( Map.Entry< Integer, PixelValueStatistics > entry : statistics.entrySet() )
		{
			System.out.println( entry.getKey() );
			System.out.println( entry.getValue() );

		}
	}

	public static ArrayImg< UnsignedIntType, IntArray > createImage()
	{
		ArrayImg< UnsignedIntType, IntArray > img
				= ArrayImgs.unsignedInts( 256, 256, 1 );
		paintPixelValues( img );
		return img;
	}

	public static void paintPixelValues( ArrayImg< UnsignedIntType, IntArray > img )
	{
		ArrayCursor< UnsignedIntType > cursor = img.cursor();
		while ( cursor.hasNext() )
			cursor.next().set( cursor.getIntPosition( 0 ) );
	}

}
