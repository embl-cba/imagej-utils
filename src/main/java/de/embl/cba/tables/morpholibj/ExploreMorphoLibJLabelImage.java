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
package de.embl.cba.tables.morpholibj;

import de.embl.cba.bdv.utils.sources.Metadata;
import de.embl.cba.bdv.utils.wrap.Wraps;
import de.embl.cba.tables.Logger;
import de.embl.cba.tables.TableColumns;
import de.embl.cba.tables.image.DefaultImageSourcesModel;
import de.embl.cba.tables.image.ImageSourcesModel;
import de.embl.cba.tables.imagesegment.SegmentProperty;
import de.embl.cba.tables.imagesegment.SegmentUtils;
import de.embl.cba.tables.tablerow.TableRowImageSegment;
import de.embl.cba.tables.view.combined.SegmentsTableAndBdvViews;
import de.embl.cba.tables.view.combined.SegmentsTableBdvAnd3dViews;
import ij.ImagePlus;
import ij.WindowManager;
import ij.text.TextWindow;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.embl.cba.tables.imagesegment.SegmentUtils.*;


public class ExploreMorphoLibJLabelImage
{
	public static final String LABEL = "Label";
	public static final String COLUMN_NAME_LABEL_IMAGE_ID = "LabelImage";
	public static final String CENTROID_X = "Centroid.X";
	public static final String CENTROID_Y = "Centroid.Y";
	public static final String CENTROID_Z = "Centroid.Z";
	public static final String MEAN_BREADTH = "MeanBreadth";

	private final ImagePlus intensityImage;
	private final ImagePlus labelImage;
	private final String resultsTableTitle;
	private final boolean enable3DView;

	private HashMap< String, ij.measure.ResultsTable > titleToResultsTable;
	private ij.measure.ResultsTable resultsTable;
	private Map< String, List< String > > columns;
	private int numSpatialDimensions;
	private String labelImageId;
	private SegmentsTableBdvAnd3dViews tableBdvAnd3dViews;
	private SegmentsTableAndBdvViews tableAndBdvViews;

	public ExploreMorphoLibJLabelImage(
			ImagePlus intensityImage,
			ImagePlus labelImage,
			String resultsTableTitle )
	{
		this( intensityImage, labelImage, resultsTableTitle, true );
	}

	public ExploreMorphoLibJLabelImage(
			ImagePlus intensityImage,
			ImagePlus labelImage,
			String resultsTableTitle,
			boolean enable3DView )
	{
		this.intensityImage = intensityImage;
		this.labelImage = labelImage;
		this.resultsTableTitle = resultsTableTitle;
		this.enable3DView = enable3DView;
		run();
	}

	private void run()
	{
		numSpatialDimensions = labelImage.getNSlices() > 1 ? 3 : 2;

		labelImageId = labelImage.getTitle();

		fetchResultsTables();

		if ( titleToResultsTable.size() == 1 )
			resultsTable = titleToResultsTable.values().iterator().next();
		else
			resultsTable = titleToResultsTable.get( resultsTableTitle );

		if ( resultsTable == null )
		{
			throwResultsTableNotFoundError();
			return;
		}

		final List< TableRowImageSegment > tableRowImageSegments
				= createMLJTableRowImageSegments( resultsTable );

		final ImageSourcesModel imageSourcesModel = createImageSourcesModel();

		if ( enable3DView && numSpatialDimensions == 3 )
		{
			tableBdvAnd3dViews = new SegmentsTableBdvAnd3dViews(
					tableRowImageSegments,
					imageSourcesModel,
					resultsTableTitle );

			tableBdvAnd3dViews.getSegments3dView().setSegmentFocusZoomLevel( 0.01 );
		}
		else
		{
			tableAndBdvViews = new SegmentsTableAndBdvViews(
					tableRowImageSegments,
					imageSourcesModel,
					resultsTableTitle );
		}
	}

	public SegmentsTableBdvAnd3dViews getTableBdvAnd3dViews()
	{
		return tableBdvAnd3dViews;
	}

	public SegmentsTableAndBdvViews getTableAndBdvViews()
	{
		return tableAndBdvViews;
	}

	private void fetchResultsTables()
	{
		titleToResultsTable = new HashMap<>();
		final Frame[] nonImageWindows = WindowManager.getNonImageWindows();
		for ( Frame nonImageWindow : nonImageWindows )
		{
			if ( nonImageWindow instanceof TextWindow )
			{
				final TextWindow textWindow = ( TextWindow ) nonImageWindow;

				final ij.measure.ResultsTable resultsTable = textWindow.getResultsTable();

				if ( resultsTable != null )
					titleToResultsTable.put( resultsTable.getTitle(), resultsTable );
			}
		}
	}

