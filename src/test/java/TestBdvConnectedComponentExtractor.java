import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import bdv.viewer.Source;
import de.embl.cba.bdv.utils.labels.ARGBConvertedRealTypeSpimDataSource;
import de.embl.cba.bdv.utils.labels.ConfigurableVolatileRealVolatileARGBConverter;
import de.embl.cba.bdv.utils.regions.BdvConnectedComponentExtractor;
import de.embl.cba.bdv.utils.transformhandlers.BehaviourTransformEventHandler3DLeftMouseDrag;
import ij.ImageJ;
import ij.ImagePlus;
import ij3d.Content;
import ij3d.Image3DUniverse;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.volatiles.VolatileARGBType;
import net.imglib2.view.Views;
import org.scijava.vecmath.Color3f;

public class TestBdvConnectedComponentExtractor
{

	public static void main( String[] args ) throws SpimDataException
	{
		final String labelsSource = TestConfigurableVolatileRealVolatileARGBConverter.class.getResource( "labels.xml" ).getFile();

		SpimData spimData = new XmlIoSpimData().load( labelsSource );

		final ConfigurableVolatileRealVolatileARGBConverter converter = new ConfigurableVolatileRealVolatileARGBConverter( );

		final Source< VolatileARGBType > labelSource = new ARGBConvertedRealTypeSpimDataSource( spimData, "labels", 0, converter );

		final BdvStackSource< VolatileARGBType > bdvStackSource =
				BdvFunctions.show( labelSource,
						BdvOptions.options().transformEventHandlerFactory( new BehaviourTransformEventHandler3DLeftMouseDrag.BehaviourTransformEventHandler3DFactory() ) );

		final BdvConnectedComponentExtractor bdvConnectedComponentExtractor = new BdvConnectedComponentExtractor(
				bdvStackSource.getBdvHandle(),
				bdvStackSource.getSources().get( 0 ).getSpimSource(),
				new RealPoint( 35, 35, 8 ),
				0 );

		RandomAccessibleInterval connectedComponentMask = bdvConnectedComponentExtractor.getConnectedComponentMask( 0 );

		connectedComponentMask = changeToImageJDimensionConvention( connectedComponentMask );

		show2D( connectedComponentMask );

		show3D( connectedComponentMask );

	}

	public static void show3D( RandomAccessibleInterval connectedComponentMask )
	{
		final ImagePlus regionMaskImp = ImageJFunctions.wrap( connectedComponentMask, "" );
		Image3DUniverse univ = new Image3DUniverse( );
		univ.show( );
		final Content content = univ.addMesh( regionMaskImp, null, "somename", 250, new boolean[]{ true, true, true }, 2 );
		content.setColor( new Color3f(0.5f, 0, 0.5f ) );
	}

	public static void show2D( RandomAccessibleInterval connectedComponentMask )
	{
		new ImageJ();
		ImageJFunctions.show( connectedComponentMask, "" );
	}

	public static RandomAccessibleInterval changeToImageJDimensionConvention( RandomAccessibleInterval connectedComponentMask )
	{
		connectedComponentMask = Views.addDimension( connectedComponentMask, 0, 0 );
		connectedComponentMask = Views.permute( connectedComponentMask, 2,3 );
		return connectedComponentMask;
	}
}
