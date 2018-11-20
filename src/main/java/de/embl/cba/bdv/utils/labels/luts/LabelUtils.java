package de.embl.cba.bdv.utils.labels.luts;

import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.volatiles.VolatileARGBType;

import java.util.Map;

public abstract class LabelUtils
{
	public final static int alpha = 0x20000000;
	final static public double goldenRatio = 1.0 / ( 0.5 * Math.sqrt( 5 ) + 0.5 );
	final static public double[] rs = new double[]{ 1, 1, 0, 0, 0, 1, 1 };
	final static public double[] gs = new double[]{ 0, 1, 1, 1, 0, 0, 0 };
	final static public double[] bs = new double[]{ 0, 0, 0, 1, 1, 1, 0 };

	public static int getSelectionColor()
	{
		final int aInt = 255;
		final int rInt = 0;
		final int gInt = 255;
		final int bInt = 0;
		return ( ( ( ( ( aInt << 8 ) | rInt ) << 8 ) | gInt ) << 8 ) | bInt;
	}

	public static int interpolate( final double[] xs, final int k, final int l, final double u, final double v ){
        return ( int )( ( v * xs[ k ] + u * xs[ l ] ) * 255.0 + 0.5 );
    }

	public static int calculateARGB( final int r, final int g, final int b, final int alpha ) {
        return ( ( ( r << 8 ) | g ) << 8 ) | b | alpha;
    }

	public static void setOutput( VolatileARGBType output,
								  double x,
								  long lx,
								  boolean isLabelSelected,
								  long selectedLabel,
								  Map< Long, Integer > lut,
								  long seed )
	{
		if ( lx != 0 )
		{
			if ( isLabelSelected )
			{
				if ( lx == selectedLabel )
				{
					output.set( getSelectionColor() );
					output.setValid( true );
				}
				else
				{
					output.set( 0 );
					output.setValid( true );
				}
			}
			else if ( lut.containsKey( lx ) )
			{
				output.setValid( true );
				output.set( lut.get( lx ) );
			}
			else
			{
				x = ( x * seed ) * goldenRatio;
				x = x - ( long ) Math.floor( x );
				x *= 6.0;
				final int k = ( int ) x;
				final int l = k + 1;
				final double u = x - k;
				final double v = 1.0 - u;
				final int red = interpolate( rs, k, l, u, v );
				final int green = interpolate( gs, k, l, u, v );
				final int blue = interpolate( bs, k, l, u, v );
				int argb = calculateARGB( red, green, blue, alpha );
				final double alpha = ARGBType.alpha( argb );
				final int aInt = Math.min( 255, ( int ) ( alpha ) );
				final int rInt = Math.min( 255, ARGBType.red( argb ) );
				final int gInt = Math.min( 255, ARGBType.green( argb ) );
				final int bInt = Math.min( 255, ARGBType.blue( argb ) );
				final int color = ( ( ( ( ( aInt << 8 ) | rInt ) << 8 ) | gInt ) << 8 ) | bInt;

				output.set( color );
				output.setValid( true );

				lut.put( lx, color );
			}
		}
		else
		{
			output.set( 0 );
		}
	}
}
