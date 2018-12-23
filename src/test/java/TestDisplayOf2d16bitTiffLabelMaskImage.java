import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.RandomAccessibleIntervalSource;
import bdv.util.volatiles.VolatileViews;
import de.embl.cba.bdv.utils.labels.ARGBConvertedVolatileRealTypeSource;
import de.embl.cba.bdv.utils.labels.ConfigurableVolatileRealVolatileARGBConverter;
import ij.IJ;
import ij.ImagePlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Volatile;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class TestDisplayOf2d16bitTiffLabelMaskImage
{
	public static < T extends RealType< T > > void main ( String[] args )
	{
		final ImagePlus imagePlus = IJ.openImage( TestDisplayOf2d16bitTiffLabelMaskImage.class.getResource( "2d-16bit-labelMask.tif" ).getFile() );

		RandomAccessibleInterval< T > wrap = ImageJFunctions.wrapReal( imagePlus );

		// needs to be at least 3D
		wrap = Views.addDimension( wrap, 0, 0);

		// should be volatile for Bdv to be responsive
		final IntervalView< T > interval = Views.interval( wrap, wrap );

		final RandomAccessibleInterval< Volatile< T > > volatileWrap = VolatileViews.wrapAsVolatile( interval );

		final RandomAccessibleIntervalSource raiSource = new RandomAccessibleIntervalSource( volatileWrap, Util.getTypeFromInterval( wrap ), imagePlus.getTitle() );


		/**
		 * Show the gray-scale image
		 */

//		BdvFunctions.show( raiSource, BdvOptions.options().is2D() ).setDisplayRange( 0, 3 );


		/**
		 * Show as ARGB image
		 */

		final ConfigurableVolatileRealVolatileARGBConverter converter = new ConfigurableVolatileRealVolatileARGBConverter();

		final ARGBConvertedVolatileRealTypeSource labelsSource = new ARGBConvertedVolatileRealTypeSource( raiSource, "test", converter );

		BdvFunctions.show( labelsSource, BdvOptions.options().is2D() );
	}
}
