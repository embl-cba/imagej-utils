package explore;

import bdv.util.*;
import bdv.viewer.Source;
import bdv.viewer.render.AccumulateProjectorARGB;
import bdv.viewer.render.AccumulateProjectorFactory;
import bdv.viewer.render.VolatileProjector;
import de.embl.cba.bdv.utils.render.AccumulateEMProjectorARGB;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.view.composite.Composite;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

public class ExploreBlendingModes
{
	public static void main( String[] args ) throws SpimDataException
	{
		SpimData spimData = new XmlIoSpimData().load( ExploreBlendingModes.class.getResource( "../mri-stack.xml" ).getFile() );

		final AccumulateEMProjectorARGB.AccumulateEMProjectorFactory accumulateProjectorFactory = new AccumulateEMProjectorARGB.AccumulateEMProjectorFactory();

		final BdvStackSource< ? > bdvStackSource = BdvFunctions.show( spimData, BdvOptions.options().accumulateProjectorFactory( accumulateProjectorFactory ) ).get( 0 );
		bdvStackSource.setDisplayRange( 0, 255 );


		SpimData spimData1 = new XmlIoSpimData().load( ExploreBlendingModes.class.getResource( "../mri-stack-shifted.xml" ).getFile() );

		final BdvHandle bdvHandle = bdvStackSource.getBdvHandle();

		accumulateProjectorFactory.setBdvHandle( bdvHandle );

		final BdvStackSource< ? > bdvStackSource1 = BdvFunctions.show( spimData1,
				BdvOptions.options().addTo( bdvHandle ) ).get( 0 );
		bdvStackSource1.setDisplayRange( 0, 255 );


		SpimData spimData2 = new XmlIoSpimData().load( ExploreBlendingModes.class.getResource( "../mri-stack-shifted1.xml" ).getFile() );

		final BdvStackSource< ? > bdvStackSource2 = BdvFunctions.show( spimData2,
				BdvOptions.options().addTo(
						bdvHandle ) ).get( 0 );

		bdvStackSource2.setDisplayRange( 0, 255 );
	}

}
