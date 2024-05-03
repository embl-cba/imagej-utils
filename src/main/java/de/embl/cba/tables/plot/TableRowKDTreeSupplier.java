/*-
 * #%L
 * Various Java code for ImageJ
 * %%
 * Copyright (C) 2018 - 2024 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package de.embl.cba.tables.plot;

import de.embl.cba.tables.Outlier;
import de.embl.cba.tables.Utils;
import de.embl.cba.tables.tablerow.TableRow;
import de.embl.cba.transforms.utils.ExecutableClass;
import net.imglib2.KDTree;
import net.imglib2.RealPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * Note: Turns out this does not really need to be a Supplier as
 * we only need one KDTree and not multiple, but it also does not
 * hurt, so I left it like this.
 *
 * @param <T>
 */
public class TableRowKDTreeSupplier < T extends TableRow > implements Supplier< KDTree< T > >
{
	final private int n = 2;

	private ArrayList< RealPoint > dataPoints;
	private ArrayList< T > dataPointTableRows;
	private Map< T, RealPoint > tableRowToRealPoint;
	double[] min = new double[ n ];
	double[] max = new double[ n ];
	private HashMap< String, Double > string2num;

	public TableRowKDTreeSupplier( List< T > tableRows, String[] columns, double[] scaleFactors )
	{
		Arrays.fill( min, Double.MAX_VALUE );
		Arrays.fill( max, - Double.MAX_VALUE );

		initialiseDataPoints( tableRows, columns, scaleFactors );

		tableRowToRealPoint = IntStream.range( 0, dataPoints.size() ).boxed()
				.collect( Collectors.toMap( i -> dataPointTableRows.get( i ), i -> dataPoints.get( i )));
	}

	/**
	 * Create a KDTree, using copies of the tableRows and dataPoints,
	 * because the KDTree modifies those lists internally,
	 * which would lead to confusion and concurrency issues.
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
		string2num = new HashMap<>(); // in case we need to plot categorical columns
		dataPoints = new ArrayList<>();
		dataPointTableRows = new ArrayList<>( );

		int size = tableRows.size();

		Double[] xy = new Double[ 2 ];
		boolean isValidDataPoint;

		for ( int i = 0; i < size; i++ )
		{
			final T tableRow = tableRows.get( i );

			if ( tableRow instanceof Outlier )
				if ( ( ( Outlier ) tableRow ).isOutlier() )  // From plateViewer for Corona screening project
					continue;

			isValidDataPoint = true;
			for ( int d = 0; d < n; d++ )
			{
				final String cell = tableRow.getCell( columns[ d ] );

				try
				{
					xy[ d ] = Utils.parseDouble( cell );
				}
				catch ( Exception e )
				{
					if ( ! string2num.containsKey( cell ) )
					{
						string2num.put( cell, Double.valueOf( string2num.size() ) );
					}

					xy[ d ] =  string2num.get( cell );
				}

				if ( xy[ d ].isNaN() || xy[ d ].isInfinite() )
				{
					isValidDataPoint = false;
					break;
				}

				xy[ d ] *= scaleFactors[ d ];

				if ( xy[ d ] < min[ d ] ) min[ d ] = xy[ d ];
				if ( xy[ d ] > max[ d ] ) max[ d ] = xy[ d ];
			}

			if ( isValidDataPoint )
			{
				dataPoints.add( new RealPoint( xy[ 0 ], xy[ 1 ] ) );
				dataPointTableRows.add( tableRow );
			}
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

	public Map< T, RealPoint > getTableRowToRealPoint()
	{
		return tableRowToRealPoint;
	}
}
