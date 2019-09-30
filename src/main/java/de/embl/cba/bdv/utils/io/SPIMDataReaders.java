package de.embl.cba.bdv.utils.io;

import bdv.viewer.Source;
import de.embl.cba.bdv.utils.BdvUtils;
import de.embl.cba.bdv.utils.converters.LinearARGBConverter;
import de.embl.cba.bdv.utils.lut.Luts;
import de.embl.cba.bdv.utils.sources.ARGBConvertedRealSource;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.volatiles.VolatileARGBType;

public class SPIMDataReaders
{

	public static Source< VolatileARGBType > openAsVolatileARGBTypeSource( String path, int sourceIndex )
	{
		final Source< ? > source0 = BdvUtils.openSource(
				path, sourceIndex );

		return ( Source< VolatileARGBType > ) new ARGBConvertedRealSource(
				source0,
				new LinearARGBConverter( 0.0, 255.0, Luts.GRAYSCALE ),
				new VolatileARGBType( ARGBType.rgba( 0, 0, 0, 0 ) ) );
	}
}
