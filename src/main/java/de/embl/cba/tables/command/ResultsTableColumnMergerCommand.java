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
package de.embl.cba.tables.command;

import de.embl.cba.tables.results.ResultsBuilder;
import de.embl.cba.tables.results.ResultsTableFetcher;
import ij.IJ;
import ij.measure.ResultsTable;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.HashMap;
import java.util.stream.Collectors;


/**
 *
 * The parent label index must be the row label in the parent column. This is the case for tables generated with MorpholibJ.
 * The row label of a ResultsTable is a special column that is accessed via the {@code getLabel(int row)} and {@code setLabel(String label, int row)} methods in the Java API.
 *
 * The aggregated child measurements will be appended as new columns to the parent results table.
 *
 */
@Plugin(type = Command.class, menuPath = "Plugins>Tables>Merge>Merge Result Tables Columns" )
public class ResultsTableColumnMergerCommand implements Command
{
	@Parameter ( label = "Results table name" )
	public String tableNameA;

	@Parameter ( label = "Results table name" )
	public String tableNameB;

	@Parameter ( label = "Output table name" )
	public String outputTableName;

	private ResultsTable tableA;
	private ResultsTable tableB;

	@Override
	public void run()
	{
		if ( ! fetchTables() ) return;

		final ResultsTable resultsTable = new ResultsBuilder( tableA ).addResult( tableB ).getResultsTable();

		resultsTable.show( outputTableName );
	}

	private boolean fetchTables()
	{
		final HashMap< String, ResultsTable > titleToTable = ResultsTableFetcher.fetchCurrentlyOpenResultsTables();

		if ( ! titleToTable.containsKey( tableNameA ) )
		{
			showResultsTableNotFoundMessage( titleToTable, tableNameA );
			return false;
		}

		if ( ! titleToTable.containsKey( tableNameB ) )
		{
			showResultsTableNotFoundMessage( titleToTable, tableNameB );
			return false;
		}

		tableA = titleToTable.get( tableNameA );
		tableB = titleToTable.get( tableNameB );
		return true;
	}

	private void showResultsTableNotFoundMessage( HashMap< String, ResultsTable > titleToTable, String parentTable )
	{
		IJ.showMessage( parentTable + "  does not exist.\n" +
				"Please choose one of the following: " + titleToTable.keySet().stream().collect( Collectors.joining("\n") ) );
	}

}
