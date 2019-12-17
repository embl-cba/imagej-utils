package examples;

import bdv.util.*;
import de.embl.cba.bdv.utils.capture.BdvViewCaptures;
import de.embl.cba.bdv.utils.capture.PixelSpacingDialog;
import ij.CompositeImage;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;
import net.imagej.ImageJ;
import net.imglib2.realtransform.AffineTransform3D;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

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
		final String path = ExampleViewCapture.class
				.getResource( "../multi-resolution-mri-stack.xml" ).getFile();

		SpimData spimData = new XmlIoSpimData().load( path );

		final List< BdvStackSource< ? > > stackSources = BdvFunctions.show( spimData, BdvOptions.options().preferredSize( 600,600 ) );
		stackSources.get( 0 ).setDisplayRange( 0, 255 );

		final BdvHandle bdvHandle = stackSources.get( 0 ).getBdvHandle();

		/**
		 * add another image
		 */
//		final IntervalView< UnsignedIntType > img = Views.translate( Utils.create2DGradientImage(), new long[]{ 0, 0, 0 } );
//
//		final BdvStackSource stackSource = BdvFunctions.show(
//				img, "image 0",
//				BdvOptions.options().addTo( bdv ) );
//		stackSource.setDisplayRange( 0, 300 );
//		stackSource.setColor( new ARGBType( ARGBType.rgba( 0, 255, 0, 0 ) ) );
//
//		final AffineTransform3D vt = new AffineTransform3D();
//		bdv.getViewerPanel().getState().getViewerTransform( vt );
//		vt.set( 0, 2, 3 );
//		bdv.getViewerPanel().setCurrentViewerTransform( vt );


		/**
		 * capture a view
		 */

//		BdvViewCaptures.captureView( bdvHandle, 5, "micron", true ).show();

		rotateView( bdvHandle );

		BdvViewCaptures.captureView( bdvHandle, 5, "micron", true ).show();

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
