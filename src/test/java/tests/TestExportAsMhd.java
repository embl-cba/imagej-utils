package tests;

import bdv.ij.util.ProgressWriterIJ;
import bdv.tools.boundingbox.TransformedRealBoxSelectionDialog;
import bdv.util.*;
import bdv.viewer.Interpolation;
import bdv.viewer.Source;
import de.embl.cba.bdv.utils.BdvDialogs;
import de.embl.cba.bdv.utils.export.BdvRealExporter;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;
import net.imagej.ImageJ;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayCursor;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.type.numeric.integer.UnsignedIntType;
import net.imglib2.util.Util;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Random;

public class TestExportAsMhd
{
	@Test
	public void run() throws SpimDataException
	{
		final SpimData spimData = new XmlIoSpimData().load( TestExportAsMhd.class.getResource( "../test-data/mri.xml" ).getFile() );

		final BdvStackSource< ? > bdvStackSource =
				BdvFunctions.show( spimData ).get( 0 );

		final BdvHandle bdvHandle = bdvStackSource.getBdvHandle();

		final Source< ? > source = bdvStackSource.getSources().get( 0 ).getSpimSource();

		final TransformedRealBoxSelectionDialog.Result result =
				BdvDialogs.showBoundingBoxDialog(
						bdvHandle,
						source );

		final ArrayList< Integer > sourceIndices = new ArrayList<>();
		sourceIndices.add( 0 );

		final BdvRealExporter exporter = new BdvRealExporter(
				bdvHandle,
				sourceIndices,
				result.getInterval(),
				result.getMinTimepoint(),
				result.getMaxTimepoint(),
				Interpolation.NLINEAR,
				new double[]{ 1, 1, 1 },
				new ProgressWriterIJ()
		);

		exporter.export();

		System.out.println( result );
	}

	public static void main( String[] args ) throws SpimDataException
	{
		new ImageJ().ui().showUI();
		new TestExportAsMhd().run();
	}
}
