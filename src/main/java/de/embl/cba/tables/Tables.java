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

import de.embl.cba.tables.imagesegment.ColumnBasedTableRowImageSegment;
import de.embl.cba.tables.table.ColumnClassAwareTableModel;
import de.embl.cba.tables.tablerow.ColumnBasedTableRow;
import de.embl.cba.tables.tablerow.DefaultColumnBasedTableRow;
import de.embl.cba.tables.tablerow.JTableFromTableRowsModelCreator;
import de.embl.cba.tables.tablerow.TableRow;
import org.scijava.table.GenericTable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Tables
{
	@Deprecated // Use JTableFromTableRowsModelCreator instead
	public static JTable jTableFromTableRows( List< ? extends TableRow > tableRows )
	{
		return new JTableFromTableRowsModelCreator( tableRows ).createJTable();
	}

	public static JTable asJTable( GenericTable genericTable )
	{
		final int numCols = genericTable.getColumnCount();

		DefaultTableModel model = new DefaultTableModel();

		for ( int col = 0; col < numCols; ++col )
		{
			model.addColumn( genericTable.getColumnHeader( col ) );
		}

		for ( int row = 0; row < genericTable.getRowCount(); ++row )
		{
			final String[] rowEntries = new String[ numCols ];

			for ( int col = 0; col < numCols; ++col )
			{
				rowEntries[ col ] = ( String ) genericTable.get( col, row );
			}

			model.addRow( rowEntries );
		}

		return new JTable( model );
	}


	public static void saveTable( JTable table, File tableOutputFile )
	{
		try
		{
			saveTableWithIOException( table, tableOutputFile );
		} catch ( IOException e )
		{
			e.printStackTrace();
		}
	}

	private static void saveTableWithIOException( JTable table, File file ) throws IOException
	{
		BufferedWriter bfw = new BufferedWriter( new FileWriter( file ) );

		final int lastColumn = table.getColumnCount() - 1;

		// header
		for ( int col = 0; col < lastColumn; col++ )
			bfw.write( table.getColumnName( col ) + "\t" );
		bfw.write( table.getColumnName( lastColumn ) + "\n" );

		// content
		for ( int row = 0; row < table.getRowCount(); row++ )
		{
			for ( int col = 0; col < lastColumn; col++ )
				bfw.write( table.getValueAt( row, col ) + "\t" );
			bfw.write( table.getValueAt( row, lastColumn ) + "\n" );
		}

		bfw.close();
	}

	public static JTable loadTable( final String path )
	{
		List< String > rows = readRows( path );

		return createJTableFromStringList( rows, null );
	}

	public static JTable loadTable( final String path, String delim )
	{
		List< String > rows = readRows( path );

		return createJTableFromStringList( rows, delim );
	}

	public static List< String > readRows( String path )
	{
		BufferedReader br = FileAndUrlUtils.getReader( path );

		List< String > rows = readRows( br );

		if ( rows.size() == 0 )
			throw new RuntimeException( "Could not read rows from table: " + path );

		return rows;
	}

	public static List< String > readRows( BufferedReader br )
	{
		try
		{
			List< String > rows = new ArrayList<>();
			String aRow;
			while ( ( aRow = br.readLine() ) != null )
				rows.add( aRow );
			br.close();
			return rows;
		} catch ( IOException e )
		{
			throw new RuntimeException( e );
		}

	}

	public static List< String > readRows( File file, int numRows )
	{
		List< String > rows = new ArrayList<>();

		try
		{
			FileInputStream fin = new FileInputStream( file );
			BufferedReader br = new BufferedReader( new InputStreamReader( fin ) );

			int rowIdx = 0;

			String aRow;
			while ( ( aRow = br.readLine() ) != null && rowIdx++ < numRows )
				rows.add( aRow );

			br.close();
		} catch ( Exception e )
		{
			e.printStackTrace();
		}

		return rows;
	}


	public static List< String > getColumnNames( List< String > strings, String delim )
	{
		StringTokenizer st = new StringTokenizer( strings.get( 0 ), delim );

		List< String > columnNames = new ArrayList<>();

		while ( st.hasMoreTokens() )
		{
			columnNames.add( st.nextToken().replace( "\"", "" ) );
		}
		return columnNames;
	}

	public static String autoDelim( String delim, List< String > strings )
	{
		if ( delim == null )
		{
			if ( strings.get( 0 ).contains( "\t" ) )
			{
				delim = "\t";
			} else if ( strings.get( 0 ).contains( "," ) )
			{
				delim = ",";
			} else if ( strings.get( 0 ).contains( ";" ) )
			{
				delim = ";";
			} else
			{
				throw new RuntimeException( "Could not identify table delimiter." );
			}

		}
		return delim;
	}

	public static void addColumnToJTable( String columnName, String value, TableModel model )
	{
		try
		{
			Utils.parseDouble( value );
			Tables.addColumn( model, columnName, 0.0D );
		}
		catch ( Exception e )
		{
			Tables.addColumn( model, columnName, "None" );
		}
	}

	public static JTable createJTableFromStringList( List< String > strings, String delim )
	{
		delim = autoDelim( delim, strings );

		StringTokenizer st = new StringTokenizer( strings.get( 0 ), delim );

		List< String > colNames = new ArrayList<>();

		while ( st.hasMoreTokens() )
			colNames.add( st.nextToken() );

		/**
		 * Init model and columns
		 */

		ColumnClassAwareTableModel model = new ColumnClassAwareTableModel();

		for ( String colName : colNames )
			model.addColumn( colName );

		int numCols = colNames.size();

		/**
		 * Add tablerow entries
		 */

		for ( int iString = 1; iString < strings.size(); ++iString )
		{
			model.addRow( new Object[ numCols ] );

			st = new StringTokenizer( strings.get( iString ), delim );

			for ( int iCol = 0; iCol < numCols; iCol++ )
			{
				String stringValue = st.nextToken();

				try
				{
					final Double numericValue = Utils.parseDouble( stringValue );
					model.setValueAt( numericValue, iString - 1, iCol );
				} catch ( Exception e )
				{
					model.setValueAt( stringValue, iString - 1, iCol );
				}
			}

		}

		model.refreshColumnClassesFromObjectColumns();

		return new JTable( model );
	}

	public static boolean isNumeric( String string )
	{
		try
		{
			Utils.parseDouble( string );
			return true;
		}
		catch ( Exception e )
		{
			return false;
		}
	}

//	// TODO: replace by more performant column based version (see below)
//	public static List< TableRowImageSegment > segmentsFromTableFile(
//			final File file,
//			String delim,
//			final Map< SegmentProperty, String > coordinateColumnMap,
//			final DefaultImageSegmentBuilder segmentBuilder )
//	{
//
//		final List< TableRowImageSegment > segments = new ArrayList<>();
//
//		final List< String > rowsInTable = readRows( file );
//
//		delim = autoDelim( delim, rowsInTable );
//
//		List< String > columns = getColumnNames( rowsInTable, delim );
//
//		for ( int row = 1; row < rowsInTable.size(); ++row )
//		{
//			final LinkedHashMap< String, Object > columnValueMap = new LinkedHashMap<>();
//
//			StringTokenizer st = new StringTokenizer( rowsInTable.get( row ), delim );
//
//			for ( String column : columns )
//			{
//				final String string = st.nextToken();
//
//				addColumn( columnValueMap, column, string );
//			}
//
//			final DefaultImageSegment segment =
//					SegmentUtils.segmentFromFeatures(
//							coordinateColumnMap,
//							columnValueMap,
//							segmentBuilder );
//
//			final TableRow tableRow = new DefaultTableRow( columnValueMap, row - 1  );
//
//			segments.add( new DefaultTableRowImageSegment( segment, tableRow ) );
//		}
//
//		return segments;
//
//	}

//	public static List< DefaultTableRowImageSegment > segmentsFromTableFileColumnWise(
//			final File file,
//			String delim,
//			final Map< SegmentProperty, String > coordinateColumnMap,
//			final DefaultImageSegmentBuilder segmentBuilder
//	)
//	{
//
//		final List< DefaultTableRowImageSegment > segments = new ArrayList<>();
//
//		final List< String > rowsInTable = readRows( file );
//
//		delim = autoDelim( delim, rowsInTable );
//
//		List< String > columns = getColumnNames( rowsInTable, delim );
//
//		final LinkedHashMap< String, List< Object > > columnToValues = new LinkedHashMap<>();
//
//		for ( int columnIndex = 0; columnIndex < columns.size(); columnIndex++ )
//		{
//			final String columnName = columns.get( columnIndex );
//			columnToValues.put( columnName, new ArrayList<>(  ) );
//			//setColumnIndex( coordinateColumnMap, columnIndex, columnName );
//		}
//
//
//		final List< TableRowMap > tableRowMaps = new ArrayList<>();
//		for ( int row = 1; row < rowsInTable.size(); ++row )
//		{
////			final LinkedHashMap< String, Object > columnValueMap = new LinkedHashMap<>();
//
//			StringTokenizer st = new StringTokenizer( rowsInTable.get( row ), delim );
//
//			for ( String column : columns )
//			{
//				final String string = st.nextToken();
//				columnToValues.get( column ).add( string );
//			}
//
//
//			final TableRowFromColumns tableRowMap =
//					new TableRowFromColumns( columnToValues, row - 1);
//
//			// TODO
////			final DefaultImageSegment segment =
////					SegmentUtils.segmentFromTableRowMap(
////							coordinateColumnMap,
////							tableRowMap,
////							segmentBuilder );
////
////
////			tableRowMaps.add( tableRowMap );
////
////			imagesegment.add( new DefaultAnnotatedImageSegment( null, tableRow ) );
//		}
//
//		return segments;
//
//	}


	public static void addColumn(
			HashMap< String, Object > columnValueMap,
			String column,
			String string )
	{
		try
		{
			final double number = Integer.parseInt( string );
			columnValueMap.put( column, number );
		}
		catch ( Exception e )
		{
			try
			{
				final double number = Utils.parseDouble( string );
				columnValueMap.put( column, number );
			}
			catch ( Exception e2 )
			{
				columnValueMap.put( column, string );
			}
		}
	}

//	public static void addStringColumn(
//			HashMap< String, Object > columnValueMap,
//			String column,
//			String string )
//	{
//		columnValueMap.put( column, string );
////		try
////		{
////			final double number = Integer.parseInt( string );
////			columnValueMap.put( column, number );
////		}
////		catch ( Exception e )
////		{
////			try
////			{
////				final double number = Utils.parseDouble( string );
////				columnValueMap.put( column, number );
////			}
////			catch ( Exception e2 )
////			{
////				columnValueMap.put( column, string );
////			}
////		}
//	}

//	public static void setColumnIndex(
//			Map< SegmentProperty, ValuePair< String, Integer > > coordinateColumnMap,
//			int columnIndex,
//			String columnName )
//	{
//		for ( SegmentProperty coordinate : coordinateColumnMap.keySet() )
//		{
//			if ( coordinateColumnMap.get( coordinate ).getA().equals( columnName ) )
//			{
//				coordinateColumnMap.put(
//						coordinate,
//						new ValuePair< String, Integer>( columnName, columnIndex ) );
//				break;
//			}
//		}
//	}
//
//	public static int getInteger( int columnIndex, String[] rowEntries  )
//	{
//		if ( columnIndex == -1 )
//		{
//			return 0;
//		}
//		else
//		{
//			return Integer.parseInt( rowEntries[ columnIndex ] );
//		}
//	}
//
//	public static String getString( int columnIndex, String[] rowEntries )
//	{
//		if ( columnIndex == -1 )
//		{
//			return "";
//		}
//		else
//		{
//			return rowEntries[ columnIndex ];
//		}
//	}
//
//	public static double getDouble( int columnIndex, String[] rowEntries )
//	{
//		return Utils.parseDouble( rowEntries[ columnIndex ] );
//	}

	public static List< String >
	getColumnEntries( List< ? extends TableRow > tableRows,
					  String columnName )
	{
		if ( tableRows instanceof ColumnBasedTableRowImageSegment )
		{
			final Map< String, List< String > > columns
					= ( ( ColumnBasedTableRowImageSegment ) tableRows ).getColumns();
			return columns.get( columnName );
		}
		else
		{
			final ArrayList< String > strings = new ArrayList<>();
			final int size = tableRows.size();
			for ( int row = 0; row < size; row++ )
				strings.add( tableRows.get( row ).getCell( columnName ) );
			return strings;
		}
	}


	public static List< String > getColumnNames( JTable jTable )
	{
		final List< String > columnNames = new ArrayList<>();

		for ( int columnIndex = 0; columnIndex < jTable.getColumnCount(); columnIndex++ )
		{
			columnNames.add( jTable.getColumnName( columnIndex ) );
		}
		return columnNames;
	}


	public static String[] getColumnNamesAsArray( JTable jTable )
	{
		final String[] columnNames = new String[ jTable.getColumnCount() ];

		for ( int columnIndex = 0; columnIndex < jTable.getColumnCount(); columnIndex++ )
		{
			columnNames[ columnIndex ] = jTable.getColumnName( columnIndex );
		}
		return columnNames;
	}

	public static < A, B > TreeMap< A, B > columnsAsTreeMap(
			final JTable table,
			final String fromColumn,
			final String toColumn )
	{

		final int fromColumnIndex = table.getColumnModel().getColumnIndex( fromColumn );
		final int toColumnIndex = table.getColumnModel().getColumnIndex( toColumn );

		final TreeMap< A, B > treeMap = new TreeMap();

		final int rowCount = table.getRowCount();

		for ( int row = 0; row < rowCount; row++ )
		{
			treeMap.put( ( A ) table.getValueAt( row, fromColumnIndex ), ( B ) table.getValueAt( row, toColumnIndex ) );
		}

		return treeMap;
	}

	public static double columnMin( JTable jTable, int col )
	{
		double min = Double.MAX_VALUE;

		final int rowCount = jTable.getRowCount();

		for ( int row = 0; row < rowCount; row++ )
		{
			final Double valueAt = ( Double ) jTable.getValueAt( row, col );

			if ( valueAt < min )
			{
				min = valueAt;
			}
		}

		return min;
	}

	public static double columnMax( JTable jTable, int col )
	{
		double max =  - Double.MAX_VALUE;

		final int rowCount = jTable.getRowCount();

		for ( int row = 0; row < rowCount; row++ )
		{
			final Double valueAt = ( Double ) jTable.getValueAt( row, col );

			if ( valueAt > max )
			{
				max = valueAt;
			}
		}

		return max;
	}

	public static HashMap< String, ArrayList< Integer > > uniqueColumnEntries( JTable jTable, int col )
	{
		final HashMap< String, ArrayList< Integer > > uniqueEntryToRowIndexSet = new HashMap<>();

		TableModel tableModel = jTable.getModel() ;
		final int rowCount = tableModel.getRowCount();

		for( int rowIndex = 0; rowIndex < rowCount; rowIndex++)
		{
			final String valueAt = tableModel.getValueAt( rowIndex, col ).toString();

			if ( ! uniqueEntryToRowIndexSet.keySet().contains( valueAt ) )
			{
				uniqueEntryToRowIndexSet.put( valueAt, new ArrayList<>(  ) );
			}

			uniqueEntryToRowIndexSet.get( valueAt ).add( rowIndex );
		}

		return uniqueEntryToRowIndexSet;
	}

	public static void addColumn( JTable table, String column, Object defaultValue )
	{
		addColumn( table.getModel(), column, defaultValue );
	}

	public static void addColumn( TableModel model, String column, Object defaultValue )
	{
		if ( model instanceof ColumnClassAwareTableModel )
		{
			( (ColumnClassAwareTableModel) model ).addColumnClass( defaultValue );
		}

		if ( model instanceof DefaultTableModel )
		{
			final Object[] rows = new Object[ model.getRowCount() ];
			Arrays.fill( rows, defaultValue );
			( (DefaultTableModel) model ).addColumn( column, rows );
		}
	}

	public static void addColumn( JTable table, String column, Object[] values )
	{
		addColumn( table.getModel(), column, values );
	}

	public static void addColumn( TableModel model, String column, Object[] values )
	{
		if ( model instanceof ColumnClassAwareTableModel )
			( (ColumnClassAwareTableModel) model ).addColumnClass( values[ 0 ] );

		if ( model instanceof DefaultTableModel )
			( (DefaultTableModel) model ).addColumn( column, values );
	}

	public static void addRelativeImagePathColumn(
			JTable table,
			String imagePath,
			String rootPath,
			String imageName )
	{
		if ( rootPath == null ) return;
		final Path relativeImagePath = getRelativePath( imagePath, rootPath );
		addColumn( table, "Path_" + imageName, relativeImagePath );
	}

	public static Path getRelativePath( String pathA, String pathB )
	{
		final Path imagePath = Paths.get( pathA );
		final Path tablePath = Paths.get( pathB );

		return tablePath.relativize( imagePath );
	}


	public static Path getAbsolutePath( String rootPath, String relativePath )
	{
		final Path path = Paths.get( rootPath, relativePath );
		final Path normalize = path.normalize();
		return normalize;
	}

	public static double asDouble( Object featureValue )
	{
		if ( featureValue instanceof Number )
			return (( Number ) featureValue).doubleValue();
		else
			return Utils.parseDouble( featureValue.toString() );
	}

	public static double[] minMax( String column, JTable table )
	{
		final int columnIndex = table.getColumnModel().getColumnIndex( column );

		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;

		final int rowCount = table.getRowCount();
		for ( int row = 0; row < rowCount; row++ )
		{
			final double value = ( Double ) table.getValueAt( row, columnIndex );
			if ( value < min ) min = value;
			if ( value > max ) max = value;
		}

		return new double[]{ min, max };
	}

	public static double[] meanSigma( String column, JTable table )
	{
		final int columnIndex = table.getColumnModel().getColumnIndex( column );
		return meanSigma( columnIndex, table );
	}

	public static double[] meanSigma( int columnIndex, JTable table )
	{

		double mean = computeMean( columnIndex, table );
		double sigma = computeSigma( columnIndex, table, mean );

		return new double[]{ mean, sigma };
	}

	public static double computeSigma( int columnIndex, JTable table, double mean )
	{
		final int rowCount = table.getRowCount();

		double sigma = 0.0;
		double value;
		for ( int row = 0; row < rowCount; row++ )
		{
			value = ( Double ) table.getValueAt( row, columnIndex );
			sigma += Math.pow( value - mean, 2 );
		}

		sigma /= rowCount; // variance
		sigma = Math.sqrt( sigma ); // sigma
		return sigma;
	}

	public static double computeMean( int columnIndex, JTable table )
	{
		final int rowCount = table.getRowCount();

		double mean = 0.0;
		for ( int row = 0; row < rowCount; row++ )
		{
			final double value = ( Double ) table.getValueAt( row, columnIndex );
			mean += value;
		}
		mean /= rowCount;
		return mean;
	}

	public static boolean isNumeric( JTable table, String columnName )
	{
		final TableModel model = table.getModel();
		final int columnIndex = table.getColumnModel().getColumnIndex( columnName );

		final Class< ? > columnClass = model.getColumnClass( columnIndex );
		return Number.class.isAssignableFrom( columnClass );
	}

	public static JTable createNewTableFromSelectedColumns( JTable table, ArrayList< String > selectedColumns )
	{
		DefaultTableModel newModel = new DefaultTableModel();
		final TableModel model = table.getModel();
		final int rowCount = table.getRowCount();

		for ( String columnName : selectedColumns )
		{
			int viewIndex = table.getColumnModel().getColumnIndex( columnName );
			int modelIndex = table.convertColumnIndexToModel( viewIndex );
			final Object[] objects = new Object[ rowCount ];
			for ( int rowIndex = 0; rowIndex < objects.length; rowIndex++ )
				objects[ rowIndex ] = model.getValueAt( rowIndex, modelIndex );

			newModel.addColumn( columnName, objects );
		}

		return new JTable( newModel );
	}

	public static List< ColumnBasedTableRow > columnBasedTableRowsFromColumns( final Map< String, List< String > > columnNamesToColumns )
	{
		final List< ColumnBasedTableRow > columnBasedTableRows = new ArrayList<>();

		final int numRows = columnNamesToColumns.values().iterator().next().size();

		for ( int row = 0; row < numRows; row++ )
		{
			final DefaultColumnBasedTableRow tableRow = new DefaultColumnBasedTableRow( row, columnNamesToColumns );

			columnBasedTableRows.add( tableRow );
		}

		return columnBasedTableRows;
	}

	public static List< ColumnBasedTableRow > open( String path )
	{
		Map< String, List< String > > columnNameToColumn = TableColumns.stringColumnsFromTableFile( path );
		return columnBasedTableRowsFromColumns( columnNameToColumn );
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
	void setJTableCell(
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
