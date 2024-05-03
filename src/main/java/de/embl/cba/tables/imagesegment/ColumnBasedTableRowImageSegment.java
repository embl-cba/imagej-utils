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
package de.embl.cba.tables.imagesegment;

import de.embl.cba.tables.Utils;
import de.embl.cba.tables.imagesegment.SegmentProperty;
import de.embl.cba.tables.tablerow.AbstractTableRow;
import de.embl.cba.tables.tablerow.ColumnBasedTableRow;
import de.embl.cba.tables.tablerow.TableRowImageSegment;
import net.imglib2.FinalRealInterval;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * All values are dynamically fetched from the columns.
 * This might be slow, but allows changes in columns to be reflected.
 * // TODO: make interface for ColumnBasedTableRow
 */
public class ColumnBasedTableRowImageSegment extends AbstractTableRow implements TableRowImageSegment, ColumnBasedTableRow
{
	private final int row;
	private final Map< String, List< String > > columns;
	private final Map< SegmentProperty, List< String > > segmentPropertyToColumn;
	private double[] position;
	FinalRealInterval boundingBox;
	private boolean isOneBasedTimePoint;
	private float[] mesh;

	public ColumnBasedTableRowImageSegment(
			int row,
			Map< String, List< String > > columns,
			Map< SegmentProperty, List< String > > segmentPropertyToColumn,
			boolean isOneBasedTimePoint )
	{
		this.row = row;
		this.columns = columns;
		this.segmentPropertyToColumn = segmentPropertyToColumn;
		this.isOneBasedTimePoint = isOneBasedTimePoint;
	}

	public Map< String, List< String > > getColumns()
	{
		return columns;
	}

	private synchronized void setPosition()
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

	@Override
	public String imageId()
	{
		return segmentPropertyToColumn
				.get( SegmentProperty.LabelImage )
				.get( row );
	}

	@Override
	public double labelId()
	{
		return Utils.parseDouble( segmentPropertyToColumn
				.get( SegmentProperty.ObjectLabel )
				.get( row ) );
	}

	@Override
	public int timePoint()
	{
		if ( segmentPropertyToColumn.get( SegmentProperty.T ) == null )
		{
			return 0;
		}
		else
		{
			int timePoint = Utils.parseDouble( segmentPropertyToColumn.get( SegmentProperty.T )
					.get( row ) ).intValue();

			if ( isOneBasedTimePoint ) timePoint -= 1;

			return timePoint;
		}
	}

	@Override
	public FinalRealInterval boundingBox()
	{
		if ( boundingBox == null )
			setBoundingBoxFromTableRow();

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

	private void setBoundingBoxFromTableRow()
	{
		// TODO: this checking needs improvement...
		if ( ! segmentPropertyToColumn.containsKey( SegmentProperty.BoundingBoxXMin ) )
		{
			boundingBox = null;
			return;
		}

		final double[] min = getBoundingBoxMin();
		final double[] max = getBoundingBoxMax();

		boundingBox = new FinalRealInterval( min, max );
	}

	private double[] getBoundingBoxMax()
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

	private double[] getBoundingBoxMin()
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
		return columns.get( columnName ).get( row );
	}

	@Override
	public void setCell( String columnName, String value )
	{
		columns.get( columnName ).set( row, value );
		this.notifyCellChangedListeners( columnName, value );
	}

	@Override
	public Set< String > getColumnNames()
	{
		return columns.keySet();
	}

	@Override
	@Deprecated
	public int rowIndex()
	{
		return row;
	}

	@Override
	public synchronized void localize( float[] position )
	{
		setPosition();

		for ( int d = 0; d < 3; d++ )
			position[ d ] = (float) this.position[ d ];
	}

	@Override
	public synchronized void localize( double[] position )
	{
		setPosition();

		for ( int d = 0; d < 3; d++ )
			position[ d ] = this.position[ d ];
	}

	@Override
	public synchronized float getFloatPosition( int d )
	{
		setPosition();
		return (float) position[ d ];
	}

	@Override
	public synchronized double getDoublePosition( int d )
	{
		setPosition();
		return position[ d ];
	}

	@Override
	public int numDimensions()
	{
		setPosition();
		return position.length;
	}
}
