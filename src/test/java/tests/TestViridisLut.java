package tests;

import de.embl.cba.bdv.utils.lut.ViridisARGBLut;
import org.junit.Test;

public class TestViridisLut
{
	@Test
	public static void main( String[] args )
	{
		final ViridisARGBLut lut = new ViridisARGBLut();
		final int argb = lut.getARGB( 0.5 );
	}
}
