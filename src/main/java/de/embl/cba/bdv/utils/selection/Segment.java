package de.embl.cba.bdv.utils.selection;

import net.imglib2.RealInterval;

public interface Segment
{
	double getLabel();

	int getTimePoint();

	double[] getPosition();

	RealInterval getBoundingBox();
}
