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

import net.imglib2.Volatile;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.volatiles.VolatileARGBType;

import java.util.HashSet;
import java.util.Set;

public class SelectableVolatileARGBConverter implements Converter< RealType, VolatileARGBType >
{
	public static final ARGBType COLOR_SELECTED = new ARGBType( ARGBType.rgba( 255, 255, 0, 255 ) );
	private Converter< RealType, VolatileARGBType > wrappedConverter;
	private Set< Double > selectedValues;
	private double brightnessNotSelected;
	private SelectionMode selectionMode;
	private ARGBType colorSelected;

	public static enum SelectionMode
	{
		DimNotSelected,
		OnlyShowSelected,
		ColorSelectedBrightYellow,
		ColorSelectedBrightYellowAndDimNotSelected;
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
		this.selectionMode = SelectionMode.DimNotSelected;
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
			switch ( selectionMode )
			{
				case DimNotSelected:
					brightnessNotSelected = 0.2;
					colorSelected = null;
					break;
				case OnlyShowSelected:
					brightnessNotSelected = 0.0;
					colorSelected = null;
					break;
				case ColorSelectedBrightYellow:
					brightnessNotSelected = 1.0;
					colorSelected = COLOR_SELECTED;
					break;
				case ColorSelectedBrightYellowAndDimNotSelected:
					brightnessNotSelected = 0.2;
					colorSelected = COLOR_SELECTED;
					break;
			}

			setOutputColor( input, output);
			output.setValid( true );

		}
	}

	public void setOutputColor( final RealType input,
								final VolatileARGBType output )
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
			else if ( colorSelected != null )
			{
				output.set( colorSelected.get() );
			}
		}
	}

	public synchronized void setSelections( Set< Double > selectedValues )
	{
		this.selectedValues = selectedValues;
	}

	public synchronized void addSelection( double value )
	{
		if ( selectedValues == null )
		{
			selectedValues = new HashSet<>( );
		}

		selectedValues.add( value );
	}

	public synchronized void removeSelection( double value )
	{
		selectedValues.remove( value );
	}

	public synchronized void clearSelections( )
	{
		this.selectedValues = null;
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

	public SelectionMode getSelectionMode()
	{
		return selectionMode;
	}

	public void setSelectionMode( SelectionMode selectionMode )
	{
		this.selectionMode = selectionMode;
	}


}
