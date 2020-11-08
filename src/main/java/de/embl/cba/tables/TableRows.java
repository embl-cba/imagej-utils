/*-
 * #%L
 * TODO
 * %%
 * Copyright (C) 2018 - 2020 EMBL
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
package de.embl.cba.tables;

import de.embl.cba.tables.imagesegment.ColumnBasedTableRowImageSegment;
import de.embl.cba.tables.tablerow.TableRow;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.util.*;

public abstract class TableRows
{
	public static < T extends TableRow >
	void addColumn( List< T > tableRows, String columnName, Object[] values )
	{
		if ( tableRows.get( 0 ) instanceof ColumnBasedTableRowImageSegment )
		{
			final Map< String, List< String > > columns
					= ( ( ColumnBasedTableRowImageSegment ) tableRows.get( 0 ) ).getColumns();

			final ArrayList< String > strings = new ArrayList<>();
			for ( int i = 0; i < values.length; i++ )
				strings.add( values[ i ].toString() );

			columns.put( columnName, strings );
		}
		else
		{
			throw new UnsupportedOperationException(
					"TableRow class not supported yet: " + tableRows.get( 0 ).getClass());
		}
	}

	public static < T extends TableRow >
	void addColumn( List< T > tableRows, String columnName, Object value )
	{
		final Object[] values = new Object[ tableRows.size() ];
		Arrays.fill( values, value );

		addColumn( tableRows, columnName, values );
	}

	@Deprecated
	public static < T extends TableRow >
	void assignValues(
			final String column,
			final Set< T > rows,
			final String value,
			JTable table )
	{
		for ( T row : rows )
			assignValue( column, row, value, table );
	}

	/**
	 * Write the values both in the TableRows and JTable
	 *
	 * TODO: this should not have to do it also in the table model.
	 * somehow there should be a notification that the values in the table row have changed.
	 * and then the TableView should take care of this!!
	 *
	 * @param column
	 * @param row
	 * @param attribute
	 * @param table
	 */
	@Deprecated
	public static  < T extends TableRow >
	void assignValue( String column,
					  T row,
					  String attribute,
					  JTable table )
	{
		// TODO: this should happen inside TableView
		final TableModel model = table.getModel();
		final int columnIndex = table.getColumnModel().getColumnIndex( column );

		final Object valueToBeReplaced = model.getValueAt(
				row.rowIndex(),
				columnIndex
		);

		if ( valueToBeReplaced.getClass().equals( Double.class ) )
		{
			try
			{
				final double number = Utils.parseDouble( attribute );

				model.setValueAt(
						number,
						row.rowIndex(),
						columnIndex );

				row.setCell( column, attribute );
			}
			catch ( Exception e )
			{
				Logger.error( "Entered value must be numeric for column: "
						+ column );
			}
		}
		else
		{
			model.setValueAt(
					attribute,
					row.rowIndex(),
					columnIndex );

			row.setCell( column, attribute );
		}
	}

	/**
	 * Write the values both in the TableRows and JTable
	 *
	 * TODO: this should not have to do it also in the table model.
	 * somehow there should be a notification that the values in the table row have changed.
	 * and then the TableView should take care of this!!
	 *
	 * @param rowIndex
	 * @param column
	 * @param value
	 * @param table
	 */
	public static < T extends TableRow >
	void setTableCell(
			int rowIndex,
			String column,
			String value,
			JTable table )
	{
		final TableModel model = table.getModel();
		final int columnIndex = table.getColumnModel().getColumnIndex( column );

		final Object valueToBeReplaced = model.getValueAt( rowIndex, columnIndex );

		if ( valueToBeReplaced.getClass().equals( Double.class ) )
		{
			try
			{
				final double number = Utils.parseDouble( value );

				model.setValueAt(
						number,
						rowIndex,
						columnIndex );
			}
			catch ( Exception e )
			{
				Logger.error( "Entered value must be numeric for column: " + column );
			}
		}
		else
		{
			model.setValueAt(
					value,
					rowIndex,
					columnIndex );
		}
	}
}
