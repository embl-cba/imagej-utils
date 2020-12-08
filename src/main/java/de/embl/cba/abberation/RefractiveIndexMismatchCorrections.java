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
	public class Settings
	{
		public double pixelCalibrationMicrometer;
		public double intensityDecayLengthMicrometer;
		public double coverslipPositionMicrometer;
		public double intensityOffset;
	}

	public static double getIntensityCorrectionFactor( long z, Settings settings )
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
	void correctIntensity( RandomAccessibleInterval< T > rai, Settings settings )
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
	RandomAccessibleInterval< T > createIntensityCorrectedImages( RandomAccessibleInterval< T > images, Settings settings )
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
			Settings settings )
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
