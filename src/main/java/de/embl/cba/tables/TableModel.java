package de.embl.cba.tables;

import de.embl.cba.tables.tablerow.TableRow;

import java.util.List;
import java.util.Set;

public interface TableModel< T extends TableRow > extends Iterable< T >
{
	int size();
	Set< String > getColumnNames();
	T getRow( int rowIndex );
	List< String > getColumn( String columnName );
	void addColumn( String columnName );
}
