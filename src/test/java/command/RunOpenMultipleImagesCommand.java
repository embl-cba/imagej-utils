package command;

import de.embl.cba.bdv.utils.viewer.OpenMultipleImagesCommand;
import net.imagej.ImageJ;

public class RunOpenMultipleImagesCommand
{
	public static void main( String[] args )
	{
		final ImageJ imageJ = new ImageJ();
		imageJ.ui().showUI();

		imageJ.command().run( OpenMultipleImagesCommand.class, true );
	}
}
