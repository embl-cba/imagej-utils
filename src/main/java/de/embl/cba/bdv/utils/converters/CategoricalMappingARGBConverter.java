package de.embl.cba.bdv.utils.converters;

import de.embl.cba.bdv.utils.lut.Luts;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.volatiles.VolatileARGBType;

import java.util.ArrayList;
import java.util.function.Function;

public class CategoricalMappingARGBConverter implements Converter< RealType, VolatileARGBType >
{
	long seed;
	final Function< Double, ? extends Object > labelToObjectFn;

	byte[][] lut;
	final private ArrayList< Object > uniqueObjectsList;

	public CategoricalMappingARGBConverter( Function< Double, ? extends Object > labelToObjectFn )
	{
		this( labelToObjectFn, Luts.GLASBEY );
	}

	public CategoricalMappingARGBConverter( Function< Double, ? extends Object > labelToObjectFn, byte[][] lut )
	{
		this.labelToObjectFn = labelToObjectFn;
		this.lut = lut;
		this.seed = 50;
		this.uniqueObjectsList = new ArrayList<>(  );
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
		Object object = labelToObjectFn.apply( realType.getRealDouble() );

		if ( object == null )
		{
			volatileARGBType.set( 0 );
			return;
		}

		if( ! uniqueObjectsList.contains( object ) )
		{
			uniqueObjectsList.add( object );
		}

		final double numberBetweenZeroAndOneExcludingZero = ( uniqueObjectsList.indexOf( object ) + 1.0 ) / uniqueObjectsList.size();
		final byte lutIndex = (byte) ( 255.0 * numberBetweenZeroAndOneExcludingZero );
		volatileARGBType.set( Luts.getARGBIndex( lutIndex, lut ) );
	}

}
