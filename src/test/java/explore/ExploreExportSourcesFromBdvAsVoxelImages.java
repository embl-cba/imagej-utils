package tests;

import bdv.ij.util.ProgressWriterIJ;
import bdv.tools.boundingbox.TransformedRealBoxSelectionDialog;
import bdv.util.*;
import bdv.viewer.Interpolation;
import bdv.viewer.Source;
import de.embl.cba.bdv.utils.BdvDialogs;
import de.embl.cba.bdv.utils.BdvUtils;
import de.embl.cba.bdv.utils.export.BdvRealSourceToVoxelImageExporter;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;
import net.imagej.ImageJ;
import org.junit.Test;

import javax.swing.*;

public class TestExportSourcesFromBdvAsVoxelImages
{
	@Test
	public void run() throws SpimDataException
	{
		final SpimData spimData = new XmlIoSpimData().load( TestExportSourcesFromBdvAsVoxelImages.class.getResource( "../test-data/mri.xml" ).getFile() );

		final BdvStackSource< ? > bdvStackSource =
				BdvFunctions.show( spimData ).get( 0 );

		final BdvHandle bdvHandle = bdvStackSource.getBdvHandle();

		final Source< ? > source = bdvStackSource.getSources().get( 0 ).getSpimSource();

		SwingUtilities.invokeLater( () ->
		{
			final TransformedRealBoxSelectionDialog.Result result =
					BdvDialogs.showBoundingBoxDialog(
							bdvHandle,
							source );

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
							Runtime.getRuntime().availableProcessors(),
							new ProgressWriterIJ()
					);

			exporter.export();
		} );

	}

	public static void main( String[] args ) throws SpimDataException
	{
		new ImageJ().ui().showUI();
		new TestExportSourcesFromBdvAsVoxelImages().run();
	}
}
