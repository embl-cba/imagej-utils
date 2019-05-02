package examples;

import net.imglib2.img.array.ArrayCursor;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.type.numeric.integer.UnsignedIntType;

public class Utils
{
	public static ArrayImg< UnsignedIntType, IntArray > create2DGradientImage()
	{
		ArrayImg< UnsignedIntType, IntArray > img
				= ArrayImgs.unsignedInts( 256, 256, 1 );
		paintPixelValues( img );
		return img;
	}

	public static void paintPixelValues( ArrayImg< UnsignedIntType, IntArray > img )
	{
		ArrayCursor< UnsignedIntType > cursor = img.cursor();
		while ( cursor.hasNext() )
			cursor.next().set( cursor.getIntPosition( 0 ) );
	}
}
