package de.embl.cba.bdv.utils.algorithms;

import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import net.imglib2.*;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.LongArray;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Pair;
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

public class ConnectedComponentExtractor< R extends RealType< R > >
{
	// input
	private final RandomAccessibleInterval< R > source;
	private final Shape shape;
	private final long maxRegionSize;

	// other
	private int n;
	private long[] min;
	private long[] max;

	public double getSeedValue()
	{
		return seedValue;
	}

	private double seedValue;
	private ArrayList< long[] > coordinates;
	private RandomAccessibleInterval< BitType > regionMask;

	// output
	private boolean maxRegionSizeReached;

	public ConnectedComponentExtractor( RandomAccessibleInterval< R > source, Shape shape, long maxRegionSize )
	{
		this.source = source;
		this.shape = shape;
		this.maxRegionSize = maxRegionSize;
		n = source.numDimensions();
	}

	public void run( long[] seed )
	{
		maxRegionSizeReached = false;

		setSeedValue( seed );

		initCoordinates( seed );

		initBoundingBox();

		floodFill();
	}

	public RandomAccessibleInterval< BitType > getCroppedRegionMask()
	{
		RandomAccessibleInterval< BitType > croppedMask = Views.interval( regionMask, new FinalInterval( min, max ) );

		return croppedMask;
	}

	public boolean isMaxRegionSizeReached()
	{
		return maxRegionSizeReached;
	}

	private void floodFill()
	{

		regionMask = new DiskCachedCellImgFactory<>( new BitType() ).create( source );
		regionMask = Views.translate( regionMask, Intervals.minAsLongArray( source ) ); // adjust offset
		final ExtendedRandomAccessibleInterval extendedRegionMask = Views.extendZero( regionMask ); // add oob strategy

		final RandomAccessible< Neighborhood< R > > neighborhood = shape.neighborhoodsRandomAccessible( Views.extendZero( source ) );
		final RandomAccess< Neighborhood< R > > neighborhoodAccess = neighborhood.randomAccess();

		final RandomAccess< BitType > extendedMaskAccess = extendedRegionMask.randomAccess();

		for ( int i = 0; i < coordinates.size(); ++i )
		{
			if ( i > maxRegionSize )
			{
				maxRegionSizeReached = true;
				break;
			}

			neighborhoodAccess.setPosition( coordinates.get( i ) );

			final Cursor< R > neighborhoodCursor = neighborhoodAccess.get().cursor();

			while ( neighborhoodCursor.hasNext() )
			{
				neighborhoodCursor.next();

				final double value = neighborhoodCursor.get().getRealDouble();

				if ( value == seedValue )
				{
					final long[] coordinate = new long[ n ];
					neighborhoodCursor.localize( coordinate );
					extendedMaskAccess.setPosition( coordinate );

					if ( ! extendedMaskAccess.get().get() )
					{
						extendedMaskAccess.get().setOne();
						coordinates.add( coordinate );
						updateBoundingBox( coordinate );
					}
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
