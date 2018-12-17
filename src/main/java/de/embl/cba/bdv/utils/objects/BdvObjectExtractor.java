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

	private RandomAccessibleInterval objectMask;
	private ArrayList< double[] > calibrations;
	private long executionTimesMillis;

	private boolean isDone;
	private boolean isInterrupted;

	private int numMipmapLevels;
	private final Source labelsSource;
	private double objectLabel;

	public BdvObjectExtractor( Bdv bdv, RealPoint coordinates, int timePoint )
	{
		this.bdv = bdv;
		this.coordinates = coordinates;
		this.timePoint = timePoint;
		this.calibrations = new ArrayList<>(  );
		this.isDone = false;
		this.isInterrupted = false;

		labelsSource = BdvUtils.getFirstVisibleLabelsSource( bdv );

		if ( labelsSource == null )
		{
			System.out.println("ERROR: no labels sources found in bdv");
			return;
		}

		numMipmapLevels = labelsSource.getNumMipmapLevels();

		for ( int level = 0; level < numMipmapLevels; ++level )
		{
			calibrations.add( BdvUtils.getCalibration( labelsSource, level ) );
		}
	}

	public RandomAccessibleInterval extractObjectMask( int level )
	{
		final long currentTimeMillis = System.currentTimeMillis();

		final RandomAccessibleInterval< IntegerType > indexImg = BdvUtils.getIndexImg( labelsSource, timePoint, level );

		final long[] positionInSourceStack = BdvUtils.getPositionInSource( labelsSource, coordinates, timePoint, level );

		final RegionExtractor regionExtractor = new RegionExtractor( indexImg, new DiamondShape( 1 ), 1000 * 1000 * 1000L );

		regionExtractor.run( positionInSourceStack );

		objectMask = regionExtractor.getCroppedRegionMask();

		objectLabel = regionExtractor.getSeedValue();

		executionTimesMillis = System.currentTimeMillis() - currentTimeMillis;

		return objectMask;
	}

	public ArrayList< double[] > getCalibrations( )
	{
		return calibrations;
	}

	public long getExecutionTimeMillis( )
	{
		return executionTimesMillis;
	}

	public double getObjectLabel()
	{
		return objectLabel;
	}

	public void stop()
	{
		isInterrupted = true;
	}

}
