package tests;

import bdv.util.*;
import de.embl.cba.bdv.utils.capture.BdvViewCaptures;
import de.embl.cba.bdv.utils.capture.PixelSpacingDialog;
import examples.Utils;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;
import net.imagej.ImageJ;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayCursor;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedIntType;
import net.imglib2.util.Util;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.junit.Test;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

import java.util.List;

public class TestViewCapture < R extends RealType< R > >
{
	@Test
	public void captureView()
	{
		int w = 1000;
		final RandomAccessibleInterval< UnsignedIntType > rai = createImage( w, w, 50 );
		final RandomAccessibleIntervalSource< ? > source = new RandomAccessibleIntervalSource( rai, Util.getTypeFromInterval( rai ), "image" );

		final BdvStackSource< ? > show = BdvFunctions.show( source );
		show.setDisplayRange( 0, w );

		BdvViewCaptures.captureView(
				show.getBdvHandle(),
				1,
				"nanometer",
				false ).show();
	}


	public static RandomAccessibleInterval< UnsignedIntType > createImage( long... dimensions )
	{
		ArrayImg< UnsignedIntType, IntArray > img
				= ArrayImgs.unsignedInts( dimensions );
		paintPixelValues( img );
		return img;
	}

	public static void paintPixelValues( ArrayImg< UnsignedIntType, IntArray > img )
	{
		ArrayCursor< UnsignedIntType > cursor = img.cursor();
		while ( cursor.hasNext() )
			cursor.next().set( cursor.getIntPosition( 0 ) );
	}

	public static void main( String[] args ) throws SpimDataException
	{
		new ImageJ().ui().showUI();
		new TestViewCapture().captureView();
	}


}
