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
package de.embl.cba.tables.command;

import de.embl.cba.bdv.utils.lut.GlasbeyARGBLut;
import de.embl.cba.bdv.utils.sources.Metadata;
import de.embl.cba.bdv.utils.wrap.Wraps;
import de.embl.cba.tables.Calibrations;
import de.embl.cba.tables.Logger;
import de.embl.cba.tables.color.LazyCategoryColoringModel;
import de.embl.cba.tables.color.SelectionColoringModel;
import de.embl.cba.tables.image.DefaultImageSourcesModel;
import de.embl.cba.tables.imagesegment.ImageSegment;
import de.embl.cba.tables.imagesegment.LazyImageSegmentsModel;
import de.embl.cba.tables.select.DefaultSelectionModel;
import de.embl.cba.tables.select.SelectionModel;
import ij.ImagePlus;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;


@Deprecated
//@Plugin(type = Command.class, menuPath = "Plugins>Segmentation>Explore>Explore Label Image" )
public class ExploreLabelImageCommand < R extends RealType< R > > implements Command
{
	@Parameter ( label = "Label image" )
	public ImagePlus labelImage;

	@Parameter ( label = "Intensity image (optional)", required = false )
	public ImagePlus intensityImage;


	@Override
	public void run()
	{
		final DefaultImageSourcesModel imageSourcesModel = createImageSourcesModel();

		final LazyImageSegmentsModel lazyImageSegmentsModel
				= new LazyImageSegmentsModel( labelImage.getTitle() );

		final SelectionModel< ImageSegment > selectionModel =
				new DefaultSelectionModel< ImageSegment >();

		final LazyCategoryColoringModel< ImageSegment > coloringModel =
				new LazyCategoryColoringModel< ImageSegment >( new GlasbeyARGBLut() );

		final SelectionColoringModel< ImageSegment > selectionColoringModel
				= new SelectionColoringModel< ImageSegment >( coloringModel, selectionModel );

		// TODO: make this work....
		//
//		new SegmentsBdvView(
//				imageSourcesModel,
//				lazyImageSegmentsModel,
//				selectionModel,
//				selectionColoringModel
//		);

	}

	public boolean is2D()
	{
		boolean is2D = labelImage.getNSlices() == 1;
		if ( intensityImage != null )
		{
			if ( intensityImage.getNSlices() > labelImage.getNSlices() )
			{
				is2D = false;
			}
		}

		return is2D;
	}

	private DefaultImageSourcesModel createImageSourcesModel()
	{
		final DefaultImageSourcesModel imageSourcesModel =
				new DefaultImageSourcesModel( is2D() );

		final String labelImageId = labelImage.getTitle();

		Logger.info( "Adding to image sources: " + labelImageId );

		imageSourcesModel.addSourceAndMetadata(
				Wraps.imagePlusAsSource4DChannelList( labelImage ).get( 0 ),
				labelImageId,
				Metadata.Modality.Segmentation,
				getNumSpatialDimensions( labelImage.getNSlices() ),
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
					getNumSpatialDimensions( intensityImage.getNSlices() ),
					Calibrations.getScalingTransform( intensityImage ),
					null
			);

			imageSourcesModel.sources().get( labelImageId )
					.metadata().imageSetIDs.add( intensityImageId );
		}

		return imageSourcesModel;
	}

	private int getNumSpatialDimensions( int nSlices )
	{
		return nSlices > 1 ? 3 : 2;
	}


}
