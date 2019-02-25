import de.embl.cba.bdv.utils.io.BdvWriter;
import ij.IJ;
import ij.ImagePlus;

import java.io.File;

public class TestBdvWriter
{
	public static void main( String[] args )
	{
		final ImagePlus imagePlus = IJ.openImage(
				TestBdvWriter.class.getResource(
						"imagePlus-20x-20y-1c-5z.zip" ).getFile() );

		String xmlOutputPath = "/Users/tischer/Desktop/bdv-test.xml";

		BdvWriter.saveAsBdv( imagePlus, new File( xmlOutputPath ) );

	}
}
