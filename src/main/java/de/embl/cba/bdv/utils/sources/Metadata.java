package de.embl.cba.bdv.utils.sources;

import bdv.util.BdvStackSource;
import ij3d.Content;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Metadata
{
	public String displayName = "Image";
	public Color displayColor = Color.white;
	public String imageId;
	public List< String > imageSetIDs = new ArrayList<>();
	public Modality modality = Modality.FM;
	public int numSpatialDimensions = 3;
	public boolean showInitially = false;
	public Double displayRangeMin = 0.0; // TODO
	public Double displayRangeMax = 65535.0; // TODO
	public BdvStackSource< ? > bdvStackSource = null;
	public Content content = null; // 3D
	public String segmentsTablePath = null;

	public enum Modality
	{
		Segmentation,
		FM,
		EM,
		XRay
	}

	public Metadata( String imageId )
	{
		this.imageId = imageId;
		imageSetIDs.add( this.imageId );
	}
}
