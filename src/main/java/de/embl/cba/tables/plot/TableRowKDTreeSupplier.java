package de.embl.cba.tables.plot;

import de.embl.cba.tables.Outlier;
import de.embl.cba.tables.Utils;
import de.embl.cba.tables.tablerow.TableRow;
import net.imglib2.KDTree;
import net.imglib2.RealPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class TableRowKDTreeSupplier < T extends TableRow > implements Supplier< KDTree< T  > >
{
	ArrayList< RealPoint > dataPoints;
	private ArrayList< T > dataPointTableRows;

	public TableRowKDTreeSupplier( List< T > tableRows, String colX, String colY )
	{
		initialiseDataPoints( tableRows, colX, colY );
		// TODO rescale dataPoints
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
	 *
	 * @param tableRows
	 * @param colX
	 * @param colY
	 */
	private void initialiseDataPoints( List< T > tableRows, String colX, String colY )
	{
		dataPoints = new ArrayList<>();
		dataPointTableRows = new ArrayList<>( );

		int size = tableRows.size();

		Double x,y;

		for ( int i = 0; i < size; i++ )
		{
			final T tableRow = tableRows.get( i );

			if ( tableRow instanceof Outlier )
				if ( ( ( Outlier ) tableRow ).isOutlier() )  // From plateViewer for Corona screening project
					continue;

			x = Utils.parseDouble( tableRow.getCell( colX ) );
			if ( x.isNaN() || x.isInfinite() ) continue;

			y = Utils.parseDouble( tableRow.getCell( colY ) );
			if ( y.isNaN() || y.isInfinite() ) continue;

			dataPoints.add( new RealPoint( x, y, 0 ) );
			dataPointTableRows.add( tableRow );
		}
	}
}
