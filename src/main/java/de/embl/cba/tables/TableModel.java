package de.embl.cba.tables;

import java.util.List;
import java.util.Set;

public interface TableModel< T >
{
	int size();
	Set< String > getColumnNames();
	T getRow( int rowIndex );
	List< String > getColumn( String columnName );
}
