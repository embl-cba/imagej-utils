package de.embl.cba.bdv.utils.render;

import bdv.viewer.Source;
import bdv.viewer.render.AccumulateProjector;
import bdv.viewer.render.AccumulateProjectorFactory;
import bdv.viewer.render.VolatileProjector;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.ARGBType;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

public class AccumulateEMProjectorARGB extends AccumulateProjector< ARGBType, ARGBType >
{
	public static AccumulateProjectorFactory< ARGBType > factory = new AccumulateProjectorFactory< ARGBType >()
	{
		@Override
		public AccumulateEMProjectorARGB createAccumulateProjector(
				final ArrayList< VolatileProjector > sourceProjectors,
				final ArrayList< Source< ? > > sources,
				final ArrayList< ? extends RandomAccessible< ? extends ARGBType > > sourceScreenImages,
				final RandomAccessibleInterval< ARGBType > targetScreenImages,
				final int numThreads,
				final ExecutorService executorService )
		{
			return new AccumulateEMProjectorARGB( sourceProjectors, sourceScreenImages, targetScreenImages, numThreads, executorService );
		}
	};

	public AccumulateEMProjectorARGB(
			final ArrayList< VolatileProjector > sourceProjectors,
			final ArrayList< ? extends RandomAccessible< ? extends ARGBType > > sources,
			final RandomAccessibleInterval< ARGBType > target,
			final int numThreads,
			final ExecutorService executorService )
	{
		super( sourceProjectors, sources, target, numThreads, executorService );
	}

	@Override
	protected void accumulate( final Cursor< ? extends ARGBType >[] accesses, final ARGBType target )
	{
		int aSum = 0, rSum = 0, gSum = 0, bSum = 0;
		for ( final Cursor< ? extends ARGBType > access : accesses )
		{
			final int value = access.get().get();
			final int a = ARGBType.alpha( value );
			final int r = ARGBType.red( value );
			final int g = ARGBType.green( value );
			final int b = ARGBType.blue( value );
			aSum += a;
			rSum += r;
			gSum += g;
			bSum += b;
		}
		if ( aSum > 255 )
			aSum = 255;
		if ( rSum > 255 )
			rSum = 255;
		if ( gSum > 255 )
			gSum = 255;
		if ( bSum > 255 )
			bSum = 255;
		target.set( ARGBType.rgba( rSum, gSum, bSum, aSum ) );
	}
}
