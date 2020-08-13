package de.embl.cba.morphometry;

import de.embl.cba.transforms.utils.Transforms;
import net.imglib2.RealPoint;

import static de.embl.cba.morphometry.Constants.X;
import static de.embl.cba.morphometry.Constants.Y;
import static java.lang.Math.atan;
import static java.lang.Math.toDegrees;

public abstract class Angles
{
	public static double angle2DToCoordinateSystemsAxisInDegrees( RealPoint point )
	{
		final double[] vector = Vectors.asDoubles( point );

		return angle2DToCoordinateSystemsAxisInDegrees( vector );
	}

	public static double angle2DToCoordinateSystemsAxisInDegrees( double[] vector )
	{

		double angleToZAxisInDegrees;

		if ( vector[ Y ] == 0 )
		{
			angleToZAxisInDegrees = Math.signum( vector[ X ] ) * 90;
		}
		else
		{
			angleToZAxisInDegrees = toDegrees( atan( vector[ X ] / vector[ Y ] ) );

			if ( vector[ Y ] < 0 )
			{
				angleToZAxisInDegrees += 180;
			}
		}

		return angleToZAxisInDegrees;
	}


	public static double angleOfSpindleAxisToXAxisInRadians( final double[] vector )
	{
		double[] xAxis = new double[]{ 1, 0, 0};

		double angleInRadians = Transforms.getAngle( vector, xAxis );

		return angleInRadians;
	}

}
