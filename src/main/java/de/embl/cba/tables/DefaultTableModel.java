package de.embl.cba.tables;

import de.embl.cba.tables.tablerow.ColumnBasedTableRow;
import de.embl.cba.tables.tablerow.TableRow;
import org.apache.commons.math3.random.StableRandomGenerator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class DefaultTableModel< T extends TableRow > extends AbstractTableModel< T >
{
	private final List< T > tableRows;

	public DefaultTableModel( List< T > tableRows )
	{
		this.tableRows = tableRows;
	}

	@Override
	public Iterator< T > iterator()
	{
		return new TableRowsIterator();
	}

	@Override
	public int size()
	{
		return tableRows.size();
	}

	@Override
	public Set< String > getColumnNames()
	{
		return tableRows.get( 0 ).getColumnNames();
	}

	@Override
	public T getRow( int rowIndex )
	{
		return tableRows.get( rowIndex );
	}

	public List< String > getColumn( String columnName ){
		if ( tableRows.get( 0 ) instanceof ColumnBasedTableRow )
		{
			final List< String > cells = ( ( ColumnBasedTableRow ) tableRows.get( 0 ) ).getColumns().get( columnName );
			return cells;
		}
		else
		{
			final ArrayList< String > cells = new ArrayList<>();
			for ( T tableRow : tableRows )
			{
				cells.add( tableRow.getCell( columnName ) );
			}
			return cells;
		}
	}

	class TableRowsIterator implements Iterator< T >
	{
		int index = 0;

		@Override
		public boolean hasNext()
		{
			return index < tableRows.size();
		}

		@Override
		public T next()
		{
			return tableRows.get( index++ );
		}
	}
}
