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
package de.embl.cba.tables;

import de.embl.cba.bdv.utils.sources.Metadata;
import de.embl.cba.bdv.utils.wrap.Wraps;
import de.embl.cba.tables.image.DefaultImageSourcesModel;
import de.embl.cba.tables.imagesegment.SegmentProperty;
import de.embl.cba.tables.imagesegment.SegmentPropertyColumnsSelectionDialog;
import de.embl.cba.tables.imagesegment.SegmentUtils;
import de.embl.cba.tables.tablerow.TableRowImageSegment;
import de.embl.cba.tables.view.combined.SegmentsTableAndBdvViews;
import de.embl.cba.tables.view.combined.SegmentsTableBdvAnd3dViews;
import ij.IJ;
import ij.ImagePlus;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.embl.cba.tables.imagesegment.SegmentPropertyColumnsSelectionDialog.NO_COLUMN_SELECTED;

public class ExploreIntensityImageAndLabelImageAndTable
{
	private final ImagePlus intensityImage;
	private final ImagePlus labelImage;

	private Map< String, List< String > > columns;
	private int numSpatialDimensions;
	private String labelImageId;

	private boolean coordinatesInTableAreCalibrated;

	public ExploreIntensityImageAndLabelImageAndTable(
			String intensityImagePath,
			String labelImagePath,
			String tablePath,
			boolean timePointsInTableAreOneBased,
			boolean coordinatesInTableAreCalibrated )
	{
		this.coordinatesInTableAreCalibrated = coordinatesInTableAreCalibrated;

		Logger.info("Opening intensity image: " + intensityImagePath );
		intensityImage = IJ.openImage( intensityImagePath );

		Logger.info("Opening label image: " + labelImagePath );
		labelImage = IJ.openImage( labelImagePath );

		Logger.info("Opening table: " + tablePath );
		final List< TableRowImageSegment > tableRowImageSegments
				= createSegments( tablePath, timePointsInTableAreOneBased );

		numSpatialDimensions = labelImage.getNSlices() > 1 ? 3 : 2;
		labelImageId = labelImage.getTitle();

		final DefaultImageSourcesModel imageSourcesModel = createImageSourcesModel();

		if ( numSpatialDimensions == 2 )
		{
			final SegmentsTableAndBdvViews views = new SegmentsTableAndBdvViews(
					tableRowImageSegments,
					imageSourcesModel,
					labelImageId );
		}
		else
		{
			final SegmentsTableBdvAnd3dViews views = new SegmentsTableBdvAnd3dViews(
					tableRowImageSegments,
					imageSourcesModel,
					labelImageId );

			views.getSegments3dView().setSegmentFocusZoomLevel( 0.1 );
			final double pixelWidth = labelImage.getCalibration().pixelWidth;
			views.getSegments3dView().setVoxelSpacing( pixelWidth );
		}
	}

	private DefaultImageSourcesModel createImageSourcesModel()
	{
		final DefaultImageSourcesModel imageSourcesModel =
				new DefaultImageSourcesModel( numSpatialDimensions == 2 );

		Logger.info( "Adding to image sources: " + labelImageId );

		if ( !coordinatesInTableAreCalibrated )
		{
			Logger.info( "Since the coordinates in the table are not calibrated, the" +
					" images will be shown in pixel units as well." );
			Utils.removeCalibration( labelImage );
			Utils.removeCalibration( intensityImage );
		}

		imageSourcesModel.addSourceAndMetadata(
				Wraps.imagePlusAsSource4DChannelList( labelImage ).get( 0 ),
				labelImageId,
				Metadata.Modality.Segmentation,
				numSpatialDimensions,
				Calibrations.getScalingTransform( labelImage ),
				null
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
					Calibrations.getScalingTransform( intensityImage ),
					null
			);

			imageSourcesModel.sources().get( labelImageId )
					.metadata().imageSetIDs.add( intensityImageId );

			imageSourcesModel.sources().get( intensityImageId )
					.metadata().contrastLimits = new double[]{ 0, intensityImage.getProcessor().getMax() };

		}

		imageSourcesModel.sources().get( labelImageId )
				.metadata().contrastLimits = new double[]{ 0, 500 };

		return imageSourcesModel;
	}

	private List< TableRowImageSegment >
	createSegments( String tablePath, boolean isOneBasedTimePoint )
	{
		columns = TableColumns.stringColumnsFromTableFile( tablePath );

		final Map< SegmentProperty, List< String > > propertyToColumn
				= createPropertyToColumnMap( columns.keySet() );

		final List< TableRowImageSegment > segments
				= SegmentUtils.tableRowImageSegmentsFromColumns( columns, propertyToColumn, isOneBasedTimePoint );

		return segments;
	}

	private Map< SegmentProperty, List< String > > createPropertyToColumnMap(
			Set< String > columnNames )
	{
		final SegmentPropertyColumnsSelectionDialog selectionDialog
				= new SegmentPropertyColumnsSelectionDialog( columnNames );

		final Map< SegmentProperty, String > segmentPropertyToColumn =
				selectionDialog.fetchUserInput();

		final Map< SegmentProperty, List< String > > propertyToColumn
				= new LinkedHashMap<>();

		for( SegmentProperty property : segmentPropertyToColumn.keySet() )
		{
			if ( segmentPropertyToColumn.get( property ).equals( NO_COLUMN_SELECTED ) )
				continue;

			propertyToColumn.put(
					property,
					this.columns.get( segmentPropertyToColumn.get( property ) ) );
		}

		return propertyToColumn;
	}


}
