package tests;

import de.embl.cba.bdv.utils.lut.ViridisARGBLut;
import org.junit.Test;

public class TestViridisLut
{
	@Test
	public void test()
	{
		final ViridisARGBLut lut = new ViridisARGBLut();
		final int argb = lut.getARGB( 0.5 );
	}

	public static void main( String[] args )
	{
		new TestViridisLut().test();
	}
}
