/*-
 * #%L
 * Various Java code for ImageJ
 * %%
 * Copyright (C) 2018 - 2021 EMBL
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
import de.embl.cba.tables.tablerow.DefaultTableRowsModel;
import de.embl.cba.tables.TableColumns;
import de.embl.cba.tables.tablerow.ResultsTableFromTableRowsModelCreator;
import de.embl.cba.tables.morpholibj.ExploreMorphoLibJLabelImage;
import de.embl.cba.tables.tablerow.TableRowImageSegment;
import de.embl.cba.tables.view.combined.SegmentsTableBdvAnd3dViews;
import ij.IJ;
import ij.measure.ResultsTable;
import net.imagej.ImageJ;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TestTableRowsAndTableModel
{
	// TODO: write a proper test without any UI popping up
	public static void main( String[] args )
	{
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();

		IJ.open( TestTableRowsAndTableModel.class.getResource(
				"../test-data/3d-image-lbl-morpho.csv" ).getFile() );

		final ExploreMorphoLibJLabelImage explore = new ExploreMorphoLibJLabelImage(
				IJ.openImage( TestTableRowsAndTableModel.class.getResource(
						"../test-data/3d-image.zip" ).getFile() ),
				IJ.openImage( TestTableRowsAndTableModel.class.getResource(
						"../test-data/3d-image-lbl.zip" ).getFile() ),
				"3d-image-lbl-morpho" );

		final SegmentsTableBdvAnd3dViews views = explore.getTableBdvAnd3dViews();

		final String tableFile = TestTableRowsAndTableModel.class.getResource( "../test-data/3d-image-lbl-morpho-colorMap.csv" ).getFile();

		final ArrayList< String > labelColumn = TableColumns.getColumn(
				views.getTableRowsTableView().getTableRows(),
				ExploreMorphoLibJLabelImage.LABEL );
		final ArrayList< String > imageColumn = TableColumns.getColumn(
				views.getTableRowsTableView().getTableRows(),
				"ImageName");

		final HashMap< String, List< String > > referenceColumns = new HashMap<>();
		referenceColumns.put( "Label", labelColumn );
		referenceColumns.put( "ImageName", imageColumn );

		final Map< String, List< String > > newColumns = TableColumns.stringColumnsFromTableFile( tableFile );

		Map< String, List< String > > columns2 =
			TableColumns.createColumnsForMergingExcludingReferenceColumns(
					referenceColumns,
					newColumns );

		views.getTableRowsTableView().addColumns( columns2 );

		// Test conversion of
		final List< TableRowImageSegment > tableRows = views.getTableRowsTableView().getTableRows();

		final DefaultTableRowsModel< TableRowImageSegment > tableModel = new DefaultTableRowsModel<>( tableRows );

		final ResultsTable resultsTable = new ResultsTableFromTableRowsModelCreator( tableModel ).createResultsTable();

		resultsTable.show( "Results from TableRows" );

		final Set< String > columnNames = tableModel.getColumnNames();
		final List< String > column = tableModel.getColumn( columnNames.iterator().next() );
		for ( String s : column )
		{
			System.out.println( s );
		}
	}
}

