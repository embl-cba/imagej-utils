package io;

import de.embl.cba.bdv.utils.io.BdvRaiVolumeExport;
import de.embl.cba.imaris.ImarisWriter;
import ij.IJ;
import ij.ImagePlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.ops.parse.token.Real;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

public class BdvExportBenchmarking
{
	public static < T extends RealType< T > > void main( String[] args )
	{
		long start = System.currentTimeMillis();
		final ImagePlus imagePlus = IJ.openImage( "/Users/tischer/Desktop/bdv-benchmark/25_lm.tif" );
		System.out.println( "Read Tiff using IJ [ms]: "
				+ ( System.currentTimeMillis() - start ) );

		start = System.currentTimeMillis();
		final ImarisWriter imarisWriter = new ImarisWriter(
				imagePlus, "/Users/tischer/Desktop/bdv-benchmark" );
		imarisWriter.write();
		System.out.println( "Save 8-bit ImagePlus as Imaris [ms]: "
				+ ( System.currentTimeMillis() - start ) );


		start = System.currentTimeMillis();
		final RandomAccessibleInterval< UnsignedShortType > copy =
				ArrayImgs.unsignedShorts( new long[]{ 2048, 2048, 100 } );
		final RandomAccessibleInterval< T > wrap = ImageJFunctions.wrapReal( imagePlus );
		LoopBuilder.setImages( wrap, copy ).forEachPixel(
				( w, c ) -> c.setShort( (short) w.getRealDouble() )  );
		System.out.println( "Copy 8-bit ImagePlus into 16-bit Arrayimg [ms]: "
				+ ( System.currentTimeMillis() - start ) );


		start = System.currentTimeMillis();
		BdvRaiVolumeExport export = new BdvRaiVolumeExport();
		export.export(
				wrap,
				"/Users/tischer/Desktop/bdv-benchmark/bdv-wrapped-8bit",
				new double[]{1,1,1},
				"pixel",
				new double[]{0,0,0}
				);
		System.out.println( "Save RAI (wrapped 8-bit ImagePlus) as Bdv [ms]: "
				+ ( System.currentTimeMillis() - start ) );

		final RandomAccessibleInterval< UnsignedShortType > shorts =
				ArrayImgs.unsignedShorts( new long[]{ 2048, 2048, 100 } );
		start = System.currentTimeMillis();
		export = new BdvRaiVolumeExport();
		export.export(
				shorts,
				"/Users/tischer/Desktop/bdv-benchmark/bdv-native-16bit",
				new double[]{1,1,1},
				"pixel",
				new double[]{0,0,0}
		);
		System.out.println( "Save RAI (native 16-bit) as Bdv [ms]: "
				+ ( System.currentTimeMillis() - start ) );

		final RandomAccessibleInterval< UnsignedByteType > bytes =
				ArrayImgs.unsignedBytes( new long[]{ 2048, 2048, 100 } );
		start = System.currentTimeMillis();
		export = new BdvRaiVolumeExport();
		export.export(
				shorts,
				"/Users/tischer/Desktop/bdv-benchmark/bdv-native-8bit",
				new double[]{1,1,1},
				"pixel",
				new double[]{0,0,0}
		);
		System.out.println( "Save RAI (native 8-bit) as Bdv [ms]: "
				+ ( System.currentTimeMillis() - start ) );

	}
}
