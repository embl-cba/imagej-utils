package de.embl.cba.morphometry.regions;

import net.imglib2.roi.labeling.LabelRegion;

public class RegionAndSize implements Comparable< RegionAndSize >
{
	final private LabelRegion region;
	final private Long size;

	public RegionAndSize( LabelRegion region, Long size )
	{
		this.region = region;
		this.size = size;
	}

	public LabelRegion getRegion()
	{
		return region;
	}

	public Long getSize()
	{
		return size;
	}

	public int compareTo( RegionAndSize r )
	{
		return ( (int) ( r.size - this.size ) );
	}
}
