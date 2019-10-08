package de.embl.cba.bdv.utils.behaviour;

import bdv.util.BdvHandle;
import de.embl.cba.bdv.utils.BdvUtils;
import de.embl.cba.bdv.utils.Logger;
import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform3D;
import org.scijava.ui.behaviour.ClickBehaviour;

public class Behaviours
{
	public static void addPositionAndViewLoggingBehaviour( BdvHandle bdv, org.scijava.ui.behaviour.util.Behaviours behaviours )
	{
		behaviours.behaviour( ( ClickBehaviour ) ( x, y ) -> {

			(new Thread( () -> {
				final RealPoint globalMouseCoordinates = BdvUtils.getGlobalMouseCoordinates( bdv );
				Logger.log( "\nBigDataViewer position: \n" + globalMouseCoordinates.toString() );
				Logger.log( "BigDataViewer transform: \n"+ getBdvViewerTransform( bdv ) );
			} )).start();

		}, "Print position and view", "P"  ) ;

	}

	public static String getBdvViewerTransform( BdvHandle bdv )
	{
		final AffineTransform3D view = new AffineTransform3D();
		bdv.getViewerPanel().getState().getViewerTransform( view );

		return view.toString().replace( "3d-affine", "View" );
	}

}
