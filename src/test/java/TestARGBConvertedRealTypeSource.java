import bdv.VolatileSpimSource;
import bdv.util.BdvFunctions;
import bdv.viewer.Source;
import de.embl.cba.bdv.utils.labels.ARGBConvertedRealTypeSource;
import de.embl.cba.bdv.utils.labels.ARGBConvertedVolatileRealTypeSource;
import de.embl.cba.bdv.utils.labels.ConfigurableRealTypeVolatileARGBTypeConverter;
import de.embl.cba.bdv.utils.labels.ConfigurableVolatileRealVolatileARGBConverter;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;
import net.imglib2.type.volatiles.VolatileARGBType;

public class TestARGBConvertedRealTypeSource
{
	public static void main( String[] args ) throws SpimDataException
	{
		final String labelsSourcePath = TestARGBConvertedRealTypeSpimDataSource.class.getResource( "labels.xml" ).getFile();

		SpimData spimData = new XmlIoSpimData().load( labelsSourcePath );

		final VolatileSpimSource volatileSpimSource = new VolatileSpimSource( spimData, 0, "name" );

//		final ConfigurableVolatileRealVolatileARGBConverter converter = new ConfigurableVolatileRealVolatileARGBConverter( );

		final ConfigurableRealTypeVolatileARGBTypeConverter converter = new ConfigurableRealTypeVolatileARGBTypeConverter( );

		final Source< VolatileARGBType > labelsSource = new ARGBConvertedRealTypeSource( volatileSpimSource, "labels", converter );

		BdvFunctions.show( labelsSource );

	}
}
