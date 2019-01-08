import bdv.VolatileSpimSource;
import bdv.util.BdvFunctions;
import bdv.util.BdvHandle;
import bdv.util.BdvOptions;
import de.embl.cba.bdv.utils.converters.LinearMappingARGBConverter;
import de.embl.cba.bdv.utils.converters.SelectableVolatileARGBConverter;
import de.embl.cba.bdv.utils.lut.Luts;
import de.embl.cba.bdv.utils.sources.SelectableVolatileARGBConvertedRealSource;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;

import javax.swing.*;

public class ExampleLargeLabelsSource
{
	public static void main( String[] args ) throws SpimDataException
	{
		SpimData labels = new XmlIoSpimData().load(
				"/Volumes/arendt/EM_6dpf_segmentation/EM-Prospr/em-segmented-cells-labels.xml" );

		final LinearMappingARGBConverter linearMappingARGBConverter =
				new LinearMappingARGBConverter( d -> d + 1, 0, 50, Luts.BLUE_WHITE_RED  );

		final SelectableVolatileARGBConverter selectableVolatileARGBConverter =
				new SelectableVolatileARGBConverter( linearMappingARGBConverter );

		final VolatileSpimSource volatileSpimSource = new VolatileSpimSource( labels, 0, "name" );

		final SelectableVolatileARGBConvertedRealSource convertedSource =
				new SelectableVolatileARGBConvertedRealSource(
						volatileSpimSource,
						selectableVolatileARGBConverter );


		final BdvHandle bdvHandle = BdvFunctions.show( convertedSource ).getBdvHandle();

		SpimData emRaw = new XmlIoSpimData().load( "/Volumes/arendt/EM_6dpf_segmentation/EM-Prospr/em-raw-full-res.xml" );

		BdvFunctions.show( emRaw, BdvOptions.options().addTo( bdvHandle ) ).get( 0 ).setDisplayRange( 0, 500 );


	}
}
