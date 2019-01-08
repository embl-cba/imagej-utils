package de.embl.cba.bdv.utils.converters;

import de.embl.cba.bdv.utils.lut.Luts;
import ij.IJ;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.volatiles.VolatileARGBType;

import java.util.function.Function;

public class LinearMappingARGBConverter extends LinearARGBConverter
{
	final Function< Double, Double > mappingFunction;

	public LinearMappingARGBConverter( double min, double max, Function< Double, Double > mappingFunction )
	{
		this( min, max, Luts.GRAYSCALE, mappingFunction );
	}

	public LinearMappingARGBConverter(
			double min,
			double max,
			byte[][] lut,
			Function< Double, Double > mappingFunction )
	{
		super( min, max, lut );
		this.mappingFunction = mappingFunction;
	}

	@Override
	public void convert( RealType realType, VolatileARGBType volatileARGBType )
	{
		final Double mappedValue = mappingFunction.apply( realType.getRealDouble() );

		if ( mappedValue == null )
		{
			volatileARGBType.set( 0 );
			return;
		}
		
		final byte lutIndex = (byte) ( 255.0 * Math.max( Math.min( ( mappedValue - min ) / ( max - min ), 1.0 ), 0.0 ) );

		volatileARGBType.set( Luts.getARGBIndex( lutIndex, lut ) );
	}

}
