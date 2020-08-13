package de.embl.cba.morphometry;

import ij.IJ;
import mcib3d.image3d.ImageInt;

public class ImageScience
{
	public static boolean isAvailable() {
		try
		{
			ImageInt.class.getName();
			return true;
		}
		catch (final NoClassDefFoundError err)
		{
			IJ.showMessage( "Please install ImageScience! [ Help > Update > Manage Update Sites ]" );
		}
		return false;
	}
}
