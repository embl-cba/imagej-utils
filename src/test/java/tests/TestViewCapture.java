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
package tests;

import bdv.util.BdvFunctions;
import bdv.util.BdvStackSource;
import bdv.util.RandomAccessibleIntervalSource;
import de.embl.cba.bdv.utils.capture.BdvViewCaptures;
import mpicbg.spim.data.SpimDataException;
import net.imagej.ImageJ;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayCursor;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedIntType;
import net.imglib2.util.Util;

import java.io.File;

public class TestViewCapture < R extends RealType< R > >
{
	//@Test
	public void captureView()
	{
		int w = 1000;
		final RandomAccessibleInterval< UnsignedIntType > rai = createImage( w, w, 50 );
		final RandomAccessibleIntervalSource< ? > source = new RandomAccessibleIntervalSource( rai, Util.getTypeFromInterval( rai ), "image" );

		final BdvStackSource< ? > show = BdvFunctions.show( source );
		show.setDisplayRange( 0, w );

//		BdvViewCaptures.captureView(
//				show.getBdvHandle(),
//				1,
//				"nanometer",
//				false ).show();
//
		BdvViewCaptures.saveScreenShot( new File("/Users/tischer/Desktop/tmp.jpg"), show.getBdvHandle().getViewerPanel() );
	}


	public static RandomAccessibleInterval< UnsignedIntType > createImage( long... dimensions )
	{
		ArrayImg< UnsignedIntType, IntArray > img
				= ArrayImgs.unsignedInts( dimensions );
		paintPixelValues( img );
		return img;
	}

	public static void paintPixelValues( ArrayImg< UnsignedIntType, IntArray > img )
	{
		ArrayCursor< UnsignedIntType > cursor = img.cursor();
		while ( cursor.hasNext() )
			cursor.next().set( cursor.getIntPosition( 0 ) );
	}

	public static void main( String[] args ) throws SpimDataException
	{
		new ImageJ().ui().showUI();
		new TestViewCapture().captureView();
	}
}
