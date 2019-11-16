package de.embl.cba.bdv.utils.io;

import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import de.embl.cba.bdv.utils.BdvUtils;
import de.embl.cba.bdv.utils.converters.LinearARGBConverter;
import de.embl.cba.bdv.utils.lut.Luts;
import de.embl.cba.bdv.utils.sources.ARGBConvertedRealSource;
import de.embl.cba.bdv.utils.sources.SourceAndVolatileARGBConverter;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.volatiles.VolatileARGBType;

public class SPIMDataReaders
{
	public static Source< VolatileARGBType > openAsVolatileARGBTypeSource( String xmlPath, int sourceIndex )
	{
		final Source< ? > source0 = BdvUtils.openSource( xmlPath, sourceIndex );

		final XmlSettingsReader xmlSettingsReader = new XmlSettingsReader();

		byte[][] lut = Luts.GRAYSCALE;
		double min = 0.0;
		double max = 65535.0;
		if ( xmlSettingsReader.tryLoadSettings( xmlPath ) )
		{
			lut = Luts.colorLut( xmlSettingsReader.getColors().get( 0 ) );
			min = xmlSettingsReader.getMinMaxGroups().get( 0 ).getMinBoundedValue().getCurrentValue();
			max = xmlSettingsReader.getMinMaxGroups().get( 0 ).getMaxBoundedValue().getCurrentValue();
		}

		return ( Source< VolatileARGBType > ) new ARGBConvertedRealSource(
				source0,
				new LinearARGBConverter( min, max, lut ),
				new VolatileARGBType( ARGBType.rgba( 0, 0, 0, 0 ) ) );
	}

	public static < R extends RealType< R > > SourceAndVolatileARGBConverter< R >
	openAsSourceAndVolatileARGBConverter( String xmlPath, int sourceIndex )
	{
		final Source< R > source = BdvUtils.openSource( xmlPath, sourceIndex );

		final XmlSettingsReader xmlSettingsReader = new XmlSettingsReader();

		byte[][] lut = Luts.GRAYSCALE;
		double min = 0.0;
		double max = 255;

		if ( xmlSettingsReader.tryLoadSettings( xmlPath ) )
		{
			lut = Luts.colorLut( xmlSettingsReader.getColors().get( 0 ) );
			min = xmlSettingsReader.getMinMaxGroups().get( 0 ).getMinBoundedValue().getCurrentValue();
			max = xmlSettingsReader.getMinMaxGroups().get( 0 ).getMaxBoundedValue().getCurrentValue();
		}

		final LinearARGBConverter< R > converter = new LinearARGBConverter< R >( min, max, lut );

		final SourceAndVolatileARGBConverter< R > sourceAndVolatileARGBConverter
				= new SourceAndVolatileARGBConverter<>( source, converter, new VolatileARGBType( ARGBType.rgba( 0, 0, 0, 0 ) ) );

		return sourceAndVolatileARGBConverter;
	}
}
