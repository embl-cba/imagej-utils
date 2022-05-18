/*-
 * #%L
 * Various Java code for ImageJ
 * %%
 * Copyright (C) 2018 - 2022 EMBL
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

import de.embl.cba.tables.results.ResultsChildAndParentMerger;
import de.embl.cba.tables.results.ResultsTableFetcher;
import ij.measure.ResultsTable;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.HashMap;


/**
 *
 * The parent label index must be the row label in the parent column. This is the case for tables generated with MorpholibJ.
 * The row label of a ResultsTable is a special column that is accessed via the {@code getLabel(int row)} and {@code setLabel(String label, int row)} methods in the Java API.
 *
 * The aggregated child measurements will be appended as new columns to the parent results table.
 *
 */
@Plugin(type = Command.class, menuPath = "Plugins>Tables>Merge>Merge Child and Parent Results Tables" )
public class ChildParentResultsTablesMergerCommand implements Command
{
	@Parameter ( label = "Parent results table name" )
	public String parentTableName;

	@Parameter ( label = "Child results table name" )
	public String childTableName;

	@Parameter ( label = "Child name (free text)" )
	public String childName;

	@Parameter ( label = "Parent label column name (existing column in child table)" )
	public String parentLabelColumn;

	@Parameter ( label = "Aggregation mode", choices = { "Mean", "Max", "Min" } )
	public String aggregationMode = "Mean";

	@Parameter ( label = "Output table name" )
	public String outputTableName = "Merged";


	private ResultsTable parentTable;
	private ResultsTable childTable;

	@Override
	public void run()
	{
		fetchTables();

		final ResultsChildAndParentMerger merger = new ResultsChildAndParentMerger( childTable, parentTable, childName, parentLabelColumn );

		merger.appendToParentTable( ResultsChildAndParentMerger.AggregationMode.valueOf( aggregationMode ) );

		parentTable.show( outputTableName );
	}

	private void fetchTables()
	{
		final ResultsTableFetcher resultsTableFetcher = new ResultsTableFetcher();

		parentTable = resultsTableFetcher.fetch( parentTableName );
		childTable = resultsTableFetcher.fetch( childTableName );
	}

}
