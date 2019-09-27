package explore;

import bdv.util.*;
import de.embl.cba.bdv.utils.BdvUtils;
import de.embl.cba.bdv.utils.render.AccumulateEMAndFMProjectorARGB;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;

public class ExploreBlendingModes
{
	public static void main( String[] args ) throws SpimDataException
	{
		SpimData spimData = new XmlIoSpimData().load(
				ExploreBlendingModes.class.getResource( "../mri-stack.xml" ).getFile() );

		final BdvStackSource< ? > bdvStackSource = BdvFunctions.show( spimData,
				BdvOptions.options().accumulateProjectorFactory( AccumulateEMAndFMProjectorARGB.factory ) ).get( 0 );

		final BdvHandle bdvHandle = bdvStackSource.getBdvHandle();
		AccumulateEMAndFMProjectorARGB.bdvHandle = bdvHandle;

		bdvStackSource.setDisplayRange( 0, 255 );
		AccumulateEMAndFMProjectorARGB.setAccumulationModality( BdvUtils.getSource( bdvStackSource, 0 ).getName(), AccumulateEMAndFMProjectorARGB.AVG );

		SpimData spimData1 = new XmlIoSpimData().load( ExploreBlendingModes.class.getResource( "../mri-stack-shifted.xml" ).getFile() );
		final BdvStackSource< ? > bdvStackSource1 = BdvFunctions.show( spimData1,
				BdvOptions.options().addTo( bdvHandle ) ).get( 0 );

		bdvStackSource1.setDisplayRange( 0, 255 );
		AccumulateEMAndFMProjectorARGB.setAccumulationModality( BdvUtils.getSource( bdvStackSource1, 0 ).getName(), AccumulateEMAndFMProjectorARGB.AVG );

		SpimData spimData2 = new XmlIoSpimData().load( ExploreBlendingModes.class.getResource( "../mri-stack-shifted1.xml" ).getFile() );

		final BdvStackSource< ? > bdvStackSource2 = BdvFunctions.show( spimData2,
				BdvOptions.options().addTo(
						bdvHandle ) ).get( 0 );

		bdvStackSource2.setDisplayRange( 0, 255 );
		AccumulateEMAndFMProjectorARGB.setAccumulationModality( BdvUtils.getSource( bdvStackSource1, 0 ).getName(), AccumulateEMAndFMProjectorARGB.SUM );

	}

}
