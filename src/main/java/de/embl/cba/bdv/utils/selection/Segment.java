package de.embl.cba.bdv.utils.selection;

import net.imglib2.FinalInterval;
import net.imglib2.RealInterval;

public interface Segment
{
	String getImageId();

	double getLabel();

	int getTimePoint();

	double[] getPosition();

	FinalInterval getBoundingBox();
}
