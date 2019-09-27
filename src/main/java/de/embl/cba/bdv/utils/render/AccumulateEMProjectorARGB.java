package de.embl.cba.bdv.utils.render;

import bdv.util.Bdv;
import bdv.util.BdvHandle;
import bdv.viewer.Source;
import bdv.viewer.render.AccumulateProjector;
import bdv.viewer.render.AccumulateProjectorFactory;
import bdv.viewer.render.VolatileProjector;
import de.embl.cba.bdv.utils.BdvUtils;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.ARGBType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class AccumulateEMProjectorARGB extends AccumulateProjector< ARGBType, ARGBType >
{
	public static final int SUM = 0;
	private BdvHandle bdvHandle;
	private Map< String, String > sourceNameToAccumulationModality;
	private ArrayList< Integer > accumulationModalities;

	public static class AccumulateEMProjectorFactory implements AccumulateProjectorFactory< ARGBType >
	{
		private AccumulateEMProjectorARGB accumulateEMProjectorARGB;

		@Override
		public AccumulateEMProjectorARGB createAccumulateProjector(
				final ArrayList< VolatileProjector > sourceProjectors,
				final ArrayList< Source< ? > > sources,
				final ArrayList< ? extends RandomAccessible< ? extends ARGBType > > sourceScreenImages,
				final RandomAccessibleInterval< ARGBType > targetScreenImages,
				final int numThreads,
				final ExecutorService executorService )
		{
			accumulateEMProjectorARGB = new AccumulateEMProjectorARGB( sourceProjectors, sources, sourceScreenImages, targetScreenImages, numThreads, executorService );

			return accumulateEMProjectorARGB;
		}

		public void setBdvHandle( BdvHandle bdvHandle )
		{
			accumulateEMProjectorARGB.setBdvHandle( bdvHandle );
		}
	}

	public AccumulateEMProjectorARGB(
			final ArrayList< VolatileProjector > sourceProjectors,
			final ArrayList< Source< ? > > sources,
			final ArrayList< ? extends RandomAccessible< ? extends ARGBType > > sourceScreenImages,
			final RandomAccessibleInterval< ARGBType > target,
			final int numThreads,
			final ExecutorService executorService )
	{
		super( sourceProjectors, sourceScreenImages, target, numThreads, executorService );
		sourceNameToAccumulationModality = new HashMap<>(  );
	}

	@Override
	protected void accumulate( final Cursor< ? extends ARGBType >[] accesses, final ARGBType target )
	{
		final List< Integer > visibleSourceIndices = BdvUtils.getVisibleSourceIndices( bdvHandle );
		accumulationModalities = new ArrayList<>();

		for( int visibleSourceIndex : visibleSourceIndices )
		{
			if ( bdvHandle != null )
			{
				final String sourceName = BdvUtils.getSourceName( bdvHandle, visibleSourceIndex );
				if ( sourceNameToAccumulationModality.containsKey( sourceName ) )
				{
					int a = 1;
				}
			} else {
				accumulationModalities.add( SUM );
			}
		}

		int aAccu = 0, rAccu = 0, gAccu = 0, bAccu = 0, numNonZero = 0;

		for ( final Cursor< ? extends ARGBType > access : accesses )
		{
			final int value = access.get().get();
			final int a = ARGBType.alpha( value );
			final int r = ARGBType.red( value );
			final int g = ARGBType.green( value );
			final int b = ARGBType.blue( value );

			if ( r == 0 && g == 0 && b == 0  )
			{
				continue;
			}
			else
			{
				numNonZero++;

				aAccu += a;
				rAccu += r;
				gAccu += g;
				bAccu += b;
			}
		}

		if ( numNonZero > 0 )
		{
			aAccu /= numNonZero;
			rAccu /= numNonZero;
			gAccu /= numNonZero;
			bAccu /= numNonZero;
		}

		if ( aAccu > 255 )
			aAccu = 255;
		if ( rAccu > 255 )
			rAccu = 255;
		if ( gAccu > 255 )
			gAccu = 255;
		if ( bAccu > 255 )
			bAccu = 255;
		target.set( ARGBType.rgba( rAccu, gAccu, bAccu, aAccu ) );
	}

	private void setBdvHandle( BdvHandle bdvHandle )
	{
		this.bdvHandle = bdvHandle;
	}


}
