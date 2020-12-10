package de.embl.cba.tables.plot;

import de.embl.cba.tables.Outlier;
import de.embl.cba.tables.Utils;
import de.embl.cba.tables.tablerow.TableRow;
import net.imglib2.KDTree;
import net.imglib2.RealPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class TableRowKDTreeSupplier < T extends TableRow > implements Supplier< KDTree< T  > >
{
	final private int n = 2;

	ArrayList< RealPoint > dataPoints;
	private ArrayList< T > dataPointTableRows;
	double[] min = new double[ n ];
	double[] max = new double[ n ];

	public TableRowKDTreeSupplier( List< T > tableRows, String[] columns, double[] scaleFactors )
	{
		Arrays.fill( min, Double.MAX_VALUE );
		Arrays.fill( max, - Double.MAX_VALUE );

		initialiseDataPoints( tableRows, columns, scaleFactors );
	}

	/**
	 * Create a KDTree, using copies of the tableRows and dataPoints,
	 * because the KDTree modifies those lists internally,
	 * which would lead to confusing and concurrency issues.
	 *
	 * @return
	 */
	@Override
	public KDTree< T > get()
	{
		final KDTree< T > kdTree = new KDTree<>( new ArrayList<>( dataPointTableRows ), new ArrayList<>( dataPoints ) );
		return kdTree;
	}

	/**
	 * Some tableRows contain entries that cannot be plotted (e.g., NaN or Inf).
	 * Also, some tableRows can be marked as outliers.
	 * Here we subset for all valid tableRows and determine the corresponding coordinates.
	 *  @param tableRows
	 * @param columns
	 * @param scaleFactors
	 */
	private void initialiseDataPoints( List< T > tableRows, String[] columns, double[] scaleFactors )
	{
		dataPoints = new ArrayList<>();
		dataPointTableRows = new ArrayList<>( );

		int size = tableRows.size();

		Double[] xy = new Double[ 2 ];

		for ( int i = 0; i < size; i++ )
		{
			final T tableRow = tableRows.get( i );

			if ( tableRow instanceof Outlier )
				if ( ( ( Outlier ) tableRow ).isOutlier() )  // From plateViewer for Corona screening project
					continue;

			for ( int d = 0; d < n; d++ )
			{
				xy[ d ] = Utils.parseDouble( tableRow.getCell( columns[ d ] ) );
				if ( xy[ d ].isNaN() || xy[ d ].isInfinite() ) continue;

				xy[ d ] *= scaleFactors[ d ];

				if ( xy[ d ] < min[ d ] ) min[ d ] = xy[ d ];
				if ( xy[ d ] > max[ d ] ) max[ d ] = xy[ d ];
			}

			dataPoints.add( new RealPoint( xy[ 0 ], xy[ 1 ] ) );
			dataPointTableRows.add( tableRow );
		}

		if ( dataPoints.size() == 0 )
			throw new UnsupportedOperationException( "Cannot create scatter plot, because there is no valid data point." );
	}

	public double[] getMin()
	{
		return min;
	}

	public double[] getMax()
	{
		return max;
	}
}
