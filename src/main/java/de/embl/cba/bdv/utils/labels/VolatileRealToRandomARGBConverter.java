/**
 * License: GPL
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package de.embl.cba.bdv.utils.labels;

import net.imglib2.converter.Converter;
import net.imglib2.type.volatiles.AbstractVolatileRealType;
import net.imglib2.type.volatiles.VolatileARGBType;

import java.util.Map;
import java.util.Set;

/**
 * Conversion logic adapted from BigCat Viewer.
 */
public class VolatileRealToRandomARGBConverter< V extends AbstractVolatileRealType > implements Converter< V, VolatileARGBType >
{
	private byte[][] lut;
	private long seed = 50;
	private Set< Double > selectedLabels;
	private final Map< Double, Double > map;


	public VolatileRealToRandomARGBConverter( )
	{
		this.lut = LUTs.GLASBEY_LUT;
		this.selectedLabels = null;
		this.map = null;
	}

	public VolatileRealToRandomARGBConverter( byte[][] lut )
	{
		this.lut = lut;
		this.selectedLabels = null;
		this.map = null;
	}

	public VolatileRealToRandomARGBConverter( byte[][] lut,
											  Set< Double > selectedLabels )
	{
		this.lut = lut;
		this.selectedLabels = selectedLabels;
		this.map = null;
	}

	public VolatileRealToRandomARGBConverter( byte[][] lut,
											  Map< Double, Double > map,
											  Set< Double > selectedLabels )
	{
		this.lut = lut;
		this.selectedLabels = selectedLabels;
		this.map = map;
	}

	@Override
	public void convert( final V input, final VolatileARGBType output )
	{
		if ( input.isValid() )
		{
			double x = input.getRealDouble();

			if ( map != null )
			{
				x = map.get( input.getRealDouble() );
			}

			if ( selectedLabels == null || selectedLabels.contains( x ) )
			{
				output.set( LUTs.getRandomColorFromLut( x, lut, seed ) );
				output.setValid( true );
			}
			else
			{
				output.set( 0 );
				output.setValid( true );
			}
		}
		else
		{
			output.setValid( false );
		}
	}

	public void incrementRandomColorGeneratorSeed()
	{
		seed++;
	}

	public void setSelectedLabels( Set< Double > selectedLabels )
	{
		this.selectedLabels = selectedLabels;
	}

}
