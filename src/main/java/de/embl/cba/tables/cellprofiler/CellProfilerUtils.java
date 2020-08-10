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
package de.embl.cba.tables.cellprofiler;

import de.embl.cba.tables.cellprofiler.FolderAndFileColumn;
import de.embl.cba.tables.command.ExploreCellProfilerCommand;

import java.io.File;
import java.util.*;

public abstract class CellProfilerUtils
{
	public static List< String > replaceFolderAndFileColumnsByPathColumn(
			Map< String, List< String > > columns )
	{
		final int numRows = columns.values().iterator().next().size();
		HashMap< String, FolderAndFileColumn >
				imageNameToFolderAndFileColumns = fetchFolderAndFileColumns( columns.keySet() );

		final List< String > pathColumnNames = new ArrayList<>();

		for ( String imageName : imageNameToFolderAndFileColumns.keySet() )
		{
			final String fileColumnName =
					imageNameToFolderAndFileColumns.get( imageName ).fileColumn();
			final String folderColumnName =
					imageNameToFolderAndFileColumns.get( imageName ).folderColumn();
			final List< ? > fileColumn = columns.get( fileColumnName );
			final List< ? > folderColumn = columns.get( folderColumnName );

			final List< String > pathColumn = new ArrayList<>();

			for ( int row = 0; row < numRows; row++ )
			{
				String imagePath = folderColumn.get( row )
						+ File.separator + fileColumn.get( row );

				pathColumn.add( imagePath );
			}

			columns.remove( fileColumnName );
			columns.remove( folderColumnName );

			final String pathColumnName = getPathColumnName( imageName );
			columns.put( pathColumnName, pathColumn );
			pathColumnNames.add( pathColumnName );
		}

		return pathColumnNames;
	}

	public static HashMap< String, FolderAndFileColumn > fetchFolderAndFileColumns(
			Set< String > columns )
	{
		final HashMap< String, FolderAndFileColumn > imageNameToFolderAndFileColumns
				= new HashMap<>();

		for ( String column : columns )
		{
			if ( column.contains( ExploreCellProfilerCommand.CELLPROFILER_FOLDER_COLUMN_PREFIX ) )
			{
				final String image =
						column.split( ExploreCellProfilerCommand.CELLPROFILER_FOLDER_COLUMN_PREFIX )[ 1 ];
				String fileColumn = getMatchingFileColumn( image, columns );
				imageNameToFolderAndFileColumns.put(
						image,
						new FolderAndFileColumn( column, fileColumn ) );
			}
		}

		return imageNameToFolderAndFileColumns;
	}

	public static String getMatchingFileColumn( String image, Set< String > columns )
	{
		String matchingFileColumn = null;

		for ( String column : columns )
		{
			if ( column.contains( ExploreCellProfilerCommand.CELLPROFILER_FILE_COLUMN_PREFIX ) && column.contains( image ) )
			{
				matchingFileColumn = column;
				break;
			}
		}

		return matchingFileColumn;
	}

	public static String getPathColumnName( String imageName )
	{
		return "Path_" + imageName;
	}

}
