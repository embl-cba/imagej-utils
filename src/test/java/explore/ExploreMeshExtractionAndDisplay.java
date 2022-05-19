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
package explore;

import customnode.CustomMesh;
import customnode.CustomTriangleMesh;
import de.embl.cba.bdv.utils.objects3d.MeshExtractor;

import ij3d.Content;
import ij3d.Image3DUniverse;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;
import net.imagej.ImageJ;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import org.scijava.vecmath.Color3f;
import org.scijava.vecmath.Point3f;

import java.util.ArrayList;

public class ExploreMeshExtractionAndDisplay
{
	public static < T extends RealType< T > > void main( String[] args ) throws SpimDataException
	{

		final String labelsSourcePath = ExploreMeshExtractionAndDisplay.class.getResource( "../labels-ulong.xml" ).getFile();

		SpimData spimData = new XmlIoSpimData().load( labelsSourcePath );
		final RandomAccessibleInterval< T > image = (RandomAccessibleInterval<T> ) spimData.getSequenceDescription()
				.getImgLoader().getSetupImgLoader( 0 ).getImage( 0 );

		final ImageJ imageJ = new ImageJ();
		imageJ.ui().showUI();

		ImageJFunctions.show( image, "" );
		final MeshExtractor meshExtractor = new MeshExtractor(
				Views.extendZero( image ),
				image,
				new AffineTransform3D(),
				new int[]{ 5, 5, 5 },
				() -> false );

		final float[] mesh = meshExtractor.generateMesh( 1L );

		int n = mesh.length;


		// add mesh
		final ArrayList< Point3f > points = new ArrayList<>();
		for ( int i = 0; i < n; )
			points.add( new Point3f( mesh[ i++ ], mesh[ i++ ], mesh[ i++ ] ) );
		final CustomMesh customMesh = new CustomTriangleMesh( points );

		Image3DUniverse univ = new Image3DUniverse( );
		univ.show( );
		final Content content = univ.addCustomMesh( customMesh, "mesh01" );
		content.setColor( new Color3f(0.5f, 0, 0.5f ) );

		// add mesh again, but now with an offset
		final ArrayList< Point3f > points2 = new ArrayList<>();
		for ( int i = 0; i < n; )
			points2.add( new Point3f( 100 + mesh[ i++ ], mesh[ i++ ], mesh[ i++ ] ) );
		final CustomMesh customMesh2 = new CustomTriangleMesh( points2 );
		univ.addCustomMesh( customMesh2, "mesh02" );
	}
}
