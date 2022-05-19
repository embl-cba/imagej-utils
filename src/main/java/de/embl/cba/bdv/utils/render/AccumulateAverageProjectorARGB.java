/*-
 * #%L
 * Various Java code for ImageJ
 * %%
 * Copyright (C) 2018 - 2022 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package de.embl.cba.bdv.utils.render;

import bdv.tools.transformation.TransformedSource;
import bdv.viewer.Source;
import bdv.viewer.render.AccumulateProjector;
import bdv.viewer.render.AccumulateProjectorFactory;
import bdv.viewer.render.VolatileProjector;
import de.embl.cba.bdv.utils.sources.Metadata;
import de.embl.cba.bdv.utils.sources.Sources;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.ARGBType;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

public class AccumulateAverageProjectorARGB extends AccumulateProjector< ARGBType, ARGBType >
{
	public static AccumulateProjectorFactory< ARGBType > factory = new AccumulateProjectorFactory< ARGBType >()
	{
		@Override
		public AccumulateAverageProjectorARGB createAccumulateProjector(
				final ArrayList< VolatileProjector > sourceProjectors,
				final ArrayList< Source< ? > > sources,
				final ArrayList< ? extends RandomAccessible< ? extends ARGBType > > sourceScreenImages,
				final RandomAccessibleInterval< ARGBType > targetScreenImage,
				final int numThreads,
				final ExecutorService executorService )
		{
			return new AccumulateAverageProjectorARGB(
					sourceProjectors,
					sources,
					sourceScreenImages,
					targetScreenImage,
					numThreads,
					executorService );
		}
	};

	private final ArrayList< Source< ? > > sourceList;

	public AccumulateAverageProjectorARGB(
			final ArrayList< VolatileProjector > sourceProjectors,
			final ArrayList< Source< ? > > sources,
			final ArrayList< ? extends RandomAccessible< ? extends ARGBType > > sourceScreenImages,
			final RandomAccessibleInterval< ARGBType > target,
			final int numThreads,
			final ExecutorService executorService )
	{
		super( sourceProjectors, sourceScreenImages, target, numThreads, executorService );
		this.sourceList = sources;
	}

	@Override
	protected void accumulate(
			final Cursor< ? extends ARGBType >[] accesses,
			final ARGBType target )
	{
		int aAvg = 0, rAvg = 0, gAvg = 0, bAvg = 0, numNonZeroAvg = 0;

		final double[] position = new double[ 3 ];
		for ( final Cursor< ? extends ARGBType > access : accesses )
		{
			final int value = access.get().get();
			access.localize( position );
			final int a = ARGBType.alpha( value );
			final int r = ARGBType.red( value );
			final int g = ARGBType.green( value );
			final int b = ARGBType.blue( value );

			//if ( a == 0 ) continue; // TODO: think about this!

			aAvg += a;
			rAvg += r;
			gAvg += g;
			bAvg += b;
			numNonZeroAvg++;
		}

		if ( numNonZeroAvg > 0 )
		{
			aAvg /= numNonZeroAvg;
			rAvg /= numNonZeroAvg;
			gAvg /= numNonZeroAvg;
			bAvg /= numNonZeroAvg;
		}

		if ( aAvg > 255 )
			aAvg = 255;
		if ( rAvg > 255 )
			rAvg = 255;
		if ( gAvg > 255 )
			gAvg = 255;
		if ( bAvg > 255 )
			bAvg = 255;

		target.set( ARGBType.rgba( rAvg, gAvg, bAvg, aAvg ) );
	}

}
