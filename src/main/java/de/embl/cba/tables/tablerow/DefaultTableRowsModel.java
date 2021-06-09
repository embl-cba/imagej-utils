package de.embl.cba.tables.tablerow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultTableRowsModel< T extends TableRow > implements TableRowsModel< T >
{
	private final List< T > tableRows;
	private Map< T, Integer > tableRowToIndex;

	public DefaultTableRowsModel( List< T > tableRows )
	{
		this.tableRows = tableRows;
		initRowIndices( tableRows );
	}

	private void initRowIndices( List< T > tableRows )
	{
		tableRowToIndex = new ConcurrentHashMap<>();
		final int numTableRows = tableRows.size();
		for ( int rowIndex = 0; rowIndex < numTableRows; rowIndex++ )
		{
			tableRowToIndex.put( tableRows.get( rowIndex ), rowIndex );
		}
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

	@Override
	public int indexOf( T tableRow )
	{
		try
		{
			return tableRowToIndex.get( tableRow );
		}
		catch ( Exception e )
		{
			throw new RuntimeException( "Table row not found in table model." );
		}
	}

	public List< String > getColumn( String columnName )
	{

//		Below is not working for concatenate tables.
//		if ( tableRows.get( 0 ) instanceof ColumnBasedTableRow )
//		{
//			final List< String > cells = ( ( ColumnBasedTableRow ) tableRows.get( 0 ) ).getColumns().get( columnName );
//			return cells;
//		}
//		else
//		{
			final ArrayList< String > cells = new ArrayList<>();
			for ( T tableRow : tableRows )
			{
				cells.add( tableRow.getCell( columnName ) );
			}
			return cells;
//		}
	}

	@Override
	public void addColumn( String columnName, String defaultEntry )
	{
		final ArrayList< String > entries = new ArrayList<>();
		final int size = tableRows.size();
		for ( int i = 0; i < size; i++ )
			entries.add( defaultEntry );

		addColumn( columnName, entries );
	}

	@Override
	public void addColumn( String columnName, List< String > entries )
	{
		if ( tableRows.get( 0 ) instanceof ColumnBasedTableRow )
		{
			final Map< String, List< String > > columns = ( ( ColumnBasedTableRow ) tableRows.get( 0 ) ).getColumns();
			columns.put( columnName, entries );
		}
		else
		{
			throw new UnsupportedOperationException( "Cannot add column to table rows of class: " + tableRows.get( 0 ).getClass());
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
