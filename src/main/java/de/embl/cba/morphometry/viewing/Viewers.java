package de.embl.cba.morphometry.viewing;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class Viewers
{
	public static void showRai3dWithImageJ( RandomAccessibleInterval< ? > rai3d, String title )
	{
		final IntervalView< ? > ts = Views.addDimension( rai3d, 0, 0 );
		final IntervalView< ? > permute = Views.permute( ts, 2, 3 );
		ImageJFunctions.show( ( RandomAccessibleInterval ) permute, title );
	}
}
