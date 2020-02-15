package de.embl.cba.bdv.utils.sources;

import bdv.util.BdvStackSource;
import ij3d.Content;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Metadata
{
	// TODO: refactor this, e.g. separate in basic metadata and other metadata that extends the basic metadata
	public String displayName = "Image";
	public Color color = null;
	public String colorMap = null;
	public Type type = null;
	public Double displayRangeMin = null;
	public Double displayRangeMax = null;
	public String imageId = null;
	public List< String > imageSetIDs = new ArrayList<>();
	public Modality modality = Modality.FM;
	public int numSpatialDimensions = 3;
	public boolean showInitially = false;
	public BdvStackSource< ? > bdvStackSource = null;
	public Content content = null; // 3D
	public String segmentsTablePath = null;
	public List< String > additionalSegmentTableNames = new ArrayList<>(  );
	public String colorByColumn = null;
	public List< Double > selectedSegmentIds = new ArrayList<>(  );
	public boolean showSelectedSegmentsIn3d = false;
	public boolean showImageIn3d = false;
	public String xmlLocation = null;

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
		Mask,
	}

	public Metadata( String imageId )
	{
		this.imageId = imageId;
		imageSetIDs.add( this.imageId );
	}
}
