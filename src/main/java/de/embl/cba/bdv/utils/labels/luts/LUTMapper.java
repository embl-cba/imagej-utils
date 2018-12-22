package de.embl.cba.bdv.utils.labels.luts;

public interface LUTMapper
{
	/**
	 *
	 * @param x
	 * @return value between 0 and 255 for 8 bit LUTs
	 */
	int getLUTIndex( double x );
}
