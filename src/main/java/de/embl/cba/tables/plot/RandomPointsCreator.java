package de.embl.cba.tables.plot;

import net.imglib2.RealInterval;
import net.imglib2.RealPoint;
import net.imglib2.type.numeric.NumericType;

import java.util.ArrayList;
import java.util.Random;

public class RandomPointsCreator < T extends NumericType< T > >
{
	private final T fixedValue;
	private int n;
	private RealInterval interval;
	private ArrayList< RealPoint > randomPointList;
	private ArrayList< T > fixedValueList;

	public RandomPointsCreator( T fixedValue, int n, RealInterval interval )
	{
		this.fixedValue = fixedValue;
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
			fixedValueList.add( fixedValue.copy() );
		}
	}

	public ArrayList< RealPoint > getPointList()
	{
		return randomPointList;
	}

	public ArrayList< T > getFixedValueList()
	{
		return fixedValueList;
	}
}
