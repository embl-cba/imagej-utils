package tests;

import de.embl.cba.bdv.utils.lut.GlasbeyARGBLut;

public class TestGlasbeyLut
{
	public static void main( String[] args )
	{
		final GlasbeyARGBLut lut = new GlasbeyARGBLut();

		final int argb = lut.getARGB( 0.0 );
		final int argb1 = lut.getARGB( 1.0 );
		final int argb2 = lut.getARGB( 0.5 );
	}
}
