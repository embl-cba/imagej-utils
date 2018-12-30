package de.embl.cba.bdv.utils.lut;

public interface ARGBLut
{
	/**
	 *
	 * @param x value to be mapped
	 * @param brightness Value between zero and one to specifiy the brightness of the color
	 * @return ARGB color index
	 */
	int getARGBIndex( double x, double brightness );
}
