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
package examples;

import bdv.util.*;
import de.embl.cba.bdv.utils.BdvUtils;
import ij.IJ;
import ij.ImagePlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

import java.util.ArrayList;

import static de.embl.cba.bdv.utils.BdvUtils.getSourceIndicesAtSelectedPoint;

public class ExampleSourcesAtMouseCoordinates
{
	public static < T extends RealType< T > > void main ( String[] args )
	{
		final ImagePlus imagePlus =
				IJ.openImage( ExampleSourcesAtMouseCoordinates.class.getResource( "2d-timelapse-16bit-labelMask.tif" ).getFile() );

		RandomAccessibleInterval< T > wrap = ImageJFunctions.wrapReal( imagePlus );

		// needs to be 3D
		wrap = Views.addDimension( wrap, 0, 0);
		// make time last dimension
		wrap = Views.permute( wrap, 3,2 );

		final RandomAccessibleIntervalSource4D raiSource
				= new RandomAccessibleIntervalSource4D(
						wrap,
						Util.getTypeFromInterval( wrap ),
						imagePlus.getTitle() );


		/**
		 * Show the gray-scale image
		 */

		final BdvStackSource show = BdvFunctions.show(
				raiSource,
				2,
				BdvOptions.options().is2D() );

		show.setDisplayRange( 0, 3 );

		Behaviours behaviours = new Behaviours( new InputTriggerConfig() );
		final BdvHandle bdvHandle = show.getBdvHandle();
		behaviours.install( bdvHandle.getTriggerbindings(), "" );

		behaviours.behaviour( ( ClickBehaviour ) ( x, y ) ->
		{
			final RealPoint point = BdvUtils.getGlobalMouseCoordinates( bdvHandle );
			final ArrayList< Integer > points =
					getSourceIndicesAtSelectedPoint( bdvHandle, point, true );
			int a = 1;
		}, "", "ctrl button1" ) ;



	}
}
