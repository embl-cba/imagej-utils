import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import bdv.viewer.Source;
import de.embl.cba.bdv.utils.labels.ARGBConvertedRealTypeSpimDataSource;
import de.embl.cba.bdv.utils.labels.ConfigurableVolatileRealVolatileARGBConverter;
import de.embl.cba.bdv.utils.transformhandlers.BehaviourTransformEventHandler3DLeftMouseDrag;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;
import net.imglib2.type.volatiles.VolatileARGBType;

@Deprecated
public class TestARGBConvertedRealTypeSpimDataSource
{
	public static void main( String[] args ) throws SpimDataException
	{
		final String labelsSourcePath = TestARGBConvertedRealTypeSpimDataSource.class.getResource( "labels.xml" ).getFile();

		SpimData spimData = new XmlIoSpimData().load( labelsSourcePath );

		final ConfigurableVolatileRealVolatileARGBConverter converter = new ConfigurableVolatileRealVolatileARGBConverter( );

		final Source< VolatileARGBType > labelsSource = new ARGBConvertedRealTypeSpimDataSource( spimData, "labels", 0, converter );

		BdvFunctions.show( labelsSource );

	}
}
