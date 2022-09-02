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

			try
			{
				Object[] objects = TableColumns.asTypedArray( strings );
				model.addColumn( columnName, objects  );
			}
			catch ( Exception e )
			{
				System.err.println("Error parsing column: " + columnName);
				System.err.println("Skipping column: " + columnName);
				e.printStackTrace();
				//throw new RuntimeException("Error parsing column: " + columnName);
			}
		}

		model.refreshColumnClassesFromObjectColumns();

		return new JTable( model );
	}
}
