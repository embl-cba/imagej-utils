package de.embl.cba.tables.tablerow;

import java.util.List;
import java.util.Set;

public interface TableRowsModel< T extends TableRow > extends Iterable< T >
{
	int size();
	Set< String > getColumnNames();
	T getRow( int rowIndex );
	int indexOf( T tableRow );
	List< String > getColumn( String columnName );
	void addColumn( String columnName, String defaultEntry );
	void addColumn( String columnName, List< String > entries );
}
