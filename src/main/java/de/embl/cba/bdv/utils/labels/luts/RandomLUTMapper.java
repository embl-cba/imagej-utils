package de.embl.cba.bdv.utils.labels.luts;

public class RandomLUTMapper implements LUTMapper
{
	final static public double goldenRatio = 1.0 / ( 0.5 * Math.sqrt( 5 ) + 0.5 );
	long seed = 50;

	@Override
	public int getLUTIndex( double x )
	{
		x = ( x * seed ) * goldenRatio;
		x = x - ( long ) Math.floor( x );
		return (int) ( 255.0 * x );
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
