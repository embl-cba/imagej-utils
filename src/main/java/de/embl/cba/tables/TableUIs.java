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

import de.embl.cba.tables.view.TableRowsTableView;
import ij.gui.GenericDialog;
import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.embl.cba.tables.FileUtils.selectPathFromProjectOrFileSystem;


public class TableUIs
{

	public static void addColumnUI( TableRowsTableView tableView )
	{
		final GenericDialog gd = new GenericDialog( "Add Custom Column" );
		gd.addStringField( "Column Name", "Column", 30 );
		gd.addStringField( "Default Value", "None", 30 );

		gd.showDialog();
		if( gd.wasCanceled() ) return;

		final String columnName = gd.getNextString();
		final String defaultValueString = gd.getNextString();

		Object defaultValue;
		try	{
			defaultValue = Utils.parseDouble( defaultValueString );
		}
		catch ( Exception e )
		{
			defaultValue = defaultValueString;
		}

		tableView.addColumn( columnName, defaultValue );
	}

	public static String selectColumnNameUI( JTable table, String text )
	{
		final String[] columnNames = Tables.getColumnNamesAsArray( table );
		final GenericDialog gd = new GenericDialog( "" );
		gd.addChoice( text, columnNames, columnNames[ 0 ] );
		gd.showDialog();
		if ( gd.wasCanceled() ) return null;
		final String columnName = gd.getNextChoice();
		return columnName;
	}

	public static ArrayList< String > selectColumnNamesUI( JTable table, String text )
	{
		final String[] columnNames = Tables.getColumnNamesAsArray( table );
		final int n = (int) Math.ceil( Math.sqrt( columnNames.length ) );
		final GenericDialog gd = new GenericDialog( "" );
		boolean[] booleans = new boolean[ columnNames.length ];
		gd.addCheckboxGroup( n, n, columnNames, booleans );
		gd.showDialog();
		if ( gd.wasCanceled() ) return null;

		final ArrayList< String > selectedColumns = new ArrayList<>();
		for ( int i = 0; i < columnNames.length; i++ )
			if ( gd.getNextBoolean() )
				selectedColumns.add( columnNames[ i ] );

		return selectedColumns;
	}

	public static void saveTableUI( JTable table )
	{
		final JFileChooser jFileChooser = new JFileChooser( "" );

		if ( jFileChooser.showSaveDialog( null ) == JFileChooser.APPROVE_OPTION )
		{
			final File selectedFile = jFileChooser.getSelectedFile();

			Tables.saveTable( table, selectedFile );
		}
	}

	public static void saveColumns( JTable table )
	{
		final ArrayList< String > selectedColumns
				= selectColumnNamesUI( table, "Select columns" );

		final JTable newTable = Tables.createNewTableFromSelectedColumns( table, selectedColumns );

		final JFileChooser jFileChooser = new JFileChooser( "" );

		if ( jFileChooser.showSaveDialog( null ) == JFileChooser.APPROVE_OPTION )
		{
			final File selectedFile = jFileChooser.getSelectedFile();

			Tables.saveTable( newTable, selectedFile );
		}
	}

	public static Map< String, List< String > > openTableUI( )
	{
		final JFileChooser jFileChooser = new JFileChooser( "" );

		if ( jFileChooser.showOpenDialog( null ) == JFileChooser.APPROVE_OPTION )
		{
			final File selectedFile = jFileChooser.getSelectedFile();

			return TableColumns.stringColumnsFromTableFile( selectedFile.toString() );
		}

		return null;
	}

	// TODO: make own class: ColumnsLoader
	public static Map< String, List< String > > loadColumns( JTable table,
															 String newTablePath,
															 String mergeByColumnName ) throws IOException
	{
		Map< String, List< String > > columns = TableColumns.openAndOrderNewColumns( table, mergeByColumnName, newTablePath );
		return columns;
	}

	public static String[] getTableNamesFromFile( String tablesLocation, String additionalTableNamesFile ) throws IOException
	{
		String additionalTablesUrl = getAdditionalTablesUrl( tablesLocation, additionalTableNamesFile );

		final BufferedReader reader = Tables.getReader( additionalTablesUrl );

		final ArrayList< String > lines = new ArrayList<>();
		String line = reader.readLine();
		while ( line != null )
		{
			lines.add( line );
			line = reader.readLine();
		}
		return lines.toArray( new String[]{} );
	}

	public static String getAdditionalTablesUrl( String tablesLocation, String additionalTableNamesFile )
	{
		String additionalTablesUrl;
		if ( tablesLocation.endsWith( "/" ) )
		{
			additionalTablesUrl = tablesLocation + additionalTableNamesFile;
		}
		else
		{
			additionalTablesUrl = tablesLocation + "/" + additionalTableNamesFile;
		}
		return additionalTablesUrl;
	}

}
