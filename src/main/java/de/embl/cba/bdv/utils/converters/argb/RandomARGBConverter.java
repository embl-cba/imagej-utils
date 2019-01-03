package de.embl.cba.bdv.utils.converters.argb;

import de.embl.cba.bdv.utils.lut.ARGBConverterUtils;
import de.embl.cba.bdv.utils.lut.Luts;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.volatiles.VolatileARGBType;

public class RandomARGBConverter implements Converter< RealType, VolatileARGBType >
{

	long seed;
	byte[][] lut;

	public RandomARGBConverter( )
	{
		this.lut = Luts.GLASBEY;
		this.seed = 50;
	}

	public RandomARGBConverter( byte[][] lut )
	{
		this.lut = lut;
		this.seed = 50;
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


	@Override
	public void convert( RealType realType, VolatileARGBType volatileARGBType )
	{
		final double random = ARGBConverterUtils.getRandomNumberBetweenZeroAndOne( realType.getRealDouble(), seed );

		volatileARGBType.set( Luts.getARGBIndex( ( byte ) ( 255.0 * random ), lut ) );
	}
}
