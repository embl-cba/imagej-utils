package de.embl.cba.bdv.utils.behaviour;

import bdv.util.Bdv;
import bdv.viewer.Source;
import de.embl.cba.bdv.utils.*;
import de.embl.cba.bdv.utils.converters.argb.SelectableVolatileARGBConverter;
import net.imglib2.RealPoint;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class BdvSelectionEventHandler
{
	final Bdv bdv;
	final SelectableVolatileARGBConverter converter;
	final Source source;
	final String sourceName;

	Behaviours bdvBehaviours;

	private String toggleSelectionTrigger = "ctrl button1";
	private String selectNoneTrigger = "ctrl Q";
	private CopyOnWriteArrayList< SelectionEventListener > selectionEventListeners;


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

		installSelectionBehaviour( toggleSelectionTrigger );

		installSelectNoneBehaviour( selectNoneTrigger );
	}

	private void installSelectNoneBehaviour( String selectNoneTrigger )
	{
		bdvBehaviours.behaviour( ( ClickBehaviour ) ( x, y ) -> {
			selectNone();
		}, sourceName + "-select-none", selectNoneTrigger );
	}


	private void installSelectionBehaviour( String objectSelectionTrigger )
	{
		bdvBehaviours.behaviour( ( ClickBehaviour ) ( x, y ) -> {
			toggleSelectionAtMousePosition();
		}, sourceName+"-toggle-selection", objectSelectionTrigger ) ;
	}

	private void toggleSelectionAtMousePosition()
	{
		final RealPoint globalMouseCoordinates = BdvUtils.getGlobalMouseCoordinates( bdv );

		final double selected = BdvUtils.getValueAtGlobalCoordinates( source, globalMouseCoordinates, 0 );

		if ( selected == 0 ) return; // background

		if ( converter.getSelections().contains( selected ) )
		{
			converter.removeSelection( selected );
			BdvUtils.repaint( bdv );
		}
		else
		{
			addSelection( selected );
		}
	}

	public void addSelection( double selected )
	{
		// notify listeners
		for ( final SelectionEventListener s : selectionEventListeners )
			s.valueSelected( selected );

		converter.addSelection( selected );
		BdvUtils.repaint( bdv );
	}

	public void addSelectionEventListener( SelectionEventListener s )
	{
		selectionEventListeners.add( s );
	}


}

