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
package de.embl.cba.bdv.utils.converters;

import de.embl.cba.bdv.utils.lut.ARGBLut;
import de.embl.cba.bdv.utils.lut.Luts;
import de.embl.cba.bdv.utils.lut.RandomARGBLut;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.real.AbstractRealType;
import net.imglib2.type.volatiles.AbstractVolatileRealType;
import net.imglib2.type.volatiles.VolatileARGBType;

import java.util.Set;

@Deprecated
public class ConfigurableVolatileRealVolatileARGBConverter
		< V extends AbstractVolatileRealType, R extends AbstractRealType< R > >
		implements Converter< V, VolatileARGBType >
{
	private byte[][] lut;
	private de.embl.cba.bdv.utils.lut.ARGBLut argbLut;
	private Set< Double > selectedValues;

	public ConfigurableVolatileRealVolatileARGBConverter( )
	{
		this.lut = Luts.GLASBEY;
		this.selectedValues = null;
		this.argbLut = new RandomARGBLut();
	}


	@Override
	public void convert( final V input, final VolatileARGBType output )
	{
		if ( input.isValid() )
		{
			final double x = input.getRealDouble();

			if ( selectedValues == null || selectedValues.contains( x ) )
			{
//				final int lutIndex = ARGBLUT.getARGBIndex( x );
//
//				final int color = ARGBType.rgba(
//						lut[ lutIndex ][ 0 ],
//						lut[ lutIndex ][ 1 ],
//						lut[ lutIndex ][ 2 ], 255 );
//
//				output.set( color );
			}
			else
			{
				output.set( 0 );
			}

			output.setValid( true );
		}
		else
		{
			output.setValid( false );
		}
	}

	/**
	 *
	 * @param selectedValues set null for showing all values
	 */
	public void onlyShowSelectedValues( Set< Double > selectedValues )
	{
		this.selectedValues = selectedValues;
	}

	/**
	 *
	 * @param lut
	 */
	public void setLUT( int[][] lut )
	{
		//this.lut = lut;
	}

	public void setLUTMapper( ARGBLut argbLut )
	{
		this.argbLut = argbLut;
	}

	public ARGBLut getLUTMapper()
	{
		return argbLut;
	}


}
