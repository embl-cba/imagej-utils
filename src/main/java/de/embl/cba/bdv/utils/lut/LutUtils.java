package de.embl.cba.bdv.utils.lut;

public class LutUtils
{
	final static public double goldenRatio = 1.0 / ( 0.5 * Math.sqrt( 5 ) + 0.5 );

	public static double getRandomNumberBetweenZeroAndOne( double x, long seed )
	{
		double random = ( x * seed ) * goldenRatio;
		random = random - ( long ) Math.floor( random );
		return random;
	}
}
