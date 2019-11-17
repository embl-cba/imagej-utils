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
import de.embl.cba.bdv.utils.behaviour.BdvBehaviours;
import de.embl.cba.bdv.utils.export.BdvRealSourceToVoxelImageExporter;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;
import net.imagej.ImageJ;
import net.imglib2.FinalRealInterval;
import org.junit.Test;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

public class ExploreExportSourcesBehaviour
{
	public void run() throws SpimDataException
	{
		/**
		 * Show sources with BDV
		 */
		final BdvHandle bdvHandle = showSourcesInBDV();

		/**
		 * Install export behaviour
		 */
		Behaviours behaviours = new Behaviours( new InputTriggerConfig() );
		behaviours.install( bdvHandle.getTriggerbindings(), "" );
		BdvBehaviours.addExportSourcesToVoxelImagesBehaviour( bdvHandle, behaviours,"ctrl E" );
	}

	public BdvHandle showSourcesInBDV() throws SpimDataException
	{
		final SpimData spimData = new XmlIoSpimData().load( ExploreExportSourcesBehaviour.class.getResource( "../mri-stack.xml" ).getFile() );

		final BdvStackSource< ? > bdvStackSource =
				BdvFunctions.show( spimData ).get( 0 );

		final BdvHandle bdvHandle = bdvStackSource.getBdvHandle();
		bdvStackSource.setDisplayRange( 0, 255 );

		final SpimData spimData2 = new XmlIoSpimData().load( ExploreExportSourcesBehaviour.class.getResource( "../mri-stack-shifted.xml" ).getFile() );

		BdvFunctions.show( spimData2, BdvOptions.options().addTo( bdvHandle ) ).get( 0 ).setDisplayRange( 0, 255 );
		return bdvHandle;
	}

	public static void main( String[] args ) throws SpimDataException
	{
		new ImageJ().ui().showUI();
		new ExploreExportSourcesBehaviour().run();
	}
}
