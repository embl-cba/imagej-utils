/*-
 * #%L
 * Various Java code for ImageJ
 * %%
 * Copyright (C) 2018 - 2021 EMBL
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
package de.embl.cba.tables.imagesegment;

import de.embl.cba.tables.Utils;
import de.embl.cba.tables.tablerow.AbstractTableRow;
import de.embl.cba.tables.tablerow.TableRow;
import de.embl.cba.tables.tablerow.TableRowImageSegment;
import net.imglib2.FinalRealInterval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * All values are set immediately
 * (unlike in the ColumnBasedTableRowImageSegment)
 */
public class DefaultTableRowImageSegment extends AbstractTableRow implements TableRowImageSegment, TableRow
{
	private final Map< String, String > cells;
	private final Map< SegmentProperty, List< String > > segmentPropertyToColumn;
	private double[] position;
	FinalRealInterval boundingBox;
	private boolean isOneBasedTimePoint;
	private float[] mesh;
	private int timePoint;
	private Double labelId;
	private String imageId;

	public DefaultTableRowImageSegment(
			int rowIndex,
			Map< String, List< String > > columns,
			Map< SegmentProperty, List< String > > segmentPropertyToColumn,
			boolean isOneBasedTimePoint )
	{
		this.segmentPropertyToColumn = segmentPropertyToColumn;
		this.isOneBasedTimePoint = isOneBasedTimePoint;

		this.cells = new LinkedHashMap<>();

		// set segment properties
		setLabelId( rowIndex );
		setImageId( rowIndex );
		setTimePoint( rowIndex );
		setPosition( rowIndex );
		initBoundingBox( rowIndex );

		// set cells
		final List< String > columnNames = new ArrayList<>( columns.keySet() );
		Collections.sort( columnNames );
		for ( String column : columnNames )
		{
			cells.put( column, columns.get( column ).get( rowIndex ) );
		}
	}

	private void setPosition( int row )
	{
		if ( position != null ) return;

		position = new double[ 3 ];

		if ( segmentPropertyToColumn.containsKey( SegmentProperty.X ) )
			position[ 0 ] = Utils.parseDouble(
								segmentPropertyToColumn
								.get( SegmentProperty.X )
								.get( row ) );

		if ( segmentPropertyToColumn.containsKey( SegmentProperty.Y ) )
			position[ 1 ] = Utils.parseDouble(
								segmentPropertyToColumn
								.get( SegmentProperty.Y )
								.get( row ) );

		if ( segmentPropertyToColumn.containsKey( SegmentProperty.Z ) )
			position[ 2 ] = Utils.parseDouble(
								segmentPropertyToColumn
								.get( SegmentProperty.Z )
								.get( row ) );
	}

	public void setImageId( int row )
	{
		imageId = segmentPropertyToColumn
				.get( SegmentProperty.LabelImage )
				.get( row );
	}

	@Override
	public String imageId()
	{
		return imageId;
	}

	private void setLabelId( int row )
	{
		labelId = Utils.parseDouble( segmentPropertyToColumn
				.get( SegmentProperty.ObjectLabel )
				.get( row ) );
	}

	@Override
	public double labelId()
	{
		return labelId;
	}

	private void setTimePoint( int row )
	{
		if ( segmentPropertyToColumn.get( SegmentProperty.T ) == null )
		{
			timePoint = 0;
		}
		else
		{
			timePoint = Utils.parseDouble( segmentPropertyToColumn.get( SegmentProperty.T )
					.get( row ) ).intValue();

			if ( isOneBasedTimePoint ) timePoint -= 1;
		}
	}

	@Override
	public int timePoint()
	{
		return timePoint;
	}

	@Override
	public FinalRealInterval boundingBox()
	{
		return boundingBox;
	}

	@Override
	public void setBoundingBox( FinalRealInterval boundingBox )
	{
		this.boundingBox = boundingBox;
	}

	@Override
	public float[] getMesh()
	{
		return mesh;
	}

	@Override
	public void setMesh( float[] mesh )
	{
		this.mesh = mesh;
	}

	private void initBoundingBox( int row )
	{
		// TODO: this checking needs improvement...
		if ( ! segmentPropertyToColumn.containsKey( SegmentProperty.BoundingBoxXMin ) )
		{
			boundingBox = null;
			return;
		}

		final double[] min = getBoundingBoxMin( row );
		final double[] max = getBoundingBoxMax( row );

		boundingBox = new FinalRealInterval( min, max );
	}

	private double[] getBoundingBoxMax( int row )
	{
		final double[] max = new double[ numDimensions() ];

		if ( segmentPropertyToColumn.containsKey( SegmentProperty.BoundingBoxXMax ) )
			max[ 0 ] = Utils.parseDouble(
					segmentPropertyToColumn
							.get( SegmentProperty.BoundingBoxXMax )
							.get( row ) );

		if ( segmentPropertyToColumn.containsKey( SegmentProperty.BoundingBoxYMax ) )
			max[ 1 ] = Utils.parseDouble(
					segmentPropertyToColumn
							.get( SegmentProperty.BoundingBoxYMax )
							.get( row ) );

		if ( segmentPropertyToColumn.containsKey( SegmentProperty.BoundingBoxZMax ) )
			max[ 2 ] = Utils.parseDouble(
					segmentPropertyToColumn
							.get( SegmentProperty.BoundingBoxZMax )
							.get( row ) );
		return max;
	}

	private double[] getBoundingBoxMin( int row )
	{
		final double[] min = new double[ numDimensions() ];

		if ( segmentPropertyToColumn.containsKey( SegmentProperty.BoundingBoxXMin ) )
			min[ 0 ] = Utils.parseDouble(
							segmentPropertyToColumn
								.get( SegmentProperty.BoundingBoxXMin )
								.get( row ) );

		if ( segmentPropertyToColumn.containsKey( SegmentProperty.BoundingBoxYMin ) )
			min[ 1 ] = Utils.parseDouble(
					segmentPropertyToColumn
							.get( SegmentProperty.BoundingBoxYMin )
							.get( row ) );

		if ( segmentPropertyToColumn.containsKey( SegmentProperty.BoundingBoxZMin ) )
			min[ 2 ] = Utils.parseDouble(
					segmentPropertyToColumn
							.get( SegmentProperty.BoundingBoxZMin )
							.get( row ) );
		return min;
	}

	@Override
	public String getCell( String columnName )
	{
		return cells.get( columnName );
	}

	@Override
	public void setCell( String columnName, String value )
	{
		cells.put( columnName, value );
		this.notifyCellChangedListeners( columnName, value );
	}

	@Override
	public Set< String > getColumnNames()
	{
		return cells.keySet();
	}

	@Override
	@Deprecated
	public int rowIndex()
	{
		return -1;
	}

	@Override
	public void localize( float[] position )
	{
		for ( int d = 0; d < 3; d++ )
			position[ d ] = (float) this.position[ d ];
	}

	@Override
	public void localize( double[] position )
	{
		for ( int d = 0; d < 3; d++ )
			position[ d ] = this.position[ d ];
	}

	@Override
	public float getFloatPosition( int d )
	{
		return (float) position[ d ];
	}

	@Override
	public double getDoublePosition( int d )
	{
		return position[ d ];
	}

	@Override
	public int numDimensions()
	{
		return position.length;
	}
}
