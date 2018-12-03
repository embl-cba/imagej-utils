package de.embl.cba.bdv.utils.overlays;

import bdv.util.BdvOverlay;
import ij.plugin.Colors;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.type.numeric.ARGBType;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

public class BdvGrayValuesOverlay extends BdvOverlay
{
	ArrayList< Double > values;
	ArrayList< ARGBType > colors;

	public BdvGrayValuesOverlay( )
	{
		super();
		values = new ArrayList<>(  );
		colors = new ArrayList<>(  );
	}

	public void setValuesAndColors( ArrayList< Double > values, ArrayList< ARGBType > colors )
	{
		this.values = values;
		this.colors = colors;
	}

	@Override
	protected void draw( final Graphics2D g )
	{

		int fontSize = 20;
		int[] stringPosition = new int[]{ 10, 20 };

		for ( int i = 0; i < values.size(); ++i )
		{
			final int colorIndex = colors.get( i ).get();
			g.setColor( new Color( ARGBType.red( colorIndex ), ARGBType.green( colorIndex ), ARGBType.blue( colorIndex ) )  );
			g.setFont( new Font("TimesRoman", Font.PLAIN, fontSize ) );
			g.drawString( "" + values.get( i ), stringPosition[ 0 ], stringPosition[ 1 ] + fontSize * i + 5);
		}

	}

}
