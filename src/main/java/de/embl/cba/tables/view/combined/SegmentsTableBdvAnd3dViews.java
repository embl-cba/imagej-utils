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
package de.embl.cba.tables.view.combined;

import bdv.util.BdvHandle;
import de.embl.cba.bdv.utils.lut.GlasbeyARGBLut;
import de.embl.cba.tables.color.LazyCategoryColoringModel;
import de.embl.cba.tables.color.SelectionColoringModel;
import de.embl.cba.tables.image.ImageSourcesModel;
import de.embl.cba.tables.plot.TableRowsScatterPlot;
import de.embl.cba.tables.select.DefaultSelectionModel;
import de.embl.cba.tables.select.SelectionModel;
import de.embl.cba.tables.tablerow.TableRowImageSegment;
import de.embl.cba.tables.view.Segments3dView;
import de.embl.cba.tables.view.SegmentsBdvView;
import de.embl.cba.tables.view.TableRowsTableView;
import ij3d.Image3DUniverse;

import java.util.ArrayList;
import java.util.List;

public class SegmentsTableBdvAnd3dViews
{
	private final List< TableRowImageSegment > tableRowImageSegments;
	private final ImageSourcesModel imageSourcesModel;
	private final String viewName;
	private SegmentsBdvView< TableRowImageSegment > segmentsBdvView;
	private TableRowsTableView< TableRowImageSegment > tableRowsTableView;
	private Segments3dView< TableRowImageSegment > segments3dView;
	private SelectionModel< TableRowImageSegment > selectionModel;

	public SegmentsTableBdvAnd3dViews(
			List< TableRowImageSegment > tableRowImageSegments,
			ImageSourcesModel imageSourcesModel,
			String viewName )
	{
		this( tableRowImageSegments, imageSourcesModel, viewName, null, null );
	}

	public SegmentsTableBdvAnd3dViews(
			List< TableRowImageSegment > tableRowImageSegments,
			ImageSourcesModel imageSourcesModel,
			String viewName,
			BdvHandle bdv,
			Image3DUniverse universe )
	{
		this.tableRowImageSegments = tableRowImageSegments;
		this.imageSourcesModel = imageSourcesModel;
		this.viewName = viewName;
		show( bdv, universe );
	}

	private void show( BdvHandle bdv, Image3DUniverse universe )
	{
		selectionModel = new DefaultSelectionModel<>();

		final LazyCategoryColoringModel< TableRowImageSegment > coloringModel
				= new LazyCategoryColoringModel<>( new GlasbeyARGBLut( 255 ) );

		final SelectionColoringModel< TableRowImageSegment > selectionColoringModel
				= new SelectionColoringModel<>(
					coloringModel, selectionModel );

		bdv = bdvView( bdv, selectionColoringModel );

		tableView( bdv, selectionColoringModel );

		threeDView( bdv, universe, selectionColoringModel );

		// scatterPlotView( bdv, selectionColoringModel );
	}

	private void scatterPlotView( BdvHandle bdv, SelectionColoringModel< TableRowImageSegment > selectionColoringModel )
	{
		new Thread( () -> {
			final ArrayList< String > columnNames = new ArrayList<>( tableRowImageSegments.get( 0 ).getColumnNames() );

			final TableRowsScatterPlot< TableRowImageSegment > scatterPlotView =
					new TableRowsScatterPlot(
							tableRowImageSegments,
							selectionColoringModel,
							new String[]{ columnNames.get( 0 ), columnNames.get( 1 )},
							new double[]{ 1.0, 1.0},
							1.0 );

			scatterPlotView.show( bdv.getViewerPanel() );
		}).start();
	}

	private void threeDView( BdvHandle bdv, Image3DUniverse universe, SelectionColoringModel< TableRowImageSegment > selectionColoringModel )
	{
		segments3dView = new Segments3dView<>(
				tableRowImageSegments,
				selectionModel,
				selectionColoringModel,
				imageSourcesModel,
				universe
		);

		segments3dView.setParentComponent( bdv.getViewerPanel() );
		bdv.getViewerPanel().addTimePointListener( segments3dView );
	}

	private void tableView( BdvHandle bdv, SelectionColoringModel< TableRowImageSegment > selectionColoringModel )
	{
		tableRowsTableView = new TableRowsTableView<>(
				tableRowImageSegments,
				selectionModel,
				selectionColoringModel,
				viewName );

		tableRowsTableView.showTableAndMenu( bdv.getViewerPanel() );
	}

	private BdvHandle bdvView( BdvHandle bdv, SelectionColoringModel< TableRowImageSegment > selectionColoringModel )
	{
		segmentsBdvView = new SegmentsBdvView<>(
				tableRowImageSegments,
				selectionModel,
				selectionColoringModel,
				imageSourcesModel,
				bdv );


		bdv = segmentsBdvView.getBdv();
		return bdv;
	}

	public SelectionModel< TableRowImageSegment > getSelectionModel()
	{
		return selectionModel;
	}

	public List< TableRowImageSegment > getTableRowImageSegments()
	{
		return tableRowImageSegments;
	}

	public SegmentsBdvView< TableRowImageSegment > getSegmentsBdvView()
	{
		return segmentsBdvView;
	}

	public TableRowsTableView< TableRowImageSegment > getTableRowsTableView()
	{
		return tableRowsTableView;
	}

	public Segments3dView< TableRowImageSegment > getSegments3dView()
	{
		return segments3dView;
	}

	/**
	 * TODO
	 * - I am not sure this is useful or necessary.
	 * - All the view implement listeners
	 */
	public void close()
	{
		segmentsBdvView.close();
		tableRowsTableView.close();
		segments3dView.close();

		segmentsBdvView = null;
		tableRowsTableView = null;
		segments3dView = null;

		System.gc();
	}
}
