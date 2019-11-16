package de.embl.cba.bdv.utils.behaviour;

import bdv.util.BdvHandle;
import de.embl.cba.bdv.utils.BdvDialogs;
import de.embl.cba.bdv.utils.BdvUtils;
import de.embl.cba.bdv.utils.Logger;
import de.embl.cba.bdv.utils.capture.BdvViewCaptures;
import de.embl.cba.bdv.utils.capture.PixelSpacingDialog;
import ij.CompositeImage;
import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform3D;
import org.scijava.ui.behaviour.ClickBehaviour;

// TODO:
// - remove logging, return things

public class BdvBehaviours
{
	public static void addPositionAndViewLoggingBehaviour(
			BdvHandle bdv,
			org.scijava.ui.behaviour.util.Behaviours behaviours,
			String trigger )
	{
		behaviours.behaviour( ( ClickBehaviour ) ( x, y ) -> {

			(new Thread( () -> {
				final RealPoint globalMouseCoordinates = BdvUtils.getGlobalMouseCoordinates( bdv );
				Logger.log( "\nBigDataViewer position: \n" + globalMouseCoordinates.toString() );
				Logger.log( "BigDataViewer transform: \n"+ getBdvViewerTransform( bdv ) );
			} )).start();

		}, "Print position and view", trigger ) ;
	}

	public static void addViewCaptureBehaviour(
			BdvHandle bdv,
			org.scijava.ui.behaviour.util.Behaviours behaviours,
			String trigger )
	{
		behaviours.behaviour( ( ClickBehaviour ) ( x, y ) ->
		{
			new Thread( () -> {
				final String pixelUnit = "micrometer";
				final PixelSpacingDialog dialog = new PixelSpacingDialog( BdvUtils.getViewerVoxelSpacing( bdv )[ 0 ], pixelUnit );
				if ( ! dialog.showDialog() ) return;
				final CompositeImage compositeImage = BdvViewCaptures.captureView(
						bdv,
						dialog.getPixelSpacing(),
						pixelUnit,
						false );
				compositeImage.show();
			}).start();
		}, "capture view", trigger ) ;
	}

	public static void addDisplaySettingsBehaviour(
			BdvHandle bdv,
			org.scijava.ui.behaviour.util.Behaviours behaviours,
			String trigger )
	{
		behaviours.behaviour( ( ClickBehaviour ) ( x, y ) ->
						BdvDialogs.showDisplaySettingsDialogForSourcesAtMousePosition(
								bdv,
								false,
								true ),
				"show display settings dialog",
				trigger ) ;
	}

	public static String getBdvViewerTransform( BdvHandle bdv )
	{
		final AffineTransform3D view = new AffineTransform3D();
		bdv.getViewerPanel().getState().getViewerTransform( view );

		return view.toString().replace( "3d-affine", "View" );
	}

}
