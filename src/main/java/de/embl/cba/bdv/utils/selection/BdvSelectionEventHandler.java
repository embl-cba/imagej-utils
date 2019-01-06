package de.embl.cba.bdv.utils.selection;

import bdv.util.Bdv;
import de.embl.cba.bdv.utils.*;
import de.embl.cba.bdv.utils.converters.SelectableVolatileARGBConverter;
import de.embl.cba.bdv.utils.sources.SelectableVolatileARGBConvertedRealSource;
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
	final SelectableVolatileARGBConvertedRealSource source;
	final SelectableVolatileARGBConverter selectableConverter;
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
	 */
	public BdvSelectionEventHandler( Bdv bdv,
									 SelectableVolatileARGBConvertedRealSource selectableSource )
	{
		this.bdv = bdv;
		this.source = selectableSource;
		this.selectableConverter = selectableSource.getSelectableConverter();
		this.sourceName = source.getName();

		this.selectionEventListeners = new CopyOnWriteArrayList<>(  );

		selectionModes = Arrays.asList( SelectableVolatileARGBConverter.SelectionMode.values() );

		installBdvBehaviours();
	}

	public Set< Double > getSelectedValues()
	{
		return selectableConverter.getSelections();
	}

	public void selectNone()
	{
		selectableConverter.setSelections( null );
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
		bdvBehaviours.behaviour( ( ClickBehaviour ) ( x, y ) ->
		{
			if ( BdvUtils.isActive( bdv, source ) )
			{
				iterateSelectionMode();
			}
		}, sourceName + "-iterate-selection", iterateSelectionMode );
	}

	private void installSelectNoneBehaviour( )
	{
		bdvBehaviours.behaviour( ( ClickBehaviour ) ( x, y ) ->
		{
			if ( BdvUtils.isActive( bdv, source ) )
			{
				selectNone();
			}
		}, sourceName + "-select-none", selectNoneTrigger );
	}

	private void installSelectionBehaviour()
	{
		bdvBehaviours.behaviour( ( ClickBehaviour ) ( x, y ) ->
		{
			if ( BdvUtils.isActive( bdv, source ) )
			{
				toggleSelectionAtMousePosition();
			}
		}, sourceName+"-toggle-selection", selectTrigger ) ;
	}

	private void toggleSelectionAtMousePosition()
	{
		final RealPoint globalMouseCoordinates = BdvUtils.getGlobalMouseCoordinates( bdv );

		final double selected = BdvUtils.getValueAtGlobalCoordinates(
				source.getWrappedRealSource(),
				globalMouseCoordinates,
				0 );

		if ( selected == 0 ) return; // background

		if ( isNewSelection( selected ) )
		{
			addSelection( selected );

			for ( final SelectionEventListener s : selectionEventListeners )
				s.valueSelected( selected );
		}
		else
		{
			selectableConverter.removeSelection( selected );
			BdvUtils.repaint( bdv );
		}
	}

	private boolean isNewSelection( double selected )
	{
		return selectableConverter.getSelections() == null || ! selectableConverter.getSelections().contains( selected );
	}

	public void addSelection( double selected )
	{
		selectableConverter.addSelection( selected );
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
		return selectableConverter;
	}

	private void iterateSelectionMode()
	{
		final int selectionModeIndex = selectionModes.indexOf( selectableConverter.getSelectionMode() );

		if ( selectionModeIndex < selectionModes.size() -1 )
		{
			selectableConverter.setSelectionMode( selectionModes.get( selectionModeIndex + 1 ) );
		}
		else
		{
			selectableConverter.setSelectionMode( selectionModes.get( 0 ) );
		}

		BdvUtils.repaint( bdv );
	}

}

