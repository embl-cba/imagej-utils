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
package de.embl.cba.tables;

import de.embl.cba.tables.tablerow.TableRow;
import ij.measure.ResultsTable;

import javax.activation.UnsupportedDataTypeException;
import javax.swing.*;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TableColumns
{
	public static Map< String, List< String > > convertResultsTableToColumns( ResultsTable resultsTable )
	{
		List< String > columnNames = Arrays.asList( resultsTable.getHeadings() );
		final int numRows = resultsTable.size();

		final Map< String, List< String > > columnNamesToValues = new LinkedHashMap<>();

		for ( String columnName : columnNames )
		{
			System.out.println( "Parsing column: " + columnName );

			final double[] columnValues = getColumnValues( resultsTable, columnName );

			final List< String > list = new ArrayList<>( );
			for ( int row = 0; row < numRows; ++row )
				list.add( Utils.toStringWithoutSuperfluousDecimals( columnValues[ row ] ) );

			columnNamesToValues.put( columnName, list );
		}

		return columnNamesToValues;
	}

	private static double[] getColumnValues( ResultsTable table, String heading )
	{
		String[] allHeaders = table.getHeadings();

		// Check if column header corresponds to row label header
		boolean hasRowLabels = hasRowLabelColumn(table);
		if (hasRowLabels && heading.equals(allHeaders[0]))
		{
			// need to parse row label column
			int nr = table.size();
			double[] values = new double[nr];
			for (int r = 0; r < nr; r++)
			{
				String label = table.getLabel(r);
				values[r] = Utils.parseDouble(label);
			}
			return values;
		}

		// determine index of column
		int index = table.getColumnIndex(heading);
		if ( index == ResultsTable.COLUMN_NOT_FOUND )
		{
			throw new RuntimeException("Unable to find column index from header: " + heading);
		}
		return table.getColumnAsDoubles(index);
	}

	private static final boolean hasRowLabelColumn( ResultsTable table )
	{
		return table.getLastColumn() == (table.getHeadings().length-2);
	}

	public static Map< String, List< String > >
	stringColumnsFromTableFile( final String path )
	{
		return stringColumnsFromTableFile( path, null );
	}

	public static Map< String, List< String > > stringColumnsFromTableFile( final String path, String delim )
	{
		final List< String > tableRowsIncludingHeader = Tables.readRows( path );

		delim = Tables.autoDelim( delim, tableRowsIncludingHeader );

		List< String > columnNames = Tables.getColumnNames( tableRowsIncludingHeader, delim );

		final Map< String, List< String > > columnNameToStrings = new LinkedHashMap<>();

		final int numColumns = columnNames.size();

		for ( int columnIndex = 0; columnIndex < numColumns; columnIndex++ )
		{
			final String columnName = columnNames.get( columnIndex );
			columnNameToStrings.put( columnName, new ArrayList<>( ) );
		}

		final int numRows = tableRowsIncludingHeader.size() - 1;

		final long start = System.currentTimeMillis();

		for ( int row = 1; row <= numRows; ++row )
		{
			final String[] split = tableRowsIncludingHeader.get( row ).split( delim );
			for ( int columnIndex = 0; columnIndex < numColumns; columnIndex++ )
			{
				columnNameToStrings.get( columnNames.get( columnIndex ) ).add( split[ columnIndex ].replace( "\"", "" ) );
			}
		}

		// System.out.println( ( System.currentTimeMillis() - start ) / 1000.0 ) ;

		return columnNameToStrings;
	}

	public static Map< String, List< String > >
	orderedStringColumnsForMerging(
			String delim, // can be null
			Map< String, List< String > > referenceColumns, // length of list corresponds to target table
			List< String > tableRowsIncludingHeader )
	{
		delim = Tables.autoDelim( delim, tableRowsIncludingHeader );

		List< String > columnNames = Tables.getColumnNames( tableRowsIncludingHeader, delim );

		final Map< String, List< String > > columnNameToStrings = new LinkedHashMap<>();

		int mergeByColumnIndex = -1;

		final int numRowsTargetTable = referenceColumns.values().iterator().next().size();
		final int numColumns = columnNames.size();

		for ( int columnIndex = 0; columnIndex < numColumns; columnIndex++ )
		{
			final String[] split = tableRowsIncludingHeader.get( 1 ).split( delim );
			final String firstCell = split[ columnIndex ];

			String defaultValue = "None"; // for text
			if ( Tables.isNumeric( firstCell ) )
				defaultValue = "NaN"; // for numbers

			final ArrayList< String > values = new ArrayList< >( Collections.nCopies( numRowsTargetTable, defaultValue ));

			final String columnName = columnNames.get( columnIndex );
			columnNameToStrings.put( columnName, values );
			if ( columnName.equals( mergeByColumnName ) )
				mergeByColumnIndex = columnIndex;
		}

		if ( mergeByColumnIndex == -1 )
			throw new UnsupportedOperationException( "Column by which to merge not found: " + mergeByColumnName );

//		final long start = System.currentTimeMillis();
		final int numRowsSourceTable = tableRowsIncludingHeader.size() - 1;

		// TODO: code looks inefficient...
		for ( int rowIndex = 0; rowIndex < numRowsSourceTable; ++rowIndex )
		{
			final String[] split = tableRowsIncludingHeader.get( rowIndex + 1 ).split( delim );
			final String referenceValue = split[ mergeByColumnIndex ];
			final int targetRowIndex = referenceColumnInTargetTable.indexOf( referenceValue );

			for ( int columnIndex = 0; columnIndex < numColumns; columnIndex++ )
			{
				final String columName = columnNames.get( columnIndex );
				columnNameToStrings.get( columName ).set( targetRowIndex, split[ columnIndex ].replace( "\"", "" ) );
			}
		}

//		System.out.println( ( System.currentTimeMillis() - start ) / 1000.0 ) ;

		return columnNameToStrings;
	}

	public static Map< String, List< ? > >
	asTypedColumns( Map< String, List< String > > columnToStringValues )
			throws UnsupportedDataTypeException
	{
		final Set< String > columnNames = columnToStringValues.keySet();

		final LinkedHashMap< String, List< ? > > columnToValues = new LinkedHashMap<>();

		for ( String columnName : columnNames )
		{
			final List< ? > values = asTypedList( columnToStringValues.get( columnName ) );
			columnToValues.put( columnName, values );
		}

		return columnToValues;
	}

	public static List< ? > asTypedList( List< String > strings )
			throws UnsupportedDataTypeException
	{
		final Class columnType = getColumnType( strings.get( 0 ) );

		int numRows = strings.size();

		if ( columnType == Double.class )
		{
			final ArrayList< Double > doubles = new ArrayList<>( numRows );

			for ( int row = 0; row < numRows; ++row )
				doubles.add( Utils.parseDouble( strings.get( row ) ) );

			return doubles;
		}
		else if ( columnType == Integer.class ) // cast to Double anyway...
		{
			final ArrayList< Double > doubles = new ArrayList<>( numRows );

			for ( int row = 0; row < numRows; ++row )
				doubles.add( Utils.parseDouble( strings.get( row ) ) );

			return doubles;
		}
		else if ( columnType == String.class )
		{
			return strings;
		}
		else
		{
			throw new UnsupportedDataTypeException("");
		}
	}

	public static Object[] asTypedArray( List< String > strings ) throws UnsupportedDataTypeException
	{
		final Class columnType = getColumnType( strings.get( 0 ) );

		int numRows = strings.size();

		if ( columnType == Double.class )
		{
			return toDoubles( strings, numRows );
		}
		else if ( columnType == Integer.class ) // cast to Double anyway...
		{
			return toDoubles( strings, numRows );
		}
		else if ( columnType == String.class )
		{
			final String[] stringsArray = new String[ strings.size() ];
			strings.toArray( stringsArray );
			return stringsArray;
		}
		else
		{
			throw new UnsupportedDataTypeException("");
		}
	}

	public static Object[] toDoubles( List< String > strings, int numRows )
	{
		final Double[] doubles = new Double[ numRows ];

		for ( int row = 0; row < numRows; ++row )
			doubles[ row ] =  Utils.parseDouble( strings.get( row ) );

		return doubles;
	}

	private static Class getColumnType( String cell )
	{
		try
		{
			Utils.parseDouble( cell );
			return Double.class;
		}
		catch ( Exception e2 )
		{
			return String.class;
		}
	}

	public static Map< String, List< String > > addLabelImageIdColumn(
			Map< String, List< String > > columns,
			String columnNameLabelImageId,
			String labelImageId )
	{
		final int numRows = columns.values().iterator().next().size();

		final List< String > labelImageIdColumn = new ArrayList<>();

		for ( int row = 0; row < numRows; row++ )
			labelImageIdColumn.add( labelImageId );

		columns.put( columnNameLabelImageId, labelImageIdColumn );

		return columns;
	}

	public static ArrayList< String > getColumn( JTable table, String columnName )
	{
		final int columnIndex = table.getColumnModel().getColumnIndex( columnName );

		final TableModel model = table.getModel();
		final int numRows = model.getRowCount();
		final ArrayList< String > column = new ArrayList<>();
		for ( int rowIndex = 0; rowIndex < numRows; ++rowIndex )
			column.add( model.getValueAt( rowIndex, columnIndex ).toString() );
		return column;
	}

	public static ArrayList< String > getColumn( List< ? extends TableRow > tableRows, String columnName )
	{
		final int numRows = tableRows.size();
		final ArrayList< String > column = new ArrayList<>();
		for ( int rowIndex = 0; rowIndex < numRows; ++rowIndex )
			column.add( tableRows.get( rowIndex ).getCell( columnName ) );
		return column;
	}

	public static Map< String, List< String > > openAndOrderNewColumns( JTable table, String referenceColumnName, String newTablePath )
	{
		final ArrayList< String > referenceColumn = getColumn(
				table,
				referenceColumnName );

		final HashMap< String, List< String > > referenceColumns = new HashMap<>();
		referenceColumns.put( referenceColumnName, referenceColumn );

		final Map< String, List< String > > columNameToValues =
				orderedStringColumnsForMerging(
						null,
						referenceColumns,
						Tables.readRows( newTablePath ) );

		return columNameToValues;
	}

	public static Map< String, List< String > > openAndOrderNewColumns( List< ? extends TableRow > tableRows, String referenceColumnName, String newTablePath )
	{
		final ArrayList< String > referenceColumn = getColumn(
				tableRows,
				referenceColumnName );

		final HashMap< String, List< String > > referenceColumns = new HashMap<>();
		referenceColumns.put( referenceColumnName, referenceColumn );

		final Map< String, List< String > > columNameToValues =
				orderedStringColumnsForMerging(
						null,
						referenceColumns,
						Tables.readRows( newTablePath ) );

		return columNameToValues;
	}
}
