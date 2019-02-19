package de.embl.cba.bdv.utils.wrap;

import bdv.tools.transformation.TransformedSource;
import bdv.util.RandomAccessibleIntervalSource4D;
import ij.IJ;
import ij.ImagePlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import java.util.ArrayList;

public class Wraps
{

	public static < R extends RealType< R > & NativeType< R > >
	ArrayList< RandomAccessibleIntervalSource4D< R > > imagePlusAsSource4DChannelList( ImagePlus imagePlus )
	{
		RandomAccessibleInterval< R > wrap = wrapXYCZT( imagePlus );

		final ArrayList< RandomAccessibleIntervalSource4D< R > > sources = new ArrayList<>();

		for ( int c = 0; c < imagePlus.getNChannels(); c++ )
		{
			RandomAccessibleInterval< R > channel = Views.hyperSlice( wrap, 2, c );

			final RandomAccessibleIntervalSource4D source4D = new RandomAccessibleIntervalSource4D(
					channel,
					Util.getTypeFromInterval( channel ),
					imagePlus.getTitle() + "-C" + c );

			sources.add( source4D );
		}

		return sources;

	}

	public static < R extends RealType< R > & NativeType< R > >
	RandomAccessibleInterval< R > wrapXYCZT( ImagePlus imagePlus )
	{
		RandomAccessibleInterval< R > wrap = ImageJFunctions.wrapReal( imagePlus );

		if ( imagePlus.getNFrames() == 1 )
		{
			wrap = Views.addDimension( wrap, 0, 0 );
		}

		if ( imagePlus.getNSlices() == 1 )
		{
			wrap = Views.addDimension( wrap, 0, 0 );
			wrap = Views.permute( wrap, wrap.numDimensions() - 1, wrap.numDimensions() - 2 );
		}

		if ( imagePlus.getNChannels() == 1 )
		{
			wrap = Views.addDimension( wrap, 0, 0 );
			wrap = Views.permute( wrap, wrap.numDimensions() - 1, wrap.numDimensions() - 2 );
			wrap = Views.permute( wrap, wrap.numDimensions() - 2, wrap.numDimensions() - 3 );
		}
		return wrap;
	}

	public static < R extends RealType< R > & NativeType< R > >
	RandomAccessibleInterval< R > wrapXYZCT( ImagePlus imagePlus )
	{
		RandomAccessibleInterval< R > wrap = ImageJFunctions.wrapReal( imagePlus );

		if ( imagePlus.getNFrames() == 1 )
		{
			wrap = Views.addDimension( wrap, 0, 0 );
		}

		if ( imagePlus.getNChannels() == 1 )
		{
			wrap = Views.addDimension( wrap, 0, 0 );
			wrap = Views.permute( wrap, wrap.numDimensions() - 1, wrap.numDimensions() - 2 );
		}

		if ( imagePlus.getNSlices() == 1 )
		{
			wrap = Views.addDimension( wrap, 0, 0 );
			wrap = Views.permute( wrap, wrap.numDimensions() - 1, wrap.numDimensions() - 2 );
			wrap = Views.permute( wrap, wrap.numDimensions() - 2, wrap.numDimensions() - 3 );
		}

		return wrap;
	}
}
