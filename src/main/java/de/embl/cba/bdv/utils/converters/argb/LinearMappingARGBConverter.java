package de.embl.cba.bdv.utils.converters.argb;

import de.embl.cba.bdv.utils.lut.Luts;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.volatiles.VolatileARGBType;

import java.util.Map;

public class LinearMappingARGBConverter implements Converter< RealType, VolatileARGBType >
{
	Map< Double, Number > map;
	double min, max;

	byte[][] lut;

	public LinearMappingARGBConverter( Map< Double, Number > map, double min, double max )
	{
		this.map = map;
		this.min = min;
		this.max = max;

		this.lut = Luts.GRAYSCALE;
	}

	public LinearMappingARGBConverter( Map< Double, Number > map, byte[][] lut, double min, double max )
	{
		this.map = map;
		this.min = min;
		this.max = max;
		this.lut = lut;
	}

	public void setLut( byte[][] lut )
	{
		this.lut = lut;
	}

	public void setMap( Map< Double, Number > map )
	{
		this.map = map;
	}

	@Override
	public void convert( RealType realType, VolatileARGBType volatileARGBType )
	{
		final byte lutIndex = (byte) ( 255.0 * ( map.get( realType.getRealDouble() ).doubleValue() - min ) / ( max - min ) );

		volatileARGBType.set( Luts.getARGBIndex( lutIndex, lut ) );
	}
}
