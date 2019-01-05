package de.embl.cba.bdv.utils.regions;

import bdv.viewer.Source;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij3d.Content;
import ij3d.Image3DUniverse;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.logic.BitType;
import net.imglib2.view.Views;
import org.scijava.vecmath.Color3f;

import java.util.ArrayList;

public class ConnectedComponents3DViewer
{
	final Source source;

	private boolean isUniverseCreated;
	private Image3DUniverse universe;
	private ImagePlus objectMask;
	private double objectLabel;

	public ConnectedComponents3DViewer( Source source )
	{
		this.source = source;
	}

	public void extractAndShow( RealPoint seed, double resolution )
	{
		createUniverse();

		objectMask = extractObject( seed, resolution );

		createMeshAndDisplay( objectMask );
	}

	private void createMeshAndDisplay( ImagePlus objectMask )
	{
		(new Thread(new Runnable()
		{
			public void run()
			{
				while ( ! isUniverseCreated )
				{
					try
					{
						Thread.sleep( 100 );
					} catch ( InterruptedException e )
					{
						e.printStackTrace();
					}
				}

				//univ.addUniverseListener( new UniverseListener( bdvObjectExtractor ) );

				long start = System.currentTimeMillis();
				final Content content = universe.addMesh( objectMask, new Color3f( 1.0f, 1.0f, 1.0f ), "object", 250, new boolean[]{ true, true, true }, 1 );
				System.out.println( "Computed mesh and created 3D display in [ms]: " + (System.currentTimeMillis() - start) );
			}
		})).start();
	}

	public ImagePlus extractObject( RealPoint coordinate, double resolution )
	{
		final BdvConnectedComponentExtractor cnnExtractor = new BdvConnectedComponentExtractor( source, coordinate, 0 );

		final ArrayList< double[] > calibrations = cnnExtractor.getSourceCalibrations();

		int level;

		for ( level = 0; level < calibrations.size(); level++ )
		{
			if ( calibrations.get( level )[ 0 ] > resolution ) break;
		}

		// duplicate ImagePlus into RAM
		final ImagePlus objectMask = asImagePlus(
				cnnExtractor.getConnectedComponentMask( level ),
				calibrations.get( level ) ).duplicate();

		objectLabel = cnnExtractor.getSeedValue();

		final long executionTimeMillis = cnnExtractor.getExecutionTimeMillis( );
		System.out.println( "Extracted object at resolution [um] " + calibrations.get( level )[ 0 ]
				+ " in " + executionTimeMillis + " ms" );
		return objectMask;
	}

	public void createUniverse()
	{
		isUniverseCreated = false;

		(new Thread(new Runnable()
		{
			public void run()
			{
				final long start = System.currentTimeMillis();
				universe = new Image3DUniverse();
				universe.show();
				isUniverseCreated = true;
				System.out.println( "Universe created in [ms]: " + (System.currentTimeMillis() - start) );
			}
		})).start();
	}

	public static ImagePlus asImagePlus( RandomAccessibleInterval< BitType > mask, double[] voxelSize )
	{
		RandomAccessibleInterval rai = Views.addDimension( mask, 0, 0 );
		rai = Views.permute( rai, 2,3 );
		final ImagePlus imp = ImageJFunctions.wrap( rai, "" );

		final Calibration calibration = new Calibration();
		calibration.pixelWidth = voxelSize[ 0 ];
		calibration.pixelHeight = voxelSize[ 1 ];
		calibration.pixelDepth = voxelSize[ 2 ];
		imp.setCalibration( calibration );

		return imp;
	}


}
