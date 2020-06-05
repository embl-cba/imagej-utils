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
