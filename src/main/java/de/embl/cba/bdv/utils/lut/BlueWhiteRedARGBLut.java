package de.embl.cba.bdv.utils.lut;

import net.imglib2.type.numeric.ARGBType;

public class BlueWhiteRedARGBLut implements ARGBLut
{
	private final int[] indices;
	private final int numColors;

	public BlueWhiteRedARGBLut( int numColors )
	{
		this.numColors = numColors;
		indices = this.blueWhiteRedARGBIndices( numColors );
	}

	@Override
	public int getARGB( double x )
	{
		return indices[ (int) ( x * (numColors - 1) ) ];
	}


	/**
	 * Lookup table going from blue to white to red.
	 *
	 *
	 * @param
	 * 		numColors
	 * @return
	 * 		ARGB indices, encoding the colors
	 */
	private final static int[] blueWhiteRedARGBIndices( int numColors )
	{

		int[][] lut = new int[ 3 ][ numColors ];

		int[] blue = new int[]{ 0, 0, 255 };
		int[] white = new int[]{ 255, 255, 255 };
		int[] red = new int[]{ 255, 0, 0 };

		final int middle = numColors / 2;

		for ( int i = 0; i < middle; i++)
		{
			for ( int rgb = 0; rgb < 3; rgb++ )
			{
				lut[ rgb ][ i ] = (int) ( blue[ rgb ] + ( 1.0 * i / middle ) * ( white[ rgb ] - blue[ rgb ] ) );
			}
		}

		for ( int i = middle; i < numColors; i++)
		{
			for ( int rgb = 0; rgb < 3; rgb++ )
			{
				lut[ rgb ][ i ] = ( int ) ( white[ rgb ] + ( 1.0 * ( i - middle ) / middle ) * ( red[ rgb ] - white[ rgb ] ) );
			}
		}

		int[] argbIndicies = new int[ numColors ];

		for (int i = 0; i < numColors; i++)
		{
			argbIndicies[ i ] = ARGBType.rgba(
					lut[ 0 ][ i ],
					lut[ 1 ][ i ],
					lut[ 2 ][ i ],
					255 );
		}

		return argbIndicies;
	}

}
