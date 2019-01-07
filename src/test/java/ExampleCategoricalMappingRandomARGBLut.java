import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.RandomAccessibleIntervalSource;
import de.embl.cba.bdv.utils.converters.CategoricalMappingARGBConverter;
import de.embl.cba.bdv.utils.converters.SelectableVolatileARGBConverter;
import de.embl.cba.bdv.utils.sources.VolatileARGBConvertedRealSource;
import ij.IJ;
import ij.ImagePlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import java.util.HashMap;

public class ExampleCategoricalMappingRandomARGBLut
{
	public static < T extends RealType< T > > void main ( String[] args )
	{
		/**
		 * This example shows how an image can be colored based on categorical (String)
		 * information, which must be available for each value in the image.
		 *
		 * To do so one needs to provide a Map< Double, Object > whichs maps
		 * the values in the image (e.g. the object label)
		 * to categorical object properties, e.g. strings describing
		 * non-numerical object properties.
		 * This map is used to create a CategoricalMappingRandomARGBConverter,
		 * which converts the categorical information into random colors.
		 */


		final ImagePlus imagePlus = IJ.openImage( ExampleCategoricalMappingRandomARGBLut.class.getResource( "2d-16bit-labelMask.tif" ).getFile() );

		RandomAccessibleInterval< T > wrap = ImageJFunctions.wrapReal( imagePlus );

		// needs to be 3D
		wrap = Views.addDimension( wrap, 0, 0 );

		final RandomAccessibleIntervalSource raiSource = new RandomAccessibleIntervalSource( wrap, Util.getTypeFromInterval( wrap ), imagePlus.getTitle() );

		final HashMap< Double, String > map = new HashMap<>();
		map.put( 1.0, "GroupA" );
		map.put( 2.0, "GroupA" );
		map.put( 3.0, "GroupB" );
		map.put( 4.0, "GroupB" );


		final SelectableVolatileARGBConverter selectableVolatileARGBConverter =
				new SelectableVolatileARGBConverter(
						new CategoricalMappingARGBConverter(
								d -> map.get( d ) ) );

		final VolatileARGBConvertedRealSource labelsSource = new VolatileARGBConvertedRealSource( raiSource, selectableVolatileARGBConverter );

		BdvFunctions.show( labelsSource, BdvOptions.options().is2D() );


	}

}
