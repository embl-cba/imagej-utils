package de.embl.cba.tables.tablerow;

import ij.measure.ResultsTable;

import java.util.Set;

public class ResultsTableFromTableRowsModelCreator
{
	private final TableRowsModel< ? > tableRowsModel;

	public ResultsTableFromTableRowsModelCreator( TableRowsModel< ? > tableRowsModel )
	{
		this.tableRowsModel = tableRowsModel;
	}

	public ResultsTable createResultsTable( )
	{
		final ResultsTable resultsTable = new ResultsTable();
		final Set< String > columnNames = tableRowsModel.getColumnNames();
		int rowIndex = -1;
		for ( TableRow tableRow : tableRowsModel )
		{
			rowIndex++;
			for ( String columnName : columnNames )
			{
				resultsTable.setValue( columnName, rowIndex, tableRow.getCell( columnName )  );
			}
		}
		return resultsTable;
	}
}
