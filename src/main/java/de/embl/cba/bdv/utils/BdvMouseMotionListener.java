package de.embl.cba.bdv.utils;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import de.embl.cba.bdv.utils.overlays.BdvGrayValuesOverlay;
import net.imglib2.RealPoint;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Map;

public class BdvMouseMotionListener implements MouseMotionListener
{

	final Bdv bdv;
	private final BdvGrayValuesOverlay bdvGrayValuesOverlay;

	public BdvMouseMotionListener( Bdv bdv )
	{
		this.bdv = bdv;
		bdvGrayValuesOverlay = new BdvGrayValuesOverlay();

		BdvFunctions.showOverlay( bdvGrayValuesOverlay,
				"gray values - overlay",
				BdvOptions.options().addTo( bdv ) );

	}

	@Override
	public void mouseDragged( MouseEvent e )
	{ }

	@Override
	public void mouseMoved( MouseEvent e )
	{
		final RealPoint realPoint = new RealPoint( 3 );
		bdv.getBdvHandle().getViewerPanel().getGlobalMouseCoordinates( realPoint );
		final int currentTimepoint = bdv.getBdvHandle().getViewerPanel().getState().getCurrentTimepoint();

		final Map< Integer, Double > pixelValuesOfActiveSources = BdvUtils.getPixelValuesOfActiveSources( bdv, realPoint, currentTimepoint );

		bdvGrayValuesOverlay.setValuesAndColors( pixelValuesOfActiveSources.values(), null );

	}
}
