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
package de.embl.cba.tables.image;

import bdv.viewer.Source;
import de.embl.cba.bdv.utils.sources.Metadata;
import de.embl.cba.tables.image.ImageSourcesModel;
import de.embl.cba.tables.image.SourceAndMetadata;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.RealType;

import java.util.HashMap;
import java.util.Map;

public class DefaultImageSourcesModel implements ImageSourcesModel
{
	private final Map< String, SourceAndMetadata< ? > > nameToSourceAndMetadata;
	private boolean is2D;

	public DefaultImageSourcesModel( boolean is2D )
	{
		this.is2D = is2D;
		nameToSourceAndMetadata = new HashMap<>();
	}

	@Override
	public Map< String, SourceAndMetadata< ? > > sources()
	{
		return nameToSourceAndMetadata;
	}

	@Override
	public boolean is2D()
	{
		return is2D;
	}

	public < R extends RealType< R > > void addSourceAndMetadata(
			Source< R > source,
			String imageId,
			Metadata.Modality flavor,
			int numSpatialDimensions,
			String segmentsTablePath,
			double displayRangeMax
	)
	{
		final Metadata metadata = new Metadata( imageId );
		metadata.modality = flavor;
		metadata.numSpatialDimensions = numSpatialDimensions;
		metadata.segmentsTablePath = segmentsTablePath;
		metadata.contrastLimits = new double[]{ 0, displayRangeMax };

		nameToSourceAndMetadata.put( imageId, new SourceAndMetadata( source, metadata ) );
	}

	public < R extends RealType< R > > void addSourceAndMetadata(
			Source< R > source,
			String imageId,
			Metadata.Modality flavor,
			int numSpatialDimensions,
			AffineTransform3D transform,
			String segmentsTablePath
	)
	{

		final Metadata metadata = new Metadata( imageId );
		metadata.modality = flavor;
		metadata.numSpatialDimensions = numSpatialDimensions;
//		metadata.sourceTransform = transform;
		metadata.segmentsTablePath = segmentsTablePath;

		nameToSourceAndMetadata.put( imageId, new SourceAndMetadata( source, metadata ) );
	}

	public < R extends RealType< R > > void addSourceAndMetadata(
			String imageId,
			SourceAndMetadata< R > sourceAndMetadata )
	{
		nameToSourceAndMetadata.put( imageId, sourceAndMetadata );
	}

}
