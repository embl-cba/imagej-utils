import bdv.VolatileSpimSource;
import bdv.util.BdvFunctions;
import de.embl.cba.bdv.utils.converters.argb.VolatileARGBConvertedRealSource;
import de.embl.cba.bdv.utils.converters.argb.SelectableVolatileARGBConverter;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;

public class ExampleARGBConvertedLabelsSource
{
	public static void main( String[] args ) throws SpimDataException
	{
		final VolatileARGBConvertedRealSource volatileARGBConvertedRealSource = getLabelsSource();

		BdvFunctions.show( volatileARGBConvertedRealSource );

	}

	public static VolatileARGBConvertedRealSource getLabelsSource() throws SpimDataException
	{
		final String labelsSourcePath = ExampleARGBConvertedLabelsSource.class.getResource( "labels.xml" ).getFile();

		SpimData spimData = new XmlIoSpimData().load( labelsSourcePath );

		final VolatileSpimSource volatileSpimSource = new VolatileSpimSource( spimData, 0, "name" );

		final SelectableVolatileARGBConverter converter = new SelectableVolatileARGBConverter( );

		return new VolatileARGBConvertedRealSource( volatileSpimSource, converter );
	}
}
