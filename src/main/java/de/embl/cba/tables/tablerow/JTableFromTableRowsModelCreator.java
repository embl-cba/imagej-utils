package de.embl.cba.tables.tablerow;

import de.embl.cba.tables.TableColumns;
import de.embl.cba.tables.Tables;
import de.embl.cba.tables.table.ColumnClassAwareTableModel;

import javax.activation.UnsupportedDataTypeException;
import javax.swing.*;
import java.util.List;
import java.util.Set;

public class JTableFromTableRowsModelCreator
{
	private final TableRowsModel< ? extends TableRow > tableRowsModel;

	public JTableFromTableRowsModelCreator( List< ? extends TableRow > tableRows )
	{
		this.tableRowsModel = new DefaultTableRowsModel<>( tableRows );
	}

	public JTableFromTableRowsModelCreator( TableRowsModel< ? > tableRowsModel )
	{
		this.tableRowsModel = tableRowsModel;
	}

	public JTable createJTable( )
	{
		ColumnClassAwareTableModel model = new ColumnClassAwareTableModel();

		final Set< String > columnNames = tableRowsModel.getRow( 0 ).getColumnNames();

		for ( String columnName : columnNames )
		{
			List< String > strings = tableRowsModel.getColumn( columnName );

			Object[] objects = null;
			try
			{
				objects = TableColumns.asTypedArray( strings );
			}
			catch ( UnsupportedDataTypeException e )
			{
				e.printStackTrace();
			}
			model.addColumn( columnName, objects  );
		}

		model.refreshColumnClassesFromObjectColumns();

		return new JTable( model );
	}
}
