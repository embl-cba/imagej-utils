import bdv.VolatileSpimSource;
import de.embl.cba.bdv.utils.sources.SelectableVolatileARGBConvertedRealSource;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;

public class Examples
{
	public static SelectableVolatileARGBConvertedRealSource getSelectable3DSource() throws SpimDataException
	{
		final String labelsSourcePath = ExampleARGBConvertedLabelsSource.class.getResource( "labels.xml" ).getFile();

		SpimData spimData = new XmlIoSpimData().load( labelsSourcePath );

		final VolatileSpimSource volatileSpimSource = new VolatileSpimSource( spimData, 0, "name" );

		return new SelectableVolatileARGBConvertedRealSource( volatileSpimSource );
	}
}
