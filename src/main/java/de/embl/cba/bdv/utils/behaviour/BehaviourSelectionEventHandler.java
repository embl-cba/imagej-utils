package de.embl.cba.bdv.utils.behaviour;

import bdv.util.Bdv;
import bdv.viewer.Source;
import de.embl.cba.bdv.utils.*;
import de.embl.cba.bdv.utils.argbconversion.SelectableRealVolatileARGBConverter;
import net.imglib2.RealPoint;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

import java.util.HashSet;
import java.util.Set;

public class BehaviourSelectionEventHandler
{
	final Bdv bdv;
	final SelectableRealVolatileARGBConverter converter;
	final Source source;
	final Set< Double > selectedValues;
	final String sourceName;

	Behaviours bdvBehaviours;

	private String toggleSelectionTrigger = "ctrl button1";
	private String selectNoneTrigger = "ctrl Q";


	/**
	 * Selection of argbconversion (objects) in a label source.
	 *  @param bdv Bdv window in which the source is shown.
	 * @param bdvBehaviours1
	 * @param source Source containing numeric values.
	 * @param converter Configurable converter, converting numeric values to colors for display.
	 */
	public BehaviourSelectionEventHandler( Bdv bdv,
										   Source source,
										   SelectableRealVolatileARGBConverter converter )
	{
		this.bdv = bdv;
		this.converter = converter;
		this.source = source;
		this.sourceName = source.getName();
		this.selectedValues = new HashSet<>( );

		installBdvBehaviours();
	}

	public Set< Double > getSelectedValues()
	{
		return selectedValues;
	}

	public void selectNone()
	{
		converter.highlightSelectedValues( null );
		selectedValues.clear();
		BdvUtils.repaint( bdv );
	}

	private void installBdvBehaviours()
	{
		bdvBehaviours = new Behaviours( new InputTriggerConfig() );
		bdvBehaviours.install( bdv.getBdvHandle().getTriggerbindings(),  sourceName + "-bdv-selection-handler" );

		installSelectionBehaviour( toggleSelectionTrigger );

		installSelectNoneBehaviour( selectNoneTrigger );
	}

	private void installSelectNoneBehaviour( String selectNoneTrigger )
	{
		bdvBehaviours.behaviour( ( ClickBehaviour ) ( x, y ) -> {
			selectNone();
		}, sourceName + "-select-none", selectNoneTrigger );
	}


	private void installSelectionBehaviour( String selectObject )
	{
		bdvBehaviours.behaviour( ( ClickBehaviour ) ( x, y ) -> {
			toggleSelectionAtMousePosition();
		}, sourceName+"-toggle-selection", selectObject ) ;
	}

	private void toggleSelectionAtMousePosition()
	{
		final RealPoint globalMouseCoordinates = BdvUtils.getGlobalMouseCoordinates( bdv );

		final double selectedLabel = BdvUtils.getValueAtGlobalCoordinates( source, globalMouseCoordinates, 0 );

		if ( selectedValues.contains( selectedLabel ) )
		{
			selectedValues.remove( selectedLabel );
		}
		else
		{
			selectedValues.add( selectedLabel );
		}

		converter.highlightSelectedValues( selectedValues );

		BdvUtils.repaint( bdv );
	}

}

