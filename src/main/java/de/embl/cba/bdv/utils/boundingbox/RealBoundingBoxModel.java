package de.embl.cba.bdv.utils.boundingbox;

import net.imglib2.RealInterval;
import net.imglib2.realtransform.AffineTransform3D;

public class RealBoundingBoxModel implements
		BoundingBoxModel,
		RealBoxSelectionPanel.RealBox
{
	private final ModifiableRealInterval interval;

	private final AffineTransform3D transform;

	public interface IntervalChangedListener
	{
		void intervalChanged();
	}

	private final Listeners.List< IntervalChangedListener > listeners;

	public RealBoundingBoxModel(
			final ModifiableRealInterval interval,
			final AffineTransform3D transform )
	{
		this.interval = interval;
		this.transform = transform;
		listeners = new Listeners.SynchronizedList<>();
	}

	@Override
	public RealInterval getInterval()
	{
		return interval;
	}

	@Override
	public void setInterval( final RealInterval i )
	{
		if ( ! equals( interval, i ) )
		{
			interval.set( i );
			listeners.list.forEach( IntervalChangedListener::intervalChanged );
		}
	}

	public static boolean equals( final RealInterval a, final RealInterval b )
	{
		if ( a.numDimensions() != b.numDimensions() )
			return false;

		for ( int d = 0; d < a.numDimensions(); ++d )
			if ( a.realMin( d ) != b.realMin( d ) || a.realMax( d ) != b.realMax( d ) )
				return false;

		return true;
	}

	@Override
	public void getTransform( final AffineTransform3D t )
	{
		t.set( transform );
	}

	public Listeners< IntervalChangedListener > intervalChangedListeners()
	{
		return listeners;
	}
}
