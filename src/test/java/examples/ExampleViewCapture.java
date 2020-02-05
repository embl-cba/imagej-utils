package examples;

import bdv.util.*;
import de.embl.cba.bdv.utils.capture.BdvViewCaptures;
import de.embl.cba.bdv.utils.capture.ViewCaptureResult;
import de.embl.cba.bdv.utils.converters.LinearARGBConverter;
import de.embl.cba.bdv.utils.converters.SelectableVolatileARGBConverter;
import de.embl.cba.bdv.utils.lut.Luts;
import de.embl.cba.bdv.utils.sources.ARGBConvertedRealSource;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;
import net.imagej.ImageJ;
import net.imglib2.img.array.ArrayCursor;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedIntType;
import net.imglib2.util.Util;

import java.util.List;

public class ExampleViewCapture
{
	public static void main( String[] args ) throws SpimDataException
	{
		new ImageJ(  ).ui().showUI();

		Prefs.showScaleBar( true );

		/**
		 * show first image
		 */
		String path = ExampleViewCapture.class
				.getResource( "../multi-resolution-mri-stack.xml" ).getFile();

		SpimData spimData = new XmlIoSpimData().load( path );

		List< BdvStackSource< ? > > stackSources = BdvFunctions.show( spimData, BdvOptions.options().preferredSize( 600,600 ) );
		stackSources.get( 0 ).setDisplayRange( 0, 255 );
		stackSources.get( 0 ).setColor( new ARGBType( ARGBType.rgba( 255,0,0,255 ) ) );

		final BdvHandle bdvHandle = stackSources.get( 0 ).getBdvHandle();

		/**
		 * add another image
		 */
		path = ExampleViewCapture.class
				.getResource( "../mri-stack-shifted.xml" ).getFile();
		spimData = new XmlIoSpimData().load( path );
		stackSources = BdvFunctions.show( spimData, BdvOptions.options().addTo( bdvHandle ) );
		stackSources.get( 0 ).setDisplayRange( 0, 255 );


		/**
		 * also add an ARBGType image
		 */

		final ArrayImg< UnsignedIntType, IntArray > img = ArrayImgs.unsignedInts( 256, 256, 1000 );
		final ArrayCursor< UnsignedIntType > cursor = img.cursor();
		while ( cursor.hasNext() )
			cursor.next().set( cursor.getIntPosition( 0 ) );

		final RandomAccessibleIntervalSource source = new RandomAccessibleIntervalSource( img, Util.getTypeFromInterval( img ), "" );
		final SelectableVolatileARGBConverter converter = new SelectableVolatileARGBConverter( new LinearARGBConverter( 0, 255,  Luts.BLUE_WHITE_RED ) );
		final ARGBConvertedRealSource convertedRealSource = new ARGBConvertedRealSource( source, converter );

		BdvFunctions.show( convertedRealSource, BdvOptions.options().addTo( bdvHandle ) );


		/**
		 * capture a view
		 */
		final ViewCaptureResult captureResult = BdvViewCaptures.captureView( bdvHandle, 1, "micron", true );

		captureResult.rawImagesStack.show();
		captureResult.rgbImage.show();

	}

	private static void rotateView( BdvHandle bdvHandle )
	{
		// get current transform
		AffineTransform3D view = new AffineTransform3D();
		bdvHandle.getViewerPanel().getState().getViewerTransform(view);

		final AffineTransform3D rotate = new AffineTransform3D();
		rotate.rotate( 1, 45.0 / Math.PI );
		rotate.translate( 500,0,300 );

		// change the transform
		view = view.preConcatenate(rotate);

		// submit to BDV
		bdvHandle.getViewerPanel().setCurrentViewerTransform(view);
	}


}
