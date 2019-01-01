import bdv.VolatileSpimSource;
import bdv.util.BdvFunctions;
import de.embl.cba.bdv.utils.argbconversion.VolatileARGBConvertedRealSource;
import de.embl.cba.bdv.utils.argbconversion.SelectableRealVolatileARGBConverter;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;

public class ExampleARGBConvertedRealTypeSource
{
	public static void main( String[] args ) throws SpimDataException
	{
		final String labelsSourcePath = ExampleARGBConvertedRealTypeSource.class.getResource( "labels.xml" ).getFile();

		SpimData spimData = new XmlIoSpimData().load( labelsSourcePath );

		final VolatileSpimSource volatileSpimSource = new VolatileSpimSource( spimData, 0, "name" );

		final SelectableRealVolatileARGBConverter converter = new SelectableRealVolatileARGBConverter( );

		final VolatileARGBConvertedRealSource volatileARGBConvertedRealSource = new VolatileARGBConvertedRealSource( volatileSpimSource, converter );

		BdvFunctions.show( volatileARGBConvertedRealSource );

	}
}