	private void throwResultsTableNotFoundError()
	{
		String error = "Results table not found: " + resultsTableTitle + "\n";
		error += "\n";
		error += "Please choose from:\n";
		for ( String title : titleToResultsTable.keySet() )
		{
			error += "- " + title + "\n";
		}
		Logger.error( error  );
	}

	private DefaultImageSourcesModel createImageSourcesModel()
	{
		final DefaultImageSourcesModel imageSourcesModel =
				new DefaultImageSourcesModel( numSpatialDimensions == 2 );

		Logger.info( "Adding to image sources: " + labelImageId );

		imageSourcesModel.addSourceAndMetadata(
				Wraps.imagePlusAsSource4DChannelList( labelImage ).get( 0 ),
				labelImageId,
				Metadata.Modality.Segmentation,
				numSpatialDimensions,
				null,
				255
		);

		imageSourcesModel.sources().get( labelImageId ).metadata().showInitially = true;

		if ( intensityImage != labelImage )
		{
			final String intensityImageId = intensityImage.getTitle();

			Logger.info( "Adding to image sources: " + intensityImageId );

			imageSourcesModel.addSourceAndMetadata(
					Wraps.imagePlusAsSource4DChannelList( intensityImage ).get( 0 ),
					intensityImageId,
					Metadata.Modality.FM,
					numSpatialDimensions,
					null,
					255
			);

			imageSourcesModel.sources().get( labelImageId )
					.metadata().imageSetIDs.add( intensityImageId );
		}

		return imageSourcesModel;
	}

	private List< TableRowImageSegment > createMLJTableRowImageSegments(
			ij.measure.ResultsTable resultsTable )
	{
		columns = TableColumns.columnsFromImageJ1ResultsTable( resultsTable );

		columns = TableColumns.addLabelImageIdColumn(
				columns,
				COLUMN_NAME_LABEL_IMAGE_ID,
				labelImageId );

		// TODO: replace this by proper bounding box
		if ( numSpatialDimensions == 3 )
		{
			columns = addBoundingBoxColumn( CENTROID_X, BB_MIN_X, true );
			columns = addBoundingBoxColumn( CENTROID_Y, BB_MIN_Y, true );
			columns = addBoundingBoxColumn( CENTROID_Z, BB_MIN_Z, true );
			columns = addBoundingBoxColumn( CENTROID_X, BB_MAX_X, false );
			columns = addBoundingBoxColumn( CENTROID_Y, BB_MAX_Y, false );
			columns = addBoundingBoxColumn( CENTROID_Z, BB_MAX_Z, false );
		}

		final Map< SegmentProperty, List< String > > segmentPropertyToColumn
				= createSegmentPropertyToColumnMap();

		final List< TableRowImageSegment > segments
				= SegmentUtils.tableRowImageSegmentsFromColumns(
						columns,
						segmentPropertyToColumn,
						true );

		return segments;
	}

	private Map< String, List< String > > addBoundingBoxColumn(
			String centroid,
			String bb,
			boolean min )
	{
		final int numRows = columns.values().iterator().next().size();

		final List< String > column = new ArrayList<>();
		for ( int row = 0; row < numRows; row++ )
		{
			final double centre = Double.parseDouble(
					columns.get( centroid ).get( row ) );

			final double meanBreadth = Double.parseDouble(
					columns.get( MEAN_BREADTH ).get( row ) );

			if ( min )
				column.add( "" + (long) ( centre - 0.5 * meanBreadth ) );
			else
				column.add( "" + (long) ( centre + 0.5 * meanBreadth ) );
		}

		columns.put( bb, column );

		return columns;

	}

	private Map< SegmentProperty, List< String > > createSegmentPropertyToColumnMap( )
	{
		final Map< SegmentProperty, List< String > > segmentPropertyToColumn
				= new HashMap<>();

		segmentPropertyToColumn.put(
				SegmentProperty.LabelImage,
				columns.get( COLUMN_NAME_LABEL_IMAGE_ID ));

		segmentPropertyToColumn.put(
				SegmentProperty.ObjectLabel,
				columns.get( LABEL ) );

		segmentPropertyToColumn.put(
				SegmentProperty.X,
				columns.get( CENTROID_X ) );

		segmentPropertyToColumn.put(
				SegmentProperty.Y,
				columns.get( CENTROID_Y ) );

		if ( numSpatialDimensions == 3 )
		{
			segmentPropertyToColumn.put(
					SegmentProperty.Z,
					columns.get( CENTROID_Z ) );

			SegmentUtils.putDefaultBoundingBoxMapping( segmentPropertyToColumn, columns );
		}

		return segmentPropertyToColumn;
	}


}
