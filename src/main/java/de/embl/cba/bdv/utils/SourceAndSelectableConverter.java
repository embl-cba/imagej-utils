package de.embl.cba.bdv.utils;

import bdv.viewer.Source;
import de.embl.cba.bdv.utils.converters.argb.SelectableVolatileARGBConverter;
import net.imglib2.type.numeric.RealType;

public class SourceAndSelectableConverter
{
	protected final Source< RealType > spimSource;

	protected final SelectableVolatileARGBConverter converter;

	public SourceAndSelectableConverter( Source< RealType > spimSource, SelectableVolatileARGBConverter converter )
	{
		this.spimSource = spimSource;
		this.converter = converter;
	}

	public Source< RealType > getSpimSource()
	{
		return spimSource;
	}

	public SelectableVolatileARGBConverter getConverter()
	{
		return converter;
	}

}
