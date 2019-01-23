import bdv.util.BdvFunctions;
import bdv.util.BdvHandle;
import bdv.util.BdvOptions;
import de.embl.cba.bdv.utils.boundingbox.*;
import de.embl.cba.transforms.utils.Transforms;
import net.imglib2.*;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.Views;

import java.util.Random;

public class RealBoundingBoxExample
{
	public static void main( String[] args )
	{

		/*
		 * Prepare image, bdv and selection ranges
		 */

		final int sourceSize = 100;

		final double[] calibration = new double[]{0.1,0.1,0.1};

		final AffineTransform3D imageTransform = new AffineTransform3D();

		for ( int d = 0; d < 3; d++ )
		{
			imageTransform.set( calibration[ d ], d, d );
		}

		final double[] sourceCenter = new double[ 3 ];
		for ( int d = 0; d < 3; d++ )
		{
			sourceCenter[ d ] = sourceSize / calibration[ d ];
		}

		double[] initialMin = new double[]{ 30, 30, 30 };
		double[] initialMax = new double[]{ 80, 80, 80 };

		double[] rangeMin = new double[]{ 0, 0, 0 };
		double[] rangeMax = new double[]{ sourceSize, sourceSize, sourceSize };

		imageTransform.apply( initialMin, initialMin );
		imageTransform.apply( initialMax, initialMax );
		imageTransform.apply( rangeMin, rangeMin );
		imageTransform.apply( rangeMax, rangeMax );

		final FinalRealInterval initialSelection = new FinalRealInterval( initialMin, initialMax );

		final FinalRealInterval selectionRange = new FinalRealInterval( rangeMin, rangeMax );

		final RandomAccessibleInterval< IntType > image = createRandomImage( sourceSize );

		final BdvHandle bdv = BdvFunctions.show( image, "",
				BdvOptions.options().sourceTransform( imageTransform )).getBdvHandle();


		/*
		 * Bounding box
		 */

		AffineTransform3D bbTransform = new AffineTransform3D();


		bbTransform = Transforms.rotationAroundIntervalCenterTransform(
				45,
				2,
				initialSelection
				);


		final InteractiveRealBoundingBox boundingBox = new InteractiveRealBoundingBox(
				bdv,
				initialSelection,
				selectionRange,
				bbTransform );

		boundingBox.show();

		boundingBox.intervalChangedListeners().add( () -> {
			/*
			 TODO: for users the interval without the bbTransform is
			 quite useless, maybe also return something else?
			 an ROI?
			  */

			boundingBox.getInterval();
		});


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
