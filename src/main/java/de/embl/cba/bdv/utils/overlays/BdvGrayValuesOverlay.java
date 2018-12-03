package de.embl.cba.bdv.utils.overlays;

import bdv.util.BdvOverlay;
import ij.plugin.Colors;
import net.imglib2.realtransform.AffineTransform2D;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

public class BdvGrayValuesOverlay extends BdvOverlay
{
	Collection< Double > values;
	ArrayList< Color > colors;

	public BdvGrayValuesOverlay( )
	{
		super();
	}

	public void setValuesAndColors( Collection< Double > values, ArrayList< Color > colors )
	{
		this.values = values;
		this.colors = colors;
	}

	@Override
	protected void draw( final Graphics2D g )
	{
		g.setColor( Color.WHITE);

		setFont( g, 10 );

		int[] stringPosition = new int[]{ 10, 10 };

		String text = "";
		for ( double value : values )
		{
			text += value + "\n";
		}

		g.drawString( text, stringPosition[ 0 ], stringPosition[ 1 ] );
	}


	private FontMetrics setFont( Graphics2D g, int fontSize )
	{
		g.setFont( new Font("TimesRoman", Font.PLAIN, fontSize ) );
		return g.getFontMetrics( g.getFont() );
	}

}
