package de.embl.cba.bdv.utils.objects;

import bdv.util.Bdv;
import bdv.viewer.Source;
import de.embl.cba.bdv.utils.BdvUtils;
import de.embl.cba.bdv.utils.algorithms.RegionExtractor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.algorithm.neighborhood.DiamondShape;
import net.imglib2.type.numeric.IntegerType;

import java.util.ArrayList;


public class BdvObjectExtractor
{
	final Bdv bdv;
	final RealPoint coordinates;
	final int timePoint;

	private ArrayList< RandomAccessibleInterval > objectMasks;
	private ArrayList<  double[] > calibrations;
	private boolean isDone;

	public BdvObjectExtractor( Bdv bdv, RealPoint coordinates, int timePoint )
	{
		this.bdv = bdv;
		this.coordinates = coordinates;
		this.timePoint = timePoint;
		this.objectMasks = new ArrayList<>(  );
		this.calibrations = new ArrayList<>(  );
		this.isDone = false;
	}

	public RandomAccessibleInterval getObjectMask( int level )
	{
		return objectMasks.get( level );
	}

	public double[] getCalibration( int level )
	{
		return calibrations.get( level );
	}

	public boolean isLevelAvailable( int level )
	{
		if ( objectMasks.size() > level )
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public boolean isDone()
	{
		return isDone;
	}

	public void run()
	{
		isDone = false;

		Source labelsSource = BdvUtils.getFirstVisibleLabelsSource( bdv );

		final int numMipmapLevels = labelsSource.getNumMipmapLevels();

		for ( int level = numMipmapLevels - 1; level >=0; --level )
		{
			final RandomAccessibleInterval< IntegerType > indexImg = BdvUtils.getIndexImg( labelsSource, timePoint, level );

			final long[] positionInSourceStack = BdvUtils.getPositionInSource( labelsSource, coordinates, timePoint, level );

			final RegionExtractor regionExtractor = new RegionExtractor( indexImg, new DiamondShape( 1 ), 1000 * 1000 * 1000L );

			regionExtractor.run( positionInSourceStack );

			objectMasks.add( regionExtractor.getCroppedRegionMask() );

			calibrations.add( BdvUtils.getCalibration( labelsSource, level ) );
		}

		isDone = true;

	}

}
