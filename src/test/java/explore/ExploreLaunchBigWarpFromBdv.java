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
import de.embl.cba.bdv.utils.viewer.MultipleImageViewer;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;
import net.imagej.ImageJ;
import net.imglib2.FinalRealInterval;

import java.io.File;

public class ExploreLaunchBigWarpFromBdv
{
	public void run() throws SpimDataException
	{
		String[] paths = new String[]{
				ExploreLaunchBigWarpFromBdv.class.getResource( "../mri-stack.xml" ).getFile(),
				ExploreLaunchBigWarpFromBdv.class.getResource( "../mri-stack-shifted.xml" ).getFile(),

		};

		final MultipleImageViewer viewer = new MultipleImageViewer( paths );
		viewer.showImages();
	}

	public static void main( String[] args ) throws SpimDataException
	{
		new ImageJ().ui().showUI();
		new ExploreLaunchBigWarpFromBdv().run();
	}
}
