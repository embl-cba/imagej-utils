/*-
 * #%L
 * Various Java code for ImageJ
 * %%
 * Copyright (C) 2018 - 2024 EMBL
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
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.volatiles.VolatileARGBType;

public class LinearARGBConverter< R extends RealType< R >> implements Converter< R, VolatileARGBType >
{
	double min, max;
	byte[][] lut;
	private double scale;

	public LinearARGBConverter( double min, double max )
	{
		this( min, max, Luts.GRAYSCALE );
	}

	public LinearARGBConverter( double min, double max, byte[][] lut )
	{
		this.min = min;
		this.max = max;
		this.lut = lut;
	}

	@Override
	public void convert( R realType, VolatileARGBType volatileARGBType )
	{
		final byte lutIndex = computeLutIndex( realType.getRealDouble() );
		volatileARGBType.set( Luts.getARGBIndex( lutIndex, lut ) );
	}

	public void setMin( double min )
	{
		this.min = min;
	}

	public void setMax( double max )
	{
		this.max = max;
	}

	public double getMin()
	{
		return min;
	}

	public double getMax()
	{
		return max;
	}

	public void setLut( byte[][] lut )
	{
		this.lut = lut;
	}

	public byte computeLutIndex( final Double value )
	{
		return (byte) ( 255.0 * Math.max( Math.min( ( value - min ) / ( max - min ), 1.0 ), 0.0 ) );
	}
}
