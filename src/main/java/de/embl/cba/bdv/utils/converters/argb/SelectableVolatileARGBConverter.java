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
package de.embl.cba.bdv.utils.converters.argb;

import net.imglib2.Volatile;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.volatiles.VolatileARGBType;

import java.util.Set;

public class SelectableVolatileARGBConverter implements Converter< RealType, VolatileARGBType >
{
	private Converter< RealType, VolatileARGBType > wrappedConverter;
	private Set< Double > selectedValues;
	private double brightnessNotSelected;
	private SelectionMode selectionMode;

	enum SelectionMode
	{
		Brightness,
		Color; // TODO: implement
	}


	public SelectableVolatileARGBConverter( )
	{
		this( new RandomARGBConverter() );
	}

	public SelectableVolatileARGBConverter( Converter< RealType, VolatileARGBType > realARGBConverter )
	{
		this.wrappedConverter = realARGBConverter;
		this.selectedValues = null;

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

		if ( input.getRealDouble() == 0 )
		{
			output.set( 0 );
			output.setValid( true );
			return;
		}
		else
		{
			if ( selectionMode.equals( SelectionMode.Brightness ) )
			{
				setColorWithAdaptedBrightness( input, output);
				output.setValid( true );
				return;
			}
		}
	}

	public void setColorWithAdaptedBrightness( final RealType input, final VolatileARGBType output )
	{
		if ( selectedValues == null )
		{
			wrappedConverter.convert( input, output );
		}
		else
		{
			wrappedConverter.convert( input, output );

			if ( ! selectedValues.contains( input.getRealDouble() ) )
			{
				output.get().mul( brightnessNotSelected);
			}
		}
	}

	public synchronized void setSelections( Set< Double > selectedValues )
	{
		this.selectedValues = selectedValues;
	}

	public synchronized void addSelection( double value )
	{
		selectedValues.add( value );
	}

	public synchronized void removeSelection( double value )
	{
		selectedValues.remove( value );
	}

	public Set< Double > getSelections()
	{
		return selectedValues;
	}

	public void setBrightnessNotSelectedValues( final double brightnessNotSelected )
	{
		this.brightnessNotSelected = brightnessNotSelected;
	}

	public void setWrappedConverter( Converter< RealType, VolatileARGBType > converter )
	{
		this.wrappedConverter = converter;
	}

	public Converter< RealType, VolatileARGBType > getWrappedConverter()
	{
		return wrappedConverter;
	}

}
