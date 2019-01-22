package de.embl.cba.bdv.utils.boundingbox;

import bdv.tools.brightness.SetupAssignments;
import bdv.util.BdvFunctions;
import bdv.util.PlaceHolderConverterSetup;
import bdv.util.PlaceHolderSource;
import bdv.viewer.DisplayMode;
import bdv.viewer.*;
import bdv.viewer.VisibilityAndGrouping.Event;
import bdv.viewer.VisibilityAndGrouping.UpdateListener;
import bdv.viewer.state.SourceGroup;
import bdv.viewer.state.SourceState;
import bdv.viewer.state.ViewerState;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedShortType;

import java.awt.*;

import static bdv.viewer.VisibilityAndGrouping.Event.SOURCE_ACTVITY_CHANGED;
import static bdv.viewer.VisibilityAndGrouping.Event.VISIBILITY_CHANGED;

/**
 * A BDV source (and converter etc) representing the BoundingBox. As the
 * {@link BoundingBoxOverlay} is able to draw the intersection, this is just a
 * placeholder source to set the visibility, color, and opacity of that
 * intersection via the standard bdv dialog.
 *
 * @author Tobias Pietzsch
 */
public class BoundingBoxPlaceholderSource implements UpdateListener, BoundingBoxSource
{
	private final BoundingBoxOverlay boxOverlay;

	private final PlaceHolderConverterSetup boxConverterSetup;

	private final Source< UnsignedShortType > boxSource;

	private final SourceAndConverter< UnsignedShortType > boxSourceAndConverter;

	private final ViewerPanel viewer;

	private final SetupAssignments setupAssignments;

	private boolean isVisible;

	private static final boolean USE_ALTERNATIVE_PLACEHOLDER_SOURCE = true;

	public BoundingBoxPlaceholderSource(
			final String name,
			final BoundingBoxOverlay boxOverlay,
			final BoundingBoxOverlay.BoundingBoxOverlaySource bbSource,
			final ViewerPanel viewer,
			final SetupAssignments setupAssignments )
	{
		this.boxOverlay = boxOverlay;
		this.viewer = viewer;
		this.setupAssignments = setupAssignments;

		final int setupId = BdvFunctions.getUnusedSetupId( setupAssignments );
		boxConverterSetup = new PlaceHolderConverterSetup( setupId, 0, 128, new ARGBType( 0x00994499) );

		boxConverterSetup.setViewer( this::repaint );
		boxSource = USE_ALTERNATIVE_PLACEHOLDER_SOURCE ? new AlternativePlaceHolderSource( name, bbSource ) : new PlaceHolderSource( name );
		boxSourceAndConverter = new SourceAndConverter<>( boxSource, ( input, output ) -> output.set( 0 ) );
	}

	@Override
	public void addToViewer()
	{
		final VisibilityAndGrouping vg = viewer.getVisibilityAndGrouping();
		if ( vg.getDisplayMode() != DisplayMode.FUSED )
		{
			final int numSources = vg.numSources();
			for ( int i = 0; i < numSources; ++i )
				vg.setSourceActive( i, vg.isSourceVisible( i ) );
			vg.setDisplayMode( DisplayMode.FUSED );
		}

		viewer.addSource( boxSourceAndConverter );
		vg.addUpdateListener( this );
		vg.setSourceActive( boxSource, true );
		vg.setCurrentSource( boxSource );

		setupAssignments.addSetup( boxConverterSetup );
		setupAssignments.getMinMaxGroup( boxConverterSetup ).setRange( 0, 255 );

		isVisible = isVisible();
		repaint();
	}

	@Override
	public void removeFromViewer()
	{
		final VisibilityAndGrouping vg = viewer.getVisibilityAndGrouping();
		vg.removeUpdateListener( this );
		viewer.removeSource( boxSource );
		setupAssignments.removeSetup( boxConverterSetup );
	}

	private boolean isVisible()
	{
		final ViewerState state = viewer.getState();
		int sourceIndex = 0;
		for ( final SourceState< ? > s : state.getSources() )
			if ( s.getSpimSource() == boxSource )
				break;
			else
				++sourceIndex;
		switch ( state.getDisplayMode() )
		{
		case SINGLE:
			return ( sourceIndex == state.getCurrentSource() );
		case GROUP:
			return state.getSourceGroups().get( state.getCurrentGroup() ).getSourceIds().contains( sourceIndex );
		case FUSED:
			return state.getSources().get( sourceIndex ).isActive();
		case FUSEDGROUP:
		default:
			for ( final SourceGroup group : state.getSourceGroups() )
				if ( group.isActive() && group.getSourceIds().contains( sourceIndex ) )
					return true;
		}
		return false;
	}

	@Override
	public void visibilityChanged( final Event e )
	{
		if ( e.id == VISIBILITY_CHANGED || e.id == SOURCE_ACTVITY_CHANGED )
		{
			final boolean wasVisible = isVisible;
			isVisible = isVisible();
			if ( wasVisible != isVisible )
				repaint();
		}
	}

	private void repaint()
	{
		boxOverlay.fillIntersection( isVisible );
		if ( isVisible )
		{
			final int alpha = Math.min( 255, ( int ) boxConverterSetup.getDisplayRangeMax() );
			final int argb = ( boxConverterSetup.getColor().get() & 0x00ffffff ) | ( alpha << 24 );
			boxOverlay.setIntersectionFillColor( new Color( argb, true ) );
		}
		viewer.getDisplay().repaint();
	}
}
