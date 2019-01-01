import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.RandomAccessibleIntervalSource;
import de.embl.cba.bdv.utils.argbconversion.SelectableRealVolatileARGBConverter;
import de.embl.cba.bdv.utils.argbconversion.VolatileARGBConvertedRealSource;
import de.embl.cba.bdv.utils.lut.Luts;
import de.embl.cba.bdv.utils.lut.StringMappingRandomARGBLut;
import ij.IJ;
import ij.ImagePlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import java.util.HashMap;

public class ExampleStringMappingRandomARGBLut
{
	public static < T extends RealType< T > > void main ( String[] args )
	{
		/**
		 * This example shows how an image can be colored based on categorical (String)
		 * information, which must be available for each value in the image.
		 *
		 * To do so one needs to provide a Map< Double, String > whichs maps
		 * the values in the image (e.g. the object label)
		 * to strings (e.g. categorical object properties).
		 *
		 * The map is used to create a StringMappingRandomARGBLut, which converts
		 * the strings into random colors.
		 */


		final ImagePlus imagePlus = IJ.openImage( ExampleStringMappingRandomARGBLut.class.getResource( "2d-16bit-labelMask.tif" ).getFile() );

		RandomAccessibleInterval< T > wrap = ImageJFunctions.wrapReal( imagePlus );

		// needs to be 3D
		wrap = Views.addDimension( wrap, 0, 0 );

		final RandomAccessibleIntervalSource raiSource = new RandomAccessibleIntervalSource( wrap, Util.getTypeFromInterval( wrap ), imagePlus.getTitle() );


		/**
		 * Configure string (categorical) mapping random Lut
		 */

		final HashMap< Double, String > map = new HashMap<>();
		map.put( 1.0, "GroupA" );
		map.put( 2.0, "GroupA" );
		map.put( 3.0, "GroupB" );
		map.put( 4.0, "GroupB" );

		final StringMappingRandomARGBLut lut = new StringMappingRandomARGBLut( map );


		/**
		 * Show as ARGB image, using above Lut
		 */

		final SelectableRealVolatileARGBConverter converter = new SelectableRealVolatileARGBConverter( lut );

		final VolatileARGBConvertedRealSource labelsSource = new VolatileARGBConvertedRealSource( raiSource, converter );

		BdvFunctions.show( labelsSource, BdvOptions.options().is2D() );
	}

}
