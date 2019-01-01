import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.RandomAccessibleIntervalSource;
import de.embl.cba.bdv.utils.argbconversion.SelectableRealVolatileARGBConverter;
import de.embl.cba.bdv.utils.argbconversion.VolatileARGBConvertedRealSource;
import de.embl.cba.bdv.utils.lut.LinearMappingARGBLut;
import ij.IJ;
import ij.ImagePlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import java.util.TreeMap;

public class ExampleMappingLut
{
	public static void main( String[] args )
	{
		final TreeMap treeMap = new TreeMap();

		treeMap.put( 1.0, 10.0 );
		treeMap.put( 2.0, 20.0 );
		treeMap.put( 3.0, 30.0 );
		treeMap.put( 4.0, 40.0 );

		final RandomAccessibleIntervalSource raiSource = getRandomAccessibleIntervalSource();

		final LinearMappingARGBLut linearMappingARGBLut = new LinearMappingARGBLut( treeMap, 0, 50 );

		final SelectableRealVolatileARGBConverter argbConverter = new SelectableRealVolatileARGBConverter( linearMappingARGBLut );

		final VolatileARGBConvertedRealSource argbSource = new VolatileARGBConvertedRealSource( raiSource, argbConverter );

		Bdv bdv = BdvFunctions.show( argbSource, BdvOptions.options().is2D() ).getBdvHandle();

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
