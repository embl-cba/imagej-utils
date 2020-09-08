package de.embl.cba.morphometry;

import ij.IJ;
import mcib3d.image3d.ImageInt;

public class ImageSuite3D
{
	public static boolean isAvailable() {
		try
		{
			ImageInt.class.getName();
			return true;
		}
		catch (final NoClassDefFoundError err)
		{
			IJ.showMessage( "Please install the 3D Image Suite!\n[ Help > Update > Manage Update Sites ]" );
		}
		return false;
	}
}
