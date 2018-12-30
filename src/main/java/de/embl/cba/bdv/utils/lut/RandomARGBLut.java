package de.embl.cba.bdv.utils.lut;

public class RandomARGBLut implements ARGBLut
{
	final static public double goldenRatio = 1.0 / ( 0.5 * Math.sqrt( 5 ) + 0.5 );
	long seed;
	byte[][] lut;


	public RandomARGBLut( )
	{
		this.lut = Luts.GLASBEY_LUT;
		this.seed = 50;
	}

	public RandomARGBLut( byte[][] lut )
	{
		this.lut = lut;
		this.seed = 50;
	}

	@Override
	public int getARGBIndex( double x, double brightness )
	{
		x = ( x * seed ) * goldenRatio;
		x = x - ( long ) Math.floor( x );

		final int argbColorIndex = Luts.getARGBIndex( ( byte ) ( 255.0 * x ), lut, brightness );

		return argbColorIndex;
	}

	public long getSeed()
	{
		return seed;
	}

	public void setSeed( long seed )
	{
		this.seed = seed;
	}

}
