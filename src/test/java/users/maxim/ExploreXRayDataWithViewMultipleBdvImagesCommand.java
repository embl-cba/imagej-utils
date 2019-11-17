package users.maxim;

import de.embl.cba.bdv.utils.viewer.OpenMultipleImagesCommand;
import net.imagej.ImageJ;

import java.io.File;

import static de.embl.cba.bdv.utils.FileUtils.getFileList;

public class ExploreXRayDataWithViewMultipleBdvImagesCommand
{
	public static void main(final String... args)
	{
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();

		final OpenMultipleImagesCommand command = new OpenMultipleImagesCommand();
		command.inputFiles = getFileList( new File( "/Volumes/cba/exchange/maxim/ver2" ), ".*.xml" ).toArray( new File[]{} );

		command.run();
	}
}
