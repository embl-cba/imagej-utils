import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import de.embl.cba.bdv.utils.selection.BdvSelectionEventHandler;
import de.embl.cba.bdv.utils.sources.SelectableVolatileARGBConvertedRealSource;
import mpicbg.spim.data.SpimDataException;

public class ExampleInteractive3DObjectView
{
	public static void main( String[] args ) throws SpimDataException
	{
		final SelectableVolatileARGBConvertedRealSource selectable3DSource = Examples.getSelectable3DSource();
		final Bdv bdv = BdvFunctions.show( selectable3DSource ).getBdvHandle();
		new BdvSelectionEventHandler( bdv, selectable3DSource );
	}
}
