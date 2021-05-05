package de.embl.cba.bdv.utils.lut;

import de.embl.cba.tables.color.ColoringLuts;

public class ColumnARGBLut implements ARGBLut
{
	@Override
	public int getARGB( double x )
	{
		return 0;
	}

	@Override
	public String getName()
	{
		return ColoringLuts.ARGB_COLUMN;
	}

	@Override
	public void setName( String name )
	{

	}
}
