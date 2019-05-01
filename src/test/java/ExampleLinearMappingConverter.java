import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.RandomAccessibleIntervalSource;
import de.embl.cba.bdv.utils.BdvDialogs;
import de.embl.cba.bdv.utils.converters.MappingLinearARGBConverter;
import de.embl.cba.bdv.utils.converters.SelectableVolatileARGBConverter;
import de.embl.cba.bdv.utils.sources.ARGBConvertedRealSource;
import ij.IJ;
import ij.ImagePlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

public class ExampleLinearMappingConverter
{
	public static void main( String[] args )
	{
		final MappingLinearARGBConverter linearConverter =
				new MappingLinearARGBConverter( 0, 4, d -> d );

		final SelectableVolatileARGBConverter selectableVolatileARGBConverter =
				new SelectableVolatileARGBConverter( linearConverter );

		final RandomAccessibleIntervalSource source = getRandomAccessibleIntervalSource();

		final ARGBConvertedRealSource argbSource =
				new ARGBConvertedRealSource( source, selectableVolatileARGBConverter );

		Bdv bdv = BdvFunctions.show( argbSource, BdvOptions.options().is2D() ).getBdvHandle();

		BdvDialogs.showBrightnessDialog( bdv,"test", linearConverter );
	}

	public static RandomAccessibleIntervalSource getRandomAccessibleIntervalSource()
	{
		final ImagePlus imagePlus = IJ.openImage( ExampleARGBConverted2d16bitTiffImage.class.getResource( "2d-16bit-labelMask.tif" ).getFile() );

		RandomAccessibleInterval< RealType > wrap = ImageJFunctions.wrapReal( imagePlus );

		// needs to be at least 3D
		wrap = Views.addDimension( wrap, 0, 0);

		return new RandomAccessibleIntervalSource( wrap, Util.getTypeFromInterval( wrap ), imagePlus.getTitle() );
	}
}
