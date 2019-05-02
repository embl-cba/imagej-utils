package examples;

import bdv.util.BdvFunctions;
import bdv.util.BdvHandle;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import de.embl.cba.bdv.utils.BdvViewCaptures;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;
import net.imagej.ImageJ;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.type.numeric.integer.UnsignedIntType;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

import java.util.List;

public class ExampleViewCapture
{
	public static void main( String[] args ) throws SpimDataException
	{
		new ImageJ().ui().showUI();

		/**
		 * show first image
		 */
		final String path = ExampleViewCapture.class
				.getResource( "../mri-stack.xml" ).getFile();

		SpimData spimData = new XmlIoSpimData().load( path );

		final List< BdvStackSource< ? > > stackSources = BdvFunctions.show( spimData );
		stackSources.get( 0 ).setDisplayRange( 0, 255 );

		final BdvHandle bdv = stackSources.get( 0 ).getBdvHandle();

		/**
		 * add another image
		 */
		final ArrayImg< UnsignedIntType, IntArray > img0 = Utils.create2DGradientImage();

		final BdvStackSource stackSource0 = BdvFunctions.show(
				img0, "image 0",
				BdvOptions.options().addTo( bdv ) );
		stackSource0.setDisplayRange( 0, 300 );

		/**
		 * install view capture behaviour
		 */
		Behaviours behaviours = new Behaviours( new InputTriggerConfig() );
		behaviours.install( bdv.getTriggerbindings(), "" );
		behaviours.behaviour( ( ClickBehaviour ) ( x, y ) ->
		{
			BdvViewCaptures.captureView(
					bdv, 1.0, "nanometer" );
		}, "capture view", "C" ) ;
	}


}
