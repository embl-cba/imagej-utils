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

import de.embl.cba.tables.TableColumns;
import de.embl.cba.tables.TableRows;
import de.embl.cba.tables.imagesegment.SegmentProperty;
import de.embl.cba.tables.imagesegment.SegmentUtils;
import de.embl.cba.tables.morpholibj.ExploreMorphoLibJLabelImage;
import de.embl.cba.tables.tablerow.DefaultTableRowsModel;
import de.embl.cba.tables.tablerow.ResultsTableFromTableRowsModelCreator;
import de.embl.cba.tables.tablerow.TableRowImageSegment;
import de.embl.cba.tables.view.combined.SegmentsTableBdvAnd3dViews;
import ij.IJ;
import ij.measure.ResultsTable;
import net.imagej.ImageJ;
import org.jruby.RubyProcess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BenchmarkTableRowsLoading
{
	public static void main( String[] args )
	{
		final String tableFile = "/Users/tischer/Downloads/aggl_embed.csv";

		long currentTimeMillis = System.currentTimeMillis();

		Map< String, List< String > > columns =
				TableColumns.stringColumnsFromTableFile( tableFile );

		TableColumns.addLabelImageIdColumn(
				columns,
				"image_id",
				"image" );

		long timeMillisLoading = System.currentTimeMillis() - currentTimeMillis;

		currentTimeMillis = System.currentTimeMillis();

		final HashMap< SegmentProperty, List< String > > segmentPropertyToColumn
				= new HashMap<>();

		segmentPropertyToColumn.put(
				SegmentProperty.ObjectLabel,
				columns.get( "label_id" ) );

		segmentPropertyToColumn.put(
				SegmentProperty.LabelImage,
				columns.get( "image_id" ) );

		final List< TableRowImageSegment > segments
				= SegmentUtils.tableRowImageSegmentsFromColumns(
				columns, segmentPropertyToColumn, false );

		long timeMillisParsing = System.currentTimeMillis() - currentTimeMillis;

	}
}

