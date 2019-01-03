package de.embl.cba.bdv.utils.converters.argb;

import de.embl.cba.bdv.utils.lut.ARGBConverterUtils;
import de.embl.cba.bdv.utils.lut.Luts;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.volatiles.VolatileARGBType;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;

public class CategoricalMappingRandomARGBConverter implements Converter< RealType, VolatileARGBType >
{
	long seed;
	Map< Double, ? extends Object > map;

	byte[][] lut;
	private ArrayList uniqueObjectsList;

	public CategoricalMappingRandomARGBConverter( Map< Double, ? extends Object > map )
	{
		this( map, Luts.GLASBEY );
	}

	public CategoricalMappingRandomARGBConverter( Map< Double, ? extends Object > map, byte[][] lut )
	{
		this.map = map;
		this.lut = lut;
		this.seed = 50;
		setUniqueObjectsList( map );
	}

	public void setLut( byte[][] lut )
	{
		this.lut = lut;
	}

	public void setMap( Map< Double, Object > map )
	{
		this.map = map;
		setUniqueObjectsList( map );
	}

	public long getSeed()
	{
		return seed;
	}

	public void setSeed( long seed )
	{
		this.seed = seed;
	}

	private void setUniqueObjectsList( Map< Double, ? extends Object > map )
	{
		uniqueObjectsList = new ArrayList(  new LinkedHashSet<>( map.values() ) );
	}

	@Override
	public void convert( RealType realType, VolatileARGBType volatileARGBType )
	{
		final Object object = map.get( realType.getRealDouble() );
		final int index = uniqueObjectsList.indexOf( object ) + 1; // +1 not to have zero
		final double random = ARGBConverterUtils.getRandomNumberBetweenZeroAndOne( index, seed );
		final byte lutIndex = (byte) ( 255.0 * random );
		volatileARGBType.set( Luts.getARGBIndex( lutIndex, lut ) );
	}

}
