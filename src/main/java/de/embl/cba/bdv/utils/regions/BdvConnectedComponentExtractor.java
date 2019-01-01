package de.embl.cba.bdv.utils.regions;

import bdv.tools.transformation.TransformedSource;
import bdv.util.Bdv;
import bdv.viewer.Source;
import de.embl.cba.bdv.utils.BdvUtils;
import de.embl.cba.bdv.utils.algorithms.ConnectedComponentExtractor;
import de.embl.cba.bdv.utils.argbconversion.ARGBConvertedRealTypeSpimDataSource;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.algorithm.neighborhood.DiamondShape;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;

import java.util.ArrayList;


public class BdvConnectedComponentExtractor < R extends RealType< R > >
{
	private final Source source;
	final RealPoint xyz;
	final int t;

	private RandomAccessibleInterval< BitType > connectedComponentMask;
	private ArrayList< double[] > calibrations;
	private long executionTimesMillis;

	private int numMipmapLevels;
	private double seedValue;

	public BdvConnectedComponentExtractor( Source source, RealPoint xyz, int t )
	{
		this.source = source;
		this.xyz = xyz;
		this.t = t;

		this.calibrations = new ArrayList<>(  );

		if ( this.source == null )
		{
			System.out.println("ERROR: no argbconversion sources found in bdv");
			return;
		}

		numMipmapLevels = this.source.getNumMipmapLevels();

		for ( int level = 0; level < numMipmapLevels; ++level )
		{
			calibrations.add( BdvUtils.getCalibration( this.source, level ) );
		}
	}

	public RandomAccessibleInterval< BitType > getConnectedComponentMask( int level )
	{
		final long currentTimeMillis = System.currentTimeMillis();

		final long[] positionInSourceStack = BdvUtils.getPositionInSource( source, xyz, t, level );

		final RandomAccessibleInterval< R > rai = getRAI( t, level );

		final ConnectedComponentExtractor connectedComponentExtractor = new ConnectedComponentExtractor( rai, new DiamondShape( 1 ), 1000 * 1000 * 1000L );

		connectedComponentExtractor.run( positionInSourceStack );

		seedValue = connectedComponentExtractor.getSeedValue();

		connectedComponentMask = connectedComponentExtractor.getCroppedRegionMask();

		executionTimesMillis = System.currentTimeMillis() - currentTimeMillis;

		return connectedComponentMask;
	}

	private RandomAccessibleInterval< R > getRAI( int t, int level )
	{
		if ( source instanceof TransformedSource )
		{
			final Source wrappedSource = ( ( TransformedSource ) source ).getWrappedSource();

			if ( wrappedSource instanceof ARGBConvertedRealTypeSpimDataSource )
			{
				return ( ( ARGBConvertedRealTypeSpimDataSource ) wrappedSource ).getWrappedRealTypeRandomAccessibleInterval( t, level );
			}
		}
		else if ( source.getType() instanceof RealType )
		{
			return source.getSource( t, level );
		}
		else
		{
			return null;
		}

		return null;

	}

	public ArrayList< double[] > getSourceCalibrations( )
	{
		return calibrations;
	}

	public long getExecutionTimeMillis( )
	{
		return executionTimesMillis;
	}

	public double getSeedValue()
	{
		return seedValue;
	}

}
