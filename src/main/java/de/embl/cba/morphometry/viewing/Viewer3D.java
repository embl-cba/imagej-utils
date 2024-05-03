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
package de.embl.cba.morphometry.viewing;

import ij.ImagePlus;
import ij.measure.Calibration;
import ij3d.Content;
import ij3d.Image3DUniverse;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.logic.BitType;
import net.imglib2.view.Views;
import org.scijava.vecmath.Color3f;

public abstract class Viewer3D
{

	public static void show3D( RandomAccessibleInterval< BitType > processedMetaPhasePlate )
	{

		(new Thread(new Runnable()
		{
			public void run()
			{
				final ImagePlus imagePlus = asImagePlus( processedMetaPhasePlate, new double[]{1.0,1.0,1.0} );
				imagePlus.show();

				Image3DUniverse univ = new Image3DUniverse();
				univ.show();
				final Content content = univ.addMesh(
						imagePlus,
						null, "object", 250, new boolean[]{ true, true, true }, 1 );
				content.setColor( new Color3f( 1.0f, 1.0f, 1.0f ) );
			}
		})).start();

	}


	public static ImagePlus asImagePlus( RandomAccessibleInterval< BitType > mask, double[] voxelSize )
	{
		RandomAccessibleInterval rai = Views.addDimension( mask, 0, 0 );
		rai = Views.permute( rai, 2,3 );
		final ImagePlus imp = ImageJFunctions.wrap( rai, "" );

		final Calibration calibration = new Calibration();
		calibration.pixelWidth = voxelSize[ 0 ];
		calibration.pixelHeight = voxelSize[ 1 ];
		calibration.pixelDepth = voxelSize[ 2 ];
		imp.setCalibration( calibration );

		return imp;
	}
}
