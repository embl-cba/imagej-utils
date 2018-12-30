import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.RandomAccessibleIntervalSource;
import de.embl.cba.bdv.utils.argbconversion.SelectableRealVolatileARGBConverter;
import de.embl.cba.bdv.utils.argbconversion.VolatileARGBConvertedRealSource;
import de.embl.cba.bdv.utils.lut.LinearARGBLut;
import de.embl.cba.bdv.utils.lut.Luts;
import net.imglib2.img.array.ArrayCursor;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.type.numeric.integer.UnsignedIntType;
import net.imglib2.util.Util;

public class TestBlueWhiteRedLut
{
	public static void main( String[] args )
	{
		final ArrayImg< UnsignedIntType, IntArray > img = ArrayImgs.unsignedInts( 256, 256, 1 );

		final ArrayCursor< UnsignedIntType > cursor = img.cursor();

		while ( cursor.hasNext() )
		{
			cursor.next().set( cursor.getIntPosition( 0 ) );
		}

		final RandomAccessibleIntervalSource source = new RandomAccessibleIntervalSource( img, Util.getTypeFromInterval( img ), "" );

		final SelectableRealVolatileARGBConverter converter = new SelectableRealVolatileARGBConverter( new LinearARGBLut( Luts.BLUE_WHITE_RED_LUT, 0, 255 ) );

		final VolatileARGBConvertedRealSource convertedRealSource = new VolatileARGBConvertedRealSource( source, converter );

		BdvFunctions.show( convertedRealSource, BdvOptions.options().is2D() );
	}
}
