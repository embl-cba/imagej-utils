package explore;

import bdv.ij.util.ProgressWriterIJ;
import bdv.tools.boundingbox.TransformedRealBoxSelectionDialog;
import bdv.util.BdvFunctions;
import bdv.util.BdvHandle;
import bdv.util.BdvStackSource;
import bdv.viewer.Interpolation;
import de.embl.cba.bdv.utils.BdvDialogs;
import de.embl.cba.bdv.utils.BdvUtils;
import de.embl.cba.bdv.utils.export.BdvRealSourceToVoxelImageExporter;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;
import net.imagej.ImageJ;
import net.imglib2.FinalRealInterval;
import org.junit.Test;

import javax.swing.*;

public class ExploreExportSourcesFromBdvAsVoxelImages
{
	@Test
	public void run() throws SpimDataException
	{
		final SpimData spimData = new XmlIoSpimData().load( ExploreExportSourcesFromBdvAsVoxelImages.class.getResource( "../test-data/mri.xml" ).getFile() );

		final BdvStackSource< ? > bdvStackSource =
				BdvFunctions.show( spimData ).get( 0 );

		final BdvHandle bdvHandle = bdvStackSource.getBdvHandle();

		SwingUtilities.invokeLater( () ->
		{
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
							new double[]{ 1, 1, 1 },
							BdvRealSourceToVoxelImageExporter.ExportModality.SaveAsTiffStacks,
							BdvRealSourceToVoxelImageExporter.ExportDataType.UnsignedShort,
							Runtime.getRuntime().availableProcessors(),
							new ProgressWriterIJ()
					);

			exporter.setOutputDirectory( "/Users/tischer/Documents/bdv-utils/src/test/resources/test-output-data" );

			exporter.export();
		} );

	}

	public static void main( String[] args ) throws SpimDataException
	{
		new ImageJ().ui().showUI();
		new ExploreExportSourcesFromBdvAsVoxelImages().run();
	}
}
