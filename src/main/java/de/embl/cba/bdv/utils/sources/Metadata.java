/*-
 * #%L
 * TODO
 * %%
 * Copyright (C) 2018 - 2020 EMBL
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

import bdv.util.BdvStackSource;
import de.embl.cba.tables.view.combined.SegmentsTableBdvAnd3dViews;
import ij3d.Content;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Metadata
{
	// TODO: refactor this, e.g. separate in basic metadata and other metadata that extends the basic metadata
	// TODO: also it really now became a SourceState as well
	public String displayName = "Image";
	public String color = null;
	public double[] contrastLimits;
	public String imageId = null;
	public List< String > imageSetIDs = new ArrayList<>();
	public Type type = Type.Image;
	public Modality modality = Modality.FM;
	public int numSpatialDimensions = 3;
	public boolean showInitially = false;
	public BdvStackSource< ? > bdvStackSource = null;
	public Content content = null; // 3D
	public String segmentsTablePath = null;
	public List< String > additionalSegmentTableNames = new ArrayList<>(  );
	public String colorByColumn = null;
	public double[] valueLimits;
	public double resolution3dView = 0; // 0 = auto adjust (see Segments3dView and UniverseUtils)
	public List< Double > selectedSegmentIds = new ArrayList<>(  );
	public boolean showSelectedSegmentsIn3d = false;
	public boolean showImageIn3d = false;
	public String xmlLocation = null;
	public SegmentsTableBdvAnd3dViews views = null;

	public enum Modality
	{
		@Deprecated
		Segmentation,
		FM,
		EM,
		XRay
	}

	public enum Type
	{
		Image,
		Segmentation,
		Mask
	}

	public Metadata( String imageId )
	{
		this.imageId = imageId;
		imageSetIDs.add( this.imageId );
	}

	public Metadata copy () {
		Metadata metadataCopy = new Metadata (this.imageId);
		metadataCopy.displayName = this.displayName;
		metadataCopy.color = this.color;
		metadataCopy.contrastLimits = this.contrastLimits != null ? this.contrastLimits.clone() : null;
		metadataCopy.imageSetIDs = this.imageSetIDs != null ? new ArrayList<>(this.imageSetIDs) : new ArrayList<>();
		metadataCopy.type = this.type;
		metadataCopy.modality = this.modality;
		metadataCopy.numSpatialDimensions = this.numSpatialDimensions;
		metadataCopy.showInitially = this.showInitially;
		metadataCopy.bdvStackSource = this.bdvStackSource;
		metadataCopy.content = this.content;
		metadataCopy.segmentsTablePath = this.segmentsTablePath;
		metadataCopy.additionalSegmentTableNames = this.additionalSegmentTableNames != null ?
				new ArrayList<>( this.additionalSegmentTableNames ) : new ArrayList<>();
		metadataCopy.colorByColumn = this.colorByColumn;
		metadataCopy.valueLimits = this.valueLimits != null ? this.valueLimits.clone() : null;
		metadataCopy.resolution3dView = this.resolution3dView;
		metadataCopy.selectedSegmentIds = this.selectedSegmentIds != null ?
				new ArrayList<>( this.selectedSegmentIds ) : new ArrayList<>();
		metadataCopy.showSelectedSegmentsIn3d = this.showSelectedSegmentsIn3d;
		metadataCopy.showImageIn3d = this.showImageIn3d;
		metadataCopy.xmlLocation = this.xmlLocation;
		metadataCopy.views = this.views;

		return metadataCopy;
	}
}
