package io;

import bdv.util.BdvFunctions;
import bdv.util.BdvStackSource;
import de.embl.cba.bdv.utils.io.BdvRaiVolumeExport;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.view.Views;

import java.util.List;
import java.util.Random;

public class ExampleBdvRaiUnsignedByteTypeVolumeExport
{
	public static void main( String[] args ) throws SpimDataException
	{
		final RandomAccessibleInterval< UnsignedByteType > rai = getRandomImage();

		final BdvRaiVolumeExport export = new BdvRaiVolumeExport();

		final String filePathWithoutExtension = "/Users/tischer/Desktop/hello";

		export.export( rai,
				filePathWithoutExtension,
				new double[]{1,1,5},
				"pixel",
				new double[]{0,0,0}
				);

		showImage( filePathWithoutExtension );

	}

	public static void showImage( String filePathWithoutExtension )
			throws SpimDataException
	{
		final SpimData spimData = new XmlIoSpimData().load(
				filePathWithoutExtension + ".xml" );

		final List< BdvStackSource< ? > > show =
				BdvFunctions.show( spimData );
		show.get( 0 ).setDisplayRange( 0, 255 );
	}

	public static
	RandomAccessibleInterval< UnsignedByteType > getRandomImage()
	{
		final RandomAccessibleInterval< UnsignedByteType > rai
				= ArrayImgs.unsignedBytes( 100, 100, 100 );

		final Cursor< UnsignedByteType > cursor =
				Views.iterable( rai ).cursor();
		final Random random = new Random();
		while (cursor.hasNext() )
			cursor.next().set( random.nextInt( 255 ) );
		return rai;
	}

}
