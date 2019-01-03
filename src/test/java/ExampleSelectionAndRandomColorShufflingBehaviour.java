import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.RandomAccessibleIntervalSource;
import de.embl.cba.bdv.utils.behaviour.BehaviourRandomColorShufflingEventHandler;
import de.embl.cba.bdv.utils.behaviour.BdvSelectionEventHandler;
import de.embl.cba.bdv.utils.converters.argb.RandomARGBConverter;
import de.embl.cba.bdv.utils.converters.argb.SelectableVolatileARGBConverter;
import de.embl.cba.bdv.utils.converters.argb.VolatileARGBConvertedRealSource;
import de.embl.cba.bdv.utils.lut.RandomARGBLut;
import ij.IJ;
import ij.ImagePlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

public class ExampleSelectionAndRandomColorShufflingBehaviour
{
	public static void main( String[] args )
	{
		final RandomAccessibleIntervalSource raiSource = ExampleARGBConverted2d16bitTiffImage.getRandomAccessibleIntervalSource();

		final RandomARGBConverter randomARGBConverter = new RandomARGBConverter();

		final SelectableVolatileARGBConverter argbConverter = new SelectableVolatileARGBConverter( randomARGBConverter );

		final VolatileARGBConvertedRealSource argbSource = new VolatileARGBConvertedRealSource( raiSource, argbConverter );

		Bdv bdv = BdvFunctions.show( argbSource, BdvOptions.options().is2D() ).getBdvHandle();

		new BdvSelectionEventHandler( bdv, argbSource, argbConverter );

		new BehaviourRandomColorShufflingEventHandler( bdv, randomARGBConverter, raiSource.getName() );
	}

}
