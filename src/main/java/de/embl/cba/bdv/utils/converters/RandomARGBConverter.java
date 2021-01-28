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
package de.embl.cba.bdv.utils.converters;

import de.embl.cba.bdv.utils.lut.Luts;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.volatiles.VolatileARGBType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RandomARGBConverter implements Converter< RealType, VolatileARGBType >
{
	final static public double goldenRatio = 1.0 / ( 0.5 * Math.sqrt( 5 ) + 0.5 );
	long seed;
	byte[][] lut;
	Map< Double, Integer > doubleToARGBIndex = new ConcurrentHashMap<>(  );

	public RandomARGBConverter( )
	{
		this.lut = Luts.GLASBEY;
		this.seed = 50;
	}

	public RandomARGBConverter( byte[][] lut )
	{
		this.lut = lut;
		this.seed = 50;
	}

	public double createRandom( double x )
	{
		double random = ( x * seed ) * goldenRatio;
		random = random - ( long ) Math.floor( random );
		return random;
	}

	public void setLut( byte[][] lut )
	{
		this.lut = lut;
	}

	public long getSeed()
	{
		return seed;
	}

	public void setSeed( long seed )
	{
		this.seed = seed;
	}


	@Override
	public void convert( RealType realType, VolatileARGBType volatileARGBType )
	{
		final double realDouble = realType.getRealDouble();

		if ( ! doubleToARGBIndex.containsKey( realDouble ) )
		{
			final double random = createRandom( realDouble );
			final int argbIndex = Luts.getARGBIndex( ( byte ) ( 255.0 * random ), lut );
			doubleToARGBIndex.put( realDouble, argbIndex );
		}

		volatileARGBType.get().set( doubleToARGBIndex.get( realDouble ) );
	}
}
