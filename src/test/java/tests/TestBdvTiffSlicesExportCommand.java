package tests;

import bdv.util.BdvFunctions;
import de.embl.cba.bdv.utils.BdvUtils;
import de.embl.cba.bdv.utils.command.BdvTiffPlanesWriterCommand;
import mpicbg.spim.data.SpimData;
import org.junit.Test;

import java.io.File;

public class TestBdvTiffSlicesExportCommand
{
	//@Test
	public void run()
	{
		final BdvTiffPlanesWriterCommand command = new BdvTiffPlanesWriterCommand();

		command.inputFolder = new File( getClass().getResource( "../test-data/tiff-slices" ).getFile() );
		command.voxelSizeX = 1.0;
		command.voxelSizeY = 1.0;
		command.voxelSizeZ = 20.0;
		command.xmlOutputPath = new File("/Users/tischer/Documents/bdv-utils/src/test/resources/test-data/mri.xml" );

		command.run();

		BdvFunctions.show( BdvUtils.openSpimData( command.xmlOutputPath.getAbsolutePath() ) );
	}

	public static void main( String[] args )
	{
		new TestBdvTiffSlicesExportCommand().run();
	}
}
