import bdv.util.BdvFunctions;
import bdv.viewer.Source;
import de.embl.cba.bdv.utils.converters.argb.ARGBConvertedRealTypeSpimDataSource;
import de.embl.cba.bdv.utils.converters.argb.ConfigurableVolatileRealVolatileARGBConverter;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;
import net.imglib2.type.volatiles.VolatileARGBType;

@Deprecated
public class ExampleARGBConvertedRealTypeSpimDataSource
{
	public static void main( String[] args ) throws SpimDataException
	{
		final String labelsSourcePath = ExampleARGBConvertedRealTypeSpimDataSource.class.getResource( "labels.xml" ).getFile();

		SpimData spimData = new XmlIoSpimData().load( labelsSourcePath );

		final ConfigurableVolatileRealVolatileARGBConverter converter = new ConfigurableVolatileRealVolatileARGBConverter( );

		final Source< VolatileARGBType > labelsSource = new ARGBConvertedRealTypeSpimDataSource( spimData, "argbconversion", 0, converter );

		BdvFunctions.show( labelsSource );

	}
}
