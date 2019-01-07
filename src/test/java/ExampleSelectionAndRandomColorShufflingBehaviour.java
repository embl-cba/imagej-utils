import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.RandomAccessibleIntervalSource;
import de.embl.cba.bdv.utils.behaviour.BehaviourRandomColorShufflingEventHandler;
import de.embl.cba.bdv.utils.selection.BdvSelectionEventHandler;
import de.embl.cba.bdv.utils.converters.RandomARGBConverter;
import de.embl.cba.bdv.utils.converters.SelectableVolatileARGBConverter;
import de.embl.cba.bdv.utils.sources.SelectableVolatileARGBConvertedRealSource;

public class ExampleSelectionAndRandomColorShufflingBehaviour
{
	public static void main( String[] args )
	{
		final RandomAccessibleIntervalSource raiSource = ExampleARGBConverted2d16bitTiffImage.getRandomAccessibleIntervalSource();

		final RandomARGBConverter randomARGBConverter = new RandomARGBConverter();

		final SelectableVolatileARGBConverter selectableConverter = new SelectableVolatileARGBConverter( randomARGBConverter );

		final SelectableVolatileARGBConvertedRealSource selectableSource = new SelectableVolatileARGBConvertedRealSource( raiSource, selectableConverter );

		Bdv bdv = BdvFunctions.show( selectableSource, BdvOptions.options().is2D() ).getBdvHandle();

		new BdvSelectionEventHandler( bdv, selectableSource );

		new BehaviourRandomColorShufflingEventHandler( bdv, randomARGBConverter, raiSource.getName() );
	}

}
