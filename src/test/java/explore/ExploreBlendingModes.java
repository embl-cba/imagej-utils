package explore;

import bdv.util.*;
import bdv.viewer.Source;
import de.embl.cba.bdv.utils.io.SPIMDataReaders;
import de.embl.cba.bdv.utils.render.AccumulateEMAndFMProjectorARGB;
import de.embl.cba.bdv.utils.sources.Metadata;
import de.embl.cba.bdv.utils.sources.Sources;
import mpicbg.spim.data.SpimDataException;
import net.imglib2.type.volatiles.VolatileARGBType;

public class ExploreBlendingModes
{
	public static void main( String[] args ) throws SpimDataException
	{
		/**
		 * EM Source
		 */

		final Source< VolatileARGBType > argbSource0 = SPIMDataReaders.openAsVolatileARGBTypeSource(
				ExploreBlendingModes.class.getResource( "../mri-stack.xml" ).getFile(), 0 );

		final BdvHandle bdvHandle = showSource( null, argbSource0, Metadata.Modality.EM );

		/**
		 * EM Source
		 */

		final Source< VolatileARGBType > argbSource1 = SPIMDataReaders.openAsVolatileARGBTypeSource(
				ExploreBlendingModes.class.getResource( "../mri-stack-shifted.xml" ).getFile(), 0 );

		showSource( bdvHandle, argbSource1, Metadata.Modality.EM );

		/**
		 * FM Source
		 */

		final Source< VolatileARGBType > argbSource2 = SPIMDataReaders.openAsVolatileARGBTypeSource(
				ExploreBlendingModes.class.getResource( "../mri-stack-shifted1.xml" ).getFile(), 0 );

		showSource( bdvHandle, argbSource2, Metadata.Modality.FM );
	}

	public static BdvHandle showSource(
			BdvHandle bdvHandle,
			Source< VolatileARGBType > source,
			Metadata.Modality em )
	{
		final Metadata metadata = new Metadata( source.getName() );
		metadata.modality = em;
		Sources.sourceToMetadata.put( source, metadata );

		return BdvFunctions.show( source, BdvOptions.options().addTo( bdvHandle ).accumulateProjectorFactory(  AccumulateEMAndFMProjectorARGB.factory ) ).getBdvHandle();
	}

}
