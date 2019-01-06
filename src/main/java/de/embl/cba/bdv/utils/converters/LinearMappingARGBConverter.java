package de.embl.cba.bdv.utils.converters;

import de.embl.cba.bdv.utils.lut.Luts;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.volatiles.VolatileARGBType;

import java.util.function.Function;

public class LinearMappingARGBConverter implements Converter< RealType, VolatileARGBType >
{
	final Function< Double, Double > mappingFunction;

	double min, max;

	byte[][] lut;

	public LinearMappingARGBConverter( Function< Double, Double > mappingFunction, double min, double max )
	{
		this( mappingFunction, min, max, Luts.GRAYSCALE );
	}

	public LinearMappingARGBConverter( Function< Double, Double > mappingFunction, double min, double max, byte[][] lut )
	{
		this.mappingFunction = mappingFunction;
		this.min = min;
		this.max = max;
		this.lut = lut;
	}

	@Override
	public void convert( RealType realType, VolatileARGBType volatileARGBType )
	{
		final byte lutIndex = (byte) ( 255.0 * ( getMappedValue( realType ) - min ) / ( max - min ) );
		volatileARGBType.set( Luts.getARGBIndex( lutIndex, lut ) );
	}

	public double getMappedValue( RealType realType )
	{
		return mappingFunction.apply( realType.getRealDouble() );
	}

	public void setMin( double min )
	{
		this.min = min;
	}

	public void setMax( double max )
	{
		this.max = max;
	}

	public void setLut( byte[][] lut )
	{
		this.lut = lut;
	}

}
