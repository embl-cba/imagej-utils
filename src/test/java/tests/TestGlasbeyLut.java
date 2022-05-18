/*-
 * #%L
 * Various Java code for ImageJ
 * %%
 * Copyright (C) 2018 - 2022 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
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
