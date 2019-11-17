package explore;

import bdv.ij.util.ProgressWriterIJ;
import bdv.tools.boundingbox.TransformedRealBoxSelectionDialog;
import bdv.util.BdvFunctions;
import bdv.util.BdvHandle;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import bdv.viewer.Interpolation;
import de.embl.cba.bdv.utils.BdvDialogs;
import de.embl.cba.bdv.utils.BdvUtils;
import de.embl.cba.bdv.utils.export.BdvRealSourceToVoxelImageExporter;
import ij.IJ;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;
import net.imagej.ImageJ;
import net.imglib2.FinalRealInterval;
import org.junit.Test;

import javax.swing.*;
import java.util.List;

public class ExploreExportSourcesFromBdvAsVoxelImages
{
	public void run() throws SpimDataException
	{
		final SpimData spimData = new XmlIoSpimData().load( ExploreExportSourcesFromBdvAsVoxelImages.class.getResource( "../mri-stack.xml" ).getFile() );

		final BdvStackSource< ? > bdvStackSource =
				BdvFunctions.show( spimData ).get( 0 );

		final BdvHandle bdvHandle = bdvStackSource.getBdvHandle();

		final SpimData spimData2 = new XmlIoSpimData().load( ExploreExportSourcesFromBdvAsVoxelImages.class.getResource( "../mri-stack-shifted.xml" ).getFile() );

		BdvFunctions.show( spimData2, BdvOptions.options().addTo( bdvHandle ) );

		final FinalRealInterval maximalRangeInterval = BdvUtils.getRealIntervalOfCurrentSource( bdvHandle );

		final TransformedRealBoxSelectionDialog.Result result =
				BdvDialogs.showBoundingBoxDialog(
						bdvHandle,
						maximalRangeInterval );

		final BdvRealSourceToVoxelImageExporter exporter =
				new BdvRealSourceToVoxelImageExporter(
						bdvHandle,
						BdvUtils.getVisibleSourceIndices( bdvHandle ),
						result.getInterval(),
						result.getMinTimepoint(),
						result.getMaxTimepoint(),
						Interpolation.NLINEAR,
						new double[]{ 0.5, 0.5, 0.5 },
						BdvRealSourceToVoxelImageExporter.ExportModality.ShowAsImagePlus,
						BdvRealSourceToVoxelImageExporter.ExportDataType.UnsignedShort,
						Runtime.getRuntime().availableProcessors(),
						new ProgressWriterIJ()
				);

		exporter.setOutputDirectory( "/Users/tischer/Documents/bdv-utils/src/test/resources/test-output-data" );

		exporter.export();

	}

	public static void main( String[] args ) throws SpimDataException
	{
		new ImageJ().ui().showUI();
		new ExploreExportSourcesFromBdvAsVoxelImages().run();
	}
}
