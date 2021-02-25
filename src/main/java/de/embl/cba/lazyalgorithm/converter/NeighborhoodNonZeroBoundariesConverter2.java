/*-
 * #%L
 * Various Java code for ImageJ
 * %%
 * Copyright (C) 2018 - 2021 EMBL
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
package de.embl.cba.lazyalgorithm.converter;

import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

/**
 * This version of NeighborhoodNonZeroBoundariesConverter
 * might be faster, but one does need to know the RAI
 * on which to compute on upon time of construction.
 *
 * @param <R>
 */
public class NeighborhoodNonZeroBoundariesConverter2< R extends RealType< R > >
		implements Converter< Neighborhood< R >, R >
{
	private final RandomAccessibleInterval< R > rai;

	public NeighborhoodNonZeroBoundariesConverter2( RandomAccessibleInterval< R > rai )
	{
		this.rai = rai;
	}

	@Override
	public void convert( Neighborhood< R > neighborhood, R output )
	{
		final double centerValue = getCenterValue( neighborhood );

		if ( centerValue == 0 )
		{
			output.setZero();
			return;
		}

		for ( R value : neighborhood )
		{
			if ( value.getRealDouble() != centerValue )
			{
				output.setReal( centerValue );
				return;
			}
		}

		output.setZero();
		return;
	}

	private double getCenterValue( Neighborhood< R > neighborhood )
	{
		long[] centrePosition = new long[ neighborhood.numDimensions() ];
		neighborhood.localize( centrePosition );

		final RandomAccess< R > randomAccess = rai.randomAccess();
		randomAccess.setPosition( centrePosition );
		return randomAccess.get().getRealDouble();
	}
}
