package de.embl.cba.bdv.utils.behaviour;

import bdv.util.Bdv;
import bdv.viewer.Source;
import de.embl.cba.bdv.utils.*;
import de.embl.cba.bdv.utils.converters.argb.SelectableVolatileARGBConverter;
import net.imglib2.RealPoint;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class BdvSelectionEventHandler
{
	final Bdv bdv;

	final SelectableVolatileARGBConverter converter;
	final Source source;
	final String sourceName;

	Behaviours bdvBehaviours;

	private String selectTrigger = "ctrl button1";
	private String selectNoneTrigger = "ctrl Q";
	private String iterateSelectionMode = "ctrl S";

	private CopyOnWriteArrayList< SelectionEventListener > selectionEventListeners;
	private List< SelectableVolatileARGBConverter.SelectionMode > selectionModes;

	/**
	 * Selection of argbconversion (objects) in a label source.
	 * @param bdv Bdv window in which the source is shown.
	 * @param source Source containing numeric values.
	 * @param converter Configurable converter, converting numeric values to colors for display.
	 */
	public BdvSelectionEventHandler( Bdv bdv,
									 Source source,
									 SelectableVolatileARGBConverter converter )
	{
		this.bdv = bdv;
		this.converter = converter;
		this.source = source;
		this.sourceName = source.getName();

		this.selectionEventListeners = new CopyOnWriteArrayList<>(  );

		selectionModes = Arrays.asList( SelectableVolatileARGBConverter.SelectionMode.values() );

		installBdvBehaviours();
	}

	public Set< Double > getSelectedValues()
	{
		return converter.getSelections();
	}

	public void selectNone()
	{
		converter.setSelections( null );
		BdvUtils.repaint( bdv );
	}

	private void installBdvBehaviours()
	{
		bdvBehaviours = new Behaviours( new InputTriggerConfig() );
		bdvBehaviours.install( bdv.getBdvHandle().getTriggerbindings(),  sourceName + "-bdv-selection-handler" );

		installSelectionBehaviour( );

		installSelectNoneBehaviour( );

		installSelectModeIterationBehaviour( );
	}

	private void installSelectModeIterationBehaviour( )
	{
		bdvBehaviours.behaviour( ( ClickBehaviour ) ( x, y ) -> {
			iterateSelectionMode();
		}, sourceName + "-iterate-selection", iterateSelectionMode );
	}

	private void installSelectNoneBehaviour( )
	{
		bdvBehaviours.behaviour( ( ClickBehaviour ) ( x, y ) -> {
			selectNone();
		}, sourceName + "-select-none", selectNoneTrigger );
	}

	private void installSelectionBehaviour()
	{
		bdvBehaviours.behaviour( ( ClickBehaviour ) ( x, y ) -> {
			toggleSelectionAtMousePosition();
		}, sourceName+"-toggle-selection", selectTrigger ) ;
	}

	private void toggleSelectionAtMousePosition()
	{
		final RealPoint globalMouseCoordinates = BdvUtils.getGlobalMouseCoordinates( bdv );

		final double selected = BdvUtils.getValueAtGlobalCoordinates( source, globalMouseCoordinates, 0 );

		if ( selected == 0 ) return; // background

		if ( converter.getSelections() == null || ! converter.getSelections().contains( selected ) )
		{
			addSelection( selected );

			// notify listeners
			for ( final SelectionEventListener s : selectionEventListeners )
				s.valueSelected( selected );
		}
		else
		{
			converter.removeSelection( selected );
			BdvUtils.repaint( bdv );
		}
	}

	public void addSelection( double selected )
	{
		converter.addSelection( selected );
		BdvUtils.repaint( bdv );
	}

	public void addSelectionEventListener( SelectionEventListener s )
	{
		selectionEventListeners.add( s );
	}

	public Bdv getBdv()
	{
		return bdv;
	}

	public SelectableVolatileARGBConverter getSelectableConverter()
	{
		return converter;
	}

	private void iterateSelectionMode()
	{
		final int selectionModeIndex = selectionModes.indexOf( converter.getSelectionMode() );

		if ( selectionModeIndex < selectionModes.size() -1 )
		{
			converter.setSelectionMode( selectionModes.get( selectionModeIndex + 1 ) );
		}
		else
		{
			converter.setSelectionMode( selectionModes.get( 0 ) );
		}

		BdvUtils.repaint( bdv );
	}

}

