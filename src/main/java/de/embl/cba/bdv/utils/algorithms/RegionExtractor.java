package de.embl.cba.bdv.utils.algorithms;

import net.imglib2.*;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;

import java.util.ArrayList;

public class RegionExtractor < R extends RealType< R > >
{

	final RandomAccessible< R > source;
	final Shape shape;
	private long[] min;
	private long[] max;
	private int n;
	private double seedValue;
	private ArrayList< long[] > coordinates;

	public RegionExtractor( RandomAccessible< R > source, Shape shape )
	{
		this.source = source;
		this.shape = shape;
		n = source.numDimensions();
	}

	public RandomAccessibleInterval< BitType > run( long[] seed )
	{
		setSeedValue( seed );

		initCoordinates( seed );

		initBoundingBox();

		floodFillCoordinates();

		final FinalInterval boundingInterval = new FinalInterval( min, max );

		return null;
	}

	private void floodFillCoordinates()
	{
		final RandomAccessible< Neighborhood< R > > neighborhood = shape.neighborhoodsRandomAccessible( source );
		final RandomAccess< Neighborhood< R > > neighborhoodAccess = neighborhood.randomAccess();

		for ( int i = 0; i < coordinates.size(); ++i )
		{
			neighborhoodAccess.setPosition( coordinates.get( i ) );

			final Cursor< R > neighborhoodCursor = neighborhoodAccess.get().cursor();

			while ( neighborhoodCursor.hasNext() )
			{
				final double value = neighborhoodCursor.next().getRealDouble();
				if ( value == seedValue )
				{
					final long[] coordinate = new long[ n ];
					neighborhoodCursor.localize( coordinate );
					coordinates.add( coordinate );
					updateBoundingBox( coordinate );
				}
			}
		}
	}

	private void initCoordinates( long[] seed )
	{
		coordinates = new ArrayList<>();
		coordinates.add( seed );
	}

	private void setSeedValue( long[] seed )
	{
		final RandomAccess< R > sourceAccess = source.randomAccess();
		sourceAccess.setPosition( seed );
		seedValue = sourceAccess.get().getRealDouble();
	}

	private void initBoundingBox( )
	{
		min = new long[ n ];
		max = new long[ n ];

		for ( int d = 0; d < min.length; ++d )
		{
			min[ d ] = Long.MAX_VALUE;
			max[ d ] = Long.MIN_VALUE;
		}
	}

	private void updateBoundingBox ( long[] coordinate )
	{
		for ( int d = 0; d < min.length; ++d )
		{
			if ( coordinate[ d ] < min[ d ] ) min[ d ] = coordinate[ d ];
			if ( coordinate[ d ] > max[ d ] ) max[ d ] = coordinate[ d ];
		}
	}

}
