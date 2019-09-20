package tests;

import bdv.img.imaris.Imaris;
import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import mpicbg.spim.data.SpimDataException;

import java.io.IOException;

public class TestXmlIoImarisImageLoader
{
	public static void main( String[] args ) throws IOException, SpimDataException
	{
		final SpimDataMinimal spimData = Imaris.openIms(
				TestXmlIoImarisImageLoader.class.getResource( "../test-data/imaris/volume.ims" ).getFile() );

		new XmlIoSpimDataMinimal().save( spimData, "/Users/tischer/Documents/bdv-utils/src/test/resources/test-data/imaris/test-data/imaris/volume.xml" );
	}
}
