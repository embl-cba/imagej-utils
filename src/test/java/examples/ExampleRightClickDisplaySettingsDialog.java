package examples;

import bdv.util.*;
import de.embl.cba.bdv.utils.BdvDialogs;
import de.embl.cba.bdv.utils.BdvUtils;
import net.imglib2.RealPoint;
import net.imglib2.img.array.ArrayCursor;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.type.numeric.integer.UnsignedIntType;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

import static de.embl.cba.bdv.utils.BdvUtils.getSourceIndicesAtSelectedPoint;

public class ExampleRightClickDisplaySettingsDialog
{
	public static void main( String[] args )
	{
		// create first source
		final ArrayImg< UnsignedIntType, IntArray > img
				= ArrayImgs.unsignedInts( 256, 256, 1 );
		final ArrayCursor< UnsignedIntType > cursor = img.cursor();
		while ( cursor.hasNext() )
			cursor.next().set( cursor.getIntPosition( 0 ) );

		// show first sources
		final BdvStackSource stackSource = BdvFunctions.show(
				img, "image 1",
				BdvOptions.options().is2D() );

		stackSource.setDisplayRange( 0, 300 );

		// get the bdvHandle
		final BdvHandle bdv = stackSource.getBdvHandle();

		// install right click behaviour
		Behaviours behaviours = new Behaviours( new InputTriggerConfig() );
		behaviours.install( bdv.getTriggerbindings(), "" );
		behaviours.behaviour( ( ClickBehaviour ) ( x, y ) ->
		{
			final RealPoint point = BdvUtils.getGlobalMouseCoordinates( bdv );
			final ArrayList< Integer > indices =
					getSourceIndicesAtSelectedPoint( bdv, point );

			if ( indices.size() == 0 ) return;

			JPanel panel = new JPanel();
			panel.setLayout( new BoxLayout( panel, BoxLayout.PAGE_AXIS ) );

			for ( int index : indices )
			{
				final JPanel settingsPanel = BdvDialogs.getSourceDisplaySettingsPanel(
						bdv,
						index,
						0.0,
						65535.0 );

				panel.add( settingsPanel );
			}

			// display the window at current mouse coordinates
			JFrame frame = new JFrame( "Adjust Display Settings" );
			frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
			frame.setContentPane( panel );
			frame.setBounds(
					MouseInfo.getPointerInfo().getLocation().x,
					MouseInfo.getPointerInfo().getLocation().y,
					120,
					10);
			frame.setResizable( false );
			frame.pack();
			frame.setVisible( true );

		}, "", "ctrl button1" ) ;



	}
}
