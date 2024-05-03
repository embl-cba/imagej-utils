/*-
 * #%L
 * Various Java code for ImageJ
 * %%
 * Copyright (C) 2018 - 2024 EMBL
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
package tests;

import de.embl.cba.tables.Tables;
import de.embl.cba.tables.color.SelectionColoringModel;
import de.embl.cba.tables.plot.TableRowsScatterPlot;
import de.embl.cba.tables.select.SelectionModels;
import de.embl.cba.tables.tablerow.ColumnBasedTableRow;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TestScatterPlot
{
	//@Test // TODO: why does this not run as a test but throws an assertion error?
	public void run()
	{
		List< ColumnBasedTableRow > tableRows = Tables.open( "src/test/resources/test-data/umap.csv" );

		SelectionColoringModel< ColumnBasedTableRow > selectionColoringModel = SelectionModels.getDefaultSelectionColoringModel();

		ArrayList< String > colNames = new ArrayList<>( tableRows.get( 0 ).getColumnNames() );

		TableRowsScatterPlot scatterPlot = new TableRowsScatterPlot(
				tableRows,
				selectionColoringModel,
				new String[]{ colNames.get( 1 ), colNames.get( 2 ) },
				new double[]{ 1.0, 1.0 },
				0.2 );

		scatterPlot.show( );
	}

	public static void main( String[] args )
	{
		new TestScatterPlot().run();
	}
}
