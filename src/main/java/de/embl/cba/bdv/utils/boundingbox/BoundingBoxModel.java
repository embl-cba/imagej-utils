package de.embl.cba.bdv.utils.boundingbox;

import net.imglib2.RealInterval;
import net.imglib2.realtransform.AffineTransform3D;

public interface BoundingBoxModel extends BoundingBoxOverlay.BoundingBoxOverlaySource
{
	@Override
	public RealInterval getInterval();

	@Override
	public void getTransform( final AffineTransform3D transform );

	public void setInterval( RealInterval interval );
}
