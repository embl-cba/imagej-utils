package de.embl.cba.bdv.utils.sources;

import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import net.imglib2.Volatile;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.volatiles.VolatileARGBType;

public class SourceAndVolatileARGBConverter< T >
{
	/**
	 * provides image data for all timepoints of one view.
	 */
	protected final Source< T > spimSource;

	/**
	 * converts {@link #spimSource} type T to ARGBType for display
	 */
	protected final Converter< T, VolatileARGBType > converter;
	private final VolatileARGBType argbConvertedOutOfBoundsValue;

	public SourceAndVolatileARGBConverter(
			final Source< T > spimSource,
			final Converter< T, VolatileARGBType > converter,
			VolatileARGBType outOfBoundsValue )
	{
		this.spimSource = spimSource;
		this.converter = converter;
		this.argbConvertedOutOfBoundsValue = outOfBoundsValue;
	}

	public SourceAndVolatileARGBConverter(
			final Source< T > source,
			final Converter< T, VolatileARGBType > converter )
	{
		this( source, converter, new VolatileARGBType( 0 ) );
	}

	/**
	 * Get the {@link Source} (provides image data for all timepoints of one
	 * angle).
	 */
	public Source< T > getSpimSource()
	{
		return spimSource;
	}

	/**
	 * Get the {@link Converter} (converts source type T to ARGBType for
	 * display).
	 */
	public Converter< T, VolatileARGBType > getConverter()
	{
		return converter;
	}

}
