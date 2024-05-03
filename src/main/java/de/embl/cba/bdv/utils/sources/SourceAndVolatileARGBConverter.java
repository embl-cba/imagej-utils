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
package de.embl.cba.bdv.utils.sources;

import bdv.viewer.Source;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.volatiles.VolatileARGBType;

public class SourceAndVolatileARGBConverter< R extends RealType< R > >
{
	/**
	 * provides image data for all timepoints of one view.
	 */
	protected final Source< R > spimSource;

	/**
	 * converts {@link #spimSource} type T to VolatileARGBType for display
	 */
	protected final Converter< R, VolatileARGBType > converter;
	private final VolatileARGBType argbConvertedOutOfBoundsValue;

	public SourceAndVolatileARGBConverter(
			final Source< R > spimSource,
			final Converter< R, VolatileARGBType > converter,
			VolatileARGBType outOfBoundsValue )
	{
		this.spimSource = spimSource;
		this.converter = converter;
		this.argbConvertedOutOfBoundsValue = outOfBoundsValue;
	}

	public SourceAndVolatileARGBConverter(
			final Source< R > source,
			final Converter< R, VolatileARGBType > converter )
	{
		this( source, converter, new VolatileARGBType( 0 ) );
	}

	/**
	 * Get the {@link Source} (provides image data for all timepoints of one
	 * angle).
	 */
	public Source< R > getSpimSource()
	{
		return spimSource;
	}

	/**
	 * Get the {@link Converter} (converts source type T to ARGBType for
	 * display).
	 */
	public Converter< R, VolatileARGBType > getConverter()
	{
		return converter;
	}

}
