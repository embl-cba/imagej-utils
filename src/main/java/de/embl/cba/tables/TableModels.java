package de.embl.cba.tables;

import de.embl.cba.tables.tablerow.TableRow;
import ij.measure.ResultsTable;

import java.util.Set;

public class TableModels
{
	public static ResultsTable resultsTableFromTableModel( TableModel< ? extends TableRow > tableModel )
	{
		final ResultsTable resultsTable = new ResultsTable();
		final Set< String > columnNames = tableModel.getColumnNames();
		int rowIndex = -1;
		for ( TableRow tableRow : tableModel )
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
