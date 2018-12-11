package de.embl.cba.bdv.utils;

import ij.ImagePlus;
import ij.measure.Calibration;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.logic.BitType;
import net.imglib2.view.Views;

public abstract class Utils
{
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
