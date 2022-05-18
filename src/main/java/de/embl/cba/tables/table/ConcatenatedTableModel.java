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
package de.embl.cba.tables.table;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.List;

public class ConcatenatedTableModel extends AbstractTableModel implements TableModelListener
{
	private List< TableModel > models;
	private int rowCount;

	public ConcatenatedTableModel() {
		models = new ArrayList<>(3);
	}

	public ConcatenatedTableModel(TableModel... models) {
		this();
		for (TableModel model : models) {
			add(model);
		}
	}

	public ConcatenatedTableModel( ArrayList< TableModel > models) {
		this();
		for ( TableModel model : models ) {
			add( model );
		}
	}

	protected int getRowOffset(TableModel model) {
		int rowOffset = 0;
		for (TableModel proxy : models) {
			if (proxy.equals(model)) {
				break;
			} else {
				rowOffset += proxy.getRowCount();
			}
		}
		return rowOffset;
	}

	protected TableModel getModelForRow(int row) {
		TableModel model = null;
		int rowOffset = 0;
		for (TableModel proxy : models) {
			if (row >= rowOffset && row < (rowOffset + proxy.getRowCount())) {
				model = proxy;
				break;
			}
			rowOffset += proxy.getRowCount();
		}
		return model;
	}

	protected void updateRowCount() {
		rowCount = 0;
		for (TableModel proxy : models) {
			rowCount += proxy.getRowCount();
		}
	}

	public void add(TableModel model) {
		int firstRow = getRowCount();
		int lastRow = firstRow + model.getRowCount() - 1;

		if ( models.size() >=1 )
		{
			final int columnCount = models.get( 0 ).getColumnCount();

			if ( columnCount != model.getColumnCount() )
				throw new UnsupportedOperationException( "Number of columns do not match!" );

			for ( int col = 0; col < columnCount; col++ )
				if ( ! model.getColumnName( col ).equals( models.get( 0 ).getColumnName( col ) ) )
					throw new UnsupportedOperationException( "Column names do not match:\n"
							+ model.getColumnName( col ) + " vs " + models.get( 0 ).getColumnName( col ) );

		}
		models.add( model );

		model.addTableModelListener(this);
		updateRowCount();
		fireTableRowsInserted(firstRow, lastRow);
	}

	public void remove(TableModel model) {
		if (models.contains(model)) {
			int firstRow = getRowOffset(model);
			int lastRow = firstRow + model.getRowCount() - 1;

			model.removeTableModelListener(this);
			models.remove(model);
			updateRowCount();
			fireTableRowsDeleted(firstRow, lastRow);
		}
	}

	@Override
	public int getRowCount() {
		return rowCount;
	}

	@Override
	public int getColumnCount() {
		int columnCount = 0;
		if (models.size() > 0) {
			columnCount = models.get(0).getColumnCount();
		}
		return columnCount;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		TableModel model = getModelForRow(rowIndex);
		if (model == null) {
			TableModel test = getModelForRow(rowIndex);
		}
		int rowOffset = getRowOffset(model);
		rowIndex -= rowOffset;
		return model.getValueAt(rowIndex, columnIndex);
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		Class clazz = String.class;
		if (models.size() > 0) {
			clazz = models.get(0).getColumnClass(columnIndex);
		}
		return clazz;
	}

	@Override
	public String getColumnName(int column) {
		String name = null;
		if (models.size() > 0) {
			name = models.get(0).getColumnName(column);
		}
		return name;
	}

	@Override
	public void tableChanged( TableModelEvent e) {
		int type = e.getType();
		if (type == TableModelEvent.INSERT || type == TableModelEvent.DELETE || type == TableModelEvent.UPDATE) {
			int firstRow = e.getFirstRow();
			int lastRow = e.getLastRow();

			TableModel model = getModelForRow(firstRow);
			int rowOffset = getRowOffset(model);

			firstRow += rowOffset;
			lastRow += rowOffset;

			updateRowCount();

			TableModelEvent proxy = new TableModelEvent(this, firstRow, lastRow, e.getColumn(), type);
			fireTableChanged(e);
		} else {
			updateRowCount();

			TableModelEvent proxy = new TableModelEvent(this, e.getFirstRow(), e.getLastRow(), e.getColumn(), type);
			fireTableChanged(e);
		}
	}
}
