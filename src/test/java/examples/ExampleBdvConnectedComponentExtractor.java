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
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import de.embl.cba.bdv.utils.sources.ARGBConvertedRealSource;
import de.embl.cba.bdv.utils.objects3d.ConnectedComponentExtractor;
import de.embl.cba.bdv.utils.behaviour.BehaviourTransformEventHandler3DLeftMouseDrag;
import ij.ImageJ;
import ij.ImagePlus;
import ij3d.Content;
import ij3d.Image3DUniverse;
import mpicbg.spim.data.SpimDataException;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.volatiles.VolatileARGBType;
import net.imglib2.view.Views;
import org.scijava.vecmath.Color3f;

public class ExampleBdvConnectedComponentExtractor
{

	public static void main( String[] args ) throws SpimDataException
	{
		final ARGBConvertedRealSource labelsSource = Examples.getSelectable3DSource();

		final BdvStackSource< VolatileARGBType > bdvStackSource =
				BdvFunctions.show( labelsSource,
						BdvOptions.options().transformEventHandlerFactory( new BehaviourTransformEventHandler3DLeftMouseDrag.BehaviourTransformEventHandler3DFactory() ) );

		final ConnectedComponentExtractor ConnectedComponentExtractor = new ConnectedComponentExtractor(
				bdvStackSource.getSources().get( 0 ).getSpimSource(),
				new RealPoint( 35, 35, 8 ),
				0 );

		RandomAccessibleInterval connectedComponentMask = ConnectedComponentExtractor.getConnectedComponentMask( 0 );

		connectedComponentMask = changeToImageJDimensionConvention( connectedComponentMask );

		show2D( connectedComponentMask );

		show3D( connectedComponentMask );

	}

	public static void show3D( RandomAccessibleInterval connectedComponentMask )
	{
		final ImagePlus regionMaskImp = ImageJFunctions.wrap( connectedComponentMask, "" );
		Image3DUniverse univ = new Image3DUniverse( );
		univ.show( );
		final Content content = univ.addMesh( regionMaskImp, null, "somename", 250, new boolean[]{ true, true, true }, 2 );

		content.setColor( new Color3f(0.5f, 0, 0.5f ) );
	}

	public static void show2D( RandomAccessibleInterval connectedComponentMask )
	{
		new ImageJ();
		ImageJFunctions.show( connectedComponentMask, "" );
	}

	public static RandomAccessibleInterval changeToImageJDimensionConvention( RandomAccessibleInterval connectedComponentMask )
	{
		connectedComponentMask = Views.addDimension( connectedComponentMask, 0, 0 );
		connectedComponentMask = Views.permute( connectedComponentMask, 2,3 );
		return connectedComponentMask;
	}
}
