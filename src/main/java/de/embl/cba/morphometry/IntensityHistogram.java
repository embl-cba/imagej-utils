package de.embl.cba.morphometry;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

public class IntensityHistogram <T extends RealType<T> & NativeType< T > >
{
	public double[] binCenters;
	public double[] frequencies;
	final public double binWidth;
	final public int numBins;
	final RandomAccessibleInterval< T > rai;

	public IntensityHistogram( RandomAccessibleInterval< T > rai, double maxValue, double binWidth )
	{
		this.binWidth = binWidth;
		this.numBins = ( int ) ( maxValue / binWidth );
		this.rai = rai;

		initializeHistogram( numBins, binWidth );
		computeFrequencies();
	}

	public void initializeHistogram( int numBins, double binWidth )
	{
		this.binCenters = new double[ numBins ];
		this.frequencies = new double[ numBins ];

		for ( int i = 0; i < numBins; ++i )
		{
			binCenters[ i ] = i * binWidth + binWidth * 0.5;
		}
	}



	public CoordinateAndValue getMode( )
	{
		final CoordinateAndValue coordinateAndValue = new CoordinateAndValue( 0.0, 0.0);

		for ( int i = 0; i < numBins - 1; ++i ) // numBins - 1 avoids the last bin containing saturated pixels
		{
			if ( frequencies[ i ] > coordinateAndValue.value )
			{
				coordinateAndValue.value = frequencies[ i ];
				coordinateAndValue.coordinate = binCenters[ i ];
			}
		}

		return coordinateAndValue;
	}

	public CoordinateAndValue getRightHandHalfMode( )
	{
		final CoordinateAndValue mode = getMode();

		final CoordinateAndValue coordinateAndValue = new CoordinateAndValue();

		for ( int i = 0; i < numBins; ++i )
		{
			if ( binCenters[ i ] > mode.coordinate )
			{
				if ( frequencies[ i ] <= mode.value / 2.0 )
				{
					coordinateAndValue.coordinate = binCenters[ i ];
					coordinateAndValue.value = frequencies[ i ];
					return coordinateAndValue;
				}
			}
		}
		return coordinateAndValue;
	}


	private void computeFrequencies()
	{
		final Cursor< T > cursor = Views.iterable( rai ).cursor();

		while( cursor.hasNext() )
		{
			increment( cursor.next().getRealDouble() );
		}
	}

	public void increment( double value )
	{
		int bin = (int) ( value / binWidth );

		if ( bin >= numBins )
		{
			bin = numBins - 1;
		}

		frequencies[ bin ]++;
	}

}
