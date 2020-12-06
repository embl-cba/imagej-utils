package de.embl.cba.tables.plot;

import net.imglib2.RealInterval;
import net.imglib2.RealPoint;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;

import java.util.ArrayList;
import java.util.Random;

public class RandomPointsCreator < T extends NumericType< T > >
{
	private final T type;
	private int n;
	private RealInterval interval;
	private ArrayList< RealPoint > randomPointList;
	private ArrayList< T > fixedValueList;

	public RandomPointsCreator( T type, int n, RealInterval interval )
	{
		this.type = type;
		this.n = n;
		this.interval = interval;
		createRandomPoints();
	}

	private void createRandomPoints()
	{
		Random rand = new Random( 60 );

		int nd = interval.numDimensions();
		double[] widths = new double[ nd ];
		double[] offset = new double[ nd ];
		for ( int d = 0; d < nd; d++ )
		{
			offset[ d ] = interval.realMin( d );
			widths[ d ] = interval.realMax( d ) - interval.realMin( d );
		}

		randomPointList = new ArrayList<>();
		fixedValueList = new ArrayList<>();

		for ( int i = 0; i < n; i++ )
		{
			RealPoint p = new RealPoint( nd );
			for ( int d = 0; d < nd; d++ )
				p.setPosition( offset[ d ] + rand.nextDouble() * widths[ d ], d );

			randomPointList.add( p );

			T value = type.copy();

			fixedValueList.add( value );
		}
	}

	public ArrayList< RealPoint > getPointList()
	{
		return randomPointList;
	}

	public ArrayList< T > getValueList()
	{
		return fixedValueList;
	}
}
