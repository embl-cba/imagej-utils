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
package de.embl.cba.bdv.utils.sources;

import bdv.viewer.Interpolation;
import bdv.viewer.Source;
import de.embl.cba.bdv.utils.Logger;
import de.embl.cba.bdv.utils.wrap.Wraps;
import ij.IJ;
import ij.ImagePlus;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccessible;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class ImagePlusFileSource < R extends RealType< R > & NativeType< R > >
		implements Source< R >
{
	private final Metadata metadata;
	private final String imagePath;
	private ModifiableRandomAccessibleIntervalSource4D< R > raiSource4D;
	private ImagePlus imagePlus;

	public ImagePlusFileSource( Metadata metadata, String imagePath )
	{
		this.metadata = metadata;
		this.imagePath = imagePath;
	}

	@Override
	public boolean isPresent( int t )
	{
		return getWrappedSource().isPresent( t );
	}

	@Override
	public RandomAccessibleInterval< R > getSource( int t, int level )
	{
		return getWrappedSource().getSource( t, level );
	}

	public ModifiableRandomAccessibleIntervalSource4D< R > getWrappedSource()
	{
		if ( raiSource4D == null ) loadAndCreateSource();

		return raiSource4D;
	}

	private void loadAndCreateSource()
	{
		openImagePlus();

		metadata.numSpatialDimensions = imagePlus.getNSlices() > 1 ? 3 : 2;

		if( metadata.modality == Metadata.Modality.Segmentation || imagePlus.getBitDepth() == 8 )
		{
			metadata.contrastLimits = new double[]{0.0,500};
		}
		else if( imagePlus.getBitDepth() == 16 )
		{
			metadata.contrastLimits = new double[]{0.0,65535.0};
		}

		raiSource4D = ( ModifiableRandomAccessibleIntervalSource4D< R > )
				Wraps.imagePlusAsSource4DChannelList( imagePlus ).get( 0 );
	}

	private void openImagePlus()
	{
		imagePlus = IJ.openImage( imagePath );

		if ( imagePlus == null )
			Logger.error("Could not open image: " + imagePath );

		imagePlus.setTitle( metadata.displayName );
	}

	@Override
	public RealRandomAccessible< R > getInterpolatedSource( int t, int level, Interpolation method )
	{
		return getWrappedSource().getInterpolatedSource( t, level, method );
	}

	@Override
	public void getSourceTransform( int t, int level, AffineTransform3D transform )
	{
		getWrappedSource().getSourceTransform( t, level, transform  );
	}

	@Override
	public R getType()
	{
		return getWrappedSource().getType();
	}

	@Override
	public String getName()
	{
		return getWrappedSource().getName();
	}

	@Override
	public VoxelDimensions getVoxelDimensions()
	{
		return getWrappedSource().getVoxelDimensions();
	}

	@Override
	public int getNumMipmapLevels()
	{
		return getWrappedSource().getNumMipmapLevels();
	}
}
