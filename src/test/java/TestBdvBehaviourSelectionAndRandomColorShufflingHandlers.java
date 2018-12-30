import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.RandomAccessibleIntervalSource;
import de.embl.cba.bdv.utils.behaviour.BehaviourRandomColorShufflingEventHandler;
import de.embl.cba.bdv.utils.behaviour.BehaviourSelectionEventHandler;
import de.embl.cba.bdv.utils.argbconversion.SelectableRealVolatileARGBConverter;
import de.embl.cba.bdv.utils.argbconversion.VolatileARGBConvertedRealSource;
import de.embl.cba.bdv.utils.lut.RandomARGBLut;
import ij.IJ;
import ij.ImagePlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

public class TestBdvBehaviourSelectionAndRandomColorShufflingHandlers
{
	public static void main( String[] args )
	{
		final RandomAccessibleIntervalSource raiSource = getRandomAccessibleIntervalSource();

		final RandomARGBLut randomARGBLUT = new RandomARGBLut();

		final SelectableRealVolatileARGBConverter argbConverter = new SelectableRealVolatileARGBConverter( randomARGBLUT );

		final VolatileARGBConvertedRealSource argbSource = new VolatileARGBConvertedRealSource( raiSource, argbConverter );

		Bdv bdv = BdvFunctions.show( argbSource, BdvOptions.options().is2D() ).getBdvHandle();

		new BehaviourSelectionEventHandler( bdv, argbSource, argbConverter );

		new BehaviourRandomColorShufflingEventHandler( bdv, randomARGBLUT, raiSource.getName() );
	}


	public static RandomAccessibleIntervalSource getRandomAccessibleIntervalSource()
	{
		final ImagePlus imagePlus = IJ.openImage( TestARGBConverted2d16bitTiffImage.class.getResource( "2d-16bit-labelMask.tif" ).getFile() );

		RandomAccessibleInterval< RealType > wrap = ImageJFunctions.wrapReal( imagePlus );

		// needs to be at least 3D
		wrap = Views.addDimension( wrap, 0, 0);

		return new RandomAccessibleIntervalSource( wrap, Util.getTypeFromInterval( wrap ), imagePlus.getTitle() );
	}
}
