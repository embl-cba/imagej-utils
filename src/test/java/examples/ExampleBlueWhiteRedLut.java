package examples;

import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.RandomAccessibleIntervalSource;
import de.embl.cba.bdv.utils.converters.LinearARGBConverter;
import de.embl.cba.bdv.utils.converters.SelectableVolatileARGBConverter;
import de.embl.cba.bdv.utils.sources.ARGBConvertedRealSource;
import de.embl.cba.bdv.utils.lut.Luts;
import net.imglib2.img.array.ArrayCursor;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.type.numeric.integer.UnsignedIntType;
import net.imglib2.util.Util;

public class ExampleBlueWhiteRedLut
{
	public static void main( String[] args )
	{
		final ArrayImg< UnsignedIntType, IntArray > img = ArrayImgs.unsignedInts( 256, 256, 1 );

		final ArrayCursor< UnsignedIntType > cursor = img.cursor();

		while ( cursor.hasNext() )
			cursor.next().set( cursor.getIntPosition( 0 ) );

		final RandomAccessibleIntervalSource source = new RandomAccessibleIntervalSource( img, Util.getTypeFromInterval( img ), "" );

		final SelectableVolatileARGBConverter converter = new SelectableVolatileARGBConverter( new LinearARGBConverter( 0, 255,  Luts.BLUE_WHITE_RED ) );

		final ARGBConvertedRealSource convertedRealSource = new ARGBConvertedRealSource( source, converter );

		BdvFunctions.show( convertedRealSource, BdvOptions.options().is2D() );
	}
}
