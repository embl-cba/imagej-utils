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
package de.embl.cba.abberation;

import de.embl.cba.morphometry.IntensityHistogram;
import de.embl.cba.morphometry.Utils;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import java.util.ArrayList;

import static de.embl.cba.morphometry.Constants.Z;
import static java.lang.Math.exp;

public abstract class RefractiveIndexMismatchCorrections
{

	public static double getIntensityCorrectionFactor( long z, RefractiveIndexMismatchCorrectionSettings settings )
	{
		/*
		f( 10 ) = 93; f( 83 ) = 30;
		f( z1 ) = v1; f( z2 ) = v2;
		f( z ) = A * exp( -z / d );

		=> d = ( z2 - z1 ) / ln( v1 / v2 );

		> log ( 93 / 30 )
		[1] 1.1314

		=> d = 	73 / 1.1314 = 64.52172;

		=> correction = 1 / exp( -z / d );

		at z = 10 we want value = 93 => z0  = 10;

		=> correction = 1 / exp( - ( z - 10 ) / d );
		 */

		double generalIntensityScaling = 1.0; // TODO: what to use here?

		double zInMicrometer = z * settings.pixelCalibrationMicrometer - settings.coverslipPositionMicrometer;

		double correctionFactor = generalIntensityScaling / exp( - zInMicrometer / settings.intensityDecayLengthMicrometer );

		return correctionFactor;
	}

	public static < T extends RealType< T > & NativeType< T > >
	void correctIntensity( RandomAccessibleInterval< T > rai, RefractiveIndexMismatchCorrectionSettings settings )
	{
		for ( long z = rai.min( Z ); z <= rai.max( Z ); ++z )
		{
			RandomAccessibleInterval< T > slice = Views.hyperSlice( rai, Z, z );

			double intensityCorrectionFactor = getIntensityCorrectionFactor( z, settings );

			Views.iterable( slice ).forEach( t ->
					{

						if ( ( t.getRealDouble() - settings.intensityOffset ) < 0 )
						{
							t.setReal( 0 );
						}
						else
						{
							t.setReal( t.getRealDouble() - settings.intensityOffset );
							t.mul( intensityCorrectionFactor );
						}
					}
			);

		}

	}

	public static <T extends RealType<T> & NativeType< T > >
	RandomAccessibleInterval< T > createIntensityCorrectedImages( RandomAccessibleInterval< T > images, RefractiveIndexMismatchCorrectionSettings settings )
	{
		ArrayList< RandomAccessibleInterval< T > > correctedImages = new ArrayList<>(  );

		long numChannels = images.dimension( 3 );

		for ( long c = 0; c < numChannels; ++c )
		{
			final RandomAccessibleInterval< T > image = Views.hyperSlice( images, 3, c );
			final RandomAccessibleInterval< T > intensityCorrectedChannel = createIntensityCorrectedChannel( image, settings );
			correctedImages.add( intensityCorrectedChannel );
		}

		return Views.stack( correctedImages );
	}


	public static < T extends RealType< T > & NativeType< T > > 
	RandomAccessibleInterval< T > createIntensityCorrectedChannel( RandomAccessibleInterval< T > image,
			RefractiveIndexMismatchCorrectionSettings settings )
	{
		settings.intensityOffset = getIntensityOffset( image );
		final RandomAccessibleInterval< T > intensityCorrectedChannel = Utils.copyAsArrayImg( image );
		correctIntensity( intensityCorrectedChannel, settings );
		return intensityCorrectedChannel;
	}

	public static < T extends RealType< T > & NativeType< T > > double getIntensityOffset( RandomAccessibleInterval< T > channel )
	{
		final IntensityHistogram intensityHistogram = new IntensityHistogram( channel, 65535, 5 );
		return intensityHistogram.getMode().coordinate;
	}

	public static double[] getAxiallyCorrectedCalibration( double[] calibration, double axialCorrectionFactor )
	{
		double[] corrected = new double[ calibration.length ];

		for ( int d = 0; d < calibration.length; ++d )
		{
			corrected[ d ] = calibration[ d ];
		}

		corrected[ Z ] *= axialCorrectionFactor;

		return corrected;
	}
}
