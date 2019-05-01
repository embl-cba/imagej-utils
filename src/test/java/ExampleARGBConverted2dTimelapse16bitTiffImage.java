import bdv.util.*;
import ij.IJ;
import ij.ImagePlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

public class ExampleARGBConverted2dTimelapse16bitTiffImage
{
	public static < T extends RealType< T > > void main ( String[] args )
	{
		final ImagePlus imagePlus = IJ.openImage( ExampleARGBConverted2dTimelapse16bitTiffImage.class.getResource( "2d-timelapse-16bit-labelMask.tif" ).getFile() );

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

		final int numTimepoints =
				show.getBdvHandle().getViewerPanel().getState().getNumTimepoints();

		int a = 1;


	}
}
