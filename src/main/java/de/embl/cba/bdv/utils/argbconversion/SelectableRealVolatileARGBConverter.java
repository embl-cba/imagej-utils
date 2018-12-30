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
package de.embl.cba.bdv.utils.argbconversion;

import de.embl.cba.bdv.utils.lut.ARGBLut;
import de.embl.cba.bdv.utils.lut.RandomARGBLut;
import net.imglib2.Volatile;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.volatiles.VolatileARGBType;

import java.util.Set;

public class SelectableRealVolatileARGBConverter implements Converter< RealType, VolatileARGBType >
{
	private ARGBLut argbLut;
	private Set< Double > selectedValues;
	private double brightnessNotSelected;
	private double brightnessSelected;
	private SelectionMode selectionMode;

	enum SelectionMode
	{
		Brightness,
		Color; // TODO: implement
	}


	public SelectableRealVolatileARGBConverter( )
	{
		this.argbLut = new RandomARGBLut();
		this.selectedValues = null;
		this.brightnessSelected = 1.0;
		this.brightnessNotSelected = 0.2;
		this.selectionMode = SelectionMode.Brightness;

	}

	public SelectableRealVolatileARGBConverter( ARGBLut argbLut )
	{
		this.argbLut = argbLut;
		this.selectedValues = null;
		this.brightnessSelected = 1.0;
		this.brightnessNotSelected = 0.2;
		this.selectionMode = SelectionMode.Brightness;
	}


	@Override
	public void convert( final RealType input, final VolatileARGBType output )
	{

		if ( input instanceof Volatile )
		{
			if ( ! ( ( Volatile ) input ).isValid() )
			{
				output.setValid( false );
				return;
			}
		}
		else
		{
			output.setValid( true );
		}

		final double x = input.getRealDouble();

		if ( x == 0 )
		{
			output.set( 0 );
			return;
		}

		if ( selectionMode.equals( SelectionMode.Brightness ) )
		{
			setColorWithAdaptedBrightness( x, output);
		}

	}

	public void setColorWithAdaptedBrightness( double x, VolatileARGBType output )
	{
		if ( selectedValues == null )
		{
			output.set( argbLut.getARGBIndex( x, brightnessSelected ) );
		}
		else
		{
			if ( ! selectedValues.contains( x ) )
			{
				output.set( argbLut.getARGBIndex( x, brightnessNotSelected ) );
			}
			else
			{
				output.set( argbLut.getARGBIndex( x, brightnessSelected ) );
			}
		}
	}

	/**
	 *
	 * @param selectedValues set null for showing all values
	 */
	public void highlightSelectedValues( Set< Double > selectedValues )
	{
		this.selectedValues = selectedValues;
	}

	public void setBrightnessOfNotHighlightedValues( final double brightnessNotSelected )
	{
		this.brightnessNotSelected = brightnessNotSelected;
	}

	public void setARGBLut( ARGBLut argbLut )
	{
		this.argbLut = argbLut;
	}

	public ARGBLut getARGBLut()
	{
		return argbLut;
	}

}
