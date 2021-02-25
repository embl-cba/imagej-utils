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
package de.embl.cba.bdv.utils.io;

import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import de.embl.cba.bdv.utils.BdvUtils;
import de.embl.cba.bdv.utils.converters.LinearARGBConverter;
import de.embl.cba.bdv.utils.lut.Luts;
import de.embl.cba.bdv.utils.sources.ARGBConvertedRealSource;
import de.embl.cba.bdv.utils.sources.SourceAndVolatileARGBConverter;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.volatiles.VolatileARGBType;

public class SPIMDataReaders
{
	public static Source< VolatileARGBType > openAsVolatileARGBTypeSource( String xmlPath, int sourceIndex )
	{
		final Source< ? > source0 = BdvUtils.openSource( xmlPath, sourceIndex );

		final XmlSettingsReader xmlSettingsReader = new XmlSettingsReader();

		byte[][] lut = Luts.GRAYSCALE;
		double min = 0.0;
		double max = 65535.0;
		if ( xmlSettingsReader.tryLoadSettings( xmlPath ) )
		{
			lut = Luts.colorLut( xmlSettingsReader.getColors().get( 0 ) );
			min = xmlSettingsReader.getMinMaxGroups().get( 0 ).getMinBoundedValue().getCurrentValue();
			max = xmlSettingsReader.getMinMaxGroups().get( 0 ).getMaxBoundedValue().getCurrentValue();
		}

		return ( Source< VolatileARGBType > ) new ARGBConvertedRealSource(
				source0,
				new LinearARGBConverter( min, max, lut ),
				new VolatileARGBType( ARGBType.rgba( 0, 0, 0, 0 ) ) );
	}

	public static < R extends RealType< R > > SourceAndVolatileARGBConverter< R >
	openAsSourceAndVolatileARGBConverter( String xmlPath, int sourceIndex )
	{
		final Source< R > source = BdvUtils.openSource( xmlPath, sourceIndex );

		final XmlSettingsReader xmlSettingsReader = new XmlSettingsReader();

		byte[][] lut = Luts.GRAYSCALE;
		double min = 0.0;
		double max = 255;

		if ( xmlSettingsReader.tryLoadSettings( xmlPath ) )
		{
			lut = Luts.colorLut( xmlSettingsReader.getColors().get( 0 ) );
			min = xmlSettingsReader.getMinMaxGroups().get( 0 ).getMinBoundedValue().getCurrentValue();
			max = xmlSettingsReader.getMinMaxGroups().get( 0 ).getMaxBoundedValue().getCurrentValue();
		}

		final LinearARGBConverter< R > converter = new LinearARGBConverter< R >( min, max, lut );

		final SourceAndVolatileARGBConverter< R > sourceAndVolatileARGBConverter
				= new SourceAndVolatileARGBConverter<>( source, converter, new VolatileARGBType( ARGBType.rgba( 0, 0, 0, 0 ) ) );

		return sourceAndVolatileARGBConverter;
	}
}
