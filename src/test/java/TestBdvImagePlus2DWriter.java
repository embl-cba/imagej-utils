import de.embl.cba.bdv.utils.io.BdvImagePlusExport;
import ij.IJ;
import ij.ImagePlus;

import java.io.File;

public class TestBdvImagePlus2DWriter
{
	public static void main( String[] args )
	{
		final ImagePlus imagePlus = IJ.openImage(
				TestBdvImagePlus2DWriter.class.getResource(
						"test-data/WellH12_PointH12_0004_ChannelDAPI,WF_GFP,TRITC_Seq0760.tif" ).getFile() );

		String xmlOutputPath = "/Users/tischer/Desktop/bdv-test.xml";

		BdvImagePlusExport.saveAsBdv( imagePlus, new File( xmlOutputPath ) );
	}
}
