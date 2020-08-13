package de.embl.cba.morphometry;

import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

public class ArrayImgs2
{
	final static public < T extends NativeType< T > >
	RandomAccessibleInterval< T > img(
			final long[] dim,
			final long[] offset,
			final T type
	)
	{
		return Views.translate( new ArrayImgFactory<>( type ).create( dim ), offset );
	}


	final static public < T extends NativeType< T > >
	RandomAccessibleInterval< T > img(
			final Interval interval,
			final T type
	)
	{
		return Views.translate(
				new ArrayImgFactory<>( type ).create( Intervals.dimensionsAsLongArray( interval ) )
				, Intervals.minAsLongArray( interval ) );
	}
}

