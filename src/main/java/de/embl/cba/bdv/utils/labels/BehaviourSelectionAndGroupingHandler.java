package de.embl.cba.bdv.utils.labels;

import bdv.util.Bdv;
import de.embl.cba.bdv.utils.BdvUtils;
import de.embl.cba.bdv.utils.labels.luts.RandomLUTMapper;
import net.imglib2.RealPoint;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

import java.util.HashSet;
import java.util.Set;

public class BehaviourSelectionAndGroupingHandler
{
	final Bdv bdv;
	final ConfigurableVolatileRealVolatileARGBConverter converter;
	final Set< Double > selectedValues;
	final String sourceName;

	private String selectObject = "shift button1";
	private String quitSelection = "Q";
	private String shuffle = "shift S";;

	public BehaviourSelectionAndGroupingHandler( Bdv bdv,
												 ConfigurableVolatileRealVolatileARGBConverter converter, String sourceName )
	{
		this.bdv = bdv;
		this.converter = converter;
		this.sourceName = sourceName;
		selectedValues = new HashSet<>( );
	}

	public void installBehaviours()
	{
		final Behaviours behaviours = new Behaviours( new InputTriggerConfig() );

		behaviours.install( bdv.getBdvHandle().getTriggerbindings(), "behaviours" );

		installSelectionBehaviour( behaviours, selectObject );

		installShowAllBehaviour( behaviours, quitSelection );

		installRandomColorShufflingBehaviour( behaviours, shuffle );

	}

	private void installRandomColorShufflingBehaviour( Behaviours behaviours, String shuffle )
	{
		behaviours.behaviour( ( ClickBehaviour ) ( x, y ) ->
		{
			shuffleRandomLUT();
		}, sourceName+"-shuffle-random-colors", shuffle );
	}

	private void shuffleRandomLUT()
	{
		if( converter.getLUTMapper() instanceof RandomLUTMapper )
		{
			final RandomLUTMapper lutMapper = ( RandomLUTMapper ) converter.getLUTMapper();
			final long seed = lutMapper.getSeed();
			lutMapper.setSeed( seed + 1 );
			repaint();
		}
		else
		{
			// do nothing
		}
	}

	private void repaint()
	{
		bdv.getBdvHandle().getViewerPanel().requestRepaint();
	}

	private void installShowAllBehaviour( Behaviours behaviours, String quitSelection )
	{
		behaviours.behaviour( ( ClickBehaviour ) ( x, y ) -> {
			showAll();
		}, sourceName+"quit-selection", quitSelection );
	}

	private void showAll()
	{
		converter.onlyShowSelectedValues( null );
		selectedValues.clear();
		repaint();
	}

	private void installSelectionBehaviour( Behaviours behaviours, String selectObject )
	{
		behaviours.behaviour( ( ClickBehaviour ) ( x, y ) -> {
			toggleSelectionAtMousePosition();
		}, sourceName+"-toggleSelectionAtMousePosition-object", selectObject ) ;
	}

	private void toggleSelectionAtMousePosition()
	{
		final RealPoint globalMouseCoordinates = BdvUtils.getGlobalMouseCoordinates( bdv );

		final double selectedLabel = BdvUtils.getValueAtGlobalPosition( globalMouseCoordinates, 0, ARGBConvertedRealSource );

		if ( selectedValues.contains( selectedLabel ) )
		{
			selectedValues.remove( selectedLabel );
		}
		else
		{
			selectedValues.add( selectedLabel );
		}

		converter.onlyShowSelectedValues( selectedValues );
		repaint();
	}
}
