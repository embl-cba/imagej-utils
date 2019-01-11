package de.embl.cba.bdv.utils.selection;

import bdv.util.Bdv;
import de.embl.cba.bdv.utils.*;
import de.embl.cba.bdv.utils.converters.SelectableVolatileARGBConverter;
import de.embl.cba.bdv.utils.objects3d.ConnectedComponentExtractorAnd3DViewer;
import de.embl.cba.bdv.utils.sources.SelectableVolatileARGBConvertedRealSource;
import net.imglib2.type.numeric.real.DoubleType;
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
	private String iterateSelectionModeTrigger = "ctrl S";
	private String viewIn3DTrigger = "ctrl shift button1";

	private CopyOnWriteArrayList< SelectionEventListener > selectionEventListeners;
	private List< SelectableVolatileARGBConverter.SelectionMode > selectionModes;
	private double resolution3DView;

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
		this.selectionModes = Arrays.asList( SelectableVolatileARGBConverter.SelectionMode.values() );

		this.resolution3DView = 0.2;

		installBdvBehaviours();
	}

	public void set3DObjectViewResolution( double resolution3DView )
	{
		this.resolution3DView = resolution3DView;
	}

	public Set< Double > getSelectedValues()
	{
		return selectableConverter.getSelections();
	}

	private void installBdvBehaviours()
	{
		bdvBehaviours = new Behaviours( new InputTriggerConfig() );
		bdvBehaviours.install( bdv.getBdvHandle().getTriggerbindings(),  sourceName + "-bdv-selection-handler" );

		installSelectionBehaviour( );
		installSelectNoneBehaviour( );
		installSelectionModeIterationBehaviour( );
		if( is3D() ) install3DViewBehaviour();

	}

	private boolean is3D()
	{
		return source.getWrappedRealSource( 0, 0 ).numDimensions() == 3;
	}

	private void install3DViewBehaviour( )
	{
		bdvBehaviours.behaviour( ( ClickBehaviour ) ( x, y ) ->
		{
			if ( BdvUtils.isActive( bdv, source ) )
			{
				viewIn3D();
			}
		}, sourceName + "-view-3d", viewIn3DTrigger );
	}

	private void viewIn3D()
	{
		new Thread( new Runnable()
		{
			@Override
			public void run()
			{
				new ConnectedComponentExtractorAnd3DViewer( source )
						.extractAndShowIn3D(
								BdvUtils.getGlobalMouseCoordinates( bdv ),
								resolution3DView );
			}
		} ).start();
	}

	private void installSelectionModeIterationBehaviour( )
	{
		bdvBehaviours.behaviour( ( ClickBehaviour ) ( x, y ) ->
		{
			if ( BdvUtils.isActive( bdv, source ) )
			{
				iterateSelectionMode();
			}
		}, sourceName + "-iterate-selection", iterateSelectionModeTrigger );
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

	public void selectNone()
	{
		selectableConverter.setSelections( null );
		BdvUtils.repaint( bdv );
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
		final double selected = BdvUtils.getValueAtGlobalCoordinates(
				source,
				BdvUtils.getGlobalMouseCoordinates( bdv ),
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
		}

		requestRepaint();
	}

	private boolean isNewSelection( double selected )
	{
		return selectableConverter.getSelections() == null || ! selectableConverter.getSelections().contains( selected );
	}

	public void addSelection( double selected )
	{
		selectableConverter.addSelection( selected );
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

	public void requestRepaint()
	{
		BdvUtils.repaint( bdv );
	}

}

