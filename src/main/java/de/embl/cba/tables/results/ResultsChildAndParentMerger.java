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
package de.embl.cba.tables.results;

import ij.measure.ResultsTable;

import java.util.*;
import java.util.stream.IntStream;

public class ResultsChildAndParentMerger
{
	private final ResultsTable childTable;
	private final ResultsTable parentTable;
	private final String childName;
	private final String childTableParentLabelColumn;

	private final HashMap< Integer, Map< String, List< Double > > > parentToFeatureToMeasurements;
	private final HashMap< Integer, Integer > parentToRowIndex;

	public enum AggregationMode
	{
		Mean,
		Sum,
		Max,
		Min
	}

	public ResultsChildAndParentMerger( ResultsTable childTable, ResultsTable parentTable, String childName, String parentLabelColumn )
	{
		this.childTable = childTable;
		this.childName = childName;
		this.childTableParentLabelColumn = parentLabelColumn;
		this.parentTable = parentTable;

		parentToRowIndex = initParentToRowIndex();
		parentToFeatureToMeasurements = initFeatureMap();
		populateFeatureMap( parentToFeatureToMeasurements );
	}

	public ResultsTable appendToParentTable( AggregationMode mode )
	{
		parentToFeatureToMeasurements.keySet().stream().forEach( parent ->
		{
			parentToFeatureToMeasurements.get( parent ).keySet().stream().forEach( measurement ->
			{
				DoubleSummaryStatistics statistics = parentToFeatureToMeasurements.get( parent ).get( measurement ).stream().mapToDouble( x -> x ).summaryStatistics();
				Integer row = parentToRowIndex.get( parent );

				if ( measurement.equals( "Label" ) )
				{
					final String column = childName + "_" + "Count";
					parentTable.setValue( column, row, statistics.getCount() );
				}
				else
				{
					final String column = "" + childName + "_" + mode + "_" + measurement;

					switch ( mode )
					{
						case Mean:
							parentTable.setValue( column, row, statistics.getAverage() );
						case Sum:
							parentTable.setValue( column, row, statistics.getSum() );
						case Max:
							parentTable.setValue( column, row, statistics.getMax() );
						case Min:
							parentTable.setValue( column, row, statistics.getMin() );
					}
				}
			} );
		} );

		return parentTable;
	}

	private HashMap< Integer, Map< String, List< Double > > > initFeatureMap()
	{
		HashMap< Integer, Map< String, List< Double > > > parentToFeatureToMeasurements = new HashMap<>();
		IntStream.range( 0, parentTable.size() ).forEach( row ->
		{
			final HashMap< String, List< Double > > featureToMeasurements = new HashMap<>();
			parentToFeatureToMeasurements.put( getParentLabel( row ), featureToMeasurements );
			Arrays.stream( childTable.getHeadings() ).forEach( column -> featureToMeasurements.put( column, new ArrayList<>() ) );
		} );

		return parentToFeatureToMeasurements;
	}

	private void populateFeatureMap( HashMap< Integer, Map< String, List< Double > > > parentToFeatureToMeasurements )
	{
		Arrays.stream( childTable.getHeadings() ).forEach( column -> {
			IntStream.range( 0, childTable.size() ).forEach( rowIndex ->
			{
				final int parentIndex = ( int ) childTable.getValue( childTableParentLabelColumn, rowIndex );

				if ( parentIndex != 0 )
				{
					if ( column.equals( "Label" ) )
					{
						parentToFeatureToMeasurements.get( parentIndex ).get( column ).add( 1.0D );
					}
					else
					{
						final double measurement = childTable.getValue( column, rowIndex );
						parentToFeatureToMeasurements.get( parentIndex ).get( column ).add( measurement );
					}
				}
				else
				{
					// child object that does not reside within any parent object
				}
			} );
		} );
	}

	private HashMap< Integer, Integer > initParentToRowIndex()
	{
		HashMap< Integer, Integer > parentLabelToRowIndex = new HashMap<>();
		IntStream.range( 0, parentTable.size() ).forEach( row ->
		{
			parentLabelToRowIndex.put( getParentLabel( row ), row );
		});

		return parentLabelToRowIndex;
	}

	private int getParentLabel( int row )
	{
		try
		{
			// when opened from csv file
			return ( int ) parentTable.getValue( "Label", row );
		}
		catch ( Exception e )
		{
			// when obtained from MorpholibJ
			return Integer.parseInt( parentTable.getLabel( row ) );
		}
	}
}
