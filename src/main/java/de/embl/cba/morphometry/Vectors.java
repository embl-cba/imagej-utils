package de.embl.cba.morphometry;

import net.imglib2.RealPoint;

public abstract class Vectors
{
	public static double[] asDoubles( RealPoint point )
	{
		final double[] vector = new double[ point.numDimensions() ];
		for ( int d = 0; d < point.numDimensions() ; d++ )
		{
			vector[ d ] = point.getDoublePosition( d );
		}
		return vector;
	}
}
