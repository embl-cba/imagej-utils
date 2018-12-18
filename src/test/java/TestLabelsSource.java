import bdv.tools.transformation.TransformedSource;
import bdv.util.*;
import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import de.embl.cba.bdv.utils.BdvUtils;
import de.embl.cba.bdv.utils.labels.ARGBConvertedRealSource;
import de.embl.cba.bdv.utils.labels.LUTs;
import de.embl.cba.bdv.utils.labels.SelectedVolatileRealToRandomARGBConverter;
import de.embl.cba.bdv.utils.transformhandlers.BehaviourTransformEventHandler3DGoogleMouse;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;
import net.imglib2.RealPoint;
import net.imglib2.type.volatiles.VolatileARGBType;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class TestLabelsSource
{

	public static void main( String[] args ) throws SpimDataException
	{

		// Loader class auto-discovery happens here:
		// https://github.com/bigdataviewer/spimdata/blob/master/src/main/java/mpicbg/spim/data/generic/sequence/ImgLoaders.java#L53

		//final File file = new File( "/Volumes/arendt/EM_6dpf_segmentation/EM-Prospr/em-segmented-cells-parapodium-labels-test.xml" );

		final File file = new File( "/Users/tischer/Desktop/bdv_test_data/test.xml" );
		//final File file = new File( "/Users/tischer/Desktop/bdv_test_data/bdv_mipmap-labels.xml" );

		SpimData spimData = new XmlIoSpimData().load( file.toString() );

		Set< Double > selectedLabels = new HashSet();
		final SelectedVolatileRealToRandomARGBConverter selectedVolatileRealToRandomARGBConverter = new SelectedVolatileRealToRandomARGBConverter( selectedLabels, LUTs.GLASBEY_LUT );
		final ARGBConvertedRealSource ARGBConvertedRealSource = new ARGBConvertedRealSource( spimData, 0, selectedVolatileRealToRandomARGBConverter );

		selectedVolatileRealToRandomARGBConverter.showAll();
		final BdvStackSource< VolatileARGBType > bdvStackSource =
				BdvFunctions.show( ARGBConvertedRealSource,
						BdvOptions.options().transformEventHandlerFactory( new BehaviourTransformEventHandler3DGoogleMouse.BehaviourTransformEventHandler3DFactory() ) );


		final Bdv bdv = bdvStackSource.getBdvHandle();

		final Behaviours behaviours = new Behaviours( new InputTriggerConfig() );
		behaviours.install( bdv.getBdvHandle().getTriggerbindings(), "behaviours" );


		behaviours.behaviour( ( ClickBehaviour ) ( x, y ) -> {
			final RealPoint globalMouseCoordinates = BdvUtils.getGlobalMouseCoordinates( bdv );
			final double selectedLabel = BdvUtils.getValueAtGlobalPosition( globalMouseCoordinates, 0, ARGBConvertedRealSource );

			if ( selectedLabels.contains( selectedLabel ) )
			{
				selectedLabels.remove( selectedLabel );
			}
			else
			{
				selectedLabels.add( selectedLabel );
			}
			selectedVolatileRealToRandomARGBConverter.setSelectedLabels( selectedLabels );
			selectedVolatileRealToRandomARGBConverter.showSelectedOnly();
			bdv.getBdvHandle().getViewerPanel().requestRepaint();
		}, "select object", "button1 shift"  ) ;


		behaviours.install( bdv.getBdvHandle().getTriggerbindings(), "behaviours" );
		behaviours.behaviour( ( ClickBehaviour ) ( x, y ) -> {
			selectedVolatileRealToRandomARGBConverter.showAll();
			selectedLabels.clear();
			bdv.getBdvHandle().getViewerPanel().requestRepaint();
		}, "quit selection", "Q" );


		final SourceAndConverter< VolatileARGBType > sourceAndConverter = bdvStackSource.getSources().get( 0 );

		final Source< VolatileARGBType > spimSource = sourceAndConverter.getSpimSource();

		if ( spimSource instanceof TransformedSource )
		{
			final Source wrappedSource = ( ( TransformedSource ) spimSource ).getWrappedSource();

			if ( wrappedSource instanceof ARGBConvertedRealSource )
			{
//				IJ.wait( 5000 );
//				((LabelsSource)wrappedSource).incrementSeed();
//				bdvStackSource.getBdvHandle().getViewerPanel().requestRepaint();
//				IJ.wait( 5000 );
//				((LabelsSource)wrappedSource).incrementSeed();
//				bdvStackSource.getBdvHandle().getViewerPanel().requestRepaint();

//				final RandomAccessibleInterval< IntegerType > indexImg = ( ( LabelsSource ) wrappedSource ).getIndexImg( 0, 0 );
//
//				final RandomAccess< IntegerType > access = indexImg.randomAccess();
//
//				final long integerLong = access.get().getIntegerLong();
//
//				int a = 1;

			}
		}
		else
		{
			int b = 2;
		}





	}
}
