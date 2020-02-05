
import bdv.VolatileSpimSource;
import de.embl.cba.bdv.utils.sources.SelectableARGBConvertedRealSource;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;

public class Examples
{
	public static SelectableARGBConvertedRealSource getSelectable3DSource() throws SpimDataException
	{
		final String labelsSourcePath = ExampleARGBConvertedLabelsSource.class.getResource( "test-data/labels-ulong.xml" ).getFile();

		SpimData spimData = new XmlIoSpimData().load( labelsSourcePath );

		final VolatileSpimSource volatileSpimSource = new VolatileSpimSource( spimData, 0, "name" );

		return new SelectableARGBConvertedRealSource( volatileSpimSource );
	}
}
