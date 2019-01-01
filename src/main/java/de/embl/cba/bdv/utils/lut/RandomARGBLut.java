package de.embl.cba.bdv.utils.lut;

public class RandomARGBLut implements ARGBLut
{
	long seed;
	byte[][] lut;

	public RandomARGBLut( )
	{
		this.lut = Luts.GLASBEY;
		this.seed = 50;
	}

	public RandomARGBLut( byte[][] lut )
	{
		this.lut = lut;
		this.seed = 50;
	}

	@Override
	public int getARGBIndex( final double x, final double brightness )
	{
		final double random = LutUtils.getRandomNumberBetweenZeroAndOne( x, seed );

		final int argbColorIndex = Luts.getARGBIndex( ( byte ) ( 255.0 * random ), lut, brightness );

		return argbColorIndex;
	}

	public void setLut( byte[][] lut )
	{
		this.lut = lut;
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
