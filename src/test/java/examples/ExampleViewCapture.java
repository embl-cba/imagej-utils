package examples;

import bdv.util.BdvFunctions;
import bdv.util.BdvHandle;
import bdv.util.BdvStackSource;
import de.embl.cba.bdv.utils.BdvDialogs;
import de.embl.cba.bdv.utils.BdvUtils;
import de.embl.cba.bdv.utils.BdvViewCaptures;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;
import net.imagej.ImageJ;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

import java.util.List;

public class ExampleViewCapture
{
	public static void main( String[] args ) throws SpimDataException
	{
		new ImageJ().ui().showUI();

		final String path = ExampleViewCapture.class
				.getResource( "../mri-stack.xml" ).getFile();

		SpimData spimData = new XmlIoSpimData().load( path );

		final List< BdvStackSource< ? > > stackSources = BdvFunctions.show( spimData );
		stackSources.get( 0 ).setDisplayRange( 0, 255 );

		final BdvHandle bdv = stackSources.get( 0 ).getBdvHandle();

		/**
		 * install view capture behaviour
		 */
		Behaviours behaviours = new Behaviours( new InputTriggerConfig() );
		behaviours.install( bdv.getTriggerbindings(), "" );
		behaviours.behaviour( ( ClickBehaviour ) ( x, y ) ->
		{
			BdvViewCaptures.captureView2(
					bdv, 1.0 );
		}, "capture view", "C" ) ;


	}
}
