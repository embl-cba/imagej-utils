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
