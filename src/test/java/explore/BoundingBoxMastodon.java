package explore;

import bdv.tools.ToggleDialogAction;
import bdv.tools.boundingbox.BoxSelectionPanel;
import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvHandle;
import bdv.util.ModifiableInterval;
import de.embl.cba.bdv.utils.boundingbox.BoundingBoxEditor;
import de.embl.cba.bdv.utils.boundingbox.BoxModePanel;
import de.embl.cba.bdv.utils.boundingbox.DefaultBoundingBoxModel;
import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.Views;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.Behaviours;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Random;

public class BoundingBoxMastodon
{
	public static void main( String[] args )
	{
		final int sourceSize = 100;
		final RandomAccessibleInterval< IntType > image = createRandomImage( sourceSize );
		final BdvHandle bdv = BdvFunctions.show( image, "" ).getBdvHandle();

		/*
		 * Initialize bounding box model from the computed interval.
		 */
		final FinalInterval interval = new FinalInterval( new long[]{ 30, 30, 30 }, new long[]{ 80, 80, 80 } );

		final DefaultBoundingBoxModel model = new DefaultBoundingBoxModel(
				new ModifiableInterval( interval ),
				new AffineTransform3D() );

		/*
		 * Create bounding box overlay and editor.
		 */

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

		/*
		 * Create bounding box dialog.
		 */

		final JDialog dialog = new JDialog( new JFrame(  ), "Bounding-box" );
		final BoxSelectionPanel boxSelectionPanel = new BoxSelectionPanel( model, image );
		final BoxModePanel boxModePanel = new BoxModePanel();
		dialog.getContentPane().add( boxSelectionPanel, BorderLayout.NORTH );
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
			boxSelectionPanel.updateSliders( model.getInterval() );
			bdv.getViewerPanel().getDisplay().repaint();
		});

		dialog.setVisible( true );

		/*
		 * Install a action to toggle the dialog
		 */
//		final Actions actions = new Actions( keyconf, "bbtest" );
//		actions.install( bdvHandle.getKeybindings(), "bbtest" );
//		actions.namedAction( new ToggleDialogAction( TOGGLE_BOUNDING_BOX, dialog ), TOGGLE_BOUNDING_BOX_KEYS );

	}

	public static RandomAccessibleInterval< IntType > createRandomImage( int numVoxels )
	{
		final RandomAccessibleInterval< IntType > ints = ArrayImgs.ints( numVoxels, numVoxels, numVoxels );
		final Cursor< IntType > cursor = Views.iterable( ints ).cursor();
		final Random random = new Random();
		while (cursor.hasNext() ) cursor.next().set( random.nextInt( 65535 ) );
		return ints;
	}
}
