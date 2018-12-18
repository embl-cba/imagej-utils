import bdv.tools.transformation.TransformedSource;
import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import de.embl.cba.bdv.utils.algorithms.RegionExtractor;
import de.embl.cba.bdv.utils.labels.LabelsSource;
import de.embl.cba.bdv.utils.labels.VolatileSelectedLabelsARGBConverter;
import de.embl.cba.bdv.utils.transformhandlers.BehaviourTransformEventHandler3DGoogleMouse;
import ij.ImageJ;
import ij.ImagePlus;
import ij3d.Content;
import ij3d.Image3DUniverse;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.DiamondShape;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.volatiles.VolatileARGBType;
import net.imglib2.view.Views;
import org.scijava.vecmath.Color3f;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class TestRegionExtractionAnd3DViewer
{

	public static void main( String[] args ) throws SpimDataException
	{
		final File file = new File( "/Users/tischer/Desktop/bdv_test_data/test.xml" );

		SpimData spimData = new XmlIoSpimData().load( file.toString() );

		Set< Double > selectedLabels = new HashSet(  );
		final VolatileSelectedLabelsARGBConverter volatileSelectedLabelsARGBConverter = new VolatileSelectedLabelsARGBConverter( selectedLabels );
		final Source< VolatileARGBType > labelSource = new LabelsSource( spimData, 0, volatileSelectedLabelsARGBConverter );

		final BdvStackSource< VolatileARGBType > bdvStackSource =
				BdvFunctions.show( labelSource,
						BdvOptions.options().transformEventHandlerFactory( new BehaviourTransformEventHandler3DGoogleMouse.BehaviourTransformEventHandler3DFactory() ) );

		final SourceAndConverter< VolatileARGBType > sourceAndConverter = bdvStackSource.getSources().get( 0 );

		final Source< VolatileARGBType > spimSource = sourceAndConverter.getSpimSource();

		final Source wrappedSource = ( ( TransformedSource ) spimSource ).getWrappedSource();

		RandomAccessibleInterval source = ( ( LabelsSource ) wrappedSource ).getWrappedSource( 0,0 );

		final RegionExtractor regionExtractor = new RegionExtractor( source, new DiamondShape( 1 ), 1000*1000*1000L );

		long startTimeMillis = System.currentTimeMillis();
		regionExtractor.run( new long[]{ 35, 35, 58 } );
		System.out.println( "Region extracted in [ms]: " + ( System.currentTimeMillis() - startTimeMillis ) );

		if ( regionExtractor.isMaxRegionSizeReached( ) )
		{
			System.out.println( "MaxRegionSizeReached" );
		}

		RandomAccessibleInterval regionMask = regionExtractor.getCroppedRegionMask();

		new ImageJ();
		regionMask = Views.addDimension( regionMask, 0, 0 );
		regionMask = Views.permute( regionMask, 2,3 );
		final ImagePlus regionMaskImp = ImageJFunctions.show( regionMask );

		source = Views.addDimension( source, 0, 0 );
		source = Views.permute( source, 2,3 );
		ImageJFunctions.show( source );

		startTimeMillis = System.currentTimeMillis();
		Image3DUniverse univ = new Image3DUniverse( );
		univ.show( );
		System.out.println( "New universe created in [ms]: " + ( System.currentTimeMillis() - startTimeMillis ) );

		startTimeMillis = System.currentTimeMillis();
		final Content content = univ.addMesh( regionMaskImp, null, "somename", 250, new boolean[]{ true, true, true }, 2 );
		content.setColor( new Color3f(0.5f, 0, 0.5f ) );
		System.out.println( "Mesh created and displayed in [ms]: " + ( System.currentTimeMillis() - startTimeMillis ) );

	}
}
