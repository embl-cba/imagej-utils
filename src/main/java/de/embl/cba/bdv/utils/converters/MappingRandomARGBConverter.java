package de.embl.cba.bdv.utils.converters;

import de.embl.cba.bdv.utils.lut.ARGBConverterUtils;
import de.embl.cba.bdv.utils.lut.Luts;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.volatiles.VolatileARGBType;

import java.util.ArrayList;
import java.util.function.Function;

public class MappingRandomARGBConverter extends RandomARGBConverter
{
	final Function< Double, ? extends Object > labelToObjectFn;
	final private ArrayList< Object > uniqueObjectsList;

	public MappingRandomARGBConverter( Function< Double, ? extends Object > labelToObjectFn )
	{
		this( labelToObjectFn, Luts.GLASBEY );
	}

	public MappingRandomARGBConverter( Function< Double, ? extends Object > labelToObjectFn, byte[][] lut )
	{
		super( lut );
		this.labelToObjectFn = labelToObjectFn;
		this.uniqueObjectsList = new ArrayList<>(  );
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

//		final double numberBetweenZeroAndOneExcludingZero = ( uniqueObjectsList.indexOf( object ) + 1.0 ) / uniqueObjectsList.size();

		final double random = ARGBConverterUtils.getRandomNumberBetweenZeroAndOne( uniqueObjectsList.indexOf( object ), seed );
		final byte lutIndex = (byte) ( 255.0 * random );
		volatileARGBType.set( Luts.getARGBIndex( lutIndex, lut ) );
	}

}
