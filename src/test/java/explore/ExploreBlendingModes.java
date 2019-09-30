package explore;

import bdv.util.*;
import bdv.viewer.Source;
import de.embl.cba.bdv.utils.io.SPIMDataReaders;
import de.embl.cba.bdv.utils.render.AccumulateEMAndFMProjectorARGB;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;
import net.imglib2.type.volatiles.VolatileARGBType;

public class ExploreBlendingModes
{
	public static void main( String[] args ) throws SpimDataException
	{
		final Source< VolatileARGBType > argbSource0 = SPIMDataReaders.openAsVolatileARGBTypeSource(
				ExploreBlendingModes.class.getResource( "../mri-stack.xml" ).getFile(), 0 );

		final BdvHandle bdvHandle = BdvFunctions.show(
				argbSource0,
				BdvOptions.options().
						accumulateProjectorFactory( AccumulateEMAndFMProjectorARGB.factory ) ).getBdvHandle();

		final Source< VolatileARGBType > argbSource1 = SPIMDataReaders.openAsVolatileARGBTypeSource(
				ExploreBlendingModes.class.getResource( "../mri-stack-shifted.xml" ).getFile(), 0 );

		BdvFunctions.show( argbSource1, BdvOptions.options().addTo( bdvHandle ) );

		final Source< VolatileARGBType > argbSource2 = SPIMDataReaders.openAsVolatileARGBTypeSource(
				ExploreBlendingModes.class.getResource( "../mri-stack-shifted1.xml" ).getFile(), 0 );

		BdvFunctions.show( argbSource2,
				BdvOptions.options().addTo(
						bdvHandle ) );
	}

}
