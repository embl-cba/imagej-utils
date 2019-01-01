package de.embl.cba.bdv.utils.lut;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;

public class StringMappingRandomARGBLut implements ARGBLut
{
	long seed;
	Map< Double, String > map;

	byte[][] lut;
	private LinkedHashSet< String > uniqueStringSet;
	private ArrayList uniqueStringList;

	public StringMappingRandomARGBLut( Map< Double, String > map )
	{
		this( map, Luts.GLASBEY );
	}

	public StringMappingRandomARGBLut( Map< Double, String > map, byte[][] lut )
	{
		this.map = map;
		this.lut = lut;
		this.seed = 50;
		setUniqueStringList( map );
	}


	public void setUniqueStringList( Map< Double, String > map )
	{
		final LinkedHashSet< String > uniqueStringSet = new LinkedHashSet<>( map.values() );
		uniqueStringList = new ArrayList( uniqueStringSet );
	}

	@Override
	public int getARGBIndex( final double x, final double brightness )
	{
		final String string = map.get( x );
		final int index = uniqueStringList.indexOf( string ) + 1; // +1 not to have zero

		double random = LutUtils.getRandomNumberBetweenZeroAndOne( index, seed );

		final int argbColorIndex = Luts.getARGBIndex( ( byte ) ( 255.0 * random ), lut, brightness );

		return argbColorIndex;
	}

	public void setLut( byte[][] lut )
	{
		this.lut = lut;
	}

	public void setMap( Map< Double, String > map )
	{
		this.map = map;
		setUniqueStringList( map );
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
