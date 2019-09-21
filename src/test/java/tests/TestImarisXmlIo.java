package tests;

import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import bdv.util.BdvFunctions;
import de.embl.cba.bdv.utils.loaders.imaris.Imaris2;
import mpicbg.spim.data.SpimDataException;
import org.junit.Test;

import java.io.IOException;

public class TestImarisXmlIo
{
	public static final String XML_FILENAME = "/Users/tischer/Documents/bdv-utils/src/test/resources/test-data/imaris/volume.xml";


	@Test
	public void createImarisXml() throws IOException, SpimDataException
	{
		final SpimDataMinimal spimData = Imaris2.openIms(
				"/Users/tischer/Documents/bdv-utils/src/test/resources/test-data/imaris/volume.ims" );

		new XmlIoSpimDataMinimal().save( spimData, XML_FILENAME );
	}

	@Test
	public void openImarisXml() throws SpimDataException
	{
		final SpimDataMinimal spimDataMinimal = new XmlIoSpimDataMinimal().load( XML_FILENAME );

		BdvFunctions.show( spimDataMinimal );
	}

	public static void main( String[] args ) throws SpimDataException, IOException
	{
		new TestImarisXmlIo().openImarisXml();
//		new TestImarisXmlIo().createImarisXml();
	}
}
