package de.embl.cba.morphometry;

import ij.gui.Roi;
import net.imglib2.FinalInterval;

import java.awt.*;
import java.util.ArrayList;

public abstract class Rois
{
	public static ArrayList< FinalInterval > asIntervals( Roi[] rois )
	{
		final ArrayList< FinalInterval > intervals = new ArrayList<>();
		for ( Roi roi : rois )
		{
			final Rectangle bounds = roi.getBounds();

			intervals.add(
					new FinalInterval(
						new long[]{ bounds.x, bounds.y },
						new long[]{ bounds.x + bounds.width - 1, bounds.y + bounds.height - 1 }
				));
		}
		return intervals;
	}
}
