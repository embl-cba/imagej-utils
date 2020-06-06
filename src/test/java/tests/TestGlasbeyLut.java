package tests;

import de.embl.cba.bdv.utils.lut.GlasbeyARGBLut;
import de.embl.cba.tables.color.ColorUtils;
import net.imglib2.type.numeric.ARGBType;

import java.awt.*;

public class TestGlasbeyLut
{
	public static void main( String[] args )
	{
		final GlasbeyARGBLut lut = new GlasbeyARGBLut();

		final Color color = ColorUtils.getColor( new ARGBType( lut.getARGB( 0 ) ) );
		final Color color1 = ColorUtils.getColor( new ARGBType( lut.getARGB( 1 ) ) );
		final Color color2 = ColorUtils.getColor( new ARGBType( lut.getARGB( 2 ) ) );
		final Color color3 = ColorUtils.getColor( new ARGBType( lut.getARGB( 3 ) ) );
		final Color color4 = ColorUtils.getColor( new ARGBType( lut.getARGB( 4 ) ) );
		final Color color5 = ColorUtils.getColor( new ARGBType( lut.getARGB( 5 ) ) );
		final Color color6 = ColorUtils.getColor( new ARGBType( lut.getARGB( 6 ) ) );
		final Color color7 = ColorUtils.getColor( new ARGBType( lut.getARGB( 7 ) ) );
		final Color color8 = ColorUtils.getColor( new ARGBType( lut.getARGB( 8 ) ) );
		final Color color9 = ColorUtils.getColor( new ARGBType( lut.getARGB( 9 ) ) );
		final Color color10 = ColorUtils.getColor( new ARGBType( lut.getARGB( 10 ) ) );
		final Color color11 = ColorUtils.getColor( new ARGBType( lut.getARGB( 11 ) ) );
		final Color color12 = ColorUtils.getColor( new ARGBType( lut.getARGB( 12 ) ) );
		final Color color13 = ColorUtils.getColor( new ARGBType( lut.getARGB( 13 ) ) );
		final Color color14 = ColorUtils.getColor( new ARGBType( lut.getARGB( 14 ) ) );

	}
}
