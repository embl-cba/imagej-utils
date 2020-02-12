import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.RandomAccessibleIntervalSource;
import de.embl.cba.bdv.utils.sources.ARGBConvertedRealSource;
import de.embl.cba.bdv.utils.converters.SelectableVolatileARGBConverter;
import de.embl.cba.bdv.utils.sources.Metadata;
import ij.IJ;
import ij.ImagePlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

public class ExampleARGBConverted2d16bitTiffImage
{
	public static < T extends RealType< T > > void main ( String[] args )
	{
		final RandomAccessibleIntervalSource raiSource = getRandomAccessibleIntervalSource();

		final SelectableVolatileARGBConverter converter = new SelectableVolatileARGBConverter();

		final ARGBConvertedRealSource labelsSource = new ARGBConvertedRealSource( raiSource, converter );

		BdvFunctions.show( labelsSource, BdvOptions.options().is2D() );
	}

	public static < T extends RealType< T > > RandomAccessibleIntervalSource getRandomAccessibleIntervalSource()
	{
		final ImagePlus imagePlus = IJ.openImage( ExampleARGBConverted2d16bitTiffImage.class.getResource( "2d-16bit-labelMask.tif" ).getFile() );

		RandomAccessibleInterval< T > wrap = ImageJFunctions.wrapReal( imagePlus );

		// needs to be at least 3D
		wrap = Views.addDimension( wrap, 0, 0);

		return new RandomAccessibleIntervalSource( wrap, Util.getTypeFromInterval( wrap ), imagePlus.getTitle() );
	}
}
