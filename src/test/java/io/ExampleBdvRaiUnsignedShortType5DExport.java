package io;

import bdv.util.BdvFunctions;
import bdv.util.BdvStackSource;
import de.embl.cba.bdv.utils.io.BdvRaiVolumeExport;
import de.embl.cba.bdv.utils.io.BdvRaiXYZCTExport;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.view.Views;

import java.util.List;
import java.util.Random;

public class ExampleBdvRaiUnsignedShortType5DExport
{
	public static void main( String[] args ) throws SpimDataException
	{
		final RandomAccessibleInterval< UnsignedShortType > rai = getRandomImage();

		final BdvRaiXYZCTExport export = new BdvRaiXYZCTExport();

		final String filePathWithoutExtension = "/Users/tischer/Desktop/image5D";

		export.export( rai,
				"5D-image",
				filePathWithoutExtension,
				new double[]{1,1,5},
				"pixel",
				new double[]{0,0,0}
				);

		showImage( filePathWithoutExtension );

	}

	public static void showImage( String filePathWithoutExtension ) throws SpimDataException
	{
		final SpimData spimData = new XmlIoSpimData().load(
				filePathWithoutExtension + ".xml" );

		final List< BdvStackSource< ? > > show = BdvFunctions.show( spimData );
		show.get( 0 ).setDisplayRange( 0, 65535 );
	}

	public static RandomAccessibleInterval< UnsignedShortType > getRandomImage()
	{
		final RandomAccessibleInterval< UnsignedShortType > rai
				= ArrayImgs.unsignedShorts( 100, 100, 100, 3, 10 );

		final Cursor< UnsignedShortType > cursor = Views.iterable( rai ).cursor();
		final Random random = new Random();
		while (cursor.hasNext() ) cursor.next().set( random.nextInt( 65535 ) );
		return rai;
	}

}
