import bdv.VolatileSpimSource;
import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import bdv.viewer.Source;
import de.embl.cba.bdv.utils.BdvUtils;
import de.embl.cba.bdv.utils.labels.ARGBConvertedRealTypeSource;
import de.embl.cba.bdv.utils.labels.ARGBConvertedVolatileRealTypeSource;
import de.embl.cba.bdv.utils.labels.ConfigurableRealTypeVolatileARGBTypeConverter;
import de.embl.cba.bdv.utils.labels.ConfigurableVolatileRealVolatileARGBConverter;
import de.embl.cba.bdv.utils.labels.luts.RandomLUTMapper;
import de.embl.cba.bdv.utils.transformhandlers.BehaviourTransformEventHandler3DLeftMouseDrag;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;
import net.imglib2.RealPoint;
import net.imglib2.type.volatiles.VolatileARGBType;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

import java.util.HashSet;
import java.util.Set;

public class TestConfigurableRealTypeVolatileARGBTypeConverter
{

	public static void main( String[] args ) throws SpimDataException
	{

		final String labelsSourcePath = TestConfigurableRealTypeVolatileARGBTypeConverter.class.getResource( "labels.xml" ).getFile();

		SpimData spimData = new XmlIoSpimData().load( labelsSourcePath );

		final VolatileSpimSource volatileSpimSource = new VolatileSpimSource( spimData, 0, "name" );

		final ConfigurableRealTypeVolatileARGBTypeConverter converter = new ConfigurableRealTypeVolatileARGBTypeConverter( );

		final Source< VolatileARGBType > labelsSource = new ARGBConvertedRealTypeSource( volatileSpimSource, "labels", converter );

		final BdvStackSource< VolatileARGBType > bdvStackSource =
				BdvFunctions.show( labelsSource,
						BdvOptions.options().transformEventHandlerFactory( new BehaviourTransformEventHandler3DLeftMouseDrag.BehaviourTransformEventHandler3DFactory() ) );

		final Bdv bdv = bdvStackSource.getBdvHandle();

		final Behaviours behaviours = new Behaviours( new InputTriggerConfig() );
		behaviours.install( bdv.getBdvHandle().getTriggerbindings(), "behaviours" );

		Set< Double > selectedValues = new HashSet();


		// Label selection
		//
		behaviours.behaviour( ( ClickBehaviour ) ( x, y ) -> {
			final RealPoint globalMouseCoordinates = BdvUtils.getGlobalMouseCoordinates( bdv );
			final double selectedLabel = BdvUtils.getValueAtGlobalCoordinates( labelsSource, globalMouseCoordinates, 0 );

			if ( selectedValues.contains( selectedLabel ) )
			{
				selectedValues.remove( selectedLabel );
			}
			else
			{
				selectedValues.add( selectedLabel );
			}
			converter.onlyShowSelectedValues( selectedValues );
			bdv.getBdvHandle().getViewerPanel().requestRepaint();
		}, "select object", "shift button1"  ) ;

		// Quit label selection
		//
		behaviours.behaviour( ( ClickBehaviour ) ( x, y ) -> {
			converter.onlyShowSelectedValues( null );
			selectedValues.clear();
			bdv.getBdvHandle().getViewerPanel().requestRepaint();
		}, "quit selection", "Q" );

		// Shuffle random colors
		//
		behaviours.behaviour( ( ClickBehaviour ) ( x, y ) -> {
			if( converter.getLUTMapper() instanceof RandomLUTMapper )
			{
				final RandomLUTMapper lutMapper = ( RandomLUTMapper ) converter.getLUTMapper();
				final long seed = lutMapper.getSeed();
				lutMapper.setSeed( seed + 1 );
				bdv.getBdvHandle().getViewerPanel().requestRepaint();
			}
		}, "shuffle random colors", "shift S" );


	}
}
