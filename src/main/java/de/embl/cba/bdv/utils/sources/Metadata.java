package de.embl.cba.bdv.utils.sources;

import bdv.util.BdvStackSource;
import ij3d.Content;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Metadata
{
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
	public ArrayList< Double > selectedSegmentIds =  new ArrayList<>(  );
	public String xmlLocation = null;

	public enum Modality
	{
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
