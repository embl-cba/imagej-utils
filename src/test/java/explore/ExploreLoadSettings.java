package explore;

import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.viewer.Source;
import de.embl.cba.bdv.utils.io.SPIMDataReaders;
import net.imglib2.type.volatiles.VolatileARGBType;

public class ExploreLoadSettings
{
	public static void main( String[] args )
	{
		final Source< VolatileARGBType > source =
				SPIMDataReaders.openAsVolatileARGBTypeSource(
						ExploreLoadSettings.class.getResource( "../test-data/Hela_full.xml" ).getFile(), 0 );

		BdvFunctions.show( source, BdvOptions.options().is2D() );
	}
}
