package de.embl.cba.bdv.utils.converters;

import bdv.util.BoundedValueDouble;
import bdv.viewer.RequestRepaint;
import de.embl.cba.bdv.utils.lut.Luts;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.volatiles.VolatileARGBType;

import java.util.Map;

public class LinearARGBConverter implements Converter< RealType, VolatileARGBType >
{

	double min, max;
	byte[][] lut;

	public LinearARGBConverter( double min, double max )
	{
		this( min, max, Luts.GRAYSCALE );
	}

	public LinearARGBConverter( double min, double max, byte[][] lut )
	{
		this.min = min;
		this.max = max;
		this.lut = lut;
	}

	@Override
	public void convert( RealType realType, VolatileARGBType volatileARGBType )
	{
		final byte lutIndex = (byte) ( 255.0 * ( realType.getRealDouble() - min ) / ( max - min ) );

		volatileARGBType.set( Luts.getARGBIndex( lutIndex, lut ) );
	}

	public void setMin( double min )
	{
		this.min = min;
	}

	public void setMax( double max )
	{
		this.max = max;
	}

	public double getMin()
	{
		return min;
	}

	public double getMax()
	{
		return max;
	}

	public void setLut( byte[][] lut )
	{
		this.lut = lut;
	}

}
