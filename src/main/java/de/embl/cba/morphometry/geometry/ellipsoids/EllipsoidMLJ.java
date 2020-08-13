package de.embl.cba.morphometry.geometry.ellipsoids;

public class EllipsoidMLJ
{
	public static final int PHI = 0, THETA = 1, PSI = 2;

	public double[] center = new double[ 3 ];
	public double[] radii = new double[ 3 ];
	public double[] eulerAnglesInDegrees = new double[ 3 ];

	@Override
	public String toString()
	{
		String s = "";
		s += "\n## MorpholibJ ellipsoid parameters:";
		s += "\ncenter_X [pixels]: " + center[0];
		s += "\ncenter_Y [pixels]: " + center[1];
		s += "\ncenter_Z [pixels]: " + center[2];
		s += "\nradii_0 [pixels]: " + radii[0];
		s += "\nradii_1 [pixels]: " + radii[1];
		s += "\nradii_2 [pixels]: " + radii[2];
		s += "\nphi [degrees]: " + eulerAnglesInDegrees[ PHI ];
		s += "\ntheta [degrees]: " + eulerAnglesInDegrees[ THETA ];
		s += "\npsi [degrees]: " + eulerAnglesInDegrees[ PSI ];

		return s;
	}
}
