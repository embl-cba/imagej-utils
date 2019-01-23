package de.embl.cba.bdv.utils.boundingbox;

import bdv.util.BdvHandle;
import net.imglib2.FinalRealInterval;
import net.imglib2.RealInterval;
import net.imglib2.realtransform.AffineTransform3D;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class InteractiveRealBoundingBox
{
	private final BdvHandle bdv;
	private final FinalRealInterval initialSelection;
	private final FinalRealInterval selectionRange;
	private final AffineTransform3D transform;

	private RealBoundingBoxModel model;

	/**
	 * Installs an interactive bounding-box with slider panel on a BDV.
	 * <p>
	 * The feature consists of an overlay added to the BDV and editing behaviours
	 * where the user can edit the bounding-box directly interacting with the
	 * overlay. The mouse button is used to drag the corners of the bounding-box.
	 * <p>
	 * In addition, a slider panel is shown where the bounding-box size can be modified
	 * as well.
	 * <p>
	 * The bounding box is not connected with any specific {@link bdv.viewer.Source}
	 * that might be currently shown in the BDV; and the coordinates of the bounding box
	 * are specified in the global coordinate system of the BDV.
	 * <p>
	 * In order to transform those global coordinates back to voxel units of
	 * a specific {@link bdv.viewer.Source} one can use the {@link AffineTransform3D}
	 * that can be obtained using {@link bdv.viewer.Source}{@code.getSourceTransform(...)}.
	 * Note that you also have to consider the {@code boundingBoxTransform}...
	 *
	 * @param bdv
	 * @param initialSelection
	 * 					the initially selected interval in global coordinates
	 * @param selectionRange
	 * 					the maximally selectable interval in global coordinates
	 * @param boundingBoxTransform
	 * 					a transform that can be used to, e.g., realize a rotated
	 * 					and/or sheared bounding box
	 */
	public InteractiveRealBoundingBox(
			BdvHandle bdv,
			FinalRealInterval initialSelection,
			FinalRealInterval selectionRange,
			AffineTransform3D boundingBoxTransform )
	{
		this.bdv = bdv;
		this.initialSelection = initialSelection;
		this.selectionRange = selectionRange;
		this.transform = boundingBoxTransform;
	}

	public void show( )
	{
		model = new RealBoundingBoxModel(
				new ModifiableRealInterval( initialSelection ),
				transform );

		final BoundingBoxEditor boxEditor = showInteractiveOverlay( model );

		showSliderPanel( model, boxEditor );
	}

	private BoundingBoxEditor showInteractiveOverlay( RealBoundingBoxModel model )
	{
		final InputTriggerConfig keyConfig = new InputTriggerConfig();
		final Behaviours behaviours = new Behaviours( keyConfig );
		behaviours.install( bdv.getBdvHandle().getTriggerbindings(),  "bounding-box" );

		final BoundingBoxEditor boxEditor = new BoundingBoxEditor(
				keyConfig,
				bdv.getViewerPanel(),
				bdv.getSetupAssignments(),
				bdv.getTriggerbindings(),
				model );

		boxEditor.setPerspective( 1, 1000 );
		boxEditor.setEditable( true );
		return boxEditor;
	}

	private void showSliderPanel(
			RealBoundingBoxModel model,
			BoundingBoxEditor boxEditor )
	{
		final JDialog dialog = new JDialog( new JFrame(  ), "Bounding-box" );
		final RealBoxSelectionPanel realBoxSelectionPanel =
				new RealBoxSelectionPanel( model, selectionRange );
		final BoxModePanel boxModePanel = new BoxModePanel();
		dialog.getContentPane().add( realBoxSelectionPanel, BorderLayout.NORTH );
		dialog.getContentPane().add( boxModePanel, BorderLayout.SOUTH );
		dialog.pack();
		dialog.setDefaultCloseOperation( WindowConstants.HIDE_ON_CLOSE );
		dialog.addComponentListener( new ComponentAdapter()
		{
			@Override
			public void componentShown( final ComponentEvent e )
			{
				boxEditor.install();
			}

			@Override
			public void componentHidden( final ComponentEvent e )
			{
				boxEditor.uninstall();
			}
		} );

		boxModePanel.modeChangeListeners().add( () -> boxEditor.setBoxDisplayMode( boxModePanel.getBoxDisplayMode() ) );

		model.intervalChangedListeners().add( () -> {
			realBoxSelectionPanel.updateSliders( model.getInterval() );
			bdv.getViewerPanel().getDisplay().repaint();
		});

		dialog.setVisible( true );
	}

	public RealInterval getInterval()
	{
		return model.getInterval();
	}

	public Listeners< RealBoundingBoxModel.IntervalChangedListener > intervalChangedListeners()
	{
		return model.intervalChangedListeners();
	}

}
